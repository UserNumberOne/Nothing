package net.minecraft.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.src.MinecraftServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;

public class CommandHelp extends CommandBase {
   private static final String[] seargeSays = new String[]{"Yolo", "/achievement take achievement.understandCommands @p", "Ask for help on twitter", "/deop @p", "Scoreboard deleted, commands blocked", "Contact helpdesk for help", "/testfornoob @p", "/trigger warning", "Oh my god, it's full of stats", "/kill @p[name=!Searge]", "Have you tried turning it off and on again?", "Sorry, no help today"};
   private final Random rand = new Random();

   public String getName() {
      return "help";
   }

   public int getRequiredPermissionLevel() {
      return 0;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.help.usage";
   }

   public List getAliases() {
      return Arrays.asList("?");
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var2 instanceof CommandBlockBaseLogic) {
         var2.sendMessage((new TextComponentString("Searge says: ")).appendText(seargeSays[this.rand.nextInt(seargeSays.length) % seargeSays.length]));
      } else {
         List var4 = this.a(var2, var1);
         boolean var5 = true;
         int var6 = (var4.size() - 1) / 7;
         int var7 = 0;

         try {
            var7 = var3.length == 0 ? 0 : parseInt(var3[0], 1, var6 + 1) - 1;
         } catch (NumberInvalidException var13) {
            Map var9 = this.a(var1);
            ICommand var10 = (ICommand)var9.get(var3[0]);
            if (var10 != null) {
               throw new WrongUsageException(var10.getUsage(var2), new Object[0]);
            }

            if (MathHelper.getInt(var3[0], -1) != -1) {
               throw var13;
            }

            throw new CommandNotFoundException();
         }

         int var8 = Math.min((var7 + 1) * 7, var4.size());
         TextComponentTranslation var15 = new TextComponentTranslation("commands.help.header", new Object[]{var7 + 1, var6 + 1});
         var15.getStyle().setColor(TextFormatting.DARK_GREEN);
         var2.sendMessage(var15);

         for(int var16 = var7 * 7; var16 < var8; ++var16) {
            ICommand var11 = (ICommand)var4.get(var16);
            TextComponentTranslation var12 = new TextComponentTranslation(var11.getUsage(var2), new Object[0]);
            var12.getStyle().setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/" + var11.getName() + " "));
            var2.sendMessage(var12);
         }

         if (var7 == 0 && var2 instanceof EntityPlayer) {
            TextComponentTranslation var17 = new TextComponentTranslation("commands.help.footer", new Object[0]);
            var17.getStyle().setColor(TextFormatting.GREEN);
            var2.sendMessage(var17);
         }

      }
   }

   protected List a(ICommandSender var1, MinecraftServer var2) {
      List var3 = var2.getCommandHandler().getPossibleCommands(var1);
      Collections.sort(var3);
      return var3;
   }

   protected Map a(MinecraftServer var1) {
      return var1.getCommandHandler().getCommands();
   }

   public List tabComplete(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      if (var3.length == 1) {
         Set var5 = this.a(var1).keySet();
         return getListOfStringsMatchingLastWord(var3, (String[])var5.toArray(new String[var5.size()]));
      } else {
         return Collections.emptyList();
      }
   }
}
