package net.minecraft.entity.ai;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

public class EntityAIVillagerInteract extends EntityAIWatchClosest2 {
   private int interactionDelay;
   private final EntityVillager villager;

   public EntityAIVillagerInteract(EntityVillager var1) {
      super(var1, EntityVillager.class, 3.0F, 0.02F);
      this.villager = var1;
   }

   public void startExecuting() {
      super.startExecuting();
      if (this.villager.canAbondonItems() && this.closestEntity instanceof EntityVillager && ((EntityVillager)this.closestEntity).wantsMoreFood()) {
         this.interactionDelay = 10;
      } else {
         this.interactionDelay = 0;
      }

   }

   public void updateTask() {
      super.updateTask();
      if (this.interactionDelay > 0) {
         --this.interactionDelay;
         if (this.interactionDelay == 0) {
            InventoryBasic var1 = this.villager.getVillagerInventory();

            for(int var2 = 0; var2 < var1.getSizeInventory(); ++var2) {
               ItemStack var3 = var1.getStackInSlot(var2);
               ItemStack var4 = null;
               if (var3 != null) {
                  Item var5 = var3.getItem();
                  if ((var5 == Items.BREAD || var5 == Items.POTATO || var5 == Items.CARROT || var5 == Items.BEETROOT) && var3.stackSize > 3) {
                     int var13 = var3.stackSize / 2;
                     var3.stackSize -= var13;
                     var4 = new ItemStack(var5, var13, var3.getMetadata());
                  } else if (var5 == Items.WHEAT && var3.stackSize > 5) {
                     int var6 = var3.stackSize / 2 / 3 * 3;
                     int var7 = var6 / 3;
                     var3.stackSize -= var6;
                     var4 = new ItemStack(Items.BREAD, var7, 0);
                  }

                  if (var3.stackSize <= 0) {
                     var1.setInventorySlotContents(var2, (ItemStack)null);
                  }
               }

               if (var4 != null) {
                  double var8 = this.villager.posY - 0.30000001192092896D + (double)this.villager.getEyeHeight();
                  EntityItem var14 = new EntityItem(this.villager.world, this.villager.posX, var8, this.villager.posZ, var4);
                  float var10 = 0.3F;
                  float var11 = this.villager.rotationYawHead;
                  float var12 = this.villager.rotationPitch;
                  var14.motionX = (double)(-MathHelper.sin(var11 * 0.017453292F) * MathHelper.cos(var12 * 0.017453292F) * 0.3F);
                  var14.motionZ = (double)(MathHelper.cos(var11 * 0.017453292F) * MathHelper.cos(var12 * 0.017453292F) * 0.3F);
                  var14.motionY = (double)(-MathHelper.sin(var12 * 0.017453292F) * 0.3F + 0.1F);
                  var14.setDefaultPickupDelay();
                  this.villager.world.spawnEntity(var14);
                  break;
               }
            }
         }
      }

   }
}
