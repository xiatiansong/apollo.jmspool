package com.sunshine.apollo.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import com.sunshine.apollo.model.JmsInfo;
import com.sunshine.apollo.model.MessageBean;

/**
 * 创建人：夏天松 <br>
 * 创建时间：2014-4-21 <br>
 * 功能描述： jms连接池<br>
 */
public class JmsConnectionPool {
    
    private static Logger log = Logger.getLogger(JmsConnectionPool.class);

    /** 是否可用 **/
    private volatile AtomicBoolean isActive = new AtomicBoolean(false);
    
    /**连接池连接信息**/
    private final JmsInfo jmsInfo;
    
    /**连接存放**/
    private final List<JmsConnection> connections = new ArrayList<JmsConnection>();
    
    private AtomicLong requestCounts = new AtomicLong(0L);
    
    public JmsConnectionPool(JmsInfo jmsInfo) {
        super();
        this.jmsInfo = jmsInfo;
        init();
        cheackPool();
    }

    /** 初始化连接池 **/
    private void init() {
        try {
            if(jmsInfo.getConnectionNum() < 1){
                log.error("connection param error：connectionNum："+ jmsInfo.getConnectionNum());
                return;
            }
            isActive.set(false);
            close();
            //清空连接池
            connections.clear();
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(jmsInfo.getBrokerUrl());
            //开始初始化
            for (int i = 0; i < jmsInfo.getConnectionNum(); i++) {
                JmsConnection jmsConnection = new JmsConnection(factory, jmsInfo);
                connections.add(jmsConnection);
            }
            //设置连接池状态为可用
            isActive.set(true);
        } catch (JMSException e) {
            log.error("init connection error：brokerUrl=" + jmsInfo.getBrokerUrl(), e);
            isActive.set(false);
        }
    }
    
    /**
     * 发布消息
     * @param destination
     * @param messageCreator
     * @throws JMSException 
     */
    public void publish(final MessageBean messageBean) throws JMSException {
        try {
            this.getConnection().publish(messageBean);
        } catch (JMSException e) {
            log.error("connection error : brokerUrl=" + jmsInfo.getBrokerUrl(), e);
            isActive.compareAndSet(true, false);
            throw new JMSException(e.getMessage(), e.getErrorCode());
        } 
    }
    
    
    /**
     * 接收消息，由于只有一个线程来接收消息，因此可以不考虑线程安全性
     * 可能会获取消息失败 失败返回消息为空
     * @param destination
     * @return Message
     * @throws JMSException
     */
    public Message receive(String destination) throws JMSException  {
        try {
            return this.getConnection().receive(destination);
        } catch (JMSException e) {
            log.error("connection error：brokerUrl=" + jmsInfo.getBrokerUrl(), e);
            isActive.compareAndSet(true, false);
            throw new JMSException(e.getMessage(), e.getErrorCode());
        } 
    }
    
    private void cheackPool() {
        if(jmsInfo.isCheakPool()){
          //timer设置为daemon线程
            new Timer(true).schedule(new TimerTask() {
                @Override
                public void run() {
                    if (!isActive.get()) {
                        log.info("--------lose connection ---> reconnect to server. brokerUrl is "  + jmsInfo.getBrokerUrl());
                        //重新初始化一遍连接池
                        init();
                    }
                }
            }, jmsInfo.getLazyCheck(), jmsInfo.getPeriodCheck());
        }
    }
    
    /** 轮询获得连接**/
    public JmsConnection getConnection(){
        int index = (int)(requestCounts.getAndIncrement() % connections.size());
        return connections.get(index);
    }
    
    
    /**关闭所有连接**/
    public void close() {
        try {
            isActive.compareAndSet(true, false);
            for(JmsConnection connection :connections){
                    connection.close();
            }
        } catch (JMSException e) {
            log.error("connection close error", e);
        }
    }
    
    public boolean isActive() {
        return isActive.get();
    }
    
    /**把isActive设置为true**/
    public void setActiveTrue() {
        this.isActive.compareAndSet(false, true);
    }

    /**把isActive设置为false**/
    public void setActiveFalse() {
        this.isActive.compareAndSet(true, false);
    }
    
    public JmsInfo getJmsInfo() {
        return jmsInfo;
    }
}