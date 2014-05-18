package com.sunshine.apollo.util;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import com.sunshine.apollo.constant.BaseConfig;
import com.sunshine.apollo.model.MessageBean;
import com.sunshine.apollo.pool.JmsConnectionPoolManager;

/**
 * 
 * 创建人：夏天松 <br>
 * 创建时间：2014-5-15 <br>
 * 功能描述： jms工具类<br>
 */
public class JmsUtil {
    
    private static JmsConnectionPoolManager poolManager=  null;
    
    /**初始化***/
    private JmsUtil(){
        poolManager  =  new JmsConnectionPoolManager(BaseConfig.getConfigParam(JmsConfig.BROKER_URL.toString()),
                BaseConfig.getConfigParam(JmsConfig.USERNAME.toString()),
                BaseConfig.getConfigParam(JmsConfig.PASSWORD.toString())
              );
    }
    /**单例实现**/
    public static JmsUtil getInstance(){
        return Singtonle.instance;
    }
    
    private static class Singtonle {
        private static JmsUtil instance =  new JmsUtil();
    }
    
    /**
     * 发布消息
     * @param destination
     * @param messageCreator
     * @throws JMSException
     */
    public static void publish(String destination, final MessageCreator messageCreator) {
        getInstance().publishMessage(destination, messageCreator);
    }

    private void publishMessage(String destination, final MessageCreator messageCreator) {
        MessageBean messageBean = new MessageBean(messageCreator, destination);
        poolManager.publish(messageBean);
    }
    
    public static Connection getConnection(){
    	return getInstance().getJmsConnection();
    }
    
    private Connection getJmsConnection(){
    	return poolManager.getConnection();
    }
    
    /**
     * 接收消息，由于只有一个线程来接收消息，因此可以不考虑线程安全性
     * @param destination
     */
    public static Message receive(String destination) {
        return getInstance().receiveMessage(destination);
    }

    private Message receiveMessage(String destination) {
        return poolManager.receive(destination);
    }
    
    /**
     * 关闭连接 void
     */
    public static void close() {
        poolManager.close();
    }

    public interface MessageCreator {
        Message createMessage(Session session) throws JMSException;
    }

    /**
     * 此三项须在子项目中配置
     */
    public enum JmsConfig {
        BROKER_URL, USERNAME, PASSWORD;
    }

}