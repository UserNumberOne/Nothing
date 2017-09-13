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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityItemFrame extends EntityHanging {
   private static final DataParameter ITEM = EntityDataManager.createKey(EntityItemFrame.class, DataSerializers.OPTIONAL_ITEM_STACK);
   private static final DataParameter ROTATION = EntityDataManager.createKey(EntityItemFrame.class, DataSerializers.VARINT);
   private float itemDropChance = 1.0F;

   public EntityItemFrame(World var1) {
      super(worldIn);
   }

   public EntityItemFrame(World var1, BlockPos var2, EnumFacing var3) {
      super(worldIn, p_i45852_2_);
      this.updateFacingWithBoundingBox(p_i45852_3_);
   }

   protected void entityInit() {
      this.getDataManager().register(ITEM, Optional.absent());
      this.getDataManager().register(ROTATION, Integer.valueOf(0));
   }

   public float getCollisionBorderSize() {
      return 0.0F;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(source)) {
         return false;
      } else if (!source.isExplosion() && this.getDisplayedItem() != null) {
         if (!this.world.isRemote) {
            this.dropItemOrSelf(source.getEntity(), false);
            this.playSound(SoundEvents.ENTITY_ITEMFRAME_REMOVE_ITEM, 1.0F, 1.0F);
            this.setDisplayedItem((ItemStack)null);
         }

         return true;
      } else {
         return super.attackEntityFrom(source, amount);
      }
   }

   public int getWidthPixels() {
      return 12;
   }

   public int getHeightPixels() {
      return 12;
   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRenderDist(double var1) {
      double d0 = 16.0D;
      d0 = d0 * 64.0D * getRenderDistanceWeight();
      return distance < d0 * d0;
   }

   public void onBroken(@Nullable Entity var1) {
      this.playSound(SoundEvents.ENTITY_ITEMFRAME_BREAK, 1.0F, 1.0F);
      this.dropItemOrSelf(brokenEntity, true);
   }

   public void playPlaceSound() {
      this.playSound(SoundEvents.ENTITY_ITEMFRAME_PLACE, 1.0F, 1.0F);
   }

   public void dropItemOrSelf(@Nullable Entity var1, boolean var2) {
      if (this.world.getGameRules().getBoolean("doEntityDrops")) {
         ItemStack itemstack = this.getDisplayedItem();
         if (entityIn instanceof EntityPlayer) {
            EntityPlayer entityplayer = (EntityPlayer)entityIn;
            if (entityplayer.capabilities.isCreativeMode) {
               this.removeFrameFromMap(itemstack);
               return;
            }
         }

         if (p_146065_2_) {
            this.entityDropItem(new ItemStack(Items.ITEM_FRAME), 0.0F);
         }

         if (itemstack != null && this.rand.nextFloat() < this.itemDropChance) {
            itemstack = itemstack.copy();
            this.removeFrameFromMap(itemstack);
            this.entityDropItem(itemstack, 0.0F);
         }
      }

   }

   private void removeFrameFromMap(ItemStack var1) {
      if (stack != null) {
         if (stack.getItem() instanceof ItemMap) {
            MapData mapdata = ((ItemMap)stack.getItem()).getMapData(stack, this.world);
            mapdata.mapDecorations.remove("frame-" + this.getEntityId());
         }

         stack.setItemFrame((EntityItemFrame)null);
      }

   }

   @Nullable
   public ItemStack getDisplayedItem() {
      return (ItemStack)((Optional)this.getDataManager().get(ITEM)).orNull();
   }

   public void setDisplayedItem(@Nullable ItemStack var1) {
      this.setDisplayedItemWithUpdate(stack, true);
   }

   private void setDisplayedItemWithUpdate(@Nullable ItemStack var1, boolean var2) {
      if (stack != null) {
         stack = stack.copy();
         stack.stackSize = 1;
         stack.setItemFrame(this);
      }

      this.getDataManager().set(ITEM, Optional.fromNullable(stack));
      this.getDataManager().setDirty(ITEM);
      if (stack != null) {
         this.playSound(SoundEvents.ENTITY_ITEMFRAME_ADD_ITEM, 1.0F, 1.0F);
      }

      if (p_174864_2_ && this.hangingPosition != null) {
         this.world.updateComparatorOutputLevel(this.hangingPosition, Blocks.AIR);
      }

   }

   public void notifyDataManagerChange(DataParameter var1) {
      if (key.equals(ITEM)) {
         ItemStack itemstack = this.getDisplayedItem();
         if (itemstack != null && itemstack.getItemFrame() != this) {
            itemstack.setItemFrame(this);
         }
      }

   }

   public int getRotation() {
      return ((Integer)this.getDataManager().get(ROTATION)).intValue();
   }

   public void setItemRotation(int var1) {
      this.setRotation(rotationIn, true);
   }

   private void setRotation(int var1, boolean var2) {
      this.getDataManager().set(ROTATION, Integer.valueOf(rotationIn % 8));
      if (p_174865_2_ && this.hangingPosition != null) {
         this.world.updateComparatorOutputLevel(this.hangingPosition, Blocks.AIR);
      }

   }

   public static void registerFixesItemFrame(DataFixer var0) {
      fixer.registerWalker(FixTypes.ENTITY, new ItemStackData("ItemFrame", new String[]{"Item"}));
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      if (this.getDisplayedItem() != null) {
         compound.setTag("Item", this.getDisplayedItem().writeToNBT(new NBTTagCompound()));
         compound.setByte("ItemRotation", (byte)this.getRotation());
         compound.setFloat("ItemDropChance", this.itemDropChance);
      }

      super.writeEntityToNBT(compound);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      NBTTagCompound nbttagcompound = compound.getCompoundTag("Item");
      if (nbttagcompound != null && !nbttagcompound.hasNoTags()) {
         this.setDisplayedItemWithUpdate(ItemStack.loadItemStackFromNBT(nbttagcompound), false);
         this.setRotation(compound.getByte("ItemRotation"), false);
         if (compound.hasKey("ItemDropChance", 99)) {
            this.itemDropChance = compound.getFloat("ItemDropChance");
         }
      }

      super.readEntityFromNBT(compound);
   }

   public boolean processInitialInteract(EntityPlayer var1, @Nullable ItemStack var2, EnumHand var3) {
      if (this.getDisplayedItem() == null) {
         if (stack != null && !this.world.isRemote) {
            this.setDisplayedItem(stack);
            if (!player.capabilities.isCreativeMode) {
               --stack.stackSize;
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
