package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

public class SPacketEntityProperties implements Packet {
   private int entityId;
   private final List snapshots = Lists.newArrayList();

   public SPacketEntityProperties() {
   }

   public SPacketEntityProperties(int var1, Collection var2) {
      this.entityId = var1;

      for(IAttributeInstance var4 : var2) {
         this.snapshots.add(new SPacketEntityProperties.Snapshot(var4.getAttribute().getName(), var4.getBaseValue(), var4.getModifiers()));
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = var1.readVarInt();
      int var2 = var1.readInt();

      for(int var3 = 0; var3 < var2; ++var3) {
         String var4 = var1.readString(64);
         double var5 = var1.readDouble();
         ArrayList var7 = Lists.newArrayList();
         int var8 = var1.readVarInt();

         for(int var9 = 0; var9 < var8; ++var9) {
            UUID var10 = var1.readUniqueId();
            var7.add(new AttributeModifier(var10, "Unknown synced attribute modifier", var1.readDouble(), var1.readByte()));
         }

         this.snapshots.add(new SPacketEntityProperties.Snapshot(var4, var5, var7));
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityId);
      var1.writeInt(this.snapshots.size());

      for(SPacketEntityProperties.Snapshot var3 : this.snapshots) {
         var1.writeString(var3.getName());
         var1.writeDouble(var3.getBaseValue());
         var1.writeVarInt(var3.getModifiers().size());

         for(AttributeModifier var5 : var3.getModifiers()) {
            var1.writeUniqueId(var5.getID());
            var1.writeDouble(var5.getAmount());
            var1.writeByte(var5.getOperation());
         }
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleEntityProperties(this);
   }

   public class Snapshot {
      private final String name;
      private final double baseValue;
      private final Collection modifiers;

      public Snapshot(String var2, double var3, Collection var5) {
         this.name = var2;
         this.baseValue = var3;
         this.modifiers = var5;
      }

      public String getName() {
         return this.name;
      }

      public double getBaseValue() {
         return this.baseValue;
      }

      public Collection getModifiers() {
         return this.modifiers;
      }
   }
}
