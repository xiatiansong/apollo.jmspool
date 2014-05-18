package com.sunshine.apollo.pool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQQueue;

import com.sunshine.apollo.constant.JmsConfig;
import com.sunshine.apollo.model.MessageBean;

/**
 * 
 * 创建人：夏天松 <br>
 * 创建时间：2014-5-16 <br>
 * 功能描述：包装的session，复用生产者和消费者<br>
 * 在有事务模式下，提供批量提交<br>
 */
public class PooledSession {
	/** session **/
	private Session session;
	/** 请求次数 **/
	private AtomicLong requestCounts = new AtomicLong(0L);
	/** 上次提交时间 **/
	private AtomicLong lastSubmitTime = new AtomicLong(System.currentTimeMillis());

	/** map：k=目标队列, v=对应的消息生产者 **/
	private Map<String, MessageProducer> producerMap = new HashMap<String, MessageProducer>();

	/** map：k=目标队列, v=对应的消息消费者 **/
	private Map<String, MessageConsumer> consumerMap = new HashMap<String, MessageConsumer>();

	public PooledSession(Session session) {
		super();
		this.session = session;
	}

	/**
	 * 发布消息
	 * 
	 * @param destination
	 * @param messageCreator
	 * @throws JMSException
	 */
	public void publish(final MessageBean messageBean) throws JMSException {
		MessageProducer producer = producerMap.get(messageBean.getDestination());
		if (producer != null) {
			Message message = messageBean.getMessageCreator().createMessage(session);
			producer.send(message);
		} else {
			createMessageProducer(messageBean.getDestination());
			publish(messageBean);
		}
		// 在有事务模式下 根据请求数批量提交或者在没有达到批量提交时每隔一段时间提交一次
		if (JmsConfig.isTransacted()) {
			if (requestCounts.intValue() % JmsConfig.getBatchNum() == 0
					|| System.currentTimeMillis() - lastSubmitTime.get() > JmsConfig.getSubmitPeriod()) {
				session.commit();
				lastSubmitTime.set(System.currentTimeMillis());
			}
		}
	}

	/**
	 * 接收消息，由于只有一个线程来接收消息，因此可以不考虑线程安全性
	 * 
	 * @param destination
	 * @return Message
	 * @throws JMSException
	 */
	public Message receive(String destination) throws JMSException {
		MessageConsumer messageConsumer = consumerMap.get(destination);
		if (messageConsumer == null) {
			createMessageConsumer(destination);
			messageConsumer = consumerMap.get(destination);
		}
		Message message = messageConsumer.receive();

		// 在有事务模式下 根据请求数批量提交或者在没有达到批量提交时每隔一段时间提交一次
		if (JmsConfig.isTransacted()) {
			if (requestCounts.intValue() % JmsConfig.getBatchNum() == 0
					|| System.currentTimeMillis() - lastSubmitTime.get() > JmsConfig.getSubmitPeriod()) {
				session.commit();
				lastSubmitTime.set(System.currentTimeMillis());
			}
		}
		return message;
	}

	/**
	 * 创建MessageProducer
	 * 
	 * @param destination
	 * @throws JMSException
	 * @author 夏天松
	 * @date 2014-4-25
	 */
	private synchronized void createMessageProducer(String destination) throws JMSException {
		if (producerMap.get(destination) == null) {
			Destination dest = new ActiveMQQueue(destination);
			MessageProducer producer = session.createProducer(dest);
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			producerMap.put(destination, producer);
		}
	}

	/**
	 * 创建MessageConsumer
	 * 
	 * @param destination
	 * @author 夏天松
	 * @throws JMSException
	 * @date 2014-4-25
	 */
	private synchronized void createMessageConsumer(String destination) throws JMSException {
		if (consumerMap.get(destination) == null) {
			Destination dest = new ActiveMQQueue(destination);
			MessageConsumer consumer = session.createConsumer(dest);
			consumerMap.put(destination, consumer);
		}
	}

	/** 关闭session **/
	public void close() throws JMSException {
		// 关闭消息生产者
		Iterator<Entry<String, MessageProducer>> iterProducer = producerMap.entrySet().iterator();
		while (iterProducer.hasNext()) {
			iterProducer.next().getValue().close();
		}
		// 关闭消息消费者
		Iterator<Entry<String, MessageConsumer>> iterConsumer = consumerMap.entrySet().iterator();
		while (iterConsumer.hasNext()) {
			iterConsumer.next().getValue().close();
		}
		session.close();
	}
}