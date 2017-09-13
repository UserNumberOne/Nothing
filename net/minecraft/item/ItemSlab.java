package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemSlab extends ItemBlock {
   private final BlockSlab singleSlab;
   private final BlockSlab doubleSlab;

   public ItemSlab(Block var1, BlockSlab var2, BlockSlab var3) {
      super(var1);
      this.singleSlab = var2;
      this.doubleSlab = var3;
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public int getMetadata(int var1) {
      return var1;
   }

   public String getUnlocalizedName(ItemStack var1) {
      return this.singleSlab.getUnlocalizedName(var1.getMetadata());
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (var1.stackSize != 0 && var2.canPlayerEdit(var4.offset(var6), var6, var1)) {
         Comparable var10 = this.singleSlab.getTypeForItem(var1);
         IBlockState var11 = var3.getBlockState(var4);
         if (var11.getBlock() == this.singleSlab) {
            IProperty var12 = this.singleSlab.getVariantProperty();
            Comparable var13 = var11.getValue(var12);
            BlockSlab.EnumBlockHalf var14 = (BlockSlab.EnumBlockHalf)var11.getValue(BlockSlab.HALF);
            if ((var6 == EnumFacing.UP && var14 == BlockSlab.EnumBlockHalf.BOTTOM || var6 == EnumFacing.DOWN && var14 == BlockSlab.EnumBlockHalf.TOP) && var13 == var10) {
               IBlockState var15 = this.makeState(var12, var13);
               AxisAlignedBB var16 = var15.getCollisionBoundingBox(var3, var4);
               if (var16 != Block.NULL_AABB && var3.checkNoEntityCollision(var16.offset(var4)) && var3.setBlockState(var4, var15, 11)) {
                  SoundType var17 = this.doubleSlab.getSoundType(var15, var3, var4, var2);
                  var3.playSound(var2, var4, var17.getPlaceSound(), SoundCategory.BLOCKS, (var17.getVolume() + 1.0F) / 2.0F, var17.getPitch() * 0.8F);
                  --var1.stackSize;
               }

               return EnumActionResult.SUCCESS;
            }
         }

         return this.tryPlace(var2, var1, var3, var4.offset(var6), var10) ? EnumActionResult.SUCCESS : super.onItemUse(var1, var2, var3, var4, var5, var6, var7, var8, var9);
      } else {
         return EnumActionResult.FAIL;
      }
   }

   @SideOnly(Side.CLIENT)
   public boolean canPlaceBlockOnSide(World var1, BlockPos var2, EnumFacing var3, EntityPlayer var4, ItemStack var5) {
      BlockPos var6 = var2;
      IProperty var7 = this.singleSlab.getVariantProperty();
      Comparable var8 = this.singleSlab.getTypeForItem(var5);
      IBlockState var9 = var1.getBlockState(var2);
      if (var9.getBlock() == this.singleSlab) {
         boolean var10 = var9.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;
         if ((var3 == EnumFacing.UP && !var10 || var3 == EnumFacing.DOWN && var10) && var8 == var9.getValue(var7)) {
            return true;
         }
      }

      var2 = var2.offset(var3);
      IBlockState var12 = var1.getBlockState(var2);
      return var12.getBlock() == this.singleSlab && var8 == var12.getValue(var7) ? true : super.canPlaceBlockOnSide(var1, var6, var3, var4, var5);
   }

   private boolean tryPlace(EntityPlayer var1, ItemStack var2, World var3, BlockPos var4, Object var5) {
      IBlockState var6 = var3.getBlockState(var4);
      if (var6.getBlock() == this.singleSlab) {
         Comparable var7 = var6.getValue(this.singleSlab.getVariantProperty());
         if (var7 == var5) {
            IBlockState var8 = this.makeState(this.singleSlab.getVariantProperty(), var7);
            AxisAlignedBB var9 = var8.getCollisionBoundingBox(var3, var4);
            if (var9 != Block.NULL_AABB && var3.checkNoEntityCollision(var9.offset(var4)) && var3.setBlockState(var4, var8, 11)) {
               SoundType var10 = this.doubleSlab.getSoundType(var8, var3, var4, var1);
               var3.playSound(var1, var4, var10.getPlaceSound(), SoundCategory.BLOCKS, (var10.getVolume() + 1.0F) / 2.0F, var10.getPitch() * 0.8F);
               --var2.stackSize;
            }

            return true;
         }
      }

      return false;
   }

   protected IBlockState makeState(IProperty var1, Comparable var2) {
      return this.doubleSlab.getDefaultState().withProperty(var1, var2);
   }
}
