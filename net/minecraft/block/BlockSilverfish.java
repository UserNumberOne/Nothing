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

   public int quantityDropped(Random var1) {
      return 0;
   }

   public static boolean canContainSilverfish(IBlockState var0) {
      Block var1 = var0.getBlock();
      return var0 == Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE) || var1 == Blocks.COBBLESTONE || var1 == Blocks.STONEBRICK;
   }

   protected ItemStack getSilkTouchDrop(IBlockState var1) {
      switch(BlockSilverfish.SyntheticClass_1.a[((BlockSilverfish.EnumType)var1.getValue(VARIANT)).ordinal()]) {
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

   public void dropBlockAsItemWithChance(World var1, BlockPos var2, IBlockState var3, float var4, int var5) {
      if (!var1.isRemote && var1.getGameRules().getBoolean("doTileDrops")) {
         EntitySilverfish var6 = new EntitySilverfish(var1);
         var6.setLocationAndAngles((double)var2.getX() + 0.5D, (double)var2.getY(), (double)var2.getZ() + 0.5D, 0.0F, 0.0F);
         var1.addEntity(var6, SpawnReason.SILVERFISH_BLOCK);
         var6.spawnExplosionParticle();
      }

   }

   public ItemStack getItem(World var1, BlockPos var2, IBlockState var3) {
      return new ItemStack(this, 1, var3.getBlock().getMetaFromState(var3));
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(VARIANT, BlockSilverfish.EnumType.byMetadata(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((BlockSilverfish.EnumType)var1.getValue(VARIANT)).getMetadata();
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
         for(BlockSilverfish.EnumType var3 : values()) {
            META_LOOKUP[var3.getMetadata()] = var3;
         }

      }

      private EnumType(int var3, String var4) {
         this(var3, var4, var4);
      }

      private EnumType(int var3, String var4, String var5) {
         this.meta = var3;
         this.name = var4;
         this.unlocalizedName = var5;
      }

      public int getMetadata() {
         return this.meta;
      }

      public String toString() {
         return this.name;
      }

      public static BlockSilverfish.EnumType byMetadata(int var0) {
         if (var0 < 0 || var0 >= META_LOOKUP.length) {
            var0 = 0;
         }

         return META_LOOKUP[var0];
      }

      public String getName() {
         return this.name;
      }

      public String getUnlocalizedName() {
         return this.unlocalizedName;
      }

      public abstract IBlockState getModelBlock();

      public static BlockSilverfish.EnumType forModelBlock(IBlockState var0) {
         for(BlockSilverfish.EnumType var4 : values()) {
            if (var0 == var4.getModelBlock()) {
               return var4;
            }
         }

         return STONE;
      }

      private EnumType(int var3, String var4, BlockSilverfish.SyntheticClass_1 var5) {
         this(var3, var4);
      }

      private EnumType(int var3, String var4, String var5, BlockSilverfish.SyntheticClass_1 var6) {
         this(var3, var4, var5);
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
