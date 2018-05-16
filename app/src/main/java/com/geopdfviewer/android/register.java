package com.geopdfviewer.android;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class register extends AppCompatActivity {
    EditText editText;
    Button button1;
    TextView textView;
    Toolbar tb;
    private static final String TAG = "register";

    //核对授权状态
    private boolean verifyLisenceStatus(String deviceId){
        SharedPreferences pref1 = getSharedPreferences("imei", MODE_PRIVATE);
        boolean isLicense = pref1.getBoolean("type", false);
        String endDate = pref1.getString("endDate", "");
        Log.w(TAG, "password: " + DataUtil.getPassword(deviceId.substring(9)) );
        Log.w(TAG, "getIMEI: islicense" + Boolean.toString(isLicense) );
        Log.w(TAG, "endDate: " +  endDate);
        if (isLicense){
            if (DataUtil.verifyDate(endDate)){
                Log.w(TAG, "endDate: " +  endDate);
                Intent intent = new Intent(register.this, select_page.class);
                startActivity(intent);
                this.finish();
                return true;
            }else {
                Log.w(TAG, "see here: " );
                SharedPreferences.Editor editor = getSharedPreferences("imei", MODE_PRIVATE).edit();
                editor.putString("mimei", deviceId);
                editor.putString("password", DataUtil.getPassword(deviceId.substring(9)));
                editor.putBoolean("type", false);
                editor.apply();
                Toast.makeText(register.this, "授权码已经过期, 请重新获取", Toast.LENGTH_LONG).show();
                return false;
            }
        }else {
            Log.w(TAG, "see here: " );
            SharedPreferences.Editor editor = getSharedPreferences("imei", MODE_PRIVATE).edit();
            editor.putString("mimei", deviceId);
            editor.putString("password", DataUtil.getPassword(deviceId.substring(9)));
            editor.putBoolean("type", false);
            editor.apply();
            return false;
        }
    }

    //请求授权
    private void requestAuthority(){
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(register.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(register.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ContextCompat.checkSelfPermission(register.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.RECORD_AUDIO);
        }
        if (ContextCompat.checkSelfPermission(register.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.CAMERA);
        }
        if (ContextCompat.checkSelfPermission(register.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(register.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()){
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(register.this, permissions, 118);
        }else {
            //getLocation();
            //initPage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 118:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须通过所有权限才能使用本程序", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }else {
                            File file = new File(Environment.getExternalStorageDirectory() + "/TuZhi");
                            if (!file.exists() && !file.isDirectory()){
                                file.mkdirs();
                            }
                            final String imei = getIMEI();
                            textView.setText("请求码: " + imei + "(长按复制)");
                        }
                    }

                }
                break;
            default:
        }
    }

    //获取设备IMEI码
    public String getIMEI(){
        String deviceId = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            deviceId = telephonyManager.getDeviceId();
        }catch (SecurityException e){

        }catch (NullPointerException e){

        }
        return deviceId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //申请动态权限
        requestAuthority();
        tb = (Toolbar) findViewById(R.id.toolbar6) ;
        setSupportActionBar(tb);
        getSupportActionBar().setTitle("授权页面");
        editText = (EditText) findViewById(R.id.inputic_edit);
        button1 = (Button) findViewById(R.id.ok_button);
        textView = (TextView) findViewById(R.id.ic_text);
        final String imei = getIMEI();
        Log.w(TAG, "imei: " + imei );
        if (!verifyLisenceStatus(imei)) {
            textView.setText("请求码: " + imei + "(长按复制)");
            textView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData myClip;
                    myClip = ClipData.newPlainText("设备码", imei);
                    manager.setPrimaryClip(myClip);
                    Log.w(TAG, "onLongClick: " + imei);
                    Toast.makeText(MyApplication.getContext(), "已复制到剪贴板", Toast.LENGTH_SHORT).show();
                    return true;
                }
            });
            button1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String edittxt = editText.getText().toString();
                    if (edittxt.length() >= 9 & edittxt.length() <= 11) {
                        Log.w(TAG, "str: " + edittxt.substring(0, edittxt.length() - 2));
                        manageInputLisence(edittxt);
                    }
                }
            });
        }
        /*Intent intent = new Intent(register.this, select_page.class);
        startActivity(intent);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maintoolbar, menu);
        menu.findItem(R.id.info).setVisible(false);
        menu.findItem(R.id.queryPOI).setVisible(false);
        menu.findItem(R.id.queryLatLng).setVisible(false);
        menu.findItem(R.id.back).setVisible(false);
        return true;

    }

    //核对输入信息
    private boolean verifyInputLisence(String edittxt){
        SharedPreferences pref1 = getSharedPreferences("imei", MODE_PRIVATE);
        String password = pref1.getString("password", "");
        if (edittxt.substring(0, edittxt.length() - 1).contentEquals(password)) return true;
        else return false;
    }

    //处理输入信息
    private void manageInputLisence(String edittxt){
        SimpleDateFormat df2 = new SimpleDateFormat("yyyy年MM月dd日");
        Date date = new Date(System.currentTimeMillis());
        String startTime = df2.format(date);
        if (verifyInputLisence(edittxt)){
            boolean isOKforGo = false;
            SharedPreferences.Editor editor = getSharedPreferences("imei", MODE_PRIVATE).edit();
            if (edittxt.substring(edittxt.length() - 1, edittxt.length()).contentEquals("x")) {
                editor.putBoolean("type", true);
                editor.putString("startDate", startTime);
                editor.putString("endDate", DataUtil.datePlus(startTime, 7));
                isOKforGo = true;
            } else if (edittxt.substring(edittxt.length() - 1, edittxt.length()).contentEquals("X")) {
                editor.putBoolean("type", true);
                editor.putString("startDate", startTime);
                editor.putString("endDate", DataUtil.datePlus(startTime, 180));
                isOKforGo = true;
            } else if (edittxt.substring(edittxt.length() - 1, edittxt.length()).contentEquals("y")) {
                editor.putBoolean("type", true);
                editor.putString("startDate", startTime);
                editor.putString("endDate", DataUtil.datePlus(startTime, 366));
                isOKforGo = true;
            }else if (edittxt.substring(edittxt.length() - 1, edittxt.length()).contentEquals("g")) {
                editor.putBoolean("type", true);
                editor.putString("startDate", startTime);
                editor.putString("endDate", DataUtil.datePlus(startTime, 30));
                isOKforGo = true;
            }else if (edittxt.substring(edittxt.length() - 1, edittxt.length()).contentEquals("G")) {
                editor.putBoolean("type", true);
                editor.putString("startDate", startTime);
                editor.putString("endDate", DataUtil.datePlus(startTime, 90));
                isOKforGo = true;
            }else if (edittxt.substring(edittxt.length() - 1, edittxt.length()).contentEquals("j")) {
                editor.putBoolean("type", true);
                editor.putString("startDate", startTime);
                editor.putString("endDate", DataUtil.datePlus(startTime, 3660));
                isOKforGo = true;
            }
            if (isOKforGo){
                editor.apply();
                Intent intent = new Intent(register.this, select_page.class);
                startActivity(intent);
                register.this.finish();
            }else Toast.makeText(MyApplication.getContext(), "请联系我们获取授权码", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(MyApplication.getContext(), "请联系我们获取授权码", Toast.LENGTH_LONG).show();
        }

    }
}
