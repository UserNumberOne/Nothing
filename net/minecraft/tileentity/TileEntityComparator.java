package net.minecraft.tileentity;

import net.minecraft.nbt.NBTTagCompound;

public class TileEntityComparator extends TileEntity {
   private int outputSignal;

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(compound);
      compound.setInteger("OutputSignal", this.outputSignal);
      return compound;
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(compound);
      this.outputSignal = compound.getInteger("OutputSignal");
   }

   public int getOutputSignal() {
      return this.outputSignal;
   }

   public void setOutputSignal(int var1) {
      this.outputSignal = outputSignalIn;
   }
}
