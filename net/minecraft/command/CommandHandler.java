package net.minecraft.command;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.CommandEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class CommandHandler implements ICommandManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map commandMap = Maps.newHashMap();
   private final Set commandSet = Sets.newHashSet();

   public int executeCommand(ICommandSender var1, String var2) {
      rawCommand = rawCommand.trim();
      if (rawCommand.startsWith("/")) {
         rawCommand = rawCommand.substring(1);
      }

      String[] astring = rawCommand.split(" ");
      String s = astring[0];
      astring = dropFirstString(astring);
      ICommand icommand = (ICommand)this.commandMap.get(s);
      int i = this.getUsernameIndex(icommand, astring);
      int j = 0;
      if (icommand == null) {
         TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("commands.generic.notFound", new Object[0]);
         textcomponenttranslation.getStyle().setColor(TextFormatting.RED);
         sender.sendMessage(textcomponenttranslation);
      } else if (icommand.checkPermission(this.getServer(), sender)) {
         CommandEvent event = new CommandEvent(icommand, sender, astring);
         if (MinecraftForge.EVENT_BUS.post(event)) {
            if (event.getException() != null) {
               Throwables.propagateIfPossible(event.getException());
            }

            return 1;
         }

         if (event.getParameters() != null) {
            astring = event.getParameters();
         }

         if (i > -1) {
            List list = EntitySelector.matchEntities(sender, astring[i], Entity.class);
            String s1 = astring[i];
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, list.size());

            for(Entity entity : list) {
               astring[i] = entity.getCachedUniqueIdString();
               if (this.tryExecute(sender, astring, icommand, rawCommand)) {
                  ++j;
               }
            }

            astring[i] = s1;
         } else {
            sender.setCommandStat(CommandResultStats.Type.AFFECTED_ENTITIES, 1);
            if (this.tryExecute(sender, astring, icommand, rawCommand)) {
               ++j;
            }
         }
      } else {
         TextComponentTranslation textcomponenttranslation1 = new TextComponentTranslation("commands.generic.permission", new Object[0]);
         textcomponenttranslation1.getStyle().setColor(TextFormatting.RED);
         sender.sendMessage(textcomponenttranslation1);
      }

      sender.setCommandStat(CommandResultStats.Type.SUCCESS_COUNT, j);
      return j;
   }

   protected boolean tryExecute(ICommandSender var1, String[] var2, ICommand var3, String var4) {
      try {
         command.execute(this.getServer(), sender, args);
         return true;
      } catch (WrongUsageException var7) {
         TextComponentTranslation textcomponenttranslation2 = new TextComponentTranslation("commands.generic.usage", new Object[]{new TextComponentTranslation(var7.getMessage(), var7.getErrorObjects())});
         textcomponenttranslation2.getStyle().setColor(TextFormatting.RED);
         sender.sendMessage(textcomponenttranslation2);
      } catch (CommandException var8) {
         TextComponentTranslation textcomponenttranslation1 = new TextComponentTranslation(var8.getMessage(), var8.getErrorObjects());
         textcomponenttranslation1.getStyle().setColor(TextFormatting.RED);
         sender.sendMessage(textcomponenttranslation1);
      } catch (Throwable var9) {
         TextComponentTranslation textcomponenttranslation = new TextComponentTranslation("commands.generic.exception", new Object[0]);
         textcomponenttranslation.getStyle().setColor(TextFormatting.RED);
         sender.sendMessage(textcomponenttranslation);
         LOGGER.warn("Couldn't process command: '" + input + "'", var9);
      }

      return false;
   }

   protected abstract MinecraftServer getServer();

   public ICommand registerCommand(ICommand var1) {
      this.commandMap.put(command.getName(), command);
      this.commandSet.add(command);

      for(String s : command.getAliases()) {
         ICommand icommand = (ICommand)this.commandMap.get(s);
         if (icommand == null || !icommand.getName().equals(s)) {
            this.commandMap.put(s, command);
         }
      }

      return command;
   }

   private static String[] dropFirstString(String[] var0) {
      String[] astring = new String[input.length - 1];
      System.arraycopy(input, 1, astring, 0, input.length - 1);
      return astring;
   }

   public List getTabCompletions(ICommandSender var1, String var2, @Nullable BlockPos var3) {
      String[] astring = input.split(" ", -1);
      String s = astring[0];
      if (astring.length == 1) {
         List list = Lists.newArrayList();

         for(Entry entry : this.commandMap.entrySet()) {
            if (CommandBase.doesStringStartWith(s, (String)entry.getKey()) && ((ICommand)entry.getValue()).checkPermission(this.getServer(), sender)) {
               list.add(entry.getKey());
            }
         }

         return list;
      } else {
         if (astring.length > 1) {
            ICommand icommand = (ICommand)this.commandMap.get(s);
            if (icommand != null && icommand.checkPermission(this.getServer(), sender)) {
               return icommand.getTabCompletions(this.getServer(), sender, dropFirstString(astring), pos);
            }
         }

         return Collections.emptyList();
      }
   }

   public List getPossibleCommands(ICommandSender var1) {
      List list = Lists.newArrayList();

      for(ICommand icommand : this.commandSet) {
         if (icommand.checkPermission(this.getServer(), sender)) {
            list.add(icommand);
         }
      }

      return list;
   }

   public Map getCommands() {
      return this.commandMap;
   }

   private int getUsernameIndex(ICommand var1, String[] var2) {
      if (command == null) {
         return -1;
      } else {
         for(int i = 0; i < args.length; ++i) {
            if (command.isUsernameIndex(args, i) && EntitySelector.matchesMultiplePlayers(args[i])) {
               return i;
            }
         }

         return -1;
      }
   }
}
