package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.EnumParticleTypes;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketParticles implements Packet {
   private EnumParticleTypes particleType;
   private float xCoord;
   private float yCoord;
   private float zCoord;
   private float xOffset;
   private float yOffset;
   private float zOffset;
   private float particleSpeed;
   private int particleCount;
   private boolean longDistance;
   private int[] particleArguments;

   public SPacketParticles() {
   }

   public SPacketParticles(EnumParticleTypes var1, boolean var2, float var3, float var4, float var5, float var6, float var7, float var8, float var9, int var10, int... var11) {
      this.particleType = particleIn;
      this.longDistance = longDistanceIn;
      this.xCoord = xIn;
      this.yCoord = yIn;
      this.zCoord = zIn;
      this.xOffset = xOffsetIn;
      this.yOffset = yOffsetIn;
      this.zOffset = zOffsetIn;
      this.particleSpeed = speedIn;
      this.particleCount = countIn;
      this.particleArguments = argumentsIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.particleType = EnumParticleTypes.getParticleFromId(buf.readInt());
      if (this.particleType == null) {
         this.particleType = EnumParticleTypes.BARRIER;
      }

      this.longDistance = buf.readBoolean();
      this.xCoord = buf.readFloat();
      this.yCoord = buf.readFloat();
      this.zCoord = buf.readFloat();
      this.xOffset = buf.readFloat();
      this.yOffset = buf.readFloat();
      this.zOffset = buf.readFloat();
      this.particleSpeed = buf.readFloat();
      this.particleCount = buf.readInt();
      int i = this.particleType.getArgumentCount();
      this.particleArguments = new int[i];

      for(int j = 0; j < i; ++j) {
         this.particleArguments[j] = buf.readVarInt();
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeInt(this.particleType.getParticleID());
      buf.writeBoolean(this.longDistance);
      buf.writeFloat(this.xCoord);
      buf.writeFloat(this.yCoord);
      buf.writeFloat(this.zCoord);
      buf.writeFloat(this.xOffset);
      buf.writeFloat(this.yOffset);
      buf.writeFloat(this.zOffset);
      buf.writeFloat(this.particleSpeed);
      buf.writeInt(this.particleCount);
      int i = this.particleType.getArgumentCount();

      for(int j = 0; j < i; ++j) {
         buf.writeVarInt(this.particleArguments[j]);
      }

   }

   @SideOnly(Side.CLIENT)
   public EnumParticleTypes getParticleType() {
      return this.particleType;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleParticles(this);
   }

   @SideOnly(Side.CLIENT)
   public boolean isLongDistance() {
      return this.longDistance;
   }

   @SideOnly(Side.CLIENT)
   public double getXCoordinate() {
      return (double)this.xCoord;
   }

   @SideOnly(Side.CLIENT)
   public double getYCoordinate() {
      return (double)this.yCoord;
   }

   @SideOnly(Side.CLIENT)
   public double getZCoordinate() {
      return (double)this.zCoord;
   }

   @SideOnly(Side.CLIENT)
   public float getXOffset() {
      return this.xOffset;
   }

   @SideOnly(Side.CLIENT)
   public float getYOffset() {
      return this.yOffset;
   }

   @SideOnly(Side.CLIENT)
   public float getZOffset() {
      return this.zOffset;
   }

   @SideOnly(Side.CLIENT)
   public float getParticleSpeed() {
      return this.particleSpeed;
   }

   @SideOnly(Side.CLIENT)
   public int getParticleCount() {
      return this.particleCount;
   }

   @SideOnly(Side.CLIENT)
   public int[] getParticleArgs() {
      return this.particleArguments;
   }
}
