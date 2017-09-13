package net.minecraft.item;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multisets;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMap extends ItemMapBase {
   protected ItemMap() {
      this.setHasSubtypes(true);
   }

   @SideOnly(Side.CLIENT)
   public static MapData loadMapData(int var0, World var1) {
      String var2 = "map_" + var0;
      MapData var3 = (MapData)var1.loadData(MapData.class, var2);
      if (var3 == null) {
         var3 = new MapData(var2);
         var1.setData(var2, var3);
      }

      return var3;
   }

   public MapData getMapData(ItemStack var1, World var2) {
      String var3 = "map_" + var1.getMetadata();
      MapData var4 = (MapData)var2.loadData(MapData.class, var3);
      if (var4 == null && !var2.isRemote) {
         var1.setItemDamage(var2.getUniqueDataId("map"));
         var3 = "map_" + var1.getMetadata();
         var4 = new MapData(var3);
         var4.scale = 3;
         var4.calculateMapCenter((double)var2.getWorldInfo().getSpawnX(), (double)var2.getWorldInfo().getSpawnZ(), var4.scale);
         var4.dimension = var2.provider.getDimension();
         var4.markDirty();
         var2.setData(var3, var4);
      }

      return var4;
   }

   public void updateMapData(World var1, Entity var2, MapData var3) {
      if (var1.provider.getDimension() == var3.dimension && var2 instanceof EntityPlayer) {
         int var4 = 1 << var3.scale;
         int var5 = var3.xCenter;
         int var6 = var3.zCenter;
         int var7 = MathHelper.floor(var2.posX - (double)var5) / var4 + 64;
         int var8 = MathHelper.floor(var2.posZ - (double)var6) / var4 + 64;
         int var9 = 128 / var4;
         if (var1.provider.hasNoSky()) {
            var9 /= 2;
         }

         MapData.MapInfo var10 = var3.getMapInfo((EntityPlayer)var2);
         ++var10.step;
         boolean var11 = false;

         for(int var12 = var7 - var9 + 1; var12 < var7 + var9; ++var12) {
            if ((var12 & 15) == (var10.step & 15) || var11) {
               var11 = false;
               double var13 = 0.0D;

               for(int var15 = var8 - var9 - 1; var15 < var8 + var9; ++var15) {
                  if (var12 >= 0 && var15 >= -1 && var12 < 128 && var15 < 128) {
                     int var16 = var12 - var7;
                     int var17 = var15 - var8;
                     boolean var18 = var16 * var16 + var17 * var17 > (var9 - 2) * (var9 - 2);
                     int var19 = (var5 / var4 + var12 - 64) * var4;
                     int var20 = (var6 / var4 + var15 - 64) * var4;
                     HashMultiset var21 = HashMultiset.create();
                     Chunk var22 = var1.getChunkFromBlockCoords(new BlockPos(var19, 0, var20));
                     if (!var22.isEmpty()) {
                        int var23 = var19 & 15;
                        int var24 = var20 & 15;
                        int var25 = 0;
                        double var26 = 0.0D;
                        if (var1.provider.hasNoSky()) {
                           int var28 = var19 + var20 * 231871;
                           var28 = var28 * var28 * 31287121 + var28 * 11;
                           if ((var28 >> 20 & 1) == 0) {
                              var21.add(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT).getMapColor(), 10);
                           } else {
                              var21.add(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE).getMapColor(), 100);
                           }

                           var26 = 100.0D;
                        } else {
                           BlockPos.MutableBlockPos var37 = new BlockPos.MutableBlockPos();

                           for(int var29 = 0; var29 < var4; ++var29) {
                              for(int var30 = 0; var30 < var4; ++var30) {
                                 int var31 = var22.getHeightValue(var29 + var23, var30 + var24) + 1;
                                 IBlockState var32 = Blocks.AIR.getDefaultState();
                                 if (var31 > 1) {
                                    label168: {
                                       while(true) {
                                          --var31;
                                          var32 = var22.getBlockState(var37.setPos(var29 + var23, var31, var30 + var24));
                                          if (var32.getMapColor() != MapColor.AIR || var31 <= 0) {
                                             break;
                                          }
                                       }

                                       if (var31 > 0 && var32.getMaterial().isLiquid()) {
                                          int var33 = var31 - 1;

                                          while(true) {
                                             IBlockState var34 = var22.getBlockState(var29 + var23, var33--, var30 + var24);
                                             ++var25;
                                             if (var33 <= 0 || !var34.getMaterial().isLiquid()) {
                                                break label168;
                                             }
                                          }
                                       }
                                    }
                                 }

                                 var26 += (double)var31 / (double)(var4 * var4);
                                 var21.add(var32.getMapColor());
                              }
                           }
                        }

                        var25 = var25 / (var4 * var4);
                        double var38 = (var26 - var13) * 4.0D / (double)(var4 + 4) + ((double)(var12 + var15 & 1) - 0.5D) * 0.4D;
                        byte var40 = 1;
                        if (var38 > 0.6D) {
                           var40 = 2;
                        }

                        if (var38 < -0.6D) {
                           var40 = 0;
                        }

                        MapColor var41 = (MapColor)Iterables.getFirst(Multisets.copyHighestCountFirst(var21), MapColor.AIR);
                        if (var41 == MapColor.WATER) {
                           var38 = (double)var25 * 0.1D + (double)(var12 + var15 & 1) * 0.2D;
                           var40 = 1;
                           if (var38 < 0.5D) {
                              var40 = 2;
                           }

                           if (var38 > 0.9D) {
                              var40 = 0;
                           }
                        }

                        var13 = var26;
                        if (var15 >= 0 && var16 * var16 + var17 * var17 < var9 * var9 && (!var18 || (var12 + var15 & 1) != 0)) {
                           byte var42 = var3.colors[var12 + var15 * 128];
                           byte var43 = (byte)(var41.colorIndex * 4 + var40);
                           if (var42 != var43) {
                              var3.colors[var12 + var15 * 128] = var43;
                              var3.updateMapData(var12, var15);
                              var11 = true;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

   }

   public void onUpdate(ItemStack var1, World var2, Entity var3, int var4, boolean var5) {
      if (!var2.isRemote) {
         MapData var6 = this.getMapData(var1, var2);
         if (var3 instanceof EntityPlayer) {
            EntityPlayer var7 = (EntityPlayer)var3;
            var6.updateVisiblePlayers(var7, var1);
         }

         if (var5 || var3 instanceof EntityPlayer && ((EntityPlayer)var3).getHeldItemOffhand() == var1) {
            this.updateMapData(var2, var3, var6);
         }
      }

   }

   @Nullable
   public Packet createMapDataPacket(ItemStack var1, World var2, EntityPlayer var3) {
      return this.getMapData(var1, var2).getMapPacket(var1, var2, var3);
   }

   public void onCreated(ItemStack var1, World var2, EntityPlayer var3) {
      NBTTagCompound var4 = var1.getTagCompound();
      if (var4 != null) {
         if (var4.hasKey("map_scale_direction", 99)) {
            scaleMap(var1, var2, var4.getInteger("map_scale_direction"));
            var4.removeTag("map_scale_direction");
         } else if (var4.getBoolean("map_tracking_position")) {
            enableMapTracking(var1, var2);
            var4.removeTag("map_tracking_position");
         }
      }

   }

   protected static void scaleMap(ItemStack var0, World var1, int var2) {
      MapData var3 = Items.FILLED_MAP.getMapData(var0, var1);
      var0.setItemDamage(var1.getUniqueDataId("map"));
      MapData var4 = new MapData("map_" + var0.getMetadata());
      var4.scale = (byte)MathHelper.clamp(var3.scale + var2, 0, 4);
      var4.trackingPosition = var3.trackingPosition;
      var4.calculateMapCenter((double)var3.xCenter, (double)var3.zCenter, var4.scale);
      var4.dimension = var3.dimension;
      var4.markDirty();
      var1.setData("map_" + var0.getMetadata(), var4);
   }

   protected static void enableMapTracking(ItemStack var0, World var1) {
      MapData var2 = Items.FILLED_MAP.getMapData(var0, var1);
      var0.setItemDamage(var1.getUniqueDataId("map"));
      MapData var3 = new MapData("map_" + var0.getMetadata());
      var3.trackingPosition = true;
      var3.xCenter = var2.xCenter;
      var3.zCenter = var2.zCenter;
      var3.scale = var2.scale;
      var3.dimension = var2.dimension;
      var3.markDirty();
      var1.setData("map_" + var0.getMetadata(), var3);
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
      MapData var5 = this.getMapData(var1, var2.world);
      if (var4) {
         if (var5 == null) {
            var3.add("Unknown map");
         } else {
            var3.add("Scaling at 1:" + (1 << var5.scale));
            var3.add("(Level " + var5.scale + "/" + 4 + ")");
         }
      }

   }
}
