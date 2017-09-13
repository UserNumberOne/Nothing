package net.minecraft.network.status.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.ServerStatusResponse;
import net.minecraft.network.status.INetHandlerStatusClient;
import net.minecraft.util.EnumTypeAdapterFactory;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketServerInfo implements Packet {
   public static final Gson GSON = (new GsonBuilder()).registerTypeAdapter(ServerStatusResponse.Version.class, new ServerStatusResponse.Version.Serializer()).registerTypeAdapter(ServerStatusResponse.Players.class, new ServerStatusResponse.Players.Serializer()).registerTypeAdapter(ServerStatusResponse.class, new ServerStatusResponse.Serializer()).registerTypeHierarchyAdapter(ITextComponent.class, new ITextComponent.Serializer()).registerTypeHierarchyAdapter(Style.class, new Style.Serializer()).registerTypeAdapterFactory(new EnumTypeAdapterFactory()).create();
   private ServerStatusResponse response;

   public SPacketServerInfo() {
   }

   public SPacketServerInfo(ServerStatusResponse var1) {
      this.response = responseIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.response = (ServerStatusResponse)JsonUtils.gsonDeserialize(GSON, buf.readString(32767), ServerStatusResponse.class);
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeString(GSON.toJson(this.response));
   }

   public void processPacket(INetHandlerStatusClient var1) {
      handler.handleServerInfo(this);
   }

   @SideOnly(Side.CLIENT)
   public ServerStatusResponse getResponse() {
      return this.response;
   }
}
