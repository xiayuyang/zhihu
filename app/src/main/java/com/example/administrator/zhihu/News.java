package com.example.administrator.zhihu;

/**
 * Created by Administrator on 2018/2/13.
 */

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class News extends AppCompatActivity {
    private static final String TAG = "NewsTAG";
    private String id;
    private String url;
    private String imgUrl;
    private String title;
    private String cssUrl;
    private String body;
    private String css;
    private NewsContent newsContent;
    private ImageView imageView;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private String html;
    private WebView webView;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d(TAG, "handleMessage: ");

            switch (msg.what) {
                case 0:
                    css = css.replace("]","");
                    css = css.replace("\\", "");
                    css = css.replace("\"", "");
                    html = newsContent.getBody();
                    html = "<head><style>img{max-width:340px !important;}</style></head>"+html;
                    html = html.replace("<div class=\"img-place-holder\">", "");
                    Log.d("1111111", "parseNewsJSON: "+css);
                    collapsingToolbarLayout.setTitle(title);
                    Glide.with(News.this)
                            .load(imgUrl)
                            .into(imageView);
                    webView.loadDataWithBaseURL(css,html,"text/html","UTF-8",null);
                    Log.d("9999999", "handleMessage: webView加载成功" );
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);

        Intent intent = getIntent();
        this.id = intent.getStringExtra("urlId");
        this.url = " http://news-at.zhihu.com/api/4/news/" + id;
        imageView = (ImageView) findViewById(R.id.app_bar_image);
        webView = (WebView) findViewById(R.id.webView);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        collapsingToolbarLayout = (CollapsingToolbarLayout)findViewById(R.id.collapsing_toolbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.news_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(null);
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.back);
        }
        sendRequestNews();
        Log.d(TAG, "onCreate: "+id);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.praise:
                Toast.makeText(News.this, "赞 +1", Toast.LENGTH_SHORT).show();
                break;
            case R.id.collect:
                Toast.makeText(News.this, "您未登录", Toast.LENGTH_SHORT).show();
                break;
            case R.id.comment:
                Toast.makeText(News.this, "您未登录", Toast.LENGTH_SHORT).show();
                break;
            default:
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.news_toolbar, menu);
        return true;
    }

    private void sendRequestNews() {
        Log.d(TAG, "sendRequestNews: ");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: ");
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    parseNewsJSON(responseData);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void sendRequestCss() {
        Log.d(TAG, "sendRequestNews: ");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: ");
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(cssUrl)
                            .build();
                    Response response = client.newCall(request).execute();
                    css = response.body().string();
                    Message msg = new Message();
                    msg.what = 0;
                    handler.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseNewsJSON(String jsonData) {
        Gson gson = new Gson();
        newsContent = gson.fromJson(jsonData, NewsContent.class);
        body = newsContent.getBody();
        imgUrl = newsContent.getImage();
        title = newsContent.getTitle();
        cssUrl = newsContent.getCss().toString().replace("[","");
        sendRequestCss();

    }
}
