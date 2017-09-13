package net.minecraft.network.play.server;

import java.io.IOException;
import java.util.UUID;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.BossInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketUpdateBossInfo implements Packet {
   private UUID uniqueId;
   private SPacketUpdateBossInfo.Operation operation;
   private ITextComponent name;
   private float percent;
   private BossInfo.Color color;
   private BossInfo.Overlay overlay;
   private boolean darkenSky;
   private boolean playEndBossMusic;
   private boolean createFog;

   public SPacketUpdateBossInfo() {
   }

   public SPacketUpdateBossInfo(SPacketUpdateBossInfo.Operation var1, BossInfo var2) {
      this.operation = var1;
      this.uniqueId = var2.getUniqueId();
      this.name = var2.getName();
      this.percent = var2.getPercent();
      this.color = var2.getColor();
      this.overlay = var2.getOverlay();
      this.darkenSky = var2.shouldDarkenSky();
      this.playEndBossMusic = var2.shouldPlayEndBossMusic();
      this.createFog = var2.shouldCreateFog();
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.uniqueId = var1.readUniqueId();
      this.operation = (SPacketUpdateBossInfo.Operation)var1.readEnumValue(SPacketUpdateBossInfo.Operation.class);
      switch(this.operation) {
      case ADD:
         this.name = var1.readTextComponent();
         this.percent = var1.readFloat();
         this.color = (BossInfo.Color)var1.readEnumValue(BossInfo.Color.class);
         this.overlay = (BossInfo.Overlay)var1.readEnumValue(BossInfo.Overlay.class);
         this.setFlags(var1.readUnsignedByte());
      case REMOVE:
      default:
         break;
      case UPDATE_PCT:
         this.percent = var1.readFloat();
         break;
      case UPDATE_NAME:
         this.name = var1.readTextComponent();
         break;
      case UPDATE_STYLE:
         this.color = (BossInfo.Color)var1.readEnumValue(BossInfo.Color.class);
         this.overlay = (BossInfo.Overlay)var1.readEnumValue(BossInfo.Overlay.class);
         break;
      case UPDATE_PROPERTIES:
         this.setFlags(var1.readUnsignedByte());
      }

   }

   private void setFlags(int var1) {
      this.darkenSky = (var1 & 1) > 0;
      this.playEndBossMusic = (var1 & 2) > 0;
      this.createFog = (var1 & 2) > 0;
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeUniqueId(this.uniqueId);
      var1.writeEnumValue(this.operation);
      switch(this.operation) {
      case ADD:
         var1.writeTextComponent(this.name);
         var1.writeFloat(this.percent);
         var1.writeEnumValue(this.color);
         var1.writeEnumValue(this.overlay);
         var1.writeByte(this.getFlags());
      case REMOVE:
      default:
         break;
      case UPDATE_PCT:
         var1.writeFloat(this.percent);
         break;
      case UPDATE_NAME:
         var1.writeTextComponent(this.name);
         break;
      case UPDATE_STYLE:
         var1.writeEnumValue(this.color);
         var1.writeEnumValue(this.overlay);
         break;
      case UPDATE_PROPERTIES:
         var1.writeByte(this.getFlags());
      }

   }

   private int getFlags() {
      int var1 = 0;
      if (this.darkenSky) {
         var1 |= 1;
      }

      if (this.playEndBossMusic) {
         var1 |= 2;
      }

      if (this.createFog) {
         var1 |= 2;
      }

      return var1;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleUpdateBossInfo(this);
   }

   @SideOnly(Side.CLIENT)
   public UUID getUniqueId() {
      return this.uniqueId;
   }

   @SideOnly(Side.CLIENT)
   public SPacketUpdateBossInfo.Operation getOperation() {
      return this.operation;
   }

   @SideOnly(Side.CLIENT)
   public ITextComponent getName() {
      return this.name;
   }

   @SideOnly(Side.CLIENT)
   public float getPercent() {
      return this.percent;
   }

   @SideOnly(Side.CLIENT)
   public BossInfo.Color getColor() {
      return this.color;
   }

   @SideOnly(Side.CLIENT)
   public BossInfo.Overlay getOverlay() {
      return this.overlay;
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldDarkenSky() {
      return this.darkenSky;
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldPlayEndBossMusic() {
      return this.playEndBossMusic;
   }

   @SideOnly(Side.CLIENT)
   public boolean shouldCreateFog() {
      return this.createFog;
   }

   public static enum Operation {
      ADD,
      REMOVE,
      UPDATE_PCT,
      UPDATE_NAME,
      UPDATE_STYLE,
      UPDATE_PROPERTIES;
   }
}
