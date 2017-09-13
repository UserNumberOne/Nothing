package net.minecraft.world.storage.loot.conditions;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.RandomValueRange;

public class EntityHasScore implements LootCondition {
   private final Map scores;
   private final LootContext.EntityTarget target;

   public EntityHasScore(Map var1, LootContext.EntityTarget var2) {
      this.scores = var1;
      this.target = var2;
   }

   public boolean testCondition(Random var1, LootContext var2) {
      Entity var3 = var2.getEntity(this.target);
      if (var3 == null) {
         return false;
      } else {
         Scoreboard var4 = var3.world.getScoreboard();

         for(Entry var6 : this.scores.entrySet()) {
            if (!this.entityScoreMatch(var3, var4, (String)var6.getKey(), (RandomValueRange)var6.getValue())) {
               return false;
            }
         }

         return true;
      }
   }

   protected boolean entityScoreMatch(Entity var1, Scoreboard var2, String var3, RandomValueRange var4) {
      ScoreObjective var5 = var2.getObjective(var3);
      if (var5 == null) {
         return false;
      } else {
         String var6 = var1 instanceof EntityPlayerMP ? var1.getName() : var1.getCachedUniqueIdString();
         return !var2.entityHasObjective(var6, var5) ? false : var4.isInRange(var2.getOrCreateScore(var6, var5).getScorePoints());
      }
   }

   public static class Serializer extends LootCondition.Serializer {
      protected Serializer() {
         super(new ResourceLocation("entity_scores"), EntityHasScore.class);
      }

      public void serialize(JsonObject var1, EntityHasScore var2, JsonSerializationContext var3) {
         JsonObject var4 = new JsonObject();

         for(Entry var6 : var2.scores.entrySet()) {
            var4.add((String)var6.getKey(), var3.serialize(var6.getValue()));
         }

         var1.add("scores", var4);
         var1.add("entity", var3.serialize(var2.target));
      }

      public EntityHasScore deserialize(JsonObject var1, JsonDeserializationContext var2) {
         Set var3 = JsonUtils.getJsonObject(var1, "scores").entrySet();
         LinkedHashMap var4 = Maps.newLinkedHashMap();

         for(Entry var6 : var3) {
            var4.put(var6.getKey(), JsonUtils.deserializeClass((JsonElement)var6.getValue(), "score", var2, RandomValueRange.class));
         }

         return new EntityHasScore(var4, (LootContext.EntityTarget)JsonUtils.deserializeClass(var1, "entity", var2, LootContext.EntityTarget.class));
      }

      // $FF: synthetic method
      public LootCondition deserialize(JsonObject var1, JsonDeserializationContext var2) {
         return this.deserialize(var1, var2);
      }
   }
}
