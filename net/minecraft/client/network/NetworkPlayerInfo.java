package net.minecraft.client.network;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class NetworkPlayerInfo {
   private final GameProfile gameProfile;
   Map playerTextures = Maps.newEnumMap(Type.class);
   private GameType gameType;
   private int responseTime;
   private boolean playerTexturesLoaded;
   private String skinType;
   private ITextComponent displayName;
   private int lastHealth;
   private int displayHealth;
   private long lastHealthTime;
   private long healthBlinkTime;
   private long renderVisibilityId;

   public NetworkPlayerInfo(GameProfile var1) {
      this.gameProfile = var1;
   }

   public NetworkPlayerInfo(SPacketPlayerListItem.AddPlayerData var1) {
      this.gameProfile = var1.getProfile();
      this.gameType = var1.getGameMode();
      this.responseTime = var1.getPing();
      this.displayName = var1.getDisplayName();
   }

   public GameProfile getGameProfile() {
      return this.gameProfile;
   }

   public GameType getGameType() {
      return this.gameType;
   }

   protected void setGameType(GameType var1) {
      this.gameType = var1;
   }

   public int getResponseTime() {
      return this.responseTime;
   }

   protected void setResponseTime(int var1) {
      this.responseTime = var1;
   }

   public boolean hasLocationSkin() {
      return this.getLocationSkin() != null;
   }

   public String getSkinType() {
      return this.skinType == null ? DefaultPlayerSkin.getSkinType(this.gameProfile.getId()) : this.skinType;
   }

   public ResourceLocation getLocationSkin() {
      this.loadPlayerTextures();
      return (ResourceLocation)Objects.firstNonNull(this.playerTextures.get(Type.SKIN), DefaultPlayerSkin.getDefaultSkin(this.gameProfile.getId()));
   }

   @Nullable
   public ResourceLocation getLocationCape() {
      this.loadPlayerTextures();
      return (ResourceLocation)this.playerTextures.get(Type.CAPE);
   }

   @Nullable
   public ResourceLocation getLocationElytra() {
      this.loadPlayerTextures();
      return (ResourceLocation)this.playerTextures.get(Type.ELYTRA);
   }

   @Nullable
   public ScorePlayerTeam getPlayerTeam() {
      return Minecraft.getMinecraft().world.getScoreboard().getPlayersTeam(this.getGameProfile().getName());
   }

   protected void loadPlayerTextures() {
      synchronized(this) {
         if (!this.playerTexturesLoaded) {
            this.playerTexturesLoaded = true;
            Minecraft.getMinecraft().getSkinManager().loadProfileTextures(this.gameProfile, new SkinManager.SkinAvailableCallback() {
               public void skinAvailable(Type var1, ResourceLocation var2, MinecraftProfileTexture var3) {
                  switch(var1) {
                  case SKIN:
                     NetworkPlayerInfo.this.playerTextures.put(Type.SKIN, var2);
                     NetworkPlayerInfo.this.skinType = var3.getMetadata("model");
                     if (NetworkPlayerInfo.this.skinType == null) {
                        NetworkPlayerInfo.this.skinType = "default";
                     }
                     break;
                  case CAPE:
                     NetworkPlayerInfo.this.playerTextures.put(Type.CAPE, var2);
                     break;
                  case ELYTRA:
                     NetworkPlayerInfo.this.playerTextures.put(Type.ELYTRA, var2);
                  }

               }
            }, true);
         }

      }
   }

   public void setDisplayName(@Nullable ITextComponent var1) {
      this.displayName = var1;
   }

   @Nullable
   public ITextComponent getDisplayName() {
      return this.displayName;
   }

   public int getLastHealth() {
      return this.lastHealth;
   }

   public void setLastHealth(int var1) {
      this.lastHealth = var1;
   }

   public int getDisplayHealth() {
      return this.displayHealth;
   }

   public void setDisplayHealth(int var1) {
      this.displayHealth = var1;
   }

   public long getLastHealthTime() {
      return this.lastHealthTime;
   }

   public void setLastHealthTime(long var1) {
      this.lastHealthTime = var1;
   }

   public long getHealthBlinkTime() {
      return this.healthBlinkTime;
   }

   public void setHealthBlinkTime(long var1) {
      this.healthBlinkTime = var1;
   }

   public long getRenderVisibilityId() {
      return this.renderVisibilityId;
   }

   public void setRenderVisibilityId(long var1) {
      this.renderVisibilityId = var1;
   }
}
