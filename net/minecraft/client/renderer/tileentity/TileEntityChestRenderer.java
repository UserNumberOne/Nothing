package net.minecraft.client.renderer.tileentity;

import java.util.Calendar;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.model.ModelChest;
import net.minecraft.client.model.ModelLargeChest;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityChestRenderer extends TileEntitySpecialRenderer {
   private static final ResourceLocation TEXTURE_TRAPPED_DOUBLE = new ResourceLocation("textures/entity/chest/trapped_double.png");
   private static final ResourceLocation TEXTURE_CHRISTMAS_DOUBLE = new ResourceLocation("textures/entity/chest/christmas_double.png");
   private static final ResourceLocation TEXTURE_NORMAL_DOUBLE = new ResourceLocation("textures/entity/chest/normal_double.png");
   private static final ResourceLocation TEXTURE_TRAPPED = new ResourceLocation("textures/entity/chest/trapped.png");
   private static final ResourceLocation TEXTURE_CHRISTMAS = new ResourceLocation("textures/entity/chest/christmas.png");
   private static final ResourceLocation TEXTURE_NORMAL = new ResourceLocation("textures/entity/chest/normal.png");
   private final ModelChest simpleChest = new ModelChest();
   private final ModelChest largeChest = new ModelLargeChest();
   private boolean isChristmas;

   public TileEntityChestRenderer() {
      Calendar var1 = Calendar.getInstance();
      if (var1.get(2) + 1 == 12 && var1.get(5) >= 24 && var1.get(5) <= 26) {
         this.isChristmas = true;
      }

   }

   public void renderTileEntityAt(TileEntityChest var1, double var2, double var4, double var6, float var8, int var9) {
      GlStateManager.enableDepth();
      GlStateManager.depthFunc(515);
      GlStateManager.depthMask(true);
      int var10;
      if (var1.hasWorld()) {
         Block var11 = var1.getBlockType();
         var10 = var1.getBlockMetadata();
         if (var11 instanceof BlockChest && var10 == 0) {
            ((BlockChest)var11).checkForSurroundingChests(var1.getWorld(), var1.getPos(), var1.getWorld().getBlockState(var1.getPos()));
            var10 = var1.getBlockMetadata();
         }

         var1.checkForAdjacentChests();
      } else {
         var10 = 0;
      }

      if (var1.adjacentChestZNeg == null && var1.adjacentChestXNeg == null) {
         ModelChest var15;
         if (var1.adjacentChestXPos == null && var1.adjacentChestZPos == null) {
            var15 = this.simpleChest;
            if (var9 >= 0) {
               this.bindTexture(DESTROY_STAGES[var9]);
               GlStateManager.matrixMode(5890);
               GlStateManager.pushMatrix();
               GlStateManager.scale(4.0F, 4.0F, 1.0F);
               GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
               GlStateManager.matrixMode(5888);
            } else if (this.isChristmas) {
               this.bindTexture(TEXTURE_CHRISTMAS);
            } else if (var1.getChestType() == BlockChest.Type.TRAP) {
               this.bindTexture(TEXTURE_TRAPPED);
            } else {
               this.bindTexture(TEXTURE_NORMAL);
            }
         } else {
            var15 = this.largeChest;
            if (var9 >= 0) {
               this.bindTexture(DESTROY_STAGES[var9]);
               GlStateManager.matrixMode(5890);
               GlStateManager.pushMatrix();
               GlStateManager.scale(8.0F, 4.0F, 1.0F);
               GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
               GlStateManager.matrixMode(5888);
            } else if (this.isChristmas) {
               this.bindTexture(TEXTURE_CHRISTMAS_DOUBLE);
            } else if (var1.getChestType() == BlockChest.Type.TRAP) {
               this.bindTexture(TEXTURE_TRAPPED_DOUBLE);
            } else {
               this.bindTexture(TEXTURE_NORMAL_DOUBLE);
            }
         }

         GlStateManager.pushMatrix();
         GlStateManager.enableRescaleNormal();
         if (var9 < 0) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         }

         GlStateManager.translate((float)var2, (float)var4 + 1.0F, (float)var6 + 1.0F);
         GlStateManager.scale(1.0F, -1.0F, -1.0F);
         GlStateManager.translate(0.5F, 0.5F, 0.5F);
         short var12 = 0;
         if (var10 == 2) {
            var12 = 180;
         }

         if (var10 == 3) {
            var12 = 0;
         }

         if (var10 == 4) {
            var12 = 90;
         }

         if (var10 == 5) {
            var12 = -90;
         }

         if (var10 == 2 && var1.adjacentChestXPos != null) {
            GlStateManager.translate(1.0F, 0.0F, 0.0F);
         }

         if (var10 == 5 && var1.adjacentChestZPos != null) {
            GlStateManager.translate(0.0F, 0.0F, -1.0F);
         }

         GlStateManager.rotate((float)var12, 0.0F, 1.0F, 0.0F);
         GlStateManager.translate(-0.5F, -0.5F, -0.5F);
         float var13 = var1.prevLidAngle + (var1.lidAngle - var1.prevLidAngle) * var8;
         if (var1.adjacentChestZNeg != null) {
            float var14 = var1.adjacentChestZNeg.prevLidAngle + (var1.adjacentChestZNeg.lidAngle - var1.adjacentChestZNeg.prevLidAngle) * var8;
            if (var14 > var13) {
               var13 = var14;
            }
         }

         if (var1.adjacentChestXNeg != null) {
            float var18 = var1.adjacentChestXNeg.prevLidAngle + (var1.adjacentChestXNeg.lidAngle - var1.adjacentChestXNeg.prevLidAngle) * var8;
            if (var18 > var13) {
               var13 = var18;
            }
         }

         var13 = 1.0F - var13;
         var13 = 1.0F - var13 * var13 * var13;
         var15.chestLid.rotateAngleX = -(var13 * 1.5707964F);
         var15.renderAll();
         GlStateManager.disableRescaleNormal();
         GlStateManager.popMatrix();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         if (var9 >= 0) {
            GlStateManager.matrixMode(5890);
            GlStateManager.popMatrix();
            GlStateManager.matrixMode(5888);
         }
      }

   }
}
