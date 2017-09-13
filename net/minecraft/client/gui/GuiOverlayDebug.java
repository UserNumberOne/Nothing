package net.minecraft.client.gui;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.Display;

@SideOnly(Side.CLIENT)
public class GuiOverlayDebug extends Gui {
   private final Minecraft mc;
   private final FontRenderer fontRenderer;

   public GuiOverlayDebug(Minecraft var1) {
      this.mc = var1;
      this.fontRenderer = var1.fontRendererObj;
   }

   public void renderDebugInfo(ScaledResolution var1) {
      this.mc.mcProfiler.startSection("debug");
      GlStateManager.pushMatrix();
      this.renderDebugInfoLeft();
      this.renderDebugInfoRight(var1);
      GlStateManager.popMatrix();
      if (this.mc.gameSettings.showLagometer) {
         this.renderLagometer();
      }

      this.mc.mcProfiler.endSection();
   }

   protected void renderDebugInfoLeft() {
      List var1 = this.call();
      var1.add("");
      var1.add("Debug: Pie [shift]: " + (this.mc.gameSettings.showDebugProfilerChart ? "visible" : "hidden") + " FPS [alt]: " + (this.mc.gameSettings.showLagometer ? "visible" : "hidden"));
      var1.add("For help: press F3 + Q");

      for(int var2 = 0; var2 < var1.size(); ++var2) {
         String var3 = (String)var1.get(var2);
         if (!Strings.isNullOrEmpty(var3)) {
            int var4 = this.fontRenderer.FONT_HEIGHT;
            int var5 = this.fontRenderer.getStringWidth(var3);
            boolean var6 = true;
            int var7 = 2 + var4 * var2;
            drawRect(1, var7 - 1, 2 + var5 + 1, var7 + var4 - 1, -1873784752);
            this.fontRenderer.drawString(var3, 2, var7, 14737632);
         }
      }

   }

   protected void renderDebugInfoRight(ScaledResolution var1) {
      List var2 = this.getDebugInfoRight();

      for(int var3 = 0; var3 < var2.size(); ++var3) {
         String var4 = (String)var2.get(var3);
         if (!Strings.isNullOrEmpty(var4)) {
            int var5 = this.fontRenderer.FONT_HEIGHT;
            int var6 = this.fontRenderer.getStringWidth(var4);
            int var7 = var1.getScaledWidth() - 2 - var6;
            int var8 = 2 + var5 * var3;
            drawRect(var7 - 1, var8 - 1, var7 + var6 + 1, var8 + var5 - 1, -1873784752);
            this.fontRenderer.drawString(var4, var7, var8, 14737632);
         }
      }

   }

