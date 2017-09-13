package net.minecraft.block;

import java.util.List;
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
import net.minecraft.item.Item;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
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
      EnumFacing enumfacing = placer.getHorizontalFacing().rotateY();

      try {
         return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(FACING, enumfacing).withProperty(DAMAGE, Integer.valueOf(meta >> 2));
      } catch (IllegalArgumentException var11) {
         if (!worldIn.isRemote) {
            LOGGER.warn(String.format("Invalid damage property for anvil at %s. Found %d, must be in [0, 1, 2]", pos, meta >> 2));
            if (placer instanceof EntityPlayer) {
               ((EntityPlayer)placer).sendMessage(new TextComponentTranslation("Invalid damage property. Please pick in [0, 1, 2]", new Object[0]));
            }
         }

         return super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, 0, placer).withProperty(FACING, enumfacing).withProperty(DAMAGE, Integer.valueOf(0));
      }
   }

   public boolean onBlockActivated(World var1, BlockPos var2, IBlockState var3, EntityPlayer var4, EnumHand var5, @Nullable ItemStack var6, EnumFacing var7, float var8, float var9, float var10) {
      if (!worldIn.isRemote) {
         playerIn.displayGui(new BlockAnvil.Anvil(worldIn, pos));
      }

      return true;
   }

   public int damageDropped(IBlockState var1) {
      return ((Integer)state.getValue(DAMAGE)).intValue();
   }

   public AxisAlignedBB getBoundingBox(IBlockState var1, IBlockAccess var2, BlockPos var3) {
      EnumFacing enumfacing = (EnumFacing)state.getValue(FACING);
      return enumfacing.getAxis() == EnumFacing.Axis.X ? X_AXIS_AABB : Z_AXIS_AABB;
   }

   @SideOnly(Side.CLIENT)
   public void getSubBlocks(Item var1, CreativeTabs var2, List var3) {
      list.add(new ItemStack(itemIn));
      list.add(new ItemStack(itemIn, 1, 1));
      list.add(new ItemStack(itemIn, 1, 2));
   }

   protected void onStartFalling(EntityFallingBlock var1) {
      fallingEntity.setHurtEntities(true);
   }

   public void onEndFalling(World var1, BlockPos var2) {
      worldIn.playEvent(1031, pos, 0);
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldSideBeRendered(IBlockState var1, IBlockAccess var2, BlockPos var3, EnumFacing var4) {
      return true;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta & 3)).withProperty(DAMAGE, Integer.valueOf((meta & 15) >> 2));
   }

   public int getMetaFromState(IBlockState var1) {
      int i = 0;
      i = i | ((EnumFacing)state.getValue(FACING)).getHorizontalIndex();
      i = i | ((Integer)state.getValue(DAMAGE)).intValue() << 2;
      return i;
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return state.getBlock() != this ? state : state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING, DAMAGE});
   }

   public static class Anvil implements IInteractionObject {
      private final World world;
      private final BlockPos position;

      public Anvil(World var1, BlockPos var2) {
         this.world = worldIn;
         this.position = pos;
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
         return new ContainerRepair(playerInventory, this.world, this.position, playerIn);
      }

      public String getGuiID() {
         return "minecraft:anvil";
      }
   }
}
