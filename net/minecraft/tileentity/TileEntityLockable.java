package net.minecraft.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;
import org.bukkit.Location;

public abstract class TileEntityLockable extends TileEntity implements IInteractionObject, ILockableContainer {
   private LockCode code = LockCode.EMPTY_CODE;

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      this.code = LockCode.fromNBT(var1);
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);
      if (this.code != null) {
         this.code.toNBT(var1);
      }

      return var1;
   }

   public boolean isLocked() {
      return this.code != null && !this.code.isEmpty();
   }

   public LockCode getLockCode() {
      return this.code;
   }

   public void setLockCode(LockCode var1) {
      this.code = var1;
   }

   public ITextComponent getDisplayName() {
      return (ITextComponent)(this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]));
   }

   public Location getLocation() {
      return new Location(this.world.getWorld(), (double)this.pos.getX(), (double)this.pos.getY(), (double)this.pos.getZ());
   }
}
