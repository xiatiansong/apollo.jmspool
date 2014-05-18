package com.sunshine.apollo;

import java.util.concurrent.CountDownLatch;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import com.sunshine.apollo.constant.JmsConfig;
import com.sunshine.apollo.util.JmsUtil;

public class TestProducer {

	private static int THREAD_COUNT = 9;

	/**
	 * 测试结果；事物型发布   10 个线程 发布消息20100 115316 ms
	 *         非事务型发布 10 个线程  发布消息20100 9314 ms
	 *         
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
							JmsUtil.publish("test-jms-pool-new",
									new JmsUtil.MessageCreator() {
										@Override
										public Message createMessage(Session session) throws JMSException {
											ObjectMessage message = session.createObjectMessage();
											message.setObject("12323563456356356763-134523=-562456245-632=45-2=456092-45=6");
											return message;
										}
									});

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
		System.out.printf("----------------------发送消息结束-------------------------------耗时：%d \n", (end-start));
		JmsUtil.close();
	}
}