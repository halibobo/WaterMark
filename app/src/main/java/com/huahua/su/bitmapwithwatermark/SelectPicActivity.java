package com.huahua.su.bitmapwithwatermark;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.huahua.su.bitmapwithwatermark.util.FileUtil;

import java.io.File;
import java.util.Date;

/**
 *
 */
public class SelectPicActivity extends Activity implements View.OnClickListener{


    /**
     * 使用照相机拍照获取图片
     */
    public static final int SELECT_PIC_BY_TACK_PHOTO = 1;
    /**
     * 使用相册中的图片
     */
    public static final int SELECT_PIC_BY_PICK_PHOTO = 2;
    public static final String KEY_PHOTO_PATH = "photo_path";
    private static final String FILE_PATH = "file_path";
    private Uri photoUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_select_pic);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * 初始化加载View
     */
    private void initView() {
        TextView tvTakePhoto = (TextView) findViewById(R.id.tv_take_photo);
        TextView tvPhonePic = (TextView) findViewById(R.id.tv_phone_pic);
        tvTakePhoto.setOnClickListener(this);
        tvPhonePic.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_take_photo:
                takePhoto();
                break;
            case R.id.tv_phone_pic:
                pickPhoto();
                break;
            default:
                finish();
                break;
        }
    }

    /**
     * 拍照获取图片
     */
    private void takePhoto() {
        //执行拍照前，应该先判断SD卡是否存在
        String SDState = Environment.getExternalStorageState();
        if (SDState.equals(Environment.MEDIA_MOUNTED)) {
            try {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//"android.media.action.IMAGE_CAPTURE"
                File file = new File(FileUtil.getInstance().getImageFile(new Date().getTime() + ""));
                if (!file.exists()) {
                    file.mkdirs();
                }
                photoUri = Uri.fromFile(file);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                intent.putExtra(FILE_PATH, file.getAbsoluteFile());
                startActivityForResult(intent, SELECT_PIC_BY_TACK_PHOTO);
                Log.v("tag", "filepath = " + file.getAbsoluteFile());
            } catch (Exception e) {
                Toast.makeText(this, getString(R.string.no_camera_power), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.no_sd_card), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 从相册中取图片
     */
    private void pickPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, SELECT_PIC_BY_PICK_PHOTO);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return super.onTouchEvent(event);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            doPhoto(requestCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 选择图片后，获取图片的路径
     *
     * @param requestCode
     * @param data
     */
    private void doPhoto(int requestCode, Intent data) {

        /**
         * 获取到的图片路径
         */
        String picPath = null;

        if (requestCode == SELECT_PIC_BY_PICK_PHOTO) {
            //if(true){  //从相册取图片，有些手机有异常情况，请注意
            if (data == null) {
                Toast.makeText(this, getString(R.string.chose_imagefile_error), Toast.LENGTH_LONG).show();
                return;
            }
            photoUri = data.getData();
            if (photoUri == null) {
                Toast.makeText(this, getString(R.string.chose_imagefile_error), Toast.LENGTH_LONG).show();
                return;
            }
            Log.v("tag", "photoUri = " + photoUri);
        }

        String[] pojo = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(photoUri, pojo, null, null, null);
        if (cursor != null) {
            int columnIndex = cursor.getColumnIndexOrThrow(pojo[0]);
            cursor.moveToFirst();
            picPath = cursor.getString(columnIndex);
            cursor.close();
        } else {   //小米4上取不到图片路径
            try {
                String[] strPaths = photoUri.toString().split("//");
                picPath = strPaths[1];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (picPath != null && (picPath.toLowerCase().endsWith(".png") || picPath.toLowerCase().endsWith(".jpg"))) {
            Intent lastIntent = getIntent();
            lastIntent.putExtra(KEY_PHOTO_PATH, picPath);
            setResult(Activity.RESULT_OK, lastIntent);
            Log.v("tag", "picPath = " + picPath);
            finish();
        } else {
            Toast.makeText(this, getString(R.string.chose_imagefile_error), Toast.LENGTH_LONG).show();
        }
    }
}
