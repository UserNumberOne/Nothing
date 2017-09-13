package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiUtilRenderComponents {
   public static String removeTextColorsIfConfigured(String var0, boolean var1) {
      return !var1 && !Minecraft.getMinecraft().gameSettings.chatColours ? TextFormatting.getTextWithoutFormattingCodes(var0) : var0;
   }

   public static List splitText(ITextComponent var0, int var1, FontRenderer var2, boolean var3, boolean var4) {
      int var5 = 0;
      TextComponentString var6 = new TextComponentString("");
      ArrayList var7 = Lists.newArrayList();
      ArrayList var8 = Lists.newArrayList(var0);

      for(int var9 = 0; var9 < var8.size(); ++var9) {
         ITextComponent var10 = (ITextComponent)var8.get(var9);
         String var11 = var10.getUnformattedComponentText();
         boolean var12 = false;
         if (var11.contains("\n")) {
            int var13 = var11.indexOf(10);
            String var14 = var11.substring(var13 + 1);
            var11 = var11.substring(0, var13 + 1);
            TextComponentString var15 = new TextComponentString(var14);
            var15.setStyle(var10.getStyle().createShallowCopy());
            var8.add(var9 + 1, var15);
            var12 = true;
         }

         String var21 = removeTextColorsIfConfigured(var10.getStyle().getFormattingCode() + var11, var4);
         String var22 = var21.endsWith("\n") ? var21.substring(0, var21.length() - 1) : var21;
         int var23 = var2.getStringWidth(var22);
         TextComponentString var16 = new TextComponentString(var22);
         var16.setStyle(var10.getStyle().createShallowCopy());
         if (var5 + var23 > var1) {
            String var17 = var2.trimStringToWidth(var21, var1 - var5, false);
            String var18 = var17.length() < var21.length() ? var21.substring(var17.length()) : null;
            if (var18 != null && !var18.isEmpty()) {
               int var19 = var17.lastIndexOf(32);
               if (var19 >= 0 && var2.getStringWidth(var21.substring(0, var19)) > 0) {
                  var17 = var21.substring(0, var19);
                  if (var3) {
                     ++var19;
                  }

                  var18 = var21.substring(var19);
               } else if (var5 > 0 && !var21.contains(" ")) {
                  var17 = "";
                  var18 = var21;
               }

               var18 = FontRenderer.getFormatFromString(var17) + var18;
               TextComponentString var20 = new TextComponentString(var18);
               var20.setStyle(var10.getStyle().createShallowCopy());
               var8.add(var9 + 1, var20);
            }

            var23 = var2.getStringWidth(var17);
            var16 = new TextComponentString(var17);
            var16.setStyle(var10.getStyle().createShallowCopy());
            var12 = true;
         }

         if (var5 + var23 <= var1) {
            var5 += var23;
            var6.appendSibling(var16);
         } else {
            var12 = true;
         }

         if (var12) {
            var7.add(var6);
            var5 = 0;
            var6 = new TextComponentString("");
         }
      }

      var7.add(var6);
      return var7;
   }
}
