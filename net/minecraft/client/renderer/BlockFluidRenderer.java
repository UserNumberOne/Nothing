package net.minecraft.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockFluidRenderer {
   private final BlockColors blockColors;
   private final TextureAtlasSprite[] atlasSpritesLava = new TextureAtlasSprite[2];
   private final TextureAtlasSprite[] atlasSpritesWater = new TextureAtlasSprite[2];
   private TextureAtlasSprite atlasSpriteWaterOverlay;

   public BlockFluidRenderer(BlockColors var1) {
      this.blockColors = var1;
      this.initAtlasSprites();
   }

   protected void initAtlasSprites() {
      TextureMap var1 = Minecraft.getMinecraft().getTextureMapBlocks();
      this.atlasSpritesLava[0] = var1.getAtlasSprite("minecraft:blocks/lava_still");
      this.atlasSpritesLava[1] = var1.getAtlasSprite("minecraft:blocks/lava_flow");
      this.atlasSpritesWater[0] = var1.getAtlasSprite("minecraft:blocks/water_still");
      this.atlasSpritesWater[1] = var1.getAtlasSprite("minecraft:blocks/water_flow");
      this.atlasSpriteWaterOverlay = var1.getAtlasSprite("minecraft:blocks/water_overlay");
   }

   public boolean renderFluid(IBlockAccess var1, IBlockState var2, BlockPos var3, VertexBuffer var4) {
      BlockLiquid var5 = (BlockLiquid)var2.getBlock();
      boolean var6 = var2.getMaterial() == Material.LAVA;
      TextureAtlasSprite[] var7 = var6 ? this.atlasSpritesLava : this.atlasSpritesWater;
      int var8 = this.blockColors.colorMultiplier(var2, var1, var3, 0);
      float var9 = (float)(var8 >> 16 & 255) / 255.0F;
      float var10 = (float)(var8 >> 8 & 255) / 255.0F;
      float var11 = (float)(var8 & 255) / 255.0F;
      boolean var12 = var2.shouldSideBeRendered(var1, var3, EnumFacing.UP);
      boolean var13 = var2.shouldSideBeRendered(var1, var3, EnumFacing.DOWN);
      boolean[] var14 = new boolean[]{var2.shouldSideBeRendered(var1, var3, EnumFacing.NORTH), var2.shouldSideBeRendered(var1, var3, EnumFacing.SOUTH), var2.shouldSideBeRendered(var1, var3, EnumFacing.WEST), var2.shouldSideBeRendered(var1, var3, EnumFacing.EAST)};
      if (!var12 && !var13 && !var14[0] && !var14[1] && !var14[2] && !var14[3]) {
         return false;
      } else {
         boolean var15 = false;
         float var16 = 0.5F;
         float var17 = 1.0F;
         float var18 = 0.8F;
         float var19 = 0.6F;
         Material var20 = var2.getMaterial();
         float var21 = this.getFluidHeight(var1, var3, var20);
         float var22 = this.getFluidHeight(var1, var3.south(), var20);
         float var23 = this.getFluidHeight(var1, var3.east().south(), var20);
         float var24 = this.getFluidHeight(var1, var3.east(), var20);
         double var25 = (double)var3.getX();
         double var27 = (double)var3.getY();
         double var29 = (double)var3.getZ();
         float var31 = 0.001F;
         if (var12) {
            var15 = true;
            float var32 = BlockLiquid.getSlopeAngle(var1, var3, var20, var2);
            TextureAtlasSprite var33 = var32 > -999.0F ? var7[1] : var7[0];
            var21 -= 0.001F;
            var22 -= 0.001F;
            var23 -= 0.001F;
            var24 -= 0.001F;
            float var34;
            float var35;
            float var36;
            float var37;
            float var38;
            float var39;
            float var40;
            float var41;
            if (var32 < -999.0F) {
               var34 = var33.getInterpolatedU(0.0D);
               var38 = var33.getInterpolatedV(0.0D);
               var35 = var34;
               var39 = var33.getInterpolatedV(16.0D);
               var36 = var33.getInterpolatedU(16.0D);
               var40 = var39;
               var37 = var36;
               var41 = var38;
            } else {
               float var42 = MathHelper.sin(var32) * 0.25F;
               float var43 = MathHelper.cos(var32) * 0.25F;
               float var44 = 8.0F;
               var34 = var33.getInterpolatedU((double)(8.0F + (-var43 - var42) * 16.0F));
               var38 = var33.getInterpolatedV((double)(8.0F + (-var43 + var42) * 16.0F));
               var35 = var33.getInterpolatedU((double)(8.0F + (-var43 + var42) * 16.0F));
               var39 = var33.getInterpolatedV((double)(8.0F + (var43 + var42) * 16.0F));
               var36 = var33.getInterpolatedU((double)(8.0F + (var43 + var42) * 16.0F));
               var40 = var33.getInterpolatedV((double)(8.0F + (var43 - var42) * 16.0F));
               var37 = var33.getInterpolatedU((double)(8.0F + (var43 - var42) * 16.0F));
               var41 = var33.getInterpolatedV((double)(8.0F + (-var43 - var42) * 16.0F));
            }

            int var76 = var2.getPackedLightmapCoords(var1, var3);
            int var77 = var76 >> 16 & '\uffff';
            int var79 = var76 & '\uffff';
            float var45 = 1.0F * var9;
            float var46 = 1.0F * var10;
            float var47 = 1.0F * var11;
            var4.pos(var25 + 0.0D, var27 + (double)var21, var29 + 0.0D).color(var45, var46, var47, 1.0F).tex((double)var34, (double)var38).lightmap(var77, var79).endVertex();
            var4.pos(var25 + 0.0D, var27 + (double)var22, var29 + 1.0D).color(var45, var46, var47, 1.0F).tex((double)var35, (double)var39).lightmap(var77, var79).endVertex();
            var4.pos(var25 + 1.0D, var27 + (double)var23, var29 + 1.0D).color(var45, var46, var47, 1.0F).tex((double)var36, (double)var40).lightmap(var77, var79).endVertex();
            var4.pos(var25 + 1.0D, var27 + (double)var24, var29 + 0.0D).color(var45, var46, var47, 1.0F).tex((double)var37, (double)var41).lightmap(var77, var79).endVertex();
            if (var5.shouldRenderSides(var1, var3.up())) {
               var4.pos(var25 + 0.0D, var27 + (double)var21, var29 + 0.0D).color(var45, var46, var47, 1.0F).tex((double)var34, (double)var38).lightmap(var77, var79).endVertex();
               var4.pos(var25 + 1.0D, var27 + (double)var24, var29 + 0.0D).color(var45, var46, var47, 1.0F).tex((double)var37, (double)var41).lightmap(var77, var79).endVertex();
               var4.pos(var25 + 1.0D, var27 + (double)var23, var29 + 1.0D).color(var45, var46, var47, 1.0F).tex((double)var36, (double)var40).lightmap(var77, var79).endVertex();
               var4.pos(var25 + 0.0D, var27 + (double)var22, var29 + 1.0D).color(var45, var46, var47, 1.0F).tex((double)var35, (double)var39).lightmap(var77, var79).endVertex();
            }
         }

         if (var13) {
            float var59 = var7[0].getMinU();
            float var61 = var7[0].getMaxU();
            float var63 = var7[0].getMinV();
            float var65 = var7[0].getMaxV();
            int var67 = var2.getPackedLightmapCoords(var1, var3.down());
            int var69 = var67 >> 16 & '\uffff';
            int var72 = var67 & '\uffff';
            var4.pos(var25, var27, var29 + 1.0D).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double)var59, (double)var65).lightmap(var69, var72).endVertex();
            var4.pos(var25, var27, var29).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double)var59, (double)var63).lightmap(var69, var72).endVertex();
            var4.pos(var25 + 1.0D, var27, var29).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double)var61, (double)var63).lightmap(var69, var72).endVertex();
            var4.pos(var25 + 1.0D, var27, var29 + 1.0D).color(0.5F, 0.5F, 0.5F, 1.0F).tex((double)var61, (double)var65).lightmap(var69, var72).endVertex();
            var15 = true;
         }

         for(int var60 = 0; var60 < 4; ++var60) {
            int var62 = 0;
            int var64 = 0;
            if (var60 == 0) {
               --var64;
            }

            if (var60 == 1) {
               ++var64;
            }

            if (var60 == 2) {
               --var62;
            }

            if (var60 == 3) {
               ++var62;
            }

            BlockPos var66 = var3.add(var62, 0, var64);
            TextureAtlasSprite var68 = var7[1];
            if (!var6) {
               Block var70 = var1.getBlockState(var66).getBlock();
               if (var70 == Blocks.GLASS || var70 == Blocks.STAINED_GLASS) {
                  var68 = this.atlasSpriteWaterOverlay;
               }
            }

            if (var14[var60]) {
               float var71;
               float var73;
               double var74;
               double var75;
               double var78;
               double var80;
               if (var60 == 0) {
                  var71 = var21;
                  var73 = var24;
                  var74 = var25;
                  var78 = var25 + 1.0D;
                  var75 = var29 + 0.0010000000474974513D;
                  var80 = var29 + 0.0010000000474974513D;
               } else if (var60 == 1) {
                  var71 = var23;
                  var73 = var22;
                  var74 = var25 + 1.0D;
                  var78 = var25;
                  var75 = var29 + 1.0D - 0.0010000000474974513D;
                  var80 = var29 + 1.0D - 0.0010000000474974513D;
               } else if (var60 == 2) {
                  var71 = var22;
                  var73 = var21;
                  var74 = var25 + 0.0010000000474974513D;
                  var78 = var25 + 0.0010000000474974513D;
                  var75 = var29 + 1.0D;
                  var80 = var29;
               } else {
                  var71 = var24;
                  var73 = var23;
                  var74 = var25 + 1.0D - 0.0010000000474974513D;
                  var78 = var25 + 1.0D - 0.0010000000474974513D;
                  var75 = var29;
                  var80 = var29 + 1.0D;
               }

               var15 = true;
               float var81 = var68.getInterpolatedU(0.0D);
               float var48 = var68.getInterpolatedU(8.0D);
               float var49 = var68.getInterpolatedV((double)((1.0F - var71) * 16.0F * 0.5F));
               float var50 = var68.getInterpolatedV((double)((1.0F - var73) * 16.0F * 0.5F));
               float var51 = var68.getInterpolatedV(8.0D);
               int var52 = var2.getPackedLightmapCoords(var1, var66);
               int var53 = var52 >> 16 & '\uffff';
               int var54 = var52 & '\uffff';
               float var55 = var60 < 2 ? 0.8F : 0.6F;
               float var56 = 1.0F * var55 * var9;
               float var57 = 1.0F * var55 * var10;
               float var58 = 1.0F * var55 * var11;
               var4.pos(var74, var27 + (double)var71, var75).color(var56, var57, var58, 1.0F).tex((double)var81, (double)var49).lightmap(var53, var54).endVertex();
               var4.pos(var78, var27 + (double)var73, var80).color(var56, var57, var58, 1.0F).tex((double)var48, (double)var50).lightmap(var53, var54).endVertex();
               var4.pos(var78, var27 + 0.0D, var80).color(var56, var57, var58, 1.0F).tex((double)var48, (double)var51).lightmap(var53, var54).endVertex();
               var4.pos(var74, var27 + 0.0D, var75).color(var56, var57, var58, 1.0F).tex((double)var81, (double)var51).lightmap(var53, var54).endVertex();
               if (var68 != this.atlasSpriteWaterOverlay) {
                  var4.pos(var74, var27 + 0.0D, var75).color(var56, var57, var58, 1.0F).tex((double)var81, (double)var51).lightmap(var53, var54).endVertex();
                  var4.pos(var78, var27 + 0.0D, var80).color(var56, var57, var58, 1.0F).tex((double)var48, (double)var51).lightmap(var53, var54).endVertex();
                  var4.pos(var78, var27 + (double)var73, var80).color(var56, var57, var58, 1.0F).tex((double)var48, (double)var50).lightmap(var53, var54).endVertex();
                  var4.pos(var74, var27 + (double)var71, var75).color(var56, var57, var58, 1.0F).tex((double)var81, (double)var49).lightmap(var53, var54).endVertex();
               }
            }
         }

         return var15;
      }
   }

   private float getFluidHeight(IBlockAccess var1, BlockPos var2, Material var3) {
      int var4 = 0;
      float var5 = 0.0F;

      for(int var6 = 0; var6 < 4; ++var6) {
         BlockPos var7 = var2.add(-(var6 & 1), 0, -(var6 >> 1 & 1));
         if (var1.getBlockState(var7.up()).getMaterial() == var3) {
            return 1.0F;
         }

         IBlockState var8 = var1.getBlockState(var7);
         Material var9 = var8.getMaterial();
         if (var9 != var3) {
            if (!var9.isSolid()) {
               ++var5;
               ++var4;
            }
         } else {
            int var10 = ((Integer)var8.getValue(BlockLiquid.LEVEL)).intValue();
            if (var10 >= 8 || var10 == 0) {
               var5 += BlockLiquid.getLiquidHeightPercent(var10) * 10.0F;
               var4 += 10;
            }

            var5 += BlockLiquid.getLiquidHeightPercent(var10);
            ++var4;
         }
      }

      return 1.0F - var5 / (float)var4;
   }
}
