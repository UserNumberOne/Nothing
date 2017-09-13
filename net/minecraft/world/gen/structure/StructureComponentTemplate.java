package net.minecraft.world.gen.structure;

import java.util.Map;
import java.util.Random;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;

public abstract class StructureComponentTemplate extends StructureComponent {
   private static final PlacementSettings DEFAULT_PLACE_SETTINGS = new PlacementSettings();
   protected Template template;
   protected PlacementSettings placeSettings;
   protected BlockPos templatePosition;

   public StructureComponentTemplate() {
      this.placeSettings = DEFAULT_PLACE_SETTINGS.setIgnoreEntities(true).setReplacedBlock(Blocks.AIR);
   }

   public StructureComponentTemplate(int var1) {
      super(var1);
      this.placeSettings = DEFAULT_PLACE_SETTINGS.setIgnoreEntities(true).setReplacedBlock(Blocks.AIR);
   }

   protected void setup(Template var1, BlockPos var2, PlacementSettings var3) {
      this.template = var1;
      this.setCoordBaseMode(EnumFacing.NORTH);
      this.templatePosition = var2;
      this.placeSettings = var3;
      this.setBoundingBoxFromTemplate();
   }

   protected void writeStructureToNBT(NBTTagCompound var1) {
      var1.setInteger("TPX", this.templatePosition.getX());
      var1.setInteger("TPY", this.templatePosition.getY());
      var1.setInteger("TPZ", this.templatePosition.getZ());
   }

   protected void readStructureFromNBT(NBTTagCompound var1) {
      this.templatePosition = new BlockPos(var1.getInteger("TPX"), var1.getInteger("TPY"), var1.getInteger("TPZ"));
   }

   public boolean addComponentParts(World var1, Random var2, StructureBoundingBox var3) {
      this.placeSettings.setBoundingBox(var3);
      this.template.addBlocksToWorld(var1, this.templatePosition, this.placeSettings);
      Map var4 = this.template.getDataBlocks(this.templatePosition, this.placeSettings);

      for(BlockPos var6 : var4.keySet()) {
         String var7 = (String)var4.get(var6);
         this.handleDataMarker(var7, var6, var1, var2, var3);
      }

      return true;
   }

   protected abstract void handleDataMarker(String var1, BlockPos var2, World var3, Random var4, StructureBoundingBox var5);

   private void setBoundingBoxFromTemplate() {
      Rotation var1 = this.placeSettings.getRotation();
      BlockPos var2 = this.template.transformedSize(var1);
      this.boundingBox = new StructureBoundingBox(0, 0, 0, var2.getX(), var2.getY() - 1, var2.getZ());
      switch(var1) {
      case NONE:
      default:
         break;
      case CLOCKWISE_90:
         this.boundingBox.offset(-var2.getX(), 0, 0);
         break;
      case COUNTERCLOCKWISE_90:
         this.boundingBox.offset(0, 0, -var2.getZ());
         break;
      case CLOCKWISE_180:
         this.boundingBox.offset(-var2.getX(), 0, -var2.getZ());
      }

      this.boundingBox.offset(this.templatePosition.getX(), this.templatePosition.getY(), this.templatePosition.getZ());
   }

   public void offset(int var1, int var2, int var3) {
      super.offset(var1, var2, var3);
      this.templatePosition = this.templatePosition.add(var1, var2, var3);
   }
}
