package com.bigwhite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfRenderer;
import android.graphics.pdf.PdfDocument.Page;
import android.graphics.pdf.PdfDocument.PageInfo;
import android.media.Image;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.SyncStateContract;
import android.transition.Slide;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by dell on 2016/1/9 0009.
 */
public class OpenPdf {

    private int currentPage = 0;

    public Bitmap pdfBitmap;
    public Bitmap image;

    private int paintWidth;
    private int paintHeight;

    private ParcelFileDescriptor mFileDescriptor;
    private PdfRenderer mPdfRenderer;
    private PdfRenderer.Page mCurrentPage;

    public OpenPdf(int width, int height) {

        this.paintHeight = height;
        this.paintWidth = width;

    }

    public void init() {
        currentPage = 0;
        try {
            if (mCurrentPage != null)
                mCurrentPage.close();
            if (mPdfRenderer != null)
                mPdfRenderer.close();
            if (mFileDescriptor != null)
                mFileDescriptor.close();
        } catch (Exception e) {
            //TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * API for initializing file descriptor and pdf renderer after selecting pdf from list
     *
     * @param filePath
     */
    public void openRenderer(String filePath) {
        Log.e("BigWhite", "进入openRenderer");
        init();
        File file = new File(filePath);
        try {
            mFileDescriptor = ParcelFileDescriptor.open(file,
                    ParcelFileDescriptor.MODE_READ_ONLY);
            mPdfRenderer = new PdfRenderer(mFileDescriptor);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        showPage(currentPage);
    }

    public void pagedown() {
        Log.e("BigWhite", "" + currentPage);
        currentPage++;
        showPage(currentPage);
    }

    public void pageup() {
        Log.e("BigWhite", "" + currentPage);
        currentPage--;
        showPage(currentPage);
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int gettotalpage() {
        return mPdfRenderer.getPageCount();
    }

    /**
     * API show to particular page index using PdfRenderer
     *
     * @param index
     */
    public void showPage(int index) {

        // For closing the current page before opening another one.
        try {
            if (pdfBitmap != null) {
                pdfBitmap.recycle();
                pdfBitmap = null;
            }
            if (mCurrentPage != null) {
                mCurrentPage.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.e("BigWhite", "进入showpage ");

        // Open page with specified index
        mCurrentPage = mPdfRenderer.openPage(index);
        Log.e("BigWhite", "进入showpage openPage 成功 ");

        Log.e("BigWhite", "width:" + paintWidth + "  height:" + paintHeight);
        pdfBitmap = Bitmap.createBitmap(paintWidth, paintHeight, Bitmap.Config.ARGB_8888);
        pdfBitmap.eraseColor(Color.WHITE);


        int pdfwidth = mCurrentPage.getWidth();
        int pdfheight = mCurrentPage.getHeight();
        Log.e("BigWhite", "showpage createBitmap 成功");
        Log.e("BigWhite", "pdf width:" + pdfwidth + "  pdfhwight:" + pdfheight);
        Matrix matrix = new Matrix();
        matrix.setTranslate(0, paintHeight);
        matrix.preRotate(270);  //要旋转的角度
        //matrix.postScale(1, 1, ((int) (mCurrentPage.getHeight()) / 2), (int) ((mCurrentPage.getWidth()) / 2));
        Log.e("BigWhite", "bi li :" + (1.0f * paintHeight / pdfwidth));
        matrix.preScale((1.0f * paintHeight / pdfwidth), (1.0f * paintWidth / pdfheight));
        //Pdf page is rendered on Bitmap
        //Rect abc = new Rect(0, 0, mCurrentPage.getWidth(), mCurrentPage.getHeight());

        mCurrentPage.render(pdfBitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        Log.e("BigWhite", "showpage render 成功");
        //Set rendered bitmap to ImageView
        // pdfView.setImageBitmap(bitmap);
    }


    private void closeRenderer() {
        try {
            if (mCurrentPage != null)
                mCurrentPage.close();
            if (mPdfRenderer != null)
                mPdfRenderer.close();
            if (mFileDescriptor != null)
                mFileDescriptor.close();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public ArrayList<Bitmap> openPdf(String path) throws InterruptedException {

        ArrayList<Bitmap> pdfList = new ArrayList<Bitmap>();

        File file = new File(path);

        try {
            mFileDescriptor = ParcelFileDescriptor.open(file,
                    ParcelFileDescriptor.MODE_READ_ONLY);
            mPdfRenderer = new PdfRenderer(mFileDescriptor);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int totlepage = mPdfRenderer.getPageCount();
        int i=0;
        for(i=0;i<totlepage;i++){

            try {
                if (pdfBitmap != null) {
                    pdfBitmap.recycle();
                    pdfBitmap = null;
                }
                if (mCurrentPage != null) {
                    mCurrentPage.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //Log.e("BigWhite", "循环 i:"+ i);
            mCurrentPage = mPdfRenderer.openPage(i);

            Bitmap pdfBitmap = Bitmap.createBitmap(paintWidth, paintHeight, Bitmap.Config.ARGB_8888);
            pdfBitmap.eraseColor(Color.WHITE);

            int pdfwidth = mCurrentPage.getWidth();
            int pdfheight = mCurrentPage.getHeight();

            //Log.e("BigWhite", "pdf width:" + pdfwidth + "  pdfhwight:" + pdfheight);
            Matrix matrix = new Matrix();
            matrix.setTranslate(0, paintHeight);
            matrix.preRotate(270);  //要旋转的角度
            //matrix.postScale(1, 1, ((int) (mCurrentPage.getHeight()) / 2), (int) ((mCurrentPage.getWidth()) / 2));
            //Log.e("BigWhite", "bi li :" + (1.0f * paintHeight / pdfwidth));
            matrix.preScale((1.0f * paintHeight / pdfwidth), (1.0f * paintWidth / pdfheight));
            //Pdf page is rendered on Bitmap
            //Rect abc = new Rect(0, 0, mCurrentPage.getWidth(), mCurrentPage.getHeight());

            mCurrentPage.render(pdfBitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
            pdfList.add(pdfBitmap);

            //Log.e("BigWhite", "成功添加一页");
        }
        closeRenderer();
        return pdfList;
    }


    public void openPicture(String path, int w, int h) {
        if (image != null) {
            image.recycle();
            image = null;
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        // 设置为ture只获取图片大小
        opts.inJustDecodeBounds = true;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
        // 返回为空
        BitmapFactory.decodeFile(path, opts);
        int width = opts.outWidth;
        int height = opts.outHeight;
        float scaleWidth = 0.f, scaleHeight = 0.f;
        if (width > w || height > h) {
            // 缩放
            scaleWidth = ((float) width) / w;
            scaleHeight = ((float) height) / h;
        }
        opts.inJustDecodeBounds = false;
        float scale = Math.max(scaleWidth, scaleHeight);
        opts.inSampleSize = (int) scale;
        WeakReference<Bitmap> weak = new WeakReference<Bitmap>(BitmapFactory.decodeFile(path, opts));
        image = Bitmap.createScaledBitmap(weak.get(), w, h, true);
    }

}
