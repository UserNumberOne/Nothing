package net.minecraft.entity.passive;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.INpc;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.ai.EntityAIFollowGolem;
import net.minecraft.entity.ai.EntityAIHarvestFarmland;
import net.minecraft.entity.ai.EntityAILookAtTradePlayer;
import net.minecraft.entity.ai.EntityAIMoveIndoors;
import net.minecraft.entity.ai.EntityAIMoveTowardsRestriction;
import net.minecraft.entity.ai.EntityAIOpenDoor;
import net.minecraft.entity.ai.EntityAIPlay;
import net.minecraft.entity.ai.EntityAIRestrictOpenDoor;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAITradePlayer;
import net.minecraft.entity.ai.EntityAIVillagerInteract;
import net.minecraft.entity.ai.EntityAIVillagerMate;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.ai.EntityAIWatchClosest2;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.pathfinding.PathNavigateGround;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Team;
import net.minecraft.stats.StatList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Tuple;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.village.Village;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftMerchantRecipe;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.event.entity.VillagerReplenishTradeEvent;

public class EntityVillager extends EntityAgeable implements IMerchant, INpc {
   private static final DataParameter PROFESSION = EntityDataManager.createKey(EntityVillager.class, DataSerializers.VARINT);
   private int randomTickDivider;
   private boolean isMating;
   private boolean isPlaying;
   Village villageObj;
   private EntityPlayer buyingPlayer;
   private MerchantRecipeList buyingList;
   private int timeUntilReset;
   private boolean needsInitilization;
   private boolean isWillingToMate;
   public int wealth;
   private String lastBuyingPlayer;
   private int careerId;
   private int careerLevel;
   private boolean isLookingForHome;
   private boolean areAdditionalTasksSet;
   public final InventoryBasic villagerInventory;
   private static final EntityVillager.ITradeList[][][][] DEFAULT_TRADE_LIST_MAP = new EntityVillager.ITradeList[][][][]{{{{new EntityVillager.EmeraldForItems(Items.WHEAT, new EntityVillager.PriceInfo(18, 22)), new EntityVillager.EmeraldForItems(Items.POTATO, new EntityVillager.PriceInfo(15, 19)), new EntityVillager.EmeraldForItems(Items.CARROT, new EntityVillager.PriceInfo(15, 19)), new EntityVillager.ListItemForEmeralds(Items.BREAD, new EntityVillager.PriceInfo(-4, -2))}, {new EntityVillager.EmeraldForItems(Item.getItemFromBlock(Blocks.PUMPKIN), new EntityVillager.PriceInfo(8, 13)), new EntityVillager.ListItemForEmeralds(Items.PUMPKIN_PIE, new EntityVillager.PriceInfo(-3, -2))}, {new EntityVillager.EmeraldForItems(Item.getItemFromBlock(Blocks.MELON_BLOCK), new EntityVillager.PriceInfo(7, 12)), new EntityVillager.ListItemForEmeralds(Items.APPLE, new EntityVillager.PriceInfo(-5, -7))}, {new EntityVillager.ListItemForEmeralds(Items.COOKIE, new EntityVillager.PriceInfo(-6, -10)), new EntityVillager.ListItemForEmeralds(Items.CAKE, new EntityVillager.PriceInfo(1, 1))}}, {{new EntityVillager.EmeraldForItems(Items.STRING, new EntityVillager.PriceInfo(15, 20)), new EntityVillager.EmeraldForItems(Items.COAL, new EntityVillager.PriceInfo(16, 24)), new EntityVillager.ItemAndEmeraldToItem(Items.FISH, new EntityVillager.PriceInfo(6, 6), Items.COOKED_FISH, new EntityVillager.PriceInfo(6, 6))}, {new EntityVillager.ListEnchantedItemForEmeralds(Items.FISHING_ROD, new EntityVillager.PriceInfo(7, 8))}}, {{new EntityVillager.EmeraldForItems(Item.getItemFromBlock(Blocks.WOOL), new EntityVillager.PriceInfo(16, 22)), new EntityVillager.ListItemForEmeralds(Items.SHEARS, new EntityVillager.PriceInfo(3, 4))}, {new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL)), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 1), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 2), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 3), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 4), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 5), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 6), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 7), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 8), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 9), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 10), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 11), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 12), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 13), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 14), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 15), new EntityVillager.PriceInfo(1, 2))}}, {{new EntityVillager.EmeraldForItems(Items.STRING, new EntityVillager.PriceInfo(15, 20)), new EntityVillager.ListItemForEmeralds(Items.ARROW, new EntityVillager.PriceInfo(-12, -8))}, {new EntityVillager.ListItemForEmeralds(Items.BOW, new EntityVillager.PriceInfo(2, 3)), new EntityVillager.ItemAndEmeraldToItem(Item.getItemFromBlock(Blocks.GRAVEL), new EntityVillager.PriceInfo(10, 10), Items.FLINT, new EntityVillager.PriceInfo(6, 10))}}}, {{{new EntityVillager.EmeraldForItems(Items.PAPER, new EntityVillager.PriceInfo(24, 36)), new EntityVillager.ListEnchantedBookForEmeralds()}, {new EntityVillager.EmeraldForItems(Items.BOOK, new EntityVillager.PriceInfo(8, 10)), new EntityVillager.ListItemForEmeralds(Items.COMPASS, new EntityVillager.PriceInfo(10, 12)), new EntityVillager.ListItemForEmeralds(Item.getItemFromBlock(Blocks.BOOKSHELF), new EntityVillager.PriceInfo(3, 4))}, {new EntityVillager.EmeraldForItems(Items.WRITTEN_BOOK, new EntityVillager.PriceInfo(2, 2)), new EntityVillager.ListItemForEmeralds(Items.CLOCK, new EntityVillager.PriceInfo(10, 12)), new EntityVillager.ListItemForEmeralds(Item.getItemFromBlock(Blocks.GLASS), new EntityVillager.PriceInfo(-5, -3))}, {new EntityVillager.ListEnchantedBookForEmeralds()}, {new EntityVillager.ListEnchantedBookForEmeralds()}, {new EntityVillager.ListItemForEmeralds(Items.NAME_TAG, new EntityVillager.PriceInfo(20, 22))}}}, {{{new EntityVillager.EmeraldForItems(Items.ROTTEN_FLESH, new EntityVillager.PriceInfo(36, 40)), new EntityVillager.EmeraldForItems(Items.GOLD_INGOT, new EntityVillager.PriceInfo(8, 10))}, {new EntityVillager.ListItemForEmeralds(Items.REDSTONE, new EntityVillager.PriceInfo(-4, -1)), new EntityVillager.ListItemForEmeralds(new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()), new EntityVillager.PriceInfo(-2, -1))}, {new EntityVillager.ListItemForEmeralds(Items.ENDER_PEARL, new EntityVillager.PriceInfo(4, 7)), new EntityVillager.ListItemForEmeralds(Item.getItemFromBlock(Blocks.GLOWSTONE), new EntityVillager.PriceInfo(-3, -1))}, {new EntityVillager.ListItemForEmeralds(Items.EXPERIENCE_BOTTLE, new EntityVillager.PriceInfo(3, 11))}}}, {{{new EntityVillager.EmeraldForItems(Items.COAL, new EntityVillager.PriceInfo(16, 24)), new EntityVillager.ListItemForEmeralds(Items.IRON_HELMET, new EntityVillager.PriceInfo(4, 6))}, {new EntityVillager.EmeraldForItems(Items.IRON_INGOT, new EntityVillager.PriceInfo(7, 9)), new EntityVillager.ListItemForEmeralds(Items.IRON_CHESTPLATE, new EntityVillager.PriceInfo(10, 14))}, {new EntityVillager.EmeraldForItems(Items.DIAMOND, new EntityVillager.PriceInfo(3, 4)), new EntityVillager.ListEnchantedItemForEmeralds(Items.DIAMOND_CHESTPLATE, new EntityVillager.PriceInfo(16, 19))}, {new EntityVillager.ListItemForEmeralds(Items.CHAINMAIL_BOOTS, new EntityVillager.PriceInfo(5, 7)), new EntityVillager.ListItemForEmeralds(Items.CHAINMAIL_LEGGINGS, new EntityVillager.PriceInfo(9, 11)), new EntityVillager.ListItemForEmeralds(Items.CHAINMAIL_HELMET, new EntityVillager.PriceInfo(5, 7)), new EntityVillager.ListItemForEmeralds(Items.CHAINMAIL_CHESTPLATE, new EntityVillager.PriceInfo(11, 15))}}, {{new EntityVillager.EmeraldForItems(Items.COAL, new EntityVillager.PriceInfo(16, 24)), new EntityVillager.ListItemForEmeralds(Items.IRON_AXE, new EntityVillager.PriceInfo(6, 8))}, {new EntityVillager.EmeraldForItems(Items.IRON_INGOT, new EntityVillager.PriceInfo(7, 9)), new EntityVillager.ListEnchantedItemForEmeralds(Items.IRON_SWORD, new EntityVillager.PriceInfo(9, 10))}, {new EntityVillager.EmeraldForItems(Items.DIAMOND, new EntityVillager.PriceInfo(3, 4)), new EntityVillager.ListEnchantedItemForEmeralds(Items.DIAMOND_SWORD, new EntityVillager.PriceInfo(12, 15)), new EntityVillager.ListEnchantedItemForEmeralds(Items.DIAMOND_AXE, new EntityVillager.PriceInfo(9, 12))}}, {{new EntityVillager.EmeraldForItems(Items.COAL, new EntityVillager.PriceInfo(16, 24)), new EntityVillager.ListEnchantedItemForEmeralds(Items.IRON_SHOVEL, new EntityVillager.PriceInfo(5, 7))}, {new EntityVillager.EmeraldForItems(Items.IRON_INGOT, new EntityVillager.PriceInfo(7, 9)), new EntityVillager.ListEnchantedItemForEmeralds(Items.IRON_PICKAXE, new EntityVillager.PriceInfo(9, 11))}, {new EntityVillager.EmeraldForItems(Items.DIAMOND, new EntityVillager.PriceInfo(3, 4)), new EntityVillager.ListEnchantedItemForEmeralds(Items.DIAMOND_PICKAXE, new EntityVillager.PriceInfo(12, 15))}}}, {{{new EntityVillager.EmeraldForItems(Items.PORKCHOP, new EntityVillager.PriceInfo(14, 18)), new EntityVillager.EmeraldForItems(Items.CHICKEN, new EntityVillager.PriceInfo(14, 18))}, {new EntityVillager.EmeraldForItems(Items.COAL, new EntityVillager.PriceInfo(16, 24)), new EntityVillager.ListItemForEmeralds(Items.COOKED_PORKCHOP, new EntityVillager.PriceInfo(-7, -5)), new EntityVillager.ListItemForEmeralds(Items.COOKED_CHICKEN, new EntityVillager.PriceInfo(-8, -6))}}, {{new EntityVillager.EmeraldForItems(Items.LEATHER, new EntityVillager.PriceInfo(9, 12)), new EntityVillager.ListItemForEmeralds(Items.LEATHER_LEGGINGS, new EntityVillager.PriceInfo(2, 4))}, {new EntityVillager.ListEnchantedItemForEmeralds(Items.LEATHER_CHESTPLATE, new EntityVillager.PriceInfo(7, 12))}, {new EntityVillager.ListItemForEmeralds(Items.SADDLE, new EntityVillager.PriceInfo(8, 10))}}}};

   public EntityVillager(World world) {
      this(world, 0);
   }

   public EntityVillager(World world, int i) {
      super(world);
      this.villagerInventory = new InventoryBasic("Items", false, 8, (CraftVillager)this.getBukkitEntity());
      this.setProfession(i);
      this.setSize(0.6F, 1.95F);
      ((PathNavigateGround)this.getNavigator()).setBreakDoors(true);
      this.setCanPickUpLoot(true);
   }

   protected void initEntityAI() {
      this.tasks.addTask(0, new EntityAISwimming(this));
      this.tasks.addTask(1, new EntityAIAvoidEntity(this, EntityZombie.class, 8.0F, 0.6D, 0.6D));
      this.tasks.addTask(1, new EntityAITradePlayer(this));
      this.tasks.addTask(1, new EntityAILookAtTradePlayer(this));
      this.tasks.addTask(2, new EntityAIMoveIndoors(this));
      this.tasks.addTask(3, new EntityAIRestrictOpenDoor(this));
      this.tasks.addTask(4, new EntityAIOpenDoor(this, true));
      this.tasks.addTask(5, new EntityAIMoveTowardsRestriction(this, 0.6D));
      this.tasks.addTask(6, new EntityAIVillagerMate(this));
      this.tasks.addTask(7, new EntityAIFollowGolem(this));
      this.tasks.addTask(9, new EntityAIWatchClosest2(this, EntityPlayer.class, 3.0F, 1.0F));
      this.tasks.addTask(9, new EntityAIVillagerInteract(this));
      this.tasks.addTask(9, new EntityAIWander(this, 0.6D));
      this.tasks.addTask(10, new EntityAIWatchClosest(this, EntityLiving.class, 8.0F));
   }

   private void setAdditionalAItasks() {
      if (!this.areAdditionalTasksSet) {
         this.areAdditionalTasksSet = true;
         if (this.isChild()) {
            this.tasks.addTask(8, new EntityAIPlay(this, 0.32D));
         } else if (this.getProfession() == 0) {
            this.tasks.addTask(6, new EntityAIHarvestFarmland(this, 0.6D));
         }
      }

   }

   protected void onGrowingAdult() {
      if (this.getProfession() == 0) {
         this.tasks.addTask(8, new EntityAIHarvestFarmland(this, 0.6D));
      }

      super.onGrowingAdult();
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.5D);
   }

   protected void updateAITasks() {
      if (--this.randomTickDivider <= 0) {
         BlockPos blockposition = new BlockPos(this);
         this.world.getVillageCollection().addToVillagerPositionList(blockposition);
         this.randomTickDivider = 70 + this.rand.nextInt(50);
         this.villageObj = this.world.getVillageCollection().getNearestVillage(blockposition, 32);
         if (this.villageObj == null) {
            this.detachHome();
         } else {
            BlockPos blockposition1 = this.villageObj.getCenter();
            this.setHomePosAndDistance(blockposition1, this.villageObj.getVillageRadius());
            if (this.isLookingForHome) {
               this.isLookingForHome = false;
               this.villageObj.setDefaultPlayerReputation(5);
            }
         }
      }

      if (!this.isTrading() && this.timeUntilReset > 0) {
         --this.timeUntilReset;
         if (this.timeUntilReset <= 0) {
            if (this.needsInitilization) {
               for(MerchantRecipe merchantrecipe : this.buyingList) {
                  if (merchantrecipe.isRecipeDisabled()) {
                     int bonus = this.rand.nextInt(6) + this.rand.nextInt(6) + 2;
                     VillagerReplenishTradeEvent event = new VillagerReplenishTradeEvent((Villager)this.getBukkitEntity(), merchantrecipe.asBukkit(), bonus);
                     Bukkit.getPluginManager().callEvent(event);
                     if (!event.isCancelled()) {
                        merchantrecipe.increaseMaxTradeUses(event.getBonus());
                     }
                  }
               }

               this.populateBuyingList();
               this.needsInitilization = false;
               if (this.villageObj != null && this.lastBuyingPlayer != null) {
                  this.world.setEntityState(this, (byte)14);
                  this.villageObj.modifyPlayerReputation(this.lastBuyingPlayer, 1);
               }
            }

            this.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 200, 0));
         }
      }

      super.updateAITasks();
   }

   public boolean processInteract(EntityPlayer entityhuman, EnumHand enumhand, @Nullable ItemStack itemstack) {
      boolean flag = itemstack != null && itemstack.getItem() == Items.SPAWN_EGG;
      if (!flag && this.isEntityAlive() && !this.isTrading() && !this.isChild()) {
         if (!this.world.isRemote && (this.buyingList == null || !this.buyingList.isEmpty())) {
            this.setCustomer(entityhuman);
            entityhuman.displayVillagerTradeGui(this);
         }

         entityhuman.addStat(StatList.TALKED_TO_VILLAGER);
         return true;
      } else {
         return super.processInteract(entityhuman, enumhand, itemstack);
      }
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(PROFESSION, Integer.valueOf(0));
   }

   public static void registerFixesVillager(DataFixer dataconvertermanager) {
      EntityLiving.registerFixesMob(dataconvertermanager, "Villager");
      dataconvertermanager.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Villager", new String[]{"Inventory"}));
      dataconvertermanager.registerWalker(FixTypes.ENTITY, new IDataWalker() {
         public NBTTagCompound process(IDataFixer dataconverter, NBTTagCompound nbttagcompound, int i) {
            if ("Villager".equals(nbttagcompound.getString("id")) && nbttagcompound.hasKey("Offers", 10)) {
               NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Offers");
               if (nbttagcompound1.hasKey("Recipes", 9)) {
                  NBTTagList nbttaglist = nbttagcompound1.getTagList("Recipes", 10);

                  for(int j = 0; j < nbttaglist.tagCount(); ++j) {
                     NBTTagCompound nbttagcompound2 = nbttaglist.getCompoundTagAt(j);
                     DataFixesManager.processItemStack(dataconverter, nbttagcompound2, i, "buy");
                     DataFixesManager.processItemStack(dataconverter, nbttagcompound2, i, "buyB");
                     DataFixesManager.processItemStack(dataconverter, nbttagcompound2, i, "sell");
                     nbttaglist.set(j, nbttagcompound2);
                  }
               }
            }

            return nbttagcompound;
         }
      });
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      super.writeEntityToNBT(nbttagcompound);
      nbttagcompound.setInteger("Profession", this.getProfession());
      nbttagcompound.setInteger("Riches", this.wealth);
      nbttagcompound.setInteger("Career", this.careerId);
      nbttagcompound.setInteger("CareerLevel", this.careerLevel);
      nbttagcompound.setBoolean("Willing", this.isWillingToMate);
      if (this.buyingList != null) {
         nbttagcompound.setTag("Offers", this.buyingList.getRecipiesAsTags());
      }

      NBTTagList nbttaglist = new NBTTagList();

      for(int i = 0; i < this.villagerInventory.getSizeInventory(); ++i) {
         ItemStack itemstack = this.villagerInventory.getStackInSlot(i);
         if (itemstack != null) {
            nbttaglist.appendTag(itemstack.writeToNBT(new NBTTagCompound()));
         }
      }

      nbttagcompound.setTag("Inventory", nbttaglist);
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      super.readEntityFromNBT(nbttagcompound);
      this.setProfession(nbttagcompound.getInteger("Profession"));
      this.wealth = nbttagcompound.getInteger("Riches");
      this.careerId = nbttagcompound.getInteger("Career");
      this.careerLevel = nbttagcompound.getInteger("CareerLevel");
      this.isWillingToMate = nbttagcompound.getBoolean("Willing");
      if (nbttagcompound.hasKey("Offers", 10)) {
         NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("Offers");
         this.buyingList = new MerchantRecipeList(nbttagcompound1);
      }

      NBTTagList nbttaglist = nbttagcompound.getTagList("Inventory", 10);

      for(int i = 0; i < nbttaglist.tagCount(); ++i) {
         ItemStack itemstack = ItemStack.loadItemStackFromNBT(nbttaglist.getCompoundTagAt(i));
         if (itemstack != null) {
            this.villagerInventory.addItem(itemstack);
         }
      }

      this.setCanPickUpLoot(true);
      this.setAdditionalAItasks();
   }

   protected boolean canDespawn() {
      return false;
   }

   protected SoundEvent getAmbientSound() {
      return this.isTrading() ? SoundEvents.ENTITY_VILLAGER_TRADING : SoundEvents.ENTITY_VILLAGER_AMBIENT;
   }

   protected SoundEvent getHurtSound() {
      return SoundEvents.ENTITY_VILLAGER_HURT;
   }

   protected SoundEvent getDeathSound() {
      return SoundEvents.ENTITY_VILLAGER_DEATH;
   }

   public void setProfession(int i) {
      this.dataManager.set(PROFESSION, Integer.valueOf(i));
   }

   public int getProfession() {
      return Math.max(((Integer)this.dataManager.get(PROFESSION)).intValue() % 5, 0);
   }

   public boolean isMating() {
      return this.isMating;
   }

   public void setMating(boolean flag) {
      this.isMating = flag;
   }

   public void setPlaying(boolean flag) {
      this.isPlaying = flag;
   }

   public boolean isPlaying() {
      return this.isPlaying;
   }

   public void setRevengeTarget(@Nullable EntityLivingBase entityliving) {
      super.setRevengeTarget(entityliving);
      if (this.villageObj != null && entityliving != null) {
         this.villageObj.addOrRenewAgressor(entityliving);
         if (entityliving instanceof EntityPlayer) {
            byte b0 = -1;
            if (this.isChild()) {
               b0 = -3;
            }

            this.villageObj.modifyPlayerReputation(entityliving.getName(), b0);
            if (this.isEntityAlive()) {
               this.world.setEntityState(this, (byte)13);
            }
         }
      }

   }

   public void onDeath(DamageSource damagesource) {
      if (this.villageObj != null) {
         Entity entity = damagesource.getEntity();
         if (entity != null) {
            if (entity instanceof EntityPlayer) {
               this.villageObj.modifyPlayerReputation(entity.getName(), -2);
            } else if (entity instanceof IMob) {
               this.villageObj.endMatingSeason();
            }
         } else {
            EntityPlayer entityhuman = this.world.getClosestPlayerToEntity(this, 16.0D);
            if (entityhuman != null) {
               this.villageObj.endMatingSeason();
            }
         }
      }

      super.onDeath(damagesource);
   }

   public void setCustomer(EntityPlayer entityhuman) {
      this.buyingPlayer = entityhuman;
   }

   public EntityPlayer getCustomer() {
      return this.buyingPlayer;
   }

   public boolean isTrading() {
      return this.buyingPlayer != null;
   }

   public boolean getIsWillingToMate(boolean flag) {
      if (!this.isWillingToMate && flag && this.hasEnoughFoodToBreed()) {
         boolean flag1 = false;

         for(int i = 0; i < this.villagerInventory.getSizeInventory(); ++i) {
            ItemStack itemstack = this.villagerInventory.getStackInSlot(i);
            if (itemstack != null) {
               if (itemstack.getItem() == Items.BREAD && itemstack.stackSize >= 3) {
                  flag1 = true;
                  this.villagerInventory.decrStackSize(i, 3);
               } else if ((itemstack.getItem() == Items.POTATO || itemstack.getItem() == Items.CARROT) && itemstack.stackSize >= 12) {
                  flag1 = true;
                  this.villagerInventory.decrStackSize(i, 12);
               }
            }

            if (flag1) {
               this.world.setEntityState(this, (byte)18);
               this.isWillingToMate = true;
               break;
            }
         }
      }

      return this.isWillingToMate;
   }

   public void setIsWillingToMate(boolean flag) {
      this.isWillingToMate = flag;
   }

   public void useRecipe(MerchantRecipe merchantrecipe) {
      merchantrecipe.incrementToolUses();
      this.livingSoundTime = -this.getTalkInterval();
      this.playSound(SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
      int i = 3 + this.rand.nextInt(4);
      if (merchantrecipe.getToolUses() == 1 || this.rand.nextInt(5) == 0) {
         this.timeUntilReset = 40;
         this.needsInitilization = true;
         this.isWillingToMate = true;
         if (this.buyingPlayer != null) {
            this.lastBuyingPlayer = this.buyingPlayer.getName();
         } else {
            this.lastBuyingPlayer = null;
         }

         i += 5;
      }

      if (merchantrecipe.getItemToBuy().getItem() == Items.EMERALD) {
         this.wealth += merchantrecipe.getItemToBuy().stackSize;
      }

      if (merchantrecipe.getRewardsExp()) {
         this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY + 0.5D, this.posZ, i));
      }

   }

   public void verifySellingItem(ItemStack itemstack) {
      if (!this.world.isRemote && this.livingSoundTime > -this.getTalkInterval() + 20) {
         this.livingSoundTime = -this.getTalkInterval();
         if (itemstack != null) {
            this.playSound(SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
         } else {
            this.playSound(SoundEvents.ENTITY_VILLAGER_NO, this.getSoundVolume(), this.getSoundPitch());
         }
      }

   }

   public MerchantRecipeList getRecipes(EntityPlayer entityhuman) {
      if (this.buyingList == null) {
         this.populateBuyingList();
      }

      return this.buyingList;
   }

   private void populateBuyingList() {
      EntityVillager.ITradeList[][][] aentityvillager_imerchantrecipeoption = DEFAULT_TRADE_LIST_MAP[this.getProfession()];
      if (this.careerId != 0 && this.careerLevel != 0) {
         ++this.careerLevel;
      } else {
         this.careerId = this.rand.nextInt(aentityvillager_imerchantrecipeoption.length) + 1;
         this.careerLevel = 1;
      }

      if (this.buyingList == null) {
         this.buyingList = new MerchantRecipeList();
      }

      int i = this.careerId - 1;
      int j = this.careerLevel - 1;
      EntityVillager.ITradeList[][] aentityvillager_imerchantrecipeoption1 = aentityvillager_imerchantrecipeoption[i];
      if (j >= 0 && j < aentityvillager_imerchantrecipeoption1.length) {
         EntityVillager.ITradeList[] aentityvillager_imerchantrecipeoption2 = aentityvillager_imerchantrecipeoption1[j];

         for(EntityVillager.ITradeList entityvillager_imerchantrecipeoption : aentityvillager_imerchantrecipeoption2) {
            MerchantRecipeList list = new MerchantRecipeList();
            entityvillager_imerchantrecipeoption.modifyMerchantRecipeList(list, this.rand);

            for(MerchantRecipe recipe : list) {
               VillagerAcquireTradeEvent event = new VillagerAcquireTradeEvent((Villager)this.getBukkitEntity(), recipe.asBukkit());
               Bukkit.getPluginManager().callEvent(event);
               if (!event.isCancelled()) {
                  this.buyingList.add(CraftMerchantRecipe.fromBukkit(event.getRecipe()).toMinecraft());
               }
            }
         }
      }

   }

   public ITextComponent getDisplayName() {
      Team scoreboardteambase = this.getTeam();
      String s = this.getCustomNameTag();
      if (s != null && !s.isEmpty()) {
         TextComponentString chatcomponenttext = new TextComponentString(ScorePlayerTeam.formatPlayerName(scoreboardteambase, s));
         chatcomponenttext.getStyle().setHoverEvent(this.getHoverEvent());
         chatcomponenttext.getStyle().setInsertion(this.getCachedUniqueIdString());
         return chatcomponenttext;
      } else {
         if (this.buyingList == null) {
            this.populateBuyingList();
         }

         String s1 = null;
         switch(this.getProfession()) {
         case 0:
            if (this.careerId == 1) {
               s1 = "farmer";
            } else if (this.careerId == 2) {
               s1 = "fisherman";
            } else if (this.careerId == 3) {
               s1 = "shepherd";
            } else if (this.careerId == 4) {
               s1 = "fletcher";
            }
            break;
         case 1:
            s1 = "librarian";
            break;
         case 2:
            s1 = "cleric";
            break;
         case 3:
            if (this.careerId == 1) {
               s1 = "armor";
            } else if (this.careerId == 2) {
               s1 = "weapon";
            } else if (this.careerId == 3) {
               s1 = "tool";
            }
            break;
         case 4:
            if (this.careerId == 1) {
               s1 = "butcher";
            } else if (this.careerId == 2) {
               s1 = "leather";
            }
         }

         if (s1 != null) {
            TextComponentTranslation chatmessage = new TextComponentTranslation("entity.Villager." + s1, new Object[0]);
            chatmessage.getStyle().setHoverEvent(this.getHoverEvent());
            chatmessage.getStyle().setInsertion(this.getCachedUniqueIdString());
            if (scoreboardteambase != null) {
               chatmessage.getStyle().setColor(scoreboardteambase.getChatFormat());
            }

            return chatmessage;
         } else {
            return super.getDisplayName();
         }
      }
   }

   public float getEyeHeight() {
      return this.isChild() ? 0.81F : 1.62F;
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance difficultydamagescaler, @Nullable IEntityLivingData groupdataentity) {
      groupdataentity = super.onInitialSpawn(difficultydamagescaler, groupdataentity);
      this.setProfession(this.world.rand.nextInt(5));
      this.setAdditionalAItasks();
      return groupdataentity;
   }

   public void setLookingForHome() {
      this.isLookingForHome = true;
   }

   public EntityVillager createChild(EntityAgeable entityageable) {
      EntityVillager entityvillager = new EntityVillager(this.world);
      entityvillager.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(entityvillager)), (IEntityLivingData)null);
      return entityvillager;
   }

   public boolean canBeLeashedTo(EntityPlayer entityhuman) {
      return false;
   }

   public void onStruckByLightning(EntityLightningBolt entitylightning) {
      if (!this.world.isRemote && !this.isDead) {
         EntityWitch entitywitch = new EntityWitch(this.world);
         entitywitch.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
         entitywitch.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(entitywitch)), (IEntityLivingData)null);
         entitywitch.setNoAI(this.isAIDisabled());
         if (this.hasCustomName()) {
            entitywitch.setCustomNameTag(this.getCustomNameTag());
            entitywitch.setAlwaysRenderNameTag(this.getAlwaysRenderNameTag());
         }

         this.world.spawnEntity(entitywitch);
         this.setDead();
      }

   }

   public InventoryBasic getVillagerInventory() {
      return this.villagerInventory;
   }

   protected void updateEquipmentIfNeeded(EntityItem entityitem) {
      ItemStack itemstack = entityitem.getEntityItem();
      Item item = itemstack.getItem();
      if (this.canVillagerPickupItem(item)) {
         ItemStack itemstack1 = this.villagerInventory.addItem(itemstack);
         if (itemstack1 == null) {
            entityitem.setDead();
         } else {
            itemstack.stackSize = itemstack1.stackSize;
         }
      }

   }

   private boolean canVillagerPickupItem(Item item) {
      return item == Items.BREAD || item == Items.POTATO || item == Items.CARROT || item == Items.WHEAT || item == Items.WHEAT_SEEDS || item == Items.BEETROOT || item == Items.BEETROOT_SEEDS;
   }

   public boolean hasEnoughFoodToBreed() {
      return this.hasEnoughItems(1);
   }

   public boolean canAbondonItems() {
      return this.hasEnoughItems(2);
   }

   public boolean wantsMoreFood() {
      boolean flag = this.getProfession() == 0;
      return flag ? !this.hasEnoughItems(5) : !this.hasEnoughItems(1);
   }

   private boolean hasEnoughItems(int i) {
      boolean flag = this.getProfession() == 0;

      for(int j = 0; j < this.villagerInventory.getSizeInventory(); ++j) {
         ItemStack itemstack = this.villagerInventory.getStackInSlot(j);
         if (itemstack != null) {
            if (itemstack.getItem() == Items.BREAD && itemstack.stackSize >= 3 * i || itemstack.getItem() == Items.POTATO && itemstack.stackSize >= 12 * i || itemstack.getItem() == Items.CARROT && itemstack.stackSize >= 12 * i || itemstack.getItem() == Items.BEETROOT && itemstack.stackSize >= 12 * i) {
               return true;
            }

            if (flag && itemstack.getItem() == Items.WHEAT && itemstack.stackSize >= 9 * i) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean isFarmItemInInventory() {
      for(int i = 0; i < this.villagerInventory.getSizeInventory(); ++i) {
         ItemStack itemstack = this.villagerInventory.getStackInSlot(i);
         if (itemstack != null && (itemstack.getItem() == Items.WHEAT_SEEDS || itemstack.getItem() == Items.POTATO || itemstack.getItem() == Items.CARROT || itemstack.getItem() == Items.BEETROOT_SEEDS)) {
            return true;
         }
      }

      return false;
   }

   public boolean replaceItemInInventory(int i, @Nullable ItemStack itemstack) {
      if (super.replaceItemInInventory(i, itemstack)) {
         return true;
      } else {
         int j = i - 300;
         if (j >= 0 && j < this.villagerInventory.getSizeInventory()) {
            this.villagerInventory.setInventorySlotContents(j, itemstack);
            return true;
         } else {
            return false;
         }
      }
   }

   public EntityAgeable createChild(EntityAgeable entityageable) {
      return this.createChild(entityageable);
   }

   static class EmeraldForItems implements EntityVillager.ITradeList {
      public Item buyingItem;
      public EntityVillager.PriceInfo price;

      public EmeraldForItems(Item item, EntityVillager.PriceInfo entityvillager_merchantoptionrandomrange) {
         this.buyingItem = item;
         this.price = entityvillager_merchantoptionrandomrange;
      }

      public void modifyMerchantRecipeList(MerchantRecipeList merchantrecipelist, Random random) {
         int i = 1;
         if (this.price != null) {
            i = this.price.getPrice(random);
         }

         merchantrecipelist.add(new MerchantRecipe(new ItemStack(this.buyingItem, i, 0), Items.EMERALD));
      }
   }

   interface ITradeList {
      void modifyMerchantRecipeList(MerchantRecipeList var1, Random var2);
   }

   static class ItemAndEmeraldToItem implements EntityVillager.ITradeList {
      public ItemStack buyingItemStack;
      public EntityVillager.PriceInfo buyingPriceInfo;
      public ItemStack sellingItemstack;
      public EntityVillager.PriceInfo sellingPriceInfo;

      public ItemAndEmeraldToItem(Item item, EntityVillager.PriceInfo entityvillager_merchantoptionrandomrange, Item item1, EntityVillager.PriceInfo entityvillager_merchantoptionrandomrange1) {
         this.buyingItemStack = new ItemStack(item);
         this.buyingPriceInfo = entityvillager_merchantoptionrandomrange;
         this.sellingItemstack = new ItemStack(item1);
         this.sellingPriceInfo = entityvillager_merchantoptionrandomrange1;
      }

      public void modifyMerchantRecipeList(MerchantRecipeList merchantrecipelist, Random random) {
         int i = 1;
         if (this.buyingPriceInfo != null) {
            i = this.buyingPriceInfo.getPrice(random);
         }

         int j = 1;
         if (this.sellingPriceInfo != null) {
            j = this.sellingPriceInfo.getPrice(random);
         }

         merchantrecipelist.add(new MerchantRecipe(new ItemStack(this.buyingItemStack.getItem(), i, this.buyingItemStack.getMetadata()), new ItemStack(Items.EMERALD), new ItemStack(this.sellingItemstack.getItem(), j, this.sellingItemstack.getMetadata())));
      }
   }

   static class ListEnchantedBookForEmeralds implements EntityVillager.ITradeList {
      public void modifyMerchantRecipeList(MerchantRecipeList merchantrecipelist, Random random) {
         Enchantment enchantment = (Enchantment)Enchantment.REGISTRY.getRandomObject(random);
         int i = MathHelper.getInt(random, enchantment.getMinLevel(), enchantment.getMaxLevel());
         ItemStack itemstack = Items.ENCHANTED_BOOK.getEnchantedItemStack(new EnchantmentData(enchantment, i));
         int j = 2 + random.nextInt(5 + i * 10) + 3 * i;
         if (enchantment.isTreasureEnchantment()) {
            j *= 2;
         }

         if (j > 64) {
            j = 64;
         }

         merchantrecipelist.add(new MerchantRecipe(new ItemStack(Items.BOOK), new ItemStack(Items.EMERALD, j), itemstack));
      }
   }

   static class ListEnchantedItemForEmeralds implements EntityVillager.ITradeList {
      public ItemStack enchantedItemStack;
      public EntityVillager.PriceInfo priceInfo;

      public ListEnchantedItemForEmeralds(Item item, EntityVillager.PriceInfo entityvillager_merchantoptionrandomrange) {
         this.enchantedItemStack = new ItemStack(item);
         this.priceInfo = entityvillager_merchantoptionrandomrange;
      }

      public void modifyMerchantRecipeList(MerchantRecipeList merchantrecipelist, Random random) {
         int i = 1;
         if (this.priceInfo != null) {
            i = this.priceInfo.getPrice(random);
         }

         ItemStack itemstack = new ItemStack(Items.EMERALD, i, 0);
         ItemStack itemstack1 = new ItemStack(this.enchantedItemStack.getItem(), 1, this.enchantedItemStack.getMetadata());
         itemstack1 = EnchantmentHelper.addRandomEnchantment(random, itemstack1, 5 + random.nextInt(15), false);
         merchantrecipelist.add(new MerchantRecipe(itemstack, itemstack1));
      }
   }

   static class ListItemForEmeralds implements EntityVillager.ITradeList {
      public ItemStack itemToBuy;
      public EntityVillager.PriceInfo priceInfo;

      public ListItemForEmeralds(Item item, EntityVillager.PriceInfo entityvillager_merchantoptionrandomrange) {
         this.itemToBuy = new ItemStack(item);
         this.priceInfo = entityvillager_merchantoptionrandomrange;
      }

      public ListItemForEmeralds(ItemStack itemstack, EntityVillager.PriceInfo entityvillager_merchantoptionrandomrange) {
         this.itemToBuy = itemstack;
         this.priceInfo = entityvillager_merchantoptionrandomrange;
      }

      public void modifyMerchantRecipeList(MerchantRecipeList merchantrecipelist, Random random) {
         int i = 1;
         if (this.priceInfo != null) {
            i = this.priceInfo.getPrice(random);
         }

         ItemStack itemstack;
         ItemStack itemstack1;
         if (i < 0) {
            itemstack = new ItemStack(Items.EMERALD);
            itemstack1 = new ItemStack(this.itemToBuy.getItem(), -i, this.itemToBuy.getMetadata());
         } else {
            itemstack = new ItemStack(Items.EMERALD, i, 0);
            itemstack1 = new ItemStack(this.itemToBuy.getItem(), 1, this.itemToBuy.getMetadata());
         }

         merchantrecipelist.add(new MerchantRecipe(itemstack, itemstack1));
      }
   }

   static class PriceInfo extends Tuple {
      public PriceInfo(int i, int j) {
         super(Integer.valueOf(i), Integer.valueOf(j));
      }

      public int getPrice(Random random) {
         return ((Integer)this.getFirst()).intValue() >= ((Integer)this.getSecond()).intValue() ? ((Integer)this.getFirst()).intValue() : ((Integer)this.getFirst()).intValue() + random.nextInt(((Integer)this.getSecond()).intValue() - ((Integer)this.getFirst()).intValue() + 1);
      }
   }
}
