package com.easyparcel.sm_bluetooth_printer;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.MethodChannel.Result;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.ParcelUuid;

import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Environment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import java.io.File;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.graphics.Matrix;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Color;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.ArrayList;
import java.lang.Thread;

import net.posprinter.posprinterface.IMyBinder;
import net.posprinter.posprinterface.ProcessData;
import net.posprinter.posprinterface.UiExecute;
import net.posprinter.service.PosprinterService;
import net.posprinter.utils.BitmapToByteData.BmpType;
import net.posprinter.utils.DataForSendToPrinterTSC;
import net.posprinter.utils.PosPrinterDev;
import net.posprinter.utils.ReadExcel;
import net.posprinter.utils.RoundQueue;
import android.content.ServiceConnection;
import android.content.ComponentName;
import android.os.IBinder;
import android.util.Log;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import android.content.BroadcastReceiver;
import android.content.Context;
import java.util.Arrays;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

/** SmBluetoothPrinterPlugin */
public class SmBluetoothPrinterPlugin implements FlutterPlugin, MethodCallHandler, ActivityAware {
  /// The MethodChannel that will the communication between Flutter and native
  /// Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine
  /// and unregister it
  /// when the Flutter Engine is detached from the Activity
  private Activity activity;
  private MethodChannel channel;
  private Context mContext;
  private OutputStream outputStream;
  private InputStream inStream;
  public static IMyBinder binder;
  public static boolean isConnect = false;
  private static BluetoothAdapter blueadapter;
  private static BluetoothSocket mmSocket = null;
  private static int pageNum;
  private static int totalPage;
  private ArrayList<Bitmap> bitmapsPdf;
  private ArrayList<HashMap<String, String>> bluetoothDeviceList = new ArrayList<HashMap<String, String>>();

  @Override
  public void onAttachedToActivity(ActivityPluginBinding activityPluginBinding) {
    // TODO: your plugin is now attached to an Activity
    activity = activityPluginBinding.getActivity();
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    // TODO: the Activity your plugin was attached to was
    // destroyed to change configuration.
    // This call will be followed by onReattachedToActivityForConfigChanges().
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding activityPluginBinding) {
    // TODO: your plugin is now attached to a new Activity
    // after a configuration change.
  }

  @Override
  public void onDetachedFromActivity() {
    // TODO: your plugin is no longer associated with an Activity.
    // Clean up references.
  }

  ServiceConnection conn = new ServiceConnection() {

    @Override
    public void onServiceDisconnected(ComponentName name) {
      // TODO Auto-generated method stub

    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      // TODO Auto-generated method stub
      // ????????????
      binder = (IMyBinder) service;
    }
  };

  private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
    int count = 0;

    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();
      Log.e("address", "action " + String.valueOf(action));
      // Discovery has found a device. Get the BluetoothDevice
      // object and its info from the Intent.
      HashMap<String, String> bluetoothData = new HashMap<String, String>();
      BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
      String deviceName = device.getName();
      String deviceHardwareAddress = String.valueOf(device.getAddress()).trim(); // MAC address
      BluetoothClass bluetoothClass = device.getBluetoothClass();
      String type = String.valueOf(bluetoothClass.getMajorDeviceClass());
      String deviceState = "";

