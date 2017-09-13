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
      if (!ItemWritableBook.isNBTValid(var0)) {
         return false;
      } else if (!var0.hasKey("title", 8)) {
         return false;
      } else {
         String var1 = var0.getString("title");
         return var1 != null && var1.length() <= 32 ? var0.hasKey("author", 8) : false;
      }
   }

   public static int getGeneration(ItemStack var0) {
      return var0.getTagCompound().getInteger("generation");
   }

   public String getItemStackDisplayName(ItemStack var1) {
      if (var1.hasTagCompound()) {
         NBTTagCompound var2 = var1.getTagCompound();
         String var3 = var2.getString("title");
         if (!StringUtils.isNullOrEmpty(var3)) {
            return var3;
         }
      }

      return super.getItemStackDisplayName(var1);
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
      if (var1.hasTagCompound()) {
         NBTTagCompound var5 = var1.getTagCompound();
         String var6 = var5.getString("author");
         if (!StringUtils.isNullOrEmpty(var6)) {
            var3.add(TextFormatting.GRAY + I18n.translateToLocalFormatted("book.byAuthor", var6));
         }

         var3.add(TextFormatting.GRAY + I18n.translateToLocal("book.generation." + var5.getInteger("generation")));
      }

   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      if (!var2.isRemote) {
         this.resolveContents(var1, var3);
      }

      var3.openBook(var1, var4);
      var3.addStat(StatList.getObjectUseStats(this));
      return new ActionResult(EnumActionResult.SUCCESS, var1);
   }

   private void resolveContents(ItemStack var1, EntityPlayer var2) {
      if (var1 != null && var1.getTagCompound() != null) {
         NBTTagCompound var3 = var1.getTagCompound();
         if (!var3.getBoolean("resolved")) {
            var3.setBoolean("resolved", true);
            if (validBookTagContents(var3)) {
               NBTTagList var4 = var3.getTagList("pages", 8);

               for(int var5 = 0; var5 < var4.tagCount(); ++var5) {
                  String var6 = var4.getStringTagAt(var5);

                  ITextComponent var7;
                  try {
                     var7 = ITextComponent.Serializer.fromJsonLenient(var6);
                     var7 = TextComponentUtils.processComponent(var2, var7, var2);
                  } catch (Exception var9) {
                     var7 = new TextComponentString(var6);
                  }

                  var4.set(var5, new NBTTagString(ITextComponent.Serializer.componentToJson(var7)));
               }

               var3.setTag("pages", var4);
               if (var2 instanceof EntityPlayerMP && var2.getHeldItemMainhand() == var1) {
                  Slot var10 = var2.openContainer.getSlotFromInventory(var2.inventory, var2.inventory.currentItem);
                  ((EntityPlayerMP)var2).connection.sendPacket(new SPacketSetSlot(0, var10.slotNumber, var1));
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
