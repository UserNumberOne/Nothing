package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
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
      if (args.length < 2) {
         throw new WrongUsageException("commands.enchant.usage", new Object[0]);
      } else {
         EntityLivingBase entitylivingbase = (EntityLivingBase)getEntity(server, sender, args[0], EntityLivingBase.class);
         sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 0);

         Enchantment enchantment;
         try {
            enchantment = Enchantment.getEnchantmentByID(parseInt(args[1], 0));
         } catch (NumberInvalidException var12) {
            enchantment = Enchantment.getEnchantmentByLocation(args[1]);
         }

         if (enchantment == null) {
            throw new NumberInvalidException("commands.enchant.notFound", new Object[]{Enchantment.getEnchantmentID(enchantment)});
         } else {
            int i = 1;
            ItemStack itemstack = entitylivingbase.getHeldItemMainhand();
            if (itemstack == null) {
               throw new CommandException("commands.enchant.noItem", new Object[0]);
            } else if (!enchantment.canApply(itemstack)) {
               throw new CommandException("commands.enchant.cantEnchant", new Object[0]);
            } else {
               if (args.length >= 3) {
                  i = parseInt(args[2], enchantment.getMinLevel(), enchantment.getMaxLevel());
               }

               if (itemstack.hasTagCompound()) {
                  NBTTagList nbttaglist = itemstack.getEnchantmentTagList();
                  if (nbttaglist != null) {
                     for(int j = 0; j < nbttaglist.tagCount(); ++j) {
                        int k = nbttaglist.getCompoundTagAt(j).getShort("id");
                        if (Enchantment.getEnchantmentByID(k) != null) {
                           Enchantment enchantment1 = Enchantment.getEnchantmentByID(k);
                           if (!enchantment.canApplyTogether(enchantment1) || !enchantment1.canApplyTogether(enchantment)) {
                              throw new CommandException("commands.enchant.cantCombine", new Object[]{enchantment.getTranslatedName(i), enchantment1.getTranslatedName(nbttaglist.getCompoundTagAt(j).getShort("lvl"))});
                           }
                        }
                     }
                  }
               }

               itemstack.addEnchantment(enchantment, i);
               notifyCommandListener(sender, this, "commands.enchant.success", new Object[0]);
               sender.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 1);
            }
         }
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : (args.length == 2 ? getListOfStringsMatchingLastWord(args, Enchantment.REGISTRY.getKeys()) : Collections.emptyList());
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return index == 0;
   }
}
