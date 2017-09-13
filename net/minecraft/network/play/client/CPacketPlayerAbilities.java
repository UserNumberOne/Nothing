package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;

public class CPacketPlayerAbilities implements Packet {
   private boolean invulnerable;
   private boolean flying;
   private boolean allowFlying;
   private boolean creativeMode;
   private float flySpeed;
   private float walkSpeed;

   public CPacketPlayerAbilities() {
   }

   public CPacketPlayerAbilities(PlayerCapabilities var1) {
      this.setInvulnerable(capabilities.disableDamage);
      this.setFlying(capabilities.isFlying);
      this.setAllowFlying(capabilities.allowFlying);
      this.setCreativeMode(capabilities.isCreativeMode);
      this.setFlySpeed(capabilities.getFlySpeed());
      this.setWalkSpeed(capabilities.getWalkSpeed());
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      byte b0 = buf.readByte();
      this.setInvulnerable((b0 & 1) > 0);
      this.setFlying((b0 & 2) > 0);
      this.setAllowFlying((b0 & 4) > 0);
      this.setCreativeMode((b0 & 8) > 0);
      this.setFlySpeed(buf.readFloat());
      this.setWalkSpeed(buf.readFloat());
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      byte b0 = 0;
      if (this.isInvulnerable()) {
         b0 = (byte)(b0 | 1);
      }

      if (this.isFlying()) {
         b0 = (byte)(b0 | 2);
      }

      if (this.isAllowFlying()) {
         b0 = (byte)(b0 | 4);
      }

      if (this.isCreativeMode()) {
         b0 = (byte)(b0 | 8);
      }

      buf.writeByte(b0);
      buf.writeFloat(this.flySpeed);
      buf.writeFloat(this.walkSpeed);
   }

   public void processPacket(INetHandlerPlayServer var1) {
      handler.processPlayerAbilities(this);
   }

   public boolean isInvulnerable() {
      return this.invulnerable;
   }

   public void setInvulnerable(boolean var1) {
      this.invulnerable = isInvulnerable;
   }

   public boolean isFlying() {
      return this.flying;
   }

   public void setFlying(boolean var1) {
      this.flying = isFlying;
   }

   public boolean isAllowFlying() {
      return this.allowFlying;
   }

   public void setAllowFlying(boolean var1) {
      this.allowFlying = isAllowFlying;
   }

   public boolean isCreativeMode() {
      return this.creativeMode;
   }

   public void setCreativeMode(boolean var1) {
      this.creativeMode = isCreativeMode;
   }

   public void setFlySpeed(float var1) {
      this.flySpeed = flySpeedIn;
   }

   public void setWalkSpeed(float var1) {
      this.walkSpeed = walkSpeedIn;
   }
}
