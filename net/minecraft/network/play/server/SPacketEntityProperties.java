package net.minecraft.network.play.server;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketEntityProperties implements Packet {
   private int entityId;
   private final List snapshots = Lists.newArrayList();

   public SPacketEntityProperties() {
   }

   public SPacketEntityProperties(int var1, Collection var2) {
      this.entityId = entityIdIn;

      for(IAttributeInstance iattributeinstance : instances) {
         this.snapshots.add(new SPacketEntityProperties.Snapshot(iattributeinstance.getAttribute().getName(), iattributeinstance.getBaseValue(), iattributeinstance.getModifiers()));
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = buf.readVarInt();
      int i = buf.readInt();

      for(int j = 0; j < i; ++j) {
         String s = buf.readString(64);
         double d0 = buf.readDouble();
         List list = Lists.newArrayList();
         int k = buf.readVarInt();

         for(int l = 0; l < k; ++l) {
            UUID uuid = buf.readUniqueId();
            list.add(new AttributeModifier(uuid, "Unknown synced attribute modifier", buf.readDouble(), buf.readByte()));
         }

         this.snapshots.add(new SPacketEntityProperties.Snapshot(s, d0, list));
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(this.entityId);
      buf.writeInt(this.snapshots.size());

      for(SPacketEntityProperties.Snapshot spacketentityproperties$snapshot : this.snapshots) {
         buf.writeString(spacketentityproperties$snapshot.getName());
         buf.writeDouble(spacketentityproperties$snapshot.getBaseValue());
         buf.writeVarInt(spacketentityproperties$snapshot.getModifiers().size());

         for(AttributeModifier attributemodifier : spacketentityproperties$snapshot.getModifiers()) {
            buf.writeUniqueId(attributemodifier.getID());
            buf.writeDouble(attributemodifier.getAmount());
            buf.writeByte(attributemodifier.getOperation());
         }
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleEntityProperties(this);
   }

   @SideOnly(Side.CLIENT)
   public int getEntityId() {
      return this.entityId;
   }

   @SideOnly(Side.CLIENT)
   public List getSnapshots() {
      return this.snapshots;
   }

   public class Snapshot {
      private final String name;
      private final double baseValue;
      private final Collection modifiers;

      public Snapshot(String var2, double var3, Collection var5) {
         this.name = nameIn;
         this.baseValue = baseValueIn;
         this.modifiers = modifiersIn;
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
