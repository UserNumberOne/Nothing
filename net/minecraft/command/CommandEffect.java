package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
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
      if (args.length < 2) {
         throw new WrongUsageException("commands.effect.usage", new Object[0]);
      } else {
         EntityLivingBase entitylivingbase = (EntityLivingBase)getEntity(server, sender, args[0], EntityLivingBase.class);
         if ("clear".equals(args[1])) {
            if (entitylivingbase.getActivePotionEffects().isEmpty()) {
               throw new CommandException("commands.effect.failure.notActive.all", new Object[]{entitylivingbase.getName()});
            }

            entitylivingbase.clearActivePotions();
            notifyCommandListener(sender, this, "commands.effect.success.removed.all", new Object[]{entitylivingbase.getName()});
         } else {
            Potion potion;
            try {
               potion = Potion.getPotionById(parseInt(args[1], 1));
            } catch (NumberInvalidException var11) {
               potion = Potion.getPotionFromResourceLocation(args[1]);
            }

            if (potion == null) {
               throw new NumberInvalidException("commands.effect.notFound", new Object[]{args[1]});
            }

            int i = 600;
            int j = 30;
            int k = 0;
            if (args.length >= 3) {
               j = parseInt(args[2], 0, 1000000);
               if (potion.isInstant()) {
                  i = j;
               } else {
                  i = j * 20;
               }
            } else if (potion.isInstant()) {
               i = 1;
            }

            if (args.length >= 4) {
               k = parseInt(args[3], 0, 255);
            }

            boolean flag = true;
            if (args.length >= 5 && "true".equalsIgnoreCase(args[4])) {
               flag = false;
            }

            if (j > 0) {
               PotionEffect potioneffect = new PotionEffect(potion, i, k, false, flag);
               entitylivingbase.addPotionEffect(potioneffect);
               notifyCommandListener(sender, this, "commands.effect.success", new Object[]{new TextComponentTranslation(potioneffect.getEffectName(), new Object[0]), Potion.getIdFromPotion(potion), k, entitylivingbase.getName(), j});
            } else {
               if (!entitylivingbase.isPotionActive(potion)) {
                  throw new CommandException("commands.effect.failure.notActive", new Object[]{new TextComponentTranslation(potion.getName(), new Object[0]), entitylivingbase.getName()});
               }

               entitylivingbase.removePotionEffect(potion);
               notifyCommandListener(sender, this, "commands.effect.success.removed", new Object[]{new TextComponentTranslation(potion.getName(), new Object[0]), entitylivingbase.getName()});
            }
         }

      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : (args.length == 2 ? getListOfStringsMatchingLastWord(args, Potion.REGISTRY.getKeys()) : (args.length == 5 ? getListOfStringsMatchingLastWord(args, new String[]{"true", "false"}) : Collections.emptyList()));
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return index == 0;
   }
}
