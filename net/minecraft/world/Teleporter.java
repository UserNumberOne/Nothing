package net.minecraft.world;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import java.util.Random;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.event.entity.EntityPortalExitEvent;
import org.bukkit.util.Vector;

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
         MathHelper.floor(var1.posX);
         MathHelper.floor(var1.posY);
         MathHelper.floor(var1.posZ);
         BlockPos var3 = this.createEndPortal(var1.posX, var1.posY, var1.posZ);
         var1.setLocationAndAngles((double)var3.getX(), (double)var3.getY(), (double)var3.getZ(), var1.rotationYaw, 0.0F);
         var1.motionX = var1.motionY = var1.motionZ = 0.0D;
      }

   }

   private BlockPos createEndPortal(double var1, double var3, double var5) {
      int var7 = MathHelper.floor(var1);
      int var8 = MathHelper.floor(var3) - 1;
      int var9 = MathHelper.floor(var5);

      for(int var10 = -2; var10 <= 2; ++var10) {
         for(int var11 = -2; var11 <= 2; ++var11) {
            for(int var12 = -1; var12 < 3; ++var12) {
               int var13 = var7 + var11 * 1 + var10 * 0;
               int var14 = var8 + var12;
               int var15 = var9 + var11 * 0 - var10 * 1;
               boolean var16 = var12 < 0;
               this.worldServerInstance.setBlockState(new BlockPos(var13, var14, var15), var16 ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState());
            }
         }
      }

      return new BlockPos(var7, var9, var9);
   }

   private BlockPos findEndPortal(BlockPos var1) {
      int var2 = var1.getX();
      int var3 = var1.getY() - 1;
      int var4 = var1.getZ();
      byte var5 = 1;
      byte var6 = 0;

      for(int var7 = -2; var7 <= 2; ++var7) {
         for(int var8 = -2; var8 <= 2; ++var8) {
            for(int var9 = -1; var9 < 3; ++var9) {
               int var10 = var2 + var8 * var5 + var7 * var6;
               int var11 = var3 + var9;
               int var12 = var4 + var8 * var6 - var7 * var5;
               boolean var13 = var9 < 0;
               if (this.worldServerInstance.getBlockState(new BlockPos(var10, var11, var12)).getBlock() != (var13 ? Blocks.OBSIDIAN : Blocks.AIR)) {
                  return null;
               }
            }
         }
      }

      return new BlockPos(var2, var3, var4);
   }

   public boolean placeInExistingPortal(Entity var1, float var2) {
      BlockPos var3 = this.findPortal(var1.posX, var1.posY, var1.posZ, 128);
      if (var3 == null) {
         return false;
      } else {
         Location var4 = new Location(this.worldServerInstance.getWorld(), (double)var3.getX(), (double)var3.getY(), (double)var3.getZ(), var2, var1.rotationPitch);
         Vector var5 = var1.getBukkitEntity().getVelocity();
         this.adjustExit(var1, var4, var5);
         var1.setLocationAndAngles(var4.getX(), var4.getY(), var4.getZ(), var4.getYaw(), var4.getPitch());
         if (var1.motionX != var5.getX() || var1.motionY != var5.getY() || var1.motionZ != var5.getZ()) {
            var1.getBukkitEntity().setVelocity(var5);
         }

         return true;
      }
   }

   public BlockPos findPortal(double var1, double var3, double var5, int var7) {
      if (this.worldServerInstance.getWorld().getEnvironment() == Environment.THE_END) {
         return this.findEndPortal(this.worldServerInstance.provider.getSpawnCoordinate());
      } else {
         double var8 = -1.0D;
         int var10 = MathHelper.floor(var1);
         int var11 = MathHelper.floor(var5);
         boolean var12 = true;
         Object var13 = BlockPos.ORIGIN;
         long var14 = ChunkPos.asLong(var10, var11);
         if (this.destinationCoordinateCache.containsKey(var14)) {
            Teleporter.PortalPosition var16 = (Teleporter.PortalPosition)this.destinationCoordinateCache.get(var14);
            var8 = 0.0D;
            var13 = var16;
            var16.lastUpdateTime = this.worldServerInstance.getTotalWorldTime();
            var12 = false;
         } else {
            BlockPos var23 = new BlockPos(var1, var3, var5);

            for(int var17 = -var7; var17 <= var7; ++var17) {
               BlockPos var20;
               for(int var18 = -var7; var18 <= var7; ++var18) {
                  for(BlockPos var19 = var23.add(var17, this.worldServerInstance.getActualHeight() - 1 - var23.getY(), var18); var19.getY() >= 0; var19 = var20) {
                     var20 = var19.down();
                     if (this.worldServerInstance.getBlockState(var19).getBlock() == Blocks.PORTAL) {
                        for(var20 = var19.down(); this.worldServerInstance.getBlockState(var20).getBlock() == Blocks.PORTAL; var20 = var20.down()) {
                           var19 = var20;
                        }

                        double var21 = var19.distanceSq(var23);
                        if (var8 < 0.0D || var21 < var8) {
                           var8 = var21;
                           var13 = var19;
                        }
                     }
                  }
               }
            }
         }

         if (var8 >= 0.0D) {
            if (var12) {
               this.destinationCoordinateCache.put(var14, new Teleporter.PortalPosition((BlockPos)var13, this.worldServerInstance.getTotalWorldTime()));
            }

            return (BlockPos)var13;
         } else {
            return null;
         }
      }
   }

   public void adjustExit(Entity var1, Location var2, Vector var3) {
      Location var4 = var2.clone();
      Vector var5 = var3.clone();
      BlockPos var6 = new BlockPos(var2.getBlockX(), var2.getBlockY(), var2.getBlockZ());
      float var7 = var2.getYaw();
      if (this.worldServerInstance.getWorld().getEnvironment() != Environment.THE_END && var1.getBukkitEntity().getWorld().getEnvironment() != Environment.THE_END && var1.getLastPortalVec() != null) {
         double var8 = (double)var6.getX() + 0.5D;
         double var10 = (double)var6.getZ() + 0.5D;
         BlockPattern.PatternHelper var12 = Blocks.PORTAL.createPatternHelper(this.worldServerInstance, var6);
         boolean var13 = var12.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
         double var14 = var12.getForwards().getAxis() == EnumFacing.Axis.X ? (double)var12.getFrontTopLeft().getZ() : (double)var12.getFrontTopLeft().getX();
         double var16 = (double)(var12.getFrontTopLeft().getY() + 1) - var1.getLastPortalVec().yCoord * (double)var12.getHeight();
         if (var13) {
            ++var14;
         }

         if (var12.getForwards().getAxis() == EnumFacing.Axis.X) {
            var10 = var14 + (1.0D - var1.getLastPortalVec().xCoord) * (double)var12.getWidth() * (double)var12.getForwards().rotateY().getAxisDirection().getOffset();
         } else {
            var8 = var14 + (1.0D - var1.getLastPortalVec().xCoord) * (double)var12.getWidth() * (double)var12.getForwards().rotateY().getAxisDirection().getOffset();
         }

         float var18 = 0.0F;
         float var19 = 0.0F;
         float var20 = 0.0F;
         float var21 = 0.0F;
         if (var12.getForwards().getOpposite() == var1.getTeleportDirection()) {
            var18 = 1.0F;
            var19 = 1.0F;
         } else if (var12.getForwards().getOpposite() == var1.getTeleportDirection().getOpposite()) {
            var18 = -1.0F;
            var19 = -1.0F;
         } else if (var12.getForwards().getOpposite() == var1.getTeleportDirection().rotateY()) {
            var20 = 1.0F;
            var21 = -1.0F;
         } else {
            var20 = -1.0F;
            var21 = 1.0F;
         }

         double var22 = var3.getX();
         double var24 = var3.getZ();
         var3.setX(var22 * (double)var18 + var24 * (double)var21);
         var3.setZ(var22 * (double)var20 + var24 * (double)var19);
         var7 = var7 - (float)(var1.getTeleportDirection().getOpposite().getHorizontalIndex() * 90) + (float)(var12.getForwards().getHorizontalIndex() * 90);
         var2.setX(var8);
         var2.setY(var16);
         var2.setZ(var10);
         var2.setYaw(var7);
      } else {
         var2.setPitch(0.0F);
         var3.setX(0);
         var3.setY(0);
         var3.setZ(0);
      }

      EntityPortalExitEvent var26 = new EntityPortalExitEvent(var1.getBukkitEntity(), var4, var2, var5, var3);
      this.worldServerInstance.getServer().getPluginManager().callEvent(var26);
      Location var27 = var26.getTo();
      if (!var26.isCancelled() && var27 != null && var1.isEntityAlive()) {
         var2.setX(var27.getX());
         var2.setY(var27.getY());
         var2.setZ(var27.getZ());
         var2.setYaw(var27.getYaw());
         var2.setPitch(var27.getPitch());
         var3.copy(var26.getAfter());
      } else {
         var2.setX(var4.getX());
         var2.setY(var4.getY());
         var2.setZ(var4.getZ());
         var2.setYaw(var4.getYaw());
         var2.setPitch(var4.getPitch());
         var3.copy(var5);
      }

   }

   public boolean makePortal(Entity var1) {
      return this.createPortal(var1.posX, var1.posY, var1.posZ, 16);
   }

   public boolean createPortal(double var1, double var3, double var5, int var7) {
      if (this.worldServerInstance.getWorld().getEnvironment() == Environment.THE_END) {
         this.createEndPortal(var1, var3, var5);
         return true;
      } else {
         double var8 = -1.0D;
         int var10 = MathHelper.floor(var1);
         int var11 = MathHelper.floor(var3);
         int var12 = MathHelper.floor(var5);
         int var13 = var10;
         int var14 = var11;
         int var15 = var12;
         int var16 = 0;
         int var17 = this.random.nextInt(4);
         BlockPos.MutableBlockPos var18 = new BlockPos.MutableBlockPos();

         for(int var19 = var10 - 16; var19 <= var10 + 16; ++var19) {
            double var20 = (double)var19 + 0.5D - var1;

            for(int var22 = var12 - 16; var22 <= var12 + 16; ++var22) {
               double var23 = (double)var22 + 0.5D - var5;

               label297:
               for(int var25 = this.worldServerInstance.getActualHeight() - 1; var25 >= 0; --var25) {
                  if (this.worldServerInstance.isAirBlock(var18.setPos(var19, var25, var22))) {
                     while(var25 > 0 && this.worldServerInstance.isAirBlock(var18.setPos(var19, var25 - 1, var22))) {
                        --var25;
                     }

                     for(int var26 = var17; var26 < var17 + 4; ++var26) {
                        int var27 = var26 % 2;
                        int var28 = 1 - var27;
                        if (var26 % 4 >= 2) {
                           var27 = -var27;
                           var28 = -var28;
                        }

                        for(int var29 = 0; var29 < 3; ++var29) {
                           for(int var30 = 0; var30 < 4; ++var30) {
                              for(int var31 = -1; var31 < 4; ++var31) {
                                 int var32 = var19 + (var30 - 1) * var27 + var29 * var28;
                                 int var33 = var25 + var31;
                                 int var34 = var22 + (var30 - 1) * var28 - var29 * var27;
                                 var18.setPos(var32, var33, var34);
                                 if (var31 < 0 && !this.worldServerInstance.getBlockState(var18).getMaterial().isSolid() || var31 >= 0 && !this.worldServerInstance.isAirBlock(var18)) {
                                    continue label297;
                                 }
                              }
                           }
                        }

                        double var35 = (double)var25 + 0.5D - var3;
                        double var37 = var20 * var20 + var35 * var35 + var23 * var23;
                        if (var8 < 0.0D || var37 < var8) {
                           var8 = var37;
                           var13 = var19;
                           var14 = var25;
                           var15 = var22;
                           var16 = var26 % 4;
                        }
                     }
                  }
               }
            }
         }

         if (var8 < 0.0D) {
            for(int var45 = var10 - 16; var45 <= var10 + 16; ++var45) {
               double var46 = (double)var45 + 0.5D - var1;

               for(int var47 = var12 - 16; var47 <= var12 + 16; ++var47) {
                  double var49 = (double)var47 + 0.5D - var5;

                  label235:
                  for(int var50 = this.worldServerInstance.getActualHeight() - 1; var50 >= 0; --var50) {
                     if (this.worldServerInstance.isAirBlock(var18.setPos(var45, var50, var47))) {
                        while(var50 > 0 && this.worldServerInstance.isAirBlock(var18.setPos(var45, var50 - 1, var47))) {
                           --var50;
                        }

                        for(int var52 = var17; var52 < var17 + 2; ++var52) {
                           int var55 = var52 % 2;
                           int var59 = 1 - var55;

                           for(int var63 = 0; var63 < 4; ++var63) {
                              for(int var67 = -1; var67 < 4; ++var67) {
                                 int var71 = var45 + (var63 - 1) * var55;
                                 int var74 = var50 + var67;
                                 int var75 = var47 + (var63 - 1) * var59;
                                 var18.setPos(var71, var74, var75);
                                 if (var67 < 0 && !this.worldServerInstance.getBlockState(var18).getMaterial().isSolid() || var67 >= 0 && !this.worldServerInstance.isAirBlock(var18)) {
                                    continue label235;
                                 }
                              }
                           }

                           double var77 = (double)var50 + 0.5D - var3;
                           double var78 = var46 * var46 + var77 * var77 + var49 * var49;
                           if (var8 < 0.0D || var78 < var8) {
                              var8 = var78;
                              var13 = var45;
                              var14 = var50;
                              var15 = var47;
                              var16 = var52 % 2;
                           }
                        }
                     }
                  }
               }
            }
         }

         int var76 = var13;
         int var39 = var14;
         int var48 = var15;
         int var40 = var16 % 2;
         int var41 = 1 - var40;
         if (var16 % 4 >= 2) {
            var40 = -var40;
            var41 = -var41;
         }

         if (var8 < 0.0D) {
            var14 = MathHelper.clamp(var14, 70, this.worldServerInstance.getActualHeight() - 10);
            var39 = var14;

            for(int var51 = -1; var51 <= 1; ++var51) {
               for(int var53 = 1; var53 < 3; ++var53) {
                  for(int var56 = -1; var56 < 3; ++var56) {
                     int var60 = var76 + (var53 - 1) * var40 + var51 * var41;
                     int var64 = var39 + var56;
                     int var68 = var48 + (var53 - 1) * var41 - var51 * var40;
                     boolean var42 = var56 < 0;
                     this.worldServerInstance.setBlockState(new BlockPos(var60, var64, var68), var42 ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState());
                  }
               }
            }
         }

         IBlockState var79 = Blocks.PORTAL.getDefaultState().withProperty(BlockPortal.AXIS, var40 == 0 ? EnumFacing.Axis.Z : EnumFacing.Axis.X);

         for(int var54 = 0; var54 < 4; ++var54) {
            for(int var57 = 0; var57 < 4; ++var57) {
               for(int var61 = -1; var61 < 4; ++var61) {
                  int var65 = var76 + (var57 - 1) * var40;
                  int var69 = var39 + var61;
                  int var72 = var48 + (var57 - 1) * var41;
                  boolean var43 = var57 == 0 || var57 == 3 || var61 == -1 || var61 == 3;
                  this.worldServerInstance.setBlockState(new BlockPos(var65, var69, var72), var43 ? Blocks.OBSIDIAN.getDefaultState() : var79, 2);
               }
            }

            for(int var58 = 0; var58 < 4; ++var58) {
               for(int var62 = -1; var62 < 4; ++var62) {
                  int var66 = var76 + (var58 - 1) * var40;
                  int var70 = var39 + var62;
                  int var73 = var48 + (var58 - 1) * var41;
                  BlockPos var80 = new BlockPos(var66, var70, var73);
                  this.worldServerInstance.notifyNeighborsOfStateChange(var80, this.worldServerInstance.getBlockState(var80).getBlock());
               }
            }
         }

         return true;
      }
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

      public int compareTo(Vec3i var1) {
         return this.compareTo(var1);
      }
   }
}
