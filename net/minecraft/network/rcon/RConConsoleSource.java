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

   public RConConsoleSource(MinecraftServer var1) {
      this.server = var1;
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

   public void sendMessage(String var1) {
      this.buffer.append(var1);
   }

   public void sendMessage(ITextComponent var1) {
      this.buffer.append(var1.getUnformattedText());
   }

   public boolean canUseCommand(int var1, String var2) {
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

   public void setCommandStat(CommandResultStats.Type var1, int var2) {
   }

   public MinecraftServer h() {
      return this.server;
   }
}
