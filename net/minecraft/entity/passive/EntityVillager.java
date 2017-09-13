package net.minecraft.entity.passive;

import java.util.List;
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
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
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
import net.minecraftforge.fml.common.registry.VillagerRegistry;
import net.minecraftforge.fml.common.registry.VillagerRegistry.VillagerProfession;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityVillager extends EntityAgeable implements IMerchant, INpc {
   private static final DataParameter PROFESSION = EntityDataManager.createKey(EntityVillager.class, DataSerializers.VARINT);
   private static final DataParameter PROFESSION_STR = EntityDataManager.createKey(EntityVillager.class, DataSerializers.STRING);
   private int randomTickDivider;
   private boolean isMating;
   private boolean isPlaying;
   Village villageObj;
   private EntityPlayer buyingPlayer;
   private MerchantRecipeList buyingList;
   private int timeUntilReset;
   private boolean needsInitilization;
   private boolean isWillingToMate;
   private int wealth;
   private String lastBuyingPlayer;
   private int careerId;
   private int careerLevel;
   private boolean isLookingForHome;
   private boolean areAdditionalTasksSet;
   private final InventoryBasic villagerInventory;
   /** @deprecated */
   @Deprecated
   private static final EntityVillager.ITradeList[][][][] DEFAULT_TRADE_LIST_MAP = new EntityVillager.ITradeList[][][][]{{{{new EntityVillager.EmeraldForItems(Items.WHEAT, new EntityVillager.PriceInfo(18, 22)), new EntityVillager.EmeraldForItems(Items.POTATO, new EntityVillager.PriceInfo(15, 19)), new EntityVillager.EmeraldForItems(Items.CARROT, new EntityVillager.PriceInfo(15, 19)), new EntityVillager.ListItemForEmeralds(Items.BREAD, new EntityVillager.PriceInfo(-4, -2))}, {new EntityVillager.EmeraldForItems(Item.getItemFromBlock(Blocks.PUMPKIN), new EntityVillager.PriceInfo(8, 13)), new EntityVillager.ListItemForEmeralds(Items.PUMPKIN_PIE, new EntityVillager.PriceInfo(-3, -2))}, {new EntityVillager.EmeraldForItems(Item.getItemFromBlock(Blocks.MELON_BLOCK), new EntityVillager.PriceInfo(7, 12)), new EntityVillager.ListItemForEmeralds(Items.APPLE, new EntityVillager.PriceInfo(-5, -7))}, {new EntityVillager.ListItemForEmeralds(Items.COOKIE, new EntityVillager.PriceInfo(-6, -10)), new EntityVillager.ListItemForEmeralds(Items.CAKE, new EntityVillager.PriceInfo(1, 1))}}, {{new EntityVillager.EmeraldForItems(Items.STRING, new EntityVillager.PriceInfo(15, 20)), new EntityVillager.EmeraldForItems(Items.COAL, new EntityVillager.PriceInfo(16, 24)), new EntityVillager.ItemAndEmeraldToItem(Items.FISH, new EntityVillager.PriceInfo(6, 6), Items.COOKED_FISH, new EntityVillager.PriceInfo(6, 6))}, {new EntityVillager.ListEnchantedItemForEmeralds(Items.FISHING_ROD, new EntityVillager.PriceInfo(7, 8))}}, {{new EntityVillager.EmeraldForItems(Item.getItemFromBlock(Blocks.WOOL), new EntityVillager.PriceInfo(16, 22)), new EntityVillager.ListItemForEmeralds(Items.SHEARS, new EntityVillager.PriceInfo(3, 4))}, {new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL)), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 1), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 2), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 3), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 4), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 5), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 6), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 7), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 8), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 9), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 10), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 11), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 12), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 13), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 14), new EntityVillager.PriceInfo(1, 2)), new EntityVillager.ListItemForEmeralds(new ItemStack(Item.getItemFromBlock(Blocks.WOOL), 1, 15), new EntityVillager.PriceInfo(1, 2))}}, {{new EntityVillager.EmeraldForItems(Items.STRING, new EntityVillager.PriceInfo(15, 20)), new EntityVillager.ListItemForEmeralds(Items.ARROW, new EntityVillager.PriceInfo(-12, -8))}, {new EntityVillager.ListItemForEmeralds(Items.BOW, new EntityVillager.PriceInfo(2, 3)), new EntityVillager.ItemAndEmeraldToItem(Item.getItemFromBlock(Blocks.GRAVEL), new EntityVillager.PriceInfo(10, 10), Items.FLINT, new EntityVillager.PriceInfo(6, 10))}}}, {{{new EntityVillager.EmeraldForItems(Items.PAPER, new EntityVillager.PriceInfo(24, 36)), new EntityVillager.ListEnchantedBookForEmeralds()}, {new EntityVillager.EmeraldForItems(Items.BOOK, new EntityVillager.PriceInfo(8, 10)), new EntityVillager.ListItemForEmeralds(Items.COMPASS, new EntityVillager.PriceInfo(10, 12)), new EntityVillager.ListItemForEmeralds(Item.getItemFromBlock(Blocks.BOOKSHELF), new EntityVillager.PriceInfo(3, 4))}, {new EntityVillager.EmeraldForItems(Items.WRITTEN_BOOK, new EntityVillager.PriceInfo(2, 2)), new EntityVillager.ListItemForEmeralds(Items.CLOCK, new EntityVillager.PriceInfo(10, 12)), new EntityVillager.ListItemForEmeralds(Item.getItemFromBlock(Blocks.GLASS), new EntityVillager.PriceInfo(-5, -3))}, {new EntityVillager.ListEnchantedBookForEmeralds()}, {new EntityVillager.ListEnchantedBookForEmeralds()}, {new EntityVillager.ListItemForEmeralds(Items.NAME_TAG, new EntityVillager.PriceInfo(20, 22))}}}, {{{new EntityVillager.EmeraldForItems(Items.ROTTEN_FLESH, new EntityVillager.PriceInfo(36, 40)), new EntityVillager.EmeraldForItems(Items.GOLD_INGOT, new EntityVillager.PriceInfo(8, 10))}, {new EntityVillager.ListItemForEmeralds(Items.REDSTONE, new EntityVillager.PriceInfo(-4, -1)), new EntityVillager.ListItemForEmeralds(new ItemStack(Items.DYE, 1, EnumDyeColor.BLUE.getDyeDamage()), new EntityVillager.PriceInfo(-2, -1))}, {new EntityVillager.ListItemForEmeralds(Items.ENDER_PEARL, new EntityVillager.PriceInfo(4, 7)), new EntityVillager.ListItemForEmeralds(Item.getItemFromBlock(Blocks.GLOWSTONE), new EntityVillager.PriceInfo(-3, -1))}, {new EntityVillager.ListItemForEmeralds(Items.EXPERIENCE_BOTTLE, new EntityVillager.PriceInfo(3, 11))}}}, {{{new EntityVillager.EmeraldForItems(Items.COAL, new EntityVillager.PriceInfo(16, 24)), new EntityVillager.ListItemForEmeralds(Items.IRON_HELMET, new EntityVillager.PriceInfo(4, 6))}, {new EntityVillager.EmeraldForItems(Items.IRON_INGOT, new EntityVillager.PriceInfo(7, 9)), new EntityVillager.ListItemForEmeralds(Items.IRON_CHESTPLATE, new EntityVillager.PriceInfo(10, 14))}, {new EntityVillager.EmeraldForItems(Items.DIAMOND, new EntityVillager.PriceInfo(3, 4)), new EntityVillager.ListEnchantedItemForEmeralds(Items.DIAMOND_CHESTPLATE, new EntityVillager.PriceInfo(16, 19))}, {new EntityVillager.ListItemForEmeralds(Items.CHAINMAIL_BOOTS, new EntityVillager.PriceInfo(5, 7)), new EntityVillager.ListItemForEmeralds(Items.CHAINMAIL_LEGGINGS, new EntityVillager.PriceInfo(9, 11)), new EntityVillager.ListItemForEmeralds(Items.CHAINMAIL_HELMET, new EntityVillager.PriceInfo(5, 7)), new EntityVillager.ListItemForEmeralds(Items.CHAINMAIL_CHESTPLATE, new EntityVillager.PriceInfo(11, 15))}}, {{new EntityVillager.EmeraldForItems(Items.COAL, new EntityVillager.PriceInfo(16, 24)), new EntityVillager.ListItemForEmeralds(Items.IRON_AXE, new EntityVillager.PriceInfo(6, 8))}, {new EntityVillager.EmeraldForItems(Items.IRON_INGOT, new EntityVillager.PriceInfo(7, 9)), new EntityVillager.ListEnchantedItemForEmeralds(Items.IRON_SWORD, new EntityVillager.PriceInfo(9, 10))}, {new EntityVillager.EmeraldForItems(Items.DIAMOND, new EntityVillager.PriceInfo(3, 4)), new EntityVillager.ListEnchantedItemForEmeralds(Items.DIAMOND_SWORD, new EntityVillager.PriceInfo(12, 15)), new EntityVillager.ListEnchantedItemForEmeralds(Items.DIAMOND_AXE, new EntityVillager.PriceInfo(9, 12))}}, {{new EntityVillager.EmeraldForItems(Items.COAL, new EntityVillager.PriceInfo(16, 24)), new EntityVillager.ListEnchantedItemForEmeralds(Items.IRON_SHOVEL, new EntityVillager.PriceInfo(5, 7))}, {new EntityVillager.EmeraldForItems(Items.IRON_INGOT, new EntityVillager.PriceInfo(7, 9)), new EntityVillager.ListEnchantedItemForEmeralds(Items.IRON_PICKAXE, new EntityVillager.PriceInfo(9, 11))}, {new EntityVillager.EmeraldForItems(Items.DIAMOND, new EntityVillager.PriceInfo(3, 4)), new EntityVillager.ListEnchantedItemForEmeralds(Items.DIAMOND_PICKAXE, new EntityVillager.PriceInfo(12, 15))}}}, {{{new EntityVillager.EmeraldForItems(Items.PORKCHOP, new EntityVillager.PriceInfo(14, 18)), new EntityVillager.EmeraldForItems(Items.CHICKEN, new EntityVillager.PriceInfo(14, 18))}, {new EntityVillager.EmeraldForItems(Items.COAL, new EntityVillager.PriceInfo(16, 24)), new EntityVillager.ListItemForEmeralds(Items.COOKED_PORKCHOP, new EntityVillager.PriceInfo(-7, -5)), new EntityVillager.ListItemForEmeralds(Items.COOKED_CHICKEN, new EntityVillager.PriceInfo(-8, -6))}}, {{new EntityVillager.EmeraldForItems(Items.LEATHER, new EntityVillager.PriceInfo(9, 12)), new EntityVillager.ListItemForEmeralds(Items.LEATHER_LEGGINGS, new EntityVillager.PriceInfo(2, 4))}, {new EntityVillager.ListEnchantedItemForEmeralds(Items.LEATHER_CHESTPLATE, new EntityVillager.PriceInfo(7, 12))}, {new EntityVillager.ListItemForEmeralds(Items.SADDLE, new EntityVillager.PriceInfo(8, 10))}}}};
   private VillagerProfession prof;

   public EntityVillager(World var1) {
      this(worldIn, 0);
   }

   public EntityVillager(World var1, int var2) {
      super(worldIn);
      this.villagerInventory = new InventoryBasic("Items", false, 8);
      this.setProfession(professionId);
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
         BlockPos blockpos = new BlockPos(this);
         this.world.getVillageCollection().addToVillagerPositionList(blockpos);
         this.randomTickDivider = 70 + this.rand.nextInt(50);
         this.villageObj = this.world.getVillageCollection().getNearestVillage(blockpos, 32);
         if (this.villageObj == null) {
            this.detachHome();
         } else {
            BlockPos blockpos1 = this.villageObj.getCenter();
            this.setHomePosAndDistance(blockpos1, this.villageObj.getVillageRadius());
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
                     merchantrecipe.increaseMaxTradeUses(this.rand.nextInt(6) + this.rand.nextInt(6) + 2);
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
      boolean flag = stack != null && stack.getItem() == Items.SPAWN_EGG;
      if (!flag && this.isEntityAlive() && !this.isTrading() && !this.isChild() && !player.isSneaking()) {
         if (!this.world.isRemote && (this.buyingList == null || !this.buyingList.isEmpty())) {
            this.setCustomer(player);
            player.displayVillagerTradeGui(this);
         }

         player.addStat(StatList.TALKED_TO_VILLAGER);
         return true;
      } else {
         return super.processInteract(player, hand, stack);
      }
   }

   protected void entityInit() {
      super.entityInit();
      this.dataManager.register(PROFESSION, Integer.valueOf(0));
      this.dataManager.register(PROFESSION_STR, "minecraft:farmer");
   }

   public static void registerFixesVillager(DataFixer var0) {
      EntityLiving.registerFixesMob(fixer, "Villager");
      fixer.registerWalker(FixTypes.ENTITY, new ItemStackDataLists("Villager", new String[]{"Inventory"}));
      fixer.registerWalker(FixTypes.ENTITY, new IDataWalker() {
         public NBTTagCompound process(IDataFixer var1, NBTTagCompound var2, int var3) {
            if ("Villager".equals(compound.getString("id")) && compound.hasKey("Offers", 10)) {
               NBTTagCompound nbttagcompound = compound.getCompoundTag("Offers");
               if (nbttagcompound.hasKey("Recipes", 9)) {
                  NBTTagList nbttaglist = nbttagcompound.getTagList("Recipes", 10);

                  for(int i = 0; i < nbttaglist.tagCount(); ++i) {
                     NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
                     DataFixesManager.processItemStack(fixer, nbttagcompound1, versionIn, "buy");
                     DataFixesManager.processItemStack(fixer, nbttagcompound1, versionIn, "buyB");
                     DataFixesManager.processItemStack(fixer, nbttagcompound1, versionIn, "sell");
                     nbttaglist.set(i, nbttagcompound1);
                  }
               }
            }

            return compound;
         }
      });
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(compound);
      compound.setInteger("Profession", this.getProfession());
      compound.setString("ProfessionName", this.getProfessionForge().getRegistryName().toString());
      compound.setInteger("Riches", this.wealth);
      compound.setInteger("Career", this.careerId);
      compound.setInteger("CareerLevel", this.careerLevel);
      compound.setBoolean("Willing", this.isWillingToMate);
      if (this.buyingList != null) {
         compound.setTag("Offers", this.buyingList.getRecipiesAsTags());
      }

      NBTTagList nbttaglist = new NBTTagList();

      for(int i = 0; i < this.villagerInventory.getSizeInventory(); ++i) {
         ItemStack itemstack = this.villagerInventory.getStackInSlot(i);
         if (itemstack != null) {
            nbttaglist.appendTag(itemstack.writeToNBT(new NBTTagCompound()));
         }
      }

      compound.setTag("Inventory", nbttaglist);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(compound);
      this.setProfession(compound.getInteger("Profession"));
      if (compound.hasKey("ProfessionName")) {
         VillagerProfession p = (VillagerProfession)VillagerRegistry.instance().getRegistry().getValue(new ResourceLocation(compound.getString("ProfessionName")));
         if (p == null) {
            p = (VillagerProfession)VillagerRegistry.instance().getRegistry().getValue(new ResourceLocation("minecraft:farmer"));
         }

         this.setProfession(p);
      }

      this.wealth = compound.getInteger("Riches");
      this.careerId = compound.getInteger("Career");
      this.careerLevel = compound.getInteger("CareerLevel");
      this.isWillingToMate = compound.getBoolean("Willing");
      if (compound.hasKey("Offers", 10)) {
         NBTTagCompound nbttagcompound = compound.getCompoundTag("Offers");
         this.buyingList = new MerchantRecipeList(nbttagcompound);
      }

      NBTTagList nbttaglist = compound.getTagList("Inventory", 10);

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

   public void setProfession(int var1) {
      this.dataManager.set(PROFESSION, Integer.valueOf(professionId));
      VillagerRegistry.onSetProfession(this, professionId);
   }

   /** @deprecated */
   @Deprecated
   public int getProfession() {
      return Math.max(((Integer)this.dataManager.get(PROFESSION)).intValue() % 5, 0);
   }

   public void setProfession(VillagerProfession var1) {
      this.dataManager.set(PROFESSION_STR, prof.getRegistryName().toString());
      this.prof = prof;
      VillagerRegistry.onSetProfession(this, prof);
   }

   public VillagerProfession getProfessionForge() {
      if (this.prof == null) {
         String p = (String)this.dataManager.get(PROFESSION_STR);
         ResourceLocation res = new ResourceLocation(p == null ? "minecraft:farmer" : p);
         this.prof = (VillagerProfession)VillagerRegistry.instance().getRegistry().getValue(res);
         if (this.prof == null) {
            return (VillagerProfession)VillagerRegistry.instance().getRegistry().getValue(new ResourceLocation("minecraft:farmer"));
         }
      }

      return this.prof;
   }

   public void notifyDataManagerChange(DataParameter var1) {
      super.notifyDataManagerChange(key);
      if (key.equals(PROFESSION_STR)) {
         String p = (String)this.dataManager.get(PROFESSION_STR);
         ResourceLocation res = new ResourceLocation(p == null ? "minecraft:farmer" : p);
         this.prof = (VillagerProfession)VillagerRegistry.instance().getRegistry().getValue(res);
      } else if (key.equals(PROFESSION)) {
         VillagerRegistry.onSetProfession(this, ((Integer)this.dataManager.get(PROFESSION)).intValue());
      }

   }

   public boolean isMating() {
      return this.isMating;
   }

   public void setMating(boolean var1) {
      this.isMating = mating;
   }

   public void setPlaying(boolean var1) {
      this.isPlaying = playing;
   }

   public boolean isPlaying() {
      return this.isPlaying;
   }

   public void setRevengeTarget(@Nullable EntityLivingBase var1) {
      super.setRevengeTarget(livingBase);
      if (this.villageObj != null && livingBase != null) {
         this.villageObj.addOrRenewAgressor(livingBase);
         if (livingBase instanceof EntityPlayer) {
            int i = -1;
            if (this.isChild()) {
               i = -3;
            }

            this.villageObj.modifyPlayerReputation(livingBase.getName(), i);
            if (this.isEntityAlive()) {
               this.world.setEntityState(this, (byte)13);
            }
         }
      }

   }

   public void onDeath(DamageSource var1) {
      if (this.villageObj != null) {
         Entity entity = cause.getEntity();
         if (entity != null) {
            if (entity instanceof EntityPlayer) {
               this.villageObj.modifyPlayerReputation(entity.getName(), -2);
            } else if (entity instanceof IMob) {
               this.villageObj.endMatingSeason();
            }
         } else {
            EntityPlayer entityplayer = this.world.getClosestPlayerToEntity(this, 16.0D);
            if (entityplayer != null) {
               this.villageObj.endMatingSeason();
            }
         }
      }

      super.onDeath(cause);
   }

   public void setCustomer(EntityPlayer var1) {
      this.buyingPlayer = player;
   }

   public EntityPlayer getCustomer() {
      return this.buyingPlayer;
   }

   public boolean isTrading() {
      return this.buyingPlayer != null;
   }

   public boolean getIsWillingToMate(boolean var1) {
      if (!this.isWillingToMate && updateFirst && this.hasEnoughFoodToBreed()) {
         boolean flag = false;

         for(int i = 0; i < this.villagerInventory.getSizeInventory(); ++i) {
            ItemStack itemstack = this.villagerInventory.getStackInSlot(i);
            if (itemstack != null) {
               if (itemstack.getItem() == Items.BREAD && itemstack.stackSize >= 3) {
                  flag = true;
                  this.villagerInventory.decrStackSize(i, 3);
               } else if ((itemstack.getItem() == Items.POTATO || itemstack.getItem() == Items.CARROT) && itemstack.stackSize >= 12) {
                  flag = true;
                  this.villagerInventory.decrStackSize(i, 12);
               }
            }

            if (flag) {
               this.world.setEntityState(this, (byte)18);
               this.isWillingToMate = true;
               break;
            }
         }
      }

      return this.isWillingToMate;
   }

   public void setIsWillingToMate(boolean var1) {
      this.isWillingToMate = isWillingToMate;
   }

   public void useRecipe(MerchantRecipe var1) {
      recipe.incrementToolUses();
      this.livingSoundTime = -this.getTalkInterval();
      this.playSound(SoundEvents.ENTITY_VILLAGER_YES, this.getSoundVolume(), this.getSoundPitch());
      int i = 3 + this.rand.nextInt(4);
      if (recipe.getToolUses() == 1 || this.rand.nextInt(5) == 0) {
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

      if (recipe.getItemToBuy().getItem() == Items.EMERALD) {
         this.wealth += recipe.getItemToBuy().stackSize;
      }

      if (recipe.getRewardsExp()) {
         this.world.spawnEntity(new EntityXPOrb(this.world, this.posX, this.posY + 0.5D, this.posZ, i));
      }

   }

   public void verifySellingItem(ItemStack var1) {
      if (!this.world.isRemote && this.livingSoundTime > -this.getTalkInterval() + 20) {
         this.livingSoundTime = -this.getTalkInterval();
         if (stack != null) {
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
      if (this.careerId != 0 && this.careerLevel != 0) {
         ++this.careerLevel;
      } else {
         this.careerId = this.getProfessionForge().getRandomCareer(this.rand) + 1;
         this.careerLevel = 1;
      }

      if (this.buyingList == null) {
         this.buyingList = new MerchantRecipeList();
      }

      int i = this.careerId - 1;
      int j = this.careerLevel - 1;
      List trades = this.getProfessionForge().getCareer(i).getTrades(j);
      if (trades != null) {
         for(EntityVillager.ITradeList entityvillager$itradelist : trades) {
            entityvillager$itradelist.modifyMerchantRecipeList(this.buyingList, this.rand);
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public void setRecipes(MerchantRecipeList var1) {
   }

   public ITextComponent getDisplayName() {
      Team team = this.getTeam();
      String s = this.getCustomNameTag();
      if (s != null && !s.isEmpty()) {
         TextComponentString textcomponentstring = new TextComponentString(ScorePlayerTeam.formatPlayerName(team, s));
         textcomponentstring.getStyle().setHoverEvent(this.getHoverEvent());
         textcomponentstring.getStyle().setInsertion(this.getCachedUniqueIdString());
         return textcomponentstring;
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

         s1 = "entity.Villager." + this.getProfessionForge().getCareer(this.careerId - 1).getName();
         TextComponentTranslation textcomponenttranslation = new TextComponentTranslation(s1, new Object[0]);
         textcomponenttranslation.getStyle().setHoverEvent(this.getHoverEvent());
         textcomponenttranslation.getStyle().setInsertion(this.getCachedUniqueIdString());
         if (team != null) {
            textcomponenttranslation.getStyle().setColor(team.getChatFormat());
         }

         return textcomponenttranslation;
      }
   }

   public float getEyeHeight() {
      return this.isChild() ? 0.81F : 1.62F;
   }

   @SideOnly(Side.CLIENT)
   public void handleStatusUpdate(byte var1) {
      if (id == 12) {
         this.spawnParticles(EnumParticleTypes.HEART);
      } else if (id == 13) {
         this.spawnParticles(EnumParticleTypes.VILLAGER_ANGRY);
      } else if (id == 14) {
         this.spawnParticles(EnumParticleTypes.VILLAGER_HAPPY);
      } else {
         super.handleStatusUpdate(id);
      }

   }

   @SideOnly(Side.CLIENT)
   private void spawnParticles(EnumParticleTypes var1) {
      for(int i = 0; i < 5; ++i) {
         double d0 = this.rand.nextGaussian() * 0.02D;
         double d1 = this.rand.nextGaussian() * 0.02D;
         double d2 = this.rand.nextGaussian() * 0.02D;
         this.world.spawnParticle(particleType, this.posX + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, this.posY + 1.0D + (double)(this.rand.nextFloat() * this.height), this.posZ + (double)(this.rand.nextFloat() * this.width * 2.0F) - (double)this.width, d0, d1, d2);
      }

   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance var1, @Nullable IEntityLivingData var2) {
      livingdata = super.onInitialSpawn(difficulty, livingdata);
      VillagerRegistry.setRandomProfession(this, this.world.rand);
      this.setAdditionalAItasks();
      return livingdata;
   }

   public void setLookingForHome() {
      this.isLookingForHome = true;
   }

   public EntityVillager createChild(EntityAgeable var1) {
      EntityVillager entityvillager = new EntityVillager(this.world);
      entityvillager.onInitialSpawn(this.world.getDifficultyForLocation(new BlockPos(entityvillager)), (IEntityLivingData)null);
      return entityvillager;
   }

   public boolean canBeLeashedTo(EntityPlayer var1) {
      return false;
   }

   public void onStruckByLightning(EntityLightningBolt var1) {
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

   protected void updateEquipmentIfNeeded(EntityItem var1) {
      ItemStack itemstack = itemEntity.getEntityItem();
      Item item = itemstack.getItem();
      if (this.canVillagerPickupItem(item)) {
         ItemStack itemstack1 = this.villagerInventory.addItem(itemstack);
         if (itemstack1 == null) {
            itemEntity.setDead();
         } else {
            itemstack.stackSize = itemstack1.stackSize;
         }
      }

   }

   private boolean canVillagerPickupItem(Item var1) {
      return itemIn == Items.BREAD || itemIn == Items.POTATO || itemIn == Items.CARROT || itemIn == Items.WHEAT || itemIn == Items.WHEAT_SEEDS || itemIn == Items.BEETROOT || itemIn == Items.BEETROOT_SEEDS;
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

   private boolean hasEnoughItems(int var1) {
      boolean flag = this.getProfession() == 0;

      for(int i = 0; i < this.villagerInventory.getSizeInventory(); ++i) {
         ItemStack itemstack = this.villagerInventory.getStackInSlot(i);
         if (itemstack != null) {
            if (itemstack.getItem() == Items.BREAD && itemstack.stackSize >= 3 * multiplier || itemstack.getItem() == Items.POTATO && itemstack.stackSize >= 12 * multiplier || itemstack.getItem() == Items.CARROT && itemstack.stackSize >= 12 * multiplier || itemstack.getItem() == Items.BEETROOT && itemstack.stackSize >= 12 * multiplier) {
               return true;
            }

            if (flag && itemstack.getItem() == Items.WHEAT && itemstack.stackSize >= 9 * multiplier) {
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

   public boolean replaceItemInInventory(int var1, @Nullable ItemStack var2) {
      if (super.replaceItemInInventory(inventorySlot, itemStackIn)) {
         return true;
      } else {
         int i = inventorySlot - 300;
         if (i >= 0 && i < this.villagerInventory.getSizeInventory()) {
            this.villagerInventory.setInventorySlotContents(i, itemStackIn);
            return true;
         } else {
            return false;
         }
      }
   }

   public static EntityVillager.ITradeList[][][][] GET_TRADES_DONT_USE() {
      return DEFAULT_TRADE_LIST_MAP;
   }

   public static class EmeraldForItems implements EntityVillager.ITradeList {
      public Item buyingItem;
      public EntityVillager.PriceInfo price;

      public EmeraldForItems(Item var1, EntityVillager.PriceInfo var2) {
         this.buyingItem = itemIn;
         this.price = priceIn;
      }

      public void modifyMerchantRecipeList(MerchantRecipeList var1, Random var2) {
         int i = 1;
         if (this.price != null) {
            i = this.price.getPrice(random);
         }

         recipeList.add(new MerchantRecipe(new ItemStack(this.buyingItem, i, 0), Items.EMERALD));
      }
   }

   public interface ITradeList {
      void modifyMerchantRecipeList(MerchantRecipeList var1, Random var2);
   }

   public static class ItemAndEmeraldToItem implements EntityVillager.ITradeList {
      public ItemStack buyingItemStack;
      public EntityVillager.PriceInfo buyingPriceInfo;
      public ItemStack sellingItemstack;
      public EntityVillager.PriceInfo sellingPriceInfo;

      public ItemAndEmeraldToItem(Item var1, EntityVillager.PriceInfo var2, Item var3, EntityVillager.PriceInfo var4) {
         this.buyingItemStack = new ItemStack(p_i45813_1_);
         this.buyingPriceInfo = p_i45813_2_;
         this.sellingItemstack = new ItemStack(p_i45813_3_);
         this.sellingPriceInfo = p_i45813_4_;
      }

      public void modifyMerchantRecipeList(MerchantRecipeList var1, Random var2) {
         int i = 1;
         if (this.buyingPriceInfo != null) {
            i = this.buyingPriceInfo.getPrice(random);
         }

         int j = 1;
         if (this.sellingPriceInfo != null) {
            j = this.sellingPriceInfo.getPrice(random);
         }

         recipeList.add(new MerchantRecipe(new ItemStack(this.buyingItemStack.getItem(), i, this.buyingItemStack.getMetadata()), new ItemStack(Items.EMERALD), new ItemStack(this.sellingItemstack.getItem(), j, this.sellingItemstack.getMetadata())));
      }
   }

   public static class ListEnchantedBookForEmeralds implements EntityVillager.ITradeList {
      public void modifyMerchantRecipeList(MerchantRecipeList var1, Random var2) {
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

         recipeList.add(new MerchantRecipe(new ItemStack(Items.BOOK), new ItemStack(Items.EMERALD, j), itemstack));
      }
   }

   public static class ListEnchantedItemForEmeralds implements EntityVillager.ITradeList {
      public ItemStack enchantedItemStack;
      public EntityVillager.PriceInfo priceInfo;

      public ListEnchantedItemForEmeralds(Item var1, EntityVillager.PriceInfo var2) {
         this.enchantedItemStack = new ItemStack(p_i45814_1_);
         this.priceInfo = p_i45814_2_;
      }

      public void modifyMerchantRecipeList(MerchantRecipeList var1, Random var2) {
         int i = 1;
         if (this.priceInfo != null) {
            i = this.priceInfo.getPrice(random);
         }

         ItemStack itemstack = new ItemStack(Items.EMERALD, i, 0);
         ItemStack itemstack1 = new ItemStack(this.enchantedItemStack.getItem(), 1, this.enchantedItemStack.getMetadata());
         itemstack1 = EnchantmentHelper.addRandomEnchantment(random, itemstack1, 5 + random.nextInt(15), false);
         recipeList.add(new MerchantRecipe(itemstack, itemstack1));
      }
   }

   public static class ListItemForEmeralds implements EntityVillager.ITradeList {
      public ItemStack itemToBuy;
      public EntityVillager.PriceInfo priceInfo;

      public ListItemForEmeralds(Item var1, EntityVillager.PriceInfo var2) {
         this.itemToBuy = new ItemStack(par1Item);
         this.priceInfo = priceInfo;
      }

      public ListItemForEmeralds(ItemStack var1, EntityVillager.PriceInfo var2) {
         this.itemToBuy = stack;
         this.priceInfo = priceInfo;
      }

      public void modifyMerchantRecipeList(MerchantRecipeList var1, Random var2) {
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

         recipeList.add(new MerchantRecipe(itemstack, itemstack1));
      }
   }

   public static class PriceInfo extends Tuple {
      public PriceInfo(int var1, int var2) {
         super(Integer.valueOf(p_i45810_1_), Integer.valueOf(p_i45810_2_));
      }

      public int getPrice(Random var1) {
         return ((Integer)this.getFirst()).intValue() >= ((Integer)this.getSecond()).intValue() ? ((Integer)this.getFirst()).intValue() : ((Integer)this.getFirst()).intValue() + rand.nextInt(((Integer)this.getSecond()).intValue() - ((Integer)this.getFirst()).intValue() + 1);
      }
   }
}
