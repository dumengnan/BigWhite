package com.bigwhite;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;


/**
 * Created by dell on 2016/1/6 0006.
 */
public class DrawView extends View {

    protected float mPenWidth = 1f;

    public float getmPenWidth() {
        return mPenWidth;
    }

    public void setmPenWidth(float mPenWidth) {
        this.mPenWidth = mPenWidth;
    }

    public DrawView(Context context, AttributeSet attribset) {
        super(context, attribset);
    }

    public DrawView(Context context)
    {
        super(context);
    }


}
