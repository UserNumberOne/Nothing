package net.minecraft.inventory;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Slot {
   private final int slotIndex;
   public final IInventory inventory;
   public int slotNumber;
   public int xPos;
   public int yPos;
   protected String backgroundName = null;
   protected ResourceLocation backgroundLocation = null;
   protected Object backgroundMap;

   public Slot(IInventory var1, int var2, int var3, int var4) {
      this.inventory = inventoryIn;
      this.slotIndex = index;
      this.xPos = xPosition;
      this.yPos = yPosition;
   }

   public void onSlotChange(ItemStack var1, ItemStack var2) {
      if (p_75220_1_ != null && p_75220_2_ != null && p_75220_1_.getItem() == p_75220_2_.getItem()) {
         int i = p_75220_2_.stackSize - p_75220_1_.stackSize;
         if (i > 0) {
            this.onCrafting(p_75220_1_, i);
         }
      }

   }

   protected void onCrafting(ItemStack var1, int var2) {
   }

   protected void onCrafting(ItemStack var1) {
   }

   public void onPickupFromSlot(EntityPlayer var1, ItemStack var2) {
      this.onSlotChanged();
   }

   public boolean isItemValid(@Nullable ItemStack var1) {
      return true;
   }

   @Nullable
   public ItemStack getStack() {
      return this.inventory.getStackInSlot(this.slotIndex);
   }

   public boolean getHasStack() {
      return this.getStack() != null;
   }

   public void putStack(@Nullable ItemStack var1) {
      this.inventory.setInventorySlotContents(this.slotIndex, stack);
      this.onSlotChanged();
   }

   public void onSlotChanged() {
      this.inventory.markDirty();
   }

   public int getSlotStackLimit() {
      return this.inventory.getInventoryStackLimit();
   }

   public int getItemStackLimit(ItemStack var1) {
      return this.getSlotStackLimit();
   }

   @Nullable
   @SideOnly(Side.CLIENT)
   public String getSlotTexture() {
      return this.backgroundName;
   }

   public ItemStack decrStackSize(int var1) {
      return this.inventory.decrStackSize(this.slotIndex, amount);
   }

   public boolean isHere(IInventory var1, int var2) {
      return inv == this.inventory && slotIn == this.slotIndex;
   }

   public boolean canTakeStack(EntityPlayer var1) {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public boolean canBeHovered() {
      return true;
   }

   @SideOnly(Side.CLIENT)
   public ResourceLocation getBackgroundLocation() {
      return this.backgroundLocation == null ? TextureMap.LOCATION_BLOCKS_TEXTURE : this.backgroundLocation;
   }

   @SideOnly(Side.CLIENT)
   public void setBackgroundLocation(ResourceLocation var1) {
      this.backgroundLocation = texture;
   }

   public void setBackgroundName(String var1) {
      this.backgroundName = name;
   }

   @SideOnly(Side.CLIENT)
   public TextureAtlasSprite getBackgroundSprite() {
      String name = this.getSlotTexture();
      return name == null ? null : this.getBackgroundMap().getAtlasSprite(name);
   }

   @SideOnly(Side.CLIENT)
   protected TextureMap getBackgroundMap() {
      if (this.backgroundMap == null) {
         this.backgroundMap = Minecraft.getMinecraft().getTextureMapBlocks();
      }

      return (TextureMap)this.backgroundMap;
   }

   public int getSlotIndex() {
      return this.slotIndex;
   }

   public boolean isSameInventory(Slot var1) {
      return this.inventory == other.inventory;
   }
}
