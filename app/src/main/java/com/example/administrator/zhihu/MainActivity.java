package com.example.administrator.zhihu;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private DrawerLayout mDrawerLayout;
    private static List<String> items = new ArrayList<>();
    private ListView main_listview;
   // private ListView main_listview2;
    private ListView menu_listview;
    private ViewPager viewPager;
    private SwipeRefreshLayout mSwipeLayout;
    private String date;
    private long firstTime;
    private ActionBar actionBar;
    Context context = this;
    private List<View> viewList = new ArrayList<>();
    private List<Latest.StoriesBean> stories_data;
    private List<Latest.TopStoriesBean> topStories_data;
    private List<Theme.OthersBean> otheres_data;
    private NewsFragment fragment;
    private int autoCurrIndex = 0;
    private List<ImageView> dots = new ArrayList<>();
    private Timer timer = new Timer();
    private Handler handle = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    MainListAdapter mainListAdapter = new MainListAdapter(MainActivity.this, 0);
                    main_listview.setAdapter(mainListAdapter);
                    for (Latest.TopStoriesBean topStoriesBean : topStories_data) {
                        View view = getLayoutInflater().from(MainActivity.this).inflate(R.layout.pager, null);
                        ImageView imageView = (ImageView) view.findViewById(R.id.pager_img);
                        TextView textView = (TextView) view.findViewById(R.id.pager_tx);
                        String str = topStoriesBean.getImage().toString().replace('[', ' ');
                        str = str.replace(']', ' ');
                        str = str.trim();
                        Glide.with(context)
                                .load(str)
                                .into(imageView);
                        textView.setText(topStoriesBean.getTitle());
                        viewList.add(view);
                    }

                    viewPager = (ViewPager) findViewById(R.id.vp);
                    viewPager.setAdapter(pagerAdapter);
                    ImageView dot1 = (ImageView)findViewById(R.id.dot1);
                    ImageView dot2 = (ImageView)findViewById(R.id.dot2);
                    ImageView dot3 = (ImageView)findViewById(R.id.dot3);
                    ImageView dot4 = (ImageView)findViewById(R.id.dot4);
                    ImageView dot5 = (ImageView)findViewById(R.id.dot5);
                    dots.add(dot1);
                    dots.add(dot2);
                    dots.add(dot3);
                    dots.add(dot4);
                    dots.add(dot5);
                    viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                        //图片左右滑动时候，将当前页的圆点图片设为选中状态
                        @Override
                        public void onPageSelected(int position) {
                            // 一定几个图片，几个圆点，但注意是从0开始的
                            int total = dots.size();
                            for (int j = 0; j < total; j++) {
                                if (j == position) {
                                    dots.get(j).setImageResource(R.drawable.dot_focused);
                                    Log.d("123123", "onPageSelected: 第"+(j+1)+"张图片");
                                } else {
                                    dots.get(j).setImageResource(R.drawable.dot_normal);
                                }
                            }
                        }
                        @Override
                        public void onPageScrolled(int i, float v, int i1) {
                        }
                        @Override
                        public void onPageScrollStateChanged(int state) {
                        }
                    });
                    mSwipeLayout.setRefreshing(false);
                    break;
                case 1:
                    for (Theme.OthersBean othersBean : otheres_data) {
                        items.add(othersBean.getName());
                    }
                    ArrayAdapter<String> menu_adapter = new ArrayAdapter<String>(
                            MainActivity.this, android.R.layout.simple_list_item_1, items);
                    menu_listview.setAdapter(menu_adapter);
                    break;
                case 3:
                    viewPager.setCurrentItem(msg.arg1);
                    break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendRequestLatest();
        sendRequestTheme();
        //轮播

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 3;
                autoCurrIndex = viewPager.getCurrentItem();
                if (autoCurrIndex == viewList.size() - 1) {
                    autoCurrIndex = -1;
                }
                message.arg1 = autoCurrIndex + 1;
                handle.sendMessage(message);
                Log.d("0123", "run: 换页");
            }
        }, 3000, 3000);

        //主ListView
        main_listview = (ListView) findViewById(R.id.lv_main);
        main_listview.addHeaderView(LayoutInflater.from(this).inflate(
                R.layout.viewpager, null));
        main_listview.addHeaderView(LayoutInflater.from(this).inflate(
                R.layout.main_header2, null));
        main_listview.addFooterView(LayoutInflater.from(this).inflate(
                R.layout.footer, null));
        View view = LayoutInflater.from(this).inflate(R.layout.footer,null);

        main_listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int i = stories_data.get(position - 2).getId();
                String str = Integer.toString(i);
                Intent intent = new Intent(MainActivity.this, News.class);
                intent.putExtra("urlId", str);
                startActivity(intent);
            }
        });

        //Toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_main);
        actionBar = getSupportActionBar();
        actionBar.setTitle("首页");
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_drawer);
        }
        //侧栏ListView
        menu_listview = (ListView) findViewById(R.id.lv_item);
        menu_listview.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fragment = new NewsFragment(otheres_data.get(position).getId(),
                        otheres_data.get(position).getThumbnail(),
                        otheres_data.get(position).getDescription());
                getSupportFragmentManager().beginTransaction()
                        .add(R.id.activity_main, fragment).addToBackStack(null).commit();
                mDrawerLayout.closeDrawers();

            }
        });
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                sendRequestLatest();
            }
        });

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.ic_dark:
                break;

            default:
        }
        return true;
    }

    private void sendRequestLatest() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://news-at.zhihu.com/api/4/news/latest ")
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    parseJSON(responseData);
                    Message msg = new Message();
                    msg.what = 0;
                    handle.sendMessage(msg);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseJSON(String jsonData) {
        Gson gson = new Gson();
        Latest latest = gson.fromJson(jsonData, Latest.class);
        date = latest.getDate();
        stories_data = latest.getStories();
        topStories_data = latest.getTop_stories();
    }

    private void sendRequestTheme() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url("http://news-at.zhihu.com/api/4/themes")
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    parseThemeJSON(responseData);
                    Message msg = new Message();
                    msg.what = 1;
                    handle.sendMessage(msg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseThemeJSON(String jsonData) {
        Gson gson = new Gson();
        Theme theme = gson.fromJson(jsonData, Theme.class);
        otheres_data = theme.getOthers();
    }

    class MainListAdapter extends ArrayAdapter {
        public MainListAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            return stories_data.size();
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FrameLayout frameLayout = (FrameLayout) getLayoutInflater().from(MainActivity.this).inflate(R.layout.main_listview_item, null);
            ImageView imageView = (ImageView) frameLayout.findViewById(R.id.img_news_main);
            TextView textView = (TextView) frameLayout.findViewById(R.id.tx_news_main);
            String str = stories_data.get(position).getImages().toString().replace("[", "");
            str = str.replace("]", "");
            Glide.with(context)
                    .load(str)
                    .into(imageView);
            textView.setText(stories_data.get(position).getTitle());
            return frameLayout;
        }

    }

    PagerAdapter pagerAdapter = new PagerAdapter() {
        @Override
        public int getCount() {
            return topStories_data.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
//                            super.destroyItem(container, position, object);
            container.removeView(viewList.get(position));
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(viewList.get(position));
            return viewList.get(position);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    };

    @Override
    public void onBackPressed() {

        if (fragment!=null&&fragment.isAdded()) {
            super.onBackPressed();
        }

        else if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
            mDrawerLayout.closeDrawers();
        } else {
            long secondTime = System.currentTimeMillis();
            if (secondTime -  firstTime > 2000) {
                Toast.makeText(this,"再按一次退出",Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
            } else {
                super.onBackPressed();
            }
        }
    }

}

