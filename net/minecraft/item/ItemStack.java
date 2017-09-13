package net.minecraft.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.BlockMushroom;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.src.MinecraftServer;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.BlockEntityTag;
import net.minecraft.util.datafix.walkers.EntityTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_10_R1.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_10_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.world.StructureGrowEvent;

public final class ItemStack {
   public static final DecimalFormat DECIMALFORMAT = new DecimalFormat("#.##");
   public int stackSize;
   public int animationsToGo;
   private Item item;
   private NBTTagCompound stackTagCompound;
   private int itemDamage;
   private EntityItemFrame itemFrame;
   private Block canDestroyCacheBlock;
   private boolean canDestroyCacheResult;
   private Block canPlaceOnCacheBlock;
   private boolean canPlaceOnCacheResult;

   public ItemStack(Block var1) {
      this(var1, 1);
   }

   public ItemStack(Block var1, int var2) {
      this(var1, var2, 0);
   }

   public ItemStack(Block var1, int var2, int var3) {
      this(Item.getItemFromBlock(var1), var2, var3);
   }

   public ItemStack(Item var1) {
      this(var1, 1);
   }

   public ItemStack(Item var1, int var2) {
      this(var1, var2, 0);
   }

   public ItemStack(Item var1, int var2, int var3) {
      this.item = var1;
      this.stackSize = var2;
      this.setItemDamage(var3);
      if (MinecraftServer.getServer() != null) {
         NBTTagCompound var4 = new NBTTagCompound();
         this.writeToNBT(var4);
         MinecraftServer.getServer().getDataConverterManager().process(FixTypes.ITEM_INSTANCE, var4);
         this.readFromNBT(var4);
      }

   }

   public static ItemStack loadItemStackFromNBT(NBTTagCompound var0) {
      ItemStack var1 = new ItemStack();
      var1.readFromNBT(var0);
      return var1.getItem() != null ? var1 : null;
   }

   private ItemStack() {
   }

   public static void registerFixes(DataFixer var0) {
      var0.registerWalker(FixTypes.ITEM_INSTANCE, new BlockEntityTag());
      var0.registerWalker(FixTypes.ITEM_INSTANCE, new EntityTag());
   }

   public ItemStack splitStack(int var1) {
      var1 = Math.min(var1, this.stackSize);
      ItemStack var2 = new ItemStack(this.item, var1, this.itemDamage);
      if (this.stackTagCompound != null) {
         var2.stackTagCompound = this.stackTagCompound.copy();
      }

      this.stackSize -= var1;
      return var2;
   }

   public Item getItem() {
      return this.item;
   }

