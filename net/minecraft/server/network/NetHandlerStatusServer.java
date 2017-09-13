package net.minecraft.server.network;

import com.mojang.authlib.GameProfile;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Iterator;
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

   public NetHandlerStatusServer(MinecraftServer var1, NetworkManager var2) {
      this.server = var1;
      this.networkManager = var2;
   }

   public void onDisconnect(ITextComponent var1) {
   }

   public void processServerQuery(CPacketServerQuery var1) {
      if (this.handled) {
         this.networkManager.closeChannel(EXIT_MESSAGE);
      } else {
         this.handled = true;
         final Object[] var2 = this.server.getPlayerList().playerEntityList.toArray();
         class 1ServerListPingEvent extends ServerListPingEvent {
            CraftIconCache icon;

            _ServerListPingEvent/* $FF was: 1ServerListPingEvent*/() {
               super(((InetSocketAddress)NetHandlerStatusServer.this.networkManager.getRemoteAddress()).getAddress(), NetHandlerStatusServer.this.server.getMotd(), NetHandlerStatusServer.this.server.getPlayerList().getMaxPlayers());
               this.icon = NetHandlerStatusServer.this.server.server.getServerIcon();
            }

            public void setServerIcon(CachedServerIcon var1) {
               if (!(var1 instanceof CraftIconCache)) {
                  throw new IllegalArgumentException(var1 + " was not created by " + CraftServer.class);
               } else {
                  this.icon = (CraftIconCache)var1;
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
                        Object[] var1 = var2;
                        int var2x = var1.length;

                        for(int var3 = this.i; var3 < var2x; ++var3) {
                           EntityPlayerMP var4 = (EntityPlayerMP)var1[var3];
                           if (var4 != null) {
                              this.i = var3 + 1;
                              this.player = var4;
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
                        EntityPlayerMP var1 = this.player;
                        this.player = null;
                        this.ret = this.i - 1;
                        return var1.getBukkitEntity();
                     }
                  }

                  public void remove() {
                     Object[] var1 = var2;
                     int var2x = this.ret;
                     if (var2x >= 0 && var1[var2x] != null) {
                        var1[var2x] = null;
                     } else {
                        throw new IllegalStateException();
                     }
                  }
               };
            }
         }

         1ServerListPingEvent var3 = new 1ServerListPingEvent();
         this.server.server.getPluginManager().callEvent(var3);
         ArrayList var4 = new ArrayList(var2.length);

         for(Object var8 : var2) {
            if (var8 != null) {
               var4.add(((EntityPlayerMP)var8).getGameProfile());
            }
         }

         ServerStatusResponse.Players var11 = new ServerStatusResponse.Players(var3.getMaxPlayers(), var4.size());
         var11.setPlayers((GameProfile[])var4.toArray(new GameProfile[var4.size()]));
         ServerStatusResponse var10 = new ServerStatusResponse();
         var10.setFavicon(var3.icon.value);
         var10.setServerDescription(new TextComponentString(var3.getMotd()));
         var10.setPlayers(var11);
         int var9 = this.server.getServerPing().getVersion().getProtocol();
         var10.setVersion(new ServerStatusResponse.Version(this.server.getServerModName() + " " + this.server.getVersion(), var9));
         this.networkManager.sendPacket(new SPacketServerInfo(var10));
      }

   }

   public void processPing(CPacketPing var1) {
      this.networkManager.sendPacket(new SPacketPong(var1.getClientTime()));
      this.networkManager.closeChannel(EXIT_MESSAGE);
   }
}
