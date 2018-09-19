package com.gome.usercenter.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Created by jfxue on 2017/7/21.
 */

public class ImageUtils {

    public static final String TAG = Constants.TAG_HEADER + "ImageUtils";

    public static Bitmap decodeAndCompress(String srcPath,float targetWidth, float targetHeight,int targetKbSize) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath,newOpts);

        newOpts.inJustDecodeBounds = false;
        int startW= newOpts.outWidth;
        int startH = newOpts.outHeight;

        float targetH = targetHeight;
        float targetW = targetWidth;

        int squareRate = 1;
        if (startW > startH && startW > targetW) {
            squareRate = (int) (newOpts.outWidth / targetW);
        } else if (startW < startH && startH > targetH) {
            squareRate = (int) (newOpts.outHeight / targetH);
        }
        if (squareRate <= 0){
            squareRate = 1;
        }
        newOpts.inSampleSize = squareRate;
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);

        return compressQuality(bitmap,targetKbSize);
    }

    public static Bitmap compressLimitDecodeCompress(Bitmap image,int maxSize,float targetWidth, float targetHeight,int targetKbSize) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        if( (baos.toByteArray().length / 1024)>maxSize) {
            baos.reset();
            image.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        newOpts.inJustDecodeBounds = false;
        int startW = newOpts.outWidth;
        int startH = newOpts.outHeight;

        float targetW = targetWidth;
        float targetH = targetHeight;

        int be = 1;
        if (startW > startH && startW > targetH) {
            be = (int) (newOpts.outWidth / targetH);
        } else if (startW < startH && startH > targetW) {
            be = (int) (newOpts.outHeight / targetW);
        }
        if (be <= 0)
            be = 1;
        newOpts.inSampleSize = be;
        newOpts.inPreferredConfig = Bitmap.Config.RGB_565;

        isBm = new ByteArrayInputStream(baos.toByteArray());
        bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
        return compressQuality(bitmap,targetKbSize);
    }

    private static Bitmap compressQuality(Bitmap bitmap, int targetKbSize) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        double mid = b.length / 1024;
        if (mid > targetKbSize) {
            double i = mid / targetKbSize;
            bitmap = zoomImage(bitmap, bitmap.getWidth() / Math.sqrt(i),
                    bitmap.getHeight() / Math.sqrt(i));
        }
        return bitmap;

    }

    public static Bitmap zoomImage(Bitmap bgimage, double newWidth, double newHeight) {
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }

    public static String Bitmap2StrByBase64(Bitmap bit){
        ByteArrayOutputStream bos=new ByteArrayOutputStream();
        bit.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bytes=bos.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    public static Bitmap getAvatarBitmapFromLocal(String path){
        Bitmap mBitmap;
        if(null == path){
            return null;
        }
        File imageFile = new File(path);

        FileInputStream fis = null;
        if(imageFile.exists()){
            try {
                fis = new FileInputStream(path);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                Log.i(TAG, "getInfo() error:"+e.toString());
                e.printStackTrace();
            }
        }else{
            Log.i(TAG, "getInfo() cannot find image path："+path);
            return null;
        }
        mBitmap  = BitmapFactory.decodeStream(fis);
        return mBitmap;
    }
}
