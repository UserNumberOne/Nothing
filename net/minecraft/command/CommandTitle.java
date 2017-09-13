package net.minecraft.command;

import com.google.gson.JsonParseException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class CommandTitle extends CommandBase {
   private static final Logger LOGGER = LogManager.getLogger();

   public String getName() {
      return "title";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.title.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 2) {
         throw new WrongUsageException("commands.title.usage", new Object[0]);
      } else {
         if (var3.length < 3) {
            if ("title".equals(var3[1]) || "subtitle".equals(var3[1])) {
               throw new WrongUsageException("commands.title.usage.title", new Object[0]);
            }

            if ("times".equals(var3[1])) {
               throw new WrongUsageException("commands.title.usage.times", new Object[0]);
            }
         }

         EntityPlayerMP var4 = a(var1, var2, var3[0]);
         SPacketTitle.Type var5 = SPacketTitle.Type.byName(var3[1]);
         if (var5 != SPacketTitle.Type.CLEAR && var5 != SPacketTitle.Type.RESET) {
            if (var5 == SPacketTitle.Type.TIMES) {
               if (var3.length != 5) {
                  throw new WrongUsageException("commands.title.usage", new Object[0]);
               } else {
                  int var12 = parseInt(var3[2]);
                  int var13 = parseInt(var3[3]);
                  int var14 = parseInt(var3[4]);
                  SPacketTitle var9 = new SPacketTitle(var12, var13, var14);
                  var4.connection.sendPacket(var9);
                  notifyCommandListener(var2, this, "commands.title.success", new Object[0]);
               }
            } else if (var3.length < 3) {
               throw new WrongUsageException("commands.title.usage", new Object[0]);
            } else {
               String var11 = buildString(var3, 2);

               ITextComponent var7;
               try {
                  var7 = ITextComponent.Serializer.jsonToComponent(var11);
               } catch (JsonParseException var10) {
                  throw toSyntaxException(var10);
               }

               SPacketTitle var8 = new SPacketTitle(var5, TextComponentUtils.processComponent(var2, var7, var4));
               var4.connection.sendPacket(var8);
               notifyCommandListener(var2, this, "commands.title.success", new Object[0]);
            }
         } else if (var3.length != 2) {
            throw new WrongUsageException("commands.title.usage", new Object[0]);
         } else {
            SPacketTitle var6 = new SPacketTitle(var5, (ITextComponent)null);
            var4.connection.sendPacket(var6);
            notifyCommandListener(var2, this, "commands.title.success", new Object[0]);
         }
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         return getListOfStringsMatchingLastWord(var3, var1.getPlayers());
      } else {
         return var3.length == 2 ? getListOfStringsMatchingLastWord(var3, SPacketTitle.Type.getNames()) : Collections.emptyList();
      }
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 0;
   }
}
