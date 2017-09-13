package net.minecraft.network.rcon;

import com.google.common.collect.Maps;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.SERVER)
public class RConThreadMain extends RConThreadBase {
   private int rconPort;
   private final int serverPort;
   private String hostname;
   private ServerSocket serverSocket;
   private final String rconPassword;
   private Map clientThreads;

   public RConThreadMain(IServer p_i1538_1_) {
      super(p_i1538_1_, "RCON Listener");
      this.rconPort = p_i1538_1_.getIntProperty("rcon.port", 0);
      this.rconPassword = p_i1538_1_.getStringProperty("rcon.password", "");
      this.hostname = p_i1538_1_.getHostname();
      this.serverPort = p_i1538_1_.getPort();
      if (0 == this.rconPort) {
         this.rconPort = this.serverPort + 10;
         this.logInfo("Setting default rcon port to " + this.rconPort);
         p_i1538_1_.setProperty("rcon.port", Integer.valueOf(this.rconPort));
         if (this.rconPassword.isEmpty()) {
            p_i1538_1_.setProperty("rcon.password", "");
         }

         p_i1538_1_.saveProperties();
      }

      if (this.hostname.isEmpty()) {
         this.hostname = "0.0.0.0";
      }

      this.initClientThreadList();
      this.serverSocket = null;
   }

   private void initClientThreadList() {
      this.clientThreads = Maps.newHashMap();
   }

   private void cleanClientThreadsMap() {
      Iterator iterator = this.clientThreads.entrySet().iterator();

      while(iterator.hasNext()) {
         Entry entry = (Entry)iterator.next();
         if (!((RConThreadClient)entry.getValue()).isRunning()) {
            iterator.remove();
         }
      }

   }

   public void run() {
      this.logInfo("RCON running on " + this.hostname + ":" + this.rconPort);

      try {
         while(this.running) {
            try {
               Socket socket = this.serverSocket.accept();
               socket.setSoTimeout(500);
               RConThreadClient rconthreadclient = new RConThreadClient(this.server, socket);
               rconthreadclient.startThread();
               this.clientThreads.put(socket.getRemoteSocketAddress(), rconthreadclient);
               this.cleanClientThreadsMap();
            } catch (SocketTimeoutException var7) {
               this.cleanClientThreadsMap();
            } catch (IOException var8) {
               if (this.running) {
                  this.logInfo("IO: " + var8.getMessage());
               }
            }
         }
      } finally {
         this.closeServerSocket(this.serverSocket);
      }

   }

   public void startThread() {
      if (this.rconPassword.isEmpty()) {
         this.logWarning("No rcon password set in '" + this.server.getSettingsFilename() + "', rcon disabled!");
      } else if (0 < this.rconPort && 65535 >= this.rconPort) {
         if (!this.running) {
            try {
               this.serverSocket = new ServerSocket(this.rconPort, 0, InetAddress.getByName(this.hostname));
               this.serverSocket.setSoTimeout(500);
               super.startThread();
            } catch (IOException var2) {
               this.logWarning("Unable to initialise rcon on " + this.hostname + ":" + this.rconPort + " : " + var2.getMessage());
            }
         }
      } else {
         this.logWarning("Invalid rcon port " + this.rconPort + " found in '" + this.server.getSettingsFilename() + "', rcon disabled!");
      }

   }
}
