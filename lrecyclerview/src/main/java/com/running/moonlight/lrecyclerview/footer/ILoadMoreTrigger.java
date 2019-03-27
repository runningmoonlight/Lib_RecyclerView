package com.running.moonlight.lrecyclerview.footer;

/**
 * Created by liuheng on 2018/7/26.
 * 自定义加载更多view需要实现这个接口
 * LRecyclerView将状态传递到刷新view，加载更多view调整UI显示
 */
public interface ILoadMoreTrigger {

	int STATE_NORMAL = 0;//通常状态
	int STATE_NO_MORE = 1;//没有更多
	int STATE_ERROR = 2;//加载出错

	void onLoadingMore();

	void onComplete(int state);

	void reset();

}
