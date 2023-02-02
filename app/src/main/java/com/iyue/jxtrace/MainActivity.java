package com.iyue.jxtrace;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;


public class MainActivity extends AppCompatActivity {

    // Used to load the 'jxtrace' library on application startup.
    static {
        System.loadLibrary("jxtrace");
    }

    private String TAG = "iyue-> MainActivity: " ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }



    /**
     * A native method that is implemented by the 'jxtrace' native library,
     * which is packaged with this application. 未使用
     */
    public native String stringFromJNI();
}