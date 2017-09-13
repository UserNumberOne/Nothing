package net.minecraft.command.server;

import com.google.gson.JsonParseException;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentUtils;

public class CommandMessageRaw extends CommandBase {
   public String getName() {
      return "tellraw";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.tellraw.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 2) {
         throw new WrongUsageException("commands.tellraw.usage", new Object[0]);
      } else {
         EntityPlayerMP var4 = a(var1, var2, var3[0]);
         String var5 = buildString(var3, 1);

         try {
            ITextComponent var6 = ITextComponent.Serializer.jsonToComponent(var5);
            var4.sendMessage(TextComponentUtils.processComponent(var2, var6, var4));
         } catch (JsonParseException var7) {
            throw toSyntaxException(var7);
         }
      }
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, var1.getPlayers()) : Collections.emptyList();
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return var2 == 0;
   }
}
