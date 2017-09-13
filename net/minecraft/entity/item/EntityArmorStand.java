package net.minecraft.entity.item;

import com.google.common.base.Predicate;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Rotations;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import org.bukkit.craftbukkit.v1_10_R1.CraftEquipmentSlot;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.inventory.EquipmentSlot;

public class EntityArmorStand extends EntityLivingBase {
   private static final Rotations DEFAULT_HEAD_ROTATION = new Rotations(0.0F, 0.0F, 0.0F);
   private static final Rotations DEFAULT_BODY_ROTATION = new Rotations(0.0F, 0.0F, 0.0F);
   private static final Rotations DEFAULT_LEFTARM_ROTATION = new Rotations(-10.0F, 0.0F, -10.0F);
   private static final Rotations DEFAULT_RIGHTARM_ROTATION = new Rotations(-15.0F, 0.0F, 10.0F);
   private static final Rotations DEFAULT_LEFTLEG_ROTATION = new Rotations(-1.0F, 0.0F, -1.0F);
   private static final Rotations DEFAULT_RIGHTLEG_ROTATION = new Rotations(1.0F, 0.0F, 1.0F);
   public static final DataParameter STATUS = EntityDataManager.createKey(EntityArmorStand.class, DataSerializers.BYTE);
   public static final DataParameter HEAD_ROTATION = EntityDataManager.createKey(EntityArmorStand.class, DataSerializers.ROTATIONS);
   public static final DataParameter BODY_ROTATION = EntityDataManager.createKey(EntityArmorStand.class, DataSerializers.ROTATIONS);
   public static final DataParameter LEFT_ARM_ROTATION = EntityDataManager.createKey(EntityArmorStand.class, DataSerializers.ROTATIONS);
   public static final DataParameter RIGHT_ARM_ROTATION = EntityDataManager.createKey(EntityArmorStand.class, DataSerializers.ROTATIONS);
   public static final DataParameter LEFT_LEG_ROTATION = EntityDataManager.createKey(EntityArmorStand.class, DataSerializers.ROTATIONS);
   public static final DataParameter RIGHT_LEG_ROTATION = EntityDataManager.createKey(EntityArmorStand.class, DataSerializers.ROTATIONS);
   private static final Predicate IS_RIDEABLE_MINECART = new Predicate() {
      public boolean apply(@Nullable Entity entity) {
         return entity instanceof EntityMinecart && ((EntityMinecart)entity).getType() == EntityMinecart.Type.RIDEABLE;
      }

      public boolean apply(Object object) {
         return this.apply((Entity)object);
      }
   };
   private final ItemStack[] handItems;
   private final ItemStack[] armorItems;
   private boolean canInteract;
   public long punchCooldown;
   private int disabledSlots;
   private boolean wasMarker;
   public Rotations headRotation;
   public Rotations bodyRotation;
   public Rotations leftArmRotation;
   public Rotations rightArmRotation;
   public Rotations leftLegRotation;
   public Rotations rightLegRotation;

   public EntityArmorStand(World world) {
      super(world);
      this.handItems = new ItemStack[2];
      this.armorItems = new ItemStack[4];
      this.headRotation = DEFAULT_HEAD_ROTATION;
      this.bodyRotation = DEFAULT_BODY_ROTATION;
      this.leftArmRotation = DEFAULT_LEFTARM_ROTATION;
      this.rightArmRotation = DEFAULT_RIGHTARM_ROTATION;
      this.leftLegRotation = DEFAULT_LEFTLEG_ROTATION;
      this.rightLegRotation = DEFAULT_RIGHTLEG_ROTATION;
      this.noClip = this.hasNoGravity();
      this.setSize(0.5F, 1.975F);
   }

   public EntityArmorStand(World world, double d0, double d1, double d2) {
      this(world);
      this.setPosition(d0, d1, d2);
   }

