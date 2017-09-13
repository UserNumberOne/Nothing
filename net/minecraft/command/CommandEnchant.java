package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandEnchant extends CommandBase {
   public String getName() {
      return "enchant";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.enchant.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 2) {
         throw new WrongUsageException("commands.enchant.usage", new Object[0]);
      } else {
         EntityLivingBase var4 = (EntityLivingBase)a(var1, var2, var3[0], EntityLivingBase.class);
         var2.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 0);

         Enchantment var5;
         try {
            var5 = Enchantment.getEnchantmentByID(parseInt(var3[1], 0));
         } catch (NumberInvalidException var12) {
            var5 = Enchantment.getEnchantmentByLocation(var3[1]);
         }

         if (var5 == null) {
            throw new NumberInvalidException("commands.enchant.notFound", new Object[]{Enchantment.getEnchantmentID(var5)});
         } else {
            int var6 = 1;
            ItemStack var7 = var4.getHeldItemMainhand();
            if (var7 == null) {
               throw new CommandException("commands.enchant.noItem", new Object[0]);
            } else if (!var5.canApply(var7)) {
               throw new CommandException("commands.enchant.cantEnchant", new Object[0]);
            } else {
               if (var3.length >= 3) {
                  var6 = parseInt(var3[2], var5.getMinLevel(), var5.getMaxLevel());
               }

               if (var7.hasTagCompound()) {
                  NBTTagList var8 = var7.getEnchantmentTagList();
                  if (var8 != null) {
                     for(int var9 = 0; var9 < var8.tagCount(); ++var9) {
                        short var10 = var8.getCompoundTagAt(var9).getShort("id");
                        if (Enchantment.getEnchantmentByID(var10) != null) {
                           Enchantment var11 = Enchantment.getEnchantmentByID(var10);
                           if (!var5.canApplyTogether(var11)) {
                              throw new CommandException("commands.enchant.cantCombine", new Object[]{var5.getTranslatedName(var6), var11.getTranslatedName(var8.getCompoundTagAt(var9).getShort("lvl"))});
                           }
                        }
                     }
                  }
               }

               var7.addEnchantment(var5, var6);
               notifyCommandListener(var2, this, "commands.enchant.success", new Object[0]);
               var2.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 1);
            }
         }
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         return getListOfStringsMatchingLastWord(var3, var1.getPlayers());
      } else {
         return var3.length == 2 ? getListOfStringsMatchingLastWord(var3, Enchantment.REGISTRY.getKeys()) : Collections.emptyList();
      }
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 0;
   }
}
