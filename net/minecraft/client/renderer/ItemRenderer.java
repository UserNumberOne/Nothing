package net.minecraft.client.renderer;

import com.google.common.base.Objects;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ItemRenderer {
   private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
   private static final ResourceLocation RES_UNDERWATER_OVERLAY = new ResourceLocation("textures/misc/underwater.png");
   private final Minecraft mc;
   private ItemStack itemStackMainHand;
   private ItemStack itemStackOffHand;
   private float equippedProgressMainHand;
   private float prevEquippedProgressMainHand;
   private float equippedProgressOffHand;
   private float prevEquippedProgressOffHand;
   private final RenderManager renderManager;
   private final RenderItem itemRenderer;

   public ItemRenderer(Minecraft var1) {
      this.mc = var1;
      this.renderManager = var1.getRenderManager();
      this.itemRenderer = var1.getRenderItem();
   }

   public void renderItem(EntityLivingBase var1, ItemStack var2, ItemCameraTransforms.TransformType var3) {
      this.renderItemSide(var1, var2, var3, false);
   }

   public void renderItemSide(EntityLivingBase var1, ItemStack var2, ItemCameraTransforms.TransformType var3, boolean var4) {
      if (var2 != null) {
         Item var5 = var2.getItem();
         Block var6 = Block.getBlockFromItem(var5);
         GlStateManager.pushMatrix();
         boolean var7 = this.itemRenderer.shouldRenderItemIn3D(var2) && this.isBlockTranslucent(var6);
         if (var7) {
            GlStateManager.depthMask(false);
         }

         this.itemRenderer.renderItem(var2, var1, var3, var4);
         if (var7) {
            GlStateManager.depthMask(true);
         }

         GlStateManager.popMatrix();
      }

   }

   private boolean isBlockTranslucent(@Nullable Block var1) {
      return var1 != null && var1.getBlockLayer() == BlockRenderLayer.TRANSLUCENT;
   }

   private void rotateArroundXAndY(float var1, float var2) {
      GlStateManager.pushMatrix();
      GlStateManager.rotate(var1, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(var2, 0.0F, 1.0F, 0.0F);
      RenderHelper.enableStandardItemLighting();
      GlStateManager.popMatrix();
   }

   private void setLightmap() {
      EntityPlayerSP var1 = this.mc.player;
      int var2 = this.mc.world.getCombinedLight(new BlockPos(var1.posX, var1.posY + (double)var1.getEyeHeight(), var1.posZ), 0);
      float var3 = (float)(var2 & '\uffff');
      float var4 = (float)(var2 >> 16);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, var3, var4);
   }

   private void rotateArm(float var1) {
      EntityPlayerSP var2 = this.mc.player;
      float var3 = var2.prevRenderArmPitch + (var2.renderArmPitch - var2.prevRenderArmPitch) * var1;
      float var4 = var2.prevRenderArmYaw + (var2.renderArmYaw - var2.prevRenderArmYaw) * var1;
      GlStateManager.rotate((var2.rotationPitch - var3) * 0.1F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate((var2.rotationYaw - var4) * 0.1F, 0.0F, 1.0F, 0.0F);
   }

   private float getMapAngleFromPitch(float var1) {
      float var2 = 1.0F - var1 / 45.0F + 0.1F;
      var2 = MathHelper.clamp(var2, 0.0F, 1.0F);
      var2 = -MathHelper.cos(var2 * 3.1415927F) * 0.5F + 0.5F;
      return var2;
   }

   private void renderArms() {
      if (!this.mc.player.isInvisible()) {
         GlStateManager.disableCull();
         GlStateManager.pushMatrix();
         GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
         this.renderArm(EnumHandSide.RIGHT);
         this.renderArm(EnumHandSide.LEFT);
         GlStateManager.popMatrix();
         GlStateManager.enableCull();
      }

   }

   private void renderArm(EnumHandSide var1) {
      this.mc.getTextureManager().bindTexture(this.mc.player.getLocationSkin());
      Render var2 = this.renderManager.getEntityRenderObject(this.mc.player);
      RenderPlayer var3 = (RenderPlayer)var2;
      GlStateManager.pushMatrix();
      float var4 = var1 == EnumHandSide.RIGHT ? 1.0F : -1.0F;
      GlStateManager.rotate(92.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(var4 * -41.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.translate(var4 * 0.3F, -1.1F, 0.45F);
      if (var1 == EnumHandSide.RIGHT) {
         var3.renderRightArm(this.mc.player);
      } else {
         var3.renderLeftArm(this.mc.player);
      }

      GlStateManager.popMatrix();
   }

   private void renderMapFirstPersonSide(float var1, EnumHandSide var2, float var3, ItemStack var4) {
      float var5 = var2 == EnumHandSide.RIGHT ? 1.0F : -1.0F;
      GlStateManager.translate(var5 * 0.125F, -0.125F, 0.0F);
      if (!this.mc.player.isInvisible()) {
         GlStateManager.pushMatrix();
         GlStateManager.rotate(var5 * 10.0F, 0.0F, 0.0F, 1.0F);
         this.renderArmFirstPerson(var1, var3, var2);
         GlStateManager.popMatrix();
      }

      GlStateManager.pushMatrix();
      GlStateManager.translate(var5 * 0.51F, -0.08F + var1 * -1.2F, -0.75F);
      float var6 = MathHelper.sqrt(var3);
      float var7 = MathHelper.sin(var6 * 3.1415927F);
      float var8 = -0.5F * var7;
      float var9 = 0.4F * MathHelper.sin(var6 * 6.2831855F);
      float var10 = -0.3F * MathHelper.sin(var3 * 3.1415927F);
      GlStateManager.translate(var5 * var8, var9 - 0.3F * var7, var10);
      GlStateManager.rotate(var7 * -45.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(var5 * var7 * -30.0F, 0.0F, 1.0F, 0.0F);
      this.renderMapFirstPerson(var4);
      GlStateManager.popMatrix();
   }

   private void renderMapFirstPerson(float var1, float var2, float var3) {
      float var4 = MathHelper.sqrt(var3);
      float var5 = -0.2F * MathHelper.sin(var3 * 3.1415927F);
      float var6 = -0.4F * MathHelper.sin(var4 * 3.1415927F);
      GlStateManager.translate(0.0F, -var5 / 2.0F, var6);
      float var7 = this.getMapAngleFromPitch(var1);
      GlStateManager.translate(0.0F, 0.04F + var2 * -1.2F + var7 * -0.5F, -0.72F);
      GlStateManager.rotate(var7 * -85.0F, 1.0F, 0.0F, 0.0F);
      this.renderArms();
      float var8 = MathHelper.sin(var4 * 3.1415927F);
      GlStateManager.rotate(var8 * 20.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.scale(2.0F, 2.0F, 2.0F);
      this.renderMapFirstPerson(this.itemStackMainHand);
   }

   private void renderMapFirstPerson(ItemStack var1) {
      GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.scale(0.38F, 0.38F, 0.38F);
      GlStateManager.disableLighting();
      this.mc.getTextureManager().bindTexture(RES_MAP_BACKGROUND);
      Tessellator var2 = Tessellator.getInstance();
      VertexBuffer var3 = var2.getBuffer();
      GlStateManager.translate(-0.5F, -0.5F, 0.0F);
      GlStateManager.scale(0.0078125F, 0.0078125F, 0.0078125F);
      var3.begin(7, DefaultVertexFormats.POSITION_TEX);
      var3.pos(-7.0D, 135.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
      var3.pos(135.0D, 135.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
      var3.pos(135.0D, -7.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
      var3.pos(-7.0D, -7.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
      var2.draw();
      MapData var4 = Items.FILLED_MAP.getMapData(var1, this.mc.world);
      if (var4 != null) {
         this.mc.entityRenderer.getMapItemRenderer().renderMap(var4, false);
      }

      GlStateManager.enableLighting();
   }

   private void renderArmFirstPerson(float var1, float var2, EnumHandSide var3) {
      boolean var4 = var3 != EnumHandSide.LEFT;
      float var5 = var4 ? 1.0F : -1.0F;
      float var6 = MathHelper.sqrt(var2);
      float var7 = -0.3F * MathHelper.sin(var6 * 3.1415927F);
      float var8 = 0.4F * MathHelper.sin(var6 * 6.2831855F);
      float var9 = -0.4F * MathHelper.sin(var2 * 3.1415927F);
      GlStateManager.translate(var5 * (var7 + 0.64000005F), var8 + -0.6F + var1 * -0.6F, var9 + -0.71999997F);
      GlStateManager.rotate(var5 * 45.0F, 0.0F, 1.0F, 0.0F);
      float var10 = MathHelper.sin(var2 * var2 * 3.1415927F);
      float var11 = MathHelper.sin(var6 * 3.1415927F);
      GlStateManager.rotate(var5 * var11 * 70.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(var5 * var10 * -20.0F, 0.0F, 0.0F, 1.0F);
      EntityPlayerSP var12 = this.mc.player;
      this.mc.getTextureManager().bindTexture(var12.getLocationSkin());
      GlStateManager.translate(var5 * -1.0F, 3.6F, 3.5F);
      GlStateManager.rotate(var5 * 120.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate(var5 * -135.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.translate(var5 * 5.6F, 0.0F, 0.0F);
      RenderPlayer var13 = (RenderPlayer)this.renderManager.getEntityRenderObject(var12);
      GlStateManager.disableCull();
      if (var4) {
         var13.renderRightArm(var12);
      } else {
         var13.renderLeftArm(var12);
      }

      GlStateManager.enableCull();
   }

   private void transformEatFirstPerson(float var1, EnumHandSide var2, ItemStack var3) {
      float var4 = (float)this.mc.player.getItemInUseCount() - var1 + 1.0F;
      float var5 = var4 / (float)var3.getMaxItemUseDuration();
      if (var5 < 0.8F) {
         float var6 = MathHelper.abs(MathHelper.cos(var4 / 4.0F * 3.1415927F) * 0.1F);
         GlStateManager.translate(0.0F, var6, 0.0F);
      }

      float var8 = 1.0F - (float)Math.pow((double)var5, 27.0D);
      int var7 = var2 == EnumHandSide.RIGHT ? 1 : -1;
      GlStateManager.translate(var8 * 0.6F * (float)var7, var8 * -0.5F, var8 * 0.0F);
      GlStateManager.rotate((float)var7 * var8 * 90.0F, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(var8 * 10.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate((float)var7 * var8 * 30.0F, 0.0F, 0.0F, 1.0F);
   }

   private void transformFirstPerson(EnumHandSide var1, float var2) {
      int var3 = var1 == EnumHandSide.RIGHT ? 1 : -1;
      float var4 = MathHelper.sin(var2 * var2 * 3.1415927F);
      GlStateManager.rotate((float)var3 * (45.0F + var4 * -20.0F), 0.0F, 1.0F, 0.0F);
      float var5 = MathHelper.sin(MathHelper.sqrt(var2) * 3.1415927F);
      GlStateManager.rotate((float)var3 * var5 * -20.0F, 0.0F, 0.0F, 1.0F);
      GlStateManager.rotate(var5 * -80.0F, 1.0F, 0.0F, 0.0F);
      GlStateManager.rotate((float)var3 * -45.0F, 0.0F, 1.0F, 0.0F);
   }

   private void transformSideFirstPerson(EnumHandSide var1, float var2) {
      int var3 = var1 == EnumHandSide.RIGHT ? 1 : -1;
      GlStateManager.translate((float)var3 * 0.56F, -0.52F + var2 * -0.6F, -0.72F);
   }

   public void renderItemInFirstPerson(float var1) {
      EntityPlayerSP var2 = this.mc.player;
      float var3 = var2.getSwingProgress(var1);
      EnumHand var4 = (EnumHand)Objects.firstNonNull(var2.swingingHand, EnumHand.MAIN_HAND);
      float var5 = var2.prevRotationPitch + (var2.rotationPitch - var2.prevRotationPitch) * var1;
      float var6 = var2.prevRotationYaw + (var2.rotationYaw - var2.prevRotationYaw) * var1;
      boolean var7 = true;
      boolean var8 = true;
      if (var2.isHandActive()) {
         ItemStack var9 = var2.getActiveItemStack();
         if (var9 != null && var9.getItem() == Items.BOW) {
            EnumHand var10 = var2.getActiveHand();
            var7 = var10 == EnumHand.MAIN_HAND;
            var8 = !var7;
         }
      }

      this.rotateArroundXAndY(var5, var6);
      this.setLightmap();
      this.rotateArm(var1);
      GlStateManager.enableRescaleNormal();
      if (var7) {
         float var11 = var4 == EnumHand.MAIN_HAND ? var3 : 0.0F;
         float var13 = 1.0F - (this.prevEquippedProgressMainHand + (this.equippedProgressMainHand - this.prevEquippedProgressMainHand) * var1);
         if (!ForgeHooksClient.renderSpecificFirstPersonHand(EnumHand.MAIN_HAND, var1, var5, var11, var13, this.itemStackMainHand)) {
            this.renderItemInFirstPerson(var2, var1, var5, EnumHand.MAIN_HAND, var11, this.itemStackMainHand, var13);
         }
      }

      if (var8) {
         float var12 = var4 == EnumHand.OFF_HAND ? var3 : 0.0F;
         float var14 = 1.0F - (this.prevEquippedProgressOffHand + (this.equippedProgressOffHand - this.prevEquippedProgressOffHand) * var1);
         if (!ForgeHooksClient.renderSpecificFirstPersonHand(EnumHand.OFF_HAND, var1, var5, var12, var14, this.itemStackOffHand)) {
            this.renderItemInFirstPerson(var2, var1, var5, EnumHand.OFF_HAND, var12, this.itemStackOffHand, var14);
         }
      }

      GlStateManager.disableRescaleNormal();
      RenderHelper.disableStandardItemLighting();
   }

   public void renderItemInFirstPerson(AbstractClientPlayer var1, float var2, float var3, EnumHand var4, float var5, @Nullable ItemStack var6, float var7) {
      boolean var8 = var4 == EnumHand.MAIN_HAND;
      EnumHandSide var9 = var8 ? var1.getPrimaryHand() : var1.getPrimaryHand().opposite();
      GlStateManager.pushMatrix();
      if (var6 == null) {
         if (var8 && !var1.isInvisible()) {
            this.renderArmFirstPerson(var7, var5, var9);
         }
      } else if (var6.getItem() instanceof ItemMap) {
         if (var8 && this.itemStackOffHand == null) {
            this.renderMapFirstPerson(var3, var7, var5);
         } else {
            this.renderMapFirstPersonSide(var7, var9, var5, var6);
         }
      } else {
         boolean var10 = var9 == EnumHandSide.RIGHT;
         if (var1.isHandActive() && var1.getItemInUseCount() > 0 && var1.getActiveHand() == var4) {
            int var17 = var10 ? 1 : -1;
            switch(var6.getItemUseAction()) {
            case NONE:
               this.transformSideFirstPerson(var9, var7);
               break;
            case EAT:
            case DRINK:
               this.transformEatFirstPerson(var2, var9, var6);
               this.transformSideFirstPerson(var9, var7);
               break;
            case BLOCK:
               this.transformSideFirstPerson(var9, var7);
               break;
            case BOW:
               this.transformSideFirstPerson(var9, var7);
               GlStateManager.translate((float)var17 * -0.2785682F, 0.18344387F, 0.15731531F);
               GlStateManager.rotate(-13.935F, 1.0F, 0.0F, 0.0F);
               GlStateManager.rotate((float)var17 * 35.3F, 0.0F, 1.0F, 0.0F);
               GlStateManager.rotate((float)var17 * -9.785F, 0.0F, 0.0F, 1.0F);
               float var18 = (float)var6.getMaxItemUseDuration() - ((float)this.mc.player.getItemInUseCount() - var2 + 1.0F);
               float var19 = var18 / 20.0F;
               var19 = (var19 * var19 + var19 * 2.0F) / 3.0F;
               if (var19 > 1.0F) {
                  var19 = 1.0F;
               }

               if (var19 > 0.1F) {
                  float var21 = MathHelper.sin((var18 - 0.1F) * 1.3F);
                  float var15 = var19 - 0.1F;
                  float var16 = var21 * var15;
                  GlStateManager.translate(var16 * 0.0F, var16 * 0.004F, var16 * 0.0F);
               }

               GlStateManager.translate(var19 * 0.0F, var19 * 0.0F, var19 * 0.04F);
               GlStateManager.scale(1.0F, 1.0F, 1.0F + var19 * 0.2F);
               GlStateManager.rotate((float)var17 * 45.0F, 0.0F, -1.0F, 0.0F);
            }
         } else {
            float var11 = -0.4F * MathHelper.sin(MathHelper.sqrt(var5) * 3.1415927F);
            float var12 = 0.2F * MathHelper.sin(MathHelper.sqrt(var5) * 6.2831855F);
            float var13 = -0.2F * MathHelper.sin(var5 * 3.1415927F);
            int var14 = var10 ? 1 : -1;
            GlStateManager.translate((float)var14 * var11, var12, var13);
            this.transformSideFirstPerson(var9, var7);
            this.transformFirstPerson(var9, var5);
         }

         this.renderItemSide(var1, var6, var10 ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, !var10);
      }

      GlStateManager.popMatrix();
   }

   public void renderOverlays(float var1) {
      GlStateManager.disableAlpha();
      if (this.mc.player.isEntityInsideOpaqueBlock()) {
         IBlockState var2 = this.mc.world.getBlockState(new BlockPos(this.mc.player));
         BlockPos var3 = new BlockPos(this.mc.player);
         EntityPlayerSP var4 = this.mc.player;

         for(int var5 = 0; var5 < 8; ++var5) {
            double var6 = var4.posX + (double)(((float)((var5 >> 0) % 2) - 0.5F) * var4.width * 0.8F);
            double var8 = var4.posY + (double)(((float)((var5 >> 1) % 2) - 0.5F) * 0.1F);
            double var10 = var4.posZ + (double)(((float)((var5 >> 2) % 2) - 0.5F) * var4.width * 0.8F);
            BlockPos var12 = new BlockPos(var6, var8 + (double)var4.getEyeHeight(), var10);
            IBlockState var13 = this.mc.world.getBlockState(var12);
            if (var13.getBlock().causesSuffocation()) {
               var2 = var13;
               var3 = var12;
            }
         }

         if (var2.getRenderType() != EnumBlockRenderType.INVISIBLE && !ForgeEventFactory.renderBlockOverlay(this.mc.player, var1, OverlayType.BLOCK, var2, var3)) {
            this.renderBlockInHand(var1, this.mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(var2));
         }
      }

      if (!this.mc.player.isSpectator()) {
         if (this.mc.player.isInsideOfMaterial(Material.WATER) && !ForgeEventFactory.renderWaterOverlay(this.mc.player, var1)) {
            this.renderWaterOverlayTexture(var1);
         }

         if (this.mc.player.isBurning() && !ForgeEventFactory.renderFireOverlay(this.mc.player, var1)) {
            this.renderFireInFirstPerson(var1);
         }
      }

      GlStateManager.enableAlpha();
   }

   private void renderBlockInHand(float var1, TextureAtlasSprite var2) {
      this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
      Tessellator var3 = Tessellator.getInstance();
      VertexBuffer var4 = var3.getBuffer();
      float var5 = 0.1F;
      GlStateManager.color(0.1F, 0.1F, 0.1F, 0.5F);
      GlStateManager.pushMatrix();
      float var6 = -1.0F;
      float var7 = 1.0F;
      float var8 = -1.0F;
      float var9 = 1.0F;
      float var10 = -0.5F;
      float var11 = var2.getMinU();
      float var12 = var2.getMaxU();
      float var13 = var2.getMinV();
      float var14 = var2.getMaxV();
      var4.begin(7, DefaultVertexFormats.POSITION_TEX);
      var4.pos(-1.0D, -1.0D, -0.5D).tex((double)var12, (double)var14).endVertex();
      var4.pos(1.0D, -1.0D, -0.5D).tex((double)var11, (double)var14).endVertex();
      var4.pos(1.0D, 1.0D, -0.5D).tex((double)var11, (double)var13).endVertex();
      var4.pos(-1.0D, 1.0D, -0.5D).tex((double)var12, (double)var13).endVertex();
      var3.draw();
      GlStateManager.popMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
   }

   private void renderWaterOverlayTexture(float var1) {
      this.mc.getTextureManager().bindTexture(RES_UNDERWATER_OVERLAY);
      Tessellator var2 = Tessellator.getInstance();
      VertexBuffer var3 = var2.getBuffer();
      float var4 = this.mc.player.getBrightness(var1);
      GlStateManager.color(var4, var4, var4, 0.5F);
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      GlStateManager.pushMatrix();
      float var5 = 4.0F;
      float var6 = -1.0F;
      float var7 = 1.0F;
      float var8 = -1.0F;
      float var9 = 1.0F;
      float var10 = -0.5F;
      float var11 = -this.mc.player.rotationYaw / 64.0F;
      float var12 = this.mc.player.rotationPitch / 64.0F;
      var3.begin(7, DefaultVertexFormats.POSITION_TEX);
      var3.pos(-1.0D, -1.0D, -0.5D).tex((double)(4.0F + var11), (double)(4.0F + var12)).endVertex();
      var3.pos(1.0D, -1.0D, -0.5D).tex((double)(0.0F + var11), (double)(4.0F + var12)).endVertex();
      var3.pos(1.0D, 1.0D, -0.5D).tex((double)(0.0F + var11), (double)(0.0F + var12)).endVertex();
      var3.pos(-1.0D, 1.0D, -0.5D).tex((double)(4.0F + var11), (double)(0.0F + var12)).endVertex();
      var2.draw();
      GlStateManager.popMatrix();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.disableBlend();
   }

   private void renderFireInFirstPerson(float var1) {
      Tessellator var2 = Tessellator.getInstance();
      VertexBuffer var3 = var2.getBuffer();
      GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
      GlStateManager.depthFunc(519);
      GlStateManager.depthMask(false);
      GlStateManager.enableBlend();
      GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
      float var4 = 1.0F;

      for(int var5 = 0; var5 < 2; ++var5) {
         GlStateManager.pushMatrix();
         TextureAtlasSprite var6 = this.mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/fire_layer_1");
         this.mc.getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
         float var7 = var6.getMinU();
         float var8 = var6.getMaxU();
         float var9 = var6.getMinV();
         float var10 = var6.getMaxV();
         float var11 = -0.5F;
         float var12 = 0.5F;
         float var13 = -0.5F;
         float var14 = 0.5F;
         float var15 = -0.5F;
         GlStateManager.translate((float)(-(var5 * 2 - 1)) * 0.24F, -0.3F, 0.0F);
         GlStateManager.rotate((float)(var5 * 2 - 1) * 10.0F, 0.0F, 1.0F, 0.0F);
         var3.begin(7, DefaultVertexFormats.POSITION_TEX);
         var3.pos(-0.5D, -0.5D, -0.5D).tex((double)var8, (double)var10).endVertex();
         var3.pos(0.5D, -0.5D, -0.5D).tex((double)var7, (double)var10).endVertex();
         var3.pos(0.5D, 0.5D, -0.5D).tex((double)var7, (double)var9).endVertex();
         var3.pos(-0.5D, 0.5D, -0.5D).tex((double)var8, (double)var9).endVertex();
         var2.draw();
         GlStateManager.popMatrix();
      }

      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      GlStateManager.disableBlend();
      GlStateManager.depthMask(true);
      GlStateManager.depthFunc(515);
   }

   public void updateEquippedItem() {
      this.prevEquippedProgressMainHand = this.equippedProgressMainHand;
      this.prevEquippedProgressOffHand = this.equippedProgressOffHand;
      EntityPlayerSP var1 = this.mc.player;
      ItemStack var2 = var1.getHeldItemMainhand();
      ItemStack var3 = var1.getHeldItemOffhand();
      if (var1.isRowingBoat()) {
         this.equippedProgressMainHand = MathHelper.clamp(this.equippedProgressMainHand - 0.4F, 0.0F, 1.0F);
         this.equippedProgressOffHand = MathHelper.clamp(this.equippedProgressOffHand - 0.4F, 0.0F, 1.0F);
      } else {
         float var4 = var1.getCooledAttackStrength(1.0F);
         this.equippedProgressMainHand += MathHelper.clamp((!ForgeHooksClient.shouldCauseReequipAnimation(this.itemStackMainHand, var2, var1.inventory.currentItem) ? var4 * var4 * var4 : 0.0F) - this.equippedProgressMainHand, -0.4F, 0.4F);
         this.equippedProgressOffHand += MathHelper.clamp((float)(!ForgeHooksClient.shouldCauseReequipAnimation(this.itemStackOffHand, var3, -1) ? 1 : 0) - this.equippedProgressOffHand, -0.4F, 0.4F);
      }

      if (this.equippedProgressMainHand < 0.1F) {
         this.itemStackMainHand = var2;
      }

      if (this.equippedProgressOffHand < 0.1F) {
         this.itemStackOffHand = var3;
      }

   }

   public void resetEquippedProgress(EnumHand var1) {
      if (var1 == EnumHand.MAIN_HAND) {
         this.equippedProgressMainHand = 0.0F;
      } else {
         this.equippedProgressOffHand = 0.0F;
      }

   }
}
