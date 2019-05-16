package com.example.tools.Imageloadertool;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.example.tools.R;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageLoader {
    private static final String TAG="Imageloader";
    private static final int CORE_POOL_SIZE=Runtime.getRuntime().availableProcessors()+1;
    private  static final int MAX_THREAD_SIZE=Runtime.getRuntime().availableProcessors()*2+1;
    private static final int KEEP_ALIVE_TIME=1;
    private static final int TAG_KEY_URI= R.id.imageloader_uri;
    private static final int DISKCACHE_SIZE=50*1024*1024;
    private boolean isDiskLruCacheCreated=false;
    private static final int IO_BUFFER_SIZE=8*1024;

    private static final ThreadFactory factory=new ThreadFactory() {
        private final AtomicInteger count=new AtomicInteger(1);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r,"ImageLoader#"+count.getAndIncrement());
        }
    };
    /*
    线程池的创建
     */
    private static final Executor LOADER_THREAD_POOL=new ThreadPoolExecutor(CORE_POOL_SIZE,MAX_THREAD_SIZE,KEEP_ALIVE_TIME,
            TimeUnit.MINUTES,new LinkedBlockingQueue<Runnable>(),factory);
    /*
    用于更新UI，
    并解决图片的错位问题
     */
    private Handler mainhandler=new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            LoaderResult loaderResult=(LoaderResult)msg.obj;
            ImageView imageView=loaderResult.imageView;
            String uri=(String)imageView.getTag(TAG_KEY_URI);
            if (uri.equals(loaderResult.uri)){
                imageView.setImageBitmap(loaderResult.bitmap);
            }else {
                Log.w(TAG, "handleMessage: URL已经改变！");
            }
        }
    };

    private static class LoaderResult{
        public ImageView imageView;
        public String uri;
        public Bitmap bitmap;

        public LoaderResult(ImageView imageView, String uri, Bitmap bitmap) {
            this.imageView = imageView;
            this.uri = uri;
            this.bitmap = bitmap;
        }
    }

    private Context mcontext;
    private ImageReizer imageReizer=new ImageReizer();
    private LruCache<String,Bitmap> lruCache;
    private DiskLruCache diskLruCache;
    /*
    构造函数内初始化LruCache
    DIskLruCache
     */
    private ImageLoader(Context context){
        context=mcontext.getApplicationContext();
        int cacheSize=(int)Runtime.getRuntime().maxMemory()/1024/8;//设置最大内存为能用内存的1/8
        lruCache=new LruCache<String, Bitmap>(cacheSize){
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getRowBytes()*value.getHeight()/1024;//单位为KB
            }
        };

        File diskDirector=getDiskCacheDir(context,"bitmap");
        if (!diskDirector.exists()){
            diskDirector.mkdirs();
        }
        try {
            diskLruCache=DiskLruCache.open(diskDirector,1,1,DISKCACHE_SIZE);
            isDiskLruCacheCreated=true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ImageLoader build(Context context){
        return new ImageLoader(context);
    }

    /*
    几种图片加载的方法
     */
    private Bitmap loadBitmapFromMemCache(String url){
        final String key=hashKeyFromUrl(url);
        Bitmap bitmap=getMemCache(key);
        return bitmap;
    }

    private Bitmap loadBitmapFromDiskCache(String url,int reqwidth,int reqheight)throws IOException{
        if (Looper.myLooper()==Looper.getMainLooper()){
            Log.w(TAG, "loadBitmapFromDiskCache: 不能在主线程中做耗时任务");
        }
        if (diskLruCache==null){
            return null;
        }
        String key=hashKeyFromUrl(url);
        FileDescriptor fileDescriptor=getFromDiskCache(key);
        Bitmap bitmap=imageReizer.decodeSamplesBitmapFromFileDescriptor(fileDescriptor,reqwidth,reqheight);
        if (bitmap!=null){
            putToMemCache(key,bitmap);
        }
        return bitmap;
    }

    private Bitmap loadBitmapFromHttp(String url,int reqwidth,int reqheight)throws IOException{
        if (Looper.myLooper()==Looper.getMainLooper()){
            Log.w(TAG, "loadBitmapFromHttp: 不能在主线程中做耗时任务");
            throw new RuntimeException("不能在主线程中进行网络请求！");
        }
        if (diskLruCache==null){
            return null;
        }
        String key=hashKeyFromUrl(url);
        addToDiskLruCache(key,url);
        return loadBitmapFromDiskCache(url,reqwidth,reqheight);
    }

    private Bitmap loadBitmapFromUrl(String murl){
        Bitmap bitmap=null;
        HttpURLConnection connection=null;
        BufferedInputStream in=null;
        try{
            final URL url=new URL(murl);
            connection=(HttpURLConnection)url.openConnection();
            in=new BufferedInputStream(connection.getInputStream(),IO_BUFFER_SIZE);
            bitmap= BitmapFactory.decodeStream(in);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            connection.disconnect();
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    /*
    同步加载图片
     */
    public Bitmap loadBitmap(String url,int reqwidth,int reqeight){
        Bitmap bitmap=loadBitmapFromMemCache(url);
        if (bitmap!=null){
            return bitmap;
        }
        try {
            bitmap=loadBitmapFromDiskCache(url,reqwidth,reqeight);
            if (bitmap!=null){
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            bitmap=loadBitmapFromHttp(url,reqwidth,reqeight);
            if (bitmap!=null){
                return bitmap;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (bitmap==null&&!isDiskLruCacheCreated){
            bitmap=loadBitmapFromUrl(url);
        }
        return bitmap;
    }

    /*
    异步加载图片
     */
    public void bindBitmap(final String url, final ImageView imageView, final int reqwidth, final int reqheight){
        imageView.setTag(TAG_KEY_URI,url);
        Bitmap bitmap=loadBitmapFromMemCache(url);
        if (bitmap!=null){
            imageView.setImageBitmap(bitmap);
            return;
        }

        Runnable loadBitmapTask=new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap=loadBitmap(url,reqwidth,reqheight);
                if (bitmap!=null){
                    LoaderResult result=new LoaderResult(imageView,url,bitmap);
                    mainhandler.obtainMessage(1,result).sendToTarget();
                }
            }
        };
        LOADER_THREAD_POOL.execute(loadBitmapTask);
    }
    /*
    LruCache的get与put方法
    */
    private Bitmap getMemCache(String key){
        return lruCache.get(key);
    }

    private void putToMemCache(String key,Bitmap bitmap){
        if (getMemCache(key)==null) {
            lruCache.put(key, bitmap);
        }
    }

    /*
    DiskLruCache的添加与查找
     */
    private void addToDiskLruCache(String key,String url){
        try {
            DiskLruCache.Editor editor=diskLruCache.edit(key);
            if (editor!=null){
                OutputStream outputStream=editor.newOutputStream(0);
                if (isHttpSuccess(url,outputStream)){
                    editor.commit();
                }else {
                    editor.abort();
                }
            }
            diskLruCache.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private FileDescriptor getFromDiskCache(String key){
        try {
            DiskLruCache.Snapshot snapshot=diskLruCache.get(key);
            if (snapshot!=null){
                FileInputStream fileInputStream=(FileInputStream)snapshot.getInputStream(0);
                FileDescriptor fileDescriptor=fileInputStream.getFD();
                fileInputStream.close();
                return fileDescriptor;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    获取key的函数
     */
    private String hashKeyFromUrl(String url){
        String cachekey;
        try{
            final MessageDigest messageDigest=MessageDigest.getInstance("MD5");
            messageDigest.update(url.getBytes());
            byte[] bytes=messageDigest.digest();
            StringBuilder sb=new StringBuilder();
            for (int i=0;i<bytes.length;i++){
                String hex=Integer.toHexString(0xFF&bytes[i]);
                if (hex.length()==1){
                    sb.append('0');
                }
                sb.append(hex);
            }
            cachekey=sb.toString();
            return cachekey;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    /*
    判断网络请求是否成功的函数
     */
    private boolean isHttpSuccess(String murl, OutputStream outputStream){
        BufferedInputStream in=null;
        BufferedOutputStream out=null;
        HttpURLConnection connection=null;
        try {
          final URL url=new URL(murl);
          connection=(HttpURLConnection)url.openConnection();
          in=new BufferedInputStream(connection.getInputStream(),IO_BUFFER_SIZE);
          out=new BufferedOutputStream(outputStream,IO_BUFFER_SIZE);

          int b;
          while ((b=in.read())!=-1){
              out.write(b);
          }
          return true;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                in.close();
                out.close();
                connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /*
    创建DiskLRUCache所必需的第一个参数的获取
     */
    private File getDiskCacheDir(Context context,String uniqueName){
        boolean externalStorgeAvailable= Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
        final String cachePath;
        if (externalStorgeAvailable){
            cachePath=context.getExternalCacheDir().getPath();
        }else {
            cachePath=context.getCacheDir().getPath();
        }
        return new File(cachePath+File.separator+uniqueName);
    }


}
