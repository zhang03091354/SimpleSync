package com.zcj.nettyClinet;

import com.zcj.nettyClinet.tools.SingletonNetty;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;


public class NettyClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //与服务器连接成功，将handler句柄保存到单例
        SingletonNetty.INSTANCE.setClientCtx(ctx);
        SingletonNetty.INSTANCE.printClientMes("Prompt infomation","客户端已连接至服务器...");

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object mesBase) throws Exception {
        //super.channelRead(ctx, msg);
        int readableBytes = ((ByteBuf)mesBase).readableBytes();
        CharSequence charSequence = ((ByteBuf)mesBase).readCharSequence(readableBytes, Charset.defaultCharset());
        String mesStr = charSequence.subSequence(0, charSequence.length()).toString();

        SingletonNetty.INSTANCE.printClientMes("time","收到消息");
        //SingletonNetty.INSTANCE.printClientMes("Prompt infomation","客户端收到消息："+mesStr);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        SingletonNetty.INSTANCE.printClientMes("Prompt infomation","客户端发生异常消息：" + cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }


}
