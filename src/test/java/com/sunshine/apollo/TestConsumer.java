package com.sunshine.apollo;

import java.util.concurrent.CountDownLatch;

import javax.jms.JMSException;

import com.sunshine.apollo.constant.JmsConfig;
import com.sunshine.apollo.util.JmsUtil;

public class TestConsumer {

	private static int THREAD_COUNT = 10;

	/**
	 * 测试结果；事物型消费 100个线程 消费消息20100 126956 ms
	 *         事物型消费 100个线程 126956 ms
	 *         非事务型发布 14050 ms
	 * @author 夏天松
	 * @throws JMSException
	 * @throws InterruptedException 
	 * @date 2014-4-22
	 */
	public static void main(String[] args) throws JMSException, InterruptedException {
		new JmsConfig();
		final CountDownLatch startGate = new CountDownLatch(1);
		final CountDownLatch endGate = new CountDownLatch(THREAD_COUNT);

		for (int i = 0; i < THREAD_COUNT; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						startGate.await();
						for (int i = 0; i < 2010; i++) {
							JmsUtil.receive("test-jms-pool-new");
						}
						endGate.countDown();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			});
			thread.start();
		}
		long start = System.currentTimeMillis();
		startGate.countDown();
		endGate.await();
		long end = System.currentTimeMillis(); 
		System.out.printf("----------------------消费消息结束-------------------------------耗时：%d \n", (end-start));
		JmsUtil.close();
	}
}