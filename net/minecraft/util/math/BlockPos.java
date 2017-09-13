package net.minecraft.util.math;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import java.util.Iterator;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Immutable
public class BlockPos extends Vec3i {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final BlockPos ORIGIN = new BlockPos(0, 0, 0);
   private static final int NUM_X_BITS = 1 + MathHelper.log2(MathHelper.smallestEncompassingPowerOfTwo(30000000));
   private static final int NUM_Z_BITS = NUM_X_BITS;
   private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_Z_BITS;
   private static final int Y_SHIFT = 0 + NUM_Z_BITS;
   private static final int X_SHIFT = Y_SHIFT + NUM_Y_BITS;
   private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
   private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;
   private static final long Z_MASK = (1L << NUM_Z_BITS) - 1L;

   public BlockPos(int var1, int var2, int var3) {
      super(var1, var2, var3);
   }

   public BlockPos(double var1, double var3, double var5) {
      super(var1, var3, var5);
   }

   public BlockPos(Entity var1) {
      this(var1.posX, var1.posY, var1.posZ);
   }

   public BlockPos(Vec3d var1) {
      this(var1.xCoord, var1.yCoord, var1.zCoord);
   }

   public BlockPos(Vec3i var1) {
      this(var1.getX(), var1.getY(), var1.getZ());
   }

   public BlockPos add(double var1, double var3, double var5) {
      return var1 == 0.0D && var3 == 0.0D && var5 == 0.0D ? this : new BlockPos((double)this.getX() + var1, (double)this.getY() + var3, (double)this.getZ() + var5);
   }

   public BlockPos add(int var1, int var2, int var3) {
      return var1 == 0 && var2 == 0 && var3 == 0 ? this : new BlockPos(this.getX() + var1, this.getY() + var2, this.getZ() + var3);
   }

   public BlockPos add(Vec3i var1) {
      return var1.getX() == 0 && var1.getY() == 0 && var1.getZ() == 0 ? this : new BlockPos(this.getX() + var1.getX(), this.getY() + var1.getY(), this.getZ() + var1.getZ());
   }

   public BlockPos subtract(Vec3i var1) {
      return var1.getX() == 0 && var1.getY() == 0 && var1.getZ() == 0 ? this : new BlockPos(this.getX() - var1.getX(), this.getY() - var1.getY(), this.getZ() - var1.getZ());
   }

   public BlockPos up() {
      return this.up(1);
   }

   public BlockPos up(int var1) {
      return this.offset(EnumFacing.UP, var1);
   }

   public BlockPos down() {
      return this.down(1);
   }

   public BlockPos down(int var1) {
      return this.offset(EnumFacing.DOWN, var1);
   }

   public BlockPos north() {
      return this.north(1);
   }

   public BlockPos north(int var1) {
      return this.offset(EnumFacing.NORTH, var1);
   }

   public BlockPos south() {
      return this.south(1);
   }

   public BlockPos south(int var1) {
      return this.offset(EnumFacing.SOUTH, var1);
   }

   public BlockPos west() {
      return this.west(1);
   }

   public BlockPos west(int var1) {
      return this.offset(EnumFacing.WEST, var1);
   }

   public BlockPos east() {
      return this.east(1);
   }

   public BlockPos east(int var1) {
      return this.offset(EnumFacing.EAST, var1);
   }

   public BlockPos offset(EnumFacing var1) {
      return this.offset(var1, 1);
   }

   public BlockPos offset(EnumFacing var1, int var2) {
      return var2 == 0 ? this : new BlockPos(this.getX() + var1.getFrontOffsetX() * var2, this.getY() + var1.getFrontOffsetY() * var2, this.getZ() + var1.getFrontOffsetZ() * var2);
   }

   public BlockPos crossProduct(Vec3i var1) {
      return new BlockPos(this.getY() * var1.getZ() - this.getZ() * var1.getY(), this.getZ() * var1.getX() - this.getX() * var1.getZ(), this.getX() * var1.getY() - this.getY() * var1.getX());
   }

   public long toLong() {
      return ((long)this.getX() & X_MASK) << X_SHIFT | ((long)this.getY() & Y_MASK) << Y_SHIFT | ((long)this.getZ() & Z_MASK) << 0;
   }

