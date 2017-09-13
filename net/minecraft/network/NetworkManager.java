package net.minecraft.network;

import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GenericFutureListener;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;
import javax.crypto.SecretKey;
import net.minecraft.util.CryptManager;
import net.minecraft.util.ITickable;
import net.minecraft.util.LazyLoadBase;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

public class NetworkManager extends SimpleChannelInboundHandler {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final Marker NETWORK_MARKER = MarkerManager.getMarker("NETWORK");
   public static final Marker NETWORK_PACKETS_MARKER = MarkerManager.getMarker("NETWORK_PACKETS", NETWORK_MARKER);
   public static final AttributeKey PROTOCOL_ATTRIBUTE_KEY = AttributeKey.valueOf("protocol");
   public static final LazyLoadBase CLIENT_NIO_EVENTLOOP = new LazyLoadBase() {
      protected NioEventLoopGroup load() {
         return new NioEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Client IO #%d").setDaemon(true).build());
      }

      protected Object load() {
         return this.load();
      }
   };
   public static final LazyLoadBase CLIENT_EPOLL_EVENTLOOP = new LazyLoadBase() {
      protected EpollEventLoopGroup load() {
         return new EpollEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Epoll Client IO #%d").setDaemon(true).build());
      }

      protected Object load() {
         return this.load();
      }
   };
   public static final LazyLoadBase CLIENT_LOCAL_EVENTLOOP = new LazyLoadBase() {
      protected LocalEventLoopGroup load() {
         return new LocalEventLoopGroup(0, (new ThreadFactoryBuilder()).setNameFormat("Netty Local Client IO #%d").setDaemon(true).build());
      }

      protected Object load() {
         return this.load();
      }
   };
   private final EnumPacketDirection direction;
   private final Queue outboundPacketsQueue = Queues.newConcurrentLinkedQueue();
   private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
   public Channel channel;
   private SocketAddress socketAddress;
   private INetHandler packetListener;
   private ITextComponent terminationReason;
   private boolean isEncrypted;
   private boolean disconnected;

   public NetworkManager(EnumPacketDirection var1) {
      this.direction = var1;
   }

   public void channelActive(ChannelHandlerContext var1) throws Exception {
      super.channelActive(var1);
      this.channel = var1.channel();
      this.socketAddress = this.channel.remoteAddress();

      try {
         this.setConnectionState(EnumConnectionState.HANDSHAKING);
      } catch (Throwable var3) {
         LOGGER.fatal(var3);
      }

   }

   public void setConnectionState(EnumConnectionState var1) {
      this.channel.attr(PROTOCOL_ATTRIBUTE_KEY).set(var1);
      this.channel.config().setAutoRead(true);
      LOGGER.debug("Enabled auto read");
   }

   public void channelInactive(ChannelHandlerContext var1) throws Exception {
      this.closeChannel(new TextComponentTranslation("disconnect.endOfStream", new Object[0]));
   }

   public void exceptionCaught(ChannelHandlerContext var1, Throwable var2) throws Exception {
      TextComponentTranslation var3;
      if (var2 instanceof TimeoutException) {
         var3 = new TextComponentTranslation("disconnect.timeout", new Object[0]);
      } else {
         var3 = new TextComponentTranslation("disconnect.genericReason", new Object[]{"Internal Exception: " + var2});
      }

      LOGGER.debug(var2);
      this.closeChannel(var3);
   }

   protected void channelRead0(ChannelHandlerContext param1, Packet param2) throws Exception {
      // $FF: Couldn't be decompiled
   }

   public void setNetHandler(INetHandler var1) {
      Validate.notNull(var1, "packetListener", new Object[0]);
      LOGGER.debug("Set listener of {} to {}", new Object[]{this, var1});
      this.packetListener = var1;
   }

