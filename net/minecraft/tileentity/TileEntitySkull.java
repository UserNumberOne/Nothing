package net.minecraft.tileentity;

import com.google.common.collect.Iterables;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.BlockSkull;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.StringUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySkull extends TileEntity implements ITickable {
   private int skullType;
   private int skullRotation;
   private GameProfile playerProfile;
   private int dragonAnimatedTicks;
   private boolean dragonAnimated;
   private static PlayerProfileCache profileCache;
   private static MinecraftSessionService sessionService;

   public static void setProfileCache(PlayerProfileCache var0) {
      profileCache = var0;
   }

   public static void setSessionService(MinecraftSessionService var0) {
      sessionService = var0;
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);
      var1.setByte("SkullType", (byte)(this.skullType & 255));
      var1.setByte("Rot", (byte)(this.skullRotation & 255));
      if (this.playerProfile != null) {
         NBTTagCompound var2 = new NBTTagCompound();
         NBTUtil.writeGameProfile(var2, this.playerProfile);
         var1.setTag("Owner", var2);
      }

      return var1;
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      this.skullType = var1.getByte("SkullType");
      this.skullRotation = var1.getByte("Rot");
      if (this.skullType == 3) {
         if (var1.hasKey("Owner", 10)) {
            this.playerProfile = NBTUtil.readGameProfileFromNBT(var1.getCompoundTag("Owner"));
         } else if (var1.hasKey("ExtraType", 8)) {
            String var2 = var1.getString("ExtraType");
            if (!StringUtils.isNullOrEmpty(var2)) {
               this.playerProfile = new GameProfile((UUID)null, var2);
               this.updatePlayerProfile();
            }
         }
      }

   }

   public void update() {
      if (this.skullType == 5) {
         if (this.world.isBlockPowered(this.pos)) {
            this.dragonAnimated = true;
            ++this.dragonAnimatedTicks;
         } else {
            this.dragonAnimated = false;
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public float getAnimationProgress(float var1) {
      return this.dragonAnimated ? (float)this.dragonAnimatedTicks + var1 : (float)this.dragonAnimatedTicks;
   }

   @Nullable
   public GameProfile getPlayerProfile() {
      return this.playerProfile;
   }

   @Nullable
   public SPacketUpdateTileEntity getUpdatePacket() {
      return new SPacketUpdateTileEntity(this.pos, 4, this.getUpdateTag());
   }

   public NBTTagCompound getUpdateTag() {
      return this.writeToNBT(new NBTTagCompound());
   }

   public void setType(int var1) {
      this.skullType = var1;
      this.playerProfile = null;
   }

   public void setPlayerProfile(@Nullable GameProfile var1) {
      this.skullType = 3;
      this.playerProfile = var1;
      this.updatePlayerProfile();
   }

   private void updatePlayerProfile() {
      this.playerProfile = updateGameprofile(this.playerProfile);
      this.markDirty();
   }

   public static GameProfile updateGameprofile(GameProfile var0) {
      if (var0 != null && !StringUtils.isNullOrEmpty(var0.getName())) {
         if (var0.isComplete() && var0.getProperties().containsKey("textures")) {
            return var0;
         } else if (profileCache != null && sessionService != null) {
            GameProfile var1 = profileCache.getGameProfileForUsername(var0.getName());
            if (var1 == null) {
               return var0;
            } else {
               Property var2 = (Property)Iterables.getFirst(var1.getProperties().get("textures"), (Object)null);
               if (var2 == null) {
                  var1 = sessionService.fillProfileProperties(var1, true);
               }

               return var1;
            }
         } else {
            return var0;
         }
      } else {
         return var0;
      }
   }

   public int getSkullType() {
      return this.skullType;
   }

   @SideOnly(Side.CLIENT)
   public int getSkullRotation() {
      return this.skullRotation;
   }

   public void setSkullRotation(int var1) {
      this.skullRotation = var1;
   }

   public void mirror(Mirror var1) {
      if (this.world != null && this.world.getBlockState(this.getPos()).getValue(BlockSkull.FACING) == EnumFacing.UP) {
         this.skullRotation = var1.mirrorRotation(this.skullRotation, 16);
      }

   }

   public void rotate(Rotation var1) {
      if (this.world != null && this.world.getBlockState(this.getPos()).getValue(BlockSkull.FACING) == EnumFacing.UP) {
         this.skullRotation = var1.rotate(this.skullRotation, 16);
      }

   }
}
