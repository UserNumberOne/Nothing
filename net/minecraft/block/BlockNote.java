package net.minecraft.block;

import com.google.common.collect.Lists;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityNote;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.NoteBlockEvent.Play;

public class BlockNote extends BlockContainer {
   private static final List INSTRUMENTS = Lists.newArrayList(new SoundEvent[]{SoundEvents.BLOCK_NOTE_HARP, SoundEvents.BLOCK_NOTE_BASEDRUM, SoundEvents.BLOCK_NOTE_SNARE, SoundEvents.BLOCK_NOTE_HAT, SoundEvents.BLOCK_NOTE_BASS});

   public BlockNote() {
      super(Material.WOOD);
      this.setCreativeTab(CreativeTabs.REDSTONE);
   }

   public void neighborChanged(IBlockState var1, World var2, BlockPos var3, Block var4) {
      boolean var5 = var2.isBlockPowered(var3);
      TileEntity var6 = var2.getTileEntity(var3);
      if (var6 instanceof TileEntityNote) {
         TileEntityNote var7 = (TileEntityNote)var6;
         if (var7.previousRedstoneState != var5) {
            if (var5) {
               var7.triggerNote(var2, var3);
            }

            var7.previousRedstoneState = var5;
         }
      }

   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (var1.isRemote) {
         return true;
      } else {
         TileEntity var11 = var1.getTileEntity(var2);
         if (var11 instanceof TileEntityNote) {
            TileEntityNote var12 = (TileEntityNote)var11;
            byte var13 = var12.note;
            var12.changePitch();
            if (var13 == var12.note) {
               return false;
            }

            var12.triggerNote(var1, var2);
            var4.addStat(StatList.NOTEBLOCK_TUNED);
         }

         return true;
      }
   }

   public void onBlockClicked(World var1, BlockPos var2, EntityPlayer var3) {
      if (!var1.isRemote) {
         TileEntity var4 = var1.getTileEntity(var2);
         if (var4 instanceof TileEntityNote) {
            ((TileEntityNote)var4).triggerNote(var1, var2);
            var3.addStat(StatList.NOTEBLOCK_PLAYED);
         }
      }

   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntityNote();
   }

   private SoundEvent getInstrument(int var1) {
      if (var1 < 0 || var1 >= INSTRUMENTS.size()) {
         var1 = 0;
      }

      return (SoundEvent)INSTRUMENTS.get(var1);
   }

   public boolean eventReceived(IBlockState var1, World var2, BlockPos var3, int var4, int var5) {
      Play var6 = new Play(var2, var3, var1, var5, var4);
      if (MinecraftForge.EVENT_BUS.post(var6)) {
         return false;
      } else {
         var4 = var6.getInstrument().ordinal();
         var5 = var6.getVanillaNoteId();
         float var7 = (float)Math.pow(2.0D, (double)(var5 - 12) / 12.0D);
         var2.playSound((EntityPlayer)null, var3, this.getInstrument(var4), SoundCategory.RECORDS, 3.0F, var7);
         var2.spawnParticle(EnumParticleTypes.NOTE, (double)var3.getX() + 0.5D, (double)var3.getY() + 1.2D, (double)var3.getZ() + 0.5D, (double)var5 / 24.0D, 0.0D, 0.0D);
         return true;
      }
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.MODEL;
   }
}
