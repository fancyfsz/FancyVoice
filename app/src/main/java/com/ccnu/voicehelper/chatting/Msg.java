package com.ccnu.voicehelper.chatting;

/**
 * Created by mona on 16/3/6.
 */
//定义消息的实体类
public class Msg {

    //设置消息类型
    public final static int TYPE_RECEIVED = 0;
    public final static int TYPE_SENT = 1;

    private String content;//消息体
    private int type;

    public Msg(String content,int type){
        this.content = content;
        this.type = type;
    }

    public String getContent(){
        return content;
    }

    public int getType(){
        return type;
    }

}
