package net.minecraft.client.particle;

import java.util.List;
import java.util.Random;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class Particle {
   private static final AxisAlignedBB EMPTY_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);
   protected World world;
   protected double prevPosX;
   protected double prevPosY;
   protected double prevPosZ;
   protected double posX;
   protected double posY;
   protected double posZ;
   protected double motionX;
   protected double motionY;
   protected double motionZ;
   private AxisAlignedBB boundingBox;
   protected boolean isCollided;
   protected boolean canCollide;
   protected boolean isExpired;
   protected float width;
   protected float height;
   protected Random rand;
   protected int particleTextureIndexX;
   protected int particleTextureIndexY;
   protected float particleTextureJitterX;
   protected float particleTextureJitterY;
   protected int particleAge;
   protected int particleMaxAge;
   protected float particleScale;
   protected float particleGravity;
   protected float particleRed;
   protected float particleGreen;
   protected float particleBlue;
   protected float particleAlpha;
   protected TextureAtlasSprite particleTexture;
   protected float particleAngle;
   protected float prevParticleAngle;
   public static double interpPosX;
   public static double interpPosY;
   public static double interpPosZ;
   public static Vec3d cameraViewDir;

   protected Particle(World var1, double var2, double var4, double var6) {
      this.boundingBox = EMPTY_AABB;
      this.width = 0.6F;
      this.height = 1.8F;
      this.rand = new Random();
      this.particleAlpha = 1.0F;
      this.world = var1;
      this.setSize(0.2F, 0.2F);
      this.setPosition(var2, var4, var6);
      this.prevPosX = var2;
      this.prevPosY = var4;
      this.prevPosZ = var6;
      this.particleRed = 1.0F;
      this.particleGreen = 1.0F;
      this.particleBlue = 1.0F;
      this.particleTextureJitterX = this.rand.nextFloat() * 3.0F;
      this.particleTextureJitterY = this.rand.nextFloat() * 3.0F;
      this.particleScale = (this.rand.nextFloat() * 0.5F + 0.5F) * 2.0F;
      this.particleMaxAge = (int)(4.0F / (this.rand.nextFloat() * 0.9F + 0.1F));
      this.particleAge = 0;
      this.canCollide = true;
   }

   public Particle(World var1, double var2, double var4, double var6, double var8, double var10, double var12) {
      this(var1, var2, var4, var6);
      this.motionX = var8 + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
      this.motionY = var10 + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
      this.motionZ = var12 + (Math.random() * 2.0D - 1.0D) * 0.4000000059604645D;
      float var14 = (float)(Math.random() + Math.random() + 1.0D) * 0.15F;
      float var15 = MathHelper.sqrt(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
      this.motionX = this.motionX / (double)var15 * (double)var14 * 0.4000000059604645D;
      this.motionY = this.motionY / (double)var15 * (double)var14 * 0.4000000059604645D + 0.10000000149011612D;
      this.motionZ = this.motionZ / (double)var15 * (double)var14 * 0.4000000059604645D;
   }

   public Particle multiplyVelocity(float var1) {
      this.motionX *= (double)var1;
      this.motionY = (this.motionY - 0.10000000149011612D) * (double)var1 + 0.10000000149011612D;
      this.motionZ *= (double)var1;
      return this;
   }

   public Particle multipleParticleScaleBy(float var1) {
      this.setSize(0.2F * var1, 0.2F * var1);
      this.particleScale *= var1;
      return this;
   }

   public void setRBGColorF(float var1, float var2, float var3) {
      this.particleRed = var1;
      this.particleGreen = var2;
      this.particleBlue = var3;
   }

   public void setAlphaF(float var1) {
      this.particleAlpha = var1;
   }

   public boolean isTransparent() {
      return false;
   }

   public float getRedColorF() {
      return this.particleRed;
   }

   public float getGreenColorF() {
      return this.particleGreen;
   }

   public float getBlueColorF() {
      return this.particleBlue;
   }

   public void setMaxAge(int var1) {
      this.particleMaxAge = var1;
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      if (this.particleAge++ >= this.particleMaxAge) {
         this.setExpired();
      }

      this.motionY -= 0.04D * (double)this.particleGravity;
      this.move(this.motionX, this.motionY, this.motionZ);
      this.motionX *= 0.9800000190734863D;
      this.motionY *= 0.9800000190734863D;
      this.motionZ *= 0.9800000190734863D;
      if (this.isCollided) {
         this.motionX *= 0.699999988079071D;
         this.motionZ *= 0.699999988079071D;
      }

   }

   public void renderParticle(VertexBuffer var1, Entity var2, float var3, float var4, float var5, float var6, float var7, float var8) {
      float var9 = (float)this.particleTextureIndexX / 16.0F;
      float var10 = var9 + 0.0624375F;
      float var11 = (float)this.particleTextureIndexY / 16.0F;
      float var12 = var11 + 0.0624375F;
      float var13 = 0.1F * this.particleScale;
      if (this.particleTexture != null) {
         var9 = this.particleTexture.getMinU();
         var10 = this.particleTexture.getMaxU();
         var11 = this.particleTexture.getMinV();
         var12 = this.particleTexture.getMaxV();
      }

      float var14 = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)var3 - interpPosX);
      float var15 = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)var3 - interpPosY);
      float var16 = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)var3 - interpPosZ);
      int var17 = this.getBrightnessForRender(var3);
      int var18 = var17 >> 16 & '\uffff';
      int var19 = var17 & '\uffff';
      Vec3d[] var20 = new Vec3d[]{new Vec3d((double)(-var4 * var13 - var7 * var13), (double)(-var5 * var13), (double)(-var6 * var13 - var8 * var13)), new Vec3d((double)(-var4 * var13 + var7 * var13), (double)(var5 * var13), (double)(-var6 * var13 + var8 * var13)), new Vec3d((double)(var4 * var13 + var7 * var13), (double)(var5 * var13), (double)(var6 * var13 + var8 * var13)), new Vec3d((double)(var4 * var13 - var7 * var13), (double)(-var5 * var13), (double)(var6 * var13 - var8 * var13))};
      if (this.particleAngle != 0.0F) {
         float var21 = this.particleAngle + (this.particleAngle - this.prevParticleAngle) * var3;
         float var22 = MathHelper.cos(var21 * 0.5F);
         float var23 = MathHelper.sin(var21 * 0.5F) * (float)cameraViewDir.xCoord;
         float var24 = MathHelper.sin(var21 * 0.5F) * (float)cameraViewDir.yCoord;
         float var25 = MathHelper.sin(var21 * 0.5F) * (float)cameraViewDir.zCoord;
         Vec3d var26 = new Vec3d((double)var23, (double)var24, (double)var25);

         for(int var27 = 0; var27 < 4; ++var27) {
            var20[var27] = var26.scale(2.0D * var20[var27].dotProduct(var26)).add(var20[var27].scale((double)(var22 * var22) - var26.dotProduct(var26))).add(var26.crossProduct(var20[var27]).scale((double)(2.0F * var22)));
         }
      }

      var1.pos((double)var14 + var20[0].xCoord, (double)var15 + var20[0].yCoord, (double)var16 + var20[0].zCoord).tex((double)var10, (double)var12).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(var18, var19).endVertex();
      var1.pos((double)var14 + var20[1].xCoord, (double)var15 + var20[1].yCoord, (double)var16 + var20[1].zCoord).tex((double)var10, (double)var11).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(var18, var19).endVertex();
      var1.pos((double)var14 + var20[2].xCoord, (double)var15 + var20[2].yCoord, (double)var16 + var20[2].zCoord).tex((double)var9, (double)var11).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(var18, var19).endVertex();
      var1.pos((double)var14 + var20[3].xCoord, (double)var15 + var20[3].yCoord, (double)var16 + var20[3].zCoord).tex((double)var9, (double)var12).color(this.particleRed, this.particleGreen, this.particleBlue, this.particleAlpha).lightmap(var18, var19).endVertex();
   }

   public int getFXLayer() {
      return 0;
   }

   public void setParticleTexture(TextureAtlasSprite var1) {
      int var2 = this.getFXLayer();
      if (var2 == 1) {
         this.particleTexture = var1;
      } else {
         throw new RuntimeException("Invalid call to Particle.setTex, use coordinate methods");
      }
   }

   public void setParticleTextureIndex(int var1) {
      if (this.getFXLayer() != 0) {
         throw new RuntimeException("Invalid call to Particle.setMiscTex");
      } else {
         this.particleTextureIndexX = var1 % 16;
         this.particleTextureIndexY = var1 / 16;
      }
   }

   public void nextTextureIndexX() {
      ++this.particleTextureIndexX;
   }

   public String toString() {
      return this.getClass().getSimpleName() + ", Pos (" + this.posX + "," + this.posY + "," + this.posZ + "), RGBA (" + this.particleRed + "," + this.particleGreen + "," + this.particleBlue + "," + this.particleAlpha + "), Age " + this.particleAge;
   }

   public void setExpired() {
      this.isExpired = true;
   }

   protected void setSize(float var1, float var2) {
      if (var1 != this.width || var2 != this.height) {
         this.width = var1;
         this.height = var2;
         AxisAlignedBB var3 = this.getBoundingBox();
         this.setBoundingBox(new AxisAlignedBB(var3.minX, var3.minY, var3.minZ, var3.minX + (double)this.width, var3.minY + (double)this.height, var3.minZ + (double)this.width));
      }

   }

   public void setPosition(double var1, double var3, double var5) {
      this.posX = var1;
      this.posY = var3;
      this.posZ = var5;
      float var7 = this.width / 2.0F;
      float var8 = this.height;
      this.setBoundingBox(new AxisAlignedBB(var1 - (double)var7, var3, var5 - (double)var7, var1 + (double)var7, var3 + (double)var8, var5 + (double)var7));
   }

   public void move(double var1, double var3, double var5) {
      double var7 = var3;
      if (this.canCollide) {
         List var9 = this.world.getCollisionBoxes((Entity)null, this.getBoundingBox().addCoord(var1, var3, var5));

         for(AxisAlignedBB var11 : var9) {
            var3 = var11.calculateYOffset(this.getBoundingBox(), var3);
         }

         this.setBoundingBox(this.getBoundingBox().offset(0.0D, var3, 0.0D));

         for(AxisAlignedBB var14 : var9) {
            var1 = var14.calculateXOffset(this.getBoundingBox(), var1);
         }

         this.setBoundingBox(this.getBoundingBox().offset(var1, 0.0D, 0.0D));

         for(AxisAlignedBB var15 : var9) {
            var5 = var15.calculateZOffset(this.getBoundingBox(), var5);
         }

         this.setBoundingBox(this.getBoundingBox().offset(0.0D, 0.0D, var5));
      } else {
         this.setBoundingBox(this.getBoundingBox().offset(var1, var3, var5));
      }

      this.resetPositionToBB();
      this.isCollided = var3 != var3 && var7 < 0.0D;
      if (var1 != var1) {
         this.motionX = 0.0D;
      }

      if (var5 != var5) {
         this.motionZ = 0.0D;
      }

   }

   protected void resetPositionToBB() {
      AxisAlignedBB var1 = this.getBoundingBox();
      this.posX = (var1.minX + var1.maxX) / 2.0D;
      this.posY = var1.minY;
      this.posZ = (var1.minZ + var1.maxZ) / 2.0D;
   }

   public int getBrightnessForRender(float var1) {
      BlockPos var2 = new BlockPos(this.posX, this.posY, this.posZ);
      return this.world.isBlockLoaded(var2) ? this.world.getCombinedLight(var2, 0) : 0;
   }

   public boolean isAlive() {
      return !this.isExpired;
   }

   public AxisAlignedBB getBoundingBox() {
      return this.boundingBox;
   }

   public void setBoundingBox(AxisAlignedBB var1) {
      this.boundingBox = var1;
   }
}
