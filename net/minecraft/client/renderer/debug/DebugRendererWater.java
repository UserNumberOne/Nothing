package net.minecraft.client.renderer.debug;

import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DebugRendererWater implements DebugRenderer.IDebugRenderer {
   private final Minecraft minecraft;
   private EntityPlayer player;
   private double xo;
   private double yo;
   private double zo;

   public DebugRendererWater(Minecraft var1) {
      this.minecraft = var1;
   }

   public void render(float var1, long var2) {
      this.player = this.minecraft.player;
      this.xo = this.player.lastTickPosX + (this.player.posX - this.player.lastTickPosX) * (double)var1;
      this.yo = this.player.lastTickPosY + (this.player.posY - this.player.lastTickPosY) * (double)var1;
      this.zo = this.player.lastTickPosZ + (this.player.posZ - this.player.lastTickPosZ) * (double)var1;
      BlockPos var4 = this.minecraft.player.getPosition();
      World var5 = this.minecraft.player.world;
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      GlStateManager.color(0.0F, 1.0F, 0.0F, 0.75F);
      GlStateManager.disableTexture2D();
      GlStateManager.glLineWidth(6.0F);

      for(BlockPos var7 : BlockPos.PooledMutableBlockPos.getAllInBox(var4.add(-10, -10, -10), var4.add(10, 10, 10))) {
         IBlockState var8 = var5.getBlockState(var7);
         if (var8.getBlock() == Blocks.WATER || var8.getBlock() == Blocks.FLOWING_WATER) {
            double var9 = (double)EntityBoat.getLiquidHeight(var8, var5, var7);
            RenderGlobal.renderFilledBox((new AxisAlignedBB((double)((float)var7.getX() + 0.01F), (double)((float)var7.getY() + 0.01F), (double)((float)var7.getZ() + 0.01F), (double)((float)var7.getX() + 0.99F), var9, (double)((float)var7.getZ() + 0.99F))).offset(-this.xo, -this.yo, -this.zo), 1.0F, 1.0F, 1.0F, 0.2F);
         }
      }

      for(BlockPos var14 : BlockPos.PooledMutableBlockPos.getAllInBox(var4.add(-10, -10, -10), var4.add(10, 10, 10))) {
         IBlockState var15 = var5.getBlockState(var14);
         if (var15.getBlock() == Blocks.WATER || var15.getBlock() == Blocks.FLOWING_WATER) {
            Integer var16 = (Integer)var15.getValue(BlockLiquid.LEVEL);
            double var10 = var16.intValue() > 7 ? 0.9D : 1.0D - 0.11D * (double)var16.intValue();
            String var12 = var15.getBlock() == Blocks.FLOWING_WATER ? "f" : "s";
            DebugRenderer.renderDebugText(var12 + " " + var16, (double)var14.getX() + 0.5D, (double)var14.getY() + var10, (double)var14.getZ() + 0.5D, var1, -16777216);
         }
      }

      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
   }
}
