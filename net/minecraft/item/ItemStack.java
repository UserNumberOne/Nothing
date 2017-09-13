package net.minecraft.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentDurability;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.BlockEntityTag;
import net.minecraft.util.datafix.walkers.EntityTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.registry.RegistryDelegate;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public final class ItemStack implements ICapabilitySerializable {
   public static final DecimalFormat DECIMALFORMAT = new DecimalFormat("#.##");
   public int stackSize;
   public int animationsToGo;
   private Item item;
   private NBTTagCompound stackTagCompound;
   int itemDamage;
   private EntityItemFrame itemFrame;
   private Block canDestroyCacheBlock;
   private boolean canDestroyCacheResult;
   private Block canPlaceOnCacheBlock;
   private boolean canPlaceOnCacheResult;
   private RegistryDelegate delegate;
   private CapabilityDispatcher capabilities;
   private NBTTagCompound capNBT;

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
      this(var1, var2, var3, (NBTTagCompound)null);
   }

   public ItemStack(Item var1, int var2, int var3, NBTTagCompound var4) {
      this.capNBT = var4;
      this.setItem(var1);
      this.stackSize = var2;
      this.itemDamage = var3;
      if (this.itemDamage < 0) {
         this.itemDamage = 0;
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
      ItemStack var2 = new ItemStack(this.item, var1, this.itemDamage, this.capabilities != null ? this.capabilities.serializeNBT() : null);
      if (this.stackTagCompound != null) {
         var2.stackTagCompound = this.stackTagCompound.copy();
      }

      this.stackSize -= var1;
      return var2;
   }

   public Item getItem() {
      return this.delegate != null ? (Item)this.delegate.get() : null;
   }

   public EnumActionResult onItemUse(EntityPlayer var1, World var2, BlockPos var3, EnumHand var4, EnumFacing var5, float var6, float var7, float var8) {
      if (!var2.isRemote) {
         return ForgeHooks.onPlaceItemIntoWorld(this, var1, var2, var3, var5, var6, var7, var8, var4);
      } else {
         EnumActionResult var9 = this.getItem().onItemUse(this, var1, var2, var3, var4, var5, var6, var7, var8);
         if (var9 == EnumActionResult.SUCCESS) {
            var1.addStat(StatList.getObjectUseStats(this.item));
         }

         return var9;
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
         var1.setTag("tag", this.stackTagCompound);
      }

      if (this.capabilities != null) {
         NBTTagCompound var3 = this.capabilities.serializeNBT();
         if (!var3.hasNoTags()) {
            var1.setTag("ForgeCaps", var3);
         }
      }

      return var1;
   }

   public void readFromNBT(NBTTagCompound var1) {
      this.capNBT = var1.hasKey("ForgeCaps") ? var1.getCompoundTag("ForgeCaps") : null;
      this.setItem(Item.getByNameOrId(var1.getString("id")));
      this.capNBT = null;
      this.stackSize = var1.getByte("Count");
      this.itemDamage = var1.getShort("Damage");
      if (this.itemDamage < 0) {
         this.itemDamage = 0;
      }

      if (var1.hasKey("tag", 10)) {
         this.stackTagCompound = var1.getCompoundTag("tag");
         if (this.item != null) {
            this.item.updateItemStackNBT(this.stackTagCompound);
         }
      } else {
         this.stackTagCompound = null;
      }

   }

   public int getMaxStackSize() {
      return this.getItem().getItemStackLimit(this);
   }

   public boolean isStackable() {
      return this.getMaxStackSize() > 1 && (!this.isItemStackDamageable() || !this.isItemDamaged());
   }

   public boolean isItemStackDamageable() {
      return this.item == null ? false : (this.item.getMaxDamage(this) <= 0 ? false : !this.hasTagCompound() || !this.getTagCompound().getBoolean("Unbreakable"));
   }

   public boolean getHasSubtypes() {
      return this.item.getHasSubtypes();
   }

   public boolean isItemDamaged() {
      return this.isItemStackDamageable() && this.getItem().isDamaged(this);
   }

   public int getItemDamage() {
      return this.getItem().getDamage(this);
   }

   public int getMetadata() {
      return this.getItem().getMetadata(this);
   }

   public void setItemDamage(int var1) {
      this.getItem().setDamage(this, var1);
   }

   public int getMaxDamage() {
      return this.item == null ? 0 : this.item.getMaxDamage(this);
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

         this.setItemDamage(this.getItemDamage() + var1);
         return this.getItemDamage() > this.getMaxDamage();
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
      return this.getItem().canHarvestBlock(var1, this);
   }

   public boolean interactWithEntity(EntityPlayer var1, EntityLivingBase var2, EnumHand var3) {
      return this.item.itemInteractionForEntity(this, var1, var2, var3);
   }

   public ItemStack copy() {
      ItemStack var1 = new ItemStack(this.item, this.stackSize, this.itemDamage, this.capabilities != null ? this.capabilities.serializeNBT() : null);
      if (this.stackTagCompound != null) {
         var1.stackTagCompound = this.stackTagCompound.copy();
      }

      return var1;
   }

   public static boolean areItemStackTagsEqual(@Nullable ItemStack var0, @Nullable ItemStack var1) {
      if (var0 == null && var1 == null) {
         return true;
      } else if (var0 == null && var1 != null) {
         return false;
      } else if (var0 != null && var1 == null) {
         return false;
      } else if (var0.stackTagCompound == null && var1.stackTagCompound != null) {
         return false;
      } else if (var0.stackTagCompound != null && var1.stackTagCompound == null) {
         return false;
      } else if (var0.stackTagCompound != null && !var0.stackTagCompound.equals(var1.stackTagCompound)) {
         return false;
      } else {
         if (var0.capabilities != null || var1.capabilities != null) {
            boolean var2 = var0.capabilities != null ? var0.capabilities.areCompatible(var1.capabilities) : var1.capabilities.areCompatible((CapabilityDispatcher)null);
            if (!var2) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean areItemStacksEqual(@Nullable ItemStack var0, @Nullable ItemStack var1) {
      return var0 == null && var1 == null ? true : (var0 != null && var1 != null ? var0.isItemStackEqual(var1) : false);
   }

   private boolean isItemStackEqual(ItemStack var1) {
      if (this.stackSize != var1.stackSize) {
         return false;
      } else if (this.item != var1.item) {
         return false;
      } else if (this.itemDamage != var1.itemDamage) {
         return false;
      } else {
         return areItemStackTagsEqual(this, var1);
      }
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

   @SideOnly(Side.CLIENT)
   public List getTooltip(EntityPlayer var1, boolean var2) {
      ArrayList var3 = Lists.newArrayList();
      String var4 = this.getDisplayName();
      if (this.hasDisplayName()) {
         var4 = TextFormatting.ITALIC + var4;
      }

      var4 = var4 + TextFormatting.RESET;
      if (var2) {
         String var5 = "";
         if (!var4.isEmpty()) {
            var4 = var4 + " (";
            var5 = ")";
         }

         int var6 = Item.getIdFromItem(this.item);
         if (this.getHasSubtypes()) {
            var4 = var4 + String.format("#%04d/%d%s", var6, this.itemDamage, var5);
         } else {
            var4 = var4 + String.format("#%04d%s", var6, var5);
         }
      } else if (!this.hasDisplayName() && this.item == Items.FILLED_MAP) {
         var4 = var4 + " #" + this.itemDamage;
      }

      var3.add(var4);
      int var20 = 0;
      if (this.hasTagCompound() && this.stackTagCompound.hasKey("HideFlags", 99)) {
         var20 = this.stackTagCompound.getInteger("HideFlags");
      }

      if ((var20 & 32) == 0) {
         this.item.addInformation(this, var1, var3, var2);
      }

      if (this.hasTagCompound()) {
         if ((var20 & 1) == 0) {
            NBTTagList var21 = this.getEnchantmentTagList();
            if (var21 != null) {
               for(int var7 = 0; var7 < var21.tagCount(); ++var7) {
                  short var8 = var21.getCompoundTagAt(var7).getShort("id");
                  short var9 = var21.getCompoundTagAt(var7).getShort("lvl");
                  if (Enchantment.getEnchantmentByID(var8) != null) {
                     var3.add(Enchantment.getEnchantmentByID(var8).getTranslatedName(var9));
                  }
               }
            }
         }

         if (this.stackTagCompound.hasKey("display", 10)) {
            NBTTagCompound var22 = this.stackTagCompound.getCompoundTag("display");
            if (var22.hasKey("color", 3)) {
               if (var2) {
                  var3.add("Color: #" + String.format("%06X", var22.getInteger("color")));
               } else {
                  var3.add(TextFormatting.ITALIC + I18n.translateToLocal("item.dyed"));
               }
            }

            if (var22.getTagId("Lore") == 9) {
               NBTTagList var26 = var22.getTagList("Lore", 8);
               if (!var26.hasNoTags()) {
                  for(int var30 = 0; var30 < var26.tagCount(); ++var30) {
                     var3.add(TextFormatting.DARK_PURPLE + "" + TextFormatting.ITALIC + var26.getStringTagAt(var30));
                  }
               }
            }
         }
      }

      for(EntityEquipmentSlot var34 : EntityEquipmentSlot.values()) {
         Multimap var10 = this.getAttributeModifiers(var34);
         if (!var10.isEmpty() && (var20 & 2) == 0) {
            var3.add("");
            var3.add(I18n.translateToLocal("item.modifiers." + var34.getName()));

            for(Entry var12 : var10.entries()) {
               AttributeModifier var13 = (AttributeModifier)var12.getValue();
               double var14 = var13.getAmount();
               boolean var16 = false;
               if (var13.getID() == Item.ATTACK_DAMAGE_MODIFIER) {
                  var14 = var14 + var1.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getBaseValue();
                  var14 = var14 + (double)EnchantmentHelper.getModifierForCreature(this, EnumCreatureAttribute.UNDEFINED);
                  var16 = true;
               } else if (var13.getID() == Item.ATTACK_SPEED_MODIFIER) {
                  var14 += var1.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue();
                  var16 = true;
               }

               double var17;
               if (var13.getOperation() != 1 && var13.getOperation() != 2) {
                  var17 = var14;
               } else {
                  var17 = var14 * 100.0D;
               }

               if (var16) {
                  var3.add(" " + I18n.translateToLocalFormatted("attribute.modifier.equals." + var13.getOperation(), DECIMALFORMAT.format(var17), I18n.translateToLocal("attribute.name." + (String)var12.getKey())));
               } else if (var14 > 0.0D) {
                  var3.add(TextFormatting.BLUE + " " + I18n.translateToLocalFormatted("attribute.modifier.plus." + var13.getOperation(), DECIMALFORMAT.format(var17), I18n.translateToLocal("attribute.name." + (String)var12.getKey())));
               } else if (var14 < 0.0D) {
                  var17 = var17 * -1.0D;
                  var3.add(TextFormatting.RED + " " + I18n.translateToLocalFormatted("attribute.modifier.take." + var13.getOperation(), DECIMALFORMAT.format(var17), I18n.translateToLocal("attribute.name." + (String)var12.getKey())));
               }
            }
         }
      }

      if (this.hasTagCompound() && this.getTagCompound().getBoolean("Unbreakable") && (var20 & 4) == 0) {
         var3.add(TextFormatting.BLUE + I18n.translateToLocal("item.unbreakable"));
      }

      if (this.hasTagCompound() && this.stackTagCompound.hasKey("CanDestroy", 9) && (var20 & 8) == 0) {
         NBTTagList var24 = this.stackTagCompound.getTagList("CanDestroy", 8);
         if (!var24.hasNoTags()) {
            var3.add("");
            var3.add(TextFormatting.GRAY + I18n.translateToLocal("item.canBreak"));

            for(int var28 = 0; var28 < var24.tagCount(); ++var28) {
               Block var32 = Block.getBlockFromName(var24.getStringTagAt(var28));
               if (var32 != null) {
                  var3.add(TextFormatting.DARK_GRAY + var32.getLocalizedName());
               } else {
                  var3.add(TextFormatting.DARK_GRAY + "missingno");
               }
            }
         }
      }

      if (this.hasTagCompound() && this.stackTagCompound.hasKey("CanPlaceOn", 9) && (var20 & 16) == 0) {
         NBTTagList var25 = this.stackTagCompound.getTagList("CanPlaceOn", 8);
         if (!var25.hasNoTags()) {
            var3.add("");
            var3.add(TextFormatting.GRAY + I18n.translateToLocal("item.canPlace"));

            for(int var29 = 0; var29 < var25.tagCount(); ++var29) {
               Block var33 = Block.getBlockFromName(var25.getStringTagAt(var29));
               if (var33 != null) {
                  var3.add(TextFormatting.DARK_GRAY + var33.getLocalizedName());
               } else {
                  var3.add(TextFormatting.DARK_GRAY + "missingno");
               }
            }
         }
      }

      if (var2) {
         if (this.isItemDamaged()) {
            var3.add("Durability: " + (this.getMaxDamage() - this.getItemDamage()) + " / " + this.getMaxDamage());
         }

         var3.add(TextFormatting.DARK_GRAY + ((ResourceLocation)Item.REGISTRY.getNameForObject(this.item)).toString());
         if (this.hasTagCompound()) {
            var3.add(TextFormatting.DARK_GRAY + "NBT: " + this.getTagCompound().getKeySet().size() + " tag(s)");
         }
      }

      ForgeEventFactory.onItemTooltip(this, var1, var3, var2);
      return var3;
   }

   @SideOnly(Side.CLIENT)
   public boolean hasEffect() {
      return this.getItem().hasEffect(this);
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
         var2 = this.getItem().getAttributeModifiers(var1, this);
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
      if (var1 == this.item && this.item != null && this.capabilities != null) {
         ICapabilityProvider var3 = this.item.initCapabilities(this, this.capabilities.serializeNBT());
         this.capabilities = ForgeEventFactory.gatherCapabilities(this.item, this, var3);
      } else if (var1 != this.item && var1 != null) {
         ICapabilityProvider var2 = var1.initCapabilities(this, this.capNBT);
         this.capabilities = ForgeEventFactory.gatherCapabilities(var1, this, var2);
      }

      if (this.capNBT != null && this.capabilities != null) {
         this.capabilities.deserializeNBT(this.capNBT);
      }

      this.delegate = var1 != null ? var1.delegate : null;
      this.item = var1;
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

   public boolean hasCapability(Capability var1, EnumFacing var2) {
      return this.capabilities == null ? false : this.capabilities.hasCapability(var1, var2);
   }

   public Object getCapability(Capability var1, EnumFacing var2) {
      return this.capabilities == null ? null : this.capabilities.getCapability(var1, var2);
   }

   public void deserializeNBT(NBTTagCompound var1) {
      this.readFromNBT(var1);
   }

   public NBTTagCompound serializeNBT() {
      NBTTagCompound var1 = new NBTTagCompound();
      this.writeToNBT(var1);
      return var1;
   }

   public boolean areCapsCompatible(ItemStack var1) {
      if (this.capabilities == null) {
         return var1.capabilities == null ? true : var1.capabilities.areCompatible((CapabilityDispatcher)null);
      } else {
         return this.capabilities.areCompatible(var1.capabilities);
      }
   }
}
