package net.minecraft.command;

import io.netty.buffer.Unpooled;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;

public class CommandStopSound extends CommandBase {
   public String getName() {
      return "stopsound";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.stopsound.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length >= 1 && var3.length <= 3) {
         int var4 = 0;
         EntityPlayerMP var5 = a(var1, var2, var3[var4++]);
         String var6 = "";
         String var7 = "";
         if (var3.length >= 2) {
            String var8 = var3[var4++];
            SoundCategory var9 = SoundCategory.getByName(var8);
            if (var9 == null) {
               throw new CommandException("commands.stopsound.unknownSoundSource", new Object[]{var8});
            }

            var6 = var9.getName();
         }

         if (var3.length == 3) {
            var7 = var3[var4++];
         }

         PacketBuffer var12 = new PacketBuffer(Unpooled.buffer());
         var12.writeString(var6);
         var12.writeString(var7);
         var5.connection.sendPacket(new SPacketCustomPayload("MC|StopSound", var12));
         if (var6.isEmpty() && var7.isEmpty()) {
            notifyCommandListener(var2, this, "commands.stopsound.success.all", new Object[]{var5.getName()});
         } else if (var7.isEmpty()) {
            notifyCommandListener(var2, this, "commands.stopsound.success.soundSource", new Object[]{var6, var5.getName()});
         } else {
            notifyCommandListener(var2, this, "commands.stopsound.success.individualSound", new Object[]{var7, var6, var5.getName()});
         }

      } else {
         throw new WrongUsageException(this.getUsage(var2), new Object[0]);
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         return getListOfStringsMatchingLastWord(var3, var1.getPlayers());
      } else if (var3.length == 2) {
         return getListOfStringsMatchingLastWord(var3, SoundCategory.getSoundCategoryNames());
      } else {
         return var3.length == 3 ? getListOfStringsMatchingLastWord(var3, SoundEvent.REGISTRY.getKeys()) : Collections.emptyList();
      }
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 0;
   }
}
