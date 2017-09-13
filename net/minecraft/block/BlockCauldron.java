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
      addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_LEGS);
      addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_WALL_WEST);
      addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_WALL_NORTH);
      addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_WALL_EAST);
      addCollisionBoxToList(pos, entityBox, collidingBoxes, AABB_WALL_SOUTH);
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
      int i = ((Integer)state.getValue(LEVEL)).intValue();
      float f = (float)pos.getY() + (6.0F + (float)(3 * i)) / 16.0F;
      if (!worldIn.isRemote && entityIn.isBurning() && i > 0 && entityIn.getEntityBoundingBox().minY <= (double)f) {
         entityIn.extinguish();
         this.setWaterLevel(worldIn, pos, state, i - 1);
      }

   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (heldItem == null) {
         return true;
      } else {
         int i = ((Integer)state.getValue(LEVEL)).intValue();
         Item item = heldItem.getItem();
         if (item == Items.WATER_BUCKET) {
            if (i < 3 && !worldIn.isRemote) {
               if (!playerIn.capabilities.isCreativeMode) {
                  playerIn.setHeldItem(hand, new ItemStack(Items.BUCKET));
               }

               playerIn.addStat(StatList.CAULDRON_FILLED);
               this.setWaterLevel(worldIn, pos, state, 3);
            }

            return true;
         } else if (item == Items.BUCKET) {
            if (i == 3 && !worldIn.isRemote) {
               if (!playerIn.capabilities.isCreativeMode) {
                  --heldItem.stackSize;
                  if (heldItem.stackSize == 0) {
                     playerIn.setHeldItem(hand, new ItemStack(Items.WATER_BUCKET));
                  } else if (!playerIn.inventory.addItemStackToInventory(new ItemStack(Items.WATER_BUCKET))) {
                     playerIn.dropItem(new ItemStack(Items.WATER_BUCKET), false);
                  }
               }

               playerIn.addStat(StatList.CAULDRON_USED);
               this.setWaterLevel(worldIn, pos, state, 0);
            }

            return true;
         } else if (item == Items.GLASS_BOTTLE) {
            if (i > 0 && !worldIn.isRemote) {
               if (!playerIn.capabilities.isCreativeMode) {
                  ItemStack itemstack1 = PotionUtils.addPotionToItemStack(new ItemStack(Items.POTIONITEM), PotionTypes.WATER);
                  playerIn.addStat(StatList.CAULDRON_USED);
                  if (--heldItem.stackSize == 0) {
                     playerIn.setHeldItem(hand, itemstack1);
                  } else if (!playerIn.inventory.addItemStackToInventory(itemstack1)) {
                     playerIn.dropItem(itemstack1, false);
                  } else if (playerIn instanceof EntityPlayerMP) {
                     ((EntityPlayerMP)playerIn).sendContainerToPlayer(playerIn.inventoryContainer);
                  }
               }

               this.setWaterLevel(worldIn, pos, state, i - 1);
            }

            return true;
         } else {
            if (i > 0 && item instanceof ItemArmor) {
               ItemArmor itemarmor = (ItemArmor)item;
               if (itemarmor.getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER && itemarmor.hasColor(heldItem) && !worldIn.isRemote) {
                  itemarmor.removeColor(heldItem);
                  this.setWaterLevel(worldIn, pos, state, i - 1);
                  playerIn.addStat(StatList.ARMOR_CLEANED);
                  return true;
               }
            }

            if (i > 0 && item instanceof ItemBanner) {
               if (TileEntityBanner.getPatterns(heldItem) > 0 && !worldIn.isRemote) {
                  ItemStack itemstack = heldItem.copy();
                  itemstack.stackSize = 1;
                  TileEntityBanner.removeBannerData(itemstack);
                  playerIn.addStat(StatList.BANNER_CLEANED);
                  if (!playerIn.capabilities.isCreativeMode) {
                     --heldItem.stackSize;
                  }

                  if (heldItem.stackSize == 0) {
                     playerIn.setHeldItem(hand, itemstack);
                  } else if (!playerIn.inventory.addItemStackToInventory(itemstack)) {
                     playerIn.dropItem(itemstack, false);
                  } else if (playerIn instanceof EntityPlayerMP) {
                     ((EntityPlayerMP)playerIn).sendContainerToPlayer(playerIn.inventoryContainer);
                  }

                  if (!playerIn.capabilities.isCreativeMode) {
                     this.setWaterLevel(worldIn, pos, state, i - 1);
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
      worldIn.setBlockState(pos, state.withProperty(LEVEL, Integer.valueOf(MathHelper.clamp(level, 0, 3))), 2);
      worldIn.updateComparatorOutputLevel(pos, this);
   }

   public void fillWithRain(World var1, BlockPos var2) {
      if (worldIn.rand.nextInt(20) == 1) {
         float f = worldIn.getBiome(pos).getFloatTemperature(pos);
         if (worldIn.getBiomeProvider().getTemperatureAtHeight(f, pos.getY()) >= 0.15F) {
            IBlockState iblockstate = worldIn.getBlockState(pos);
            if (((Integer)iblockstate.getValue(LEVEL)).intValue() < 3) {
               worldIn.setBlockState(pos, iblockstate.cycleProperty(LEVEL), 2);
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
      return ((Integer)blockState.getValue(LEVEL)).intValue();
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(LEVEL, Integer.valueOf(meta));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)state.getValue(LEVEL)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{LEVEL});
   }

   public boolean isPassable(IBlockAccess var1, BlockPos var2) {
      return true;
   }
}