   public EnumActionResult onItemUse(EntityPlayer var1, World var2, BlockPos var3, EnumHand var4, EnumFacing var5, float var6, float var7, float var8) {
      int var9 = this.getMetadata();
      int var10 = this.stackSize;
      if (!(this.getItem() instanceof ItemBucket)) {
         var2.captureBlockStates = true;
         if (this.getItem() instanceof ItemDye && this.getMetadata() == 15) {
            Block var11 = var2.getBlockState(var3).getBlock();
            if (var11 == Blocks.SAPLING || var11 instanceof BlockMushroom) {
               var2.captureTreeGeneration = true;
            }
         }
      }

      EnumActionResult var26 = this.getItem().onItemUse(this, var1, var2, var3, var4, var5, var6, var7, var8);
      int var12 = this.getMetadata();
      int var13 = this.stackSize;
      this.stackSize = var10;
      this.setItemDamage(var9);
      var2.captureBlockStates = false;
      if (var26 == EnumActionResult.SUCCESS && var2.captureTreeGeneration && var2.capturedBlockStates.size() > 0) {
         var2.captureTreeGeneration = false;
         Location var27 = new Location(var2.getWorld(), (double)var3.getX(), (double)var3.getY(), (double)var3.getZ());
         TreeType var28 = BlockSapling.treeType;
         BlockSapling.treeType = null;
         List var33 = (List)var2.capturedBlockStates.clone();
         var2.capturedBlockStates.clear();
         StructureGrowEvent var37 = null;
         if (var28 != null) {
            boolean var38 = this.getItem() == Items.DYE && var9 == 15;
            var37 = new StructureGrowEvent(var27, var28, var38, (Player)var1.getBukkitEntity(), var33);
            Bukkit.getPluginManager().callEvent(var37);
         }

         if (var37 == null || !var37.isCancelled()) {
            if (this.stackSize == var10 && this.getMetadata() == var9) {
               this.setItemDamage(var12);
               this.stackSize = var13;
            }

            for(BlockState var39 : var33) {
               var39.update(true);
            }
         }

         return var26;
      } else {
         var2.captureTreeGeneration = false;
         if (var26 == EnumActionResult.SUCCESS) {
            Object var14 = null;
            List var15 = (List)var2.capturedBlockStates.clone();
            var2.capturedBlockStates.clear();
            if (var15.size() > 1) {
               var14 = CraftEventFactory.callBlockMultiPlaceEvent(var2, var1, var4, var15, var3.getX(), var3.getY(), var3.getZ());
            } else if (var15.size() == 1) {
               var14 = CraftEventFactory.callBlockPlaceEvent(var2, var1, var4, (BlockState)var15.get(0), var3.getX(), var3.getY(), var3.getZ());
            }

            if (var14 != null && (((BlockPlaceEvent)var14).isCancelled() || !((BlockPlaceEvent)var14).canBuild())) {
               var26 = EnumActionResult.FAIL;
               ((BlockPlaceEvent)var14).getPlayer().updateInventory();

               for(BlockState var32 : var15) {
                  var32.update(true, false);
               }
            } else {
               if (this.stackSize == var10 && this.getMetadata() == var9) {
                  this.setItemDamage(var12);
                  this.stackSize = var13;
               }

               for(BlockState var16 : var15) {
                  int var18 = var16.getX();
                  int var19 = var16.getY();
                  int var20 = var16.getZ();
                  int var21 = ((CraftBlockState)var16).getFlag();
                  Material var22 = var16.getType();
                  Block var23 = CraftMagicNumbers.getBlock(var22);
                  BlockPos var24 = new BlockPos(var18, var19, var20);
                  IBlockState var25 = var2.getBlockState(var24);
                  if (!(var25 instanceof BlockContainer)) {
                     var25.getBlock().onBlockAdded(var2, var24, var25);
                  }

                  var2.notifyAndUpdatePhysics(var24, (Chunk)null, var23.getDefaultState(), var25, var21);
               }

               for(Entry var29 : var2.capturedTileEntities.entrySet()) {
                  var2.setTileEntity((BlockPos)var29.getKey(), (TileEntity)var29.getValue());
               }

               if (this.getItem() instanceof ItemRecord) {
                  ((BlockJukebox)Blocks.JUKEBOX).insertRecord(var2, var3, var2.getBlockState(var3), this);
                  var2.playEvent((EntityPlayer)null, 1010, var3, Item.getIdFromItem(this.item));
                  --this.stackSize;
                  var1.addStat(StatList.RECORD_PLAYED);
               }

               if (this.getItem() == Items.SKULL) {
                  BlockPos var30 = var3;
                  if (!var2.getBlockState(var3).getBlock().isReplaceable(var2, var3)) {
                     if (!var2.getBlockState(var3).getMaterial().isSolid()) {
                        var30 = null;
                     } else {
                        var30 = var3.offset(var5);
                     }
                  }

                  if (var30 != null) {
                     TileEntity var35 = var2.getTileEntity(var30);
                     if (var35 instanceof TileEntitySkull) {
                        Blocks.SKULL.checkWitherSpawn(var2, var30, (TileEntitySkull)var35);
                     }
                  }
               }

               if (this.getItem() instanceof ItemBlock) {
                  SoundType var31 = ((ItemBlock)this.getItem()).getBlock().getSoundType();
                  var2.playSound(var1, var3, var31.getPlaceSound(), SoundCategory.BLOCKS, (var31.getVolume() + 1.0F) / 2.0F, var31.getPitch() * 0.8F);
               }

               var1.addStat(StatList.getObjectUseStats(this.item));
            }
         }

         var2.capturedTileEntities.clear();
         var2.capturedBlockStates.clear();
         return var26;
      }
   }

   public float getStrVsBlock(IBlockState var1) {
      return this.getItem().getStrVsBlock(this, var1);
   }

