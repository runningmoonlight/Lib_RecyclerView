package com.running.moonlight.lrecyclerview.decoration;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;

/**
 * Created by liuhengd on 2018/8/3.
 * 吸顶效果
 */
public class SectionDecoration extends RecyclerView.ItemDecoration {
	private DecorationCallback mCallback;
	private TextPaint mTextPaint;
	private Paint mHeaderPaint;
	private int mPaddingLeft;
	private int mBaseLine;
	private int mTopHeight;//吸顶的高度
	private int mHeaderViewCount;

	public SectionDecoration(int headerColor, int textColor, int textSize, int headerHeight, int paddingLeft, int headerViewCount, DecorationCallback callback) {
		mHeaderPaint = new Paint();
		mHeaderPaint.setColor(headerColor);

		mTextPaint = new TextPaint();
		mTextPaint.setTypeface(Typeface.DEFAULT_BOLD);
		mTextPaint.setAntiAlias(true);
		mTextPaint.setTextSize(textSize);
		mTextPaint.setColor(textColor);
		mTextPaint.setTextAlign(Paint.Align.LEFT);
		Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
		mBaseLine = (headerHeight - (int)(fontMetrics.descent - fontMetrics.ascent)) / 2;

		this.mTopHeight = headerHeight;
		this.mPaddingLeft = paddingLeft;
		this.mHeaderViewCount = headerViewCount;
		this.mCallback = callback;
	}


	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		super.getItemOffsets(outRect, view, parent, state);
		int pos = parent.getChildAdapterPosition(view);
		pos -= mHeaderViewCount;
		long groupId = mCallback.getGroupId(pos);
		if (groupId < 0) return;
		if (pos == 0 || isFirstInGroup(pos)) {
			outRect.set(0, mTopHeight, 0, 0);
		}
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
		super.onDraw(c, parent, state);
		int left = parent.getPaddingLeft();
		int right = parent.getWidth() - parent.getPaddingRight();
		int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			View view = parent.getChildAt(i);
			int position = parent.getChildAdapterPosition(view);
			position -= mHeaderViewCount;
			long groupId = mCallback.getGroupId(position);
			if (groupId < 0) return;
			String textLine = mCallback.getGroupFirstLine(position).toUpperCase();
			if (position == 0 || isFirstInGroup(position)) {
				float top = view.getTop() - mTopHeight;
				float bottom = view.getTop();
				c.drawRect(left, top, right, bottom, mHeaderPaint);//绘制红色矩形
				c.drawText(textLine, left + mPaddingLeft, bottom - mBaseLine, mTextPaint);//绘制文本
			}
		}
	}

	@Override
	public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
		super.onDrawOver(c, parent, state);
		int itemCount = state.getItemCount();
		int childCount = parent.getChildCount();
		int left = parent.getPaddingLeft();
		int right = parent.getWidth() - parent.getPaddingRight();

		long preGroupId, groupId = -1;
		for (int i = 0; i < childCount; i++) {
			View view = parent.getChildAt(i);
			int position = parent.getChildAdapterPosition(view);
			position -= mHeaderViewCount;

			preGroupId = groupId;
			groupId = mCallback.getGroupId(position);
			if (groupId < 0 || groupId == preGroupId) continue;

			String textLine = mCallback.getGroupFirstLine(position).toUpperCase();
			if (TextUtils.isEmpty(textLine)) continue;

			int viewBottom = view.getBottom();
			float textY = Math.max(mTopHeight, view.getTop());
			if (position + 1 < itemCount) { //下一个和当前不一样移动当前
				long nextGroupId = mCallback.getGroupId(position + 1);
				if (nextGroupId != groupId && viewBottom < textY ) {//组内最后一个view进入了header
					textY = viewBottom;
				}
			}
			c.drawRect(left, textY - mTopHeight, right, textY, mHeaderPaint);
			c.drawText(textLine, left + mPaddingLeft, textY - mBaseLine, mTextPaint);
		}
	}

	private boolean isFirstInGroup(int pos) {
		if (pos == 0) {
			return true;
		} else {
			long prevGroupId = mCallback.getGroupId(pos - 1);
			long groupId = mCallback.getGroupId(pos);
			return prevGroupId != groupId;
		}
	}

	public interface DecorationCallback {

		long getGroupId(int position);

		String getGroupFirstLine(int position);
	}
}
