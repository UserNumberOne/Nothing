package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockPressurePlateWeighted extends BlockBasePressurePlate {
   public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
   private final int maxWeight;

   protected BlockPressurePlateWeighted(Material var1, int var2) {
      this(materialIn, p_i46379_2_, materialIn.getMaterialMapColor());
   }

   protected BlockPressurePlateWeighted(Material var1, int var2, MapColor var3) {
      super(materialIn, color);
      this.setDefaultState(this.blockState.getBaseState().withProperty(POWER, Integer.valueOf(0)));
      this.maxWeight = p_i46380_2_;
   }

   protected int computeRedstoneStrength(World var1, BlockPos var2) {
      int i = Math.min(worldIn.getEntitiesWithinAABB(Entity.class, PRESSURE_AABB.offset(pos)).size(), this.maxWeight);
      if (i > 0) {
         float f = (float)Math.min(this.maxWeight, i) / (float)this.maxWeight;
         return MathHelper.ceil(f * 15.0F);
      } else {
         return 0;
      }
   }

   protected void playClickOnSound(World var1, BlockPos var2) {
      worldIn.playSound((EntityPlayer)null, color, SoundEvents.BLOCK_METAL_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.90000004F);
   }

   protected void playClickOffSound(World var1, BlockPos var2) {
      worldIn.playSound((EntityPlayer)null, pos, SoundEvents.BLOCK_METAL_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.75F);
   }

   protected int getRedstoneStrength(IBlockState var1) {
      return ((Integer)state.getValue(POWER)).intValue();
   }

   protected IBlockState setRedstoneStrength(IBlockState var1, int var2) {
      return state.withProperty(POWER, Integer.valueOf(strength));
   }

   public int tickRate(World var1) {
      return 10;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(POWER, Integer.valueOf(meta));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)state.getValue(POWER)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{POWER});
   }
}
