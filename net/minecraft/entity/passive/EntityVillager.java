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

   public EntityVillager(World var1) {
      this(var1, 0);
   }

   public EntityVillager(World var1, int var2) {
      super(var1);
      this.villagerInventory = new InventoryBasic("Items", false, 8, (CraftVillager)this.getBukkitEntity());
      this.setProfession(var2);
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
         BlockPos var1 = new BlockPos(this);
         this.world.getVillageCollection().addToVillagerPositionList(var1);
         this.randomTickDivider = 70 + this.rand.nextInt(50);
         this.villageObj = this.world.getVillageCollection().getNearestVillage(var1, 32);
         if (this.villageObj == null) {
            this.detachHome();
         } else {
            BlockPos var2 = this.villageObj.getCenter();
            this.setHomePosAndDistance(var2, this.villageObj.getVillageRadius());
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
               for(MerchantRecipe var6 : this.buyingList) {
                  if (var6.isRecipeDisabled()) {
                     int var3 = this.rand.nextInt(6) + this.rand.nextInt(6) + 2;
                     VillagerReplenishTradeEvent var4 = new VillagerReplenishTradeEvent((Villager)this.getBukkitEntity(), var6.asBukkit(), var3);
                     Bukkit.getPluginManager().callEvent(var4);
                     if (!var4.isCancelled()) {
                        var6.increaseMaxTradeUses(var4.getBonus());
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

   public boolean processInteract(EntityPlayer var1, EnumHand var2, @Nullable ItemStack var3) {
      boolean var4 = var3 != null && var3.getItem() == Items.SPAWN_EGG;
      if (!var4 && this.isEntityAlive() && !this.isTrading() && !this.isChild()) {
         if (!this.world.isRemote && (this.buyingList == null || !this.buyingList.isEmpty())) {
            this.setCustomer(var1);
            var1.displayVillagerTradeGui(this);
         }

         var1.addStat(StatList.TALKED_TO_VILLAGER);
         return true;
      } else {
         return super.processInteract(var1, var2, var3);
      }
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(PROFESSION, Integer.valueOf(0));
   }

   public static void registerFixesVillager(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "Villager");
      var0.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Villager", new String[]{"Inventory"}));
      var0.registerWalker(FixTypes.ENTITY, new IDataWalker() {
         public NBTTagCompound process(IDataFixer var1, NBTTagCompound var2, int var3) {
            if ("Villager".equals(var2.getString("id")) && var2.hasKey("Offers", 10)) {
               NBTTagCompound var4 = var2.getCompoundTag("Offers");
               if (var4.hasKey("Recipes", 9)) {
                  NBTTagList var5 = var4.getTagList("Recipes", 10);

                  for(int var6 = 0; var6 < var5.tagCount(); ++var6) {
                     NBTTagCompound var7 = var5.getCompoundTagAt(var6);
                     DataFixesManager.processItemStack(var1, var7, var3, "buy");
                     DataFixesManager.processItemStack(var1, var7, var3, "buyB");
                     DataFixesManager.processItemStack(var1, var7, var3, "sell");
                     var5.set(var6, var7);
                  }
               }
            }

            return var2;
         }
      });
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(var1);
      var1.setInteger("Profession", this.getProfession());
      var1.setInteger("Riches", this.wealth);
      var1.setInteger("Career", this.careerId);
      var1.setInteger("CareerLevel", this.careerLevel);
      var1.setBoolean("Willing", this.isWillingToMate);
      if (this.buyingList != null) {
         var1.setTag("Offers", this.buyingList.getRecipiesAsTags());
      }

      NBTTagList var2 = new NBTTagList();

      for(int var3 = 0; var3 < this.villagerInventory.getSizeInventory(); ++var3) {
         ItemStack var4 = this.villagerInventory.getStackInSlot(var3);
         if (var4 != null) {
            var2.appendTag(var4.writeToNBT(new NBTTagCompound()));
         }
      }

      var1.setTag("Inventory", var2);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(var1);
      this.setProfession(var1.getInteger("Profession"));
      this.wealth = var1.getInteger("Riches");
      this.careerId = var1.getInteger("Career");
      this.careerLevel = var1.getInteger("CareerLevel");
      this.isWillingToMate = var1.getBoolean("Willing");
      if (var1.hasKey("Offers", 10)) {
         NBTTagCompound var2 = var1.getCompoundTag("Offers");
         this.buyingList = new MerchantRecipeList(var2);
      }

      NBTTagList var5 = var1.getTagList("Inventory", 10);

      for(int var3 = 0; var3 < var5.tagCount(); ++var3) {
         ItemStack var4 = ItemStack.loadItemStackFromNBT(var5.getCompoundTagAt(var3));
         if (var4 != null) {
            this.villagerInventory.addItem(var4);
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

   public void setProfession(int var1) {
      this.dataManager.set(PROFESSION, Integer.valueOf(var1));
   }

   public int getProfession() {
      return Math.max(((Integer)this.dataManager.get(PROFESSION)).intValue() % 5, 0);
   }

   public boolean isMating() {
      return this.isMating;
   }

   public void setMating(boolean var1) {
      this.isMating = var1;
   }

   public void setPlaying(boolean var1) {
      this.isPlaying = var1;
   }

   public boolean isPlaying() {
      return this.isPlaying;
   }

   public void setRevengeTarget(@Nullable EntityLivingBase var1) {
      super.setRevengeTarget(var1);
      if (this.villageObj != null && var1 != null) {
         this.villageObj.addOrRenewAgressor(var1);
         if (var1 instanceof EntityPlayer) {
            byte var2 = -1;
            if (this.isChild()) {
               var2 = -3;
            }

            this.villageObj.modifyPlayerReputation(var1.getName(), var2);
            if (this.isEntityAlive()) {
               this.world.setEntityState(this, (byte)13);
            }
         }
      }

   }

   public void onDeath(DamageSource var1) {
      if (this.villageObj != null) {
         Entity var2 = var1.getEntity();
         if (var2 != null) {
            if (var2 instanceof EntityPlayer) {
               this.villageObj.modifyPlayerReputation(var2.getName(), -2);
            } else if (var2 instanceof IMob) {
               this.villageObj.endMatingSeason();
            }
         } else {
            EntityPlayer var3 = this.world.getClosestPlayerToEntity(this, 16.0D);
            if (var3 != null) {
               this.villageObj.endMatingSeason();
            }
         }
      }

      super.onDeath(var1);
   }

   public void setCustomer(EntityPlayer var1) {
      this.buyingPlayer = var1;
   }

   public EntityPlayer getCustomer() {
      return this.buyingPlayer;
   }

   public boolean isTrading() {
      return this.buyingPlayer != null;
   }

   public boolean getIsWillingToMate(boolean var1) {
      if (!this.isWillingToMate && var1 && this.hasEnoughFoodToBreed()) {
         boolean var2 = false;

         for(int var3 = 0; var3 < this.villagerInventory.getSizeInventory(); ++var3) {
            ItemStack var4 = this.villagerInventory.getStackInSlot(var3);
            if (var4 != null) {
               if (var4.getItem() == Items.BREAD && var4.stackSize >= 3) {
                  var2 = true;
                  this.villagerInventory.decrStackSize(var3, 3);
               } else if ((var4.getItem() == Items.POTATO || var4.getItem() == Items.CARROT) && var4.stackSize >= 12) {
                  var2 = true;
                  this.villagerInventory.decrStackSize(var3, 12);
               }
            }

            if (var2) {
               this.world.setEntityState(this, (byte)18);
               this.isWillingToMate = true;
               break;
            }
         }
      }

      return this.isWillingToMate;
   }

   public void setIsWillingToMate(boolean var1) {
      this.isWillingToMate = var1;
   }

   public void useRecipe(MerchantRecipe var1) {
      var1.incrementToolUses();
      this.livingSoundTime = -this.getTalkInterval();
      this.playSound(SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
      int var2 = 3 + this.rand.nextInt(4);
      if (var1.getToolUses() == 1 || this.rand.nextInt(5) == 0) {
         this.timeUntilReset = 40;
         this.needsInitilization = true;
         this.isWillingToMate = true;
         if (this.buyingPlayer != null) {
            this.lastBuyingPlayer = this.buyingPlayer.getName();
         } else {
            this.lastBuyingPlayer = null;
         }

         var2 += 5;
      }

      if (var1.getItemToBuy().getItem() == Items.EMERALD) {
         this.wealth += var1.getItemToBuy().stackSize;
      }

      if (var1.getRewardsExp()) {
         this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY + 0.5D, this.posZ, var2));
      }

   }

   public void verifySellingItem(ItemStack var1) {
      if (!this.world.isRemote && this.livingSoundTime > -this.getTalkInterval() + 20) {
         this.livingSoundTime = -this.getTalkInterval();
         if (var1 != null) {
            this.playSound(SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
         } else {
            this.playSound(SoundEvents.ENTITY_VILLAGER_NO, this.getSoundVolume(), this.getSoundPitch());
         }
      }

   }

   public MerchantRecipeList getRecipes(EntityPlayer var1) {
      if (this.buyingList == null) {
         this.populateBuyingList();
      }

      return this.buyingList;
   }

   private void populateBuyingList() {
      EntityVillager.ITradeList[][][] var1 = DEFAULT_TRADE_LIST_MAP[this.getProfession()];
      if (this.careerId != 0 && this.careerLevel != 0) {
         ++this.careerLevel;
      } else {
         this.careerId = this.rand.nextInt(var1.length) + 1;
         this.careerLevel = 1;
      }

      if (this.buyingList == null) {
         this.buyingList = new MerchantRecipeList();
      }

      int var2 = this.careerId - 1;
      int var3 = this.careerLevel - 1;
      EntityVillager.ITradeList[][] var4 = var1[var2];
      if (var3 >= 0 && var3 < var4.length) {
         EntityVillager.ITradeList[] var5 = var4[var3];

         for(EntityVillager.ITradeList var9 : var5) {
            MerchantRecipeList var10 = new MerchantRecipeList();
            var9.modifyMerchantRecipeList(var10, this.rand);

            for(MerchantRecipe var12 : var10) {
               VillagerAcquireTradeEvent var13 = new VillagerAcquireTradeEvent((Villager)this.getBukkitEntity(), var12.asBukkit());
               Bukkit.getPluginManager().callEvent(var13);
               if (!var13.isCancelled()) {
                  this.buyingList.add(CraftMerchantRecipe.fromBukkit(var13.getRecipe()).toMinecraft());
               }
            }
         }
      }

   }

   public ITextComponent getDisplayName() {
      Team var1 = this.getTeam();
      String var2 = this.getCustomNameTag();
      if (var2 != null && !var2.isEmpty()) {
         TextComponentString var5 = new TextComponentString(ScorePlayerTeam.formatPlayerName(var1, var2));
         var5.getStyle().setHoverEvent(this.getHoverEvent());
         var5.getStyle().setInsertion(this.getCachedUniqueIdString());
         return var5;
      } else {
         if (this.buyingList == null) {
            this.populateBuyingList();
         }

         String var3 = null;
         switch(this.getProfession()) {
         case 0:
            if (this.careerId == 1) {
               var3 = "farmer";
            } else if (this.careerId == 2) {
               var3 = "fisherman";
            } else if (this.careerId == 3) {
               var3 = "shepherd";
            } else if (this.careerId == 4) {
               var3 = "fletcher";
            }
            break;
         case 1:
            var3 = "librarian";
            break;
         case 2:
            var3 = "cleric";
            break;
         case 3:
            if (this.careerId == 1) {
               var3 = "armor";
            } else if (this.careerId == 2) {
               var3 = "weapon";
            } else if (this.careerId == 3) {
               var3 = "tool";
            }
            break;
         case 4:
            if (this.careerId == 1) {
               var3 = "butcher";
            } else if (this.careerId == 2) {
               var3 = "leather";
            }
         }

         if (var3 != null) {
            TextComponentTranslation var4 = new TextComponentTranslation("entity.Villager." + var3, new Object[0]);
            var4.getStyle().setHoverEvent(this.getHoverEvent());
            var4.getStyle().setInsertion(this.getCachedUniqueIdString());
            if (var1 != null) {
               var4.getStyle().setColor(var1.getChatFormat());
            }

            return var4;
         } else {
            return super.getDisplayName();
         }
      }
   }

   public float getEyeHeight() {
      return this.isChild() ? 0.81F : 1.62F;
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance var1, @Nullable IEntityLivingData var2) {
      var2 = super.onInitialSpawn(var1, var2);
      this.setProfession(this.world.rand.nextInt(5));
      this.setAdditionalAItasks();
      return var2;
   }

   public void setLookingForHome() {
      this.isLookingForHome = true;
   }

   public EntityVillager createChild(EntityAgeable var1) {
      EntityVillager var2 = new EntityVillager(this.world);
      var2.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(var2)), (IEntityLivingData)null);
      return var2;
   }

   public boolean canBeLeashedTo(EntityPlayer var1) {
      return false;
   }

   public void onStruckByLightning(EntityLightningBolt var1) {
      if (!this.world.isRemote && !this.isDead) {
         EntityWitch var2 = new EntityWitch(this.world);
         var2.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
         var2.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(var2)), (IEntityLivingData)null);
         var2.setNoAI(this.isAIDisabled());
         if (this.hasCustomName()) {
            var2.setCustomNameTag(this.getCustomNameTag());
            var2.setAlwaysRenderNameTag(this.getAlwaysRenderNameTag());
         }

         this.world.spawnEntity(var2);
         this.setDead();
      }

   }

   public InventoryBasic getVillagerInventory() {
      return this.villagerInventory;
   }

   protected void updateEquipmentIfNeeded(EntityItem var1) {
      ItemStack var2 = var1.getEntityItem();
      Item var3 = var2.getItem();
      if (this.canVillagerPickupItem(var3)) {
         ItemStack var4 = this.villagerInventory.addItem(var2);
         if (var4 == null) {
            var1.setDead();
         } else {
            var2.stackSize = var4.stackSize;
         }
      }

   }

   private boolean canVillagerPickupItem(Item var1) {
      return var1 == Items.BREAD || var1 == Items.POTATO || var1 == Items.CARROT || var1 == Items.WHEAT || var1 == Items.WHEAT_SEEDS || var1 == Items.BEETROOT || var1 == Items.BEETROOT_SEEDS;
   }

   public boolean hasEnoughFoodToBreed() {
      return this.hasEnoughItems(1);
   }

   public boolean canAbondonItems() {
      return this.hasEnoughItems(2);
   }

   public boolean wantsMoreFood() {
      boolean var1 = this.getProfession() == 0;
      return var1 ? !this.hasEnoughItems(5) : !this.hasEnoughItems(1);
   }

   private boolean hasEnoughItems(int var1) {
      boolean var2 = this.getProfession() == 0;

      for(int var3 = 0; var3 < this.villagerInventory.getSizeInventory(); ++var3) {
         ItemStack var4 = this.villagerInventory.getStackInSlot(var3);
         if (var4 != null) {
            if (var4.getItem() == Items.BREAD && var4.stackSize >= 3 * var1 || var4.getItem() == Items.POTATO && var4.stackSize >= 12 * var1 || var4.getItem() == Items.CARROT && var4.stackSize >= 12 * var1 || var4.getItem() == Items.BEETROOT && var4.stackSize >= 12 * var1) {
               return true;
            }

            if (var2 && var4.getItem() == Items.WHEAT && var4.stackSize >= 9 * var1) {
               return true;
            }
         }
      }

      return false;
   }

   public boolean isFarmItemInInventory() {
      for(int var1 = 0; var1 < this.villagerInventory.getSizeInventory(); ++var1) {
         ItemStack var2 = this.villagerInventory.getStackInSlot(var1);
         if (var2 != null && (var2.getItem() == Items.WHEAT_SEEDS || var2.getItem() == Items.POTATO || var2.getItem() == Items.CARROT || var2.getItem() == Items.BEETROOT_SEEDS)) {
            return true;
         }
      }

      return false;
   }

   public boolean replaceItemInInventory(int var1, @Nullable ItemStack var2) {
      if (super.replaceItemInInventory(var1, var2)) {
         return true;
      } else {
         int var3 = var1 - 300;
         if (var3 >= 0 && var3 < this.villagerInventory.getSizeInventory()) {
            this.villagerInventory.setInventorySlotContents(var3, var2);
            return true;
         } else {
            return false;
         }
      }
   }

   public EntityAgeable createChild(EntityAgeable var1) {
      return this.createChild(var1);
   }

   static class EmeraldForItems implements EntityVillager.ITradeList {
      public Item buyingItem;
      public EntityVillager.PriceInfo price;

      public EmeraldForItems(Item var1, EntityVillager.PriceInfo var2) {
         this.buyingItem = var1;
         this.price = var2;
      }

      public void modifyMerchantRecipeList(MerchantRecipeList var1, Random var2) {
         int var3 = 1;
         if (this.price != null) {
            var3 = this.price.getPrice(var2);
         }

         var1.add(new MerchantRecipe(new ItemStack(this.buyingItem, var3, 0), Items.EMERALD));
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

      public ItemAndEmeraldToItem(Item var1, EntityVillager.PriceInfo var2, Item var3, EntityVillager.PriceInfo var4) {
         this.buyingItemStack = new ItemStack(var1);
         this.buyingPriceInfo = var2;
         this.sellingItemstack = new ItemStack(var3);
         this.sellingPriceInfo = var4;
      }

      public void modifyMerchantRecipeList(MerchantRecipeList var1, Random var2) {
         int var3 = 1;
         if (this.buyingPriceInfo != null) {
            var3 = this.buyingPriceInfo.getPrice(var2);
         }

         int var4 = 1;
         if (this.sellingPriceInfo != null) {
            var4 = this.sellingPriceInfo.getPrice(var2);
         }

         var1.add(new MerchantRecipe(new ItemStack(this.buyingItemStack.getItem(), var3, this.buyingItemStack.getMetadata()), new ItemStack(Items.EMERALD), new ItemStack(this.sellingItemstack.getItem(), var4, this.sellingItemstack.getMetadata())));
      }
   }

   static class ListEnchantedBookForEmeralds implements EntityVillager.ITradeList {
      public void modifyMerchantRecipeList(MerchantRecipeList var1, Random var2) {
         Enchantment var3 = (Enchantment)Enchantment.REGISTRY.getRandomObject(var2);
         int var4 = MathHelper.getInt(var2, var3.getMinLevel(), var3.getMaxLevel());
         ItemStack var5 = Items.ENCHANTED_BOOK.getEnchantedItemStack(new EnchantmentData(var3, var4));
         int var6 = 2 + var2.nextInt(5 + var4 * 10) + 3 * var4;
         if (var3.isTreasureEnchantment()) {
            var6 *= 2;
         }

         if (var6 > 64) {
            var6 = 64;
         }

         var1.add(new MerchantRecipe(new ItemStack(Items.BOOK), new ItemStack(Items.EMERALD, var6), var5));
      }
   }

   static class ListEnchantedItemForEmeralds implements EntityVillager.ITradeList {
      public ItemStack enchantedItemStack;
      public EntityVillager.PriceInfo priceInfo;

      public ListEnchantedItemForEmeralds(Item var1, EntityVillager.PriceInfo var2) {
         this.enchantedItemStack = new ItemStack(var1);
         this.priceInfo = var2;
      }

      public void modifyMerchantRecipeList(MerchantRecipeList var1, Random var2) {
         int var3 = 1;
         if (this.priceInfo != null) {
            var3 = this.priceInfo.getPrice(var2);
         }

         ItemStack var4 = new ItemStack(Items.EMERALD, var3, 0);
         ItemStack var5 = new ItemStack(this.enchantedItemStack.getItem(), 1, this.enchantedItemStack.getMetadata());
         var5 = EnchantmentHelper.addRandomEnchantment(var2, var5, 5 + var2.nextInt(15), false);
         var1.add(new MerchantRecipe(var4, var5));
      }
   }

   static class ListItemForEmeralds implements EntityVillager.ITradeList {
      public ItemStack itemToBuy;
      public EntityVillager.PriceInfo priceInfo;

      public ListItemForEmeralds(Item var1, EntityVillager.PriceInfo var2) {
         this.itemToBuy = new ItemStack(var1);
         this.priceInfo = var2;
      }

      public ListItemForEmeralds(ItemStack var1, EntityVillager.PriceInfo var2) {
         this.itemToBuy = var1;
         this.priceInfo = var2;
      }

      public void modifyMerchantRecipeList(MerchantRecipeList var1, Random var2) {
         int var3 = 1;
         if (this.priceInfo != null) {
            var3 = this.priceInfo.getPrice(var2);
         }

         ItemStack var4;
         ItemStack var5;
         if (var3 < 0) {
            var4 = new ItemStack(Items.EMERALD);
            var5 = new ItemStack(this.itemToBuy.getItem(), -var3, this.itemToBuy.getMetadata());
         } else {
            var4 = new ItemStack(Items.EMERALD, var3, 0);
            var5 = new ItemStack(this.itemToBuy.getItem(), 1, this.itemToBuy.getMetadata());
         }

         var1.add(new MerchantRecipe(var4, var5));
      }
   }

   static class PriceInfo extends Tuple {
      public PriceInfo(int var1, int var2) {
         super(Integer.valueOf(var1), Integer.valueOf(var2));
      }

      public int getPrice(Random var1) {
         return ((Integer)this.getFirst()).intValue() >= ((Integer)this.getSecond()).intValue() ? ((Integer)this.getFirst()).intValue() : ((Integer)this.getFirst()).intValue() + var1.nextInt(((Integer)this.getSecond()).intValue() - ((Integer)this.getFirst()).intValue() + 1);
      }
   }
}
