package com.bigwhite;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dell on 2016/1/9 0009.
 */
public class FileManagerActivity extends ListActivity{
    private List<String> items = null;
    private List<String> paths = null;
    private String rootPath = Environment.getExternalStorageDirectory().getPath();
    private String curPath = Environment.getExternalStorageDirectory().getPath();
    private TextView mPath;
    private String filepath = null;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_filemanager);

        mPath = (TextView) findViewById(R.id.mPath);

        Button buttonConfirm = (Button) findViewById(R.id.buttonConfirm);
        buttonConfirm.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                if(filepath != null){
                    Intent data = new Intent(FileManagerActivity.this, MainActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("file", filepath);
                    data.putExtras(bundle);
                    setResult(2, data);
                    finish();
                }
            }
        });
        Button buttonCancle = (Button) findViewById(R.id.buttonCancle);
        buttonCancle.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                finish();
            }
        });
        getFileDir(rootPath);
    }

    private void getFileDir(String filePath) {
        mPath.setText(filePath);
        items = new ArrayList<String>();
        paths = new ArrayList<String>();
        File f = new File(filePath);
        File[] files = f.listFiles();

        if (!filePath.equals(rootPath)) {
            items.add("b1");
            paths.add(rootPath);
            items.add("b2");
            paths.add(f.getParent());
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            items.add(file.getName());
            paths.add(file.getPath());
        }

        setListAdapter(new MyAdapter(this, items, paths));
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        File file = new File(paths.get(position));
        if (file.isDirectory()) {
            curPath = paths.get(position);
            getFileDir(paths.get(position));
        } else {
            filepath = null;
            String fname = file.getName();
            String end = file.getName().substring(fname.lastIndexOf(".")+1,fname.length());
            if(end.equals("pdf")){
                filepath = curPath+"/"+file.getName();
            }
        }
    }
}
