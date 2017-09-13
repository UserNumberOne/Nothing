package net.minecraft.command.server;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.server.MinecraftServer;
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
      if (args.length < 4) {
         throw new WrongUsageException("commands.teleport.usage", new Object[0]);
      } else {
         Entity entity = getEntity(server, sender, args[0]);
         if (entity.world != null) {
            int i = 4096;
            Vec3d vec3d = sender.getPositionVector();
            int j = 1;
            CommandBase.CoordinateArg commandbase$coordinatearg = parseCoordinate(vec3d.xCoord, args[j++], true);
            CommandBase.CoordinateArg commandbase$coordinatearg1 = parseCoordinate(vec3d.yCoord, args[j++], -4096, 4096, false);
            CommandBase.CoordinateArg commandbase$coordinatearg2 = parseCoordinate(vec3d.zCoord, args[j++], true);
            Entity entity1 = sender.getCommandSenderEntity() == null ? entity : sender.getCommandSenderEntity();
            CommandBase.CoordinateArg commandbase$coordinatearg3 = parseCoordinate(args.length > j ? (double)entity1.rotationYaw : (double)entity.rotationYaw, args.length > j ? args[j] : "~", false);
            ++j;
            CommandBase.CoordinateArg commandbase$coordinatearg4 = parseCoordinate(args.length > j ? (double)entity1.rotationPitch : (double)entity.rotationPitch, args.length > j ? args[j] : "~", false);
            doTeleport(entity, commandbase$coordinatearg, commandbase$coordinatearg1, commandbase$coordinatearg2, commandbase$coordinatearg3, commandbase$coordinatearg4);
            notifyCommandListener(sender, this, "commands.teleport.success.coordinates", new Object[]{entity.getName(), commandbase$coordinatearg.getResult(), commandbase$coordinatearg1.getResult(), commandbase$coordinatearg2.getResult()});
         }

      }
   }

   private static void doTeleport(Entity var0, CommandBase.CoordinateArg var1, CommandBase.CoordinateArg var2, CommandBase.CoordinateArg var3, CommandBase.CoordinateArg var4, CommandBase.CoordinateArg var5) {
      if (p_189862_0_ instanceof EntityPlayerMP) {
         Set set = EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class);
         float f = (float)p_189862_4_.getAmount();
         if (p_189862_4_.isRelative()) {
            set.add(SPacketPlayerPosLook.EnumFlags.Y_ROT);
         } else {
            f = MathHelper.wrapDegrees(f);
         }

         float f1 = (float)p_189862_5_.getAmount();
         if (p_189862_5_.isRelative()) {
            set.add(SPacketPlayerPosLook.EnumFlags.X_ROT);
         } else {
            f1 = MathHelper.wrapDegrees(f1);
         }

         p_189862_0_.dismountRidingEntity();
         ((EntityPlayerMP)p_189862_0_).connection.setPlayerLocation(p_189862_1_.getResult(), p_189862_2_.getResult(), p_189862_3_.getResult(), f, f1, set);
         p_189862_0_.setRotationYawHead(f);
      } else {
         float f2 = (float)MathHelper.wrapDegrees(p_189862_4_.getResult());
         float f3 = (float)MathHelper.wrapDegrees(p_189862_5_.getResult());
         f3 = MathHelper.clamp(f3, -90.0F, 90.0F);
         p_189862_0_.setLocationAndAngles(p_189862_1_.getResult(), p_189862_2_.getResult(), p_189862_3_.getResult(), f2, f3);
         p_189862_0_.setRotationYawHead(f2);
      }

      if (!(p_189862_0_ instanceof EntityLivingBase) || !((EntityLivingBase)p_189862_0_).isElytraFlying()) {
         p_189862_0_.motionY = 0.0D;
         p_189862_0_.onGround = true;
      }

   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return args.length == 1 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : (args.length > 1 && args.length <= 4 ? getTabCompletionCoordinate(args, 1, pos) : Collections.emptyList());
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return index == 0;
   }
}
