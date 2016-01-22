package com.bigwhite;

/**
 * Created by LGQ on 2016/1/20.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.util.Xml;

public class OpenPPT {
    // 定义文字在bitmap上的初始位置
    private int x ;
    private int y ;

    private int currentpage;
    private int totalpage ;
    public Bitmap currentbitmap = null;
    private Paint paint;

    private int paintWidth;
    private int paintHeight;
    List<String> ls = new ArrayList<String>();
    ArrayList<String> everypage = new ArrayList<String>();


    public OpenPPT(int width, int height){
        this.paintHeight = height;
        this.paintWidth = width;
    }

    public void readPPTX(String path) {
        currentpage = 0;
        totalpage = 0;
        ZipFile xlsxFile = null;

        try {
            xlsxFile = new ZipFile(new File(path));// pptx按照读取zip格式读取
        } catch (ZipException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            Log.e("BigWhite","PPT readPPTX");
            ZipEntry sharedStringXML = xlsxFile.getEntry("[Content_Types].xml");// 找到里面存放内容的文件
            InputStream inputStream = xlsxFile.getInputStream(sharedStringXML);// 将得到文件流
            XmlPullParser xmlParser = Xml.newPullParser();// 实例化pull
            xmlParser.setInput(inputStream, "utf-8");// 将流放进pull中
            int evtType = xmlParser.getEventType();// 得到标签类型的状态
            while (evtType != XmlPullParser.END_DOCUMENT) {// 循环读取流
                switch (evtType) {
                    case XmlPullParser.START_TAG: // 判断标签开始读取
                        String tag = xmlParser.getName();// 得到标签
                        if (tag.equalsIgnoreCase("Override")) {
                            String s = xmlParser
                                    .getAttributeValue(null, "PartName");
                            if (s.lastIndexOf("/ppt/slides/slide") == 0) {
                                ls.add(s);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:// 标签读取结束
                        break;
                    default:
                        break;
                }
                evtType = xmlParser.next();// 读取下一个标签
            }
        } catch (ZipException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }

        totalpage = ls.size();
        Log.e("BigWhite","PPT totalpage :"+totalpage);

        for (int i = 1; i < (ls.size() + 1); i++) {  // 假设有6张幻灯片
            String  river=null;
            try {
                ZipEntry sharedStringXML = xlsxFile.getEntry("ppt/slides/slide"
                        + i + ".xml");// 找到里面存放内容的文件
                InputStream inputStream = xlsxFile
                        .getInputStream(sharedStringXML);// 将得到文件流
                XmlPullParser xmlParser = Xml.newPullParser();// 实例化pull
                xmlParser.setInput(inputStream, "utf-8");// 将流放进pull中
                int evtType = xmlParser.getEventType();// 得到标签类型的状态
                while (evtType != XmlPullParser.END_DOCUMENT) {// 循环读取流
                    switch (evtType) {
                        case XmlPullParser.START_TAG: // 判断标签开始读取
                            String tag = xmlParser.getName();// 得到标签
                            if (tag.equalsIgnoreCase("t")) {
                                river += xmlParser.nextText() + "\n";
                            }
                            break;
                        case XmlPullParser.END_TAG:// 标签读取结束
                            break;
                        default:
                            break;
                    }
                    evtType = xmlParser.next();// 读取下一个标签
                }



            } catch (ZipException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }

            if (river == null) {
                river = "解析文件出现问题";
            }else{
                everypage.add(river);
            }
        }
        Log.e("BigWhite","everypage size :"+everypage.size());
        paint = new Paint();
        paint.setStrokeWidth(3.0f);
        paint.setTextSize(50.0f);

        getcurrentbitmap();
    }

    public void getcurrentbitmap() {
        x=200;
        y=200;
        if (currentbitmap != null) {
            currentbitmap.recycle();
            currentbitmap = null;
        }
        // 创建canvas，设置canvas背景颜色，创建bitmap，将bitmap放进canvas当中，创建画笔
        currentbitmap = Bitmap.createBitmap(paintWidth, paintHeight ,
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(currentbitmap);
        canvas.drawColor(Color.WHITE);

        Log.e("BigWhite", "currentpage :" + currentpage);
        Log.e("BigWhite", "内容 :" + everypage.get(currentpage));
        canvas.drawText(everypage.get(currentpage), x, y, paint);
        canvas.save(Canvas.ALL_SAVE_FLAG);
        canvas.restore();
    }

    public int getcurrentpage() {
        return currentpage;
    }

    public int gettotalpage() {
        return totalpage;
    }

    public void pageup() {
        currentpage--;
        getcurrentbitmap();
        Log.e("BigWhite", "PPT 向上翻页 :" + currentpage);
    }

    public void pagedown() {
        currentpage++;
        getcurrentbitmap();
        Log.e("BigWhite", "PPT 向下翻页 :" + currentpage);
    }

}
