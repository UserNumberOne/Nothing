package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.status.INetHandlerStatusServer;
import net.minecraft.network.status.client.CPacketPing;
import net.minecraft.network.status.client.CPacketServerQuery;
import net.minecraft.network.status.server.SPacketPong;
import net.minecraft.network.status.server.SPacketServerInfo;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftIconCache;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.util.CachedServerIcon;

public class NetHandlerStatusServer implements INetHandlerStatusServer {
   private static final ITextComponent EXIT_MESSAGE = new TextComponentString("Status request has been handled.");
   private final MinecraftServer server;
   private final NetworkManager networkManager;
   private boolean handled;

   public NetHandlerStatusServer(MinecraftServer minecraftserver, NetworkManager networkmanager) {
      this.server = minecraftserver;
      this.networkManager = networkmanager;
   }

   public void onDisconnect(ITextComponent ichatbasecomponent) {
   }

   public void processServerQuery(CPacketServerQuery packetstatusinstart) {
      if (this.handled) {
         this.networkManager.closeChannel(EXIT_MESSAGE);
      } else {
         this.handled = true;
         final Object[] players = this.server.getPlayerList().playerEntityList.toArray();
         class 1ServerListPingEvent extends ServerListPingEvent {
            CraftIconCache icon;

            _ServerListPingEvent/* $FF was: 1ServerListPingEvent*/() {
               super(((InetSocketAddress)NetHandlerStatusServer.this.networkManager.getRemoteAddress()).getAddress(), NetHandlerStatusServer.this.server.getMotd(), NetHandlerStatusServer.this.server.getPlayerList().getMaxPlayers());
               this.icon = NetHandlerStatusServer.this.server.server.getServerIcon();
            }

            public void setServerIcon(CachedServerIcon icon) {
               if (!(icon instanceof CraftIconCache)) {
                  throw new IllegalArgumentException(icon + " was not created by " + CraftServer.class);
               } else {
                  this.icon = (CraftIconCache)icon;
               }
            }

            public Iterator iterator() throws UnsupportedOperationException {
               return new Iterator() {
                  int i;
                  int ret = Integer.MIN_VALUE;
                  EntityPlayerMP player;

                  public boolean hasNext() {
                     if (this.player != null) {
                        return true;
                     } else {
                        Object[] currentPlayers = players;
                        int length = currentPlayers.length;

                        for(int i = this.i; i < length; ++i) {
                           EntityPlayerMP player = (EntityPlayerMP)currentPlayers[i];
                           if (player != null) {
                              this.i = i + 1;
                              this.player = player;
                              return true;
                           }
                        }

                        return false;
                     }
                  }

                  public Player next() {
                     if (!this.hasNext()) {
                        throw new NoSuchElementException();
                     } else {
                        EntityPlayerMP player = this.player;
                        this.player = null;
                        this.ret = this.i - 1;
                        return player.getBukkitEntity();
                     }
                  }

                  public void remove() {
                     Object[] currentPlayers = players;
                     int i = this.ret;
                     if (i >= 0 && currentPlayers[i] != null) {
                        currentPlayers[i] = null;
                     } else {
                        throw new IllegalStateException();
                     }
                  }
               };
            }
         }

         1ServerListPingEvent event = new 1ServerListPingEvent();
         this.server.server.getPluginManager().callEvent(event);
         List profiles = new ArrayList(players.length);

         for(Object player : players) {
            if (player != null) {
               profiles.add(((EntityPlayerMP)player).getGameProfile());
            }
         }

         ServerStatusResponse.Players playerSample = new ServerStatusResponse.Players(event.getMaxPlayers(), profiles.size());
         playerSample.setPlayers((GameProfile[])profiles.toArray(new GameProfile[profiles.size()]));
         ServerStatusResponse ping = new ServerStatusResponse();
         ping.setFavicon(event.icon.value);
         ping.setServerDescription(new TextComponentString(event.getMotd()));
         ping.setPlayers(playerSample);
         int version = this.server.getServerPing().getVersion().getProtocol();
         ping.setVersion(new ServerStatusResponse.Version(this.server.getServerModName() + " " + this.server.getVersion(), version));
         this.networkManager.sendPacket(new SPacketServerInfo(ping));
      }

   }

   public void processPing(CPacketPing packetstatusinping) {
      this.networkManager.sendPacket(new SPacketPong(packetstatusinping.getClientTime()));
      this.networkManager.closeChannel(EXIT_MESSAGE);
   }
}
