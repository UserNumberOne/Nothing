package net.minecraft.command;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;

public class EntitySelector {
   private static final Pattern TOKEN_PATTERN = Pattern.compile("^@([pare])(?:\\[([\\w\\.:=,!-]*)\\])?$");
   private static final Pattern INT_LIST_PATTERN = Pattern.compile("\\G([-!]?[\\w-]*)(?:$|,)");
   private static final Pattern KEY_VALUE_LIST_PATTERN = Pattern.compile("\\G([\\w:]+)=([-!]?[\\w\\.-]*)(?:$|,)");
   private static final Set WORLD_BINDING_ARGS = Sets.newHashSet(new String[]{"x", "y", "z", "dx", "dy", "dz", "rm", "r"});

   @Nullable
   public static EntityPlayerMP matchOnePlayer(ICommandSender var0, String var1) {
      return (EntityPlayerMP)matchOneEntity(var0, var1, EntityPlayerMP.class);
   }

   @Nullable
   public static Entity matchOneEntity(ICommandSender var0, String var1, Class var2) {
      List var3 = matchEntities(var0, var1, var2);
      return var3.size() == 1 ? (Entity)var3.get(0) : null;
   }

   @Nullable
   public static ITextComponent matchEntitiesToTextComponent(ICommandSender var0, String var1) {
      List var2 = matchEntities(var0, var1, Entity.class);
      if (var2.isEmpty()) {
         return null;
      } else {
         ArrayList var3 = Lists.newArrayList();

         for(Entity var5 : var2) {
            var3.add(var5.getDisplayName());
         }

         return CommandBase.join(var3);
      }
   }

   public static List matchEntities(ICommandSender var0, String var1, Class var2) {
      Matcher var3 = TOKEN_PATTERN.matcher(var1);
      if (var3.matches() && var0.canUseCommand(1, "@")) {
         Map var4 = getArgumentMap(var3.group(2));
         if (!isEntityTypeValid(var0, var4)) {
            return Collections.emptyList();
         } else {
            String var5 = var3.group(1);
            BlockPos var6 = getBlockPosFromArguments(var4, var0.getPosition());
            Vec3d var7 = getPosFromArguments(var4, var0.getPositionVector());
            List var8 = getWorlds(var0, var4);
            ArrayList var9 = Lists.newArrayList();

            for(World var11 : var8) {
               if (var11 != null) {
                  ArrayList var12 = Lists.newArrayList();
                  var12.addAll(getTypePredicates(var4, var5));
                  var12.addAll(getXpLevelPredicates(var4));
                  var12.addAll(getGamemodePredicates(var4));
                  var12.addAll(getTeamPredicates(var4));
                  var12.addAll(getScorePredicates(var0, var4));
                  var12.addAll(getNamePredicates(var4));
                  var12.addAll(getTagPredicates(var4));
                  var12.addAll(getRadiusPredicates(var4, var7));
                  var12.addAll(getRotationsPredicates(var4));
                  var12.addAll(ForgeEventFactory.gatherEntitySelectors(var4, var5, var0, var7));
                  var9.addAll(filterResults(var4, var2, var12, var5, var11, var6));
               }
            }

            return getEntitiesFromPredicates(var9, var4, var0, var2, var5, var7);
         }
      } else {
         return Collections.emptyList();
      }
   }

   private static List getWorlds(ICommandSender var0, Map var1) {
      ArrayList var2 = Lists.newArrayList();
      if (hasArgument(var1)) {
         var2.add(var0.getEntityWorld());
      } else {
         Collections.addAll(var2, var0.getServer().worlds);
      }

      return var2;
   }

   private static boolean isEntityTypeValid(ICommandSender var0, Map var1) {
      String var2 = getArgument(var1, "type");
      var2 = var2 != null && var2.startsWith("!") ? var2.substring(1) : var2;
      if (var2 != null && !EntityList.isStringValidEntityName(var2)) {
         TextComponentTranslation var3 = new TextComponentTranslation("commands.generic.entity.invalidType", new Object[]{var2});
         var3.getStyle().setColor(TextFormatting.RED);
         var0.sendMessage(var3);
         return false;
      } else {
         return true;
      }
   }

