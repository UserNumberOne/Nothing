package net.minecraft.tileentity;

import com.google.common.base.Joiner;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.annotation.Nullable;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.ICommandSender;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.item.EntityMinecartCommandBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.v1_10_R1.command.VanillaCommandWrapper;

public abstract class CommandBlockBaseLogic implements ICommandSender {
   private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss");
   private int successCount;
   private boolean trackOutput = true;
   private ITextComponent lastOutput;
   private String commandStored = "";
   private String customName = "@";
   private final CommandResultStats resultStats = new CommandResultStats();
   protected CommandSender sender;

   public int getSuccessCount() {
      return this.successCount;
   }

   public void setSuccessCount(int i) {
      this.successCount = i;
   }

   public ITextComponent getLastOutput() {
      return (ITextComponent)(this.lastOutput == null ? new TextComponentString("") : this.lastOutput);
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setString("Command", this.commandStored);
      nbttagcompound.setInteger("SuccessCount", this.successCount);
      nbttagcompound.setString("CustomName", this.customName);
      nbttagcompound.setBoolean("TrackOutput", this.trackOutput);
      if (this.lastOutput != null && this.trackOutput) {
         nbttagcompound.setString("LastOutput", ITextComponent.Serializer.componentToJson(this.lastOutput));
      }

      this.resultStats.writeStatsToNBT(nbttagcompound);
      return nbttagcompound;
   }

   public void readDataFromNBT(NBTTagCompound nbttagcompound) {
      this.commandStored = nbttagcompound.getString("Command");
      this.successCount = nbttagcompound.getInteger("SuccessCount");
      if (nbttagcompound.hasKey("CustomName", 8)) {
         this.customName = nbttagcompound.getString("CustomName");
      }

      if (nbttagcompound.hasKey("TrackOutput", 1)) {
         this.trackOutput = nbttagcompound.getBoolean("TrackOutput");
      }

      if (nbttagcompound.hasKey("LastOutput", 8) && this.trackOutput) {
         try {
            this.lastOutput = ITextComponent.Serializer.jsonToComponent(nbttagcompound.getString("LastOutput"));
         } catch (Throwable var3) {
            this.lastOutput = new TextComponentString(var3.getMessage());
         }
      } else {
         this.lastOutput = null;
      }

      this.resultStats.readStatsFromNBT(nbttagcompound);
   }

   public boolean canUseCommand(int i, String s) {
      return i <= 2;
   }

   public void setCommand(String s) {
      this.commandStored = s;
      this.successCount = 0;
   }

   public String getCommand() {
      return this.commandStored;
   }

   public void trigger(World world) {
      if (world.isRemote) {
         this.successCount = 0;
      } else if ("Searge".equalsIgnoreCase(this.commandStored)) {
         this.lastOutput = new TextComponentString("#itzlipofutzli");
         this.successCount = 1;
      } else {
         MinecraftServer minecraftserver = this.h();
         if (minecraftserver != null && minecraftserver.M() && minecraftserver.getEnableCommandBlock()) {
            minecraftserver.getCommandHandler();

            try {
               this.lastOutput = null;
               this.successCount = executeCommand(this, this.sender, this.commandStored);
            } catch (Throwable var6) {
               CrashReport crashreport = CrashReport.makeCrashReport(var6, "Executing command block");
               CrashReportCategory crashreportsystemdetails = crashreport.makeCategory("Command to be executed");
               crashreportsystemdetails.setDetail("Command", new ICrashReportDetail() {
                  public String call() throws Exception {
                     return CommandBlockBaseLogic.this.getCommand();
                  }

                  public Object call() throws Exception {
                     return this.call();
                  }
               });
               crashreportsystemdetails.setDetail("Name", new ICrashReportDetail() {
                  public String call() throws Exception {
                     return CommandBlockBaseLogic.this.getName();
                  }

                  public Object call() throws Exception {
                     return this.call();
                  }
               });
               throw new ReportedException(crashreport);
            }
         } else {
            this.successCount = 0;
         }
      }

   }

