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
      this.theToolMaterial = var1;
      this.maxStackSize = 1;
      this.setMaxDamage(var1.getMaxUses());
      this.setCreativeTab(CreativeTabs.TOOLS);
      this.speed = var1.getDamageVsEntity() + 1.0F;
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (!var2.canPlayerEdit(var4.offset(var6), var6, var1)) {
         return EnumActionResult.FAIL;
      } else {
         int var10 = ForgeEventFactory.onHoeUse(var1, var2, var3, var4);
         if (var10 != 0) {
            return var10 > 0 ? EnumActionResult.SUCCESS : EnumActionResult.FAIL;
         } else {
            IBlockState var11 = var3.getBlockState(var4);
            Block var12 = var11.getBlock();
            if (var6 != EnumFacing.DOWN && var3.isAirBlock(var4.up())) {
               if (var12 == Blocks.GRASS || var12 == Blocks.GRASS_PATH) {
                  this.setBlock(var1, var2, var3, var4, Blocks.FARMLAND.getDefaultState());
                  return EnumActionResult.SUCCESS;
               }

               if (var12 == Blocks.DIRT) {
                  switch((BlockDirt.DirtType)var11.getValue(BlockDirt.VARIANT)) {
                  case DIRT:
                     this.setBlock(var1, var2, var3, var4, Blocks.FARMLAND.getDefaultState());
                     return EnumActionResult.SUCCESS;
                  case COARSE_DIRT:
                     this.setBlock(var1, var2, var3, var4, Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT));
                     return EnumActionResult.SUCCESS;
                  }
               }
            }

            return EnumActionResult.PASS;
         }
      }
   }

   public boolean hitEntity(ItemStack var1, EntityLivingBase var2, EntityLivingBase var3) {
      var1.damageItem(1, var3);
      return true;
   }

   protected void setBlock(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, IBlockState var5) {
      var3.playSound(var2, var4, SoundEvents.ITEM_HOE_TILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
      if (!var3.isRemote) {
         var3.setBlockState(var4, var5, 11);
         var1.damageItem(1, var2);
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
      Multimap var2 = super.getItemAttributeModifiers(var1);
      if (var1 == EntityEquipmentSlot.MAINHAND) {
         var2.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", 0.0D, 0));
         var2.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", (double)(this.speed - 4.0F), 0));
      }

      return var2;
   }
}
