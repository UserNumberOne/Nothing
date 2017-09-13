package net.minecraft.item;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multisets;
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
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.MapData;
import org.bukkit.Bukkit;
import org.bukkit.event.server.MapInitializeEvent;

public class ItemMap extends ItemMapBase {
   protected ItemMap() {
      this.setHasSubtypes(true);
   }

   public MapData getMapData(ItemStack var1, World var2) {
      World var3 = (World)var2.getServer().getServer().worlds.get(0);
      String var4 = "map_" + var1.getMetadata();
      MapData var5 = (MapData)var3.loadData(MapData.class, var4);
      if (var5 == null && !var2.isRemote) {
         var1.setItemDamage(var3.getUniqueDataId("map"));
         var4 = "map_" + var1.getMetadata();
         var5 = new MapData(var4);
         var5.scale = 3;
         var5.calculateMapCenter((double)var2.getWorldInfo().getSpawnX(), (double)var2.getWorldInfo().getSpawnZ(), var5.scale);
         var5.dimension = (byte)((WorldServer)var2).dimension;
         var5.markDirty();
         var3.setData(var4, var5);
         MapInitializeEvent var6 = new MapInitializeEvent(var5.mapView);
         Bukkit.getServer().getPluginManager().callEvent(var6);
      }

      return var5;
   }

   public void updateMapData(World var1, Entity var2, MapData var3) {
      if (((WorldServer)var1).dimension == var3.dimension && var2 instanceof EntityPlayer) {
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
                           BlockPos.MutableBlockPos var39 = new BlockPos.MutableBlockPos();

                           for(int var29 = 0; var29 < var4; ++var29) {
                              for(int var30 = 0; var30 < var4; ++var30) {
                                 int var31 = var22.getHeightValue(var29 + var23, var30 + var24) + 1;
                                 IBlockState var32 = Blocks.AIR.getDefaultState();
                                 if (var31 > 1) {
                                    label165: {
                                       while(true) {
                                          --var31;
                                          var32 = var22.getBlockState(var39.setPos(var29 + var23, var31, var30 + var24));
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
                                                break label165;
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
                        double var35 = (var26 - var13) * 4.0D / (double)(var4 + 4) + ((double)(var12 + var15 & 1) - 0.5D) * 0.4D;
                        byte var40 = 1;
                        if (var35 > 0.6D) {
                           var40 = 2;
                        }

                        if (var35 < -0.6D) {
                           var40 = 0;
                        }

                        MapColor var41 = (MapColor)Iterables.getFirst(Multisets.copyHighestCountFirst(var21), MapColor.AIR);
                        if (var41 == MapColor.WATER) {
                           var35 = (double)var25 * 0.1D + (double)(var12 + var15 & 1) * 0.2D;
                           var40 = 1;
                           if (var35 < 0.5D) {
                              var40 = 2;
                           }

                           if (var35 > 0.9D) {
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
      var1 = (World)var1.getServer().getServer().worlds.get(0);
      var0.setItemDamage(var1.getUniqueDataId("map"));
      MapData var4 = new MapData("map_" + var0.getMetadata());
      var4.scale = (byte)MathHelper.clamp(var3.scale + var2, 0, 4);
      var4.trackingPosition = var3.trackingPosition;
      var4.calculateMapCenter((double)var3.xCenter, (double)var3.zCenter, var4.scale);
      var4.dimension = var3.dimension;
      var4.markDirty();
      var1.setData("map_" + var0.getMetadata(), var4);
      MapInitializeEvent var5 = new MapInitializeEvent(var4.mapView);
      Bukkit.getServer().getPluginManager().callEvent(var5);
   }

   protected static void enableMapTracking(ItemStack var0, World var1) {
      MapData var2 = Items.FILLED_MAP.getMapData(var0, var1);
      var1 = (World)var1.getServer().getServer().worlds.get(0);
      var0.setItemDamage(var1.getUniqueDataId("map"));
      MapData var3 = new MapData("map_" + var0.getMetadata());
      var3.trackingPosition = true;
      var3.xCenter = var2.xCenter;
      var3.zCenter = var2.zCenter;
      var3.scale = var2.scale;
      var3.dimension = var2.dimension;
      var3.markDirty();
      var1.setData("map_" + var0.getMetadata(), var3);
      MapInitializeEvent var4 = new MapInitializeEvent(var3.mapView);
      Bukkit.getServer().getPluginManager().callEvent(var4);
   }
}
