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

   public void setSuccessCount(int var1) {
      this.successCount = var1;
   }

   public ITextComponent getLastOutput() {
      return (ITextComponent)(this.lastOutput == null ? new TextComponentString("") : this.lastOutput);
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      var1.setString("Command", this.commandStored);
      var1.setInteger("SuccessCount", this.successCount);
      var1.setString("CustomName", this.customName);
      var1.setBoolean("TrackOutput", this.trackOutput);
      if (this.lastOutput != null && this.trackOutput) {
         var1.setString("LastOutput", ITextComponent.Serializer.componentToJson(this.lastOutput));
      }

      this.resultStats.writeStatsToNBT(var1);
      return var1;
   }

   public void readDataFromNBT(NBTTagCompound var1) {
      this.commandStored = var1.getString("Command");
      this.successCount = var1.getInteger("SuccessCount");
      if (var1.hasKey("CustomName", 8)) {
         this.customName = var1.getString("CustomName");
      }

      if (var1.hasKey("TrackOutput", 1)) {
         this.trackOutput = var1.getBoolean("TrackOutput");
      }

      if (var1.hasKey("LastOutput", 8) && this.trackOutput) {
         try {
            this.lastOutput = ITextComponent.Serializer.jsonToComponent(var1.getString("LastOutput"));
         } catch (Throwable var3) {
            this.lastOutput = new TextComponentString(var3.getMessage());
         }
      } else {
         this.lastOutput = null;
      }

      this.resultStats.readStatsFromNBT(var1);
   }

   public boolean canUseCommand(int var1, String var2) {
      return var1 <= 2;
   }

   public void setCommand(String var1) {
      this.commandStored = var1;
      this.successCount = 0;
   }

   public String getCommand() {
      return this.commandStored;
   }

   public void trigger(World var1) {
      if (var1.isRemote) {
         this.successCount = 0;
      } else if ("Searge".equalsIgnoreCase(this.commandStored)) {
         this.lastOutput = new TextComponentString("#itzlipofutzli");
         this.successCount = 1;
      } else {
         MinecraftServer var2 = this.h();
         if (var2 != null && var2.M() && var2.getEnableCommandBlock()) {
            var2.getCommandHandler();

            try {
               this.lastOutput = null;
               this.successCount = executeCommand(this, this.sender, this.commandStored);
            } catch (Throwable var6) {
               CrashReport var4 = CrashReport.makeCrashReport(var6, "Executing command block");
               CrashReportCategory var5 = var4.makeCategory("Command to be executed");
               var5.setDetail("Command", new ICrashReportDetail() {
                  public String call() throws Exception {
                     return CommandBlockBaseLogic.this.getCommand();
                  }

                  public Object call() throws Exception {
                     return this.call();
                  }
               });
               var5.setDetail("Name", new ICrashReportDetail() {
                  public String call() throws Exception {
                     return CommandBlockBaseLogic.this.getName();
                  }

                  public Object call() throws Exception {
                     return this.call();
                  }
               });
               throw new ReportedException(var4);
            }
         } else {
            this.successCount = 0;
         }
      }

   }

   public static int executeCommand(ICommandSender var0, CommandSender var1, String var2) {
      SimpleCommandMap var3 = var0.getEntityWorld().getServer().getCommandMap();
      Joiner var4 = Joiner.on(" ");
      if (var2.startsWith("/")) {
         var2 = var2.substring(1);
      }

      String[] var5 = var2.split(" ");
      ArrayList var6 = new ArrayList();
      String var7 = var5[0];
      if (var7.startsWith("minecraft:")) {
         var7 = var7.substring("minecraft:".length());
      }

      if (var7.startsWith("bukkit:")) {
         var7 = var7.substring("bukkit:".length());
      }

      if (!var7.equalsIgnoreCase("stop") && !var7.equalsIgnoreCase("kick") && !var7.equalsIgnoreCase("op") && !var7.equalsIgnoreCase("deop") && !var7.equalsIgnoreCase("ban") && !var7.equalsIgnoreCase("ban-ip") && !var7.equalsIgnoreCase("pardon") && !var7.equalsIgnoreCase("pardon-ip") && !var7.equalsIgnoreCase("reload")) {
         Command var8 = var3.getCommand(var5[0]);
         if (var0.getEntityWorld().getServer().getCommandBlockOverride(var5[0])) {
            var8 = var3.getCommand("minecraft:" + var5[0]);
         }

         if (var8 instanceof VanillaCommandWrapper) {
            var2 = var2.trim();
            if (var2.startsWith("/")) {
               var2 = var2.substring(1);
            }

            String[] var21 = var2.split(" ");
            var21 = VanillaCommandWrapper.dropFirstArgument(var21);
            return !((VanillaCommandWrapper)var8).testPermission(var1) ? 0 : ((VanillaCommandWrapper)var8).dispatchVanillaCommand(var1, var0, var21);
         } else if (var3.getCommand(var5[0]) == null) {
            return 0;
         } else {
            var6.add(var5);
            WorldServer[] var9 = MinecraftServer.getServer().worldServer;
            MinecraftServer var10 = MinecraftServer.getServer();
            var10.worldServer = new WorldServer[var10.worlds.size()];
            var10.worldServer[0] = (WorldServer)var0.getEntityWorld();
            int var11 = 0;

            for(int var12 = 1; var12 < var10.worldServer.length; ++var12) {
               WorldServer var13 = (WorldServer)var10.worlds.get(var11++);
               if (var10.worldServer[0] == var13) {
                  --var12;
               } else {
                  var10.worldServer[var12] = var13;
               }
            }

            try {
               ArrayList var23 = new ArrayList();

               for(int var25 = 0; var25 < var5.length; ++var25) {
                  if (EntitySelector.hasArguments(var5[var25])) {
                     for(int var14 = 0; var14 < var6.size(); ++var14) {
                        var23.addAll(buildCommands(var0, (String[])var6.get(var14), var25));
                     }

                     ArrayList var27 = var6;
                     var6 = var23;
                     var23 = var27;
                     var27.clear();
                  }
               }
            } finally {
               MinecraftServer.getServer().worldServer = var9;
            }

            int var24 = 0;

            for(int var26 = 0; var26 < var6.size(); ++var26) {
               try {
                  if (var3.dispatch(var1, var4.join(Arrays.asList((String[])var6.get(var26))))) {
                     ++var24;
                  }
               } catch (Throwable var18) {
                  if (var0.getCommandSenderEntity() instanceof EntityMinecartCommandBlock) {
                     MinecraftServer.getServer().server.getLogger().log(Level.WARNING, String.format("MinecartCommandBlock at (%d,%d,%d) failed to handle command", var0.getPosition().getX(), var0.getPosition().getY(), var0.getPosition().getZ()), var18);
                  } else if (var0 instanceof CommandBlockBaseLogic) {
                     CommandBlockBaseLogic var15 = (CommandBlockBaseLogic)var0;
                     MinecraftServer.getServer().server.getLogger().log(Level.WARNING, String.format("CommandBlock at (%d,%d,%d) failed to handle command", var15.getPosition().getX(), var15.getPosition().getY(), var15.getPosition().getZ()), var18);
                  } else {
                     MinecraftServer.getServer().server.getLogger().log(Level.WARNING, String.format("Unknown CommandBlock failed to handle command"), var18);
                  }
               }
            }

            return var24;
         }
      } else {
         return 0;
      }
   }

   private static ArrayList buildCommands(ICommandSender var0, String[] var1, int var2) {
      ArrayList var3 = new ArrayList();
      List var4 = EntitySelector.matchEntities(var0, var1[var2], EntityPlayerMP.class);
      if (var4 != null) {
         for(EntityPlayerMP var6 : var4) {
            if (var6.world == var0.getEntityWorld()) {
               String[] var7 = (String[])var1.clone();
               var7[var2] = var6.getName();
               var3.add(var7);
            }
         }
      }

      return var3;
   }

   public String getName() {
      return this.customName;
   }

   public ITextComponent getDisplayName() {
      return new TextComponentString(this.getName());
   }

   public void setName(String var1) {
      this.customName = var1;
   }

   public void sendMessage(ITextComponent var1) {
      if (this.trackOutput && this.getEntityWorld() != null && !this.getEntityWorld().isRemote) {
         this.lastOutput = (new TextComponentString("[" + TIMESTAMP_FORMAT.format(new Date()) + "] ")).appendSibling(var1);
         this.updateCommand();
      }

   }

   public boolean sendCommandFeedback() {
      MinecraftServer var1 = this.h();
      return var1 == null || !var1.M() || var1.worldServer[0].getGameRules().getBoolean("commandBlockOutput");
   }

   public void setCommandStat(CommandResultStats.Type var1, int var2) {
      this.resultStats.a(this.h(), this, var1, var2);
   }

   public abstract void updateCommand();

   public void setLastOutput(@Nullable ITextComponent var1) {
      this.lastOutput = var1;
   }

   public void setTrackOutput(boolean var1) {
      this.trackOutput = var1;
   }

   public boolean shouldTrackOutput() {
      return this.trackOutput;
   }

   public boolean tryOpenEditCommandBlock(EntityPlayer var1) {
      if (!var1.canUseCommandBlock()) {
         return false;
      } else {
         if (var1.getEntityWorld().isRemote) {
            var1.displayGuiEditCommandCart(this);
         }

         return true;
      }
   }

   public CommandResultStats getCommandResultStats() {
      return this.resultStats;
   }
}