      if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
        deviceState = "Connected";
      } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
        deviceState = "Disconnected";
      } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
        deviceState = "Disconnected";
      }

      Log.e("address", "bluetoothDeviceList " + String.valueOf(bluetoothDeviceList.size()));
      if (bluetoothDeviceList.size() > 0) {
        ArrayList<HashMap<String, String>> checkDevice = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> duplicateDevice;
        int dupliCount = 0;
        for (int i = 0; i < bluetoothDeviceList.size(); i++) {
          duplicateDevice = new HashMap<String, String>();
          HashMap<String, String> hashmap = bluetoothDeviceList.get(i);
          String address = hashmap.get("address");
          String state = hashmap.get("state");

          if (deviceHardwareAddress.equals(address)) {
            if (!state.equals(deviceState)) {
              hashmap.put("state", deviceState);
            }
            duplicateDevice.put("name", deviceName);
            duplicateDevice.put("address", deviceHardwareAddress);
            checkDevice.add(duplicateDevice);
            dupliCount++;
          }
        }

        if (checkDevice.size() == 0) {
          bluetoothData.put("name", deviceName);
          bluetoothData.put("address", deviceHardwareAddress);
          bluetoothData.put("type", type);
          bluetoothData.put("state", deviceState);
          bluetoothDeviceList.add(bluetoothData);
          count++;
        }
      } else {
        bluetoothData.put("name", deviceName);
        bluetoothData.put("address", deviceHardwareAddress);
        bluetoothData.put("type", type);
        bluetoothData.put("state", deviceState);
        bluetoothDeviceList.add(bluetoothData);
        count++;
      }
    }
  };

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    init(
        flutterPluginBinding.getApplicationContext(),
        flutterPluginBinding.getBinaryMessenger());
  }

  private void init(Context context, BinaryMessenger messenger) {
    this.mContext = context;
    channel = new MethodChannel(messenger, "sm_bluetooth_printer");
    channel.setMethodCallHandler(this);

  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("initPrinter")) {
      try {
        Intent intent = new Intent(mContext, PosprinterService.class);
        activity.bindService(intent, conn, mContext.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        mContext.registerReceiver(bluetoothReceiver, filter);
      } catch (Exception e) {
        Log.e("exception", e.getMessage());
      }

      result.success(true);
    } else if (call.method.equals("bluetoothDevice")) {
      blueadapter = BluetoothAdapter.getDefaultAdapter();

      if (!blueadapter.isEnabled()) {
        // ??????????????????
        result.error("Error", "Please make sure to on turn on Bluetooth and Location", null);
      } else {
        if (!blueadapter.isDiscovering()) {
          blueadapter.startDiscovery();
        }
        Log.e("start_bluetooth", bluetoothDeviceList.toString());
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
          @Override
          public void run() {
            // Do the task...
            if (blueadapter != null && blueadapter.isDiscovering()) {
              // blueadapter.cancelDiscovery();
            }
            result.success(bluetoothDeviceList);
          }
        };
        handler.postDelayed(runnable, 5000);

        // ArrayList<HashMap<String, String>> bluetooth = discoverBluetooth();
        // if(bluetooth.isEmpty())
        // {
        // result.error("Error","No available device", null);
        // }
        // else
        // {
        // result.success(bluetooth);
        // }
      }
    } else if (call.method.equals("bluetoothConnect")) {
      blueadapter = BluetoothAdapter.getDefaultAdapter();
      String deviceAddr = call.argument("deviceAddr");
      BluetoothDevice device = blueadapter.getRemoteDevice(deviceAddr);
      System.out.println(deviceAddr);
      Log.e("success", deviceAddr);
      // ParcelUuid[] uuids = device.getUuids();

      try {
        // blueadapter.cancelDiscovery();
        binder.connectBtPort(device.getAddress().toString(), new UiExecute() {

          @Override
          public void onsucess() {
            Log.e("success", "Success connect 1234");

            // TODO Auto-generated method stub
            // ??????????????????UI??????????????????
            isConnect = true;
            result.success("Success connected device");
            // executePrint();
            // Toast.makeText(getApplicationContext(),getString(R.string.con_success),
            // 0).show();
            // btn0.setText(getString(R.string.con_success));
            // ?????????????????????????????????????????????
            // ??????????????????????????????UiExecute????????????
            // ????????????????????????????????????????????????????????????????????????????????????
            // ????????????????????????????????????????????????????????????????????????????????????????????????
            // ????????????????????????????????????????????????onfailed
            // binder.acceptdatafromprinter(new UiExecute() {

            // @Override
            // public void onsucess() {
            // Log.e("success","Success read device");
            // result.success("Success read device");

            // }

            // @Override
            // public void onfailed() {
            // Log.e("error","Fail read device");
            // result.error("Error", "Fail read device", null);
            // isConnect=false;
            // // Toast.makeText(getApplicationContext(),
            // getString(R.string.con_has_discon), 0).show();
            // }
            // });
          }

          @Override
          public void onfailed() {
            Log.e("error", "Fail connect device");
            // TODO Auto-generated method stub
            // ??????????????????UI??????????????????
            isConnect = false;
            result.error("Error", "Fail connect device", null);
            // Toast.makeText(getApplicationContext(), getString(R.string.con_failed),
            // 0).show();
            // btn0.setText("????????????");
          }
        });
      } catch (Exception e) {
        // try{
        // mmSocket.close();
        // } catch (IOException exception){}
        Log.e("Error", e.getMessage());
        Log.e("Error", "Socket's create failed");
        result.error("Error", "Fail to connect device", null);
      }
    } else if (call.method.equals("bluetoothDisconnect")) {
      try {
        // mmSocket.close();
        binder.disconnectCurrentPort(new UiExecute() {
          @Override
          public void onsucess() {
            result.success("Success disconnect bluetooth");
            // TODO Auto-generated method stub

          }

          @Override
          public void onfailed() {
            result.error("Error", "Fail to disconnect bluetooth", null);
            // TODO Auto-generated method stub

          }
        });

      } catch (Exception e) {
        result.error("Error", "Fail to disconnect bluetooth", null);
      }
    } else if (call.method.equals("bluetoothDisconnect")) {
      try {
        // mmSocket.close();
        binder.disconnectCurrentPort(new UiExecute() {
          @Override
          public void onsucess() {
            result.success("Success disconnect bluetooth");
            // TODO Auto-generated method stub

          }

          @Override
          public void onfailed() {
            result.error("Error", "Fail to disconnect bluetooth", null);
            // TODO Auto-generated method stub

          }
        });

      } catch (Exception e) {
        result.error("Error", "Fail to disconnect bluetooth", null);
      }
    } else if (call.method.equals("testPrint")) {
      // String filePath = call.argument("filePath");
      BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

      if (bluetooth != null) {

        //////// close for previous using flutter blue function
        // if (bluetooth.isEnabled()) {

        // Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();

        // if (pairedDevices.size() > 0) {
        // // There are paired devices. Get the name and address of each paired device.
        // for (BluetoothDevice device : pairedDevices) {

        // String deviceName = device.getName();
        // String deviceHardwareAddress = device.getAddress(); // MAC address
        // String dev = deviceName + "-" + deviceHardwareAddress;
        // if (deviceName.startsWith("SMK")) {

        // bluetooth.cancelDiscovery();

        // ParcelUuid[] uuids = device.getUuids();
        // // System.out.println(deviceHardwareAddress);
        // if(deviceHardwareAddress != null || deviceHardwareAddress != "")
        // {
        // binder.connectBtPort(deviceHardwareAddress.toString(), new UiExecute() {

        // @Override
        // public void onsucess() {
        // Log.e("success","Success connect 1234");

        // // TODO Auto-generated method stub
        // //??????????????????UI??????????????????
        // isConnect=true;
        // executePrint();
        // // Toast.makeText(getApplicationContext(),getString(R.string.con_success),
        // 0).show();
        // // btn0.setText(getString(R.string.con_success));
        // //?????????????????????????????????????????????
        // //??????????????????????????????UiExecute????????????
        // //????????????????????????????????????????????????????????????????????????????????????
        // //????????????????????????????????????????????????????????????????????????????????????????????????
        // //????????????????????????????????????????????????onfailed
        // binder.acceptdatafromprinter(new UiExecute() {

        // @Override
        // public void onsucess() {
        // Log.e("success","Success connect 5678");
        // // TODO Auto-generated method stub

        // }

        // @Override
        // public void onfailed() {
        // Log.e("error","Fail connect 5678");
        // // TODO Auto-generated method stub
        // isConnect=false;
        // // Toast.makeText(getApplicationContext(),
        // getString(R.string.con_has_discon), 0).show();
        // }
        // });
        // }

        // @Override
        // public void onfailed() {
        // Log.e("error","Fail connect 1234");
        // // TODO Auto-generated method stub
        // //??????????????????UI??????????????????
        // isConnect=false;
        // // Toast.makeText(getApplicationContext(), getString(R.string.con_failed),
        // 0).show();
        // //btn0.setText("????????????");
        // }
        // });

        // }

        // }
        // }
        // }

        // }
        if (bluetooth.isEnabled()) {

          executePrint();
        }

      }

    } else if (call.method.equals("printAWB")) {
      // String filePath = call.argument("filePath");
      BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
      if (bluetooth != null) {
        if (bluetooth.isEnabled()) {
          printFile(result);
        }
      }

    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  public void write(String s) throws IOException {
    outputStream.write(s.getBytes());
  }

  public void executePrint() {
    Log.e("printer", "Execute print");
    binder.writeDataByYouself(new UiExecute() {

      @Override
      public void onsucess() {
        Log.e("success", "Execute print 123");
      }

      @Override
      public void onfailed() {
        Log.e("fail", "Fail execute print 123");
      }
    }, new ProcessData() {// ??????????????????ProcessData???????????????
                          // ?????????????????????processDataBeforeSend?????????????????????????????????
      @Override
      public List<byte[]> processDataBeforeSend() {
        // TODO Auto-generated method stub
        // ???????????????list
        ArrayList<byte[]> list = new ArrayList<byte[]>();
        // ????????????????????????????????????????????????????????????????????????gbk??????????????????????????????????????????????????????????????????????????????
        DataForSendToPrinterTSC.setCharsetName("gbk");// ?????????????????????gbk
        // ????????????????????????????????????byte[]??????,???????????????
        // ???????????????size????????????,???60mm,???30mm,??????????????????dot???inch????????????????????????????????????????????????
        byte[] data0 = DataForSendToPrinterTSC
            .sizeBymm(80, 40);
        list.add(data0);
        // ??????Gap,??????
        list.add(DataForSendToPrinterTSC.gapBymm(2, 0));

        // ????????????
        list.add(DataForSendToPrinterTSC.cls());
        // ????????????????????????int x???x????????????????????????int y???y????????????????????????
        // string font??????????????????int rotation??????????????????
        // int x_multiplication?????????x??????????????????
        // int y_multiplication,y??????????????????
        // string content???????????????
        byte[] data1 = DataForSendToPrinterTSC
            .text(10, 10, "4", 0, 1, 1,
                "123abc");
        list.add(data1);
        //// ????????????,int x;int y;int width,???????????????int height,????????????
        list.add(DataForSendToPrinterTSC.bar(20,
            40, 200, 3));
        //// ????????????
        list.add(DataForSendToPrinterTSC.barCode(
            60, 50, "128", 100, 1, 0, 2, 2,
            "abcdef12345"));
        // ???????????????
        list.add(DataForSendToPrinterTSC.qrCode(20, 180, "L", 4, "A", 0, "M2", "S7", "abcdef"));
        // ??????
        list.add(DataForSendToPrinterTSC.print(1));
        return list;
      }
    });
  }

  public void printFile(Result result) {
    String fileName = "print_awb.pdf";
    String filePath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS + "/"
        + fileName;
    Log.e("TEST BITMAP", "filePath " + filePath);
    Uri pdfUri = Uri.fromFile(new File(filePath));
    String getPdfPath = PictureHelper.getPath(mContext, pdfUri);
    String returnMsg;
    // Log.e("PdfPath", getPdfPath);

    File pdfFile = new File(getPdfPath);
    bitmapsPdf = pdfToBitmap(pdfFile);

    if (bitmapsPdf == null) {
      Log.e("TEST BITMAP", "NULL  NULL  NULL  NULL");
      // return "Fail to convert to bitmap";
    }
    totalPage = bitmapsPdf.size() - 1;
    pageNum = 0;
    callPrinter(result);
  }

  private void callPrinter(Result result) {
    binder.writeDataByYouself(new UiExecute() {

      @Override
      public void onsucess() {
        Log.e("success", "Success to print");
        if (pageNum < totalPage) {
          pageNum++;
          callPrinter(result);
        } else {
          result.success("Sending data to printer");
        }
        // return 200;
        // TODO Auto-generated method stub
      }

      @Override
      public void onfailed() {
        Log.e("fail", "Fail to print");
        result.error("Error", "Failed to print", null);
        // return 400;
        // TODO Auto-generated method stub
      }
    }, new ProcessData() {// ??????????????????????????????

      @Override
      public List<byte[]> processDataBeforeSend() {
        long startTime = System.nanoTime();
        Log.e("StartTimeCallPrinter", String.valueOf(startTime));
        // TODO Auto-generated method stub
        int paperHeight = 148;
        int paperWidth = 105;
        ArrayList<byte[]> list = new ArrayList<byte[]>();

        byte[] data0 = DataForSendToPrinterTSC.sizeBymm(paperWidth, paperHeight);
        list.add(data0);
        // ??????Gap,??????
        list.add(DataForSendToPrinterTSC.gapBymm(2, 0));
        // ????????????
        list.add(DataForSendToPrinterTSC.cls());

        list.add(
            DataForSendToPrinterTSC.bitmap(0, 0, 0, bitmapsPdf.get(pageNum), BmpType.Dithering, (int) (paperWidth * 8),
                (int) (paperHeight * 8)));
        // Log.e("TEST BITMAP", bitmapsPdf.get(i).getHeight() + ":::::" +
        // bitmapsPdf.get(i).getWidth());

        list.add(DataForSendToPrinterTSC.print(1));

        // long EndTime = System.nanoTime();
        // Log.e("EndTimeCallPrinter", String.valueOf(EndTime));

        // createFileWithByte(list);
        return list;
      }

    });
  }

  private ArrayList<Bitmap> pdfToBitmap(File pdfFile) {
    ArrayList<Bitmap> bitmaps = new ArrayList<>();
    String fileName = "test.png";
    String filePath = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS + "/"
        + fileName;
    File file = new File(filePath);

    long startTime = System.nanoTime();
    Log.e("StartTimeConvertImage", String.valueOf(startTime));
    try {
      PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));

      Bitmap bitmap;
      final int pageCount = renderer.getPageCount();
      for (int i = 0; i < pageCount; i++) {
        PdfRenderer.Page page = renderer.openPage(i);

        // int width = getResources().getDisplayMetrics().densityDpi / 72 *
        // page.getWidth();
        // int height = getResources().getDisplayMetrics().densityDpi / 72 *
        // page.getHeight();

        int width = 600;
        int height = 1000;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // Paint bitmap before rendering
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
        // Log.e("Before Image in Memory size", String.valueOf(bitmap.getByteCount()) );
        // bitmap = toGrayscale(bitmap);

        // Bitmap resizeSmallerBitmap = Bitmap.createScaledBitmap(bitmap, 690, 1034,
        // false);
        // Bitmap resizeBitmap = Bitmap.createScaledBitmap(resizeSmallerBitmap, width,
        // height, false);

        // ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        // byte[] byteArray = baos.toByteArray();
        // Bitmap updatedBitmap = BitmapFactory.decodeByteArray(byteArray, 0,
        // byteArray.length);
        // Bitmap decoded = BitmapFactory.decodeStream(new
        // ByteArrayInputStream(baos.toByteArray()));

        // Log.e("After Image in Memory size",
        // String.valueOf(resizeBitmap.getByteCount()) );

        bitmaps.add(bitmap);
        // close the page
        page.close();

      }

      // close the renderer
      renderer.close();

      // saving image into sdcard.
      // check if file already exists, then delete it.
      // if (file.exists()) file.delete();

      // // Saving image in PNG format with 100% quality.
      // try {
      // FileOutputStream out = new FileOutputStream(file);
      // bitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
      // Log.v("Saved Image - ", file.getAbsolutePath());
      // out.flush();
      // out.close();
      // } catch (Exception e) {
      // Log.e("Fail","Fail to save to pdf image");
      // }
      // bitmap.recycle();
      // resizeSmallerBitmap.recycle();
      // resizeBitmap.recycle();

    } catch (Exception ex) {
      Log.e("Fail", "Fail to convert to bitmap", ex);
    }

    long endTime = System.nanoTime();
    Log.e("EndTimeConvertImage", String.valueOf(endTime));
    return bitmaps;
  }

  private static int calculateInSampleSize(
      BitmapFactory.Options options, int reqWidth, int reqHeight) {
    // Raw height and width of image
    final int height = options.outHeight;
    final int width = options.outWidth;
    int inSampleSize = 1;

    if (height > reqHeight || width > reqWidth) {

      final int halfHeight = height / 2;
      final int halfWidth = width / 2;

      // Calculate the largest inSampleSize value that is a power of 2 and keeps both
      // height and width larger than the requested height and width.
      while ((halfHeight / inSampleSize) >= reqHeight
          && (halfWidth / inSampleSize) >= reqWidth) {
        inSampleSize *= 2;
      }
    }

    return inSampleSize;
  }

  private Bitmap toGrayscale(Bitmap bmpOriginal) {
    int width, height;
    height = bmpOriginal.getHeight();
    width = bmpOriginal.getWidth();

    Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, bmpOriginal.getConfig());
    Canvas c = new Canvas(bmpGrayscale);
    Paint paint = new Paint();
    // ColorMatrix cm = new ColorMatrix();
    // cm.setSaturation(0);
    // ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
    // paint.setColorFilter(f);
    c.drawColor(Color.WHITE);
    c.drawBitmap(bmpOriginal, 0, 0, null);
    // bmpOriginal.recycle();d
    return bmpGrayscale;
  }

  private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
    int width = bm.getWidth();
    int height = bm.getHeight();
    float scaleWidth = ((float) newWidth) / width;
    float scaleHeight = ((float) newHeight) / height;
    // CREATE A MATRIX FOR THE MANIPULATION
    Matrix matrix = new Matrix();
    // RESIZE THE BIT MAP
    matrix.postScale(scaleWidth, scaleHeight);

    // "RECREATE" THE NEW BITMAP
    Bitmap resizedBitmap = Bitmap.createBitmap(
        bm, 0, 0, width, height, matrix, false);
    // bm.recycle();
    return resizedBitmap;
  }

  public ArrayList<HashMap<String, String>> discoverBluetooth() {
    if (!blueadapter.isDiscovering()) {
      blueadapter.startDiscovery();
    }

    Set<BluetoothDevice> device = blueadapter.getBondedDevices();
    ArrayList<HashMap<String, String>> bluetoothDeviceList = new ArrayList<HashMap<String, String>>();
    int count = 0;

    if (device.size() > 0) {
      for (Iterator<BluetoothDevice> it = device.iterator(); it.hasNext();) {
        HashMap<String, String> bluetoothData = new HashMap<String, String>();
        BluetoothDevice btd = it.next();
        BluetoothClass bluetoothClass = btd.getBluetoothClass();

        bluetoothData.put("name", btd.getName());
        bluetoothData.put("address", btd.getAddress());
        bluetoothData.put("type", String.valueOf(bluetoothClass.getMajorDeviceClass()));
        if (btd.getBondState() == BluetoothDevice.BOND_NONE) {
          bluetoothData.put("state", "Connected");
        } else if (btd.getBondState() == BluetoothDevice.BOND_BONDED) {
          bluetoothData.put("state", "Unconnected");
        }
        bluetoothDeviceList.add(bluetoothData);
        count++;
      }
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        // Do the task...
        if (blueadapter != null && blueadapter.isDiscovering()) {
          blueadapter.cancelDiscovery();
        }
      }
    };
    handler.postDelayed(runnable, 5000);

    return bluetoothDeviceList;
  }

}