   public ActionResult useItemRightClick(World var1, EntityPlayer var2, EnumHand var3) {
      return this.getItem().onItemRightClick(this, var1, var2, var3);
   }

   @Nullable
   public ItemStack onItemUseFinish(World var1, EntityLivingBase var2) {
      return this.getItem().onItemUseFinish(this, var1, var2);
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      ResourceLocation var2 = (ResourceLocation)Item.REGISTRY.getNameForObject(this.item);
      var1.setString("id", var2 == null ? "minecraft:air" : var2.toString());
      var1.setByte("Count", (byte)this.stackSize);
      var1.setShort("Damage", (short)this.itemDamage);
      if (this.stackTagCompound != null) {
         var1.setTag("tag", this.stackTagCompound.copy());
      }

      return var1;
   }

   public void readFromNBT(NBTTagCompound var1) {
      this.item = Item.getByNameOrId(var1.getString("id"));
      this.stackSize = var1.getByte("Count");
      this.setItemDamage(var1.getShort("Damage"));
      if (var1.hasKey("tag", 10)) {
         this.stackTagCompound = (NBTTagCompound)var1.getCompoundTag("tag").copy();
         if (this.item != null) {
            this.item.updateItemStackNBT(this.stackTagCompound);
         }
      }

   }

   public int getMaxStackSize() {
      return this.getItem().getItemStackLimit();
   }

   public boolean isStackable() {
      return this.getMaxStackSize() > 1 && (!this.isItemStackDamageable() || !this.isItemDamaged());
   }

   public boolean isItemStackDamageable() {
      return this.item == null ? false : (this.item.getMaxDamage() <= 0 ? false : !this.hasTagCompound() || !this.getTagCompound().getBoolean("Unbreakable"));
   }

   public boolean getHasSubtypes() {
      return this.item.getHasSubtypes();
   }

   public boolean isItemDamaged() {
      return this.isItemStackDamageable() && this.itemDamage > 0;
   }

   public int getItemDamage() {
      return this.itemDamage;
   }

   public int getMetadata() {
      return this.itemDamage;
   }

   public void setItemDamage(int var1) {
      if (var1 == 32767) {
         this.itemDamage = var1;
      } else {
         if (CraftMagicNumbers.getBlock(CraftMagicNumbers.getId(this.getItem())) != Blocks.AIR && !this.getHasSubtypes() && !this.getItem().isDamageable()) {
            var1 = 0;
         }

         if (CraftMagicNumbers.getBlock(CraftMagicNumbers.getId(this.getItem())) == Blocks.DOUBLE_PLANT && (var1 > 5 || var1 < 0)) {
            var1 = 0;
         }

         this.itemDamage = var1;
      }
   }

   public int getMaxDamage() {
      return this.item == null ? 0 : this.item.getMaxDamage();
   }

