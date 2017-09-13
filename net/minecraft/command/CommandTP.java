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

   public String getUsage(ICommandSender icommandlistener) {
      return "commands.tp.usage";
   }

   public void execute(MinecraftServer minecraftserver, ICommandSender icommandlistener, String[] astring) throws CommandException {
      if (astring.length < 1) {
         throw new WrongUsageException("commands.tp.usage", new Object[0]);
      } else {
         byte b0 = 0;
         Object object;
         if (astring.length != 2 && astring.length != 4 && astring.length != 6) {
            object = getCommandSenderAsPlayer(icommandlistener);
         } else {
            object = b(minecraftserver, icommandlistener, astring[0]);
            b0 = 1;
         }

         if (astring.length != 1 && astring.length != 2) {
            if (astring.length < b0 + 3) {
               throw new WrongUsageException("commands.tp.usage", new Object[0]);
            }

            if (((Entity)object).world != null) {
               int i = b0 + 1;
               CommandBase.CoordinateArg commandabstract_commandnumber = parseCoordinate(((Entity)object).posX, astring[b0], true);
               CommandBase.CoordinateArg commandabstract_commandnumber1 = parseCoordinate(((Entity)object).posY, astring[i++], -4096, 4096, false);
               CommandBase.CoordinateArg commandabstract_commandnumber2 = parseCoordinate(((Entity)object).posZ, astring[i++], true);
               CommandBase.CoordinateArg commandabstract_commandnumber3 = parseCoordinate((double)((Entity)object).rotationYaw, astring.length > i ? astring[i++] : "~", false);
               CommandBase.CoordinateArg commandabstract_commandnumber4 = parseCoordinate((double)((Entity)object).rotationPitch, astring.length > i ? astring[i] : "~", false);
               teleportEntityToCoordinates((Entity)object, commandabstract_commandnumber, commandabstract_commandnumber1, commandabstract_commandnumber2, commandabstract_commandnumber3, commandabstract_commandnumber4);
               notifyCommandListener(icommandlistener, this, "commands.tp.success.coordinates", new Object[]{((Entity)object).getName(), commandabstract_commandnumber.getResult(), commandabstract_commandnumber1.getResult(), commandabstract_commandnumber2.getResult()});
            }
         } else {
            Entity entity = b(minecraftserver, icommandlistener, astring[astring.length - 1]);
            if (((Entity)object).getBukkitEntity().teleport(entity.getBukkitEntity(), TeleportCause.COMMAND)) {
               notifyCommandListener(icommandlistener, this, "commands.tp.success", new Object[]{((Entity)object).getName(), entity.getName()});
            }
         }

      }
   }

   private static void teleportEntityToCoordinates(Entity entity, CommandBase.CoordinateArg commandabstract_commandnumber, CommandBase.CoordinateArg commandabstract_commandnumber1, CommandBase.CoordinateArg commandabstract_commandnumber2, CommandBase.CoordinateArg commandabstract_commandnumber3, CommandBase.CoordinateArg commandabstract_commandnumber4) {
      if (entity instanceof EntityPlayerMP) {
         EnumSet enumset = EnumSet.noneOf(SPacketPlayerPosLook.EnumFlags.class);
         if (commandabstract_commandnumber.isRelative()) {
            enumset.add(SPacketPlayerPosLook.EnumFlags.X);
         }

         if (commandabstract_commandnumber1.isRelative()) {
            enumset.add(SPacketPlayerPosLook.EnumFlags.Y);
         }

         if (commandabstract_commandnumber2.isRelative()) {
            enumset.add(SPacketPlayerPosLook.EnumFlags.Z);
         }

         if (commandabstract_commandnumber4.isRelative()) {
            enumset.add(SPacketPlayerPosLook.EnumFlags.X_ROT);
         }

         if (commandabstract_commandnumber3.isRelative()) {
            enumset.add(SPacketPlayerPosLook.EnumFlags.Y_ROT);
         }

         float f = (float)commandabstract_commandnumber3.getAmount();
         if (!commandabstract_commandnumber3.isRelative()) {
            f = MathHelper.wrapDegrees(f);
         }

         float f1 = (float)commandabstract_commandnumber4.getAmount();
         if (!commandabstract_commandnumber4.isRelative()) {
            f1 = MathHelper.wrapDegrees(f1);
         }

         entity.dismountRidingEntity();
         ((EntityPlayerMP)entity).connection.setPlayerLocation(commandabstract_commandnumber.getAmount(), commandabstract_commandnumber1.getAmount(), commandabstract_commandnumber2.getAmount(), f, f1, enumset);
         entity.setRotationYawHead(f);
      } else {
         float f2 = (float)MathHelper.wrapDegrees(commandabstract_commandnumber3.getResult());
         float f = (float)MathHelper.wrapDegrees(commandabstract_commandnumber4.getResult());
         f = MathHelper.clamp(f, -90.0F, 90.0F);
         entity.setLocationAndAngles(commandabstract_commandnumber.getResult(), commandabstract_commandnumber1.getResult(), commandabstract_commandnumber2.getResult(), f2, f);
         entity.setRotationYawHead(f2);
      }

      if (!(entity instanceof EntityLivingBase) || !((EntityLivingBase)entity).isElytraFlying()) {
         entity.motionY = 0.0D;
         entity.onGround = true;
      }

   }

   public List tabComplete(MinecraftServer minecraftserver, ICommandSender icommandlistener, String[] astring, @Nullable BlockPos blockposition) {
      return astring.length != 1 && astring.length != 2 ? Collections.emptyList() : getListOfStringsMatchingLastWord(astring, minecraftserver.getPlayers());
   }

   public boolean isUsernameIndex(String[] astring, int i) {
      return i == 0;
   }

   public int compareTo(ICommand o) {
      return this.compareTo(o);
   }
}
