package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBeacon;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.HttpUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockBeacon extends BlockContainer {
   public BlockBeacon() {
      super(Material.GLASS, MapColor.DIAMOND);
      this.setHardness(3.0F);
      this.setCreativeTab(CreativeTabs.MISC);
   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntityBeacon();
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (var1.isRemote) {
         return true;
      } else {
         TileEntity var11 = var1.getTileEntity(var2);
         if (var11 instanceof TileEntityBeacon) {
            var4.displayGUIChest((TileEntityBeacon)var11);
            var4.addStat(StatList.BEACON_INTERACTION);
         }

         return true;
      }
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.MODEL;
   }

   public void onBlockPlacedBy(World var1, BlockPos var2, IBlockState var3, EntityLivingBase var4, ItemStack var5) {
      super.onBlockPlacedBy(var1, var2, var3, var4, var5);
      if (var5.hasDisplayName()) {
         TileEntity var6 = var1.getTileEntity(var2);
         if (var6 instanceof TileEntityBeacon) {
            ((TileEntityBeacon)var6).setName(var5.getDisplayName());
         }
      }

   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      TileEntity var5 = var2.getTileEntity(var3);
      if (var5 instanceof TileEntityBeacon) {
         ((TileEntityBeacon)var5).updateBeacon();
         var2.addBlockEvent(var3, this, 1, 0);
      }

   }

   @SideOnly(Side.CLIENT)
   public BlockRenderLayer getBlockLayer() {
      return BlockRenderLayer.CUTOUT;
   }

   public static void updateColorAsync(final World var0, final BlockPos var1) {
      HttpUtil.DOWNLOADER_EXECUTOR.submit(new Runnable() {
         public void run() {
            Chunk var1x = var0.getChunkFromBlockCoords(var1);

            for(int var2 = var1.getY() - 1; var2 >= 0; --var2) {
               final BlockPos var3 = new BlockPos(var1.getX(), var2, var1.getZ());
               if (!var1x.canSeeSky(var3)) {
                  break;
               }

               IBlockState var4 = var0.getBlockState(var3);
               if (var4.getBlock() == Blocks.BEACON) {
                  ((WorldServer)var0).addScheduledTask(new Runnable() {
                     public void run() {
                        TileEntity var1x = var0.getTileEntity(var3);
                        if (var1x instanceof TileEntityBeacon) {
                           ((TileEntityBeacon)var1x).updateBeacon();
                           var0.addBlockEvent(var3, Blocks.BEACON, 1, 0);
                        }

                     }
                  });
               }
            }

         }
      });
   }
}
