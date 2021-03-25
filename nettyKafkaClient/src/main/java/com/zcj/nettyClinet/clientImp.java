package com.zcj.nettyClinet;

import com.zcj.nettyClinet.tools.LinkedQueue;
import com.zcj.nettyClinet.tools.SingletonNetty;
import com.zcj.nettyClinet.tools.SyncFileInfo;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.text.SimpleDateFormat;
import java.util.Date;

public class clientImp {
    public void startClient(LinkedQueue<SyncFileInfo> fileList, String host, int port){
        SingletonNetty.INSTANCE.setFileList(fileList);

        EventLoopGroup group = new NioEventLoopGroup();
        SingletonNetty.INSTANCE.printClientMes("Prompt infomation","开启客户端...");
        clientInit();

        try {
            //创建客户端启动对象
            //注意客户端使用的不是 ServerBootstrap 而是 Bootstrap
            Bootstrap bootstrap = new Bootstrap();

            //设置相关参数
            bootstrap.group(group) //设置线程组
                    .channel(NioSocketChannel.class) // 设置客户端通道的实现类(反射)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //消息长度不包含长度区域的大小
                            pipeline.addLast("encoder", new PackageCollectorEncoder());
                            pipeline.addLast("decoder",new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE,SingletonNetty.LENGTH_BYTES_OFFSET,SingletonNetty.LENGTH_BYTES_LENGTH,SingletonNetty.LENGTH_BYTES_ADJUSTMENT,SingletonNetty.LENGTH_BYTES_IGNORE));
                            pipeline.addLast(new NettyClientHandler()); //加入自己的处理器
                        }
                    });

            //启动客户端去连接服务器端
            //关于 ChannelFuture 要分析，涉及到netty的异步模型
            ChannelFuture channelFuture = bootstrap.connect(host, port).sync();


            //以下是转线程逻辑
            ClientBusiness.serviceCode();

            //给关闭通道进行监听
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }
    }

    private void clientInit(){
        //SingletonNetty.INSTANCE.setClientCurrentState(SingletonNetty.ProcessState.SERVER_AND_CLIENT_IDLE);
        //做一些初始化工作
        SingletonNetty.INSTANCE.getSrcBlockList().initQueue();
    }
}
