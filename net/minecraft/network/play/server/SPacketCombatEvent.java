package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.CombatTracker;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class SPacketCombatEvent implements Packet {
   public SPacketCombatEvent.Event eventType;
   public int playerId;
   public int entityId;
   public int duration;
   public ITextComponent deathMessage;

   public SPacketCombatEvent() {
   }

   public SPacketCombatEvent(CombatTracker var1, SPacketCombatEvent.Event var2) {
      this(var1, var2, true);
   }

   public SPacketCombatEvent(CombatTracker var1, SPacketCombatEvent.Event var2, boolean var3) {
      this.eventType = var2;
      EntityLivingBase var4 = var1.getBestAttacker();
      switch(var2) {
      case END_COMBAT:
         this.duration = var1.getCombatDuration();
         this.entityId = var4 == null ? -1 : var4.getEntityId();
         break;
      case ENTITY_DIED:
         this.playerId = var1.getFighter().getEntityId();
         this.entityId = var4 == null ? -1 : var4.getEntityId();
         if (var3) {
            this.deathMessage = var1.getDeathMessage();
         } else {
            this.deathMessage = new TextComponentString("");
         }
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.eventType = (SPacketCombatEvent.Event)var1.readEnumValue(SPacketCombatEvent.Event.class);
      if (this.eventType == SPacketCombatEvent.Event.END_COMBAT) {
         this.duration = var1.readVarInt();
         this.entityId = var1.readInt();
      } else if (this.eventType == SPacketCombatEvent.Event.ENTITY_DIED) {
         this.playerId = var1.readVarInt();
         this.entityId = var1.readInt();
         this.deathMessage = var1.readTextComponent();
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeEnumValue(this.eventType);
      if (this.eventType == SPacketCombatEvent.Event.END_COMBAT) {
         var1.writeVarInt(this.duration);
         var1.writeInt(this.entityId);
      } else if (this.eventType == SPacketCombatEvent.Event.ENTITY_DIED) {
         var1.writeVarInt(this.playerId);
         var1.writeInt(this.entityId);
         var1.writeTextComponent(this.deathMessage);
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleCombatEvent(this);
   }

   public static enum Event {
      ENTER_COMBAT,
      END_COMBAT,
      ENTITY_DIED;
   }
}
