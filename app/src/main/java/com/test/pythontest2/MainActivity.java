package com.test.pythontest2;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.test.pythontest2.pythonToJava.JavaBean;
import com.test.pythontest2.pythonToJava.YouZhi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity+++";
    public final static int REQ_PERMISSION_CODE = 0x1000; // 权限标识码
    public final static int SETTING_REQ_PERMISSION_CODE = 0x2000; // Setting返回时权限标识码
    private final int PHOTO_REQUEST_CODE = 0; // 图库选择图片返回标识码
    private static final int REQUEST_IMAGE_CAPTURE = 1; // 相机拍照返回标识码
    private AlertDialog mDialog; // 权限未授权弹窗
    private Button usb_camera, camera, gallery;
    private TextView textView; // Python数据处理显示/正在处理数据显示
    private ImageView imageView;
    private Python py;
    private LoadingUtil loadingUtil; // 简易loading
    public static Bitmap bitmap = null; // USB相机bitmap
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 动态申请权限
        checkPermission();
        // 初始化Python环境
        initPython();
        py = Python.getInstance();

        // 初始化View与Class
        usb_camera = findViewById(R.id.usb_camera);
        camera = findViewById(R.id.camera);
        gallery = findViewById(R.id.gallery);
        textView = findViewById(R.id.text);
        imageView = findViewById(R.id.image);
        loadingUtil = new LoadingUtil(this);

        // 调转USB相机拍照界面
        usb_camera.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });
        // 调转图库选择图片
        gallery.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent,PHOTO_REQUEST_CODE);
            }
        });
        // 调用相机拍照
        camera.setOnClickListener(view -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            }
        });
    }
    public String saveBitmap(Bitmap bitmap){
        try {
            final String picPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/ep_camera/";
            File f_path = new File(picPath);
            if (!f_path.exists()) {
                f_path.mkdirs();
            }
            String path = picPath + System.currentTimeMillis() + ".jpg";
            FileOutputStream fos = new FileOutputStream(path);//图片保存路径
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);//压缩格式，质量，压缩路径
            showToast("图像保存成功");fos.close();
            return path;
        } catch (Exception e) {
            showToast("保存图片失败：" + e.getMessage());
            return null;
        }
    }
    // 调用Python进行图像处理
    public void python(final String path){
        textView.setText(path);
        loadingUtil.setText(path);
        loadingUtil.showLoadingImage();
        loadingUtil.dismissLoadingButton();
        loadingUtil.show();
        new Thread(() -> {
            try {
                // 访问Python函数并传入数据
                PyObject obj4 = py.getModule("youzhi").callAttr("get_youzhi", path);
                // 返回数据解析为Java对象
                final YouZhi data = obj4.toJava(YouZhi.class);
                runOnUiThread(() -> {
                    textView.setText(data.toString());
                    loadingUtil.dismiss();
                });
            } catch (Exception e) {
                loadingUtil.setText(e.getMessage());
                loadingUtil.dismissLoadingImage();
                loadingUtil.showLoadingButton();
            }
        }).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case PHOTO_REQUEST_CODE: // 图库选择
                if(resultCode == RESULT_OK){
                    Uri uri = data.getData();
                    //通过uri的方式返回
                    try {
                        //通过uri获取到bitmap对象
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        imageView.setImageBitmap(bitmap);
                        String path = saveBitmap(bitmap);
                        if (path != null){
                            python(path);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        showToast(e.getMessage());
                    }

                }else {
                    showToast("未选择图像");
                }
                break;
            case REQUEST_IMAGE_CAPTURE: // 相机拍照
                if(resultCode == RESULT_OK){
                    Bitmap thumbnail = data.getParcelableExtra("data");
                    imageView.setImageBitmap(thumbnail);
                    String path = saveBitmap(thumbnail);
                    if (path != null){
                        python(path);
                    }
                }else {
                    showToast("未选择图像");
                }
            case SETTING_REQ_PERMISSION_CODE: // 权限申请
                checkPermission();
                break;
            default:
                break;
        }
    }

    // 根据图片URI获取真实路径
    public String getPath(Uri uri){
        String[] proj = {MediaStore.Images.Media.DATA};
        //好像是android多媒体数据库的封装接口，具体的看Android文档
        Cursor cursor = managedQuery(uri, proj, null, null, null);
        //按我个人理解 这个是获得用户选择的图片的索引值
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        //将光标移至开头 ，这个很重要，不小心很容易引起越界
        cursor.moveToFirst();
        //最后根据索引值获取图片路径
        return cursor.getString(column_index);
    }

    private void showToast(String text){
        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
    // 初始化Python环境
    void initPython(){
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }
    }
    // 调用python代码示例
    void callPythonCode(){
        Python py = Python.getInstance();
        // 调用hello.py模块中的greet函数，并传一个参数
        // 等价用法：py.getModule("hello").get("greet").call("Android");
        py.getModule("hello").callAttr("greet", "Android");

        // 调用python内建函数help()，输出了帮助信息
        py.getBuiltins().get("help").call();

        PyObject obj1 = py.getModule("hello").callAttr("add", 2,3);
        // 将Python返回值换为Java中的Integer类型
        Integer sum = obj1.toJava(Integer.class);
        Log.d(TAG,"add = "+sum.toString());

        // 调用python函数，命名式传参，等同 sub(10,b=1,c=3)
        PyObject obj2 = py.getModule("hello").callAttr("sub", 10,new Kwarg("b", 1), new Kwarg("c", 3));
        Integer result = obj2.toJava(Integer.class);
        Log.d(TAG,"sub = "+result.toString());

        // 调用Python函数，将返回的Python中的list转为Java的list
        PyObject obj3 = py.getModule("hello").callAttr("get_list", 10,"xx",5.6,'c');
        List<PyObject> pyList = obj3.asList();
        Log.d(TAG,"get_list = "+pyList.toString());

        // 将Java的ArrayList对象传入Python中使用
        List<PyObject> params = new ArrayList<PyObject>();
        params.add(PyObject.fromJava("alex"));
        params.add(PyObject.fromJava("bruce"));
        py.getModule("hello").callAttr("print_list", params);

        // Python中调用Java类
        PyObject obj4 = py.getModule("hello").callAttr("get_java_bean");
        JavaBean data = obj4.toJava(JavaBean.class);
        data.print();
    }

    // 处理USB相机拍照bitmap
    @Override
    protected void onResume() {
        super.onResume();
        if (bitmap != null){
            imageView.setImageBitmap(bitmap);
            String path = saveBitmap(bitmap);
            bitmap = null;
            python(path);
        }
    }

    // 动态权限申请
    private boolean checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissions = new ArrayList<>();
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA);
            }
            if (permissions.size() != 0) {
                ActivityCompat.requestPermissions(this,permissions.toArray(new String[0]), REQ_PERMISSION_CODE);
                return false;
            }
        }

        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    // 已授权
                    if (grantResults[i] == 0) {
                        continue;
                    }
                    // 防止重复弹窗
                    if (mDialog != null && mDialog.isShowing()) {
                        continue;
                    }
                    // 未授权
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i])) {
                        //选择禁止
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("授权");
                        builder.setMessage("需要允许授权才可使用");
                        builder.setPositiveButton("去允许", (dialog, id) -> {
                            checkPermission();
                        });
                        mDialog = builder.create();
                        mDialog.setCanceledOnTouchOutside(false);
                        mDialog.setCancelable(false);
                        mDialog.show();
                    }else {
                        // 选择禁止并勾选禁止后不再询问
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("授权");
                        builder.setMessage("需要允许授权才可使用");
                        builder.setPositiveButton("去授权", (dialog, id) -> {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            // 调起应用设置页面
                            startActivityForResult(intent, SETTING_REQ_PERMISSION_CODE);
                        });
                        mDialog = builder.create();
                        mDialog.setCanceledOnTouchOutside(false);
                        mDialog.setCancelable(false);
                        mDialog.show();
                    }

                }
                break;
            default:
                break;
        }
    }
}
