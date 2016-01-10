package com.bigwhite;

import android.app.Application;
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
    }
    //生成新的bitmap
    public static Bitmap createBitmap(int width, int height)
    {
        Bitmap bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
        Log.e("BigWhite","createBitmap success");
        return bitmap;
    }

}
