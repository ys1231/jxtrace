package com.iyue.jxtrace.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iyue.jxtrace.IyuePackageInfo;
import com.iyue.jxtrace.R;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class IyuePakageAdapter extends RecyclerView.Adapter<IyuePakageAdapter.IyueViewHolder> {


    private List<IyuePackageInfo> mPackageInfoList;
    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private int old_position=999;
    // 和模块共享数据
    SharedPreferences mPref;
    private String currentHookPackageName;
    private String TAG = "iyue-> IyuePakageAdapter: s" ;

    public IyuePakageAdapter(Context mContext, List<IyuePackageInfo> mPakageInfoList) {
        this.mContext = mContext;
        this.mPackageInfoList = mPakageInfoList;
        this.mLayoutInflater = LayoutInflater.from(mContext);
        try {                                                                  // 新版本不再支持 MODE_WORLD_READABLE
            mPref = mContext.getSharedPreferences(mContext.getPackageName(), mContext.MODE_PRIVATE);
            currentHookPackageName = mPref.getString("hookPackageName", "");
            Toast.makeText(mContext,"获取root权限方便模块调用!", Toast.LENGTH_SHORT).show();
            Toast.makeText(mContext,"lspoased 里也要选择需要hook的app", Toast.LENGTH_SHORT).show();

        } catch (SecurityException ignored) {
            Log.d(TAG, "IyuePakageAdapter: getSharedPreferences fail:"+ignored.getMessage());
            mPref = null; // other fallback, if any
            currentHookPackageName = "";
        }
    }

    /**
     * @return 获取当前已经保存的包名
     */
    private String getCurrentPackageName(){
        String currentHookPackageName = mPref.getString("hookPackageName", "");
        if(currentHookPackageName.isEmpty()){
            return "";
        }else {
            return currentHookPackageName;
        }
    }

    /**
     * 保存需要hook的app包名给xposedmodule使用
     * @param packageName
     */
    private void saveHookPackage(String packageName){
        SharedPreferences.Editor edit = mPref.edit();
        try {
            edit.putString("hookPackageName", packageName);
            edit.commit();
        }catch (Exception e){
            edit.putString("hookPackageName", packageName);
            edit.apply();
        }
        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream dataOutputStream = new DataOutputStream(p.getOutputStream());
//            String cmd = "chmod 777 /data/data/"+mContext.getPackageName()+"/shared_prefs/com.iyue.jxtrace.xml";
//            dataOutputStream.writeBytes(cmd + "\n");
//            dataOutputStream.flush();
            String cmd = "chmod 777 -R /data/local/tmp";
            dataOutputStream.writeBytes(cmd + "\n");
            dataOutputStream.flush();
            cmd = "echo "+"\""+packageName+"\""+" > /data/local/tmp/hookPackage";
            dataOutputStream.writeBytes(cmd + "\n");
            dataOutputStream.flush();
            dataOutputStream.close();
        }catch (Exception e){
            e.printStackTrace();

        }


        Log.d(TAG, "saveHookPackage: "+packageName);
    }
    @Override
    public int getItemCount() {
        return mPackageInfoList.size();
    }

    @NonNull
    @Override
    public IyueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = mLayoutInflater.inflate(R.layout.iyue_pakagelist_item, parent, false);
        IyueViewHolder iyueViewHolder = new IyueViewHolder(inflate);
        return iyueViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull IyueViewHolder holder, @SuppressLint("RecyclerView") int position) {
        IyuePackageInfo iyuePakageInfo = mPackageInfoList.get(position);
        holder.mImageView.setImageDrawable(iyuePakageInfo.getImage());
        holder.mTitle.setText(iyuePakageInfo.getTitle());
        holder.mPackageName.setText(iyuePakageInfo.getPakageName());
        if (old_position==position || holder.mPackageName.getText().equals(getCurrentPackageName())){
            holder.mRelativeLayout.setBackgroundColor(Color.GREEN);
        }else {
            holder.mRelativeLayout.setBackgroundColor(Color.WHITE);
        }
        holder.mRelativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(old_position !=999 && old_position != position){
                    notifyDataSetChanged();
//                }
                Toast.makeText(mContext,"强制退出"+iyuePakageInfo.getPakageName()+"app即可生效 ",Toast.LENGTH_SHORT).show();
                v.setBackgroundColor(Color.GREEN);
                old_position = position;
                // 保存需要hook的
                saveHookPackage(iyuePakageInfo.getPakageName());
            }
        });
    }


    class IyueViewHolder extends RecyclerView.ViewHolder {

        ImageView mImageView;
        TextView mTitle;
        TextView mPackageName;
        RelativeLayout mRelativeLayout;

        public IyueViewHolder(@NonNull View itemView) {
            super(itemView);
            this.mImageView = itemView.findViewById(R.id.iv_image);
            this.mTitle = itemView.findViewById(R.id.tv_title);
            this.mPackageName = itemView.findViewById(R.id.tv_pakageName);
            // 用于绑定整个item的点击事件
            this.mRelativeLayout = itemView.findViewById(R.id.iv_item_container);
        }
    }

}
