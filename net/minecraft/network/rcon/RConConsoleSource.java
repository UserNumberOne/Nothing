package net.minecraft.network.rcon;

import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class RConConsoleSource implements ICommandSender {
   private final StringBuffer buffer = new StringBuffer();
   private final MinecraftServer server;

   public RConConsoleSource(MinecraftServer minecraftserver) {
      this.server = minecraftserver;
   }

   public void resetLog() {
      this.buffer.setLength(0);
   }

   public String getLogContents() {
      return this.buffer.toString();
   }

   public String getName() {
      return "Rcon";
   }

   public ITextComponent getDisplayName() {
      return new TextComponentString(this.getName());
   }

   public void sendMessage(String message) {
      this.buffer.append(message);
   }

   public void sendMessage(ITextComponent ichatbasecomponent) {
      this.buffer.append(ichatbasecomponent.getUnformattedText());
   }

   public boolean canUseCommand(int i, String s) {
      return true;
   }

   public BlockPos getPosition() {
      return BlockPos.ORIGIN;
   }

   public Vec3d getPositionVector() {
      return Vec3d.ZERO;
   }

   public World getEntityWorld() {
      return this.server.getEntityWorld();
   }

   public Entity getCommandSenderEntity() {
      return null;
   }

   public boolean sendCommandFeedback() {
      return true;
   }

   public void setCommandStat(CommandResultStats.Type commandobjectiveexecutor_enumcommandresult, int i) {
   }

   public MinecraftServer h() {
      return this.server;
   }
}
