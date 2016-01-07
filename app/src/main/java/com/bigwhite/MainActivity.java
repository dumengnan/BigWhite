package com.bigwhite;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends Activity {

    private static final int REQ_SEL_PAGE = 0;
    private PopupWindow mPopupWindow;
    private LayoutInflater mLayoutInflater;

    private View mToolbox;
    private ImageView mColorWhite;
    private ImageView mColorBlue;
    private ImageView mColorGreen;
    private ImageView mColorRed;
    private ImageView mColorYellow;
    private ImageView mColor;
    private ImageView mEdit;
    private ImageView mEraser;
    private ImageView mClear;
    private ImageView mSettings;
    private ImageView mPage;
    private ImageView mNewPage;
    private ImageView mShape;
    private ImageView mUndo;
    private ImageView mRedo;
    private Handler mHandler;

    private ImageView paintArea;
    private int inColor;
    private float inPenWidth;
    private DrawView drawView;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;

    float startX;
    float startY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("BigWhite", "MainActivity oncreate");
        setContentView(R.layout.activity_main);

        initView();//界面相关组件初始化
        initPenSize();//画笔宽度设置监听初始化
        initClear(); //一键清除功能初始化
        initNewPage();
        initColor();//颜色选择初始化
     //   initEraser();
    }
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        //根据之前选择的笔迹宽度，以及笔迹颜色进行处理
        paint.setStrokeWidth(drawView.getmPenWidth());
        paint.setColor(Color.BLACK);

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if(baseBitmap == null)
                {
                    baseBitmap = Bitmap.createBitmap(paintArea.getWidth(),
                            paintArea.getHeight(),Bitmap.Config.ARGB_8888);
                    canvas = new Canvas(baseBitmap);
                    canvas.drawColor(Color.WHITE);
                }
                startX = event.getX();
                startY = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                float stopX = event.getX();
                float stopY = event.getY();

                canvas.drawLine(startX,startY,stopX,stopY,paint);

                startX = event.getX();
                startY = event.getY();

                paintArea.setImageBitmap(baseBitmap);
                break;

            case MotionEvent.ACTION_UP:
                break;
            default:
                break;

        }
        return true;
    }
    @Override
    protected void onPause()
    {
        super.onPause();
    }
    private void initView()
    {
        //获得xml文件里定义的view
        mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        paintArea = (ImageView) findViewById(R.id.paint_area);

        //产生一个对话框，里面存放要选择的笔迹宽度以及笔迹颜色等
        mPopupWindow = new PopupWindow(new View(this));
        mPopupWindow.setFocusable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());

        //获取笔迹宽度的view对象
        mEdit = (ImageView) findViewById(R.id.edit);

        mEraser = (ImageView) findViewById(R.id.eraser);

        mToolbox = findViewById(R.id.toolbox);

        mClear = (ImageView) findViewById(R.id.clear);

        drawView = new DrawView(this);
        paint = new Paint();

    }
    private void initPenSize()
    {
        final int POP_WINDOW_WIDTH = WindowManager.LayoutParams.WRAP_CONTENT;
        final int POP_WINDOW_HEIGHT = (int) (getResources().getDisplayMetrics().density *60 + 0.5f);
        final View popupView = mLayoutInflater.inflate(R.layout.view_popup_pen,null);
        final View width1 = popupView.findViewById(R.id.pen_width1);
        final View width2 = popupView.findViewById(R.id.pen_width2);
        final View width3 = popupView.findViewById(R.id.pen_width3);

        mEdit.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                mEdit.setSelected(true);  //将笔迹设为真
                mEraser.setSelected(false);//将橡皮擦置为假

                mPopupWindow.setContentView(popupView); //将笔迹宽度放入对话框中
                mPopupWindow.setWidth(POP_WINDOW_WIDTH);
                mPopupWindow.setHeight(POP_WINDOW_HEIGHT);

                mPopupWindow.setAnimationStyle(R.style.pop_settings);
                mPopupWindow.showAtLocation(mEdit, Gravity.LEFT | Gravity.TOP, mToolbox.getRight(), mToolbox.getTop()
                        + mEdit.getTop() - (int) (getResources().getDisplayMetrics().density * 5 + 0.5f));

                float penSize = drawView.getmPenWidth(); //默认的笔迹宽度1f

                if (penSize == 1) {
                    width1.setSelected(true);
                    width2.setSelected(false);
                    width3.setSelected(false);
                } else if (penSize == 3) {
                    width2.setSelected(true);
                    width1.setSelected(false);
                    width3.setSelected(false);
                } else if (penSize == 6) {
                    width3.setSelected(true);
                    width1.setSelected(false);
                    width2.setSelected(false);
                }
            }
        });
        //监听选中的笔迹宽度
        width1.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
            drawView.setmPenWidth(1f);
            mPopupWindow.dismiss();
            mEdit.setSelected(false);
        }
    });
        width2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawView.setmPenWidth(3f);
                Log.e("BigWhite","selected 3f pen size");
                mPopupWindow.dismiss();
                mEdit.setSelected(false);
            }
        });
        width3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawView.setmPenWidth(6f);
                Log.e("BigWhite","selected 6f pen size");
                mPopupWindow.dismiss();
                mEdit.setSelected(false);
            }
        });
    }

    private void initClear()
    {
        mClear.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramView)
            {
                mClear.setSelected(true);
                if(baseBitmap != null)
                {
                    baseBitmap = Bitmap.createBitmap(paintArea.getWidth(),
                            paintArea.getHeight(), Bitmap.Config.ARGB_8888);

                    canvas = new Canvas(baseBitmap);
                    canvas.drawColor(Color.WHITE);
                    paintArea.setImageBitmap(baseBitmap);
                }
                mClear.setSelected(false);
            }
        });
    }

    private void initColor()
    {
        final int POP_WINDOW_WIDTH = WindowManager.LayoutParams.WRAP_CONTENT;
        final int POP_WINDOW_HEIGHT = (int) (getResources().getDisplayMetrics().density*60 + 0.5f);
        final View popupView = mLayoutInflater.inflate(R.layout.view_color_popup, null);
        final View colorFrame = findViewById(R.id.color_frame);

        colorFrame.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramView)
            {
                mPopupWindow.setContentView(popupView);
                mPopupWindow.setWidth(POP_WINDOW_WIDTH);
                mPopupWindow.setHeight(POP_WINDOW_HEIGHT);
                mPopupWindow.setAnimationStyle(R.style.pop_settings);
                mPopupWindow.showAtLocation(colorFrame, Gravity.LEFT | Gravity.TOP, mToolbox.getRight(),
                        mToolbox.getTop() + colorFrame.getTop() - (int) (getResources().getDisplayMetrics().density*5 + 0.5f));
            }
        });
    }

    private void initNewPage()
    {

    }
}