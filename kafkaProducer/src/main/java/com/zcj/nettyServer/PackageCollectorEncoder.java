package com.zcj.nettyServer;

import com.zcj.nettyServer.tools.SingletonNetty;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PackageCollectorEncoder extends MessageToByteEncoder<String>
{

    @Override
    protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out)
    {
        byte[] bytes = msg.getBytes();// 将对象转换为byte
        int length = bytes.length;// 读取 ProtoMsg 消息的长度
        ByteBuf buf = Unpooled.buffer(SingletonNetty.INSTANCE.LENGTH_BYTES_LENGTH + length);
        // 再将 ProtoMsg 消息的长度写入
        buf.writeInt(length);
        // 写入 ProtoMsg 消息的消息体
        buf.writeBytes(bytes);
        //发送
        out.writeBytes(buf);
        buf.release();
    }
}

