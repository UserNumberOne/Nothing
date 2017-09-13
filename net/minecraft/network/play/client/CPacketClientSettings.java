package net.minecraft.network.play.client;

import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayServer;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CPacketClientSettings implements Packet {
   private String lang;
   private int view;
   private EntityPlayer.EnumChatVisibility chatVisibility;
   private boolean enableColors;
   private int modelPartFlags;
   private EnumHandSide mainHand;

   public CPacketClientSettings() {
   }

   @SideOnly(Side.CLIENT)
   public CPacketClientSettings(String var1, int var2, EntityPlayer.EnumChatVisibility var3, boolean var4, int var5, EnumHandSide var6) {
      this.lang = var1;
      this.view = var2;
      this.chatVisibility = var3;
      this.enableColors = var4;
      this.modelPartFlags = var5;
      this.mainHand = var6;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.lang = var1.readString(7);
      this.view = var1.readByte();
      this.chatVisibility = (EntityPlayer.EnumChatVisibility)var1.readEnumValue(EntityPlayer.EnumChatVisibility.class);
      this.enableColors = var1.readBoolean();
      this.modelPartFlags = var1.readUnsignedByte();
      this.mainHand = (EnumHandSide)var1.readEnumValue(EnumHandSide.class);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeString(this.lang);
      var1.writeByte(this.view);
      var1.writeEnumValue(this.chatVisibility);
      var1.writeBoolean(this.enableColors);
      var1.writeByte(this.modelPartFlags);
      var1.writeEnumValue(this.mainHand);
   }

   public void processPacket(INetHandlerPlayServer var1) {
      var1.processClientSettings(this);
   }

   public String getLang() {
      return this.lang;
   }

   public EntityPlayer.EnumChatVisibility getChatVisibility() {
      return this.chatVisibility;
   }

   public boolean isColorsEnabled() {
      return this.enableColors;
   }

   public int getModelPartFlags() {
      return this.modelPartFlags;
   }

   public EnumHandSide getMainHand() {
      return this.mainHand;
   }
}
