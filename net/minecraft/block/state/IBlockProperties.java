package net.minecraft.block.state;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.material.EnumPushReaction;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface IBlockProperties {
   Material getMaterial();

   boolean isFullBlock();

   boolean canEntitySpawn(Entity var1);

   int getLightOpacity();

   int getLightValue();

   boolean useNeighborBrightness();

   MapColor getMapColor();

   IBlockState withRotation(Rotation var1);

   IBlockState withMirror(Mirror var1);

   boolean isFullCube();

   EnumBlockRenderType getRenderType();

   boolean isBlockNormalCube();

   boolean isNormalCube();

   boolean canProvidePower();

   int getWeakPower(IBlockAccess var1, BlockPos var2, EnumFacing var3);

   boolean hasComparatorInputOverride();

   int getComparatorInputOverride(World var1, BlockPos var2);

   float getBlockHardness(World var1, BlockPos var2);

   float getPlayerRelativeBlockHardness(EntityPlayer var1, World var2, BlockPos var3);

   int getStrongPower(IBlockAccess var1, BlockPos var2, EnumFacing var3);

   EnumPushReaction getMobilityFlag();

   IBlockState getActualState(IBlockAccess var1, BlockPos var2);

   boolean isOpaqueCube();

   @Nullable
   AxisAlignedBB getCollisionBoundingBox(World var1, BlockPos var2);

   void addCollisionBoxToList(World var1, BlockPos var2, AxisAlignedBB var3, List var4, @Nullable Entity var5);

   AxisAlignedBB getBoundingBox(IBlockAccess var1, BlockPos var2);

   RayTraceResult collisionRayTrace(World var1, BlockPos var2, Vec3d var3, Vec3d var4);

   boolean isFullyOpaque();
}
