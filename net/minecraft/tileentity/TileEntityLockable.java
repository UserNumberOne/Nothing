package net.minecraft.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public abstract class TileEntityLockable extends TileEntity implements IInteractionObject, ILockableContainer {
   private LockCode code = LockCode.EMPTY_CODE;
   private IItemHandler itemHandler;

   public void readFromNBT(NBTTagCompound compound) {
      super.readFromNBT(compound);
      this.code = LockCode.fromNBT(compound);
   }

   public NBTTagCompound writeToNBT(NBTTagCompound compound) {
      super.writeToNBT(compound);
      if (this.code != null) {
         this.code.toNBT(compound);
      }

      return compound;
   }

   public boolean isLocked() {
      return this.code != null && !this.code.isEmpty();
   }

   public LockCode getLockCode() {
      return this.code;
   }

   public void setLockCode(LockCode code) {
      this.code = code;
   }

   public ITextComponent getDisplayName() {
      return (ITextComponent)(this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]));
   }

   protected IItemHandler createUnSidedHandler() {
      return new InvWrapper(this);
   }

   public Object getCapability(Capability capability, EnumFacing facing) {
      if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
         return this.itemHandler == null ? (this.itemHandler = this.createUnSidedHandler()) : this.itemHandler;
      } else {
         return super.getCapability(capability, facing);
      }
   }

   public boolean hasCapability(Capability capability, EnumFacing facing) {
      return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
   }
}
