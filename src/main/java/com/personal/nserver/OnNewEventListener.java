package com.personal.nserver;

import io.netty.channel.ChannelHandlerContext;

public interface OnNewEventListener {

    void onMessage(ChannelHandlerContext ctx, String msg);

    void onServerFilure(Exception e, String errorMsg);

    void onServerSuccess(String msg);

    void onChannelError(Exception e,String errorMsg);


    void onChannelsChanged(ChannelInfo info,Operate operate);

}