   public boolean isServerWorld() {
      return super.isServerWorld() && !this.hasNoGravity();
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(STATUS, Byte.valueOf((byte)0));
      this.dataManager.register(HEAD_ROTATION, DEFAULT_HEAD_ROTATION);
      this.dataManager.register(BODY_ROTATION, DEFAULT_BODY_ROTATION);
      this.dataManager.register(LEFT_ARM_ROTATION, DEFAULT_LEFTARM_ROTATION);
      this.dataManager.register(RIGHT_ARM_ROTATION, DEFAULT_RIGHTARM_ROTATION);
      this.dataManager.register(LEFT_LEG_ROTATION, DEFAULT_LEFTLEG_ROTATION);
      this.dataManager.register(RIGHT_LEG_ROTATION, DEFAULT_RIGHTLEG_ROTATION);
   }

   public Iterable getHeldEquipment() {
      return Arrays.asList(this.handItems);
   }

   public Iterable getArmorInventoryList() {
      return Arrays.asList(this.armorItems);
   }

   @Nullable
   public ItemStack getItemStackFromSlot(EntityEquipmentSlot enumitemslot) {
      ItemStack itemstack = null;
      switch(EntityArmorStand.SyntheticClass_1.a[enumitemslot.getSlotType().ordinal()]) {
      case 1:
         itemstack = this.handItems[enumitemslot.getIndex()];
         break;
      case 2:
         itemstack = this.armorItems[enumitemslot.getIndex()];
      }

      return itemstack;
   }

   public void setItemStackToSlot(EntityEquipmentSlot enumitemslot, @Nullable ItemStack itemstack) {
      switch(EntityArmorStand.SyntheticClass_1.a[enumitemslot.getSlotType().ordinal()]) {
      case 1:
         this.playEquipSound(itemstack);
         this.handItems[enumitemslot.getIndex()] = itemstack;
         break;
      case 2:
         this.playEquipSound(itemstack);
         this.armorItems[enumitemslot.getIndex()] = itemstack;
      }

   }

   public boolean replaceItemInInventory(int i, @Nullable ItemStack itemstack) {
      EntityEquipmentSlot enumitemslot;
      if (i == 98) {
         enumitemslot = EntityEquipmentSlot.MAINHAND;
      } else if (i == 99) {
         enumitemslot = EntityEquipmentSlot.OFFHAND;
      } else if (i == 100 + EntityEquipmentSlot.HEAD.getIndex()) {
         enumitemslot = EntityEquipmentSlot.HEAD;
      } else if (i == 100 + EntityEquipmentSlot.CHEST.getIndex()) {
         enumitemslot = EntityEquipmentSlot.CHEST;
      } else if (i == 100 + EntityEquipmentSlot.LEGS.getIndex()) {
         enumitemslot = EntityEquipmentSlot.LEGS;
      } else {
         if (i != 100 + EntityEquipmentSlot.FEET.getIndex()) {
            return false;
         }

         enumitemslot = EntityEquipmentSlot.FEET;
      }

      if (itemstack != null && !EntityLiving.isItemStackInSlot(enumitemslot, itemstack) && enumitemslot != EntityEquipmentSlot.HEAD) {
         return false;
      } else {
         this.setItemStackToSlot(enumitemslot, itemstack);
         return true;
      }
   }

   public static void registerFixesArmorStand(DataFixer dataconvertermanager) {
      dataconvertermanager.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("ArmorStand", new String[]{"ArmorItems", "HandItems"}));
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      NBTTagList nbttaglist = new NBTTagList();

      for(ItemStack itemstack : this.armorItems) {
         NBTTagCompound nbttagcompound1 = new NBTTagCompound();
         if (itemstack != null) {
            itemstack.writeToNBT(nbttagcompound1);
         }

         nbttaglist.appendTag(nbttagcompound1);
      }

      nbttagcompound.setTag("ArmorItems", nbttaglist);
      NBTTagList nbttaglist1 = new NBTTagList();

      for(ItemStack itemstack1 : this.handItems) {
         NBTTagCompound nbttagcompound2 = new NBTTagCompound();
         if (itemstack1 != null) {
            itemstack1.writeToNBT(nbttagcompound2);
         }

         nbttaglist1.appendTag(nbttagcompound2);
      }

      nbttagcompound.setTag("HandItems", nbttaglist1);
      if (this.getAlwaysRenderNameTag() && (this.getCustomNameTag() == null || this.getCustomNameTag().isEmpty())) {
         nbttagcompound.setBoolean("CustomNameVisible", this.getAlwaysRenderNameTag());
      }