   public static int executeCommand(ICommandSender sender, CommandSender bSender, String command) {
      SimpleCommandMap commandMap = sender.getEntityWorld().getServer().getCommandMap();
      Joiner joiner = Joiner.on(" ");
      if (command.startsWith("/")) {
         command = command.substring(1);
      }

      String[] args = command.split(" ");
      ArrayList commands = new ArrayList();
      String cmd = args[0];
      if (cmd.startsWith("minecraft:")) {
         cmd = cmd.substring("minecraft:".length());
      }

      if (cmd.startsWith("bukkit:")) {
         cmd = cmd.substring("bukkit:".length());
      }

      if (!cmd.equalsIgnoreCase("stop") && !cmd.equalsIgnoreCase("kick") && !cmd.equalsIgnoreCase("op") && !cmd.equalsIgnoreCase("deop") && !cmd.equalsIgnoreCase("ban") && !cmd.equalsIgnoreCase("ban-ip") && !cmd.equalsIgnoreCase("pardon") && !cmd.equalsIgnoreCase("pardon-ip") && !cmd.equalsIgnoreCase("reload")) {
         Command commandBlockCommand = commandMap.getCommand(args[0]);
         if (sender.getEntityWorld().getServer().getCommandBlockOverride(args[0])) {
            commandBlockCommand = commandMap.getCommand("minecraft:" + args[0]);
         }

         if (commandBlockCommand instanceof VanillaCommandWrapper) {
            command = command.trim();
            if (command.startsWith("/")) {
               command = command.substring(1);
            }

            String[] as = command.split(" ");
            as = VanillaCommandWrapper.dropFirstArgument(as);
            return !((VanillaCommandWrapper)commandBlockCommand).testPermission(bSender) ? 0 : ((VanillaCommandWrapper)commandBlockCommand).dispatchVanillaCommand(bSender, sender, as);
         } else if (commandMap.getCommand(args[0]) == null) {
            return 0;
         } else {
            commands.add(args);
            WorldServer[] prev = MinecraftServer.getServer().worldServer;
            MinecraftServer server = MinecraftServer.getServer();
            server.worldServer = new WorldServer[server.worlds.size()];
            server.worldServer[0] = (WorldServer)sender.getEntityWorld();
            int bpos = 0;

            for(int pos = 1; pos < server.worldServer.length; ++pos) {
               WorldServer world = (WorldServer)server.worlds.get(bpos++);
               if (server.worldServer[0] == world) {
                  --pos;
               } else {
                  server.worldServer[pos] = world;
               }
            }

            try {
               ArrayList newCommands = new ArrayList();

               for(int i = 0; i < args.length; ++i) {
                  if (EntitySelector.hasArguments(args[i])) {
                     for(int j = 0; j < commands.size(); ++j) {
                        newCommands.addAll(buildCommands(sender, (String[])commands.get(j), i));
                     }

                     ArrayList temp = commands;
                     commands = newCommands;
                     newCommands = temp;
                     temp.clear();
                  }
               }
            } finally {
               MinecraftServer.getServer().worldServer = prev;
            }

            int completed = 0;

            for(int i = 0; i < commands.size(); ++i) {
               try {
                  if (commandMap.dispatch(bSender, joiner.join(Arrays.asList((String[])commands.get(i))))) {
                     ++completed;
                  }
               } catch (Throwable var18) {
                  if (sender.getCommandSenderEntity() instanceof EntityMinecartCommandBlock) {
                     MinecraftServer.getServer().server.getLogger().log(Level.WARNING, String.format("MinecartCommandBlock at (%d,%d,%d) failed to handle command", sender.getPosition().getX(), sender.getPosition().getY(), sender.getPosition().getZ()), var18);
                  } else if (sender instanceof CommandBlockBaseLogic) {
                     CommandBlockBaseLogic listener = (CommandBlockBaseLogic)sender;
                     MinecraftServer.getServer().server.getLogger().log(Level.WARNING, String.format("CommandBlock at (%d,%d,%d) failed to handle command", listener.getPosition().getX(), listener.getPosition().getY(), listener.getPosition().getZ()), var18);
                  } else {
                     MinecraftServer.getServer().server.getLogger().log(Level.WARNING, String.format("Unknown CommandBlock failed to handle command"), var18);
                  }
               }
            }

            return completed;
         }
      } else {
         return 0;
      }
   }

   private static ArrayList buildCommands(ICommandSender sender, String[] args, int pos) {
      ArrayList commands = new ArrayList();
      List players = EntitySelector.matchEntities(sender, args[pos], EntityPlayerMP.class);
      if (players != null) {
         for(EntityPlayerMP player : players) {
            if (player.world == sender.getEntityWorld()) {
               String[] command = (String[])args.clone();
               command[pos] = player.getName();
               commands.add(command);
            }
         }
      }

      return commands;
   }

   public String getName() {
      return this.customName;
   }

   public ITextComponent getDisplayName() {
      return new TextComponentString(this.getName());
   }

   public void setName(String s) {
      this.customName = s;
   }

   public void sendMessage(ITextComponent ichatbasecomponent) {
      if (this.trackOutput && this.getEntityWorld() != null && !this.getEntityWorld().isRemote) {
         this.lastOutput = (new TextComponentString("[" + TIMESTAMP_FORMAT.format(new Date()) + "] ")).appendSibling(ichatbasecomponent);
         this.updateCommand();
      }

   }

   public boolean sendCommandFeedback() {
      MinecraftServer minecraftserver = this.h();
      return minecraftserver == null || !minecraftserver.M() || minecraftserver.worldServer[0].getGameRules().getBoolean("commandBlockOutput");
   }

   public void setCommandStat(CommandResultStats.Type commandobjectiveexecutor_enumcommandresult, int i) {
      this.resultStats.a(this.h(), this, commandobjectiveexecutor_enumcommandresult, i);
   }

   public abstract void updateCommand();

   public void setLastOutput(@Nullable ITextComponent ichatbasecomponent) {
      this.lastOutput = ichatbasecomponent;
   }

   public void setTrackOutput(boolean flag) {
      this.trackOutput = flag;
   }

   public boolean shouldTrackOutput() {
      return this.trackOutput;
   }

   public boolean tryOpenEditCommandBlock(EntityPlayer entityhuman) {
      if (!entityhuman.canUseCommandBlock()) {
         return false;
      } else {
         if (entityhuman.getEntityWorld().isRemote) {
            entityhuman.displayGuiEditCommandCart(this);
         }

         return true;
      }
   }

   public CommandResultStats getCommandResultStats() {
      return this.resultStats;
   }
}
