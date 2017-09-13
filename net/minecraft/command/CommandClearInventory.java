package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandClearInventory extends CommandBase {
   public String getName() {
      return "clear";
   }

   public String getUsage(ICommandSender var1) {
      return "commands.clear.usage";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      EntityPlayerMP var4 = var3.length == 0 ? getCommandSenderAsPlayer(var2) : getPlayer(var1, var2, var3[0]);
      Item var5 = var3.length >= 2 ? getItemByText(var2, var3[1]) : null;
      int var6 = var3.length >= 3 ? parseInt(var3[2], -1) : -1;
      int var7 = var3.length >= 4 ? parseInt(var3[3], -1) : -1;
      NBTTagCompound var8 = null;
      if (var3.length >= 5) {
         try {
            var8 = JsonToNBT.getTagFromJson(buildString(var3, 4));
         } catch (NBTException var10) {
            throw new CommandException("commands.clear.tagError", new Object[]{var10.getMessage()});
         }
      }

      if (var3.length >= 2 && var5 == null) {
         throw new CommandException("commands.clear.failure", new Object[]{var4.getName()});
      } else {
         int var9 = var4.inventory.clearMatchingItems(var5, var6, var7, var8);
         var4.inventoryContainer.detectAndSendChanges();
         if (!var4.capabilities.isCreativeMode) {
            var4.updateHeldItem();
         }

         var2.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, var9);
         if (var9 == 0) {
            throw new CommandException("commands.clear.failure", new Object[]{var4.getName()});
         } else {
            if (var7 == 0) {
               var2.sendMessage(new TextComponentTranslation("commands.clear.testing", new Object[]{var4.getName(), var9}));
            } else {
               notifyCommandListener(var2, this, "commands.clear.success", new Object[]{var4.getName(), var9});
            }

         }
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames()) : (var3.length == 2 ? getListOfStringsMatchingLastWord(var3, Item.REGISTRY.getKeys()) : Collections.emptyList());
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 0;
   }
}
