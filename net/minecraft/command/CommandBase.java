package net.minecraft.command;

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.google.gson.JsonParseException;
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
      Throwable throwable = ExceptionUtils.getRootCause(e);
      String s = "";
      if (throwable != null) {
         s = throwable.getMessage();
         if (s.contains("setLenient")) {
            s = s.substring(s.indexOf("to accept ") + 10);
         }
      }

      return new SyntaxErrorException("commands.tellraw.jsonException", new Object[]{s});
   }

   protected static NBTTagCompound entityToNBT(Entity var0) {
      NBTTagCompound nbttagcompound = theEntity.writeToNBT(new NBTTagCompound());
      if (theEntity instanceof EntityPlayer) {
         ItemStack itemstack = ((EntityPlayer)theEntity).inventory.getCurrentItem();
         if (itemstack != null && itemstack.getItem() != null) {
            nbttagcompound.setTag("SelectedItem", itemstack.writeToNBT(new NBTTagCompound()));
         }
      }

      return nbttagcompound;
   }

   public int getRequiredPermissionLevel() {
      return 4;
   }

   public List getAliases() {
      return Collections.emptyList();
   }

   public boolean checkPermission(MinecraftServer var1, ICommandSender var2) {
      return sender.canUseCommand(this.getRequiredPermissionLevel(), this.getName());
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return Collections.emptyList();
   }

   public static int parseInt(String var0) throws NumberInvalidException {
      try {
         return Integer.parseInt(input);
      } catch (NumberFormatException var2) {
         throw new NumberInvalidException("commands.generic.num.invalid", new Object[]{input});
      }
   }

   public static int parseInt(String var0, int var1) throws NumberInvalidException {
      return parseInt(input, min, Integer.MAX_VALUE);
   }

   public static int parseInt(String var0, int var1, int var2) throws NumberInvalidException {
      int i = parseInt(input);
      if (i < min) {
         throw new NumberInvalidException("commands.generic.num.tooSmall", new Object[]{i, min});
      } else if (i > max) {
         throw new NumberInvalidException("commands.generic.num.tooBig", new Object[]{i, max});
      } else {
         return i;
      }
   }

   public static long parseLong(String var0) throws NumberInvalidException {
      try {
         return Long.parseLong(input);
      } catch (NumberFormatException var2) {
         throw new NumberInvalidException("commands.generic.num.invalid", new Object[]{input});
      }
   }

   public static long parseLong(String var0, long var1, long var3) throws NumberInvalidException {
      long i = parseLong(input);
      if (i < min) {
         throw new NumberInvalidException("commands.generic.num.tooSmall", new Object[]{i, min});
      } else if (i > max) {
         throw new NumberInvalidException("commands.generic.num.tooBig", new Object[]{i, max});
      } else {
         return i;
      }
   }

   public static BlockPos parseBlockPos(ICommandSender var0, String[] var1, int var2, boolean var3) throws NumberInvalidException {
      BlockPos blockpos = sender.getPosition();
      return new BlockPos(parseDouble((double)blockpos.getX(), args[startIndex], -30000000, 30000000, centerBlock), parseDouble((double)blockpos.getY(), args[startIndex + 1], 0, 256, false), parseDouble((double)blockpos.getZ(), args[startIndex + 2], -30000000, 30000000, centerBlock));
   }

   public static double parseDouble(String var0) throws NumberInvalidException {
      try {
         double d0 = Double.parseDouble(input);
         if (!Doubles.isFinite(d0)) {
            throw new NumberInvalidException("commands.generic.num.invalid", new Object[]{input});
         } else {
            return d0;
         }
      } catch (NumberFormatException var3) {
         throw new NumberInvalidException("commands.generic.num.invalid", new Object[]{input});
      }
   }

   public static double parseDouble(String var0, double var1) throws NumberInvalidException {
      return parseDouble(input, min, Double.MAX_VALUE);
   }

   public static double parseDouble(String var0, double var1, double var3) throws NumberInvalidException {
      double d0 = parseDouble(input);
      if (d0 < min) {
         throw new NumberInvalidException("commands.generic.double.tooSmall", new Object[]{d0, min});
      } else if (d0 > max) {
         throw new NumberInvalidException("commands.generic.double.tooBig", new Object[]{d0, max});
      } else {
         return d0;
      }
   }

   public static boolean parseBoolean(String var0) throws CommandException {
      if (!"true".equals(input) && !"1".equals(input)) {
         if (!"false".equals(input) && !"0".equals(input)) {
            throw new CommandException("commands.generic.boolean.invalid", new Object[]{input});
         } else {
            return false;
         }
      } else {
         return true;
      }
   }

   public static EntityPlayerMP getCommandSenderAsPlayer(ICommandSender var0) throws PlayerNotFoundException {
      if (sender instanceof EntityPlayerMP) {
         return (EntityPlayerMP)sender;
      } else {
         throw new PlayerNotFoundException("You must specify which player you wish to perform this action on.", new Object[0]);
      }
   }

   public static EntityPlayerMP getPlayer(MinecraftServer var0, ICommandSender var1, String var2) throws PlayerNotFoundException {
      EntityPlayerMP entityplayermp = EntitySelector.matchOnePlayer(sender, target);
      if (entityplayermp == null) {
         try {
            entityplayermp = server.getPlayerList().getPlayerByUUID(UUID.fromString(target));
         } catch (IllegalArgumentException var5) {
            ;
         }
      }

      if (entityplayermp == null) {
         entityplayermp = server.getPlayerList().getPlayerByUsername(target);
      }

      if (entityplayermp == null) {
         throw new PlayerNotFoundException();
      } else {
         return entityplayermp;
      }
   }

   public static Entity getEntity(MinecraftServer var0, ICommandSender var1, String var2) throws EntityNotFoundException {
      return getEntity(server, sender, target, Entity.class);
   }

   public static Entity getEntity(MinecraftServer var0, ICommandSender var1, String var2, Class var3) throws EntityNotFoundException {
      Entity entity = EntitySelector.matchOneEntity(sender, target, targetClass);
      if (entity == null) {
         entity = server.getPlayerList().getPlayerByUsername(target);
      }

      if (entity == null) {
         try {
            UUID uuid = UUID.fromString(target);
            entity = server.getEntityFromUuid(uuid);
            if (entity == null) {
               entity = server.getPlayerList().getPlayerByUUID(uuid);
            }
         } catch (IllegalArgumentException var6) {
            throw new EntityNotFoundException("commands.generic.entity.invalidUuid", new Object[0]);
         }
      }

      if (entity != null && targetClass.isAssignableFrom(entity.getClass())) {
         return entity;
      } else {
         throw new EntityNotFoundException();
      }
   }

   public static List getEntityList(MinecraftServer var0, ICommandSender var1, String var2) throws EntityNotFoundException {
      return (List)(EntitySelector.hasArguments(target) ? EntitySelector.matchEntities(sender, target, Entity.class) : Lists.newArrayList(new Entity[]{getEntity(server, sender, target)}));
   }

   public static String getPlayerName(MinecraftServer var0, ICommandSender var1, String var2) throws PlayerNotFoundException {
      try {
         return getPlayer(server, sender, target).getName();
      } catch (PlayerNotFoundException var4) {
         if (target != null && !target.startsWith("@")) {
            return target;
         } else {
            throw var4;
         }
      }
   }

   public static String getEntityName(MinecraftServer var0, ICommandSender var1, String var2) throws EntityNotFoundException {
      try {
         return getPlayer(server, sender, target).getName();
      } catch (PlayerNotFoundException var6) {
         try {
            return getEntity(server, sender, target).getCachedUniqueIdString();
         } catch (EntityNotFoundException var5) {
            if (target != null && !target.startsWith("@")) {
               return target;
            } else {
               throw var5;
            }
         }
      }
   }

   public static ITextComponent getChatComponentFromNthArg(ICommandSender var0, String[] var1, int var2) throws CommandException, PlayerNotFoundException {
      return getChatComponentFromNthArg(sender, args, index, false);
   }

   public static ITextComponent getChatComponentFromNthArg(ICommandSender var0, String[] var1, int var2, boolean var3) throws PlayerNotFoundException {
      ITextComponent itextcomponent = new TextComponentString("");

      for(int i = index; i < args.length; ++i) {
         if (i > index) {
            itextcomponent.appendText(" ");
         }

         ITextComponent itextcomponent1 = ForgeHooks.newChatWithLinks(args[i]);
         if (p_147176_3_) {
            ITextComponent itextcomponent2 = EntitySelector.matchEntitiesToTextComponent(sender, args[i]);
            if (itextcomponent2 == null) {
               if (EntitySelector.hasArguments(args[i])) {
                  throw new PlayerNotFoundException();
               }
            } else {
               itextcomponent1 = itextcomponent2;
            }
         }

         itextcomponent.appendSibling(itextcomponent1);
      }

      return itextcomponent;
   }

   public static String buildString(String[] var0, int var1) {
      StringBuilder stringbuilder = new StringBuilder();

      for(int i = startPos; i < args.length; ++i) {
         if (i > startPos) {
            stringbuilder.append(" ");
         }

         String s = args[i];
         stringbuilder.append(s);
      }

      return stringbuilder.toString();
   }

   public static CommandBase.CoordinateArg parseCoordinate(double var0, String var2, boolean var3) throws NumberInvalidException {
      return parseCoordinate(base, selectorArg, -30000000, 30000000, centerBlock);
   }

   public static CommandBase.CoordinateArg parseCoordinate(double var0, String var2, int var3, int var4, boolean var5) throws NumberInvalidException {
      boolean flag = selectorArg.startsWith("~");
      if (flag && Double.isNaN(base)) {
         throw new NumberInvalidException("commands.generic.num.invalid", new Object[]{base});
      } else {
         double d0 = 0.0D;
         if (!flag || selectorArg.length() > 1) {
            boolean flag1 = selectorArg.contains(".");
            if (flag) {
               selectorArg = selectorArg.substring(1);
            }

            d0 += parseDouble(selectorArg);
            if (!flag1 && !flag && centerBlock) {
               d0 += 0.5D;
            }
         }

         double d1 = d0 + (flag ? base : 0.0D);
         if (min != 0 || max != 0) {
            if (d1 < (double)min) {
               throw new NumberInvalidException("commands.generic.double.tooSmall", new Object[]{d1, min});
            }

            if (d1 > (double)max) {
               throw new NumberInvalidException("commands.generic.double.tooBig", new Object[]{d1, max});
            }
         }

         return new CommandBase.CoordinateArg(d1, d0, flag);
      }
   }

   public static double parseDouble(double var0, String var2, boolean var3) throws NumberInvalidException {
      return parseDouble(base, input, -30000000, 30000000, centerBlock);
   }

   public static double parseDouble(double var0, String var2, int var3, int var4, boolean var5) throws NumberInvalidException {
      boolean flag = input.startsWith("~");
      if (flag && Double.isNaN(base)) {
         throw new NumberInvalidException("commands.generic.num.invalid", new Object[]{base});
      } else {
         double d0 = flag ? base : 0.0D;
         if (!flag || input.length() > 1) {
            boolean flag1 = input.contains(".");
            if (flag) {
               input = input.substring(1);
            }

            d0 += parseDouble(input);
            if (!flag1 && !flag && centerBlock) {
               d0 += 0.5D;
            }
         }

         if (min != 0 || max != 0) {
            if (d0 < (double)min) {
               throw new NumberInvalidException("commands.generic.double.tooSmall", new Object[]{d0, min});
            }

            if (d0 > (double)max) {
               throw new NumberInvalidException("commands.generic.double.tooBig", new Object[]{d0, max});
            }
         }

         return d0;
      }
   }

   public static Item getItemByText(ICommandSender var0, String var1) throws NumberInvalidException {
      ResourceLocation resourcelocation = new ResourceLocation(id);
      Item item = (Item)Item.REGISTRY.getObject(resourcelocation);
      if (item == null) {
         throw new NumberInvalidException("commands.give.item.notFound", new Object[]{resourcelocation});
      } else {
         return item;
      }
   }

   public static Block getBlockByText(ICommandSender var0, String var1) throws NumberInvalidException {
      ResourceLocation resourcelocation = new ResourceLocation(id);
      if (!Block.REGISTRY.containsKey(resourcelocation)) {
         throw new NumberInvalidException("commands.give.block.notFound", new Object[]{resourcelocation});
      } else {
         Block block = (Block)Block.REGISTRY.getObject(resourcelocation);
         if (block == null) {
            throw new NumberInvalidException("commands.give.block.notFound", new Object[]{resourcelocation});
         } else {
            return block;
         }
      }
   }

   public static String joinNiceString(Object[] var0) {
      StringBuilder stringbuilder = new StringBuilder();

      for(int i = 0; i < elements.length; ++i) {
         String s = elements[i].toString();
         if (i > 0) {
            if (i == elements.length - 1) {
               stringbuilder.append(" and ");
            } else {
               stringbuilder.append(", ");
            }
         }

         stringbuilder.append(s);
      }

      return stringbuilder.toString();
   }

   public static ITextComponent join(List var0) {
      ITextComponent itextcomponent = new TextComponentString("");

      for(int i = 0; i < components.size(); ++i) {
         if (i > 0) {
            if (i == components.size() - 1) {
               itextcomponent.appendText(" and ");
            } else if (i > 0) {
               itextcomponent.appendText(", ");
            }
         }

         itextcomponent.appendSibling((ITextComponent)components.get(i));
      }

      return itextcomponent;
   }

   public static String joinNiceStringFromCollection(Collection var0) {
      return joinNiceString(strings.toArray(new String[strings.size()]));
   }

   public static List getTabCompletionCoordinate(String[] var0, int var1, @Nullable BlockPos var2) {
      if (pos == null) {
         return Lists.newArrayList(new String[]{"~"});
      } else {
         int i = inputArgs.length - 1;
         String s;
         if (i == index) {
            s = Integer.toString(pos.getX());
         } else if (i == index + 1) {
            s = Integer.toString(pos.getY());
         } else {
            if (i != index + 2) {
               return Collections.emptyList();
            }

            s = Integer.toString(pos.getZ());
         }

         return Lists.newArrayList(new String[]{s});
      }
   }

   @Nullable
   public static List getTabCompletionCoordinateXZ(String[] var0, int var1, @Nullable BlockPos var2) {
      if (lookedPos == null) {
         return Lists.newArrayList(new String[]{"~"});
      } else {
         int i = inputArgs.length - 1;
         String s;
         if (i == index) {
            s = Integer.toString(lookedPos.getX());
         } else {
            if (i != index + 1) {
               return null;
            }

            s = Integer.toString(lookedPos.getZ());
         }

         return Lists.newArrayList(new String[]{s});
      }
   }

   public static boolean doesStringStartWith(String var0, String var1) {
      return region.regionMatches(true, 0, original, 0, original.length());
   }

   public static List getListOfStringsMatchingLastWord(String[] var0, String... var1) {
      return getListOfStringsMatchingLastWord(args, Arrays.asList(possibilities));
   }

   public static List getListOfStringsMatchingLastWord(String[] var0, Collection var1) {
      String s = inputArgs[inputArgs.length - 1];
      List list = Lists.newArrayList();
      if (!possibleCompletions.isEmpty()) {
         for(String s1 : Iterables.transform(possibleCompletions, Functions.toStringFunction())) {
            if (doesStringStartWith(s, s1)) {
               list.add(s1);
            }
         }

         if (list.isEmpty()) {
            for(Object object : possibleCompletions) {
               if (object instanceof ResourceLocation && doesStringStartWith(s, ((ResourceLocation)object).getResourcePath())) {
                  list.add(String.valueOf(object));
               }
            }
         }
      }

      return list;
   }

   public boolean isUsernameIndex(String[] var1, int var2) {
      return false;
   }

   public static void notifyCommandListener(ICommandSender var0, ICommand var1, String var2, Object... var3) {
      notifyCommandListener(sender, command, 0, translationKey, translationArgs);
   }

   public static void notifyCommandListener(ICommandSender var0, ICommand var1, int var2, String var3, Object... var4) {
      if (commandListener != null) {
         commandListener.notifyListener(sender, command, flags, translationKey, translationArgs);
      }

   }

   public static void setCommandListener(ICommandListener var0) {
      commandListener = listener;
   }

   public int compareTo(ICommand var1) {
      return this.getName().compareTo(p_compareTo_1_.getName());
   }

   public static class CoordinateArg {
      private final double result;
      private final double amount;
      private final boolean isRelative;

      protected CoordinateArg(double var1, double var3, boolean var5) {
         this.result = resultIn;
         this.amount = amountIn;
         this.isRelative = relative;
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
