package net.minecraft.command;

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.gson.JsonParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.ForgeHooks;
import org.apache.commons.lang3.exception.ExceptionUtils;

public abstract class CommandBase implements ICommand {
   private static ICommandListener commandListener;

   protected static SyntaxErrorException toSyntaxException(JsonParseException var0) {
      Throwable var1 = ExceptionUtils.getRootCause(var0);
      String var2 = "";
      if (var1 != null) {
         var2 = var1.getMessage();
         if (var2.contains("setLenient")) {
            var2 = var2.substring(var2.indexOf("to accept ") + 10);
         }
      }

      return new SyntaxErrorException("commands.tellraw.jsonException", new Object[]{var2});
   }

   protected static NBTTagCompound entityToNBT(Entity var0) {
      NBTTagCompound var1 = var0.writeToNBT(new NBTTagCompound());
      if (var0 instanceof EntityPlayer) {
         ItemStack var2 = ((EntityPlayer)var0).inventory.getCurrentItem();
         if (var2 != null && var2.getItem() != null) {
            var1.setTag("SelectedItem", var2.writeToNBT(new NBTTagCompound()));
         }
      }

      return var1;
   }

   public int getRequiredPermissionLevel() {
      return 4;
   }

   public List getAliases() {
      return Collections.emptyList();
   }

   public boolean checkPermission(MinecraftServer var1, ICommandSender var2) {
      return var2.canUseCommand(this.getRequiredPermissionLevel(), this.getName());
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return Collections.emptyList();
   }

   public static int parseInt(String var0) throws NumberInvalidException {
      try {
         return Integer.parseInt(var0);
      } catch (NumberFormatException var2) {
         throw new NumberInvalidException("commands.generic.num.invalid", new Object[]{var0});
      }
   }

   public static int parseInt(String var0, int var1) throws NumberInvalidException {
      return parseInt(var0, var1, Integer.MAX_VALUE);
   }

   public static int parseInt(String var0, int var1, int var2) throws NumberInvalidException {
      int var3 = parseInt(var0);
      if (var3 < var1) {
         throw new NumberInvalidException("commands.generic.num.tooSmall", new Object[]{var3, var1});
      } else if (var3 > var2) {
         throw new NumberInvalidException("commands.generic.num.tooBig", new Object[]{var3, var2});
      } else {
         return var3;
      }
   }

   public static long parseLong(String var0) throws NumberInvalidException {
      try {
         return Long.parseLong(var0);
      } catch (NumberFormatException var2) {
         throw new NumberInvalidException("commands.generic.num.invalid", new Object[]{var0});
      }
   }

   public static long parseLong(String var0, long var1, long var3) throws NumberInvalidException {
      long var5 = parseLong(var0);
      if (var5 < var1) {
         throw new NumberInvalidException("commands.generic.num.tooSmall", new Object[]{var5, var1});
      } else if (var5 > var3) {
         throw new NumberInvalidException("commands.generic.num.tooBig", new Object[]{var5, var3});
      } else {
         return var5;
      }
   }

   public static BlockPos parseBlockPos(ICommandSender var0, String[] var1, int var2, boolean var3) throws NumberInvalidException {
      BlockPos var4 = var0.getPosition();
      return new BlockPos(parseDouble((double)var4.getX(), var1[var2], -30000000, 30000000, var3), parseDouble((double)var4.getY(), var1[var2 + 1], 0, 256, false), parseDouble((double)var4.getZ(), var1[var2 + 2], -30000000, 30000000, var3));
   }

   public static double parseDouble(String var0) throws NumberInvalidException {
      try {
         double var1 = Double.parseDouble(var0);
         if (!Doubles.isFinite(var1)) {
            throw new NumberInvalidException("commands.generic.num.invalid", new Object[]{var0});
         } else {
            return var1;
         }
      } catch (NumberFormatException var3) {
         throw new NumberInvalidException("commands.generic.num.invalid", new Object[]{var0});
      }
   }

   public static double parseDouble(String var0, double var1) throws NumberInvalidException {
      return parseDouble(var0, var1, Double.MAX_VALUE);
   }

