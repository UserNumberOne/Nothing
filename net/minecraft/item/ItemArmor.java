package net.minecraft.item;

import com.google.common.base.Predicates;
import com.google.common.collect.Multimap;
import java.util.List;
import java.util.UUID;
import net.minecraft.block.BlockDispenser;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemArmor extends Item {
   private static final int[] MAX_DAMAGE_ARRAY = new int[]{13, 15, 16, 11};
   private static final UUID[] ARMOR_MODIFIERS = new UUID[]{UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};
   public static final String[] EMPTY_SLOT_NAMES = new String[]{"minecraft:items/empty_armor_slot_boots", "minecraft:items/empty_armor_slot_leggings", "minecraft:items/empty_armor_slot_chestplate", "minecraft:items/empty_armor_slot_helmet"};
   public static final IBehaviorDispenseItem DISPENSER_BEHAVIOR = new BehaviorDefaultDispenseItem() {
      protected ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
         ItemStack var3 = ItemArmor.dispenseArmor(var1, var2);
         return var3 != null ? var3 : super.dispenseStack(var1, var2);
      }
   };
   public final EntityEquipmentSlot armorType;
   public final int damageReduceAmount;
   public final float toughness;
   public final int renderIndex;
   private final ItemArmor.ArmorMaterial material;

   public static ItemStack dispenseArmor(IBlockSource var0, ItemStack var1) {
      BlockPos var2 = var0.getBlockPos().offset((EnumFacing)var0.getBlockState().getValue(BlockDispenser.FACING));
      List var3 = var0.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(var2), Predicates.and(EntitySelectors.NOT_SPECTATING, new EntitySelectors.ArmoredMob(var1)));
      if (var3.isEmpty()) {
         return null;
      } else {
         EntityLivingBase var4 = (EntityLivingBase)var3.get(0);
         EntityEquipmentSlot var5 = EntityLiving.getSlotForItemStack(var1);
         ItemStack var6 = var1.copy();
         var6.stackSize = 1;
         var4.setItemStackToSlot(var5, var6);
         if (var4 instanceof EntityLiving) {
            ((EntityLiving)var4).setDropChance(var5, 2.0F);
         }

         --var1.stackSize;
         return var1;
      }
   }

   public ItemArmor(ItemArmor.ArmorMaterial var1, int var2, EntityEquipmentSlot var3) {
      this.material = var1;
      this.armorType = var3;
      this.renderIndex = var2;
      this.damageReduceAmount = var1.getDamageReductionAmount(var3);
      this.setMaxDamage(var1.getDurability(var3));
      this.toughness = var1.getToughness();
      this.maxStackSize = 1;
      this.setCreativeTab(CreativeTabs.COMBAT);
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, DISPENSER_BEHAVIOR);
   }

   @SideOnly(Side.CLIENT)
   public EntityEquipmentSlot getEquipmentSlot() {
      return this.armorType;
   }

   public int getItemEnchantability() {
      return this.material.getEnchantability();
   }

   public ItemArmor.ArmorMaterial getArmorMaterial() {
      return this.material;
   }

   public boolean hasColor(ItemStack var1) {
      if (this.material != ItemArmor.ArmorMaterial.LEATHER) {
         return false;
      } else {
         NBTTagCompound var2 = var1.getTagCompound();
         return var2 != null && var2.hasKey("display", 10) ? var2.getCompoundTag("display").hasKey("color", 3) : false;
      }
   }

   public int getColor(ItemStack var1) {
      if (this.material != ItemArmor.ArmorMaterial.LEATHER) {
         return 16777215;
      } else {
         NBTTagCompound var2 = var1.getTagCompound();
         if (var2 != null) {
            NBTTagCompound var3 = var2.getCompoundTag("display");
            if (var3 != null && var3.hasKey("color", 3)) {
               return var3.getInteger("color");
            }
         }

         return 10511680;
      }
   }

   public void removeColor(ItemStack var1) {
      if (this.material == ItemArmor.ArmorMaterial.LEATHER) {
         NBTTagCompound var2 = var1.getTagCompound();
         if (var2 != null) {
            NBTTagCompound var3 = var2.getCompoundTag("display");
            if (var3.hasKey("color")) {
               var3.removeTag("color");
            }
         }
      }

   }

   public void setColor(ItemStack var1, int var2) {
      if (this.material != ItemArmor.ArmorMaterial.LEATHER) {
         throw new UnsupportedOperationException("Can't dye non-leather!");
      } else {
         NBTTagCompound var3 = var1.getTagCompound();
         if (var3 == null) {
            var3 = new NBTTagCompound();
            var1.setTagCompound(var3);
         }

         NBTTagCompound var4 = var3.getCompoundTag("display");
         if (!var3.hasKey("display", 10)) {
            var3.setTag("display", var4);
         }

         var4.setInteger("color", var2);
      }
   }

   public boolean getIsRepairable(ItemStack var1, ItemStack var2) {
      return this.material.getRepairItem() == var2.getItem() ? true : super.getIsRepairable(var1, var2);
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      EntityEquipmentSlot var5 = EntityLiving.getSlotForItemStack(var1);
      ItemStack var6 = var3.getItemStackFromSlot(var5);
      if (var6 == null) {
         var3.setItemStackToSlot(var5, var1.copy());
         var1.stackSize = 0;
         return new ActionResult(EnumActionResult.SUCCESS, var1);
      } else {
         return new ActionResult(EnumActionResult.FAIL, var1);
      }
   }

   public Multimap getItemAttributeModifiers(EntityEquipmentSlot var1) {
      Multimap var2 = super.getItemAttributeModifiers(var1);
      if (var1 == this.armorType) {
         var2.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_MODIFIERS[var1.getIndex()], "Armor modifier", (double)this.damageReduceAmount, 0));
         var2.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier(ARMOR_MODIFIERS[var1.getIndex()], "Armor toughness", (double)this.toughness, 0));
      }

      return var2;
   }

   public boolean hasOverlay(ItemStack var1) {
      return this.material == ItemArmor.ArmorMaterial.LEATHER || this.getColor(var1) != 16777215;
   }

   public static enum ArmorMaterial {
      LEATHER("leather", 5, new int[]{1, 2, 3, 1}, 15, SoundEvents.ITEM_ARMOR_EQUIP_LEATHER, 0.0F),
      CHAIN("chainmail", 15, new int[]{1, 4, 5, 2}, 12, SoundEvents.ITEM_ARMOR_EQUIP_CHAIN, 0.0F),
      IRON("iron", 15, new int[]{2, 5, 6, 2}, 9, SoundEvents.ITEM_ARMOR_EQUIP_IRON, 0.0F),
      GOLD("gold", 7, new int[]{1, 3, 5, 2}, 25, SoundEvents.ITEM_ARMOR_EQUIP_GOLD, 0.0F),
      DIAMOND("diamond", 33, new int[]{3, 6, 8, 3}, 10, SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND, 2.0F);

      private final String name;
      private final int maxDamageFactor;
      private final int[] damageReductionAmountArray;
      private final int enchantability;
      private final SoundEvent soundEvent;
      private final float toughness;
      public Item customCraftingMaterial = null;

      private ArmorMaterial(String var3, int var4, int[] var5, int var6, SoundEvent var7, float var8) {
         this.name = var3;
         this.maxDamageFactor = var4;
         this.damageReductionAmountArray = var5;
         this.enchantability = var6;
         this.soundEvent = var7;
         this.toughness = var8;
      }

      public int getDurability(EntityEquipmentSlot var1) {
         return ItemArmor.MAX_DAMAGE_ARRAY[var1.getIndex()] * this.maxDamageFactor;
      }

      public int getDamageReductionAmount(EntityEquipmentSlot var1) {
         return this.damageReductionAmountArray[var1.getIndex()];
      }

      public int getEnchantability() {
         return this.enchantability;
      }

      public SoundEvent getSoundEvent() {
         return this.soundEvent;
      }

      public Item getRepairItem() {
         switch(this) {
         case LEATHER:
            return Items.LEATHER;
         case CHAIN:
            return Items.IRON_INGOT;
         case GOLD:
            return Items.GOLD_INGOT;
         case IRON:
            return Items.IRON_INGOT;
         case DIAMOND:
            return Items.DIAMOND;
         default:
            return this.customCraftingMaterial;
         }
      }

      @SideOnly(Side.CLIENT)
      public String getName() {
         return this.name;
      }

      public float getToughness() {
         return this.toughness;
      }
   }
}
