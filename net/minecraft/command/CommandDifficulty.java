package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.EnumDifficulty;

public class CommandDifficulty extends CommandBase {
   public String getName() {
      return "difficulty";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.difficulty.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (args.length <= 0) {
         throw new WrongUsageException("commands.difficulty.usage", new Object[0]);
      } else {
         EnumDifficulty enumdifficulty = this.getDifficultyFromCommand(args[0]);
         server.setDifficultyForAllWorlds(enumdifficulty);
         notifyCommandListener(sender, this, "commands.difficulty.success", new Object[]{new TextComponentTranslation(enumdifficulty.getDifficultyResourceKey(), new Object[0])});
      }
   }

   protected EnumDifficulty getDifficultyFromCommand(String var1) throws CommandException, NumberInvalidException {
      return !"peaceful".equalsIgnoreCase(difficultyString) && !"p".equalsIgnoreCase(difficultyString) ? (!"easy".equalsIgnoreCase(difficultyString) && !"e".equalsIgnoreCase(difficultyString) ? (!"normal".equalsIgnoreCase(difficultyString) && !"n".equalsIgnoreCase(difficultyString) ? (!"hard".equalsIgnoreCase(difficultyString) && !"h".equalsIgnoreCase(difficultyString) ? EnumDifficulty.getDifficultyEnum(parseInt(difficultyString, 0, 3)) : EnumDifficulty.HARD) : EnumDifficulty.NORMAL) : EnumDifficulty.EASY) : EnumDifficulty.PEACEFUL;
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return args.length == 1 ? getListOfStringsMatchingLastWord(args, new String[]{"peaceful", "easy", "normal", "hard"}) : Collections.emptyList();
   }
}
