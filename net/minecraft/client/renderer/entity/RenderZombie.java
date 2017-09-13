package net.minecraft.client.renderer.entity;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.model.ModelZombieVillager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerVillagerArmor;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.ZombieType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderZombie extends RenderBiped {
   private static final ResourceLocation ZOMBIE_VILLAGER_TEXTURES = new ResourceLocation("textures/entity/zombie_villager/zombie_villager.png");
   private static final ResourceLocation ZOMBIE_VILLAGER_FARMER_LOCATION = new ResourceLocation("textures/entity/zombie_villager/zombie_farmer.png");
   private static final ResourceLocation ZOMBIE_VILLAGER_LIBRARIAN_LOC = new ResourceLocation("textures/entity/zombie_villager/zombie_librarian.png");
   private static final ResourceLocation ZOMBIE_VILLAGER_PRIEST_LOCATION = new ResourceLocation("textures/entity/zombie_villager/zombie_priest.png");
   private static final ResourceLocation ZOMBIE_VILLAGER_SMITH_LOCATION = new ResourceLocation("textures/entity/zombie_villager/zombie_smith.png");
   private static final ResourceLocation ZOMBIE_VILLAGER_BUTCHER_LOCATION = new ResourceLocation("textures/entity/zombie_villager/zombie_butcher.png");
   private static final ResourceLocation ZOMBIE_TEXTURES = new ResourceLocation("textures/entity/zombie/zombie.png");
   private static final ResourceLocation HUSK_ZOMBIE_TEXTURES = new ResourceLocation("textures/entity/zombie/husk.png");
   private final ModelBiped defaultModel;
   private final ModelZombieVillager zombieVillagerModel;
   private final List villagerLayers;
   private final List defaultLayers;

   public RenderZombie(RenderManager var1) {
      super(renderManagerIn, new ModelZombie(), 0.5F, 1.0F);
      LayerRenderer layerrenderer = (LayerRenderer)this.layerRenderers.get(0);
      this.defaultModel = this.modelBipedMain;
      this.zombieVillagerModel = new ModelZombieVillager();
      this.addLayer(new LayerHeldItem(this));
      LayerBipedArmor layerbipedarmor = new LayerBipedArmor(this) {
         protected void initArmor() {
            this.modelLeggings = new ModelZombie(0.5F, true);
            this.modelArmor = new ModelZombie(1.0F, true);
         }
      };
      this.addLayer(layerbipedarmor);
      this.defaultLayers = Lists.newArrayList(this.layerRenderers);
      if (layerrenderer instanceof LayerCustomHead) {
         this.removeLayer(layerrenderer);
         this.addLayer(new LayerCustomHead(this.zombieVillagerModel.bipedHead));
      }

      this.removeLayer(layerbipedarmor);
      this.addLayer(new LayerVillagerArmor(this));
      this.villagerLayers = Lists.newArrayList(this.layerRenderers);
   }

   protected void preRenderCallback(EntityZombie var1, float var2) {
      if (entitylivingbaseIn.getZombieType() == ZombieType.HUSK) {
         float f = 1.0625F;
         GlStateManager.scale(1.0625F, 1.0625F, 1.0625F);
      }

      super.preRenderCallback(entitylivingbaseIn, partialTickTime);
   }

   public void doRender(EntityZombie var1, double var2, double var4, double var6, float var8, float var9) {
      this.swapArmor(entity);
      super.doRender(entity, x, y, z, entityYaw, partialTicks);
   }

   protected ResourceLocation getEntityTexture(EntityZombie var1) {
      if (entity.isVillager()) {
         return entity.getVillagerTypeForge().getZombieSkin();
      } else {
         return entity.getZombieType() == ZombieType.HUSK ? HUSK_ZOMBIE_TEXTURES : ZOMBIE_TEXTURES;
      }
   }

   private void swapArmor(EntityZombie var1) {
      if (zombie.isVillager()) {
         this.mainModel = this.zombieVillagerModel;
         this.layerRenderers = this.villagerLayers;
      } else {
         this.mainModel = this.defaultModel;
         this.layerRenderers = this.defaultLayers;
      }

      this.modelBipedMain = (ModelBiped)this.mainModel;
   }

   protected void applyRotations(EntityZombie var1, float var2, float var3, float var4) {
      if (entityLiving.isConverting()) {
         p_77043_3_ += (float)(Math.cos((double)entityLiving.ticksExisted * 3.25D) * 3.141592653589793D * 0.25D);
      }

      super.applyRotations(entityLiving, p_77043_2_, p_77043_3_, partialTicks);
   }
}
