package net.minecraft.tileentity;

import java.util.Random;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerEnchantment;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IInteractionObject;

public class TileEntityEnchantmentTable extends TileEntity implements ITickable, IInteractionObject {
   public int tickCount;
   public float pageFlip;
   public float pageFlipPrev;
   public float flipT;
   public float flipA;
   public float bookSpread;
   public float bookSpreadPrev;
   public float bookRotation;
   public float bookRotationPrev;
   public float tRot;
   private static final Random rand = new Random();
   private String customName;

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);
      if (this.hasCustomName()) {
         var1.setString("CustomName", this.customName);
      }

      return var1;
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      if (var1.hasKey("CustomName", 8)) {
         this.customName = var1.getString("CustomName");
      }

   }

   public void update() {
      this.bookSpreadPrev = this.bookSpread;
      this.bookRotationPrev = this.bookRotation;
      EntityPlayer var1 = this.world.getClosestPlayer((double)((float)this.pos.getX() + 0.5F), (double)((float)this.pos.getY() + 0.5F), (double)((float)this.pos.getZ() + 0.5F), 3.0D, false);
      if (var1 != null) {
         double var2 = var1.posX - (double)((float)this.pos.getX() + 0.5F);
         double var4 = var1.posZ - (double)((float)this.pos.getZ() + 0.5F);
         this.tRot = (float)MathHelper.atan2(var4, var2);
         this.bookSpread += 0.1F;
         if (this.bookSpread < 0.5F || rand.nextInt(40) == 0) {
            float var6 = this.flipT;

            while(true) {
               this.flipT += (float)(rand.nextInt(4) - rand.nextInt(4));
               if (var6 != this.flipT) {
                  break;
               }
            }
         }
      } else {
         this.tRot += 0.02F;
         this.bookSpread -= 0.1F;
      }

      while(this.bookRotation >= 3.1415927F) {
         this.bookRotation -= 6.2831855F;
      }

      while(this.bookRotation < -3.1415927F) {
         this.bookRotation += 6.2831855F;
      }

      while(this.tRot >= 3.1415927F) {
         this.tRot -= 6.2831855F;
      }

      while(this.tRot < -3.1415927F) {
         this.tRot += 6.2831855F;
      }

      float var7;
      for(var7 = this.tRot - this.bookRotation; var7 >= 3.1415927F; var7 -= 6.2831855F) {
         ;
      }

      while(var7 < -3.1415927F) {
         var7 += 6.2831855F;
      }

      this.bookRotation += var7 * 0.4F;
      this.bookSpread = MathHelper.clamp(this.bookSpread, 0.0F, 1.0F);
      ++this.tickCount;
      this.pageFlipPrev = this.pageFlip;
      float var3 = (this.flipT - this.pageFlip) * 0.4F;
      float var9 = 0.2F;
      var3 = MathHelper.clamp(var3, -0.2F, 0.2F);
      this.flipA += (var3 - this.flipA) * 0.9F;
      this.pageFlip += this.flipA;
   }

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.enchant";
   }

   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   public void setCustomName(String var1) {
      this.customName = var1;
   }

   public ITextComponent getDisplayName() {
      return (ITextComponent)(this.hasCustomName() ? new TextComponentString(this.getName()) : new TextComponentTranslation(this.getName(), new Object[0]));
   }

   public Container createContainer(InventoryPlayer var1, EntityPlayer var2) {
      return new ContainerEnchantment(var1, this.world, this.pos);
   }

   public String getGuiID() {
      return "minecraft:enchanting_table";
   }
}
