package net.minecraft.entity.item;

import com.google.common.base.Optional;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapData;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class EntityItemFrame extends EntityHanging {
   private static final DataParameter ITEM = EntityDataManager.createKey(EntityItemFrame.class, DataSerializers.OPTIONAL_ITEM_STACK);
   private static final DataParameter ROTATION = EntityDataManager.createKey(EntityItemFrame.class, DataSerializers.VARINT);
   private float itemDropChance = 1.0F;

   public EntityItemFrame(World world) {
      super(world);
   }

   public EntityItemFrame(World world, BlockPos blockposition, EnumFacing enumdirection) {
      super(world, blockposition);
      this.updateFacingWithBoundingBox(enumdirection);
   }

   protected void entityInit() {
      this.getDataManager().register(ITEM, Optional.absent());
      this.getDataManager().register(ROTATION, Integer.valueOf(0));
   }

   public float getCollisionBorderSize() {
      return 0.0F;
   }

   public boolean attackEntityFrom(DamageSource damagesource, float f) {
      if (this.isEntityInvulnerable(damagesource)) {
         return false;
      } else if (!damagesource.isExplosion() && this.getDisplayedItem() != null) {
         if (!this.world.isRemote) {
            if (CraftEventFactory.handleNonLivingEntityDamageEvent(this, damagesource, (double)f, false) || this.isDead) {
               return true;
            }

            this.dropItemOrSelf(damagesource.getEntity(), false);
            this.playSound(SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, 1.0F, 1.0F);
            this.setDisplayedItem((ItemStack)null);
         }

         return true;
      } else {
         return super.attackEntityFrom(damagesource, f);
      }
   }

   public int getWidthPixels() {
      return 12;
   }

   public int getHeightPixels() {
      return 12;
   }

   public void onBroken(@Nullable Entity entity) {
      this.playSound(SoundEvents.ENTITY_ITEMFRAME_BREAK, 1.0F, 1.0F);
      this.dropItemOrSelf(entity, true);
   }

   public void playPlaceSound() {
      this.playSound(SoundEvents.ENTITY_ITEMFRAME_PLACE, 1.0F, 1.0F);
   }

   public void dropItemOrSelf(@Nullable Entity entity, boolean flag) {
      if (this.world.getGameRules().getBoolean("doEntityDrops")) {
         ItemStack itemstack = this.getDisplayedItem();
         if (entity instanceof EntityPlayer) {
            EntityPlayer entityhuman = (EntityPlayer)entity;
            if (entityhuman.capabilities.isCreativeMode) {
               this.removeFrameFromMap(itemstack);
               return;
            }
         }

         if (flag) {
            this.entityDropItem(new ItemStack(Items.ITEM_FRAME), 0.0F);
         }

         if (itemstack != null && this.rand.nextFloat() < this.itemDropChance) {
            itemstack = itemstack.copy();
            this.removeFrameFromMap(itemstack);
            this.entityDropItem(itemstack, 0.0F);
         }
      }

   }

   private void removeFrameFromMap(ItemStack itemstack) {
      if (itemstack != null) {
         if (itemstack.getItem() == Items.FILLED_MAP) {
            MapData worldmap = ((ItemMap)itemstack.getItem()).getMapData(itemstack, this.world);
            worldmap.mapDecorations.remove("frame-" + this.getEntityId());
         }

         itemstack.setItemFrame((EntityItemFrame)null);
      }

   }

   @Nullable
   public ItemStack getDisplayedItem() {
      return (ItemStack)((Optional)this.getDataManager().get(ITEM)).orNull();
   }

   public void setDisplayedItem(@Nullable ItemStack itemstack) {
      this.setDisplayedItemWithUpdate(itemstack, true);
   }

   private void setDisplayedItemWithUpdate(@Nullable ItemStack itemstack, boolean flag) {
      if (itemstack != null) {
         itemstack = itemstack.copy();
         itemstack.stackSize = 1;
         itemstack.setItemFrame(this);
      }

      this.getDataManager().set(ITEM, Optional.fromNullable(itemstack));
      this.getDataManager().setDirty(ITEM);
      if (itemstack != null) {
         this.playSound(SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, 1.0F, 1.0F);
      }

      if (flag && this.hangingPosition != null) {
         this.world.updateComparatorOutputLevel(this.hangingPosition, Blocks.AIR);
      }

   }

   public void notifyDataManagerChange(DataParameter datawatcherobject) {
      if (datawatcherobject.equals(ITEM)) {
         ItemStack itemstack = this.getDisplayedItem();
         if (itemstack != null && itemstack.getItemFrame() != this) {
            itemstack.setItemFrame(this);
         }
      }

   }

   public int getRotation() {
      return ((Integer)this.getDataManager().get(ROTATION)).intValue();
   }

   public void setItemRotation(int i) {
      this.setRotation(i, true);
   }

   private void setRotation(int i, boolean flag) {
      this.getDataManager().set(ROTATION, Integer.valueOf(i % 8));
      if (flag && this.hangingPosition != null) {
         this.world.updateComparatorOutputLevel(this.hangingPosition, Blocks.AIR);
      }

   }

   public static void registerFixesItemFrame(DataFixer dataconvertermanager) {
      dataconvertermanager.registerWalker(FixTypes.ENTITY, new ItemStackData("ItemFrame", new String[]{"Item"}));
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      if (this.getDisplayedItem() != null) {
         nbttagcompound.setTag("Item", this.getDisplayedItem().writeToNBT(new NBTTagCompound()));
         nbttagcompound.setByte("ItemRotation", (byte)this.getRotation());
         nbttagcompound.setFloat("ItemDropChance", this.itemDropChance);
      }

      super.writeEntityToNBT(nbttagcompound);
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Item");
      if (nbttagcompound1 != null && !nbttagcompound1.hasNoTags()) {
         this.setDisplayedItemWithUpdate(ItemStack.loadItemStackFromNBT(nbttagcompound1), false);
         this.setRotation(nbttagcompound.getByte("ItemRotation"), false);
         if (nbttagcompound.hasKey("ItemDropChance", 99)) {
            this.itemDropChance = nbttagcompound.getFloat("ItemDropChance");
         }
      }

      super.readEntityFromNBT(nbttagcompound);
   }

   public boolean processInitialInteract(EntityPlayer entityhuman, @Nullable ItemStack itemstack, EnumHand enumhand) {
      if (this.getDisplayedItem() == null) {
         if (itemstack != null && !this.world.isRemote) {
            this.setDisplayedItem(itemstack);
            if (!entityhuman.capabilities.isCreativeMode) {
               --itemstack.stackSize;
            }
         }
      } else if (!this.world.isRemote) {
         this.playSound(SoundEvents.ENTITY_ITEMFRAME_ROTATE_ITEM, 1.0F, 1.0F);
         this.setItemRotation(this.getRotation() + 1);
      }

      return true;
   }

   public int getAnalogOutput() {
      return this.getDisplayedItem() == null ? 0 : this.getRotation() % 8 + 1;
   }
}
