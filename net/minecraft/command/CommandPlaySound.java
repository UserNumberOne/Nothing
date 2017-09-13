package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class CommandPlaySound extends CommandBase {
   public String getName() {
      return "playsound";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.playsound.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 2) {
         throw new WrongUsageException(this.getUsage(var2), new Object[0]);
      } else {
         int var4 = 0;
         String var5 = var3[var4++];
         String var6 = var3[var4++];
         SoundCategory var7 = SoundCategory.getByName(var6);
         if (var7 == null) {
            throw new CommandException("commands.playsound.unknownSoundSource", new Object[]{var6});
         } else {
            EntityPlayerMP var8 = a(var1, var2, var3[var4++]);
            Vec3d var9 = var2.getPositionVector();
            double var10 = var9.xCoord;
            if (var3.length > var4) {
               var10 = parseDouble(var10, var3[var4++], true);
            }

            double var12 = var9.yCoord;
            if (var3.length > var4) {
               var12 = parseDouble(var12, var3[var4++], 0, 0, false);
            }

            double var14 = var9.zCoord;
            if (var3.length > var4) {
               var14 = parseDouble(var14, var3[var4++], true);
            }

            double var16 = 1.0D;
            if (var3.length > var4) {
               var16 = parseDouble(var3[var4++], 0.0D, 3.4028234663852886E38D);
            }

            double var18 = 1.0D;
            if (var3.length > var4) {
               var18 = parseDouble(var3[var4++], 0.0D, 2.0D);
            }

            double var20 = 0.0D;
            if (var3.length > var4) {
               var20 = parseDouble(var3[var4], 0.0D, 1.0D);
            }

            double var22 = var16 > 1.0D ? var16 * 16.0D : 16.0D;
            double var24 = var8.getDistance(var10, var12, var14);
            if (var24 > var22) {
               if (var20 <= 0.0D) {
                  throw new CommandException("commands.playsound.playerTooFar", new Object[]{var8.getName()});
               }

               double var26 = var10 - var8.posX;
               double var28 = var12 - var8.posY;
               double var30 = var14 - var8.posZ;
               double var32 = Math.sqrt(var26 * var26 + var28 * var28 + var30 * var30);
               if (var32 > 0.0D) {
                  var10 = var8.posX + var26 / var32 * 2.0D;
                  var12 = var8.posY + var28 / var32 * 2.0D;
                  var14 = var8.posZ + var30 / var32 * 2.0D;
               }

               var16 = var20;
            }

            var8.connection.sendPacket(new SPacketCustomSound(var5, var7, var10, var12, var14, (float)var16, (float)var18));
            notifyCommandListener(var2, this, "commands.playsound.success", new Object[]{var5, var8.getName()});
         }
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         return getListOfStringsMatchingLastWord(var3, SoundEvent.REGISTRY.getKeys());
      } else if (var3.length == 2) {
         return getListOfStringsMatchingLastWord(var3, SoundCategory.getSoundCategoryNames());
      } else if (var3.length == 3) {
         return getListOfStringsMatchingLastWord(var3, var1.getPlayers());
      } else {
         return var3.length > 3 && var3.length <= 6 ? getTabCompletionCoordinate(var3, 3, var4) : Collections.emptyList();
      }
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 2;
   }
}
