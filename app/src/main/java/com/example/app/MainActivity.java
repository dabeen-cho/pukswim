package com.example.app;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.SmsManager;
import android.content.SharedPreferences;
import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

import android.media.MediaPlayer;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    private BluetoothSPP bt;
    int size;
    boolean blockDetected = false;
    boolean wallDetected = false;
    boolean downhillDetected = false;
    private SharedPreferences UserInfo;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView phoneNumberTextView = findViewById(R.id.phoneNumberEditText);
        TextView heightTextView = findViewById(R.id.heightEditText);
        TextView weightTextView = findViewById(R.id.weightEditText);

        String phoneNumber = getIntent().getStringExtra("phone_number");
        String height = getIntent().getStringExtra("height");
        String weight = getIntent().getStringExtra("weight");

        phoneNumberTextView.setText("보호자 번호: " + phoneNumber);
        heightTextView.setText("키: " + height);
        weightTextView.setText("몸무게: " + weight);






        bt = new BluetoothSPP(this); //Initializing


        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            TextView distance22 = findViewById(R.id.distance2); //텍스트뷰를 통해 초음파 센서 값 받아오기
            TextView distance33 = findViewById(R.id.distance3);
            TextView distance44 = findViewById(R.id.distance4);
            TextView resultt = findViewById(R.id.result);
            double finalSize = MainActivity.this.size / 10 * 2.5 * 3; // final size를 사용자의 보폭 기준 3걸음으로 설정


            public void onDataReceived(byte[] data, String message) { //데이터 수신용 코드 추가


                String[] array = message.split(",");

                distance22.setText(array[0]);
                distance33.setText(array[1]);
                distance44.setText(array[2]);


                double distance2 = Double.parseDouble(array[0]); //초음파센서값 3개 array 형식으로 안드로이드 스튜디오에 받아오기
                double distance3 = Double.parseDouble(array[1]);
                double distance4 = Double.parseDouble(array[2]);


                float[][] input = new float[][]{{(float) distance2, (float) distance3, (float) distance4}};
                float[][] output = new float[1][1];

                Interpreter tflite = getTfliteInterpreter("model.tflite");
                tflite.run(input, output);

                double prediction = output[0][0]; // Get the predicted value

                ImageView imageViewResult = findViewById(R.id.warning);



                if(prediction<100){
                    imageViewResult.setImageResource(R.drawable.warning);
                    resultt.setText(String.valueOf("머리 충격"));

                    sendMessage(phoneNumber, "환자의 머리에 충격이 있습니다");




                }
                else if(prediction<1000){
                    resultt.setText(String.valueOf("손 충격"));


                }else{
                    resultt.setText(String.valueOf("발 충격"));


                }




            }
            private void sendMessage(String phoneNumber, String message) {
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNumber, null, message, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        );


        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때

            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() { //연결해제
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() { //연결실패
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnConnect = findViewById(R.id.btnConnect); //연결시도
        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }
        });




    }

    private Interpreter getTfliteInterpreter(String modelpath) {
        try {
            return new Interpreter(loadModelFile(MainActivity.this, modelpath));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private MappedByteBuffer loadModelFile(Activity activity, String modelPath) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(modelPath);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }


    public void onDestroy() {
        super.onDestroy();
        bt.stopService(); //블루투스 중지
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
//                setup();
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
//                setup();
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }




}
