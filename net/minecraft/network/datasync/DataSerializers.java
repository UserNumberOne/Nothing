package net.minecraft.network.datasync;

import com.google.common.base.Optional;
import java.io.IOException;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IntIdentityHashBiMap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Rotations;
import net.minecraft.util.text.ITextComponent;

public class DataSerializers {
   private static final IntIdentityHashBiMap REGISTRY = new IntIdentityHashBiMap(16);
   public static final DataSerializer BYTE = new DataSerializer() {
      public void write(PacketBuffer var1, Byte var2) {
         var1.writeByte(var2.byteValue());
      }

      public Byte read(PacketBuffer var1) {
         return var1.readByte();
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(var1, this);
      }
   };
   public static final DataSerializer VARINT = new DataSerializer() {
      public void write(PacketBuffer var1, Integer var2) {
         var1.writeVarInt(var2.intValue());
      }

      public Integer read(PacketBuffer var1) {
         return var1.readVarInt();
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(var1, this);
      }
   };
   public static final DataSerializer FLOAT = new DataSerializer() {
      public void write(PacketBuffer var1, Float var2) {
         var1.writeFloat(var2.floatValue());
      }

      public Float read(PacketBuffer var1) {
         return var1.readFloat();
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(var1, this);
      }
   };
   public static final DataSerializer STRING = new DataSerializer() {
      public void write(PacketBuffer var1, String var2) {
         var1.writeString(var2);
      }

      public String read(PacketBuffer var1) {
         return var1.readString(32767);
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(var1, this);
      }
   };
   public static final DataSerializer TEXT_COMPONENT = new DataSerializer() {
      public void write(PacketBuffer var1, ITextComponent var2) {
         var1.writeTextComponent(var2);
      }

      public ITextComponent read(PacketBuffer var1) throws IOException {
         return var1.readTextComponent();
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(var1, this);
      }
   };
   public static final DataSerializer OPTIONAL_ITEM_STACK = new DataSerializer() {
      public void write(PacketBuffer var1, Optional var2) {
         var1.writeItemStack((ItemStack)var2.orNull());
      }

      public Optional read(PacketBuffer var1) throws IOException {
         return Optional.fromNullable(var1.readItemStack());
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(var1, this);
      }
   };
   public static final DataSerializer OPTIONAL_BLOCK_STATE = new DataSerializer() {
      public void write(PacketBuffer var1, Optional var2) {
         if (var2.isPresent()) {
            var1.writeVarInt(Block.getStateId((IBlockState)var2.get()));
         } else {
            var1.writeVarInt(0);
         }

      }

      public Optional read(PacketBuffer var1) {
         int var2 = var1.readVarInt();
         return var2 == 0 ? Optional.absent() : Optional.of(Block.getStateById(var2));
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(var1, this);
      }
   };
   public static final DataSerializer BOOLEAN = new DataSerializer() {
      public void write(PacketBuffer var1, Boolean var2) {
         var1.writeBoolean(var2.booleanValue());
      }

      public Boolean read(PacketBuffer var1) {
         return var1.readBoolean();
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(var1, this);
      }
   };
   public static final DataSerializer ROTATIONS = new DataSerializer() {
      public void write(PacketBuffer var1, Rotations var2) {
         var1.writeFloat(var2.getX());
         var1.writeFloat(var2.getY());
         var1.writeFloat(var2.getZ());
      }

      public Rotations read(PacketBuffer var1) {
         return new Rotations(var1.readFloat(), var1.readFloat(), var1.readFloat());
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(var1, this);
      }
   };
   public static final DataSerializer BLOCK_POS = new DataSerializer() {
      public void write(PacketBuffer var1, BlockPos var2) {
         var1.writeBlockPos(var2);
      }

      public BlockPos read(PacketBuffer var1) {
         return var1.readBlockPos();
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(var1, this);
      }
   };
   public static final DataSerializer OPTIONAL_BLOCK_POS = new DataSerializer() {
      public void write(PacketBuffer var1, Optional var2) {
         var1.writeBoolean(var2.isPresent());
         if (var2.isPresent()) {
            var1.writeBlockPos((BlockPos)var2.get());
         }

      }

      public Optional read(PacketBuffer var1) {
         return !var1.readBoolean() ? Optional.absent() : Optional.of(var1.readBlockPos());
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(var1, this);
      }
   };
   public static final DataSerializer FACING = new DataSerializer() {
      public void write(PacketBuffer var1, EnumFacing var2) {
         var1.writeEnumValue(var2);
      }

      public EnumFacing read(PacketBuffer var1) {
         return (EnumFacing)var1.readEnumValue(EnumFacing.class);
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(var1, this);
      }
   };
   public static final DataSerializer OPTIONAL_UNIQUE_ID = new DataSerializer() {
      public void write(PacketBuffer var1, Optional var2) {
         var1.writeBoolean(var2.isPresent());
         if (var2.isPresent()) {
            var1.writeUniqueId((UUID)var2.get());
         }

      }

      public Optional read(PacketBuffer var1) {
         return !var1.readBoolean() ? Optional.absent() : Optional.of(var1.readUniqueId());
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(var1, this);
      }
   };

   public static void registerSerializer(DataSerializer var0) {
      REGISTRY.add(var0);
   }

   @Nullable
   public static DataSerializer getSerializer(int var0) {
      return (DataSerializer)REGISTRY.get(var0);
   }

   public static int getSerializerId(DataSerializer var0) {
      return REGISTRY.getId(var0);
   }

   static {
      registerSerializer(BYTE);
      registerSerializer(VARINT);
      registerSerializer(FLOAT);
      registerSerializer(STRING);
      registerSerializer(TEXT_COMPONENT);
      registerSerializer(OPTIONAL_ITEM_STACK);
      registerSerializer(BOOLEAN);
      registerSerializer(ROTATIONS);
      registerSerializer(BLOCK_POS);
      registerSerializer(OPTIONAL_BLOCK_POS);
      registerSerializer(FACING);
      registerSerializer(OPTIONAL_UNIQUE_ID);
      registerSerializer(OPTIONAL_BLOCK_STATE);
   }
}