   private static List getTypePredicates(Map var0, String var1) {
      ArrayList var2 = Lists.newArrayList();
      final String var3 = getArgument(var0, "type");
      final boolean var4 = var3 != null && var3.startsWith("!");
      if (var4) {
         var3 = var3.substring(1);
      }

      boolean var5 = !var1.equals("e");
      boolean var6 = var1.equals("r") && var3 != null;
      if ((var3 == null || !var1.equals("e")) && !var6) {
         if (var5) {
            var2.add(new Predicate() {
               public boolean apply(@Nullable Entity var1) {
                  return var1 instanceof EntityPlayer;
               }
            });
         }
      } else {
         var2.add(new Predicate() {
            public boolean apply(@Nullable Entity var1) {
               return EntityList.isStringEntityName(var1, var3) != var4;
            }
         });
      }

      return var2;
   }

   private static List getXpLevelPredicates(Map var0) {
      ArrayList var1 = Lists.newArrayList();
      final int var2 = getInt(var0, "lm", -1);
      final int var3 = getInt(var0, "l", -1);
      if (var2 > -1 || var3 > -1) {
         var1.add(new Predicate() {
            public boolean apply(@Nullable Entity var1) {
               if (!(var1 instanceof EntityPlayerMP)) {
                  return false;
               } else {
                  EntityPlayerMP var2x = (EntityPlayerMP)var1;
                  return (var2 <= -1 || var2x.experienceLevel >= var2) && (var3 <= -1 || var2x.experienceLevel <= var3);
               }
            }
         });
      }

      return var1;
   }

   private static List getGamemodePredicates(Map var0) {
      ArrayList var1 = Lists.newArrayList();
      String var2 = getArgument(var0, "m");
      if (var2 == null) {
         return var1;
      } else {
         final boolean var3 = var2.startsWith("!");
         if (var3) {
            var2 = var2.substring(1);
         }

         final GameType var4;
         try {
            int var5 = Integer.parseInt(var2);
            var4 = GameType.parseGameTypeWithDefault(var5, GameType.NOT_SET);
         } catch (Throwable var6) {
            var4 = GameType.parseGameTypeWithDefault(var2, GameType.NOT_SET);
         }

         var1.add(new Predicate() {
            public boolean apply(@Nullable Entity var1) {
               if (!(var1 instanceof EntityPlayerMP)) {
                  return false;
               } else {
                  EntityPlayerMP var2 = (EntityPlayerMP)var1;
                  GameType var3x = var2.interactionManager.getGameType();
                  return var3 ? var3x != var4 : var3x == var4;
               }
            }
         });
         return var1;
      }
   }

   private static List getTeamPredicates(Map var0) {
      ArrayList var1 = Lists.newArrayList();
      final String var2 = getArgument(var0, "team");
      final boolean var3 = var2 != null && var2.startsWith("!");
      if (var3) {
         var2 = var2.substring(1);
      }

      if (var2 != null) {
         var1.add(new Predicate() {
            public boolean apply(@Nullable Entity var1) {
               if (!(var1 instanceof EntityLivingBase)) {
                  return false;
               } else {
                  EntityLivingBase var2x = (EntityLivingBase)var1;
                  Team var3x = var2x.getTeam();
                  String var4 = var3x == null ? "" : var3x.getRegisteredName();
                  return var4.equals(var2) != var3;
               }
            }
         });
      }

      return var1;
   }

