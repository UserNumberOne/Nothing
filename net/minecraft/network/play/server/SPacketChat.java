package net.minecraft.network.play.server;

import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketChat implements Packet {
   private ITextComponent chatComponent;
   private byte type;

   public SPacketChat() {
   }

   public SPacketChat(ITextComponent var1) {
      this(componentIn, (byte)1);
   }

   public SPacketChat(ITextComponent var1, byte var2) {
      this.chatComponent = componentIn;
      this.type = typeIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.chatComponent = buf.readTextComponent();
      this.type = buf.readByte();
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeTextComponent(this.chatComponent);
      buf.writeByte(this.type);
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleChat(this);
   }

   @SideOnly(Side.CLIENT)
   public ITextComponent getChatComponent() {
      return this.chatComponent;
   }

   public boolean isSystem() {
      return this.type == 1 || this.type == 2;
   }

   @SideOnly(Side.CLIENT)
   public byte getType() {
      return this.type;
   }
}
