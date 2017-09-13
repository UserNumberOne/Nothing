package net.minecraft.entity.item;

import net.minecraft.block.BlockRailBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

public class EntityMinecartTNT extends EntityMinecart {
   private int minecartTNTFuse = -1;

   public EntityMinecartTNT(World var1) {
      super(var1);
   }

   public EntityMinecartTNT(World var1, double var2, double var4, double var6) {
      super(var1, var2, var4, var6);
   }

   public static void registerFixesMinecartTNT(DataFixer var0) {
      EntityMinecart.registerFixesMinecart(var0, "MinecartTNT");
   }

   public EntityMinecart.Type getType() {
      return EntityMinecart.Type.TNT;
   }

   public IBlockState getDefaultDisplayTile() {
      return Blocks.TNT.getDefaultState();
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.minecartTNTFuse > 0) {
         --this.minecartTNTFuse;
         this.world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D);
      } else if (this.minecartTNTFuse == 0) {
         this.explodeCart(this.motionX * this.motionX + this.motionZ * this.motionZ);
      }

      if (this.isCollidedHorizontally) {
         double var1 = this.motionX * this.motionX + this.motionZ * this.motionZ;
         if (var1 >= 0.009999999776482582D) {
            this.explodeCart(var1);
         }
      }

   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      Entity var3 = var1.getSourceOfDamage();
      if (var3 instanceof EntityArrow) {
         EntityArrow var4 = (EntityArrow)var3;
         if (var4.isBurning()) {
            this.explodeCart(var4.motionX * var4.motionX + var4.motionY * var4.motionY + var4.motionZ * var4.motionZ);
         }
      }

      return super.attackEntityFrom(var1, var2);
   }

   public void killMinecart(DamageSource var1) {
      super.killMinecart(var1);
      double var2 = this.motionX * this.motionX + this.motionZ * this.motionZ;
      if (!var1.isExplosion() && this.world.getGameRules().getBoolean("doEntityDrops")) {
         this.entityDropItem(new ItemStack(Blocks.TNT, 1), 0.0F);
      }

      if (var1.isFireDamage() || var1.isExplosion() || var2 >= 0.009999999776482582D) {
         this.explodeCart(var2);
      }

   }

   protected void explodeCart(double var1) {
      if (!this.world.isRemote) {
         double var3 = Math.sqrt(var1);
         if (var3 > 5.0D) {
            var3 = 5.0D;
         }

         this.world.createExplosion(this, this.posX, this.posY, this.posZ, (float)(4.0D + this.rand.nextDouble() * 1.5D * var3), true);
         this.setDead();
      }

   }

   public void fall(float var1, float var2) {
      if (var1 >= 3.0F) {
         float var3 = var1 / 10.0F;
         this.explodeCart((double)(var3 * var3));
      }

      super.fall(var1, var2);
   }

   public void onActivatorRailPass(int var1, int var2, int var3, boolean var4) {
      if (var4 && this.minecartTNTFuse < 0) {
         this.ignite();
      }

   }

   public void ignite() {
      this.minecartTNTFuse = 80;
      if (!this.world.isRemote) {
         this.world.setEntityState(this, (byte)10);
         if (!this.isSilent()) {
            this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
         }
      }

   }

   public boolean isIgnited() {
      return this.minecartTNTFuse > -1;
   }

   public float getExplosionResistance(Explosion var1, World var2, BlockPos var3, IBlockState var4) {
      return !this.isIgnited() || !BlockRailBase.isRailBlock(var4) && !BlockRailBase.isRailBlock(var2, var3.up()) ? super.getExplosionResistance(var1, var2, var3, var4) : 0.0F;
   }

   public boolean verifyExplosion(Explosion var1, World var2, BlockPos var3, IBlockState var4, float var5) {
      return !this.isIgnited() || !BlockRailBase.isRailBlock(var4) && !BlockRailBase.isRailBlock(var2, var3.up()) ? super.verifyExplosion(var1, var2, var3, var4, var5) : false;
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      if (var1.hasKey("TNTFuse", 99)) {
         this.minecartTNTFuse = var1.getInteger("TNTFuse");
      }

   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setInteger("TNTFuse", this.minecartTNTFuse);
   }
}
