package net.minecraft.client.renderer.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.event.RenderItemInFrameEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderItemFrame extends Render {
   private static final ResourceLocation MAP_BACKGROUND_TEXTURES = new ResourceLocation("textures/map/map_background.png");
   private final Minecraft mc = Minecraft.getMinecraft();
   private final ModelResourceLocation itemFrameModel = new ModelResourceLocation("item_frame", "normal");
   private final ModelResourceLocation mapModel = new ModelResourceLocation("item_frame", "map");
   private final RenderItem itemRenderer;

   public RenderItemFrame(RenderManager var1, RenderItem var2) {
      super(var1);
      this.itemRenderer = var2;
   }

   public void doRender(EntityItemFrame var1, double var2, double var4, double var6, float var8, float var9) {
      GlStateManager.pushMatrix();
      BlockPos var10 = var1.getHangingPosition();
      double var11 = (double)var10.getX() - var1.posX + var2;
      double var13 = (double)var10.getY() - var1.posY + var4;
      double var15 = (double)var10.getZ() - var1.posZ + var6;
      GlStateManager.translate(var11 + 0.5D, var13 + 0.5D, var15 + 0.5D);
      GlStateManager.rotate(180.0F - var1.rotationYaw, 0.0F, 1.0F, 0.0F);
      this.renderManager.renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
      BlockRendererDispatcher var17 = this.mc.getBlockRendererDispatcher();
      ModelManager var18 = var17.getBlockModelShapes().getModelManager();
      IBakedModel var19;
      if (var1.getDisplayedItem() != null && var1.getDisplayedItem().getItem() == Items.FILLED_MAP) {
         var19 = var18.getModel(this.mapModel);
      } else {
         var19 = var18.getModel(this.itemFrameModel);
      }

      GlStateManager.pushMatrix();
      GlStateManager.translate(-0.5F, -0.5F, -0.5F);
      if (this.renderOutlines) {
         GlStateManager.enableColorMaterial();
         GlStateManager.enableOutlineMode(this.getTeamColor(var1));
      }

      var17.getBlockModelRenderer().renderModelBrightnessColor(var19, 1.0F, 1.0F, 1.0F, 1.0F);
      if (this.renderOutlines) {
         GlStateManager.disableOutlineMode();
         GlStateManager.disableColorMaterial();
      }

      GlStateManager.popMatrix();
      GlStateManager.translate(0.0F, 0.0F, 0.4375F);
      this.renderItem(var1);
      GlStateManager.popMatrix();
      this.renderName(var1, var2 + (double)((float)var1.facingDirection.getFrontOffsetX() * 0.3F), var4 - 0.25D, var6 + (double)((float)var1.facingDirection.getFrontOffsetZ() * 0.3F));
   }

   protected ResourceLocation getEntityTexture(EntityItemFrame var1) {
      return null;
   }

   private void renderItem(EntityItemFrame var1) {
      ItemStack var2 = var1.getDisplayedItem();
      if (var2 != null) {
         EntityItem var3 = new EntityItem(var1.world, 0.0D, 0.0D, 0.0D, var2);
         Item var4 = var3.getEntityItem().getItem();
         var3.getEntityItem().stackSize = 1;
         var3.hoverStart = 0.0F;
         GlStateManager.pushMatrix();
         GlStateManager.disableLighting();
         int var5 = var1.getRotation();
         if (var4 instanceof ItemMap) {
            var5 = var5 % 4 * 2;
         }

         GlStateManager.rotate((float)var5 * 360.0F / 8.0F, 0.0F, 0.0F, 1.0F);
         RenderItemInFrameEvent var6 = new RenderItemInFrameEvent(var1, this);
         if (!MinecraftForge.EVENT_BUS.post(var6)) {
            if (var4 instanceof ItemMap) {
               this.renderManager.renderEngine.bindTexture(MAP_BACKGROUND_TEXTURES);
               GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
               float var7 = 0.0078125F;
               GlStateManager.scale(0.0078125F, 0.0078125F, 0.0078125F);
               GlStateManager.translate(-64.0F, -64.0F, 0.0F);
               MapData var8 = Items.FILLED_MAP.getMapData(var3.getEntityItem(), var1.world);
               GlStateManager.translate(0.0F, 0.0F, -1.0F);
               if (var8 != null) {
                  this.mc.entityRenderer.getMapItemRenderer().renderMap(var8, true);
               }
            } else {
               GlStateManager.scale(0.5F, 0.5F, 0.5F);
               if (!this.itemRenderer.shouldRenderItemIn3D(var3.getEntityItem()) || var4 instanceof ItemSkull) {
                  GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
               }

               GlStateManager.pushAttrib();
               RenderHelper.enableStandardItemLighting();
               this.itemRenderer.renderItem(var3.getEntityItem(), ItemCameraTransforms.TransformType.FIXED);
               RenderHelper.disableStandardItemLighting();
               GlStateManager.popAttrib();
            }
         }

         GlStateManager.enableLighting();
         GlStateManager.popMatrix();
      }

   }

   protected void renderName(EntityItemFrame var1, double var2, double var4, double var6) {
      if (Minecraft.isGuiEnabled() && var1.getDisplayedItem() != null && var1.getDisplayedItem().hasDisplayName() && this.renderManager.pointedEntity == var1) {
         double var8 = var1.getDistanceSqToEntity(this.renderManager.renderViewEntity);
         float var10 = var1.isSneaking() ? 32.0F : 64.0F;
         if (var8 < (double)(var10 * var10)) {
            String var11 = var1.getDisplayedItem().getDisplayName();
            this.renderLivingLabel(var1, var11, var2, var4, var6, 64);
         }
      }

   }
}
