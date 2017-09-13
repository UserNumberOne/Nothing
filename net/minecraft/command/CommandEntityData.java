package net.minecraft.command;

import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.src.MinecraftServer;

public class CommandEntityData extends CommandBase {
   public String getName() {
      return "entitydata";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.entitydata.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 2) {
         throw new WrongUsageException("commands.entitydata.usage", new Object[0]);
      } else {
         Entity var4 = b(var1, var2, var3[0]);
         if (var4 instanceof EntityPlayer) {
            throw new CommandException("commands.entitydata.noPlayers", new Object[]{var4.getDisplayName()});
         } else {
            NBTTagCompound var5 = entityToNBT(var4);
            NBTTagCompound var6 = var5.copy();

            NBTTagCompound var7;
            try {
               var7 = JsonToNBT.getTagFromJson(getChatComponentFromNthArg(var2, var3, 1).getUnformattedText());
            } catch (NBTException var9) {
               throw new CommandException("commands.entitydata.tagError", new Object[]{var9.getMessage()});
            }

            UUID var8 = var4.getUniqueID();
            var5.merge(var7);
            var4.setUniqueId(var8);
            if (var5.equals(var6)) {
               throw new CommandException("commands.entitydata.failed", new Object[]{var5.toString()});
            } else {
               var4.readFromNBT(var5);
               notifyCommandListener(var2, this, "commands.entitydata.success", new Object[]{var5.toString()});
            }
         }
      }
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 0;
   }
}
