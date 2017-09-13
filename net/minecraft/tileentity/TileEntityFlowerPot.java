package net.minecraft.tileentity;

import javax.annotation.Nullable;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;

public class TileEntityFlowerPot extends TileEntity {
   private Item flowerPotItem;
   private int flowerPotData;

   public TileEntityFlowerPot() {
   }

   public TileEntityFlowerPot(Item var1, int var2) {
      this.flowerPotItem = var1;
      this.flowerPotData = var2;
   }

   public static void registerFixesFlowerPot(DataFixer var0) {
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);
      ResourceLocation var2 = (ResourceLocation)Item.REGISTRY.getNameForObject(this.flowerPotItem);
      var1.setString("Item", var2 == null ? "" : var2.toString());
      var1.setInteger("Data", this.flowerPotData);
      return var1;
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      if (var1.hasKey("Item", 8)) {
         this.flowerPotItem = Item.getByNameOrId(var1.getString("Item"));
      } else {
         this.flowerPotItem = Item.getItemById(var1.getInteger("Item"));
      }

      this.flowerPotData = var1.getInteger("Data");
   }

   @Nullable
   public SPacketUpdateTileEntity getUpdatePacket() {
      return new SPacketUpdateTileEntity(this.pos, 5, this.getUpdateTag());
   }

   public NBTTagCompound getUpdateTag() {
      return this.writeToNBT(new NBTTagCompound());
   }

   public void setFlowerPotData(Item var1, int var2) {
      this.flowerPotItem = var1;
      this.flowerPotData = var2;
   }

   @Nullable
   public ItemStack getFlowerItemStack() {
      return this.flowerPotItem == null ? null : new ItemStack(this.flowerPotItem, 1, this.flowerPotData);
   }

   @Nullable
   public Item getFlowerPotItem() {
      return this.flowerPotItem;
   }

   public int getFlowerPotData() {
      return this.flowerPotData;
   }
}
