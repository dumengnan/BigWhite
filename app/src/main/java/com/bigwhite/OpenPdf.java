package com.bigwhite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;

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

        paintHeight = height;
        paintWidth = width;

    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int gettotalpage(){
        return mPdfRenderer.getPageCount();
    }
    /**
     * API for initializing file descriptor and pdf renderer after selecting pdf from list
     * @param filePath
     */
    public void openRenderer(String filePath) {
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

    public void pageup(){
        Log.e("BigWhite", "" + currentPage);
        currentPage++;
        showPage(currentPage);
    }

    public void pagedown(){
        Log.e("BigWhite", "" + currentPage);
        currentPage--;
        showPage(currentPage);
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
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * API show to particular page index using PdfRenderer
     * @param index
     */
    public void showPage(int index) {
        if (mPdfRenderer == null || mPdfRenderer.getPageCount() <= index
                || index < 0) {
            return;
        }
        // For closing the current page before opening another one.
        try {
            if (mCurrentPage != null) {
                mCurrentPage.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Open page with specified index
        mCurrentPage = mPdfRenderer.openPage(index);

        pdfBitmap= Bitmap.createBitmap(paintWidth,
                paintHeight, Bitmap.Config.ARGB_8888);

        //Pdf page is rendered on Bitmap
        mCurrentPage.render(pdfBitmap, null, null,
                PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        //Set rendered bitmap to ImageView
       // pdfView.setImageBitmap(bitmap);
    }

    public void openPicture(String path, int w, int h) {
        if(image != null){
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
        opts.inSampleSize = (int)scale;
        WeakReference<Bitmap> weak = new WeakReference<Bitmap>(BitmapFactory.decodeFile(path, opts));
        image =  Bitmap.createScaledBitmap(weak.get(), w, h, true);
    }


}
