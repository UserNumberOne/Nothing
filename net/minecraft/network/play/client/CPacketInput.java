package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CPacketInput implements Packet {
   private float strafeSpeed;
   private float forwardSpeed;
   private boolean jumping;
   private boolean sneaking;

   public CPacketInput() {
   }

   @SideOnly(Side.CLIENT)
   public CPacketInput(float var1, float var2, boolean var3, boolean var4) {
      this.strafeSpeed = strafeSpeedIn;
      this.forwardSpeed = forwardSpeedIn;
      this.jumping = jumpingIn;
      this.sneaking = sneakingIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.strafeSpeed = buf.readFloat();
      this.forwardSpeed = buf.readFloat();
      byte b0 = buf.readByte();
      this.jumping = (b0 & 1) > 0;
      this.sneaking = (b0 & 2) > 0;
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeFloat(this.strafeSpeed);
      buf.writeFloat(this.forwardSpeed);
      byte b0 = 0;
      if (this.jumping) {
         b0 = (byte)(b0 | 1);
      }

      if (this.sneaking) {
         b0 = (byte)(b0 | 2);
      }

      buf.writeByte(b0);
   }

   public void processPacket(INetHandlerPlayServer var1) {
      handler.processInput(this);
   }

   public float getStrafeSpeed() {
      return this.strafeSpeed;
   }

   public float getForwardSpeed() {
      return this.forwardSpeed;
   }

   public boolean isJumping() {
      return this.jumping;
   }

   public boolean isSneaking() {
      return this.sneaking;
   }
}
