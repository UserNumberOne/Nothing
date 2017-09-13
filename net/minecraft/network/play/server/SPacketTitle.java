package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketTitle implements Packet {
   private SPacketTitle.Type type;
   private ITextComponent message;
   private int fadeInTime;
   private int displayTime;
   private int fadeOutTime;

   public SPacketTitle() {
   }

   public SPacketTitle(SPacketTitle.Type var1, ITextComponent var2) {
      this(typeIn, messageIn, -1, -1, -1);
   }

   public SPacketTitle(int var1, int var2, int var3) {
      this(SPacketTitle.Type.TIMES, (ITextComponent)null, fadeInTimeIn, displayTimeIn, fadeOutTimeIn);
   }

   public SPacketTitle(SPacketTitle.Type var1, @Nullable ITextComponent var2, int var3, int var4, int var5) {
      this.type = typeIn;
      this.message = messageIn;
      this.fadeInTime = fadeInTimeIn;
      this.displayTime = displayTimeIn;
      this.fadeOutTime = fadeOutTimeIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.type = (SPacketTitle.Type)buf.readEnumValue(SPacketTitle.Type.class);
      if (this.type == SPacketTitle.Type.TITLE || this.type == SPacketTitle.Type.SUBTITLE) {
         this.message = buf.readTextComponent();
      }

      if (this.type == SPacketTitle.Type.TIMES) {
         this.fadeInTime = buf.readInt();
         this.displayTime = buf.readInt();
         this.fadeOutTime = buf.readInt();
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeEnumValue(this.type);
      if (this.type == SPacketTitle.Type.TITLE || this.type == SPacketTitle.Type.SUBTITLE) {
         buf.writeTextComponent(this.message);
      }

      if (this.type == SPacketTitle.Type.TIMES) {
         buf.writeInt(this.fadeInTime);
         buf.writeInt(this.displayTime);
         buf.writeInt(this.fadeOutTime);
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleTitle(this);
   }

   @SideOnly(Side.CLIENT)
   public SPacketTitle.Type getType() {
      return this.type;
   }

   @SideOnly(Side.CLIENT)
   public ITextComponent getMessage() {
      return this.message;
   }

   @SideOnly(Side.CLIENT)
   public int getFadeInTime() {
      return this.fadeInTime;
   }

   @SideOnly(Side.CLIENT)
   public int getDisplayTime() {
      return this.displayTime;
   }

   @SideOnly(Side.CLIENT)
   public int getFadeOutTime() {
      return this.fadeOutTime;
   }

   public static enum Type {
      TITLE,
      SUBTITLE,
      TIMES,
      CLEAR,
      RESET;

      public static SPacketTitle.Type byName(String var0) {
         for(SPacketTitle.Type spackettitle$type : values()) {
            if (spackettitle$type.name().equalsIgnoreCase(name)) {
               return spackettitle$type;
            }
         }

         return TITLE;
      }

      public static String[] getNames() {
         String[] astring = new String[values().length];
         int i = 0;

         for(SPacketTitle.Type spackettitle$type : values()) {
            astring[i++] = spackettitle$type.name().toLowerCase();
         }

         return astring;
      }
   }
}
