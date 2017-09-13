package net.minecraft.block;

import com.google.common.base.Predicate;
import javax.annotation.Nullable;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockWorldState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.pattern.BlockMaterialMatcher;
import net.minecraft.block.state.pattern.BlockPattern;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.block.state.pattern.FactoryBlockPattern;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.util.BlockStateListPopulator;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class BlockPumpkin extends BlockHorizontal {
   private BlockPattern snowmanBasePattern;
   private BlockPattern snowmanPattern;
   private BlockPattern golemBasePattern;
   private BlockPattern golemPattern;
   private static final Predicate IS_PUMPKIN = new Predicate() {
      public boolean apply(@Nullable IBlockState iblockdata) {
         return iblockdata != null && (iblockdata.getBlock() == Blocks.PUMPKIN || iblockdata.getBlock() == Blocks.LIT_PUMPKIN);
      }

      public boolean apply(Object object) {
         return this.apply((IBlockState)object);
      }
   };

   protected BlockPumpkin() {
      super(Material.GOURD, MapColor.ADOBE);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public void onBlockAdded(World world, BlockPos blockposition, IBlockState iblockdata) {
      super.onBlockAdded(world, blockposition, iblockdata);
      this.trySpawnGolem(world, blockposition);
   }

   public boolean canDispenserPlace(World world, BlockPos blockposition) {
      return this.getSnowmanBasePattern().match(world, blockposition) != null || this.getGolemBasePattern().match(world, blockposition) != null;
   }

   private void trySpawnGolem(World world, BlockPos blockposition) {
      BlockPattern.PatternHelper shapedetector_shapedetectorcollection = this.getSnowmanPattern().match(world, blockposition);
      BlockStateListPopulator blockList = new BlockStateListPopulator(world.getWorld());
      if (shapedetector_shapedetectorcollection != null) {
         for(int i = 0; i < this.getSnowmanPattern().getThumbLength(); ++i) {
            BlockWorldState shapedetectorblock = shapedetector_shapedetectorcollection.translateOffset(0, i, 0);
            BlockPos pos = shapedetectorblock.getPos();
            blockList.setTypeId(pos.getX(), pos.getY(), pos.getZ(), 0);
         }

         EntitySnowman entitysnowman = new EntitySnowman(world);
         BlockPos blockposition1 = shapedetector_shapedetectorcollection.translateOffset(0, 2, 0).getPos();
         entitysnowman.setLocationAndAngles((double)blockposition1.getX() + 0.5D, (double)blockposition1.getY() + 0.05D, (double)blockposition1.getZ() + 0.5D, 0.0F, 0.0F);
         if (world.addEntity(entitysnowman, SpawnReason.BUILD_SNOWMAN)) {
            blockList.updateList();

            for(int j = 0; j < 120; ++j) {
               world.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, (double)blockposition1.getX() + world.rand.nextDouble(), (double)blockposition1.getY() + world.rand.nextDouble() * 2.5D, (double)blockposition1.getZ() + world.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
            }

            for(int var19 = 0; var19 < this.getSnowmanPattern().getThumbLength(); ++var19) {
               BlockWorldState shapedetectorblock1 = shapedetector_shapedetectorcollection.translateOffset(0, var19, 0);
               world.notifyNeighborsRespectDebug(shapedetectorblock1.getPos(), Blocks.AIR);
            }
         }
      } else {
         shapedetector_shapedetectorcollection = this.getGolemPattern().match(world, blockposition);
         if (shapedetector_shapedetectorcollection != null) {
            for(int i = 0; i < this.getGolemPattern().getPalmLength(); ++i) {
               for(int k = 0; k < this.getGolemPattern().getThumbLength(); ++k) {
                  BlockPos pos = shapedetector_shapedetectorcollection.translateOffset(i, k, 0).getPos();
                  blockList.setTypeId(pos.getX(), pos.getY(), pos.getZ(), 0);
               }
            }

            BlockPos blockposition2 = shapedetector_shapedetectorcollection.translateOffset(1, 2, 0).getPos();
            EntityIronGolem entityirongolem = new EntityIronGolem(world);
            entityirongolem.setPlayerCreated(true);
            entityirongolem.setLocationAndAngles((double)blockposition2.getX() + 0.5D, (double)blockposition2.getY() + 0.05D, (double)blockposition2.getZ() + 0.5D, 0.0F, 0.0F);
            if (world.addEntity(entityirongolem, SpawnReason.BUILD_IRONGOLEM)) {
               blockList.updateList();

               for(int j = 0; j < 120; ++j) {
                  world.spawnParticle(EnumParticleTypes.SNOWBALL, (double)blockposition2.getX() + world.rand.nextDouble(), (double)blockposition2.getY() + world.rand.nextDouble() * 3.9D, (double)blockposition2.getZ() + world.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
               }

               for(int var21 = 0; var21 < this.getGolemPattern().getPalmLength(); ++var21) {
                  for(int l = 0; l < this.getGolemPattern().getThumbLength(); ++l) {
                     BlockWorldState shapedetectorblock2 = shapedetector_shapedetectorcollection.translateOffset(var21, l, 0);
                     world.notifyNeighborsRespectDebug(shapedetectorblock2.getPos(), Blocks.AIR);
                  }
               }
            }
         }
      }

   }

   public boolean canPlaceBlockAt(World world, BlockPos blockposition) {
      return world.getBlockState(blockposition).getBlock().blockMaterial.isReplaceable() && world.getBlockState(blockposition.down()).isFullyOpaque();
   }

   public IBlockState withRotation(IBlockState iblockdata, Rotation enumblockrotation) {
      return iblockdata.withProperty(FACING, enumblockrotation.rotate((EnumFacing)iblockdata.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState iblockdata, Mirror enumblockmirror) {
      return iblockdata.withRotation(enumblockmirror.toRotation((EnumFacing)iblockdata.getValue(FACING)));
   }

   public IBlockState getStateForPlacement(World world, BlockPos blockposition, EnumFacing enumdirection, float f, float f1, float f2, int i, EntityLivingBase entityliving) {
      return this.getDefaultState().withProperty(FACING, entityliving.getHorizontalFacing().getOpposite());
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(i));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      return ((EnumFacing)iblockdata.getValue(FACING)).getHorizontalIndex();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{FACING});
   }

   protected BlockPattern getSnowmanBasePattern() {
      if (this.snowmanBasePattern == null) {
         this.snowmanBasePattern = FactoryBlockPattern.start().aisle(" ", "#", "#").where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.SNOW))).build();
      }

      return this.snowmanBasePattern;
   }

   protected BlockPattern getSnowmanPattern() {
      if (this.snowmanPattern == null) {
         this.snowmanPattern = FactoryBlockPattern.start().aisle("^", "#", "#").where('^', BlockWorldState.hasState(IS_PUMPKIN)).where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.SNOW))).build();
      }

      return this.snowmanPattern;
   }

   protected BlockPattern getGolemBasePattern() {
      if (this.golemBasePattern == null) {
         this.golemBasePattern = FactoryBlockPattern.start().aisle("~ ~", "###", "~#~").where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.IRON_BLOCK))).where('~', BlockWorldState.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
      }

      return this.golemBasePattern;
   }

   protected BlockPattern getGolemPattern() {
      if (this.golemPattern == null) {
         this.golemPattern = FactoryBlockPattern.start().aisle("~^~", "###", "~#~").where('^', BlockWorldState.hasState(IS_PUMPKIN)).where('#', BlockWorldState.hasState(BlockStateMatcher.forBlock(Blocks.IRON_BLOCK))).where('~', BlockWorldState.hasState(BlockMaterialMatcher.forMaterial(Material.AIR))).build();
      }

      return this.golemPattern;
   }
}
