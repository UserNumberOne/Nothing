package net.minecraft.client.renderer.entity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderFallingBlock extends Render {
   public RenderFallingBlock(RenderManager var1) {
      super(var1);
      this.shadowSize = 0.5F;
   }

   public void doRender(EntityFallingBlock var1, double var2, double var4, double var6, float var8, float var9) {
      if (var1.getBlock() != null) {
         IBlockState var10 = var1.getBlock();
         if (var10.getRenderType() == EnumBlockRenderType.MODEL) {
            World var11 = var1.getWorldObj();
            if (var10 != var11.getBlockState(new BlockPos(var1)) && var10.getRenderType() != EnumBlockRenderType.INVISIBLE) {
               this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
               GlStateManager.pushMatrix();
               GlStateManager.disableLighting();
               Tessellator var12 = Tessellator.getInstance();
               VertexBuffer var13 = var12.getBuffer();
               if (this.renderOutlines) {
                  GlStateManager.enableColorMaterial();
                  GlStateManager.enableOutlineMode(this.getTeamColor(var1));
               }

               var13.begin(7, DefaultVertexFormats.BLOCK);
               BlockPos var14 = new BlockPos(var1.posX, var1.getEntityBoundingBox().maxY, var1.posZ);
               GlStateManager.translate((float)(var2 - (double)var14.getX() - 0.5D), (float)(var4 - (double)var14.getY()), (float)(var6 - (double)var14.getZ() - 0.5D));
               BlockRendererDispatcher var15 = Minecraft.getMinecraft().getBlockRendererDispatcher();
               var15.getBlockModelRenderer().renderModel(var11, var15.getModelForState(var10), var10, var14, var13, false, MathHelper.getPositionRandom(var1.getOrigin()));
               var12.draw();
               if (this.renderOutlines) {
                  GlStateManager.disableOutlineMode();
                  GlStateManager.disableColorMaterial();
               }

               GlStateManager.enableLighting();
               GlStateManager.popMatrix();
               super.doRender(var1, var2, var4, var6, var8, var9);
            }
         }
      }

   }

   protected ResourceLocation getEntityTexture(EntityFallingBlock var1) {
      return TextureMap.LOCATION_BLOCKS_TEXTURE;
   }
}
