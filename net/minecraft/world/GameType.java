package net.minecraft.world;

import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum GameType {
   NOT_SET(-1, "", ""),
   SURVIVAL(0, "survival", "s"),
   CREATIVE(1, "creative", "c"),
   ADVENTURE(2, "adventure", "a"),
   SPECTATOR(3, "spectator", "sp");

   int id;
   String name;
   String shortName;

   private GameType(int var3, String var4, String var5) {
      this.id = var3;
      this.name = var4;
      this.shortName = var5;
   }

   public int getID() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public void configurePlayerCapabilities(PlayerCapabilities var1) {
      if (this == CREATIVE) {
         var1.allowFlying = true;
         var1.isCreativeMode = true;
         var1.disableDamage = true;
      } else if (this == SPECTATOR) {
         var1.allowFlying = true;
         var1.isCreativeMode = false;
         var1.disableDamage = true;
         var1.isFlying = true;
      } else {
         var1.allowFlying = false;
         var1.isCreativeMode = false;
         var1.disableDamage = false;
         var1.isFlying = false;
      }

      var1.allowEdit = !this.isAdventure();
   }

   public boolean isAdventure() {
      return this == ADVENTURE || this == SPECTATOR;
   }

   public boolean isCreative() {
      return this == CREATIVE;
   }

   public boolean isSurvivalOrAdventure() {
      return this == SURVIVAL || this == ADVENTURE;
   }

   public static GameType getByID(int var0) {
      return parseGameTypeWithDefault(var0, SURVIVAL);
   }

   public static GameType parseGameTypeWithDefault(int var0, GameType var1) {
      for(GameType var5 : values()) {
         if (var5.id == var0) {
            return var5;
         }
      }

      return var1;
   }

   @SideOnly(Side.CLIENT)
   public static GameType getByName(String var0) {
      return parseGameTypeWithDefault(var0, SURVIVAL);
   }

   public static GameType parseGameTypeWithDefault(String var0, GameType var1) {
      for(GameType var5 : values()) {
         if (var5.name.equals(var0) || var5.shortName.equals(var0)) {
            return var5;
         }
      }

      return var1;
   }
}
