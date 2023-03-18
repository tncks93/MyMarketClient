package com.project_bong.mymarket.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ImageProcessor {

    private static final String AUTHORITY = "com.project_bong.mymarket.fileprovider";

    private Context mContext;

    public ImageProcessor(Context context){
        mContext = context;
    }

    public String saveJPEGFromSingleBitmap(Bitmap bm,String fileName) throws IOException{
        File tempFile = createTempFile(fileName);


        FileOutputStream os = new FileOutputStream(tempFile);
        bm.compress(Bitmap.CompressFormat.JPEG,100,os);
        os.close();

        String path = tempFile.getAbsolutePath();

        return path;


    }

    public Bitmap getBitmapFromContentUri(Uri contentUri) throws Exception{
        Bitmap imgBitmap = null;
        ContentResolver cr = mContext.getContentResolver();
        if(Build.VERSION.SDK_INT < 28){
            imgBitmap = MediaStore.Images.Media.getBitmap(cr,contentUri);
        }else{
            ImageDecoder.Source src = ImageDecoder.createSource(cr,contentUri);
            imgBitmap = ImageDecoder.decodeBitmap(src);
        }

        return imgBitmap;

    }

    public File createTempFile(String fileName) throws IOException {

        File storageDir = new File(mContext.getFilesDir() + "/MyMarket/");
        if (!storageDir.exists()) {
            boolean isMade = storageDir.mkdir();
        }

        File tempFile = File.createTempFile(fileName, ".jpeg", storageDir);



        return tempFile;


    }

    public ArrayList<MultipartBody.Part> getImagePartListFromPaths(ArrayList<String> pathList,String prefix){
        ArrayList<MultipartBody.Part> fileList = new ArrayList<>();
        for(int i=0;i<pathList.size();i++){
            File imageFile = new File(pathList.get(i));
            RequestBody imageBody = RequestBody.create(imageFile, MediaType.parse("image/jpeg"));
            String fileName = prefix+"_"+System.currentTimeMillis()+i+".jpeg";
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image[]",fileName,imageBody);
            fileList.add(imagePart);
        }
        return fileList;
    }

    public void deleteFile(String path){
        try{
            File file = new File(path);
            if(file.exists()){
                file.delete();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static String getAUTHORITY(){
        return AUTHORITY;
    }
}
