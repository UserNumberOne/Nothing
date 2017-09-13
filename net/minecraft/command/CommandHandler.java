package net.minecraft.command;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class CommandHandler implements ICommandManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map commandMap = Maps.newHashMap();
   private final Set commandSet = Sets.newHashSet();

   public int executeCommand(ICommandSender var1, String var2) {
      var2 = var2.trim();
      if (var2.startsWith("/")) {
         var2 = var2.substring(1);
      }

      String[] var3 = var2.split(" ");
      String var4 = var3[0];
      var3 = dropFirstString(var3);
      ICommand var5 = (ICommand)this.commandMap.get(var4);
      int var6 = this.getUsernameIndex(var5, var3);
      int var7 = 0;
      if (var5 == null) {
         TextComponentTranslation var8 = new TextComponentTranslation("commands.generic.notFound", new Object[0]);
         var8.getStyle().setColor(TextFormatting.RED);
         var1.sendMessage(var8);
      } else if (var5.canUse(this.a(), var1)) {
         if (var6 > -1) {
            List var14 = EntitySelector.matchEntities(var1, var3[var6], Entity.class);
            String var9 = var3[var6];
            var1.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, var14.size());

            for(Entity var11 : var14) {
               var3[var6] = var11.getCachedUniqueIdString();
               if (this.tryExecute(var1, var3, var5, var2)) {
                  ++var7;
               }
            }

            var3[var6] = var9;
         } else {
            var1.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, 1);
            if (this.tryExecute(var1, var3, var5, var2)) {
               ++var7;
            }
         }
      } else {
         TextComponentTranslation var15 = new TextComponentTranslation("commands.generic.permission", new Object[0]);
         var15.getStyle().setColor(TextFormatting.RED);
         var1.sendMessage(var15);
      }

      var1.setCommandStat(CommandResultStats.Type.SUCCESS_COUNT, var7);
      return var7;
   }

   protected boolean tryExecute(ICommandSender var1, String[] var2, ICommand var3, String var4) {
      try {
         var3.execute(this.a(), var1, var2);
         return true;
      } catch (WrongUsageException var7) {
         TextComponentTranslation var11 = new TextComponentTranslation("commands.generic.usage", new Object[]{new TextComponentTranslation(var7.getMessage(), var7.getErrorObjects())});
         var11.getStyle().setColor(TextFormatting.RED);
         var1.sendMessage(var11);
      } catch (CommandException var8) {
         TextComponentTranslation var10 = new TextComponentTranslation(var8.getMessage(), var8.getErrorObjects());
         var10.getStyle().setColor(TextFormatting.RED);
         var1.sendMessage(var10);
      } catch (Throwable var9) {
         TextComponentTranslation var6 = new TextComponentTranslation("commands.generic.exception", new Object[0]);
         var6.getStyle().setColor(TextFormatting.RED);
         var1.sendMessage(var6);
         LOGGER.warn("Couldn't process command: '{}'", new Object[]{var4});
      }

      return false;
   }

   protected abstract MinecraftServer a();

   public ICommand registerCommand(ICommand var1) {
      this.commandMap.put(var1.getName(), var1);
      this.commandSet.add(var1);

      for(String var3 : var1.getAliases()) {
         ICommand var4 = (ICommand)this.commandMap.get(var3);
         if (var4 == null || !var4.getName().equals(var3)) {
            this.commandMap.put(var3, var1);
         }
      }

      return var1;
   }

   private static String[] dropFirstString(String[] var0) {
      String[] var1 = new String[var0.length - 1];
      System.arraycopy(var0, 1, var1, 0, var0.length - 1);
      return var1;
   }

   public List getTabCompletions(ICommandSender var1, String var2, @Nullable BlockPos var3) {
      String[] var4 = var2.split(" ", -1);
      String var5 = var4[0];
      if (var4.length == 1) {
         ArrayList var9 = Lists.newArrayList();

         for(Entry var8 : this.commandMap.entrySet()) {
            if (CommandBase.doesStringStartWith(var5, (String)var8.getKey()) && ((ICommand)var8.getValue()).canUse(this.a(), var1)) {
               var9.add(var8.getKey());
            }
         }

         return var9;
      } else {
         if (var4.length > 1) {
            ICommand var6 = (ICommand)this.commandMap.get(var5);
            if (var6 != null && var6.canUse(this.a(), var1)) {
               return var6.tabComplete(this.a(), var1, dropFirstString(var4), var3);
            }
         }

         return Collections.emptyList();
      }
   }

   public List getPossibleCommands(ICommandSender var1) {
      ArrayList var2 = Lists.newArrayList();

      for(ICommand var4 : this.commandSet) {
         if (var4.canUse(this.a(), var1)) {
            var2.add(var4);
         }
      }

      return var2;
   }

   public Map getCommands() {
      return this.commandMap;
   }

   private int getUsernameIndex(ICommand var1, String[] var2) {
      if (var1 == null) {
         return -1;
      } else {
         for(int var3 = 0; var3 < var2.length; ++var3) {
            if (var1.isUsernameIndex(var2, var3) && EntitySelector.matchesMultiplePlayers(var2[var3])) {
               return var3;
            }
         }

         return -1;
      }
   }
}
