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

   public EntityItem(World var1, double var2, double var4, double var6) {
      super(var1);
      this.lastTick = MinecraftServer.currentTick;
      this.health = 5;
      this.hoverStart = (float)(Math.random() * 3.141592653589793D * 2.0D);
      this.setSize(0.25F, 0.25F);
      this.setPosition(var2, var4, var6);
      this.rotationYaw = (float)(Math.random() * 360.0D);
      this.motionX = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D));
      this.motionY = 0.20000000298023224D;
      this.motionZ = (double)((float)(Math.random() * 0.20000000298023224D - 0.10000000149011612D));
   }

   public EntityItem(World var1, double var2, double var4, double var6, ItemStack var8) {
      this(var1, var2, var4, var6);
      if (var8 != null && var8.getItem() != null) {
         this.setEntityItemStack(var8);
      }
   }

   protected boolean canTriggerWalking() {
      return false;
   }

   public EntityItem(World var1) {
      super(var1);
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
         int var1 = MinecraftServer.currentTick - this.lastTick;
         if (this.delayBeforeCanPickup != 32767) {
            this.delayBeforeCanPickup -= var1;
         }

         if (this.age != -32768) {
            this.age += var1;
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
         boolean var2 = (int)this.prevPosX != (int)this.posX || (int)this.prevPosY != (int)this.posY || (int)this.prevPosZ != (int)this.posZ;
         if (var2 || this.ticksExisted % 25 == 0) {
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

         float var3 = 0.98F;
         if (this.onGround) {
            var3 = this.world.getBlockState(new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ))).getBlock().slipperiness * 0.98F;
         }

         this.motionX *= (double)var3;
         this.motionY *= 0.9800000190734863D;
         this.motionZ *= (double)var3;
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
      for(EntityItem var2 : this.world.getEntitiesWithinAABB(EntityItem.class, this.getEntityBoundingBox().expand(0.5D, 0.0D, 0.5D))) {
         this.combineItems(var2);
      }

   }

   private boolean combineItems(EntityItem var1) {
      if (var1 == this) {
         return false;
      } else if (var1.isEntityAlive() && this.isEntityAlive()) {
         ItemStack var2 = this.getEntityItem();
         ItemStack var3 = var1.getEntityItem();
         if (this.delayBeforeCanPickup != 32767 && var1.delayBeforeCanPickup != 32767) {
            if (this.age != -32768 && var1.age != -32768) {
               if (var3.getItem() != var2.getItem()) {
                  return false;
               } else if (var3.hasTagCompound() ^ var2.hasTagCompound()) {
                  return false;
               } else if (var3.hasTagCompound() && !var3.getTagCompound().equals(var2.getTagCompound())) {
                  return false;
               } else if (var3.getItem() == null) {
                  return false;
               } else if (var3.getItem().getHasSubtypes() && var3.getMetadata() != var2.getMetadata()) {
                  return false;
               } else if (var3.stackSize < var2.stackSize) {
                  return var1.combineItems(this);
               } else if (var3.stackSize + var2.stackSize > var3.getMaxStackSize()) {
                  return false;
               } else if (CraftEventFactory.callItemMergeEvent(this, var1).isCancelled()) {
                  return false;
               } else {
                  var3.stackSize += var2.stackSize;
                  var1.delayBeforeCanPickup = Math.max(var1.delayBeforeCanPickup, this.delayBeforeCanPickup);
                  var1.age = Math.min(var1.age, this.age);
                  var1.setEntityItemStack(var3);
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

   protected void dealFireDamage(int var1) {
      this.attackEntityFrom(DamageSource.inFire, (float)var1);
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (this.isEntityInvulnerable(var1)) {
         return false;
      } else if (this.getEntityItem() != null && this.getEntityItem().getItem() == Items.NETHER_STAR && var1.isExplosion()) {
         return false;
      } else if (CraftEventFactory.handleNonLivingEntityDamageEvent(this, var1, (double)var2)) {
         return false;
      } else {
         this.setBeenAttacked();
         this.health = (int)((float)this.health - var2);
         if (this.health <= 0) {
            this.setDead();
         }

         return false;
      }
   }

   public static void registerFixesItem(DataFixer var0) {
      var0.registerWalker(FixTypes.ENTITY, new ItemStackData("Item", new String[]{"Item"}));
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      var1.setShort("Health", (short)this.health);
      var1.setShort("Age", (short)this.age);
      var1.setShort("PickupDelay", (short)this.delayBeforeCanPickup);
      if (this.getThrower() != null) {
         var1.setString("Thrower", this.thrower);
      }

      if (this.getOwner() != null) {
         var1.setString("Owner", this.owner);
      }

      if (this.getEntityItem() != null) {
         var1.setTag("Item", this.getEntityItem().writeToNBT(new NBTTagCompound()));
      }

   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      this.health = var1.getShort("Health");
      this.age = var1.getShort("Age");
      if (var1.hasKey("PickupDelay")) {
         this.delayBeforeCanPickup = var1.getShort("PickupDelay");
      }

      if (var1.hasKey("Owner")) {
         this.owner = var1.getString("Owner");
      }

      if (var1.hasKey("Thrower")) {
         this.thrower = var1.getString("Thrower");
      }

      NBTTagCompound var2 = var1.getCompoundTag("Item");
      if (var2 != null) {
         ItemStack var3 = ItemStack.loadItemStackFromNBT(var2);
         if (var3 != null) {
            this.setEntityItemStack(var3);
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

   public void onCollideWithPlayer(EntityPlayer var1) {
      if (!this.world.isRemote) {
         ItemStack var2 = this.getEntityItem();
         int var3 = var2.stackSize;
         int var4 = var1.inventory.canHold(var2);
         int var5 = var2.stackSize - var4;
         if (this.delayBeforeCanPickup <= 0 && var4 > 0) {
            var2.stackSize = var4;
            PlayerPickupItemEvent var6 = new PlayerPickupItemEvent((Player)var1.getBukkitEntity(), (Item)this.getBukkitEntity(), var5);
            this.world.getServer().getPluginManager().callEvent(var6);
            var2.stackSize = var4 + var5;
            if (var6.isCancelled()) {
               return;
            }

            this.delayBeforeCanPickup = 0;
         }

         if (this.delayBeforeCanPickup == 0 && (this.owner == null || 6000 - this.age <= 200 || this.owner.equals(var1.getName())) && var1.inventory.addItemStackToInventory(var2)) {
            if (var2.getItem() == net.minecraft.item.Item.getItemFromBlock(Blocks.LOG)) {
               var1.addStat(AchievementList.MINE_WOOD);
            }

            if (var2.getItem() == net.minecraft.item.Item.getItemFromBlock(Blocks.LOG2)) {
               var1.addStat(AchievementList.MINE_WOOD);
            }

            if (var2.getItem() == Items.LEATHER) {
               var1.addStat(AchievementList.KILL_COW);
            }

            if (var2.getItem() == Items.DIAMOND) {
               var1.addStat(AchievementList.DIAMONDS);
            }

            if (var2.getItem() == Items.BLAZE_ROD) {
               var1.addStat(AchievementList.BLAZE_ROD);
            }

            if (var2.getItem() == Items.DIAMOND && this.getThrower() != null) {
               EntityPlayer var7 = this.world.getPlayerEntityByName(this.getThrower());
               if (var7 != null && var7 != var1) {
                  var7.addStat(AchievementList.DIAMONDS_TO_YOU);
               }
            }

            if (!this.isSilent()) {
               this.world.playSound((EntityPlayer)null, var1.posX, var1.posY, var1.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2F, ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            }

            var1.onItemPickup(this, var3);
            if (var2.stackSize <= 0) {
               this.setDead();
            }

            var1.addStat(StatList.getObjectsPickedUpStats(var2.getItem()), var3);
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
   public Entity changeDimension(int var1) {
      Entity var2 = super.changeDimension(var1);
      if (!this.world.isRemote && var2 instanceof EntityItem) {
         ((EntityItem)var2).searchForOtherItemsNearby();
      }

      return var2;
   }

   public ItemStack getEntityItem() {
      ItemStack var1 = (ItemStack)((Optional)this.getDataManager().get(ITEM)).orNull();
      if (var1 == null) {
         if (this.world != null) {
            LOGGER.error("Item entity {} has no item?!", new Object[]{this.getEntityId()});
         }

         return new ItemStack(Blocks.STONE);
      } else {
         return var1;
      }
   }

   public void setEntityItemStack(@Nullable ItemStack var1) {
      this.getDataManager().set(ITEM, Optional.fromNullable(var1));
      this.getDataManager().setDirty(ITEM);
   }

   public String getOwner() {
      return this.owner;
   }

   public void setOwner(String var1) {
      this.owner = var1;
   }

   public String getThrower() {
      return this.thrower;
   }

   public void setThrower(String var1) {
      this.thrower = var1;
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

   public void setPickupDelay(int var1) {
      this.delayBeforeCanPickup = var1;
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
