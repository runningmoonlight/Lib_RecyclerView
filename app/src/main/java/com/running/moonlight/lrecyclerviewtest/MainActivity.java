package com.running.moonlight.lrecyclerviewtest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.running.moonlight.lrecyclerview.LRecyclerView;
import com.running.moonlight.lrecyclerview.adapter.CommonAdapter;
import com.running.moonlight.lrecyclerview.adapter.CommonVH;
import com.running.moonlight.lrecyclerview.adapter.StateWrapperAdapter;
import com.running.moonlight.lrecyclerview.footer.ILoadMoreTrigger;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LRecyclerView mLRecyclerView;
    private CommonAdapter<String> mCommonAdapter;
    private StateWrapperAdapter mWrapperAdapter;

    private List<String> mData = new ArrayList<>(20);
    private int page = 1;
    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLRecyclerView = findViewById(R.id.lrv);
        mLRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        View empty = LayoutInflater.from(this).inflate(R.layout.view_empty, mLRecyclerView, false);
        View error = LayoutInflater.from(this).inflate(R.layout.view_error, mLRecyclerView, false);
        View loading = LayoutInflater.from(this).inflate(R.layout.view_loading, mLRecyclerView, false);
        mCommonAdapter = new CommonAdapter<String>() {
            @Override
            public int initLayoutId(int viewType) {
                return R.layout.item_test;
            }

            @Override
            public void convert(CommonVH vh, int position, String s) {
                vh.setText(R.id.tv_item, s);
                vh.setImageResource(R.id.iv_item, R.drawable.ic_refresh_arrow);
                vh.setOnClickListener(R.id.tv_item, position);
                vh.setOnLongClickListener(R.id.iv_item, position);

//				ImageView imageView = vh.getView(R.id.tv_item);
                //Glide.with(DemoActivity.this).load(url).into(imageView);
            }
        };
        mCommonAdapter.setOnClickAction(new CommonAdapter.OnClickAction<String>() {
            @Override
            public void onClick(View view, int position, String s) {
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
            }
        });

        mCommonAdapter.setOnLongClickAction(new CommonAdapter.OnLongClickAction<String>() {
            @Override
            public boolean onLongClick(View view, int position, String s) {
                Toast.makeText(MainActivity.this, s, Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        mCommonAdapter.setDataList(mData);

        mWrapperAdapter = new StateWrapperAdapter(this, mLRecyclerView, mCommonAdapter, R.layout.view_loading, R.layout.view_empty, R.layout.view_error);

        mLRecyclerView.setAdapter(mWrapperAdapter);

        mLRecyclerView.setHeaderView(R.layout.view_header);
        mLRecyclerView.setFooterView(R.layout.view_footer);

        mLRecyclerView.setOnRefreshListener(new LRecyclerView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
                mCommonAdapter.notifyDataSetChanged();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLRecyclerView.completeRefresh(false);
                        mWrapperAdapter.setState(StateWrapperAdapter.STATE_EMPTY);
                    }
                }, 3000);
            }
        });

        mLRecyclerView.setOnLoadMoreListener(new LRecyclerView.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                nextPageData();
                mCommonAdapter.notifyDataSetChanged();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mLRecyclerView.completeLoadMore(ILoadMoreTrigger.STATE_NO_MORE);
                    }
                }, 3000);
            }
        });
    }


    private List<String> refreshData() {
        mData.clear();
        page = 1;
        for (int i = 0; i < 10; i++) {
            mData.add("test~~~i--" + i + "--page--" + page);
        }
        return mData;
    }

    private List<String> nextPageData() {
        page++;
        for (int i = 0; i < 10; i++) {
            mData.add("test~~~i--" + i + "--page--" + page);
        }
        return mData;
    }
}
