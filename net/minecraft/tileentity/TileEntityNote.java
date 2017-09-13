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

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);
      var1.setByte("note", this.note);
      var1.setBoolean("powered", this.previousRedstoneState);
      return var1;
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      this.note = var1.getByte("note");
      this.note = (byte)MathHelper.clamp(this.note, 0, 24);
      this.previousRedstoneState = var1.getBoolean("powered");
   }

   public void changePitch() {
      this.note = (byte)((this.note + 1) % 25);
      this.markDirty();
   }

   public void triggerNote(World var1, BlockPos var2) {
      if (var1.getBlockState(var2.up()).getMaterial() == Material.AIR) {
         Material var3 = var1.getBlockState(var2.down()).getMaterial();
         byte var4 = 0;
         if (var3 == Material.ROCK) {
            var4 = 1;
         }

         if (var3 == Material.SAND) {
            var4 = 2;
         }

         if (var3 == Material.GLASS) {
            var4 = 3;
         }

         if (var3 == Material.WOOD) {
            var4 = 4;
         }

         NotePlayEvent var5 = CraftEventFactory.callNotePlayEvent(this.world, var2.getX(), var2.getY(), var2.getZ(), var4, this.note);
         if (!var5.isCancelled()) {
            var1.addBlockEvent(var2, Blocks.NOTEBLOCK, var5.getInstrument().getType(), var5.getNote().getId());
         }
      }

   }
}
