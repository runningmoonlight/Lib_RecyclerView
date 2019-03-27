package com.running.moonlight.lrecyclerview.indexbar;

/**
 * Created by liuheng on 2018/10/29.
 * 索引数据的标记的基类
 */
public abstract class BaseIndexBean {

	protected String source;//索引数据
	private String indexPinyin;//索引数据的拼音
	private String indexTag;//索引标签

	public BaseIndexBean(String source) {
		this.source = source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getSource() {
		return source;
	}

	public void setIndexPinyin(String indexPinyin) {
		this.indexPinyin = indexPinyin;
	}

	public String getIndexPinyin() {
		return indexPinyin;
	}

	public void setIndexTag(String indexTag) {
		this.indexTag = indexTag;
	}

	public String getIndexTag() {
		return indexTag;
	}

	//是否需要被转换为拼音，默认为true
	//可根据实际需要设置，为false则放在列表的最前面
	public boolean isNeedToPinyin() {
		return true;
	}

	//转换成拼音的目标字段
	//需要子类重写，一般为source，可加工处理
	public abstract String getTagSource();
}
