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
      this(tracker, eventIn, true);
   }

   public SPacketCombatEvent(CombatTracker var1, SPacketCombatEvent.Event var2, boolean var3) {
      this.eventType = eventIn;
      EntityLivingBase entitylivingbase = tracker.getBestAttacker();
      switch(eventIn) {
      case END_COMBAT:
         this.duration = tracker.getCombatDuration();
         this.entityId = entitylivingbase == null ? -1 : entitylivingbase.getEntityId();
         break;
      case ENTITY_DIED:
         this.playerId = tracker.getFighter().getEntityId();
         this.entityId = entitylivingbase == null ? -1 : entitylivingbase.getEntityId();
         if (p_i46932_3_) {
            this.deathMessage = tracker.getDeathMessage();
         } else {
            this.deathMessage = new TextComponentString("");
         }
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.eventType = (SPacketCombatEvent.Event)buf.readEnumValue(SPacketCombatEvent.Event.class);
      if (this.eventType == SPacketCombatEvent.Event.END_COMBAT) {
         this.duration = buf.readVarInt();
         this.entityId = buf.readInt();
      } else if (this.eventType == SPacketCombatEvent.Event.ENTITY_DIED) {
         this.playerId = buf.readVarInt();
         this.entityId = buf.readInt();
         this.deathMessage = buf.readTextComponent();
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeEnumValue(this.eventType);
      if (this.eventType == SPacketCombatEvent.Event.END_COMBAT) {
         buf.writeVarInt(this.duration);
         buf.writeInt(this.entityId);
      } else if (this.eventType == SPacketCombatEvent.Event.ENTITY_DIED) {
         buf.writeVarInt(this.playerId);
         buf.writeInt(this.entityId);
         buf.writeTextComponent(this.deathMessage);
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleCombatEvent(this);
   }

   public static enum Event {
      ENTER_COMBAT,
      END_COMBAT,
      ENTITY_DIED;
   }
}