   public boolean attemptDamageItem(int var1, Random var2) {
      if (!this.isItemStackDamageable()) {
         return false;
      } else {
         if (var1 > 0) {
            int var3 = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, this);
            int var4 = 0;

            for(int var5 = 0; var3 > 0 && var5 < var1; ++var5) {
               if (EnchantmentDurability.negateDamage(this, var3, var2)) {
                  ++var4;
               }
            }

            var1 -= var4;
            if (var1 <= 0) {
               return false;
            }
         }

         this.itemDamage += var1;
         return this.itemDamage > this.getMaxDamage();
      }
   }

   public void damageItem(int var1, EntityLivingBase var2) {
      if ((!(var2 instanceof EntityPlayer) || !((EntityPlayer)var2).capabilities.isCreativeMode) && this.isItemStackDamageable() && this.attemptDamageItem(var1, var2.getRNG())) {
         var2.renderBrokenItemStack(this);
         --this.stackSize;
         if (var2 instanceof EntityPlayer) {
            EntityPlayer var3 = (EntityPlayer)var2;
            var3.addStat(StatList.getObjectBreakStats(this.item));
         }

         if (this.stackSize < 0) {
            this.stackSize = 0;
         }

         if (this.stackSize == 0 && var2 instanceof EntityPlayer) {
            CraftEventFactory.callPlayerItemBreakEvent((EntityPlayer)var2, this);
         }

         this.itemDamage = 0;
      }

   }

   public void hitEntity(EntityLivingBase var1, EntityPlayer var2) {
      boolean var3 = this.item.hitEntity(this, var1, var2);
      if (var3) {
         var2.addStat(StatList.getObjectUseStats(this.item));
      }

   }

   public void onBlockDestroyed(World var1, IBlockState var2, BlockPos var3, EntityPlayer var4) {
      boolean var5 = this.item.onBlockDestroyed(this, var1, var2, var3, var4);
      if (var5) {
         var4.addStat(StatList.getObjectUseStats(this.item));
      }

   }

   public boolean canHarvestBlock(IBlockState var1) {
      return this.item.canHarvestBlock(var1);
   }

   public boolean interactWithEntity(EntityPlayer var1, EntityLivingBase var2, EnumHand var3) {
      return this.item.itemInteractionForEntity(this, var1, var2, var3);
   }

   public ItemStack copy() {
      ItemStack var1 = new ItemStack(this.item, this.stackSize, this.itemDamage);
      if (this.stackTagCompound != null) {
         var1.stackTagCompound = this.stackTagCompound.copy();
      }

      return var1;
   }

   public static boolean areItemStackTagsEqual(@Nullable ItemStack var0, @Nullable ItemStack var1) {
      return var0 == null && var1 == null ? true : (var0 != null && var1 != null ? (var0.stackTagCompound == null && var1.stackTagCompound != null ? false : var0.stackTagCompound == null || var0.stackTagCompound.equals(var1.stackTagCompound)) : false);
   }

   public static boolean areItemStacksEqual(@Nullable ItemStack var0, @Nullable ItemStack var1) {
      return var0 == null && var1 == null ? true : (var0 != null && var1 != null ? var0.isItemStackEqual(var1) : false);
   }

   private boolean isItemStackEqual(ItemStack var1) {
      return this.stackSize != var1.stackSize ? false : (this.item != var1.item ? false : (this.itemDamage != var1.itemDamage ? false : (this.stackTagCompound == null && var1.stackTagCompound != null ? false : this.stackTagCompound == null || this.stackTagCompound.equals(var1.stackTagCompound))));
   }

   public static boolean areItemsEqual(@Nullable ItemStack var0, @Nullable ItemStack var1) {
      return var0 == var1 ? true : (var0 != null && var1 != null ? var0.isItemEqual(var1) : false);
   }

   public static boolean areItemsEqualIgnoreDurability(@Nullable ItemStack var0, @Nullable ItemStack var1) {
      return var0 == var1 ? true : (var0 != null && var1 != null ? var0.isItemEqualIgnoreDurability(var1) : false);
   }

   public boolean isItemEqual(@Nullable ItemStack var1) {
      return var1 != null && this.item == var1.item && this.itemDamage == var1.itemDamage;
   }

   public boolean isItemEqualIgnoreDurability(@Nullable ItemStack var1) {
      return !this.isItemStackDamageable() ? this.isItemEqual(var1) : var1 != null && this.item == var1.item;
   }

   public String getUnlocalizedName() {
      return this.item.getUnlocalizedName(this);
   }

   public static ItemStack copyItemStack(ItemStack var0) {
      return var0 == null ? null : var0.copy();
   }

   public String toString() {
      return this.stackSize + "x" + this.item.getUnlocalizedName() + "@" + this.itemDamage;
   }

   public void updateAnimation(World var1, Entity var2, int var3, boolean var4) {
      if (this.animationsToGo > 0) {
         --this.animationsToGo;
      }

      if (this.item != null) {
         this.item.onUpdate(this, var1, var2, var3, var4);
      }

   }

   public void onCrafting(World var1, EntityPlayer var2, int var3) {
      var2.addStat(StatList.getCraftStats(this.item), var3);
      this.item.onCreated(this, var1, var2);
   }

   public int getMaxItemUseDuration() {
      return this.getItem().getMaxItemUseDuration(this);
   }

   public EnumAction getItemUseAction() {
      return this.getItem().getItemUseAction(this);
   }

   public void onPlayerStoppedUsing(World var1, EntityLivingBase var2, int var3) {
      this.getItem().onPlayerStoppedUsing(this, var1, var2, var3);
   }

   public boolean hasTagCompound() {
      return this.stackTagCompound != null;
   }

   @Nullable
   public NBTTagCompound getTagCompound() {
      return this.stackTagCompound;
   }

   public NBTTagCompound getSubCompound(String var1, boolean var2) {
      if (this.stackTagCompound != null && this.stackTagCompound.hasKey(var1, 10)) {
         return this.stackTagCompound.getCompoundTag(var1);
      } else if (var2) {
         NBTTagCompound var3 = new NBTTagCompound();
         this.setTagInfo(var1, var3);
         return var3;
      } else {
         return null;
      }
   }

   public NBTTagList getEnchantmentTagList() {
      return this.stackTagCompound == null ? null : this.stackTagCompound.getTagList("ench", 10);
   }

   public void setTagCompound(NBTTagCompound var1) {
      this.stackTagCompound = var1;
   }

   public String getDisplayName() {
      String var1 = this.getItem().getItemStackDisplayName(this);
      if (this.stackTagCompound != null && this.stackTagCompound.hasKey("display", 10)) {
         NBTTagCompound var2 = this.stackTagCompound.getCompoundTag("display");
         if (var2.hasKey("Name", 8)) {
            var1 = var2.getString("Name");
         }
      }

      return var1;
   }

   public ItemStack setStackDisplayName(String var1) {
      if (this.stackTagCompound == null) {
         this.stackTagCompound = new NBTTagCompound();
      }

      if (!this.stackTagCompound.hasKey("display", 10)) {
         this.stackTagCompound.setTag("display", new NBTTagCompound());
      }

      this.stackTagCompound.getCompoundTag("display").setString("Name", var1);
      return this;
   }

   public void clearCustomName() {
      if (this.stackTagCompound != null && this.stackTagCompound.hasKey("display", 10)) {
         NBTTagCompound var1 = this.stackTagCompound.getCompoundTag("display");
         var1.removeTag("Name");
         if (var1.hasNoTags()) {
            this.stackTagCompound.removeTag("display");
            if (this.stackTagCompound.hasNoTags()) {
               this.setTagCompound((NBTTagCompound)null);
            }
         }
      }

   }

   public boolean hasDisplayName() {
      return this.stackTagCompound == null ? false : (!this.stackTagCompound.hasKey("display", 10) ? false : this.stackTagCompound.getCompoundTag("display").hasKey("Name", 8));
   }

   public EnumRarity getRarity() {
      return this.getItem().getRarity(this);
   }

   public boolean isItemEnchantable() {
      return !this.getItem().isEnchantable(this) ? false : !this.isItemEnchanted();
   }

   public void addEnchantment(Enchantment var1, int var2) {
      if (this.stackTagCompound == null) {
         this.setTagCompound(new NBTTagCompound());
      }

      if (!this.stackTagCompound.hasKey("ench", 9)) {
         this.stackTagCompound.setTag("ench", new NBTTagList());
      }

      NBTTagList var3 = this.stackTagCompound.getTagList("ench", 10);
      NBTTagCompound var4 = new NBTTagCompound();
      var4.setShort("id", (short)Enchantment.getEnchantmentID(var1));
      var4.setShort("lvl", (short)((byte)var2));
      var3.appendTag(var4);
   }

   public boolean isItemEnchanted() {
      return this.stackTagCompound != null && this.stackTagCompound.hasKey("ench", 9);
   }

   public void setTagInfo(String var1, NBTBase var2) {
      if (this.stackTagCompound == null) {
         this.setTagCompound(new NBTTagCompound());
      }

      this.stackTagCompound.setTag(var1, var2);
   }

   public boolean canEditBlocks() {
      return this.getItem().canItemEditBlocks();
   }

   public boolean isOnItemFrame() {
      return this.itemFrame != null;
   }

   public void setItemFrame(EntityItemFrame var1) {
      this.itemFrame = var1;
   }

   @Nullable
   public EntityItemFrame getItemFrame() {
      return this.itemFrame;
   }

   public int getRepairCost() {
      return this.hasTagCompound() && this.stackTagCompound.hasKey("RepairCost", 3) ? this.stackTagCompound.getInteger("RepairCost") : 0;
   }

   public void setRepairCost(int var1) {
      if (!this.hasTagCompound()) {
         this.stackTagCompound = new NBTTagCompound();
      }

      this.stackTagCompound.setInteger("RepairCost", var1);
   }

   public Multimap getAttributeModifiers(EntityEquipmentSlot var1) {
      Object var2;
      if (this.hasTagCompound() && this.stackTagCompound.hasKey("AttributeModifiers", 9)) {
         var2 = HashMultimap.create();
         NBTTagList var3 = this.stackTagCompound.getTagList("AttributeModifiers", 10);

         for(int var4 = 0; var4 < var3.tagCount(); ++var4) {
            NBTTagCompound var5 = var3.getCompoundTagAt(var4);
            AttributeModifier var6 = SharedMonsterAttributes.readAttributeModifierFromNBT(var5);
            if (var6 != null && (!var5.hasKey("Slot", 8) || var5.getString("Slot").equals(var1.getName())) && var6.getID().getLeastSignificantBits() != 0L && var6.getID().getMostSignificantBits() != 0L) {
               ((Multimap)var2).put(var5.getString("AttributeName"), var6);
            }
         }
      } else {
         var2 = this.getItem().getItemAttributeModifiers(var1);
      }

      return (Multimap)var2;
   }

   public void addAttributeModifier(String var1, AttributeModifier var2, EntityEquipmentSlot var3) {
      if (this.stackTagCompound == null) {
         this.stackTagCompound = new NBTTagCompound();
      }

      if (!this.stackTagCompound.hasKey("AttributeModifiers", 9)) {
         this.stackTagCompound.setTag("AttributeModifiers", new NBTTagList());
      }

      NBTTagList var4 = this.stackTagCompound.getTagList("AttributeModifiers", 10);
      NBTTagCompound var5 = SharedMonsterAttributes.writeAttributeModifierToNBT(var2);
      var5.setString("AttributeName", var1);
      if (var3 != null) {
         var5.setString("Slot", var3.getName());
      }

      var4.appendTag(var5);
   }

   /** @deprecated */
   @Deprecated
   public void setItem(Item var1) {
      this.item = var1;
      this.setItemDamage(this.getMetadata());
   }

   public ITextComponent getTextComponent() {
      TextComponentString var1 = new TextComponentString(this.getDisplayName());
      if (this.hasDisplayName()) {
         var1.getStyle().setItalic(Boolean.valueOf(true));
      }

      ITextComponent var2 = (new TextComponentString("[")).appendSibling(var1).appendText("]");
      if (this.item != null) {
         NBTTagCompound var3 = this.writeToNBT(new NBTTagCompound());
         var2.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new TextComponentString(var3.toString())));
         var2.getStyle().setColor(this.getRarity().rarityColor);
      }

      return var2;
   }

   public boolean canDestroy(Block var1) {
      if (var1 == this.canDestroyCacheBlock) {
         return this.canDestroyCacheResult;
      } else {
         this.canDestroyCacheBlock = var1;
         if (this.hasTagCompound() && this.stackTagCompound.hasKey("CanDestroy", 9)) {
            NBTTagList var2 = this.stackTagCompound.getTagList("CanDestroy", 8);

            for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
               Block var4 = Block.getBlockFromName(var2.getStringTagAt(var3));
               if (var4 == var1) {
                  this.canDestroyCacheResult = true;
                  return true;
               }
            }
         }

         this.canDestroyCacheResult = false;
         return false;
      }
   }

   public boolean canPlaceOn(Block var1) {
      if (var1 == this.canPlaceOnCacheBlock) {
         return this.canPlaceOnCacheResult;
      } else {
         this.canPlaceOnCacheBlock = var1;
         if (this.hasTagCompound() && this.stackTagCompound.hasKey("CanPlaceOn", 9)) {
            NBTTagList var2 = this.stackTagCompound.getTagList("CanPlaceOn", 8);

            for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
               Block var4 = Block.getBlockFromName(var2.getStringTagAt(var3));
               if (var4 == var1) {
                  this.canPlaceOnCacheResult = true;
                  return true;
               }
            }
         }

         this.canPlaceOnCacheResult = false;
         return false;
      }
   }
}
