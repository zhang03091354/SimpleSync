package com.zcj.nettyServer;

import com.zcj.nettyServer.tools.SingletonNetty;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class serverImp {
    public void startNettyServer(String hostname, int port){

        SingletonNetty.INSTANCE.printServerMes("Prompt infomation","开启服务器。。。。");
        serverInit();

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try{
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("encoder", new PackageCollectorEncoder());
                            pipeline.addLast("decoder",new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, SingletonNetty.LENGTH_BYTES_OFFSET, SingletonNetty.LENGTH_BYTES_LENGTH, SingletonNetty.LENGTH_BYTES_ADJUSTMENT, SingletonNetty.LENGTH_BYTES_IGNORE));
                            pipeline.addLast(new NettyServerHandler()); //加入自己的处理器
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(hostname,port).sync();
            //业务逻辑
            ServerBusiness.serviceCode();

            channelFuture.channel().closeFuture().sync();
        } catch (Exception e){
            e.printStackTrace();
        } finally{
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void serverInit(){
        //初始化系统状态
        SingletonNetty.INSTANCE.setServerCurrentState(SingletonNetty.ProcessState.SERVER_AND_CLIENT_IDLE);
        //初始化kafka生产者
        SingletonNetty.INSTANCE.kafkaProducerInit();
    }
}
