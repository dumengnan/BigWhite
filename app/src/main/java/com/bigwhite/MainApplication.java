package com.bigwhite;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by dell on 2016/1/5 0005.
 */
public class MainApplication extends Application {
    public static final float scale = 210f / 297f;
    public static int CANVAS_WIDTH;
    public static int CANVAS_HEIGHT;

    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;

    private static Bitmap curBitmap;

    private static Bitmap sourceBitmap;
    private static String curName;

    @Override
    public void onCreate()
    {
        Log.e("BigWhite","MainApplication onCreate");
        super.onCreate();

        Display display = ((WindowManager) getSystemService("window")).getDefaultDisplay();

        if (display.getWidth() < display.getHeight())
        {
            SCREEN_HEIGHT = display.getWidth();
            SCREEN_WIDTH = display.getHeight();
        }
        else
        {
            SCREEN_WIDTH = display.getWidth();
            SCREEN_HEIGHT = display.getHeight();
        }
        Log.e("BigWhite","The Screen Size : " + SCREEN_HEIGHT+" "+SCREEN_WIDTH);
        CANVAS_WIDTH = MainApplication.SCREEN_WIDTH;
        CANVAS_HEIGHT = (int) (MainApplication.SCREEN_WIDTH / scale);

       // createPage(); //创建新的画布.
    }
    public static String getBitmapName()
    {
        return curName;
    }

    public static void setBitmapName(String name)
    {
        curName = name;
    }

    public static Bitmap getBitmap()
    {
        return curBitmap;
    }

    //生成新的bitmap
    public static Bitmap createBitmap(int width, int height)
    {
        Bitmap bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        //bitmap.eraseColor(Color.WHITE);
        Log.e("BigWhite","createBitmap success");
        return bitmap;
    }

    //新增页面， 含有背景图片的
    public static Bitmap addNewPage(Bitmap backImg)
    {
        if(sourceBitmap != null)
        {
            sourceBitmap.recycle();
            sourceBitmap = null;
        }

        //新增
      //  sourceBitmap = createBitmap();
        Canvas canvas = new Canvas(sourceBitmap);
        canvas.drawBitmap(backImg, 0, 0, null);

        curBitmap = Bitmap.createBitmap(sourceBitmap);

        return curBitmap;
    }
    //新增页面， 默认背景的
    public static Bitmap addNewPage()
    {
        if (sourceBitmap != null) {
            sourceBitmap.recycle();
            sourceBitmap = null;
        }
       // curBitmap = createBitmap();
        return curBitmap;
    }

}
