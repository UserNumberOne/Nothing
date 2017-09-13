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

   public EntityFallingBlock(World var1) {
      super(var1);
   }

   public EntityFallingBlock(World var1, double var2, double var4, double var6, IBlockState var8) {
      super(var1);
      this.fallTile = var8;
      this.preventEntitySpawning = true;
      this.setSize(0.98F, 0.98F);
      this.setPosition(var2, var4 + (double)((1.0F - this.height) / 2.0F), var6);
      this.motionX = 0.0D;
      this.motionY = 0.0D;
      this.motionZ = 0.0D;
      this.prevPosX = var2;
      this.prevPosY = var4;
      this.prevPosZ = var6;
      this.setOrigin(new BlockPos(this));
   }

   public void setOrigin(BlockPos var1) {
      this.dataManager.set(ORIGIN, var1);
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
      Block var1 = this.fallTile.getBlock();
      if (this.fallTile.getMaterial() == Material.AIR) {
         this.setDead();
      } else {
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         if (this.fallTime++ == 0) {
            BlockPos var2 = new BlockPos(this);
            if (this.world.getBlockState(var2).getBlock() == var1 && !CraftEventFactory.callEntityChangeBlockEvent(this, var2, Blocks.AIR, 0).isCancelled()) {
               this.world.setBlockToAir(var2);
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
            BlockPos var9 = new BlockPos(this);
            if (this.onGround) {
               IBlockState var3 = this.world.getBlockState(var9);
               if (BlockFalling.canFallThrough(this.world.getBlockState(new BlockPos(this.posX, this.posY - 0.009999999776482582D, this.posZ)))) {
                  this.onGround = false;
               }

               this.motionX *= 0.699999988079071D;
               this.motionZ *= 0.699999988079071D;
               this.motionY *= -0.5D;
               if (var3.getBlock() != Blocks.PISTON_EXTENSION) {
                  this.setDead();
                  if (!this.canSetAsBlock) {
                     if (this.world.canBlockBePlaced(var1, var9, true, EnumFacing.UP, (Entity)null, (ItemStack)null) && !BlockFalling.canFallThrough(this.world.getBlockState(var9.down()))) {
                        if (CraftEventFactory.callEntityChangeBlockEvent(this, var9, this.fallTile.getBlock(), this.fallTile.getBlock().getMetaFromState(this.fallTile)).isCancelled()) {
                           return;
                        }

                        this.world.setBlockState(var9, this.fallTile, 3);
                        if (var1 instanceof BlockFalling) {
                           ((BlockFalling)var1).onEndFalling(this.world, var9);
                        }

                        if (this.tileEntityData != null && var1 instanceof ITileEntityProvider) {
                           TileEntity var4 = this.world.getTileEntity(var9);
                           if (var4 != null) {
                              NBTTagCompound var5 = var4.writeToNBT(new NBTTagCompound());

                              for(String var7 : this.tileEntityData.getKeySet()) {
                                 NBTBase var8 = this.tileEntityData.getTag(var7);
                                 if (!"x".equals(var7) && !"y".equals(var7) && !"z".equals(var7)) {
                                    var5.setTag(var7, var8.copy());
                                 }
                              }

                              var4.readFromNBT(var5);
                              var4.markDirty();
                           }
                        }
                     } else if (this.shouldDropItem && this.world.getGameRules().getBoolean("doEntityDrops")) {
                        this.entityDropItem(new ItemStack(var1, 1, var1.damageDropped(this.fallTile)), 0.0F);
                     }
                  }
               }
            } else if (this.fallTime > 100 && !this.world.isRemote && (var9.getY() < 1 || var9.getY() > 256) || this.fallTime > 600) {
               if (this.shouldDropItem && this.world.getGameRules().getBoolean("doEntityDrops")) {
                  this.entityDropItem(new ItemStack(var1, 1, var1.damageDropped(this.fallTile)), 0.0F);
               }

               this.setDead();
            }
         }
      }

   }

   public void fall(float var1, float var2) {
      Block var3 = this.fallTile.getBlock();
      if (this.hurtEntities) {
         int var4 = MathHelper.ceil(var1 - 1.0F);
         if (var4 > 0) {
            ArrayList var5 = Lists.newArrayList(this.world.getEntitiesWithinAABBExcludingEntity(this, this.getEntityBoundingBox()));
            boolean var6 = var3 == Blocks.ANVIL;
            DamageSource var7 = var6 ? DamageSource.anvil : DamageSource.fallingBlock;

            for(Entity var9 : var5) {
               CraftEventFactory.entityDamage = this;
               var9.attackEntityFrom(var7, (float)Math.min(MathHelper.floor((float)var4 * this.fallHurtAmount), this.fallHurtMax));
               CraftEventFactory.entityDamage = null;
            }

            if (var6 && (double)this.rand.nextFloat() < 0.05000000074505806D + (double)var4 * 0.05D) {
               int var10 = ((Integer)this.fallTile.getValue(BlockAnvil.DAMAGE)).intValue();
               ++var10;
               if (var10 > 2) {
                  this.canSetAsBlock = true;
               } else {
                  this.fallTile = this.fallTile.withProperty(BlockAnvil.DAMAGE, Integer.valueOf(var10));
               }
            }
         }
      }

   }

   public static void registerFixesFallingBlock(DataFixer var0) {
   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      Block var2 = this.fallTile != null ? this.fallTile.getBlock() : Blocks.AIR;
      ResourceLocation var3 = (ResourceLocation)Block.REGISTRY.getNameForObject(var2);
      var1.setString("Block", var3 == null ? "" : var3.toString());
      var1.setByte("Data", (byte)var2.getMetaFromState(this.fallTile));
      var1.setInteger("Time", this.fallTime);
      var1.setBoolean("DropItem", this.shouldDropItem);
      var1.setBoolean("HurtEntities", this.hurtEntities);
      var1.setFloat("FallHurtAmount", this.fallHurtAmount);
      var1.setInteger("FallHurtMax", this.fallHurtMax);
      if (this.tileEntityData != null) {
         var1.setTag("TileEntityData", this.tileEntityData);
      }

   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
      int var2 = var1.getByte("Data") & 255;
      if (var1.hasKey("Block", 8)) {
         this.fallTile = Block.getBlockFromName(var1.getString("Block")).getStateFromMeta(var2);
      } else if (var1.hasKey("TileID", 99)) {
         this.fallTile = Block.getBlockById(var1.getInteger("TileID")).getStateFromMeta(var2);
      } else {
         this.fallTile = Block.getBlockById(var1.getByte("Tile") & 255).getStateFromMeta(var2);
      }

      this.fallTime = var1.getInteger("Time");
      Block var3 = this.fallTile.getBlock();
      if (var1.hasKey("HurtEntities", 99)) {
         this.hurtEntities = var1.getBoolean("HurtEntities");
         this.fallHurtAmount = var1.getFloat("FallHurtAmount");
         this.fallHurtMax = var1.getInteger("FallHurtMax");
      } else if (var3 == Blocks.ANVIL) {
         this.hurtEntities = true;
      }

      if (var1.hasKey("DropItem", 99)) {
         this.shouldDropItem = var1.getBoolean("DropItem");
      }

      if (var1.hasKey("TileEntityData", 10)) {
         this.tileEntityData = var1.getCompoundTag("TileEntityData");
      }

      if (var3 == null || var3.getDefaultState().getMaterial() == Material.AIR) {
         this.fallTile = Blocks.SAND.getDefaultState();
      }

   }

   public void setHurtEntities(boolean var1) {
      this.hurtEntities = var1;
   }

   public void addEntityCrashInfo(CrashReportCategory var1) {
      super.addEntityCrashInfo(var1);
      if (this.fallTile != null) {
         Block var2 = this.fallTile.getBlock();
         var1.addCrashSection("Immitating block ID", Integer.valueOf(Block.getIdFromBlock(var2)));
         var1.addCrashSection("Immitating block data", Integer.valueOf(var2.getMetaFromState(this.fallTile)));
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
