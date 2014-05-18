package com.sunshine.apollo.pool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;

import org.apache.log4j.Logger;

import com.sunshine.apollo.constant.JmsConfig;
import com.sunshine.apollo.model.JmsInfo;
import com.sunshine.apollo.model.MessageBean;

/**
 * 
 * 创建人：夏天松 <br>
 * 创建时间：2014-5-15 <br>
 * 功能描述：连接池管理 <br>
 */
public class JmsConnectionPoolManager {

    private static Logger log = Logger.getLogger(JmsConnectionPoolManager.class);

    /** 配置文件中的连接url、以逗号分隔开 **/
    private String brokerUrl;
    
    /** 连接url数组 **/
    private String[] urls;

    /** 用户名 **/
    private String userName;

    /** 密码 **/
    private String password;
    
    /** 每个url的连接数连接数 **/
    private int connectionNum = 1;
    
    /**连接池存放**/
    private List<JmsConnectionPool> pools = new ArrayList<JmsConnectionPool>();
    
    private AtomicLong requestCounts = new AtomicLong(0L);
    
    /**发布出现异常的消息放入队列**/
    private LinkedBlockingQueue<MessageBean> messageBeanQueue = new LinkedBlockingQueue<MessageBean>();
    
    /** 初始化 **/
    public JmsConnectionPoolManager(String brokerUrl, String userName, String password){
        super();
        this.brokerUrl = brokerUrl;
        this.userName = userName;
        this.password = password;
        this.connectionNum = JmsConfig.getConnectionNum();
        init();
    }
    
    /**初始化方法**/
    private void init() {
        if(connectionNum < 1){
            log.error("connection param error：connectionNum："+ connectionNum);
            return;
        }
        urls = this.brokerUrl.split(",");
        if (urls.length < 1) {
            urls = this.brokerUrl.split(" ");
        }
        if (urls.length >= 1) {
            for (String url : urls) {
                JmsInfo jmsInfo = new JmsInfo(url, userName, password, connectionNum, JmsConfig.isCheakPool(), JmsConfig.getLazyCheck(), JmsConfig.getPeriodCheck());
                JmsConnectionPool jmsConnectionPool = new JmsConnectionPool(jmsInfo);
                jmsConnectionPool.setActiveTrue();
                pools.add(jmsConnectionPool);
            }
        }
        //消费异常消息
        processMessageQueue();
        log.info("connection initialization is complete");
    }
    
   /**消费异常消息: 发布消息发生异常的消息**/
    private void processMessageQueue() {
        Thread processMessageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(!Thread.currentThread().isInterrupted()){
                    try {
                        MessageBean messageBean = messageBeanQueue.take();
                        publish(messageBean);
                        log.info("process message queue is running");
                    } catch (InterruptedException e) {
                        log.error("process messageThread is interrupted", e);
                    }
                }
            }
        });
        processMessageThread.setDaemon(true);
        processMessageThread.start();
    }

    /** 获得连接池**/
    private JmsConnectionPool getPool(){
        int index = (int)(requestCounts.getAndIncrement() % pools.size());
        JmsConnectionPool jmsConnectionPool = pools.get(index);
        if(jmsConnectionPool.isActive()){
            return jmsConnectionPool;
        }else{
            return this.getPool();
        }
    }
    
    public Connection getConnection(){
    	return this.getPool().getConnection().getConnection();
    }

    /** 发布消息**/
    public void publish(MessageBean messageBean) {
        JmsConnectionPool pool =  this.getPool();
        try {
            pool.publish(messageBean);
        } catch (JMSException e) {
            log.error("connection error：brokerUrl=" + pool.getJmsInfo().getBrokerUrl(), e);
            messageBeanQueue.add(messageBean);
        }
    }

    public Message receive(String destination) {
        JmsConnectionPool pool =  this.getPool();
        try {
            return pool.receive(destination);
        } catch (JMSException e) {
            log.error("connection error：brokerUrl=" + pool.getJmsInfo().getBrokerUrl(), e);
        }
        return null;
    }

    /**关闭连接池**/
    public void close() {
        for (JmsConnectionPool pool : pools) {
            pool.setActiveFalse();
            pool.close();
        }
    }
}