package net.minecraft.world.gen.structure;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.loot.LootTableList;

public class StructureEndCityPieces {
   public static final TemplateManager MANAGER = new TemplateManager("structures");
   private static final PlacementSettings OVERWRITE = (new PlacementSettings()).setIgnoreEntities(true);
   private static final PlacementSettings INSERT = (new PlacementSettings()).setIgnoreEntities(true).setReplacedBlock(Blocks.AIR);
   private static final StructureEndCityPieces.IGenerator HOUSE_TOWER_GENERATOR = new StructureEndCityPieces.IGenerator() {
      public void init() {
      }

      public boolean generate(int var1, StructureEndCityPieces.CityTemplate var2, BlockPos var3, List var4, Random var5) {
         if (var1 > 8) {
            return false;
         } else {
            Rotation var6 = var2.placeSettings.getRotation();
            StructureEndCityPieces.CityTemplate var7 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var2, var3, "base_floor", var6, true));
            int var8 = var5.nextInt(3);
            if (var8 == 0) {
               StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var7, new BlockPos(-1, 4, -1), "base_roof", var6, true));
            } else if (var8 == 1) {
               var7 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var7, new BlockPos(-1, 0, -1), "second_floor_2", var6, false));
               var7 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var7, new BlockPos(-1, 8, -1), "second_roof", var6, false));
               StructureEndCityPieces.recursiveChildren(StructureEndCityPieces.TOWER_GENERATOR, var1 + 1, var7, (BlockPos)null, var4, var5);
            } else if (var8 == 2) {
               var7 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var7, new BlockPos(-1, 0, -1), "second_floor_2", var6, false));
               var7 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var7, new BlockPos(-1, 4, -1), "third_floor_c", var6, false));
               var7 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var7, new BlockPos(-1, 8, -1), "third_roof", var6, true));
               StructureEndCityPieces.recursiveChildren(StructureEndCityPieces.TOWER_GENERATOR, var1 + 1, var7, (BlockPos)null, var4, var5);
            }

            return true;
         }
      }
   };
   private static final List TOWER_BRIDGES = Lists.newArrayList(new Tuple[]{new Tuple(Rotation.NONE, new BlockPos(1, -1, 0)), new Tuple(Rotation.CLOCKWISE_90, new BlockPos(6, -1, 1)), new Tuple(Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 5)), new Tuple(Rotation.CLOCKWISE_180, new BlockPos(5, -1, 6))});
   private static final StructureEndCityPieces.IGenerator TOWER_GENERATOR = new StructureEndCityPieces.IGenerator() {
      public void init() {
      }

      public boolean generate(int var1, StructureEndCityPieces.CityTemplate var2, BlockPos var3, List var4, Random var5) {
         Rotation var6 = var2.placeSettings.getRotation();
         StructureEndCityPieces.CityTemplate var7 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var2, new BlockPos(3 + var5.nextInt(2), -3, 3 + var5.nextInt(2)), "tower_base", var6, true));
         var7 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var7, new BlockPos(0, 7, 0), "tower_piece", var6, true));
         StructureEndCityPieces.CityTemplate var8 = var5.nextInt(3) == 0 ? var7 : null;
         int var9 = 1 + var5.nextInt(3);

         for(int var10 = 0; var10 < var9; ++var10) {
            var7 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var7, new BlockPos(0, 4, 0), "tower_piece", var6, true));
            if (var10 < var9 - 1 && var5.nextBoolean()) {
               var8 = var7;
            }
         }

         if (var8 != null) {
            for(Tuple var11 : StructureEndCityPieces.TOWER_BRIDGES) {
               if (var5.nextBoolean()) {
                  StructureEndCityPieces.CityTemplate var12 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var8, (BlockPos)var11.getSecond(), "bridge_end", var6.add((Rotation)var11.getFirst()), true));
                  StructureEndCityPieces.recursiveChildren(StructureEndCityPieces.TOWER_BRIDGE_GENERATOR, var1 + 1, var12, (BlockPos)null, var4, var5);
               }
            }

            StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var7, new BlockPos(-1, 4, -1), "tower_top", var6, true));
         } else {
            if (var1 != 7) {
               return StructureEndCityPieces.recursiveChildren(StructureEndCityPieces.FAT_TOWER_GENERATOR, var1 + 1, var7, (BlockPos)null, var4, var5);
            }

            StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var7, new BlockPos(-1, 4, -1), "tower_top", var6, true));
         }

         return true;
      }
   };
   private static final StructureEndCityPieces.IGenerator TOWER_BRIDGE_GENERATOR = new StructureEndCityPieces.IGenerator() {
      public boolean shipCreated;

      public void init() {
         this.shipCreated = false;
      }

      public boolean generate(int var1, StructureEndCityPieces.CityTemplate var2, BlockPos var3, List var4, Random var5) {
         Rotation var6 = var2.placeSettings.getRotation();
         int var7 = var5.nextInt(4) + 1;
         StructureEndCityPieces.CityTemplate var8 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var2, new BlockPos(0, 0, -4), "bridge_piece", var6, true));
         var8.componentType = -1;
         byte var9 = 0;

         for(int var10 = 0; var10 < var7; ++var10) {
            if (var5.nextBoolean()) {
               var8 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var8, new BlockPos(0, var9, -4), "bridge_piece", var6, true));
               var9 = 0;
            } else {
               if (var5.nextBoolean()) {
                  var8 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var8, new BlockPos(0, var9, -4), "bridge_steep_stairs", var6, true));
               } else {
                  var8 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var8, new BlockPos(0, var9, -8), "bridge_gentle_stairs", var6, true));
               }

               var9 = 4;
            }
         }

         if (!this.shipCreated && var5.nextInt(10 - var1) == 0) {
            StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var8, new BlockPos(-8 + var5.nextInt(8), var9, -70 + var5.nextInt(10)), "ship", var6, true));
            this.shipCreated = true;
         } else if (!StructureEndCityPieces.recursiveChildren(StructureEndCityPieces.HOUSE_TOWER_GENERATOR, var1 + 1, var8, new BlockPos(-3, var9 + 1, -11), var4, var5)) {
            return false;
         }

         var8 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var8, new BlockPos(4, var9, 0), "bridge_end", var6.add(Rotation.CLOCKWISE_180), true));
         var8.componentType = -1;
         return true;
      }
   };
   private static final List FAT_TOWER_BRIDGES = Lists.newArrayList(new Tuple[]{new Tuple(Rotation.NONE, new BlockPos(4, -1, 0)), new Tuple(Rotation.CLOCKWISE_90, new BlockPos(12, -1, 4)), new Tuple(Rotation.COUNTERCLOCKWISE_90, new BlockPos(0, -1, 8)), new Tuple(Rotation.CLOCKWISE_180, new BlockPos(8, -1, 12))});
   private static final StructureEndCityPieces.IGenerator FAT_TOWER_GENERATOR = new StructureEndCityPieces.IGenerator() {
      public void init() {
      }

      public boolean generate(int var1, StructureEndCityPieces.CityTemplate var2, BlockPos var3, List var4, Random var5) {
         Rotation var6 = var2.placeSettings.getRotation();
         StructureEndCityPieces.CityTemplate var7 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var2, new BlockPos(-3, 4, -3), "fat_tower_base", var6, true));
         var7 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var7, new BlockPos(0, 4, 0), "fat_tower_middle", var6, true));

         for(int var8 = 0; var8 < 2 && var5.nextInt(3) != 0; ++var8) {
            var7 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var7, new BlockPos(0, 8, 0), "fat_tower_middle", var6, true));

            for(Tuple var10 : StructureEndCityPieces.FAT_TOWER_BRIDGES) {
               if (var5.nextBoolean()) {
                  StructureEndCityPieces.CityTemplate var11 = StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var7, (BlockPos)var10.getSecond(), "bridge_end", var6.add((Rotation)var10.getFirst()), true));
                  StructureEndCityPieces.recursiveChildren(StructureEndCityPieces.TOWER_BRIDGE_GENERATOR, var1 + 1, var11, (BlockPos)null, var4, var5);
               }
            }
         }

         StructureEndCityPieces.func_189935_b(var4, StructureEndCityPieces.addPiece(var7, new BlockPos(-2, 8, -2), "fat_tower_top", var6, true));
         return true;
      }
   };

   public static void registerPieces() {
      MapGenStructureIO.registerStructureComponent(StructureEndCityPieces.CityTemplate.class, "ECP");
   }

   private static StructureEndCityPieces.CityTemplate addPiece(StructureEndCityPieces.CityTemplate var0, BlockPos var1, String var2, Rotation var3, boolean var4) {
      StructureEndCityPieces.CityTemplate var5 = new StructureEndCityPieces.CityTemplate(var2, var0.templatePosition, var3, var4);
      BlockPos var6 = var0.template.calculateConnectedPos(var0.placeSettings, var1, var5.placeSettings, BlockPos.ORIGIN);
      var5.offset(var6.getX(), var6.getY(), var6.getZ());
      return var5;
   }

   public static void beginHouseTower(BlockPos var0, Rotation var1, List var2, Random var3) {
      FAT_TOWER_GENERATOR.init();
      HOUSE_TOWER_GENERATOR.init();
      TOWER_BRIDGE_GENERATOR.init();
      TOWER_GENERATOR.init();
      StructureEndCityPieces.CityTemplate var4 = func_189935_b(var2, new StructureEndCityPieces.CityTemplate("base_floor", var0, var1, true));
      var4 = func_189935_b(var2, addPiece(var4, new BlockPos(-1, 0, -1), "second_floor", var1, false));
      var4 = func_189935_b(var2, addPiece(var4, new BlockPos(-1, 4, -1), "third_floor", var1, false));
      var4 = func_189935_b(var2, addPiece(var4, new BlockPos(-1, 8, -1), "third_roof", var1, true));
      recursiveChildren(TOWER_GENERATOR, 1, var4, (BlockPos)null, var2, var3);
   }

   private static StructureEndCityPieces.CityTemplate func_189935_b(List var0, StructureEndCityPieces.CityTemplate var1) {
      var0.add(var1);
      return var1;
   }

   private static boolean recursiveChildren(StructureEndCityPieces.IGenerator var0, int var1, StructureEndCityPieces.CityTemplate var2, BlockPos var3, List var4, Random var5) {
      if (var1 > 8) {
         return false;
      } else {
         ArrayList var6 = Lists.newArrayList();
         if (var0.generate(var1, var2, var3, var6, var5)) {
            boolean var7 = false;
            int var8 = var5.nextInt();

            for(StructureComponent var10 : var6) {
               var10.componentType = var8;
               StructureComponent var11 = StructureComponent.findIntersecting(var4, var10.getBoundingBox());
               if (var11 != null && var11.componentType != var2.componentType) {
                  var7 = true;
                  break;
               }
            }

            if (!var7) {
               var4.addAll(var6);
               return true;
            }
         }

         return false;
      }
   }

   public static class CityTemplate extends StructureComponentTemplate {
      private String pieceName;
      private Rotation rotation;
      private boolean overwrite;

      public CityTemplate() {
      }

      public CityTemplate(String var1, BlockPos var2, Rotation var3, boolean var4) {
         super(0);
         this.pieceName = var1;
         this.rotation = var3;
         this.overwrite = var4;
         this.loadAndSetup(var2);
      }

      private void loadAndSetup(BlockPos var1) {
         Template var2 = StructureEndCityPieces.MANAGER.getTemplate((MinecraftServer)null, new ResourceLocation("endcity/" + this.pieceName));
         PlacementSettings var3;
         if (this.overwrite) {
            var3 = StructureEndCityPieces.OVERWRITE.copy().setRotation(this.rotation);
         } else {
            var3 = StructureEndCityPieces.INSERT.copy().setRotation(this.rotation);
         }

         this.setup(var2, var1, var3);
      }

      protected void writeStructureToNBT(NBTTagCompound var1) {
         super.writeStructureToNBT(var1);
         var1.setString("Template", this.pieceName);
         var1.setString("Rot", this.rotation.name());
         var1.setBoolean("OW", this.overwrite);
      }

      protected void readStructureFromNBT(NBTTagCompound var1) {
         super.readStructureFromNBT(var1);
         this.pieceName = var1.getString("Template");
         this.rotation = Rotation.valueOf(var1.getString("Rot"));
         this.overwrite = var1.getBoolean("OW");
         this.loadAndSetup(this.templatePosition);
      }

      protected void handleDataMarker(String var1, BlockPos var2, World var3, Random var4, StructureBoundingBox var5) {
         if (var1.startsWith("Chest")) {
            BlockPos var6 = var2.down();
            if (var5.isVecInside(var6)) {
               TileEntity var7 = var3.getTileEntity(var6);
               if (var7 instanceof TileEntityChest) {
                  ((TileEntityChest)var7).setLootTable(LootTableList.CHESTS_END_CITY_TREASURE, var4.nextLong());
               }
            }
         } else if (var1.startsWith("Sentry")) {
            EntityShulker var8 = new EntityShulker(var3);
            var8.setPosition((double)var2.getX() + 0.5D, (double)var2.getY() + 0.5D, (double)var2.getZ() + 0.5D);
            var8.setAttachmentPos(var2);
            var3.spawnEntity(var8);
         } else if (var1.startsWith("Elytra")) {
            EntityItemFrame var9 = new EntityItemFrame(var3, var2, this.rotation.rotate(EnumFacing.SOUTH));
            var9.setDisplayedItem(new ItemStack(Items.ELYTRA));
            var3.spawnEntity(var9);
         }

      }
   }

   interface IGenerator {
      void init();

      boolean generate(int var1, StructureEndCityPieces.CityTemplate var2, BlockPos var3, List var4, Random var5);
   }
}
