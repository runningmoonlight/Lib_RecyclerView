package com.running.moonlight.lrecyclerview.adapter;

import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by liuheng on 2018/12/14.
 */
public class CommonVH extends RecyclerView.ViewHolder {

	private SparseArray<View> mViews;
	private View mItemView;
	private View.OnClickListener mOnClickListener;
	private View.OnLongClickListener mOnLongClickListener;

	public CommonVH(View itemView, View.OnClickListener onClickListener, View.OnLongClickListener onLongClickListener) {
		super(itemView);
		this.mOnClickListener = onClickListener;
		this.mOnLongClickListener = onLongClickListener;
		this.mItemView = itemView;
		mViews = new SparseArray<>();
	}

	public <T extends View> T getView(@IdRes int viewId) {
		View view = mViews.get(viewId);
		if (view == null) {
			view = mItemView.findViewById(viewId);
			mViews.put(viewId, view);
		}
		return (T) view;
	}

	public void setText(@IdRes int viewId, String text) {
		TextView textView = getView(viewId);
		textView.setText(text);
	}

	public void setText(@IdRes int viewId, @StringRes int stringResId) {
		TextView textView = getView(viewId);
		textView.setText(stringResId);
	}

	public void setImageResource(@IdRes int viewId, @DrawableRes int drawableResId) {
		ImageView imageView = getView(viewId);
		imageView.setImageResource(drawableResId);
	}

	public void setVisibility(@IdRes int viewId, boolean visible) {
		View view = getView(viewId);
		view.setVisibility(visible ? View.VISIBLE : View.GONE);
	}

	public void setOnClickListener(@IdRes int viewId, int position) {
		View view = getView(viewId);
		view.setTag(viewId, position);
		view.setOnClickListener(mOnClickListener);
	}

	public void setOnLongClickListener(@IdRes int viewId, int position) {
		View view = getView(viewId);
		view.setTag(viewId, position);
		view.setOnLongClickListener(mOnLongClickListener);
	}
}
