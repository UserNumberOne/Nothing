package net.minecraft.client.network;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.status.INetHandlerStatusClient;
import net.minecraft.network.status.client.CPacketPing;
import net.minecraft.network.status.client.CPacketServerQuery;
import net.minecraft.network.status.server.SPacketPong;
import net.minecraft.network.status.server.SPacketServerInfo;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class ServerPinger {
   private static final Splitter PING_RESPONSE_SPLITTER = Splitter.on('\u0000').limit(6);
   private static final Logger LOGGER = LogManager.getLogger();
   private final List pingDestinations = Collections.synchronizedList(Lists.newArrayList());

   public void ping(final ServerData var1) throws UnknownHostException {
      ServerAddress var2 = ServerAddress.fromString(var1.serverIP);
      final NetworkManager var3 = NetworkManager.createNetworkManagerAndConnect(InetAddress.getByName(var2.getIP()), var2.getPort(), false);
      this.pingDestinations.add(var3);
      var1.serverMOTD = "Pinging...";
      var1.pingToServer = -1L;
      var1.playerList = null;
      var3.setNetHandler(new INetHandlerStatusClient() {
         private boolean successful;
         private boolean receivedStatus;
         private long pingSentAt;

         public void handleServerInfo(SPacketServerInfo var1x) {
            if (this.receivedStatus) {
               var3.closeChannel(new TextComponentString("Received unrequested status"));
            } else {
               this.receivedStatus = true;
               ServerStatusResponse var2 = var1x.getResponse();
               if (var2.getServerDescription() != null) {
                  var1.serverMOTD = var2.getServerDescription().getFormattedText();
               } else {
                  var1.serverMOTD = "";
               }

               if (var2.getVersion() != null) {
                  var1.gameVersion = var2.getVersion().getName();
                  var1.version = var2.getVersion().getProtocol();
               } else {
                  var1.gameVersion = "Old";
                  var1.version = 0;
               }

               if (var2.getPlayers() == null) {
                  var1.populationInfo = TextFormatting.DARK_GRAY + "???";
               } else {
                  var1.populationInfo = TextFormatting.GRAY + "" + var2.getPlayers().getOnlinePlayerCount() + "" + TextFormatting.DARK_GRAY + "/" + TextFormatting.GRAY + var2.getPlayers().getMaxPlayers();
                  if (ArrayUtils.isNotEmpty(var2.getPlayers().getPlayers())) {
                     StringBuilder var3x = new StringBuilder();

                     for(GameProfile var7 : var2.getPlayers().getPlayers()) {
                        if (var3x.length() > 0) {
                           var3x.append("\n");
                        }

                        var3x.append(var7.getName());
                     }

                     if (var2.getPlayers().getPlayers().length < var2.getPlayers().getOnlinePlayerCount()) {
                        if (var3x.length() > 0) {
                           var3x.append("\n");
                        }

                        var3x.append("... and ").append(var2.getPlayers().getOnlinePlayerCount() - var2.getPlayers().getPlayers().length).append(" more ...");
                     }

                     var1.playerList = var3x.toString();
                  }
               }

               if (var2.getFavicon() != null) {
                  String var8 = var2.getFavicon();
                  if (var8.startsWith("data:image/png;base64,")) {
                     var1.setBase64EncodedIconData(var8.substring("data:image/png;base64,".length()));
                  } else {
                     ServerPinger.LOGGER.error("Invalid server icon (unknown format)");
                  }
               } else {
                  var1.setBase64EncodedIconData((String)null);
               }

               FMLClientHandler.instance().bindServerListData(var1, var2);
               this.pingSentAt = Minecraft.getSystemTime();
               var3.sendPacket(new CPacketPing(this.pingSentAt));
               this.successful = true;
            }

         }

         public void handlePong(SPacketPong var1x) {
            long var2 = this.pingSentAt;
            long var4 = Minecraft.getSystemTime();
            var1.pingToServer = var4 - var2;
            var3.closeChannel(new TextComponentString("Finished"));
         }

         public void onDisconnect(ITextComponent var1x) {
            if (!this.successful) {
               ServerPinger.LOGGER.error("Can't ping {}: {}", new Object[]{var1.serverIP, var1x.getUnformattedText()});
               var1.serverMOTD = TextFormatting.DARK_RED + "Can't connect to server.";
               var1.populationInfo = "";
               ServerPinger.this.tryCompatibilityPing(var1);
            }

         }
      });

      try {
         var3.sendPacket(new C00Handshake(210, var2.getIP(), var2.getPort(), EnumConnectionState.STATUS, true));
         var3.sendPacket(new CPacketServerQuery());
      } catch (Throwable var5) {
         LOGGER.error(var5);
      }

   }

   private void tryCompatibilityPing(final ServerData var1) {
      final ServerAddress var2 = ServerAddress.fromString(var1.serverIP);
      ((Bootstrap)((Bootstrap)((Bootstrap)(new Bootstrap()).group((EventLoopGroup)NetworkManager.CLIENT_NIO_EVENTLOOP.getValue())).handler(new ChannelInitializer() {
         protected void initChannel(Channel var1x) throws Exception {
            try {
               var1x.config().setOption(ChannelOption.TCP_NODELAY, Boolean.valueOf(true));
            } catch (ChannelException var3) {
               ;
            }

            var1x.pipeline().addLast(new ChannelHandler[]{new SimpleChannelInboundHandler() {
               public void channelActive(ChannelHandlerContext var1x) throws Exception {
                  super.channelActive(var1x);
                  ByteBuf var2x = Unpooled.buffer();

                  try {
                     var2x.writeByte(254);
                     var2x.writeByte(1);
                     var2x.writeByte(250);
                     char[] var3 = "MC|PingHost".toCharArray();
                     var2x.writeShort(var3.length);

                     for(char var7 : var3) {
                        var2x.writeChar(var7);
                     }

                     var2x.writeShort(7 + 2 * var2.getIP().length());
                     var2x.writeByte(127);
                     var3 = var2.getIP().toCharArray();
                     var2x.writeShort(var3.length);

                     for(char var15 : var3) {
                        var2x.writeChar(var15);
                     }

                     var2x.writeInt(var2.getPort());
                     var1x.channel().writeAndFlush(var2x).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
                  } finally {
                     var2x.release();
                  }

               }

               protected void channelRead0(ChannelHandlerContext var1x, ByteBuf var2x) throws Exception {
                  short var3 = var2x.readUnsignedByte();
                  if (var3 == 255) {
                     String var4 = new String(var2x.readBytes(var2x.readShort() * 2).array(), Charsets.UTF_16BE);
                     String[] var5 = (String[])Iterables.toArray(ServerPinger.PING_RESPONSE_SPLITTER.split(var4), String.class);
                     if ("§1".equals(var5[0])) {
                        int var6 = MathHelper.getInt(var5[1], 0);
                        String var7 = var5[2];
                        String var8 = var5[3];
                        int var9 = MathHelper.getInt(var5[4], -1);
                        int var10 = MathHelper.getInt(var5[5], -1);
                        var1.version = -1;
                        var1.gameVersion = var7;
                        var1.serverMOTD = var8;
                        var1.populationInfo = TextFormatting.GRAY + "" + var9 + "" + TextFormatting.DARK_GRAY + "/" + TextFormatting.GRAY + var10;
                     }
                  }

                  var1x.close();
               }

               public void exceptionCaught(ChannelHandlerContext var1x, Throwable var2x) throws Exception {
                  var1x.close();
               }
            }});
         }
      })).channel(NioSocketChannel.class)).connect(var2.getIP(), var2.getPort());
   }

   public void pingPendingNetworks() {
      synchronized(this.pingDestinations) {
         Iterator var2 = this.pingDestinations.iterator();

         while(var2.hasNext()) {
            NetworkManager var3 = (NetworkManager)var2.next();
            if (var3.isChannelOpen()) {
               var3.processReceivedPackets();
            } else {
               var2.remove();
               var3.checkDisconnected();
            }
         }

      }
   }

   public void clearPendingNetworks() {
      synchronized(this.pingDestinations) {
         Iterator var2 = this.pingDestinations.iterator();

         while(var2.hasNext()) {
            NetworkManager var3 = (NetworkManager)var2.next();
            if (var3.isChannelOpen()) {
               var2.remove();
               var3.closeChannel(new TextComponentString("Cancelled"));
            }
         }

      }
   }
}
