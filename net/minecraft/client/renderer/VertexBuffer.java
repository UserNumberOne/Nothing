package net.minecraft.client.renderer;

import com.google.common.primitives.Floats;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Comparator;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class VertexBuffer {
   private static final Logger LOGGER = LogManager.getLogger();
   private ByteBuffer byteBuffer;
   private IntBuffer rawIntBuffer;
   private ShortBuffer rawShortBuffer;
   private FloatBuffer rawFloatBuffer;
   private int vertexCount;
   private VertexFormatElement vertexFormatElement;
   private int vertexFormatIndex;
   private boolean noColor;
   private int drawMode;
   private double xOffset;
   private double yOffset;
   private double zOffset;
   private VertexFormat vertexFormat;
   private boolean isDrawing;

   public VertexBuffer(int var1) {
      this.byteBuffer = GLAllocation.createDirectByteBuffer(var1 * 4);
      this.rawIntBuffer = this.byteBuffer.asIntBuffer();
      this.rawShortBuffer = this.byteBuffer.asShortBuffer();
      this.rawFloatBuffer = this.byteBuffer.asFloatBuffer();
   }

   private void growBuffer(int var1) {
      if (MathHelper.roundUp(var1, 4) / 4 > this.rawIntBuffer.remaining() || this.vertexCount * this.vertexFormat.getNextOffset() + var1 > this.byteBuffer.capacity()) {
         int var2 = this.byteBuffer.capacity();
         int var3 = var2 + MathHelper.roundUp(var1, 2097152);
         LOGGER.debug("Needed to grow BufferBuilder buffer: Old size {} bytes, new size {} bytes.", new Object[]{var2, var3});
         int var4 = this.rawIntBuffer.position();
         ByteBuffer var5 = GLAllocation.createDirectByteBuffer(var3);
         this.byteBuffer.position(0);
         var5.put(this.byteBuffer);
         var5.rewind();
         this.byteBuffer = var5;
         this.rawFloatBuffer = this.byteBuffer.asFloatBuffer().asReadOnlyBuffer();
         this.rawIntBuffer = this.byteBuffer.asIntBuffer();
         this.rawIntBuffer.position(var4);
         this.rawShortBuffer = this.byteBuffer.asShortBuffer();
         this.rawShortBuffer.position(var4 << 1);
      }

   }

   public void sortVertexData(float var1, float var2, float var3) {
      int var4 = this.vertexCount / 4;
      final float[] var5 = new float[var4];

      for(int var6 = 0; var6 < var4; ++var6) {
         var5[var6] = getDistanceSq(this.rawFloatBuffer, (float)((double)var1 + this.xOffset), (float)((double)var2 + this.yOffset), (float)((double)var3 + this.zOffset), this.vertexFormat.getIntegerSize(), var6 * this.vertexFormat.getNextOffset());
      }

      Integer[] var15 = new Integer[var4];

      for(int var7 = 0; var7 < var15.length; ++var7) {
         var15[var7] = var7;
      }

      Arrays.sort(var15, new Comparator() {
         public int compare(Integer var1, Integer var2) {
            return Floats.compare(var5[var2.intValue()], var5[var1.intValue()]);
         }
      });
      BitSet var16 = new BitSet();
      int var8 = this.vertexFormat.getNextOffset();
      int[] var9 = new int[var8];

      for(int var10 = var16.nextClearBit(0); var10 < var15.length; var10 = var16.nextClearBit(var10 + 1)) {
         int var11 = var15[var10].intValue();
         if (var11 != var10) {
            this.rawIntBuffer.limit(var11 * var8 + var8);
            this.rawIntBuffer.position(var11 * var8);
            this.rawIntBuffer.get(var9);
            int var12 = var11;

            for(int var13 = var15[var11].intValue(); var12 != var10; var13 = var15[var13].intValue()) {
               this.rawIntBuffer.limit(var13 * var8 + var8);
               this.rawIntBuffer.position(var13 * var8);
               IntBuffer var14 = this.rawIntBuffer.slice();
               this.rawIntBuffer.limit(var12 * var8 + var8);
               this.rawIntBuffer.position(var12 * var8);
               this.rawIntBuffer.put(var14);
               var16.set(var12);
               var12 = var13;
            }

            this.rawIntBuffer.limit(var10 * var8 + var8);
            this.rawIntBuffer.position(var10 * var8);
            this.rawIntBuffer.put(var9);
         }

         var16.set(var10);
      }

      this.rawIntBuffer.limit(this.rawIntBuffer.capacity());
      this.rawIntBuffer.position(this.getBufferSize());
   }

   public VertexBuffer.State getVertexState() {
      this.rawIntBuffer.rewind();
      int var1 = this.getBufferSize();
      this.rawIntBuffer.limit(var1);
      int[] var2 = new int[var1];
      this.rawIntBuffer.get(var2);
      this.rawIntBuffer.limit(this.rawIntBuffer.capacity());
      this.rawIntBuffer.position(var1);
      return new VertexBuffer.State(var2, new VertexFormat(this.vertexFormat));
   }

   private int getBufferSize() {
      return this.vertexCount * this.vertexFormat.getIntegerSize();
   }

   private static float getDistanceSq(FloatBuffer var0, float var1, float var2, float var3, int var4, int var5) {
      float var6 = var0.get(var5 + var4 * 0 + 0);
      float var7 = var0.get(var5 + var4 * 0 + 1);
      float var8 = var0.get(var5 + var4 * 0 + 2);
      float var9 = var0.get(var5 + var4 * 1 + 0);
      float var10 = var0.get(var5 + var4 * 1 + 1);
      float var11 = var0.get(var5 + var4 * 1 + 2);
      float var12 = var0.get(var5 + var4 * 2 + 0);
      float var13 = var0.get(var5 + var4 * 2 + 1);
      float var14 = var0.get(var5 + var4 * 2 + 2);
      float var15 = var0.get(var5 + var4 * 3 + 0);
      float var16 = var0.get(var5 + var4 * 3 + 1);
      float var17 = var0.get(var5 + var4 * 3 + 2);
      float var18 = (var6 + var9 + var12 + var15) * 0.25F - var1;
      float var19 = (var7 + var10 + var13 + var16) * 0.25F - var2;
      float var20 = (var8 + var11 + var14 + var17) * 0.25F - var3;
      return var18 * var18 + var19 * var19 + var20 * var20;
   }

   public void setVertexState(VertexBuffer.State var1) {
      this.rawIntBuffer.clear();
      this.growBuffer(var1.getRawBuffer().length * 4);
      this.rawIntBuffer.put(var1.getRawBuffer());
      this.vertexCount = var1.getVertexCount();
      this.vertexFormat = new VertexFormat(var1.getVertexFormat());
   }

   public void reset() {
      this.vertexCount = 0;
      this.vertexFormatElement = null;
      this.vertexFormatIndex = 0;
   }

   public void begin(int var1, VertexFormat var2) {
      if (this.isDrawing) {
         throw new IllegalStateException("Already building!");
      } else {
         this.isDrawing = true;
         this.reset();
         this.drawMode = var1;
         this.vertexFormat = var2;
         this.vertexFormatElement = var2.getElement(this.vertexFormatIndex);
         this.noColor = false;
         this.byteBuffer.limit(this.byteBuffer.capacity());
      }
   }

   public VertexBuffer tex(double var1, double var3) {
      int var5 = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.vertexFormatIndex);
      switch(this.vertexFormatElement.getType()) {
      case FLOAT:
         this.byteBuffer.putFloat(var5, (float)var1);
         this.byteBuffer.putFloat(var5 + 4, (float)var3);
         break;
      case UINT:
      case INT:
         this.byteBuffer.putInt(var5, (int)var1);
         this.byteBuffer.putInt(var5 + 4, (int)var3);
         break;
      case USHORT:
      case SHORT:
         this.byteBuffer.putShort(var5, (short)((int)var3));
         this.byteBuffer.putShort(var5 + 2, (short)((int)var1));
         break;
      case UBYTE:
      case BYTE:
         this.byteBuffer.put(var5, (byte)((int)var3));
         this.byteBuffer.put(var5 + 1, (byte)((int)var1));
      }

      this.nextVertexFormatIndex();
      return this;
   }

   public VertexBuffer lightmap(int var1, int var2) {
      int var3 = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.vertexFormatIndex);
      switch(this.vertexFormatElement.getType()) {
      case FLOAT:
         this.byteBuffer.putFloat(var3, (float)var1);
         this.byteBuffer.putFloat(var3 + 4, (float)var2);
         break;
      case UINT:
      case INT:
         this.byteBuffer.putInt(var3, var1);
         this.byteBuffer.putInt(var3 + 4, var2);
         break;
      case USHORT:
      case SHORT:
         this.byteBuffer.putShort(var3, (short)var2);
         this.byteBuffer.putShort(var3 + 2, (short)var1);
         break;
      case UBYTE:
      case BYTE:
         this.byteBuffer.put(var3, (byte)var2);
         this.byteBuffer.put(var3 + 1, (byte)var1);
      }

      this.nextVertexFormatIndex();
      return this;
   }

   public void putBrightness4(int var1, int var2, int var3, int var4) {
      int var5 = (this.vertexCount - 4) * this.vertexFormat.getIntegerSize() + this.vertexFormat.getUvOffsetById(1) / 4;
      int var6 = this.vertexFormat.getNextOffset() >> 2;
      this.rawIntBuffer.put(var5, var1);
      this.rawIntBuffer.put(var5 + var6, var2);
      this.rawIntBuffer.put(var5 + var6 * 2, var3);
      this.rawIntBuffer.put(var5 + var6 * 3, var4);
   }

   public void putPosition(double var1, double var3, double var5) {
      int var7 = this.vertexFormat.getIntegerSize();
      int var8 = (this.vertexCount - 4) * var7;

      for(int var9 = 0; var9 < 4; ++var9) {
         int var10 = var8 + var9 * var7;
         int var11 = var10 + 1;
         int var12 = var11 + 1;
         this.rawIntBuffer.put(var10, Float.floatToRawIntBits((float)(var1 + this.xOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(var10))));
         this.rawIntBuffer.put(var11, Float.floatToRawIntBits((float)(var3 + this.yOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(var11))));
         this.rawIntBuffer.put(var12, Float.floatToRawIntBits((float)(var5 + this.zOffset) + Float.intBitsToFloat(this.rawIntBuffer.get(var12))));
      }

   }

   public int getColorIndex(int var1) {
      return ((this.vertexCount - var1) * this.vertexFormat.getNextOffset() + this.vertexFormat.getColorOffset()) / 4;
   }

   public void putColorMultiplier(float var1, float var2, float var3, int var4) {
      int var5 = this.getColorIndex(var4);
      int var6 = -1;
      if (!this.noColor) {
         var6 = this.rawIntBuffer.get(var5);
         if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
            int var7 = (int)((float)(var6 & 255) * var1);
            int var8 = (int)((float)(var6 >> 8 & 255) * var2);
            int var9 = (int)((float)(var6 >> 16 & 255) * var3);
            var6 = var6 & -16777216;
            var6 = var6 | var9 << 16 | var8 << 8 | var7;
         } else {
            int var13 = (int)((float)(var6 >> 24 & 255) * var1);
            int var14 = (int)((float)(var6 >> 16 & 255) * var2);
            int var15 = (int)((float)(var6 >> 8 & 255) * var3);
            var6 = var6 & 255;
            var6 = var6 | var13 << 24 | var14 << 16 | var15 << 8;
         }
      }

      this.rawIntBuffer.put(var5, var6);
   }

   private void putColor(int var1, int var2) {
      int var3 = this.getColorIndex(var2);
      int var4 = var1 >> 16 & 255;
      int var5 = var1 >> 8 & 255;
      int var6 = var1 & 255;
      int var7 = var1 >> 24 & 255;
      this.putColorRGBA(var3, var4, var5, var6, var7);
   }

   public void putColorRGB_F(float var1, float var2, float var3, int var4) {
      int var5 = this.getColorIndex(var4);
      int var6 = MathHelper.clamp((int)(var1 * 255.0F), 0, 255);
      int var7 = MathHelper.clamp((int)(var2 * 255.0F), 0, 255);
      int var8 = MathHelper.clamp((int)(var3 * 255.0F), 0, 255);
      this.putColorRGBA(var5, var6, var7, var8, 255);
   }

   public void putColorRGBA(int var1, int var2, int var3, int var4, int var5) {
      if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
         this.rawIntBuffer.put(var1, var5 << 24 | var4 << 16 | var3 << 8 | var2);
      } else {
         this.rawIntBuffer.put(var1, var2 << 24 | var3 << 16 | var4 << 8 | var5);
      }

   }

   public void noColor() {
      this.noColor = true;
   }

   public VertexBuffer color(float var1, float var2, float var3, float var4) {
      return this.color((int)(var1 * 255.0F), (int)(var2 * 255.0F), (int)(var3 * 255.0F), (int)(var4 * 255.0F));
   }

   public VertexBuffer color(int var1, int var2, int var3, int var4) {
      if (this.noColor) {
         return this;
      } else {
         int var5 = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.vertexFormatIndex);
         switch(this.vertexFormatElement.getType()) {
         case FLOAT:
            this.byteBuffer.putFloat(var5, (float)var1 / 255.0F);
            this.byteBuffer.putFloat(var5 + 4, (float)var2 / 255.0F);
            this.byteBuffer.putFloat(var5 + 8, (float)var3 / 255.0F);
            this.byteBuffer.putFloat(var5 + 12, (float)var4 / 255.0F);
            break;
         case UINT:
         case INT:
            this.byteBuffer.putFloat(var5, (float)var1);
            this.byteBuffer.putFloat(var5 + 4, (float)var2);
            this.byteBuffer.putFloat(var5 + 8, (float)var3);
            this.byteBuffer.putFloat(var5 + 12, (float)var4);
            break;
         case USHORT:
         case SHORT:
            this.byteBuffer.putShort(var5, (short)var1);
            this.byteBuffer.putShort(var5 + 2, (short)var2);
            this.byteBuffer.putShort(var5 + 4, (short)var3);
            this.byteBuffer.putShort(var5 + 6, (short)var4);
            break;
         case UBYTE:
         case BYTE:
            if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
               this.byteBuffer.put(var5, (byte)var1);
               this.byteBuffer.put(var5 + 1, (byte)var2);
               this.byteBuffer.put(var5 + 2, (byte)var3);
               this.byteBuffer.put(var5 + 3, (byte)var4);
            } else {
               this.byteBuffer.put(var5, (byte)var4);
               this.byteBuffer.put(var5 + 1, (byte)var3);
               this.byteBuffer.put(var5 + 2, (byte)var2);
               this.byteBuffer.put(var5 + 3, (byte)var1);
            }
         }

         this.nextVertexFormatIndex();
         return this;
      }
   }

   public void addVertexData(int[] var1) {
      this.growBuffer(var1.length * 4);
      this.rawIntBuffer.position(this.getBufferSize());
      this.rawIntBuffer.put(var1);
      this.vertexCount += var1.length / this.vertexFormat.getIntegerSize();
   }

   public void endVertex() {
      ++this.vertexCount;
      this.growBuffer(this.vertexFormat.getNextOffset());
   }

   public VertexBuffer pos(double var1, double var3, double var5) {
      int var7 = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.vertexFormatIndex);
      switch(this.vertexFormatElement.getType()) {
      case FLOAT:
         this.byteBuffer.putFloat(var7, (float)(var1 + this.xOffset));
         this.byteBuffer.putFloat(var7 + 4, (float)(var3 + this.yOffset));
         this.byteBuffer.putFloat(var7 + 8, (float)(var5 + this.zOffset));
         break;
      case UINT:
      case INT:
         this.byteBuffer.putInt(var7, Float.floatToRawIntBits((float)(var1 + this.xOffset)));
         this.byteBuffer.putInt(var7 + 4, Float.floatToRawIntBits((float)(var3 + this.yOffset)));
         this.byteBuffer.putInt(var7 + 8, Float.floatToRawIntBits((float)(var5 + this.zOffset)));
         break;
      case USHORT:
      case SHORT:
         this.byteBuffer.putShort(var7, (short)((int)(var1 + this.xOffset)));
         this.byteBuffer.putShort(var7 + 2, (short)((int)(var3 + this.yOffset)));
         this.byteBuffer.putShort(var7 + 4, (short)((int)(var5 + this.zOffset)));
         break;
      case UBYTE:
      case BYTE:
         this.byteBuffer.put(var7, (byte)((int)(var1 + this.xOffset)));
         this.byteBuffer.put(var7 + 1, (byte)((int)(var3 + this.yOffset)));
         this.byteBuffer.put(var7 + 2, (byte)((int)(var5 + this.zOffset)));
      }

      this.nextVertexFormatIndex();
      return this;
   }

   public void putNormal(float var1, float var2, float var3) {
      int var4 = (byte)((int)(var1 * 127.0F)) & 255;
      int var5 = (byte)((int)(var2 * 127.0F)) & 255;
      int var6 = (byte)((int)(var3 * 127.0F)) & 255;
      int var7 = var4 | var5 << 8 | var6 << 16;
      int var8 = this.vertexFormat.getNextOffset() >> 2;
      int var9 = (this.vertexCount - 4) * var8 + this.vertexFormat.getNormalOffset() / 4;
      this.rawIntBuffer.put(var9, var7);
      this.rawIntBuffer.put(var9 + var8, var7);
      this.rawIntBuffer.put(var9 + var8 * 2, var7);
      this.rawIntBuffer.put(var9 + var8 * 3, var7);
   }

   private void nextVertexFormatIndex() {
      ++this.vertexFormatIndex;
      this.vertexFormatIndex %= this.vertexFormat.getElementCount();
      this.vertexFormatElement = this.vertexFormat.getElement(this.vertexFormatIndex);
      if (this.vertexFormatElement.getUsage() == VertexFormatElement.EnumUsage.PADDING) {
         this.nextVertexFormatIndex();
      }

   }

   public VertexBuffer normal(float var1, float var2, float var3) {
      int var4 = this.vertexCount * this.vertexFormat.getNextOffset() + this.vertexFormat.getOffset(this.vertexFormatIndex);
      switch(this.vertexFormatElement.getType()) {
      case FLOAT:
         this.byteBuffer.putFloat(var4, var1);
         this.byteBuffer.putFloat(var4 + 4, var2);
         this.byteBuffer.putFloat(var4 + 8, var3);
         break;
      case UINT:
      case INT:
         this.byteBuffer.putInt(var4, (int)var1);
         this.byteBuffer.putInt(var4 + 4, (int)var2);
         this.byteBuffer.putInt(var4 + 8, (int)var3);
         break;
      case USHORT:
      case SHORT:
         this.byteBuffer.putShort(var4, (short)((int)(var1 * 32767.0F) & '\uffff'));
         this.byteBuffer.putShort(var4 + 2, (short)((int)(var2 * 32767.0F) & '\uffff'));
         this.byteBuffer.putShort(var4 + 4, (short)((int)(var3 * 32767.0F) & '\uffff'));
         break;
      case UBYTE:
      case BYTE:
         this.byteBuffer.put(var4, (byte)((int)(var1 * 127.0F) & 255));
         this.byteBuffer.put(var4 + 1, (byte)((int)(var2 * 127.0F) & 255));
         this.byteBuffer.put(var4 + 2, (byte)((int)(var3 * 127.0F) & 255));
      }

      this.nextVertexFormatIndex();
      return this;
   }

   public void setTranslation(double var1, double var3, double var5) {
      this.xOffset = var1;
      this.yOffset = var3;
      this.zOffset = var5;
   }

   public void finishDrawing() {
      if (!this.isDrawing) {
         throw new IllegalStateException("Not building!");
      } else {
         this.isDrawing = false;
         this.byteBuffer.position(0);
         this.byteBuffer.limit(this.getBufferSize() * 4);
      }
   }

   public ByteBuffer getByteBuffer() {
      return this.byteBuffer;
   }

   public VertexFormat getVertexFormat() {
      return this.vertexFormat;
   }

   public int getVertexCount() {
      return this.vertexCount;
   }

   public int getDrawMode() {
      return this.drawMode;
   }

   public void putColor4(int var1) {
      for(int var2 = 0; var2 < 4; ++var2) {
         this.putColor(var1, var2 + 1);
      }

   }

   public void putColorRGB_F4(float var1, float var2, float var3) {
      for(int var4 = 0; var4 < 4; ++var4) {
         this.putColorRGB_F(var1, var2, var3, var4 + 1);
      }

   }

   public boolean isColorDisabled() {
      return this.noColor;
   }

   @SideOnly(Side.CLIENT)
   public class State {
      private final int[] stateRawBuffer;
      private final VertexFormat stateVertexFormat;

      public State(int[] var2, VertexFormat var3) {
         this.stateRawBuffer = var2;
         this.stateVertexFormat = var3;
      }

      public int[] getRawBuffer() {
         return this.stateRawBuffer;
      }

      public int getVertexCount() {
         return this.stateRawBuffer.length / this.stateVertexFormat.getIntegerSize();
      }

      public VertexFormat getVertexFormat() {
         return this.stateVertexFormat;
      }
   }
}
