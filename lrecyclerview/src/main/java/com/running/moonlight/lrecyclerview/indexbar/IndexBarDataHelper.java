package com.running.moonlight.lrecyclerview.indexbar;

import android.support.annotation.NonNull;

import com.github.promeg.pinyinhelper.Pinyin;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by liuhengd on 2018/10/29.
 *
 * IndexBar的数据相关帮助类
 */
public class IndexBarDataHelper {

	private static final String TAG_START = "*";
	private static final String REGEX_A_Z = "[A-Z]";
	private static final String TAG_END = "#";

	/**
	 * 数据源转拼音
	 * @param dataList 数据源
	 */
	public static void convert(List<? extends BaseIndexBean> dataList) {
		if (null == dataList || dataList.isEmpty())
			return;

		for (int i = 0, size = dataList.size(); i < size; i++) {
			BaseIndexBean baseIndexBean = dataList.get(i);
			StringBuilder stringBuilder = new StringBuilder();
			if (baseIndexBean.isNeedToPinyin()) {
				String tagSource = baseIndexBean.getTagSource();
				for (int j = 0, length = tagSource.length(); j < length; j++) {
					//使用TinyPinyin将char转换为拼音
					stringBuilder.append((Pinyin.toPinyin(tagSource.charAt(j)).toUpperCase()));
				}
				baseIndexBean.setIndexPinyin(stringBuilder.toString());
			}
		}
	}

	/**
	 * 设置indexTag，
	 * 不转拼音的特殊bean为"*"，放在开头
	 * 一般为首字母A-Z，
	 * 转换拼音的特殊字符为'#'，放在最后
	 */
	public static void fillIndexTag(List<? extends BaseIndexBean> dataList) {
		if (null == dataList || dataList.isEmpty())
			return;

		for (int i = 0, size = dataList.size(); i < size; i++) {
			BaseIndexBean baseIndexBean = dataList.get(i);
			if (baseIndexBean.isNeedToPinyin()) {
				String tag = baseIndexBean.getIndexPinyin().substring(0, 1);
				if (tag.matches(REGEX_A_Z)) {
					baseIndexBean.setIndexTag(tag);
				} else {
					baseIndexBean.setIndexTag(TAG_END);
				}
			} else {
				baseIndexBean.setIndexTag(TAG_START);
			}
		}
	}

	/**
	 * 对数据源排序
	 */
	public static void sortSourceData(List<? extends BaseIndexBean> dataList) {
		if (null == dataList || dataList.isEmpty())
			return;

		convert(dataList);
		fillIndexTag(dataList);
		//排序
		Collections.sort(dataList, new Comparator<BaseIndexBean>() {
			@Override
			public int compare(BaseIndexBean o1, BaseIndexBean o2) {
				if (TAG_START.equals(o1.getIndexTag()) && !TAG_START.equals(o2.getIndexTag())) {
					return -1;
				} else if (TAG_END.equals(o2.getIndexTag()) && !TAG_END.equals(o1.getIndexTag())) {
					return 1;
				} else {
					return o1.getIndexTag().compareTo(o2.getIndexTag());
				}
//				if (!o1.isNeedToPinyin() && !o2.isNeedToPinyin()) {
//					return 0;
//				} else if (!o1.isNeedToPinyin()) {
//					return -1;
//				} else if (!o2.isNeedToPinyin()) {
//					return 1;
//				} else if (o1.getIndexTag().equals(TAG_END)) {
//					return 1;
//				} else if (o2.getIndexTag().equals(TAG_END)) {
//					return -1;
//				} else {
//					return o1.getIndexTag().compareTo(o2.getIndexTag());
//				}
			}
		});
	}

	/**
	 * 数据源排序后，更新indexTag数据
	 */
	public static void flushSortedIndexData(List<? extends BaseIndexBean> sourceList, @NonNull List<String> indexList) {
		if (null == sourceList || sourceList.isEmpty())
			return;

		indexList.clear();
		String indexTag;
		for (int i = 0, size = sourceList.size(); i < size; i++) {
			indexTag = sourceList.get(i).getIndexTag();
			if (!indexList.contains(indexTag)) {
				indexList.add(indexTag);
			}
		}
	}
}
