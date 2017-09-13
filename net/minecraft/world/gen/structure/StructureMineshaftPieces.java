package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class StructureMineshaftPieces {
   public static void registerStructurePieces() {
      MapGenStructureIO.registerStructureComponent(StructureMineshaftPieces.Corridor.class, "MSCorridor");
      MapGenStructureIO.registerStructureComponent(StructureMineshaftPieces.Cross.class, "MSCrossing");
      MapGenStructureIO.registerStructureComponent(StructureMineshaftPieces.Room.class, "MSRoom");
      MapGenStructureIO.registerStructureComponent(StructureMineshaftPieces.Stairs.class, "MSStairs");
   }

   private static StructureMineshaftPieces.Peice func_189940_a(List var0, Random var1, int var2, int var3, int var4, @Nullable EnumFacing var5, int var6, MapGenMineshaft.Type var7) {
      int var8 = var1.nextInt(100);
      if (var8 >= 80) {
         StructureBoundingBox var9 = StructureMineshaftPieces.Cross.findCrossing(var0, var1, var2, var3, var4, var5);
         if (var9 != null) {
            return new StructureMineshaftPieces.Cross(var6, var1, var9, var5, var7);
         }
      } else if (var8 >= 70) {
         StructureBoundingBox var10 = StructureMineshaftPieces.Stairs.findStairs(var0, var1, var2, var3, var4, var5);
         if (var10 != null) {
            return new StructureMineshaftPieces.Stairs(var6, var1, var10, var5, var7);
         }
      } else {
         StructureBoundingBox var11 = StructureMineshaftPieces.Corridor.findCorridorSize(var0, var1, var2, var3, var4, var5);
         if (var11 != null) {
            return new StructureMineshaftPieces.Corridor(var6, var1, var11, var5, var7);
         }
      }

      return null;
   }

   private static StructureMineshaftPieces.Peice func_189938_b(StructureComponent var0, List var1, Random var2, int var3, int var4, int var5, EnumFacing var6, int var7) {
      if (var7 > 8) {
         return null;
      } else if (Math.abs(var3 - var0.getBoundingBox().minX) <= 80 && Math.abs(var5 - var0.getBoundingBox().minZ) <= 80) {
         MapGenMineshaft.Type var8 = ((StructureMineshaftPieces.Peice)var0).mineShaftType;
         StructureMineshaftPieces.Peice var9 = func_189940_a(var1, var2, var3, var4, var5, var6, var7 + 1, var8);
         if (var9 != null) {
            var1.add(var9);
            var9.buildComponent(var0, var1, var2);
         }

         return var9;
      } else {
         return null;
      }
   }

   public static class Corridor extends StructureMineshaftPieces.Peice {
      private boolean hasRails;
      private boolean hasSpiders;
      private boolean spawnerPlaced;
      private int sectionCount;

      public Corridor() {
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setBoolean("hr", this.hasRails);
         var1.setBoolean("sc", this.hasSpiders);
         var1.setBoolean("hps", this.spawnerPlaced);
         var1.setInteger("Num", this.sectionCount);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.hasRails = var1.getBoolean("hr");
         this.hasSpiders = var1.getBoolean("sc");
         this.spawnerPlaced = var1.getBoolean("hps");
         this.sectionCount = var1.getInteger("Num");
      }

      public Corridor(int var1, Random var2, StructureBoundingBox var3, EnumFacing var4, MapGenMineshaft.Type var5) {
         super(var1, var5);
         this.setCoordBaseMode(var4);
         this.boundingBox = var3;
         this.hasRails = var2.nextInt(3) == 0;
         this.hasSpiders = !this.hasRails && var2.nextInt(23) == 0;
         if (this.getCoordBaseMode().getAxis() == EnumFacing.Axis.Z) {
            this.sectionCount = var3.getZSize() / 5;
         } else {
            this.sectionCount = var3.getXSize() / 5;
         }

      }

      public static StructureBoundingBox findCorridorSize(List var0, Random var1, int var2, int var3, int var4, EnumFacing var5) {
         StructureBoundingBox var6 = new StructureBoundingBox(var2, var3, var4, var2, var3 + 2, var4);

         int var7;
         for(var7 = var1.nextInt(3) + 2; var7 > 0; --var7) {
            int var8 = var7 * 5;
            switch(var5) {
            case NORTH:
            default:
               var6.maxX = var2 + 2;
               var6.minZ = var4 - (var8 - 1);
               break;
            case SOUTH:
               var6.maxX = var2 + 2;
               var6.maxZ = var4 + (var8 - 1);
               break;
            case WEST:
               var6.minX = var2 - (var8 - 1);
               var6.maxZ = var4 + 2;
               break;
            case EAST:
               var6.maxX = var2 + (var8 - 1);
               var6.maxZ = var4 + 2;
            }

            if (StructureComponent.findIntersecting(var0, var6) == null) {
               break;
            }
         }

         return var7 > 0 ? var6 : null;
      }

      public void buildComponent(StructureComponent var1, List var2, Random var3) {
         int var4 = this.getComponentType();
         int var5 = var3.nextInt(4);
         EnumFacing var6 = this.getCoordBaseMode();
         if (var6 != null) {
            switch(var6) {
            case NORTH:
            default:
               if (var5 <= 1) {
                  StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX, this.boundingBox.minY - 1 + var3.nextInt(3), this.boundingBox.minZ - 1, var6, var4);
               } else if (var5 == 2) {
                  StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY - 1 + var3.nextInt(3), this.boundingBox.minZ, EnumFacing.WEST, var4);
               } else {
                  StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY - 1 + var3.nextInt(3), this.boundingBox.minZ, EnumFacing.EAST, var4);
               }
               break;
            case SOUTH:
               if (var5 <= 1) {
                  StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX, this.boundingBox.minY - 1 + var3.nextInt(3), this.boundingBox.maxZ + 1, var6, var4);
               } else if (var5 == 2) {
                  StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY - 1 + var3.nextInt(3), this.boundingBox.maxZ - 3, EnumFacing.WEST, var4);
               } else {
                  StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY - 1 + var3.nextInt(3), this.boundingBox.maxZ - 3, EnumFacing.EAST, var4);
               }
               break;
            case WEST:
               if (var5 <= 1) {
                  StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY - 1 + var3.nextInt(3), this.boundingBox.minZ, var6, var4);
               } else if (var5 == 2) {
                  StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX, this.boundingBox.minY - 1 + var3.nextInt(3), this.boundingBox.minZ - 1, EnumFacing.NORTH, var4);
               } else {
                  StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX, this.boundingBox.minY - 1 + var3.nextInt(3), this.boundingBox.maxZ + 1, EnumFacing.SOUTH, var4);
               }
               break;
            case EAST:
               if (var5 <= 1) {
                  StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY - 1 + var3.nextInt(3), this.boundingBox.minZ, var6, var4);
               } else if (var5 == 2) {
                  StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.maxX - 3, this.boundingBox.minY - 1 + var3.nextInt(3), this.boundingBox.minZ - 1, EnumFacing.NORTH, var4);
               } else {
                  StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.maxX - 3, this.boundingBox.minY - 1 + var3.nextInt(3), this.boundingBox.maxZ + 1, EnumFacing.SOUTH, var4);
               }
            }
         }

         if (var4 < 8) {
            if (var6 != EnumFacing.NORTH && var6 != EnumFacing.SOUTH) {
               for(int var9 = this.boundingBox.minX + 3; var9 + 3 <= this.boundingBox.maxX; var9 += 5) {
                  int var10 = var3.nextInt(5);
                  if (var10 == 0) {
                     StructureMineshaftPieces.func_189938_b(var1, var2, var3, var9, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, var4 + 1);
                  } else if (var10 == 1) {
                     StructureMineshaftPieces.func_189938_b(var1, var2, var3, var9, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, var4 + 1);
                  }
               }
            } else {
               for(int var7 = this.boundingBox.minZ + 3; var7 + 3 <= this.boundingBox.maxZ; var7 += 5) {
                  int var8 = var3.nextInt(5);
                  if (var8 == 0) {
                     StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY, var7, EnumFacing.WEST, var4 + 1);
                  } else if (var8 == 1) {
                     StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY, var7, EnumFacing.EAST, var4 + 1);
                  }
               }
            }
         }

      }

      protected boolean generateChest(World var1, StructureBoundingBox var2, Random var3, int var4, int var5, int var6, ResourceLocation var7) {
         BlockPos var8 = new BlockPos(this.getXWithOffset(var4, var6), this.getYWithOffset(var5), this.getZWithOffset(var4, var6));
         if (var2.isVecInside(var8) && var1.getBlockState(var8).getMaterial() == Material.AIR && var1.getBlockState(var8.down()).getMaterial() != Material.AIR) {
            IBlockState var9 = Blocks.RAIL.getDefaultState().withProperty(BlockRail.SHAPE, var3.nextBoolean() ? BlockRailBase.EnumRailDirection.NORTH_SOUTH : BlockRailBase.EnumRailDirection.EAST_WEST);
            this.setBlockState(var1, var9, var4, var5, var6, var2);
            EntityMinecartChest var10 = new EntityMinecartChest(var1, (double)((float)var8.getX() + 0.5F), (double)((float)var8.getY() + 0.5F), (double)((float)var8.getZ() + 0.5F));
            var10.setLootTable(var7, var3.nextLong());
            var1.spawnEntity(var10);
            return true;
         } else {
            return false;
         }
      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
         } else {
            boolean var4 = false;
            boolean var5 = true;
            boolean var6 = false;
            boolean var7 = true;
            int var8 = this.sectionCount * 5 - 1;
            IBlockState var9 = this.func_189917_F_();
            this.fillWithBlocks(var1, var3, 0, 0, 0, 2, 1, var8, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.func_189914_a(var1, var3, var2, 0.8F, 0, 2, 0, 2, 2, var8, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false, 0);
            if (this.hasSpiders) {
               this.func_189914_a(var1, var3, var2, 0.6F, 0, 0, 0, 2, 1, var8, Blocks.WEB.getDefaultState(), Blocks.AIR.getDefaultState(), false, 8);
            }

            for(int var10 = 0; var10 < this.sectionCount; ++var10) {
               int var11 = 2 + var10 * 5;
               this.func_189921_a(var1, var3, 0, 0, var11, 2, 2, var2);
               this.func_189922_a(var1, var3, var2, 0.1F, 0, 2, var11 - 1);
               this.func_189922_a(var1, var3, var2, 0.1F, 2, 2, var11 - 1);
               this.func_189922_a(var1, var3, var2, 0.1F, 0, 2, var11 + 1);
               this.func_189922_a(var1, var3, var2, 0.1F, 2, 2, var11 + 1);
               this.func_189922_a(var1, var3, var2, 0.05F, 0, 2, var11 - 2);
               this.func_189922_a(var1, var3, var2, 0.05F, 2, 2, var11 - 2);
               this.func_189922_a(var1, var3, var2, 0.05F, 0, 2, var11 + 2);
               this.func_189922_a(var1, var3, var2, 0.05F, 2, 2, var11 + 2);
               if (var2.nextInt(100) == 0) {
                  this.generateChest(var1, var3, var2, 2, 0, var11 - 1, LootTableList.CHESTS_ABANDONED_MINESHAFT);
               }

               if (var2.nextInt(100) == 0) {
                  this.generateChest(var1, var3, var2, 0, 0, var11 + 1, LootTableList.CHESTS_ABANDONED_MINESHAFT);
               }

               if (this.hasSpiders && !this.spawnerPlaced) {
                  int var12 = this.getYWithOffset(0);
                  int var13 = var11 - 1 + var2.nextInt(3);
                  int var14 = this.getXWithOffset(1, var13);
                  int var15 = this.getZWithOffset(1, var13);
                  BlockPos var16 = new BlockPos(var14, var12, var15);
                  if (var3.isVecInside(var16) && this.func_189916_b(var1, 1, 0, var13, var3) < 8) {
                     this.spawnerPlaced = true;
                     var1.setBlockState(var16, Blocks.MOB_SPAWNER.getDefaultState(), 2);
                     TileEntity var17 = var1.getTileEntity(var16);
                     if (var17 instanceof TileEntityMobSpawner) {
                        ((TileEntityMobSpawner)var17).getSpawnerBaseLogic().setEntityName("CaveSpider");
                     }
                  }
               }
            }

            for(int var18 = 0; var18 <= 2; ++var18) {
               for(int var20 = 0; var20 <= var8; ++var20) {
                  boolean var22 = true;
                  IBlockState var24 = this.getBlockStateFromPos(var1, var18, -1, var20, var3);
                  if (var24.getMaterial() == Material.AIR && this.func_189916_b(var1, var18, -1, var20, var3) < 8) {
                     boolean var26 = true;
                     this.setBlockState(var1, var9, var18, -1, var20, var3);
                  }
               }
            }

            if (this.hasRails) {
               IBlockState var19 = Blocks.RAIL.getDefaultState().withProperty(BlockRail.SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH);

               for(int var21 = 0; var21 <= var8; ++var21) {
                  IBlockState var23 = this.getBlockStateFromPos(var1, 1, -1, var21, var3);
                  if (var23.getMaterial() != Material.AIR && var23.isFullBlock()) {
                     float var25 = this.func_189916_b(var1, 1, 0, var21, var3) > 8 ? 0.9F : 0.7F;
                     this.randomlyPlaceBlock(var1, var3, var2, var25, 1, 0, var21, var19);
                  }
               }
            }

            return true;
         }
      }

      private void func_189921_a(World var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6, int var7, Random var8) {
         if (this.func_189918_a(var1, var2, var3, var7, var6, var5)) {
            IBlockState var9 = this.func_189917_F_();
            IBlockState var10 = this.func_189919_b();
            IBlockState var11 = Blocks.AIR.getDefaultState();
            this.fillWithBlocks(var1, var2, var3, var4, var5, var3, var6 - 1, var5, var10, var11, false);
            this.fillWithBlocks(var1, var2, var7, var4, var5, var7, var6 - 1, var5, var10, var11, false);
            if (var8.nextInt(4) == 0) {
               this.fillWithBlocks(var1, var2, var3, var6, var5, var3, var6, var5, var9, var11, false);
               this.fillWithBlocks(var1, var2, var7, var6, var5, var7, var6, var5, var9, var11, false);
            } else {
               this.fillWithBlocks(var1, var2, var3, var6, var5, var7, var6, var5, var9, var11, false);
               this.randomlyPlaceBlock(var1, var2, var8, 0.05F, var3 + 1, var6, var5 - 1, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.NORTH));
               this.randomlyPlaceBlock(var1, var2, var8, 0.05F, var3 + 1, var6, var5 + 1, Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.SOUTH));
            }

         }
      }

      private void func_189922_a(World var1, StructureBoundingBox var2, Random var3, float var4, int var5, int var6, int var7) {
         if (this.func_189916_b(var1, var5, var6, var7, var2) < 8) {
            this.randomlyPlaceBlock(var1, var2, var3, var4, var5, var6, var7, Blocks.WEB.getDefaultState());
         }

      }
   }

   public static class Cross extends StructureMineshaftPieces.Peice {
      private EnumFacing corridorDirection;
      private boolean isMultipleFloors;

      public Cross() {
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setBoolean("tf", this.isMultipleFloors);
         var1.setInteger("D", this.corridorDirection.getHorizontalIndex());
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.isMultipleFloors = var1.getBoolean("tf");
         this.corridorDirection = EnumFacing.getHorizontal(var1.getInteger("D"));
      }

      public Cross(int var1, Random var2, StructureBoundingBox var3, @Nullable EnumFacing var4, MapGenMineshaft.Type var5) {
         super(var1, var5);
         this.corridorDirection = var4;
         this.boundingBox = var3;
         this.isMultipleFloors = var3.getYSize() > 3;
      }

      public static StructureBoundingBox findCrossing(List var0, Random var1, int var2, int var3, int var4, EnumFacing var5) {
         StructureBoundingBox var6 = new StructureBoundingBox(var2, var3, var4, var2, var3 + 2, var4);
         if (var1.nextInt(4) == 0) {
            var6.maxY += 4;
         }

         switch(var5) {
         case NORTH:
         default:
            var6.minX = var2 - 1;
            var6.maxX = var2 + 3;
            var6.minZ = var4 - 4;
            break;
         case SOUTH:
            var6.minX = var2 - 1;
            var6.maxX = var2 + 3;
            var6.maxZ = var4 + 3 + 1;
            break;
         case WEST:
            var6.minX = var2 - 4;
            var6.minZ = var4 - 1;
            var6.maxZ = var4 + 3;
            break;
         case EAST:
            var6.maxX = var2 + 3 + 1;
            var6.minZ = var4 - 1;
            var6.maxZ = var4 + 3;
         }

         return StructureComponent.findIntersecting(var0, var6) != null ? null : var6;
      }

      public void buildComponent(StructureComponent var1, List var2, Random var3) {
         int var4 = this.getComponentType();
         switch(this.corridorDirection) {
         case NORTH:
         default:
            StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, var4);
            StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.WEST, var4);
            StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.EAST, var4);
            break;
         case SOUTH:
            StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, var4);
            StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.WEST, var4);
            StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.EAST, var4);
            break;
         case WEST:
            StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, var4);
            StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, var4);
            StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.WEST, var4);
            break;
         case EAST:
            StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, var4);
            StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, var4);
            StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, EnumFacing.EAST, var4);
         }

         if (this.isMultipleFloors) {
            if (var3.nextBoolean()) {
               StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.minY + 3 + 1, this.boundingBox.minZ - 1, EnumFacing.NORTH, var4);
            }

            if (var3.nextBoolean()) {
               StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY + 3 + 1, this.boundingBox.minZ + 1, EnumFacing.WEST, var4);
            }

            if (var3.nextBoolean()) {
               StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY + 3 + 1, this.boundingBox.minZ + 1, EnumFacing.EAST, var4);
            }

            if (var3.nextBoolean()) {
               StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX + 1, this.boundingBox.minY + 3 + 1, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, var4);
            }
         }

      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
         } else {
            IBlockState var4 = this.func_189917_F_();
            if (this.isMultipleFloors) {
               this.fillWithBlocks(var1, var3, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ, this.boundingBox.maxX - 1, this.boundingBox.minY + 3 - 1, this.boundingBox.maxZ, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
               this.fillWithBlocks(var1, var3, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxX, this.boundingBox.minY + 3 - 1, this.boundingBox.maxZ - 1, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
               this.fillWithBlocks(var1, var3, this.boundingBox.minX + 1, this.boundingBox.maxY - 2, this.boundingBox.minZ, this.boundingBox.maxX - 1, this.boundingBox.maxY, this.boundingBox.maxZ, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
               this.fillWithBlocks(var1, var3, this.boundingBox.minX, this.boundingBox.maxY - 2, this.boundingBox.minZ + 1, this.boundingBox.maxX, this.boundingBox.maxY, this.boundingBox.maxZ - 1, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
               this.fillWithBlocks(var1, var3, this.boundingBox.minX + 1, this.boundingBox.minY + 3, this.boundingBox.minZ + 1, this.boundingBox.maxX - 1, this.boundingBox.minY + 3, this.boundingBox.maxZ - 1, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            } else {
               this.fillWithBlocks(var1, var3, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ, this.boundingBox.maxX - 1, this.boundingBox.maxY, this.boundingBox.maxZ, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
               this.fillWithBlocks(var1, var3, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxX, this.boundingBox.maxY, this.boundingBox.maxZ - 1, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            }

            this.func_189923_b(var1, var3, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxY);
            this.func_189923_b(var1, var3, this.boundingBox.minX + 1, this.boundingBox.minY, this.boundingBox.maxZ - 1, this.boundingBox.maxY);
            this.func_189923_b(var1, var3, this.boundingBox.maxX - 1, this.boundingBox.minY, this.boundingBox.minZ + 1, this.boundingBox.maxY);
            this.func_189923_b(var1, var3, this.boundingBox.maxX - 1, this.boundingBox.minY, this.boundingBox.maxZ - 1, this.boundingBox.maxY);

            for(int var5 = this.boundingBox.minX; var5 <= this.boundingBox.maxX; ++var5) {
               for(int var6 = this.boundingBox.minZ; var6 <= this.boundingBox.maxZ; ++var6) {
                  if (this.getBlockStateFromPos(var1, var5, this.boundingBox.minY - 1, var6, var3).getMaterial() == Material.AIR && this.func_189916_b(var1, var5, this.boundingBox.minY - 1, var6, var3) < 8) {
                     this.setBlockState(var1, var4, var5, this.boundingBox.minY - 1, var6, var3);
                  }
               }
            }

            return true;
         }
      }

      private void func_189923_b(World var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6) {
         if (this.getBlockStateFromPos(var1, var3, var6 + 1, var5, var2).getMaterial() != Material.AIR) {
            this.fillWithBlocks(var1, var2, var3, var4, var5, var3, var6, var5, this.func_189917_F_(), Blocks.AIR.getDefaultState(), false);
         }

      }
   }

   abstract static class Peice extends StructureComponent {
      protected MapGenMineshaft.Type mineShaftType;

      public Peice() {
      }

      public Peice(int var1, MapGenMineshaft.Type var2) {
         super(var1);
         this.mineShaftType = var2;
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         var1.setInteger("MST", this.mineShaftType.ordinal());
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         this.mineShaftType = MapGenMineshaft.Type.byId(var1.getInteger("MST"));
      }

      protected IBlockState func_189917_F_() {
         switch(this.mineShaftType) {
         case NORMAL:
         default:
            return Blocks.PLANKS.getDefaultState();
         case MESA:
            return Blocks.PLANKS.getDefaultState().withProperty(BlockPlanks.VARIANT, BlockPlanks.EnumType.DARK_OAK);
         }
      }

      protected IBlockState func_189919_b() {
         switch(this.mineShaftType) {
         case NORMAL:
         default:
            return Blocks.OAK_FENCE.getDefaultState();
         case MESA:
            return Blocks.DARK_OAK_FENCE.getDefaultState();
         }
      }

      protected boolean func_189918_a(World var1, StructureBoundingBox var2, int var3, int var4, int var5, int var6) {
         for(int var7 = var3; var7 <= var4; ++var7) {
            if (this.getBlockStateFromPos(var1, var7, var5 + 1, var6, var2).getMaterial() == Material.AIR) {
               return false;
            }
         }

         return true;
      }
   }

   public static class Room extends StructureMineshaftPieces.Peice {
      private final List roomsLinkedToTheRoom = Lists.newLinkedList();

      public Room() {
      }

      public Room(int var1, Random var2, int var3, int var4, MapGenMineshaft.Type var5) {
         super(var1, var5);
         this.mineShaftType = var5;
         this.boundingBox = new StructureBoundingBox(var3, 50, var4, var3 + 7 + var2.nextInt(6), 54 + var2.nextInt(6), var4 + 7 + var2.nextInt(6));
      }

      public void buildComponent(StructureComponent var1, List var2, Random var3) {
         int var4 = this.getComponentType();
         int var5 = this.boundingBox.getYSize() - 3 - 1;
         if (var5 <= 0) {
            var5 = 1;
         }

         int var9;
         for(var9 = 0; var9 < this.boundingBox.getXSize(); var9 = var9 + 4) {
            var9 = var9 + var3.nextInt(this.boundingBox.getXSize());
            if (var9 + 3 > this.boundingBox.getXSize()) {
               break;
            }

            StructureMineshaftPieces.Peice var7 = StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX + var9, this.boundingBox.minY + var3.nextInt(var5) + 1, this.boundingBox.minZ - 1, EnumFacing.NORTH, var4);
            if (var7 != null) {
               StructureBoundingBox var8 = var7.getBoundingBox();
               this.roomsLinkedToTheRoom.add(new StructureBoundingBox(var8.minX, var8.minY, this.boundingBox.minZ, var8.maxX, var8.maxY, this.boundingBox.minZ + 1));
            }
         }

         for(var9 = 0; var9 < this.boundingBox.getXSize(); var9 = var9 + 4) {
            var9 = var9 + var3.nextInt(this.boundingBox.getXSize());
            if (var9 + 3 > this.boundingBox.getXSize()) {
               break;
            }

            StructureMineshaftPieces.Peice var16 = StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX + var9, this.boundingBox.minY + var3.nextInt(var5) + 1, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, var4);
            if (var16 != null) {
               StructureBoundingBox var19 = var16.getBoundingBox();
               this.roomsLinkedToTheRoom.add(new StructureBoundingBox(var19.minX, var19.minY, this.boundingBox.maxZ - 1, var19.maxX, var19.maxY, this.boundingBox.maxZ));
            }
         }

         for(var9 = 0; var9 < this.boundingBox.getZSize(); var9 = var9 + 4) {
            var9 = var9 + var3.nextInt(this.boundingBox.getZSize());
            if (var9 + 3 > this.boundingBox.getZSize()) {
               break;
            }

            StructureMineshaftPieces.Peice var17 = StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY + var3.nextInt(var5) + 1, this.boundingBox.minZ + var9, EnumFacing.WEST, var4);
            if (var17 != null) {
               StructureBoundingBox var20 = var17.getBoundingBox();
               this.roomsLinkedToTheRoom.add(new StructureBoundingBox(this.boundingBox.minX, var20.minY, var20.minZ, this.boundingBox.minX + 1, var20.maxY, var20.maxZ));
            }
         }

         for(var9 = 0; var9 < this.boundingBox.getZSize(); var9 = var9 + 4) {
            var9 = var9 + var3.nextInt(this.boundingBox.getZSize());
            if (var9 + 3 > this.boundingBox.getZSize()) {
               break;
            }

            StructureMineshaftPieces.Peice var18 = StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY + var3.nextInt(var5) + 1, this.boundingBox.minZ + var9, EnumFacing.EAST, var4);
            if (var18 != null) {
               StructureBoundingBox var21 = var18.getBoundingBox();
               this.roomsLinkedToTheRoom.add(new StructureBoundingBox(this.boundingBox.maxX - 1, var21.minY, var21.minZ, this.boundingBox.maxX, var21.maxY, var21.maxZ));
            }
         }

      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
         } else {
            this.fillWithBlocks(var1, var3, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ, this.boundingBox.maxX, this.boundingBox.minY, this.boundingBox.maxZ, Blocks.DIRT.getDefaultState(), Blocks.AIR.getDefaultState(), true);
            this.fillWithBlocks(var1, var3, this.boundingBox.minX, this.boundingBox.minY + 1, this.boundingBox.minZ, this.boundingBox.maxX, Math.min(this.boundingBox.minY + 3, this.boundingBox.maxY), this.boundingBox.maxZ, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);

            for(StructureBoundingBox var5 : this.roomsLinkedToTheRoom) {
               this.fillWithBlocks(var1, var3, var5.minX, var5.maxY - 2, var5.minZ, var5.maxX, var5.maxY, var5.maxZ, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            }

            this.randomlyRareFillWithBlocks(var1, var3, this.boundingBox.minX, this.boundingBox.minY + 4, this.boundingBox.minZ, this.boundingBox.maxX, this.boundingBox.maxY, this.boundingBox.maxZ, Blocks.AIR.getDefaultState(), false);
            return true;
         }
      }

      public void offset(int var1, int var2, int var3) {
         super.offset(var1, var2, var3);

         for(StructureBoundingBox var5 : this.roomsLinkedToTheRoom) {
            var5.offset(var1, var2, var3);
         }

      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         NBTTagList var2 = new NBTTagList();

         for(StructureBoundingBox var4 : this.roomsLinkedToTheRoom) {
            var2.appendTag(var4.toNBTTagIntArray());
         }

         var1.setTag("Entrances", var2);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         NBTTagList var2 = var1.getTagList("Entrances", 11);

         for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
            this.roomsLinkedToTheRoom.add(new StructureBoundingBox(var2.getIntArrayAt(var3)));
         }

      }
   }

   public static class Stairs extends StructureMineshaftPieces.Peice {
      public Stairs() {
      }

      public Stairs(int var1, Random var2, StructureBoundingBox var3, EnumFacing var4, MapGenMineshaft.Type var5) {
         super(var1, var5);
         this.setCoordBaseMode(var4);
         this.boundingBox = var3;
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
      }

      public static StructureBoundingBox findStairs(List var0, Random var1, int var2, int var3, int var4, EnumFacing var5) {
         StructureBoundingBox var6 = new StructureBoundingBox(var2, var3 - 5, var4, var2, var3 + 2, var4);
         switch(var5) {
         case NORTH:
         default:
            var6.maxX = var2 + 2;
            var6.minZ = var4 - 8;
            break;
         case SOUTH:
            var6.maxX = var2 + 2;
            var6.maxZ = var4 + 8;
            break;
         case WEST:
            var6.minX = var2 - 8;
            var6.maxZ = var4 + 2;
            break;
         case EAST:
            var6.maxX = var2 + 8;
            var6.maxZ = var4 + 2;
         }

         return StructureComponent.findIntersecting(var0, var6) != null ? null : var6;
      }

      public void buildComponent(StructureComponent var1, List var2, Random var3) {
         int var4 = this.getComponentType();
         EnumFacing var5 = this.getCoordBaseMode();
         if (var5 != null) {
            switch(var5) {
            case NORTH:
            default:
               StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.minZ - 1, EnumFacing.NORTH, var4);
               break;
            case SOUTH:
               StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX, this.boundingBox.minY, this.boundingBox.maxZ + 1, EnumFacing.SOUTH, var4);
               break;
            case WEST:
               StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.minX - 1, this.boundingBox.minY, this.boundingBox.minZ, EnumFacing.WEST, var4);
               break;
            case EAST:
               StructureMineshaftPieces.func_189938_b(var1, var2, var3, this.boundingBox.maxX + 1, this.boundingBox.minY, this.boundingBox.minZ, EnumFacing.EAST, var4);
            }
         }

      }

      public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
         if (this.isLiquidInStructureBoundingBox(var1, var3)) {
            return false;
         } else {
            this.fillWithBlocks(var1, var3, 0, 5, 0, 2, 7, 1, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            this.fillWithBlocks(var1, var3, 0, 0, 7, 2, 2, 8, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);

            for(int var4 = 0; var4 < 5; ++var4) {
               this.fillWithBlocks(var1, var3, 0, 5 - var4 - (var4 < 4 ? 1 : 0), 2 + var4, 2, 7 - var4, 2 + var4, Blocks.AIR.getDefaultState(), Blocks.AIR.getDefaultState(), false);
            }

            return true;
         }
      }
   }
}
