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
      boolean flag = worldIn.isBlockPowered(pos);
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof TileEntityNote) {
         TileEntityNote tileentitynote = (TileEntityNote)tileentity;
         if (tileentitynote.previousRedstoneState != flag) {
            if (flag) {
               tileentitynote.triggerNote(worldIn, pos);
            }

            tileentitynote.previousRedstoneState = flag;
         }
      }

   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (worldIn.isRemote) {
         return true;
      } else {
         TileEntity tileentity = worldIn.getTileEntity(pos);
         if (tileentity instanceof TileEntityNote) {
            TileEntityNote tileentitynote = (TileEntityNote)tileentity;
            int old = tileentitynote.note;
            tileentitynote.changePitch();
            if (old == tileentitynote.note) {
               return false;
            }

            tileentitynote.triggerNote(worldIn, pos);
            playerIn.addStat(StatList.NOTEBLOCK_TUNED);
         }

         return true;
      }
   }

   public void onBlockClicked(World var1, BlockPos var2, EntityPlayer var3) {
      if (!worldIn.isRemote) {
         TileEntity tileentity = worldIn.getTileEntity(pos);
         if (tileentity instanceof TileEntityNote) {
            ((TileEntityNote)tileentity).triggerNote(worldIn, pos);
            playerIn.addStat(StatList.NOTEBLOCK_PLAYED);
         }
      }

   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new TileEntityNote();
   }

   private SoundEvent getInstrument(int var1) {
      if (p_185576_1_ < 0 || p_185576_1_ >= INSTRUMENTS.size()) {
         p_185576_1_ = 0;
      }

      return (SoundEvent)INSTRUMENTS.get(p_185576_1_);
   }

   public boolean eventReceived(IBlockState var1, World var2, BlockPos var3, int var4, int var5) {
      Play e = new Play(worldIn, pos, state, param, id);
      if (MinecraftForge.EVENT_BUS.post(e)) {
         return false;
      } else {
         id = e.getInstrument().ordinal();
         param = e.getVanillaNoteId();
         float f = (float)Math.pow(2.0D, (double)(param - 12) / 12.0D);
         worldIn.playSound((EntityPlayer)null, pos, this.getInstrument(id), SoundCategory.RECORDS, 3.0F, f);
         worldIn.spawnParticle(EnumParticleTypes.NOTE, (double)pos.getX() + 0.5D, (double)pos.getY() + 1.2D, (double)pos.getZ() + 0.5D, (double)param / 24.0D, 0.0D, 0.0D);
         return true;
      }
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.MODEL;
   }
}
