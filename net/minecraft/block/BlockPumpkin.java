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

public class BlockPumpkin extends BlockHorizontal {
   private BlockPattern snowmanBasePattern;
   private BlockPattern snowmanPattern;
   private BlockPattern golemBasePattern;
   private BlockPattern golemPattern;
   private static final Predicate IS_PUMPKIN = new Predicate() {
      public boolean apply(@Nullable IBlockState var1) {
         return var1 != null && (var1.getBlock() == Blocks.PUMPKIN || var1.getBlock() == Blocks.LIT_PUMPKIN);
      }
   };

   protected BlockPumpkin() {
      super(Material.GOURD, MapColor.ADOBE);
      this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
      this.setTickRandomly(true);
      this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
   }

   public void onBlockAdded(World var1, BlockPos var2, IBlockState var3) {
      super.onBlockAdded(var1, var2, var3);
      this.trySpawnGolem(var1, var2);
   }

   public boolean canDispenserPlace(World var1, BlockPos var2) {
      return this.getSnowmanBasePattern().match(var1, var2) != null || this.getGolemBasePattern().match(var1, var2) != null;
   }

   private void trySpawnGolem(World var1, BlockPos var2) {
      BlockPattern.PatternHelper var3 = this.getSnowmanPattern().match(var1, var2);
      if (var3 != null) {
         for(int var4 = 0; var4 < this.getSnowmanPattern().getThumbLength(); ++var4) {
            BlockWorldState var5 = var3.translateOffset(0, var4, 0);
            var1.setBlockState(var5.getPos(), Blocks.AIR.getDefaultState(), 2);
         }

         EntitySnowman var10 = new EntitySnowman(var1);
         BlockPos var13 = var3.translateOffset(0, 2, 0).getPos();
         var10.setLocationAndAngles((double)var13.getX() + 0.5D, (double)var13.getY() + 0.05D, (double)var13.getZ() + 0.5D, 0.0F, 0.0F);
         var1.spawnEntity(var10);

         for(int var6 = 0; var6 < 120; ++var6) {
            var1.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, (double)var13.getX() + var1.rand.nextDouble(), (double)var13.getY() + var1.rand.nextDouble() * 2.5D, (double)var13.getZ() + var1.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
         }

         for(int var16 = 0; var16 < this.getSnowmanPattern().getThumbLength(); ++var16) {
            BlockWorldState var7 = var3.translateOffset(0, var16, 0);
            var1.notifyNeighborsRespectDebug(var7.getPos(), Blocks.AIR);
         }
      } else {
         var3 = this.getGolemPattern().match(var1, var2);
         if (var3 != null) {
            for(int var11 = 0; var11 < this.getGolemPattern().getPalmLength(); ++var11) {
               for(int var14 = 0; var14 < this.getGolemPattern().getThumbLength(); ++var14) {
                  var1.setBlockState(var3.translateOffset(var11, var14, 0).getPos(), Blocks.AIR.getDefaultState(), 2);
               }
            }

            BlockPos var12 = var3.translateOffset(1, 2, 0).getPos();
            EntityIronGolem var15 = new EntityIronGolem(var1);
            var15.setPlayerCreated(true);
            var15.setLocationAndAngles((double)var12.getX() + 0.5D, (double)var12.getY() + 0.05D, (double)var12.getZ() + 0.5D, 0.0F, 0.0F);
            var1.spawnEntity(var15);

            for(int var17 = 0; var17 < 120; ++var17) {
               var1.spawnParticle(EnumParticleTypes.SNOWBALL, (double)var12.getX() + var1.rand.nextDouble(), (double)var12.getY() + var1.rand.nextDouble() * 3.9D, (double)var12.getZ() + var1.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
            }

            for(int var18 = 0; var18 < this.getGolemPattern().getPalmLength(); ++var18) {
               for(int var19 = 0; var19 < this.getGolemPattern().getThumbLength(); ++var19) {
                  BlockWorldState var8 = var3.translateOffset(var18, var19, 0);
                  var1.notifyNeighborsRespectDebug(var8.getPos(), Blocks.AIR);
               }
            }
         }
      }

   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return var1.getBlockState(var2).getBlock().isReplaceable(var1, var2) && var1.getBlockState(var2.down()).isSideSolid(var1, var2, EnumFacing.UP);
   }

   public IBlockState withRotation(IBlockState var1, Rotation var2) {
      return var1.withProperty(FACING, var2.rotate((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState withMirror(IBlockState var1, Mirror var2) {
      return var1.withRotation(var2.toRotation((EnumFacing)var1.getValue(FACING)));
   }

   public IBlockState getStateForPlacement(World var1, BlockPos var2, EnumFacing var3, float var4, float var5, float var6, int var7, EntityLivingBase var8) {
      return this.getDefaultState().withProperty(FACING, var8.getHorizontalFacing().getOpposite());
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((EnumFacing)var1.getValue(FACING)).getHorizontalIndex();
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
