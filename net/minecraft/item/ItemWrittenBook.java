package net.minecraft.item;

import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.StringUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemWrittenBook extends Item {
   public ItemWrittenBook() {
      this.setMaxStackSize(1);
   }

   public static boolean validBookTagContents(NBTTagCompound var0) {
      if (!ItemWritableBook.isNBTValid(nbt)) {
         return false;
      } else if (!nbt.hasKey("title", 8)) {
         return false;
      } else {
         String s = nbt.getString("title");
         return s != null && s.length() <= 32 ? nbt.hasKey("author", 8) : false;
      }
   }

   public static int getGeneration(ItemStack var0) {
      return book.getTagCompound().getInteger("generation");
   }

   public String getItemStackDisplayName(ItemStack var1) {
      if (stack.hasTagCompound()) {
         NBTTagCompound nbttagcompound = stack.getTagCompound();
         String s = nbttagcompound.getString("title");
         if (!StringUtils.isNullOrEmpty(s)) {
            return s;
         }
      }

      return super.getItemStackDisplayName(stack);
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
      if (stack.hasTagCompound()) {
         NBTTagCompound nbttagcompound = stack.getTagCompound();
         String s = nbttagcompound.getString("author");
         if (!StringUtils.isNullOrEmpty(s)) {
            tooltip.add(TextFormatting.GRAY + I18n.translateToLocalFormatted("book.byAuthor", s));
         }

         tooltip.add(TextFormatting.GRAY + I18n.translateToLocal("book.generation." + nbttagcompound.getInteger("generation")));
      }

   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      if (!worldIn.isRemote) {
         this.resolveContents(itemStackIn, playerIn);
      }

      playerIn.openBook(itemStackIn, hand);
      playerIn.addStat(StatList.getObjectUseStats(this));
      return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
   }

   private void resolveContents(ItemStack var1, EntityPlayer var2) {
      if (stack != null && stack.getTagCompound() != null) {
         NBTTagCompound nbttagcompound = stack.getTagCompound();
         if (!nbttagcompound.getBoolean("resolved")) {
            nbttagcompound.setBoolean("resolved", true);
            if (validBookTagContents(nbttagcompound)) {
               NBTTagList nbttaglist = nbttagcompound.getTagList("pages", 8);

               for(int i = 0; i < nbttaglist.tagCount(); ++i) {
                  String s = nbttaglist.getStringTagAt(i);

                  ITextComponent lvt_7_1_;
                  try {
                     lvt_7_1_ = ITextComponent.Serializer.fromJsonLenient(s);
                     lvt_7_1_ = TextComponentUtils.processComponent(player, lvt_7_1_, player);
                  } catch (Exception var9) {
                     lvt_7_1_ = new TextComponentString(s);
                  }

                  nbttaglist.set(i, new NBTTagString(ITextComponent.Serializer.componentToJson(lvt_7_1_)));
               }

               nbttagcompound.setTag("pages", nbttaglist);
               if (player instanceof EntityPlayerMP && player.getHeldItemMainhand() == stack) {
                  Slot slot = player.openContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem);
                  ((EntityPlayerMP)player).connection.sendPacket(new SPacketSetSlot(0, slot.slotNumber, stack));
               }
            }
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean hasEffect(ItemStack var1) {
      return true;
   }
}
