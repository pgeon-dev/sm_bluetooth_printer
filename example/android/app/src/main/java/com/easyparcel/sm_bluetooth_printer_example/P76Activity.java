// package com.easyparcel.sm_bluetooth_printer_example;

// import java.io.UnsupportedEncodingException;
// import java.util.ArrayList;
// import java.util.List;

// import net.posprinter.posprinterface.ProcessData;
// import net.posprinter.posprinterface.UiExecute;
// import net.posprinter.utils.DataForSendToPrinterPos76;
// import net.posprinter.utils.DataForSendToPrinterPos80;
// import net.posprinter.utils.BitmapToByteData.BmpType;
// import android.app.Activity;
// import android.content.Intent;
// import android.database.Cursor;
// import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
// import android.net.Uri;
// import android.os.Bundle;
// import android.provider.MediaStore;
// import android.view.View;
// import android.view.View.OnClickListener;
// import android.widget.Button;
// import android.widget.Toast;

// public class P76Activity extends Activity{
// 	private Button bt1,bt2;
// 	@Override
// 	protected void onCreate(Bundle savedInstanceState) {
// 		// TODO Auto-generated method stub
// 		super.onCreate(savedInstanceState);
// 		// setContentView(R.layout.p76_activity);
// 		// setupView();
// 		addListener();
// 	}

// 	private void addListener() {
// 		// TODO Auto-generated method stub
// 		bt1.setOnClickListener(new OnClickListener() {
			
// 			@Override
// 			public void onClick(View v) {
// 				if (MainActivity.isConnect) {
// 					// TODO Auto-generated method stub
// 					MainActivity.binder.writeDataByYouself(new UiExecute() {

// 						@Override
// 						public void onsucess() {
// 							// TODO Auto-generated method stub

// 						}

// 						@Override
// 						public void onfailed() {
// 							// TODO Auto-generated method stub

// 						}
// 					}, new ProcessData() {

// 						@Override
// 						public List<byte[]> processDataBeforeSend() {
// 							// TODO Auto-generated method stub
// 							List<byte[]> list = new ArrayList<byte[]>();
// 							//����һ���������ӡ���ı�,ת��Ϊbyte[]���ͣ������ӵ�Ҫ���͵����ݵļ���list��
// 							String str = "Welcome to use the impact and thermal printer manufactured by professional POS receipt printer company!";
// 							byte[] data1 = strTobytes(str);
// 							list.add(data1);
// 							//׷��һ����ӡ����ָ���Ϊ��pos��ӡ����һ�вŴ�ӡ������һ�У�����ӡ
// 							list.add(DataForSendToPrinterPos76
// 									.printAndFeedLine());
// 							return list;
// 						}
// 					});
// 				}else {
// 					Toast.makeText(getApplicationContext(), "�������Ӵ�ӡ����", 0).show();
// 				}
// 			}
// 		});
// 		bt2.setOnClickListener(new OnClickListener() {
			
// 			@Override
// 			public void onClick(View v) {
// 				if (MainActivity.isConnect) {
// 					// TODO Auto-generated method stub
// 					// TODO Auto-generated method stub
// 					Intent intent;
// 					intent = new Intent(Intent.ACTION_GET_CONTENT);
// 					intent.addCategory(Intent.CATEGORY_OPENABLE);
// 					intent.setType("image/*");
// 					startActivityForResult(intent, 0);
// 				}else {
// 					Toast.makeText(getApplicationContext(), "�������Ӵ�ӡ����", 0).show();
// 				}
// 			}
// 		});
// 	}
// 	@Override
// 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
// 		// TODO Auto-generated method stub
// 		super.onActivityResult(requestCode, resultCode, data);
// 		if (requestCode==0&&resultCode==RESULT_OK) {
// 			//ͨ��ȥͼ��ѡ��ͼƬ��Ȼ��õ����ص�bitmap����
// 			Uri selectedImage = data.getData();
// 			String[] filePathColumn = { MediaStore.Images.Media.DATA };
// 			Cursor cursor = getContentResolver().query(selectedImage,
// 					filePathColumn, null, null, null);
// 			cursor.moveToFirst();
// 			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
// 			final String picturePath = cursor.getString(columnIndex);
// 			cursor.close();
// 			final Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
// 			MainActivity.binder.writeDataByYouself(new UiExecute() {

// 				@Override
// 				public void onsucess() {
// 					// TODO Auto-generated method stub

// 				}

// 				@Override
// 				public void onfailed() {
// 					// TODO Auto-generated method stub

// 				}
// 			}, new ProcessData() {//�������ݵĴ����ͷ�װ

// 						@Override
// 						public List<byte[]> processDataBeforeSend() {
// 							// TODO Auto-generated method stub
// 							ArrayList<byte[]> list = new ArrayList<byte[]>();
// //							//������Դ�ӡλ�ã���ͼƬ����
// //							int w=bitmap.getWidth();
// //							int h=bitmap.getHeight();
// //							int x = 0;
// //							if (w<576) {//576λ80��ӡ���Ĵ�ӡֽ�Ŀɴ�ӡ����
// //								x=(576-w)/2;
// //							}
// //							int m=x%256;
// //							int n=x/256;
// //							Log.i("���Դ�ӡλ��", "m="+m+",n="+n);
// //							
// //							
// //							
// //							list.add(DataForSendToPrinterPos80.printRasterBmp(0, bitmap, BmpType.Threshold ,AlignType.Center,576));
// 							list.add(DataForSendToPrinterPos76.initializePrinter());
// 							list.add(DataForSendToPrinterPos76.selectBmpModel(0, bitmap, BmpType.Dithering));
// //							byte[] data={0x1b,0x2a,0x00,0x10,0x00,0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x00,0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x0a};
// //							list.add(data);
// 							return list;
// 						}
// 					});
// 		}
		
// 	}

// 	// private void setupView() {
// 	// 	// TODO Auto-generated method stub
// 	// 	bt1=(Button) findViewById(R.id.button1);
// 	// 	bt2=(Button) findViewById(R.id.button2);		
// 	// }
// 	/**
// 	 * �ַ���תbyte����
// 	 * */
// 	public static byte[] strTobytes(String str){
// 		byte[] b=null,data=null;
// 		try {
// 			b = str.getBytes("utf-8");
// 			data=new String(b,"utf-8").getBytes("gbk");
// 		} catch (UnsupportedEncodingException e) {
// 			// TODO Auto-generated catch block
// 			e.printStackTrace();
// 		}
// 		return data;
// 	}
// }
