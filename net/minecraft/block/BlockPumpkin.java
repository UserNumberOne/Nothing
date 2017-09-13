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
      public boolean apply(@Nullable IBlockState var1) {
         return var1 != null && (var1.getBlock() == Blocks.PUMPKIN || var1.getBlock() == Blocks.LIT_PUMPKIN);
      }

      public boolean apply(Object var1) {
         return this.apply((IBlockState)var1);
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
      BlockStateListPopulator var4 = new BlockStateListPopulator(var1.getWorld());
      if (var3 != null) {
         for(int var5 = 0; var5 < this.getSnowmanPattern().getThumbLength(); ++var5) {
            BlockWorldState var6 = var3.translateOffset(0, var5, 0);
            BlockPos var7 = var6.getPos();
            var4.setTypeId(var7.getX(), var7.getY(), var7.getZ(), 0);
         }

         EntitySnowman var13 = new EntitySnowman(var1);
         BlockPos var16 = var3.translateOffset(0, 2, 0).getPos();
         var13.setLocationAndAngles((double)var16.getX() + 0.5D, (double)var16.getY() + 0.05D, (double)var16.getZ() + 0.5D, 0.0F, 0.0F);
         if (var1.addEntity(var13, SpawnReason.BUILD_SNOWMAN)) {
            var4.updateList();

            for(int var8 = 0; var8 < 120; ++var8) {
               var1.spawnParticle(EnumParticleTypes.SNOW_SHOVEL, (double)var16.getX() + var1.rand.nextDouble(), (double)var16.getY() + var1.rand.nextDouble() * 2.5D, (double)var16.getZ() + var1.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
            }

            for(int var19 = 0; var19 < this.getSnowmanPattern().getThumbLength(); ++var19) {
               BlockWorldState var9 = var3.translateOffset(0, var19, 0);
               var1.notifyNeighborsRespectDebug(var9.getPos(), Blocks.AIR);
            }
         }
      } else {
         var3 = this.getGolemPattern().match(var1, var2);
         if (var3 != null) {
            for(int var12 = 0; var12 < this.getGolemPattern().getPalmLength(); ++var12) {
               for(int var14 = 0; var14 < this.getGolemPattern().getThumbLength(); ++var14) {
                  BlockPos var17 = var3.translateOffset(var12, var14, 0).getPos();
                  var4.setTypeId(var17.getX(), var17.getY(), var17.getZ(), 0);
               }
            }

            BlockPos var15 = var3.translateOffset(1, 2, 0).getPos();
            EntityIronGolem var18 = new EntityIronGolem(var1);
            var18.setPlayerCreated(true);
            var18.setLocationAndAngles((double)var15.getX() + 0.5D, (double)var15.getY() + 0.05D, (double)var15.getZ() + 0.5D, 0.0F, 0.0F);
            if (var1.addEntity(var18, SpawnReason.BUILD_IRONGOLEM)) {
               var4.updateList();

               for(int var20 = 0; var20 < 120; ++var20) {
                  var1.spawnParticle(EnumParticleTypes.SNOWBALL, (double)var15.getX() + var1.rand.nextDouble(), (double)var15.getY() + var1.rand.nextDouble() * 3.9D, (double)var15.getZ() + var1.rand.nextDouble(), 0.0D, 0.0D, 0.0D);
               }

               for(int var21 = 0; var21 < this.getGolemPattern().getPalmLength(); ++var21) {
                  for(int var22 = 0; var22 < this.getGolemPattern().getThumbLength(); ++var22) {
                     BlockWorldState var10 = var3.translateOffset(var21, var22, 0);
                     var1.notifyNeighborsRespectDebug(var10.getPos(), Blocks.AIR);
                  }
               }
            }
         }
      }

   }

   public boolean canPlaceBlockAt(World var1, BlockPos var2) {
      return var1.getBlockState(var2).getBlock().blockMaterial.isReplaceable() && var1.getBlockState(var2.down()).isFullyOpaque();
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
