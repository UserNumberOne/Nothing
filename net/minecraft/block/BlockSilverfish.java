package net.minecraft.block;

import java.util.Random;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class BlockSilverfish extends Block {
   public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockSilverfish.EnumType.class);

   public BlockSilverfish() {
      super(Material.CLAY);
      this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockSilverfish.EnumType.STONE));
      this.setHardness(0.0F);
      this.setCreativeTab(CreativeTabs.DECORATIONS);
   }

   public int quantityDropped(Random random) {
      return 0;
   }

   public static boolean canContainSilverfish(IBlockState iblockdata) {
      Block block = iblockdata.getBlock();
      return iblockdata == Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE) || block == Blocks.COBBLESTONE || block == Blocks.STONEBRICK;
   }

   protected ItemStack getSilkTouchDrop(IBlockState iblockdata) {
      switch(BlockSilverfish.SyntheticClass_1.a[((BlockSilverfish.EnumType)iblockdata.getValue(VARIANT)).ordinal()]) {
      case 1:
         return new ItemStack(Blocks.COBBLESTONE);
      case 2:
         return new ItemStack(Blocks.STONEBRICK);
      case 3:
         return new ItemStack(Blocks.STONEBRICK, 1, BlockStoneBrick.EnumType.MOSSY.getMetadata());
      case 4:
         return new ItemStack(Blocks.STONEBRICK, 1, BlockStoneBrick.EnumType.CRACKED.getMetadata());
      case 5:
         return new ItemStack(Blocks.STONEBRICK, 1, BlockStoneBrick.EnumType.CHISELED.getMetadata());
      default:
         return new ItemStack(Blocks.STONE);
      }
   }

   public void dropBlockAsItemWithChance(World world, BlockPos blockposition, IBlockState iblockdata, float f, int i) {
      if (!world.isRemote && world.getGameRules().getBoolean("doTileDrops")) {
         EntitySilverfish entitysilverfish = new EntitySilverfish(world);
         entitysilverfish.setLocationAndAngles((double)blockposition.getX() + 0.5D, (double)blockposition.getY(), (double)blockposition.getZ() + 0.5D, 0.0F, 0.0F);
         world.addEntity(entitysilverfish, SpawnReason.SILVERFISH_BLOCK);
         entitysilverfish.spawnExplosionParticle();
      }

   }

   public ItemStack getItem(World world, BlockPos blockposition, IBlockState iblockdata) {
      return new ItemStack(this, 1, iblockdata.getBlock().getMetaFromState(iblockdata));
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(VARIANT, BlockSilverfish.EnumType.byMetadata(i));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      return ((BlockSilverfish.EnumType)iblockdata.getValue(VARIANT)).getMetadata();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{VARIANT});
   }

   public static enum EnumType implements IStringSerializable {
      STONE(0, "stone") {
         public IBlockState getModelBlock() {
            return Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE);
         }
      },
      COBBLESTONE(1, "cobblestone", "cobble") {
         public IBlockState getModelBlock() {
            return Blocks.COBBLESTONE.getDefaultState();
         }
      },
      STONEBRICK(2, "stone_brick", "brick") {
         public IBlockState getModelBlock() {
            return Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.DEFAULT);
         }
      },
      MOSSY_STONEBRICK(3, "mossy_brick", "mossybrick") {
         public IBlockState getModelBlock() {
            return Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.MOSSY);
         }
      },
      CRACKED_STONEBRICK(4, "cracked_brick", "crackedbrick") {
         public IBlockState getModelBlock() {
            return Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CRACKED);
         }
      },
      CHISELED_STONEBRICK(5, "chiseled_brick", "chiseledbrick") {
         public IBlockState getModelBlock() {
            return Blocks.STONEBRICK.getDefaultState().withProperty(BlockStoneBrick.VARIANT, BlockStoneBrick.EnumType.CHISELED);
         }
      };

      private static final BlockSilverfish.EnumType[] META_LOOKUP = new BlockSilverfish.EnumType[values().length];
      private final int meta;
      private final String name;
      private final String unlocalizedName;

      static {
         for(BlockSilverfish.EnumType blockmonstereggs_enummonstereggvarient : values()) {
            META_LOOKUP[blockmonstereggs_enummonstereggvarient.getMetadata()] = blockmonstereggs_enummonstereggvarient;
         }

      }

      private EnumType(int i, String s) {
         this(i, s, s);
      }

      private EnumType(int i, String s, String s1) {
         this.meta = i;
         this.name = s;
         this.unlocalizedName = s1;
      }

      public int getMetadata() {
         return this.meta;
      }

      public String toString() {
         return this.name;
      }

      public static BlockSilverfish.EnumType byMetadata(int i) {
         if (i < 0 || i >= META_LOOKUP.length) {
            i = 0;
         }

         return META_LOOKUP[i];
      }

      public String getName() {
         return this.name;
      }

      public String getUnlocalizedName() {
         return this.unlocalizedName;
      }

      public abstract IBlockState getModelBlock();

      public static BlockSilverfish.EnumType forModelBlock(IBlockState iblockdata) {
         for(BlockSilverfish.EnumType blockmonstereggs_enummonstereggvarient : values()) {
            if (iblockdata == blockmonstereggs_enummonstereggvarient.getModelBlock()) {
               return blockmonstereggs_enummonstereggvarient;
            }
         }

         return STONE;
      }

      private EnumType(int i, String s, BlockSilverfish.SyntheticClass_1 blockmonstereggs_syntheticclass_1) {
         this(i, s);
      }

      private EnumType(int i, String s, String s1, BlockSilverfish.SyntheticClass_1 blockmonstereggs_syntheticclass_1) {
         this(i, s, s1);
      }

      // $FF: synthetic method
      EnumType(int var3, String var4, BlockSilverfish.EnumType var5) {
         this(var3, var4);
      }

      // $FF: synthetic method
      EnumType(int var3, String var4, String var5, BlockSilverfish.EnumType var6) {
         this(var3, var4, var5);
      }
   }

   static class SyntheticClass_1 {
      static final int[] a = new int[BlockSilverfish.EnumType.values().length];

      static {
         try {
            a[BlockSilverfish.EnumType.COBBLESTONE.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
            ;
         }

         try {
            a[BlockSilverfish.EnumType.STONEBRICK.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
            ;
         }

         try {
            a[BlockSilverfish.EnumType.MOSSY_STONEBRICK.ordinal()] = 3;
         } catch (NoSuchFieldError var2) {
            ;
         }

         try {
            a[BlockSilverfish.EnumType.CRACKED_STONEBRICK.ordinal()] = 4;
         } catch (NoSuchFieldError var1) {
            ;
         }

         try {
            a[BlockSilverfish.EnumType.CHISELED_STONEBRICK.ordinal()] = 5;
         } catch (NoSuchFieldError var0) {
            ;
         }

      }
   }
}
