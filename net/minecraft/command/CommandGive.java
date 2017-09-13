package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;

public class CommandGive extends CommandBase {
   public String getName() {
      return "give";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.give.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 2) {
         throw new WrongUsageException("commands.give.usage", new Object[0]);
      } else {
         EntityPlayerMP var4 = a(var1, var2, var3[0]);
         Item var5 = getItemByText(var2, var3[1]);
         int var6 = var3.length >= 3 ? parseInt(var3[2], 1, 64) : 1;
         int var7 = var3.length >= 4 ? parseInt(var3[3]) : 0;
         ItemStack var8 = new ItemStack(var5, var6, var7);
         if (var3.length >= 5) {
            String var9 = getChatComponentFromNthArg(var2, var3, 4).getUnformattedText();

            try {
               var8.setTagCompound(JsonToNBT.getTagFromJson(var9));
            } catch (NBTException var11) {
               throw new CommandException("commands.give.tagError", new Object[]{var11.getMessage()});
            }
         }

         boolean var12 = var4.inventory.addItemStackToInventory(var8);
         if (var12) {
            var4.world.playSound((EntityPlayer)null, var4.posX, var4.posY, var4.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((var4.getRNG().nextFloat() - var4.getRNG().nextFloat()) * 0.7F + 1.0F) * 2.0F);
            var4.inventoryContainer.detectAndSendChanges();
         }

         if (var12 && var8.stackSize <= 0) {
            var8.stackSize = 1;
            var2.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, var6);
            EntityItem var13 = var4.dropItem(var8, false);
            if (var13 != null) {
               var13.makeFakeItem();
            }
         } else {
            var2.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, var6 - var8.stackSize);
            EntityItem var10 = var4.dropItem(var8, false);
            if (var10 != null) {
               var10.setNoPickupDelay();
               var10.setOwner(var4.getName());
            }
         }

         notifyCommandListener(var2, this, "commands.give.success", new Object[]{var8.getTextComponent(), var6, var4.getName()});
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         return getListOfStringsMatchingLastWord(var3, var1.getPlayers());
      } else {
         return var3.length == 2 ? getListOfStringsMatchingLastWord(var3, Item.REGISTRY.getKeys()) : Collections.emptyList();
      }
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 0;
   }
}
