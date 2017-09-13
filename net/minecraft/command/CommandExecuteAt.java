package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class CommandExecuteAt extends CommandBase {
   public String getName() {
      return "execute";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.execute.usage";
   }

   public void execute(final MinecraftServer var1, final ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 5) {
         throw new WrongUsageException("commands.execute.usage", new Object[0]);
      } else {
         final Entity var4 = getEntity(var1, var2, var3[0], Entity.class);
         final double var5 = parseDouble(var4.posX, var3[1], false);
         final double var7 = parseDouble(var4.posY, var3[2], false);
         final double var9 = parseDouble(var4.posZ, var3[3], false);
         final BlockPos var11 = new BlockPos(var5, var7, var9);
         byte var12 = 4;
         if ("detect".equals(var3[4]) && var3.length > 10) {
            World var13 = var4.getEntityWorld();
            double var14 = parseDouble(var5, var3[5], false);
            double var16 = parseDouble(var7, var3[6], false);
            double var18 = parseDouble(var9, var3[7], false);
            Block var20 = getBlockByText(var2, var3[8]);
            int var21 = parseInt(var3[9], -1, 15);
            BlockPos var22 = new BlockPos(var14, var16, var18);
            if (!var13.isBlockLoaded(var22)) {
               throw new CommandException("commands.execute.failed", new Object[]{"detect", var4.getName()});
            }

            IBlockState var23 = var13.getBlockState(var22);
            if (var23.getBlock() != var20 || var21 >= 0 && var23.getBlock().getMetaFromState(var23) != var21) {
               throw new CommandException("commands.execute.failed", new Object[]{"detect", var4.getName()});
            }

            var12 = 10;
         }

         String var25 = buildString(var3, var12);
         ICommandSender var26 = new ICommandSender() {
            public String getName() {
               return var4.getName();
            }

            public ITextComponent getDisplayName() {
               return var4.getDisplayName();
            }

            public void sendMessage(ITextComponent var1x) {
               var2.sendMessage(var1x);
            }

            public boolean canUseCommand(int var1x, String var2x) {
               return var2.canUseCommand(var1x, var2x);
            }

            public BlockPos getPosition() {
               return var11;
            }

            public Vec3d getPositionVector() {
               return new Vec3d(var5, var7, var9);
            }

            public World getEntityWorld() {
               return var4.world;
            }

            public Entity getCommandSenderEntity() {
               return var4;
            }

            public boolean sendCommandFeedback() {
               return var1 == null || var1.worlds[0].getGameRules().getBoolean("commandBlockOutput");
            }

            public void setCommandStat(CommandResultStats.Type var1x, int var2x) {
               var4.setCommandStat(var1x, var2x);
            }

            public MinecraftServer getServer() {
               return var4.getServer();
            }
         };
         ICommandManager var15 = var1.getCommandManager();

         try {
            int var27 = var15.executeCommand(var26, var25);
            if (var27 < 1) {
               throw new CommandException("commands.execute.allInvocationsFailed", new Object[]{var25});
            }
         } catch (Throwable var24) {
            throw new CommandException("commands.execute.failed", new Object[]{var25, var4.getName()});
         }
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, var1.getOnlinePlayerNames()) : (var3.length > 1 && var3.length <= 4 ? getTabCompletionCoordinate(var3, 1, var4) : (var3.length > 5 && var3.length <= 8 && "detect".equals(var3[4]) ? getTabCompletionCoordinate(var3, 5, var4) : (var3.length == 9 && "detect".equals(var3[4]) ? getListOfStringsMatchingLastWord(var3, Block.REGISTRY.getKeys()) : Collections.emptyList())));
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 0;
   }
}
