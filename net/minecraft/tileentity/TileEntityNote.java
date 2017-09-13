package net.minecraft.tileentity;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class TileEntityNote extends TileEntity {
   public byte note;
   public boolean previousRedstoneState;

   public NBTTagCompound writeToNBT(NBTTagCompound compound) {
      super.writeToNBT(compound);
      compound.setByte("note", this.note);
      compound.setBoolean("powered", this.previousRedstoneState);
      return compound;
   }

   public void readFromNBT(NBTTagCompound compound) {
      super.readFromNBT(compound);
      this.note = compound.getByte("note");
      this.note = (byte)MathHelper.clamp(this.note, 0, 24);
      this.previousRedstoneState = compound.getBoolean("powered");
   }

   public void changePitch() {
      byte old = this.note;
      this.note = (byte)((this.note + 1) % 25);
      if (ForgeHooks.onNoteChange(this, old)) {
         this.markDirty();
      }
   }

   public void triggerNote(World worldIn, BlockPos posIn) {
      if (worldIn.getBlockState(posIn.up()).getMaterial() == Material.AIR) {
         Material material = worldIn.getBlockState(posIn.down()).getMaterial();
         int i = 0;
         if (material == Material.ROCK) {
            i = 1;
         }

         if (material == Material.SAND) {
            i = 2;
         }

         if (material == Material.GLASS) {
            i = 3;
         }

         if (material == Material.WOOD) {
            i = 4;
         }

         worldIn.addBlockEvent(posIn, Blocks.NOTEBLOCK, i, this.note);
      }

   }
}
