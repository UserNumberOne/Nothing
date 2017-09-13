package net.minecraft.client.renderer.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DebugRendererHeightMap implements DebugRenderer.IDebugRenderer {
   private final Minecraft minecraft;

   public DebugRendererHeightMap(Minecraft var1) {
      this.minecraft = var1;
   }

   public void render(float var1, long var2) {
      EntityPlayerSP var4 = this.minecraft.player;
      WorldClient var5 = this.minecraft.world;
      double var6 = var4.lastTickPosX + (var4.posX - var4.lastTickPosX) * (double)var1;
      double var8 = var4.lastTickPosY + (var4.posY - var4.lastTickPosY) * (double)var1;
      double var10 = var4.lastTickPosZ + (var4.posZ - var4.lastTickPosZ) * (double)var1;
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      GlStateManager.disableTexture2D();
      BlockPos var12 = new BlockPos(var4.posX, 0.0D, var4.posZ);
      Iterable var13 = BlockPos.MutableBlockPos.getAllInBox(var12.add(-40, 0, -40), var12.add(40, 0, 40));
      Tessellator var14 = Tessellator.getInstance();
      VertexBuffer var15 = var14.getBuffer();
      var15.begin(5, DefaultVertexFormats.POSITION_COLOR);

      for(BlockPos var17 : var13) {
         int var18 = var5.getHeight(var17.getX(), var17.getZ());
         if (var5.getBlockState(var17.add(0, var18, 0).down()) == Blocks.AIR.getDefaultState()) {
            RenderGlobal.addChainedFilledBoxVertices(var15, (double)((float)var17.getX() + 0.25F) - var6, (double)var18 - var8, (double)((float)var17.getZ() + 0.25F) - var10, (double)((float)var17.getX() + 0.75F) - var6, (double)var18 + 0.09375D - var8, (double)((float)var17.getZ() + 0.75F) - var10, 0.0F, 0.0F, 1.0F, 0.5F);
         } else {
            RenderGlobal.addChainedFilledBoxVertices(var15, (double)((float)var17.getX() + 0.25F) - var6, (double)var18 - var8, (double)((float)var17.getZ() + 0.25F) - var10, (double)((float)var17.getX() + 0.75F) - var6, (double)var18 + 0.09375D - var8, (double)((float)var17.getZ() + 0.75F) - var10, 0.0F, 1.0F, 0.0F, 0.5F);
         }
      }

      var14.draw();
      GlStateManager.enableTexture2D();
      GlStateManager.popMatrix();
   }
}
