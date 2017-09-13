package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandEffect extends CommandBase {
   public String getName() {
      return "effect";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.effect.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 2) {
         throw new WrongUsageException("commands.effect.usage", new Object[0]);
      } else {
         EntityLivingBase var4 = (EntityLivingBase)a(var1, var2, var3[0], EntityLivingBase.class);
         if ("clear".equals(var3[1])) {
            if (var4.getActivePotionEffects().isEmpty()) {
               throw new CommandException("commands.effect.failure.notActive.all", new Object[]{var4.getName()});
            } else {
               var4.clearActivePotions();
               notifyCommandListener(var2, this, "commands.effect.success.removed.all", new Object[]{var4.getName()});
            }
         } else {
            Potion var5;
            try {
               var5 = Potion.getPotionById(parseInt(var3[1], 1));
            } catch (NumberInvalidException var11) {
               var5 = Potion.getPotionFromResourceLocation(var3[1]);
            }

            if (var5 == null) {
               throw new NumberInvalidException("commands.effect.notFound", new Object[]{var3[1]});
            } else {
               int var6 = 600;
               int var7 = 30;
               int var8 = 0;
               if (var3.length >= 3) {
                  var7 = parseInt(var3[2], 0, 1000000);
                  if (var5.isInstant()) {
                     var6 = var7;
                  } else {
                     var6 = var7 * 20;
                  }
               } else if (var5.isInstant()) {
                  var6 = 1;
               }

               if (var3.length >= 4) {
                  var8 = parseInt(var3[3], 0, 255);
               }

               boolean var9 = true;
               if (var3.length >= 5 && "true".equalsIgnoreCase(var3[4])) {
                  var9 = false;
               }

               if (var7 > 0) {
                  PotionEffect var10 = new PotionEffect(var5, var6, var8, false, var9);
                  var4.addPotionEffect(var10);
                  notifyCommandListener(var2, this, "commands.effect.success", new Object[]{new TextComponentTranslation(var10.getEffectName(), new Object[0]), Potion.getIdFromPotion(var5), var8, var4.getName(), var7});
               } else if (var4.isPotionActive(var5)) {
                  var4.removePotionEffect(var5);
                  notifyCommandListener(var2, this, "commands.effect.success.removed", new Object[]{new TextComponentTranslation(var5.getName(), new Object[0]), var4.getName()});
               } else {
                  throw new CommandException("commands.effect.failure.notActive", new Object[]{new TextComponentTranslation(var5.getName(), new Object[0]), var4.getName()});
               }
            }
         }
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         return getListOfStringsMatchingLastWord(var3, var1.getPlayers());
      } else if (var3.length == 2) {
         return getListOfStringsMatchingLastWord(var3, Potion.REGISTRY.getKeys());
      } else {
         return var3.length == 5 ? getListOfStringsMatchingLastWord(var3, new String[]{"true", "false"}) : Collections.emptyList();
      }
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 0;
   }
}
