package net.minecraft.world.gen.feature;

import java.util.Random;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;

public class WorldGenFossils extends WorldGenerator {
   private static final ResourceLocation STRUCTURE_SPINE_01 = new ResourceLocation("fossils/fossil_spine_01");
   private static final ResourceLocation STRUCTURE_SPINE_02 = new ResourceLocation("fossils/fossil_spine_02");
   private static final ResourceLocation STRUCTURE_SPINE_03 = new ResourceLocation("fossils/fossil_spine_03");
   private static final ResourceLocation STRUCTURE_SPINE_04 = new ResourceLocation("fossils/fossil_spine_04");
   private static final ResourceLocation STRUCTURE_SPINE_01_COAL = new ResourceLocation("fossils/fossil_spine_01_coal");
   private static final ResourceLocation STRUCTURE_SPINE_02_COAL = new ResourceLocation("fossils/fossil_spine_02_coal");
   private static final ResourceLocation STRUCTURE_SPINE_03_COAL = new ResourceLocation("fossils/fossil_spine_03_coal");
   private static final ResourceLocation STRUCTURE_SPINE_04_COAL = new ResourceLocation("fossils/fossil_spine_04_coal");
   private static final ResourceLocation STRUCTURE_SKULL_01 = new ResourceLocation("fossils/fossil_skull_01");
   private static final ResourceLocation STRUCTURE_SKULL_02 = new ResourceLocation("fossils/fossil_skull_02");
   private static final ResourceLocation STRUCTURE_SKULL_03 = new ResourceLocation("fossils/fossil_skull_03");
   private static final ResourceLocation STRUCTURE_SKULL_04 = new ResourceLocation("fossils/fossil_skull_04");
   private static final ResourceLocation STRUCTURE_SKULL_01_COAL = new ResourceLocation("fossils/fossil_skull_01_coal");
   private static final ResourceLocation STRUCTURE_SKULL_02_COAL = new ResourceLocation("fossils/fossil_skull_02_coal");
   private static final ResourceLocation STRUCTURE_SKULL_03_COAL = new ResourceLocation("fossils/fossil_skull_03_coal");
   private static final ResourceLocation STRUCTURE_SKULL_04_COAL = new ResourceLocation("fossils/fossil_skull_04_coal");
   private static final ResourceLocation[] FOSSILS = new ResourceLocation[]{STRUCTURE_SPINE_01, STRUCTURE_SPINE_02, STRUCTURE_SPINE_03, STRUCTURE_SPINE_04, STRUCTURE_SKULL_01, STRUCTURE_SKULL_02, STRUCTURE_SKULL_03, STRUCTURE_SKULL_04};
   private static final ResourceLocation[] FOSSILS_COAL = new ResourceLocation[]{STRUCTURE_SPINE_01_COAL, STRUCTURE_SPINE_02_COAL, STRUCTURE_SPINE_03_COAL, STRUCTURE_SPINE_04_COAL, STRUCTURE_SKULL_01_COAL, STRUCTURE_SKULL_02_COAL, STRUCTURE_SKULL_03_COAL, STRUCTURE_SKULL_04_COAL};

   public boolean generate(World var1, Random var2, BlockPos var3) {
      Random var4 = var1.getChunkFromChunkCoords(var3.getX(), var3.getZ()).getRandomWithSeed(987234911L);
      MinecraftServer var5 = var1.getMinecraftServer();
      Rotation[] var6 = Rotation.values();
      Rotation var7 = var6[var4.nextInt(var6.length)];
      int var8 = var4.nextInt(FOSSILS.length);
      TemplateManager var9 = var1.getSaveHandler().getStructureTemplateManager();
      Template var10 = var9.a(var5, FOSSILS[var8]);
      Template var11 = var9.a(var5, FOSSILS_COAL[var8]);
      ChunkPos var12 = new ChunkPos(var3);
      StructureBoundingBox var13 = new StructureBoundingBox(var12.getXStart(), 0, var12.getZStart(), var12.getXEnd(), 256, var12.getZEnd());
      PlacementSettings var14 = (new PlacementSettings()).setRotation(var7).setBoundingBox(var13).setRandom(var4);
      BlockPos var15 = var10.transformedSize(var7);
      int var16 = var4.nextInt(16 - var15.getX());
      int var17 = var4.nextInt(16 - var15.getZ());
      int var18 = 256;

      for(int var19 = 0; var19 < var15.getX(); ++var19) {
         for(int var20 = 0; var20 < var15.getX(); ++var20) {
            var18 = Math.min(var18, var1.getHeight(var3.getX() + var19 + var16, var3.getZ() + var20 + var17));
         }
      }

      int var21 = Math.max(var18 - 15 - var4.nextInt(10), 10);
      BlockPos var22 = var10.getZeroPositionWithTransform(var3.add(var16, var21, var17), Mirror.NONE, var7);
      var14.setIntegrity(0.9F);
      var10.addBlocksToWorld(var1, var22, var14, 4);
      var14.setIntegrity(0.1F);
      var11.addBlocksToWorld(var1, var22, var14, 4);
      return true;
   }
}
