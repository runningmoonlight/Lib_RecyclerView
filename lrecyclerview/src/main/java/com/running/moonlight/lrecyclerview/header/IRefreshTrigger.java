package com.running.moonlight.lrecyclerview.header;

/**
 * Created by liuhengd on 2018/7/26.
 * 自定义刷新view需要实现这个接口
 * LRecyclerView将状态传递到刷新view，刷新view调整UI显示
 */
public interface IRefreshTrigger {

	void onStart(int headerHeight);

	void onMove(int movedDistance);

	void onRefresh();

	void onComplete(boolean isSuccess);

	void onReset();
}
