package net.minecraft.world;

import com.google.common.base.Objects;
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
   private boolean visible;

   public BossInfoServer(ITextComponent var1, BossInfo.Color var2, BossInfo.Overlay var3) {
      super(MathHelper.getRandomUUID(), nameIn, colorIn, overlayIn);
      this.readOnlyPlayers = Collections.unmodifiableSet(this.players);
      this.visible = true;
   }

   public void setPercent(float var1) {
      if (percentIn != this.percent) {
         super.setPercent(percentIn);
         this.sendUpdate(SPacketUpdateBossInfo.Operation.UPDATE_PCT);
      }

   }

   public void setColor(BossInfo.Color var1) {
      if (colorIn != this.color) {
         super.setColor(colorIn);
         this.sendUpdate(SPacketUpdateBossInfo.Operation.UPDATE_STYLE);
      }

   }

   public void setOverlay(BossInfo.Overlay var1) {
      if (overlayIn != this.overlay) {
         super.setOverlay(overlayIn);
         this.sendUpdate(SPacketUpdateBossInfo.Operation.UPDATE_STYLE);
      }

   }

   public BossInfo setDarkenSky(boolean var1) {
      if (darkenSkyIn != this.darkenSky) {
         super.setDarkenSky(darkenSkyIn);
         this.sendUpdate(SPacketUpdateBossInfo.Operation.UPDATE_PROPERTIES);
      }

      return this;
   }

   public BossInfo setPlayEndBossMusic(boolean var1) {
      if (playEndBossMusicIn != this.playEndBossMusic) {
         super.setPlayEndBossMusic(playEndBossMusicIn);
         this.sendUpdate(SPacketUpdateBossInfo.Operation.UPDATE_PROPERTIES);
      }

      return this;
   }

   public BossInfo setCreateFog(boolean var1) {
      if (createFogIn != this.createFog) {
         super.setCreateFog(createFogIn);
         this.sendUpdate(SPacketUpdateBossInfo.Operation.UPDATE_PROPERTIES);
      }

      return this;
   }

   public void setName(ITextComponent var1) {
      if (!Objects.equal(nameIn, this.name)) {
         super.setName(nameIn);
         this.sendUpdate(SPacketUpdateBossInfo.Operation.UPDATE_NAME);
      }

   }

   private void sendUpdate(SPacketUpdateBossInfo.Operation var1) {
      if (this.visible) {
         SPacketUpdateBossInfo spacketupdatebossinfo = new SPacketUpdateBossInfo(operationIn, this);

         for(EntityPlayerMP entityplayermp : this.players) {
            entityplayermp.connection.sendPacket(spacketupdatebossinfo);
         }
      }

   }

   public void addPlayer(EntityPlayerMP var1) {
      if (this.players.add(player) && this.visible) {
         player.connection.sendPacket(new SPacketUpdateBossInfo(SPacketUpdateBossInfo.Operation.ADD, this));
      }

   }

   public void removePlayer(EntityPlayerMP var1) {
      if (this.players.remove(player) && this.visible) {
         player.connection.sendPacket(new SPacketUpdateBossInfo(SPacketUpdateBossInfo.Operation.REMOVE, this));
      }

   }

   public void setVisible(boolean var1) {
      if (visibleIn != this.visible) {
         this.visible = visibleIn;

         for(EntityPlayerMP entityplayermp : this.players) {
            entityplayermp.connection.sendPacket(new SPacketUpdateBossInfo(visibleIn ? SPacketUpdateBossInfo.Operation.ADD : SPacketUpdateBossInfo.Operation.REMOVE, this));
         }
      }

   }

   public Collection getPlayers() {
      return this.readOnlyPlayers;
   }
}
