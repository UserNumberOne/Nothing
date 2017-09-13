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
      this.successCount = successCountIn;
   }

   public ITextComponent getLastOutput() {
      return (ITextComponent)(this.lastOutput == null ? new TextComponentString("") : this.lastOutput);
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      p_189510_1_.setString("Command", this.commandStored);
      p_189510_1_.setInteger("SuccessCount", this.successCount);
      p_189510_1_.setString("CustomName", this.customName);
      p_189510_1_.setBoolean("TrackOutput", this.trackOutput);
      if (this.lastOutput != null && this.trackOutput) {
         p_189510_1_.setString("LastOutput", ITextComponent.Serializer.componentToJson(this.lastOutput));
      }

      this.resultStats.writeStatsToNBT(p_189510_1_);
      return p_189510_1_;
   }

   public void readDataFromNBT(NBTTagCompound var1) {
      this.commandStored = nbt.getString("Command");
      this.successCount = nbt.getInteger("SuccessCount");
      if (nbt.hasKey("CustomName", 8)) {
         this.customName = nbt.getString("CustomName");
      }

      if (nbt.hasKey("TrackOutput", 1)) {
         this.trackOutput = nbt.getBoolean("TrackOutput");
      }

      if (nbt.hasKey("LastOutput", 8) && this.trackOutput) {
         try {
            this.lastOutput = ITextComponent.Serializer.jsonToComponent(nbt.getString("LastOutput"));
         } catch (Throwable var3) {
            this.lastOutput = new TextComponentString(var3.getMessage());
         }
      } else {
         this.lastOutput = null;
      }

      this.resultStats.readStatsFromNBT(nbt);
   }

   public boolean canUseCommand(int var1, String var2) {
      return permLevel <= 2;
   }

   public void setCommand(String var1) {
      this.commandStored = command;
      this.successCount = 0;
   }

   public String getCommand() {
      return this.commandStored;
   }

   public void trigger(World var1) {
      if (worldIn.isRemote) {
         this.successCount = 0;
      } else if ("Searge".equalsIgnoreCase(this.commandStored)) {
         this.lastOutput = new TextComponentString("#itzlipofutzli");
         this.successCount = 1;
      } else {
         MinecraftServer minecraftserver = this.getServer();
         if (minecraftserver != null && minecraftserver.isAnvilFileSet() && minecraftserver.isCommandBlockEnabled()) {
            ICommandManager icommandmanager = minecraftserver.getCommandManager();

            try {
               this.lastOutput = null;
               this.successCount = icommandmanager.executeCommand(this, this.commandStored);
            } catch (Throwable var7) {
               CrashReport crashreport = CrashReport.makeCrashReport(var7, "Executing command block");
               CrashReportCategory crashreportcategory = crashreport.makeCategory("Command to be executed");
               crashreportcategory.setDetail("Command", new ICrashReportDetail() {
                  public String call() throws Exception {
                     return CommandBlockBaseLogic.this.getCommand();
                  }
               });
               crashreportcategory.setDetail("Name", new ICrashReportDetail() {
                  public String call() throws Exception {
                     return CommandBlockBaseLogic.this.getName();
                  }
               });
               throw new ReportedException(crashreport);
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
      this.customName = name;
   }

   public void sendMessage(ITextComponent var1) {
      if (this.trackOutput && this.getEntityWorld() != null && !this.getEntityWorld().isRemote) {
         this.lastOutput = (new TextComponentString("[" + TIMESTAMP_FORMAT.format(new Date()) + "] ")).appendSibling(component);
         this.updateCommand();
      }

   }

   public boolean sendCommandFeedback() {
      MinecraftServer minecraftserver = this.getServer();
      return minecraftserver == null || !minecraftserver.isAnvilFileSet() || minecraftserver.worlds[0].getGameRules().getBoolean("commandBlockOutput");
   }

   public void setCommandStat(CommandResultStats.Type var1, int var2) {
      this.resultStats.setCommandStatForSender(this.getServer(), this, type, amount);
   }

   public abstract void updateCommand();

   @SideOnly(Side.CLIENT)
   public abstract int getCommandBlockType();

   @SideOnly(Side.CLIENT)
   public abstract void fillInInfo(ByteBuf var1);

   public void setLastOutput(@Nullable ITextComponent var1) {
      this.lastOutput = lastOutputMessage;
   }

   public void setTrackOutput(boolean var1) {
      this.trackOutput = shouldTrackOutput;
   }

   public boolean shouldTrackOutput() {
      return this.trackOutput;
   }

   public boolean tryOpenEditCommandBlock(EntityPlayer var1) {
      if (!playerIn.canUseCommandBlock()) {
         return false;
      } else {
         if (playerIn.getEntityWorld().isRemote) {
            playerIn.displayGuiEditCommandCart(this);
         }

         return true;
      }
   }

   public CommandResultStats getCommandResultStats() {
      return this.resultStats;
   }
}
