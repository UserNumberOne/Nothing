package net.minecraft.world;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketUpdateBossInfo;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;

public class BossInfoServer extends BossInfo {
   private final Set players = Sets.newHashSet();
   private final Set readOnlyPlayers;
   public boolean visible;

   public BossInfoServer(ITextComponent var1, BossInfo.Color var2, BossInfo.Overlay var3) {
      super(MathHelper.getRandomUUID(), var1, var2, var3);
      this.readOnlyPlayers = Collections.unmodifiableSet(this.players);
      this.visible = true;
   }

   public void setPercent(float var1) {
      if (var1 != this.percent) {
         super.setPercent(var1);
         this.sendUpdate(SPacketUpdateBossInfo.Operation.UPDATE_PCT);
      }

   }

   public BossInfo setDarkenSky(boolean var1) {
      if (var1 != this.darkenSky) {
         super.setDarkenSky(var1);
         this.sendUpdate(SPacketUpdateBossInfo.Operation.UPDATE_PROPERTIES);
      }

      return this;
   }

   public BossInfo setPlayEndBossMusic(boolean var1) {
      if (var1 != this.playEndBossMusic) {
         super.setPlayEndBossMusic(var1);
         this.sendUpdate(SPacketUpdateBossInfo.Operation.UPDATE_PROPERTIES);
      }

      return this;
   }

   public BossInfo setCreateFog(boolean var1) {
      if (var1 != this.createFog) {
         super.setCreateFog(var1);
         this.sendUpdate(SPacketUpdateBossInfo.Operation.UPDATE_PROPERTIES);
      }

      return this;
   }

   public void sendUpdate(SPacketUpdateBossInfo.Operation var1) {
      if (this.visible) {
         SPacketUpdateBossInfo var2 = new SPacketUpdateBossInfo(var1, this);

         for(EntityPlayerMP var4 : this.players) {
            var4.connection.sendPacket(var2);
         }
      }

   }

   public void addPlayer(EntityPlayerMP var1) {
      if (this.players.add(var1) && this.visible) {
         var1.connection.sendPacket(new SPacketUpdateBossInfo(SPacketUpdateBossInfo.Operation.ADD, this));
      }

   }

   public void removePlayer(EntityPlayerMP var1) {
      if (this.players.remove(var1) && this.visible) {
         var1.connection.sendPacket(new SPacketUpdateBossInfo(SPacketUpdateBossInfo.Operation.REMOVE, this));
      }

   }

   public void setVisible(boolean var1) {
      if (var1 != this.visible) {
         this.visible = var1;

         for(EntityPlayerMP var3 : this.players) {
            var3.connection.sendPacket(new SPacketUpdateBossInfo(var1 ? SPacketUpdateBossInfo.Operation.ADD : SPacketUpdateBossInfo.Operation.REMOVE, this));
         }
      }

   }

   public Collection getPlayers() {
      return this.readOnlyPlayers;
   }
}
