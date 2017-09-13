package net.minecraft.block;

import com.google.common.base.Predicate;
import java.util.Arrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockNewLeaf extends BlockLeaves {
   public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockPlanks.EnumType.class, new Predicate() {
      public boolean apply(@Nullable BlockPlanks.EnumType var1) {
         return p_apply_1_.getMetadata() >= 4;
      }
   });

   public BlockNewLeaf() {
      this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(CHECK_DECAY, Boolean.valueOf(true)).withProperty(DECAYABLE, Boolean.valueOf(true)));
   }

   protected void dropApple(World var1, BlockPos var2, IBlockState var3, int var4) {
      if (state.getValue(VARIANT) == BlockPlanks.EnumType.DARK_OAK && worldIn.rand.nextInt(chance) == 0) {
         spawnAsEntity(worldIn, pos, new ItemStack(Items.APPLE));
      }

   }

   public int damageDropped(IBlockState var1) {
      return ((BlockPlanks.EnumType)state.getValue(VARIANT)).getMetadata();
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(this, 1, state.getBlock().getMetaFromState(state) & 3);
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      list.add(new ItemStack(itemIn, 1, 0));
      list.add(new ItemStack(itemIn, 1, 1));
   }

   protected ItemStack getSilkTouchDrop(IBlockState var1) {
      return new ItemStack(Item.getItemFromBlock(this), 1, ((BlockPlanks.EnumType)state.getValue(VARIANT)).getMetadata() - 4);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(VARIANT, this.getWoodType(meta)).withProperty(DECAYABLE, Boolean.valueOf((meta & 4) == 0)).withProperty(CHECK_DECAY, Boolean.valueOf((meta & 8) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      int i = 0;
      i = i | ((BlockPlanks.EnumType)state.getValue(VARIANT)).getMetadata() - 4;
      if (!((Boolean)state.getValue(DECAYABLE)).booleanValue()) {
         i |= 4;
      }

      if (((Boolean)state.getValue(CHECK_DECAY)).booleanValue()) {
         i |= 8;
      }

      return i;
   }

   public BlockPlanks.EnumType getWoodType(int var1) {
      return BlockPlanks.EnumType.byMetadata((meta & 3) + 4);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{VARIANT, CHECK_DECAY, DECAYABLE});
   }

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, @Nullable TileEntity var5, @Nullable ItemStack var6) {
      super.harvestBlock(worldIn, player, pos, state, te, stack);
   }

   public List onSheared(ItemStack var1, IBlockAccess var2, BlockPos var3, int var4) {
      return Arrays.asList(new ItemStack(this, 1, ((BlockPlanks.EnumType)world.getBlockState(pos).getValue(VARIANT)).getMetadata() - 4));
   }
}
