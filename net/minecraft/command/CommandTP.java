package net.minecraft.command;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class CommandTP extends CommandBase {
   public String getName() {
      return "tp";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.tp.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 1) {
         throw new WrongUsageException("commands.tp.usage", new Object[0]);
      } else {
         byte var4 = 0;
         Object var5;
         if (var3.length != 2 && var3.length != 4 && var3.length != 6) {
            var5 = getCommandSenderAsPlayer(var2);
         } else {
            var5 = b(var1, var2, var3[0]);
            var4 = 1;
         }

         if (var3.length != 1 && var3.length != 2) {
            if (var3.length < var4 + 3) {
               throw new WrongUsageException("commands.tp.usage", new Object[0]);
            }

            if (((Entity)var5).world != null) {
               int var12 = var4 + 1;
               CommandBase.CoordinateArg var7 = parseCoordinate(((Entity)var5).posX, var3[var4], true);
               CommandBase.CoordinateArg var8 = parseCoordinate(((Entity)var5).posY, var3[var12++], -4096, 4096, false);
               CommandBase.CoordinateArg var9 = parseCoordinate(((Entity)var5).posZ, var3[var12++], true);
               CommandBase.CoordinateArg var10 = parseCoordinate((double)((Entity)var5).rotationYaw, var3.length > var12 ? var3[var12++] : "~", false);
               CommandBase.CoordinateArg var11 = parseCoordinate((double)((Entity)var5).rotationPitch, var3.length > var12 ? var3[var12] : "~", false);
               teleportEntityToCoordinates((Entity)var5, var7, var8, var9, var10, var11);
               notifyCommandListener(var2, this, "commands.tp.success.coordinates", new Object[]{((Entity)var5).getName(), var7.getResult(), var8.getResult(), var9.getResult()});
            }
         } else {
            Entity var6 = b(var1, var2, var3[var3.length - 1]);
            if (((Entity)var5).getBukkitEntity().teleport(var6.getBukkitEntity(), TeleportCause.COMMAND)) {
               notifyCommandListener(var2, this, "commands.tp.success", new Object[]{((Entity)var5).getName(), var6.getName()});
            }
         }

      }
   }

   private static void teleportEntityToCoordinates(Entity var0, CommandBase.CoordinateArg var1, CommandBase.CoordinateArg var2, CommandBase.CoordinateArg var3, CommandBase.CoordinateArg var4, CommandBase.CoordinateArg var5) {
      if (var0 instanceof EntityPlayerMP) {
         EnumSet var6 = EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class);
         if (var1.isRelative()) {
            var6.add(SPacketPlayerPosLook.EnumFlags.X);
         }

         if (var2.isRelative()) {
            var6.add(SPacketPlayerPosLook.EnumFlags.Y);
         }

         if (var3.isRelative()) {
            var6.add(SPacketPlayerPosLook.EnumFlags.Z);
         }

         if (var5.isRelative()) {
            var6.add(SPacketPlayerPosLook.EnumFlags.X_ROT);
         }

         if (var4.isRelative()) {
            var6.add(SPacketPlayerPosLook.EnumFlags.Y_ROT);
         }

         float var7 = (float)var4.getAmount();
         if (!var4.isRelative()) {
            var7 = MathHelper.wrapDegrees(var7);
         }

         float var8 = (float)var5.getAmount();
         if (!var5.isRelative()) {
            var8 = MathHelper.wrapDegrees(var8);
         }

         var0.dismountRidingEntity();
         ((EntityPlayerMP)var0).connection.setPlayerLocation(var1.getAmount(), var2.getAmount(), var3.getAmount(), var7, var8, var6);
         var0.setRotationYawHead(var7);
      } else {
         float var9 = (float)MathHelper.wrapDegrees(var4.getResult());
         float var10 = (float)MathHelper.wrapDegrees(var5.getResult());
         var10 = MathHelper.clamp(var10, -90.0F, 90.0F);
         var0.setLocationAndAngles(var1.getResult(), var2.getResult(), var3.getResult(), var9, var10);
         var0.setRotationYawHead(var9);
      }

      if (!(var0 instanceof EntityLivingBase) || !((EntityLivingBase)var0).isElytraFlying()) {
         var0.motionY = 0.0D;
         var0.onGround = true;
      }

   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length != 1 && var3.length != 2 ? Collections.emptyList() : getListOfStringsMatchingLastWord(var3, var1.getPlayers());
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 0;
   }

   public int compareTo(ICommand var1) {
      return this.compareTo(var1);
   }
}
