package com.ifeng.recallScheduler.threadUtil;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ctr服务的线程工厂类
 *
 */
public class BaseThreadFactory implements ThreadFactory {

	/**
	 * 线程计数器
	 */
	private final AtomicInteger threadId = new AtomicInteger(0);

	private String threadNamePrefix = null;

	/**
	 * 线程名前缀
	 * @param threadNamePrefix
	 */
	public BaseThreadFactory(String threadNamePrefix) {
		this.threadNamePrefix = threadNamePrefix;
	}
	
	@Override
	public Thread newThread(Runnable r) {
		Thread ret = new Thread(Thread.currentThread().getThreadGroup(), r, 
				this.threadNamePrefix + threadId.getAndIncrement(), 0);
		ret.setDaemon(false);
		if (threadId.get() == Integer.MAX_VALUE) {
			threadId.set(0);
		}
		return ret;
	}
}
