package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Queues;
import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IntegerCache;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class VisGraph {
   private static final int DX = (int)Math.pow(16.0D, 0.0D);
   private static final int DZ = (int)Math.pow(16.0D, 1.0D);
   private static final int DY = (int)Math.pow(16.0D, 2.0D);
   private final BitSet bitSet = new BitSet(4096);
   private static final int[] INDEX_OF_EDGES = new int[1352];
   private int empty = 4096;

   public void setOpaqueCube(BlockPos var1) {
      this.bitSet.set(getIndex(var1), true);
      --this.empty;
   }

   private static int getIndex(BlockPos var0) {
      return getIndex(var0.getX() & 15, var0.getY() & 15, var0.getZ() & 15);
   }

   private static int getIndex(int var0, int var1, int var2) {
      return var0 << 0 | var1 << 8 | var2 << 4;
   }

   public SetVisibility computeVisibility() {
      SetVisibility var1 = new SetVisibility();
      if (4096 - this.empty < 256) {
         var1.setAllVisible(true);
      } else if (this.empty == 0) {
         var1.setAllVisible(false);
      } else {
         for(int var5 : INDEX_OF_EDGES) {
            if (!this.bitSet.get(var5)) {
               var1.setManyVisible(this.floodFill(var5));
            }
         }
      }

      return var1;
   }

   public Set getVisibleFacings(BlockPos var1) {
      return this.floodFill(getIndex(var1));
   }

   private Set floodFill(int var1) {
      EnumSet var2 = EnumSet.noneOf(EnumFacing.class);
      ArrayDeque var3 = Queues.newArrayDeque();
      var3.add(IntegerCache.getInteger(var1));
      this.bitSet.set(var1, true);

      while(!var3.isEmpty()) {
         int var4 = ((Integer)var3.poll()).intValue();
         this.addEdges(var4, var2);

         for(EnumFacing var8 : EnumFacing.values()) {
            int var9 = this.getNeighborIndexAtFace(var4, var8);
            if (var9 >= 0 && !this.bitSet.get(var9)) {
               this.bitSet.set(var9, true);
               var3.add(IntegerCache.getInteger(var9));
            }
         }
      }

      return var2;
   }

   private void addEdges(int var1, Set var2) {
      int var3 = var1 >> 0 & 15;
      if (var3 == 0) {
         var2.add(EnumFacing.WEST);
      } else if (var3 == 15) {
         var2.add(EnumFacing.EAST);
      }

      int var4 = var1 >> 8 & 15;
      if (var4 == 0) {
         var2.add(EnumFacing.DOWN);
      } else if (var4 == 15) {
         var2.add(EnumFacing.UP);
      }

      int var5 = var1 >> 4 & 15;
      if (var5 == 0) {
         var2.add(EnumFacing.NORTH);
      } else if (var5 == 15) {
         var2.add(EnumFacing.SOUTH);
      }

   }

   private int getNeighborIndexAtFace(int var1, EnumFacing var2) {
      switch(var2) {
      case DOWN:
         if ((var1 >> 8 & 15) == 0) {
            return -1;
         }

         return var1 - DY;
      case UP:
         if ((var1 >> 8 & 15) == 15) {
            return -1;
         }

         return var1 + DY;
      case NORTH:
         if ((var1 >> 4 & 15) == 0) {
            return -1;
         }

         return var1 - DZ;
      case SOUTH:
         if ((var1 >> 4 & 15) == 15) {
            return -1;
         }

         return var1 + DZ;
      case WEST:
         if ((var1 >> 0 & 15) == 0) {
            return -1;
         }

         return var1 - DX;
      case EAST:
         if ((var1 >> 0 & 15) == 15) {
            return -1;
         }

         return var1 + DX;
      default:
         return -1;
      }
   }

   static {
      boolean var0 = false;
      boolean var1 = true;
      int var2 = 0;

      for(int var3 = 0; var3 < 16; ++var3) {
         for(int var4 = 0; var4 < 16; ++var4) {
            for(int var5 = 0; var5 < 16; ++var5) {
               if (var3 == 0 || var3 == 15 || var4 == 0 || var4 == 15 || var5 == 0 || var5 == 15) {
                  INDEX_OF_EDGES[var2++] = getIndex(var3, var4, var5);
               }
            }
         }
      }

   }
}
