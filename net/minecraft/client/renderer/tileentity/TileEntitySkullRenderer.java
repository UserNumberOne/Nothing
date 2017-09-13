package net.minecraft.client.renderer.tileentity;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelDragonHead;
import net.minecraft.client.model.ModelHumanoidHead;
import net.minecraft.client.model.ModelSkeletonHead;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntitySkullRenderer extends TileEntitySpecialRenderer {
   private static final ResourceLocation SKELETON_TEXTURES = new ResourceLocation("textures/entity/skeleton/skeleton.png");
   private static final ResourceLocation WITHER_SKELETON_TEXTURES = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");
   private static final ResourceLocation ZOMBIE_TEXTURES = new ResourceLocation("textures/entity/zombie/zombie.png");
   private static final ResourceLocation CREEPER_TEXTURES = new ResourceLocation("textures/entity/creeper/creeper.png");
   private static final ResourceLocation DRAGON_TEXTURES = new ResourceLocation("textures/entity/enderdragon/dragon.png");
   private final ModelDragonHead dragonHead = new ModelDragonHead(0.0F);
   public static TileEntitySkullRenderer instance;
   private final ModelSkeletonHead skeletonHead = new ModelSkeletonHead(0, 0, 64, 32);
   private final ModelSkeletonHead humanoidHead = new ModelHumanoidHead();

   public void renderTileEntityAt(TileEntitySkull var1, double var2, double var4, double var6, float var8, int var9) {
      EnumFacing var10 = EnumFacing.getFront(var1.getBlockMetadata() & 7);
      float var11 = var1.getAnimationProgress(var8);
      this.renderSkull((float)var2, (float)var4, (float)var6, var10, (float)(var1.getSkullRotation() * 360) / 16.0F, var1.getSkullType(), var1.getPlayerProfile(), var9, var11);
   }

   public void setRendererDispatcher(TileEntityRendererDispatcher var1) {
      super.setRendererDispatcher(var1);
      instance = this;
   }

   public void renderSkull(float var1, float var2, float var3, EnumFacing var4, float var5, int var6, @Nullable GameProfile var7, int var8, float var9) {
      Object var10 = this.skeletonHead;
      if (var8 >= 0) {
         this.bindTexture(DESTROY_STAGES[var8]);
         GlStateManager.matrixMode(5890);
         GlStateManager.pushMatrix();
         GlStateManager.scale(4.0F, 2.0F, 1.0F);
         GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
         GlStateManager.matrixMode(5888);
      } else {
         switch(var6) {
         case 0:
         default:
            this.bindTexture(SKELETON_TEXTURES);
            break;
         case 1:
            this.bindTexture(WITHER_SKELETON_TEXTURES);
            break;
         case 2:
            this.bindTexture(ZOMBIE_TEXTURES);
            var10 = this.humanoidHead;
            break;
         case 3:
            var10 = this.humanoidHead;
            ResourceLocation var11 = DefaultPlayerSkin.getDefaultSkinLegacy();
            if (var7 != null) {
               Minecraft var12 = Minecraft.getMinecraft();
               Map var13 = var12.getSkinManager().loadSkinFromCache(var7);
               if (var13.containsKey(Type.SKIN)) {
                  var11 = var12.getSkinManager().loadSkin((MinecraftProfileTexture)var13.get(Type.SKIN), Type.SKIN);
               } else {
                  UUID var14 = EntityPlayer.getUUID(var7);
                  var11 = DefaultPlayerSkin.getDefaultSkin(var14);
               }
            }

            this.bindTexture(var11);
            break;
         case 4:
            this.bindTexture(CREEPER_TEXTURES);
            break;
         case 5:
            this.bindTexture(DRAGON_TEXTURES);
            var10 = this.dragonHead;
         }
      }

      GlStateManager.pushMatrix();
      GlStateManager.disableCull();
      if (var4 == EnumFacing.UP) {
         GlStateManager.translate(var1 + 0.5F, var2, var3 + 0.5F);
      } else {
         switch(var4) {
         case NORTH:
            GlStateManager.translate(var1 + 0.5F, var2 + 0.25F, var3 + 0.74F);
            break;
         case SOUTH:
            GlStateManager.translate(var1 + 0.5F, var2 + 0.25F, var3 + 0.26F);
            var5 = 180.0F;
            break;
         case WEST:
            GlStateManager.translate(var1 + 0.74F, var2 + 0.25F, var3 + 0.5F);
            var5 = 270.0F;
            break;
         case EAST:
         default:
            GlStateManager.translate(var1 + 0.26F, var2 + 0.25F, var3 + 0.5F);
            var5 = 90.0F;
         }
      }

      float var15 = 0.0625F;
      GlStateManager.enableRescaleNormal();
      GlStateManager.scale(-1.0F, -1.0F, 1.0F);
      GlStateManager.enableAlpha();
      if (var6 == 3) {
         GlStateManager.enableBlendProfile(GlStateManager.Profile.PLAYER_SKIN);
      }

      ((ModelBase)var10).render((Entity)null, var9, 0.0F, 0.0F, var5, 0.0F, 0.0625F);
      GlStateManager.popMatrix();
      if (var8 >= 0) {
         GlStateManager.matrixMode(5890);
         GlStateManager.popMatrix();
         GlStateManager.matrixMode(5888);
      }

   }
}
