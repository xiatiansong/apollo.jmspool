package com.sunshine.apollo;


import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;

public class TestOriginProducer {

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
		for(int i = 0;i< 100;i++){
			Session session = con.createSession(false, 1);
			ObjectMessage message = session.createObjectMessage();
            message.setObject("12323563456356356763-134523=-562456245-632=45-2=456092-45=6");
            session.createProducer(new ActiveMQQueue("test-jms-pool-new")).send(message);
			Thread.sleep(1000);
			System.out.println("-------------------------------"+i);
		}
		Thread.sleep(10000);
	}

}
