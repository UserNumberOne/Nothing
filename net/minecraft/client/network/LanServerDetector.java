package net.minecraft.client.network;

import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.client.multiplayer.ThreadLanServerPing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class LanServerDetector {
   private static final AtomicInteger ATOMIC_COUNTER = new AtomicInteger(0);
   private static final Logger LOGGER = LogManager.getLogger();

   @SideOnly(Side.CLIENT)
   public static class LanServerList {
      private final List listOfLanServers = Lists.newArrayList();
      boolean wasUpdated;

      public synchronized boolean getWasUpdated() {
         return this.wasUpdated;
      }

      public synchronized void setWasNotUpdated() {
         this.wasUpdated = false;
      }

      public synchronized List getLanServers() {
         return Collections.unmodifiableList(this.listOfLanServers);
      }

      public synchronized void addServer(String var1, InetAddress var2) {
         String s = ThreadLanServerPing.getMotdFromPingResponse(pingResponse);
         String s1 = ThreadLanServerPing.getAdFromPingResponse(pingResponse);
         if (s1 != null) {
            s1 = ipAddress.getHostAddress() + ":" + s1;
            boolean flag = false;

            for(LanServerInfo lanserverinfo : this.listOfLanServers) {
               if (lanserverinfo.getServerIpPort().equals(s1)) {
                  lanserverinfo.updateLastSeen();
                  flag = true;
                  break;
               }
            }

            if (!flag) {
               this.listOfLanServers.add(new LanServerInfo(s, s1));
               this.wasUpdated = true;
            }
         }

      }
   }

   @SideOnly(Side.CLIENT)
   public static class ThreadLanServerFind extends Thread {
      private final LanServerDetector.LanServerList localServerList;
      private final InetAddress broadcastAddress;
      private final MulticastSocket socket;

      public ThreadLanServerFind(LanServerDetector.LanServerList var1) throws IOException {
         super("LanServerDetector #" + LanServerDetector.ATOMIC_COUNTER.incrementAndGet());
         this.localServerList = list;
         this.setDaemon(true);
         this.socket = new MulticastSocket(4445);
         this.broadcastAddress = InetAddress.getByName("224.0.2.60");
         this.socket.setSoTimeout(5000);
         this.socket.joinGroup(this.broadcastAddress);
      }

      public void run() {
         byte[] abyte = new byte[1024];

         while(!this.isInterrupted()) {
            DatagramPacket datagrampacket = new DatagramPacket(abyte, abyte.length);

            try {
               this.socket.receive(datagrampacket);
            } catch (SocketTimeoutException var5) {
               continue;
            } catch (IOException var6) {
               LanServerDetector.LOGGER.error("Couldn't ping server", var6);
               break;
            }

            String s = new String(datagrampacket.getData(), datagrampacket.getOffset(), datagrampacket.getLength());
            LanServerDetector.LOGGER.debug("{}: {}", new Object[]{datagrampacket.getAddress(), s});
            this.localServerList.addServer(s, datagrampacket.getAddress());
         }

         try {
            this.socket.leaveGroup(this.broadcastAddress);
         } catch (IOException var4) {
            ;
         }

         this.socket.close();
      }
   }
}
