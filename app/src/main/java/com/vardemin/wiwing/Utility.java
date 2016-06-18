package com.vardemin.wiwing;

/**
 * Created by xavie on 16.05.2016.
 */
import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;

public class Utility {

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getPhoto(byte[] image) {
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inPurgeable = true;
        return BitmapFactory.decodeByteArray(image, 0, image.length,options);
    }

    public static Bitmap cropToSquare(Bitmap bitmap){
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();
        int scaleW = (width > 256) ? 256 : width;
        float scaleHd = height * ((float)scaleW/width);
        int scaleH = Math.round(scaleHd);
        Bitmap cropImg = Bitmap.createScaledBitmap(bitmap, scaleW, scaleH, false);
        width = scaleW;
        height = scaleH;
        int newWidth = (height > width) ? width : height;
        int newHeight = (height > width)? height - ( height - width) : height;
        int cropW = (width / 2) - (newWidth/2);
        cropW = (cropW < 0)? 0: cropW;
        int cropH = (height/2) - (newHeight/ 2);
        cropH = (cropH < 0)? 0: cropH;
        cropImg = Bitmap.createBitmap(cropImg, cropW, cropH, newWidth, newHeight);
        return cropImg;
    }

    public static float convertDpToPixel(float dp, Activity context)
    {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return px;
    }
}