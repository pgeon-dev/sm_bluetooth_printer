package com.easyparcel.sm_bluetooth_printer_example;

import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

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

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "bluetooth";
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

    ServiceConnection conn=new ServiceConnection() {
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			// TODO Auto-generated method stub
			//绑定成功
			binder=(IMyBinder) service;
		}
	};

  @Override
	protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    //绑定service，获取ImyBinder对象
    // Intent intent=new Intent(this,PosprinterService.class);
    // bindService(intent, conn, BIND_AUTO_CREATE);
    // IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
    // filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
    // filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
    // filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
    // registerReceiver(receiver, filter);
  }

 @Override
 public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
 super.configureFlutterEngine(flutterEngine);
    new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
        .setMethodCallHandler(
          (call, result) -> {
            // Note: this method is invoked on the main thread.
            if (call.method.equals("getBatteryLevel")) {
              int batteryLevel = getBatteryLevel();

              if (batteryLevel != -1) {
                result.success(batteryLevel);
              } else {
                result.error("UNAVAILABLE", "Battery level not available.", null);
              }
            }
            else if(call.method.equals("bluetoothDevice"))
            {
                blueadapter=BluetoothAdapter.getDefaultAdapter();
               
                if(!blueadapter.isEnabled()){  
                  //请求用户开启  
                  result.error("Error","Please make sure to on turn on Bluetooth and Location", null);
                } else {
                  if (!blueadapter.isDiscovering()) {
                    blueadapter.startDiscovery();
                  }
                  Log.e("start_bluetooth",bluetoothDeviceList.toString());
                  Handler handler = new Handler();
                  Runnable runnable = new Runnable() {
                      @Override
                      public void run() {
                          // Do the task...
                        if(blueadapter!=null&&blueadapter.isDiscovering()){  
                            blueadapter.cancelDiscovery();  
						}  
                          result.success(bluetoothDeviceList);
                      }
                  };
                  handler.postDelayed(runnable, 5000);

                 
                  // ArrayList<HashMap<String, String>> bluetooth = discoverBluetooth();
                  // if(bluetooth.isEmpty())
                  // {
                  //   result.error("Error","No available device", null);
                  // }
                  // else
                  // {
                  //   result.success(bluetooth);
                  // }
                } 
            }
            else if(call.method.equals("bluetoothConnect")){
                blueadapter=BluetoothAdapter.getDefaultAdapter();
                String deviceAddr = call.argument("deviceAddr");
                BluetoothDevice device = blueadapter.getRemoteDevice(deviceAddr);
                // System.out.println(deviceAddr);
                ParcelUuid[] uuids = device.getUuids();
               
                try {
                    blueadapter.cancelDiscovery();  
                    binder.connectBtPort(device.getAddress().toString(), new UiExecute() {
                              
						@Override
						public void onsucess() {
							Log.e("success","Success connect 1234");
							
							// TODO Auto-generated method stub
							//连接成功后在UI线程中的执行
							isConnect=true;
							result.success("Success connected device");
							// executePrint();
							// Toast.makeText(getApplicationContext(),getString(R.string.con_success), 0).show();
							// btn0.setText(getString(R.string.con_success));
							//此处也可以开启读取打印机的数据
							//参数同样是一个实现的UiExecute接口对象
							//如果读的过程重出现异常，可以判断连接也发生异常，已经断开
							//这个读取的方法中，会一直在一条子线程中执行读取打印机发生的数据，
							//直到连接断开或异常才结束，并执行onfailed
							// binder.acceptdatafromprinter(new UiExecute() {
								
							//     @Override
							//     public void onsucess() {
							//          Log.e("success","Success read device");
							//          result.success("Success read device");
									
							//     }
								
							//     @Override
							//     public void onfailed() {
							//         Log.e("error","Fail read device");
							//         result.error("Error", "Fail read device", null);
							//         isConnect=false;
							//         // Toast.makeText(getApplicationContext(), getString(R.string.con_has_discon), 0).show();
							//     }
							// });
						}
						
						@Override
						public void onfailed() {
							Log.e("error","Fail connect device");
							// TODO Auto-generated method stub
							//连接失败后在UI线程中的执行
							isConnect=false;
								result.error("Error", "Fail connect device", null);
							// Toast.makeText(getApplicationContext(), getString(R.string.con_failed), 0).show();
							//btn0.setText("连接失败");
						}
					});
                } catch (Exception e) {
                    // try{
                    //     mmSocket.close();
                    // } catch (IOException exception){}
					Log.e("Error", e.getMessage());
                    Log.e("Error", "Socket's create failed");
                    result.error("Error", "Fail to connect device", null);
                }
            }
            else if(call.method.equals("bluetoothDisconnect")){
              try{
                // mmSocket.close();
                binder.disconnectCurrentPort(new UiExecute() {
                  @Override
                  public void onsucess() {
                    result.success("Success disconnect bluetooth");
                    // TODO Auto-generated method stub
                    
                  }
                  
                  @Override
                  public void onfailed() {
                    result.error("Error","Fail to disconnect bluetooth", null);
                    // TODO Auto-generated method stub
                    
                  }
                });
                
              } catch (Exception e) {
                result.error("Error","Fail to disconnect bluetooth", null);
              }
            }
            else if(call.method.equals("testPrint")){
				// String filePath = call.argument("filePath");
                BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();

                if (bluetooth != null) {
                
                ////////close for previous using flutter blue function 
                // if (bluetooth.isEnabled()) {

                //     Set<BluetoothDevice> pairedDevices = bluetooth.getBondedDevices();

                //     if (pairedDevices.size() > 0) {
                //     // There are paired devices. Get the name and address of each paired device.
                //     for (BluetoothDevice device : pairedDevices) {

                //         String deviceName = device.getName();
                //         String deviceHardwareAddress = device.getAddress(); // MAC address
                //         String dev = deviceName + "-" + deviceHardwareAddress;
                //         if (deviceName.startsWith("SMK")) {
                           
                //         bluetooth.cancelDiscovery();
                       
                //         ParcelUuid[] uuids = device.getUuids();
                //         // System.out.println(deviceHardwareAddress);
                //         if(deviceHardwareAddress != null || deviceHardwareAddress != "")
                //         {
                //             binder.connectBtPort(deviceHardwareAddress.toString(), new UiExecute() {
                              
                //                 @Override
                //                 public void onsucess() {
                //                     Log.e("success","Success connect 1234");
                                    
                //                     // TODO Auto-generated method stub
                //                     //连接成功后在UI线程中的执行
                //                     isConnect=true;
                //                     executePrint();
                //                     // Toast.makeText(getApplicationContext(),getString(R.string.con_success), 0).show();
                //                     // btn0.setText(getString(R.string.con_success));
                //                     //此处也可以开启读取打印机的数据
                //                     //参数同样是一个实现的UiExecute接口对象
                //                     //如果读的过程重出现异常，可以判断连接也发生异常，已经断开
                //                     //这个读取的方法中，会一直在一条子线程中执行读取打印机发生的数据，
                //                     //直到连接断开或异常才结束，并执行onfailed
                //                     binder.acceptdatafromprinter(new UiExecute() {
                                        
                //                         @Override
                //                         public void onsucess() {
                //                              Log.e("success","Success connect 5678");
                //                             // TODO Auto-generated method stub
                                            
                //                         }
                                        
                //                         @Override
                //                         public void onfailed() {
                //                             Log.e("error","Fail connect 5678");
                //                             // TODO Auto-generated method stub
                //                             isConnect=false;
                //                             // Toast.makeText(getApplicationContext(), getString(R.string.con_has_discon), 0).show();
                //                         }
                //                     });
                //                 }
                                
                //                 @Override
                //                 public void onfailed() {
                //                   Log.e("error","Fail connect 1234");
                //                     // TODO Auto-generated method stub
                //                     //连接失败后在UI线程中的执行
                //                     isConnect=false;
                //                     // Toast.makeText(getApplicationContext(), getString(R.string.con_failed), 0).show();
                //                     //btn0.setText("连接失败");
                //                 }
                //             });

                //         }
                     

                //         }
                //     }
                // }

                // }
					if (bluetooth.isEnabled()) {

						executePrint();
					}

                }

            }
			else if(call.method.equals("printAWB")){
				// String filePath = call.argument("filePath");
                BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
                if (bluetooth != null) {
					if (bluetooth.isEnabled()) {
						printFile();
					}
				}

			}
			else if(call.method.equals("testConvertToImg")){
				printFile();
			}
            else {
              result.notImplemented();
            }
          }
        );
 	}

	private int getBatteryLevel() {
		int batteryLevel = -1;
		if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
		BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
		batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
		} else {
		Intent intent = new ContextWrapper(getApplicationContext()).
			registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
		batteryLevel = (intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100) /
			intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
		}

		return batteryLevel;
	}

  public void write(String s) throws IOException {
    outputStream.write(s.getBytes());
  }

  public void executePrint(){
    	Log.e("printer","Execute print");
    	binder.writeDataByYouself(new UiExecute() {

			@Override
			public void onsucess() {
               	Log.e("success","Execute print 123");
			}

			@Override
			public void onfailed() {
              	Log.e("fail","Fail execute print 123");
			}
        }, new ProcessData() {//第二个参数是ProcessData接口的实现
								//这个接口的重写processDataBeforeSend这个处理你要发送的指令
			@Override
			public List<byte[]> processDataBeforeSend() {
				// TODO Auto-generated method stub
				//初始化一个list
				ArrayList<byte[]> list = new ArrayList<byte[]>();
				//在打印请可以先设置打印内容的字符编码类型，默认为gbk，请选择打印机可识别的类型，参看编程手册，打印代码页
				DataForSendToPrinterTSC.setCharsetName("gbk");//不设置，默认为gbk
				//通过工具类得到一个指令的byte[]数据,以文本为例
				//首先得设置size标签尺寸,宽60mm,高30mm,也可以调用以dot或inch为单位的方法具体换算参考编程手册
				byte[] data0 = DataForSendToPrinterTSC
						.sizeBymm(80, 40);
				list.add(data0);
				//设置Gap,同上
				list.add(DataForSendToPrinterTSC.gapBymm(2, 0));
				
				//清除缓存
				list.add(DataForSendToPrinterTSC.cls());
				//条码指令，参数：int x，x方向打印起始点；int y，y方向打印起始点；
				//string font，字体类型；int rotation，旋转角度；
				//int x_multiplication，字体x方向放大倍数
				//int y_multiplication,y方向放大倍数
				//string content，打印内容
				byte[] data1 = DataForSendToPrinterTSC
						.text(10, 10, "4", 0, 1, 1,
								"123abc");
				list.add(data1);
				////打印直线,int x;int y;int width,线的宽度，int height,线的高度
				list.add(DataForSendToPrinterTSC.bar(20,
						40, 200, 3));
				////打印条码
				list.add(DataForSendToPrinterTSC.barCode(
						60, 50, "128", 100, 1, 0, 2, 2,
						"abcdef12345"));
				//打印二维码
				list.add(DataForSendToPrinterTSC.qrCode(20, 180, "L", 4, "A", 0, "M2", "S7", "abcdef"));
				//打印
				list.add(DataForSendToPrinterTSC.print(1));
				return list;
			}
        });
  }

  public void printFile() {
	  	String fileName = "print_awb.pdf";
	  	String filePath = Environment.getExternalStorageDirectory() + "/"+Environment.DIRECTORY_DOWNLOADS+ "/"+fileName;
		Uri pdfUri = Uri.fromFile(new File(filePath));
		String getPdfPath = PictureHelper.getPath(getApplicationContext(),pdfUri);
		String returnMsg;
		// Log.e("PdfPath", getPdfPath);

		File pdfFile = new File(getPdfPath);
		bitmapsPdf = pdfToBitmap(pdfFile);
		
		if(bitmapsPdf == null){
			Log.e("TEST BITMAP", "NULL  NULL  NULL  NULL");
			// return "Fail to convert to bitmap";
		}
		totalPage = bitmapsPdf.size() - 1;
		pageNum = 0;
		callPrinter();
  	}

	private void callPrinter(){
		binder.writeDataByYouself(new UiExecute() {

			@Override
			public void onsucess() {
				Log.e("success","Success to print");
				if(pageNum < totalPage)
				{
					pageNum++;
					callPrinter();
				}
				// return 200;
				// TODO Auto-generated method stub
			}

			@Override
			public void onfailed() {
				Log.e("fail","Fail to print");
				// return 400;
				// TODO Auto-generated method stub
			}
		}, new ProcessData() {//发送数据的处理和封装

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
				// 设置Gap,同上
				list.add(DataForSendToPrinterTSC.gapBymm(2, 0));
				// 清除缓存
				list.add(DataForSendToPrinterTSC.cls());

				list.add(DataForSendToPrinterTSC.bitmap(0, 0, 0, bitmapsPdf.get(pageNum), BmpType.Dithering, (int) (paperWidth * 8),
						(int) (paperHeight * 8)));
				// Log.e("TEST BITMAP", bitmapsPdf.get(i).getHeight() + ":::::" + bitmapsPdf.get(i).getWidth());

				list.add(DataForSendToPrinterTSC.print(1));
				
				// long EndTime = System.nanoTime();
				// Log.e("EndTimeCallPrinter", String.valueOf(EndTime));
				
		
				// createFileWithByte(list);
				return list;
			}

		});
	}

  	private  ArrayList<Bitmap> pdfToBitmap(File pdfFile) {
		ArrayList<Bitmap> bitmaps = new ArrayList<>();
		String fileName = "W7CQKU.pdf";
		String filePath = Environment.getExternalStorageDirectory() + "/"+Environment.DIRECTORY_DOWNLOADS+ "/"+fileName;
		File file = new File(filePath);

		long startTime = System.nanoTime();
		Log.e("StartTimeConvertImage", String.valueOf(startTime));
		try {
			PdfRenderer renderer = new PdfRenderer(ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY));

			Bitmap bitmap;
			final int pageCount = renderer.getPageCount();
			for (int i = 0; i < pageCount; i++) {
				PdfRenderer.Page page = renderer.openPage(i);

				// int width = getResources().getDisplayMetrics().densityDpi / 72 * page.getWidth();
				// int height = getResources().getDisplayMetrics().densityDpi / 72 * page.getHeight();
			
				int width = 840;
				int height = 1184;
				bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
				 // Paint bitmap before rendering
				Canvas canvas = new Canvas(bitmap);
				canvas.drawColor(Color.WHITE);
				canvas.drawBitmap(bitmap, 0, 0, null);
				page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
				// Log.e("Before Image in Memory size", String.valueOf(bitmap.getByteCount()) );
				// bitmap = toGrayscale(bitmap);

				// Bitmap resizeSmallerBitmap = Bitmap.createScaledBitmap(bitmap, 690, 1034, false);
				// Bitmap resizeBitmap = Bitmap.createScaledBitmap(resizeSmallerBitmap, width, height, false);

				
				// ByteArrayOutputStream baos = new ByteArrayOutputStream();
				// bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
				// byte[] byteArray = baos.toByteArray();
        		// Bitmap updatedBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
				// Bitmap decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(baos.toByteArray()));

				// Log.e("After Image in Memory size", String.valueOf(resizeBitmap.getByteCount()) );
			
				
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
			// 	FileOutputStream out = new FileOutputStream(file);
			// 	bitmap.compress(Bitmap.CompressFormat.PNG, 80, out);
			// 	Log.v("Saved Image - ", file.getAbsolutePath());
			// 	out.flush();
			// 	out.close();
			// } catch (Exception e) {
			// 	Log.e("Fail","Fail to save to pdf image");
			// }
			// bitmap.recycle();
			// resizeSmallerBitmap.recycle();
			// resizeBitmap.recycle();

		} catch (Exception ex) {
			Log.e("Fail","Fail to convert to bitmap", ex);
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

	private Bitmap toGrayscale(Bitmap bmpOriginal)
	{        
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

	public ArrayList<HashMap<String, String>> discoverBluetooth(){
		if (!blueadapter.isDiscovering()) {
				blueadapter.startDiscovery();
			}

		Set<BluetoothDevice> device=blueadapter.getBondedDevices();  
		ArrayList<HashMap<String, String>> bluetoothDeviceList = new ArrayList<HashMap<String, String>>();
		int count = 0;

			if(device.size()>0){ 
				for(Iterator<BluetoothDevice> it=device.iterator();it.hasNext();){  
			HashMap<String, String> bluetoothData = new HashMap<String, String>();
					BluetoothDevice btd=it.next();  
			BluetoothClass bluetoothClass = btd.getBluetoothClass();
		
			bluetoothData.put("name",btd.getName());
			bluetoothData.put("address",btd.getAddress());
			bluetoothData.put("type", String.valueOf(bluetoothClass.getMajorDeviceClass()));
			if (btd.getBondState() == BluetoothDevice.BOND_NONE) {
			bluetoothData.put("state", "Connected");
			} else if (btd.getBondState() == BluetoothDevice.BOND_BONDED) {
			bluetoothData.put("state", "Unconnected");
			}
			bluetoothDeviceList.add(count, bluetoothData);
			count++;
				}  
			}

		Handler handler = new Handler();
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				// Do the task...
			if(blueadapter!=null&&blueadapter.isDiscovering()){  
				blueadapter.cancelDiscovery();  
				}  
			}
		};
		handler.postDelayed(runnable, 5000);
			
		return bluetoothDeviceList;
	}

	private final BroadcastReceiver receiver = new BroadcastReceiver() {
		int count = 0;
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
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
			}else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				deviceState ="Disconnected";
			}

			Log.e("address", "bluetoothDeviceList "+ String.valueOf(bluetoothDeviceList.size()));
			if(bluetoothDeviceList.size() > 0)
			{
				ArrayList<HashMap<String, String>> checkDevice = new ArrayList<HashMap<String, String>>();
				HashMap<String, String> duplicateDevice;
				int dupliCount = 0;
				for(int i = 0; i < bluetoothDeviceList.size(); i++)
				{
				duplicateDevice = new HashMap<String, String>();
				HashMap<String, String> hashmap= bluetoothDeviceList.get(i);
				String address = hashmap.get("address");
				String state = hashmap.get("state");
				
				if(deviceHardwareAddress.equals(address))
				{
					if(!state.equals(deviceState))
					{
					hashmap.put("state", deviceState);
					}
					duplicateDevice.put("name", deviceName);
					duplicateDevice.put("address", deviceHardwareAddress);
					checkDevice.add(dupliCount, duplicateDevice);
					dupliCount++;
				}
				}
				
				if(checkDevice.size() == 0)
				{
					bluetoothData.put("name", deviceName);
					bluetoothData.put("address", deviceHardwareAddress);
					bluetoothData.put("type", type);
					bluetoothData.put("state", deviceState);
					bluetoothDeviceList.add(count, bluetoothData);
					count++;
				}
			}
			else
			{
				bluetoothData.put("name", deviceName);
				bluetoothData.put("address", deviceHardwareAddress);
				bluetoothData.put("type", type);
				bluetoothData.put("state", deviceState);
				bluetoothDeviceList.add(count, bluetoothData);
				count++;
			}
		}
	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// Don't forget to unregister the ACTION_FOUND receiver.
		unregisterReceiver(receiver);
	}

}