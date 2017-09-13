package net.minecraft.block;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import java.util.Collection;
import javax.annotation.Nullable;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.IStringSerializable;

public abstract class BlockFlower extends BlockBush {
   protected PropertyEnum type;

   protected BlockFlower() {
      this.setDefaultState(this.blockState.getBaseState().withProperty(this.getTypeProperty(), this.getBlockType() == BlockFlower.EnumFlowerColor.RED ? BlockFlower.EnumFlowerType.POPPY : BlockFlower.EnumFlowerType.DANDELION));
   }

   public int damageDropped(IBlockState var1) {
      return ((BlockFlower.EnumFlowerType)var1.getValue(this.getTypeProperty())).getMeta();
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(this.getTypeProperty(), BlockFlower.EnumFlowerType.getType(this.getBlockType(), var1));
   }

   public abstract BlockFlower.EnumFlowerColor getBlockType();

   public IProperty getTypeProperty() {
      if (this.type == null) {
         this.type = PropertyEnum.create("type", BlockFlower.EnumFlowerType.class, new Predicate() {
            public boolean apply(@Nullable BlockFlower.EnumFlowerType var1) {
               return var1.getBlockType() == BlockFlower.this.getBlockType();
            }

            // $FF: synthetic method
            public boolean apply(Object var1) {
               return this.apply((BlockFlower.EnumFlowerType)var1);
            }
         });
      }

      return this.type;
   }

   public int getMetaFromState(IBlockState var1) {
      return ((BlockFlower.EnumFlowerType)var1.getValue(this.getTypeProperty())).getMeta();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{this.getTypeProperty()});
   }

   public static enum EnumFlowerColor {
      YELLOW,
      RED;

      public BlockFlower getBlock() {
         return this == YELLOW ? Blocks.YELLOW_FLOWER : Blocks.RED_FLOWER;
      }
   }

   public static enum EnumFlowerType implements IStringSerializable {
      DANDELION(BlockFlower.EnumFlowerColor.YELLOW, 0, "dandelion"),
      POPPY(BlockFlower.EnumFlowerColor.RED, 0, "poppy"),
      BLUE_ORCHID(BlockFlower.EnumFlowerColor.RED, 1, "blue_orchid", "blueOrchid"),
      ALLIUM(BlockFlower.EnumFlowerColor.RED, 2, "allium"),
      HOUSTONIA(BlockFlower.EnumFlowerColor.RED, 3, "houstonia"),
      RED_TULIP(BlockFlower.EnumFlowerColor.RED, 4, "red_tulip", "tulipRed"),
      ORANGE_TULIP(BlockFlower.EnumFlowerColor.RED, 5, "orange_tulip", "tulipOrange"),
      WHITE_TULIP(BlockFlower.EnumFlowerColor.RED, 6, "white_tulip", "tulipWhite"),
      PINK_TULIP(BlockFlower.EnumFlowerColor.RED, 7, "pink_tulip", "tulipPink"),
      OXEYE_DAISY(BlockFlower.EnumFlowerColor.RED, 8, "oxeye_daisy", "oxeyeDaisy");

      private static final BlockFlower.EnumFlowerType[][] TYPES_FOR_BLOCK = new BlockFlower.EnumFlowerType[BlockFlower.EnumFlowerColor.values().length][];
      private final BlockFlower.EnumFlowerColor blockType;
      private final int meta;
      private final String name;
      private final String unlocalizedName;

      private EnumFlowerType(BlockFlower.EnumFlowerColor var3, int var4, String var5) {
         this(var3, var4, var5, var5);
      }

      private EnumFlowerType(BlockFlower.EnumFlowerColor var3, int var4, String var5, String var6) {
         this.blockType = var3;
         this.meta = var4;
         this.name = var5;
         this.unlocalizedName = var6;
      }

      public BlockFlower.EnumFlowerColor getBlockType() {
         return this.blockType;
      }

      public int getMeta() {
         return this.meta;
      }

      public static BlockFlower.EnumFlowerType getType(BlockFlower.EnumFlowerColor var0, int var1) {
         BlockFlower.EnumFlowerType[] var2 = TYPES_FOR_BLOCK[var0.ordinal()];
         if (var1 < 0 || var1 >= var2.length) {
            var1 = 0;
         }

         return var2[var1];
      }

      public String toString() {
         return this.name;
      }

      public String getName() {
         return this.name;
      }

      public String getUnlocalizedName() {
         return this.unlocalizedName;
      }

      static {
         for(final BlockFlower.EnumFlowerColor var3 : BlockFlower.EnumFlowerColor.values()) {
            Collection var4 = Collections2.filter(Lists.newArrayList(values()), new Predicate() {
               public boolean apply(@Nullable BlockFlower.EnumFlowerType var1) {
                  return var1.getBlockType() == var3;
               }

               // $FF: synthetic method
               public boolean apply(Object var1) {
                  return this.apply((BlockFlower.EnumFlowerType)var1);
               }
            });
            TYPES_FOR_BLOCK[var3.ordinal()] = (BlockFlower.EnumFlowerType[])var4.toArray(new BlockFlower.EnumFlowerType[var4.size()]);
         }

      }
   }
}
