package com.zcj.nettyServer;

import com.sun.scenario.animation.shared.SingleLoopClipEnvelope;
import com.zcj.nettyServer.tools.SingletonNetty;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;

public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object mesBase) throws Exception {
        int readableBytes = ((ByteBuf)mesBase).readableBytes();
        CharSequence charSequence = ((ByteBuf)mesBase).readCharSequence(readableBytes, Charset.defaultCharset());
        String mesStr = charSequence.subSequence(0, charSequence.length()).toString();
        SingletonNetty.INSTANCE.printServerMes("Prompt infomation","收到消息："+mesStr.substring(0,100));

        SingletonNetty.INSTANCE.nettyMesList.add(mesStr);

        //channelHandlerContext.channel().writeAndFlush(mesStr);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        SingletonNetty.INSTANCE.setServerCtx(ctx);
        SingletonNetty.INSTANCE.printServerMes("Prompt infomation","新加入节点客户端！");
        super.channelActive(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SingletonNetty.INSTANCE.printServerMes("Prompt infomation","服务器端发生异常消息："+cause.getMessage());
        SingletonNetty.INSTANCE.setServerCurrentState(SingletonNetty.ProcessState.SERVER_AND_CLIENT_IDLE);
        cause.printStackTrace();
        ctx.close();
    }

}
