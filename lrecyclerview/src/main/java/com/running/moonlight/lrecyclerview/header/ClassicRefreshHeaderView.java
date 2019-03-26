package com.running.moonlight.lrecyclerview.header;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.running.moonlight.lrecyclerview.R;


/**
 * Created by liuhengd on 2018/7/26.
 * 自定义下拉刷新view的示例
 */
public class ClassicRefreshHeaderView extends RelativeLayout implements IRefreshTrigger {

	private ImageView mIvArrow;
	private TextView mTvRefresh;
	private ProgressBar mPbRefresh;

	private Animation mRotateUpAnim;
	private Animation mRotateDownAnim;

	private boolean mRotated;
	private int mHeight;

	public ClassicRefreshHeaderView(Context context) {
		this(context, null);
	}

	public ClassicRefreshHeaderView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ClassicRefreshHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		inflate(context, R.layout.view_classic_refresh_header, this);
		mTvRefresh = findViewById(R.id.tv_refresh);
		mIvArrow = findViewById(R.id.iv_arrow);
		mPbRefresh = findViewById(R.id.pb_refresh);

		mRotateUpAnim = AnimationUtils.loadAnimation(context, R.anim.rotate_up);
		mRotateDownAnim = AnimationUtils.loadAnimation(context, R.anim.rotate_down);
	}

	@Override
	public void onStart(int headerHeight) {
		this.mHeight = headerHeight;
	}

	@Override
	public void onMove(int movedDistance) {
		mIvArrow.setVisibility(VISIBLE);
		mPbRefresh.setVisibility(GONE);
		if (movedDistance <= mHeight) {
			if (mRotated) {
				mIvArrow.clearAnimation();
				mIvArrow.startAnimation(mRotateDownAnim);
				mRotated = false;
			}
			mTvRefresh.setText(R.string.rv_header_refresh_normal);
		} else {
			mTvRefresh.setText(R.string.rv_header_refresh_release);
			if (!mRotated) {
				mIvArrow.clearAnimation();
				mIvArrow.startAnimation(mRotateUpAnim);
				mRotated = true;
			}
		}
	}

	@Override
	public void onRefresh() {
		mIvArrow.clearAnimation();
		mIvArrow.setVisibility(GONE);
		mPbRefresh.setVisibility(VISIBLE);
		mTvRefresh.setText(R.string.rv_header_refreshing);
	}

	@Override
	public void onComplete(boolean isSuccess) {
		mRotated = false;
		mIvArrow.clearAnimation();
		mIvArrow.setVisibility(GONE);
		mPbRefresh.setVisibility(GONE);
		mTvRefresh.setText(isSuccess ? R.string.rv_header_refresh_done : R.string.rv_header_refresh_error);
	}

	@Override
	public void onReset() {
		mRotated = false;
		mIvArrow.clearAnimation();
		mIvArrow.setVisibility(GONE);
		mPbRefresh.setVisibility(GONE);
	}
}
