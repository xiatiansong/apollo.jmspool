package com.sunshine.datastructure;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 创建人：夏天松 <br>
 * 创建时间：2014-5-15 <br>
 * 功能描述： 扩展一下LinkedHashMap这个类，让他实现LRU算法<br>
 */
public class LRULinkedHashMap<K, V> extends LinkedHashMap<K, V> {
	// 定义缓存的容量
	private int capacity;
	private static final long serialVersionUID = 8100125356643636503L;

	/**
	 * 带参数的构造器
	 * @param capacity
	 */
	public LRULinkedHashMap(int capacity) {
		// 调用LinkedHashMap的构造器，传入以下参数
		super(16, 0.75f, true);
		// 传入指定的缓存最大容量
		this.capacity = capacity;
	}

	/**
	 *  实现LRU的关键方法，如果map里面的元素个数大于了缓存最大容量，则删除链表的顶端元素
	 */
	@Override
	public boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		System.out.println(eldest.getKey() + "=" + eldest.getValue());
		return size() > capacity;
	}
}