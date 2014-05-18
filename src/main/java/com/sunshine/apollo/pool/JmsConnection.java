package com.sunshine.apollo.pool;

import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.log4j.Logger;

import com.sunshine.apollo.constant.JmsConfig;
import com.sunshine.apollo.model.JmsInfo;
import com.sunshine.apollo.model.MessageBean;

/**
 * 创建人：夏天松 <br>
 * 创建时间：2014-4-21 <br>
 * 功能描述：jms connection包装类 <br>
 */
public class JmsConnection {

	private static Logger log = Logger.getLogger(JmsConnection.class);

	private ConnectionFactory connectionFactory;

	/** jms 连接 **/
	private Connection connection;

	/** 连接池连接信息 **/
	private final JmsInfo jmsInfo;

	/** 空闲session池 **/
	private LinkedBlockingQueue<PooledSession> freeSession = new LinkedBlockingQueue<PooledSession>();

	/** 活动session池 **/
	private LinkedBlockingQueue<PooledSession> activeSession = new LinkedBlockingQueue<PooledSession>();

	public JmsConnection(ConnectionFactory connectionFactory, JmsInfo jmsInfo) throws JMSException {
		super();
		this.connectionFactory = connectionFactory;
		this.jmsInfo = jmsInfo;
		init();
	}

	private void init() throws JMSException {
		this.connection = connectionFactory.createConnection(jmsInfo.getUserName(), jmsInfo.getPassword());
		this.connection.start();
		// creare session pool
		int concurrentSession = JmsConfig.getConcurrentSession();
		if (concurrentSession < 1) {
			log.error("concurrent session param error：concurrentSession=" + concurrentSession);
			return;
		}
		// 清空session
		freeSession.clear();
		activeSession.clear();

		for (int i = 0; i < concurrentSession; i++) {
			freeSession.add(new PooledSession(
					connection.createSession(JmsConfig.isTransacted(), JmsConfig.getAckMode())));
		}
	}

	/**
	 * 发布消息
	 * 
	 * @param messageCreator
	 * @throws JMSException
	 */
	public void publish(final MessageBean messageBean) throws JMSException {
		PooledSession pooledSession = null;
		try {
			pooledSession = freeSession.take();
			activeSession.put(pooledSession);
			pooledSession.publish(messageBean);
		} catch (InterruptedException e) {
			log.error("take or put jmsConnection to queue is interrupted", e);
		} finally {
			// 释放连接
			try {
				if (pooledSession != null) {
					freeSession.put(pooledSession);
					activeSession.remove(pooledSession);
				}
			} catch (InterruptedException e) {
				log.error("take or put jmsConnection to queue is interrupted", e);
			}
		}
	}

	/**
	 * 接收消息
	 * 
	 * @param destination
	 * @throws JMSException
	 */
	public Message receive(String destination) throws JMSException {
		PooledSession pooledSession = null;
        try {
        	pooledSession = freeSession.take();
        	activeSession.put(pooledSession);
            return pooledSession.receive(destination);
        } catch (InterruptedException e) {
            log.error("take or put jmsConnection to queue is interrupted", e);
        } finally {
            //释放连接
            try {
                if(pooledSession != null){
                	freeSession.put(pooledSession);
                	activeSession.remove(pooledSession);
                }
            } catch (InterruptedException e) {
                log.error("take or put jmsConnection to queue is interrupted", e);
            }
        }
        //接收消息异常 返回空
        return null;
	}

	/** 关闭连接 **/
	public void close() throws JMSException {
		while(true){
            if(freeSession.isEmpty() && activeSession.isEmpty()){
                break;
            }
            PooledSession session = freeSession.poll();
            if(session != null){
            	session.close();
            }
        }
		connection.close();
	}

	public Connection getConnection() {
		return connection;
	}
}