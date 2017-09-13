package net.minecraft.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Random;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;

public class Teleporter {
   private final WorldServer worldServerInstance;
   private final Random random;
   private final Long2ObjectMap destinationCoordinateCache = new Long2ObjectOpenHashMap(4096);

   public Teleporter(WorldServer var1) {
      this.worldServerInstance = var1;
      this.random = new Random(var1.getSeed());
   }

   public void placeInPortal(Entity var1, float var2) {
      if (this.worldServerInstance.provider.getDimensionType().getId() != 1) {
         if (!this.placeInExistingPortal(var1, var2)) {
            this.makePortal(var1);
            this.placeInExistingPortal(var1, var2);
         }
      } else {
         int var3 = MathHelper.floor(var1.posX);
         int var4 = MathHelper.floor(var1.posY) - 1;
         int var5 = MathHelper.floor(var1.posZ);
         boolean var6 = true;
         boolean var7 = false;

         for(int var8 = -2; var8 <= 2; ++var8) {
            for(int var9 = -2; var9 <= 2; ++var9) {
               for(int var10 = -1; var10 < 3; ++var10) {
                  int var11 = var3 + var9 * 1 + var8 * 0;
                  int var12 = var4 + var10;
                  int var13 = var5 + var9 * 0 - var8 * 1;
                  boolean var14 = var10 < 0;
                  this.worldServerInstance.setBlockState(new BlockPos(var11, var12, var13), var14 ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState());
               }
            }
         }

         var1.setLocationAndAngles((double)var3, (double)var4, (double)var5, var1.rotationYaw, 0.0F);
         var1.motionX = 0.0D;
         var1.motionY = 0.0D;
         var1.motionZ = 0.0D;
      }

   }