   public static double parseDouble(String var0, double var1, double var3) throws NumberInvalidException {
      double var5 = parseDouble(var0);
      if (var5 < var1) {
         throw new NumberInvalidException("commands.generic.double.tooSmall", new Object[]{var5, var1});
      } else if (var5 > var3) {
         throw new NumberInvalidException("commands.generic.double.tooBig", new Object[]{var5, var3});
      } else {
         return var5;
      }
   }

   public static boolean parseBoolean(String var0) throws CommandException {
      if (!"true".equals(var0) && !"1".equals(var0)) {
         if (!"false".equals(var0) && !"0".equals(var0)) {
            throw new CommandException("commands.generic.boolean.invalid", new Object[]{var0});
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public static EntityPlayerMP getCommandSenderAsPlayer(ICommandSender var0) throws PlayerNotFoundException {
      if (var0 instanceof EntityPlayerMP) {
         return (EntityPlayerMP)var0;
      } else {
         throw new PlayerNotFoundException("You must specify which player you wish to perform this action on.", new Object[0]);
      }
   }

   public static EntityPlayerMP getPlayer(MinecraftServer var0, ICommandSender var1, String var2) throws PlayerNotFoundException {
      EntityPlayerMP var3 = EntitySelector.matchOnePlayer(var1, var2);
      if (var3 == null) {
         try {
            var3 = var0.getPlayerList().getPlayerByUUID(UUID.fromString(var2));
         } catch (IllegalArgumentException var5) {
            ;
         }
      }

      if (var3 == null) {
         var3 = var0.getPlayerList().getPlayerByUsername(var2);
      }

      if (var3 == null) {
         throw new PlayerNotFoundException();
      } else {
         return var3;
      }
   }

   public static Entity getEntity(MinecraftServer var0, ICommandSender var1, String var2) throws EntityNotFoundException {
      return getEntity(var0, var1, var2, Entity.class);
   }

   public static Entity getEntity(MinecraftServer var0, ICommandSender var1, String var2, Class var3) throws EntityNotFoundException {
      Object var4 = EntitySelector.matchOneEntity(var1, var2, var3);
      if (var4 == null) {
         var4 = var0.getPlayerList().getPlayerByUsername(var2);
      }

      if (var4 == null) {
         try {
            UUID var5 = UUID.fromString(var2);
            var4 = var0.getEntityFromUuid(var5);
            if (var4 == null) {
               var4 = var0.getPlayerList().getPlayerByUUID(var5);
            }
         } catch (IllegalArgumentException var6) {
            throw new EntityNotFoundException("commands.generic.entity.invalidUuid", new Object[0]);
         }
      }

      if (var4 != null && var3.isAssignableFrom(var4.getClass())) {
         return (Entity)var4;
      } else {
         throw new EntityNotFoundException();
      }
   }

   public static List getEntityList(MinecraftServer var0, ICommandSender var1, String var2) throws EntityNotFoundException {
      return (List)(EntitySelector.hasArguments(var2) ? EntitySelector.matchEntities(var1, var2, Entity.class) : Lists.newArrayList(new Entity[]{getEntity(var0, var1, var2)}));
   }

   public static String getPlayerName(MinecraftServer var0, ICommandSender var1, String var2) throws PlayerNotFoundException {
      try {
         return getPlayer(var0, var1, var2).getName();
      } catch (PlayerNotFoundException var4) {
         if (var2 != null && !var2.startsWith("@")) {
            return var2;
         } else {
            throw var4;
         }
      }
   }

   public static String getEntityName(MinecraftServer var0, ICommandSender var1, String var2) throws EntityNotFoundException {
      try {
         return getPlayer(var0, var1, var2).getName();
      } catch (PlayerNotFoundException var6) {
         try {
            return getEntity(var0, var1, var2).getCachedUniqueIdString();
         } catch (EntityNotFoundException var5) {
            if (var2 != null && !var2.startsWith("@")) {
               return var2;
            } else {
               throw var5;
            }
         }
      }
   }

   public static ITextComponent getChatComponentFromNthArg(ICommandSender var0, String[] var1, int var2) throws CommandException, PlayerNotFoundException {
      return getChatComponentFromNthArg(var0, var1, var2, false);
   }

   public static ITextComponent getChatComponentFromNthArg(ICommandSender var0, String[] var1, int var2, boolean var3) throws PlayerNotFoundException {
      TextComponentString var4 = new TextComponentString("");

      for(int var5 = var2; var5 < var1.length; ++var5) {
         if (var5 > var2) {
            var4.appendText(" ");
         }

         ITextComponent var6 = ForgeHooks.newChatWithLinks(var1[var5]);
         if (var3) {
            ITextComponent var7 = EntitySelector.matchEntitiesToTextComponent(var0, var1[var5]);
            if (var7 == null) {
               if (EntitySelector.hasArguments(var1[var5])) {
                  throw new PlayerNotFoundException();
               }
            } else {
               var6 = var7;
            }
         }

         var4.appendSibling(var6);
      }

      return var4;
   }

   public static String buildString(String[] var0, int var1) {
      StringBuilder var2 = new StringBuilder();

      for(int var3 = var1; var3 < var0.length; ++var3) {
         if (var3 > var1) {
            var2.append(" ");
         }

         String var4 = var0[var3];
         var2.append(var4);
      }

      return var2.toString();
   }

   public static CommandBase.CoordinateArg parseCoordinate(double var0, String var2, boolean var3) throws NumberInvalidException {
      return parseCoordinate(var0, var2, -30000000, 30000000, var3);
   }

   public static CommandBase.CoordinateArg parseCoordinate(double var0, String var2, int var3, int var4, boolean var5) throws NumberInvalidException {
      boolean var6 = var2.startsWith("~");
      if (var6 && Double.isNaN(var0)) {
         throw new NumberInvalidException("commands.generic.num.invalid", new Object[]{var0});
      } else {
         double var7 = 0.0D;
         if (!var6 || var2.length() > 1) {
            boolean var9 = var2.contains(".");
            if (var6) {
               var2 = var2.substring(1);
            }

            var7 += parseDouble(var2);
            if (!var9 && !var6 && var5) {
               var7 += 0.5D;
            }
         }

         double var11 = var7 + (var6 ? var0 : 0.0D);
         if (var3 != 0 || var4 != 0) {
            if (var11 < (double)var3) {
               throw new NumberInvalidException("commands.generic.double.tooSmall", new Object[]{var11, var3});
            }

            if (var11 > (double)var4) {
               throw new NumberInvalidException("commands.generic.double.tooBig", new Object[]{var11, var4});
            }
         }

         return new CommandBase.CoordinateArg(var11, var7, var6);
      }
   }

   public static double parseDouble(double var0, String var2, boolean var3) throws NumberInvalidException {
      return parseDouble(var0, var2, -30000000, 30000000, var3);
   }

   public static double parseDouble(double var0, String var2, int var3, int var4, boolean var5) throws NumberInvalidException {
      boolean var6 = var2.startsWith("~");
      if (var6 && Double.isNaN(var0)) {
         throw new NumberInvalidException("commands.generic.num.invalid", new Object[]{var0});
      } else {
         double var7 = var6 ? var0 : 0.0D;
         if (!var6 || var2.length() > 1) {
            boolean var9 = var2.contains(".");
            if (var6) {
               var2 = var2.substring(1);
            }

            var7 += parseDouble(var2);
            if (!var9 && !var6 && var5) {
               var7 += 0.5D;
            }
         }

         if (var3 != 0 || var4 != 0) {
            if (var7 < (double)var3) {
               throw new NumberInvalidException("commands.generic.double.tooSmall", new Object[]{var7, var3});
            }

            if (var7 > (double)var4) {
               throw new NumberInvalidException("commands.generic.double.tooBig", new Object[]{var7, var4});
            }
         }

         return var7;
      }
   }

   public static Item getItemByText(ICommandSender var0, String var1) throws NumberInvalidException {
      ResourceLocation var2 = new ResourceLocation(var1);
      Item var3 = (Item)Item.REGISTRY.getObject(var2);
      if (var3 == null) {
         throw new NumberInvalidException("commands.give.item.notFound", new Object[]{var2});
      } else {
         return var3;
      }
   }

   public static Block getBlockByText(ICommandSender var0, String var1) throws NumberInvalidException {
      ResourceLocation var2 = new ResourceLocation(var1);
      if (!Block.REGISTRY.containsKey(var2)) {
         throw new NumberInvalidException("commands.give.block.notFound", new Object[]{var2});
      } else {
         Block var3 = (Block)Block.REGISTRY.getObject(var2);
         if (var3 == null) {
            throw new NumberInvalidException("commands.give.block.notFound", new Object[]{var2});
         } else {
            return var3;
         }
      }
   }

   public static String joinNiceString(Object[] var0) {
      StringBuilder var1 = new StringBuilder();

      for(int var2 = 0; var2 < var0.length; ++var2) {
         String var3 = var0[var2].toString();
         if (var2 > 0) {
            if (var2 == var0.length - 1) {
               var1.append(" and ");
            } else {
               var1.append(", ");
            }
         }

         var1.append(var3);
      }

      return var1.toString();
   }

   public static ITextComponent join(List var0) {
      TextComponentString var1 = new TextComponentString("");

      for(int var2 = 0; var2 < var0.size(); ++var2) {
         if (var2 > 0) {
            if (var2 == var0.size() - 1) {
               var1.appendText(" and ");
            } else if (var2 > 0) {
               var1.appendText(", ");
            }
         }

         var1.appendSibling((ITextComponent)var0.get(var2));
      }

      return var1;
   }

   public static String joinNiceStringFromCollection(Collection var0) {
      return joinNiceString(var0.toArray(new String[var0.size()]));
   }

   public static List getTabCompletionCoordinate(String[] var0, int var1, @Nullable BlockPos var2) {
      if (var2 == null) {
         return Lists.newArrayList(new String[]{"~"});
      } else {
         int var3 = var0.length - 1;
         String var4;
         if (var3 == var1) {
            var4 = Integer.toString(var2.getX());
         } else if (var3 == var1 + 1) {
            var4 = Integer.toString(var2.getY());
         } else {
            if (var3 != var1 + 2) {
               return Collections.emptyList();
            }

            var4 = Integer.toString(var2.getZ());
         }

         return Lists.newArrayList(new String[]{var4});
      }
   }

   @Nullable
   public static List getTabCompletionCoordinateXZ(String[] var0, int var1, @Nullable BlockPos var2) {
      if (var2 == null) {
         return Lists.newArrayList(new String[]{"~"});
      } else {
         int var3 = var0.length - 1;
         String var4;
         if (var3 == var1) {
            var4 = Integer.toString(var2.getX());
         } else {
            if (var3 != var1 + 1) {
               return null;
            }

            var4 = Integer.toString(var2.getZ());
         }

         return Lists.newArrayList(new String[]{var4});
      }
   }

   public static boolean doesStringStartWith(String var0, String var1) {
      return var1.regionMatches(true, 0, var0, 0, var0.length());
   }

   public static List getListOfStringsMatchingLastWord(String[] var0, String... var1) {
      return getListOfStringsMatchingLastWord(var0, Arrays.asList(var1));
   }

   public static List getListOfStringsMatchingLastWord(String[] var0, Collection var1) {
      String var2 = var0[var0.length - 1];
      ArrayList var3 = Lists.newArrayList();
      if (!var1.isEmpty()) {
         for(String var5 : Iterables.transform(var1, Functions.toStringFunction())) {
            if (doesStringStartWith(var2, var5)) {
               var3.add(var5);
            }
         }

         if (var3.isEmpty()) {
            for(Object var7 : var1) {
               if (var7 instanceof ResourceLocation && doesStringStartWith(var2, ((ResourceLocation)var7).getResourcePath())) {
                  var3.add(String.valueOf(var7));
               }
            }
         }
      }

      return var3;
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return false;
   }

   public static void notifyCommandListener(ICommandSender var0, ICommand var1, String var2, Object... var3) {
      notifyCommandListener(var0, var1, 0, var2, var3);
   }

   public static void notifyCommandListener(ICommandSender var0, ICommand var1, int var2, String var3, Object... var4) {
      if (commandListener != null) {
         commandListener.notifyListener(var0, var1, var2, var3, var4);
      }

   }

   public static void setCommandListener(ICommandListener var0) {
      commandListener = var0;
   }

   public int compareTo(ICommand var1) {
      return this.getName().compareTo(var1.getName());
   }

   public static class CoordinateArg {
      private final double result;
      private final double amount;
      private final boolean isRelative;

      protected CoordinateArg(double var1, double var3, boolean var5) {
         this.result = var1;
         this.amount = var3;
         this.isRelative = var5;
      }

      public double getResult() {
         return this.result;
      }

      public double getAmount() {
         return this.amount;
      }

      public boolean isRelative() {
         return this.isRelative;
      }
   }
}
