package net.minecraft.network.play.server;

import java.io.IOException;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.potion.Potion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketRemoveEntityEffect implements Packet {
   private int entityId;
   private Potion effectId;

   public SPacketRemoveEntityEffect() {
   }

   public SPacketRemoveEntityEffect(int var1, Potion var2) {
      this.entityId = entityIdIn;
      this.effectId = potionIn;
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.entityId = buf.readVarInt();
      this.effectId = Potion.getPotionById(buf.readUnsignedByte());
   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(this.entityId);
      buf.writeByte(Potion.getIdFromPotion(this.effectId));
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleRemoveEntityEffect(this);
   }

   @Nullable
   @SideOnly(Side.CLIENT)
   public Entity getEntity(World var1) {
      return worldIn.getEntityByID(this.entityId);
   }

   @Nullable
   @SideOnly(Side.CLIENT)
   public Potion getPotion() {
      return this.effectId;
   }
}
