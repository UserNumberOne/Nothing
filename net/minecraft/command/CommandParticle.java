package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class CommandParticle extends CommandBase {
   public String getName() {
      return "particle";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.particle.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 8) {
         throw new WrongUsageException("commands.particle.usage", new Object[0]);
      } else {
         boolean var4 = false;
         EnumParticleTypes var5 = EnumParticleTypes.getByName(var3[0]);
         if (var5 == null) {
            throw new CommandException("commands.particle.notFound", new Object[]{var3[0]});
         } else {
            String var6 = var3[0];
            Vec3d var7 = var2.getPositionVector();
            double var8 = (double)((float)parseDouble(var7.xCoord, var3[1], true));
            double var10 = (double)((float)parseDouble(var7.yCoord, var3[2], true));
            double var12 = (double)((float)parseDouble(var7.zCoord, var3[3], true));
            double var14 = (double)((float)parseDouble(var3[4]));
            double var16 = (double)((float)parseDouble(var3[5]));
            double var18 = (double)((float)parseDouble(var3[6]));
            double var20 = (double)((float)parseDouble(var3[7]));
            int var22 = 0;
            if (var3.length > 8) {
               var22 = parseInt(var3[8], 0);
            }

            boolean var23 = false;
            if (var3.length > 9 && "force".equals(var3[9])) {
               var23 = true;
            }

            EntityPlayerMP var24;
            if (var3.length > 10) {
               var24 = getPlayer(var1, var2, var3[10]);
            } else {
               var24 = null;
            }

            int[] var25 = new int[var5.getArgumentCount()];

            for(int var26 = 0; var26 < var25.length; ++var26) {
               if (var3.length > 11 + var26) {
                  try {
                     var25[var26] = Integer.parseInt(var3[11 + var26]);
                  } catch (NumberFormatException var28) {
                     throw new CommandException("commands.particle.invalidParam", new Object[]{var3[11 + var26]});
                  }
               }
            }

            World var29 = var2.getEntityWorld();
            if (var29 instanceof WorldServer) {
               WorldServer var27 = (WorldServer)var29;
               if (var24 == null) {
                  var27.spawnParticle(var5, var23, var8, var10, var12, var22, var14, var16, var18, var20, var25);
               } else {
                  var27.spawnParticle(var24, var5, var23, var8, var10, var12, var22, var14, var16, var18, var20, var25);
               }

               notifyCommandListener(var2, this, "commands.particle.success", new Object[]{var6, Math.max(var22, 1)});
            }

         }
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, EnumParticleTypes.getParticleNames()) : (var3.length > 1 && var3.length <= 4 ? getTabCompletionCoordinate(var3, 1, var4) : (var3.length == 10 ? getListOfStringsMatchingLastWord(var3, new String[]{"normal", "force"}) : (var3.length == 11 ? getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames()) : Collections.emptyList())));
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 10;
   }
}
