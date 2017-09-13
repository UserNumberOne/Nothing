package net.minecraft.block;

import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerRepair;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlockAnvil extends BlockFalling {
   public static final PropertyDirection FACING = BlockHorizontal.FACING;
   public static final PropertyInteger DAMAGE = PropertyInteger.create("damage", 0, 2);
   protected static final AxisAlignedBB X_AXIS_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.125D, 1.0D, 1.0D, 0.875D);
   protected static final AxisAlignedBB Z_AXIS_AABB = new AxisAlignedBB(0.125D, 0.0D, 0.0D, 0.875D, 1.0D, 1.0D);
   protected static final Logger LOGGER = LogManager.getLogger();

   protected BlockAnvil() {
      super(Material.ANVIL);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(DAMAGE, Integer.valueOf(0)));
      this.setLightOpacity(0);
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public boolean isFullCube(IBlockState var1) {
      return false;
   }

   public boolean isOpaqueCube(IBlockState var1) {
      return false;
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      EnumFacing var9 = var8.getHorizontalFacing().rotateY();

      try {
         return super.getStateForPlacement(var1, var2, var3, var4, var5, var6, var7, var8).withProperty(FACING, var9).withProperty(DAMAGE, Integer.valueOf(var7 >> 2));
      } catch (IllegalArgumentException var11) {
         if (!var1.isRemote) {
            LOGGER.warn(String.format("Invalid damage property for anvil at %s. Found %d, must be in [0, 1, 2]", var2, var7 >> 2));
            if (var8 instanceof EntityPlayer) {
               ((EntityPlayer)var8).sendMessage(new TextComponentTranslation("Invalid damage property. Please pick in [0, 1, 2]", new Object[0]));
            }
         }

         return super.getStateForPlacement(var1, var2, var3, var4, var5, var6, 0, var8).withProperty(FACING, var9).withProperty(DAMAGE, Integer.valueOf(0));
      }
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (!var1.isRemote) {
         var4.displayGui(new BlockAnvil.Anvil(var1, var2));
      }

      return true;
   }

   public int damageDropped(IBlockState var1) {
      return ((Integer)var1.getValue(DAMAGE)).intValue();
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      EnumFacing var4 = (EnumFacing)var1.getValue(FACING);
      return var4.getAxis() == EnumFacing.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
   }

   protected void onStartFalling(EntityFallingBlock var1) {
      var1.setHurtEntities(true);
   }

   public void onEndFalling(World var1, BlockPos var2) {
      var1.playEvent(1031, var2, 0);
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(var1 & 3)).withProperty(DAMAGE, Integer.valueOf((var1 & 15) >> 2));
   }

   public int getMetaFromState(IBlockState var1) {
      int var2 = 0;
      var2 = var2 | ((EnumFacing)var1.getValue(FACING)).getHorizontalIndex();
      var2 = var2 | ((Integer)var1.getValue(DAMAGE)).intValue() << 2;
      return var2;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.getBlock() != this ? var1 : var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, DAMAGE});
   }

   public static class Anvil implements IInteractionObject {
      private final World world;
      private final BlockPos position;

      public Anvil(World var1, BlockPos var2) {
         this.world = var1;
         this.position = var2;
      }

      public String getName() {
         return "anvil";
      }

      public boolean hasCustomName() {
         return false;
      }

      public ITextComponent getDisplayName() {
         return new TextComponentTranslation(Blocks.ANVIL.getUnlocalizedName() + ".name", new Object[0]);
      }

      public Container createContainer(InventoryPlayer var1, EntityPlayer var2) {
         return new ContainerRepair(var1, this.world, this.position, var2);
      }

      public String getGuiID() {
         return "minecraft:anvil";
      }
   }
}