   private static List getScorePredicates(final ICommandSender var0, Map var1) {
      final Map var2 = getScoreMap(var1);
      return (List)(var2.isEmpty() ? Collections.emptyList() : Lists.newArrayList(new Predicate[]{new Predicate() {
         public boolean apply(@Nullable Entity var1) {
            if (var1 == null) {
               return false;
            } else {
               Scoreboard var2x = var0.getServer().worldServerForDimension(0).getScoreboard();

               for(Entry var4 : var2.entrySet()) {
                  String var5 = (String)var4.getKey();
                  boolean var6 = false;
                  if (var5.endsWith("_min") && var5.length() > 4) {
                     var6 = true;
                     var5 = var5.substring(0, var5.length() - 4);
                  }

                  ScoreObjective var7 = var2x.getObjective(var5);
                  if (var7 == null) {
                     return false;
                  }

                  String var8 = var1 instanceof EntityPlayerMP ? var1.getName() : var1.getCachedUniqueIdString();
                  if (!var2x.entityHasObjective(var8, var7)) {
                     return false;
                  }

                  Score var9 = var2x.getOrCreateScore(var8, var7);
                  int var10 = var9.getScorePoints();
                  if (var10 < ((Integer)var4.getValue()).intValue() && var6) {
                     return false;
                  }

                  if (var10 > ((Integer)var4.getValue()).intValue() && !var6) {
                     return false;
                  }
               }

               return true;
            }
         }
      }}));
   }

   private static List getNamePredicates(Map var0) {
      ArrayList var1 = Lists.newArrayList();
      final String var2 = getArgument(var0, "name");
      final boolean var3 = var2 != null && var2.startsWith("!");
      if (var3) {
         var2 = var2.substring(1);
      }

      if (var2 != null) {
         var1.add(new Predicate() {
            public boolean apply(@Nullable Entity var1) {
               return var1 != null && var1.getName().equals(var2) != var3;
            }
         });
      }

      return var1;
   }

   private static List getTagPredicates(Map var0) {
      ArrayList var1 = Lists.newArrayList();
      final String var2 = getArgument(var0, "tag");
      final boolean var3 = var2 != null && var2.startsWith("!");
      if (var3) {
         var2 = var2.substring(1);
      }

      if (var2 != null) {
         var1.add(new Predicate() {
            public boolean apply(@Nullable Entity var1) {
               return var1 == null ? false : ("".equals(var2) ? var1.getTags().isEmpty() != var3 : var1.getTags().contains(var2) != var3);
            }
         });
      }

      return var1;
   }

   private static List getRadiusPredicates(Map var0, final Vec3d var1) {
      double var2 = (double)getInt(var0, "rm", -1);
      double var4 = (double)getInt(var0, "r", -1);
      final boolean var6 = var2 < -0.5D;
      final boolean var7 = var4 < -0.5D;
      if (var6 && var7) {
         return Collections.emptyList();
      } else {
         double var8 = Math.max(var2, 1.0E-4D);
         final double var10 = var8 * var8;
         double var12 = Math.max(var4, 1.0E-4D);
         final double var14 = var12 * var12;
         return Lists.newArrayList(new Predicate[]{new Predicate() {
            public boolean apply(@Nullable Entity var1x) {
               if (var1x == null) {
                  return false;
               } else {
                  double var2 = var1.squareDistanceTo(var1x.posX, var1x.posY, var1x.posZ);
                  return (var6 || var2 >= var10) && (var7 || var2 <= var14);
               }
            }
         }});
      }
   }

   private static List getRotationsPredicates(Map var0) {
      ArrayList var1 = Lists.newArrayList();
      if (var0.containsKey("rym") || var0.containsKey("ry")) {
         final int var2 = MathHelper.clampAngle(getInt(var0, "rym", 0));
         final int var3 = MathHelper.clampAngle(getInt(var0, "ry", 359));
         var1.add(new Predicate() {
            public boolean apply(@Nullable Entity var1) {
               if (var1 == null) {
                  return false;
               } else {
                  int var2x = MathHelper.clampAngle(MathHelper.floor(var1.rotationYaw));
                  return var2 > var3 ? var2x >= var2 || var2x <= var3 : var2x >= var2 && var2x <= var3;
               }
            }
         });
      }

      if (var0.containsKey("rxm") || var0.containsKey("rx")) {
         final int var4 = MathHelper.clampAngle(getInt(var0, "rxm", 0));
         final int var5 = MathHelper.clampAngle(getInt(var0, "rx", 359));
         var1.add(new Predicate() {
            public boolean apply(@Nullable Entity var1) {
               if (var1 == null) {
                  return false;
               } else {
                  int var2 = MathHelper.clampAngle(MathHelper.floor(var1.rotationPitch));
                  return var4 > var5 ? var2 >= var4 || var2 <= var5 : var2 >= var4 && var2 <= var5;
               }
            }
         });
      }

      return var1;
   }

