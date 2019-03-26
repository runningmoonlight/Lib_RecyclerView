package com.running.moonlight.lrecyclerview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import com.running.moonlight.lrecyclerview.adapter.SimpleVH;
import com.running.moonlight.lrecyclerview.footer.ILoadMoreTrigger;
import com.running.moonlight.lrecyclerview.header.HeaderFrameLayout;
import com.running.moonlight.lrecyclerview.header.IRefreshTrigger;
import com.running.moonlight.lrecyclerview.header.RefreshHeaderLayout;


/**
 * Created by liuhengd on 2018/7/25.
 * 在RecyclerView中封装了刷新View、header、footer、加载更多view，实现下拉刷新和上拉加载更多功能
 * 刷新view和加载更多view可以写在布局中，也可以代码设置
 * 当代码中控制开始刷新和下拉松手刷新时，刷新操作和动画同步进行
 */
public class LRecyclerView extends RecyclerView {

	private static final String TAG = LRecyclerView.class.getSimpleName();

	private static final int STATE_NORMAL = 0;//不可见
	private static final int STATE_SWIPING_TO_REFRESH = 1;//下拉距离小于刷新View的高度
	private static final int STATE_RELEASE_TO_REFRESH = 2;//下拉距离大于刷新View的高度
	private static final int STATE_REFRESHING = 3;//刷新中

	private int mState = STATE_NORMAL;

	private WrapperAdapter mWrapperAdapter;

	private RefreshHeaderLayout mRefreshHeaderL;//刷新View的容器
	private HeaderFrameLayout mLoadMoreFooterFL;//加载更多View的容器
	private View mRefreshHeaderView;//刷新View
	private View mLoadMoreFooterView;//加载更多View
	private HeaderFrameLayout mHeaderViewFL;//headerView的容器
	private HeaderFrameLayout mFooterViewFL;//footerView的容器

	private boolean mRefreshEnable;//是否可下拉刷新
	private boolean mLoadMoreEnable;//是否能加载更多
	private int mRefreshFinalMoveOffset;//刷新View的最大下拉距离

	private OnRefreshListener mOnRefreshListener;//下拉刷新的listener
	private OnLoadMoreListener mOnLoadMoreListener;//加载更多的listener

	private boolean mFooterLoading;//是否正在加载更多
	private boolean mNoMore;//是否没有更多

	public LRecyclerView(Context context) {
		this(context, null);
	}

	public LRecyclerView(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		@LayoutRes int refreshHeaderLayoutRes = -1;
		@LayoutRes int loadMoreFooterLayoutRes = -1;

		if (attrs != null) {
			TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LRecyclerView, defStyle, 0);
			for (int i = 0, n = a.getIndexCount(); i < n; i++) {
				int attr = a.getIndex(i);
				if (attr == R.styleable.LRecyclerView_refreshHeaderLayout) {
					refreshHeaderLayoutRes = a.getResourceId(R.styleable.LRecyclerView_refreshHeaderLayout, -1);
				} else if (attr == R.styleable.LRecyclerView_loadMoreFooterLayout) {
					loadMoreFooterLayoutRes = a.getResourceId(R.styleable.LRecyclerView_loadMoreFooterLayout, -1);
				} else if (attr == R.styleable.LRecyclerView_refreshEnable) {
					mRefreshEnable = a.getBoolean(R.styleable.LRecyclerView_refreshEnable, false);
				} else if (attr == R.styleable.LRecyclerView_loadMoreEnable) {
					mLoadMoreEnable = a.getBoolean(R.styleable.LRecyclerView_loadMoreEnable, false);
				} else if (attr == R.styleable.LRecyclerView_refreshFinalMoveOffset) {
					mRefreshFinalMoveOffset = a.getDimensionPixelOffset(R.styleable.LRecyclerView_refreshFinalMoveOffset, 0);
				}
			}
			a.recycle();
		}

		initHeaderAndFooterView();

