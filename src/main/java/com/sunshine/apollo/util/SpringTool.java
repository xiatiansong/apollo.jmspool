package com.sunshine.apollo.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 
 * 创建人：夏天松 <br>
 * 创建时间：2014-5-16 <br>
 * 功能描述： 获取spring产生的bean实例<br>
 */
@Component
public class SpringTool implements ApplicationContextAware {

	private static ApplicationContext applicationContext;

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		SpringTool.applicationContext = applicationContext;
	}

	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}

	/**
	 * 根据Bean名称获取实例
	 * @param name Bean注册名称
	 * @return bean实例
	 * @throws BeansException
	 */
	public static Object getBean(String name) throws BeansException {
		return applicationContext.getBean(name);
	}

	/**
	 * 根据Class获取实例
	 * @param nameBean注册名称
	 * @return bean实例
	 * @throws BeansException
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object getBean(Class cls) throws BeansException {
		return applicationContext.getBean(cls);
	}

	/**
	 * 根据Class获取实例
	 * @param nameBean注册名称
	 * @return bean实例
	 * @throws BeansException
	 */
	public static boolean containsBean(String name) throws BeansException {
		return applicationContext.containsBean(name);
	}
}