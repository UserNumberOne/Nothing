package net.minecraft.command;

import com.google.common.collect.Maps;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.src.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CommandReplaceItem extends CommandBase {
   private static final Map SHORTCUTS = Maps.newHashMap();

   public String getName() {
      return "replaceitem";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.replaceitem.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 1) {
         throw new WrongUsageException("commands.replaceitem.usage", new Object[0]);
      } else {
         boolean var4;
         if ("entity".equals(var3[0])) {
            var4 = false;
         } else {
            if (!"block".equals(var3[0])) {
               throw new WrongUsageException("commands.replaceitem.usage", new Object[0]);
            }

            var4 = true;
         }

         int var5;
         if (var4) {
            if (var3.length < 6) {
               throw new WrongUsageException("commands.replaceitem.block.usage", new Object[0]);
            }

            var5 = 4;
         } else {
            if (var3.length < 4) {
               throw new WrongUsageException("commands.replaceitem.entity.usage", new Object[0]);
            }

            var5 = 2;
         }

         String var6 = var3[var5];
         int var7 = this.getSlotForShortcut(var3[var5++]);

         Item var8;
         try {
            var8 = getItemByText(var2, var3[var5]);
         } catch (NumberInvalidException var17) {
            if (Block.getBlockFromName(var3[var5]) != Blocks.AIR) {
               throw var17;
            }

            var8 = null;
         }

         ++var5;
         int var9 = var3.length > var5 ? parseInt(var3[var5++], 1, 64) : 1;
         int var10 = var3.length > var5 ? parseInt(var3[var5++]) : 0;
         ItemStack var11 = new ItemStack(var8, var9, var10);
         if (var3.length > var5) {
            String var12 = getChatComponentFromNthArg(var2, var3, var5).getUnformattedText();

            try {
               var11.setTagCompound(JsonToNBT.getTagFromJson(var12));
            } catch (NBTException var16) {
               throw new CommandException("commands.replaceitem.tagError", new Object[]{var16.getMessage()});
            }
         }

         if (var11.getItem() == null) {
            var11 = null;
         }

         if (var4) {
            var2.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 0);
            BlockPos var20 = parseBlockPos(var2, var3, 1, false);
            World var13 = var2.getEntityWorld();
            TileEntity var14 = var13.getTileEntity(var20);
            if (var14 == null || !(var14 instanceof IInventory)) {
               throw new CommandException("commands.replaceitem.noContainer", new Object[]{var20.getX(), var20.getY(), var20.getZ()});
            }

            IInventory var15 = (IInventory)var14;
            if (var7 >= 0 && var7 < var15.getSizeInventory()) {
               var15.setInventorySlotContents(var7, var11);
            }
         } else {
            Entity var21 = b(var1, var2, var3[1]);
            var2.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, 0);
            if (var21 instanceof EntityPlayer) {
               ((EntityPlayer)var21).inventoryContainer.detectAndSendChanges();
            }

            if (!var21.replaceItemInInventory(var7, var11)) {
               throw new CommandException("commands.replaceitem.failed", new Object[]{var6, var9, var11 == null ? "Air" : var11.getTextComponent()});
            }

            if (var21 instanceof EntityPlayer) {
               ((EntityPlayer)var21).inventoryContainer.detectAndSendChanges();
            }
         }

         var2.setCommandStat(CommandResultStats.Type.AFFECTED_ITEMS, var9);
         notifyCommandListener(var2, this, "commands.replaceitem.success", new Object[]{var6, var9, var11 == null ? "Air" : var11.getTextComponent()});
      }
   }

   private int getSlotForShortcut(String var1) throws CommandException {
      if (!SHORTCUTS.containsKey(var1)) {
         throw new CommandException("commands.generic.parameter.invalid", new Object[]{var1});
      } else {
         return ((Integer)SHORTCUTS.get(var1)).intValue();
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         return getListOfStringsMatchingLastWord(var3, new String[]{"entity", "block"});
      } else if (var3.length == 2 && "entity".equals(var3[0])) {
         return getListOfStringsMatchingLastWord(var3, var1.getPlayers());
      } else if (var3.length >= 2 && var3.length <= 4 && "block".equals(var3[0])) {
         return getTabCompletionCoordinate(var3, 1, var4);
      } else if ((var3.length != 3 || !"entity".equals(var3[0])) && (var3.length != 5 || !"block".equals(var3[0]))) {
         return (var3.length != 4 || !"entity".equals(var3[0])) && (var3.length != 6 || !"block".equals(var3[0])) ? Collections.emptyList() : getListOfStringsMatchingLastWord(var3, Item.REGISTRY.getKeys());
      } else {
         return getListOfStringsMatchingLastWord(var3, SHORTCUTS.keySet());
      }
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var1.length > 0 && "entity".equals(var1[0]) && var2 == 1;
   }

   static {
      for(int var0 = 0; var0 < 54; ++var0) {
         SHORTCUTS.put("slot.container." + var0, Integer.valueOf(var0));
      }

      for(int var1 = 0; var1 < 9; ++var1) {
         SHORTCUTS.put("slot.hotbar." + var1, Integer.valueOf(var1));
      }

      for(int var2 = 0; var2 < 27; ++var2) {
         SHORTCUTS.put("slot.inventory." + var2, Integer.valueOf(9 + var2));
      }

      for(int var3 = 0; var3 < 27; ++var3) {
         SHORTCUTS.put("slot.enderchest." + var3, Integer.valueOf(200 + var3));
      }

      for(int var4 = 0; var4 < 8; ++var4) {
         SHORTCUTS.put("slot.villager." + var4, Integer.valueOf(300 + var4));
      }

      for(int var5 = 0; var5 < 15; ++var5) {
         SHORTCUTS.put("slot.horse." + var5, Integer.valueOf(500 + var5));
      }

      SHORTCUTS.put("slot.weapon", Integer.valueOf(98));
      SHORTCUTS.put("slot.weapon.mainhand", Integer.valueOf(98));
      SHORTCUTS.put("slot.weapon.offhand", Integer.valueOf(99));
      SHORTCUTS.put("slot.armor.head", Integer.valueOf(100 + EntityEquipmentSlot.HEAD.getIndex()));
      SHORTCUTS.put("slot.armor.chest", Integer.valueOf(100 + EntityEquipmentSlot.CHEST.getIndex()));
      SHORTCUTS.put("slot.armor.legs", Integer.valueOf(100 + EntityEquipmentSlot.LEGS.getIndex()));
      SHORTCUTS.put("slot.armor.feet", Integer.valueOf(100 + EntityEquipmentSlot.FEET.getIndex()));
      SHORTCUTS.put("slot.horse.saddle", Integer.valueOf(400));
      SHORTCUTS.put("slot.horse.armor", Integer.valueOf(401));
      SHORTCUTS.put("slot.horse.chest", Integer.valueOf(499));
   }
}
