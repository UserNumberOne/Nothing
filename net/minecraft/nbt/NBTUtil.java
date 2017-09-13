package net.minecraft.nbt;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.UnmodifiableIterator;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.util.UUID;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;

public final class NBTUtil {
   @Nullable
   public static GameProfile readGameProfileFromNBT(NBTTagCompound var0) {
      String var1 = null;
      String var2 = null;
      if (var0.hasKey("Name", 8)) {
         var1 = var0.getString("Name");
      }

      if (var0.hasKey("Id", 8)) {
         var2 = var0.getString("Id");
      }

      if (StringUtils.isNullOrEmpty(var1) && StringUtils.isNullOrEmpty(var2)) {
         return null;
      } else {
         UUID var3;
         try {
            var3 = UUID.fromString(var2);
         } catch (Throwable var12) {
            var3 = null;
         }

         GameProfile var4 = new GameProfile(var3, var1);
         if (var0.hasKey("Properties", 10)) {
            NBTTagCompound var5 = var0.getCompoundTag("Properties");

            for(String var7 : var5.getKeySet()) {
               NBTTagList var8 = var5.getTagList(var7, 10);

               for(int var9 = 0; var9 < var8.tagCount(); ++var9) {
                  NBTTagCompound var10 = var8.getCompoundTagAt(var9);
                  String var11 = var10.getString("Value");
                  if (var10.hasKey("Signature", 8)) {
                     var4.getProperties().put(var7, new Property(var7, var11, var10.getString("Signature")));
                  } else {
                     var4.getProperties().put(var7, new Property(var7, var11));
                  }
               }
            }
         }

         return var4;
      }
   }

   public static NBTTagCompound writeGameProfile(NBTTagCompound var0, GameProfile var1) {
      if (!StringUtils.isNullOrEmpty(var1.getName())) {
         var0.setString("Name", var1.getName());
      }

      if (var1.getId() != null) {
         var0.setString("Id", var1.getId().toString());
      }

      if (!var1.getProperties().isEmpty()) {
         NBTTagCompound var2 = new NBTTagCompound();

         for(String var4 : var1.getProperties().keySet()) {
            NBTTagList var5 = new NBTTagList();

            for(Property var7 : var1.getProperties().get(var4)) {
               NBTTagCompound var8 = new NBTTagCompound();
               var8.setString("Value", var7.getValue());
               if (var7.hasSignature()) {
                  var8.setString("Signature", var7.getSignature());
               }

               var5.appendTag(var8);
            }

            var2.setTag(var4, var5);
         }

         var0.setTag("Properties", var2);
      }

      return var0;
   }

   @VisibleForTesting
   public static boolean areNBTEquals(NBTBase var0, NBTBase var1, boolean var2) {
      if (var0 == var1) {
         return true;
      } else if (var0 == null) {
         return true;
      } else if (var1 == null) {
         return false;
      } else if (!var0.getClass().equals(var1.getClass())) {
         return false;
      } else if (var0 instanceof NBTTagCompound) {
         NBTTagCompound var9 = (NBTTagCompound)var0;
         NBTTagCompound var10 = (NBTTagCompound)var1;

         for(String var12 : var9.getKeySet()) {
            NBTBase var13 = var9.getTag(var12);
            if (!areNBTEquals(var13, var10.getTag(var12), var2)) {
               return false;
            }
         }

         return true;
      } else if (var0 instanceof NBTTagList && var2) {
         NBTTagList var3 = (NBTTagList)var0;
         NBTTagList var4 = (NBTTagList)var1;
         if (var3.tagCount() == 0) {
            return var4.tagCount() == 0;
         } else {
            for(int var5 = 0; var5 < var3.tagCount(); ++var5) {
               NBTBase var6 = var3.get(var5);
               boolean var7 = false;

               for(int var8 = 0; var8 < var4.tagCount(); ++var8) {
                  if (areNBTEquals(var6, var4.get(var8), var2)) {
                     var7 = true;
                     break;
                  }
               }

               if (!var7) {
                  return false;
               }
            }

            return true;
         }
      } else {
         return var0.equals(var1);
      }
   }

   public static NBTTagCompound createUUIDTag(UUID var0) {
      NBTTagCompound var1 = new NBTTagCompound();
      var1.setLong("M", var0.getMostSignificantBits());
      var1.setLong("L", var0.getLeastSignificantBits());
      return var1;
   }

   public static UUID getUUIDFromTag(NBTTagCompound var0) {
      return new UUID(var0.getLong("M"), var0.getLong("L"));
   }

   public static BlockPos getPosFromTag(NBTTagCompound var0) {
      return new BlockPos(var0.getInteger("X"), var0.getInteger("Y"), var0.getInteger("Z"));
   }

   public static NBTTagCompound createPosTag(BlockPos var0) {
      NBTTagCompound var1 = new NBTTagCompound();
      var1.setInteger("X", var0.getX());
      var1.setInteger("Y", var0.getY());
      var1.setInteger("Z", var0.getZ());
      return var1;
   }

   public static IBlockState readBlockState(NBTTagCompound var0) {
      if (!var0.hasKey("Name", 8)) {
         return Blocks.AIR.getDefaultState();
      } else {
         Block var1 = (Block)Block.REGISTRY.getObject(new ResourceLocation(var0.getString("Name")));
         IBlockState var2 = var1.getDefaultState();
         if (var0.hasKey("Properties", 10)) {
            NBTTagCompound var3 = var0.getCompoundTag("Properties");
            BlockStateContainer var4 = var1.getBlockState();

            for(String var6 : var3.getKeySet()) {
               IProperty var7 = var4.getProperty(var6);
               if (var7 != null) {
                  var2 = setValueHelper(var2, var7, var3.getString(var6));
               }
            }
         }

         return var2;
      }
   }

   private static IBlockState setValueHelper(IBlockState var0, IProperty var1, String var2) {
      return var0.withProperty(var1, (Comparable)var1.parseValue(var2).get());
   }

   public static NBTTagCompound writeBlockState(NBTTagCompound var0, IBlockState var1) {
      var0.setString("Name", ((ResourceLocation)Block.REGISTRY.getNameForObject(var1.getBlock())).toString());
      if (!var1.getProperties().isEmpty()) {
         NBTTagCompound var2 = new NBTTagCompound();
         UnmodifiableIterator var3 = var1.getProperties().entrySet().iterator();

         while(var3.hasNext()) {
            Entry var4 = (Entry)var3.next();
            IProperty var5 = (IProperty)var4.getKey();
            var2.setString(var5.getName(), getName(var5, (Comparable)var4.getValue()));
         }

         var0.setTag("Properties", var2);
      }

      return var0;
   }

   private static String getName(IProperty var0, Comparable var1) {
      return var0.getName(var1);
   }
}
