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
      this.statisticMap = statisticMapIn;
   }

   public void processPacket(INetHandlerPlayClient var1) {
      handler.handleStatistics(this);
   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      int i = buf.readVarInt();
      this.statisticMap = Maps.newHashMap();

      for(int j = 0; j < i; ++j) {
         StatBase statbase = StatList.getOneShotStat(buf.readString(32767));
         int k = buf.readVarInt();
         if (statbase != null) {
            this.statisticMap.put(statbase, Integer.valueOf(k));
         }
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      buf.writeVarInt(this.statisticMap.size());

      for(Entry entry : this.statisticMap.entrySet()) {
         buf.writeString(((StatBase)entry.getKey()).statId);
         buf.writeVarInt(((Integer)entry.getValue()).intValue());
      }

   }

   @SideOnly(Side.CLIENT)
   public Map getStatisticMap() {
      return this.statisticMap;
   }
}
