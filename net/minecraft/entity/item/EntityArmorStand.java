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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
      public boolean apply(@Nullable Entity var1) {
         return var1 instanceof EntityMinecart && ((EntityMinecart)var1).getType() == EntityMinecart.Type.RIDEABLE;
      }
   };
   private final ItemStack[] handItems;
   private final ItemStack[] armorItems;
   private boolean canInteract;
   public long punchCooldown;
   private int disabledSlots;
   private boolean wasMarker;
   private Rotations headRotation;
   private Rotations bodyRotation;
   private Rotations leftArmRotation;
   private Rotations rightArmRotation;
   private Rotations leftLegRotation;
   private Rotations rightLegRotation;

   public EntityArmorStand(World var1) {
      super(var1);
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

   public EntityArmorStand(World var1, double var2, double var4, double var6) {
      this(var1);
      this.setPosition(var2, var4, var6);
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
   public ItemStack getItemStackFromSlot(EntityEquipmentSlot var1) {
      ItemStack var2 = null;
      switch(var1.getSlotType()) {
      case HAND:
         var2 = this.handItems[var1.getIndex()];
         break;
      case ARMOR:
         var2 = this.armorItems[var1.getIndex()];
      }

      return var2;
   }

   public void setItemStackToSlot(EntityEquipmentSlot var1, @Nullable ItemStack var2) {
      switch(var1.getSlotType()) {
      case HAND:
         this.playEquipSound(var2);
         this.handItems[var1.getIndex()] = var2;
         break;
      case ARMOR:
         this.playEquipSound(var2);
         this.armorItems[var1.getIndex()] = var2;
      }

   }

   public boolean replaceItemInInventory(int var1, @Nullable ItemStack var2) {
      EntityEquipmentSlot var3;
      if (var1 == 98) {
         var3 = EntityEquipmentSlot.MAINHAND;
      } else if (var1 == 99) {
         var3 = EntityEquipmentSlot.OFFHAND;
      } else if (var1 == 100 + EntityEquipmentSlot.HEAD.getIndex()) {
         var3 = EntityEquipmentSlot.HEAD;
      } else if (var1 == 100 + EntityEquipmentSlot.CHEST.getIndex()) {
         var3 = EntityEquipmentSlot.CHEST;
      } else if (var1 == 100 + EntityEquipmentSlot.LEGS.getIndex()) {
         var3 = EntityEquipmentSlot.LEGS;
      } else {
         if (var1 != 100 + EntityEquipmentSlot.FEET.getIndex()) {
            return false;
         }

         var3 = EntityEquipmentSlot.FEET;
      }

      if (var2 != null && !EntityLiving.isItemStackInSlot(var3, var2) && var3 != EntityEquipmentSlot.HEAD) {
         return false;
      } else {
         this.setItemStackToSlot(var3, var2);
         return true;
      }
   }

   public static void registerFixesArmorStand(DataFixer var0) {
      var0.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("ArmorStand", new String[]{"ArmorItems", "HandItems"}));
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      NBTTagList var2 = new NBTTagList();

      for(ItemStack var6 : this.armorItems) {
         NBTTagCompound var7 = new NBTTagCompound();
         if (var6 != null) {
            var6.writeToNBT(var7);
         }

         var2.appendTag(var7);
      }

      var1.setTag("ArmorItems", var2);
      NBTTagList var9 = new NBTTagList();

      for(ItemStack var13 : this.handItems) {
         NBTTagCompound var8 = new NBTTagCompound();
         if (var13 != null) {
            var13.writeToNBT(var8);
         }

         var9.appendTag(var8);
      }

      var1.setTag("HandItems", var9);
      if (this.getAlwaysRenderNameTag() && (this.getCustomNameTag() == null || this.getCustomNameTag().isEmpty())) {
         var1.setBoolean("CustomNameVisible", this.getAlwaysRenderNameTag());
      }

      var1.setBoolean("Invisible", this.isInvisible());
      var1.setBoolean("Small", this.isSmall());
      var1.setBoolean("ShowArms", this.getShowArms());
      var1.setInteger("DisabledSlots", this.disabledSlots);
      var1.setBoolean("NoBasePlate", this.hasNoBasePlate());
      if (this.hasMarker()) {
         var1.setBoolean("Marker", this.hasMarker());
      }

      var1.setTag("Pose", this.readPoseFromNBT());
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      if (var1.hasKey("ArmorItems", 9)) {
         NBTTagList var2 = var1.getTagList("ArmorItems", 10);

         for(int var3 = 0; var3 < this.armorItems.length; ++var3) {
            this.armorItems[var3] = ItemStack.loadItemStackFromNBT(var2.getCompoundTagAt(var3));
         }
      }

      if (var1.hasKey("HandItems", 9)) {
         NBTTagList var4 = var1.getTagList("HandItems", 10);

         for(int var6 = 0; var6 < this.handItems.length; ++var6) {
            this.handItems[var6] = ItemStack.loadItemStackFromNBT(var4.getCompoundTagAt(var6));
         }
      }

      this.setInvisible(var1.getBoolean("Invisible"));
      this.setSmall(var1.getBoolean("Small"));
      this.setShowArms(var1.getBoolean("ShowArms"));
      this.disabledSlots = var1.getInteger("DisabledSlots");
      this.setNoBasePlate(var1.getBoolean("NoBasePlate"));
      this.setMarker(var1.getBoolean("Marker"));
      this.wasMarker = !this.hasMarker();
      this.noClip = this.hasNoGravity();
      NBTTagCompound var5 = var1.getCompoundTag("Pose");
      this.writePoseToNBT(var5);
   }

   private void writePoseToNBT(NBTTagCompound var1) {
      NBTTagList var2 = var1.getTagList("Head", 5);
      this.setHeadRotation(var2.hasNoTags() ? DEFAULT_HEAD_ROTATION : new Rotations(var2));
      NBTTagList var3 = var1.getTagList("Body", 5);
      this.setBodyRotation(var3.hasNoTags() ? DEFAULT_BODY_ROTATION : new Rotations(var3));
      NBTTagList var4 = var1.getTagList("LeftArm", 5);
      this.setLeftArmRotation(var4.hasNoTags() ? DEFAULT_LEFTARM_ROTATION : new Rotations(var4));
      NBTTagList var5 = var1.getTagList("RightArm", 5);
      this.setRightArmRotation(var5.hasNoTags() ? DEFAULT_RIGHTARM_ROTATION : new Rotations(var5));
      NBTTagList var6 = var1.getTagList("LeftLeg", 5);
      this.setLeftLegRotation(var6.hasNoTags() ? DEFAULT_LEFTLEG_ROTATION : new Rotations(var6));
      NBTTagList var7 = var1.getTagList("RightLeg", 5);
      this.setRightLegRotation(var7.hasNoTags() ? DEFAULT_RIGHTLEG_ROTATION : new Rotations(var7));
   }

   private NBTTagCompound readPoseFromNBT() {
      NBTTagCompound var1 = new NBTTagCompound();
      if (!DEFAULT_HEAD_ROTATION.equals(this.headRotation)) {
         var1.setTag("Head", this.headRotation.writeToNBT());
      }

      if (!DEFAULT_BODY_ROTATION.equals(this.bodyRotation)) {
         var1.setTag("Body", this.bodyRotation.writeToNBT());
      }

      if (!DEFAULT_LEFTARM_ROTATION.equals(this.leftArmRotation)) {
         var1.setTag("LeftArm", this.leftArmRotation.writeToNBT());
      }

      if (!DEFAULT_RIGHTARM_ROTATION.equals(this.rightArmRotation)) {
         var1.setTag("RightArm", this.rightArmRotation.writeToNBT());
      }

      if (!DEFAULT_LEFTLEG_ROTATION.equals(this.leftLegRotation)) {
         var1.setTag("LeftLeg", this.leftLegRotation.writeToNBT());
      }

      if (!DEFAULT_RIGHTLEG_ROTATION.equals(this.rightLegRotation)) {
         var1.setTag("RightLeg", this.rightLegRotation.writeToNBT());
      }

      return var1;
   }

   public boolean canBePushed() {
      return false;
   }

   protected void collideWithEntity(Entity var1) {
   }

   protected void collideWithNearbyEntities() {
      List var1 = this.world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox(), IS_RIDEABLE_MINECART);

      for(int var2 = 0; var2 < var1.size(); ++var2) {
         Entity var3 = (Entity)var1.get(var2);
         if (this.getDistanceSqToEntity(var3) <= 0.2D) {
            var3.applyEntityCollision(this);
         }
      }

   }

   public EnumActionResult applyPlayerInteraction(EntityPlayer var1, Vec3d var2, @Nullable ItemStack var3, EnumHand var4) {
      if (this.hasMarker()) {
         return EnumActionResult.PASS;
      } else if (!this.world.isRemote && !var1.isSpectator()) {
         EntityEquipmentSlot var5 = EntityEquipmentSlot.MAINHAND;
         boolean var6 = var3 != null;
         Item var7 = var6 ? var3.getItem() : null;
         if (var6 && var7 instanceof ItemArmor) {
            var5 = ((ItemArmor)var7).armorType;
         }

         if (var6 && (var7 == Items.SKULL || var7 == Item.getItemFromBlock(Blocks.PUMPKIN))) {
            var5 = EntityEquipmentSlot.HEAD;
         }

         double var8 = 0.1D;
         double var10 = 0.9D;
         double var12 = 0.4D;
         double var14 = 1.6D;
         EntityEquipmentSlot var16 = EntityEquipmentSlot.MAINHAND;
         boolean var17 = this.isSmall();
         double var18 = var17 ? var2.yCoord * 2.0D : var2.yCoord;
         if (var18 >= 0.1D && var18 < 0.1D + (var17 ? 0.8D : 0.45D) && this.getItemStackFromSlot(EntityEquipmentSlot.FEET) != null) {
            var16 = EntityEquipmentSlot.FEET;
         } else if (var18 >= 0.9D + (var17 ? 0.3D : 0.0D) && var18 < 0.9D + (var17 ? 1.0D : 0.7D) && this.getItemStackFromSlot(EntityEquipmentSlot.CHEST) != null) {
            var16 = EntityEquipmentSlot.CHEST;
         } else if (var18 >= 0.4D && var18 < 0.4D + (var17 ? 1.0D : 0.8D) && this.getItemStackFromSlot(EntityEquipmentSlot.LEGS) != null) {
            var16 = EntityEquipmentSlot.LEGS;
         } else if (var18 >= 1.6D && this.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null) {
            var16 = EntityEquipmentSlot.HEAD;
         }

         boolean var20 = this.getItemStackFromSlot(var16) != null;
         if (this.isDisabled(var16) || this.isDisabled(var5)) {
            var16 = var5;
            if (this.isDisabled(var5)) {
               return EnumActionResult.FAIL;
            }
         }

         if (var6 && var5 == EntityEquipmentSlot.MAINHAND && !this.getShowArms()) {
            return EnumActionResult.FAIL;
         } else {
            if (var6) {
               this.swapItem(var1, var5, var3, var4);
            } else if (var20) {
               this.swapItem(var1, var16, var3, var4);
            }

            return EnumActionResult.SUCCESS;
         }
      } else {
         return EnumActionResult.SUCCESS;
      }
   }

   private boolean isDisabled(EntityEquipmentSlot var1) {
      return (this.disabledSlots & 1 << var1.getSlotIndex()) != 0;
   }

   private void swapItem(EntityPlayer var1, EntityEquipmentSlot var2, @Nullable ItemStack var3, EnumHand var4) {
      ItemStack var5 = this.getItemStackFromSlot(var2);
      if ((var5 == null || (this.disabledSlots & 1 << var2.getSlotIndex() + 8) == 0) && (var5 != null || (this.disabledSlots & 1 << var2.getSlotIndex() + 16) == 0)) {
         if (var1.capabilities.isCreativeMode && (var5 == null || var5.getItem() == Item.getItemFromBlock(Blocks.AIR)) && var3 != null) {
            ItemStack var7 = var3.copy();
            var7.stackSize = 1;
            this.setItemStackToSlot(var2, var7);
         } else if (var3 != null && var3.stackSize > 1) {
            if (var5 == null) {
               ItemStack var6 = var3.copy();
               var6.stackSize = 1;
               this.setItemStackToSlot(var2, var6);
               --var3.stackSize;
            }
         } else {
            this.setItemStackToSlot(var2, var3);
            var1.setHeldItem(var4, var5);
         }
      }

   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      if (!this.world.isRemote && !this.isDead) {
         if (DamageSource.outOfWorld.equals(var1)) {
            this.setDead();
            return false;
         } else if (!this.isEntityInvulnerable(var1) && !this.canInteract && !this.hasMarker()) {
            if (var1.isExplosion()) {
               this.dropContents();
               this.setDead();
               return false;
            } else if (DamageSource.inFire.equals(var1)) {
               if (this.isBurning()) {
                  this.damageArmorStand(0.15F);
               } else {
                  this.setFire(5);
               }

               return false;
            } else if (DamageSource.onFire.equals(var1) && this.getHealth() > 0.5F) {
               this.damageArmorStand(4.0F);
               return false;
            } else {
               boolean var3 = "arrow".equals(var1.getDamageType());
               boolean var4 = "player".equals(var1.getDamageType());
               if (!var4 && !var3) {
                  return false;
               } else {
                  if (var1.getSourceOfDamage() instanceof EntityArrow) {
                     var1.getSourceOfDamage().setDead();
                  }

                  if (var1.getEntity() instanceof EntityPlayer && !((EntityPlayer)var1.getEntity()).capabilities.allowEdit) {
                     return false;
                  } else if (var1.isCreativePlayer()) {
                     this.playParticles();
                     this.setDead();
                     return false;
                  } else {
                     long var5 = this.world.getTotalWorldTime();
                     if (var5 - this.punchCooldown > 5L && !var3) {
                        this.world.setEntityState(this, (byte)32);
                        this.punchCooldown = var5;
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

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      if (var1 == 32) {
         if (this.world.isRemote) {
            this.world.playSound(this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARMORSTAND_HIT, this.getSoundCategory(), 0.3F, 1.0F, false);
            this.punchCooldown = this.world.getTotalWorldTime();
         }
      } else {
         super.handleStatusUpdate(var1);
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean isInRangeToRenderDist(double var1) {
      double var3 = this.getEntityBoundingBox().getAverageEdgeLength() * 4.0D;
      if (Double.isNaN(var3) || var3 == 0.0D) {
         var3 = 4.0D;
      }

      var3 = var3 * 64.0D;
      return var1 < var3 * var3;
   }

   private void playParticles() {
      if (this.world instanceof WorldServer) {
         ((WorldServer)this.world).spawnParticle(EnumParticleTypes.BLOCK_DUST, this.posX, this.posY + (double)this.height / 1.5D, this.posZ, 10, (double)(this.width / 4.0F), (double)(this.height / 4.0F), (double)(this.width / 4.0F), 0.05D, Block.getStateId(Blocks.PLANKS.getDefaultState()));
      }

   }

   private void damageArmorStand(float var1) {
      float var2 = this.getHealth();
      var2 = var2 - var1;
      if (var2 <= 0.5F) {
         this.dropContents();
         this.setDead();
      } else {
         this.setHealth(var2);
      }

   }

   private void dropBlock() {
      Block.spawnAsEntity(this.world, new BlockPos(this), new ItemStack(Items.ARMOR_STAND));
      this.dropContents();
   }

   private void dropContents() {
      this.world.playSound((EntityPlayer)null, this.posX, this.posY, this.posZ, SoundEvents.ENTITY_ARMORSTAND_BREAK, this.getSoundCategory(), 1.0F, 1.0F);

      for(int var1 = 0; var1 < this.handItems.length; ++var1) {
         if (this.handItems[var1] != null && this.handItems[var1].stackSize > 0) {
            if (this.handItems[var1] != null) {
               Block.spawnAsEntity(this.world, (new BlockPos(this)).up(), this.handItems[var1]);
            }

            this.handItems[var1] = null;
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

   protected float updateDistance(float var1, float var2) {
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

   public void moveEntityWithHeading(float var1, float var2) {
      if (!this.hasNoGravity()) {
         super.moveEntityWithHeading(var1, var2);
      }

   }

   public void onUpdate() {
      super.onUpdate();
      Rotations var1 = (Rotations)this.dataManager.get(HEAD_ROTATION);
      if (!this.headRotation.equals(var1)) {
         this.setHeadRotation(var1);
      }

      Rotations var2 = (Rotations)this.dataManager.get(BODY_ROTATION);
      if (!this.bodyRotation.equals(var2)) {
         this.setBodyRotation(var2);
      }

      Rotations var3 = (Rotations)this.dataManager.get(LEFT_ARM_ROTATION);
      if (!this.leftArmRotation.equals(var3)) {
         this.setLeftArmRotation(var3);
      }

      Rotations var4 = (Rotations)this.dataManager.get(RIGHT_ARM_ROTATION);
      if (!this.rightArmRotation.equals(var4)) {
         this.setRightArmRotation(var4);
      }

      Rotations var5 = (Rotations)this.dataManager.get(LEFT_LEG_ROTATION);
      if (!this.leftLegRotation.equals(var5)) {
         this.setLeftLegRotation(var5);
      }

      Rotations var6 = (Rotations)this.dataManager.get(RIGHT_LEG_ROTATION);
      if (!this.rightLegRotation.equals(var6)) {
         this.setRightLegRotation(var6);
      }

      boolean var7 = this.hasMarker();
      if (!this.wasMarker && var7) {
         this.updateBoundingBox(false);
         this.preventEntitySpawning = false;
      } else {
         if (!this.wasMarker || var7) {
            return;
         }

         this.updateBoundingBox(true);
         this.preventEntitySpawning = true;
      }

      this.wasMarker = var7;
   }

   private void updateBoundingBox(boolean var1) {
      double var2 = this.posX;
      double var4 = this.posY;
      double var6 = this.posZ;
      if (var1) {
         this.setSize(0.5F, 1.975F);
      } else {
         this.setSize(0.0F, 0.0F);
      }

      this.setPosition(var2, var4, var6);
   }

   protected void updatePotionMetadata() {
      this.setInvisible(this.canInteract);
   }

   public void setInvisible(boolean var1) {
      this.canInteract = var1;
      super.setInvisible(var1);
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

   private void setSmall(boolean var1) {
      this.dataManager.set(STATUS, Byte.valueOf(this.setBit(((Byte)this.dataManager.get(STATUS)).byteValue(), 1, var1)));
   }

   public boolean isSmall() {
      return (((Byte)this.dataManager.get(STATUS)).byteValue() & 1) != 0;
   }

   private void setShowArms(boolean var1) {
      this.dataManager.set(STATUS, Byte.valueOf(this.setBit(((Byte)this.dataManager.get(STATUS)).byteValue(), 4, var1)));
   }

   public boolean getShowArms() {
      return (((Byte)this.dataManager.get(STATUS)).byteValue() & 4) != 0;
   }

   private void setNoBasePlate(boolean var1) {
      this.dataManager.set(STATUS, Byte.valueOf(this.setBit(((Byte)this.dataManager.get(STATUS)).byteValue(), 8, var1)));
   }

   public boolean hasNoBasePlate() {
      return (((Byte)this.dataManager.get(STATUS)).byteValue() & 8) != 0;
   }

   private void setMarker(boolean var1) {
      this.dataManager.set(STATUS, Byte.valueOf(this.setBit(((Byte)this.dataManager.get(STATUS)).byteValue(), 16, var1)));
   }

   public boolean hasMarker() {
      return (((Byte)this.dataManager.get(STATUS)).byteValue() & 16) != 0;
   }

   private byte setBit(byte var1, int var2, boolean var3) {
      if (var3) {
         var1 = (byte)(var1 | var2);
      } else {
         var1 = (byte)(var1 & ~var2);
      }

      return var1;
   }

   public void setHeadRotation(Rotations var1) {
      this.headRotation = var1;
      this.dataManager.set(HEAD_ROTATION, var1);
   }

   public void setBodyRotation(Rotations var1) {
      this.bodyRotation = var1;
      this.dataManager.set(BODY_ROTATION, var1);
   }

   public void setLeftArmRotation(Rotations var1) {
      this.leftArmRotation = var1;
      this.dataManager.set(LEFT_ARM_ROTATION, var1);
   }

   public void setRightArmRotation(Rotations var1) {
      this.rightArmRotation = var1;
      this.dataManager.set(RIGHT_ARM_ROTATION, var1);
   }

   public void setLeftLegRotation(Rotations var1) {
      this.leftLegRotation = var1;
      this.dataManager.set(LEFT_LEG_ROTATION, var1);
   }

   public void setRightLegRotation(Rotations var1) {
      this.rightLegRotation = var1;
      this.dataManager.set(RIGHT_LEG_ROTATION, var1);
   }

   public Rotations getHeadRotation() {
      return this.headRotation;
   }

   public Rotations getBodyRotation() {
      return this.bodyRotation;
   }

   @SideOnly(Side.CLIENT)
   public Rotations getLeftArmRotation() {
      return this.leftArmRotation;
   }

   @SideOnly(Side.CLIENT)
   public Rotations getRightArmRotation() {
      return this.rightArmRotation;
   }

   @SideOnly(Side.CLIENT)
   public Rotations getLeftLegRotation() {
      return this.leftLegRotation;
   }

   @SideOnly(Side.CLIENT)
   public Rotations getRightLegRotation() {
      return this.rightLegRotation;
   }

   public boolean canBeCollidedWith() {
      return super.canBeCollidedWith() && !this.hasMarker();
   }

   public EnumHandSide getPrimaryHand() {
      return EnumHandSide.RIGHT;
   }

   protected SoundEvent getFallSound(int var1) {
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

   public void onStruckByLightning(EntityLightningBolt var1) {
   }

   public boolean canBeHitWithPotion() {
      return false;
   }
}