		if (refreshHeaderLayoutRes != -1) {
			setRefreshHeaderView(refreshHeaderLayoutRes);
		}
		if (loadMoreFooterLayoutRes != -1) {
			setLoadMoreFooterView(loadMoreFooterLayoutRes);
		}
	}

	private void initHeaderAndFooterView() {
		mRefreshHeaderL = new RefreshHeaderLayout(getContext());
		mRefreshHeaderL.setLayoutParams(new RefreshHeaderLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));

		mHeaderViewFL = new HeaderFrameLayout(getContext());
		mHeaderViewFL.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		mFooterViewFL = new HeaderFrameLayout(getContext());
		mFooterViewFL.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

		mLoadMoreFooterFL = new HeaderFrameLayout(getContext());
		mLoadMoreFooterFL.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
	}

	@Override
	protected void onMeasure(int widthSpec, int heightSpec) {
		super.onMeasure(widthSpec, heightSpec);
		if (mRefreshHeaderView != null) {//最大下拉距离必须大于刷新view的高度，否则设置无效
			if (mRefreshFinalMoveOffset < mRefreshHeaderView.getMeasuredHeight()) {
				mRefreshFinalMoveOffset = 0;
			}
		}
	}

	public void setRefreshEnable(boolean enable) {
		this.mRefreshEnable = enable;
	}

	public void setLoadMoreEnable(boolean enable) {
		this.mLoadMoreEnable = enable;
	}

	public void setOnRefreshListener(OnRefreshListener refreshListener) {
		this.mOnRefreshListener = refreshListener;
	}

	public void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener) {
		this.mOnLoadMoreListener = loadMoreListener;
	}

	public void setRefreshFinalMoveOffset(int refreshFinalMoveOffset) {
		this.mRefreshFinalMoveOffset = refreshFinalMoveOffset;
	}

	public void setRefreshHeaderView(@LayoutRes int refreshHeaderLayoutRes) {
		View refreshHeaderView = LayoutInflater.from(getContext()).inflate(refreshHeaderLayoutRes, mRefreshHeaderL, false);
		setRefreshHeaderView(refreshHeaderView);
		setHasFixedSize(true);//有下拉刷新，说明高宽确定，所以这里设置为true
	}

	public void setRefreshHeaderView(View refreshHeaderView) {
		mRefreshHeaderL.removeAllViews();
		mRefreshHeaderL.addView(refreshHeaderView);
		this.mRefreshHeaderView = refreshHeaderView;

		//测量mRefreshHeaderView的高宽
		//这里做计算，是因为进入界面首次刷新，mRefreshHeaderView的高度可能还没计算出来，手动计算，采用mRefreshHeaderL测量
		int screenWidth = mRefreshHeaderL.getContext().getResources().getDisplayMetrics().widthPixels;
		int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(screenWidth, MeasureSpec.UNSPECIFIED);
		int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		mRefreshHeaderL.measure(widthMeasureSpec, heightMeasureSpec);
	}

	public void setLoadMoreFooterView(@LayoutRes int loadMoreFooterLayoutRes) {
		View loadMoreFooterView = LayoutInflater.from(getContext()).inflate(loadMoreFooterLayoutRes, mLoadMoreFooterFL, false);
		setLoadMoreFooterView(loadMoreFooterView);
	}

	public void setLoadMoreFooterView(View loadMoreFooterView) {
		mLoadMoreFooterFL.removeAllViews();
		mLoadMoreFooterFL.addView(loadMoreFooterView);
		this.mLoadMoreFooterView = loadMoreFooterView;
	}

	public void setHeaderView(@LayoutRes int headerRes) {
//		View headerView = LayoutInflater.from(getContext()).inflate(headerRes, mHeaderViewFL, false);
		LayoutInflater.from(getContext()).inflate(headerRes, mHeaderViewFL);
	}

	public void setHeaderView(View headerView) {
		mHeaderViewFL.removeAllViews();
		mHeaderViewFL.addView(headerView);
	}

	public void setFooterView(@LayoutRes int footerRes) {
		LayoutInflater.from(getContext()).inflate(footerRes, mFooterViewFL);
	}

	public void setFooterView(View footerView) {
		mFooterViewFL.removeAllViews();
		mFooterViewFL.addView(footerView);
	}

	/**
	 * 开始刷新，请求数据前调用
	 */
	public void beginRefresh() {
		if (mState == STATE_NORMAL && mRefreshEnable && mRefreshHeaderView != null && mOnRefreshListener != null) {
			smoothScroll(mRefreshHeaderL.getMeasuredHeight(), mRefreshHeaderView.getMeasuredHeight(), 200);
			mState = STATE_REFRESHING;
			mRefreshTrigger.onRefresh();

			mOnRefreshListener.onRefresh();
		}
	}

	/**
	 * 刷新完成，请求数据完成时调用
	 * @param isSuccess 是否刷新成功
	 */
	public void completeRefresh(boolean isSuccess) {
		mNoMore = false;
		if (mState == STATE_REFRESHING) {
			smoothScroll(mRefreshHeaderL.getMeasuredHeight(), 0, 300);
			mState = STATE_NORMAL;
			mRefreshTrigger.onComplete(isSuccess);

			//控制LoadMoreFooterView是否显示
			if (isSuccess
					&& mLoadMoreEnable && mLoadMoreFooterView != null && mOnLoadMoreListener != null) {
				postDelayed(new Runnable() {//数据还未刷新到RecyclerView的Adapter中，所以延时判断
					@Override
					public void run() {
						LayoutManager layoutManager = getLayoutManager();
						int totalItemCount = layoutManager.getItemCount();
						int visibleItemCount = layoutManager.getChildCount();
						if (visibleItemCount > 0 && totalItemCount > 5 && totalItemCount > visibleItemCount) {
							if (mLoadMoreFooterView instanceof ILoadMoreTrigger) {
								((ILoadMoreTrigger) mLoadMoreFooterView).reset();
							} else {
								mLoadMoreFooterView.setVisibility(GONE);
							}
						} else {
							mLoadMoreFooterView.setVisibility(GONE);
						}
					}
				}, 100);

			} else {
				if (mLoadMoreFooterView != null) {
					mLoadMoreFooterView.setVisibility(GONE);
				}
			}
		} else {//这种情况应该不会出现
			setRefreshHeaderLayoutHeight(0);
			mState = STATE_NORMAL;
			mRefreshTrigger.onReset();
		}
	}

	public void resetFooter() {
		if (mLoadMoreFooterView != null && mLoadMoreFooterView instanceof ILoadMoreTrigger) {
			((ILoadMoreTrigger) mLoadMoreFooterView).reset();
		}
	}

	/**
	 * 加载更多完成时调用
	 * @param state
	 * {@link ILoadMoreTrigger#STATE_NORMAL:通常状态(加载成功且有数据时传入)
	 * @link ILoadMoreTrigger#STATE_NO_MORE:没有更多
	 * @link ILoadMoreTrigger#TATE_ERROR:加载出错}
	 */
	public void completeLoadMore(int state) {
		mFooterLoading = false;
		if (state == ILoadMoreTrigger.STATE_NO_MORE) {
			mNoMore = true;
		}
		if (mLoadMoreFooterView == null)
			return;

		if (mLoadMoreFooterView instanceof ILoadMoreTrigger) {
			((ILoadMoreTrigger) mLoadMoreFooterView).onComplete(state);
		} else {
			mLoadMoreFooterView.setVisibility(GONE);
		}
	}

	@Override
	public void setAdapter(Adapter adapter) {
		if (mWrapperAdapter == null) {
			mWrapperAdapter = new WrapperAdapter(adapter);
			super.setAdapter(mWrapperAdapter);
		} else {
			mWrapperAdapter.setAdapter(adapter);
		}
	}

	/**
	 * 获取原本设置的Adapter
	 * @return 被包装的Adapter
	 */
	public Adapter getOriginalAdapter() {
		return mWrapperAdapter == null ? null : mWrapperAdapter.mAdapter;
	}

	private float mLastTouchY;

	@Override
	public boolean onInterceptTouchEvent(MotionEvent e) {

		switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastTouchY = e.getRawY();//这里必须写，RecyclerView默认处理滑动，ACTION_DOWN事件可能传不到onTouchEvent
				break;
		}
		return super.onInterceptTouchEvent(e);
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN:
				mLastTouchY = e.getRawY();
				break;
			case MotionEvent.ACTION_MOVE:
				float deltaY = e.getRawY() - mLastTouchY;
				mLastTouchY = e.getRawY();

				if (mRefreshEnable
						&& mRefreshHeaderView != null
						&& mOnRefreshListener != null
						&& getScrollState() == SCROLL_STATE_DRAGGING
						&& canTriggerRefresh()) {

					int refreshHeaderLayoutHeight = mRefreshHeaderL.getMeasuredHeight();
					int refreshHeaderViewHeight = mRefreshHeaderView.getMeasuredHeight();

					if (deltaY > 0 && mState == STATE_NORMAL) {
						mState = STATE_SWIPING_TO_REFRESH;
						mRefreshTrigger.onStart(refreshHeaderViewHeight);
					} else if (deltaY < 0) {
						if (mState == STATE_SWIPING_TO_REFRESH && refreshHeaderLayoutHeight <= 0) {
							mState = STATE_NORMAL;
						}
						if (mState == STATE_NORMAL) {
							break;
						}
					}

					if (mState == STATE_SWIPING_TO_REFRESH || mState == STATE_RELEASE_TO_REFRESH) {
						if (refreshHeaderLayoutHeight >= refreshHeaderViewHeight) {
							mState = STATE_RELEASE_TO_REFRESH;
						} else {
							mState = STATE_SWIPING_TO_REFRESH;
						}
						fingerMove(deltaY);
						return true;
					}
				}
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				if (mState == STATE_SWIPING_TO_REFRESH) {
					smoothScroll(mRefreshHeaderL.getMeasuredHeight(), 0, 300);
					mState = STATE_NORMAL;
				} else if (mState == STATE_RELEASE_TO_REFRESH) {
					smoothScroll(mRefreshHeaderL.getMeasuredHeight(), mRefreshHeaderView.getMeasuredHeight(), 300);
					mState = STATE_REFRESHING;
					mRefreshTrigger.onRefresh();
					if (mOnRefreshListener != null) {
						mOnRefreshListener.onRefresh();
					}
				}
				break;
			default:
				break;
		}
		return super.onTouchEvent(e);
	}

	@Override
	public void onScrollStateChanged(int state) {
		super.onScrollStateChanged(state);

		//上拉加载更多相关
		if (state == RecyclerView.SCROLL_STATE_IDLE
				&& mLoadMoreEnable
				&& mLoadMoreFooterView != null
				&& mOnLoadMoreListener != null
				&& !mFooterLoading
				&& !mNoMore) {
			LayoutManager layoutManager = getLayoutManager();
			int totalItemCount = layoutManager.getItemCount();
			int visibleItemCount = layoutManager.getChildCount();

			View lastChild = getChildAt(getChildCount() - 1);
			int position = getChildLayoutPosition(lastChild);

			if (visibleItemCount > 0 //itemView必须大于0
					&& totalItemCount > 5 //排除error或empty的情况(header+footer数量为4，errorView或者emptyView为1)
					&& totalItemCount > visibleItemCount //itemView必须占满一个屏幕
					&& position >= totalItemCount - 1) { //可见的最后一个itemView是最后一个itemView
				if (mLoadMoreFooterView instanceof ILoadMoreTrigger) {
					((ILoadMoreTrigger) mLoadMoreFooterView).onLoadingMore();
				}
				mOnLoadMoreListener.onLoadMore();
				mFooterLoading = true;
			}
		}
	}

	private void fingerMove(float deltaY) {
		int distance = (int) (deltaY * 0.5f + 0.5f);//刷新view滑动的距离为手指滑动距离的一半，产生阻尼效果
		int height = mRefreshHeaderL.getMeasuredHeight();

		int nextHeight = height + distance;
		if (mRefreshFinalMoveOffset > 0) {//控制下边界
			if (nextHeight > mRefreshFinalMoveOffset) {
				nextHeight = mRefreshFinalMoveOffset;
			}
		}
		if (nextHeight < 0) {//控制上边界
			nextHeight = 0;
		}
		setRefreshHeaderLayoutHeight(nextHeight);
		mRefreshTrigger.onMove(nextHeight);
	}

	private void smoothScroll(int fromValue, int toValue, int millisecond) {
		ValueAnimator valueAnimator = ValueAnimator.ofInt(fromValue, toValue);
		valueAnimator.setDuration(millisecond);
		valueAnimator.setInterpolator(new DecelerateInterpolator());
		valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				int height = (int) animation.getAnimatedValue();
				setRefreshHeaderLayoutHeight(height);
			}
		});
		valueAnimator.start();
	}

	private boolean canTriggerRefresh() {
		if (mWrapperAdapter == null) {
			return false;
		}
		View firstChild = getChildAt(0);
		int position = getChildLayoutPosition(firstChild);
		if (position == 0) {
			return firstChild.getTop() == mRefreshHeaderL.getTop();
		}
		return false;
	}

	private void setRefreshHeaderLayoutHeight(int height) {
		mRefreshHeaderL.getLayoutParams().height = height;
		mRefreshHeaderL.requestLayout();
	}

	private IRefreshTrigger mRefreshTrigger = new IRefreshTrigger() {
		@Override
		public void onStart(int headerHeight) {
			if (mRefreshHeaderView != null && mRefreshHeaderView instanceof IRefreshTrigger) {
				((IRefreshTrigger) mRefreshHeaderView).onStart(headerHeight);
			}
		}

		@Override
		public void onMove(int movedDistance) {
			if (mRefreshHeaderView != null && mRefreshHeaderView instanceof IRefreshTrigger) {
				((IRefreshTrigger) mRefreshHeaderView).onMove(movedDistance);
			}
		}

		@Override
		public void onRefresh() {
			if (mRefreshHeaderView != null && mRefreshHeaderView instanceof IRefreshTrigger) {
				((IRefreshTrigger) mRefreshHeaderView).onRefresh();
			}
		}

		@Override
		public void onComplete(boolean isSuccess) {
			if (mRefreshHeaderView != null && mRefreshHeaderView instanceof IRefreshTrigger) {
				((IRefreshTrigger) mRefreshHeaderView).onComplete(isSuccess);
			}
		}

		@Override
		public void onReset() {
			if (mRefreshHeaderView != null && mRefreshHeaderView instanceof IRefreshTrigger) {
				((IRefreshTrigger) mRefreshHeaderView).onReset();
			}
		}
	};

	public interface OnRefreshListener {
		void onRefresh();
	}

	public interface OnLoadMoreListener {
		void onLoadMore();
	}

	/**
	 * Created by liuhengd on 2018/7/31.
	 * 下拉刷新和上拉加载的包装Adapter
	 * 通过对Adapter包装，增加下拉刷新view、headerView、footerView和加载更多View
	 * 为简化处理itemView的类型和数量，四个view固定存在，但headerView和footerView只能有一个，扩展性一般
	 */
	private class WrapperAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

		private static final int TYPE_REFRESH_VIEW = Integer.MIN_VALUE;
		private static final int TYPE_HEADER_VIEW = Integer.MIN_VALUE + 1;
		private static final int TYPE_FOOTER_VIEW = Integer.MAX_VALUE - 1;
		private static final int TYPE_LOAD_MORE_VIEW = Integer.MAX_VALUE ;

		private RecyclerView.Adapter mAdapter;

		public WrapperAdapter(RecyclerView.Adapter adapter) {
			this.mAdapter = adapter;
			//mAdapter数据改变，通知Observer；Observer通知WrapperAdapter;WrapperAdapter通知RecyclerView更新UI。
			mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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
			});
		}

		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
			if (viewType == TYPE_REFRESH_VIEW) {
				return new SimpleVH(mRefreshHeaderL);
			} else if (viewType == TYPE_HEADER_VIEW) {
				return new SimpleVH(mHeaderViewFL);
			} else if (viewType == TYPE_FOOTER_VIEW) {
				return new SimpleVH(mFooterViewFL);
			} else if (viewType == TYPE_LOAD_MORE_VIEW) {
				return new SimpleVH(mLoadMoreFooterFL);
			} else {
				return mAdapter.onCreateViewHolder(parent, viewType);
			}
		}

		@Override
		public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
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

		private void setAdapter(RecyclerView.Adapter adapter) {
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
}
