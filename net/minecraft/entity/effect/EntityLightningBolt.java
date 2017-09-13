package net.minecraft.entity.effect;

import java.util.List;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class EntityLightningBolt extends EntityWeatherEffect {
   private int lightningState;
   public long boltVertex;
   private int boltLivingTime;
   private final boolean effectOnly;
   public boolean isEffect;

   public EntityLightningBolt(World var1, double var2, double var4, double var6, boolean var8) {
      super(var1);
      this.isEffect = var8;
      this.setLocationAndAngles(var2, var4, var6, 0.0F, 0.0F);
      this.lightningState = 2;
      this.boltVertex = this.rand.nextLong();
      this.boltLivingTime = this.rand.nextInt(3) + 1;
      this.effectOnly = var8;
      BlockPos var9 = new BlockPos(this);
      if (!var8 && !var1.isRemote && var1.getGameRules().getBoolean("doFireTick") && (var1.getDifficulty() == EnumDifficulty.NORMAL || var1.getDifficulty() == EnumDifficulty.HARD) && var1.isAreaLoaded(var9, 10)) {
         if (var1.getBlockState(var9).getMaterial() == Material.AIR && Blocks.FIRE.canPlaceBlockAt(var1, var9) && !CraftEventFactory.callBlockIgniteEvent(var1, var9.getX(), var9.getY(), var9.getZ(), this).isCancelled()) {
            var1.setBlockState(var9, Blocks.FIRE.getDefaultState());
         }

         for(int var10 = 0; var10 < 4; ++var10) {
            BlockPos var11 = var9.add(this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1, this.rand.nextInt(3) - 1);
            if (var1.getBlockState(var11).getMaterial() == Material.AIR && Blocks.FIRE.canPlaceBlockAt(var1, var11) && !CraftEventFactory.callBlockIgniteEvent(var1, var11.getX(), var11.getY(), var11.getZ(), this).isCancelled()) {
               var1.setBlockState(var11, Blocks.FIRE.getDefaultState());
            }
         }
      }

   }

   public SoundCategory getSoundCategory() {
      return SoundCategory.WEATHER;
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.lightningState == 2) {
         float var1 = 0.8F + this.rand.nextFloat() * 0.2F;
         int var2 = ((WorldServer)this.world).getServer().getViewDistance() * 16;

         for(EntityPlayerMP var4 : this.world.playerEntities) {
            double var5 = this.posX - var4.posX;
            double var7 = this.posZ - var4.posZ;
            double var9 = var5 * var5 + var7 * var7;
            if (var9 > (double)(var2 * var2)) {
               double var11 = Math.sqrt(var9);
               double var13 = var4.posX + var5 / var11 * (double)var2;
               double var15 = var4.posZ + var7 / var11 * (double)var2;
               var4.connection.sendPacket(new SPacketSoundEffect(SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.WEATHER, var13, this.posY, var15, 10000.0F, var1));
            } else {
               var4.connection.sendPacket(new SPacketSoundEffect(SoundEvents.ENTITY_LIGHTNING_THUNDER, SoundCategory.WEATHER, this.posX, this.posY, this.posZ, 10000.0F, var1));
            }
         }

         this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_LIGHTNING_IMPACT, SoundCategory.WEATHER, 2.0F, 0.5F + this.rand.nextFloat() * 0.2F);
      }

      --this.lightningState;
      if (this.lightningState < 0) {
         if (this.boltLivingTime == 0) {
            this.setDead();
         } else if (this.lightningState < -this.rand.nextInt(10)) {
            --this.boltLivingTime;
            this.lightningState = 1;
            if (!this.effectOnly && !this.world.isRemote) {
               this.boltVertex = this.rand.nextLong();
               BlockPos var17 = new BlockPos(this);
               if (this.world.getGameRules().getBoolean("doFireTick") && this.world.isAreaLoaded(var17, 10) && this.world.getBlockState(var17).getMaterial() == Material.AIR && Blocks.FIRE.canPlaceBlockAt(this.world, var17) && !this.isEffect && !CraftEventFactory.callBlockIgniteEvent(this.world, var17.getX(), var17.getY(), var17.getZ(), this).isCancelled()) {
                  this.world.setBlockState(var17, Blocks.FIRE.getDefaultState());
               }
            }
         }
      }

      if (this.lightningState >= 0 && !this.isEffect) {
         if (this.world.isRemote) {
            this.world.setLastLightningBolt(2);
         } else if (!this.effectOnly) {
            List var18 = this.world.getEntitiesWithinAABBExcludingEntity(this, new AxisAlignedBB(this.posX - 3.0D, this.posY - 3.0D, this.posZ - 3.0D, this.posX + 3.0D, this.posY + 6.0D + 3.0D, this.posZ + 3.0D));

            for(int var19 = 0; var19 < var18.size(); ++var19) {
               Entity var20 = (Entity)var18.get(var19);
               var20.onStruckByLightning(this);
            }
         }
      }

   }

   protected void entityInit() {
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
   }
}
