package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketOpenWindow implements Packet {
   private int windowId;
   private String inventoryType;
   private ITextComponent windowTitle;
   private int slotCount;
   private int entityId;

   public SPacketOpenWindow() {
   }

   public SPacketOpenWindow(int var1, String var2, ITextComponent var3) {
      this(windowIdIn, inventoryTypeIn, windowTitleIn, 0);
   }

   public SPacketOpenWindow(int var1, String var2, ITextComponent var3, int var4) {
      this.windowId = windowIdIn;
      this.inventoryType = inventoryTypeIn;
      this.windowTitle = windowTitleIn;
      this.slotCount = slotCountIn;
   }

   public SPacketOpenWindow(int var1, String var2, ITextComponent var3, int var4, int var5) {
      this(windowIdIn, inventoryTypeIn, windowTitleIn, slotCountIn);
      this.entityId = entityIdIn;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleOpenWindow(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.windowId = buf.readUnsignedByte();
      this.inventoryType = buf.readString(32);
      this.windowTitle = buf.readTextComponent();
      this.slotCount = buf.readUnsignedByte();
      if (this.inventoryType.equals("EntityHorse")) {
         this.entityId = buf.readInt();
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeByte(this.windowId);
      buf.writeString(this.inventoryType);
      buf.writeTextComponent(this.windowTitle);
      buf.writeByte(this.slotCount);
      if (this.inventoryType.equals("EntityHorse")) {
         buf.writeInt(this.entityId);
      }

   }

   @SideOnly(Side.CLIENT)
   public int getWindowId() {
      return this.windowId;
   }

   @SideOnly(Side.CLIENT)
   public String getGuiId() {
      return this.inventoryType;
   }

   @SideOnly(Side.CLIENT)
   public ITextComponent getWindowTitle() {
      return this.windowTitle;
   }

   @SideOnly(Side.CLIENT)
   public int getSlotCount() {
      return this.slotCount;
   }

   @SideOnly(Side.CLIENT)
   public int getEntityId() {
      return this.entityId;
   }

   @SideOnly(Side.CLIENT)
   public boolean hasSlots() {
      return this.slotCount > 0;
   }
}
