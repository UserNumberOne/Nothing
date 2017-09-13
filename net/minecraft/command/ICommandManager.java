package net.minecraft.command;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.math.BlockPos;

public interface ICommandManager {
   int executeCommand(ICommandSender var1, String var2);

   List getTabCompletions(ICommandSender var1, String var2, @Nullable BlockPos var3);

   List getPossibleCommands(ICommandSender var1);

   Map getCommands();
}
