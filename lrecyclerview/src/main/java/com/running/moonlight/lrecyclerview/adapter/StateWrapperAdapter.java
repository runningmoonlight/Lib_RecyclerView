package com.running.moonlight.lrecyclerview.adapter;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by liuheng on 2018/8/2.
 * RecyclerView状态的包装Adapter
 * 四种状态：Normal、Loading、Empty、Error
 *
 */
public class StateWrapperAdapter extends RecyclerView.Adapter {

	public static final int STATE_NORMAL = 0;
	public static final int STATE_LOADING = 1;
	public static final int STATE_EMPTY = 2;
	public static final int STATE_ERROR = 3;

	private final View mLoadingView;
	private final View mEmptyView;
	private final View mErrorView;

	private final RecyclerView.Adapter mAdapter;

	@IntDef({STATE_NORMAL, STATE_LOADING, STATE_EMPTY, STATE_ERROR})
	@Retention(RetentionPolicy.SOURCE)
	public @interface State {
	}
	private int mState = STATE_NORMAL;

	private View.OnClickListener mEmptyClickListener;
	private View.OnClickListener mErrorClickListener;

	public StateWrapperAdapter(RecyclerView.Adapter adapter, View loadingView, View emptyView, View errorView) {
		this.mAdapter = adapter;
		this.mLoadingView = loadingView;
		this.mEmptyView = emptyView;
		this.mErrorView = errorView;

		registerObserver();
	}

	public StateWrapperAdapter(Context context, ViewGroup parent, RecyclerView.Adapter adapter, @LayoutRes int loadingRes, @LayoutRes int emptyRes, @LayoutRes int errorRes) {
		this.mAdapter = adapter;
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		this.mLoadingView = layoutInflater.inflate(loadingRes, parent, false);
		this.mEmptyView = layoutInflater.inflate(emptyRes, parent, false);
		this.mErrorView = layoutInflater.inflate(errorRes, parent, false);

		registerObserver();
	}

	private void registerObserver() {
		mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
			@Override
			public void onChanged() {
				StateWrapperAdapter.this.notifyDataSetChanged();
			}

			@Override
			public void onItemRangeChanged(int positionStart, int itemCount) {
				StateWrapperAdapter.this.notifyItemRangeChanged(positionStart, itemCount);
			}

			@Override
			public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
				StateWrapperAdapter.this.notifyItemRangeChanged(positionStart, itemCount, payload);
			}

			@Override
			public void onItemRangeInserted(int positionStart, int itemCount) {
				StateWrapperAdapter.this.notifyItemRangeInserted(positionStart, itemCount);
			}

			@Override
			public void onItemRangeRemoved(int positionStart, int itemCount) {
				StateWrapperAdapter.this.notifyItemRangeRemoved(positionStart, itemCount);
			}

			@Override
			public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
				StateWrapperAdapter.this.notifyDataSetChanged();
			}
		});
	}

	public void setState(@State int state) {
		this.mState = state;
		mAdapter.notifyDataSetChanged();
//		notifyDataSetChanged();
	}

	public void setOnEmptyClickListener(View.OnClickListener listener) {
		this.mEmptyClickListener = listener;
	}

	public void setOnErrorClickListener(View.OnClickListener listener) {
		this.mErrorClickListener = listener;
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		switch (viewType) {
			case STATE_LOADING:
				return new SimpleVH(mLoadingView);
			case STATE_EMPTY:
				mEmptyView.setOnClickListener(mEmptyClickListener);
				return new SimpleVH(mEmptyView);
			case STATE_ERROR:
				mErrorView.setOnClickListener(mErrorClickListener);
				return new SimpleVH(mErrorView);
			default:
				return mAdapter.onCreateViewHolder(parent, viewType);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		switch (mState) {
			case STATE_LOADING:
			case STATE_EMPTY:
			case STATE_ERROR:
				break;
			default:
				mAdapter.onBindViewHolder(holder, position);
		}
	}

	@Override
	public int getItemCount() {
//		Log.i(TAG, "getItemCount()，mState-->" + mState);
		switch (mState) {
			case STATE_LOADING:
			case STATE_EMPTY:
			case STATE_ERROR:
				return 1;
			default:
				return mAdapter.getItemCount();
		}
	}

	@Override
	public int getItemViewType(int position) {
		return mState;//这里返回不同的mState，才会重新调用onCreateViewHolder
	}

}
