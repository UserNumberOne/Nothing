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

   public EntityItemFrame(World var1) {
      super(var1);
   }

   public EntityItemFrame(World var1, BlockPos var2, EnumFacing var3) {
      super(var1, var2);
      this.updateFacingWithBoundingBox(var3);
   }

   protected void entityInit() {
      this.getDataManager().register(ITEM, Optional.absent());
      this.getDataManager().register(ROTATION, Integer.valueOf(0));
   }

   public float getCollisionBorderSize() {
      return 0.0F;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else if (!var1.isExplosion() && this.getDisplayedItem() != null) {
         if (!this.world.isRemote) {
            if (CraftEventFactory.handleNonLivingEntityDamageEvent(this, var1, (double)var2, false) || this.isDead) {
               return true;
            }

            this.dropItemOrSelf(var1.getEntity(), false);
            this.playSound(SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, 1.0F, 1.0F);
            this.setDisplayedItem((ItemStack)null);
         }

         return true;
      } else {
         return super.attackEntityFrom(var1, var2);
      }
   }

   public int getWidthPixels() {
      return 12;
   }

   public int getHeightPixels() {
      return 12;
   }

   public void onBroken(@Nullable Entity var1) {
      this.playSound(SoundEvents.ENTITY_ITEMFRAME_BREAK, 1.0F, 1.0F);
      this.dropItemOrSelf(var1, true);
   }

   public void playPlaceSound() {
      this.playSound(SoundEvents.ENTITY_ITEMFRAME_PLACE, 1.0F, 1.0F);
   }

   public void dropItemOrSelf(@Nullable Entity var1, boolean var2) {
      if (this.world.getGameRules().getBoolean("doEntityDrops")) {
         ItemStack var3 = this.getDisplayedItem();
         if (var1 instanceof EntityPlayer) {
            EntityPlayer var4 = (EntityPlayer)var1;
            if (var4.capabilities.isCreativeMode) {
               this.removeFrameFromMap(var3);
               return;
            }
         }

         if (var2) {
            this.entityDropItem(new ItemStack(Items.ITEM_FRAME), 0.0F);
         }

         if (var3 != null && this.rand.nextFloat() < this.itemDropChance) {
            var3 = var3.copy();
            this.removeFrameFromMap(var3);
            this.entityDropItem(var3, 0.0F);
         }
      }

   }

   private void removeFrameFromMap(ItemStack var1) {
      if (var1 != null) {
         if (var1.getItem() == Items.FILLED_MAP) {
            MapData var2 = ((ItemMap)var1.getItem()).getMapData(var1, this.world);
            var2.mapDecorations.remove("frame-" + this.getEntityId());
         }

         var1.setItemFrame((EntityItemFrame)null);
      }

   }

   @Nullable
   public ItemStack getDisplayedItem() {
      return (ItemStack)((Optional)this.getDataManager().get(ITEM)).orNull();
   }

   public void setDisplayedItem(@Nullable ItemStack var1) {
      this.setDisplayedItemWithUpdate(var1, true);
   }

   private void setDisplayedItemWithUpdate(@Nullable ItemStack var1, boolean var2) {
      if (var1 != null) {
         var1 = var1.copy();
         var1.stackSize = 1;
         var1.setItemFrame(this);
      }

      this.getDataManager().set(ITEM, Optional.fromNullable(var1));
      this.getDataManager().setDirty(ITEM);
      if (var1 != null) {
         this.playSound(SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, 1.0F, 1.0F);
      }

      if (var2 && this.hangingPosition != null) {
         this.world.updateComparatorOutputLevel(this.hangingPosition, Blocks.AIR);
      }

   }

   public void notifyDataManagerChange(DataParameter var1) {
      if (var1.equals(ITEM)) {
         ItemStack var2 = this.getDisplayedItem();
         if (var2 != null && var2.getItemFrame() != this) {
            var2.setItemFrame(this);
         }
      }

   }

   public int getRotation() {
      return ((Integer)this.getDataManager().get(ROTATION)).intValue();
   }

   public void setItemRotation(int var1) {
      this.setRotation(var1, true);
   }

   private void setRotation(int var1, boolean var2) {
      this.getDataManager().set(ROTATION, Integer.valueOf(var1 % 8));
      if (var2 && this.hangingPosition != null) {
         this.world.updateComparatorOutputLevel(this.hangingPosition, Blocks.AIR);
      }

   }

   public static void registerFixesItemFrame(DataFixer var0) {
      var0.registerWalker(FixTypes.ENTITY, new ItemStackData("ItemFrame", new String[]{"Item"}));
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      if (this.getDisplayedItem() != null) {
         var1.setTag("Item", this.getDisplayedItem().writeToNBT(new NBTTagCompound()));
         var1.setByte("ItemRotation", (byte)this.getRotation());
         var1.setFloat("ItemDropChance", this.itemDropChance);
      }

      super.writeEntityToNBT(var1);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      NBTTagCompound var2 = var1.getCompoundTag("Item");
      if (var2 != null && !var2.hasNoTags()) {
         this.setDisplayedItemWithUpdate(ItemStack.loadItemStackFromNBT(var2), false);
         this.setRotation(var1.getByte("ItemRotation"), false);
         if (var1.hasKey("ItemDropChance", 99)) {
            this.itemDropChance = var1.getFloat("ItemDropChance");
         }
      }

      super.readEntityFromNBT(var1);
   }

   public boolean processInitialInteract(EntityPlayer var1, @Nullable ItemStack var2, EnumHand var3) {
      if (this.getDisplayedItem() == null) {
         if (var2 != null && !this.world.isRemote) {
            this.setDisplayedItem(var2);
            if (!var1.capabilities.isCreativeMode) {
               --var2.stackSize;
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
