package net.minecraft.client.renderer.debug;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DebugRendererPathfinding implements DebugRenderer.IDebugRenderer {
   private final Minecraft minecraft;
   private final Map pathMap = Maps.newHashMap();
   private final Map pathMaxDistance = Maps.newHashMap();
   private final Map creationMap = Maps.newHashMap();
   private EntityPlayer player;
   private double xo;
   private double yo;
   private double zo;

   public DebugRendererPathfinding(Minecraft var1) {
      this.minecraft = var1;
   }

   public void addPath(int var1, Path var2, float var3) {
      this.pathMap.put(Integer.valueOf(var1), var2);
      this.creationMap.put(Integer.valueOf(var1), Long.valueOf(System.currentTimeMillis()));
      this.pathMaxDistance.put(Integer.valueOf(var1), Float.valueOf(var3));
   }

   public void render(float var1, long var2) {
      if (this.pathMap.size() != 0) {
         long var4 = System.currentTimeMillis();
         this.player = this.minecraft.player;
         this.xo = this.player.lastTickPosX + (this.player.posX - this.player.lastTickPosX) * (double)var1;
         this.yo = this.player.lastTickPosY + (this.player.posY - this.player.lastTickPosY) * (double)var1;
         this.zo = this.player.lastTickPosZ + (this.player.posZ - this.player.lastTickPosZ) * (double)var1;
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
         GlStateManager.color(0.0F, 1.0F, 0.0F, 0.75F);
         GlStateManager.disableTexture2D();
         GlStateManager.glLineWidth(6.0F);

         for(Integer var7 : this.pathMap.keySet()) {
            Path var8 = (Path)this.pathMap.get(var7);
            float var9 = ((Float)this.pathMaxDistance.get(var7)).floatValue();
            this.renderPathLine(var1, var8);
            PathPoint var10 = var8.getTarget();
            if (this.addDistanceToPlayer(var10) <= 40.0F) {
               RenderGlobal.renderFilledBox((new AxisAlignedBB((double)((float)var10.xCoord + 0.25F), (double)((float)var10.yCoord + 0.25F), (double)var10.zCoord + 0.25D, (double)((float)var10.xCoord + 0.75F), (double)((float)var10.yCoord + 0.75F), (double)((float)var10.zCoord + 0.75F))).offset(-this.xo, -this.yo, -this.zo), 0.0F, 1.0F, 0.0F, 0.5F);

               for(int var11 = 0; var11 < var8.getCurrentPathLength(); ++var11) {
                  PathPoint var12 = var8.getPathPointFromIndex(var11);
                  if (this.addDistanceToPlayer(var12) <= 40.0F) {
                     float var13 = var11 == var8.getCurrentPathIndex() ? 1.0F : 0.0F;
                     float var14 = var11 == var8.getCurrentPathIndex() ? 0.0F : 1.0F;
                     RenderGlobal.renderFilledBox((new AxisAlignedBB((double)((float)var12.xCoord + 0.5F - var9), (double)((float)var12.yCoord + 0.01F * (float)var11), (double)((float)var12.zCoord + 0.5F - var9), (double)((float)var12.xCoord + 0.5F + var9), (double)((float)var12.yCoord + 0.25F + 0.01F * (float)var11), (double)((float)var12.zCoord + 0.5F + var9))).offset(-this.xo, -this.yo, -this.zo), var13, 0.0F, var14, 0.5F);
                  }
               }
            }
         }

         for(Integer var17 : this.pathMap.keySet()) {
            Path var19 = (Path)this.pathMap.get(var17);

            for(PathPoint var30 : var19.getClosedSet()) {
               if (this.addDistanceToPlayer(var30) <= 40.0F) {
                  DebugRenderer.renderDebugText(String.format("%s", var30.nodeType), (double)var30.xCoord + 0.5D, (double)var30.yCoord + 0.75D, (double)var30.zCoord + 0.5D, var1, -65536);
                  DebugRenderer.renderDebugText(String.format("%.2f", var30.costMalus), (double)var30.xCoord + 0.5D, (double)var30.yCoord + 0.25D, (double)var30.zCoord + 0.5D, var1, -65536);
               }
            }

            for(PathPoint var31 : var19.getOpenSet()) {
               if (this.addDistanceToPlayer(var31) <= 40.0F) {
                  DebugRenderer.renderDebugText(String.format("%s", var31.nodeType), (double)var31.xCoord + 0.5D, (double)var31.yCoord + 0.75D, (double)var31.zCoord + 0.5D, var1, -16776961);
                  DebugRenderer.renderDebugText(String.format("%.2f", var31.costMalus), (double)var31.xCoord + 0.5D, (double)var31.yCoord + 0.25D, (double)var31.zCoord + 0.5D, var1, -16776961);
               }
            }

            for(int var23 = 0; var23 < var19.getCurrentPathLength(); ++var23) {
               PathPoint var27 = var19.getPathPointFromIndex(var23);
               if (this.addDistanceToPlayer(var27) <= 40.0F) {
                  DebugRenderer.renderDebugText(String.format("%s", var27.nodeType), (double)var27.xCoord + 0.5D, (double)var27.yCoord + 0.75D, (double)var27.zCoord + 0.5D, var1, -1);
                  DebugRenderer.renderDebugText(String.format("%.2f", var27.costMalus), (double)var27.xCoord + 0.5D, (double)var27.yCoord + 0.25D, (double)var27.zCoord + 0.5D, var1, -1);
               }
            }
         }

         for(Integer var24 : (Integer[])this.creationMap.keySet().toArray(new Integer[0])) {
            if (var4 - ((Long)this.creationMap.get(var24)).longValue() > 20000L) {
               this.pathMap.remove(var24);
               this.creationMap.remove(var24);
            }
         }

         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }

   }

   public void renderPathLine(float var1, Path var2) {
      Tessellator var3 = Tessellator.getInstance();
      VertexBuffer var4 = var3.getBuffer();
      var4.begin(3, DefaultVertexFormats.POSITION_COLOR);

      for(int var5 = 0; var5 < var2.getCurrentPathLength(); ++var5) {
         PathPoint var6 = var2.getPathPointFromIndex(var5);
         if (this.addDistanceToPlayer(var6) <= 40.0F) {
            float var7 = (float)var5 / (float)var2.getCurrentPathLength() * 0.33F;
            int var8 = var5 == 0 ? 0 : MathHelper.hsvToRGB(var7, 0.9F, 0.9F);
            int var9 = var8 >> 16 & 255;
            int var10 = var8 >> 8 & 255;
            int var11 = var8 & 255;
            var4.pos((double)var6.xCoord - this.xo + 0.5D, (double)var6.yCoord - this.yo + 0.5D, (double)var6.zCoord - this.zo + 0.5D).color(var9, var10, var11, 255).endVertex();
         }
      }

      var3.draw();
   }

   private float addDistanceToPlayer(PathPoint var1) {
      return (float)(Math.abs((double)var1.xCoord - this.player.posX) + Math.abs((double)var1.yCoord - this.player.posY) + Math.abs((double)var1.zCoord - this.player.posZ));
   }
}
