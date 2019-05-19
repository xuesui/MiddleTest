package com.example.tools;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.ResourceBusyException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tools.Imageloadertool.ImageLoader;
import com.example.tools.Utils.MyApplication;
import com.example.tools.downloadtool.DownloadService;

import java.io.IOException;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener {
    MyApplication myApplication = new MyApplication();
    private ImageLoader imageLoader = ImageLoader.build(myApplication.getContext());
    public static int index = 0;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Button playOrPause;
    private Button star;
    private Button good;
    private boolean isLiked = true;
    private boolean isStar = true;
    private Button download;
    private SeekBar seekBar;
    private TextView localtime;
    private TextView totalTime;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (mediaPlayer.isPlaying()) {
                localtime.setText(" " + (int) mediaPlayer.getCurrentPosition() / 1000 / 60 + ":" + (mediaPlayer.getCurrentPosition() / 1000) % 60);
                seekBar.setProgress(mediaPlayer.getCurrentPosition() / mediaPlayer.getDuration());
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
        playOrPause = (Button) findViewById(R.id.play);
        playOrPause.setOnClickListener(this);
        final Button next = (Button) findViewById(R.id.next_button);
        next.setOnClickListener(this);
        Button last = (Button) findViewById(R.id.last_button);
        last.setOnClickListener(this);
        good = (Button) findViewById(R.id.good_button);
        good.setOnClickListener(this);
        star = (Button) findViewById(R.id.star_button);
        star.setOnClickListener(this);
        download = (Button) findViewById(R.id.download_button);
        download.setOnClickListener(this);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Toast.makeText(DetailActivity.this, "暂时没有拖动进度条到指定位置播放音乐的功能", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        //音频相关操作
        initMediaPlayer();
        changeUI();

    }

    private void dateTime() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                handler.sendMessage(new Message());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    private void changeUI() {
        TextView musicName = (TextView) findViewById(R.id.musicName);
        musicName.setText(MainActivity.musicNameList.get(index));
        TextView author = (TextView) findViewById(R.id.author);
        author.setText(MainActivity.authorNameList.get(index));
        ImageView imageView = (ImageView) findViewById(R.id.coverpic);
        imageLoader.bindBitmap(MainActivity.picUrlList.get(index), imageView, 640, 480);
        localtime = (TextView) findViewById(R.id.locatime);
        localtime.setText(" " + (int) mediaPlayer.getCurrentPosition() / 1000 / 60 + ":" + (mediaPlayer.getCurrentPosition() / 1000) % 60);
        totalTime = (TextView) findViewById(R.id.totaltime);
        totalTime.setText(" " + (int) mediaPlayer.getDuration() / 1000 / 60 + ":" + (mediaPlayer.getDuration() / 1000) % 60);
        Resources resources = myApplication.getContext().getResources();
        Drawable drawable = resources.getDrawable(R.drawable.ic_like_off);
        good.setBackground(drawable);
        isLiked = true;
        Resources resources2 = myApplication.getContext().getResources();
        Drawable drawable2 = resources2.getDrawable(R.drawable.ic_star_off);
        star.setBackground(drawable2);
        isStar = true;
    }


    private void initMediaPlayer() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mediaPlayer.setDataSource(MainActivity.musicUrl.get(index));
                    mediaPlayer.prepare();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            changeUI();
                        }
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play:
                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    Resources resources = myApplication.getContext().getResources();
                    Drawable drawable = resources.getDrawable(R.drawable.ic_play_running);
                    playOrPause.setBackground(drawable);
                    dateTime();
                } else {
                    mediaPlayer.pause();
                    Resources resources = myApplication.getContext().getResources();
                    Drawable drawable = resources.getDrawable(R.drawable.ic_play_pause);
                    playOrPause.setBackground(drawable);
                }
                break;
            case R.id.next_button:
                if (mediaPlayer.isPlaying() && index < 798) {
                    try {
                        index++;
                        mediaPlayer.reset();
                        initMediaPlayer();
                        mediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (index == 798) {
                        Toast.makeText(this, "这已经是最后一首歌了", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "现在没有音乐正在播放", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.last_button:
                if (mediaPlayer.isPlaying() && index > 0) {
                    index--;
                    try {
                        mediaPlayer.reset();
                        initMediaPlayer();
                        mediaPlayer.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    if (index == 0) {
                        Toast.makeText(this, "这已经是第一首歌了", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "现在没有音乐正在播放", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            case R.id.good_button:
                if (isLiked) {
                    Resources resources = myApplication.getContext().getResources();
                    Drawable drawable = resources.getDrawable(R.drawable.ic_like_on);
                    good.setBackground(drawable);
                    isLiked = false;
                } else {
                    Resources resources = myApplication.getContext().getResources();
                    Drawable drawable = resources.getDrawable(R.drawable.ic_like_off);
                    good.setBackground(drawable);
                    isLiked = true;
                }
                break;
            case R.id.star_button:
                if (isStar) {
                    Resources resources = myApplication.getContext().getResources();
                    Drawable drawable = resources.getDrawable(R.drawable.ic_star_on);
                    star.setBackground(drawable);
                    isStar = false;
                } else {
                    Resources resources = myApplication.getContext().getResources();
                    Drawable drawable = resources.getDrawable(R.drawable.ic_star_off);
                    star.setBackground(drawable);
                    isStar = true;
                }
                break;
            default:
                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "拒绝将无法下载", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }
}
