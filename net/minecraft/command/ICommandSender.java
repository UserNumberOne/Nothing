package net.minecraft.command;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public interface ICommandSender {
   String getName();

   ITextComponent getDisplayName();

   void sendMessage(ITextComponent var1);

   boolean canUseCommand(int var1, String var2);

   BlockPos getPosition();

   Vec3d getPositionVector();

   World getEntityWorld();

   @Nullable
   Entity getCommandSenderEntity();

   boolean sendCommandFeedback();

   void setCommandStat(CommandResultStats.Type var1, int var2);

   @Nullable
   MinecraftServer h();
}
