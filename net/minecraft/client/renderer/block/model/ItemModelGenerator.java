package net.minecraft.client.renderer.block.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

@SideOnly(Side.CLIENT)
public class ItemModelGenerator {
   public static final List LAYERS = Lists.newArrayList(new String[]{"layer0", "layer1", "layer2", "layer3", "layer4"});

   public ModelBlock makeItemModel(TextureMap var1, ModelBlock var2) {
      HashMap var3 = Maps.newHashMap();
      ArrayList var4 = Lists.newArrayList();

      for(int var5 = 0; var5 < LAYERS.size(); ++var5) {
         String var6 = (String)LAYERS.get(var5);
         if (!var2.isTexturePresent(var6)) {
            break;
         }

         String var7 = var2.resolveTextureName(var6);
         var3.put(var6, var7);
         TextureAtlasSprite var8 = var1.getAtlasSprite((new ResourceLocation(var7)).toString());
         var4.addAll(this.getBlockParts(var5, var6, var8));
      }

      if (var4.isEmpty()) {
         return null;
      } else {
         var3.put("particle", var2.isTexturePresent("particle") ? var2.resolveTextureName("particle") : (String)var3.get("layer0"));
         return new ModelBlock((ResourceLocation)null, var4, var3, false, false, var2.getAllTransforms(), var2.getOverrides());
      }
   }

   private List getBlockParts(int var1, String var2, TextureAtlasSprite var3) {
      HashMap var4 = Maps.newHashMap();
      var4.put(EnumFacing.SOUTH, new BlockPartFace((EnumFacing)null, var1, var2, new BlockFaceUV(new float[]{0.0F, 0.0F, 16.0F, 16.0F}, 0)));
      var4.put(EnumFacing.NORTH, new BlockPartFace((EnumFacing)null, var1, var2, new BlockFaceUV(new float[]{16.0F, 0.0F, 0.0F, 16.0F}, 0)));
      ArrayList var5 = Lists.newArrayList();
      var5.add(new BlockPart(new Vector3f(0.0F, 0.0F, 7.5F), new Vector3f(16.0F, 16.0F, 8.5F), var4, (BlockPartRotation)null, true));
      var5.addAll(this.getBlockParts(var3, var2, var1));
      return var5;
   }

   private List getBlockParts(TextureAtlasSprite var1, String var2, int var3) {
      float var4 = (float)var1.getIconWidth();
      float var5 = (float)var1.getIconHeight();
      ArrayList var6 = Lists.newArrayList();

      for(ItemModelGenerator.Span var8 : this.getSpans(var1)) {
         float var9 = 0.0F;
         float var10 = 0.0F;
         float var11 = 0.0F;
         float var12 = 0.0F;
         float var13 = 0.0F;
         float var14 = 0.0F;
         float var15 = 0.0F;
         float var16 = 0.0F;
         float var17 = 0.0F;
         float var18 = 0.0F;
         float var19 = (float)var8.getMin();
         float var20 = (float)var8.getMax();
         float var21 = (float)var8.getAnchor();
         ItemModelGenerator.SpanFacing var22 = var8.getFacing();
         switch(var22) {
         case UP:
            var13 = var19;
            var9 = var19;
            var11 = var14 = var20 + 1.0F;
            var15 = var21;
            var10 = var21;
            var16 = var21;
            var12 = var21;
            var17 = 16.0F / var4;
            var18 = 16.0F / (var5 - 1.0F);
            break;
         case DOWN:
            var16 = var21;
            var15 = var21;
            var13 = var19;
            var9 = var19;
            var11 = var14 = var20 + 1.0F;
            var10 = var21 + 1.0F;
            var12 = var21 + 1.0F;
            var17 = 16.0F / var4;
            var18 = 16.0F / (var5 - 1.0F);
            break;
         case LEFT:
            var13 = var21;
            var9 = var21;
            var14 = var21;
            var11 = var21;
            var16 = var19;
            var10 = var19;
            var12 = var15 = var20 + 1.0F;
            var17 = 16.0F / (var4 - 1.0F);
            var18 = 16.0F / var5;
            break;
         case RIGHT:
            var14 = var21;
            var13 = var21;
            var9 = var21 + 1.0F;
            var11 = var21 + 1.0F;
            var16 = var19;
            var10 = var19;
            var12 = var15 = var20 + 1.0F;
            var17 = 16.0F / (var4 - 1.0F);
            var18 = 16.0F / var5;
         }

         float var23 = 16.0F / var4;
         float var24 = 16.0F / var5;
         var9 = var9 * var23;
         var11 = var11 * var23;
         var10 = var10 * var24;
         var12 = var12 * var24;
         var10 = 16.0F - var10;
         var12 = 16.0F - var12;
         var13 = var13 * var17;
         var14 = var14 * var17;
         var15 = var15 * var18;
         var16 = var16 * var18;
         HashMap var25 = Maps.newHashMap();
         var25.put(var22.getFacing(), new BlockPartFace((EnumFacing)null, var3, var2, new BlockFaceUV(new float[]{var13, var15, var14, var16}, 0)));
         switch(var22) {
         case UP:
            var6.add(new BlockPart(new Vector3f(var9, var10, 7.5F), new Vector3f(var11, var10, 8.5F), var25, (BlockPartRotation)null, true));
            break;
         case DOWN:
            var6.add(new BlockPart(new Vector3f(var9, var12, 7.5F), new Vector3f(var11, var12, 8.5F), var25, (BlockPartRotation)null, true));
            break;
         case LEFT:
            var6.add(new BlockPart(new Vector3f(var9, var10, 7.5F), new Vector3f(var9, var12, 8.5F), var25, (BlockPartRotation)null, true));
            break;
         case RIGHT:
            var6.add(new BlockPart(new Vector3f(var11, var10, 7.5F), new Vector3f(var11, var12, 8.5F), var25, (BlockPartRotation)null, true));
         }
      }

      return var6;
   }

