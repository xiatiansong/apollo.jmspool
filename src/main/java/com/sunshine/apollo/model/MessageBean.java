package com.sunshine.apollo.model;

import com.sunshine.apollo.util.JmsUtil.MessageCreator;


/**
 * 创建人：夏天松 <br>
 * 创建时间：2014-4-28 <br>
 * 功能描述： 发送消息实体<br>
 */
public class MessageBean {
    
    private MessageCreator messageCreator;
    
    private String destination;

    public MessageBean(){}
    
    public MessageBean(MessageCreator messageCreator, String destination) {
        super();
        this.messageCreator = messageCreator;
        this.destination = destination;
    }

    public MessageCreator getMessageCreator() {
        return messageCreator;
    }

    public void setMessageCreator(MessageCreator messageCreator) {
        this.messageCreator = messageCreator;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }
}