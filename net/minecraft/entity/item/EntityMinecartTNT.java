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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityMinecartTNT extends EntityMinecart {
   private int minecartTNTFuse = -1;

   public EntityMinecartTNT(World var1) {
      super(worldIn);
   }

   public EntityMinecartTNT(World var1, double var2, double var4, double var6) {
      super(worldIn, x, y, z);
   }

   public static void registerFixesMinecartTNT(DataFixer var0) {
      EntityMinecart.registerFixesMinecart(fixer, "MinecartTNT");
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
         double d0 = this.motionX * this.motionX + this.motionZ * this.motionZ;
         if (d0 >= 0.009999999776482582D) {
            this.explodeCart(d0);
         }
      }

   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      Entity entity = source.getSourceOfDamage();
      if (entity instanceof EntityArrow) {
         EntityArrow entityarrow = (EntityArrow)entity;
         if (entityarrow.isBurning()) {
            this.explodeCart(entityarrow.motionX * entityarrow.motionX + entityarrow.motionY * entityarrow.motionY + entityarrow.motionZ * entityarrow.motionZ);
         }
      }

      return super.attackEntityFrom(source, amount);
   }

   public void killMinecart(DamageSource var1) {
      super.killMinecart(source);
      double d0 = this.motionX * this.motionX + this.motionZ * this.motionZ;
      if (!source.isExplosion() && this.world.getGameRules().getBoolean("doEntityDrops")) {
         this.entityDropItem(new ItemStack(Blocks.TNT, 1), 0.0F);
      }

      if (source.isFireDamage() || source.isExplosion() || d0 >= 0.009999999776482582D) {
         this.explodeCart(d0);
      }

   }

   protected void explodeCart(double var1) {
      if (!this.world.isRemote) {
         double d0 = Math.sqrt(p_94103_1_);
         if (d0 > 5.0D) {
            d0 = 5.0D;
         }

         this.world.createExplosion(this, this.posX, this.posY, this.posZ, (float)(4.0D + this.rand.nextDouble() * 1.5D * d0), true);
         this.setDead();
      }

   }

   public void fall(float var1, float var2) {
      if (distance >= 3.0F) {
         float f = distance / 10.0F;
         this.explodeCart((double)(f * f));
      }

      super.fall(distance, damageMultiplier);
   }

   public void onActivatorRailPass(int var1, int var2, int var3, boolean var4) {
      if (receivingPower && this.minecartTNTFuse < 0) {
         this.ignite();
      }

   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      if (id == 10) {
         this.ignite();
      } else {
         super.handleStatusUpdate(id);
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

   @SideOnly(Side.CLIENT)
   public int getFuseTicks() {
      return this.minecartTNTFuse;
   }

   public boolean isIgnited() {
      return this.minecartTNTFuse > -1;
   }

   public float getExplosionResistance(Explosion var1, World var2, BlockPos var3, IBlockState var4) {
      return this.isIgnited() && (BlockRailBase.isRailBlock(blockStateIn) || BlockRailBase.isRailBlock(worldIn, pos.up())) ? 0.0F : super.getExplosionResistance(explosionIn, worldIn, pos, blockStateIn);
   }

   public boolean verifyExplosion(Explosion var1, World var2, BlockPos var3, IBlockState var4, float var5) {
      return this.isIgnited() && (BlockRailBase.isRailBlock(blockStateIn) || BlockRailBase.isRailBlock(worldIn, pos.up())) ? false : super.verifyExplosion(explosionIn, worldIn, pos, blockStateIn, p_174816_5_);
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(compound);
      if (compound.hasKey("TNTFuse", 99)) {
         this.minecartTNTFuse = compound.getInteger("TNTFuse");
      }

   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(compound);
      compound.setInteger("TNTFuse", this.minecartTNTFuse);
   }
}
