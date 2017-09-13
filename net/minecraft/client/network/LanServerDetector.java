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
         String var3 = ThreadLanServerPing.getMotdFromPingResponse(var1);
         String var4 = ThreadLanServerPing.getAdFromPingResponse(var1);
         if (var4 != null) {
            var4 = var2.getHostAddress() + ":" + var4;
            boolean var5 = false;

            for(LanServerInfo var7 : this.listOfLanServers) {
               if (var7.getServerIpPort().equals(var4)) {
                  var7.updateLastSeen();
                  var5 = true;
                  break;
               }
            }

            if (!var5) {
               this.listOfLanServers.add(new LanServerInfo(var3, var4));
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
         this.localServerList = var1;
         this.setDaemon(true);
         this.socket = new MulticastSocket(4445);
         this.broadcastAddress = InetAddress.getByName("224.0.2.60");
         this.socket.setSoTimeout(5000);
         this.socket.joinGroup(this.broadcastAddress);
      }

      public void run() {
         byte[] var1 = new byte[1024];

         while(!this.isInterrupted()) {
            DatagramPacket var2 = new DatagramPacket(var1, var1.length);

            try {
               this.socket.receive(var2);
            } catch (SocketTimeoutException var5) {
               continue;
            } catch (IOException var6) {
               LanServerDetector.LOGGER.error("Couldn't ping server", var6);
               break;
            }

            String var3 = new String(var2.getData(), var2.getOffset(), var2.getLength());
            LanServerDetector.LOGGER.debug("{}: {}", new Object[]{var2.getAddress(), var3});
            this.localServerList.addServer(var3, var2.getAddress());
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
