package net.minecraft.item;

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

public class ItemTool extends Item {
   private final Set effectiveBlocks;
   protected float efficiencyOnProperMaterial;
   protected float damageVsEntity;
   protected float attackSpeed;
   protected Item.ToolMaterial toolMaterial;

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
   }

   protected ItemTool(Item.ToolMaterial var1, Set var2) {
      this(0.0F, 0.0F, var1, var2);
   }

   public float getStrVsBlock(ItemStack var1, IBlockState var2) {
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
      return this.toolMaterial.getRepairItem() == var2.getItem() ? true : super.getIsRepairable(var1, var2);
   }

   public Multimap getItemAttributeModifiers(EntityEquipmentSlot var1) {
      Multimap var2 = super.getItemAttributeModifiers(var1);
      if (var1 == EntityEquipmentSlot.MAINHAND) {
         var2.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", (double)this.damageVsEntity, 0));
         var2.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", (double)this.attackSpeed, 0));
      }

      return var2;
   }
}