   private List getSpans(TextureAtlasSprite var1) {
      int var2 = var1.getIconWidth();
      int var3 = var1.getIconHeight();
      ArrayList var4 = Lists.newArrayList();

      for(int var5 = 0; var5 < var1.getFrameCount(); ++var5) {
         int[] var6 = var1.getFrameTextureData(var5)[0];

         for(int var7 = 0; var7 < var3; ++var7) {
            for(int var8 = 0; var8 < var2; ++var8) {
               boolean var9 = !this.isTransparent(var6, var8, var7, var2, var3);
               this.checkTransition(ItemModelGenerator.SpanFacing.UP, var4, var6, var8, var7, var2, var3, var9);
               this.checkTransition(ItemModelGenerator.SpanFacing.DOWN, var4, var6, var8, var7, var2, var3, var9);
               this.checkTransition(ItemModelGenerator.SpanFacing.LEFT, var4, var6, var8, var7, var2, var3, var9);
               this.checkTransition(ItemModelGenerator.SpanFacing.RIGHT, var4, var6, var8, var7, var2, var3, var9);
            }
         }
      }

      return var4;
   }

   private void checkTransition(ItemModelGenerator.SpanFacing var1, List var2, int[] var3, int var4, int var5, int var6, int var7, boolean var8) {
      boolean var9 = this.isTransparent(var3, var4 + var1.getXOffset(), var5 + var1.getYOffset(), var6, var7) && var8;
      if (var9) {
         this.createOrExpandSpan(var2, var1, var4, var5);
      }

   }

   private void createOrExpandSpan(List var1, ItemModelGenerator.SpanFacing var2, int var3, int var4) {
      ItemModelGenerator.Span var5 = null;

      for(ItemModelGenerator.Span var7 : var1) {
         if (var7.getFacing() == var2) {
            int var8 = var2.isHorizontal() ? var4 : var3;
            if (var7.getAnchor() == var8) {
               var5 = var7;
               break;
            }
         }
      }

      int var9 = var2.isHorizontal() ? var4 : var3;
      int var10 = var2.isHorizontal() ? var3 : var4;
      if (var5 == null) {
         var1.add(new ItemModelGenerator.Span(var2, var10, var9));
      } else {
         var5.expand(var10);
      }

   }

   private boolean isTransparent(int[] var1, int var2, int var3, int var4, int var5) {
      return var2 >= 0 && var3 >= 0 && var2 < var4 && var3 < var5 ? (var1[var3 * var4 + var2] >> 24 & 255) == 0 : true;
   }

   @SideOnly(Side.CLIENT)
   static class Span {
      private final ItemModelGenerator.SpanFacing spanFacing;
      private int min;
      private int max;
      private final int anchor;

      public Span(ItemModelGenerator.SpanFacing var1, int var2, int var3) {
         this.spanFacing = var1;
         this.min = var2;
         this.max = var2;
         this.anchor = var3;
      }

      public void expand(int var1) {
         if (var1 < this.min) {
            this.min = var1;
         } else if (var1 > this.max) {
            this.max = var1;
         }

      }

      public ItemModelGenerator.SpanFacing getFacing() {
         return this.spanFacing;
      }

      public int getMin() {
         return this.min;
      }

      public int getMax() {
         return this.max;
      }

      public int getAnchor() {
         return this.anchor;
      }
   }

   @SideOnly(Side.CLIENT)
   static enum SpanFacing {
      UP(EnumFacing.UP, 0, -1),
      DOWN(EnumFacing.DOWN, 0, 1),
      LEFT(EnumFacing.EAST, -1, 0),
      RIGHT(EnumFacing.WEST, 1, 0);

      private final EnumFacing facing;
      private final int xOffset;
      private final int yOffset;

      private SpanFacing(EnumFacing var3, int var4, int var5) {
         this.facing = var3;
         this.xOffset = var4;
         this.yOffset = var5;
      }

      public EnumFacing getFacing() {
         return this.facing;
      }

      public int getXOffset() {
         return this.xOffset;
      }

      public int getYOffset() {
         return this.yOffset;
      }

      private boolean isHorizontal() {
         return this == DOWN || this == UP;
      }
   }
}
