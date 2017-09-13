package net.minecraft.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleItemPickup extends Particle {
   private final Entity item;
   private final Entity target;
   private int age;
   private final int maxAge;
   private final float yOffset;
   private final RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();

   public ParticleItemPickup(World var1, Entity var2, Entity var3, float var4) {
      super(var1, var2.posX, var2.posY, var2.posZ, var2.motionX, var2.motionY, var2.motionZ);
      this.item = var2;
      this.target = var3;
      this.maxAge = 3;
      this.yOffset = var4;
   }

   public void renderParticle(VertexBuffer var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      float var9 = ((float)this.age + var3) / (float)this.maxAge;
      var9 = var9 * var9;
      double var10 = this.item.posX;
      double var12 = this.item.posY;
      double var14 = this.item.posZ;
      double var16 = this.target.lastTickPosX + (this.target.posX - this.target.lastTickPosX) * (double)var3;
      double var18 = this.target.lastTickPosY + (this.target.posY - this.target.lastTickPosY) * (double)var3 + (double)this.yOffset;
      double var20 = this.target.lastTickPosZ + (this.target.posZ - this.target.lastTickPosZ) * (double)var3;
      double var22 = var10 + (var16 - var10) * (double)var9;
      double var24 = var12 + (var18 - var12) * (double)var9;
      double var26 = var14 + (var20 - var14) * (double)var9;
      int var28 = this.getBrightnessForRender(var3);
      int var29 = var28 % 65536;
      int var30 = var28 / 65536;
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)var29, (float)var30);
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      var22 = var22 - interpPosX;
      var24 = var24 - interpPosY;
      var26 = var26 - interpPosZ;
      GlStateManager.enableLighting();
      this.renderManager.doRenderEntity(this.item, var22, var24, var26, this.item.rotationYaw, var3, false);
   }

   public void onUpdate() {
      ++this.age;
      if (this.age == this.maxAge) {
         this.setExpired();
      }

   }

   public int getFXLayer() {
      return 3;
   }
}
