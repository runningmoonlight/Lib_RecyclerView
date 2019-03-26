package com.running.moonlight.lrecyclerview.header;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by liuhengd on 2018/7/30.
 * 刷新headerView的ViewGroup，自定义了onMeasure()和onLayout()方法
 * 控制刷新view显示在下边缘
 */
public class RefreshHeaderLayout extends ViewGroup {

	public RefreshHeaderLayout(Context context) {
		super(context);
	}

	public RefreshHeaderLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public RefreshHeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
	}

	@Override
	protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
		return new LayoutParams(p);
	}

	@Override
	public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
		return new LayoutParams(getContext(), attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (getChildCount() > 0) {
			// 这里childHeightMeasureSpec设置为UNSPECIFIED
			// refreshHeaderView的高度必须设置为具体的值，这样MeasureSpec转换为EXACTLY和对应的值，否则，高度会是0
			// 这样做，refreshHeaderView绘制完整，在onLayout()中控制下边缘和ViewGroup下边缘对齐
			// 如果使用heightMeasureSpec，refreshHeaderView的高度很可能和Container的高度一致，导致refreshHeaderView显示不全时显示上半部分
			int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			View childView = getChildAt(0);
			measureChildWithMargins(childView, widthMeasureSpec, 0, childHeightMeasureSpec, 0);
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();

		int paddingLeft = getPaddingLeft();
		int paddingTop = getPaddingTop();
		int paddingRight = getPaddingRight();
		int paddingBottom = getPaddingBottom();

		if (getChildCount() > 0) {
			View childView = getChildAt(0);
			int childWidth = childView.getMeasuredWidth();
			int childHeight = childView.getMeasuredHeight();

			MarginLayoutParams marginLayoutParams = (MarginLayoutParams) childView.getLayoutParams();
			int childLeft = l + paddingLeft + marginLayoutParams.leftMargin;
			// 这里，childTop的计算保证childView在一直在下边缘的位置
//			int childTop = t + paddingTop + marginLayoutParams.topMargin + height - childHeight - marginLayoutParams.bottomMargin - paddingBottom;//t会造成位置偏移
			int childTop = paddingTop + marginLayoutParams.topMargin + height - childHeight - marginLayoutParams.bottomMargin - paddingBottom;
			int childRight = childLeft + childWidth;
			int childBottom = childTop + childHeight;

			childView.layout(childLeft, childTop, childRight, childBottom);
		}
	}

	public static class LayoutParams extends MarginLayoutParams {

		public LayoutParams(Context c, AttributeSet attrs) {
			super(c, attrs);
		}

		public LayoutParams(int width, int height) {
			super(width, height);
		}

		public LayoutParams(MarginLayoutParams source) {
			super(source);
		}

		public LayoutParams(ViewGroup.LayoutParams source) {
			super(source);
		}
	}
}
