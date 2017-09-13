package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackData;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockJukebox extends BlockContainer {
   public static final PropertyBool HAS_RECORD = PropertyBool.create("has_record");

   public static void registerFixesJukebox(DataFixer var0) {
      fixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackData("RecordPlayer", new String[]{"RecordItem"}));
   }

   protected BlockJukebox() {
      super(Material.WOOD, MapColor.DIRT);
      this.setDefaultState(this.blockState.getBaseState().withProperty(HAS_RECORD, Boolean.valueOf(false)));
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (((Boolean)state.getValue(HAS_RECORD)).booleanValue()) {
         this.dropRecord(worldIn, pos, state);
         state = state.withProperty(HAS_RECORD, Boolean.valueOf(false));
         worldIn.setBlockState(pos, state, 2);
         return true;
      } else {
         return false;
      }
   }

   public void insertRecord(World var1, BlockPos var2, IBlockState var3, ItemStack var4) {
      if (!worldIn.isRemote) {
         TileEntity tileentity = worldIn.getTileEntity(pos);
         if (tileentity instanceof BlockJukebox.TileEntityJukebox) {
            ((BlockJukebox.TileEntityJukebox)tileentity).setRecord(recordStack.copy());
            worldIn.setBlockState(pos, state.withProperty(HAS_RECORD, Boolean.valueOf(true)), 2);
         }
      }

   }

   private void dropRecord(World var1, BlockPos var2, IBlockState var3) {
      if (!worldIn.isRemote) {
         TileEntity tileentity = worldIn.getTileEntity(pos);
         if (tileentity instanceof BlockJukebox.TileEntityJukebox) {
            BlockJukebox.TileEntityJukebox blockjukebox$tileentityjukebox = (BlockJukebox.TileEntityJukebox)tileentity;
            ItemStack itemstack = blockjukebox$tileentityjukebox.getRecord();
            if (itemstack != null) {
               worldIn.playEvent(1010, pos, 0);
               worldIn.playRecord(pos, (SoundEvent)null);
               blockjukebox$tileentityjukebox.setRecord((ItemStack)null);
               float f = 0.7F;
               double d0 = (double)(worldIn.rand.nextFloat() * 0.7F) + 0.15000000596046448D;
               double d1 = (double)(worldIn.rand.nextFloat() * 0.7F) + 0.06000000238418579D + 0.6D;
               double d2 = (double)(worldIn.rand.nextFloat() * 0.7F) + 0.15000000596046448D;
               ItemStack itemstack1 = itemstack.copy();
               EntityItem entityitem = new EntityItem(worldIn, (double)pos.getX() + d0, (double)pos.getY() + d1, (double)pos.getZ() + d2, itemstack1);
               entityitem.setDefaultPickupDelay();
               worldIn.spawnEntity(entityitem);
            }
         }
      }

   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      this.dropRecord(worldIn, pos, state);
      super.breakBlock(worldIn, pos, state);
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      if (!worldIn.isRemote) {
         super.dropBlockAsItemWithChance(worldIn, pos, state, chance, 0);
      }

   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new BlockJukebox.TileEntityJukebox();
   }

   public boolean hasComparatorInputOverride(IBlockState var1) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState var1, World var2, BlockPos var3) {
      TileEntity tileentity = worldIn.getTileEntity(pos);
      if (tileentity instanceof BlockJukebox.TileEntityJukebox) {
         ItemStack itemstack = ((BlockJukebox.TileEntityJukebox)tileentity).getRecord();
         if (itemstack != null) {
            return Item.getIdFromItem(itemstack.getItem()) + 1 - Item.getIdFromItem(Items.RECORD_13);
         }
      }

      return 0;
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.MODEL;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(HAS_RECORD, Boolean.valueOf(meta > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Boolean)state.getValue(HAS_RECORD)).booleanValue() ? 1 : 0;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{HAS_RECORD});
   }

   public static class TileEntityJukebox extends TileEntity {
      private ItemStack record;

      public void readFromNBT(NBTTagCompound var1) {
         super.readFromNBT(compound);
         if (compound.hasKey("RecordItem", 10)) {
            this.setRecord(ItemStack.loadItemStackFromNBT(compound.getCompoundTag("RecordItem")));
         } else if (compound.getInteger("Record") > 0) {
            this.setRecord(new ItemStack(Item.getItemById(compound.getInteger("Record"))));
         }

      }

      public NBTTagCompound writeToNBT(NBTTagCompound var1) {
         super.writeToNBT(compound);
         if (this.getRecord() != null) {
            compound.setTag("RecordItem", this.getRecord().writeToNBT(new NBTTagCompound()));
         }

         return compound;
      }

      @Nullable
      public ItemStack getRecord() {
         return this.record;
      }

      public void setRecord(@Nullable ItemStack var1) {
         this.record = recordStack;
         this.markDirty();
      }
   }
}
