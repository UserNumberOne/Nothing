package net.minecraft.entity.item;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class EntityFallingBlock extends Entity {
   private IBlockState fallTile;
   public int fallTime;
   public boolean shouldDropItem = true;
   private boolean canSetAsBlock;
   public boolean hurtEntities;
   private int fallHurtMax = 40;
   private float fallHurtAmount = 2.0F;
   public NBTTagCompound tileEntityData;
   protected static final DataParameter ORIGIN = EntityDataManager.createKey(EntityFallingBlock.class, DataSerializers.BLOCK_POS);

   public EntityFallingBlock(World world) {
      super(world);
   }

   public EntityFallingBlock(World world, double d0, double d1, double d2, IBlockState iblockdata) {
      super(world);
      this.fallTile = iblockdata;
      this.preventEntitySpawning = true;
      this.setSize(0.98F, 0.98F);
      this.setPosition(d0, d1 + (double)((1.0F - this.height) / 2.0F), d2);
      this.motionX = 0.0D;
      this.motionY = 0.0D;
      this.motionZ = 0.0D;
      this.prevPosX = d0;
      this.prevPosY = d1;
      this.prevPosZ = d2;
      this.setOrigin(new BlockPos(this));
   }

   public void setOrigin(BlockPos blockposition) {
      this.dataManager.set(ORIGIN, blockposition);
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   protected void entityInit() {
      this.dataManager.register(ORIGIN, BlockPos.ORIGIN);
   }

   public boolean canBeCollidedWith() {
      return !this.isDead;
   }

   public void onUpdate() {
      Block block = this.fallTile.getBlock();
      if (this.fallTile.getMaterial() == Material.AIR) {
         this.setDead();
      } else {
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         if (this.fallTime++ == 0) {
            BlockPos blockposition = new BlockPos(this);
            if (this.world.getBlockState(blockposition).getBlock() == block && !CraftEventFactory.callEntityChangeBlockEvent(this, blockposition, Blocks.AIR, 0).isCancelled()) {
               this.world.setBlockToAir(blockposition);
            } else if (!this.world.isRemote) {
               this.setDead();
               return;
            }
         }

         if (!this.hasNoGravity()) {
            this.motionY -= 0.03999999910593033D;
         }

         this.move(this.motionX, this.motionY, this.motionZ);
         this.motionX *= 0.9800000190734863D;
         this.motionY *= 0.9800000190734863D;
         this.motionZ *= 0.9800000190734863D;
         if (!this.world.isRemote) {
            BlockPos blockposition = new BlockPos(this);
            if (this.onGround) {
               IBlockState iblockdata = this.world.getBlockState(blockposition);
               if (BlockFalling.canFallThrough(this.world.getBlockState(new BlockPos(this.posX, this.posY - 0.009999999776482582D, this.posZ)))) {
                  this.onGround = false;
               }

               this.motionX *= 0.699999988079071D;
               this.motionZ *= 0.699999988079071D;
               this.motionY *= -0.5D;
               if (iblockdata.getBlock() != Blocks.PISTON_EXTENSION) {
                  this.setDead();
                  if (!this.canSetAsBlock) {
                     if (this.world.canBlockBePlaced(block, blockposition, true, EnumFacing.UP, (Entity)null, (ItemStack)null) && !BlockFalling.canFallThrough(this.world.getBlockState(blockposition.down()))) {
                        if (CraftEventFactory.callEntityChangeBlockEvent(this, blockposition, this.fallTile.getBlock(), this.fallTile.getBlock().getMetaFromState(this.fallTile)).isCancelled()) {
                           return;
                        }

                        this.world.setBlockState(blockposition, this.fallTile, 3);
                        if (block instanceof BlockFalling) {
                           ((BlockFalling)block).onEndFalling(this.world, blockposition);
                        }

                        if (this.tileEntityData != null && block instanceof ITileEntityProvider) {
                           TileEntity tileentity = this.world.getTileEntity(blockposition);
                           if (tileentity != null) {
                              NBTTagCompound nbttagcompound = tileentity.writeToNBT(new NBTTagCompound());

                              for(String s : this.tileEntityData.getKeySet()) {
                                 NBTBase nbtbase = this.tileEntityData.getTag(s);
                                 if (!"x".equals(s) && !"y".equals(s) && !"z".equals(s)) {
                                    nbttagcompound.setTag(s, nbtbase.copy());
                                 }
                              }

                              tileentity.readFromNBT(nbttagcompound);
                              tileentity.markDirty();
                           }
                        }
                     } else if (this.shouldDropItem && this.world.getGameRules().getBoolean("doEntityDrops")) {
                        this.entityDropItem(new ItemStack(block, 1, block.damageDropped(this.fallTile)), 0.0F);
                     }
                  }
               }
            } else if (this.fallTime > 100 && !this.world.isRemote && (blockposition.getY() < 1 || blockposition.getY() > 256) || this.fallTime > 600) {
               if (this.shouldDropItem && this.world.getGameRules().getBoolean("doEntityDrops")) {
                  this.entityDropItem(new ItemStack(block, 1, block.damageDropped(this.fallTile)), 0.0F);
               }

               this.setDead();
            }
         }
      }

   }

   public void fall(float f, float f1) {
      Block block = this.fallTile.getBlock();
      if (this.hurtEntities) {
         int i = MathHelper.ceil(f - 1.0F);
         if (i > 0) {
            ArrayList arraylist = Lists.newArrayList(this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox()));
            boolean flag = block == Blocks.ANVIL;
            DamageSource damagesource = flag ? DamageSource.anvil : DamageSource.fallingBlock;

            for(Entity entity : arraylist) {
               CraftEventFactory.entityDamage = this;
               entity.attackEntityFrom(damagesource, (float)Math.min(MathHelper.floor((float)i * this.fallHurtAmount), this.fallHurtMax));
               CraftEventFactory.entityDamage = null;
            }

            if (flag && (double)this.rand.nextFloat() < 0.05000000074505806D + (double)i * 0.05D) {
               int j = ((Integer)this.fallTile.getValue(BlockAnvil.DAMAGE)).intValue();
               ++j;
               if (j > 2) {
                  this.canSetAsBlock = true;
               } else {
                  this.fallTile = this.fallTile.withProperty(BlockAnvil.DAMAGE, Integer.valueOf(j));
               }
            }
         }
      }

   }

   public static void registerFixesFallingBlock(DataFixer dataconvertermanager) {
   }

   protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      Block block = this.fallTile != null ? this.fallTile.getBlock() : Blocks.AIR;
      ResourceLocation minecraftkey = (ResourceLocation)Block.REGISTRY.getNameForObject(block);
      nbttagcompound.setString("Block", minecraftkey == null ? "" : minecraftkey.toString());
      nbttagcompound.setByte("Data", (byte)block.getMetaFromState(this.fallTile));
      nbttagcompound.setInteger("Time", this.fallTime);
      nbttagcompound.setBoolean("DropItem", this.shouldDropItem);
      nbttagcompound.setBoolean("HurtEntities", this.hurtEntities);
      nbttagcompound.setFloat("FallHurtAmount", this.fallHurtAmount);
      nbttagcompound.setInteger("FallHurtMax", this.fallHurtMax);
      if (this.tileEntityData != null) {
         nbttagcompound.setTag("TileEntityData", this.tileEntityData);
      }

   }

   protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      int i = nbttagcompound.getByte("Data") & 255;
      if (nbttagcompound.hasKey("Block", 8)) {
         this.fallTile = Block.getBlockFromName(nbttagcompound.getString("Block")).getStateFromMeta(i);
      } else if (nbttagcompound.hasKey("TileID", 99)) {
         this.fallTile = Block.getBlockById(nbttagcompound.getInteger("TileID")).getStateFromMeta(i);
      } else {
         this.fallTile = Block.getBlockById(nbttagcompound.getByte("Tile") & 255).getStateFromMeta(i);
      }

      this.fallTime = nbttagcompound.getInteger("Time");
      Block block = this.fallTile.getBlock();
      if (nbttagcompound.hasKey("HurtEntities", 99)) {
         this.hurtEntities = nbttagcompound.getBoolean("HurtEntities");
         this.fallHurtAmount = nbttagcompound.getFloat("FallHurtAmount");
         this.fallHurtMax = nbttagcompound.getInteger("FallHurtMax");
      } else if (block == Blocks.ANVIL) {
         this.hurtEntities = true;
      }

      if (nbttagcompound.hasKey("DropItem", 99)) {
         this.shouldDropItem = nbttagcompound.getBoolean("DropItem");
      }

      if (nbttagcompound.hasKey("TileEntityData", 10)) {
         this.tileEntityData = nbttagcompound.getCompoundTag("TileEntityData");
      }

      if (block == null || block.getDefaultState().getMaterial() == Material.AIR) {
         this.fallTile = Blocks.SAND.getDefaultState();
      }

   }

   public void setHurtEntities(boolean flag) {
      this.hurtEntities = flag;
   }

   public void addEntityCrashInfo(CrashReportCategory crashreportsystemdetails) {
      super.addEntityCrashInfo(crashreportsystemdetails);
      if (this.fallTile != null) {
         Block block = this.fallTile.getBlock();
         crashreportsystemdetails.addCrashSection("Immitating block ID", Integer.valueOf(Block.getIdFromBlock(block)));
         crashreportsystemdetails.addCrashSection("Immitating block data", Integer.valueOf(block.getMetaFromState(this.fallTile)));
      }

   }

   @Nullable
   public IBlockState getBlock() {
      return this.fallTile;
   }

   public boolean ignoreItemEntityData() {
      return true;
   }
}
