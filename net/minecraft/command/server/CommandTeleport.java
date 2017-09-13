package net.minecraft.command.server;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class CommandTeleport extends CommandBase {
   public String getName() {
      return "teleport";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.teleport.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 4) {
         throw new WrongUsageException("commands.teleport.usage", new Object[0]);
      } else {
         Entity var4 = b(var1, var2, var3[0]);
         if (var4.world != null) {
            boolean var5 = true;
            Vec3d var6 = var2.getPositionVector();
            int var7 = 1;
            CommandBase.CoordinateArg var8 = parseCoordinate(var6.xCoord, var3[var7++], true);
            CommandBase.CoordinateArg var9 = parseCoordinate(var6.yCoord, var3[var7++], -4096, 4096, false);
            CommandBase.CoordinateArg var10 = parseCoordinate(var6.zCoord, var3[var7++], true);
            Entity var11 = var2.getCommandSenderEntity() == null ? var4 : var2.getCommandSenderEntity();
            CommandBase.CoordinateArg var12 = parseCoordinate(var3.length > var7 ? (double)var11.rotationYaw : (double)var4.rotationYaw, var3.length > var7 ? var3[var7] : "~", false);
            ++var7;
            CommandBase.CoordinateArg var13 = parseCoordinate(var3.length > var7 ? (double)var11.rotationPitch : (double)var4.rotationPitch, var3.length > var7 ? var3[var7] : "~", false);
            doTeleport(var4, var8, var9, var10, var12, var13);
            notifyCommandListener(var2, this, "commands.teleport.success.coordinates", new Object[]{var4.getName(), var8.getResult(), var9.getResult(), var10.getResult()});
         }
      }
   }

   private static void doTeleport(Entity var0, CommandBase.CoordinateArg var1, CommandBase.CoordinateArg var2, CommandBase.CoordinateArg var3, CommandBase.CoordinateArg var4, CommandBase.CoordinateArg var5) {
      if (var0 instanceof EntityPlayerMP) {
         EnumSet var6 = EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class);
         float var7 = (float)var4.getAmount();
         if (var4.isRelative()) {
            var6.add(SPacketPlayerPosLook.EnumFlags.Y_ROT);
         } else {
            var7 = MathHelper.wrapDegrees(var7);
         }

         float var8 = (float)var5.getAmount();
         if (var5.isRelative()) {
            var6.add(SPacketPlayerPosLook.EnumFlags.X_ROT);
         } else {
            var8 = MathHelper.wrapDegrees(var8);
         }

         var0.dismountRidingEntity();
         ((EntityPlayerMP)var0).connection.setPlayerLocation(var1.getResult(), var2.getResult(), var3.getResult(), var7, var8, var6);
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
      if (var3.length == 1) {
         return getListOfStringsMatchingLastWord(var3, var1.getPlayers());
      } else {
         return var3.length > 1 && var3.length <= 4 ? getTabCompletionCoordinate(var3, 1, var4) : Collections.emptyList();
      }
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 0;
   }
}