   public void sendPacket(Packet var1) {
      if (this.isChannelOpen()) {
         this.flushOutboundQueue();
         this.dispatchPacket(var1, (GenericFutureListener[])null);
      } else {
         this.readWriteLock.writeLock().lock();

         try {
            this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(var1, (GenericFutureListener[])null));
         } finally {
            this.readWriteLock.writeLock().unlock();
         }
      }

   }

   public void sendPacket(Packet var1, GenericFutureListener var2, GenericFutureListener... var3) {
      if (this.isChannelOpen()) {
         this.flushOutboundQueue();
         this.dispatchPacket(var1, (GenericFutureListener[])ArrayUtils.add(var3, 0, var2));
      } else {
         this.readWriteLock.writeLock().lock();

         try {
            this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(var1, (GenericFutureListener[])ArrayUtils.add(var3, 0, var2)));
         } finally {
            this.readWriteLock.writeLock().unlock();
         }
      }

   }

   private void dispatchPacket(final Packet var1, @Nullable final GenericFutureListener[] var2) {
      final EnumConnectionState var3 = EnumConnectionState.getFromPacket(var1);
      final EnumConnectionState var4 = (EnumConnectionState)this.channel.attr(PROTOCOL_ATTRIBUTE_KEY).get();
      if (var4 != var3) {
         LOGGER.debug("Disabled auto read");
         this.channel.config().setAutoRead(false);
      }

      if (this.channel.eventLoop().inEventLoop()) {
         if (var3 != var4) {
            this.setConnectionState(var3);
         }

         ChannelFuture var5 = this.channel.writeAndFlush(var1);
         if (var2 != null) {
            var5.addListeners(var2);
         }

         var5.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
      } else {
         this.channel.eventLoop().execute(new Runnable() {
            public void run() {
               if (var3 != var4) {
                  NetworkManager.this.setConnectionState(var3);
               }

               ChannelFuture var1x = NetworkManager.this.channel.writeAndFlush(var1);
               if (var2 != null) {
                  var1x.addListeners(var2);
               }

               var1x.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            }
         });
      }

   }

   private void flushOutboundQueue() {
      if (this.channel != null && this.channel.isOpen()) {
         this.readWriteLock.readLock().lock();

         try {
            while(!this.outboundPacketsQueue.isEmpty()) {
               NetworkManager.InboundHandlerTuplePacketListener var1 = (NetworkManager.InboundHandlerTuplePacketListener)this.outboundPacketsQueue.poll();
               this.dispatchPacket(var1.packet, var1.futureListeners);
            }
         } finally {
            this.readWriteLock.readLock().unlock();
         }
      }

   }

   public void processReceivedPackets() {
      this.flushOutboundQueue();
      if (this.packetListener instanceof ITickable) {
         ((ITickable)this.packetListener).update();
      }

      this.channel.flush();
   }

   public SocketAddress getRemoteAddress() {
      return this.socketAddress;
   }

   public void closeChannel(ITextComponent var1) {
      if (this.channel.isOpen()) {
         this.channel.close();
         this.terminationReason = var1;
      }

   }

   public boolean isLocalChannel() {
      return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
   }

   public void enableEncryption(SecretKey var1) {
      this.isEncrypted = true;
      this.channel.pipeline().addBefore("splitter", "decrypt", new NettyEncryptingDecoder(CryptManager.createNetCipherInstance(2, var1)));
      this.channel.pipeline().addBefore("prepender", "encrypt", new NettyEncryptingEncoder(CryptManager.createNetCipherInstance(1, var1)));
   }

   public boolean isChannelOpen() {
      return this.channel != null && this.channel.isOpen();
   }

   public boolean hasNoChannel() {
      return this.channel == null;
   }

   public INetHandler getNetHandler() {
      return this.packetListener;
   }

   public ITextComponent getExitMessage() {
      return this.terminationReason;
   }

   public void disableAutoRead() {
      this.channel.config().setAutoRead(false);
   }

   public void setCompressionThreshold(int var1) {
      if (var1 >= 0) {
         if (this.channel.pipeline().get("decompress") instanceof NettyCompressionDecoder) {
            ((NettyCompressionDecoder)this.channel.pipeline().get("decompress")).setCompressionThreshold(var1);
         } else {
            this.channel.pipeline().addBefore("decoder", "decompress", new NettyCompressionDecoder(var1));
         }

         if (this.channel.pipeline().get("compress") instanceof NettyCompressionEncoder) {
            ((NettyCompressionEncoder)this.channel.pipeline().get("compress")).setCompressionThreshold(var1);
         } else {
            this.channel.pipeline().addBefore("encoder", "compress", new NettyCompressionEncoder(var1));
         }
      } else {
         if (this.channel.pipeline().get("decompress") instanceof NettyCompressionDecoder) {
            this.channel.pipeline().remove("decompress");
         }

         if (this.channel.pipeline().get("compress") instanceof NettyCompressionEncoder) {
            this.channel.pipeline().remove("compress");
         }
      }

   }

   public void checkDisconnected() {
      if (this.channel != null && !this.channel.isOpen()) {
         if (this.disconnected) {
            LOGGER.warn("handleDisconnection() called twice");
         } else {
            this.disconnected = true;
            if (this.getExitMessage() != null) {
               this.getNetHandler().onDisconnect(this.getExitMessage());
            } else if (this.getNetHandler() != null) {
               this.getNetHandler().onDisconnect(new TextComponentString("Disconnected"));
            }
         }
      }

   }

   protected void channelRead0(ChannelHandlerContext param1, Packet param2) throws Exception {
      // $FF: Couldn't be decompiled
   }

   static class InboundHandlerTuplePacketListener {
      private final Packet packet;
      private final GenericFutureListener[] futureListeners;

      public InboundHandlerTuplePacketListener(Packet var1, GenericFutureListener... var2) {
         this.packet = var1;
         this.futureListeners = var2;
      }
   }
}
