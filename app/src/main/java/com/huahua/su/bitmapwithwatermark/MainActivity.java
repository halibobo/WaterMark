package com.huahua.su.bitmapwithwatermark;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.huahua.su.bitmapwithwatermark.util.FileUtil;

public class MainActivity extends AppCompatActivity {

    private final int requestCode = 0x101;
    private ImageView imageView;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        imageView = (ImageView) findViewById(R.id.imageShow);
        editText = (EditText) findViewById(R.id.editText);
        findViewById(R.id.btnPhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SelectPicActivity.class);
                startActivityForResult(intent, requestCode);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == this.requestCode) {
                String picPath;
                picPath = data.getStringExtra(SelectPicActivity.KEY_PHOTO_PATH);
                if (picPath == null || picPath.equals("")) {
                    Toast.makeText(this, R.string.no_choosed_pic, Toast.LENGTH_SHORT).show();
                    return;
                }
                Bitmap bm = null;
                try {
                     bm = FileUtil.getInstance().getImage(picPath,imageView.getWidth(),imageView.getHeight()); //获取限定宽高的bitmap，不限定则容易占用内存过大及OOM
                    if (bm == null) {
                        Toast.makeText(this, R.string.no_choosed_pic, Toast.LENGTH_SHORT).show();
                    }else{
                        if (addWatermarkBitmap(bm, editText.getText().toString())) {
                            Toast.makeText(this, "水印生成成功，文件已保存在 " + FileUtil.getInstance().IMAGE_PATH, Toast.LENGTH_SHORT).show();
                        }
                    }
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    if (bm != null) {
                        bm.recycle();
                    }
                    System.gc();
                }
            }
        }
    }

    private boolean addWatermarkBitmap(Bitmap bitmap,String str) {
        int destWidth = bitmap.getWidth();   //此处的bitmap已经限定好宽高
        int destHeight = bitmap.getHeight();
        Bitmap icon = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888); //定好宽高的全彩bitmap
        Canvas canvas = new Canvas(icon);//初始化画布绘制的图像到icon上

        Paint photoPaint = new Paint(); //建立画笔
        photoPaint.setDither(true); //获取跟清晰的图像采样
        photoPaint.setFilterBitmap(true);//过滤一些

        Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());//创建一个指定的新矩形的坐标
        Rect dst = new Rect(0, 0, destWidth, destHeight);//创建一个指定的新矩形的坐标
        canvas.drawBitmap(bitmap, src, dst, photoPaint);//将photo 缩放或则扩大到 dst使用的填充区photoPaint

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);//设置画笔
        textPaint.setTextSize(destWidth/20);//字体大小
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);//采用默认的宽度
        textPaint.setAntiAlias(true);  //抗锯齿
        textPaint.setStrokeWidth(3);
        textPaint.setAlpha(15);
        textPaint.setStyle(Paint.Style.STROKE); //空心
        textPaint.setColor(Color.WHITE);//采用的颜色
        textPaint.setShadowLayer(1f, 0f, 3f, Color.LTGRAY);
//        textPaint.setShadowLayer(3f, 1, 1,getResources().getColor(android.R.color.white));//影音的设置
        canvas.drawText(str, destWidth/2, destHeight-45, textPaint);//绘制上去字，开始未知x,y采用那只笔绘制
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
        imageView.setImageBitmap(icon);
        return FileUtil.getInstance().saveMyBitmap(icon); //保存至文件
    }
}
