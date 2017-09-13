package net.minecraft.command;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class CommandShowSeed extends CommandBase {
   public boolean checkPermission(MinecraftServer var1, ICommandSender var2) {
      return var1.isSinglePlayer() || super.checkPermission(var1, var2);
   }

   public String getName() {
      return "seed";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.seed.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      Object var4 = var2 instanceof EntityPlayer ? ((EntityPlayer)var2).world : var1.worldServerForDimension(0);
      var2.sendMessage(new TextComponentTranslation("commands.seed.success", new Object[]{((World)var4).getSeed()}));
   }
}
