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

   public MapData getMapData(ItemStack itemstack, World world) {
      World worldMain = (World)world.getServer().getServer().worlds.get(0);
      String s = "map_" + itemstack.getMetadata();
      MapData worldmap = (MapData)worldMain.loadData(MapData.class, s);
      if (worldmap == null && !world.isRemote) {
         itemstack.setItemDamage(worldMain.getUniqueDataId("map"));
         s = "map_" + itemstack.getMetadata();
         worldmap = new MapData(s);
         worldmap.scale = 3;
         worldmap.calculateMapCenter((double)world.getWorldInfo().getSpawnX(), (double)world.getWorldInfo().getSpawnZ(), worldmap.scale);
         worldmap.dimension = (byte)((WorldServer)world).dimension;
         worldmap.markDirty();
         worldMain.setData(s, worldmap);
         MapInitializeEvent event = new MapInitializeEvent(worldmap.mapView);
         Bukkit.getServer().getPluginManager().callEvent(event);
      }

      return worldmap;
   }

   public void updateMapData(World world, Entity entity, MapData worldmap) {
      if (((WorldServer)world).dimension == worldmap.dimension && entity instanceof EntityPlayer) {
         int i = 1 << worldmap.scale;
         int j = worldmap.xCenter;
         int k = worldmap.zCenter;
         int l = MathHelper.floor(entity.posX - (double)j) / i + 64;
         int i1 = MathHelper.floor(entity.posZ - (double)k) / i + 64;
         int j1 = 128 / i;
         if (world.provider.hasNoSky()) {
            j1 /= 2;
         }

         MapData.MapInfo worldmap_worldmaphumantracker = worldmap.getMapInfo((EntityPlayer)entity);
         ++worldmap_worldmaphumantracker.step;
         boolean flag = false;

         for(int k1 = l - j1 + 1; k1 < l + j1; ++k1) {
            if ((k1 & 15) == (worldmap_worldmaphumantracker.step & 15) || flag) {
               flag = false;
               double d0 = 0.0D;

               for(int l1 = i1 - j1 - 1; l1 < i1 + j1; ++l1) {
                  if (k1 >= 0 && l1 >= -1 && k1 < 128 && l1 < 128) {
                     int i2 = k1 - l;
                     int j2 = l1 - i1;
                     boolean flag1 = i2 * i2 + j2 * j2 > (j1 - 2) * (j1 - 2);
                     int k2 = (j / i + k1 - 64) * i;
                     int l2 = (k / i + l1 - 64) * i;
                     HashMultiset hashmultiset = HashMultiset.create();
                     Chunk chunk = world.getChunkFromBlockCoords(new BlockPos(k2, 0, l2));
                     if (!chunk.isEmpty()) {
                        int i3 = k2 & 15;
                        int j3 = l2 & 15;
                        int k3 = 0;
                        double d1 = 0.0D;
                        if (world.provider.hasNoSky()) {
                           int l3 = k2 + l2 * 231871;
                           l3 = l3 * l3 * 31287121 + l3 * 11;
                           if ((l3 >> 20 & 1) == 0) {
                              hashmultiset.add(Blocks.DIRT.getDefaultState().withProperty(BlockDirt.VARIANT, BlockDirt.DirtType.DIRT).getMapColor(), 10);
                           } else {
                              hashmultiset.add(Blocks.STONE.getDefaultState().withProperty(BlockStone.VARIANT, BlockStone.EnumType.STONE).getMapColor(), 100);
                           }

                           d1 = 100.0D;
                        } else {
                           BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos();

                           for(int i4 = 0; i4 < i; ++i4) {
                              for(int j4 = 0; j4 < i; ++j4) {
                                 int k4 = chunk.getHeightValue(i4 + i3, j4 + j3) + 1;
                                 IBlockState iblockdata = Blocks.AIR.getDefaultState();
                                 if (k4 > 1) {
                                    label165: {
                                       while(true) {
                                          --k4;
                                          iblockdata = chunk.getBlockState(blockposition_mutableblockposition.setPos(i4 + i3, k4, j4 + j3));
                                          if (iblockdata.getMapColor() != MapColor.AIR || k4 <= 0) {
                                             break;
                                          }
                                       }

                                       if (k4 > 0 && iblockdata.getMaterial().isLiquid()) {
                                          int l4 = k4 - 1;

                                          while(true) {
                                             IBlockState iblockdata1 = chunk.getBlockState(i4 + i3, l4--, j4 + j3);
                                             ++k3;
                                             if (l4 <= 0 || !iblockdata1.getMaterial().isLiquid()) {
                                                break label165;
                                             }
                                          }
                                       }
                                    }
                                 }

                                 d1 += (double)k4 / (double)(i * i);
                                 hashmultiset.add(iblockdata.getMapColor());
                              }
                           }
                        }

                        k3 = k3 / (i * i);
                        double d2 = (d1 - d0) * 4.0D / (double)(i + 4) + ((double)(k1 + l1 & 1) - 0.5D) * 0.4D;
                        byte b0 = 1;
                        if (d2 > 0.6D) {
                           b0 = 2;
                        }

                        if (d2 < -0.6D) {
                           b0 = 0;
                        }

                        MapColor materialmapcolor = (MapColor)Iterables.getFirst(Multisets.copyHighestCountFirst(hashmultiset), MapColor.AIR);
                        if (materialmapcolor == MapColor.WATER) {
                           d2 = (double)k3 * 0.1D + (double)(k1 + l1 & 1) * 0.2D;
                           b0 = 1;
                           if (d2 < 0.5D) {
                              b0 = 2;
                           }

                           if (d2 > 0.9D) {
                              b0 = 0;
                           }
                        }

                        d0 = d1;
                        if (l1 >= 0 && i2 * i2 + j2 * j2 < j1 * j1 && (!flag1 || (k1 + l1 & 1) != 0)) {
                           byte b1 = worldmap.colors[k1 + l1 * 128];
                           byte b2 = (byte)(materialmapcolor.colorIndex * 4 + b0);
                           if (b1 != b2) {
                              worldmap.colors[k1 + l1 * 128] = b2;
                              worldmap.updateMapData(k1, l1);
                              flag = true;
                           }
                        }
                     }
                  }
               }
            }
         }
      }

   }

   public void onUpdate(ItemStack itemstack, World world, Entity entity, int i, boolean flag) {
      if (!world.isRemote) {
         MapData worldmap = this.getMapData(itemstack, world);
         if (entity instanceof EntityPlayer) {
            EntityPlayer entityhuman = (EntityPlayer)entity;
            worldmap.updateVisiblePlayers(entityhuman, itemstack);
         }

         if (flag || entity instanceof EntityPlayer && ((EntityPlayer)entity).getHeldItemOffhand() == itemstack) {
            this.updateMapData(world, entity, worldmap);
         }
      }

   }

   @Nullable
   public Packet createMapDataPacket(ItemStack itemstack, World world, EntityPlayer entityhuman) {
      return this.getMapData(itemstack, world).getMapPacket(itemstack, world, entityhuman);
   }

   public void onCreated(ItemStack itemstack, World world, EntityPlayer entityhuman) {
      NBTTagCompound nbttagcompound = itemstack.getTagCompound();
      if (nbttagcompound != null) {
         if (nbttagcompound.hasKey("map_scale_direction", 99)) {
            scaleMap(itemstack, world, nbttagcompound.getInteger("map_scale_direction"));
            nbttagcompound.removeTag("map_scale_direction");
         } else if (nbttagcompound.getBoolean("map_tracking_position")) {
            enableMapTracking(itemstack, world);
            nbttagcompound.removeTag("map_tracking_position");
         }
      }

   }

   protected static void scaleMap(ItemStack itemstack, World world, int i) {
      MapData worldmap = Items.FILLED_MAP.getMapData(itemstack, world);
      world = (World)world.getServer().getServer().worlds.get(0);
      itemstack.setItemDamage(world.getUniqueDataId("map"));
      MapData worldmap1 = new MapData("map_" + itemstack.getMetadata());
      worldmap1.scale = (byte)MathHelper.clamp(worldmap.scale + i, 0, 4);
      worldmap1.trackingPosition = worldmap.trackingPosition;
      worldmap1.calculateMapCenter((double)worldmap.xCenter, (double)worldmap.zCenter, worldmap1.scale);
      worldmap1.dimension = worldmap.dimension;
      worldmap1.markDirty();
      world.setData("map_" + itemstack.getMetadata(), worldmap1);
      MapInitializeEvent event = new MapInitializeEvent(worldmap1.mapView);
      Bukkit.getServer().getPluginManager().callEvent(event);
   }

   protected static void enableMapTracking(ItemStack itemstack, World world) {
      MapData worldmap = Items.FILLED_MAP.getMapData(itemstack, world);
      world = (World)world.getServer().getServer().worlds.get(0);
      itemstack.setItemDamage(world.getUniqueDataId("map"));
      MapData worldmap1 = new MapData("map_" + itemstack.getMetadata());
      worldmap1.trackingPosition = true;
      worldmap1.xCenter = worldmap.xCenter;
      worldmap1.zCenter = worldmap.zCenter;
      worldmap1.scale = worldmap.scale;
      worldmap1.dimension = worldmap.dimension;
      worldmap1.markDirty();
      world.setData("map_" + itemstack.getMetadata(), worldmap1);
      MapInitializeEvent event = new MapInitializeEvent(worldmap1.mapView);
      Bukkit.getServer().getPluginManager().callEvent(event);
   }
}
