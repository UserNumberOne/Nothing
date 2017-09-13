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
         ItemStack itemstack = ItemArmor.dispenseArmor(source, stack);
         return itemstack != null ? itemstack : super.dispenseStack(source, stack);
      }
   };
   public final EntityEquipmentSlot armorType;
   public final int damageReduceAmount;
   public final float toughness;
   public final int renderIndex;
   private final ItemArmor.ArmorMaterial material;

   public static ItemStack dispenseArmor(IBlockSource var0, ItemStack var1) {
      BlockPos blockpos = blockSource.getBlockPos().offset((EnumFacing)blockSource.getBlockState().getValue(BlockDispenser.FACING));
      List list = blockSource.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(blockpos), Predicates.and(EntitySelectors.NOT_SPECTATING, new EntitySelectors.ArmoredMob(stack)));
      if (list.isEmpty()) {
         return null;
      } else {
         EntityLivingBase entitylivingbase = (EntityLivingBase)list.get(0);
         EntityEquipmentSlot entityequipmentslot = EntityLiving.getSlotForItemStack(stack);
         ItemStack itemstack = stack.copy();
         itemstack.stackSize = 1;
         entitylivingbase.setItemStackToSlot(entityequipmentslot, itemstack);
         if (entitylivingbase instanceof EntityLiving) {
            ((EntityLiving)entitylivingbase).setDropChance(entityequipmentslot, 2.0F);
         }

         --stack.stackSize;
         return stack;
      }
   }

   public ItemArmor(ItemArmor.ArmorMaterial var1, int var2, EntityEquipmentSlot var3) {
      this.material = materialIn;
      this.armorType = equipmentSlotIn;
      this.renderIndex = renderIndexIn;
      this.damageReduceAmount = materialIn.getDamageReductionAmount(equipmentSlotIn);
      this.setMaxDamage(materialIn.getDurability(equipmentSlotIn));
      this.toughness = materialIn.getToughness();
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
         NBTTagCompound nbttagcompound = stack.getTagCompound();
         return nbttagcompound != null && nbttagcompound.hasKey("display", 10) ? nbttagcompound.getCompoundTag("display").hasKey("color", 3) : false;
      }
   }

   public int getColor(ItemStack var1) {
      if (this.material != ItemArmor.ArmorMaterial.LEATHER) {
         return 16777215;
      } else {
         NBTTagCompound nbttagcompound = stack.getTagCompound();
         if (nbttagcompound != null) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");
            if (nbttagcompound1 != null && nbttagcompound1.hasKey("color", 3)) {
               return nbttagcompound1.getInteger("color");
            }
         }

         return 10511680;
      }
   }

   public void removeColor(ItemStack var1) {
      if (this.material == ItemArmor.ArmorMaterial.LEATHER) {
         NBTTagCompound nbttagcompound = stack.getTagCompound();
         if (nbttagcompound != null) {
            NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");
            if (nbttagcompound1.hasKey("color")) {
               nbttagcompound1.removeTag("color");
            }
         }
      }

   }

   public void setColor(ItemStack var1, int var2) {
      if (this.material != ItemArmor.ArmorMaterial.LEATHER) {
         throw new UnsupportedOperationException("Can't dye non-leather!");
      } else {
         NBTTagCompound nbttagcompound = stack.getTagCompound();
         if (nbttagcompound == null) {
            nbttagcompound = new NBTTagCompound();
            stack.setTagCompound(nbttagcompound);
         }

         NBTTagCompound nbttagcompound1 = nbttagcompound.getCompoundTag("display");
         if (!nbttagcompound.hasKey("display", 10)) {
            nbttagcompound.setTag("display", nbttagcompound1);
         }

         nbttagcompound1.setInteger("color", color);
      }
   }

   public boolean getIsRepairable(ItemStack var1, ItemStack var2) {
      return this.material.getRepairItem() == repair.getItem() ? true : super.getIsRepairable(toRepair, repair);
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      EntityEquipmentSlot entityequipmentslot = EntityLiving.getSlotForItemStack(itemStackIn);
      ItemStack itemstack = playerIn.getItemStackFromSlot(entityequipmentslot);
      if (itemstack == null) {
         playerIn.setItemStackToSlot(entityequipmentslot, itemStackIn.copy());
         itemStackIn.stackSize = 0;
         return new ActionResult(EnumActionResult.SUCCESS, itemStackIn);
      } else {
         return new ActionResult(EnumActionResult.FAIL, itemStackIn);
      }
   }

   public Multimap getItemAttributeModifiers(EntityEquipmentSlot var1) {
      Multimap multimap = super.getItemAttributeModifiers(equipmentSlot);
      if (equipmentSlot == this.armorType) {
         multimap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor modifier", (double)this.damageReduceAmount, 0));
         multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor toughness", (double)this.toughness, 0));
      }

      return multimap;
   }

   public boolean hasOverlay(ItemStack var1) {
      return this.material == ItemArmor.ArmorMaterial.LEATHER || this.getColor(stack) != 16777215;
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
         this.name = nameIn;
         this.maxDamageFactor = maxDamageFactorIn;
         this.damageReductionAmountArray = damageReductionAmountArrayIn;
         this.enchantability = enchantabilityIn;
         this.soundEvent = soundEventIn;
         this.toughness = toughnessIn;
      }

      public int getDurability(EntityEquipmentSlot var1) {
         return ItemArmor.MAX_DAMAGE_ARRAY[armorType.getIndex()] * this.maxDamageFactor;
      }

      public int getDamageReductionAmount(EntityEquipmentSlot var1) {
         return this.damageReductionAmountArray[armorType.getIndex()];
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
