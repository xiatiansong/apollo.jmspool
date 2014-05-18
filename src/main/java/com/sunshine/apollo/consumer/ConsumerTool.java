package com.sunshine.apollo.consumer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.sunshine.apollo.constant.JmsConfig;
import com.sunshine.apollo.util.JmsUtil;

/**
 * 启动消费线程取消息
 * 创建人：夏天松 <br>
 * 创建时间：2013-9-11 <br>
 * 功能描述： <br>
 */
public final class ConsumerTool {
	
	private static final Logger LOG = Logger.getLogger(ConsumerTool.class);
	
	/**并行消费者数量**/
	private int parallelThreads = 1;
	
	private List<CommonConsumer> threads = null;
	
	public ConsumerTool(){
		this.parallelThreads = JmsConfig.getConcurrentSession();
	}
	
	/**
	 * 开启多个线程消费消息
	 * @author 夏天松 
	 * @date 2013-9-11
	 */
	public void startConsumeMessage(){
		//创建消费者容器
		threads = new ArrayList<CommonConsumer>();
		//添加消费者
		for (int threadCount = 1; threadCount <= parallelThreads; threadCount++) {
			CommonConsumer commonConsumer = new CommonConsumer();
			threads.add(commonConsumer);
			commonConsumer.setDaemon(true);
			commonConsumer.start();
		}
	}
	
	/**
	 * 停止消费消息，最好是在关闭tomcat时调用
	 * @author 夏天松 
	 * @date 2013-9-11
	 */
	public void stopConsumeMessage(){
		try{
			if(threads != null && threads.size() != 0){
				for (int i = 0;i < threads.size(); i++) {
					CommonConsumer consumer = threads.get(i);
					consumer.getSession().commit();
					consumer.closeConsumerAndSession();
					//中断消费者
					if(!consumer.isInterrupted()){
						consumer.interrupt();
					}
					threads.remove(consumer);
					//移除对象后集合的索引都变小，需要重置i
					i--;
				}
			}
		}catch(Exception e){
			LOG.error("停止消费线程失败", e);
		}finally{
			JmsUtil.close();
		}
	}
	
	/**
	 * 重新启动消费者线程
	 * @author 夏天松 
	 * @date 2013-9-23
	 */
	public void restartConsumeMessage(){
		//停止消费者线程
		stopConsumeMessage();
		//重新加载配置
		JmsConfig.reloadValue();
		//重新设置并发数
		this.parallelThreads = JmsConfig.getConcurrentSession();
		//启动消费者线程
		startConsumeMessage();
	}
}
