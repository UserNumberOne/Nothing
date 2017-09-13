package net.minecraft.block;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockNewLeaf extends BlockLeaves {
   public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockPlanks.EnumType.class, new Predicate() {
      public boolean apply(@Nullable BlockPlanks.EnumType var1) {
         return var1.getMetadata() >= 4;
      }

      // $FF: synthetic method
      public boolean apply(Object var1) {
         return this.apply((BlockPlanks.EnumType)var1);
      }
   });

   public BlockNewLeaf() {
      this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockPlanks.EnumType.ACACIA).withProperty(CHECK_DECAY, Boolean.valueOf(true)).withProperty(DECAYABLE, Boolean.valueOf(true)));
   }

   protected void dropApple(World var1, BlockPos var2, IBlockState var3, int var4) {
      if (var3.getValue(VARIANT) == BlockPlanks.EnumType.DARK_OAK && var1.rand.nextInt(var4) == 0) {
         spawnAsEntity(var1, var2, new ItemStack(Items.APPLE));
      }

   }

   public int damageDropped(IBlockState var1) {
      return ((BlockPlanks.EnumType)var1.getValue(VARIANT)).getMetadata();
   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(this, 1, var3.getBlock().getMetaFromState(var3) & 3);
   }

   protected ItemStack getSilkTouchDrop(IBlockState var1) {
      return new ItemStack(Item.getItemFromBlock(this), 1, ((BlockPlanks.EnumType)var1.getValue(VARIANT)).getMetadata() - 4);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(VARIANT, this.getWoodType(var1)).withProperty(DECAYABLE, Boolean.valueOf((var1 & 4) == 0)).withProperty(CHECK_DECAY, Boolean.valueOf((var1 & 8) > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      var2 = var2 | ((BlockPlanks.EnumType)var1.getValue(VARIANT)).getMetadata() - 4;
      if (!((Boolean)var1.getValue(DECAYABLE)).booleanValue()) {
         var2 |= 4;
      }

      if (((Boolean)var1.getValue(CHECK_DECAY)).booleanValue()) {
         var2 |= 8;
      }

      return var2;
   }

   public BlockPlanks.EnumType getWoodType(int var1) {
      return BlockPlanks.EnumType.byMetadata((var1 & 3) + 4);
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{VARIANT, CHECK_DECAY, DECAYABLE});
   }

   public void harvestBlock(World var1, EntityPlayer var2, BlockPos var3, IBlockState var4, @Nullable TileEntity var5, @Nullable ItemStack var6) {
      if (!var1.isRemote && var6 != null && var6.getItem() == Items.SHEARS) {
         var2.addStat(StatList.getBlockStats(this));
         spawnAsEntity(var1, var3, new ItemStack(Item.getItemFromBlock(this), 1, ((BlockPlanks.EnumType)var4.getValue(VARIANT)).getMetadata() - 4));
      } else {
         super.harvestBlock(var1, var2, var3, var4, var5, var6);
      }
   }
}
