package net.minecraft.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.network.play.server.SPacketCooldown;

public class CooldownTrackerServer extends CooldownTracker {
   private final EntityPlayerMP player;

   public CooldownTrackerServer(EntityPlayerMP var1) {
      this.player = playerIn;
   }

   protected void notifyOnSet(Item var1, int var2) {
      super.notifyOnSet(itemIn, ticksIn);
      this.player.connection.sendPacket(new SPacketCooldown(itemIn, ticksIn));
   }

   protected void notifyOnRemove(Item var1) {
      super.notifyOnRemove(itemIn);
      this.player.connection.sendPacket(new SPacketCooldown(itemIn, 0));
   }
}