   public boolean placeInExistingPortal(Entity var1, float var2) {
      boolean var3 = true;
      double var4 = -1.0D;
      int var6 = MathHelper.floor(var1.posX);
      int var7 = MathHelper.floor(var1.posZ);
      boolean var8 = true;
      Object var9 = BlockPos.ORIGIN;
      long var10 = ChunkPos.asLong(var6, var7);
      if (this.destinationCoordinateCache.containsKey(var10)) {
         Teleporter.PortalPosition var12 = (Teleporter.PortalPosition)this.destinationCoordinateCache.get(var10);
         var4 = 0.0D;
         var9 = var12;
         var12.lastUpdateTime = this.worldServerInstance.getTotalWorldTime();
         var8 = false;
      } else {
         BlockPos var30 = new BlockPos(var1);

         for(int var13 = -128; var13 <= 128; ++var13) {
            BlockPos var14;
            for(int var15 = -128; var15 <= 128; ++var15) {
               for(BlockPos var16 = var30.add(var13, this.worldServerInstance.getActualHeight() - 1 - var30.getY(), var15); var16.getY() >= 0; var16 = var14) {
                  var14 = var16.down();
                  if (this.worldServerInstance.getBlockState(var16).getBlock() == Blocks.PORTAL) {
                     for(var14 = var16.down(); this.worldServerInstance.getBlockState(var14).getBlock() == Blocks.PORTAL; var14 = var14.down()) {
                        var16 = var14;
                     }

                     double var17 = var16.distanceSq(var30);
                     if (var4 < 0.0D || var17 < var4) {
                        var4 = var17;
                        var9 = var16;
                     }
                  }
               }
            }
         }
      }

      if (var4 >= 0.0D) {
         if (var8) {
            this.destinationCoordinateCache.put(var10, new Teleporter.PortalPosition((BlockPos)var9, this.worldServerInstance.getTotalWorldTime()));
         }

         double var31 = (double)((BlockPos)var9).getX() + 0.5D;
         double var32 = (double)((BlockPos)var9).getZ() + 0.5D;
         BlockPattern.PatternHelper var33 = Blocks.PORTAL.createPatternHelper(this.worldServerInstance, (BlockPos)var9);
         boolean var34 = var33.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
         double var18 = var33.getForwards().getAxis() == EnumFacing.Axis.X ? (double)var33.getFrontTopLeft().getZ() : (double)var33.getFrontTopLeft().getX();
         double var20 = (double)(var33.getFrontTopLeft().getY() + 1) - var1.getLastPortalVec().yCoord * (double)var33.getHeight();
         if (var34) {
            ++var18;
         }

         if (var33.getForwards().getAxis() == EnumFacing.Axis.X) {
            var32 = var18 + (1.0D - var1.getLastPortalVec().xCoord) * (double)var33.getWidth() * (double)var33.getForwards().rotateY().getAxisDirection().getOffset();
         } else {
            var31 = var18 + (1.0D - var1.getLastPortalVec().xCoord) * (double)var33.getWidth() * (double)var33.getForwards().rotateY().getAxisDirection().getOffset();
         }

         float var22 = 0.0F;
         float var23 = 0.0F;
         float var24 = 0.0F;
         float var25 = 0.0F;
         if (var33.getForwards().getOpposite() == var1.getTeleportDirection()) {
            var22 = 1.0F;
            var23 = 1.0F;
         } else if (var33.getForwards().getOpposite() == var1.getTeleportDirection().getOpposite()) {
            var22 = -1.0F;
            var23 = -1.0F;
         } else if (var33.getForwards().getOpposite() == var1.getTeleportDirection().rotateY()) {
            var24 = 1.0F;
            var25 = -1.0F;
         } else {
            var24 = -1.0F;
            var25 = 1.0F;
         }

         double var26 = var1.motionX;
         double var28 = var1.motionZ;
         var1.motionX = var26 * (double)var22 + var28 * (double)var25;
         var1.motionZ = var26 * (double)var24 + var28 * (double)var23;
         var1.rotationYaw = var2 - (float)(var1.getTeleportDirection().getOpposite().getHorizontalIndex() * 90) + (float)(var33.getForwards().getHorizontalIndex() * 90);
         if (var1 instanceof EntityPlayerMP) {
            ((EntityPlayerMP)var1).connection.setPlayerLocation(var31, var20, var32, var1.rotationYaw, var1.rotationPitch);
         } else {
            var1.setLocationAndAngles(var31, var20, var32, var1.rotationYaw, var1.rotationPitch);
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean makePortal(Entity var1) {
      boolean var2 = true;
      double var3 = -1.0D;
      int var5 = MathHelper.floor(var1.posX);
      int var6 = MathHelper.floor(var1.posY);
      int var7 = MathHelper.floor(var1.posZ);
      int var8 = var5;
      int var9 = var6;
      int var10 = var7;
      int var11 = 0;
      int var12 = this.random.nextInt(4);
      BlockPos.MutableBlockPos var13 = new BlockPos.MutableBlockPos();

      for(int var14 = var5 - 16; var14 <= var5 + 16; ++var14) {
         double var15 = (double)var14 + 0.5D - var1.posX;

         for(int var17 = var7 - 16; var17 <= var7 + 16; ++var17) {
            double var18 = (double)var17 + 0.5D - var1.posZ;

            label291:
            for(int var20 = this.worldServerInstance.getActualHeight() - 1; var20 >= 0; --var20) {
               if (this.worldServerInstance.isAirBlock(var13.setPos(var14, var20, var17))) {
                  while(var20 > 0 && this.worldServerInstance.isAirBlock(var13.setPos(var14, var20 - 1, var17))) {
                     --var20;
                  }

                  for(int var21 = var12; var21 < var12 + 4; ++var21) {
                     int var22 = var21 % 2;
                     int var23 = 1 - var22;
                     if (var21 % 4 >= 2) {
                        var22 = -var22;
                        var23 = -var23;
                     }

                     for(int var24 = 0; var24 < 3; ++var24) {
                        for(int var25 = 0; var25 < 4; ++var25) {
                           for(int var26 = -1; var26 < 4; ++var26) {
                              int var27 = var14 + (var25 - 1) * var22 + var24 * var23;
                              int var28 = var20 + var26;
                              int var29 = var17 + (var25 - 1) * var23 - var24 * var22;
                              var13.setPos(var27, var28, var29);
                              if (var26 < 0 && !this.worldServerInstance.getBlockState(var13).getMaterial().isSolid() || var26 >= 0 && !this.worldServerInstance.isAirBlock(var13)) {
                                 continue label291;
                              }
                           }
                        }
                     }

                     double var55 = (double)var20 + 0.5D - var1.posY;
                     double var65 = var15 * var15 + var55 * var55 + var18 * var18;
                     if (var3 < 0.0D || var65 < var3) {
                        var3 = var65;
                        var8 = var14;
                        var9 = var20;
                        var10 = var17;
                        var11 = var21 % 4;
                     }
                  }
               }
            }
         }
      }

      if (var3 < 0.0D) {
         for(int var31 = var5 - 16; var31 <= var5 + 16; ++var31) {
            double var33 = (double)var31 + 0.5D - var1.posX;

            for(int var35 = var7 - 16; var35 <= var7 + 16; ++var35) {
               double var37 = (double)var35 + 0.5D - var1.posZ;

               label229:
               for(int var40 = this.worldServerInstance.getActualHeight() - 1; var40 >= 0; --var40) {
                  if (this.worldServerInstance.isAirBlock(var13.setPos(var31, var40, var35))) {
                     while(var40 > 0 && this.worldServerInstance.isAirBlock(var13.setPos(var31, var40 - 1, var35))) {
                        --var40;
                     }

                     for(int var43 = var12; var43 < var12 + 2; ++var43) {
                        int var47 = var43 % 2;
                        int var51 = 1 - var47;

                        for(int var56 = 0; var56 < 4; ++var56) {
                           for(int var61 = -1; var61 < 4; ++var61) {
                              int var66 = var31 + (var56 - 1) * var47;
                              int var70 = var40 + var61;
                              int var71 = var35 + (var56 - 1) * var51;
                              var13.setPos(var66, var70, var71);
                              if (var61 < 0 && !this.worldServerInstance.getBlockState(var13).getMaterial().isSolid() || var61 >= 0 && !this.worldServerInstance.isAirBlock(var13)) {
                                 continue label229;
                              }
                           }
                        }

                        double var57 = (double)var40 + 0.5D - var1.posY;
                        double var67 = var33 * var33 + var57 * var57 + var37 * var37;
                        if (var3 < 0.0D || var67 < var3) {
                           var3 = var67;
                           var8 = var31;
                           var9 = var40;
                           var10 = var35;
                           var11 = var43 % 2;
                        }
                     }
                  }
               }
            }
         }
      }

      int var32 = var8;
      int var34 = var9;
      int var16 = var10;
      int var36 = var11 % 2;
      int var38 = 1 - var36;
      if (var11 % 4 >= 2) {
         var36 = -var36;
         var38 = -var38;
      }

      if (var3 < 0.0D) {
         var9 = MathHelper.clamp(var9, 70, this.worldServerInstance.getActualHeight() - 10);
         var34 = var9;

         for(int var19 = -1; var19 <= 1; ++var19) {
            for(int var41 = 1; var41 < 3; ++var41) {
               for(int var44 = -1; var44 < 3; ++var44) {
                  int var48 = var32 + (var41 - 1) * var36 + var19 * var38;
                  int var52 = var34 + var44;
                  int var58 = var16 + (var41 - 1) * var38 - var19 * var36;
                  boolean var62 = var44 < 0;
                  this.worldServerInstance.setBlockState(new BlockPos(var48, var52, var58), var62 ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState());
               }
            }
         }
      }

      IBlockState var39 = Blocks.PORTAL.getDefaultState().withProperty(BlockPortal.AXIS, var36 == 0 ? EnumFacing.Axis.Z : EnumFacing.Axis.X);

      for(int var42 = 0; var42 < 4; ++var42) {
         for(int var45 = 0; var45 < 4; ++var45) {
            for(int var49 = -1; var49 < 4; ++var49) {
               int var53 = var32 + (var45 - 1) * var36;
               int var59 = var34 + var49;
               int var63 = var16 + (var45 - 1) * var38;
               boolean var68 = var45 == 0 || var45 == 3 || var49 == -1 || var49 == 3;
               this.worldServerInstance.setBlockState(new BlockPos(var53, var59, var63), var68 ? Blocks.OBSIDIAN.getDefaultState() : var39, 2);
            }
         }

         for(int var46 = 0; var46 < 4; ++var46) {
            for(int var50 = -1; var50 < 4; ++var50) {
               int var54 = var32 + (var46 - 1) * var36;
               int var60 = var34 + var50;
               int var64 = var16 + (var46 - 1) * var38;
               BlockPos var69 = new BlockPos(var54, var60, var64);
               this.worldServerInstance.notifyNeighborsOfStateChange(var69, this.worldServerInstance.getBlockState(var69).getBlock());
            }
         }
      }

      return true;
   }

   public void removeStalePortalLocations(long var1) {
      if (var1 % 100L == 0L) {
         long var3 = var1 - 300L;
         ObjectIterator var5 = this.destinationCoordinateCache.values().iterator();

         while(var5.hasNext()) {
            Teleporter.PortalPosition var6 = (Teleporter.PortalPosition)var5.next();
            if (var6 == null || var6.lastUpdateTime < var3) {
               var5.remove();
            }
         }
      }

   }

   public class PortalPosition extends BlockPos {
      public long lastUpdateTime;

      public PortalPosition(BlockPos var2, long var3) {
         super(var2.getX(), var2.getY(), var2.getZ());
         this.lastUpdateTime = var3;
      }
   }
}
