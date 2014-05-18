package com.sunshine.apollo.consumer;

import java.io.IOException;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.Topic;

import org.apache.log4j.Logger;

import com.sunshine.apollo.constant.JmsConfig;
import com.sunshine.apollo.listener.ProcessRecordsListener;
import com.sunshine.apollo.util.JmsUtil;

public class CommonConsumer extends Thread {
	
	private static final Logger LOG = Logger.getLogger(CommonConsumer.class);
	
	/**消费session**/
	private Session session = null;
	
	/** 消息接收的消费者**/
	private MessageConsumer consumer = null; 
	
	/**消息监听器**/
	private MessageListener messageListener = null; 
	
	/** 消息是否需要事务 **/
	private boolean transacted = false;
	
	/**AUTO_ACKNOWLEDGE = 1, CLIENT_ACKNOWLEDGE = 2, DUPS_OK_ACKNOWLEDGE = 3, SESSION_TRANSACTED = 0**/
	private int ackMode = Session.AUTO_ACKNOWLEDGE;

	/** topic消费者名称 **/
	private String consumerName = "zqgame-ad";

	/** topic是否使用同一个链接 **/
	private boolean durable;

	/** 是否是topic **/
	private boolean topic;
	
	/**一次性处理消息的数量,处理完后即退出**/
	private int maxiumMessages;
	 
	/**两次接收消息的时间间隔,0为无间隔,单位毫秒**/
	private long receiveTimeOut;
	
	/**消费目标名称**/
	private String destinationReg = "com.zqgame.activeMQ.visitRecords";
	
	public CommonConsumer(){
		super();
		this.transacted = JmsConfig.isTransacted();
		this.ackMode = JmsConfig.getAckMode();
		this.consumerName = JmsConfig.getConsumerName() + "-"+ Thread.currentThread().getId();
		this.durable = JmsConfig.isDurable();
		this.topic = JmsConfig.isTopic();
		this.maxiumMessages = JmsConfig.getMaxiumMessages();
		this.receiveTimeOut = JmsConfig.getReceiveTimeOut();
		this.destinationReg = JmsConfig.getDestinationReg();
	}
	
	public void run() {
		Connection connection = null;
		try {
			// 创建Connection,每个consumer有一个,需要自己不能关闭
			connection = JmsUtil.getConnection();
			// 创建Session
			session = connection.createSession(transacted, ackMode);
			//目标对象
			Destination destination = null;
			if (topic) {
				destination = session.createTopic(destinationReg);
			} else {
				destination = session.createQueue(destinationReg);
			}

			if (durable && topic) {
				consumer = session.createDurableSubscriber((Topic) destination,	consumerName);
			} else {
				consumer = session.createConsumer(destination);
			}
			//创建消息监听器
			messageListener = new ProcessRecordsListener( session);
			
			//当指定接收maxiumMessages数量的消息时，就消费maxiumMessages量，然后退出
			if (maxiumMessages > 0) {
				consumeMessagesAndClose(connection, session, consumer);
			} else {
				//没有指定多长时间间隔取一次消息
				if (receiveTimeOut == 0) {
					consumer.setMessageListener(messageListener);
				} else {
					//指定时间间隔后进入此方法
					consumeMessagesAndClose(connection, session, consumer, receiveTimeOut);
				}
			}
		} catch (Exception e) {
			this.closeConsumerAndSession();
			LOG.error("启动线程进行消费消息出错",e);
		}
	}
	
	/**
	 * 关闭consumer，
	 * @author 夏天松 
	 * @date 2013-9-11
	 */
	public void closeConsumerAndSession(){
		if (consumer != null) {
			try {
				consumer.setMessageListener(null);
				consumer.close();
				consumer = null;
			} catch (JMSException e) {
				LOG.error("关闭consumer失败",e);
			}finally{
				consumer = null;
			}
		}
		if (session != null) {
			try {
				session.close();
				session = null;
			} catch (JMSException e) {
				LOG.error("关闭session失败",e);
			}finally{
				session = null;
			}
		}
	}

	/**
	 * 消费指定数量的消息
	 *  
	 * @param connection
	 * @param session
	 * @param consumer
	 * @throws JMSException
	 * @throws IOException void
	 * @author 夏天松 
	 * @date 2013-9-11
	 */
	protected void consumeMessagesAndClose(Connection connection,	Session session, MessageConsumer consumer) 
				    throws JMSException,IOException {
		//消费maxiumMessages数量的消息
		for (int i = 0; i < maxiumMessages;) {
			Message message = consumer.receive(1000);
			if (message != null) {
				messageListener.onMessage(message);
			}
		}
	}
	
	/**
	 * 指定时间间隔消费消息
	 *  
	 * @param connection
	 * @param session
	 * @param consumer
	 * @param timeout
	 * @throws JMSException
	 * @throws IOException void
	 * @author 夏天松 
	 * @date 2013-9-11
	 */
	protected void consumeMessagesAndClose(Connection connection,	Session session, MessageConsumer consumer, long timeout)
			        throws JMSException, IOException {
		//接收消息
		Message message = null;
		while(!this.isInterrupted()){
			if ((message = consumer.receive(timeout)) != null) {
				messageListener.onMessage(message);
			}
		}
	}

	public void setAckMode(String ackMode) {
		if ("CLIENT_ACKNOWLEDGE".equals(ackMode)) {
			this.ackMode = Session.CLIENT_ACKNOWLEDGE;
		}
		if ("AUTO_ACKNOWLEDGE".equals(ackMode)) {
			this.ackMode = Session.AUTO_ACKNOWLEDGE;
		}
		if ("DUPS_OK_ACKNOWLEDGE".equals(ackMode)) {
			this.ackMode = Session.DUPS_OK_ACKNOWLEDGE;
		}
		if ("SESSION_TRANSACTED".equals(ackMode)) {
			this.ackMode = Session.SESSION_TRANSACTED;
		}
	}

	public void setTransacted(boolean transacted) {
		this.transacted = transacted;
	}

	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	public void setTopic(boolean topic) {
		this.topic = topic;
	}

	public void setMaxiumMessages(int maxiumMessages) {
		this.maxiumMessages = maxiumMessages;
	}

	public void setReceiveTimeOut(long receiveTimeOut) {
		this.receiveTimeOut = receiveTimeOut;
	}

	public void setDestinationReg(String destinationReg) {
		this.destinationReg = destinationReg;
	}

	public Session getSession() {
		return session;
	}

	public MessageConsumer getConsumer() {
		return consumer;
	}  
}