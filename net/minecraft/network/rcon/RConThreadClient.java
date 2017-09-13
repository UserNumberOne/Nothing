package net.minecraft.network.rcon;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RConThreadClient extends RConThreadBase {
   private static final Logger LOGGER = LogManager.getLogger();
   private boolean loggedIn;
   private Socket clientSocket;
   private final byte[] buffer = new byte[1460];
   private final String rconPassword;

   RConThreadClient(IServer var1, Socket var2) {
      super(var1, "RCON Client");
      this.clientSocket = var2;

      try {
         this.clientSocket.setSoTimeout(0);
      } catch (Exception var4) {
         this.running = false;
      }

      this.rconPassword = var1.getStringProperty("rcon.password", "");
      this.logInfo("Rcon connection from: " + var2.getInetAddress());
   }

   public void run() {
      while(true) {
         try {
            if (!this.running) {
               return;
            }

            BufferedInputStream var1 = new BufferedInputStream(this.clientSocket.getInputStream());
            int var2 = var1.read(this.buffer, 0, 1460);
            if (10 <= var2) {
               int var3 = 0;
               int var4 = RConUtils.getBytesAsLEInt(this.buffer, 0, var2);
               if (var4 != var2 - 4) {
                  return;
               }

               var3 = var3 + 4;
               int var5 = RConUtils.getBytesAsLEInt(this.buffer, var3, var2);
               var3 = var3 + 4;
               int var6 = RConUtils.getRemainingBytesAsLEInt(this.buffer, var3);
               var3 = var3 + 4;
               switch(var6) {
               case 2:
                  if (this.loggedIn) {
                     String var8 = RConUtils.getBytesAsString(this.buffer, var3, var2);

                     try {
                        this.sendMultipacketResponse(var5, this.server.handleRConCommand(var8));
                     } catch (Exception var16) {
                        this.sendMultipacketResponse(var5, "Error executing: " + var8 + " (" + var16.getMessage() + ")");
                     }
                     continue;
                  }

                  this.sendLoginFailedResponse();
                  continue;
               case 3:
                  String var7 = RConUtils.getBytesAsString(this.buffer, var3, var2);
                  int var10000 = var3 + var7.length();
                  if (!var7.isEmpty() && var7.equals(this.rconPassword)) {
                     this.loggedIn = true;
                     this.sendResponse(var5, 2, "");
                     continue;
                  }

                  this.loggedIn = false;
                  this.sendLoginFailedResponse();
                  continue;
               default:
                  this.sendMultipacketResponse(var5, String.format("Unknown request %s", Integer.toHexString(var6)));
                  continue;
               }
            }
         } catch (SocketTimeoutException var17) {
            return;
         } catch (IOException var18) {
            return;
         } catch (Exception var19) {
            LOGGER.error("Exception whilst parsing RCON input", var19);
            return;
         } finally {
            this.closeSocket();
         }

         return;
      }
   }

   private void sendResponse(int var1, int var2, String var3) throws IOException {
      ByteArrayOutputStream var4 = new ByteArrayOutputStream(1248);
      DataOutputStream var5 = new DataOutputStream(var4);
      byte[] var6 = var3.getBytes("UTF-8");
      var5.writeInt(Integer.reverseBytes(var6.length + 10));
      var5.writeInt(Integer.reverseBytes(var1));
      var5.writeInt(Integer.reverseBytes(var2));
      var5.write(var6);
      var5.write(0);
      var5.write(0);
      this.clientSocket.getOutputStream().write(var4.toByteArray());
   }

   private void sendLoginFailedResponse() throws IOException {
      this.sendResponse(-1, 2, "");
   }

   private void sendMultipacketResponse(int var1, String var2) throws IOException {
      int var3 = var2.length();

      while(true) {
         int var4 = 4096 <= var3 ? 4096 : var3;
         this.sendResponse(var1, 0, var2.substring(0, var4));
         var2 = var2.substring(var4);
         var3 = var2.length();
         if (0 == var3) {
            break;
         }
      }

   }

   private void closeSocket() {
      if (null != this.clientSocket) {
         try {
            this.clientSocket.close();
         } catch (IOException var2) {
            this.logWarning("IO: " + var2.getMessage());
         }

         this.clientSocket = null;
      }
   }
}
