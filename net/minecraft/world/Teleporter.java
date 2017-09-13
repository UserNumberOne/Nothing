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

   public Teleporter(WorldServer worldserver) {
      this.worldServerInstance = worldserver;
      this.random = new Random(worldserver.getSeed());
   }

   public void placeInPortal(Entity entity, float f) {
      if (this.worldServerInstance.provider.getDimensionType().getId() != 1) {
         if (!this.placeInExistingPortal(entity, f)) {
            this.makePortal(entity);
            this.placeInExistingPortal(entity, f);
         }
      } else {
         MathHelper.floor(entity.posX);
         MathHelper.floor(entity.posY);
         MathHelper.floor(entity.posZ);
         BlockPos created = this.createEndPortal(entity.posX, entity.posY, entity.posZ);
         entity.setLocationAndAngles((double)created.getX(), (double)created.getY(), (double)created.getZ(), entity.rotationYaw, 0.0F);
         entity.motionX = entity.motionY = entity.motionZ = 0.0D;
      }

   }

   private BlockPos createEndPortal(double x, double y, double z) {
      int i = MathHelper.floor(x);
      int j = MathHelper.floor(y) - 1;
      int k = MathHelper.floor(z);

      for(int l = -2; l <= 2; ++l) {
         for(int i1 = -2; i1 <= 2; ++i1) {
            for(int j1 = -1; j1 < 3; ++j1) {
               int k1 = i + i1 * 1 + l * 0;
               int l1 = j + j1;
               int i2 = k + i1 * 0 - l * 1;
               boolean flag2 = j1 < 0;
               this.worldServerInstance.setBlockState(new BlockPos(k1, l1, i2), flag2 ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState());
            }
         }
      }

      return new BlockPos(i, k, k);
   }

   private BlockPos findEndPortal(BlockPos portal) {
      int i = portal.getX();
      int j = portal.getY() - 1;
      int k = portal.getZ();
      byte b0 = 1;
      byte b1 = 0;

      for(int l = -2; l <= 2; ++l) {
         for(int i1 = -2; i1 <= 2; ++i1) {
            for(int j1 = -1; j1 < 3; ++j1) {
               int k1 = i + i1 * b0 + l * b1;
               int l1 = j + j1;
               int i2 = k + i1 * b1 - l * b0;
               boolean flag = j1 < 0;
               if (this.worldServerInstance.getBlockState(new BlockPos(k1, l1, i2)).getBlock() != (flag ? Blocks.OBSIDIAN : Blocks.AIR)) {
                  return null;
               }
            }
         }
      }

      return new BlockPos(i, j, k);
   }

   public boolean placeInExistingPortal(Entity entity, float f) {
      BlockPos found = this.findPortal(entity.posX, entity.posY, entity.posZ, 128);
      if (found == null) {
         return false;
      } else {
         Location exit = new Location(this.worldServerInstance.getWorld(), (double)found.getX(), (double)found.getY(), (double)found.getZ(), f, entity.rotationPitch);
         Vector velocity = entity.getBukkitEntity().getVelocity();
         this.adjustExit(entity, exit, velocity);
         entity.setLocationAndAngles(exit.getX(), exit.getY(), exit.getZ(), exit.getYaw(), exit.getPitch());
         if (entity.motionX != velocity.getX() || entity.motionY != velocity.getY() || entity.motionZ != velocity.getZ()) {
            entity.getBukkitEntity().setVelocity(velocity);
         }

         return true;
      }
   }

   public BlockPos findPortal(double x, double y, double z, int radius) {
      if (this.worldServerInstance.getWorld().getEnvironment() == Environment.THE_END) {
         return this.findEndPortal(this.worldServerInstance.provider.getSpawnCoordinate());
      } else {
         double d0 = -1.0D;
         int i = MathHelper.floor(x);
         int j = MathHelper.floor(z);
         boolean flag1 = true;
         Object object = BlockPos.ORIGIN;
         long k = ChunkPos.asLong(i, j);
         if (this.destinationCoordinateCache.containsKey(k)) {
            Teleporter.PortalPosition portaltravelagent_chunkcoordinatesportal = (Teleporter.PortalPosition)this.destinationCoordinateCache.get(k);
            d0 = 0.0D;
            object = portaltravelagent_chunkcoordinatesportal;
            portaltravelagent_chunkcoordinatesportal.lastUpdateTime = this.worldServerInstance.getTotalWorldTime();
            flag1 = false;
         } else {
            BlockPos blockposition = new BlockPos(x, y, z);

            for(int l = -radius; l <= radius; ++l) {
               BlockPos blockposition1;
               for(int i1 = -radius; i1 <= radius; ++i1) {
                  for(BlockPos blockposition2 = blockposition.add(l, this.worldServerInstance.getActualHeight() - 1 - blockposition.getY(), i1); blockposition2.getY() >= 0; blockposition2 = blockposition1) {
                     blockposition1 = blockposition2.down();
                     if (this.worldServerInstance.getBlockState(blockposition2).getBlock() == Blocks.PORTAL) {
                        for(blockposition1 = blockposition2.down(); this.worldServerInstance.getBlockState(blockposition1).getBlock() == Blocks.PORTAL; blockposition1 = blockposition1.down()) {
                           blockposition2 = blockposition1;
                        }

                        double d1 = blockposition2.distanceSq(blockposition);
                        if (d0 < 0.0D || d1 < d0) {
                           d0 = d1;
                           object = blockposition2;
                        }
                     }
                  }
               }
            }
         }

         if (d0 >= 0.0D) {
            if (flag1) {
               this.destinationCoordinateCache.put(k, new Teleporter.PortalPosition((BlockPos)object, this.worldServerInstance.getTotalWorldTime()));
            }

            return (BlockPos)object;
         } else {
            return null;
         }
      }
   }

   public void adjustExit(Entity entity, Location position, Vector velocity) {
      Location from = position.clone();
      Vector before = velocity.clone();
      BlockPos object = new BlockPos(position.getBlockX(), position.getBlockY(), position.getBlockZ());
      float f = position.getYaw();
      if (this.worldServerInstance.getWorld().getEnvironment() != Environment.THE_END && entity.getBukkitEntity().getWorld().getEnvironment() != Environment.THE_END && entity.getLastPortalVec() != null) {
         double d2 = (double)object.getX() + 0.5D;
         double d3 = (double)object.getZ() + 0.5D;
         BlockPattern.PatternHelper shapedetector_shapedetectorcollection = Blocks.PORTAL.createPatternHelper(this.worldServerInstance, object);
         boolean flag2 = shapedetector_shapedetectorcollection.getForwards().rotateY().getAxisDirection() == EnumFacing.AxisDirection.NEGATIVE;
         double d4 = shapedetector_shapedetectorcollection.getForwards().getAxis() == EnumFacing.Axis.X ? (double)shapedetector_shapedetectorcollection.getFrontTopLeft().getZ() : (double)shapedetector_shapedetectorcollection.getFrontTopLeft().getX();
         double d5 = (double)(shapedetector_shapedetectorcollection.getFrontTopLeft().getY() + 1) - entity.getLastPortalVec().yCoord * (double)shapedetector_shapedetectorcollection.getHeight();
         if (flag2) {
            ++d4;
         }

         if (shapedetector_shapedetectorcollection.getForwards().getAxis() == EnumFacing.Axis.X) {
            d3 = d4 + (1.0D - entity.getLastPortalVec().xCoord) * (double)shapedetector_shapedetectorcollection.getWidth() * (double)shapedetector_shapedetectorcollection.getForwards().rotateY().getAxisDirection().getOffset();
         } else {
            d2 = d4 + (1.0D - entity.getLastPortalVec().xCoord) * (double)shapedetector_shapedetectorcollection.getWidth() * (double)shapedetector_shapedetectorcollection.getForwards().rotateY().getAxisDirection().getOffset();
         }

         float f1 = 0.0F;
         float f2 = 0.0F;
         float f3 = 0.0F;
         float f4 = 0.0F;
         if (shapedetector_shapedetectorcollection.getForwards().getOpposite() == entity.getTeleportDirection()) {
            f1 = 1.0F;
            f2 = 1.0F;
         } else if (shapedetector_shapedetectorcollection.getForwards().getOpposite() == entity.getTeleportDirection().getOpposite()) {
            f1 = -1.0F;
            f2 = -1.0F;
         } else if (shapedetector_shapedetectorcollection.getForwards().getOpposite() == entity.getTeleportDirection().rotateY()) {
            f3 = 1.0F;
            f4 = -1.0F;
         } else {
            f3 = -1.0F;
            f4 = 1.0F;
         }

         double d6 = velocity.getX();
         double d7 = velocity.getZ();
         velocity.setX(d6 * (double)f1 + d7 * (double)f4);
         velocity.setZ(d6 * (double)f3 + d7 * (double)f2);
         f = f - (float)(entity.getTeleportDirection().getOpposite().getHorizontalIndex() * 90) + (float)(shapedetector_shapedetectorcollection.getForwards().getHorizontalIndex() * 90);
         position.setX(d2);
         position.setY(d5);
         position.setZ(d3);
         position.setYaw(f);
      } else {
         position.setPitch(0.0F);
         velocity.setX(0);
         velocity.setY(0);
         velocity.setZ(0);
      }

      EntityPortalExitEvent event = new EntityPortalExitEvent(entity.getBukkitEntity(), from, position, before, velocity);
      this.worldServerInstance.getServer().getPluginManager().callEvent(event);
      Location to = event.getTo();
      if (!event.isCancelled() && to != null && entity.isEntityAlive()) {
         position.setX(to.getX());
         position.setY(to.getY());
         position.setZ(to.getZ());
         position.setYaw(to.getYaw());
         position.setPitch(to.getPitch());
         velocity.copy(event.getAfter());
      } else {
         position.setX(from.getX());
         position.setY(from.getY());
         position.setZ(from.getZ());
         position.setYaw(from.getYaw());
         position.setPitch(from.getPitch());
         velocity.copy(before);
      }

   }

   public boolean makePortal(Entity entity) {
      return this.createPortal(entity.posX, entity.posY, entity.posZ, 16);
   }

   public boolean createPortal(double x, double y, double z, int b0) {
      if (this.worldServerInstance.getWorld().getEnvironment() == Environment.THE_END) {
         this.createEndPortal(x, y, z);
         return true;
      } else {
         double d0 = -1.0D;
         int i = MathHelper.floor(x);
         int j = MathHelper.floor(y);
         int k = MathHelper.floor(z);
         int l = i;
         int i1 = j;
         int j1 = k;
         int k1 = 0;
         int l1 = this.random.nextInt(4);
         BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos();

         for(int i2 = i - 16; i2 <= i + 16; ++i2) {
            double d1 = (double)i2 + 0.5D - x;

            for(int j2 = k - 16; j2 <= k + 16; ++j2) {
               double d2 = (double)j2 + 0.5D - z;

               label297:
               for(int k2 = this.worldServerInstance.getActualHeight() - 1; k2 >= 0; --k2) {
                  if (this.worldServerInstance.isAirBlock(blockposition_mutableblockposition.setPos(i2, k2, j2))) {
                     while(k2 > 0 && this.worldServerInstance.isAirBlock(blockposition_mutableblockposition.setPos(i2, k2 - 1, j2))) {
                        --k2;
                     }

                     for(int l2 = l1; l2 < l1 + 4; ++l2) {
                        int i3 = l2 % 2;
                        int j3 = 1 - i3;
                        if (l2 % 4 >= 2) {
                           i3 = -i3;
                           j3 = -j3;
                        }

                        for(int k3 = 0; k3 < 3; ++k3) {
                           for(int l3 = 0; l3 < 4; ++l3) {
                              for(int i4 = -1; i4 < 4; ++i4) {
                                 int j4 = i2 + (l3 - 1) * i3 + k3 * j3;
                                 int k4 = k2 + i4;
                                 int l4 = j2 + (l3 - 1) * j3 - k3 * i3;
                                 blockposition_mutableblockposition.setPos(j4, k4, l4);
                                 if (i4 < 0 && !this.worldServerInstance.getBlockState(blockposition_mutableblockposition).getMaterial().isSolid() || i4 >= 0 && !this.worldServerInstance.isAirBlock(blockposition_mutableblockposition)) {
                                    continue label297;
                                 }
                              }
                           }
                        }

                        double d3 = (double)k2 + 0.5D - y;
                        double d4 = d1 * d1 + d3 * d3 + d2 * d2;
                        if (d0 < 0.0D || d4 < d0) {
                           d0 = d4;
                           l = i2;
                           i1 = k2;
                           j1 = j2;
                           k1 = l2 % 4;
                        }
                     }
                  }
               }
            }
         }

         if (d0 < 0.0D) {
            for(int var45 = i - 16; var45 <= i + 16; ++var45) {
               double d1 = (double)var45 + 0.5D - x;

               for(int j2 = k - 16; j2 <= k + 16; ++j2) {
                  double d2 = (double)j2 + 0.5D - z;

                  label235:
                  for(int k2 = this.worldServerInstance.getActualHeight() - 1; k2 >= 0; --k2) {
                     if (this.worldServerInstance.isAirBlock(blockposition_mutableblockposition.setPos(var45, k2, j2))) {
                        while(k2 > 0 && this.worldServerInstance.isAirBlock(blockposition_mutableblockposition.setPos(var45, k2 - 1, j2))) {
                           --k2;
                        }

                        for(int l2 = l1; l2 < l1 + 2; ++l2) {
                           int i3 = l2 % 2;
                           int j3 = 1 - i3;

                           for(int k3 = 0; k3 < 4; ++k3) {
                              for(int l3 = -1; l3 < 4; ++l3) {
                                 int i4 = var45 + (k3 - 1) * i3;
                                 int j4 = k2 + l3;
                                 int k4 = j2 + (k3 - 1) * j3;
                                 blockposition_mutableblockposition.setPos(i4, j4, k4);
                                 if (l3 < 0 && !this.worldServerInstance.getBlockState(blockposition_mutableblockposition).getMaterial().isSolid() || l3 >= 0 && !this.worldServerInstance.isAirBlock(blockposition_mutableblockposition)) {
                                    continue label235;
                                 }
                              }
                           }

                           double d3 = (double)k2 + 0.5D - y;
                           double d4 = d1 * d1 + d3 * d3 + d2 * d2;
                           if (d0 < 0.0D || d4 < d0) {
                              d0 = d4;
                              l = var45;
                              i1 = k2;
                              j1 = j2;
                              k1 = l2 % 2;
                           }
                        }
                     }
                  }
               }
            }
         }

         int i5 = l;
         int j5 = i1;
         int j2 = j1;
         int k5 = k1 % 2;
         int l5 = 1 - k5;
         if (k1 % 4 >= 2) {
            k5 = -k5;
            l5 = -l5;
         }

         if (d0 < 0.0D) {
            i1 = MathHelper.clamp(i1, 70, this.worldServerInstance.getActualHeight() - 10);
            j5 = i1;

            for(int k2 = -1; k2 <= 1; ++k2) {
               for(int l2 = 1; l2 < 3; ++l2) {
                  for(int i3 = -1; i3 < 3; ++i3) {
                     int j3 = i5 + (l2 - 1) * k5 + k2 * l5;
                     int k3 = j5 + i3;
                     int l3 = j2 + (l2 - 1) * l5 - k2 * k5;
                     boolean flag1 = i3 < 0;
                     this.worldServerInstance.setBlockState(new BlockPos(j3, k3, l3), flag1 ? Blocks.OBSIDIAN.getDefaultState() : Blocks.AIR.getDefaultState());
                  }
               }
            }
         }

         IBlockState iblockdata = Blocks.PORTAL.getDefaultState().withProperty(BlockPortal.AXIS, k5 == 0 ? EnumFacing.Axis.Z : EnumFacing.Axis.X);

         for(int l2 = 0; l2 < 4; ++l2) {
            for(int i3 = 0; i3 < 4; ++i3) {
               for(int j3 = -1; j3 < 4; ++j3) {
                  int k3 = i5 + (i3 - 1) * k5;
                  int l3 = j5 + j3;
                  int i4 = j2 + (i3 - 1) * l5;
                  boolean flag2 = i3 == 0 || i3 == 3 || j3 == -1 || j3 == 3;
                  this.worldServerInstance.setBlockState(new BlockPos(k3, l3, i4), flag2 ? Blocks.OBSIDIAN.getDefaultState() : iblockdata, 2);
               }
            }

            for(int var58 = 0; var58 < 4; ++var58) {
               for(int j3 = -1; j3 < 4; ++j3) {
                  int k3 = i5 + (var58 - 1) * k5;
                  int l3 = j5 + j3;
                  int i4 = j2 + (var58 - 1) * l5;
                  BlockPos blockposition = new BlockPos(k3, l3, i4);
                  this.worldServerInstance.notifyNeighborsOfStateChange(blockposition, this.worldServerInstance.getBlockState(blockposition).getBlock());
               }
            }
         }

         return true;
      }
   }

   public void removeStalePortalLocations(long i) {
      if (i % 100L == 0L) {
         long j = i - 300L;
         ObjectIterator objectiterator = this.destinationCoordinateCache.values().iterator();

         while(objectiterator.hasNext()) {
            Teleporter.PortalPosition portaltravelagent_chunkcoordinatesportal = (Teleporter.PortalPosition)objectiterator.next();
            if (portaltravelagent_chunkcoordinatesportal == null || portaltravelagent_chunkcoordinatesportal.lastUpdateTime < j) {
               objectiterator.remove();
            }
         }
      }

   }

   public class PortalPosition extends BlockPos {
      public long lastUpdateTime;

      public PortalPosition(BlockPos blockposition, long i) {
         super(blockposition.getX(), blockposition.getY(), blockposition.getZ());
         this.lastUpdateTime = i;
      }

      public int compareTo(Vec3i o) {
         return this.compareTo(o);
      }
   }
}
