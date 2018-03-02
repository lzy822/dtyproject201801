package com.geopdfviewer.android;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
    public String ReadPDF(){
        PDFView pdfView = (PDFView) findViewById(R.id.pdfView);
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
                    Toast.makeText(this, "l11111", Toast.LENGTH_LONG).show();
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
            setTitle(pdfFileName);


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
        return Integer.toString(num_line);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_interface);

        final TextView textView = (TextView) findViewById(R.id.textView);
        Button button = (Button) findViewById(R.id.readButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = load();
                textView.setText(str);
            }
        });
        textView.setText(ReadPDF());


        }
        public void save(String str){
            FileOutputStream out = null;
            BufferedWriter writer = null;
            try {
                out = openFileOutput("data", Context.MODE_PRIVATE);
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
        public String load() {
            FileInputStream in = null;
            BufferedReader reader = null;
            StringBuilder content = new StringBuilder();
            try {
                in = openFileInput("data");
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
    }


