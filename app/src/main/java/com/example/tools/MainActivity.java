package com.example.tools;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.tools.Imageloadertool.ImageLoader;
import com.example.tools.Utils.MainListView;
import com.example.tools.Utils.MyApplication;
import com.example.tools.baseadapter.CommonAdapter;
import com.example.tools.baseadapter.CommonViewHolder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final OkHttpClient client = new OkHttpClient();
    MyApplication myApplication = new MyApplication();
    private ImageLoader imageLoader = ImageLoader.build(myApplication.getContext());
    private static final String TAG = "JSON";
    private DrawerLayout drawerLayout;
    private ArrayList<MainListView> stringArrayList;
    private PopupWindow mPopWindow;
    private String paths;
    private File file;
    private ImageButton imageButton;
    private static int REQUEST_CAMERA = 1;
    private static int IMAGE_REQUEST_CODE = 2;
    private static final String DETAIL_URL = "http://elf.egos.hosigus.com/music/playlist/detail?id=24381616";
    private static final String MUSIC_URL = "http://music.163.com/song/media/outer/url?id=";
    public static List<String> musicNameList = new ArrayList<>();
    public static List<String> authorNameList = new ArrayList<>();
    public static List<String> albumNameList = new ArrayList<>();
    public static List<String> picUrlList = new ArrayList<>();
    public static List<String> musicIdList = new ArrayList<>();
    public static List<String> musicUrl = new ArrayList<>();
    private ImageView picImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Button changePictureButton = (Button) findViewById(R.id.name_button);


        //菜单的设置
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_setting);
        }
        //listview的设置
        ListView listView = (ListView) findViewById(R.id.main_listview);
        initstringArrayList();
        CommonAdapter stringAdapter = new CommonAdapter<MainListView>(stringArrayList, R.layout.main_listview_item) {
            @Override
            protected void bindView(CommonViewHolder holder, MainListView item) {
                holder.setTextView(R.id.textview, item.getThings());
            }
        };
        listView.setAdapter(stringAdapter);

        //头像更换
        imageButton = (ImageButton) findViewById(R.id.head_button);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow();
            }
        });
        //转换到详情页面
        Button detailButton = (Button) findViewById(R.id.detail_button);
        detailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });
        //JSON解析音乐详情并初始化第一界面
        picImage = (ImageView) findViewById(R.id.picImage);
        postRequest();

        //图片转动


    }

    private void postRequest() {
        RequestBody formBody = new FormBody.Builder()
                .add(",", ",")
                .build();

        final Request request = new Request.Builder()
                .url(DETAIL_URL)
                .post(formBody)
                .build();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Response response = null;
                try {
                    response = client.newCall(request).execute();
                    parseJSONWithJSONObject(response.body().string());
                    imageLoader.bindBitmap(picUrlList.get(DetailActivity.index), picImage, 640, 480);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView musicName = (TextView) findViewById(R.id.music_name);
                            musicName.setText(musicNameList.get(DetailActivity.index));
                            TextView authorName = (TextView) findViewById(R.id.music_author);
                            authorName.setText(authorNameList.get(DetailActivity.index));
                        }
                    });
                    if (response.isSuccessful()) {
                        Log.i("WY", "打印POST响应的数据：");
                    } else {
                        throw new IOException("Unexpected code " + response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    private void parseJSONWithJSONObject(String responseData) {
        try {
            JSONObject rootObject = new JSONObject(responseData);
            String playlist = rootObject.getString("playlist");
            JSONObject rootObject1 = new JSONObject(playlist);
            String tracks = rootObject1.getString("tracks");
            JSONArray jsonArray = new JSONArray(tracks);
            String trackIds = rootObject1.getString("trackIds");
            JSONArray jsonArray2 = new JSONArray(trackIds);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                musicNameList.add(jsonObject.getString("name"));
                Log.d(TAG, "musicName: " + musicNameList.get(i));
                JSONArray jsonArray1 = jsonObject.getJSONArray("ar");
                JSONObject jsonObject1 = jsonArray1.getJSONObject(0);
                authorNameList.add(jsonObject1.getString("name"));
                Log.d(TAG, "authorName:" + authorNameList.get(i));
                JSONObject jsonObject2 = jsonObject.getJSONObject("al");
                albumNameList.add(jsonObject2.getString("name"));
                Log.d(TAG, "albumName:" + albumNameList.get(i));
                picUrlList.add(jsonObject2.getString("picUrl"));
                Log.d(TAG, "picUrl:" + picUrlList.get(i));
                JSONObject jsonObject3 = jsonArray2.getJSONObject(i);
                musicIdList.add(jsonObject3.getString("id"));
                Log.d(TAG, "musicId:" + musicIdList.get(i));
                musicUrl.add(MUSIC_URL + musicIdList.get(i) + ".mp3");
                Log.d(TAG, "musicUrl:" + musicUrl.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showPopupWindow() {
        View contentView = LayoutInflater.from(MainActivity.this).inflate(R.layout.popuplayout, null);
        mPopWindow = new PopupWindow(contentView,
                DrawerLayout.LayoutParams.WRAP_CONTENT, DrawerLayout.LayoutParams.WRAP_CONTENT, true);
        mPopWindow.setContentView(contentView);
        //设置各个控件的点击响应
        TextView tv1 = (TextView) contentView.findViewById(R.id.pop_choosePhoto);
        TextView tv2 = (TextView) contentView.findViewById(R.id.pop_cancel);
        tv1.setOnClickListener(this);
        tv2.setOnClickListener(this);
        //显示PopupWindow
        View rootview = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_main, null);
        mPopWindow.showAtLocation(rootview, Gravity.BOTTOM, 0, 0);
    }

    //点击事件
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.pop_choosePhoto:
                getPhoto();
                mPopWindow.dismiss();
                break;
            case R.id.pop_cancel:
                mPopWindow.dismiss();
                break;
            default:
                break;
        }
    }

    private void getPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, IMAGE_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CAMERA && resultCode == RESULT_OK) {
            Log.e("TAG", "---------" + FileProvider.getUriForFile(this, "com.example.tools.fileprovider", file));
            imageButton.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(file);
            mediaScanIntent.setData(contentUri);
            sendBroadcast(mediaScanIntent);
        }
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                Uri selectedImage = data.getData();
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                paths = cursor.getString(columnIndex);
                cursor.close();
                Bitmap bitmap = BitmapFactory.decodeFile(paths);
                imageButton.setImageBitmap(bitmap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
                break;
        }
        return true;
    }

    private void initstringArrayList() {
        stringArrayList = new ArrayList<>();
        stringArrayList.add(new MainListView("日推 Daily Recommend"));
        stringArrayList.add(new MainListView("广场 Comments Plaza"));
        stringArrayList.add(new MainListView("我的收藏 My Collection"));
    }


}


