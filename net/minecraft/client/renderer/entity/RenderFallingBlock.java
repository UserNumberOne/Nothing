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
      super(renderManagerIn);
      this.shadowSize = 0.5F;
   }

   public void doRender(EntityFallingBlock var1, double var2, double var4, double var6, float var8, float var9) {
      if (entity.getBlock() != null) {
         IBlockState iblockstate = entity.getBlock();
         if (iblockstate.getRenderType() == EnumBlockRenderType.MODEL) {
            World world = entity.getWorldObj();
            if (iblockstate != world.getBlockState(new BlockPos(entity)) && iblockstate.getRenderType() != EnumBlockRenderType.INVISIBLE) {
               this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
               GlStateManager.pushMatrix();
               GlStateManager.disableLighting();
               Tessellator tessellator = Tessellator.getInstance();
               VertexBuffer vertexbuffer = tessellator.getBuffer();
               if (this.renderOutlines) {
                  GlStateManager.enableColorMaterial();
                  GlStateManager.enableOutlineMode(this.getTeamColor(entity));
               }

               vertexbuffer.begin(7, DefaultVertexFormats.BLOCK);
               BlockPos blockpos = new BlockPos(entity.posX, entity.getEntityBoundingBox().maxY, entity.posZ);
               GlStateManager.translate((float)(x - (double)blockpos.getX() - 0.5D), (float)(y - (double)blockpos.getY()), (float)(z - (double)blockpos.getZ() - 0.5D));
               BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
               blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(iblockstate), iblockstate, blockpos, vertexbuffer, false, MathHelper.getPositionRandom(entity.getOrigin()));
               tessellator.draw();
               if (this.renderOutlines) {
                  GlStateManager.disableOutlineMode();
                  GlStateManager.disableColorMaterial();
               }

               GlStateManager.enableLighting();
               GlStateManager.popMatrix();
               super.doRender(entity, x, y, z, entityYaw, partialTicks);
            }
         }
      }

   }

   protected ResourceLocation getEntityTexture(EntityFallingBlock var1) {
      return TextureMap.LOCATION_BLOCKS_TEXTURE;
   }
}
