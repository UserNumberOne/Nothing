package net.minecraft.client.renderer;

import java.util.BitSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.model.pipeline.LightUtil;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockModelRenderer {
   private final BlockColors blockColors;

   public BlockModelRenderer(BlockColors var1) {
      this.blockColors = var1;
   }

   public boolean renderModel(IBlockAccess var1, IBakedModel var2, IBlockState var3, BlockPos var4, VertexBuffer var5, boolean var6) {
      return this.renderModel(var1, var2, var3, var4, var5, var6, MathHelper.getPositionRandom(var4));
   }

   public boolean renderModel(IBlockAccess var1, IBakedModel var2, IBlockState var3, BlockPos var4, VertexBuffer var5, boolean var6, long var7) {
      boolean var9 = Minecraft.isAmbientOcclusionEnabled() && var3.getLightValue() == 0 && var2.isAmbientOcclusion();

      try {
         return var9 ? this.renderModelSmooth(var1, var2, var3, var4, var5, var6, var7) : this.renderModelFlat(var1, var2, var3, var4, var5, var6, var7);
      } catch (Throwable var13) {
         CrashReport var11 = CrashReport.makeCrashReport(var13, "Tesselating block model");
         CrashReportCategory var12 = var11.makeCategory("Block model being tesselated");
         CrashReportCategory.addBlockInfo(var12, var4, var3);
         var12.addCrashSection("Using AO", Boolean.valueOf(var9));
         throw new ReportedException(var11);
      }
   }

   public boolean renderModelSmooth(IBlockAccess var1, IBakedModel var2, IBlockState var3, BlockPos var4, VertexBuffer var5, boolean var6, long var7) {
      boolean var9 = false;
      float[] var10 = new float[EnumFacing.values().length * 2];
      BitSet var11 = new BitSet(3);
      BlockModelRenderer.AmbientOcclusionFace var12 = new BlockModelRenderer.AmbientOcclusionFace();

      for(EnumFacing var16 : EnumFacing.values()) {
         List var17 = var2.getQuads(var3, var16, var7);
         if (!var17.isEmpty() && (!var6 || var3.shouldSideBeRendered(var1, var4, var16))) {
            this.renderQuadsSmooth(var1, var3, var4, var5, var17, var10, var11, var12);
            var9 = true;
         }
      }

      List var18 = var2.getQuads(var3, (EnumFacing)null, var7);
      if (!var18.isEmpty()) {
         this.renderQuadsSmooth(var1, var3, var4, var5, var18, var10, var11, var12);
         var9 = true;
      }

      return var9;
   }

   public boolean renderModelFlat(IBlockAccess var1, IBakedModel var2, IBlockState var3, BlockPos var4, VertexBuffer var5, boolean var6, long var7) {
      boolean var9 = false;
      BitSet var10 = new BitSet(3);

      for(EnumFacing var14 : EnumFacing.values()) {
         List var15 = var2.getQuads(var3, var14, var7);
         if (!var15.isEmpty() && (!var6 || var3.shouldSideBeRendered(var1, var4, var14))) {
            int var16 = var3.getPackedLightmapCoords(var1, var4.offset(var14));
            this.renderQuadsFlat(var1, var3, var4, var16, false, var5, var15, var10);
            var9 = true;
         }
      }

      List var17 = var2.getQuads(var3, (EnumFacing)null, var7);
      if (!var17.isEmpty()) {
         this.renderQuadsFlat(var1, var3, var4, -1, true, var5, var17, var10);
         var9 = true;
      }

      return var9;
   }

   private void renderQuadsSmooth(IBlockAccess var1, IBlockState var2, BlockPos var3, VertexBuffer var4, List var5, float[] var6, BitSet var7, BlockModelRenderer.AmbientOcclusionFace var8) {
      double var9 = (double)var3.getX();
      double var11 = (double)var3.getY();
      double var13 = (double)var3.getZ();
      Block var15 = var2.getBlock();
      Block.EnumOffsetType var16 = var15.getOffsetType();
      if (var16 != Block.EnumOffsetType.NONE) {
         long var17 = MathHelper.getPositionRandom(var3);
         var9 += ((double)((float)(var17 >> 16 & 15L) / 15.0F) - 0.5D) * 0.5D;
         var13 += ((double)((float)(var17 >> 24 & 15L) / 15.0F) - 0.5D) * 0.5D;
         if (var16 == Block.EnumOffsetType.XYZ) {
            var11 += ((double)((float)(var17 >> 20 & 15L) / 15.0F) - 1.0D) * 0.2D;
         }
      }

      int var24 = 0;

      for(int var18 = var5.size(); var24 < var18; ++var24) {
         BakedQuad var19 = (BakedQuad)var5.get(var24);
         this.fillQuadBounds(var2, var19.getVertexData(), var19.getFace(), var6, var7);
         var8.updateVertexBrightness(var1, var2, var3, var19.getFace(), var6, var7);
         var4.addVertexData(var19.getVertexData());
         var4.putBrightness4(var8.vertexBrightness[0], var8.vertexBrightness[1], var8.vertexBrightness[2], var8.vertexBrightness[3]);
         if (var19.shouldApplyDiffuseLighting()) {
            float var20 = LightUtil.diffuseLight(var19.getFace());
            float[] var10000 = var8.vertexColorMultiplier;
            var10000[0] *= var20;
            var10000 = var8.vertexColorMultiplier;
            var10000[1] *= var20;
            var10000 = var8.vertexColorMultiplier;
            var10000[2] *= var20;
            var10000 = var8.vertexColorMultiplier;
            var10000[3] *= var20;
         }

         if (var19.hasTintIndex()) {
            int var25 = this.blockColors.colorMultiplier(var2, var1, var3, var19.getTintIndex());
            if (EntityRenderer.anaglyphEnable) {
               var25 = TextureUtil.anaglyphColor(var25);
            }

            float var21 = (float)(var25 >> 16 & 255) / 255.0F;
            float var22 = (float)(var25 >> 8 & 255) / 255.0F;
            float var23 = (float)(var25 & 255) / 255.0F;
            var4.putColorMultiplier(var8.vertexColorMultiplier[0] * var21, var8.vertexColorMultiplier[0] * var22, var8.vertexColorMultiplier[0] * var23, 4);
            var4.putColorMultiplier(var8.vertexColorMultiplier[1] * var21, var8.vertexColorMultiplier[1] * var22, var8.vertexColorMultiplier[1] * var23, 3);
            var4.putColorMultiplier(var8.vertexColorMultiplier[2] * var21, var8.vertexColorMultiplier[2] * var22, var8.vertexColorMultiplier[2] * var23, 2);
            var4.putColorMultiplier(var8.vertexColorMultiplier[3] * var21, var8.vertexColorMultiplier[3] * var22, var8.vertexColorMultiplier[3] * var23, 1);
         } else {
            var4.putColorMultiplier(var8.vertexColorMultiplier[0], var8.vertexColorMultiplier[0], var8.vertexColorMultiplier[0], 4);
            var4.putColorMultiplier(var8.vertexColorMultiplier[1], var8.vertexColorMultiplier[1], var8.vertexColorMultiplier[1], 3);
            var4.putColorMultiplier(var8.vertexColorMultiplier[2], var8.vertexColorMultiplier[2], var8.vertexColorMultiplier[2], 2);
            var4.putColorMultiplier(var8.vertexColorMultiplier[3], var8.vertexColorMultiplier[3], var8.vertexColorMultiplier[3], 1);
         }

         var4.putPosition(var9, var11, var13);
      }

   }

   private void fillQuadBounds(IBlockState var1, int[] var2, EnumFacing var3, @Nullable float[] var4, BitSet var5) {
      float var6 = 32.0F;
      float var7 = 32.0F;
      float var8 = 32.0F;
      float var9 = -32.0F;
      float var10 = -32.0F;
      float var11 = -32.0F;

      for(int var12 = 0; var12 < 4; ++var12) {
         float var13 = Float.intBitsToFloat(var2[var12 * 7]);
         float var14 = Float.intBitsToFloat(var2[var12 * 7 + 1]);
         float var15 = Float.intBitsToFloat(var2[var12 * 7 + 2]);
         var6 = Math.min(var6, var13);
         var7 = Math.min(var7, var14);
         var8 = Math.min(var8, var15);
         var9 = Math.max(var9, var13);
         var10 = Math.max(var10, var14);
         var11 = Math.max(var11, var15);
      }

      if (var4 != null) {
         var4[EnumFacing.WEST.getIndex()] = var6;
         var4[EnumFacing.EAST.getIndex()] = var9;
         var4[EnumFacing.DOWN.getIndex()] = var7;
         var4[EnumFacing.UP.getIndex()] = var10;
         var4[EnumFacing.NORTH.getIndex()] = var8;
         var4[EnumFacing.SOUTH.getIndex()] = var11;
         var4[EnumFacing.WEST.getIndex() + EnumFacing.values().length] = 1.0F - var6;
         var4[EnumFacing.EAST.getIndex() + EnumFacing.values().length] = 1.0F - var9;
         var4[EnumFacing.DOWN.getIndex() + EnumFacing.values().length] = 1.0F - var7;
         var4[EnumFacing.UP.getIndex() + EnumFacing.values().length] = 1.0F - var10;
         var4[EnumFacing.NORTH.getIndex() + EnumFacing.values().length] = 1.0F - var8;
         var4[EnumFacing.SOUTH.getIndex() + EnumFacing.values().length] = 1.0F - var11;
      }

      float var16 = 1.0E-4F;
      float var17 = 0.9999F;
      switch(var3) {
      case DOWN:
         var5.set(1, var6 >= 1.0E-4F || var8 >= 1.0E-4F || var9 <= 0.9999F || var11 <= 0.9999F);
         var5.set(0, (var7 < 1.0E-4F || var1.isFullCube()) && var7 == var10);
         break;
      case UP:
         var5.set(1, var6 >= 1.0E-4F || var8 >= 1.0E-4F || var9 <= 0.9999F || var11 <= 0.9999F);
         var5.set(0, (var10 > 0.9999F || var1.isFullCube()) && var7 == var10);
         break;
      case NORTH:
         var5.set(1, var6 >= 1.0E-4F || var7 >= 1.0E-4F || var9 <= 0.9999F || var10 <= 0.9999F);
         var5.set(0, (var8 < 1.0E-4F || var1.isFullCube()) && var8 == var11);
         break;
      case SOUTH:
         var5.set(1, var6 >= 1.0E-4F || var7 >= 1.0E-4F || var9 <= 0.9999F || var10 <= 0.9999F);
         var5.set(0, (var11 > 0.9999F || var1.isFullCube()) && var8 == var11);
         break;
      case WEST:
         var5.set(1, var7 >= 1.0E-4F || var8 >= 1.0E-4F || var10 <= 0.9999F || var11 <= 0.9999F);
         var5.set(0, (var6 < 1.0E-4F || var1.isFullCube()) && var6 == var9);
         break;
      case EAST:
         var5.set(1, var7 >= 1.0E-4F || var8 >= 1.0E-4F || var10 <= 0.9999F || var11 <= 0.9999F);
         var5.set(0, (var9 > 0.9999F || var1.isFullCube()) && var6 == var9);
      }

   }

   private void renderQuadsFlat(IBlockAccess var1, IBlockState var2, BlockPos var3, int var4, boolean var5, VertexBuffer var6, List var7, BitSet var8) {
      double var9 = (double)var3.getX();
      double var11 = (double)var3.getY();
      double var13 = (double)var3.getZ();
      Block var15 = var2.getBlock();
      Block.EnumOffsetType var16 = var15.getOffsetType();
      if (var16 != Block.EnumOffsetType.NONE) {
         int var17 = var3.getX();
         int var18 = var3.getZ();
         long var19 = (long)(var17 * 3129871) ^ (long)var18 * 116129781L;
         var19 = var19 * var19 * 42317861L + var19 * 11L;
         var9 += ((double)((float)(var19 >> 16 & 15L) / 15.0F) - 0.5D) * 0.5D;
         var13 += ((double)((float)(var19 >> 24 & 15L) / 15.0F) - 0.5D) * 0.5D;
         if (var16 == Block.EnumOffsetType.XYZ) {
            var11 += ((double)((float)(var19 >> 20 & 15L) / 15.0F) - 1.0D) * 0.2D;
         }
      }

      int var25 = 0;

      for(int var26 = var7.size(); var25 < var26; ++var25) {
         BakedQuad var28 = (BakedQuad)var7.get(var25);
         if (var5) {
            this.fillQuadBounds(var2, var28.getVertexData(), var28.getFace(), (float[])null, var8);
            BlockPos var20 = var8.get(0) ? var3.offset(var28.getFace()) : var3;
            var4 = var2.getPackedLightmapCoords(var1, var20);
         }

         var6.addVertexData(var28.getVertexData());
         var6.putBrightness4(var4, var4, var4, var4);
         if (var28.hasTintIndex()) {
            int var29 = this.blockColors.colorMultiplier(var2, var1, var3, var28.getTintIndex());
            if (EntityRenderer.anaglyphEnable) {
               var29 = TextureUtil.anaglyphColor(var29);
            }

            float var21 = (float)(var29 >> 16 & 255) / 255.0F;
            float var22 = (float)(var29 >> 8 & 255) / 255.0F;
            float var23 = (float)(var29 & 255) / 255.0F;
            if (var28.shouldApplyDiffuseLighting()) {
               float var24 = LightUtil.diffuseLight(var28.getFace());
               var21 *= var24;
               var22 *= var24;
               var23 *= var24;
            }

            var6.putColorMultiplier(var21, var22, var23, 4);
            var6.putColorMultiplier(var21, var22, var23, 3);
            var6.putColorMultiplier(var21, var22, var23, 2);
            var6.putColorMultiplier(var21, var22, var23, 1);
         } else if (var28.shouldApplyDiffuseLighting()) {
            float var30 = LightUtil.diffuseLight(var28.getFace());
            var6.putColorMultiplier(var30, var30, var30, 4);
            var6.putColorMultiplier(var30, var30, var30, 3);
            var6.putColorMultiplier(var30, var30, var30, 2);
            var6.putColorMultiplier(var30, var30, var30, 1);
         }

         var6.putPosition(var9, var11, var13);
      }

   }

   public void renderModelBrightnessColor(IBakedModel var1, float var2, float var3, float var4, float var5) {
      this.renderModelBrightnessColor((IBlockState)null, var1, var2, var3, var4, var5);
   }

   public void renderModelBrightnessColor(IBlockState var1, IBakedModel var2, float var3, float var4, float var5, float var6) {
      for(EnumFacing var10 : EnumFacing.values()) {
         this.renderModelBrightnessColorQuads(var3, var4, var5, var6, var2.getQuads(var1, var10, 0L));
      }

      this.renderModelBrightnessColorQuads(var3, var4, var5, var6, var2.getQuads(var1, (EnumFacing)null, 0L));
   }

   public void renderModelBrightness(IBakedModel var1, IBlockState var2, float var3, boolean var4) {
      Block var5 = var2.getBlock();
      GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
      int var6 = this.blockColors.colorMultiplier(var2, (IBlockAccess)null, (BlockPos)null, 0);
      if (EntityRenderer.anaglyphEnable) {
         var6 = TextureUtil.anaglyphColor(var6);
      }

      float var7 = (float)(var6 >> 16 & 255) / 255.0F;
      float var8 = (float)(var6 >> 8 & 255) / 255.0F;
      float var9 = (float)(var6 & 255) / 255.0F;
      if (!var4) {
         GlStateManager.color(var3, var3, var3, 1.0F);
      }

      this.renderModelBrightnessColor(var2, var1, var3, var7, var8, var9);
   }

   private void renderModelBrightnessColorQuads(float var1, float var2, float var3, float var4, List var5) {
      Tessellator var6 = Tessellator.getInstance();
      VertexBuffer var7 = var6.getBuffer();
      int var8 = 0;

      for(int var9 = var5.size(); var8 < var9; ++var8) {
         BakedQuad var10 = (BakedQuad)var5.get(var8);
         var7.begin(7, DefaultVertexFormats.ITEM);
         var7.addVertexData(var10.getVertexData());
         if (var10.hasTintIndex()) {
            var7.putColorRGB_F4(var2 * var1, var3 * var1, var4 * var1);
         } else {
            var7.putColorRGB_F4(var1, var1, var1);
         }

         Vec3i var11 = var10.getFace().getDirectionVec();
         var7.putNormal((float)var11.getX(), (float)var11.getY(), (float)var11.getZ());
         var6.draw();
      }

   }

   @SideOnly(Side.CLIENT)
   class AmbientOcclusionFace {
      private final float[] vertexColorMultiplier = new float[4];
      private final int[] vertexBrightness = new int[4];

      public void updateVertexBrightness(IBlockAccess var1, IBlockState var2, BlockPos var3, EnumFacing var4, float[] var5, BitSet var6) {
         BlockPos var7 = var6.get(0) ? var3.offset(var4) : var3;
         BlockPos.PooledMutableBlockPos var8 = BlockPos.PooledMutableBlockPos.retain();
         BlockModelRenderer.EnumNeighborInfo var9 = BlockModelRenderer.EnumNeighborInfo.getNeighbourInfo(var4);
         BlockPos.PooledMutableBlockPos var10 = BlockPos.PooledMutableBlockPos.retain(var7).move(var9.corners[0]);
         BlockPos.PooledMutableBlockPos var11 = BlockPos.PooledMutableBlockPos.retain(var7).move(var9.corners[1]);
         BlockPos.PooledMutableBlockPos var12 = BlockPos.PooledMutableBlockPos.retain(var7).move(var9.corners[2]);
         BlockPos.PooledMutableBlockPos var13 = BlockPos.PooledMutableBlockPos.retain(var7).move(var9.corners[3]);
         int var14 = var2.getPackedLightmapCoords(var1, var10);
         int var15 = var2.getPackedLightmapCoords(var1, var11);
         int var16 = var2.getPackedLightmapCoords(var1, var12);
         int var17 = var2.getPackedLightmapCoords(var1, var13);
         float var18 = var1.getBlockState(var10).getAmbientOcclusionLightValue();
         float var19 = var1.getBlockState(var11).getAmbientOcclusionLightValue();
         float var20 = var1.getBlockState(var12).getAmbientOcclusionLightValue();
         float var21 = var1.getBlockState(var13).getAmbientOcclusionLightValue();
         boolean var22 = var1.getBlockState(var8.setPos(var10).move(var4)).isTranslucent();
         boolean var23 = var1.getBlockState(var8.setPos(var11).move(var4)).isTranslucent();
         boolean var24 = var1.getBlockState(var8.setPos(var12).move(var4)).isTranslucent();
         boolean var25 = var1.getBlockState(var8.setPos(var13).move(var4)).isTranslucent();
         float var26;
         int var27;
         if (!var24 && !var22) {
            var26 = var18;
            var27 = var14;
         } else {
            BlockPos.PooledMutableBlockPos var28 = var8.setPos(var10).move(var9.corners[2]);
            var26 = var1.getBlockState(var28).getAmbientOcclusionLightValue();
            var27 = var2.getPackedLightmapCoords(var1, var28);
         }

         int var29;
         float var61;
         if (!var25 && !var22) {
            var61 = var18;
            var29 = var14;
         } else {
            BlockPos.PooledMutableBlockPos var30 = var8.setPos(var10).move(var9.corners[3]);
            var61 = var1.getBlockState(var30).getAmbientOcclusionLightValue();
            var29 = var2.getPackedLightmapCoords(var1, var30);
         }

         int var31;
         float var62;
         if (!var24 && !var23) {
            var62 = var19;
            var31 = var15;
         } else {
            BlockPos.PooledMutableBlockPos var32 = var8.setPos(var11).move(var9.corners[2]);
            var62 = var1.getBlockState(var32).getAmbientOcclusionLightValue();
            var31 = var2.getPackedLightmapCoords(var1, var32);
         }

         int var33;
         float var63;
         if (!var25 && !var23) {
            var63 = var19;
            var33 = var15;
         } else {
            BlockPos.PooledMutableBlockPos var34 = var8.setPos(var11).move(var9.corners[3]);
            var63 = var1.getBlockState(var34).getAmbientOcclusionLightValue();
            var33 = var2.getPackedLightmapCoords(var1, var34);
         }

         int var64 = var2.getPackedLightmapCoords(var1, var3);
         if (var6.get(0) || !var1.getBlockState(var3.offset(var4)).isOpaqueCube()) {
            var64 = var2.getPackedLightmapCoords(var1, var3.offset(var4));
         }

         float var35 = var6.get(0) ? var1.getBlockState(var7).getAmbientOcclusionLightValue() : var1.getBlockState(var3).getAmbientOcclusionLightValue();
         BlockModelRenderer.VertexTranslations var36 = BlockModelRenderer.VertexTranslations.getVertexTranslations(var4);
         var8.release();
         var10.release();
         var11.release();
         var12.release();
         var13.release();
         if (var6.get(1) && var9.doNonCubicWeight) {
            float var65 = (var21 + var18 + var61 + var35) * 0.25F;
            float var66 = (var20 + var18 + var26 + var35) * 0.25F;
            float var67 = (var20 + var19 + var62 + var35) * 0.25F;
            float var68 = (var21 + var19 + var63 + var35) * 0.25F;
            float var41 = var5[var9.vert0Weights[0].shape] * var5[var9.vert0Weights[1].shape];
            float var42 = var5[var9.vert0Weights[2].shape] * var5[var9.vert0Weights[3].shape];
            float var43 = var5[var9.vert0Weights[4].shape] * var5[var9.vert0Weights[5].shape];
            float var44 = var5[var9.vert0Weights[6].shape] * var5[var9.vert0Weights[7].shape];
            float var45 = var5[var9.vert1Weights[0].shape] * var5[var9.vert1Weights[1].shape];
            float var46 = var5[var9.vert1Weights[2].shape] * var5[var9.vert1Weights[3].shape];
            float var47 = var5[var9.vert1Weights[4].shape] * var5[var9.vert1Weights[5].shape];
            float var48 = var5[var9.vert1Weights[6].shape] * var5[var9.vert1Weights[7].shape];
            float var49 = var5[var9.vert2Weights[0].shape] * var5[var9.vert2Weights[1].shape];
            float var50 = var5[var9.vert2Weights[2].shape] * var5[var9.vert2Weights[3].shape];
            float var51 = var5[var9.vert2Weights[4].shape] * var5[var9.vert2Weights[5].shape];
            float var52 = var5[var9.vert2Weights[6].shape] * var5[var9.vert2Weights[7].shape];
            float var53 = var5[var9.vert3Weights[0].shape] * var5[var9.vert3Weights[1].shape];
            float var54 = var5[var9.vert3Weights[2].shape] * var5[var9.vert3Weights[3].shape];
            float var55 = var5[var9.vert3Weights[4].shape] * var5[var9.vert3Weights[5].shape];
            float var56 = var5[var9.vert3Weights[6].shape] * var5[var9.vert3Weights[7].shape];
            this.vertexColorMultiplier[var36.vert0] = var65 * var41 + var66 * var42 + var67 * var43 + var68 * var44;
            this.vertexColorMultiplier[var36.vert1] = var65 * var45 + var66 * var46 + var67 * var47 + var68 * var48;
            this.vertexColorMultiplier[var36.vert2] = var65 * var49 + var66 * var50 + var67 * var51 + var68 * var52;
            this.vertexColorMultiplier[var36.vert3] = var65 * var53 + var66 * var54 + var67 * var55 + var68 * var56;
            int var57 = this.getAoBrightness(var17, var14, var29, var64);
            int var58 = this.getAoBrightness(var16, var14, var27, var64);
            int var59 = this.getAoBrightness(var16, var15, var31, var64);
            int var60 = this.getAoBrightness(var17, var15, var33, var64);
            this.vertexBrightness[var36.vert0] = this.getVertexBrightness(var57, var58, var59, var60, var41, var42, var43, var44);
            this.vertexBrightness[var36.vert1] = this.getVertexBrightness(var57, var58, var59, var60, var45, var46, var47, var48);
            this.vertexBrightness[var36.vert2] = this.getVertexBrightness(var57, var58, var59, var60, var49, var50, var51, var52);
            this.vertexBrightness[var36.vert3] = this.getVertexBrightness(var57, var58, var59, var60, var53, var54, var55, var56);
         } else {
            float var37 = (var21 + var18 + var61 + var35) * 0.25F;
            float var38 = (var20 + var18 + var26 + var35) * 0.25F;
            float var39 = (var20 + var19 + var62 + var35) * 0.25F;
            float var40 = (var21 + var19 + var63 + var35) * 0.25F;
            this.vertexBrightness[var36.vert0] = this.getAoBrightness(var17, var14, var29, var64);
            this.vertexBrightness[var36.vert1] = this.getAoBrightness(var16, var14, var27, var64);
            this.vertexBrightness[var36.vert2] = this.getAoBrightness(var16, var15, var31, var64);
            this.vertexBrightness[var36.vert3] = this.getAoBrightness(var17, var15, var33, var64);
            this.vertexColorMultiplier[var36.vert0] = var37;
            this.vertexColorMultiplier[var36.vert1] = var38;
            this.vertexColorMultiplier[var36.vert2] = var39;
            this.vertexColorMultiplier[var36.vert3] = var40;
         }

      }

      private int getAoBrightness(int var1, int var2, int var3, int var4) {
         if (var1 == 0) {
            var1 = var4;
         }

         if (var2 == 0) {
            var2 = var4;
         }

         if (var3 == 0) {
            var3 = var4;
         }

         return var1 + var2 + var3 + var4 >> 2 & 16711935;
      }

      private int getVertexBrightness(int var1, int var2, int var3, int var4, float var5, float var6, float var7, float var8) {
         int var9 = (int)((float)(var1 >> 16 & 255) * var5 + (float)(var2 >> 16 & 255) * var6 + (float)(var3 >> 16 & 255) * var7 + (float)(var4 >> 16 & 255) * var8) & 255;
         int var10 = (int)((float)(var1 & 255) * var5 + (float)(var2 & 255) * var6 + (float)(var3 & 255) * var7 + (float)(var4 & 255) * var8) & 255;
         return var9 << 16 | var10;
      }
   }

   @SideOnly(Side.CLIENT)
   public static enum EnumNeighborInfo {
      DOWN(new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST, EnumFacing.NORTH, EnumFacing.SOUTH}, 0.5F, true, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.SOUTH, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.SOUTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.NORTH, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.NORTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.NORTH, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.NORTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.SOUTH, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.SOUTH}),
      UP(new EnumFacing[]{EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH}, 1.0F, true, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.SOUTH, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.SOUTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.NORTH, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.NORTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.NORTH, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.NORTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.SOUTH, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.SOUTH}),
      NORTH(new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.EAST, EnumFacing.WEST}, 0.8F, true, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.FLIP_WEST}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.FLIP_EAST}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.FLIP_EAST}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.FLIP_WEST}),
      SOUTH(new EnumFacing[]{EnumFacing.WEST, EnumFacing.EAST, EnumFacing.DOWN, EnumFacing.UP}, 0.8F, true, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.WEST}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.FLIP_WEST, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.WEST, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.WEST}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.EAST}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.FLIP_EAST, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.EAST, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.EAST}),
      WEST(new EnumFacing[]{EnumFacing.UP, EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH}, 0.6F, true, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.SOUTH, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.SOUTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.NORTH, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.NORTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.NORTH, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.NORTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.SOUTH, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.SOUTH}),
      EAST(new EnumFacing[]{EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH}, 0.6F, true, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.SOUTH, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.SOUTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.NORTH, BlockModelRenderer.Orientation.FLIP_DOWN, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.DOWN, BlockModelRenderer.Orientation.NORTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.NORTH, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.FLIP_NORTH, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.NORTH}, new BlockModelRenderer.Orientation[]{BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.SOUTH, BlockModelRenderer.Orientation.FLIP_UP, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.FLIP_SOUTH, BlockModelRenderer.Orientation.UP, BlockModelRenderer.Orientation.SOUTH});

      private final EnumFacing[] corners;
      private final float shadeWeight;
      private final boolean doNonCubicWeight;
      private final BlockModelRenderer.Orientation[] vert0Weights;
      private final BlockModelRenderer.Orientation[] vert1Weights;
      private final BlockModelRenderer.Orientation[] vert2Weights;
      private final BlockModelRenderer.Orientation[] vert3Weights;
      private static final BlockModelRenderer.EnumNeighborInfo[] VALUES = new BlockModelRenderer.EnumNeighborInfo[6];

      private EnumNeighborInfo(EnumFacing[] var3, float var4, boolean var5, BlockModelRenderer.Orientation[] var6, BlockModelRenderer.Orientation[] var7, BlockModelRenderer.Orientation[] var8, BlockModelRenderer.Orientation[] var9) {
         this.corners = var3;
         this.shadeWeight = var4;
         this.doNonCubicWeight = var5;
         this.vert0Weights = var6;
         this.vert1Weights = var7;
         this.vert2Weights = var8;
         this.vert3Weights = var9;
      }

      public static BlockModelRenderer.EnumNeighborInfo getNeighbourInfo(EnumFacing var0) {
         return VALUES[var0.getIndex()];
      }

      static {
         VALUES[EnumFacing.DOWN.getIndex()] = DOWN;
         VALUES[EnumFacing.UP.getIndex()] = UP;
         VALUES[EnumFacing.NORTH.getIndex()] = NORTH;
         VALUES[EnumFacing.SOUTH.getIndex()] = SOUTH;
         VALUES[EnumFacing.WEST.getIndex()] = WEST;
         VALUES[EnumFacing.EAST.getIndex()] = EAST;
      }
   }

   @SideOnly(Side.CLIENT)
   public static enum Orientation {
      DOWN(EnumFacing.DOWN, false),
      UP(EnumFacing.UP, false),
      NORTH(EnumFacing.NORTH, false),
      SOUTH(EnumFacing.SOUTH, false),
      WEST(EnumFacing.WEST, false),
      EAST(EnumFacing.EAST, false),
      FLIP_DOWN(EnumFacing.DOWN, true),
      FLIP_UP(EnumFacing.UP, true),
      FLIP_NORTH(EnumFacing.NORTH, true),
      FLIP_SOUTH(EnumFacing.SOUTH, true),
      FLIP_WEST(EnumFacing.WEST, true),
      FLIP_EAST(EnumFacing.EAST, true);

      private final int shape;

      private Orientation(EnumFacing var3, boolean var4) {
         this.shape = var3.getIndex() + (var4 ? EnumFacing.values().length : 0);
      }
   }

   @SideOnly(Side.CLIENT)
   static enum VertexTranslations {
      DOWN(0, 1, 2, 3),
      UP(2, 3, 0, 1),
      NORTH(3, 0, 1, 2),
      SOUTH(0, 1, 2, 3),
      WEST(3, 0, 1, 2),
      EAST(1, 2, 3, 0);

      private final int vert0;
      private final int vert1;
      private final int vert2;
      private final int vert3;
      private static final BlockModelRenderer.VertexTranslations[] VALUES = new BlockModelRenderer.VertexTranslations[6];

      private VertexTranslations(int var3, int var4, int var5, int var6) {
         this.vert0 = var3;
         this.vert1 = var4;
         this.vert2 = var5;
         this.vert3 = var6;
      }

      public static BlockModelRenderer.VertexTranslations getVertexTranslations(EnumFacing var0) {
         return VALUES[var0.getIndex()];
      }

      static {
         VALUES[EnumFacing.DOWN.getIndex()] = DOWN;
         VALUES[EnumFacing.UP.getIndex()] = UP;
         VALUES[EnumFacing.NORTH.getIndex()] = NORTH;
         VALUES[EnumFacing.SOUTH.getIndex()] = SOUTH;
         VALUES[EnumFacing.WEST.getIndex()] = WEST;
         VALUES[EnumFacing.EAST.getIndex()] = EAST;
      }
   }
}
