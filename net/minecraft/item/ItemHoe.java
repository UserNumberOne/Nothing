package net.minecraft.item;

import com.google.common.collect.Multimap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemHoe extends Item {
   private final float speed;
   protected Item.ToolMaterial theToolMaterial;

   public ItemHoe(Item.ToolMaterial var1) {
      this.theToolMaterial = material;
      this.maxStackSize = 1;
      this.setMaxDamage(material.getMaxUses());
      this.setCreativeTab(CreativeTabs.TOOLS);
      this.speed = material.getDamageVsEntity() + 1.0F;
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (!playerIn.canPlayerEdit(pos.offset(facing), facing, stack)) {
         return EnumActionResult.FAIL;
      } else {
         int hook = ForgeEventFactory.onHoeUse(stack, playerIn, worldIn, pos);
         if (hook != 0) {
            return hook > 0 ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
         } else {
            IBlockState iblockstate = worldIn.getBlockState(pos);
            Block block = iblockstate.getBlock();
            if (facing != EnumFacing.DOWN && worldIn.isAirBlock(pos.up())) {
               if (block == Blocks.GRASS || block == Blocks.GRASS_PATH) {
                  this.setBlock(stack, playerIn, worldIn, pos, Blocks.FARMLAND.getDefaultState());
                  return EnumActionResult.SUCCESS;
               }

               if (block == Blocks.DIRT) {
                  switch((BlockDirt.DirtType)iblockstate.getValue(BlockDirt.VARIANT)) {
                  case DIRT:
                     this.setBlock(stack, playerIn, worldIn, pos, Blocks.FARMLAND.getDefaultState());
                     return EnumActionResult.SUCCESS;
                  case COARSE_DIRT:
                     this.setBlock(stack, playerIn, worldIn, pos, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT));
                     return EnumActionResult.SUCCESS;
                  }
               }
            }

            return EnumActionResult.PASS;
         }
      }
   }

   public boolean hitEntity(ItemStack var1, EntityLivingBase var2, EntityLivingBase var3) {
      stack.damageItem(1, attacker);
      return true;
   }

   protected void setBlock(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, IBlockState var5) {
      worldIn.playSound(player, pos, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
      if (!worldIn.isRemote) {
         worldIn.setBlockState(pos, state, 11);
         stack.damageItem(1, player);
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean isFull3D() {
      return true;
   }

   public String getMaterialName() {
      return this.theToolMaterial.toString();
   }

   public Multimap getItemAttributeModifiers(EntityEquipmentSlot var1) {
      Multimap multimap = super.getItemAttributeModifiers(equipmentSlot);
      if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
         multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 0.0D, 0));
         multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", (double)(this.speed - 4.0F), 0));
      }

      return multimap;
   }
}
