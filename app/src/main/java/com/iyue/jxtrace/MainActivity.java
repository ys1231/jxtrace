package com.iyue.jxtrace;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.iyue.jxtrace.adapter.IyuePakageAdapter;
import com.iyue.jxtrace.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'jxtrace' library on application startup.
    static {
        System.loadLibrary("jxtrace");
    }

    private ActivityMainBinding binding;
    private RecyclerView mRecyclerView=null;
    private List<IyuePackageInfo> mPakageInfoList;
    private IyuePakageAdapter mPakageAdapter=null;
    private String TAG = "iyue-> MainActivity: " ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
        initData();
        initEvents();
    }

    private void initEvents() {
        mPakageAdapter = new IyuePakageAdapter(this,mPakageInfoList);
        mRecyclerView.setAdapter(mPakageAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initData() {

        mPakageInfoList = new ArrayList<>();
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo packageInfo = packages.get(i);
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) { //非系统应用
                // AppInfo 自定义类，包含应用信息
                // 1. 获取应用名称
                String appName =applicationInfo.loadLabel(getPackageManager()).toString();
                // 2. 获取应用包名
                String packageName = applicationInfo.packageName;
                if(packageName.equals(getPackageName()))
                    continue;
                // 3. 获取图标
                Drawable drawable = applicationInfo.loadIcon(getPackageManager());
                IyuePackageInfo pakageInfo = new IyuePackageInfo(appName,packageName,drawable);
                mPakageInfoList.add(pakageInfo);
//                Log.d(TAG, "getAppInfo: "+pakageInfo.toString());

            } else { // 系统应用

            }
        }



    }

    private void initView() {

        mRecyclerView = binding.recyclervivew;


    }

    /**
     * A native method that is implemented by the 'jxtrace' native library,
     * which is packaged with this application. 未使用
     */
    public native String stringFromJNI();
}