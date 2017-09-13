package net.minecraft.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.PotionTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.CauldronLevelChangeEvent.ChangeReason;

public class BlockCauldron extends Block {
   public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 3);
   protected static final AxisAlignedBB AABB_LEGS = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.3125D, 1.0D);
   protected static final AxisAlignedBB AABB_WALL_NORTH = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 0.125D);
   protected static final AxisAlignedBB AABB_WALL_SOUTH = new AxisAlignedBB(0.0D, 0.0D, 0.875D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB AABB_WALL_EAST = new AxisAlignedBB(0.875D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
   protected static final AxisAlignedBB AABB_WALL_WEST = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.125D, 1.0D, 1.0D);

   public BlockCauldron() {
      super(Material.IRON, MapColor.STONE);
      this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, Integer.valueOf(0)));
   }

   public void addCollisionBoxToList(IBlockState var1, World var2, BlockPos var3, AxisAlignedBB var4, List var5, @Nullable Entity var6) {
      addCollisionBoxToList(var3, var4, var5, AABB_LEGS);
      addCollisionBoxToList(var3, var4, var5, AABB_WALL_WEST);
      addCollisionBoxToList(var3, var4, var5, AABB_WALL_NORTH);
      addCollisionBoxToList(var3, var4, var5, AABB_WALL_EAST);
      addCollisionBoxToList(var3, var4, var5, AABB_WALL_SOUTH);
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return FULL_BLOCK_AABB;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public void onEntityCollidedWithBlock(World var1, BlockPos var2, IBlockState var3, Entity var4) {
      int var5 = ((Integer)var3.getValue(LEVEL)).intValue();
      float var6 = (float)var2.getY() + (6.0F + (float)(3 * var5)) / 16.0F;
      if (!var1.isRemote && var4.isBurning() && var5 > 0 && var4.getEntityBoundingBox().minY <= (double)var6) {
         if (!this.changeLevel(var1, var2, var3, var5 - 1, var4, ChangeReason.EXTINGUISH)) {
            return;
         }

         var4.extinguish();
      }

   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (var6 == null) {
         return true;
      } else {
         int var11 = ((Integer)var3.getValue(LEVEL)).intValue();
         Item var12 = var6.getItem();
         if (var12 == Items.WATER_BUCKET) {
            if (var11 < 3 && !var1.isRemote) {
               if (!this.changeLevel(var1, var2, var3, 3, var4, ChangeReason.BUCKET_EMPTY)) {
                  return true;
               }

               if (!var4.capabilities.isCreativeMode) {
                  var4.setHeldItem(var5, new ItemStack(Items.BUCKET));
               }

               var4.addStat(StatList.CAULDRON_FILLED);
            }

            return true;
         } else if (var12 == Items.BUCKET) {
            if (var11 == 3 && !var1.isRemote) {
               if (!this.changeLevel(var1, var2, var3, 0, var4, ChangeReason.BUCKET_FILL)) {
                  return true;
               }

               if (!var4.capabilities.isCreativeMode) {
                  --var6.stackSize;
                  if (var6.stackSize == 0) {
                     var4.setHeldItem(var5, new ItemStack(Items.WATER_BUCKET));
                  } else if (!var4.inventory.addItemStackToInventory(new ItemStack(Items.WATER_BUCKET))) {
                     var4.dropItem(new ItemStack(Items.WATER_BUCKET), false);
                  }
               }

               var4.addStat(StatList.CAULDRON_USED);
            }

            return true;
         } else if (var12 == Items.GLASS_BOTTLE) {
            if (var11 > 0 && !var1.isRemote) {
               if (!this.changeLevel(var1, var2, var3, var11 - 1, var4, ChangeReason.BOTTLE_FILL)) {
                  return true;
               }

               if (!var4.capabilities.isCreativeMode) {
                  ItemStack var15 = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER);
                  var4.addStat(StatList.CAULDRON_USED);
                  if (--var6.stackSize == 0) {
                     var4.setHeldItem(var5, var15);
                  } else if (!var4.inventory.addItemStackToInventory(var15)) {
                     var4.dropItem(var15, false);
                  } else if (var4 instanceof EntityPlayerMP) {
                     ((EntityPlayerMP)var4).sendContainerToPlayer(var4.inventoryContainer);
                  }
               }
            }

            return true;
         } else {
            if (var11 > 0 && var12 instanceof ItemArmor) {
               ItemArmor var14 = (ItemArmor)var12;
               if (var14.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER && var14.hasColor(var6) && !var1.isRemote) {
                  if (!this.changeLevel(var1, var2, var3, var11 - 1, var4, ChangeReason.ARMOR_WASH)) {
                     return true;
                  }

                  var14.removeColor(var6);
                  var4.addStat(StatList.ARMOR_CLEANED);
                  return true;
               }
            }

            if (var11 > 0 && var12 instanceof ItemBanner) {
               if (TileEntityBanner.getPatterns(var6) > 0 && !var1.isRemote) {
                  ItemStack var13 = var6.copy();
                  var13.stackSize = 1;
                  TileEntityBanner.removeBannerData(var13);
                  var4.addStat(StatList.BANNER_CLEANED);
                  if (!var4.capabilities.isCreativeMode) {
                     --var6.stackSize;
                  }

                  if (var6.stackSize == 0) {
                     var4.setHeldItem(var5, var13);
                  } else if (!var4.inventory.addItemStackToInventory(var13)) {
                     var4.dropItem(var13, false);
                  } else if (var4 instanceof EntityPlayerMP) {
                     ((EntityPlayerMP)var4).sendContainerToPlayer(var4.inventoryContainer);
                  }

                  if (!var4.capabilities.isCreativeMode) {
                     this.changeLevel(var1, var2, var3, var11 - 1, var4, ChangeReason.BANNER_WASH);
                  }
               }

               return true;
            } else {
               return false;
            }
         }
      }
   }

   public void setWaterLevel(World var1, BlockPos var2, IBlockState var3, int var4) {
      this.changeLevel(var1, var2, var3, var4, (Entity)null, ChangeReason.UNKNOWN);
   }

   private boolean changeLevel(World var1, BlockPos var2, IBlockState var3, int var4, Entity var5, ChangeReason var6) {
      int var7 = MathHelper.clamp(var4, 0, 3).intValue();
      CauldronLevelChangeEvent var8 = new CauldronLevelChangeEvent(var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ()), var5 == null ? null : var5.getBukkitEntity(), var6, ((Integer)var3.getValue(LEVEL)).intValue(), var7);
      var1.getServer().getPluginManager().callEvent(var8);
      if (var8.isCancelled()) {
         return false;
      } else {
         var1.setBlockState(var2, var3.withProperty(LEVEL, Integer.valueOf(var7)), 2);
         var1.updateComparatorOutputLevel(var2, this);
         return true;
      }
   }

   public void fillWithRain(World var1, BlockPos var2) {
      if (var1.rand.nextInt(20) == 1) {
         float var3 = var1.getBiome(var2).getFloatTemperature(var2);
         if (var1.getBiomeProvider().getTemperatureAtHeight(var3, var2.getY()) >= 0.15F) {
            IBlockState var4 = var1.getBlockState(var2);
            if (((Integer)var4.getValue(LEVEL)).intValue() < 3) {
               this.setWaterLevel(var1, var2, var4.cycleProperty(LEVEL), 2);
            }
         }
      }

   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Items.CAULDRON;
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(Items.CAULDRON);
   }

   public boolean hasComparatorInputOverride(IBlockState var1) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState var1, World var2, BlockPos var3) {
      return ((Integer)var1.getValue(LEVEL)).intValue();
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(LEVEL, Integer.valueOf(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(LEVEL)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{LEVEL});
   }

   public boolean isPassable(IBlockAccess var1, BlockPos var2) {
      return true;
   }
}
