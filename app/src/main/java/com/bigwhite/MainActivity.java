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
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    private ImageView mColorBlack;
    private ImageView mColor;
    private ImageView mEdit;
    private ImageView mEraser;
    private ImageView mClear;
    private ImageView mNewPage;
    private ImageView pageup;
    private ImageView pagedown;
    private TextView pagenumber;
    private ImageView pageclose;
    private ImageView savepage;


    private ImageView paintArea;
    private DrawView drawView;
    private Bitmap baseBitmap;
    private Canvas canvas;
    private Paint paint;

    private Xfermode fermode;

    float startX;
    float startY;

    private Drawable drawable;
    private OpenPdf openPdf;
    private ArrayList<Bitmap> drawList = new ArrayList<Bitmap>();
    private int type = 1; //标识当前打开的背景，1标识图片(默认)  2标识PDF
    LinearLayout toolbox, toolbox2;

    private int screen_width;
    private int screen_height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("BigWhite", "MainActivity oncreate");
        setContentView(R.layout.activity_main);

        initView();//界面相关组件初始化
        initPenSize();//画笔宽度设置监听初始化
        initClear(); //一键清除功能初始化
        initNewPage();//加载背景（eg.word ppt pdf）
        initColor();//颜色选择初始化
        initEraser();//橡皮擦初始化
        initpageup();//初始化pdf上一页
        initpagedown();//初始化pdf下一页
        initsavepage();//初始化保存按键
        initPageClose();//返回初始状态
    }

    public static Drawable BitmapConvertToDrawale(Bitmap bitmap) {
        Drawable drawable = new BitmapDrawable(bitmap);
        return drawable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //根据之前选择的笔迹宽度，以及笔迹颜色进行处理
        paint.setStrokeWidth(drawView.getmPenWidth());

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (baseBitmap == null) {
                    //Log.e("BigWihte","basebit 为空");
                    baseBitmap = Bitmap.createBitmap(screen_width, screen_height, Bitmap.Config.ARGB_8888);
                    baseBitmap.eraseColor(Color.TRANSPARENT);
                    canvas = new Canvas(baseBitmap);
                    canvas.drawColor(Color.TRANSPARENT);
                }
                //Log.e("BigWihte","basebit 不为空");
                startX = event.getX();
                startY = event.getY();
                break;

            case MotionEvent.ACTION_MOVE:
                //Log.e("BigWihte","移动");
                float stopX = event.getX();
                float stopY = event.getY();

                canvas.drawLine(startX, startY, stopX, stopY, paint);

                startX = event.getX();
                startY = event.getY();

                paintArea.setImageBitmap(baseBitmap);
                break;

            case MotionEvent.ACTION_UP:
                //Log.e("BigWihte","放开");
                break;
            default:
                break;

        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    //初始化界面，获得图标的ID
    private void initView() {
        //获得xml文件里定义的view
        mLayoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        paintArea = (ImageView) findViewById(R.id.paint_area);
        toolbox = (LinearLayout) findViewById(R.id.toolbox);
        toolbox2 = (LinearLayout) findViewById(R.id.toolbox2);

        Display display = getWindowManager().getDefaultDisplay();
        screen_width = display.getWidth();
        screen_height = display.getHeight();
        //创建起始白色背景图片
        Bitmap bitmap = MainApplication.createBitmap(screen_width, screen_height);
        bitmap.eraseColor(Color.WHITE);
        drawable = BitmapConvertToDrawale(bitmap);
        getWindow().setBackgroundDrawable(drawable);


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
        pagenumber = (TextView) findViewById(R.id.pagenumber);
        pageup = (ImageView) findViewById(R.id.undo);
        pagedown = (ImageView) findViewById(R.id.redo);
        savepage = (ImageView) findViewById(R.id.save_page);
        pageclose = (ImageView) findViewById(R.id.page);

        // 获取屏幕宽和高
        openPdf = new OpenPdf(screen_width, screen_height);

        drawView = new DrawView(this);
        paint = new Paint();
        fermode = paint.getXfermode();

    }

    //初始化画笔的宽度
    private void initPenSize() {
        final int POP_WINDOW_WIDTH = WindowManager.LayoutParams.WRAP_CONTENT;
        final int POP_WINDOW_HEIGHT = (int) (getResources().getDisplayMetrics().density * 60 + 0.5f);
        final View popupView = mLayoutInflater.inflate(R.layout.view_popup_pen, null);
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

    //初始化清除图标
    private void initClear() {
        mClear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                mClear.setSelected(true); //设置当前点击的按钮变色
                mEdit.setSelected(false);
                mEraser.setSelected(false);
                mNewPage.setSelected(false);

                if (baseBitmap != null) {
                    baseBitmap = null;
                    baseBitmap = Bitmap.createBitmap(screen_width, screen_height, Bitmap.Config.ARGB_8888);
                    baseBitmap.eraseColor(Color.TRANSPARENT);
                    canvas = new Canvas(baseBitmap);
                    canvas.drawColor(Color.TRANSPARENT);
                    paintArea.setImageBitmap(baseBitmap);
                }
                mClear.setSelected(false);
                Log.e("Bigwhite", "清除成功");
            }
        });
    }

    //初始化颜色图标
    private void initColor() {
        final int POP_WINDOW_WIDTH = WindowManager.LayoutParams.WRAP_CONTENT;
        final int POP_WINDOW_HEIGHT = (int) (getResources().getDisplayMetrics().density * 60 + 0.5f);
        final View popupView = mLayoutInflater.inflate(R.layout.view_color_popup, null);
        final View colorFrame = findViewById(R.id.color_frame);

        colorFrame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
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
        mColorBlack = (ImageView) popupView.findViewById(R.id.color_black);
        mColorBlack.setBackgroundColor(Color.BLACK);

        mColorWhite.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawView.setmColor(Color.WHITE);
                paint.setColor(drawView.getmColor());
                mPopupWindow.dismiss();
                mColor.setBackgroundColor(Color.WHITE);
            }
        });
        mColorBlue.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
        mColorRed.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
        mColorBlack.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawView.setmColor(Color.BLACK);
                paint.setColor(drawView.getmColor());
                mPopupWindow.dismiss();
                mColor.setBackgroundColor(Color.BLACK);
            }
        });
    }

    //橡皮檫
    private void initEraser() {
        final int POP_WINDOW_WIDTH = WindowManager.LayoutParams.WRAP_CONTENT;
        final int POP_WINDOW_HEIGHT = (int) (getResources().getDisplayMetrics().density * 60 + 0.5f);
        final View eraser_popupView = mLayoutInflater.inflate(R.layout.view_popup_eraser, null);
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

    //返回白色背景界面
    private void initPageClose() {
        pageclose.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                Bitmap bitmap = MainApplication.createBitmap(MainApplication.SCREEN_WIDTH, MainApplication.SCREEN_HEIGHT);
                bitmap.eraseColor(Color.WHITE);
                drawable = BitmapConvertToDrawale(bitmap);
                getWindow().setBackgroundDrawable(drawable);

                mClear.performClick();
                closetools();
                drawList.clear();
                type = 1;
            }
        });
    }

    //初始化+号，打开文件管理器
    private void initNewPage() {
        mNewPage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                mNewPage.setSelected(true);
                mClear.setSelected(false);
                mEdit.setSelected(false);
                mEraser.setSelected(false);

                //加载文件管理器界面
                Log.e("BigWhite", "list file from sdcard to choose");
                Intent intent = new Intent(MainActivity.this, FileManagerActivity.class);
                startActivityForResult(intent, FILE_RESULT_CODE);
            }
        });
    }

    //向上翻页
    private void initpageup() {

        pageup.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {

                Bitmap temp = baseBitmap.copy(baseBitmap.getConfig(), true);
                int currentpage = openPdf.getCurrentPage();
                if (currentpage != 0) {
                    Log.e("BigWhite", "向上翻页");

                    if (currentpage == drawList.size()) {
                        drawList.add(temp);
                    } else {
                        drawList.set(currentpage, temp);
                    }
                    openPdf.pageup();
                    setBackground(openPdf.pdfBitmap);
                    currentpage = openPdf.getCurrentPage();
                    pagenumber.setText("" + (currentpage + 1));

                    Log.e("Bigwhite", "设置页数成功：" + (currentpage + 1));

                    baseBitmap = (Bitmap) drawList.get(currentpage);
                    canvas = new Canvas(baseBitmap);
                    canvas.drawColor(Color.TRANSPARENT);
                    paintArea.setImageBitmap(baseBitmap);
                    //paintArea.setImageBitmap((Bitmap)drawList.get(currentpage));
                    /*
                    if(drawList.size() > currentpage){
                        if(baseBitmap.equals((Bitmap)drawList.get(currentpage))){
                            Log.e("BigWihte","basebit 与缓存相等");
                            canvas = new Canvas(baseBitmap);
                            canvas.drawColor(Color.TRANSPARENT);
                        }
                    }
                    */
                }
            }
        });
    }

    //向下翻页
    private void initpagedown() {
        pagedown.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {

                Bitmap temp = baseBitmap.copy(baseBitmap.getConfig(), true);
                int currentpage = openPdf.getCurrentPage();
                int totalpage = openPdf.gettotalpage();
                if (currentpage < totalpage - 1) {
                    Log.e("Bigwhite", "向下翻页");

                    if (currentpage == drawList.size()) {
                        drawList.add(temp);
                    } else {
                        drawList.set(currentpage, temp);
                    }
                    Log.e("Bigwhite", "drawList 数目：" + drawList.size());

                    openPdf.pagedown();
                    setBackground(openPdf.pdfBitmap);
                    currentpage = openPdf.getCurrentPage();
                    pagenumber.setText("" + (currentpage + 1));

                    Log.e("Bigwhite", "设置页数成功：" + (currentpage + 1));

                    try {
                        if (drawList.size() > currentpage) {
                            baseBitmap = (Bitmap) drawList.get(currentpage);
                            //paintArea.setImageBitmap((Bitmap)drawList.get(currentpage));
                            canvas = new Canvas(baseBitmap);
                            canvas.drawColor(Color.TRANSPARENT);
                            paintArea.setImageBitmap(baseBitmap);
                            Log.e("Bigwhite", "4");
                        } else {
                            Log.e("Bigwhite", "5");
                            mClear.performClick();
                        }

                    } catch (Exception e) {
                        Log.e("Bigwhite", "翻页错误");
                        e.printStackTrace();
                    }

                }

            }
        });
    }

    //初始化保存按键
    private void initsavepage() {
        savepage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View paramView) {
                toolbox.setVisibility(View.GONE);
                toolbox2.setVisibility(View.GONE);
                // 获取windows中最顶层的view
                View view = getWindow().getDecorView();
                view.buildDrawingCache();

                // 获取状态栏高度
                Rect rect = new Rect();
                view.getWindowVisibleDisplayFrame(rect);
                int statusBarHeights = rect.top;
                Display display = getWindowManager().getDefaultDisplay();

                // 获取屏幕宽和高
                int widths = display.getWidth();
                int heights = display.getHeight();

                // 允许当前窗口保存缓存信息
                view.setDrawingCacheEnabled(true);

                // 去掉状态栏
                Bitmap bmp = Bitmap.createBitmap(view.getDrawingCache(), 0,
                        statusBarHeights, widths, heights - statusBarHeights);

                // 销毁缓存信息
                view.destroyDrawingCache();
                saveToFile(bmp);
                toolbox.setVisibility(View.VISIBLE);
                toolbox2.setVisibility(View.VISIBLE);
            }
        });
    }

    //显示pdf的功能键
    private void showtools() {
        pageup.setVisibility(View.VISIBLE);
        pagedown.setVisibility(View.VISIBLE);
        pagenumber.setVisibility(View.VISIBLE);
        pagenumber.setText("1");
    }

    //屏蔽pdf的功能键
    private void closetools() {
        pageup.setVisibility(View.INVISIBLE);
        pagedown.setVisibility(View.INVISIBLE);
        pagenumber.setVisibility(View.INVISIBLE);
        pagenumber.setText("1");
    }

    //设置背景
    private void setBackground(Bitmap background) {
        drawable = BitmapConvertToDrawale(background);
        this.getWindow().setBackgroundDrawable(drawable);
    }

    //处理文件管理器的反馈
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        mClear.performClick();
        if (FILE_RESULT_CODE == requestCode) {
            Bundle bundle = null;
            if (data != null && (bundle = data.getExtras()) != null) {

                String openedPdfFileName = bundle.getString("file");
                if (openedPdfFileName != null) {

                    Log.e("BigWhite", "open pdf or picture is " + openedPdfFileName);

                    if (resultCode == 2) {
                        showtools(); //显示pdf的工具栏
                        type = 2; //标识打开的文档为pdf
                        Log.e("BigWhite", "进入pdf");
                        //重新绘图
                        //openPdf.openRenderer(openedPdfFileName);

                        openPdf.openRenderer(openedPdfFileName);

                        if (openPdf.pdfBitmap != null) {
                            Log.e("BigWhite", "重新画图");
                            Log.e("BigWhite", "drawList size:" + drawList.size());
                            setBackground(openPdf.pdfBitmap);
                        }

                        //初始化drawList
                       drawList.clear();

                    } else if (resultCode == 3) {
                        Log.e("BigWhite", "进入图片");
                        closetools();

                        //重新绘图
                        openPdf.openPicture(openedPdfFileName, screen_width, screen_height);
                        if (openPdf.image != null) {
                            if (baseBitmap != null) {
                                baseBitmap = null;
                            }
                            drawable = BitmapConvertToDrawale(openPdf.image);
                            this.getWindow().setBackgroundDrawable(drawable);
                            drawList.clear();
                            Log.e("BigWhite", "drawList 初始化长度："+drawList.size());
                        }

                    }
                } else {
                    Toast.makeText(getApplicationContext(),
                            "No pdf file found, Please create new Pdf file",
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    //截图
    private void saveToFile(final Bitmap savebitmap) {
        // 获取SDCard目录,2.2的时候为:/mnt/sdcart 2.1的时候为：/sdcard
        File sdCardDir = Environment.getExternalStorageDirectory();
        Log.e("BigWhite", "******************SD:" + sdCardDir);

        File dir = new File(sdCardDir.getPath() + File.separator + "BigWhite" + File.separator
                + (new SimpleDateFormat("yyyy-MM-dd")).format(new Date()));

        if (!dir.exists()) {
            dir.mkdirs();
        }

        try {
            String name = (new SimpleDateFormat("HHmmss")).format(new Date());
            File saveFile = new File(dir.getPath(), name + ".png");
            FileOutputStream outStream = new FileOutputStream(saveFile);
            savebitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
            outStream.close();
            Log.e("BigWhite", "保存成功");
            Toast.makeText(getApplicationContext(),
                    "Save the PDF success : " + saveFile.getPath(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("BigWhite", "保存失败");
            Toast.makeText(getApplicationContext(),
                    "Save the PDF failure",
                    Toast.LENGTH_LONG).show();
        }

    }


}