package net.minecraft.entity.item;

import javax.annotation.Nullable;
import net.minecraft.block.BlockFurnace;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityMinecartFurnace extends EntityMinecart {
   private static final DataParameter POWERED = EntityDataManager.createKey(EntityMinecartFurnace.class, DataSerializers.BOOLEAN);
   private int fuel;
   public double pushX;
   public double pushZ;

   public EntityMinecartFurnace(World var1) {
      super(var1);
   }

   public EntityMinecartFurnace(World var1, double var2, double var4, double var6) {
      super(var1, var2, var4, var6);
   }

   public static void registerFixesMinecartFurnace(DataFixer var0) {
      EntityMinecart.registerFixesMinecart(var0, "MinecartFurnace");
   }

   public EntityMinecart.Type getType() {
      return EntityMinecart.Type.FURNACE;
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(POWERED, Boolean.valueOf(false));
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.fuel > 0) {
         --this.fuel;
      }

      if (this.fuel <= 0) {
         this.pushX = 0.0D;
         this.pushZ = 0.0D;
      }

      this.setMinecartPowered(this.fuel > 0);
      if (this.isMinecartPowered() && this.rand.nextInt(4) == 0) {
         this.world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, this.posX, this.posY + 0.8D, this.posZ, 0.0D, 0.0D, 0.0D);
      }

   }

   protected double getMaximumSpeed() {
      return 0.2D;
   }

   public void killMinecart(DamageSource var1) {
      super.killMinecart(var1);
      if (!var1.isExplosion() && this.world.getGameRules().getBoolean("doEntityDrops")) {
         this.entityDropItem(new ItemStack(Blocks.FURNACE, 1), 0.0F);
      }

   }

   protected void moveAlongTrack(BlockPos var1, IBlockState var2) {
      super.moveAlongTrack(var1, var2);
      double var3 = this.pushX * this.pushX + this.pushZ * this.pushZ;
      if (var3 > 1.0E-4D && this.motionX * this.motionX + this.motionZ * this.motionZ > 0.001D) {
         var3 = (double)MathHelper.sqrt(var3);
         this.pushX /= var3;
         this.pushZ /= var3;
         if (this.pushX * this.motionX + this.pushZ * this.motionZ < 0.0D) {
            this.pushX = 0.0D;
            this.pushZ = 0.0D;
         } else {
            double var5 = var3 / this.getMaximumSpeed();
            this.pushX *= var5;
            this.pushZ *= var5;
         }
      }

   }

   protected void applyDrag() {
      double var1 = this.pushX * this.pushX + this.pushZ * this.pushZ;
      if (var1 > 1.0E-4D) {
         var1 = (double)MathHelper.sqrt(var1);
         this.pushX /= var1;
         this.pushZ /= var1;
         double var3 = 1.0D;
         this.motionX *= 0.800000011920929D;
         this.motionY *= 0.0D;
         this.motionZ *= 0.800000011920929D;
         this.motionX += this.pushX * 1.0D;
         this.motionZ += this.pushZ * 1.0D;
      } else {
         this.motionX *= 0.9800000190734863D;
         this.motionY *= 0.0D;
         this.motionZ *= 0.9800000190734863D;
      }

      super.applyDrag();
   }

   public boolean processInitialInteract(EntityPlayer var1, @Nullable ItemStack var2, EnumHand var3) {
      if (var2 != null && var2.getItem() == Items.COAL && this.fuel + 3600 <= 32000) {
         if (!var1.capabilities.isCreativeMode) {
            --var2.stackSize;
         }

         this.fuel += 3600;
      }

      this.pushX = this.posX - var1.posX;
      this.pushZ = this.posZ - var1.posZ;
      return true;
   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setDouble("PushX", this.pushX);
      var1.setDouble("PushZ", this.pushZ);
      var1.setShort("Fuel", (short)this.fuel);
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.pushX = var1.getDouble("PushX");
      this.pushZ = var1.getDouble("PushZ");
      this.fuel = var1.getShort("Fuel");
   }

   protected boolean isMinecartPowered() {
      return ((Boolean)this.dataManager.get(POWERED)).booleanValue();
   }

   protected void setMinecartPowered(boolean var1) {
      this.dataManager.set(POWERED, Boolean.valueOf(var1));
   }

   public IBlockState getDefaultDisplayTile() {
      return (this.isMinecartPowered() ? Blocks.LIT_FURNACE : Blocks.FURNACE).getDefaultState().withProperty(BlockFurnace.FACING, EnumFacing.NORTH);
   }
}
