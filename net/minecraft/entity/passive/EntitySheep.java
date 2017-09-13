package net.minecraft.entity.passive;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIEatGrass;
import net.minecraft.entity.ai.EntityAIFollowParent;
import net.minecraft.entity.ai.EntityAILookIdle;
import net.minecraft.entity.ai.EntityAIMate;
import net.minecraft.entity.ai.EntityAIPanic;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITempt;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.entity.SheepRegrowWoolEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.InventoryView;

public class EntitySheep extends EntityAnimal {
   private static final DataParameter DYE_COLOR = EntityDataManager.createKey(EntitySheep.class, DataSerializers.BYTE);
   private final InventoryCrafting inventoryCrafting = new InventoryCrafting(new Container() {
      public boolean canInteractWith(EntityPlayer entityhuman) {
         return false;
      }

      public InventoryView getBukkitView() {
         return null;
      }
   }, 2, 1);
   private static final Map DYE_TO_RGB = Maps.newEnumMap(EnumDyeColor.class);
   private int sheepTimer;
   private EntityAIEatGrass entityAIEatGrass;

   static {
      DYE_TO_RGB.put(EnumDyeColor.WHITE, new float[]{1.0F, 1.0F, 1.0F});
      DYE_TO_RGB.put(EnumDyeColor.ORANGE, new float[]{0.85F, 0.5F, 0.2F});
      DYE_TO_RGB.put(EnumDyeColor.MAGENTA, new float[]{0.7F, 0.3F, 0.85F});
      DYE_TO_RGB.put(EnumDyeColor.LIGHT_BLUE, new float[]{0.4F, 0.6F, 0.85F});
      DYE_TO_RGB.put(EnumDyeColor.YELLOW, new float[]{0.9F, 0.9F, 0.2F});
      DYE_TO_RGB.put(EnumDyeColor.LIME, new float[]{0.5F, 0.8F, 0.1F});
      DYE_TO_RGB.put(EnumDyeColor.PINK, new float[]{0.95F, 0.5F, 0.65F});
      DYE_TO_RGB.put(EnumDyeColor.GRAY, new float[]{0.3F, 0.3F, 0.3F});
      DYE_TO_RGB.put(EnumDyeColor.SILVER, new float[]{0.6F, 0.6F, 0.6F});
      DYE_TO_RGB.put(EnumDyeColor.CYAN, new float[]{0.3F, 0.5F, 0.6F});
      DYE_TO_RGB.put(EnumDyeColor.PURPLE, new float[]{0.5F, 0.25F, 0.7F});
      DYE_TO_RGB.put(EnumDyeColor.BLUE, new float[]{0.2F, 0.3F, 0.7F});
      DYE_TO_RGB.put(EnumDyeColor.BROWN, new float[]{0.4F, 0.3F, 0.2F});
      DYE_TO_RGB.put(EnumDyeColor.GREEN, new float[]{0.4F, 0.5F, 0.2F});
      DYE_TO_RGB.put(EnumDyeColor.RED, new float[]{0.6F, 0.2F, 0.2F});
      DYE_TO_RGB.put(EnumDyeColor.BLACK, new float[]{0.1F, 0.1F, 0.1F});
   }

   public static float[] getDyeRgb(EnumDyeColor enumcolor) {
      return (float[])DYE_TO_RGB.get(enumcolor);
   }

   public EntitySheep(World world) {
      super(world);
      this.setSize(0.9F, 1.3F);
      this.inventoryCrafting.setInventorySlotContents(0, new ItemStack(Items.DYE));
      this.inventoryCrafting.setInventorySlotContents(1, new ItemStack(Items.DYE));
      this.inventoryCrafting.resultInventory = new InventoryCraftResult();
   }

   protected void initEntityAI() {
      this.entityAIEatGrass = new EntityAIEatGrass(this);
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(1, new EntityAIPanic(this, 1.25D));
      this.tasks.addTask(2, new EntityAIMate(this, 1.0D));
      this.tasks.addTask(3, new EntityAITempt(this, 1.1D, Items.WHEAT, false));
      this.tasks.addTask(4, new EntityAIFollowParent(this, 1.1D));
      this.tasks.addTask(5, this.entityAIEatGrass);
      this.tasks.addTask(6, new EntityAIWander(this, 1.0D));
      this.tasks.addTask(7, new EntityAIWatchClosest(this, EntityPlayer.class, 6.0F));
      this.tasks.addTask(8, new EntityAILookIdle(this));
   }

   protected void updateAITasks() {
      this.sheepTimer = this.entityAIEatGrass.getEatingGrassTimer();
      super.updateAITasks();
   }

