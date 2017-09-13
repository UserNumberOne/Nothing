package net.minecraft.tileentity;

import io.netty.buffer.ByteBuf;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.Nullable;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class CommandBlockBaseLogic implements ICommandSender {
   private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("HH:mm:ss");
   private int successCount;
   private boolean trackOutput = true;
   private ITextComponent lastOutput;
   private String commandStored = "";
   private String customName = "@";
   private final CommandResultStats resultStats = new CommandResultStats();

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
         MinecraftServer var2 = this.getServer();
         if (var2 != null && var2.isAnvilFileSet() && var2.isCommandBlockEnabled()) {
            ICommandManager var3 = var2.getCommandManager();

            try {
               this.lastOutput = null;
               this.successCount = var3.executeCommand(this, this.commandStored);
            } catch (Throwable var7) {
               CrashReport var5 = CrashReport.makeCrashReport(var7, "Executing command block");
               CrashReportCategory var6 = var5.makeCategory("Command to be executed");
               var6.setDetail("Command", new ICrashReportDetail() {
                  public String call() throws Exception {
                     return CommandBlockBaseLogic.this.getCommand();
                  }
               });
               var6.setDetail("Name", new ICrashReportDetail() {
                  public String call() throws Exception {
                     return CommandBlockBaseLogic.this.getName();
                  }
               });
               throw new ReportedException(var5);
            }
         } else {
            this.successCount = 0;
         }
      }

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
      MinecraftServer var1 = this.getServer();
      return var1 == null || !var1.isAnvilFileSet() || var1.worlds[0].getGameRules().getBoolean("commandBlockOutput");
   }

   public void setCommandStat(CommandResultStats.Type var1, int var2) {
      this.resultStats.setCommandStatForSender(this.getServer(), this, var1, var2);
   }

   public abstract void updateCommand();

   @SideOnly(Side.CLIENT)
   public abstract int getCommandBlockType();

   @SideOnly(Side.CLIENT)
   public abstract void fillInInfo(ByteBuf var1);

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