   private static List filterResults(Map var0, Class var1, List var2, String var3, World var4, BlockPos var5) {
      ArrayList var6 = Lists.newArrayList();
      String var7 = getArgument(var0, "type");
      var7 = var7 != null && var7.startsWith("!") ? var7.substring(1) : var7;
      boolean var8 = !var3.equals("e");
      boolean var9 = var3.equals("r") && var7 != null;
      int var10 = getInt(var0, "dx", 0);
      int var11 = getInt(var0, "dy", 0);
      int var12 = getInt(var0, "dz", 0);
      int var13 = getInt(var0, "r", -1);
      Predicate var14 = Predicates.and(var2);
      Predicate var15 = Predicates.and(EntitySelectors.IS_ALIVE, var14);
      int var16 = var4.playerEntities.size();
      int var17 = var4.loadedEntityList.size();
      boolean var18 = var16 < var17 / 16;
      if (!var0.containsKey("dx") && !var0.containsKey("dy") && !var0.containsKey("dz")) {
         if (var13 >= 0) {
            AxisAlignedBB var22 = new AxisAlignedBB((double)(var5.getX() - var13), (double)(var5.getY() - var13), (double)(var5.getZ() - var13), (double)(var5.getX() + var13 + 1), (double)(var5.getY() + var13 + 1), (double)(var5.getZ() + var13 + 1));
            if (var8 && var18 && !var9) {
               var6.addAll(var4.getPlayers(var1, var15));
            } else {
               var6.addAll(var4.getEntitiesWithinAABB(var1, var22, var15));
            }
         } else if (var3.equals("a")) {
            var6.addAll(var4.getPlayers(var1, var14));
         } else if (var3.equals("p") || var3.equals("r") && !var9) {
            var6.addAll(var4.getPlayers(var1, var15));
         } else {
            var6.addAll(var4.getEntities(var1, var15));
         }
      } else {
         final AxisAlignedBB var19 = getAABB(var5, var10, var11, var12);
         if (var8 && var18 && !var9) {
            Predicate var20 = new Predicate() {
               public boolean apply(@Nullable Entity var1) {
                  return var1 != null && var19.intersectsWith(var1.getEntityBoundingBox());
               }
            };
            var6.addAll(var4.getPlayers(var1, Predicates.and(var15, var20)));
         } else {
            var6.addAll(var4.getEntitiesWithinAABB(var1, var19, var15));
         }
      }

      return var6;
   }

   private static List getEntitiesFromPredicates(List var0, Map var1, ICommandSender var2, Class var3, String var4, final Vec3d var5) {
      int var6 = getInt(var1, "c", !var4.equals("a") && !var4.equals("e") ? 1 : 0);
      if (!var4.equals("p") && !var4.equals("a") && !var4.equals("e")) {
         if (var4.equals("r")) {
            Collections.shuffle((List)var0);
         }
      } else {
         Collections.sort((List)var0, new Comparator() {
            public int compare(Entity var1, Entity var2) {
               return ComparisonChain.start().compare(var1.getDistanceSq(var5.xCoord, var5.yCoord, var5.zCoord), var2.getDistanceSq(var5.xCoord, var5.yCoord, var5.zCoord)).result();
            }
         });
      }

      Entity var7 = var2.getCommandSenderEntity();
      if (var7 != null && var3.isAssignableFrom(var7.getClass()) && var6 == 1 && ((List)var0).contains(var7) && !"r".equals(var4)) {
         var0 = Lists.newArrayList(new Entity[]{var7});
      }

      if (var6 != 0) {
         if (var6 < 0) {
            Collections.reverse((List)var0);
         }

         var0 = ((List)var0).subList(0, Math.min(Math.abs(var6), ((List)var0).size()));
      }

      return (List)var0;
   }