      nbttagcompound.setBoolean("Invisible", this.isInvisible());
      nbttagcompound.setBoolean("Small", this.isSmall());
      nbttagcompound.setBoolean("ShowArms", this.getShowArms());
      nbttagcompound.setInteger("DisabledSlots", this.disabledSlots);
      nbttagcompound.setBoolean("NoBasePlate", this.hasNoBasePlate());
      if (this.hasMarker()) {
         nbttagcompound.setBoolean("Marker", this.hasMarker());
      }

      nbttagcompound.setTag("Pose", this.readPoseFromNBT());
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      if (nbttagcompound.hasKey("ArmorItems", 9)) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("ArmorItems", 10);

         for(int i = 0; i < this.armorItems.length; ++i) {
            this.armorItems[i] = ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i));
         }
      }

      if (nbttagcompound.hasKey("HandItems", 9)) {
         NBTTagList nbttaglist = nbttagcompound.getTagList("HandItems", 10);

         for(int i = 0; i < this.handItems.length; ++i) {
            this.handItems[i] = ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i));
         }
      }

      this.setInvisible(nbttagcompound.getBoolean("Invisible"));
      this.setSmall(nbttagcompound.getBoolean("Small"));
      this.setShowArms(nbttagcompound.getBoolean("ShowArms"));
      this.disabledSlots = nbttagcompound.getInteger("DisabledSlots");
      this.setNoBasePlate(nbttagcompound.getBoolean("NoBasePlate"));
      this.setMarker(nbttagcompound.getBoolean("Marker"));
      this.wasMarker = !this.hasMarker();
      this.noClip = this.hasNoGravity();
      NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Pose");
      this.writePoseToNBT(nbttagcompound1);
   }

   private void writePoseToNBT(NBTTagCompound nbttagcompound) {
      NBTTagList nbttaglist = nbttagcompound.getTagList("Head", 5);
      this.setHeadRotation(nbttaglist.hasNoTags() ? DEFAULT_HEAD_ROTATION : new Rotations(nbttaglist));
      NBTTagList nbttaglist1 = nbttagcompound.getTagList("Body", 5);
      this.setBodyRotation(nbttaglist1.hasNoTags() ? DEFAULT_BODY_ROTATION : new Rotations(nbttaglist1));
      NBTTagList nbttaglist2 = nbttagcompound.getTagList("LeftArm", 5);
      this.setLeftArmRotation(nbttaglist2.hasNoTags() ? DEFAULT_LEFTARM_ROTATION : new Rotations(nbttaglist2));
      NBTTagList nbttaglist3 = nbttagcompound.getTagList("RightArm", 5);
      this.setRightArmRotation(nbttaglist3.hasNoTags() ? DEFAULT_RIGHTARM_ROTATION : new Rotations(nbttaglist3));
      NBTTagList nbttaglist4 = nbttagcompound.getTagList("LeftLeg", 5);
      this.setLeftLegRotation(nbttaglist4.hasNoTags() ? DEFAULT_LEFTLEG_ROTATION : new Rotations(nbttaglist4));
      NBTTagList nbttaglist5 = nbttagcompound.getTagList("RightLeg", 5);
      this.setRightLegRotation(nbttaglist5.hasNoTags() ? DEFAULT_RIGHTLEG_ROTATION : new Rotations(nbttaglist5));
   }

   private NBTTagCompound readPoseFromNBT() {
      NBTTagCompound nbttagcompound = new NBTTagCompound();
      if (!DEFAULT_HEAD_ROTATION.equals(this.headRotation)) {
         nbttagcompound.setTag("Head", this.headRotation.writeToNBT());
      }

      if (!DEFAULT_BODY_ROTATION.equals(this.bodyRotation)) {
         nbttagcompound.setTag("Body", this.bodyRotation.writeToNBT());
      }

      if (!DEFAULT_LEFTARM_ROTATION.equals(this.leftArmRotation)) {
         nbttagcompound.setTag("LeftArm", this.leftArmRotation.writeToNBT());
      }

      if (!DEFAULT_RIGHTARM_ROTATION.equals(this.rightArmRotation)) {
         nbttagcompound.setTag("RightArm", this.rightArmRotation.writeToNBT());
      }

      if (!DEFAULT_LEFTLEG_ROTATION.equals(this.leftLegRotation)) {
         nbttagcompound.setTag("LeftLeg", this.leftLegRotation.writeToNBT());
      }

      if (!DEFAULT_RIGHTLEG_ROTATION.equals(this.rightLegRotation)) {
         nbttagcompound.setTag("RightLeg", this.rightLegRotation.writeToNBT());
      }

      return nbttagcompound;
   }

   public boolean canBePushed() {
      return false;
   }

   protected void collideWithEntity(Entity entity) {
   }

   protected void collideWithNearbyEntities() {
      List list = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox(), IS_RIDEABLE_MINECART);

      for(int i = 0; i < list.size(); ++i) {
         Entity entity = (Entity)list.get(i);
         if (this.getDistanceSqToEntity(entity) <= 0.2D) {
            entity.applyEntityCollision(this);
         }
      }

   }

   public EnumActionResult applyPlayerInteraction(EntityPlayer entityhuman, Vec3d vec3d, @Nullable ItemStack itemstack, EnumHand enumhand) {
      if (this.hasMarker()) {
         return EnumActionResult.PASS;
      } else if (!this.world.isRemote && !entityhuman.isSpectator()) {
         EntityEquipmentSlot enumitemslot = EntityEquipmentSlot.MAINHAND;
         boolean flag = itemstack != null;
         Item item = flag ? itemstack.getItem() : null;
         if (flag && item instanceof ItemArmor) {
            enumitemslot = ((ItemArmor)item).armorType;
         }

         if (flag && (item == Items.SKULL || item == Item.getItemFromBlock(Blocks.PUMPKIN))) {
            enumitemslot = EntityEquipmentSlot.HEAD;
         }

         EntityEquipmentSlot enumitemslot1 = EntityEquipmentSlot.MAINHAND;
         boolean flag1 = this.isSmall();
         double d4 = flag1 ? vec3d.yCoord * 2.0D : vec3d.yCoord;
         if (d4 >= 0.1D && d4 < 0.1D + (flag1 ? 0.8D : 0.45D) && this.getItemStackFromSlot(EntityEquipmentSlot.FEET) != null) {
            enumitemslot1 = EntityEquipmentSlot.FEET;
         } else if (d4 >= 0.9D + (flag1 ? 0.3D : 0.0D) && d4 < 0.9D + (flag1 ? 1.0D : 0.7D) && this.getItemStackFromSlot(EntityEquipmentSlot.CHEST) != null) {
            enumitemslot1 = EntityEquipmentSlot.CHEST;
         } else if (d4 >= 0.4D && d4 < 0.4D + (flag1 ? 1.0D : 0.8D) && this.getItemStackFromSlot(EntityEquipmentSlot.LEGS) != null) {
            enumitemslot1 = EntityEquipmentSlot.LEGS;
         } else if (d4 >= 1.6D && this.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null) {
            enumitemslot1 = EntityEquipmentSlot.HEAD;
         }

         boolean flag2 = this.getItemStackFromSlot(enumitemslot1) != null;
         if (this.isDisabled(enumitemslot1) || this.isDisabled(enumitemslot)) {
            enumitemslot1 = enumitemslot;
            if (this.isDisabled(enumitemslot)) {
               return EnumActionResult.FAIL;
            }
         }

         if (flag && enumitemslot == EntityEquipmentSlot.MAINHAND && !this.getShowArms()) {
            return EnumActionResult.FAIL;
         } else {
            if (flag) {
               this.swapItem(entityhuman, enumitemslot, itemstack, enumhand);
            } else if (flag2) {
               this.swapItem(entityhuman, enumitemslot1, itemstack, enumhand);
            }

            return EnumActionResult.SUCCESS;
         }
      } else {
         return EnumActionResult.SUCCESS;
      }
   }

   private boolean isDisabled(EntityEquipmentSlot enumitemslot) {
      return (this.disabledSlots & 1 << enumitemslot.getSlotIndex()) != 0;
   }

   private void swapItem(EntityPlayer entityhuman, EntityEquipmentSlot enumitemslot, @Nullable ItemStack itemstack, EnumHand enumhand) {
      ItemStack itemstack1 = this.getItemStackFromSlot(enumitemslot);
      if ((itemstack1 == null || (this.disabledSlots & 1 << enumitemslot.getSlotIndex() + 8) == 0) && (itemstack1 != null || (this.disabledSlots & 1 << enumitemslot.getSlotIndex() + 16) == 0)) {
         org.bukkit.inventory.ItemStack armorStandItem = CraftItemStack.asCraftMirror(itemstack1);
         org.bukkit.inventory.ItemStack playerHeldItem = CraftItemStack.asCraftMirror(itemstack);
         Player player = (Player)entityhuman.getBukkitEntity();
         ArmorStand self = (ArmorStand)this.getBukkitEntity();
         EquipmentSlot slot = CraftEquipmentSlot.getSlot(enumitemslot);
         PlayerArmorStandManipulateEvent armorStandManipulateEvent = new PlayerArmorStandManipulateEvent(player, self, playerHeldItem, armorStandItem, slot);
         this.world.getServer().getPluginManager().callEvent(armorStandManipulateEvent);
         if (armorStandManipulateEvent.isCancelled()) {
            return;
         }

         if (entityhuman.capabilities.isCreativeMode && (itemstack1 == null || itemstack1.getItem() == Item.getItemFromBlock(Blocks.AIR)) && itemstack != null) {
            ItemStack itemstack2 = itemstack.copy();
            itemstack2.stackSize = 1;
            this.setItemStackToSlot(enumitemslot, itemstack2);
         } else if (itemstack != null && itemstack.stackSize > 1) {
            if (itemstack1 == null) {
               ItemStack itemstack2 = itemstack.copy();
               itemstack2.stackSize = 1;
               this.setItemStackToSlot(enumitemslot, itemstack2);
               --itemstack.stackSize;
            }
         } else {
            this.setItemStackToSlot(enumitemslot, itemstack);
            entityhuman.setHeldItem(enumhand, itemstack1);
         }
      }

   }

   public boolean attackEntityFrom(DamageSource damagesource, float f) {
      if (CraftEventFactory.handleNonLivingEntityDamageEvent(this, damagesource, (double)f)) {
         return false;
      } else if (!this.world.isRemote && !this.isDead) {
         if (DamageSource.outOfWorld.equals(damagesource)) {
            this.setDead();
            return false;
         } else if (!this.isEntityInvulnerable(damagesource) && !this.canInteract && !this.hasMarker()) {
            if (damagesource.isExplosion()) {
               this.dropContents();
               this.setDead();
               return false;
            } else if (DamageSource.inFire.equals(damagesource)) {
               if (this.isBurning()) {
                  this.damageArmorStand(0.15F);
               } else {
                  this.setFire(5);
               }

               return false;
            } else if (DamageSource.onFire.equals(damagesource) && this.getHealth() > 0.5F) {
               this.damageArmorStand(4.0F);
               return false;
            } else {
               boolean flag = "arrow".equals(damagesource.getDamageType());
               boolean flag1 = "player".equals(damagesource.getDamageType());
               if (!flag1 && !flag) {
                  return false;
               } else {
                  if (damagesource.getSourceOfDamage() instanceof EntityArrow) {
                     damagesource.getSourceOfDamage().setDead();
                  }

                  if (damagesource.getEntity() instanceof EntityPlayer && !((EntityPlayer)damagesource.getEntity()).capabilities.allowEdit) {
                     return false;
                  } else if (damagesource.isCreativePlayer()) {
                     this.playParticles();
                     this.setDead();
                     return false;
                  } else {
                     long i = this.world.getTotalWorldTime();
                     if (i - this.punchCooldown > 5L && !flag) {
                        this.world.setEntityState(this, (byte)32);
                        this.punchCooldown = i;
                     } else {
                        this.dropBlock();
                        this.playParticles();
                        this.setDead();
                     }

                     return false;
                  }
               }
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void playParticles() {
      if (this.world instanceof WorldServer) {
         ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + (double)this.height / 1.5D, this.posZ, 10, (double)(this.width / 4.0F), (double)(this.height / 4.0F), (double)(this.width / 4.0F), 0.05D, Block.getStateId(Blocks.PLANKS.getDefaultState()));
      }

   }

   private void damageArmorStand(float f) {
      float f1 = this.getHealth();
      f1 = f1 - f;
      if (f1 <= 0.5F) {
         this.dropContents();
         this.setDead();
      } else {
         this.setHealth(f1);
      }

   }

   private void dropBlock() {
      Block.spawnAsEntity(this.world, new BlockPos(this), new ItemStack(Items.ARMOR_STAND));
      this.dropContents();
   }

   private void dropContents() {
      this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARMORSTAND_BREAK, this.getSoundCategory(), 1.0F, 1.0F);

      for(int i = 0; i < this.handItems.length; ++i) {
         if (this.handItems[i] != null && this.handItems[i].stackSize > 0) {
            if (this.handItems[i] != null) {
               Block.spawnAsEntity(this.world, (new BlockPos(this)).up(), this.handItems[i]);
            }

            this.handItems[i] = null;
         }
      }

      for(int var2 = 0; var2 < this.armorItems.length; ++var2) {
         if (this.armorItems[var2] != null && this.armorItems[var2].stackSize > 0) {
            if (this.armorItems[var2] != null) {
               Block.spawnAsEntity(this.world, (new BlockPos(this)).up(), this.armorItems[var2]);
            }

            this.armorItems[var2] = null;
         }
      }

   }

   protected float updateDistance(float f, float f1) {
      this.prevRenderYawOffset = this.prevRotationYaw;
      this.renderYawOffset = this.rotationYaw;
      return 0.0F;
   }

   public float getEyeHeight() {
      return this.isChild() ? this.height * 0.5F : this.height * 0.9F;
   }

   public double getYOffset() {
      return this.hasMarker() ? 0.0D : 0.10000000149011612D;
   }

   public void moveEntityWithHeading(float f, float f1) {
      if (!this.hasNoGravity()) {
         super.moveEntityWithHeading(f, f1);
      }

   }

   public void onUpdate() {
      super.onUpdate();
      Rotations vector3f = (Rotations)this.dataManager.get(HEAD_ROTATION);
      if (!this.headRotation.equals(vector3f)) {
         this.setHeadRotation(vector3f);
      }

      Rotations vector3f1 = (Rotations)this.dataManager.get(BODY_ROTATION);
      if (!this.bodyRotation.equals(vector3f1)) {
         this.setBodyRotation(vector3f1);
      }

      Rotations vector3f2 = (Rotations)this.dataManager.get(LEFT_ARM_ROTATION);
      if (!this.leftArmRotation.equals(vector3f2)) {
         this.setLeftArmRotation(vector3f2);
      }

      Rotations vector3f3 = (Rotations)this.dataManager.get(RIGHT_ARM_ROTATION);
      if (!this.rightArmRotation.equals(vector3f3)) {
         this.setRightArmRotation(vector3f3);
      }

      Rotations vector3f4 = (Rotations)this.dataManager.get(LEFT_LEG_ROTATION);
      if (!this.leftLegRotation.equals(vector3f4)) {
         this.setLeftLegRotation(vector3f4);
      }

      Rotations vector3f5 = (Rotations)this.dataManager.get(RIGHT_LEG_ROTATION);
      if (!this.rightLegRotation.equals(vector3f5)) {
         this.setRightLegRotation(vector3f5);
      }

      boolean flag = this.hasMarker();
      if (!this.wasMarker && flag) {
         this.updateBoundingBox(false);
         this.preventEntitySpawning = false;
      } else {
         if (!this.wasMarker || flag) {
            return;
         }

         this.updateBoundingBox(true);
         this.preventEntitySpawning = true;
      }

      this.wasMarker = flag;
   }

   private void updateBoundingBox(boolean flag) {
      double d0 = this.posX;
      double d1 = this.posY;
      double d2 = this.posZ;
      if (flag) {
         this.setSize(0.5F, 1.975F);
      } else {
         this.setSize(0.0F, 0.0F);
      }

      this.setPosition(d0, d1, d2);
   }

   protected void updatePotionMetadata() {
      this.setInvisible(this.canInteract);
   }

   public void setInvisible(boolean flag) {
      this.canInteract = flag;
      super.setInvisible(flag);
   }

   public boolean isChild() {
      return this.isSmall();
   }

   public void onKillCommand() {
      this.setDead();
   }

   public boolean isImmuneToExplosions() {
      return this.isInvisible();
   }

   public void setSmall(boolean flag) {
      this.dataManager.set(STATUS, Byte.valueOf(this.setBit(((Byte)this.dataManager.get(STATUS)).byteValue(), 1, flag)));
   }

   public boolean isSmall() {
      return (((Byte)this.dataManager.get(STATUS)).byteValue() & 1) != 0;
   }

   public void setShowArms(boolean flag) {
      this.dataManager.set(STATUS, Byte.valueOf(this.setBit(((Byte)this.dataManager.get(STATUS)).byteValue(), 4, flag)));
   }

   public boolean getShowArms() {
      return (((Byte)this.dataManager.get(STATUS)).byteValue() & 4) != 0;
   }

   public void setNoBasePlate(boolean flag) {
      this.dataManager.set(STATUS, Byte.valueOf(this.setBit(((Byte)this.dataManager.get(STATUS)).byteValue(), 8, flag)));
   }

   public boolean hasNoBasePlate() {
      return (((Byte)this.dataManager.get(STATUS)).byteValue() & 8) != 0;
   }

   public void setMarker(boolean flag) {
      this.dataManager.set(STATUS, Byte.valueOf(this.setBit(((Byte)this.dataManager.get(STATUS)).byteValue(), 16, flag)));
   }

   public boolean hasMarker() {
      return (((Byte)this.dataManager.get(STATUS)).byteValue() & 16) != 0;
   }

   private byte setBit(byte b0, int i, boolean flag) {
      if (flag) {
         b0 = (byte)(b0 | i);
      } else {
         b0 = (byte)(b0 & ~i);
      }

      return b0;
   }

   public void setHeadRotation(Rotations vector3f) {
      this.headRotation = vector3f;
      this.dataManager.set(HEAD_ROTATION, vector3f);
   }

   public void setBodyRotation(Rotations vector3f) {
      this.bodyRotation = vector3f;
      this.dataManager.set(BODY_ROTATION, vector3f);
   }

   public void setLeftArmRotation(Rotations vector3f) {
      this.leftArmRotation = vector3f;
      this.dataManager.set(LEFT_ARM_ROTATION, vector3f);
   }

   public void setRightArmRotation(Rotations vector3f) {
      this.rightArmRotation = vector3f;
      this.dataManager.set(RIGHT_ARM_ROTATION, vector3f);
   }

   public void setLeftLegRotation(Rotations vector3f) {
      this.leftLegRotation = vector3f;
      this.dataManager.set(LEFT_LEG_ROTATION, vector3f);
   }

   public void setRightLegRotation(Rotations vector3f) {
      this.rightLegRotation = vector3f;
      this.dataManager.set(RIGHT_LEG_ROTATION, vector3f);
   }

   public Rotations getHeadRotation() {
      return this.headRotation;
   }

   public Rotations getBodyRotation() {
      return this.bodyRotation;
   }

   public boolean canBeCollidedWith() {
      return super.canBeCollidedWith() && !this.hasMarker();
   }

   public EnumHandSide getPrimaryHand() {
      return EnumHandSide.RIGHT;
   }

   protected SoundEvent getFallSound(int i) {
      return SoundEvents.ENTITY_ARMORSTAND_FALL;
   }

   @Nullable
   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_ARMORSTAND_HIT;
   }

   @Nullable
   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_ARMORSTAND_BREAK;
   }

   public void onStruckByLightning(EntityLightningBolt entitylightning) {
   }

   public boolean canBeHitWithPotion() {
      return false;
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[EntityEquipmentSlot.Type.values().length];

      static {
         try {
            a[EntityEquipmentSlot.Type.HAND.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EntityEquipmentSlot.Type.ARMOR.ordinal()] = 2;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
