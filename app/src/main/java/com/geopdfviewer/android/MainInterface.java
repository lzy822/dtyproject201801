package com.geopdfviewer.android;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle;
import com.github.barteksc.pdfviewer.util.FitPolicy;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class MainInterface extends AppCompatActivity  implements OnPageChangeListener, OnLoadCompleteListener,
        OnPageErrorListener {
    private static final String TAG = "MainInterface";
    public static final String SAMPLE_FILE = "pdf/sample1.pdf";
    public static final String SAMPLE_FILE1 = "txt/geo_information.txt";
    Integer pageNumber = 0;
    public String content;
    public int num_line = 0;
    String pdfFileName;
    public boolean isESRI = false;
    String info;
    PDFView pdfView;
    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageChanged(int page, int pageCount) {

    }

    @Override
    public void onPageError(int page, Throwable t) {

    }

    public void WKTFormat() {

    }
    public void ReadPDF(){
        pdfView = (PDFView) findViewById(R.id.pdfView);
        pdfFileName = SAMPLE_FILE;
        pdfView.fromAsset(SAMPLE_FILE)
                .defaultPage(pageNumber)
                .onPageChange(this)
                .enableAnnotationRendering(true)
                .onLoad(this)
                .scrollHandle(new DefaultScrollHandle(this))
                .spacing(10) // in dp
                .onPageError(this)
                .pageFitPolicy(FitPolicy.BOTH)
                .load();
        try {
            InputStream inputStream = getAssets().open(SAMPLE_FILE);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuffer sb = new StringBuffer("");
            String line;

            while((line = bufferedReader.readLine()) != null) {
                //Toast.makeText(this, "正在获取内容!", Toast.LENGTH_LONG).show();
                sb.append(line + "/n");
                if(line.contains("PROJCS")) {

                    content = line.substring(line.indexOf("PROJCS["), line.indexOf(")>>"));

                }
                if (line.contains("ESRI") || line.contains("esri") || line.contains("arcgis") || line.contains("ARCGIS") || line.contains("Adobe"))
                {
                    isESRI = true;
                    //Toast.makeText(this, "l11111", Toast.LENGTH_LONG).show();
                }
                //content = line;
                num_line += 1;
            }
            if (isESRI == true) {
                content = "ESRI::" + content;
                save(content);
            } else {
                save(content);
            }
            //Toast.makeText(this, content, Toast.LENGTH_LONG).show();
            setTitle(subassetstring(pdfFileName));


            /*String path = "/geo_information.txt";
            File file = new File(path);
            FileWriter writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            bw.write(sb.toString());
            bw.close();
            writer.close();*/



            Toast.makeText(this, "获取完毕!", Toast.LENGTH_LONG).show();
            inputStream.close();
        }
        catch (IOException e) {
            Toast.makeText(this, "无法获取示例文件!", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_interface);
        pdfView = (PDFView) findViewById(R.id.pdfView);
        final TextView textView = (TextView) findViewById(R.id.textview);
        //final FloatingActionsMenu menu = (FloatingActionsMenu) findViewById(R.id.fam);
        com.getbase.floatingactionbutton.FloatingActionButton button1 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.measure);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("开始测量!");
                Log.e(TAG, "onClick: " );
            }
        });
        com.getbase.floatingactionbutton.FloatingActionButton button2 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.north);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("指北针!");
                pdfView.resetZoom();
            }
        });
        com.getbase.floatingactionbutton.FloatingActionButton button3 = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.restorezoom);
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textView.setText("zoom!");
                pdfView.resetZoomWithAnimation();
            }
        });



        ReadPDF();


        }

    public void save(String str){
        info = str;
        String fname = "data_" + subassetstring(pdfFileName);
        FileOutputStream out = null;
        BufferedWriter writer = null;
        try {
            out = openFileOutput(fname, Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(str);
            } catch (IOException e) {
            e.printStackTrace();
            } finally {
            try {
                if (writer != null) {
                    writer.close();
                    }} catch (IOException ee) {
                ee.printStackTrace();
                    }

            }
        }
    public String load(String name) {
        String fname = "data_" + name;
            FileInputStream in = null;
            BufferedReader reader = null;
            StringBuilder content = new StringBuilder();
            try {
                in = openFileInput(fname);
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return content.toString();
        }
    public String subassetstring(String str){
        str = str.substring(4, str.indexOf("."));
        return str;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.maintoolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.back:
                this.finish();
                break;
            case R.id.info:
                //String data = "data_" + subassetstring(pdfFileName);
                String data = info;
                Intent intent = new Intent(MainInterface.this, info_page.class);
                intent.putExtra("extra_data", data);
                startActivity(intent);


                break;
            default:
        }
        return true;
    }
}



