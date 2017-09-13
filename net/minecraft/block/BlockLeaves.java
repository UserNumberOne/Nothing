package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.event.block.LeavesDecayEvent;

public abstract class BlockLeaves extends Block {
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
      int var4 = var2.getX();
      int var5 = var2.getY();
      int var6 = var2.getZ();
      if (var1.isAreaLoaded(new BlockPos(var4 - 2, var5 - 2, var6 - 2), new BlockPos(var4 + 2, var5 + 2, var6 + 2))) {
         for(int var7 = -1; var7 <= 1; ++var7) {
            for(int var8 = -1; var8 <= 1; ++var8) {
               for(int var9 = -1; var9 <= 1; ++var9) {
                  BlockPos var10 = var2.add(var7, var8, var9);
                  IBlockState var11 = var1.getBlockState(var10);
                  if (var11.getMaterial() == Material.LEAVES && !((Boolean)var11.getValue(CHECK_DECAY)).booleanValue()) {
                     var1.setBlockState(var10, var11.withProperty(CHECK_DECAY, Boolean.valueOf(true)), 4);
                  }
               }
            }
         }
      }

   }

   public void updateTick(World var1, BlockPos var2, IBlockState var3, Random var4) {
      if (!var1.isRemote && ((Boolean)var3.getValue(CHECK_DECAY)).booleanValue() && ((Boolean)var3.getValue(DECAYABLE)).booleanValue()) {
         int var5 = var2.getX();
         int var6 = var2.getY();
         int var7 = var2.getZ();
         if (this.surroundings == null) {
            this.surroundings = new int['耀'];
         }

         if (var1.isAreaLoaded(new BlockPos(var5 - 5, var6 - 5, var7 - 5), new BlockPos(var5 + 5, var6 + 5, var7 + 5))) {
            BlockPos.MutableBlockPos var8 = new BlockPos.MutableBlockPos();

            for(int var9 = -4; var9 <= 4; ++var9) {
               for(int var10 = -4; var10 <= 4; ++var10) {
                  for(int var11 = -4; var11 <= 4; ++var11) {
                     IBlockState var12 = var1.getBlockState(var8.setPos(var5 + var9, var6 + var10, var7 + var11));
                     Block var13 = var12.getBlock();
                     if (var13 != Blocks.LOG && var13 != Blocks.LOG2) {
                        if (var12.getMaterial() == Material.LEAVES) {
                           this.surroundings[(var9 + 16) * 1024 + (var10 + 16) * 32 + var11 + 16] = -2;
                        } else {
                           this.surroundings[(var9 + 16) * 1024 + (var10 + 16) * 32 + var11 + 16] = -1;
                        }
                     } else {
                        this.surroundings[(var9 + 16) * 1024 + (var10 + 16) * 32 + var11 + 16] = 0;
                     }
                  }
               }
            }

            for(int var15 = 1; var15 <= 4; ++var15) {
               for(int var16 = -4; var16 <= 4; ++var16) {
                  for(int var17 = -4; var17 <= 4; ++var17) {
                     for(int var18 = -4; var18 <= 4; ++var18) {
                        if (this.surroundings[(var16 + 16) * 1024 + (var17 + 16) * 32 + var18 + 16] == var15 - 1) {
                           if (this.surroundings[(var16 + 16 - 1) * 1024 + (var17 + 16) * 32 + var18 + 16] == -2) {
                              this.surroundings[(var16 + 16 - 1) * 1024 + (var17 + 16) * 32 + var18 + 16] = var15;
                           }

                           if (this.surroundings[(var16 + 16 + 1) * 1024 + (var17 + 16) * 32 + var18 + 16] == -2) {
                              this.surroundings[(var16 + 16 + 1) * 1024 + (var17 + 16) * 32 + var18 + 16] = var15;
                           }

                           if (this.surroundings[(var16 + 16) * 1024 + (var17 + 16 - 1) * 32 + var18 + 16] == -2) {
                              this.surroundings[(var16 + 16) * 1024 + (var17 + 16 - 1) * 32 + var18 + 16] = var15;
                           }

                           if (this.surroundings[(var16 + 16) * 1024 + (var17 + 16 + 1) * 32 + var18 + 16] == -2) {
                              this.surroundings[(var16 + 16) * 1024 + (var17 + 16 + 1) * 32 + var18 + 16] = var15;
                           }

                           if (this.surroundings[(var16 + 16) * 1024 + (var17 + 16) * 32 + (var18 + 16 - 1)] == -2) {
                              this.surroundings[(var16 + 16) * 1024 + (var17 + 16) * 32 + (var18 + 16 - 1)] = var15;
                           }

                           if (this.surroundings[(var16 + 16) * 1024 + (var17 + 16) * 32 + var18 + 16 + 1] == -2) {
                              this.surroundings[(var16 + 16) * 1024 + (var17 + 16) * 32 + var18 + 16 + 1] = var15;
                           }
                        }
                     }
                  }
               }
            }
         }

         int var14 = this.surroundings[16912];
         if (var14 >= 0) {
            var1.setBlockState(var2, var3.withProperty(CHECK_DECAY, Boolean.valueOf(false)), 4);
         } else {
            this.destroy(var1, var2);
         }
      }

   }

   private void destroy(World var1, BlockPos var2) {
      LeavesDecayEvent var3 = new LeavesDecayEvent(var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ()));
      var1.getServer().getPluginManager().callEvent(var3);
      if (!var3.isCancelled() && var1.getBlockState(var2).getBlock() == this) {
         this.dropBlockAsItem(var1, var2, var1.getBlockState(var2), 0);
         var1.setBlockToAir(var2);
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
      if (!var1.isRemote) {
         int var6 = this.getSaplingDropChance(var3);
         if (var5 > 0) {
            var6 -= 2 << var5;
            if (var6 < 10) {
               var6 = 10;
            }
         }

         if (var1.rand.nextInt(var6) == 0) {
            Item var7 = this.getItemDropped(var3, var1.rand, var5);
            spawnAsEntity(var1, var2, new ItemStack(var7, 1, this.damageDropped(var3)));
         }

         var6 = 200;
         if (var5 > 0) {
            var6 -= 10 << var5;
            if (var6 < 40) {
               var6 = 40;
            }
         }

         this.dropApple(var1, var2, var3, var6);
      }

   }

   protected void dropApple(World var1, BlockPos var2, IBlockState var3, int var4) {
   }

   protected int getSaplingDropChance(IBlockState var1) {
      return 20;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return !this.leavesFancy;
   }

   public boolean causesSuffocation() {
      return false;
   }

   public abstract BlockPlanks.EnumType getWoodType(int var1);
}
