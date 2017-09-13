package net.minecraft.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.model.pipeline.ForgeBlockModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class BlockRendererDispatcher implements IResourceManagerReloadListener {
   private final BlockModelShapes blockModelShapes;
   private final BlockModelRenderer blockModelRenderer;
   private final ChestRenderer chestRenderer = new ChestRenderer();
   private final BlockFluidRenderer fluidRenderer;

   public BlockRendererDispatcher(BlockModelShapes var1, BlockColors var2) {
      this.blockModelShapes = var1;
      this.blockModelRenderer = new ForgeBlockModelRenderer(var2);
      this.fluidRenderer = new BlockFluidRenderer(var2);
   }

   public BlockModelShapes getBlockModelShapes() {
      return this.blockModelShapes;
   }

   public void renderBlockDamage(IBlockState var1, BlockPos var2, TextureAtlasSprite var3, IBlockAccess var4) {
      if (var1.getRenderType() == EnumBlockRenderType.MODEL) {
         var1 = var1.getActualState(var4, var2);
         IBakedModel var5 = this.blockModelShapes.getModelForState(var1);
         IBakedModel var6 = ForgeHooksClient.getDamageModel(var5, var3, var1, var4, var2);
         this.blockModelRenderer.renderModel(var4, var6, var1, var2, Tessellator.getInstance().getBuffer(), true);
      }

   }

   public boolean renderBlock(IBlockState var1, BlockPos var2, IBlockAccess var3, VertexBuffer var4) {
      try {
         EnumBlockRenderType var5 = var1.getRenderType();
         if (var5 == EnumBlockRenderType.INVISIBLE) {
            return false;
         } else {
            if (var3.getWorldType() != WorldType.DEBUG_WORLD) {
               try {
                  var1 = var1.getActualState(var3, var2);
               } catch (Exception var8) {
                  ;
               }
            }

            switch(var5) {
            case MODEL:
               IBakedModel var11 = this.getModelForState(var1);
               var1 = var1.getBlock().getExtendedState(var1, var3, var2);
               return this.blockModelRenderer.renderModel(var3, var11, var1, var2, var4, true);
            case ENTITYBLOCK_ANIMATED:
               return false;
            case LIQUID:
               return this.fluidRenderer.renderFluid(var3, var1, var2, var4);
            default:
               return false;
            }
         }
      } catch (Throwable var9) {
         CrashReport var6 = CrashReport.makeCrashReport(var9, "Tesselating block in world");
         CrashReportCategory var7 = var6.makeCategory("Block being tesselated");
         CrashReportCategory.addBlockInfo(var7, var2, var1.getBlock(), var1.getBlock().getMetaFromState(var1));
         throw new ReportedException(var6);
      }
   }

   public BlockModelRenderer getBlockModelRenderer() {
      return this.blockModelRenderer;
   }

   public IBakedModel getModelForState(IBlockState var1) {
      return this.blockModelShapes.getModelForState(var1);
   }

   public void renderBlockBrightness(IBlockState var1, float var2) {
      EnumBlockRenderType var3 = var1.getRenderType();
      if (var3 != EnumBlockRenderType.INVISIBLE) {
         switch(var3) {
         case MODEL:
            IBakedModel var4 = this.getModelForState(var1);
            this.blockModelRenderer.renderModelBrightness(var4, var1, var2, true);
            break;
         case ENTITYBLOCK_ANIMATED:
            this.chestRenderer.renderChestBrightness(var1.getBlock(), var2);
         case LIQUID:
         }
      }

   }

   public boolean isEntityBlockAnimated(Block var1) {
      if (var1 == null) {
         return false;
      } else {
         EnumBlockRenderType var2 = var1.getDefaultState().getRenderType();
         return var2 == EnumBlockRenderType.MODEL ? false : var2 == EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
      }
   }

   public void onResourceManagerReload(IResourceManager var1) {
      this.fluidRenderer.initAtlasSprites();
   }
}
