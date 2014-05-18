package com.sunshine.apollo.model;

/**
 * 
 * 创建人：夏天松 <br>
 * 创建时间：2014-4-25 <br>
 * 功能描述：连接池信息 <br>
 */
public class JmsInfo {
    
    /** 用户名 **/
    private String userName;

    /** 密码 **/
    private String password;

    /** 连接地址 **/
    private String brokerUrl;
    
    /**是否定时检查连接池  **/
    private boolean isCheakPool = true; 
    
    /** 每个url的连接数连接数 **/
    private int connectionNum = 1;
    
    /**延迟多少时间后开始 检查  **/
    private long lazyCheck = 1000*10L;
    
    /**检查频率  **/
    private long periodCheck = 1000L;

    public JmsInfo(String brokerUrl, String userName, String password, int connectionNum) {
        super();
        this.userName = userName;
        this.password = password;
        this.brokerUrl = brokerUrl;
        this.connectionNum = connectionNum;
    }

    public JmsInfo(String brokerUrl, String userName, String password, int connectionNum,boolean isCheakPool,
            long lazyCheck, long periodCheck) {
        super();
        this.userName = userName;
        this.password = password;
        this.brokerUrl = brokerUrl;
        this.connectionNum = connectionNum;
        this.isCheakPool = isCheakPool;
        this.lazyCheck = lazyCheck;
        this.periodCheck = periodCheck;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public boolean isCheakPool() {
        return isCheakPool;
    }

    public void setCheakPool(boolean isCheakPool) {
        this.isCheakPool = isCheakPool;
    }

    public long getLazyCheck() {
        return lazyCheck;
    }

    public void setLazyCheck(long lazyCheck) {
        this.lazyCheck = lazyCheck;
    }

    public long getPeriodCheck() {
        return periodCheck;
    }

    public void setPeriodCheck(long periodCheck) {
        this.periodCheck = periodCheck;
    }

    public int getConnectionNum() {
        return connectionNum;
    }

    public void setConnectionNum(int connectionNum) {
        this.connectionNum = connectionNum;
    }
}