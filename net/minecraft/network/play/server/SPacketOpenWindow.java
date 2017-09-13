package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;

public class SPacketOpenWindow implements Packet {
   private int windowId;
   private String inventoryType;
   private ITextComponent windowTitle;
   private int slotCount;
   private int entityId;

   public SPacketOpenWindow() {
   }

   public SPacketOpenWindow(int var1, String var2, ITextComponent var3) {
      this(var1, var2, var3, 0);
   }

   public SPacketOpenWindow(int var1, String var2, ITextComponent var3, int var4) {
      this.windowId = var1;
      this.inventoryType = var2;
      this.windowTitle = var3;
      this.slotCount = var4;
   }

   public SPacketOpenWindow(int var1, String var2, ITextComponent var3, int var4, int var5) {
      this(var1, var2, var3, var4);
      this.entityId = var5;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleOpenWindow(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.windowId = var1.readUnsignedByte();
      this.inventoryType = var1.readString(32);
      this.windowTitle = var1.readTextComponent();
      this.slotCount = var1.readUnsignedByte();
      if (this.inventoryType.equals("EntityHorse")) {
         this.entityId = var1.readInt();
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeByte(this.windowId);
      var1.writeString(this.inventoryType);
      var1.writeTextComponent(this.windowTitle);
      var1.writeByte(this.slotCount);
      if (this.inventoryType.equals("EntityHorse")) {
         var1.writeInt(this.entityId);
      }

   }
}
