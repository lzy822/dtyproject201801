package com.geopdfviewer.android;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

public class register extends AppCompatActivity {
    EditText editText;
    Button button1;
    TextView textView;
    Toolbar tb;
    private static final String TAG = "register";

    //计算识别码
    private String getPassword(String deviceId){
        String password;
        password = "l" + deviceId.substring(8) + "Zy";
        Log.w(TAG, "getPassword: " +  password);
        return password;
    }

    //获取设备IMEI码
    private String getIMEI(){
        String deviceId = "";
        try {
            TelephonyManager telephonyManager = (TelephonyManager) getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
            /*if (Build.VERSION.SDK_INT > 16) deviceId = telephonyManager.getImei();
            else */
            deviceId = telephonyManager.getDeviceId();
            SharedPreferences pref1 = getSharedPreferences("imei", MODE_PRIVATE);
            boolean isLicense = pref1.getBoolean("type", false);
            Log.w(TAG, "getIMEI: islicense" + Boolean.toString(isLicense) );
            if (isLicense){
                Intent intent = new Intent(register.this, select_page.class);
                startActivity(intent);
                this.finish();
            }else {
                SharedPreferences.Editor editor = getSharedPreferences("imei", MODE_PRIVATE).edit();
                editor.putString("mimei", deviceId);
                editor.putString("password", getPassword(deviceId));
                editor.putBoolean("type", false);
                editor.apply();
            }
            /*SharedPreferences.Editor editor = getSharedPreferences("imei", MODE_PRIVATE).edit();
            editor.putString("mimei", deviceId);
            editor.putString("password", getPassword(deviceId));
            editor.putInt("type", 1);*/
        }catch (SecurityException e){

        }catch (NullPointerException e){

        }
        Log.w(TAG, "getIMEI: " + deviceId );
        return deviceId;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        tb = (Toolbar) findViewById(R.id.toolbar6) ;
        setSupportActionBar(tb);
        getSupportActionBar().setTitle("授权页面");
        editText = (EditText) findViewById(R.id.inputic_edit);
        button1 = (Button) findViewById(R.id.ok_button);
        textView = (TextView) findViewById(R.id.ic_text);
        final String imei = getIMEI();
        textView.setText("本机识别号: " + imei + "(长按复制)");
        textView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipboardManager manager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                manager.setText(imei);
                Toast.makeText(MyApplication.getContext(), "已复制到剪贴板", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences pref1 = getSharedPreferences("imei", MODE_PRIVATE);
                String password = pref1.getString("password", "");
                Log.w(TAG, "onClick: " + password );
                if (editText.getText().toString().contentEquals(password) ){
                    SharedPreferences.Editor editor = getSharedPreferences("imei", MODE_PRIVATE).edit();
                    editor.putBoolean("type", true);
                    editor.apply();
                    Intent intent = new Intent(register.this, select_page.class);
                    startActivity(intent);
                    register.this.finish();
                }else Toast.makeText(MyApplication.getContext(), "请联系我们获取授权码", Toast.LENGTH_LONG).show();
            }
        });
        /*Intent intent = new Intent(register.this, select_page.class);
        startActivity(intent);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maintoolbar, menu);
        menu.findItem(R.id.info).setVisible(false);
        menu.findItem(R.id.queryPOI).setVisible(false);
        menu.findItem(R.id.query).setVisible(false);
        menu.findItem(R.id.back).setVisible(false);
        return true;

    }
}
