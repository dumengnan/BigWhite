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

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.hwpf.usermodel.CharacterRun;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.hwpf.usermodel.Section;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

public class OpenDoc {
    // 定义文字在bitmap上的初始位置
    private int x ;
    private int y ;

    private String[] tmp;
    private ArrayList<Integer> line;

    private int currentpage=0;
    private int totalpage=0;
    public Bitmap currentbitmap;
    private Paint paint;

    private int paintWidth;
    private int paintHeight;

    public OpenDoc(int width, int height) {
        this.paintHeight = height;
        this.paintWidth = width;
    }

    public void opendoc(final String path) {

        Log.e("BigWhite", "open the file:" + path);
        File file = new File(path);
        String cont = null;
        line = new ArrayList<Integer>();
        currentpage = 0;
        totalpage = 0;

        line.add(0);

        try {
            // 获得输入流并且将.doc文档中的文字读取至String中
            FileInputStream fin = new FileInputStream(file);
            cont = readDoc1(fin);

            // 将获得的.doc文档按照换行符划分至多行，并存储至字符串数组中,计算.doc文档的总页数
            tmp = cont.split("\n");

            int num = 0, stand = 18; //num用于计数当前有多少行 stand自定义一页显示多少行
            int count = 0; //标记一行可以分成多少行
            int i=0;
            for ( i= 0; i < tmp.length; i++) {
                //Log.e("BigWhite", "第" + (i + 1) + "行的内容的是：" + tmp[i]);
                if (tmp[i].length() > 55) {

                    if (tmp[i].length() % 55 == 0) {
                        count = tmp[i].length() / 55;
                    } else {
                        count = tmp[i].length() / 55 + 1;
                    }
                    num += count;
                    //Log.e("BigWhite", "num：" + num);

                    if (num == stand) {
                        if(i == tmp.length -1){
                            num = 0;
                            line.add(i);
                            totalpage += 1;
                            break;
                        }else {
                            num = 0;
                            line.add(i);
                            line.add(i + 1);
                            totalpage += 1;
                        }
                    } else if (num > stand) {
                        num = 0;
                        line.add(i - 1);
                        line.add(i);
                        totalpage += 1;
                        i--;
                    }
                } else {
                    num++;
                    if (num == stand) {
                        if(i == tmp.length -1){
                            num = 0;
                            line.add(i);
                            totalpage += 1;
                            break;
                        }else{
                            num = 0;
                            line.add(i);
                            line.add(i + 1);
                            totalpage += 1;
                        }
                    }
                    //Log.e("BigWhite", "num：" + num);
                }
            }
            if(i != tmp.length -1 ){
                line.add(i-1);
                totalpage++;
            }

            Log.e("BigWhite", "tmp.length = " + tmp.length);

            Log.e("BigWhite", "totalpage = " + totalpage);

            Log.e("BigWhite", "Arreylist line: " + line);
            fin.close();

            paint = new Paint();
            paint.setStrokeWidth(3.0f);
            paint.setTextSize(30.0f);

            getcurrentbitmap();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    // 直接抽取全部内容
    public String readDoc1(InputStream is) throws IOException {
        WordExtractor extractor = new WordExtractor(is);
        return extractor.getText();
    }

    public void getcurrentbitmap() {
        x=170;
        y=170;
        if (currentbitmap != null) {
            currentbitmap.recycle();
            currentbitmap = null;
        }
        // 创建canvas，设置canvas背景颜色，创建bitmap，将bitmap放进canvas当中，创建画笔
        currentbitmap = Bitmap.createBitmap(paintWidth , paintHeight ,
                Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(currentbitmap);
        canvas.drawColor(Color.WHITE);

        Log.e("BigWhite", "currentpage :" + currentpage);
        Log.e("BigWhite","line :"+line);

        int fisrt = line.get(currentpage*2);
        int last = line.get(currentpage * 2 + 1);

        Log.e("BigWhite","first:"+fisrt+"  last: "+last);

        for (int i = fisrt; i <= last; i++) {
            Log.e("BigWhite", "循环 : " + i);
            if (tmp[i].length() < 55) {
                canvas.drawText("        " + tmp[i], x, y, paint);
                y += 50;
                //Log.e("BigWhite", "一行  NUM");
            } else {
                int num = 0;
                if (tmp[i].length()% 55 == 0) {
                    num = tmp[i].length() / 55;
                } else {
                    num = tmp[i].length() / 55 + 1;
                }
                //Log.e("BigWhite", "多行  NUM:" + num);
                for (int j = 0; j <= num - 1; j++) {
                    if (j == 0) {
                        canvas.drawText("        " + tmp[i].substring(j * 55, j * 55 + 54), x, y, paint);
                        y += 50;
                    }else if (j == num - 1) {
                        if( num == 2) {
                            canvas.drawText(tmp[i].substring(j * 55 + 2), x, y, paint);
                        }else{
                            canvas.drawText(tmp[i].substring(j * 55), x, y, paint);
                        }
                        y += 50;
                    } else{
                        canvas.drawText(tmp[i].substring(j * 55 , j * 55 + 56), x, y, paint);
                        y += 50;
                    }
                }

            }

        }
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
        Log.e("BigWhite", "DOC :" + currentpage);
    }

    public void pagedown() {
        currentpage++;
        getcurrentbitmap();
        Log.e("BigWhite", "DOC :" + currentpage);
    }

}
