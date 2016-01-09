package com.bigwhite;

import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by dell on 2016/1/9 0009.
 */
public class OpenPdf {

    private static int currentPage = 0;

    public Bitmap pdfBitmap;

    private int paintWidth;
    private int paintHeight;

    private ParcelFileDescriptor mFileDescriptor;
    private PdfRenderer mPdfRenderer;
    private PdfRenderer.Page mCurrentPage;

    public OpenPdf(int width, int height) {

        paintHeight = height;
        paintWidth = width;

    }

    public static int getCurrentPage() {
        return currentPage;
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
        currentPage++;
        showPage(currentPage);
    }

    public void pagedown(){
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


}
