package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.server.network.NetHandlerHandshakeTCP;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.LazyLoadBase;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkSystem {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final LazyLoadBase SERVER_NIO_EVENTLOOP = new LazyLoadBase() {
      protected NioEventLoopGroup load() {
         return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Server IO #%d").setDaemon(true).build());
      }

      // $FF: synthetic method
      protected Object load() {
         return this.load();
      }
   };
   public static final LazyLoadBase SERVER_EPOLL_EVENTLOOP = new LazyLoadBase() {
      protected EpollEventLoopGroup load() {
         return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Server IO #%d").setDaemon(true).build());
      }

      // $FF: synthetic method
      protected Object load() {
         return this.load();
      }
   };
   public static final LazyLoadBase SERVER_LOCAL_EVENTLOOP = new LazyLoadBase() {
      protected LocalEventLoopGroup load() {
         return new LocalEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Server IO #%d").setDaemon(true).build());
      }

      // $FF: synthetic method
      protected Object load() {
         return this.load();
      }
   };
   private final MinecraftServer mcServer;
   public volatile boolean isAlive;
   private final List endpoints = Collections.synchronizedList(Lists.newArrayList());
   private final List networkManagers = Collections.synchronizedList(Lists.newArrayList());

   public NetworkSystem(MinecraftServer var1) {
      this.mcServer = var1;
      this.isAlive = true;
   }

   public void addLanEndpoint(InetAddress var1, int var2) throws IOException {
      synchronized(this.endpoints) {
         Class var4;
         LazyLoadBase var5;
         if (Epoll.isAvailable() && this.mcServer.ae()) {
            var4 = EpollServerSocketChannel.class;
            var5 = SERVER_EPOLL_EVENTLOOP;
            LOGGER.info("Using epoll channel type");
         } else {
            var4 = NioServerSocketChannel.class;
            var5 = SERVER_NIO_EVENTLOOP;
            LOGGER.info("Using default channel type");
         }

         this.endpoints.add(((ServerBootstrap)((ServerBootstrap)(new ServerBootstrap()).channel(var4)).childHandler(new ChannelInitializer() {
            protected void initChannel(Channel var1) throws Exception {
               try {
                  var1.config().setOption(ChannelOption.TCP_NODELAY, Boolean.valueOf(true));
               } catch (ChannelException var3) {
                  ;
               }

               var1.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).addLast("legacy_query", new LegacyPingHandler(NetworkSystem.this)).addLast("splitter", new NettyVarint21FrameDecoder()).addLast("decoder", new NettyPacketDecoder(EnumPacketDirection.SERVERBOUND)).addLast("prepender", new NettyVarint21FrameEncoder()).addLast("encoder", new NettyPacketEncoder(EnumPacketDirection.CLIENTBOUND));
               NetworkManager var2 = new NetworkManager(EnumPacketDirection.SERVERBOUND);
               NetworkSystem.this.networkManagers.add(var2);
               var1.pipeline().addLast("packet_handler", var2);
               var2.setNetHandler(new NetHandlerHandshakeTCP(NetworkSystem.this.mcServer, var2));
            }
         }).group((EventLoopGroup)var5.getValue()).localAddress(var1, var2)).bind().syncUninterruptibly());
      }
   }

   public void terminateEndpoints() {
      this.isAlive = false;

      for(ChannelFuture var2 : this.endpoints) {
         try {
            var2.channel().close().sync();
         } catch (InterruptedException var4) {
            LOGGER.error("Interrupted whilst closing channel");
         }
      }

   }

   public void networkTick() {
      synchronized(this.networkManagers) {
         Iterator var2 = this.networkManagers.iterator();

         while(var2.hasNext()) {
            final NetworkManager var3 = (NetworkManager)var2.next();
            if (!var3.hasNoChannel()) {
               if (var3.isChannelOpen()) {
                  try {
                     var3.processReceivedPackets();
                  } catch (Exception var8) {
                     if (var3.isLocalChannel()) {
                        CrashReport var10 = CrashReport.makeCrashReport(var8, "Ticking memory connection");
                        CrashReportCategory var6 = var10.makeCategory("Ticking connection");
                        var6.setDetail("Connection", new ICrashReportDetail() {
                           public String call() throws Exception {
                              return var3.toString();
                           }

                           // $FF: synthetic method
                           public Object call() throws Exception {
                              return this.call();
                           }
                        });
                        throw new ReportedException(var10);
                     }

                     LOGGER.warn("Failed to handle packet for {}", new Object[]{var3.getRemoteAddress(), var8});
                     final TextComponentString var5 = new TextComponentString("Internal server error");
                     var3.sendPacket(new SPacketDisconnect(var5), new GenericFutureListener() {
                        public void operationComplete(Future var1) throws Exception {
                           var3.closeChannel(var5);
                        }
                     });
                     var3.disableAutoRead();
                  }
               } else {
                  var2.remove();
                  var3.checkDisconnected();
               }
            }
         }

      }
   }

   public MinecraftServer d() {
      return this.mcServer;
   }
}
