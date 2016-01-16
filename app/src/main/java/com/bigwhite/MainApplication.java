package com.bigwhite;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
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

    @Override
    public void onCreate()
    {
        Log.e("BigWhite","MainApplication onCreate");
        super.onCreate();

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();

        if (width < height)
        {
            SCREEN_HEIGHT = width;
            SCREEN_WIDTH = height;
        }
        else
        {
            SCREEN_WIDTH = width;
            SCREEN_HEIGHT = height;
        }
        Log.e("BigWhite","The Screen Size : " + SCREEN_HEIGHT+" "+SCREEN_WIDTH);
        CANVAS_WIDTH = MainApplication.SCREEN_WIDTH;
        CANVAS_HEIGHT = (int) (MainApplication.SCREEN_WIDTH / scale);
    }
    //生成新的bitmap
    public static Bitmap createBitmap(int width, int height)
    {
        Bitmap bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Log.e("BigWhite","createBitmap width:"+width+"  height:"+height);
        return bitmap;
    }

}
