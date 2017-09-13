package net.minecraft.world.gen.structure.template;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.Mirror;
import net.minecraft.util.ObjectIntIdentityMap;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

public class Template {
   private final List blocks = Lists.newArrayList();
   private final List entities = Lists.newArrayList();
   private BlockPos size = BlockPos.ORIGIN;
   private String author = "?";

   public BlockPos getSize() {
      return this.size;
   }

   public void setAuthor(String var1) {
      this.author = var1;
   }

   public String getAuthor() {
      return this.author;
   }

   public void takeBlocksFromWorld(World var1, BlockPos var2, BlockPos var3, boolean var4, @Nullable Block var5) {
      if (var3.getX() >= 1 && var3.getY() >= 1 && var3.getZ() >= 1) {
         BlockPos var6 = var2.add(var3).add(-1, -1, -1);
         ArrayList var7 = Lists.newArrayList();
         ArrayList var8 = Lists.newArrayList();
         ArrayList var9 = Lists.newArrayList();
         BlockPos var10 = new BlockPos(Math.min(var2.getX(), var6.getX()), Math.min(var2.getY(), var6.getY()), Math.min(var2.getZ(), var6.getZ()));
         BlockPos var11 = new BlockPos(Math.max(var2.getX(), var6.getX()), Math.max(var2.getY(), var6.getY()), Math.max(var2.getZ(), var6.getZ()));
         this.size = var3;

         for(BlockPos.MutableBlockPos var13 : BlockPos.getAllInBoxMutable(var10, var11)) {
            BlockPos var14 = var13.subtract(var10);
            IBlockState var15 = var1.getBlockState(var13);
            if (var5 == null || var5 != var15.getBlock()) {
               TileEntity var16 = var1.getTileEntity(var13);
               if (var16 != null) {
                  NBTTagCompound var17 = var16.writeToNBT(new NBTTagCompound());
                  var17.removeTag("x");
                  var17.removeTag("y");
                  var17.removeTag("z");
                  var8.add(new Template.BlockInfo(var14, var15, var17));
               } else if (!var15.isFullBlock() && !var15.isFullCube()) {
                  var9.add(new Template.BlockInfo(var14, var15, (NBTTagCompound)null));
               } else {
                  var7.add(new Template.BlockInfo(var14, var15, (NBTTagCompound)null));
               }
            }
         }

         this.blocks.clear();
         this.blocks.addAll(var7);
         this.blocks.addAll(var8);
         this.blocks.addAll(var9);
         if (var4) {
            this.takeEntitiesFromWorld(var1, var10, var11.add(1, 1, 1));
         } else {
            this.entities.clear();
         }

      }
   }

   private void takeEntitiesFromWorld(World var1, BlockPos var2, BlockPos var3) {
      List var4 = var1.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(var2, var3), new Predicate() {
         public boolean apply(@Nullable Entity var1) {
            return !(var1 instanceof EntityPlayer);
         }

         // $FF: synthetic method
         public boolean apply(Object var1) {
            return this.apply((Entity)var1);
         }
      });
      this.entities.clear();

