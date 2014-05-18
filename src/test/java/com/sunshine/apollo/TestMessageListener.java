package com.sunshine.apollo;

import com.sunshine.apollo.consumer.ConsumerTool;

public class TestMessageListener {

	/**
	 * @param args
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub
		ConsumerTool cc = new ConsumerTool();
		cc.startConsumeMessage();
		Thread.sleep(100000);
	}

}
