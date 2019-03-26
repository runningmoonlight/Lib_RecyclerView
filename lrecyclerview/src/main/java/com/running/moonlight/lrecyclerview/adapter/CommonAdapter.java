package com.running.moonlight.lrecyclerview.adapter;


import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by liuhengd on 2018/7/19.
 * RecyclerView的通用Adapter，抽象实现，可免去创建单独的Adapter类
 * 可设置ItemView中view的文字、图片、可见性、单击和长按事件
 *
 */
public abstract class CommonAdapter<DATA> extends RecyclerView.Adapter<CommonVH> {

	private List<DATA> mDataList;//数据
	private OnClickAction<DATA> mOnClickAction;//点击事件的回调
	private OnLongClickAction<DATA> mOnLongClickAction;//长按事件的回调

	/**
	 * 一个Adapter对应唯一一个View.OnClickListener
	 * itemView中需要设置点击事件的view在{@link #onBindViewHolder}保存position
	 * 在回调中通过view的id来区分对应的点击事件
	 */
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mOnClickAction != null) {
				int position = (int) v.getTag(v.getId());
				mOnClickAction.onClick(v, position, mDataList.get(position));
			}
		}
	};
	/**
	 * 一个Adapter对应唯一一个View.OnLongClickListener
	 * 同上
	 */
	private View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			if (mOnLongClickAction != null) {
				int position = (int) v.getTag(v.getId());
				return mOnLongClickAction.onLongClick(v, position, mDataList.get(position));
			}
			return false;
		}
	};

	public CommonAdapter() {

	}

	public CommonAdapter(List<DATA> dataList) {
		this.mDataList = dataList;
	}

	public abstract @LayoutRes int initLayoutId(int viewType);

	public abstract void convert(CommonVH vh, int position, DATA data);

	@Override
	public CommonVH onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(parent.getContext()).inflate(initLayoutId(viewType), parent, false);
		return new CommonVH(itemView, mOnClickListener, mOnLongClickListener);
	}

	@Override
	public void onBindViewHolder(@NonNull CommonVH holder, int position) {
		convert(holder, position, mDataList.get(position));
	}

	@Override
	public int getItemCount() {
		return mDataList == null ? 0 : mDataList.size();
	}

	public void setDataList(List<DATA> dataList) {
		this.mDataList = dataList;
	}

	public void setOnClickAction(OnClickAction<DATA> onClickAction) {
		this.mOnClickAction = onClickAction;
	}

	public void setOnLongClickAction(OnLongClickAction<DATA> onLongClickAction) {
		this.mOnLongClickAction = onLongClickAction;
	}

	public interface OnClickAction<DATA> {
		void onClick(View view, int position, DATA data);
	}

	public interface OnLongClickAction<DATA> {
		boolean onLongClick(View view, int position, DATA data);
	}
}
