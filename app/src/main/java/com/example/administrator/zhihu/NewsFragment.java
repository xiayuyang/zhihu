package com.example.administrator.zhihu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.ContentValues.TAG;




@SuppressLint("ValidFragment")
public class NewsFragment extends Fragment {
    private DrawerLayout mDrawerLayout;
    private int urlId;
    private String imgURL;
    private String description;
    private ListView listView;
    private ListView menu_listview;
    private NewsFragment fragment;
    private List<Theme.OthersBean> otheres_data;
    private ImageView imageView;
    private TextView textView;
    private String url;
    private String title;


    private List<ThemeContents.StoriesBean> contents_data;

    public NewsFragment(int id, String imgURL, String description) {
        urlId = id;
        this.imgURL = imgURL;
        this.description = description;
        url = "http://news-at.zhihu.com/api/4/theme/" + urlId;
    }

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    NewsFragment.MainListAdapter mainListAdapter = new NewsFragment.MainListAdapter(getContext(), 0);
                    ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
                    actionBar.setTitle(title);
                    listView.setAdapter(mainListAdapter);
                    listView.setOnItemClickListener(new ListView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            if (position==0)
                                return;
                            int i = contents_data.get(position - 1).getId();

                            String str = Integer.toString(i);
                            Intent intent = new Intent(getActivity(), News.class);
                            intent.putExtra("urlId", str);
                            startActivity(intent);
                        }
                    });

            }

        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_items_fragment, container, false);
        sendRequestThemeStoris();
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar_fragment);
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);

        listView = (ListView) view.findViewById(R.id.lv_menu_news);
        View headerView = inflater.inflate(R.layout.theme_header, null);
        imageView = (ImageView) headerView.findViewById(R.id.theme_img);
        textView = (TextView) headerView.findViewById(R.id.theme_tx);
        listView.addHeaderView(headerView);
        Glide.with(this)
                .load(imgURL)
                .into(imageView);
        textView.setText(description);


        return view;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.e(TAG, "onCreateOptionsMenu()");
        menu.clear();
        inflater.inflate(R.menu.menu_toolbar, menu);
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.add:
                Toast toast = Toast.makeText(getActivity(), "已关注", Toast.LENGTH_LONG);
                toast.show();

                break;
            default:
        }
        return true;
    }


    private void sendRequestThemeStoris() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(url)
                            .build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    parseThemeStoriesJSON(responseData);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parseThemeStoriesJSON(String jsonData) {
        Gson gson = new Gson();
        ThemeContents themeContents = gson.fromJson(jsonData, ThemeContents.class);
        contents_data = themeContents.getStories();
        title = themeContents.getName();
        Message msg = new Message();
        msg.what = 0;
        handler.sendMessage(msg);
    }


    class MainListAdapter extends ArrayAdapter {
        public MainListAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public int getCount() {
            return contents_data.size();
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            FrameLayout frameLayout = (FrameLayout) LayoutInflater.from(((AppCompatActivity) getActivity())).inflate(R.layout.main_listview_item, null);
            ImageView imageView = (ImageView) frameLayout.findViewById(R.id.img_news_main);
            TextView textView = (TextView) frameLayout.findViewById(R.id.tx_news_main);
            if (contents_data.get(position).getImages() != null) {
                String str = contents_data.get(position).getImages().toString().replace('[', ' ');
                str = str.replace(']', ' ');
                str = str.trim();
                Glide.with(NewsFragment.this)
                        .load(str)
                        .into(imageView);
            } else
                imageView.setVisibility(imageView.GONE);
            textView.setText(contents_data.get(position).getTitle());
            return frameLayout;
        }
    }


}
