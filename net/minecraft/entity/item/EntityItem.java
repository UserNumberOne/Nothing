package net.minecraft.entity.item;

import com.google.common.base.Optional;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.src.MinecraftServer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class EntityItem extends Entity {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final DataParameter ITEM = EntityDataManager.createKey(EntityItem.class, DataSerializers.OPTIONAL_ITEM_STACK);
   private int age;
   public int delayBeforeCanPickup;
   private int health;
   private String thrower;
   private String owner;
   public float hoverStart;
   private int lastTick;

   public EntityItem(World world, double d0, double d1, double d2) {
      super(world);
      this.lastTick = MinecraftServer.currentTick;
      this.health = 5;
      this.hoverStart = (float)(Math.random() * 3.141592653589793D * 2.0D);
      this.setSize(0.25F, 0.25F);
      this.setPosition(d0, d1, d2);
      this.rotationYaw = (float)(Math.random() * 360.0D);
      this.motionX = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D));
      this.motionY = 0.20000000298023224D;
      this.motionZ = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D));
   }

   public EntityItem(World world, double d0, double d1, double d2, ItemStack itemstack) {
      this(world, d0, d1, d2);
      if (itemstack != null && itemstack.getItem() != null) {
         this.setEntityItemStack(itemstack);
      }
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   public EntityItem(World world) {
      super(world);
      this.lastTick = MinecraftServer.currentTick;
      this.health = 5;
      this.hoverStart = (float)(Math.random() * 3.141592653589793D * 2.0D);
      this.setSize(0.25F, 0.25F);
      this.setEntityItemStack(new ItemStack(Blocks.AIR, 0));
   }

   protected void entityInit() {
      this.getDataManager().register(ITEM, Optional.absent());
   }

   public void onUpdate() {
      if (this.getEntityItem() == null) {
         this.setDead();
      } else {
         super.onUpdate();
         int elapsedTicks = MinecraftServer.currentTick - this.lastTick;
         if (this.delayBeforeCanPickup != 32767) {
            this.delayBeforeCanPickup -= elapsedTicks;
         }

         if (this.age != -32768) {
            this.age += elapsedTicks;
         }

         this.lastTick = MinecraftServer.currentTick;
         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         if (!this.hasNoGravity()) {
            this.motionY -= 0.03999999910593033D;
         }

         this.noClip = this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
         this.move(this.motionX, this.motionY, this.motionZ);
         boolean flag = (int)this.prevPosX != (int)this.posX || (int)this.prevPosY != (int)this.posY || (int)this.prevPosZ != (int)this.posZ;
         if (flag || this.ticksExisted % 25 == 0) {
            if (this.world.getBlockState(new BlockPos(this)).getMaterial() == Material.LAVA) {
               this.motionY = 0.20000000298023224D;
               this.motionX = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
               this.motionZ = (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
               this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
            }

            if (!this.world.isRemote) {
               this.searchForOtherItemsNearby();
            }
         }

         float f = 0.98F;
         if (this.onGround) {
            f = this.world.getBlockState(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ))).getBlock().slipperiness * 0.98F;
         }

         this.motionX *= (double)f;
         this.motionY *= 0.9800000190734863D;
         this.motionZ *= (double)f;
         if (this.onGround) {
            this.motionY *= -0.5D;
         }

         this.handleWaterMovement();
         if (!this.world.isRemote && this.age >= 6000) {
            if (CraftEventFactory.callItemDespawnEvent(this).isCancelled()) {
               this.age = 0;
               return;
            }

            this.setDead();
         }
      }

   }

   private void searchForOtherItemsNearby() {
      for(EntityItem entityitem : this.world.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().expand(0.5D, 0.0D, 0.5D))) {
         this.combineItems(entityitem);
      }

   }

   private boolean combineItems(EntityItem entityitem) {
      if (entityitem == this) {
         return false;
      } else if (entityitem.isEntityAlive() && this.isEntityAlive()) {
         ItemStack itemstack = this.getEntityItem();
         ItemStack itemstack1 = entityitem.getEntityItem();
         if (this.delayBeforeCanPickup != 32767 && entityitem.delayBeforeCanPickup != 32767) {
            if (this.age != -32768 && entityitem.age != -32768) {
               if (itemstack1.getItem() != itemstack.getItem()) {
                  return false;
               } else if (itemstack1.hasTagCompound() ^ itemstack.hasTagCompound()) {
                  return false;
               } else if (itemstack1.hasTagCompound() && !itemstack1.getTagCompound().equals(itemstack.getTagCompound())) {
                  return false;
               } else if (itemstack1.getItem() == null) {
                  return false;
               } else if (itemstack1.getItem().getHasSubtypes() && itemstack1.getMetadata() != itemstack.getMetadata()) {
                  return false;
               } else if (itemstack1.stackSize < itemstack.stackSize) {
                  return entityitem.combineItems(this);
               } else if (itemstack1.stackSize + itemstack.stackSize > itemstack1.getMaxStackSize()) {
                  return false;
               } else if (CraftEventFactory.callItemMergeEvent(this, entityitem).isCancelled()) {
                  return false;
               } else {
                  itemstack1.stackSize += itemstack.stackSize;
                  entityitem.delayBeforeCanPickup = Math.max(entityitem.delayBeforeCanPickup, this.delayBeforeCanPickup);
                  entityitem.age = Math.min(entityitem.age, this.age);
                  entityitem.setEntityItemStack(itemstack1);
                  this.setDead();
                  return true;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public void setAgeToCreativeDespawnTime() {
      this.age = 4800;
   }

   public boolean handleWaterMovement() {
      if (this.world.handleMaterialAcceleration(this.getEntityBoundingBox(), Material.WATER, this)) {
         if (!this.inWater && !this.firstUpdate) {
            this.resetHeight();
         }

         this.inWater = true;
      } else {
         this.inWater = false;
      }

      return this.inWater;
   }

   protected void dealFireDamage(int i) {
      this.attackEntityFrom(DamageSource.inFire, (float)i);
   }

   public boolean attackEntityFrom(DamageSource damagesource, float f) {
      if (this.isEntityInvulnerable(damagesource)) {
         return false;
      } else if (this.getEntityItem() != null && this.getEntityItem().getItem() == Items.NETHER_STAR && damagesource.isExplosion()) {
         return false;
      } else if (CraftEventFactory.handleNonLivingEntityDamageEvent(this, damagesource, (double)f)) {
         return false;
      } else {
         this.setBeenAttacked();
         this.health = (int)((float)this.health - f);
         if (this.health <= 0) {
            this.setDead();
         }

         return false;
      }
   }

   public static void registerFixesItem(DataFixer dataconvertermanager) {
      dataconvertermanager.registerWalker(FixTypes.ENTITY, new ItemStackData("Item", new String[]{"Item"}));
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setShort("Health", (short)this.health);
      nbttagcompound.setShort("Age", (short)this.age);
      nbttagcompound.setShort("PickupDelay", (short)this.delayBeforeCanPickup);
      if (this.getThrower() != null) {
         nbttagcompound.setString("Thrower", this.thrower);
      }

      if (this.getOwner() != null) {
         nbttagcompound.setString("Owner", this.owner);
      }

      if (this.getEntityItem() != null) {
         nbttagcompound.setTag("Item", this.getEntityItem().writeToNBT(new NBTTagCompound()));
      }

   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      this.health = nbttagcompound.getShort("Health");
      this.age = nbttagcompound.getShort("Age");
      if (nbttagcompound.hasKey("PickupDelay")) {
         this.delayBeforeCanPickup = nbttagcompound.getShort("PickupDelay");
      }

      if (nbttagcompound.hasKey("Owner")) {
         this.owner = nbttagcompound.getString("Owner");
      }

      if (nbttagcompound.hasKey("Thrower")) {
         this.thrower = nbttagcompound.getString("Thrower");
      }

      NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Item");
      if (nbttagcompound1 != null) {
         ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttagcompound1);
         if (itemstack != null) {
            this.setEntityItemStack(itemstack);
         } else {
            this.setDead();
         }
      } else {
         this.setDead();
      }

      if (this.getEntityItem() == null) {
         this.setDead();
      }

   }

   public void onCollideWithPlayer(EntityPlayer entityhuman) {
      if (!this.world.isRemote) {
         ItemStack itemstack = this.getEntityItem();
         int i = itemstack.stackSize;
         int canHold = entityhuman.inventory.canHold(itemstack);
         int remaining = itemstack.stackSize - canHold;
         if (this.delayBeforeCanPickup <= 0 && canHold > 0) {
            itemstack.stackSize = canHold;
            PlayerPickupItemEvent event = new PlayerPickupItemEvent((Player)entityhuman.getBukkitEntity(), (Item)this.getBukkitEntity(), remaining);
            this.world.getServer().getPluginManager().callEvent(event);
            itemstack.stackSize = canHold + remaining;
            if (event.isCancelled()) {
               return;
            }

            this.delayBeforeCanPickup = 0;
         }

         if (this.delayBeforeCanPickup == 0 && (this.owner == null || 6000 - this.age <= 200 || this.owner.equals(entityhuman.getName())) && entityhuman.inventory.addItemStackToInventory(itemstack)) {
            if (itemstack.getItem() == net.minecraft.item.Item.getItemFromBlock(Blocks.LOG)) {
               entityhuman.addStat(AchievementList.MINE_WOOD);
            }

            if (itemstack.getItem() == net.minecraft.item.Item.getItemFromBlock(Blocks.LOG2)) {
               entityhuman.addStat(AchievementList.MINE_WOOD);
            }

            if (itemstack.getItem() == Items.LEATHER) {
               entityhuman.addStat(AchievementList.KILL_COW);
            }

            if (itemstack.getItem() == Items.DIAMOND) {
               entityhuman.addStat(AchievementList.DIAMONDS);
            }

            if (itemstack.getItem() == Items.BLAZE_ROD) {
               entityhuman.addStat(AchievementList.BLAZE_ROD);
            }

            if (itemstack.getItem() == Items.DIAMOND && this.getThrower() != null) {
               EntityPlayer entityhuman1 = this.world.getPlayerEntityByName(this.getThrower());
               if (entityhuman1 != null && entityhuman1 != entityhuman) {
                  entityhuman1.addStat(AchievementList.DIAMONDS_TO_YOU);
               }
            }

            if (!this.isSilent()) {
               this.world.playSound((EntityPlayer)null, entityhuman.posX, entityhuman.posY, entityhuman.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }

            entityhuman.onItemPickup(this, i);
            if (itemstack.stackSize <= 0) {
               this.setDead();
            }

            entityhuman.addStat(StatList.getObjectsPickedUpStats(itemstack.getItem()), i);
         }
      }

   }

   public String getName() {
      return this.hasCustomName() ? this.getCustomNameTag() : I18n.translateToLocal("item." + this.getEntityItem().getUnlocalizedName());
   }

   public boolean canBeAttackedWithItem() {
      return false;
   }

   @Nullable
   public Entity changeDimension(int i) {
      Entity entity = super.changeDimension(i);
      if (!this.world.isRemote && entity instanceof EntityItem) {
         ((EntityItem)entity).searchForOtherItemsNearby();
      }

      return entity;
   }

   public ItemStack getEntityItem() {
      ItemStack itemstack = (ItemStack)((Optional)this.getDataManager().get(ITEM)).orNull();
      if (itemstack == null) {
         if (this.world != null) {
            LOGGER.error("Item entity {} has no item?!", new Object[]{this.getEntityId()});
         }

         return new ItemStack(Blocks.STONE);
      } else {
         return itemstack;
      }
   }

   public void setEntityItemStack(@Nullable ItemStack itemstack) {
      this.getDataManager().set(ITEM, Optional.fromNullable(itemstack));
      this.getDataManager().setDirty(ITEM);
   }

   public String getOwner() {
      return this.owner;
   }

   public void setOwner(String s) {
      this.owner = s;
   }

   public String getThrower() {
      return this.thrower;
   }

   public void setThrower(String s) {
      this.thrower = s;
   }

   public void setDefaultPickupDelay() {
      this.delayBeforeCanPickup = 10;
   }

   public void setNoPickupDelay() {
      this.delayBeforeCanPickup = 0;
   }

   public void setInfinitePickupDelay() {
      this.delayBeforeCanPickup = 32767;
   }

   public void setPickupDelay(int i) {
      this.delayBeforeCanPickup = i;
   }

   public boolean cannotPickup() {
      return this.delayBeforeCanPickup > 0;
   }

   public void setNoDespawn() {
      this.age = -6000;
   }

   public void makeFakeItem() {
      this.setInfinitePickupDelay();
      this.age = 5999;
   }
}
