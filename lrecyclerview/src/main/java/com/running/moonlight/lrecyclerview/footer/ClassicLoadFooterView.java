package com.running.moonlight.lrecyclerview.footer;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.running.moonlight.lrecyclerview.R;


/**
 * Created by liuhengd on 2018/7/27.
 * 加载更多的footer示例
 */
public class ClassicLoadFooterView extends RelativeLayout implements ILoadMoreTrigger {

	private TextView mTvLoad;
	private ProgressBar mPbLoad;

	public ClassicLoadFooterView(Context context) {
		this(context, null);
	}

	public ClassicLoadFooterView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ClassicLoadFooterView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		inflate(context, R.layout.view_classic_load_footer, this);
		mTvLoad = findViewById(R.id.tv_load_more);
		mPbLoad = findViewById(R.id.pb_load_more);
		setVisibility(GONE);
	}

	@Override
	public void onLoadingMore() {
		setVisibility(VISIBLE);
		mTvLoad.setVisibility(VISIBLE);
		mTvLoad.setText(R.string.rv_footer_loading);
		mPbLoad.setVisibility(VISIBLE);
	}

	@Override
	public void onComplete(int state) {
		switch (state) {
			case STATE_NO_MORE:
				mPbLoad.setVisibility(GONE);
				mTvLoad.setVisibility(VISIBLE);
				mTvLoad.setText(R.string.rv_footer_no_more);
				break;
			case STATE_ERROR:
				mPbLoad.setVisibility(GONE);
				mTvLoad.setVisibility(VISIBLE);
				mTvLoad.setText(R.string.rv_footer_load_error);
				break;
			case STATE_NORMAL:
//				setVisibility(GONE);
				mPbLoad.setVisibility(GONE);
				mTvLoad.setVisibility(VISIBLE);
				mTvLoad.setText(R.string.rv_footer_normal);
				break;
		}
	}

	@Override
	public void reset() {
		setVisibility(VISIBLE);
		mPbLoad.setVisibility(GONE);
		mTvLoad.setVisibility(VISIBLE);
		mTvLoad.setText(R.string.rv_footer_normal);
	}
}
