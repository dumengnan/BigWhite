package com.bigwhite;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int FILE_RESULT_CODE = 1;
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
    private ImageView mNewPage;

    private ImageView paintArea;
    private DrawView drawView;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;

    private Xfermode fermode;

    float startX;
    float startY;

    private Drawable drawable;
    private Bitmap  image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("BigWhite", "MainActivity oncreate");
        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawableResource(R.drawable.ic_eraser);
        initView();//界面相关组件初始化
        initPenSize();//画笔宽度设置监听初始化
        initClear(); //一键清除功能初始化
        initNewPage();//加载背景（eg.word ppt pdf）
        initColor();//颜色选择初始化
        initEraser();
    }

    public static Drawable BitmapConvertToDrawale(Bitmap bitmap) {
// Bitmap bitmap = new Bitmap();
        Drawable drawable = new BitmapDrawable(bitmap);
        return drawable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        //根据之前选择的笔迹宽度，以及笔迹颜色进行处理
        paint.setStrokeWidth(drawView.getmPenWidth());

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                if(baseBitmap == null)
                {
                    baseBitmap = Bitmap.createBitmap(paintArea.getWidth(),
                            paintArea.getHeight(),Bitmap.Config.ARGB_8888);
                    baseBitmap.eraseColor(Color.TRANSPARENT);
                    canvas = new Canvas(baseBitmap);
                    canvas.drawColor(Color.TRANSPARENT);
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

        mColor = (ImageView) findViewById(R.id.color);//颜色框

        mNewPage = (ImageView) findViewById(R.id.new_page); //加载背景ppt word pdf

        drawView = new DrawView(this);

        paint = new Paint();
        fermode=paint.getXfermode();

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
                mEdit.setSelected(true);  //设置当前点击的按钮变色
                mEraser.setSelected(false);
                mClear.setSelected(false);
                mNewPage.setSelected(false);

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
            paint.setColor(drawView.getmColor());
            paint.setAlpha(255);
            paint.setXfermode(fermode);
            mPopupWindow.dismiss();
        }
    });
        width2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawView.setmPenWidth(3f);
                paint.setColor(drawView.getmColor());
                paint.setAlpha(255);
                paint.setXfermode(fermode);
                mPopupWindow.dismiss();
            }
        });
        width3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawView.setmPenWidth(6f);
                paint.setColor(drawView.getmColor());
                paint.setAlpha(255);
                paint.setXfermode(fermode);
                mPopupWindow.dismiss();
            }
        });
    }

    private void initClear()
    {
        mClear.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramView)
            {
                mClear.setSelected(true); //设置当前点击的按钮变色
                mEdit.setSelected(false);
                mEraser.setSelected(false);
                mNewPage.setSelected(false);

                if(baseBitmap != null)
                {
                    baseBitmap = Bitmap.createBitmap(paintArea.getWidth(),
                            paintArea.getHeight(), Bitmap.Config.ARGB_8888);

                    baseBitmap.eraseColor(Color.TRANSPARENT);
                    canvas = new Canvas(baseBitmap);
                    canvas.drawColor(Color.TRANSPARENT);
                    paintArea.setImageBitmap(baseBitmap);
                }
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
                mEraser.setSelected(false);
                mEdit.setSelected(false);
                mClear.setSelected(false);
                mNewPage.setSelected(false);

                mPopupWindow.setContentView(popupView);
                mPopupWindow.setWidth(POP_WINDOW_WIDTH);
                mPopupWindow.setHeight(POP_WINDOW_HEIGHT);
                mPopupWindow.setAnimationStyle(R.style.pop_settings);
                mPopupWindow.showAtLocation(colorFrame, Gravity.LEFT | Gravity.TOP, mToolbox.getRight(),
                        mToolbox.getTop() + colorFrame.getTop() - (int) (getResources().getDisplayMetrics().density * 5 + 0.5f));
            }
        });

        mColorWhite = (ImageView) popupView.findViewById(R.id.color_white);
        mColorWhite.setBackgroundColor(Color.WHITE);
        mColorBlue = (ImageView) popupView.findViewById(R.id.color_blue);
        mColorBlue.setBackgroundColor(Color.BLUE);
        mColorGreen = (ImageView) popupView.findViewById(R.id.color_green);
        mColorGreen.setBackgroundColor(Color.GREEN);
        mColorRed = (ImageView) popupView.findViewById(R.id.color_red);
        mColorRed.setBackgroundColor(Color.RED);
        mColorYellow = (ImageView) popupView.findViewById(R.id.color_yellow);
        mColorYellow.setBackgroundColor(Color.YELLOW);

        mColorWhite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawView.setmColor(Color.WHITE);
                paint.setColor(drawView.getmColor());
                mPopupWindow.dismiss();
                mColor.setBackgroundColor(Color.WHITE);
            }
        });
        mColorBlue.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                drawView.setmColor(Color.BLUE);
                paint.setColor(drawView.getmColor());
                mPopupWindow.dismiss();
                mColor.setBackgroundColor(Color.BLUE);
            }
        });
        mColorGreen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawView.setmColor(Color.GREEN);
                paint.setColor(drawView.getmColor());
                mPopupWindow.dismiss();
                mColor.setBackgroundColor(Color.GREEN);
            }
        });
        mColorRed.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                drawView.setmColor(Color.RED);
                paint.setColor(drawView.getmColor());
                mPopupWindow.dismiss();
                mColor.setBackgroundColor(Color.RED);
            }
        });
        mColorYellow.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawView.setmColor(Color.YELLOW);
                paint.setColor(drawView.getmColor());
                mPopupWindow.dismiss();
                mColor.setBackgroundColor(Color.YELLOW);
            }
        });
    }

    private void initNewPage()
    {
        mNewPage.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View paramView)
            {
                mNewPage.setSelected(true);
                mClear.setSelected(false);
                mEdit.setSelected(false);
                mEraser.setSelected(false);

                //加载文件管理器界面
                Log.e("BigWhite","list file from sdcard to choose");
                Intent intent = new Intent(MainActivity.this,FileManagerActivity.class);
                startActivityForResult(intent,FILE_RESULT_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(FILE_RESULT_CODE == requestCode){
            Bundle bundle = null;
            if(data!=null&&(bundle=data.getExtras())!=null){
                Log.e("BigWhite","select foleder file name is " + bundle.getString("file"));
               // textView.setText("选择文件夹为："+bundle.getString("file"));
                String openedPdfFileName;
                OpenPdf openPdf = new OpenPdf(paintArea.getWidth(),paintArea.getHeight());

                openedPdfFileName = bundle.getString("file");
                if(openedPdfFileName != null){

                    Log.e("BigWhite", "open pdf file is " + openedPdfFileName);

                    if(resultCode == 2){
                        System.out.println("选择文件为：" + openedPdfFileName);
                        openPdf.openRenderer(openedPdfFileName);

                        if(openPdf.pdfBitmap != null)
                        {
                            baseBitmap = openPdf.pdfBitmap;
                            canvas = new Canvas(baseBitmap);
                            paintArea.setImageBitmap(openPdf.pdfBitmap);
                        }
                    }else if(resultCode == 3){
                        System.out.println("选择图片为：" + openedPdfFileName);
                        openPdf.openPicture(openedPdfFileName,paintArea.getWidth(),paintArea.getHeight());

                        drawable = BitmapConvertToDrawale(openPdf.image);
                        getWindow().setBackgroundDrawable(drawable);

                        //baseBitmap = openPdf.image;
                        //canvas = new Canvas(baseBitmap);
                        //paintArea.setImageBitmap(openPdf.image);
                    }
                }else {
                    Toast.makeText(getApplicationContext(),
                            "No pdf file found, Please create new Pdf file",
                            Toast.LENGTH_LONG).show();
                    //updateView(CurrentView.OPTIONS_LAYOUT);
                   // updateActionBarText();
                }
            }
        }
    }

    private void initEraser()
    {
        final int POP_WINDOW_WIDTH = WindowManager.LayoutParams.WRAP_CONTENT;
        final int POP_WINDOW_HEIGHT = (int) (getResources().getDisplayMetrics().density *60 + 0.5f);
        final View eraser_popupView = mLayoutInflater.inflate(R.layout.view_popup_eraser,null);
        final View eraser_width1 = eraser_popupView.findViewById(R.id.eraser_width1);
        final View eraser_width2 = eraser_popupView.findViewById(R.id.eraser_width2);
        final View eraser_width3 = eraser_popupView.findViewById(R.id.eraser_width3);

        mEraser.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                mEraser.setSelected(true);//设置当前点击的按钮变色
                mEdit.setSelected(false);
                mClear.setSelected(false);
                mNewPage.setSelected(false);

                mPopupWindow.setContentView(eraser_popupView); //将橡皮宽度放入对话框中
                mPopupWindow.setWidth(POP_WINDOW_WIDTH);
                mPopupWindow.setHeight(POP_WINDOW_HEIGHT);

                mPopupWindow.setAnimationStyle(R.style.pop_settings);
                mPopupWindow.showAtLocation(mEraser, Gravity.LEFT | Gravity.TOP, mToolbox.getRight(), mToolbox.getTop()
                        + mEraser.getTop() - (int) (getResources().getDisplayMetrics().density * 5 + 0.5f));

                float eraserSize = drawView.getmPenWidth(); //默认的橡皮擦宽度1

                if (eraserSize == 1) {
                    eraser_width1.setSelected(true);
                    eraser_width2.setSelected(false);
                    eraser_width3.setSelected(false);
                } else if (eraserSize == 3) {
                    eraser_width1.setSelected(false);
                    eraser_width2.setSelected(true);
                    eraser_width3.setSelected(false);
                } else if (eraserSize == 6) {
                    eraser_width1.setSelected(false);
                    eraser_width2.setSelected(false);
                    eraser_width3.setSelected(true);
                }
            }
        });
        //监听选中的橡皮擦宽度
        eraser_width1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawView.setmPenWidth(1f);
                mEraser.setSelected(true);
                paint.setAlpha(0);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                mPopupWindow.dismiss();
            }
        });
        eraser_width2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawView.setmPenWidth(3f);
                Log.e("BigWhite", "selected 3f pen size");
                mEraser.setSelected(true);
                paint.setAlpha(0);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                mPopupWindow.dismiss();
            }
        });
        eraser_width3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawView.setmPenWidth(6f);
                Log.e("BigWhite", "selected 6f pen size");
                mEraser.setSelected(true);
                paint.setAlpha(0);
                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
                mPopupWindow.dismiss();
            }
        });

    }
}