   protected List call() {
      BlockPos var1 = new BlockPos(this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().getEntityBoundingBox().minY, this.mc.getRenderViewEntity().posZ);
      if (this.mc.isReducedDebug()) {
         return Lists.newArrayList(new String[]{"Minecraft 1.10.2 (" + this.mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.mc.debug, this.mc.renderGlobal.getDebugInfoRenders(), this.mc.renderGlobal.getDebugInfoEntities(), "P: " + this.mc.effectRenderer.getStatistics() + ". T: " + this.mc.world.getDebugLoadedEntities(), this.mc.world.getProviderName(), "", String.format("Chunk-relative: %d %d %d", var1.getX() & 15, var1.getY() & 15, var1.getZ() & 15)});
      } else {
         Entity var2 = this.mc.getRenderViewEntity();
         EnumFacing var3 = var2.getHorizontalFacing();
         String var4 = "Invalid";
         switch(var3) {
         case NORTH:
            var4 = "Towards negative Z";
            break;
         case SOUTH:
            var4 = "Towards positive Z";
            break;
         case WEST:
            var4 = "Towards negative X";
            break;
         case EAST:
            var4 = "Towards positive X";
         }

         ArrayList var5 = Lists.newArrayList(new String[]{"Minecraft 1.10.2 (" + this.mc.getVersion() + "/" + ClientBrandRetriever.getClientModName() + ("release".equalsIgnoreCase(this.mc.getVersionType()) ? "" : "/" + this.mc.getVersionType()) + ")", this.mc.debug, this.mc.renderGlobal.getDebugInfoRenders(), this.mc.renderGlobal.getDebugInfoEntities(), "P: " + this.mc.effectRenderer.getStatistics() + ". T: " + this.mc.world.getDebugLoadedEntities(), this.mc.world.getProviderName(), "", String.format("XYZ: %.3f / %.5f / %.3f", this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().getEntityBoundingBox().minY, this.mc.getRenderViewEntity().posZ), String.format("Block: %d %d %d", var1.getX(), var1.getY(), var1.getZ()), String.format("Chunk: %d %d %d in %d %d %d", var1.getX() & 15, var1.getY() & 15, var1.getZ() & 15, var1.getX() >> 4, var1.getY() >> 4, var1.getZ() >> 4), String.format("Facing: %s (%s) (%.1f / %.1f)", var3, var4, MathHelper.wrapDegrees(var2.rotationYaw), MathHelper.wrapDegrees(var2.rotationPitch))});
         if (this.mc.world != null) {
            Chunk var6 = this.mc.world.getChunkFromBlockCoords(var1);
            if (this.mc.world.isBlockLoaded(var1) && var1.getY() >= 0 && var1.getY() < 256) {
               if (!var6.isEmpty()) {
                  var5.add("Biome: " + var6.getBiome(var1, this.mc.world.getBiomeProvider()).getBiomeName());
                  var5.add("Light: " + var6.getLightSubtracted(var1, 0) + " (" + var6.getLightFor(EnumSkyBlock.SKY, var1) + " sky, " + var6.getLightFor(EnumSkyBlock.BLOCK, var1) + " block)");
                  DifficultyInstance var7 = this.mc.world.getDifficultyForLocation(var1);
                  if (this.mc.isIntegratedServerRunning() && this.mc.getIntegratedServer() != null) {
                     EntityPlayerMP var8 = this.mc.getIntegratedServer().getPlayerList().getPlayerByUUID(this.mc.player.getUniqueID());
                     if (var8 != null) {
                        var7 = var8.world.getDifficultyForLocation(new BlockPos(var8));
                     }
                  }

                  var5.add(String.format("Local Difficulty: %.2f // %.2f (Day %d)", var7.getAdditionalDifficulty(), var7.getClampedAdditionalDifficulty(), this.mc.world.getWorldTime() / 24000L));
               } else {
                  var5.add("Waiting for chunk...");
               }
            } else {
               var5.add("Outside of world...");
            }
         }

         if (this.mc.entityRenderer != null && this.mc.entityRenderer.isShaderActive()) {
            var5.add("Shader: " + this.mc.entityRenderer.getShaderGroup().getShaderGroupName());
         }

         if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK && this.mc.objectMouseOver.getBlockPos() != null) {
            BlockPos var9 = this.mc.objectMouseOver.getBlockPos();
            var5.add(String.format("Looking at: %d %d %d", var9.getX(), var9.getY(), var9.getZ()));
         }

         return var5;
      }
   }

   protected List getDebugInfoRight() {
      long var1 = Runtime.getRuntime().maxMemory();
      long var3 = Runtime.getRuntime().totalMemory();
      long var5 = Runtime.getRuntime().freeMemory();
      long var7 = var3 - var5;
      ArrayList var9 = Lists.newArrayList(new String[]{String.format("Java: %s %dbit", System.getProperty("java.version"), this.mc.isJava64bit() ? 64 : 32), String.format("Mem: % 2d%% %03d/%03dMB", var7 * 100L / var1, bytesToMb(var7), bytesToMb(var1)), String.format("Allocated: % 2d%% %03dMB", var3 * 100L / var1, bytesToMb(var3)), "", String.format("CPU: %s", OpenGlHelper.getCpu()), "", String.format("Display: %dx%d (%s)", Display.getWidth(), Display.getHeight(), GlStateManager.glGetString(7936)), GlStateManager.glGetString(7937), GlStateManager.glGetString(7938)});
      var9.add("");
      var9.addAll(FMLCommonHandler.instance().getBrandings(false));
      if (this.mc.isReducedDebug()) {
         return var9;
      } else {
         if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK && this.mc.objectMouseOver.getBlockPos() != null) {
            BlockPos var10 = this.mc.objectMouseOver.getBlockPos();
            IBlockState var11 = this.mc.world.getBlockState(var10);
            if (this.mc.world.getWorldType() != WorldType.DEBUG_WORLD) {
               var11 = var11.getActualState(this.mc.world, var10);
            }

            var9.add("");
            var9.add(String.valueOf(Block.REGISTRY.getNameForObject(var11.getBlock())));

            IProperty var14;
            String var16;
            for(UnmodifiableIterator var12 = var11.getProperties().entrySet().iterator(); var12.hasNext(); var9.add(var14.getName() + ": " + var16)) {
               Entry var13 = (Entry)var12.next();
               var14 = (IProperty)var13.getKey();
               Comparable var15 = (Comparable)var13.getValue();
               var16 = var14.getName(var15);
               if (Boolean.TRUE.equals(var15)) {
                  var16 = TextFormatting.GREEN + var16;
               } else if (Boolean.FALSE.equals(var15)) {
                  var16 = TextFormatting.RED + var16;
               }
            }
         }

         return var9;
      }
   }

   public void renderLagometer() {
      GlStateManager.disableDepth();
      FrameTimer var1 = this.mc.getFrameTimer();
      int var2 = var1.getLastIndex();
      int var3 = var1.getIndex();
      long[] var4 = var1.getFrames();
      ScaledResolution var5 = new ScaledResolution(this.mc);
      int var6 = var2;
      int var7 = 0;
      drawRect(0, var5.getScaledHeight() - 60, 240, var5.getScaledHeight(), -1873784752);

      while(var6 != var3) {
         int var8 = var1.getLagometerValue(var4[var6], 30);
         int var9 = this.getFrameColor(MathHelper.clamp(var8, 0, 60), 0, 30, 60);
         this.drawVerticalLine(var7, var5.getScaledHeight(), var5.getScaledHeight() - var8, var9);
         ++var7;
         var6 = var1.parseIndex(var6 + 1);
      }

      drawRect(1, var5.getScaledHeight() - 30 + 1, 14, var5.getScaledHeight() - 30 + 10, -1873784752);
      this.fontRenderer.drawString("60", 2, var5.getScaledHeight() - 30 + 2, 14737632);
      this.drawHorizontalLine(0, 239, var5.getScaledHeight() - 30, -1);
      drawRect(1, var5.getScaledHeight() - 60 + 1, 14, var5.getScaledHeight() - 60 + 10, -1873784752);
      this.fontRenderer.drawString("30", 2, var5.getScaledHeight() - 60 + 2, 14737632);
      this.drawHorizontalLine(0, 239, var5.getScaledHeight() - 60, -1);
      this.drawHorizontalLine(0, 239, var5.getScaledHeight() - 1, -1);
      this.drawVerticalLine(0, var5.getScaledHeight() - 60, var5.getScaledHeight(), -1);
      this.drawVerticalLine(239, var5.getScaledHeight() - 60, var5.getScaledHeight(), -1);
      if (this.mc.gameSettings.limitFramerate <= 120) {
         this.drawHorizontalLine(0, 239, var5.getScaledHeight() - 60 + this.mc.gameSettings.limitFramerate / 2, -16711681);
      }

      GlStateManager.enableDepth();
   }

   private int getFrameColor(int var1, int var2, int var3, int var4) {
      return var1 < var3 ? this.blendColors(-16711936, -256, (float)var1 / (float)var3) : this.blendColors(-256, -65536, (float)(var1 - var3) / (float)(var4 - var3));
   }

   private int blendColors(int var1, int var2, float var3) {
      int var4 = var1 >> 24 & 255;
      int var5 = var1 >> 16 & 255;
      int var6 = var1 >> 8 & 255;
      int var7 = var1 & 255;
      int var8 = var2 >> 24 & 255;
      int var9 = var2 >> 16 & 255;
      int var10 = var2 >> 8 & 255;
      int var11 = var2 & 255;
      int var12 = MathHelper.clamp((int)((float)var4 + (float)(var8 - var4) * var3), 0, 255);
      int var13 = MathHelper.clamp((int)((float)var5 + (float)(var9 - var5) * var3), 0, 255);
      int var14 = MathHelper.clamp((int)((float)var6 + (float)(var10 - var6) * var3), 0, 255);
      int var15 = MathHelper.clamp((int)((float)var7 + (float)(var11 - var7) * var3), 0, 255);
      return var12 << 24 | var13 << 16 | var14 << 8 | var15;
   }

   private static long bytesToMb(long var0) {
      return var0 / 1024L / 1024L;
   }
}