   public void onLivingUpdate() {
      if (this.world.isRemote) {
         this.sheepTimer = Math.max(0, this.sheepTimer - 1);
      }

      super.onLivingUpdate();
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(8.0D);
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.23000000417232513D);
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(DYE_COLOR, Byte.valueOf((byte)0));
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      if (this.getSheared()) {
         return LootTableList.ENTITIES_SHEEP;
      } else {
         switch(EntitySheep.SyntheticClass_1.a[this.getFleeceColor().ordinal()]) {
         case 1:
         default:
            return LootTableList.ENTITIES_SHEEP_WHITE;
         case 2:
            return LootTableList.ENTITIES_SHEEP_ORANGE;
         case 3:
            return LootTableList.ENTITIES_SHEEP_MAGENTA;
         case 4:
            return LootTableList.ENTITIES_SHEEP_LIGHT_BLUE;
         case 5:
            return LootTableList.ENTITIES_SHEEP_YELLOW;
         case 6:
            return LootTableList.ENTITIES_SHEEP_LIME;
         case 7:
            return LootTableList.ENTITIES_SHEEP_PINK;
         case 8:
            return LootTableList.ENTITIES_SHEEP_GRAY;
         case 9:
            return LootTableList.ENTITIES_SHEEP_SILVER;
         case 10:
            return LootTableList.ENTITIES_SHEEP_CYAN;
         case 11:
            return LootTableList.ENTITIES_SHEEP_PURPLE;
         case 12:
            return LootTableList.ENTITIES_SHEEP_BLUE;
         case 13:
            return LootTableList.ENTITIES_SHEEP_BROWN;
         case 14:
            return LootTableList.ENTITIES_SHEEP_GREEN;
         case 15:
            return LootTableList.ENTITIES_SHEEP_RED;
         case 16:
            return LootTableList.ENTITIES_SHEEP_BLACK;
         }
      }
   }

   public boolean processInteract(EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack) {
      if (itemstack != null && itemstack.getItem() == Items.SHEARS && !this.getSheared() && !this.isChild()) {
         if (!this.world.isRemote) {
            PlayerShearEntityEvent event = new PlayerShearEntityEvent((Player)entityhuman.getBukkitEntity(), this.getBukkitEntity());
            this.world.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
               return false;
            }

            this.setSheared(true);
            int i = 1 + this.rand.nextInt(3);

            for(int j = 0; j < i; ++j) {
               this.forceDrops = true;
               EntityItem entityitem = this.entityDropItem(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, this.getFleeceColor().getMetadata()), 1.0F);
               this.forceDrops = false;
               entityitem.motionY += (double)(this.rand.nextFloat() * 0.05F);
               entityitem.motionX += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
               entityitem.motionZ += (double)((this.rand.nextFloat() - this.rand.nextFloat()) * 0.1F);
            }
         }

         itemstack.damageItem(1, entityhuman);
         this.playSound(SoundEvents.ENTITY_SHEEP_SHEAR, 1.0F, 1.0F);
      }

      return super.processInteract(entityhuman, enumhand, itemstack);
   }

   public static void registerFixesSheep(DataFixer dataconvertermanager) {
      EntityLiving.registerFixesMob(dataconvertermanager, "Sheep");
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      nbttagcompound.setBoolean("Sheared", this.getSheared());
      nbttagcompound.setByte("Color", (byte)this.getFleeceColor().getMetadata());
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      this.setSheared(nbttagcompound.getBoolean("Sheared"));
      this.setFleeceColor(EnumDyeColor.byMetadata(nbttagcompound.getByte("Color")));
   }

   protected SoundEvent getAmbientSound() {
      return SoundEvents.ENTITY_SHEEP_AMBIENT;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_SHEEP_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_SHEEP_DEATH;
   }

   protected void playStepSound(BlockPos blockposition, Block block) {
      this.playSound(SoundEvents.ENTITY_SHEEP_STEP, 0.15F, 1.0F);
   }

   public EnumDyeColor getFleeceColor() {
      return EnumDyeColor.byMetadata(((Byte)this.dataManager.get(DYE_COLOR)).byteValue() & 15);
   }

   public void setFleeceColor(EnumDyeColor enumcolor) {
      byte b0 = ((Byte)this.dataManager.get(DYE_COLOR)).byteValue();
      this.dataManager.set(DYE_COLOR, Byte.valueOf((byte)(b0 & 240 | enumcolor.getMetadata() & 15)));
   }

   public boolean getSheared() {
      return (((Byte)this.dataManager.get(DYE_COLOR)).byteValue() & 16) != 0;
   }

   public void setSheared(boolean flag) {
      byte b0 = ((Byte)this.dataManager.get(DYE_COLOR)).byteValue();
      if (flag) {
         this.dataManager.set(DYE_COLOR, Byte.valueOf((byte)(b0 | 16)));
      } else {
         this.dataManager.set(DYE_COLOR, Byte.valueOf((byte)(b0 & -17)));
      }

   }

   public static EnumDyeColor getRandomSheepColor(Random random) {
      int i = random.nextInt(100);
      return i < 5 ? EnumDyeColor.BLACK : (i < 10 ? EnumDyeColor.GRAY : (i < 15 ? EnumDyeColor.SILVER : (i < 18 ? EnumDyeColor.BROWN : (random.nextInt(500) == 0 ? EnumDyeColor.PINK : EnumDyeColor.WHITE))));
   }

   public EntitySheep createChild(EntityAgeable entityageable) {
      EntitySheep entitysheep = (EntitySheep)entityageable;
      EntitySheep entitysheep1 = new EntitySheep(this.world);
      entitysheep1.setFleeceColor(this.getDyeColorMixFromParents(this, entitysheep));
      return entitysheep1;
   }

   public void eatGrassBonus() {
      SheepRegrowWoolEvent event = new SheepRegrowWoolEvent((Sheep)this.getBukkitEntity());
      this.world.getServer().getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
         this.setSheared(false);
         if (this.isChild()) {
            this.addGrowth(60);
         }

      }
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance difficultydamagescaler, @Nullable IEntityLivingData groupdataentity) {
      groupdataentity = super.onInitialSpawn(difficultydamagescaler, groupdataentity);
      this.setFleeceColor(getRandomSheepColor(this.world.rand));
      return groupdataentity;
   }

   private EnumDyeColor getDyeColorMixFromParents(EntityAnimal entityanimal, EntityAnimal entityanimal1) {
      int i = ((EntitySheep)entityanimal).getFleeceColor().getDyeDamage();
      int j = ((EntitySheep)entityanimal1).getFleeceColor().getDyeDamage();
      this.inventoryCrafting.getStackInSlot(0).setItemDamage(i);
      this.inventoryCrafting.getStackInSlot(1).setItemDamage(j);
      ItemStack itemstack = CraftingManager.getInstance().findMatchingRecipe(this.inventoryCrafting, ((EntitySheep)entityanimal).world);
      int k;
      if (itemstack != null && itemstack.getItem() == Items.DYE) {
         k = itemstack.getMetadata();
      } else {
         k = this.world.rand.nextBoolean() ? i : j;
      }

      return EnumDyeColor.byDyeDamage(k);
   }

   public float getEyeHeight() {
      return 0.95F * this.height;
   }

   public EntityAgeable createChild(EntityAgeable entityageable) {
      return this.createChild(entityageable);
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[EnumDyeColor.values().length];

      static {
         try {
            a[EnumDyeColor.WHITE.ordinal()] = 1;
         } catch (NoSuchFieldError var15) {
            ;
         }

         try {
            a[EnumDyeColor.ORANGE.ordinal()] = 2;
         } catch (NoSuchFieldError var14) {
            ;
         }

         try {
            a[EnumDyeColor.MAGENTA.ordinal()] = 3;
         } catch (NoSuchFieldError var13) {
            ;
         }

         try {
            a[EnumDyeColor.LIGHT_BLUE.ordinal()] = 4;
         } catch (NoSuchFieldError var12) {
            ;
         }

         try {
            a[EnumDyeColor.YELLOW.ordinal()] = 5;
         } catch (NoSuchFieldError var11) {
            ;
         }

         try {
            a[EnumDyeColor.LIME.ordinal()] = 6;
         } catch (NoSuchFieldError var10) {
            ;
         }

         try {
            a[EnumDyeColor.PINK.ordinal()] = 7;
         } catch (NoSuchFieldError var9) {
            ;
         }

         try {
            a[EnumDyeColor.GRAY.ordinal()] = 8;
         } catch (NoSuchFieldError var8) {
            ;
         }

         try {
            a[EnumDyeColor.SILVER.ordinal()] = 9;
         } catch (NoSuchFieldError var7) {
            ;
         }

         try {
            a[EnumDyeColor.CYAN.ordinal()] = 10;
         } catch (NoSuchFieldError var6) {
            ;
         }

         try {
            a[EnumDyeColor.PURPLE.ordinal()] = 11;
         } catch (NoSuchFieldError var5) {
            ;
         }

         try {
            a[EnumDyeColor.BLUE.ordinal()] = 12;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            a[EnumDyeColor.BROWN.ordinal()] = 13;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[EnumDyeColor.GREEN.ordinal()] = 14;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[EnumDyeColor.RED.ordinal()] = 15;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[EnumDyeColor.BLACK.ordinal()] = 16;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
