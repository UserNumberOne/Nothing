package net.minecraft.block;

import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndPortal;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.bukkit.Location;
import org.bukkit.event.entity.EntityPortalEnterEvent;

public class BlockEndPortal extends BlockContainer {
   protected static final AxisAlignedBB END_PORTAL_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D);

   protected BlockEndPortal(Material material) {
      super(material);
      this.setLightLevel(1.0F);
   }

   public TileEntity createNewTileEntity(World world, int i) {
      return new TileEntityEndPortal();
   }

   public AxisAlignedBB getBoundingBox(IBlockState iblockdata, IBlockAccess iblockaccess, BlockPos blockposition) {
      return END_PORTAL_AABB;
   }

   public void addCollisionBoxToList(IBlockState iblockdata, World world, BlockPos blockposition, AxisAlignedBB axisalignedbb, List list, @Nullable Entity entity) {
   }

   public boolean isOpaqueCube(IBlockState iblockdata) {
      return false;
   }

   public boolean isFullCube(IBlockState iblockdata) {
      return false;
   }

   public int quantityDropped(Random random) {
      return 0;
   }

   public void onEntityCollidedWithBlock(World world, BlockPos blockposition, IBlockState iblockdata, Entity entity) {
      if (!entity.isRiding() && !entity.isBeingRidden() && entity.isNonBoss() && !world.isRemote && entity.getEntityBoundingBox().intersectsWith(iblockdata.getBoundingBox(world, blockposition).offset(blockposition))) {
         EntityPortalEnterEvent event = new EntityPortalEnterEvent(entity.getBukkitEntity(), new Location(world.getWorld(), (double)blockposition.getX(), (double)blockposition.getY(), (double)blockposition.getZ()));
         world.getServer().getPluginManager().callEvent(event);
         entity.changeDimension(1);
      }

   }

   @Nullable
   public ItemStack getItem(World world, BlockPos blockposition, IBlockState iblockdata) {
      return null;
   }

   public MapColor getMapColor(IBlockState iblockdata) {
      return MapColor.BLACK;
   }
}
