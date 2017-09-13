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
         buf.writeByte(value.byteValue());
      }

      public Byte read(PacketBuffer var1) {
         return buf.readByte();
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(id, this);
      }
   };
   public static final DataSerializer VARINT = new DataSerializer() {
      public void write(PacketBuffer var1, Integer var2) {
         buf.writeVarInt(value.intValue());
      }

      public Integer read(PacketBuffer var1) {
         return buf.readVarInt();
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(id, this);
      }
   };
   public static final DataSerializer FLOAT = new DataSerializer() {
      public void write(PacketBuffer var1, Float var2) {
         buf.writeFloat(value.floatValue());
      }

      public Float read(PacketBuffer var1) {
         return buf.readFloat();
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(id, this);
      }
   };
   public static final DataSerializer STRING = new DataSerializer() {
      public void write(PacketBuffer var1, String var2) {
         buf.writeString(value);
      }

      public String read(PacketBuffer var1) {
         return buf.readString(32767);
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(id, this);
      }
   };
   public static final DataSerializer TEXT_COMPONENT = new DataSerializer() {
      public void write(PacketBuffer var1, ITextComponent var2) {
         buf.writeTextComponent(value);
      }

      public ITextComponent read(PacketBuffer var1) throws IOException {
         return buf.readTextComponent();
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(id, this);
      }
   };
   public static final DataSerializer OPTIONAL_ITEM_STACK = new DataSerializer() {
      public void write(PacketBuffer var1, Optional var2) {
         buf.writeItemStack((ItemStack)value.orNull());
      }

      public Optional read(PacketBuffer var1) throws IOException {
         return Optional.fromNullable(buf.readItemStack());
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(id, this);
      }
   };
   public static final DataSerializer OPTIONAL_BLOCK_STATE = new DataSerializer() {
      public void write(PacketBuffer var1, Optional var2) {
         if (value.isPresent()) {
            buf.writeVarInt(Block.getStateId((IBlockState)value.get()));
         } else {
            buf.writeVarInt(0);
         }

      }

      public Optional read(PacketBuffer var1) {
         int i = buf.readVarInt();
         return i == 0 ? Optional.absent() : Optional.of(Block.getStateById(i));
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(id, this);
      }
   };
   public static final DataSerializer BOOLEAN = new DataSerializer() {
      public void write(PacketBuffer var1, Boolean var2) {
         buf.writeBoolean(value.booleanValue());
      }

      public Boolean read(PacketBuffer var1) {
         return buf.readBoolean();
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(id, this);
      }
   };
   public static final DataSerializer ROTATIONS = new DataSerializer() {
      public void write(PacketBuffer var1, Rotations var2) {
         buf.writeFloat(value.getX());
         buf.writeFloat(value.getY());
         buf.writeFloat(value.getZ());
      }

      public Rotations read(PacketBuffer var1) {
         return new Rotations(buf.readFloat(), buf.readFloat(), buf.readFloat());
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(id, this);
      }
   };
   public static final DataSerializer BLOCK_POS = new DataSerializer() {
      public void write(PacketBuffer var1, BlockPos var2) {
         buf.writeBlockPos(value);
      }

      public BlockPos read(PacketBuffer var1) {
         return buf.readBlockPos();
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(id, this);
      }
   };
   public static final DataSerializer OPTIONAL_BLOCK_POS = new DataSerializer() {
      public void write(PacketBuffer var1, Optional var2) {
         buf.writeBoolean(value.isPresent());
         if (value.isPresent()) {
            buf.writeBlockPos((BlockPos)value.get());
         }

      }

      public Optional read(PacketBuffer var1) {
         return !buf.readBoolean() ? Optional.absent() : Optional.of(buf.readBlockPos());
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(id, this);
      }
   };
   public static final DataSerializer FACING = new DataSerializer() {
      public void write(PacketBuffer var1, EnumFacing var2) {
         buf.writeEnumValue(value);
      }

      public EnumFacing read(PacketBuffer var1) {
         return (EnumFacing)buf.readEnumValue(EnumFacing.class);
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(id, this);
      }
   };
   public static final DataSerializer OPTIONAL_UNIQUE_ID = new DataSerializer() {
      public void write(PacketBuffer var1, Optional var2) {
         buf.writeBoolean(value.isPresent());
         if (value.isPresent()) {
            buf.writeUniqueId((UUID)value.get());
         }

      }

      public Optional read(PacketBuffer var1) {
         return !buf.readBoolean() ? Optional.absent() : Optional.of(buf.readUniqueId());
      }

      public DataParameter createKey(int var1) {
         return new DataParameter(id, this);
      }
   };

   public static void registerSerializer(DataSerializer var0) {
      REGISTRY.add(serializer);
   }

   @Nullable
   public static DataSerializer getSerializer(int var0) {
      return (DataSerializer)REGISTRY.get(id);
   }

   public static int getSerializerId(DataSerializer var0) {
      return REGISTRY.getId(serializer);
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
