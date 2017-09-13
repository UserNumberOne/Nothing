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
      var0.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackData("RecordPlayer", new String[]{"RecordItem"}));
   }

   protected BlockJukebox() {
      super(Material.WOOD, MapColor.DIRT);
      this.setDefaultState(this.blockState.getBaseState().withProperty(HAS_RECORD, Boolean.valueOf(false)));
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (((Boolean)var3.getValue(HAS_RECORD)).booleanValue()) {
         this.dropRecord(var1, var2, var3);
         var3 = var3.withProperty(HAS_RECORD, Boolean.valueOf(false));
         var1.setBlockState(var2, var3, 2);
         return true;
      } else {
         return false;
      }
   }

   public void insertRecord(World var1, BlockPos var2, IBlockState var3, ItemStack var4) {
      if (!var1.isRemote) {
         TileEntity var5 = var1.getTileEntity(var2);
         if (var5 instanceof BlockJukebox.TileEntityJukebox) {
            ((BlockJukebox.TileEntityJukebox)var5).setRecord(var4.copy());
            var1.setBlockState(var2, var3.withProperty(HAS_RECORD, Boolean.valueOf(true)), 2);
         }
      }

   }

   public void dropRecord(World var1, BlockPos var2, IBlockState var3) {
      if (!var1.isRemote) {
         TileEntity var4 = var1.getTileEntity(var2);
         if (var4 instanceof BlockJukebox.TileEntityJukebox) {
            BlockJukebox.TileEntityJukebox var5 = (BlockJukebox.TileEntityJukebox)var4;
            ItemStack var6 = var5.getRecord();
            if (var6 != null) {
               var1.playEvent(1010, var2, 0);
               var1.playRecord(var2, (SoundEvent)null);
               var5.setRecord((ItemStack)null);
               double var7 = (double)(var1.rand.nextFloat() * 0.7F) + 0.15000000596046448D;
               double var9 = (double)(var1.rand.nextFloat() * 0.7F) + 0.06000000238418579D + 0.6D;
               double var11 = (double)(var1.rand.nextFloat() * 0.7F) + 0.15000000596046448D;
               ItemStack var13 = var6.copy();
               EntityItem var14 = new EntityItem(var1, (double)var2.getX() + var7, (double)var2.getY() + var9, (double)var2.getZ() + var11, var13);
               var14.setDefaultPickupDelay();
               var1.spawnEntity(var14);
            }
         }
      }

   }

   public void breakBlock(World var1, BlockPos var2, IBlockState var3) {
      this.dropRecord(var1, var2, var3);
      super.breakBlock(var1, var2, var3);
   }

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      if (!var1.isRemote) {
         super.dropBlockAsItemWithChance(var1, var2, var3, var4, 0);
      }

   }

   public TileEntity createNewTileEntity(World var1, int var2) {
      return new BlockJukebox.TileEntityJukebox();
   }

   public boolean hasComparatorInputOverride(IBlockState var1) {
      return true;
   }

   public int getComparatorInputOverride(IBlockState var1, World var2, BlockPos var3) {
      TileEntity var4 = var2.getTileEntity(var3);
      if (var4 instanceof BlockJukebox.TileEntityJukebox) {
         ItemStack var5 = ((BlockJukebox.TileEntityJukebox)var4).getRecord();
         if (var5 != null) {
            return Item.getIdFromItem(var5.getItem()) + 1 - Item.getIdFromItem(Items.RECORD_13);
         }
      }

      return 0;
   }

   public EnumBlockRenderType getRenderType(IBlockState var1) {
      return EnumBlockRenderType.MODEL;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(HAS_RECORD, Boolean.valueOf(var1 > 0));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Boolean)var1.getValue(HAS_RECORD)).booleanValue() ? 1 : 0;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{HAS_RECORD});
   }

   public static class TileEntityJukebox extends TileEntity {
      private ItemStack record;

      public void readFromNBT(NBTTagCompound var1) {
         super.readFromNBT(var1);
         if (var1.hasKey("RecordItem", 10)) {
            this.setRecord(ItemStack.loadItemStackFromNBT(var1.getCompoundTag("RecordItem")));
         } else if (var1.getInteger("Record") > 0) {
            this.setRecord(new ItemStack(Item.getItemById(var1.getInteger("Record"))));
         }

      }

      public NBTTagCompound writeToNBT(NBTTagCompound var1) {
         super.writeToNBT(var1);
         if (this.getRecord() != null) {
            var1.setTag("RecordItem", this.getRecord().writeToNBT(new NBTTagCompound()));
         }

         return var1;
      }

      @Nullable
      public ItemStack getRecord() {
         return this.record;
      }

      public void setRecord(@Nullable ItemStack var1) {
         if (var1 != null) {
            var1.stackSize = 1;
         }

         this.record = var1;
         this.markDirty();
      }
   }
}
