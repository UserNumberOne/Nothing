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
import io.netty.util.concurrent.Future;
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

   public NetworkManager(EnumPacketDirection enumprotocoldirection) {
      this.direction = enumprotocoldirection;
   }

   public void channelActive(ChannelHandlerContext channelhandlercontext) throws Exception {
      super.channelActive(channelhandlercontext);
      this.channel = channelhandlercontext.channel();
      this.socketAddress = this.channel.remoteAddress();

      try {
         this.setConnectionState(EnumConnectionState.HANDSHAKING);
      } catch (Throwable var3) {
         LOGGER.fatal(var3);
      }

   }

   public void setConnectionState(EnumConnectionState enumprotocol) {
      this.channel.attr(PROTOCOL_ATTRIBUTE_KEY).set(enumprotocol);
      this.channel.config().setAutoRead(true);
      LOGGER.debug("Enabled auto read");
   }

   public void channelInactive(ChannelHandlerContext channelhandlercontext) throws Exception {
      this.closeChannel(new TextComponentTranslation("disconnect.endOfStream", new Object[0]));
   }

   public void exceptionCaught(ChannelHandlerContext channelhandlercontext, Throwable throwable) throws Exception {
      TextComponentTranslation chatmessage;
      if (throwable instanceof TimeoutException) {
         chatmessage = new TextComponentTranslation("disconnect.timeout", new Object[0]);
      } else {
         chatmessage = new TextComponentTranslation("disconnect.genericReason", new Object[]{"Internal Exception: " + throwable});
      }

      LOGGER.debug(throwable);
      this.closeChannel(chatmessage);
   }

   protected void channelRead0(ChannelHandlerContext param1, Packet param2) throws Exception {
      // $FF: Couldn't be decompiled
   }

   public void setNetHandler(INetHandler packetlistener) {
      Validate.notNull(packetlistener, "packetListener", new Object[0]);
      LOGGER.debug("Set listener of {} to {}", new Object[]{this, packetlistener});
      this.packetListener = packetlistener;
   }

   public void sendPacket(Packet packet) {
      if (this.isChannelOpen()) {
         this.flushOutboundQueue();
         this.dispatchPacket(packet, (GenericFutureListener[])null);
      } else {
         this.readWriteLock.writeLock().lock();

         try {
            this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packet, (GenericFutureListener[])null));
         } finally {
            this.readWriteLock.writeLock().unlock();
         }
      }

   }

   public void sendPacket(Packet packet, GenericFutureListener genericfuturelistener, GenericFutureListener... agenericfuturelistener) {
      if (this.isChannelOpen()) {
         this.flushOutboundQueue();
         this.dispatchPacket(packet, (GenericFutureListener[])ArrayUtils.add(agenericfuturelistener, 0, genericfuturelistener));
      } else {
         this.readWriteLock.writeLock().lock();

         try {
            this.outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packet, (GenericFutureListener[])ArrayUtils.add(agenericfuturelistener, 0, genericfuturelistener)));
         } finally {
            this.readWriteLock.writeLock().unlock();
         }
      }

   }

   private void dispatchPacket(final Packet packet, @Nullable final GenericFutureListener[] agenericfuturelistener) {
      final EnumConnectionState enumprotocol = EnumConnectionState.getFromPacket(packet);
      final EnumConnectionState enumprotocol1 = (EnumConnectionState)this.channel.attr(PROTOCOL_ATTRIBUTE_KEY).get();
      if (enumprotocol1 != enumprotocol) {
         LOGGER.debug("Disabled auto read");
         this.channel.config().setAutoRead(false);
      }

      if (this.channel.eventLoop().inEventLoop()) {
         if (enumprotocol != enumprotocol1) {
            this.setConnectionState(enumprotocol);
         }

         ChannelFuture channelfuture = this.channel.writeAndFlush(packet);
         if (agenericfuturelistener != null) {
            channelfuture.addListeners(agenericfuturelistener);
         }

         channelfuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
      } else {
         this.channel.eventLoop().execute(new Runnable() {
            public void run() {
               if (enumprotocol != enumprotocol1) {
                  NetworkManager.this.setConnectionState(enumprotocol);
               }

               ChannelFuture channelfuture = NetworkManager.this.channel.writeAndFlush(packet);
               if (agenericfuturelistener != null) {
                  channelfuture.addListeners(agenericfuturelistener);
               }

               channelfuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            }
         });
      }

   }

   private void flushOutboundQueue() {
      if (this.channel != null && this.channel.isOpen()) {
         this.readWriteLock.readLock().lock();

         try {
            while(!this.outboundPacketsQueue.isEmpty()) {
               NetworkManager.InboundHandlerTuplePacketListener networkmanager_queuedpacket = (NetworkManager.InboundHandlerTuplePacketListener)this.outboundPacketsQueue.poll();
               this.dispatchPacket(networkmanager_queuedpacket.packet, networkmanager_queuedpacket.futureListeners);
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

   public void closeChannel(ITextComponent ichatbasecomponent) {
      if (this.channel.isOpen()) {
         this.channel.close();
         this.terminationReason = ichatbasecomponent;
      }

   }

   public boolean isLocalChannel() {
      return this.channel instanceof LocalChannel || this.channel instanceof LocalServerChannel;
   }

   public void enableEncryption(SecretKey secretkey) {
      this.isEncrypted = true;
      this.channel.pipeline().addBefore("splitter", "decrypt", new NettyEncryptingDecoder(CryptManager.createNetCipherInstance(2, secretkey)));
      this.channel.pipeline().addBefore("prepender", "encrypt", new NettyEncryptingEncoder(CryptManager.createNetCipherInstance(1, secretkey)));
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

   public void setCompressionThreshold(int i) {
      if (i >= 0) {
         if (this.channel.pipeline().get("decompress") instanceof NettyCompressionDecoder) {
            ((NettyCompressionDecoder)this.channel.pipeline().get("decompress")).setCompressionThreshold(i);
         } else {
            this.channel.pipeline().addBefore("decoder", "decompress", new NettyCompressionDecoder(i));
         }

         if (this.channel.pipeline().get("compress") instanceof NettyCompressionEncoder) {
            ((NettyCompressionEncoder)this.channel.pipeline().get("compress")).setCompressionThreshold(i);
         } else {
            this.channel.pipeline().addBefore("encoder", "compress", new NettyCompressionEncoder(i));
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

      public InboundHandlerTuplePacketListener(Packet packet, GenericFutureListener... agenericfuturelistener) {
         this.packet = packet;
         this.futureListeners = agenericfuturelistener;
      }
   }
}
