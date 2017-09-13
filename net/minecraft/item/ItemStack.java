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

   public ItemStack(Block block) {
      this(block, 1);
   }

   public ItemStack(Block block, int i) {
      this(block, i, 0);
   }

   public ItemStack(Block block, int i, int j) {
      this(Item.getItemFromBlock(block), i, j);
   }

   public ItemStack(Item item) {
      this(item, 1);
   }

   public ItemStack(Item item, int i) {
      this(item, i, 0);
   }

   public ItemStack(Item item, int i, int j) {
      this.item = item;
      this.stackSize = i;
      this.setItemDamage(j);
      if (MinecraftServer.getServer() != null) {
         NBTTagCompound savedStack = new NBTTagCompound();
         this.writeToNBT(savedStack);
         MinecraftServer.getServer().getDataConverterManager().process(FixTypes.ITEM_INSTANCE, savedStack);
         this.readFromNBT(savedStack);
      }

   }

   public static ItemStack loadItemStackFromNBT(NBTTagCompound nbttagcompound) {
      ItemStack itemstack = new ItemStack();
      itemstack.readFromNBT(nbttagcompound);
      return itemstack.getItem() != null ? itemstack : null;
   }

   private ItemStack() {
   }

   public static void registerFixes(DataFixer dataconvertermanager) {
      dataconvertermanager.registerWalker(FixTypes.ITEM_INSTANCE, new BlockEntityTag());
      dataconvertermanager.registerWalker(FixTypes.ITEM_INSTANCE, new EntityTag());
   }

   public ItemStack splitStack(int i) {
      i = Math.min(i, this.stackSize);
      ItemStack itemstack = new ItemStack(this.item, i, this.itemDamage);
      if (this.stackTagCompound != null) {
         itemstack.stackTagCompound = this.stackTagCompound.copy();
      }

      this.stackSize -= i;
      return itemstack;
   }

   public Item getItem() {
      return this.item;
   }

   public EnumActionResult onItemUse(EntityPlayer entityhuman, World world, BlockPos blockposition, EnumHand enumhand, EnumFacing enumdirection, float f, float f1, float f2) {
      int data = this.getMetadata();
      int count = this.stackSize;
      if (!(this.getItem() instanceof ItemBucket)) {
         world.captureBlockStates = true;
         if (this.getItem() instanceof ItemDye && this.getMetadata() == 15) {
            Block block = world.getBlockState(blockposition).getBlock();
            if (block == Blocks.SAPLING || block instanceof BlockMushroom) {
               world.captureTreeGeneration = true;
            }
         }
      }

      EnumActionResult enuminteractionresult = this.getItem().onItemUse(this, entityhuman, world, blockposition, enumhand, enumdirection, f, f1, f2);
      int newData = this.getMetadata();
      int newCount = this.stackSize;
      this.stackSize = count;
      this.setItemDamage(data);
      world.captureBlockStates = false;
      if (enuminteractionresult == EnumActionResult.SUCCESS && world.captureTreeGeneration && world.capturedBlockStates.size() > 0) {
         world.captureTreeGeneration = false;
         Location location = new Location(world.getWorld(), (double)blockposition.getX(), (double)blockposition.getY(), (double)blockposition.getZ());
         TreeType treeType = BlockSapling.treeType;
         BlockSapling.treeType = null;
         List blocks = (List)world.capturedBlockStates.clone();
         world.capturedBlockStates.clear();
         StructureGrowEvent event = null;
         if (treeType != null) {
            boolean isBonemeal = this.getItem() == Items.DYE && data == 15;
            event = new StructureGrowEvent(location, treeType, isBonemeal, (Player)entityhuman.getBukkitEntity(), blocks);
            Bukkit.getPluginManager().callEvent(event);
         }

         if (event == null || !event.isCancelled()) {
            if (this.stackSize == count && this.getMetadata() == data) {
               this.setItemDamage(newData);
               this.stackSize = newCount;
            }

            for(BlockState blockstate : blocks) {
               blockstate.update(true);
            }
         }

         return enuminteractionresult;
      } else {
         world.captureTreeGeneration = false;
         if (enuminteractionresult == EnumActionResult.SUCCESS) {
            BlockPlaceEvent placeEvent = null;
            List blocks = (List)world.capturedBlockStates.clone();
            world.capturedBlockStates.clear();
            if (blocks.size() > 1) {
               placeEvent = CraftEventFactory.callBlockMultiPlaceEvent(world, entityhuman, enumhand, blocks, blockposition.getX(), blockposition.getY(), blockposition.getZ());
            } else if (blocks.size() == 1) {
               placeEvent = CraftEventFactory.callBlockPlaceEvent(world, entityhuman, enumhand, (BlockState)blocks.get(0), blockposition.getX(), blockposition.getY(), blockposition.getZ());
            }

            if (placeEvent != null && (placeEvent.isCancelled() || !placeEvent.canBuild())) {
               enuminteractionresult = EnumActionResult.FAIL;
               placeEvent.getPlayer().updateInventory();

               for(BlockState blockstate : blocks) {
                  blockstate.update(true, false);
               }
            } else {
               if (this.stackSize == count && this.getMetadata() == data) {
                  this.setItemDamage(newData);
                  this.stackSize = newCount;
               }

               for(BlockState blockstate : blocks) {
                  int x = blockstate.getX();
                  int y = blockstate.getY();
                  int z = blockstate.getZ();
                  int updateFlag = ((CraftBlockState)blockstate).getFlag();
                  Material mat = blockstate.getType();
                  Block oldBlock = CraftMagicNumbers.getBlock(mat);
                  BlockPos newblockposition = new BlockPos(x, y, z);
                  IBlockState block = world.getBlockState(newblockposition);
                  if (!(block instanceof BlockContainer)) {
                     block.getBlock().onBlockAdded(world, newblockposition, block);
                  }

                  world.notifyAndUpdatePhysics(newblockposition, (Chunk)null, oldBlock.getDefaultState(), block, updateFlag);
               }

               for(Entry e : world.capturedTileEntities.entrySet()) {
                  world.setTileEntity((BlockPos)e.getKey(), (TileEntity)e.getValue());
               }

               if (this.getItem() instanceof ItemRecord) {
                  ((BlockJukebox)Blocks.JUKEBOX).insertRecord(world, blockposition, world.getBlockState(blockposition), this);
                  world.playEvent((EntityPlayer)null, 1010, blockposition, Item.getIdFromItem(this.item));
                  --this.stackSize;
                  entityhuman.addStat(StatList.RECORD_PLAYED);
               }

               if (this.getItem() == Items.SKULL) {
                  BlockPos bp = blockposition;
                  if (!world.getBlockState(blockposition).getBlock().isReplaceable(world, blockposition)) {
                     if (!world.getBlockState(blockposition).getMaterial().isSolid()) {
                        bp = null;
                     } else {
                        bp = blockposition.offset(enumdirection);
                     }
                  }

                  if (bp != null) {
                     TileEntity te = world.getTileEntity(bp);
                     if (te instanceof TileEntitySkull) {
                        Blocks.SKULL.checkWitherSpawn(world, bp, (TileEntitySkull)te);
                     }
                  }
               }

               if (this.getItem() instanceof ItemBlock) {
                  SoundType soundeffecttype = ((ItemBlock)this.getItem()).getBlock().getSoundType();
                  world.playSound(entityhuman, blockposition, soundeffecttype.getPlaceSound(), SoundCategory.BLOCKS, (soundeffecttype.getVolume() + 1.0F) / 2.0F, soundeffecttype.getPitch() * 0.8F);
               }

               entityhuman.addStat(StatList.getObjectUseStats(this.item));
            }
         }

         world.capturedTileEntities.clear();
         world.capturedBlockStates.clear();
         return enuminteractionresult;
      }
   }

   public float getStrVsBlock(IBlockState iblockdata) {
      return this.getItem().getStrVsBlock(this, iblockdata);
   }

   public ActionResult useItemRightClick(World world, EntityPlayer entityhuman, EnumHand enumhand) {
      return this.getItem().onItemRightClick(this, world, entityhuman, enumhand);
   }

   @Nullable
   public ItemStack onItemUseFinish(World world, EntityLivingBase entityliving) {
      return this.getItem().onItemUseFinish(this, world, entityliving);
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      ResourceLocation minecraftkey = (ResourceLocation)Item.REGISTRY.getNameForObject(this.item);
      nbttagcompound.setString("id", minecraftkey == null ? "minecraft:air" : minecraftkey.toString());
      nbttagcompound.setByte("Count", (byte)this.stackSize);
      nbttagcompound.setShort("Damage", (short)this.itemDamage);
      if (this.stackTagCompound != null) {
         nbttagcompound.setTag("tag", this.stackTagCompound.copy());
      }

      return nbttagcompound;
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      this.item = Item.getByNameOrId(nbttagcompound.getString("id"));
      this.stackSize = nbttagcompound.getByte("Count");
      this.setItemDamage(nbttagcompound.getShort("Damage"));
      if (nbttagcompound.hasKey("tag", 10)) {
         this.stackTagCompound = (NBTTagCompound)nbttagcompound.getCompoundTag("tag").copy();
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

   public void setItemDamage(int i) {
      if (i == 32767) {
         this.itemDamage = i;
      } else {
         if (CraftMagicNumbers.getBlock(CraftMagicNumbers.getId(this.getItem())) != Blocks.AIR && !this.getHasSubtypes() && !this.getItem().isDamageable()) {
            i = 0;
         }

         if (CraftMagicNumbers.getBlock(CraftMagicNumbers.getId(this.getItem())) == Blocks.DOUBLE_PLANT && (i > 5 || i < 0)) {
            i = 0;
         }

         this.itemDamage = i;
      }
   }

   public int getMaxDamage() {
      return this.item == null ? 0 : this.item.getMaxDamage();
   }

   public boolean attemptDamageItem(int i, Random random) {
      if (!this.isItemStackDamageable()) {
         return false;
      } else {
         if (i > 0) {
            int j = EnchantmentHelper.getEnchantmentLevel(Enchantments.UNBREAKING, this);
            int k = 0;

            for(int l = 0; j > 0 && l < i; ++l) {
               if (EnchantmentDurability.negateDamage(this, j, random)) {
                  ++k;
               }
            }

            i -= k;
            if (i <= 0) {
               return false;
            }
         }

         this.itemDamage += i;
         return this.itemDamage > this.getMaxDamage();
      }
   }

   public void damageItem(int i, EntityLivingBase entityliving) {
      if ((!(entityliving instanceof EntityPlayer) || !((EntityPlayer)entityliving).capabilities.isCreativeMode) && this.isItemStackDamageable() && this.attemptDamageItem(i, entityliving.getRNG())) {
         entityliving.renderBrokenItemStack(this);
         --this.stackSize;
         if (entityliving instanceof EntityPlayer) {
            EntityPlayer entityhuman = (EntityPlayer)entityliving;
            entityhuman.addStat(StatList.getObjectBreakStats(this.item));
         }

         if (this.stackSize < 0) {
            this.stackSize = 0;
         }

         if (this.stackSize == 0 && entityliving instanceof EntityPlayer) {
            CraftEventFactory.callPlayerItemBreakEvent((EntityPlayer)entityliving, this);
         }

         this.itemDamage = 0;
      }

   }

   public void hitEntity(EntityLivingBase entityliving, EntityPlayer entityhuman) {
      boolean flag = this.item.hitEntity(this, entityliving, entityhuman);
      if (flag) {
         entityhuman.addStat(StatList.getObjectUseStats(this.item));
      }

   }

   public void onBlockDestroyed(World world, IBlockState iblockdata, BlockPos blockposition, EntityPlayer entityhuman) {
      boolean flag = this.item.onBlockDestroyed(this, world, iblockdata, blockposition, entityhuman);
      if (flag) {
         entityhuman.addStat(StatList.getObjectUseStats(this.item));
      }

   }

   public boolean canHarvestBlock(IBlockState iblockdata) {
      return this.item.canHarvestBlock(iblockdata);
   }

   public boolean interactWithEntity(EntityPlayer entityhuman, EntityLivingBase entityliving, EnumHand enumhand) {
      return this.item.itemInteractionForEntity(this, entityhuman, entityliving, enumhand);
   }

   public ItemStack copy() {
      ItemStack itemstack = new ItemStack(this.item, this.stackSize, this.itemDamage);
      if (this.stackTagCompound != null) {
         itemstack.stackTagCompound = this.stackTagCompound.copy();
      }

      return itemstack;
   }

   public static boolean areItemStackTagsEqual(@Nullable ItemStack itemstack, @Nullable ItemStack itemstack1) {
      return itemstack == null && itemstack1 == null ? true : (itemstack != null && itemstack1 != null ? (itemstack.stackTagCompound == null && itemstack1.stackTagCompound != null ? false : itemstack.stackTagCompound == null || itemstack.stackTagCompound.equals(itemstack1.stackTagCompound)) : false);
   }

   public static boolean areItemStacksEqual(@Nullable ItemStack itemstack, @Nullable ItemStack itemstack1) {
      return itemstack == null && itemstack1 == null ? true : (itemstack != null && itemstack1 != null ? itemstack.isItemStackEqual(itemstack1) : false);
   }

   private boolean isItemStackEqual(ItemStack itemstack) {
      return this.stackSize != itemstack.stackSize ? false : (this.item != itemstack.item ? false : (this.itemDamage != itemstack.itemDamage ? false : (this.stackTagCompound == null && itemstack.stackTagCompound != null ? false : this.stackTagCompound == null || this.stackTagCompound.equals(itemstack.stackTagCompound))));
   }

   public static boolean areItemsEqual(@Nullable ItemStack itemstack, @Nullable ItemStack itemstack1) {
      return itemstack == itemstack1 ? true : (itemstack != null && itemstack1 != null ? itemstack.isItemEqual(itemstack1) : false);
   }

   public static boolean areItemsEqualIgnoreDurability(@Nullable ItemStack itemstack, @Nullable ItemStack itemstack1) {
      return itemstack == itemstack1 ? true : (itemstack != null && itemstack1 != null ? itemstack.isItemEqualIgnoreDurability(itemstack1) : false);
   }

   public boolean isItemEqual(@Nullable ItemStack itemstack) {
      return itemstack != null && this.item == itemstack.item && this.itemDamage == itemstack.itemDamage;
   }

   public boolean isItemEqualIgnoreDurability(@Nullable ItemStack itemstack) {
      return !this.isItemStackDamageable() ? this.isItemEqual(itemstack) : itemstack != null && this.item == itemstack.item;
   }

   public String getUnlocalizedName() {
      return this.item.getUnlocalizedName(this);
   }

   public static ItemStack copyItemStack(ItemStack itemstack) {
      return itemstack == null ? null : itemstack.copy();
   }

   public String toString() {
      return this.stackSize + "x" + this.item.getUnlocalizedName() + "@" + this.itemDamage;
   }

   public void updateAnimation(World world, Entity entity, int i, boolean flag) {
      if (this.animationsToGo > 0) {
         --this.animationsToGo;
      }

      if (this.item != null) {
         this.item.onUpdate(this, world, entity, i, flag);
      }

   }

   public void onCrafting(World world, EntityPlayer entityhuman, int i) {
      entityhuman.addStat(StatList.getCraftStats(this.item), i);
      this.item.onCreated(this, world, entityhuman);
   }

   public int getMaxItemUseDuration() {
      return this.getItem().getMaxItemUseDuration(this);
   }

   public EnumAction getItemUseAction() {
      return this.getItem().getItemUseAction(this);
   }

   public void onPlayerStoppedUsing(World world, EntityLivingBase entityliving, int i) {
      this.getItem().onPlayerStoppedUsing(this, world, entityliving, i);
   }

   public boolean hasTagCompound() {
      return this.stackTagCompound != null;
   }

   @Nullable
   public NBTTagCompound getTagCompound() {
      return this.stackTagCompound;
   }

   public NBTTagCompound getSubCompound(String s, boolean flag) {
      if (this.stackTagCompound != null && this.stackTagCompound.hasKey(s, 10)) {
         return this.stackTagCompound.getCompoundTag(s);
      } else if (flag) {
         NBTTagCompound nbttagcompound = new NBTTagCompound();
         this.setTagInfo(s, nbttagcompound);
         return nbttagcompound;
      } else {
         return null;
      }
   }

   public NBTTagList getEnchantmentTagList() {
      return this.stackTagCompound == null ? null : this.stackTagCompound.getTagList("ench", 10);
   }

   public void setTagCompound(NBTTagCompound nbttagcompound) {
      this.stackTagCompound = nbttagcompound;
   }

   public String getDisplayName() {
      String s = this.getItem().getItemStackDisplayName(this);
      if (this.stackTagCompound != null && this.stackTagCompound.hasKey("display", 10)) {
         NBTTagCompound nbttagcompound = this.stackTagCompound.getCompoundTag("display");
         if (nbttagcompound.hasKey("Name", 8)) {
            s = nbttagcompound.getString("Name");
         }
      }

      return s;
   }

   public ItemStack setStackDisplayName(String s) {
      if (this.stackTagCompound == null) {
         this.stackTagCompound = new NBTTagCompound();
      }

      if (!this.stackTagCompound.hasKey("display", 10)) {
         this.stackTagCompound.setTag("display", new NBTTagCompound());
      }

      this.stackTagCompound.getCompoundTag("display").setString("Name", s);
      return this;
   }

   public void clearCustomName() {
      if (this.stackTagCompound != null && this.stackTagCompound.hasKey("display", 10)) {
         NBTTagCompound nbttagcompound = this.stackTagCompound.getCompoundTag("display");
         nbttagcompound.removeTag("Name");
         if (nbttagcompound.hasNoTags()) {
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

   public void addEnchantment(Enchantment enchantment, int i) {
      if (this.stackTagCompound == null) {
         this.setTagCompound(new NBTTagCompound());
      }

      if (!this.stackTagCompound.hasKey("ench", 9)) {
         this.stackTagCompound.setTag("ench", new NBTTagList());
      }

      NBTTagList nbttaglist = this.stackTagCompound.getTagList("ench", 10);
      NBTTagCompound nbttagcompound = new NBTTagCompound();
      nbttagcompound.setShort("id", (short)Enchantment.getEnchantmentID(enchantment));
      nbttagcompound.setShort("lvl", (short)((byte)i));
      nbttaglist.appendTag(nbttagcompound);
   }

   public boolean isItemEnchanted() {
      return this.stackTagCompound != null && this.stackTagCompound.hasKey("ench", 9);
   }

   public void setTagInfo(String s, NBTBase nbtbase) {
      if (this.stackTagCompound == null) {
         this.setTagCompound(new NBTTagCompound());
      }

      this.stackTagCompound.setTag(s, nbtbase);
   }

   public boolean canEditBlocks() {
      return this.getItem().canItemEditBlocks();
   }

   public boolean isOnItemFrame() {
      return this.itemFrame != null;
   }

   public void setItemFrame(EntityItemFrame entityitemframe) {
      this.itemFrame = entityitemframe;
   }

   @Nullable
   public EntityItemFrame getItemFrame() {
      return this.itemFrame;
   }

   public int getRepairCost() {
      return this.hasTagCompound() && this.stackTagCompound.hasKey("RepairCost", 3) ? this.stackTagCompound.getInteger("RepairCost") : 0;
   }

   public void setRepairCost(int i) {
      if (!this.hasTagCompound()) {
         this.stackTagCompound = new NBTTagCompound();
      }

      this.stackTagCompound.setInteger("RepairCost", i);
   }

   public Multimap getAttributeModifiers(EntityEquipmentSlot enumitemslot) {
      Object object;
      if (this.hasTagCompound() && this.stackTagCompound.hasKey("AttributeModifiers", 9)) {
         object = HashMultimap.create();
         NBTTagList nbttaglist = this.stackTagCompound.getTagList("AttributeModifiers", 10);

         for(int i = 0; i < nbttaglist.tagCount(); ++i) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(i);
            AttributeModifier attributemodifier = SharedMonsterAttributes.readAttributeModifierFromNBT(nbttagcompound);
            if (attributemodifier != null && (!nbttagcompound.hasKey("Slot", 8) || nbttagcompound.getString("Slot").equals(enumitemslot.getName())) && attributemodifier.getID().getLeastSignificantBits() != 0L && attributemodifier.getID().getMostSignificantBits() != 0L) {
               ((Multimap)object).put(nbttagcompound.getString("AttributeName"), attributemodifier);
            }
         }
      } else {
         object = this.getItem().getItemAttributeModifiers(enumitemslot);
      }

      return (Multimap)object;
   }

   public void addAttributeModifier(String s, AttributeModifier attributemodifier, EntityEquipmentSlot enumitemslot) {
      if (this.stackTagCompound == null) {
         this.stackTagCompound = new NBTTagCompound();
      }

      if (!this.stackTagCompound.hasKey("AttributeModifiers", 9)) {
         this.stackTagCompound.setTag("AttributeModifiers", new NBTTagList());
      }

      NBTTagList nbttaglist = this.stackTagCompound.getTagList("AttributeModifiers", 10);
      NBTTagCompound nbttagcompound = SharedMonsterAttributes.writeAttributeModifierToNBT(attributemodifier);
      nbttagcompound.setString("AttributeName", s);
      if (enumitemslot != null) {
         nbttagcompound.setString("Slot", enumitemslot.getName());
      }

      nbttaglist.appendTag(nbttagcompound);
   }

   /** @deprecated */
   @Deprecated
   public void setItem(Item item) {
      this.item = item;
      this.setItemDamage(this.getMetadata());
   }

   public ITextComponent getTextComponent() {
      TextComponentString chatcomponenttext = new TextComponentString(this.getDisplayName());
      if (this.hasDisplayName()) {
         chatcomponenttext.getStyle().setItalic(Boolean.valueOf(true));
      }

      ITextComponent ichatbasecomponent = (new TextComponentString("[")).appendSibling(chatcomponenttext).appendText("]");
      if (this.item != null) {
         NBTTagCompound nbttagcompound = this.writeToNBT(new NBTTagCompound());
         ichatbasecomponent.getStyle().setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_ITEM, new TextComponentString(nbttagcompound.toString())));
         ichatbasecomponent.getStyle().setColor(this.getRarity().rarityColor);
      }

      return ichatbasecomponent;
   }

   public boolean canDestroy(Block block) {
      if (block == this.canDestroyCacheBlock) {
         return this.canDestroyCacheResult;
      } else {
         this.canDestroyCacheBlock = block;
         if (this.hasTagCompound() && this.stackTagCompound.hasKey("CanDestroy", 9)) {
            NBTTagList nbttaglist = this.stackTagCompound.getTagList("CanDestroy", 8);

            for(int i = 0; i < nbttaglist.tagCount(); ++i) {
               Block block1 = Block.getBlockFromName(nbttaglist.getStringTagAt(i));
               if (block1 == block) {
                  this.canDestroyCacheResult = true;
                  return true;
               }
            }
         }

         this.canDestroyCacheResult = false;
         return false;
      }
   }

   public boolean canPlaceOn(Block block) {
      if (block == this.canPlaceOnCacheBlock) {
         return this.canPlaceOnCacheResult;
      } else {
         this.canPlaceOnCacheBlock = block;
         if (this.hasTagCompound() && this.stackTagCompound.hasKey("CanPlaceOn", 9)) {
            NBTTagList nbttaglist = this.stackTagCompound.getTagList("CanPlaceOn", 8);

            for(int i = 0; i < nbttaglist.tagCount(); ++i) {
               Block block1 = Block.getBlockFromName(nbttaglist.getStringTagAt(i));
               if (block1 == block) {
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
