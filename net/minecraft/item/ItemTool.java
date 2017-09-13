package net.minecraft.item;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import java.util.Set;
import net.minecraft.block.Block;
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

   protected ItemTool(float attackDamageIn, float attackSpeedIn, Item.ToolMaterial materialIn, Set effectiveBlocksIn) {
      this.efficiencyOnProperMaterial = 4.0F;
      this.toolMaterial = materialIn;
      this.effectiveBlocks = effectiveBlocksIn;
      this.maxStackSize = 1;
      this.setMaxDamage(materialIn.getMaxUses());
      this.efficiencyOnProperMaterial = materialIn.getEfficiencyOnProperMaterial();
      this.damageVsEntity = attackDamageIn + materialIn.getDamageVsEntity();
      this.attackSpeed = attackSpeedIn;
      this.setCreativeTab(CreativeTabs.TOOLS);
      if (this instanceof ItemPickaxe) {
         this.toolClass = "pickaxe";
      } else if (this instanceof ItemAxe) {
         this.toolClass = "axe";
      } else if (this instanceof ItemSpade) {
         this.toolClass = "shovel";
      }

   }

   protected ItemTool(Item.ToolMaterial materialIn, Set effectiveBlocksIn) {
      this(0.0F, 0.0F, materialIn, effectiveBlocksIn);
   }

   public float getStrVsBlock(ItemStack stack, IBlockState state) {
      for(String type : this.getToolClasses(stack)) {
         if (state.getBlock().isToolEffective(type, state)) {
            return this.efficiencyOnProperMaterial;
         }
      }

      return this.effectiveBlocks.contains(state.getBlock()) ? this.efficiencyOnProperMaterial : 1.0F;
   }

   public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
      stack.damageItem(2, attacker);
      return true;
   }

   public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
      if ((double)state.getBlockHardness(worldIn, pos) != 0.0D) {
         stack.damageItem(1, entityLiving);
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

   public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
      ItemStack mat = this.toolMaterial.getRepairItemStack();
      return mat != null && OreDictionary.itemMatches(mat, repair, false) ? true : super.getIsRepairable(toRepair, repair);
   }

   public Multimap getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
      Multimap multimap = super.getItemAttributeModifiers(equipmentSlot);
      if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
         multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", (double)this.damageVsEntity, 0));
         multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", (double)this.attackSpeed, 0));
      }

      return multimap;
   }

   public int getHarvestLevel(ItemStack stack, String toolClass) {
      int level = super.getHarvestLevel(stack, toolClass);
      return level == -1 && toolClass != null && toolClass.equals(this.toolClass) ? this.toolMaterial.getHarvestLevel() : level;
   }

   public Set getToolClasses(ItemStack stack) {
      return (Set)(this.toolClass != null ? ImmutableSet.of(this.toolClass) : super.getToolClasses(stack));
   }
}
