package com.test.pythontest2;

import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

// 简易loading
public class LoadingUtil {
    private AlertDialog.Builder alterDiaglog;
    private AlertDialog dialog;
    private TextView loading_text;
    private ImageView loading_image;
    private Button loading_btn_ok;
    private Activity activity;

    public Context getActivity() {
        return activity;
    }

    public LoadingUtil(Activity context) {
        this.activity = context;
        alterDiaglog = new AlertDialog.Builder(context){};
        //获取界面
        View dialogView = LayoutInflater.from(context).inflate(R.layout.loading, null);
        alterDiaglog.setView(dialogView);
        dialog = alterDiaglog.create();
        //设置点击其他区域不会关闭
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(false);
        loading_image=dialogView.findViewById(R.id.loading_image);
        loading_text=dialogView.findViewById(R.id.loading_text);
        loading_btn_ok = dialogView.findViewById(R.id.loading_btn_ok);
        loading_btn_ok.setOnClickListener(view -> {
            if (dialog.isShowing()){
                dialog.dismiss();
            }
        });
    }

    public void show(){
        activity.runOnUiThread(() -> {
            //显示
            dialog.show();
            Window dialogWindow = dialog.getWindow();
            dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
            //p.height = WindowManager.LayoutParams.MATCH_PARENT;
            //p.width = WindowManager.LayoutParams.MATCH_PARENT;
            p.gravity = Gravity.CENTER;//设置位置
            dialogWindow.setAttributes(p);
            dialog.getWindow().getDecorView().setBackgroundResource(0x00000000);
        });

    }

    public void dismiss(){
        activity.runOnUiThread(() -> {
            if (dialog.isShowing()) {
                dialog.dismiss();
            }
        });
    }

    public void setText(final String text){
        activity.runOnUiThread(() -> loading_text.setText(text));
    }

    public void showLoadingImage(){
        activity.runOnUiThread(() -> loading_image.setVisibility(View.VISIBLE));
    }

    public void dismissLoadingImage(){
        activity.runOnUiThread(() -> loading_image.setVisibility(View.GONE));
    }

    public void showLoadingButton(){
        activity.runOnUiThread(() -> loading_btn_ok.setVisibility(View.VISIBLE));
    }

    public void dismissLoadingButton(){
        activity.runOnUiThread(() -> loading_btn_ok.setVisibility(View.GONE));
    }
}
