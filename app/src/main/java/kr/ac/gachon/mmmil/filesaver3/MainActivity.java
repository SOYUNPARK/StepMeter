package kr.ac.gachon.mmmil.filesaver3;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

//import kr.ac.gachon.mmmil.filesaver3.R;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity implements SensorEventListener {
    private SensorManager mSensorManager;
    Sensor mag_sensor;
    //Sensor acc_sensor;
    int starting = 0;   // 앱이 실행되었을 때 센서
    int saving = 0;
    int buttonClicked = 1;
    //float AccX;
    //float AccY;
    //float AccZ;
    long time = 0; // millisecond 저장
    //long lastTime;
    long timeFileName;

    float[] acc_data;
    float[] mag_data;

    float[] rotation;
    //float[] result_data;
    //float[] m_result_data;
    float[] m_acc_data_rotated;

    TextView tvDataX;
    TextView tvDataY;
    TextView tvDataZ;
    TextView savingstate;

    Vector<Float> vX;
    Vector<Float> vY;
    Vector<Float> vZ;
    Vector<Long> vT;

    Vector<Float> v_accX;
    Vector<Float> v_accY;
    Vector<Float> v_accZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // mEditContents = (EditText) findViewById(R.id.editContents);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mag_sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        //acc_sensor =  mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        vX = new Vector<Float>(20000, 1000);
        vY = new Vector<Float>(20000, 1000);
        vZ = new Vector<Float>(20000, 1000);
        vT = new Vector<Long>(20000, 1000);

        v_accX = new Vector<Float>(20000, 1000);
        v_accY = new Vector<Float>(20000, 1000);
        v_accZ = new Vector<Float>(20000, 1000);

        mag_data = new float[3]; //센서데이터를 저장할 배열 생성
        acc_data = new float[3];
        rotation = new float[9];
        //result_data = new float[3];
        //m_result_data = new float[3];
        m_acc_data_rotated = new float[3];

        savingstate = (TextView) findViewById(R.id.redletter);
        tvDataX = (TextView) findViewById(R.id.tvaccX);
        tvDataY = (TextView) findViewById(R.id.tvaccY);
        tvDataZ = (TextView) findViewById(R.id.tvaccZ);


        // findViewById(R.id.save).setOnClickListener( new OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //       writeContents(mEditContents.getText().toString());
        //      mEditContents.setText("");
        //     Toast.makeText(MainActivity.this, "내용을 썼습니다.",
        //             Toast.LENGTH_SHORT).show();
        //    }
        //  });
    }

    @Override
    protected void onPause() {
        // 센서 리스너 해제
        mSensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 센서 리스너 등록
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST);
        mSensorManager.registerListener(this, mag_sensor, SensorManager.SENSOR_DELAY_FASTEST);
        //센서가 전달되는 속도 SENSOR_DELAY_UI / SENSOR_DELAY_FASTEST / SENSOR_DELAY_GAME
        starting = 1;

    }


    @Override
    public void onSensorChanged(SensorEvent event) {
        if ((starting * saving) == 1) {   // 리스너가 등록되고, [보행시작] 버튼 클릭시 if문 true
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {  //센서가 읽어들인 값이 마그네틱필드일때
                mag_data = event.values.clone();
            }   //데이터를 모두 mag_data 배열에 저장
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {  //센서가 읽어들인 값이 가속도값일때
                acc_data = event.values.clone(); //데이터를 모두 acc_data 배열에 저장  -> 한번에 받아서 파일출력할때 구별

                vX.add(acc_data[0]);
                vY.add(acc_data[1]);
                vZ.add(acc_data[2]);
                vT.add(time);

                time = System.currentTimeMillis();
                tvDataX.setText(String.valueOf(acc_data[0]));
                tvDataY.setText(String.valueOf(acc_data[1]));
                tvDataZ.setText(String.valueOf(acc_data[2]));
                if (mag_data != null && acc_data != null) { //널체크     ///  센서 매니저의 로테이션 로직, 오리엔테이션,
                    SensorManager.getRotationMatrix(rotation, null, acc_data, mag_data); //회전메트릭스 연산 params( R gravity, I magnetic, acc, mag ) (0,0,9.8) 회전배열 반환 R
/*
            SensorManager.getOrientation(rotation, result_data); //연산값으로 방향값 산출
            result_data[0] = (float) Math.toDegrees(result_data[0]); // 방향값을 각도로 변환 rad -> deg
            if (result_data[0] < 0) {
                m_result_data[0] += 360; //z의 deg값이 0보다 작을경우 360을더해줌ㄴ
            }
*/
                    m_acc_data_rotated[0] = rotation[0] * acc_data[0] + rotation[1] * acc_data[1] + rotation[2] * acc_data[2];  //회전변환 후의 accx값
                    m_acc_data_rotated[1] = rotation[3] * acc_data[0] + rotation[4] * acc_data[1] + rotation[5] * acc_data[2];  //회전변환 후의 accy값
                    m_acc_data_rotated[2] = rotation[6] * acc_data[0] + rotation[7] * acc_data[1] + rotation[8] * acc_data[2];  //회전변환 후의 accz값 // 매트릭스 생성 ㄴㄴ, 즉시 곱해주자

                    v_accX.add(m_acc_data_rotated[0]);
                    v_accY.add(m_acc_data_rotated[1]);
                    v_accZ.add(m_acc_data_rotated[2]);
                }
                try {
                    sleep(2);
                    //Log.i("Sensorchanged ", " m_acc_data_rotated " + m_acc_data_rotated[0] +"  ,    time : " +time);    //http://www.fileformat.info/tip/java/date2millis.htm  milliseconds convert

                } catch (InterruptedException ie) {  //sleep 주기를 입력받도록, 지금은 start, save가 모두 true면 실행, sleep주기도 입력받아야만 실행되도록
                }
            }
        }
    }


  /*
  ///////////각각의 변수 선언해서 각각 받아왔었음   --> 지금은 배열주소로 얻어와서 인덱스로 접근
            tvDataX = (TextView) findViewById(R.id.tvaccX);
            tvDataY = (TextView) findViewById(R.id.tvaccY);
            tvDataZ = (TextView) findViewById(R.id.tvaccZ);

            AccX = event.values[0];
            AccY = event.values[1];
            AccZ = event.values[2];

            vX.add(AccX);
            vY.add(AccY);
            vZ.add(AccZ);
            vT.add(time);

            time = System.currentTimeMillis();

            tvDataX.setText(String.valueOf(AccX));
            tvDataY.setText(String.valueOf(AccY));
            tvDataZ.setText(String.valueOf(AccZ));
*/

    /*********** 센서값의 정확도를 측정할 수 있음  ************/
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    /*********** [보행시작]버튼  ************/
    public void start(View v) {
        if (v.getId() == R.id.start) {
            saving = 1;
            savingstate.setText("기록 중....");
        }
    }

    /*********** [보행저장]버튼  ************/
    public void save(View v) {
        if (v.getId() == R.id.save) {
            if (buttonClicked == 1) {
                timeFileName = System.currentTimeMillis();  //버튼을 눌렀을때의 시간으로 파일명 설정
                savingstate.setText("파일 저장 중....");
                writeContents();
                //Log.i("Time Class ", " Time value in milliseconds " + time);    //http://www.fileformat.info/tip/java/date2millis.htm  milliseconds convert
            }
            else
                savingstate.setText("중복저장은 불가능합니다.");
        }
    }

    /**************** 파일 저장 **************/
    public final int PERMISSIONS_REQUEST_CODE = 1;

    private void writeContents() {
        int hasWriteExternalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasWriteExternalStoragePermission == PackageManager.PERMISSION_GRANTED) {   //이미 퍼미션을 가지고 있나
        } else {  //가지고 있지 않으면 퍼미션 요청
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
        }

        /// 파일 출력 디렉토리 확인 및 생성
        File temppath = new File(Environment.getExternalStorageDirectory(),
                "SENSOR");           //외장메모리의 경로 저장        /storage/emulated/0/SENSOR
        if (!temppath.exists()) {        //해당 디렉터리가 존재하지 않으면 생성
            temppath.mkdirs();
            Toast.makeText(MainActivity.this, "외부경로 존재x",
                    Toast.LENGTH_SHORT).show();
        }

        /// 파일 열기
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd_HH:mm:ss"); // 파일명 서식, 내가 설정
        Date resultdate = new Date(timeFileName);

        buttonClicked = 0;
        File tempfile = new File(temppath, date.format(resultdate) + ".txt"); //txt파일명을 변수로 (current Time -> yymmdd.txt)
        FileWriter output = null;
        try {
            output = new FileWriter(tempfile, false);  //true면 append
            // 컨텐츠를 쓰기
            for(int i=0;i<5;i++){
                vT.remove(i);
                vX.remove(i);
                vY.remove(i);
                vZ.remove(i);
                v_accX.remove(i);
                v_accY.remove(i);
                v_accZ.remove(i);
            }
            output.write("% Vector size : " + vT.size() + "\r\n");
            output.write("% Date and time : " + tempfile + "\r\n");


            for (int j = 0; j < vT.size(); j++) {
                output.write(Long.toString((vT.elementAt(j)) - vT.elementAt(0)));
                output.write(",");
                output.write(String.format("%.2f", vX.elementAt(j)));
                output.write(",");
                output.write(String.format("%.2f", vY.elementAt(j)));
                output.write(",");
                output.write(String.format("%.2f", vZ.elementAt(j)));
                output.write(",");
                output.write(String.format("%.2f", v_accX.elementAt(j)));
                output.write(",");
                output.write(String.format("%.2f", v_accY.elementAt(j)));
                output.write(",");
                output.write(String.format("%.2f", v_accZ.elementAt(j)));
                output.write("\r\n");
            }

            savingstate.setText("파일 저장 완료");
            onPause();
            tvDataX.setText("-");
            tvDataY.setText("-");
            tvDataZ.setText("-");

            //output.write(contents);
            //output.write("\r\n"); // \n만 쓰면 개행문자(네모,동그라미)삽입 \r\n쓰면 개행(window)  \r만쓰면 음표(개행문자인듯)
            Toast.makeText(MainActivity.this, "내용을 썼습니다.", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Toast.makeText(MainActivity.this, "FileNotFoundException",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "IOException",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "IOException with file.close",
                            Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }
    }
}


        /*

         if (sensor == Sensor.TYPE_ACCELEROMETER) {
                switch (accuracy) {
                    case 0:
                        System.out.println("Unreliable");
                        break;
                    case 1:
                        System.out.println("Low Accuracy");
                        break;
                    case 2:
                        System.out.println("Medium Accuracy");
                        break;
                    case 3:
                        System.out.println("High Accuracy");
                        break;
                }
            }
        }


 */





        /*
        File filepath = new File(Environment.getExternalStorageDirectory(),
                "SENSOR");
        if (!filepath.exists()) {
            filepath.mkdirs();
            Toast.makeText(MainActivity.this, "외부경로 존재x",
                    Toast.LENGTH_SHORT).show();
        }

        // 파일을 열기
        File savefile = new File(filepath, "sensor.txt");
        FileWriter output = null;

        try {
            savefile.createNewFile();
            BufferedWriter Write = new BufferedWriter(new FileWriter(savefile));
            Write.append(AccX+"," + AccY+"," + AccZ);
            Write.append("\r\n");
            Write.flush();
            Write.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {

            FileOutputStream fout = new FileOutputStream(savefile);

            output = new FileWriter(savefile, false);  //true면 이어쓰기
            // 컨텐츠를 쓰기
            output.write(String.valueOf(acc));
            output.write("\r\n"); // \n만 쓰면 개행문자삽입 \r\n쓰면 개행 \r만쓰면 음표(개행문자인듯)
            Toast.makeText(MainActivity.this, "내용을 썼습니다.", Toast.LENGTH_SHORT).show();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(MainActivity.this, "IOException",
                    Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    */


