package com.sunshine.apollo.listener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sunshine.apollo.constant.JmsConfig;

/**
 * 
 * 创建人：夏天松 <br>
 * 创建时间：2014-5-16 <br>
 * 功能描述：ActiveMQ消息监听器 <br>
 */
public class ProcessRecordsListener implements MessageListener {
	/** 日志对象 **/
	private static final Logger LOGGER = LoggerFactory.getLogger(ProcessRecordsListener.class);

	/**
	 * Default batch size for CLIENT_ACKNOWLEDGEMENT or SESSION_TRANSACTED
	 * 即批量提交数
	 **/
	private long batch = 50;

	/** 消息是否需要事务 **/
	private boolean transacted = false;

	/**
	 * AUTO_ACKNOWLEDGE = 1, CLIENT_ACKNOWLEDGE = 2, DUPS_OK_ACKNOWLEDGE = 3,
	 * SESSION_TRANSACTED = 0
	 **/
	private int ackMode = Session.AUTO_ACKNOWLEDGE;

	private Session session;

	public ProcessRecordsListener() {
	}

	public ProcessRecordsListener(Session session) {
		this.session = session;
		this.transacted = JmsConfig.isTransacted();
		this.ackMode = JmsConfig.getAckMode();
		this.batch = JmsConfig.getBatchNum();
	}

	public ProcessRecordsListener(Session session, boolean transacted, int ackMode) {
		this.session = session;
		this.transacted = transacted;
		this.ackMode = ackMode;
		this.batch = JmsConfig.getBatchNum();
	}

	@Override
	public void onMessage(final Message message) {
		try {
			boolean isRollBack = false;
			try {
				ObjectMessage m = (ObjectMessage) message;
				System.out.println(m.getObject());
				//处理消息 在service处理压力比较大时可以使用多线程
			} catch (Exception e) {
				LOGGER.error("多线程存储广告数据到数据库出错", e);
				if (transacted) {
					try {
						session.rollback();
						isRollBack = true;
					} catch (JMSException je) {
						LOGGER.error("回滚消息出错!", je);
					}
				}
			}
			// 提交消息
			if (transacted && !isRollBack) {
				session.commit();
			} else if (ackMode == Session.CLIENT_ACKNOWLEDGE) {
				message.acknowledge();
			}
		} catch (JMSException e) {
			LOGGER.error("ProcessRecordsListener" + e);
		}
	}
}