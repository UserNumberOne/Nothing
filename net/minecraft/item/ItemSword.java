package net.minecraft.item;

import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemSword extends Item {
   private final float attackDamage;
   private final Item.ToolMaterial material;

   public ItemSword(Item.ToolMaterial var1) {
      this.material = var1;
      this.maxStackSize = 1;
      this.setMaxDamage(var1.getMaxUses());
      this.setCreativeTab(CreativeTabs.COMBAT);
      this.attackDamage = 3.0F + var1.getDamageVsEntity();
   }

   public float getDamageVsEntity() {
      return this.material.getDamageVsEntity();
   }

   public float getStrVsBlock(ItemStack var1, IBlockState var2) {
      Block var3 = var2.getBlock();
      if (var3 == Blocks.WEB) {
         return 15.0F;
      } else {
         Material var4 = var2.getMaterial();
         return var4 != Material.PLANTS && var4 != Material.VINE && var4 != Material.CORAL && var4 != Material.LEAVES && var4 != Material.GOURD ? 1.0F : 1.5F;
      }
   }

   public boolean hitEntity(ItemStack var1, EntityLivingBase var2, EntityLivingBase var3) {
      var1.damageItem(1, var3);
      return true;
   }

   public boolean onBlockDestroyed(ItemStack var1, World var2, IBlockState var3, BlockPos var4, EntityLivingBase var5) {
      if ((double)var3.getBlockHardness(var2, var4) != 0.0D) {
         var1.damageItem(2, var5);
      }

      return true;
   }

   public boolean canHarvestBlock(IBlockState var1) {
      return var1.getBlock() == Blocks.WEB;
   }

   public int getItemEnchantability() {
      return this.material.getEnchantability();
   }

   public String getToolMaterialName() {
      return this.material.toString();
   }

   public boolean getIsRepairable(ItemStack var1, ItemStack var2) {
      return this.material.getRepairItem() == var2.getItem() ? true : super.getIsRepairable(var1, var2);
   }

   public Multimap getItemAttributeModifiers(EntityEquipmentSlot var1) {
      Multimap var2 = super.getItemAttributeModifiers(var1);
      if (var1 == EntityEquipmentSlot.MAINHAND) {
         var2.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double)this.attackDamage, 0));
         var2.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
      }

      return var2;
   }
}
