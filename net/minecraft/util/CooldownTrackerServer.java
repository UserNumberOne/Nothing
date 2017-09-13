package net.minecraft.util;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.network.play.server.SPacketCooldown;

public class CooldownTrackerServer extends CooldownTracker {
   private final EntityPlayerMP player;

   public CooldownTrackerServer(EntityPlayerMP var1) {
      this.player = var1;
   }

   protected void notifyOnSet(Item var1, int var2) {
      super.notifyOnSet(var1, var2);
      this.player.connection.sendPacket(new SPacketCooldown(var1, var2));
   }

   protected void notifyOnRemove(Item var1) {
      super.notifyOnRemove(var1);
      this.player.connection.sendPacket(new SPacketCooldown(var1, 0));
   }
}
