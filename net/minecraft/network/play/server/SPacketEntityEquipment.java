package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketEntityEquipment implements Packet {
   private int entityID;
   private EntityEquipmentSlot equipmentSlot;
   private ItemStack itemStack;

   public SPacketEntityEquipment() {
   }

   public SPacketEntityEquipment(int var1, EntityEquipmentSlot var2, @Nullable ItemStack var3) {
      this.entityID = var1;
      this.equipmentSlot = var2;
      this.itemStack = var3 == null ? null : var3.copy();
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityID = var1.readVarInt();
      this.equipmentSlot = (EntityEquipmentSlot)var1.readEnumValue(EntityEquipmentSlot.class);
      this.itemStack = var1.readItemStack();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.entityID);
      var1.writeEnumValue(this.equipmentSlot);
      var1.writeItemStack(this.itemStack);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleEntityEquipment(this);
   }

   @SideOnly(Side.CLIENT)
   public ItemStack getItemStack() {
      return this.itemStack;
   }

   @SideOnly(Side.CLIENT)
   public int getEntityID() {
      return this.entityID;
   }

   @SideOnly(Side.CLIENT)
   public EntityEquipmentSlot getEquipmentSlot() {
      return this.equipmentSlot;
   }
}