      for(Entity var6 : var4) {
         Vec3d var7 = new Vec3d(var6.posX - (double)var2.getX(), var6.posY - (double)var2.getY(), var6.posZ - (double)var2.getZ());
         NBTTagCompound var8 = new NBTTagCompound();
         var6.writeToNBTOptional(var8);
         BlockPos var9;
         if (var6 instanceof EntityPainting) {
            var9 = ((EntityPainting)var6).getHangingPosition().subtract(var2);
         } else {
            var9 = new BlockPos(var7);
         }

         this.entities.add(new Template.EntityInfo(var7, var9, var8));
      }

   }

   public Map getDataBlocks(BlockPos var1, PlacementSettings var2) {
      HashMap var3 = Maps.newHashMap();
      StructureBoundingBox var4 = var2.getBoundingBox();

      for(Template.BlockInfo var6 : this.blocks) {
         BlockPos var7 = transformedBlockPos(var2, var6.pos).add(var1);
         if (var4 == null || var4.isVecInside(var7)) {
            IBlockState var8 = var6.blockState;
            if (var8.getBlock() == Blocks.STRUCTURE_BLOCK && var6.tileentityData != null) {
               TileEntityStructure.Mode var9 = TileEntityStructure.Mode.valueOf(var6.tileentityData.getString("mode"));
               if (var9 == TileEntityStructure.Mode.DATA) {
                  var3.put(var7, var6.tileentityData.getString("metadata"));
               }
            }
         }
      }

      return var3;
   }

   public BlockPos calculateConnectedPos(PlacementSettings var1, BlockPos var2, PlacementSettings var3, BlockPos var4) {
      BlockPos var5 = transformedBlockPos(var1, var2);
      BlockPos var6 = transformedBlockPos(var3, var4);
      return var5.subtract(var6);
   }

   public static BlockPos transformedBlockPos(PlacementSettings var0, BlockPos var1) {
      return transformedBlockPos(var1, var0.getMirror(), var0.getRotation());
   }

   public void addBlocksToWorldChunk(World var1, BlockPos var2, PlacementSettings var3) {
      var3.setBoundingBoxFromChunk();
      this.addBlocksToWorld(var1, var2, var3);
   }

   public void addBlocksToWorld(World var1, BlockPos var2, PlacementSettings var3) {
      this.addBlocksToWorld(var1, var2, new BlockRotationProcessor(var2, var3), var3, 2);
   }

   public void addBlocksToWorld(World var1, BlockPos var2, PlacementSettings var3, int var4) {
      this.addBlocksToWorld(var1, var2, new BlockRotationProcessor(var2, var3), var3, var4);
   }

   public void addBlocksToWorld(World var1, BlockPos var2, @Nullable ITemplateProcessor var3, PlacementSettings var4, int var5) {
      if (!this.blocks.isEmpty() && this.size.getX() >= 1 && this.size.getY() >= 1 && this.size.getZ() >= 1) {
         Block var6 = var4.getReplacedBlock();
         StructureBoundingBox var7 = var4.getBoundingBox();

         for(Template.BlockInfo var9 : this.blocks) {
            BlockPos var10 = transformedBlockPos(var4, var9.pos).add(var2);
            Template.BlockInfo var11 = var3 != null ? var3.processBlock(var1, var10, var9) : var9;
            if (var11 != null) {
               Block var12 = var11.blockState.getBlock();
               if ((var6 == null || var6 != var12) && (!var4.getIgnoreStructureBlock() || var12 != Blocks.STRUCTURE_BLOCK) && (var7 == null || var7.isVecInside(var10))) {
                  IBlockState var13 = var11.blockState.withMirror(var4.getMirror());
                  IBlockState var14 = var13.withRotation(var4.getRotation());
                  if (var11.tileentityData != null) {
                     TileEntity var15 = var1.getTileEntity(var10);
                     if (var15 != null) {
                        if (var15 instanceof IInventory) {
                           ((IInventory)var15).clear();
                        }

                        var1.setBlockState(var10, Blocks.BARRIER.getDefaultState(), 4);
                     }
                  }

                  if (var1.setBlockState(var10, var14, var5) && var11.tileentityData != null) {
                     TileEntity var20 = var1.getTileEntity(var10);
                     if (var20 != null) {
                        var11.tileentityData.setInteger("x", var10.getX());
                        var11.tileentityData.setInteger("y", var10.getY());
                        var11.tileentityData.setInteger("z", var10.getZ());
                        var20.readFromNBT(var11.tileentityData);
                        var20.mirror(var4.getMirror());
                        var20.rotate(var4.getRotation());
                     }
                  }
               }
            }
         }

         for(Template.BlockInfo var17 : this.blocks) {
            if (var6 == null || var6 != var17.blockState.getBlock()) {
               BlockPos var18 = transformedBlockPos(var4, var17.pos).add(var2);
               if (var7 == null || var7.isVecInside(var18)) {
                  var1.notifyNeighborsRespectDebug(var18, var17.blockState.getBlock());
                  if (var17.tileentityData != null) {
                     TileEntity var19 = var1.getTileEntity(var18);
                     if (var19 != null) {
                        var19.markDirty();
                     }
                  }
               }
            }
         }

         if (!var4.getIgnoreEntities()) {
            this.addEntitiesToWorld(var1, var2, var4.getMirror(), var4.getRotation(), var7);
         }

      }
   }

   private void addEntitiesToWorld(World var1, BlockPos var2, Mirror var3, Rotation var4, @Nullable StructureBoundingBox var5) {
      for(Template.EntityInfo var7 : this.entities) {
         BlockPos var8 = transformedBlockPos(var7.blockPos, var3, var4).add(var2);
         if (var5 == null || var5.isVecInside(var8)) {
            NBTTagCompound var9 = var7.entityData;
            Vec3d var10 = transformedVec3d(var7.pos, var3, var4);
            Vec3d var11 = var10.addVector((double)var2.getX(), (double)var2.getY(), (double)var2.getZ());
            NBTTagList var12 = new NBTTagList();
            var12.appendTag(new NBTTagDouble(var11.xCoord));
            var12.appendTag(new NBTTagDouble(var11.yCoord));
            var12.appendTag(new NBTTagDouble(var11.zCoord));
            var9.setTag("Pos", var12);
            var9.setUniqueId("UUID", UUID.randomUUID());

            Entity var13;
            try {
               var13 = EntityList.createEntityFromNBT(var9, var1);
            } catch (Exception var15) {
               var13 = null;
            }

            if (var13 != null) {
               float var14 = var13.getMirroredYaw(var3);
               var14 = var14 + (var13.rotationYaw - var13.getRotatedYaw(var4));
               var13.setLocationAndAngles(var11.xCoord, var11.yCoord, var11.zCoord, var14, var13.rotationPitch);
               var1.spawnEntity(var13);
            }
         }
      }

   }

   public BlockPos transformedSize(Rotation var1) {
      switch(var1) {
      case COUNTERCLOCKWISE_90:
      case CLOCKWISE_90:
         return new BlockPos(this.size.getZ(), this.size.getY(), this.size.getX());
      default:
         return this.size;
      }
   }

   private static BlockPos transformedBlockPos(BlockPos var0, Mirror var1, Rotation var2) {
      int var3 = var0.getX();
      int var4 = var0.getY();
      int var5 = var0.getZ();
      boolean var6 = true;
      switch(var1) {
      case LEFT_RIGHT:
         var5 = -var5;
         break;
      case FRONT_BACK:
         var3 = -var3;
         break;
      default:
         var6 = false;
      }

      switch(var2) {
      case COUNTERCLOCKWISE_90:
         return new BlockPos(var5, var4, -var3);
      case CLOCKWISE_90:
         return new BlockPos(-var5, var4, var3);
      case CLOCKWISE_180:
         return new BlockPos(-var3, var4, -var5);
      default:
         return var6 ? new BlockPos(var3, var4, var5) : var0;
      }
   }

   private static Vec3d transformedVec3d(Vec3d var0, Mirror var1, Rotation var2) {
      double var3 = var0.xCoord;
      double var5 = var0.yCoord;
      double var7 = var0.zCoord;
      boolean var9 = true;
      switch(var1) {
      case LEFT_RIGHT:
         var7 = 1.0D - var7;
         break;
      case FRONT_BACK:
         var3 = 1.0D - var3;
         break;
      default:
         var9 = false;
      }

      switch(var2) {
      case COUNTERCLOCKWISE_90:
         return new Vec3d(var7, var5, 1.0D - var3);
      case CLOCKWISE_90:
         return new Vec3d(1.0D - var7, var5, var3);
      case CLOCKWISE_180:
         return new Vec3d(1.0D - var3, var5, 1.0D - var7);
      default:
         return var9 ? new Vec3d(var3, var5, var7) : var0;
      }
   }

   public BlockPos getZeroPositionWithTransform(BlockPos var1, Mirror var2, Rotation var3) {
      int var4 = this.getSize().getX() - 1;
      int var5 = this.getSize().getZ() - 1;
      int var6 = var2 == Mirror.FRONT_BACK ? var4 : 0;
      int var7 = var2 == Mirror.LEFT_RIGHT ? var5 : 0;
      BlockPos var8 = var1;
      switch(var3) {
      case COUNTERCLOCKWISE_90:
         var8 = var1.add(var7, 0, var4 - var6);
         break;
      case CLOCKWISE_90:
         var8 = var1.add(var5 - var7, 0, var6);
         break;
      case CLOCKWISE_180:
         var8 = var1.add(var4 - var6, 0, var5 - var7);
         break;
      case NONE:
         var8 = var1.add(var6, 0, var7);
      }

      return var8;
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      Template.BasicPalette var2 = new Template.BasicPalette();
      NBTTagList var3 = new NBTTagList();

      for(Template.BlockInfo var5 : this.blocks) {
         NBTTagCompound var6 = new NBTTagCompound();
         var6.setTag("pos", this.writeInts(var5.pos.getX(), var5.pos.getY(), var5.pos.getZ()));
         var6.setInteger("state", var2.idFor(var5.blockState));
         if (var5.tileentityData != null) {
            var6.setTag("nbt", var5.tileentityData);
         }

         var3.appendTag(var6);
      }

      NBTTagList var8 = new NBTTagList();

      for(Template.EntityInfo var11 : this.entities) {
         NBTTagCompound var7 = new NBTTagCompound();
         var7.setTag("pos", this.writeDoubles(var11.pos.xCoord, var11.pos.yCoord, var11.pos.zCoord));
         var7.setTag("blockPos", this.writeInts(var11.blockPos.getX(), var11.blockPos.getY(), var11.blockPos.getZ()));
         if (var11.entityData != null) {
            var7.setTag("nbt", var11.entityData);
         }

         var8.appendTag(var7);
      }

      NBTTagList var10 = new NBTTagList();

      for(IBlockState var13 : var2) {
         var10.appendTag(NBTUtil.writeBlockState(new NBTTagCompound(), var13));
      }

      var1.setTag("palette", var10);
      var1.setTag("blocks", var3);
      var1.setTag("entities", var8);
      var1.setTag("size", this.writeInts(this.size.getX(), this.size.getY(), this.size.getZ()));
      var1.setInteger("version", 1);
      var1.setString("author", this.author);
      return var1;
   }

   public void read(NBTTagCompound var1) {
      this.blocks.clear();
      this.entities.clear();
      NBTTagList var2 = var1.getTagList("size", 3);
      this.size = new BlockPos(var2.getIntAt(0), var2.getIntAt(1), var2.getIntAt(2));
      this.author = var1.getString("author");
      Template.BasicPalette var3 = new Template.BasicPalette();
      NBTTagList var4 = var1.getTagList("palette", 10);

      for(int var5 = 0; var5 < var4.tagCount(); ++var5) {
         var3.addMapping(NBTUtil.readBlockState(var4.getCompoundTagAt(var5)), var5);
      }

      NBTTagList var14 = var1.getTagList("blocks", 10);

      for(int var6 = 0; var6 < var14.tagCount(); ++var6) {
         NBTTagCompound var7 = var14.getCompoundTagAt(var6);
         NBTTagList var8 = var7.getTagList("pos", 3);
         BlockPos var9 = new BlockPos(var8.getIntAt(0), var8.getIntAt(1), var8.getIntAt(2));
         IBlockState var10 = var3.stateFor(var7.getInteger("state"));
         NBTTagCompound var11;
         if (var7.hasKey("nbt")) {
            var11 = var7.getCompoundTag("nbt");
         } else {
            var11 = null;
         }

         this.blocks.add(new Template.BlockInfo(var9, var10, var11));
      }

      NBTTagList var15 = var1.getTagList("entities", 10);

      for(int var16 = 0; var16 < var15.tagCount(); ++var16) {
         NBTTagCompound var17 = var15.getCompoundTagAt(var16);
         NBTTagList var18 = var17.getTagList("pos", 6);
         Vec3d var19 = new Vec3d(var18.getDoubleAt(0), var18.getDoubleAt(1), var18.getDoubleAt(2));
         NBTTagList var20 = var17.getTagList("blockPos", 3);
         BlockPos var12 = new BlockPos(var20.getIntAt(0), var20.getIntAt(1), var20.getIntAt(2));
         if (var17.hasKey("nbt")) {
            NBTTagCompound var13 = var17.getCompoundTag("nbt");
            this.entities.add(new Template.EntityInfo(var19, var12, var13));
         }
      }

   }

   private NBTTagList writeInts(int... var1) {
      NBTTagList var2 = new NBTTagList();

      for(int var6 : var1) {
         var2.appendTag(new NBTTagInt(var6));
      }

      return var2;
   }

   private NBTTagList writeDoubles(double... var1) {
      NBTTagList var2 = new NBTTagList();

      for(double var6 : var1) {
         var2.appendTag(new NBTTagDouble(var6));
      }

      return var2;
   }

   static class BasicPalette implements Iterable {
      public static final IBlockState DEFAULT_BLOCK_STATE = Blocks.AIR.getDefaultState();
      final ObjectIntIdentityMap ids;
      private int lastId;

      private BasicPalette() {
         this.ids = new ObjectIntIdentityMap(16);
      }

      public int idFor(IBlockState var1) {
         int var2 = this.ids.get(var1);
         if (var2 == -1) {
            var2 = this.lastId++;
            this.ids.put(var1, var2);
         }

         return var2;
      }

      @Nullable
      public IBlockState stateFor(int var1) {
         IBlockState var2 = (IBlockState)this.ids.getByValue(var1);
         return var2 == null ? DEFAULT_BLOCK_STATE : var2;
      }

      public Iterator iterator() {
         return this.ids.iterator();
      }

      public void addMapping(IBlockState var1, int var2) {
         this.ids.put(var1, var2);
      }
   }

   public static class BlockInfo {
      public final BlockPos pos;
      public final IBlockState blockState;
      public final NBTTagCompound tileentityData;

      public BlockInfo(BlockPos var1, IBlockState var2, @Nullable NBTTagCompound var3) {
         this.pos = var1;
         this.blockState = var2;
         this.tileentityData = var3;
      }
   }

   public static class EntityInfo {
      public final Vec3d pos;
      public final BlockPos blockPos;
      public final NBTTagCompound entityData;

      public EntityInfo(Vec3d var1, BlockPos var2, NBTTagCompound var3) {
         this.pos = var1;
         this.blockPos = var2;
         this.entityData = var3;
      }
   }
}
