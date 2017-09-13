package net.minecraft.world.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec4b;
import net.minecraft.world.WorldSavedData;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.craftbukkit.v1_10_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_10_R1.map.CraftMapView;
import org.bukkit.craftbukkit.v1_10_R1.map.RenderData;
import org.bukkit.map.MapCursor;

public class MapData extends WorldSavedData {
   public int xCenter;
   public int zCenter;
   public byte dimension;
   public boolean trackingPosition;
   public byte scale;
   public byte[] colors = new byte[16384];
   public List playersArrayList = Lists.newArrayList();
   private final Map playersHashMap = Maps.newHashMap();
   public Map mapDecorations = Maps.newLinkedHashMap();
   public final CraftMapView mapView = new CraftMapView(this);
   private CraftServer server = (CraftServer)Bukkit.getServer();
   private UUID uniqueId = null;

   public MapData(String s) {
      super(s);
   }

   public void calculateMapCenter(double d0, double d1, int i) {
      int j = 128 * (1 << i);
      int k = MathHelper.floor((d0 + 64.0D) / (double)j);
      int l = MathHelper.floor((d1 + 64.0D) / (double)j);
      this.xCenter = k * j + j / 2 - 64;
      this.zCenter = l * j + j / 2 - 64;
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      byte dimension = nbttagcompound.getByte("dimension");
      if (dimension >= 10) {
         long least = nbttagcompound.getLong("UUIDLeast");
         long most = nbttagcompound.getLong("UUIDMost");
         if (least != 0L && most != 0L) {
            this.uniqueId = new UUID(most, least);
            CraftWorld world = (CraftWorld)this.server.getWorld(this.uniqueId);
            if (world == null) {
               dimension = 127;
            } else {
               dimension = (byte)world.getHandle().dimension;
            }
         }
      }

      this.dimension = dimension;
      this.xCenter = nbttagcompound.getInteger("xCenter");
      this.zCenter = nbttagcompound.getInteger("zCenter");
      this.scale = nbttagcompound.getByte("scale");
      this.scale = (byte)MathHelper.clamp(this.scale, 0, 4);
      if (nbttagcompound.hasKey("trackingPosition", 1)) {
         this.trackingPosition = nbttagcompound.getBoolean("trackingPosition");
      } else {
         this.trackingPosition = true;
      }

      short short0 = nbttagcompound.getShort("width");
      short short1 = nbttagcompound.getShort("height");
      if (short0 == 128 && short1 == 128) {
         this.colors = nbttagcompound.getByteArray("colors");
      } else {
         byte[] abyte = nbttagcompound.getByteArray("colors");
         this.colors = new byte[16384];
         int i = (128 - short0) / 2;
         int j = (128 - short1) / 2;

         for(int k = 0; k < short1; ++k) {
            int l = k + j;
            if (l >= 0 || l < 128) {
               for(int i1 = 0; i1 < short0; ++i1) {
                  int j1 = i1 + i;
                  if (j1 >= 0 || j1 < 128) {
                     this.colors[j1 + l * 128] = abyte[i1 + k * short0];
                  }
               }
            }
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      if (this.dimension >= 10) {
         if (this.uniqueId == null) {
            for(World world : this.server.getWorlds()) {
               CraftWorld cWorld = (CraftWorld)world;
               if (cWorld.getHandle().dimension == this.dimension) {
                  this.uniqueId = cWorld.getUID();
                  break;
               }
            }
         }

         if (this.uniqueId != null) {
            nbttagcompound.setLong("UUIDLeast", this.uniqueId.getLeastSignificantBits());
            nbttagcompound.setLong("UUIDMost", this.uniqueId.getMostSignificantBits());
         }
      }

      nbttagcompound.setByte("dimension", this.dimension);
      nbttagcompound.setInteger("xCenter", this.xCenter);
      nbttagcompound.setInteger("zCenter", this.zCenter);
      nbttagcompound.setByte("scale", this.scale);
      nbttagcompound.setShort("width", (short)128);
      nbttagcompound.setShort("height", (short)128);
      nbttagcompound.setByteArray("colors", this.colors);
      nbttagcompound.setBoolean("trackingPosition", this.trackingPosition);
      return nbttagcompound;
   }

   public void updateVisiblePlayers(EntityPlayer entityhuman, ItemStack itemstack) {
      if (!this.playersHashMap.containsKey(entityhuman)) {
         MapData.MapInfo worldmap_worldmaphumantracker = new MapData.MapInfo(entityhuman);
         this.playersHashMap.put(entityhuman, worldmap_worldmaphumantracker);
         this.playersArrayList.add(worldmap_worldmaphumantracker);
      }

      if (!entityhuman.inventory.hasItemStack(itemstack)) {
         this.mapDecorations.remove(entityhuman.getName());
      }

      for(int i = 0; i < this.playersArrayList.size(); ++i) {
         MapData.MapInfo worldmap_worldmaphumantracker1 = (MapData.MapInfo)this.playersArrayList.get(i);
         if (!worldmap_worldmaphumantracker1.entityplayerObj.isDead && (worldmap_worldmaphumantracker1.entityplayerObj.inventory.hasItemStack(itemstack) || itemstack.isOnItemFrame())) {
            if (!itemstack.isOnItemFrame() && worldmap_worldmaphumantracker1.entityplayerObj.dimension == this.dimension && this.trackingPosition) {
               this.updateDecorations(0, worldmap_worldmaphumantracker1.entityplayerObj.world, worldmap_worldmaphumantracker1.entityplayerObj.getName(), worldmap_worldmaphumantracker1.entityplayerObj.posX, worldmap_worldmaphumantracker1.entityplayerObj.posZ, (double)worldmap_worldmaphumantracker1.entityplayerObj.rotationYaw);
            }
         } else {
            this.playersHashMap.remove(worldmap_worldmaphumantracker1.entityplayerObj);
            this.playersArrayList.remove(worldmap_worldmaphumantracker1);
         }
      }

      if (itemstack.isOnItemFrame() && this.trackingPosition) {
         EntityItemFrame entityitemframe = itemstack.getItemFrame();
         BlockPos blockposition = entityitemframe.getHangingPosition();
         this.updateDecorations(1, entityhuman.world, "frame-" + entityitemframe.getEntityId(), (double)blockposition.getX(), (double)blockposition.getZ(), (double)(entityitemframe.facingDirection.getHorizontalIndex() * 90));
      }

      if (itemstack.hasTagCompound() && itemstack.getTagCompound().hasKey("Decorations", 9)) {
         NBTTagList nbttaglist = itemstack.getTagCompound().getTagList("Decorations", 10);

         for(int j = 0; j < nbttaglist.tagCount(); ++j) {
            NBTTagCompound nbttagcompound = nbttaglist.getCompoundTagAt(j);
            if (!this.mapDecorations.containsKey(nbttagcompound.getString("id"))) {
               this.updateDecorations(nbttagcompound.getByte("type"), entityhuman.world, nbttagcompound.getString("id"), nbttagcompound.getDouble("x"), nbttagcompound.getDouble("z"), nbttagcompound.getDouble("rot"));
            }
         }
      }

   }

   private void updateDecorations(int i, net.minecraft.world.World world, String s, double d0, double d1, double d2) {
      int j = 1 << this.scale;
      float f = (float)(d0 - (double)this.xCenter) / (float)j;
      float f1 = (float)(d1 - (double)this.zCenter) / (float)j;
      byte b0 = (byte)((int)((double)(f * 2.0F) + 0.5D));
      byte b1 = (byte)((int)((double)(f1 * 2.0F) + 0.5D));
      byte b2;
      if (f >= -63.0F && f1 >= -63.0F && f <= 63.0F && f1 <= 63.0F) {
         d2 = d2 + (d2 < 0.0D ? -8.0D : 8.0D);
         b2 = (byte)((int)(d2 * 16.0D / 360.0D));
         if (this.dimension < 0) {
            int k = (int)(world.getWorldInfo().getWorldTime() / 10L);
            b2 = (byte)(k * k * 34187121 + k * 121 >> 15 & 15);
         }
      } else {
         if (Math.abs(f) >= 320.0F || Math.abs(f1) >= 320.0F) {
            this.mapDecorations.remove(s);
            return;
         }

         i = 6;
         b2 = 0;
         if (f <= -63.0F) {
            b0 = -128;
         }

         if (f1 <= -63.0F) {
            b1 = -128;
         }

         if (f >= 63.0F) {
            b0 = 127;
         }

         if (f1 >= 63.0F) {
            b1 = 127;
         }
      }

      this.mapDecorations.put(s, new Vec4b((byte)i, b0, b1, b2));
   }

   @Nullable
   public Packet getMapPacket(ItemStack itemstack, net.minecraft.world.World world, EntityPlayer entityhuman) {
      MapData.MapInfo worldmap_worldmaphumantracker = (MapData.MapInfo)this.playersHashMap.get(entityhuman);
      return worldmap_worldmaphumantracker == null ? null : worldmap_worldmaphumantracker.getPacket(itemstack);
   }

   public void updateMapData(int i, int j) {
      super.markDirty();

      for(MapData.MapInfo worldmap_worldmaphumantracker : this.playersArrayList) {
         worldmap_worldmaphumantracker.update(i, j);
      }

   }

   public MapData.MapInfo getMapInfo(EntityPlayer entityhuman) {
      MapData.MapInfo worldmap_worldmaphumantracker = (MapData.MapInfo)this.playersHashMap.get(entityhuman);
      if (worldmap_worldmaphumantracker == null) {
         worldmap_worldmaphumantracker = new MapData.MapInfo(entityhuman);
         this.playersHashMap.put(entityhuman, worldmap_worldmaphumantracker);
         this.playersArrayList.add(worldmap_worldmaphumantracker);
      }

      return worldmap_worldmaphumantracker;
   }

   public class MapInfo {
      public final EntityPlayer entityplayerObj;
      private boolean isDirty = true;
      private int minX;
      private int minY;
      private int maxX = 127;
      private int maxY = 127;
      private int tick;
      public int step;

      public MapInfo(EntityPlayer entityhuman) {
         this.entityplayerObj = entityhuman;
      }

      public Packet getPacket(ItemStack itemstack) {
         RenderData render = MapData.this.mapView.render((CraftPlayer)this.entityplayerObj.getBukkitEntity());
         Collection icons = new ArrayList();

         for(MapCursor cursor : render.cursors) {
            if (cursor.isVisible()) {
               icons.add(new Vec4b(cursor.getRawType(), cursor.getX(), cursor.getY(), cursor.getDirection()));
            }
         }

         if (this.isDirty) {
            this.isDirty = false;
            return new SPacketMaps(itemstack.getMetadata(), MapData.this.scale, MapData.this.trackingPosition, icons, render.buffer, this.minX, this.minY, this.maxX + 1 - this.minX, this.maxY + 1 - this.minY);
         } else {
            return this.tick++ % 5 == 0 ? new SPacketMaps(itemstack.getMetadata(), MapData.this.scale, MapData.this.trackingPosition, icons, render.buffer, 0, 0, 0, 0) : null;
         }
      }

      public void update(int i, int j) {
         if (this.isDirty) {
            this.minX = Math.min(this.minX, i);
            this.minY = Math.min(this.minY, j);
            this.maxX = Math.max(this.maxX, i);
            this.maxY = Math.max(this.maxY, j);
         } else {
            this.isDirty = true;
            this.minX = i;
            this.minY = j;
            this.maxX = i;
            this.maxY = j;
         }

      }
   }
}
