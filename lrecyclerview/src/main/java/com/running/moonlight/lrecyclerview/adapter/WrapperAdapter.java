package com.running.moonlight.lrecyclerview.adapter;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by liuheng on 2018/7/31.
 * 下拉刷新和上拉加载的包装Adapter
 * 通过对Adapter包装，增加下拉刷新view、headerView、footerView和加载更多View
 * 为简化处理itemView的类型和数量，四个view固定存在，但headerView和footerView只能有一个，扩展性一般
 *
 * 这个类仅用在LRecyclerView内，所以迁移为LRecyclerView的内部类。
 * 这个类的修饰符改为default，并添加@Deprecated；不直接删除，可供参考阅读（毕竟花了很长时间才写出来的）。
 */
@Deprecated
class WrapperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_REFRESH_VIEW = Integer.MIN_VALUE;
	private static final int TYPE_HEADER_VIEW = Integer.MIN_VALUE + 1;
	private static final int TYPE_FOOTER_VIEW = Integer.MAX_VALUE - 1;
	private static final int TYPE_LOAD_MORE_VIEW = Integer.MAX_VALUE ;

	private RecyclerView.Adapter mAdapter;
	private final View mRefreshView;
	private final View mHeaderView;
	private final View mFooterView;
	private final View mLoadMoreView;

	//mAdapter数据改变，通知mObserver；mObserver通知WrapperAdapter;WrapperAdapter通知RecyclerView更新UI。
	private RecyclerView.AdapterDataObserver mObserver = new RecyclerView.AdapterDataObserver() {
		@Override
		public void onChanged() {
			WrapperAdapter.this.notifyDataSetChanged();
		}

		@Override
		public void onItemRangeChanged(int positionStart, int itemCount) {
			WrapperAdapter.this.notifyItemRangeChanged(positionStart + 2, itemCount);
		}

		@Override
		public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
			WrapperAdapter.this.notifyItemRangeChanged(positionStart + 2, itemCount, payload);
		}

		@Override
		public void onItemRangeInserted(int positionStart, int itemCount) {
			WrapperAdapter.this.notifyItemRangeInserted(positionStart + 2, itemCount);
		}

		@Override
		public void onItemRangeRemoved(int positionStart, int itemCount) {
			WrapperAdapter.this.notifyItemRangeRemoved(positionStart + 2, itemCount);
		}

		@Override
		public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
			WrapperAdapter.this.notifyDataSetChanged();
		}
	};

	public WrapperAdapter(RecyclerView.Adapter adapter, View refreshView, View headerView, View footerView, View loadMoreView) {
		this.mAdapter = adapter;
		this.mRefreshView = refreshView;
		this.mHeaderView = headerView;
		this.mFooterView = footerView;
		this.mLoadMoreView = loadMoreView;

		mAdapter.registerAdapterDataObserver(mObserver);
	}

	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		if (viewType == TYPE_REFRESH_VIEW) {
			return new SimpleVH(mRefreshView);
		} else if (viewType == TYPE_HEADER_VIEW) {
			return new SimpleVH(mHeaderView);
		} else if (viewType == TYPE_FOOTER_VIEW) {
			return new SimpleVH(mFooterView);
		} else if (viewType == TYPE_LOAD_MORE_VIEW) {
			return new SimpleVH(mLoadMoreView);
		} else {
			return mAdapter.onCreateViewHolder(parent, viewType);
		}
	}

	@Override
	public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
		if (position > 1 && position < mAdapter.getItemCount() + 2) {
			mAdapter.onBindViewHolder(holder, position - 2);
		}
	}

	@Override
	public int getItemCount() {
		return mAdapter.getItemCount() + 4;
	}

	@Override
	public int getItemViewType(int position) {
		int itemCount = mAdapter.getItemCount();
		if (position == 0) {
			return TYPE_REFRESH_VIEW;
		} else if (position == 1) {
			return TYPE_HEADER_VIEW;
		} else if (position > 1 && position < itemCount + 2) {
			return mAdapter.getItemViewType(position);
		} else if (position == itemCount + 2) {
			return TYPE_FOOTER_VIEW;
		} else if (position == itemCount + 3) {
			return TYPE_LOAD_MORE_VIEW;
		}
		throw new IllegalArgumentException("The position = " + position + "has wrong type.");
	}

	@Override
	public void onAttachedToRecyclerView(final RecyclerView recyclerView) {
		RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
		if (layoutManager instanceof GridLayoutManager) {
			final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
			final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
			gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
				@Override
				public int getSpanSize(int position) {
					WrapperAdapter wrapperAdapter = (WrapperAdapter) recyclerView.getAdapter();
					if (isFullSpanType(wrapperAdapter.getItemViewType(position))) {
						return gridLayoutManager.getSpanCount();
					} else if (spanSizeLookup != null) {
						return spanSizeLookup.getSpanSize(position - 2);
					}
					return 1;
				}
			});
		}
		super.onAttachedToRecyclerView(recyclerView);
	}

	@Override
	public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
		int position = holder.getAdapterPosition();
		int type = getItemViewType(position);
		if (isFullSpanType(type)) {
			ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
			if (layoutParams instanceof StaggeredGridLayoutManager.LayoutParams) {
				((StaggeredGridLayoutManager.LayoutParams) layoutParams).setFullSpan(true);
			}
		}
		super.onViewAttachedToWindow(holder);
	}

	public RecyclerView.Adapter getAdapter() {
		return mAdapter;
	}

	public void setAdapter(RecyclerView.Adapter adapter) {
		this.mAdapter = adapter;
		notifyDataSetChanged();
	}

	private boolean isFullSpanType(int type) {
		return type == TYPE_REFRESH_VIEW
				|| type == TYPE_HEADER_VIEW
				|| type == TYPE_FOOTER_VIEW
				|| type == TYPE_LOAD_MORE_VIEW;
	}
}
