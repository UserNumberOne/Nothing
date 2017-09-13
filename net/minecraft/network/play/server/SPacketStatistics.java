package net.minecraft.network.play.server;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SPacketStatistics implements Packet {
   private Map statisticMap;

   public SPacketStatistics() {
   }

   public SPacketStatistics(Map var1) {
      this.statisticMap = var1;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handleStatistics(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      int var2 = var1.readVarInt();
      this.statisticMap = Maps.newHashMap();

      for(int var3 = 0; var3 < var2; ++var3) {
         StatBase var4 = StatList.getOneShotStat(var1.readString(32767));
         int var5 = var1.readVarInt();
         if (var4 != null) {
            this.statisticMap.put(var4, Integer.valueOf(var5));
         }
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeVarInt(this.statisticMap.size());

      for(Entry var3 : this.statisticMap.entrySet()) {
         var1.writeString(((StatBase)var3.getKey()).statId);
         var1.writeVarInt(((Integer)var3.getValue()).intValue());
      }

   }

   @SideOnly(Side.CLIENT)
   public Map getStatisticMap() {
      return this.statisticMap;
   }
}
