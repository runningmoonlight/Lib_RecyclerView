package com.running.moonlight.lrecyclerview.decoration;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.Px;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.running.moonlight.lrecyclerview.header.HeaderFrameLayout;


/**
 * Created by liuhengd on 2018/8/3.
 * {@link LinearLayoutManager}的分隔线
 */
public class DividerDecoration extends RecyclerView.ItemDecoration {

	@Px
	private int mDividerHeight;//分隔线高度
	@Px
	private int mLeftOffset;//分隔线左偏移
	@Px
	private int mTopOffset;//分隔线上偏移
	@Px
	private int mRightOffset;//分隔线右偏移
	@Px
	private int mBottomOffset;//分隔线下偏移

	private Paint mDividerPaint;//画笔
	private @ColorInt int mDividerColor;//分隔线颜色
	private int mOrientation = LinearLayoutManager.VERTICAL;//RecyclerView布局方向

	/**
	 * 用于绘制空白分隔线
	 * @param dividerHeight
	 */
	public DividerDecoration(@Px int dividerHeight) {
		this.mDividerHeight = dividerHeight;
		mDividerPaint = new Paint();
	}

	public DividerDecoration(@Px int dividerHeight, @Px int leftOffset, @Px int topOffset,
							 @Px int rightOffset, @Px int bottomOffset, @ColorInt int dividerColor) {
		this.mDividerHeight = dividerHeight;
		this.mLeftOffset = leftOffset;
		this.mTopOffset = topOffset;
		this.mRightOffset = rightOffset;
		this.mBottomOffset = bottomOffset;
		this.mDividerColor = dividerColor;

		mDividerPaint = new Paint();
		mDividerPaint.setColor(dividerColor);
	}


	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		if (mOrientation == LinearLayoutManager.HORIZONTAL) {
			outRect.set(0, 0, mDividerHeight, 0);
		} else {
			//对于LRecyclerView，header和footer不显示分隔线，refreshHeaderView有分隔线
			if (view instanceof HeaderFrameLayout)
				return;

			outRect.set(0, 0, 0, mDividerHeight);
		}
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
		if (mOrientation == LinearLayoutManager.HORIZONTAL) {
			drawHorizontal(c, parent);
		} else {
			drawVertical(c, parent);
		}
	}

	private void drawVertical(Canvas c, RecyclerView parent) {
		if (mDividerColor == 0)//当做透明分隔线，不需要绘制
			return;

		int left = parent.getPaddingLeft();
		int right = parent.getWidth() - parent.getPaddingRight();
		int childCount = parent.getChildCount();
		for (int i = 0; i <= childCount - 1; i++) {
			View childView = parent.getChildAt(i);
			RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) childView.getLayoutParams();
			int top = childView.getBottom() + params.bottomMargin;
			int bottom = top + mDividerHeight;
			c.drawRect(left + mLeftOffset, top + mTopOffset, right + mRightOffset, bottom + mBottomOffset, mDividerPaint);
		}
	}

	private void drawHorizontal(Canvas c, RecyclerView parent) {
		if (mDividerColor == 0)//当做透明分隔线，不需要绘制
			return;

		int top = parent.getPaddingTop();
		int bottom = parent.getHeight() - parent.getPaddingBottom();
		int childCount = parent.getChildCount();
		for (int i = 0; i <= childCount - 1; i++) {
			View childView = parent.getChildAt(i);
			RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) childView.getLayoutParams();
			int left = childView.getRight() + params.rightMargin;
			int right = left + mDividerHeight;
			c.drawRect(left + mLeftOffset, top + mTopOffset, right + mRightOffset, bottom + mBottomOffset, mDividerPaint);
		}
	}
}
