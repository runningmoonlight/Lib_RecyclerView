package com.running.moonlight.lrecyclerview.indexbar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.running.moonlight.lrecyclerview.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by liuheng on 2018/10/29.
 * 列表页的右侧索引栏，配合{@link com.running.moonlight.lrecyclerview.LRecyclerView}使用
 */
public class IndexBar extends View {

	private static final String TAG = IndexBar.class.getSimpleName();

	private static final String[] INDEX_TAGS = {"A", "B", "C", "D", "E", "F", "G", "H", "I",
			"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
			"W", "X", "Y", "Z", "#"};//#是默认索引数据标签，在最后位置

	private int mWidth;
	private int mHeight;
	private int mTagHeight;
	private Paint mPaint;

	//------可代码或xml设置的IndexBar属性，建议在xml中设置
	private boolean mNeedRealIndex = true;//是否用实际数据来生成索引tag，默认为true
	private int mNormalBackground = 0x00000000;//背景，默认为透明
	private int mPressBackground = 0x39000000;//手指按下时的背景色
	private int mTextSize;//默认15dp
	private int mTextColor = Color.BLACK;

	//------需要配合RecyclerView设置的属性
	private TextView mPressedShowTv;//触摸IndexBar时，特写显示的TextView
	private List<? extends BaseIndexBean> mSourceList;//数据源
	private LinearLayoutManager mLayoutManager;//RecyclerView的LayoutManager
	private int mHeaderViewCount;//RecyclerView的header数

	private List<String> mIndexTags;//索引tags
	private OnIndexPressedListener mIndexPressedListener = new OnIndexPressedListener() {
		@Override
		public void onIndexPressed(int index, String tag) {
			if (mPressedShowTv != null) {
				mPressedShowTv.setVisibility(VISIBLE);
				mPressedShowTv.setText(tag);
			}
			if (mLayoutManager != null) {
				int position = getPositionByTag(tag);
				if (position != -1) {
					mLayoutManager.scrollToPositionWithOffset(position, 0);
				}
			}
		}

		@Override
		public void onMotionEventEnd() {
			if (mPressedShowTv != null) {
				mPressedShowTv.setVisibility(GONE);
			}
		}
	};

	public interface OnIndexPressedListener {
		void onIndexPressed(int index, String text);
		void onMotionEventEnd();
	}

	public IndexBar(Context context) {
		this(context, null);
	}

