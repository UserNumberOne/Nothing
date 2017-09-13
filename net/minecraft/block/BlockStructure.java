package net.minecraft.block;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockStructure extends BlockContainer {
   public static final PropertyEnum MODE = PropertyEnum.create("mode", TileEntityStructure.Mode.class);

   public BlockStructure() {
      super(Material.IRON, MapColor.SILVER);
      this.setDefaultState(this.blockState.getBaseState());
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntityStructure();
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      return tileentity instanceof TileEntityStructure ? ((TileEntityStructure)tileentity).usedBy(playerIn) : false;
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
      if (!worldIn.isRemote) {
         TileEntity tileentity = worldIn.getTileEntity(pos);
         if (tileentity instanceof TileEntityStructure) {
            TileEntityStructure tileentitystructure = (TileEntityStructure)tileentity;
            tileentitystructure.createdBy(placer);
         }
      }

   }

   @Nullable
   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return super.getItem(worldIn, pos, state);
   }

   public int quantityDropped(Random var1) {
      return 0;
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.MODEL;
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getDefaultState().withProperty(MODE, TileEntityStructure.Mode.DATA);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(MODE, TileEntityStructure.Mode.getById(meta));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((TileEntityStructure.Mode)state.getValue(MODE)).getModeId();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{MODE});
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      if (!worldIn.isRemote) {
         TileEntity tileentity = worldIn.getTileEntity(pos);
         if (tileentity instanceof TileEntityStructure) {
            TileEntityStructure tileentitystructure = (TileEntityStructure)tileentity;
            boolean flag = worldIn.isBlockPowered(pos);
            boolean flag1 = tileentitystructure.isPowered();
            if (flag && !flag1) {
               tileentitystructure.setPowered(true);
               this.trigger(tileentitystructure);
            } else if (!flag && flag1) {
               tileentitystructure.setPowered(false);
            }
         }
      }

   }

   private void trigger(TileEntityStructure var1) {
      switch(p_189874_1_.getMode()) {
      case SAVE:
         p_189874_1_.save(false);
         break;
      case LOAD:
         p_189874_1_.load(false);
         break;
      case CORNER:
         p_189874_1_.unloadStructure();
      case DATA:
      }

   }
}