   public static BlockPos fromLong(long var0) {
      int var2 = (int)(var0 << 64 - X_SHIFT - NUM_X_BITS >> 64 - NUM_X_BITS);
      int var3 = (int)(var0 << 64 - Y_SHIFT - NUM_Y_BITS >> 64 - NUM_Y_BITS);
      int var4 = (int)(var0 << 64 - NUM_Z_BITS >> 64 - NUM_Z_BITS);
      return new BlockPos(var2, var3, var4);
   }

   public static Iterable getAllInBox(BlockPos var0, BlockPos var1) {
      final BlockPos var2 = new BlockPos(Math.min(var0.getX(), var1.getX()), Math.min(var0.getY(), var1.getY()), Math.min(var0.getZ(), var1.getZ()));
      final BlockPos var3 = new BlockPos(Math.max(var0.getX(), var1.getX()), Math.max(var0.getY(), var1.getY()), Math.max(var0.getZ(), var1.getZ()));
      return new Iterable() {
         public Iterator iterator() {
            return new AbstractIterator() {
               private BlockPos lastReturned;

               protected BlockPos computeNext() {
                  if (this.lastReturned == null) {
                     this.lastReturned = var2;
                     return this.lastReturned;
                  } else if (this.lastReturned.equals(var3)) {
                     return (BlockPos)this.endOfData();
                  } else {
                     int var1 = this.lastReturned.getX();
                     int var2x = this.lastReturned.getY();
                     int var3x = this.lastReturned.getZ();
                     if (var1 < var3.getX()) {
                        ++var1;
                     } else if (var2x < var3.getY()) {
                        var1 = var2.getX();
                        ++var2x;
                     } else if (var3x < var3.getZ()) {
                        var1 = var2.getX();
                        var2x = var2.getY();
                        ++var3x;
                     }

                     this.lastReturned = new BlockPos(var1, var2x, var3x);
                     return this.lastReturned;
                  }
               }
            };
         }
      };
   }

   public BlockPos toImmutable() {
      return this;
   }

   public static Iterable getAllInBoxMutable(BlockPos var0, BlockPos var1) {
      final BlockPos var2 = new BlockPos(Math.min(var0.getX(), var1.getX()), Math.min(var0.getY(), var1.getY()), Math.min(var0.getZ(), var1.getZ()));
      final BlockPos var3 = new BlockPos(Math.max(var0.getX(), var1.getX()), Math.max(var0.getY(), var1.getY()), Math.max(var0.getZ(), var1.getZ()));
      return new Iterable() {
         public Iterator iterator() {
            return new AbstractIterator() {
               private BlockPos.MutableBlockPos theBlockPos;

               protected BlockPos.MutableBlockPos computeNext() {
                  if (this.theBlockPos == null) {
                     this.theBlockPos = new BlockPos.MutableBlockPos(var2.getX(), var2.getY(), var2.getZ());
                     return this.theBlockPos;
                  } else if (this.theBlockPos.equals(var3)) {
                     return (BlockPos.MutableBlockPos)this.endOfData();
                  } else {
                     int var1 = this.theBlockPos.getX();
                     int var2x = this.theBlockPos.getY();
                     int var3x = this.theBlockPos.getZ();
                     if (var1 < var3.getX()) {
                        ++var1;
                     } else if (var2x < var3.getY()) {
                        var1 = var2.getX();
                        ++var2x;
                     } else if (var3x < var3.getZ()) {
                        var1 = var2.getX();
                        var2x = var2.getY();
                        ++var3x;
                     }

                     this.theBlockPos.x = var1;
                     this.theBlockPos.y = var2x;
                     this.theBlockPos.z = var3x;
                     return this.theBlockPos;
                  }
               }
            };
         }
      };
   }

   public static class MutableBlockPos extends BlockPos {
      protected int x;
      protected int y;
      protected int z;

      public MutableBlockPos() {
         this(0, 0, 0);
      }

      public MutableBlockPos(BlockPos var1) {
         this(var1.getX(), var1.getY(), var1.getZ());
      }

      public MutableBlockPos(int var1, int var2, int var3) {
         super(0, 0, 0);
         this.x = var1;
         this.y = var2;
         this.z = var3;
      }

