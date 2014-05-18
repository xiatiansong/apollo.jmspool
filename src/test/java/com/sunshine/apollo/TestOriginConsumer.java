package com.sunshine.apollo;


import java.util.concurrent.CountDownLatch;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

import com.sunshine.apollo.util.JmsUtil;

public class TestOriginConsumer {

	private static int THREAD_COUNT = 10;
	
	/** 
	 *  
	 * @param args void
	 * @author 夏天松 
	 * @throws JMSException 
	 * @throws InterruptedException 
	 * @date 2014-5-16 
	 */
	public static void main(String[] args) throws JMSException, InterruptedException {
		// TODO Auto-generated method stub
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin", "password", "tcp://192.168.139.120:61613");
		Connection con = connectionFactory.createConnection();
		con.start();
		Session session = con.createSession(false, 1);
		MessageConsumer mc = session.createConsumer(session.createQueue("test-jms-pool-new"));
		for(int i = 0;i< 10000;i++){
            mc.receive();
		}
		Thread.sleep(10000);
	}
	
	public static void test() throws JMSException, InterruptedException {
		//new JmsConfig();
		final CountDownLatch startGate = new CountDownLatch(1);
		final CountDownLatch endGate = new CountDownLatch(THREAD_COUNT);

		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory("admin", "password", "tcp://192.168.139.120:61613");
		Connection con = connectionFactory.createConnection();
		con.start();
		Session session = con.createSession(false, 1);
		final MessageConsumer mc = session.createConsumer(session.createQueue("test-jms-pool-new"));
		
		for (int i = 0; i < THREAD_COUNT; i++) {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						startGate.await();
						for (int i = 0; i < 2010; i++) {
							mc.receive();
						}
						endGate.countDown();
					} catch (InterruptedException | JMSException e) {
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
