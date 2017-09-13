package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;

public class SPacketTitle implements Packet {
   private SPacketTitle.Type type;
   private ITextComponent message;
   private int fadeInTime;
   private int displayTime;
   private int fadeOutTime;

   public SPacketTitle() {
   }

   public SPacketTitle(SPacketTitle.Type var1, ITextComponent var2) {
      this(var1, var2, -1, -1, -1);
   }

   public SPacketTitle(int var1, int var2, int var3) {
      this(SPacketTitle.Type.TIMES, (ITextComponent)null, var1, var2, var3);
   }

   public SPacketTitle(SPacketTitle.Type var1, @Nullable ITextComponent var2, int var3, int var4, int var5) {
      this.type = var1;
      this.message = var2;
      this.fadeInTime = var3;
      this.displayTime = var4;
      this.fadeOutTime = var5;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.type = (SPacketTitle.Type)var1.readEnumValue(SPacketTitle.Type.class);
      if (this.type == SPacketTitle.Type.TITLE || this.type == SPacketTitle.Type.SUBTITLE) {
         this.message = var1.readTextComponent();
      }

      if (this.type == SPacketTitle.Type.TIMES) {
         this.fadeInTime = var1.readInt();
         this.displayTime = var1.readInt();
         this.fadeOutTime = var1.readInt();
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeEnumValue(this.type);
      if (this.type == SPacketTitle.Type.TITLE || this.type == SPacketTitle.Type.SUBTITLE) {
         var1.writeTextComponent(this.message);
      }

      if (this.type == SPacketTitle.Type.TIMES) {
         var1.writeInt(this.fadeInTime);
         var1.writeInt(this.displayTime);
         var1.writeInt(this.fadeOutTime);
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleTitle(this);
   }

   public static enum Type {
      TITLE,
      SUBTITLE,
      TIMES,
      CLEAR,
      RESET;

      public static SPacketTitle.Type byName(String var0) {
         for(SPacketTitle.Type var4 : values()) {
            if (var4.name().equalsIgnoreCase(var0)) {
               return var4;
            }
         }

         return TITLE;
      }

      public static String[] getNames() {
         String[] var0 = new String[values().length];
         int var1 = 0;

         for(SPacketTitle.Type var5 : values()) {
            var0[var1++] = var5.name().toLowerCase();
         }

         return var0;
      }
   }
}
