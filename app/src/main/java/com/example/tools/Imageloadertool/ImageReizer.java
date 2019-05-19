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
    private static final String TAG = "ImageReizer";

    public ImageReizer() {

    }

    /*
    从资源文件中压缩
     */
    public Bitmap decodeSampledBitmapFromResourse(Resources res, int resId, int reqwidth, int reqheight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = caculateInSampleSize(reqwidth, reqheight, options);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    /*
    从文件中压缩
     */
    public Bitmap decodeSamplesBitmapFromFileDescriptor(FileDescriptor fd, int reqwidth, int reqheught) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd,null,options);
        options.inSampleSize=caculateInSampleSize(reqwidth,reqheught,options);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd,null,options);
    }

    /*
    计算采样率的算法
     */
    private int caculateInSampleSize(int reqwidth, int reqheight, BitmapFactory.Options options) {
        if (reqwidth == 0 || reqheight == 0) {
            return 1;
        }
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqheight || width > reqwidth) {
            final int halfHeight = height / 2;
            final int halfwidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqheight && (halfwidth / inSampleSize) >= reqwidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
