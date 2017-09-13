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
      this.lang = langIn;
      this.view = renderDistanceIn;
      this.chatVisibility = chatVisibilityIn;
      this.enableColors = chatColorsIn;
      this.modelPartFlags = modelPartsIn;
      this.mainHand = mainHandIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.lang = buf.readString(7);
      this.view = buf.readByte();
      this.chatVisibility = (EntityPlayer.EnumChatVisibility)buf.readEnumValue(EntityPlayer.EnumChatVisibility.class);
      this.enableColors = buf.readBoolean();
      this.modelPartFlags = buf.readUnsignedByte();
      this.mainHand = (EnumHandSide)buf.readEnumValue(EnumHandSide.class);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeString(this.lang);
      buf.writeByte(this.view);
      buf.writeEnumValue(this.chatVisibility);
      buf.writeBoolean(this.enableColors);
      buf.writeByte(this.modelPartFlags);
      buf.writeEnumValue(this.mainHand);
   }

   public void processPacket(INetHandlerPlayServer var1) {
      handler.processClientSettings(this);
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
