package net.minecraft.client.entity;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityOtherPlayerMP extends AbstractClientPlayer {
   private int otherPlayerMPPosRotationIncrements;
   private double otherPlayerMPX;
   private double otherPlayerMPY;
   private double otherPlayerMPZ;
   private double otherPlayerMPYaw;
   private double otherPlayerMPPitch;

   public EntityOtherPlayerMP(World var1, GameProfile var2) {
      super(var1, var2);
      this.stepHeight = 0.0F;
      this.noClip = true;
      this.renderOffsetY = 0.25F;
   }

   public boolean isInRangeToRenderDist(double var1) {
      double var3 = this.getEntityBoundingBox().getAverageEdgeLength() * 10.0D;
      if (Double.isNaN(var3)) {
         var3 = 1.0D;
      }

      var3 = var3 * 64.0D * getRenderDistanceWeight();
      return var1 < var3 * var3;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      return true;
   }

   public void setPositionAndRotationDirect(double var1, double var3, double var5, float var7, float var8, int var9, boolean var10) {
      this.otherPlayerMPX = var1;
      this.otherPlayerMPY = var3;
      this.otherPlayerMPZ = var5;
      this.otherPlayerMPYaw = (double)var7;
      this.otherPlayerMPPitch = (double)var8;
      this.otherPlayerMPPosRotationIncrements = var9;
   }

   public void onUpdate() {
      this.renderOffsetY = 0.0F;
      super.onUpdate();
      this.prevLimbSwingAmount = this.limbSwingAmount;
      double var1 = this.posX - this.prevPosX;
      double var3 = this.posZ - this.prevPosZ;
      float var5 = MathHelper.sqrt(var1 * var1 + var3 * var3) * 4.0F;
      if (var5 > 1.0F) {
         var5 = 1.0F;
      }

      this.limbSwingAmount += (var5 - this.limbSwingAmount) * 0.4F;
      this.limbSwing += this.limbSwingAmount;
   }

   public void onLivingUpdate() {
      if (this.otherPlayerMPPosRotationIncrements > 0) {
         double var1 = this.posX + (this.otherPlayerMPX - this.posX) / (double)this.otherPlayerMPPosRotationIncrements;
         double var3 = this.posY + (this.otherPlayerMPY - this.posY) / (double)this.otherPlayerMPPosRotationIncrements;
         double var5 = this.posZ + (this.otherPlayerMPZ - this.posZ) / (double)this.otherPlayerMPPosRotationIncrements;

         double var7;
         for(var7 = this.otherPlayerMPYaw - (double)this.rotationYaw; var7 < -180.0D; var7 += 360.0D) {
            ;
         }

         while(var7 >= 180.0D) {
            var7 -= 360.0D;
         }

         this.rotationYaw = (float)((double)this.rotationYaw + var7 / (double)this.otherPlayerMPPosRotationIncrements);
         this.rotationPitch = (float)((double)this.rotationPitch + (this.otherPlayerMPPitch - (double)this.rotationPitch) / (double)this.otherPlayerMPPosRotationIncrements);
         --this.otherPlayerMPPosRotationIncrements;
         this.setPosition(var1, var3, var5);
         this.setRotation(this.rotationYaw, this.rotationPitch);
      }

      this.prevCameraYaw = this.cameraYaw;
      this.updateArmSwingProgress();
      float var9 = MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      float var2 = (float)Math.atan(-this.motionY * 0.20000000298023224D) * 15.0F;
      if (var9 > 0.1F) {
         var9 = 0.1F;
      }

      if (!this.onGround || this.getHealth() <= 0.0F) {
         var9 = 0.0F;
      }

      if (this.onGround || this.getHealth() <= 0.0F) {
         var2 = 0.0F;
      }

      this.cameraYaw += (var9 - this.cameraYaw) * 0.4F;
      this.cameraPitch += (var2 - this.cameraPitch) * 0.8F;
      this.world.theProfiler.startSection("push");
      this.collideWithNearbyEntities();
      this.world.theProfiler.endSection();
   }

   public void sendMessage(ITextComponent var1) {
      Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(var1);
   }

   public boolean canUseCommand(int var1, String var2) {
      return false;
   }

   public BlockPos getPosition() {
      return new BlockPos(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D);
   }
}
