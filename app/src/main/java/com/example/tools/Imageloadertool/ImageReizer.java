package com.example.tools.Imageloadertool;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.FileDescriptor;

/*
图片压缩类
实现从资源文件中压缩
实现从文件中压缩
 */
public class ImageReizer {
    public ImageReizer() {

    }

    /*
    从资源文件中压缩
     */
    public Bitmap decodeSampledBitmapFromResourse(Resources res, int resId, int reqwidth, int reqheight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = caculateInSampleSize(reqwidth, reqheight, options);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /*
    从文件中压缩
     */
    public Bitmap decodeSamplesBitmapFromFileDescriptor(FileDescriptor fd, int reqwidth, int reqheught) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = caculateInSampleSize(reqwidth, reqheught, options);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd, null, options);
    }

    /*
    计算采样率的算法
     */
    private int caculateInSampleSize(int reqwidth, int reqheight, BitmapFactory.Options options) {
        int inSampleSize = 1;
        int width = options.outWidth;
        int height = options.outHeight;
        while (width / inSampleSize > reqwidth && height / inSampleSize > reqheight) {
            inSampleSize *= 2;
        }
        return inSampleSize;
    }
}
