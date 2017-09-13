package net.minecraft.command.server;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class CommandTestFor extends CommandBase {
   public String getName() {
      return "testfor";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.testfor.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 1) {
         throw new WrongUsageException("commands.testfor.usage", new Object[0]);
      } else {
         Entity var4 = getEntity(var1, var2, var3[0]);
         NBTTagCompound var5 = null;
         if (var3.length >= 2) {
            try {
               var5 = JsonToNBT.getTagFromJson(buildString(var3, 1));
            } catch (NBTException var7) {
               throw new CommandException("commands.testfor.tagError", new Object[]{var7.getMessage()});
            }
         }

         if (var5 != null) {
            NBTTagCompound var6 = entityToNBT(var4);
            if (!NBTUtil.areNBTEquals(var5, var6, true)) {
               throw new CommandException("commands.testfor.failure", new Object[]{var4.getName()});
            }
         }

         notifyCommandListener(var2, this, "commands.testfor.success", new Object[]{var4.getName()});
      }
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 0;
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames()) : Collections.emptyList();
   }
}
