package net.minecraft.client.renderer.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityStructureRenderer extends TileEntitySpecialRenderer {
   public void renderTileEntityAt(TileEntityStructure var1, double var2, double var4, double var6, float var8, int var9) {
      if (Minecraft.getMinecraft().player.canUseCommandBlock() || Minecraft.getMinecraft().player.isSpectator()) {
         super.renderTileEntityAt(var1, var2, var4, var6, var8, var9);
         BlockPos var10 = var1.getPosition();
         BlockPos var11 = var1.getStructureSize();
         if (var11.getX() >= 1 && var11.getY() >= 1 && var11.getZ() >= 1 && (var1.getMode() == TileEntityStructure.Mode.SAVE || var1.getMode() == TileEntityStructure.Mode.LOAD)) {
            double var12 = 0.01D;
            double var14 = (double)var10.getX();
            double var16 = (double)var10.getZ();
            double var18 = var4 + (double)var10.getY() - 0.01D;
            double var20 = var18 + (double)var11.getY() + 0.02D;
            double var22;
            double var24;
            switch(var1.getMirror()) {
            case LEFT_RIGHT:
               var22 = (double)var11.getX() + 0.02D;
               var24 = -((double)var11.getZ() + 0.02D);
               break;
            case FRONT_BACK:
               var22 = -((double)var11.getX() + 0.02D);
               var24 = (double)var11.getZ() + 0.02D;
               break;
            default:
               var22 = (double)var11.getX() + 0.02D;
               var24 = (double)var11.getZ() + 0.02D;
            }

            double var26;
            double var28;
            double var30;
            double var32;
            switch(var1.getRotation()) {
            case CLOCKWISE_90:
               var26 = var2 + (var24 < 0.0D ? var14 - 0.01D : var14 + 1.0D + 0.01D);
               var28 = var6 + (var22 < 0.0D ? var16 + 1.0D + 0.01D : var16 - 0.01D);
               var30 = var26 - var24;
               var32 = var28 + var22;
               break;
            case CLOCKWISE_180:
               var26 = var2 + (var22 < 0.0D ? var14 - 0.01D : var14 + 1.0D + 0.01D);
               var28 = var6 + (var24 < 0.0D ? var16 - 0.01D : var16 + 1.0D + 0.01D);
               var30 = var26 - var22;
               var32 = var28 - var24;
               break;
            case COUNTERCLOCKWISE_90:
               var26 = var2 + (var24 < 0.0D ? var14 + 1.0D + 0.01D : var14 - 0.01D);
               var28 = var6 + (var22 < 0.0D ? var16 - 0.01D : var16 + 1.0D + 0.01D);
               var30 = var26 + var24;
               var32 = var28 - var22;
               break;
            default:
               var26 = var2 + (var22 < 0.0D ? var14 + 1.0D + 0.01D : var14 - 0.01D);
               var28 = var6 + (var24 < 0.0D ? var16 + 1.0D + 0.01D : var16 - 0.01D);
               var30 = var26 + var22;
               var32 = var28 + var24;
            }

            boolean var34 = true;
            boolean var35 = true;
            boolean var36 = true;
            Tessellator var37 = Tessellator.getInstance();
            VertexBuffer var38 = var37.getBuffer();
            GlStateManager.disableFog();
            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            this.setLightmapDisabled(true);
            if (var1.getMode() == TileEntityStructure.Mode.SAVE || var1.showsBoundingBox()) {
               this.renderBox(var37, var38, var26, var18, var28, var30, var20, var32, 255, 223, 127);
            }

            if (var1.getMode() == TileEntityStructure.Mode.SAVE && var1.showsAir()) {
               this.renderInvisibleBlocks(var1, var2, var4, var6, var10, var37, var38, true);
               this.renderInvisibleBlocks(var1, var2, var4, var6, var10, var37, var38, false);
            }

            this.setLightmapDisabled(false);
            GlStateManager.glLineWidth(1.0F);
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
            GlStateManager.enableDepth();
            GlStateManager.depthMask(true);
            GlStateManager.enableFog();
         }
      }

   }

   private void renderInvisibleBlocks(TileEntityStructure var1, double var2, double var4, double var6, BlockPos var8, Tessellator var9, VertexBuffer var10, boolean var11) {
      GlStateManager.glLineWidth(var11 ? 3.0F : 1.0F);
      var10.begin(3, DefaultVertexFormats.POSITION_COLOR);
      World var12 = var1.getWorld();
      BlockPos var13 = var1.getPos();
      BlockPos var14 = var13.add(var8);

      for(BlockPos var16 : BlockPos.MutableBlockPos.getAllInBox(var14, var14.add(var1.getStructureSize()).add(-1, -1, -1))) {
         IBlockState var17 = var12.getBlockState(var16);
         boolean var18 = var17 == Blocks.AIR.getDefaultState();
         boolean var19 = var17 == Blocks.STRUCTURE_VOID.getDefaultState();
         if (var18 || var19) {
            float var20 = var18 ? 0.05F : 0.0F;
            double var21 = (double)((float)(var16.getX() - var13.getX()) + 0.45F) + var2 - (double)var20;
            double var23 = (double)((float)(var16.getY() - var13.getY()) + 0.45F) + var4 - (double)var20;
            double var25 = (double)((float)(var16.getZ() - var13.getZ()) + 0.45F) + var6 - (double)var20;
            double var27 = (double)((float)(var16.getX() - var13.getX()) + 0.55F) + var2 + (double)var20;
            double var29 = (double)((float)(var16.getY() - var13.getY()) + 0.55F) + var4 + (double)var20;
            double var31 = (double)((float)(var16.getZ() - var13.getZ()) + 0.55F) + var6 + (double)var20;
            if (var11) {
               RenderGlobal.drawBoundingBox(var10, var21, var23, var25, var27, var29, var31, 0.0F, 0.0F, 0.0F, 1.0F);
            } else if (var18) {
               RenderGlobal.drawBoundingBox(var10, var21, var23, var25, var27, var29, var31, 0.5F, 0.5F, 1.0F, 1.0F);
            } else {
               RenderGlobal.drawBoundingBox(var10, var21, var23, var25, var27, var29, var31, 1.0F, 0.25F, 0.25F, 1.0F);
            }
         }
      }

      var9.draw();
   }

   private void renderBox(Tessellator var1, VertexBuffer var2, double var3, double var5, double var7, double var9, double var11, double var13, int var15, int var16, int var17) {
      GlStateManager.glLineWidth(2.0F);
      var2.begin(3, DefaultVertexFormats.POSITION_COLOR);
      var2.pos(var3, var5, var7).color((float)var16, (float)var16, (float)var16, 0.0F).endVertex();
      var2.pos(var3, var5, var7).color(var16, var16, var16, var15).endVertex();
      var2.pos(var9, var5, var7).color(var16, var17, var17, var15).endVertex();
      var2.pos(var9, var5, var13).color(var16, var16, var16, var15).endVertex();
      var2.pos(var3, var5, var13).color(var16, var16, var16, var15).endVertex();
      var2.pos(var3, var5, var7).color(var17, var17, var16, var15).endVertex();
      var2.pos(var3, var11, var7).color(var17, var16, var17, var15).endVertex();
      var2.pos(var9, var11, var7).color(var16, var16, var16, var15).endVertex();
      var2.pos(var9, var11, var13).color(var16, var16, var16, var15).endVertex();
      var2.pos(var3, var11, var13).color(var16, var16, var16, var15).endVertex();
      var2.pos(var3, var11, var7).color(var16, var16, var16, var15).endVertex();
      var2.pos(var3, var11, var13).color(var16, var16, var16, var15).endVertex();
      var2.pos(var3, var5, var13).color(var16, var16, var16, var15).endVertex();
      var2.pos(var9, var5, var13).color(var16, var16, var16, var15).endVertex();
      var2.pos(var9, var11, var13).color(var16, var16, var16, var15).endVertex();
      var2.pos(var9, var11, var7).color(var16, var16, var16, var15).endVertex();
      var2.pos(var9, var5, var7).color(var16, var16, var16, var15).endVertex();
      var2.pos(var9, var5, var7).color((float)var16, (float)var16, (float)var16, 0.0F).endVertex();
      var1.draw();
      GlStateManager.glLineWidth(1.0F);
   }

   public boolean isGlobalRenderer(TileEntityStructure var1) {
      return true;
   }
}
