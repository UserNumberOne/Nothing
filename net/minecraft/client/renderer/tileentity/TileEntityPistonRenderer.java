package net.minecraft.client.renderer.tileentity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityPistonRenderer extends TileEntitySpecialRenderer {
   private BlockRendererDispatcher blockRenderer;

   public void renderTileEntityAt(TileEntityPiston var1, double var2, double var4, double var6, float var8, int var9) {
      if (this.blockRenderer == null) {
         this.blockRenderer = Minecraft.getMinecraft().getBlockRendererDispatcher();
      }

      BlockPos var10 = var1.getPos();
      IBlockState var11 = var1.getPistonState();
      Block var12 = var11.getBlock();
      if (var11.getMaterial() != Material.AIR && var1.getProgress(var8) < 1.0F) {
         Tessellator var13 = Tessellator.getInstance();
         VertexBuffer var14 = var13.getBuffer();
         this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
         RenderHelper.disableStandardItemLighting();
         GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
         GlStateManager.enableBlend();
         GlStateManager.disableCull();
         if (Minecraft.isAmbientOcclusionEnabled()) {
            GlStateManager.shadeModel(7425);
         } else {
            GlStateManager.shadeModel(7424);
         }

         var14.begin(7, DefaultVertexFormats.BLOCK);
         var14.setTranslation((double)((float)var2 - (float)var10.getX() + var1.getOffsetX(var8)), (double)((float)var4 - (float)var10.getY() + var1.getOffsetY(var8)), (double)((float)var6 - (float)var10.getZ() + var1.getOffsetZ(var8)));
         World var15 = this.getWorld();
         if (var12 == Blocks.PISTON_HEAD && var1.getProgress(var8) < 0.5F) {
            var11 = var11.withProperty(BlockPistonExtension.SHORT, Boolean.valueOf(true));
            this.renderStateModel(var10, var11, var14, var15, true);
         } else if (var1.shouldPistonHeadBeRendered() && !var1.isExtending()) {
            BlockPistonExtension.EnumPistonType var16 = var12 == Blocks.STICKY_PISTON ? BlockPistonExtension.EnumPistonType.STICKY : BlockPistonExtension.EnumPistonType.DEFAULT;
            IBlockState var17 = Blocks.PISTON_HEAD.getDefaultState().withProperty(BlockPistonExtension.TYPE, var16).withProperty(BlockPistonExtension.FACING, var11.getValue(BlockPistonBase.FACING));
            var17 = var17.withProperty(BlockPistonExtension.SHORT, Boolean.valueOf(var1.getProgress(var8) >= 0.5F));
            this.renderStateModel(var10, var17, var14, var15, true);
            var14.setTranslation((double)((float)var2 - (float)var10.getX()), (double)((float)var4 - (float)var10.getY()), (double)((float)var6 - (float)var10.getZ()));
            var11 = var11.withProperty(BlockPistonBase.EXTENDED, Boolean.valueOf(true));
            this.renderStateModel(var10, var11, var14, var15, true);
         } else {
            this.renderStateModel(var10, var11, var14, var15, false);
         }

         var14.setTranslation(0.0D, 0.0D, 0.0D);
         var13.draw();
         RenderHelper.enableStandardItemLighting();
      }

   }

   private boolean renderStateModel(BlockPos var1, IBlockState var2, VertexBuffer var3, World var4, boolean var5) {
      return this.blockRenderer.getBlockModelRenderer().renderModel(var4, this.blockRenderer.getModelForState(var2), var2, var1, var3, var5);
   }
}
