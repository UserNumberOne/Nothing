package net.minecraft.util.text;

import java.util.List;
import net.minecraft.command.CommandException;
import net.minecraft.command.EntityNotFoundException;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class TextComponentUtils {
   public static ITextComponent processComponent(ICommandSender var0, ITextComponent var1, Entity var2) throws CommandException {
      Object var3 = null;
      if (var1 instanceof TextComponentScore) {
         TextComponentScore var4 = (TextComponentScore)var1;
         String var5 = var4.getName();
         if (EntitySelector.hasArguments(var5)) {
            List var6 = EntitySelector.matchEntities(var0, var5, Entity.class);
            if (var6.size() != 1) {
               throw new EntityNotFoundException();
            }

            Entity var7 = (Entity)var6.get(0);
            if (var7 instanceof EntityPlayer) {
               var5 = var7.getName();
            } else {
               var5 = var7.getCachedUniqueIdString();
            }
         }

         var3 = var2 != null && var5.equals("*") ? new TextComponentScore(var2.getName(), var4.getObjective()) : new TextComponentScore(var5, var4.getObjective());
         ((TextComponentScore)var3).resolve(var0);
      } else if (var1 instanceof TextComponentSelector) {
         String var9 = ((TextComponentSelector)var1).getSelector();
         var3 = EntitySelector.matchEntitiesToTextComponent(var0, var9);
         if (var3 == null) {
            var3 = new TextComponentString("");
         }
      } else if (var1 instanceof TextComponentString) {
         var3 = new TextComponentString(((TextComponentString)var1).getText());
      } else {
         if (!(var1 instanceof TextComponentTranslation)) {
            return var1;
         }

         Object[] var10 = ((TextComponentTranslation)var1).getFormatArgs();

         for(int var12 = 0; var12 < var10.length; ++var12) {
            Object var14 = var10[var12];
            if (var14 instanceof ITextComponent) {
               var10[var12] = processComponent(var0, (ITextComponent)var14, var2);
            }
         }

         var3 = new TextComponentTranslation(((TextComponentTranslation)var1).getKey(), var10);
      }

      Style var11 = var1.getStyle();
      if (var11 != null) {
         ((ITextComponent)var3).setStyle(var11.createShallowCopy());
      }

      for(ITextComponent var15 : var1.getSiblings()) {
         ((ITextComponent)var3).appendSibling(processComponent(var0, var15, var2));
      }

      return (ITextComponent)var3;
   }
}
