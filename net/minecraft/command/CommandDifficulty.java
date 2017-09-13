package net.minecraft.command;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.src.MinecraftServer;
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
      if (var3.length <= 0) {
         throw new WrongUsageException("commands.difficulty.usage", new Object[0]);
      } else {
         EnumDifficulty var4 = this.getDifficultyFromCommand(var3[0]);
         var1.a(var4);
         notifyCommandListener(var2, this, "commands.difficulty.success", new Object[]{new TextComponentTranslation(var4.getDifficultyResourceKey(), new Object[0])});
      }
   }

   protected EnumDifficulty getDifficultyFromCommand(String var1) throws NumberInvalidException {
      if (!"peaceful".equalsIgnoreCase(var1) && !"p".equalsIgnoreCase(var1)) {
         if (!"easy".equalsIgnoreCase(var1) && !"e".equalsIgnoreCase(var1)) {
            if (!"normal".equalsIgnoreCase(var1) && !"n".equalsIgnoreCase(var1)) {
               return !"hard".equalsIgnoreCase(var1) && !"h".equalsIgnoreCase(var1) ? EnumDifficulty.getDifficultyEnum(parseInt(var1, 0, 3)) : EnumDifficulty.HARD;
            } else {
               return EnumDifficulty.NORMAL;
            }
         } else {
            return EnumDifficulty.EASY;
         }
      } else {
         return EnumDifficulty.PEACEFUL;
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, new String[]{"peaceful", "easy", "normal", "hard"}) : Collections.emptyList();
   }
}
