package com.sunshine.apollo.constant;

/**
 * 
 * 创建人：夏天松 <br>
 * 创建时间：2014-5-15 <br>
 * 功能描述： jms配置文件加载类<br>
 */
public class JmsConfig extends BaseConfig {

	private static String destinationReg;
	private static String destinationLog;

	/** 消息是否需要事务 **/
	private static boolean transacted = false;

	/**
	 * Default batch size for CLIENT_ACKNOWLEDGEMENT or SESSION_TRANSACTED
	 * 即批量提交数
	 **/
	private static int ackMode;

	/** topic消费者名称 **/
	private static String consumerName = "zqgame-ad";

	/** topic是否使用同一个链接 **/
	private static boolean durable;

	/** 是否是topic，否即是queue **/
	private static boolean topic;

	/** 批量处理消息量 **/
	private static int batchNum;

	/** 接收一次消息的数量，然后关闭连接 **/
	private static int maxiumMessages;

	/** 接收消息时间间隔 **/
	private static long receiveTimeOut;

	/** jms connection的数量  一个应用一般一个connection **/
	private static int connectionNum;
	
	/**每一个对应的session数量(每个session会绑定一个producer和一个consumer)**/
	private static int concurrentSession;
	
	 /**是否定时检查连接池  **/
    private static boolean cheakPool = true; 
    
    /**延迟多少时间后开始 检查  **/
    private static long lazyCheck = 1000*10L;
    
    /**检查频率  **/
    private static long periodCheck = 1000L;
    
    /**事务模式下提交消息的间隔  **/
    private static long submitPeriod = 1000 * 10L;

	private static String FILE_PATH = "config/jms.properties";

	static {
		loadValue(FILE_PATH, new JmsConfig());
	}

	/**
	 * 重新加载配置
	 * 
	 * @author 夏天松
	 * @date 2013-9-23
	 */
	public static void reloadValue() {
		reloadValue(FILE_PATH, new JmsConfig());
	}

	public static String getDestinationReg() {
		return destinationReg;
	}

	public static boolean isTransacted() {
		return transacted;
	}

	public static int getAckMode() {
		return ackMode;
	}

	public static String getConsumerName() {
		return consumerName;
	}

	public static boolean isDurable() {
		return durable;
	}

	public static boolean isTopic() {
		return topic;
	}

	public static int getMaxiumMessages() {
		return maxiumMessages;
	}

	public static long getReceiveTimeOut() {
		return receiveTimeOut;
	}

	public static int getBatchNum() {
		return batchNum;
	}

	public static String getDestinationLog() {
		return destinationLog;
	}

	public static int getConnectionNum() {
		return connectionNum;
	}

	public static int getConcurrentSession() {
		return concurrentSession;
	}

	public static boolean isCheakPool() {
		return cheakPool;
	}

	public static long getLazyCheck() {
		return lazyCheck;
	}

	public static long getPeriodCheck() {
		return periodCheck;
	}

	public static long getSubmitPeriod() {
		return submitPeriod;
	}
	
}