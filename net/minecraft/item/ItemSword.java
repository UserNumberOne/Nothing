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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

public class ItemSword extends Item {
   private final float attackDamage;
   private final Item.ToolMaterial material;

   public ItemSword(Item.ToolMaterial var1) {
      this.material = material;
      this.maxStackSize = 1;
      this.setMaxDamage(material.getMaxUses());
      this.setCreativeTab(CreativeTabs.COMBAT);
      this.attackDamage = 3.0F + material.getDamageVsEntity();
   }

   public float getDamageVsEntity() {
      return this.material.getDamageVsEntity();
   }

   public float getStrVsBlock(ItemStack var1, IBlockState var2) {
      Block block = state.getBlock();
      if (block == Blocks.WEB) {
         return 15.0F;
      } else {
         Material material = state.getMaterial();
         return material != Material.PLANTS && material != Material.VINE && material != Material.CORAL && material != Material.LEAVES && material != Material.GOURD ? 1.0F : 1.5F;
      }
   }

   public boolean hitEntity(ItemStack var1, EntityLivingBase var2, EntityLivingBase var3) {
      stack.damageItem(1, attacker);
      return true;
   }

   public boolean onBlockDestroyed(ItemStack var1, World var2, IBlockState var3, BlockPos var4, EntityLivingBase var5) {
      if ((double)state.getBlockHardness(worldIn, pos) != 0.0D) {
         stack.damageItem(2, entityLiving);
      }

      return true;
   }

   public boolean canHarvestBlock(IBlockState var1) {
      return blockIn.getBlock() == Blocks.WEB;
   }

   @SideOnly(Side.CLIENT)
   public boolean isFull3D() {
      return true;
   }

   public int getItemEnchantability() {
      return this.material.getEnchantability();
   }

   public String getToolMaterialName() {
      return this.material.toString();
   }

   public boolean getIsRepairable(ItemStack var1, ItemStack var2) {
      ItemStack mat = this.material.getRepairItemStack();
      return mat != null && OreDictionary.itemMatches(mat, repair, false) ? true : super.getIsRepairable(toRepair, repair);
   }

   public Multimap getItemAttributeModifiers(EntityEquipmentSlot var1) {
      Multimap multimap = super.getItemAttributeModifiers(equipmentSlot);
      if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
         multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", (double)this.attackDamage, 0));
         multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -2.4000000953674316D, 0));
      }

      return multimap;
   }
}
