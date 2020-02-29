package com.example.asynctask;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    public static final int INIT_PROGRESS = 0;
    public static final String APK_URL = "http://download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk";
    public static final String FILE_NAME = "imooc.apk";
    private ProgressBar mprogressBar;
    private Button mbtn;
    private TextView mtv;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        permission();

        initView();

        setListener();

        setData();

    }

    private void permission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i]);
            }
        }
    }

    private void setData() {
        mprogressBar.setProgress(INIT_PROGRESS);
        mbtn.setText(R.string.click_download);
        mtv.setText(R.string.download_text);
    }

    private void setListener() {
        mbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: 2020/2/15 下载任务
                DownloadAsyncTask asyncTask=new DownloadAsyncTask();
                asyncTask.execute(APK_URL);
            }
        });

    }

    private void initView() {
        mprogressBar = findViewById(R.id.progressBar);
        mbtn = findViewById(R.id.button);
        mtv = findViewById(R.id.textView);

    }

    public class DownloadAsyncTask extends AsyncTask<String, Integer, Boolean>{

        String mFilePath;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mbtn.setText(R.string.downloading);
            mtv.setText(R.string.downloading);
            mprogressBar.setProgress(INIT_PROGRESS  );
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if(params !=null &&params.length>0){
                String apkurl=params[0];

                System.err.println("地址： "+apkurl);
                try {
                    System.err.println("提示");

                    URL url = new URL(apkurl);

                    URLConnection urlConnection=url.openConnection();

                    InputStream inputStream=urlConnection.getInputStream();
                    //下载的总长度
                    int contentLength =urlConnection.getContentLength();

                    mFilePath = getExternalFilesDir(null) + File.separator + FILE_NAME;

                    System.err.println("文件路径"+ mFilePath);

                    File apkFile = new File(mFilePath);

                    if(apkFile.exists()){
                        boolean result=apkFile.delete();
                        if(!result){
                            return false;
                        }
                    }
                    //已下载的大小
                    int downloadSize=0;

                    //byte数组
                    byte[] bytes=new byte[1024];

                    int length;

                    OutputStream outputStream=new FileOutputStream(mFilePath);

                    while ((length=inputStream.read(bytes)) != -1){
                        outputStream.write(bytes,0,length);
                        //累加文件大小
                        downloadSize+=length;
                        //发送进度
                        publishProgress(downloadSize * 100/contentLength);
                    }
                    inputStream.close();
                    outputStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }

            }else {
                return false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mbtn.setText(getString(R.string.download_finish));
            mtv.setText(result? getString(R.string.download_finish )+mFilePath: getString(R.string.download_fail));
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            if(values != null && values.length > 0) {
                mprogressBar.setProgress(values[0]);
            }
        }
    }

}