	public IndexBar(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public IndexBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15, getResources().getDisplayMetrics());

		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.IndexBar, defStyleAttr, 0);
		for (int i = 0, n = a.getIndexCount(); i < n; i++) {
			int attr = a.getIndex(i);
			if (attr == R.styleable.IndexBar_indexBarPressBackground) {
				mPressBackground = a.getColor(attr, mPressBackground);
			} else if (attr == R.styleable.IndexBar_indexBarTextColor) {
				mTextColor = a.getColor(attr, mTextColor);
			} else if (attr == R.styleable.IndexBar_indexBarTextSize) {
				mTextSize = a.getDimensionPixelSize(attr, mTextSize);
			} else if (attr == R.styleable.IndexBar_indexBarNeedRealIndex) {
				mNeedRealIndex = a.getBoolean(attr, true);
			}
		}
		a.recycle();

		if (getBackground() != null && getBackground() instanceof ColorDrawable) {
			mNormalBackground = ((ColorDrawable) getBackground()).getColor();
		}

		if (mNeedRealIndex) {
			mIndexTags = new ArrayList<>(30);
		} else {
			mIndexTags = Arrays.asList(INDEX_TAGS);
		}

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setTextSize(mTextSize);
		mPaint.setColor(mTextColor);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int wMode = MeasureSpec.getMode(widthMeasureSpec);
		int wSize = MeasureSpec.getSize(widthMeasureSpec);
		int hMode = MeasureSpec.getMode(heightMeasureSpec);
		int hSize = MeasureSpec.getSize(heightMeasureSpec);

		int measureWidth = 0;
		int measureHeight = 0;
		Rect indexRect = new Rect();
		String indexTag;
		for (int i = 0, size = mIndexTags.size(); i < size; i++) {
			indexTag = mIndexTags.get(i);
			mPaint.getTextBounds(indexTag, 0, indexTag.length(), indexRect);
			measureWidth = Math.max(indexRect.width(), measureWidth);//测量单个tag的最大高宽
			measureHeight = Math.max(indexRect.height(), measureHeight);
		}
		measureHeight = measureHeight * mIndexTags.size();//计算所有tag的高

		switch (wMode) {
			case MeasureSpec.EXACTLY:
				measureWidth = wSize;
				break;
			case MeasureSpec.AT_MOST:
				measureWidth = Math.min(wSize, measureWidth);
				break;
			case MeasureSpec.UNSPECIFIED:
				break;
		}
		switch (hMode) {
			case MeasureSpec.EXACTLY:
				measureHeight = hSize;
				break;
			case MeasureSpec.AT_MOST:
				measureHeight = Math.min(hSize, measureHeight);
				break;
			case MeasureSpec.UNSPECIFIED:
				break;
		}

		setMeasuredDimension(measureWidth, measureHeight);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		int top = getPaddingTop();
		String indexTag;
		Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
		//单个tag的上边界到top线(bottom线到下边界)的距离
		int offset = (int) ((mTagHeight - fontMetrics.bottom + fontMetrics.top) / 2);
		//计算baseLine
		int baseLine = (int) (mTagHeight - offset - fontMetrics.bottom);
		for (int i = 0, size = mIndexTags.size(); i < size; i++) {
			indexTag = mIndexTags.get(i);
			canvas.drawText(indexTag, mWidth / 2 - mPaint.measureText(indexTag) / 2,
					top + mTagHeight * i + baseLine, mPaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				setBackgroundColor(mPressBackground);
				//这里没有break
			case MotionEvent.ACTION_MOVE:
				float y = event.getY();
				int pressIndex = (int) ((y - getPaddingTop()) / mTagHeight);
				//控制边界0~mIndexTags.size()
				pressIndex = Math.max(0, Math.min(pressIndex, mIndexTags.size()));
				mIndexPressedListener.onIndexPressed(pressIndex, mIndexTags.get(pressIndex));
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				setBackgroundColor(mNormalBackground);
				mIndexPressedListener.onMotionEventEnd();
				break;
		}
		return true;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);

		mWidth = w;
		mHeight = h;
		if (mIndexTags == null || mIndexTags.isEmpty())
			return;
		computeTagHeight();
	}

	private void computeTagHeight() {
		mTagHeight = (mHeight - getPaddingTop() - getPaddingBottom()) / mIndexTags.size();
	}

	private int getPositionByTag(String tag) {
		if (mSourceList == null || mSourceList.isEmpty()) {
			return -1;
		}
		if (TextUtils.isEmpty(tag)) {
			return -1;
		}
		for (int i = 0, size = mSourceList.size(); i < size; i++) {
			if (tag.equals(mSourceList.get(i).getIndexTag())) {
				return i + mHeaderViewCount;
			}
		}
		return -1;
	}

	public void setNeedRealIndex(boolean needRealIndex) {
		if (mNeedRealIndex == needRealIndex)
			return;

		this.mNeedRealIndex = needRealIndex;
		if (mNeedRealIndex) {
			mIndexTags = new ArrayList<>(30);
		} else {
			mIndexTags = Arrays.asList(INDEX_TAGS);
		}
	}

	public void setPressedBackground(int color) {
		this.mPressBackground = color;
	}

	public void setNormalBackground(int color) {
		this.mNormalBackground = color;
		setBackgroundColor(mNormalBackground);
	}

	public void setTextSize(int textSize) {
		this.mTextSize = textSize;
	}

	public void setTextColor(int textColor) {
		this.mTextColor = textColor;
	}

	//----------------------------------------------------------------

	public IndexBar setPressedTextView(TextView textView) {
		this.mPressedShowTv = textView;
		return this;
	}

	public IndexBar setSourceList(List<? extends BaseIndexBean> sourceList) {
		this.mSourceList = sourceList;

		if (mSourceList == null || mSourceList.isEmpty()) {
			return this;
		}
		IndexBarDataHelper.sortSourceData(mSourceList);

		if (mNeedRealIndex) {
			IndexBarDataHelper.flushSortedIndexData(mSourceList, mIndexTags);
			computeTagHeight();
		}
		return this;
	}

	public IndexBar setLayoutManager(LinearLayoutManager layoutManager) {
		this.mLayoutManager = layoutManager;
		return this;
	}

	public IndexBar setHeaderViewCount(int headerViewCount) {
		this.mHeaderViewCount = headerViewCount;
		return this;
	}

}
