package net.minecraft.block.state.pattern;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import javax.annotation.Nullable;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class BlockPattern {
   private final Predicate[][][] blockMatches;
   private final int fingerLength;
   private final int thumbLength;
   private final int palmLength;

   public BlockPattern(Predicate[][][] var1) {
      this.blockMatches = var1;
      this.fingerLength = var1.length;
      if (this.fingerLength > 0) {
         this.thumbLength = var1[0].length;
         if (this.thumbLength > 0) {
            this.palmLength = var1[0][0].length;
         } else {
            this.palmLength = 0;
         }
      } else {
         this.thumbLength = 0;
         this.palmLength = 0;
      }

   }

   public int getFingerLength() {
      return this.fingerLength;
   }

   public int getThumbLength() {
      return this.thumbLength;
   }

   public int getPalmLength() {
      return this.palmLength;
   }

   @Nullable
   private BlockPattern.PatternHelper checkPatternAt(BlockPos var1, EnumFacing var2, EnumFacing var3, LoadingCache var4) {
      for(int var5 = 0; var5 < this.palmLength; ++var5) {
         for(int var6 = 0; var6 < this.thumbLength; ++var6) {
            for(int var7 = 0; var7 < this.fingerLength; ++var7) {
               if (!this.blockMatches[var7][var6][var5].apply(var4.getUnchecked(translateOffset(var1, var2, var3, var5, var6, var7)))) {
                  return null;
               }
            }
         }
      }

      return new BlockPattern.PatternHelper(var1, var2, var3, var4, this.palmLength, this.thumbLength, this.fingerLength);
   }

   @Nullable
   public BlockPattern.PatternHelper match(World var1, BlockPos var2) {
      LoadingCache var3 = createLoadingCache(var1, false);
      int var4 = Math.max(Math.max(this.palmLength, this.thumbLength), this.fingerLength);

      for(BlockPos var6 : BlockPos.getAllInBox(var2, var2.add(var4 - 1, var4 - 1, var4 - 1))) {
         for(EnumFacing var10 : EnumFacing.values()) {
            for(EnumFacing var14 : EnumFacing.values()) {
               if (var14 != var10 && var14 != var10.getOpposite()) {
                  BlockPattern.PatternHelper var15 = this.checkPatternAt(var6, var10, var14, var3);
                  if (var15 != null) {
                     return var15;
                  }
               }
            }
         }
      }

      return null;
   }

   public static LoadingCache createLoadingCache(World var0, boolean var1) {
      return CacheBuilder.newBuilder().build(new BlockPattern.CacheLoader(var0, var1));
   }

   protected static BlockPos translateOffset(BlockPos var0, EnumFacing var1, EnumFacing var2, int var3, int var4, int var5) {
      if (var1 != var2 && var1 != var2.getOpposite()) {
         Vec3i var6 = new Vec3i(var1.getFrontOffsetX(), var1.getFrontOffsetY(), var1.getFrontOffsetZ());
         Vec3i var7 = new Vec3i(var2.getFrontOffsetX(), var2.getFrontOffsetY(), var2.getFrontOffsetZ());
         Vec3i var8 = var6.crossProduct(var7);
         return var0.add(var7.getX() * -var4 + var8.getX() * var3 + var6.getX() * var5, var7.getY() * -var4 + var8.getY() * var3 + var6.getY() * var5, var7.getZ() * -var4 + var8.getZ() * var3 + var6.getZ() * var5);
      } else {
         throw new IllegalArgumentException("Invalid forwards & up combination");
      }
   }

   static class CacheLoader extends com.google.common.cache.CacheLoader {
      private final World world;
      private final boolean forceLoad;

      public CacheLoader(World var1, boolean var2) {
         this.world = var1;
         this.forceLoad = var2;
      }

      public BlockWorldState load(BlockPos var1) throws Exception {
         return new BlockWorldState(this.world, var1, this.forceLoad);
      }

      // $FF: synthetic method
      public Object load(Object var1) throws Exception {
         return this.load((BlockPos)var1);
      }
   }

   public static class PatternHelper {
      private final BlockPos frontTopLeft;
      private final EnumFacing forwards;
      private final EnumFacing up;
      private final LoadingCache lcache;
      private final int width;
      private final int height;
      private final int depth;

      public PatternHelper(BlockPos var1, EnumFacing var2, EnumFacing var3, LoadingCache var4, int var5, int var6, int var7) {
         this.frontTopLeft = var1;
         this.forwards = var2;
         this.up = var3;
         this.lcache = var4;
         this.width = var5;
         this.height = var6;
         this.depth = var7;
      }

      public BlockPos getFrontTopLeft() {
         return this.frontTopLeft;
      }

      public EnumFacing getForwards() {
         return this.forwards;
      }

      public EnumFacing getUp() {
         return this.up;
      }

      public int getWidth() {
         return this.width;
      }

      public int getHeight() {
         return this.height;
      }

      public BlockWorldState translateOffset(int var1, int var2, int var3) {
         return (BlockWorldState)this.lcache.getUnchecked(BlockPattern.translateOffset(this.frontTopLeft, this.getForwards(), this.getUp(), var1, var2, var3));
      }

      public String toString() {
         return Objects.toStringHelper(this).add("up", this.up).add("forwards", this.forwards).add("frontTopLeft", this.frontTopLeft).toString();
      }
   }
}
