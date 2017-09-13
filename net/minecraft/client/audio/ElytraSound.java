package net.minecraft.client.audio;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ElytraSound extends MovingSound {
   private final EntityPlayerSP player;
   private int time;

   public ElytraSound(EntityPlayerSP var1) {
      super(SoundEvents.ITEM_ELYTRA_FLYING, SoundCategory.PLAYERS);
      this.player = var1;
      this.repeat = true;
      this.repeatDelay = 0;
      this.volume = 0.1F;
   }

   public void update() {
      ++this.time;
      if (!this.player.isDead && (this.time <= 20 || this.player.isElytraFlying())) {
         this.xPosF = (float)this.player.posX;
         this.yPosF = (float)this.player.posY;
         this.zPosF = (float)this.player.posZ;
         float var1 = MathHelper.sqrt(this.player.motionX * this.player.motionX + this.player.motionZ * this.player.motionZ + this.player.motionY * this.player.motionY);
         float var2 = var1 / 2.0F;
         if ((double)var1 >= 0.01D) {
            this.volume = MathHelper.clamp(var2 * var2, 0.0F, 1.0F);
         } else {
            this.volume = 0.0F;
         }

         if (this.time < 20) {
            this.volume = 0.0F;
         } else if (this.time < 40) {
            this.volume = (float)((double)this.volume * ((double)(this.time - 20) / 20.0D));
         }

         float var3 = 0.8F;
         if (this.volume > 0.8F) {
            this.pitch = 1.0F + (this.volume - 0.8F);
         } else {
            this.pitch = 1.0F;
         }
      } else {
         this.donePlaying = true;
      }

   }
}
