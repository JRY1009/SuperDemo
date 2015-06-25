package com.jry.superdemo;

import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class MainActivity extends ActionBarActivity {

    public static final String CMD_DEFAULT_LAUNCHER = "defaultLauncher";
    public static final String SUPER_CLASSNAME = "com.jry.superlibrary.SuperClass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    setDefaultLauncher(MainActivity.this, "superlibrary_dex.jar", "com.leidianos.launcher", "com.shouxinzm.launcher.Launcher");
                } catch (Exception e) {
                    Log.e("MainActivity", e.toString());
                }
            }
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static void setDefaultLauncher(Context cx, String jar, String pkg, String cls) {

        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            String data_path = cx.getFilesDir().getParent();
            String jar_path = data_path + "/" + jar;
            File out_file = new File(jar_path);
            if (!out_file.exists()) {
                inBuff = new BufferedInputStream(cx.getAssets().open(jar));
                outBuff = new BufferedOutputStream(new FileOutputStream(out_file));

                byte[] b = new byte[1024 * 5];
                int len;
                while ((len = inBuff.read(b)) != -1) {
                    outBuff.write(b, 0, len);
                }
                outBuff.flush();
            }

            String params = " " + CMD_DEFAULT_LAUNCHER + " " + pkg + " " + cls + "\n";
            String result = appProcessCmd(jar_path, SUPER_CLASSNAME, params);
            Log.e("setDefaultLauncher", result);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inBuff != null)
                    inBuff.close();
                if (outBuff != null)
                    outBuff.close();
            } catch (Exception e) {
            }
        }
    }

    private static String appProcessCmd(String jar, String cls, String params) throws Exception {
        String result = "none";
        DataOutputStream os = null;
        InputStream inputStream = null;
        try {
            Process p = Runtime.getRuntime().exec("su\n");

            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("export LD_LIBRARY_PATH=/vendor/lib:/system/lib\n");
            os.writeBytes("export CLASSPATH=" + jar + "\n");
            os.writeBytes("exec app_process /system/bin " + cls + params);
            os.flush();
            p.waitFor();

            inputStream = p.getInputStream();
            if (inputStream.available() > 0)
                result = getStringFromIO(inputStream);
        } finally {
            if (os != null)
                os.close();
            if (inputStream != null)
                inputStream.close();
        }
        return result;
    }

    public static String getStringFromIO(InputStream inputStream) {
        BufferedReader br = null;
        String result = null;
        try {
            String temp;
            StringBuilder sb = new StringBuilder();
            br = new BufferedReader(new InputStreamReader(inputStream));
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }

            result = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                }
        }
        return result;
    }
}