   private static AxisAlignedBB getAABB(BlockPos var0, int var1, int var2, int var3) {
      boolean var4 = var1 < 0;
      boolean var5 = var2 < 0;
      boolean var6 = var3 < 0;
      int var7 = var0.getX() + (var4 ? var1 : 0);
      int var8 = var0.getY() + (var5 ? var2 : 0);
      int var9 = var0.getZ() + (var6 ? var3 : 0);
      int var10 = var0.getX() + (var4 ? 0 : var1) + 1;
      int var11 = var0.getY() + (var5 ? 0 : var2) + 1;
      int var12 = var0.getZ() + (var6 ? 0 : var3) + 1;
      return new AxisAlignedBB((double)var7, (double)var8, (double)var9, (double)var10, (double)var11, (double)var12);
   }

   private static BlockPos getBlockPosFromArguments(Map var0, BlockPos var1) {
      return new BlockPos(getInt(var0, "x", var1.getX()), getInt(var0, "y", var1.getY()), getInt(var0, "z", var1.getZ()));
   }

   private static Vec3d getPosFromArguments(Map var0, Vec3d var1) {
      return new Vec3d(getCoordinate(var0, "x", var1.xCoord, true), getCoordinate(var0, "y", var1.yCoord, false), getCoordinate(var0, "z", var1.zCoord, true));
   }

   private static double getCoordinate(Map var0, String var1, double var2, boolean var4) {
      return var0.containsKey(var1) ? (double)MathHelper.getInt((String)var0.get(var1), MathHelper.floor(var2)) + (var4 ? 0.5D : 0.0D) : var2;
   }

   private static boolean hasArgument(Map var0) {
      for(String var2 : WORLD_BINDING_ARGS) {
         if (var0.containsKey(var2)) {
            return true;
         }
      }

      return false;
   }

   private static int getInt(Map var0, String var1, int var2) {
      return var0.containsKey(var1) ? MathHelper.getInt((String)var0.get(var1), var2) : var2;
   }

   @Nullable
   private static String getArgument(Map var0, String var1) {
      return (String)var0.get(var1);
   }

   public static Map getScoreMap(Map var0) {
      HashMap var1 = Maps.newHashMap();

      for(String var3 : var0.keySet()) {
         if (var3.startsWith("score_") && var3.length() > "score_".length()) {
            var1.put(var3.substring("score_".length()), Integer.valueOf(MathHelper.getInt((String)var0.get(var3), 1)));
         }
      }

      return var1;
   }

   public static boolean matchesMultiplePlayers(String var0) {
      Matcher var1 = TOKEN_PATTERN.matcher(var0);
      if (!var1.matches()) {
         return false;
      } else {
         Map var2 = getArgumentMap(var1.group(2));
         String var3 = var1.group(1);
         int var4 = !"a".equals(var3) && !"e".equals(var3) ? 1 : 0;
         return getInt(var2, "c", var4) != 1;
      }
   }

   public static boolean hasArguments(String var0) {
      return TOKEN_PATTERN.matcher(var0).matches();
   }

   private static Map getArgumentMap(@Nullable String var0) {
      HashMap var1 = Maps.newHashMap();
      if (var0 == null) {
         return var1;
      } else {
         int var2 = 0;
         int var3 = -1;

         for(Matcher var4 = INT_LIST_PATTERN.matcher(var0); var4.find(); var3 = var4.end()) {
            String var5 = null;
            switch(var2++) {
            case 0:
               var5 = "x";
               break;
            case 1:
               var5 = "y";
               break;
            case 2:
               var5 = "z";
               break;
            case 3:
               var5 = "r";
            }

            if (var5 != null && !var4.group(1).isEmpty()) {
               var1.put(var5, var4.group(1));
            }
         }

         if (var3 < var0.length()) {
            Matcher var6 = KEY_VALUE_LIST_PATTERN.matcher(var3 == -1 ? var0 : var0.substring(var3));

            while(var6.find()) {
               var1.put(var6.group(1), var6.group(2));
            }
         }

         return var1;
      }
   }
}
