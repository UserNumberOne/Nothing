package net.minecraft.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class BlockLeaves extends Block implements IShearable {
   public static final PropertyBool DECAYABLE = PropertyBool.create("decayable");
   public static final PropertyBool CHECK_DECAY = PropertyBool.create("check_decay");
   protected boolean leavesFancy;
   int[] surroundings;

   public BlockLeaves() {
      super(Material.LEAVES);
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.DECORATIONS);
      this.setHardness(0.2F);
      this.setLightOpacity(1);
      this.setSoundType(SoundType.PLANT);
   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      boolean var4 = true;
      boolean var5 = true;
      int var6 = var2.getX();
      int var7 = var2.getY();
      int var8 = var2.getZ();
      if (var1.isAreaLoaded(new BlockPos(var6 - 2, var7 - 2, var8 - 2), new BlockPos(var6 + 2, var7 + 2, var8 + 2))) {
         for(int var9 = -1; var9 <= 1; ++var9) {
            for(int var10 = -1; var10 <= 1; ++var10) {
               for(int var11 = -1; var11 <= 1; ++var11) {
                  BlockPos var12 = var2.add(var9, var10, var11);
                  IBlockState var13 = var1.getBlockState(var12);
                  if (var13.getBlock().isLeaves(var13, var1, var12)) {
                     var13.getBlock().beginLeavesDecay(var13, var1, var12);
                  }
               }
            }
         }
      }

   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!var1.isRemote && ((Boolean)var3.getValue(CHECK_DECAY)).booleanValue() && ((Boolean)var3.getValue(DECAYABLE)).booleanValue()) {
         boolean var5 = true;
         boolean var6 = true;
         int var7 = var2.getX();
         int var8 = var2.getY();
         int var9 = var2.getZ();
         boolean var10 = true;
         boolean var11 = true;
         boolean var12 = true;
         if (this.surroundings == null) {
            this.surroundings = new int['耀'];
         }

         if (var1.isAreaLoaded(new BlockPos(var7 - 5, var8 - 5, var9 - 5), new BlockPos(var7 + 5, var8 + 5, var9 + 5))) {
            BlockPos.MutableBlockPos var13 = new BlockPos.MutableBlockPos();

            for(int var14 = -4; var14 <= 4; ++var14) {
               for(int var15 = -4; var15 <= 4; ++var15) {
                  for(int var16 = -4; var16 <= 4; ++var16) {
                     IBlockState var17 = var1.getBlockState(var13.setPos(var7 + var14, var8 + var15, var9 + var16));
                     Block var18 = var17.getBlock();
                     if (!var18.canSustainLeaves(var17, var1, var13.setPos(var7 + var14, var8 + var15, var9 + var16))) {
                        if (var18.isLeaves(var17, var1, var13.setPos(var7 + var14, var8 + var15, var9 + var16))) {
                           this.surroundings[(var14 + 16) * 1024 + (var15 + 16) * 32 + var16 + 16] = -2;
                        } else {
                           this.surroundings[(var14 + 16) * 1024 + (var15 + 16) * 32 + var16 + 16] = -1;
                        }
                     } else {
                        this.surroundings[(var14 + 16) * 1024 + (var15 + 16) * 32 + var16 + 16] = 0;
                     }
                  }
               }
            }

            for(int var20 = 1; var20 <= 4; ++var20) {
               for(int var21 = -4; var21 <= 4; ++var21) {
                  for(int var22 = -4; var22 <= 4; ++var22) {
                     for(int var23 = -4; var23 <= 4; ++var23) {
                        if (this.surroundings[(var21 + 16) * 1024 + (var22 + 16) * 32 + var23 + 16] == var20 - 1) {
                           if (this.surroundings[(var21 + 16 - 1) * 1024 + (var22 + 16) * 32 + var23 + 16] == -2) {
                              this.surroundings[(var21 + 16 - 1) * 1024 + (var22 + 16) * 32 + var23 + 16] = var20;
                           }

                           if (this.surroundings[(var21 + 16 + 1) * 1024 + (var22 + 16) * 32 + var23 + 16] == -2) {
                              this.surroundings[(var21 + 16 + 1) * 1024 + (var22 + 16) * 32 + var23 + 16] = var20;
                           }

                           if (this.surroundings[(var21 + 16) * 1024 + (var22 + 16 - 1) * 32 + var23 + 16] == -2) {
                              this.surroundings[(var21 + 16) * 1024 + (var22 + 16 - 1) * 32 + var23 + 16] = var20;
                           }

                           if (this.surroundings[(var21 + 16) * 1024 + (var22 + 16 + 1) * 32 + var23 + 16] == -2) {
                              this.surroundings[(var21 + 16) * 1024 + (var22 + 16 + 1) * 32 + var23 + 16] = var20;
                           }

                           if (this.surroundings[(var21 + 16) * 1024 + (var22 + 16) * 32 + (var23 + 16 - 1)] == -2) {
                              this.surroundings[(var21 + 16) * 1024 + (var22 + 16) * 32 + (var23 + 16 - 1)] = var20;
                           }

                           if (this.surroundings[(var21 + 16) * 1024 + (var22 + 16) * 32 + var23 + 16 + 1] == -2) {
                              this.surroundings[(var21 + 16) * 1024 + (var22 + 16) * 32 + var23 + 16 + 1] = var20;
                           }
                        }
                     }
                  }
               }
            }
         }

         int var19 = this.surroundings[16912];
         if (var19 >= 0) {
            var1.setBlockState(var2, var3.withProperty(CHECK_DECAY, Boolean.valueOf(false)), 4);
         } else {
            this.destroy(var1, var2);
         }
      }

   }

   private void destroy(World var1, BlockPos var2) {
      this.dropBlockAsItem(var1, var2, var1.getBlockState(var2), 0);
      var1.setBlockToAir(var2);
   }

   @SideOnly(Side.CLIENT)
   public void randomDisplayTick(IBlockState var1, World var2, BlockPos var3, Random var4) {
      if (var2.isRainingAt(var3.up()) && !var2.getBlockState(var3.down()).isFullyOpaque() && var4.nextInt(15) == 1) {
         double var5 = (double)((float)var3.getX() + var4.nextFloat());
         double var7 = (double)var3.getY() - 0.05D;
         double var9 = (double)((float)var3.getZ() + var4.nextFloat());
         var2.spawnParticle(EnumParticleTypes.DRIP_WATER, var5, var7, var9, 0.0D, 0.0D, 0.0D);
      }

   }

   public int quantityDropped(Random var1) {
      return var1.nextInt(20) == 0 ? 1 : 0;
   }

   @Nullable
   public Item getItemDropped(IBlockState var1, Random var2, int var3) {
      return Item.getItemFromBlock(Blocks.SAPLING);
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      super.dropBlockAsItemWithChance(var1, var2, var3, var4, var5);
   }

   protected void dropApple(World var1, BlockPos var2, IBlockState var3, int var4) {
   }

   protected int getSaplingDropChance(IBlockState var1) {
      return 20;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return !this.leavesFancy;
   }

   @SideOnly(Side.CLIENT)
   public void setGraphicsLevel(boolean var1) {
      this.leavesFancy = var1;
   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return this.leavesFancy ? BlockRenderLayer.CUTOUT_MIPPED : BlockRenderLayer.SOLID;
   }

   public boolean causesSuffocation() {
      return false;
   }

   public abstract BlockPlanks.EnumType getWoodType(int var1);

   public boolean isShearable(ItemStack var1, IBlockAccess var2, BlockPos var3) {
      return true;
   }

   public boolean isLeaves(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      return true;
   }

   public void beginLeavesDecay(IBlockState var1, World var2, BlockPos var3) {
      if (!((Boolean)var1.getValue(CHECK_DECAY)).booleanValue()) {
         var2.setBlockState(var3, var1.withProperty(CHECK_DECAY, Boolean.valueOf(true)), 4);
      }

   }

   public List getDrops(IBlockAccess var1, BlockPos var2, IBlockState var3, int var4) {
      ArrayList var5 = new ArrayList();
      Random var6 = var1 instanceof World ? ((World)var1).rand : new Random();
      int var7 = this.getSaplingDropChance(var3);
      if (var4 > 0) {
         var7 -= 2 << var4;
         if (var7 < 10) {
            var7 = 10;
         }
      }

      if (var6.nextInt(var7) == 0) {
         var5.add(new ItemStack(this.getItemDropped(var3, var6, var4), 1, this.damageDropped(var3)));
      }

      var7 = 200;
      if (var4 > 0) {
         var7 -= 10 << var4;
         if (var7 < 40) {
            var7 = 40;
         }
      }

      this.captureDrops(true);
      if (var1 instanceof World) {
         this.dropApple((World)var1, var2, var3, var7);
      }

      var5.addAll(this.captureDrops(false));
      return var5;
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return !this.leavesFancy && var2.getBlockState(var3.offset(var4)).getBlock() == this ? false : super.shouldSideBeRendered(var1, var2, var3, var4);
   }
}
