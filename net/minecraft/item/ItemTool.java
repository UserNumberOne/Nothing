package net.minecraft.item;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.Set;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class ItemTool extends Item {
   private final Set effectiveBlocks;
   protected float efficiencyOnProperMaterial;
   protected float damageVsEntity;
   protected float attackSpeed;
   protected Item.ToolMaterial toolMaterial;
   private String toolClass;

   protected ItemTool(float var1, float var2, Item.ToolMaterial var3, Set var4) {
      this.efficiencyOnProperMaterial = 4.0F;
      this.toolMaterial = var3;
      this.effectiveBlocks = var4;
      this.maxStackSize = 1;
      this.setMaxDamage(var3.getMaxUses());
      this.efficiencyOnProperMaterial = var3.getEfficiencyOnProperMaterial();
      this.damageVsEntity = var1 + var3.getDamageVsEntity();
      this.attackSpeed = var2;
      this.setCreativeTab(CreativeTabs.TOOLS);
      if (this instanceof ItemPickaxe) {
         this.toolClass = "pickaxe";
      } else if (this instanceof ItemAxe) {
         this.toolClass = "axe";
      } else if (this instanceof ItemSpade) {
         this.toolClass = "shovel";
      }

   }

   protected ItemTool(Item.ToolMaterial var1, Set var2) {
      this(0.0F, 0.0F, var1, var2);
   }

   public float getStrVsBlock(ItemStack var1, IBlockState var2) {
      for(String var4 : this.getToolClasses(var1)) {
         if (var2.getBlock().isToolEffective(var4, var2)) {
            return this.efficiencyOnProperMaterial;
         }
      }

      return this.effectiveBlocks.contains(var2.getBlock()) ? this.efficiencyOnProperMaterial : 1.0F;
   }

   public boolean hitEntity(ItemStack var1, EntityLivingBase var2, EntityLivingBase var3) {
      var1.damageItem(2, var3);
      return true;
   }

   public boolean onBlockDestroyed(ItemStack var1, World var2, IBlockState var3, BlockPos var4, EntityLivingBase var5) {
      if ((double)var3.getBlockHardness(var2, var4) != 0.0D) {
         var1.damageItem(1, var5);
      }

      return true;
   }

   @SideOnly(Side.CLIENT)
   public boolean isFull3D() {
      return true;
   }

   public Item.ToolMaterial getToolMaterial() {
      return this.toolMaterial;
   }

   public int getItemEnchantability() {
      return this.toolMaterial.getEnchantability();
   }

   public String getToolMaterialName() {
      return this.toolMaterial.toString();
   }

   public boolean getIsRepairable(ItemStack var1, ItemStack var2) {
      ItemStack var3 = this.toolMaterial.getRepairItemStack();
      return var3 != null && OreDictionary.itemMatches(var3, var2, false) ? true : super.getIsRepairable(var1, var2);
   }

   public Multimap getItemAttributeModifiers(EntityEquipmentSlot var1) {
      Multimap var2 = super.getItemAttributeModifiers(var1);
      if (var1 == EntityEquipmentSlot.MAINHAND) {
         var2.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", (double)this.damageVsEntity, 0));
         var2.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", (double)this.attackSpeed, 0));
      }

      return var2;
   }

   public int getHarvestLevel(ItemStack var1, String var2) {
      int var3 = super.getHarvestLevel(var1, var2);
      return var3 == -1 && var2 != null && var2.equals(this.toolClass) ? this.toolMaterial.getHarvestLevel() : var3;
   }

   public Set getToolClasses(ItemStack var1) {
      return (Set)(this.toolClass != null ? ImmutableSet.of(this.toolClass) : super.getToolClasses(var1));
   }
}
