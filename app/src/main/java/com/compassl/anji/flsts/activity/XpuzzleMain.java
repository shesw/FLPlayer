package com.compassl.anji.flsts.activity;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.compassl.anji.flsts.R;
import com.compassl.anji.flsts.adapter.GridPicListAdapter;
import com.compassl.anji.flsts.util.ScreenUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

    public class XpuzzleMain extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ma";

    private PopupWindow mPopupWindow;
    private View mPopupView;
    private GridView mGvPicList;
    private int[] mResPicId;
    private List<Bitmap> mPicList;
    private TextView mTvPuzzleMainTypeSelected;
    private static final int RESULT_IMAGE = 100;
    private static final int RESULT_CAMERA = 200;
    private static final String IMAGE_TYPE = "image/*";
    public static String TEMP_IMG_PATH;
    private int mType = 2;
    private LayoutInflater mLayoutInflater;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_xpuzzle_main);
        TEMP_IMG_PATH = getExternalCacheDir()+"";
        mPicList = new ArrayList<>();
        initView();
        mGvPicList.setAdapter(new GridPicListAdapter(XpuzzleMain.this,mPicList));
        mGvPicList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == mResPicId.length-1){
                    showDialogCustom();
                }else {
                    Intent intent = new Intent(XpuzzleMain.this,PuzzleMain.class);
                    intent.putExtra("picSelectedID",mResPicId[position]);
                    intent.putExtra("mType",mType);
                    startActivity(intent);
                }
            }
        });
        mTvPuzzleMainTypeSelected.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupShow(v);
            }
        });
    }

    private Uri uri;
    private String[] mCustomItem = new String[]{"本地相册","相机拍照片"};
    private void showDialogCustom() {
        AlertDialog.Builder builder = new AlertDialog.Builder(XpuzzleMain.this);
        builder.setTitle("选择：");
        builder.setItems(mCustomItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (0 == which){
                    if (ContextCompat.checkSelfPermission(XpuzzleMain.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                        ActivityCompat.requestPermissions(XpuzzleMain.this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                    }else {
                        openAlbum();
                    }
                }else if (1 == which){
                    File outputImage = new File(getExternalCacheDir(),"temp.png");
                    try {
                        if (outputImage.exists()){
                            outputImage.delete();
                        }
                        outputImage.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (Build.VERSION.SDK_INT>=24){
                        uri = FileProvider.getUriForFile(XpuzzleMain.this,"com.compassl.anji.fileprovider",outputImage);
                    }else {
                        uri = Uri.fromFile(outputImage);
                    }
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
                    startActivityForResult(intent,RESULT_CAMERA);
                }
            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:
                if (grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else {
                    Toast.makeText(this,"permisson denied",Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void openAlbum() {
        //Intent intent = new Intent(Intent.ACTION_PICK,null);
        //intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,IMAGE_TYPE);
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,RESULT_IMAGE);
    }

    private TextView mTvType2;
    private TextView mTvType3;
    private TextView mTvType4;
    private void initView(){
        mGvPicList = (GridView) findViewById(R.id.gv_xpuzzle_main_pic_list);
        mResPicId = new int[]{
                R.drawable.pic1, R.drawable.pic2, R.drawable.pic3,
                R.drawable.pic4, R.drawable.pic5, R.drawable.pic6,
                R.drawable.pic7, R.drawable.pic8, R.drawable.pic9,
                R.drawable.pic10, R.drawable.pic11, R.drawable.pic12,
                R.drawable.pic13, R.drawable.pic14,
                R.drawable.pic15, R.mipmap.ic_launcher
        };
        Bitmap[] bitmaps = new Bitmap[mResPicId.length];
        for (int i = 0;i<bitmaps.length;i++){
            bitmaps[i] = BitmapFactory.decodeResource(getResources(),mResPicId[i]);
            mPicList.add(bitmaps[i]);
        }

        mTvPuzzleMainTypeSelected = (TextView) findViewById(R.id.tv_puzzle_main_type_selected);
        mLayoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mPopupView = mLayoutInflater.inflate(R.layout.xpuzzle_main_type_selected,null);
        mTvType2 = (TextView) mPopupView.findViewById(R.id.tv_main_type_2);
        mTvType3 = (TextView) mPopupView.findViewById(R.id.tv_main_type_3);
        mTvType4 = (TextView) mPopupView.findViewById(R.id.tv_main_type_4);
        mTvType2.setOnClickListener(this);
        mTvType3.setOnClickListener(this);
        mTvType4.setOnClickListener(this);
    }

    private void popupShow(View view){
        int density = (int) ScreenUtil.getDeviceDensity(this);
        mPopupWindow = new PopupWindow(mPopupView,200*density,50*density);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setOutsideTouchable(true);
        Drawable transparent = new ColorDrawable(Color.TRANSPARENT);
        mPopupWindow.setBackgroundDrawable(transparent);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        mPopupWindow.showAtLocation(view, Gravity.NO_GRAVITY,location[0]-40*density,location[1]+30*density);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            if (requestCode == RESULT_IMAGE && data!=null){
                if (Build.VERSION.SDK_INT>=19){
                    handleImageOnKitKat(data);
                }else {
                    handleImageBeforeKitKat(data);
                }
            }else if (requestCode == RESULT_CAMERA){
                Intent intent = new Intent(XpuzzleMain.this,PuzzleMain.class);
//                String path = getImagePath(uri,null);
//                intent.putExtra("mPicPath",path);
                intent.putExtra("mPicPath",uri.toString());
                intent.putExtra("picSelectedID",100);
                startActivity(intent);
            }
        }
    }
    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if (DocumentsContract.isDocumentUri(this,uri)){
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID+"="+id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else  if ("com.android.providers.download.documents".equals(uri.getAuthority())){
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_download"),Long.valueOf(docId));
                imagePath = getImagePath(contentUri,null);
            }else if ("file".equalsIgnoreCase(uri.getScheme())){
                imagePath = uri.getPath();
            }
            Intent intent = new Intent(XpuzzleMain.this,PuzzleMain.class);
            intent.putExtra("mPicPath",imagePath);
            intent.putExtra("mType",mType);
            startActivity(intent);
        }
    }

    private void handleImageBeforeKitKat(Intent data) {
        Uri uri1 = data.getData();
        String imagePath = getImagePath(uri1,null);
        Intent intent = new Intent(XpuzzleMain.this,PuzzleMain.class);
        intent.putExtra("mPicPath",imagePath);
        intent.putExtra("mType",mType);
        startActivity(intent);
    }

    private String getImagePath(Uri uri1, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri1,null,selection,null,null);
        if (cursor!=null){
            if (cursor.moveToFirst()){
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_main_type_2:
                mType = 2;
                mTvPuzzleMainTypeSelected.setText("2 X 2");
                break;
            case R.id.tv_main_type_3:
                mType = 3;
                mTvPuzzleMainTypeSelected.setText("3 X 3");
                break;
            case R.id.tv_main_type_4:
                mType = 4;
                mTvPuzzleMainTypeSelected.setText("4 X 4");
                break;
        }
    }
}
