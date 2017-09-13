package net.minecraft.world.storage;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec4b;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

public class MapData extends WorldSavedData {
   public int xCenter;
   public int zCenter;
   public int dimension;
   public boolean trackingPosition;
   public byte scale;
   public byte[] colors = new byte[16384];
   public List playersArrayList = Lists.newArrayList();
   private final Map playersHashMap = Maps.newHashMap();
   public Map mapDecorations = Maps.newLinkedHashMap();

   public MapData(String var1) {
      super(var1);
   }

   public void calculateMapCenter(double var1, double var3, int var5) {
      int var6 = 128 * (1 << var5);
      int var7 = MathHelper.floor((var1 + 64.0D) / (double)var6);
      int var8 = MathHelper.floor((var3 + 64.0D) / (double)var6);
      this.xCenter = var7 * var6 + var6 / 2 - 64;
      this.zCenter = var8 * var6 + var6 / 2 - 64;
   }

   public void readFromNBT(NBTTagCompound var1) {
      NBTBase var2 = var1.getTag("dimension");
      if (var2 instanceof NBTTagByte) {
         this.dimension = ((NBTTagByte)var2).getByte();
      } else {
         this.dimension = ((NBTTagInt)var2).getInt();
      }

      this.xCenter = var1.getInteger("xCenter");
      this.zCenter = var1.getInteger("zCenter");
      this.scale = var1.getByte("scale");
      this.scale = (byte)MathHelper.clamp(this.scale, 0, 4);
      if (var1.hasKey("trackingPosition", 1)) {
         this.trackingPosition = var1.getBoolean("trackingPosition");
      } else {
         this.trackingPosition = true;
      }

      short var3 = var1.getShort("width");
      short var4 = var1.getShort("height");
      if (var3 == 128 && var4 == 128) {
         this.colors = var1.getByteArray("colors");
      } else {
         byte[] var5 = var1.getByteArray("colors");
         this.colors = new byte[16384];
         int var6 = (128 - var3) / 2;
         int var7 = (128 - var4) / 2;

         for(int var8 = 0; var8 < var4; ++var8) {
            int var9 = var8 + var7;
            if (var9 >= 0 || var9 < 128) {
               for(int var10 = 0; var10 < var3; ++var10) {
                  int var11 = var10 + var6;
                  if (var11 >= 0 || var11 < 128) {
                     this.colors[var11 + var9 * 128] = var5[var10 + var8 * var3];
                  }
               }
            }
         }
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      var1.setInteger("dimension", this.dimension);
      var1.setInteger("xCenter", this.xCenter);
      var1.setInteger("zCenter", this.zCenter);
      var1.setByte("scale", this.scale);
      var1.setShort("width", (short)128);
      var1.setShort("height", (short)128);
      var1.setByteArray("colors", this.colors);
      var1.setBoolean("trackingPosition", this.trackingPosition);
      return var1;
   }

   public void updateVisiblePlayers(EntityPlayer var1, ItemStack var2) {
      if (!this.playersHashMap.containsKey(var1)) {
         MapData.MapInfo var3 = new MapData.MapInfo(var1);
         this.playersHashMap.put(var1, var3);
         this.playersArrayList.add(var3);
      }

      if (!var1.inventory.hasItemStack(var2)) {
         this.mapDecorations.remove(var1.getName());
      }

      for(int var6 = 0; var6 < this.playersArrayList.size(); ++var6) {
         MapData.MapInfo var4 = (MapData.MapInfo)this.playersArrayList.get(var6);
         if (!var4.entityplayerObj.isDead && (var4.entityplayerObj.inventory.hasItemStack(var2) || var2.isOnItemFrame())) {
            if (!var2.isOnItemFrame() && var4.entityplayerObj.dimension == this.dimension && this.trackingPosition) {
               this.updateDecorations(0, var4.entityplayerObj.world, var4.entityplayerObj.getName(), var4.entityplayerObj.posX, var4.entityplayerObj.posZ, (double)var4.entityplayerObj.rotationYaw);
            }
         } else {
            this.playersHashMap.remove(var4.entityplayerObj);
            this.playersArrayList.remove(var4);
         }
      }

      if (var2.isOnItemFrame() && this.trackingPosition) {
         EntityItemFrame var7 = var2.getItemFrame();
         BlockPos var9 = var7.getHangingPosition();
         this.updateDecorations(1, var1.world, "frame-" + var7.getEntityId(), (double)var9.getX(), (double)var9.getZ(), (double)(var7.facingDirection.getHorizontalIndex() * 90));
      }

      if (var2.hasTagCompound() && var2.getTagCompound().hasKey("Decorations", 9)) {
         NBTTagList var8 = var2.getTagCompound().getTagList("Decorations", 10);

         for(int var10 = 0; var10 < var8.tagCount(); ++var10) {
            NBTTagCompound var5 = var8.getCompoundTagAt(var10);
            if (!this.mapDecorations.containsKey(var5.getString("id"))) {
               this.updateDecorations(var5.getByte("type"), var1.world, var5.getString("id"), var5.getDouble("x"), var5.getDouble("z"), var5.getDouble("rot"));
            }
         }
      }

   }

   private void updateDecorations(int var1, World var2, String var3, double var4, double var6, double var8) {
      int var10 = 1 << this.scale;
      float var11 = (float)(var4 - (double)this.xCenter) / (float)var10;
      float var12 = (float)(var6 - (double)this.zCenter) / (float)var10;
      byte var13 = (byte)((int)((double)(var11 * 2.0F) + 0.5D));
      byte var14 = (byte)((int)((double)(var12 * 2.0F) + 0.5D));
      boolean var15 = true;
      byte var16;
      if (var11 >= -63.0F && var12 >= -63.0F && var11 <= 63.0F && var12 <= 63.0F) {
         var8 = var8 + (var8 < 0.0D ? -8.0D : 8.0D);
         var16 = (byte)((int)(var8 * 16.0D / 360.0D));
         if (var2.provider.shouldMapSpin(var3, var4, var6, var8)) {
            int var17 = (int)(var2.getWorldInfo().getWorldTime() / 10L);
            var16 = (byte)(var17 * var17 * 34187121 + var17 * 121 >> 15 & 15);
         }
      } else {
         if (Math.abs(var11) >= 320.0F || Math.abs(var12) >= 320.0F) {
            this.mapDecorations.remove(var3);
            return;
         }

         var1 = 6;
         var16 = 0;
         if (var11 <= -63.0F) {
            var13 = -128;
         }

         if (var12 <= -63.0F) {
            var14 = -128;
         }

         if (var11 >= 63.0F) {
            var13 = 127;
         }

         if (var12 >= 63.0F) {
            var14 = 127;
         }
      }

      this.mapDecorations.put(var3, new Vec4b((byte)var1, var13, var14, var16));
   }

   @Nullable
   public Packet getMapPacket(ItemStack var1, World var2, EntityPlayer var3) {
      MapData.MapInfo var4 = (MapData.MapInfo)this.playersHashMap.get(var3);
      return var4 == null ? null : var4.getPacket(var1);
   }

   public void updateMapData(int var1, int var2) {
      super.markDirty();

      for(MapData.MapInfo var4 : this.playersArrayList) {
         var4.update(var1, var2);
      }

   }

   public MapData.MapInfo getMapInfo(EntityPlayer var1) {
      MapData.MapInfo var2 = (MapData.MapInfo)this.playersHashMap.get(var1);
      if (var2 == null) {
         var2 = new MapData.MapInfo(var1);
         this.playersHashMap.put(var1, var2);
         this.playersArrayList.add(var2);
      }

      return var2;
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

      public MapInfo(EntityPlayer var2) {
         this.entityplayerObj = var2;
      }

      public Packet getPacket(ItemStack var1) {
         if (this.isDirty) {
            this.isDirty = false;
            return new SPacketMaps(var1.getMetadata(), MapData.this.scale, MapData.this.trackingPosition, MapData.this.mapDecorations.values(), MapData.this.colors, this.minX, this.minY, this.maxX + 1 - this.minX, this.maxY + 1 - this.minY);
         } else {
            return this.tick++ % 5 == 0 ? new SPacketMaps(var1.getMetadata(), MapData.this.scale, MapData.this.trackingPosition, MapData.this.mapDecorations.values(), MapData.this.colors, 0, 0, 0, 0) : null;
         }
      }

      public void update(int var1, int var2) {
         if (this.isDirty) {
            this.minX = Math.min(this.minX, var1);
            this.minY = Math.min(this.minY, var2);
            this.maxX = Math.max(this.maxX, var1);
            this.maxY = Math.max(this.maxY, var2);
         } else {
            this.isDirty = true;
            this.minX = var1;
            this.minY = var2;
            this.maxX = var1;
            this.maxY = var2;
         }

      }
   }
}