      public int getX() {
         return this.x;
      }

      public int getY() {
         return this.y;
      }

      public int getZ() {
         return this.z;
      }

      public BlockPos.MutableBlockPos setPos(int var1, int var2, int var3) {
         this.x = var1;
         this.y = var2;
         this.z = var3;
         return this;
      }

      public BlockPos.MutableBlockPos setPos(double var1, double var3, double var5) {
         return this.setPos(MathHelper.floor(var1), MathHelper.floor(var3), MathHelper.floor(var5));
      }

      @SideOnly(Side.CLIENT)
      public BlockPos.MutableBlockPos setPos(Entity var1) {
         return this.setPos(var1.posX, var1.posY, var1.posZ);
      }

      public BlockPos.MutableBlockPos setPos(Vec3i var1) {
         return this.setPos(var1.getX(), var1.getY(), var1.getZ());
      }

      public BlockPos.MutableBlockPos move(EnumFacing var1) {
         return this.move(var1, 1);
      }

      public BlockPos.MutableBlockPos move(EnumFacing var1, int var2) {
         return this.setPos(this.x + var1.getFrontOffsetX() * var2, this.y + var1.getFrontOffsetY() * var2, this.z + var1.getFrontOffsetZ() * var2);
      }

      public void setY(int var1) {
         this.y = var1;
      }

      public BlockPos toImmutable() {
         return new BlockPos(this);
      }
   }

   public static final class PooledMutableBlockPos extends BlockPos.MutableBlockPos {
      private boolean released;
      private static final List POOL = Lists.newArrayList();

      private PooledMutableBlockPos(int var1, int var2, int var3) {
         super(var1, var2, var3);
      }

      public static BlockPos.PooledMutableBlockPos retain() {
         return retain(0, 0, 0);
      }

      public static BlockPos.PooledMutableBlockPos retain(double var0, double var2, double var4) {
         return retain(MathHelper.floor(var0), MathHelper.floor(var2), MathHelper.floor(var4));
      }

      @SideOnly(Side.CLIENT)
      public static BlockPos.PooledMutableBlockPos retain(Vec3i var0) {
         return retain(var0.getX(), var0.getY(), var0.getZ());
      }

      public static BlockPos.PooledMutableBlockPos retain(int var0, int var1, int var2) {
         synchronized(POOL) {
            if (!POOL.isEmpty()) {
               BlockPos.PooledMutableBlockPos var4 = (BlockPos.PooledMutableBlockPos)POOL.remove(POOL.size() - 1);
               if (var4 != null && var4.released) {
                  var4.released = false;
                  var4.setPos(var0, var1, var2);
                  return var4;
               }
            }
         }

         return new BlockPos.PooledMutableBlockPos(var0, var1, var2);
      }

      public void release() {
         synchronized(POOL) {
            if (POOL.size() < 100) {
               POOL.add(this);
            }

            this.released = true;
         }
      }

      public BlockPos.PooledMutableBlockPos setPos(int var1, int var2, int var3) {
         if (this.released) {
            BlockPos.LOGGER.error("PooledMutableBlockPosition modified after it was released.", new Throwable());
            this.released = false;
         }

         return (BlockPos.PooledMutableBlockPos)super.setPos(var1, var2, var3);
      }

      @SideOnly(Side.CLIENT)
      public BlockPos.PooledMutableBlockPos setPos(Entity var1) {
         return (BlockPos.PooledMutableBlockPos)super.setPos(var1);
      }

      public BlockPos.PooledMutableBlockPos setPos(double var1, double var3, double var5) {
         return (BlockPos.PooledMutableBlockPos)super.setPos(var1, var3, var5);
      }

      public BlockPos.PooledMutableBlockPos setPos(Vec3i var1) {
         return (BlockPos.PooledMutableBlockPos)super.setPos(var1);
      }

      public BlockPos.PooledMutableBlockPos move(EnumFacing var1) {
         return (BlockPos.PooledMutableBlockPos)super.move(var1);
      }

      public BlockPos.PooledMutableBlockPos move(EnumFacing var1, int var2) {
         return (BlockPos.PooledMutableBlockPos)super.move(var1, var2);
      }
   }
}
