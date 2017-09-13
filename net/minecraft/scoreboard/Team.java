package net.minecraft.scoreboard;

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.util.text.TextFormatting;

public abstract class Team {
   public boolean isSameTeam(@Nullable Team var1) {
      if (var1 == null) {
         return false;
      } else {
         return this == var1;
      }
   }

   public abstract String getRegisteredName();

   public abstract String formatString(String var1);

   public abstract boolean getAllowFriendlyFire();

   public abstract TextFormatting getChatFormat();

   public abstract Collection getMembershipCollection();

   public abstract Team.EnumVisible getDeathMessageVisibility();

   public abstract Team.CollisionRule getCollisionRule();

   public static enum CollisionRule {
      ALWAYS("always", 0),
      NEVER("never", 1),
      HIDE_FOR_OTHER_TEAMS("pushOtherTeams", 2),
      HIDE_FOR_OWN_TEAM("pushOwnTeam", 3);

      private static final Map nameMap = Maps.newHashMap();
      public final String name;
      public final int id;

      public static String[] getNames() {
         return (String[])nameMap.keySet().toArray(new String[nameMap.size()]);
      }

      public static Team.CollisionRule getByName(String var0) {
         return (Team.CollisionRule)nameMap.get(var0);
      }

      private CollisionRule(String var3, int var4) {
         this.name = var3;
         this.id = var4;
      }

      static {
         for(Team.CollisionRule var3 : values()) {
            nameMap.put(var3.name, var3);
         }

      }
   }

   public static enum EnumVisible {
      ALWAYS("always", 0),
      NEVER("never", 1),
      HIDE_FOR_OTHER_TEAMS("hideForOtherTeams", 2),
      HIDE_FOR_OWN_TEAM("hideForOwnTeam", 3);

      private static final Map nameMap = Maps.newHashMap();
      public final String internalName;
      public final int id;

      public static String[] getNames() {
         return (String[])nameMap.keySet().toArray(new String[nameMap.size()]);
      }

      public static Team.EnumVisible getByName(String var0) {
         return (Team.EnumVisible)nameMap.get(var0);
      }

      private EnumVisible(String var3, int var4) {
         this.internalName = var3;
         this.id = var4;
      }

      static {
         for(Team.EnumVisible var3 : values()) {
            nameMap.put(var3.internalName, var3);
         }

      }
   }
}
