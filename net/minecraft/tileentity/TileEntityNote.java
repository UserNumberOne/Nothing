package net.minecraft.tileentity;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.block.NotePlayEvent;

public class TileEntityNote extends TileEntity {
   public byte note;
   public boolean previousRedstoneState;

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      nbttagcompound.setByte("note", this.note);
      nbttagcompound.setBoolean("powered", this.previousRedstoneState);
      return nbttagcompound;
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      this.note = nbttagcompound.getByte("note");
      this.note = (byte)MathHelper.clamp(this.note, 0, 24);
      this.previousRedstoneState = nbttagcompound.getBoolean("powered");
   }

   public void changePitch() {
      this.note = (byte)((this.note + 1) % 25);
      this.markDirty();
   }

   public void triggerNote(World world, BlockPos blockposition) {
      if (world.getBlockState(blockposition.up()).getMaterial() == Material.AIR) {
         Material material = world.getBlockState(blockposition.down()).getMaterial();
         byte b0 = 0;
         if (material == Material.ROCK) {
            b0 = 1;
         }

         if (material == Material.SAND) {
            b0 = 2;
         }

         if (material == Material.GLASS) {
            b0 = 3;
         }

         if (material == Material.WOOD) {
            b0 = 4;
         }

         NotePlayEvent event = CraftEventFactory.callNotePlayEvent(this.world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), b0, this.note);
         if (!event.isCancelled()) {
            world.addBlockEvent(blockposition, Blocks.NOTEBLOCK, event.getInstrument().getType(), event.getNote().getId());
         }
      }

   }
}
