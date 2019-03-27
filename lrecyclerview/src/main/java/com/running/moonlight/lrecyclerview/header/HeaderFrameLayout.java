package com.running.moonlight.lrecyclerview.header;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by liuheng on 2018/8/16.
 * LRecyclerView中header、footer和loaderMoreFooter的View的容器
 * 这样写是为了方便处理分隔线的问题
 */
public class HeaderFrameLayout extends FrameLayout {
	public HeaderFrameLayout(@NonNull Context context) {
		this(context, null);
	}

	public HeaderFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public HeaderFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}
}
