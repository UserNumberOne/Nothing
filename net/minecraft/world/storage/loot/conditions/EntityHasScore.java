package net.minecraft.world.storage.loot.conditions;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
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
      this.scores = scoreIn;
      this.target = targetIn;
   }

   public boolean testCondition(Random var1, LootContext var2) {
      Entity entity = context.getEntity(this.target);
      if (entity == null) {
         return false;
      } else {
         Scoreboard scoreboard = entity.world.getScoreboard();

         for(Entry entry : this.scores.entrySet()) {
            if (!this.entityScoreMatch(entity, scoreboard, (String)entry.getKey(), (RandomValueRange)entry.getValue())) {
               return false;
            }
         }

         return true;
      }
   }

   protected boolean entityScoreMatch(Entity var1, Scoreboard var2, String var3, RandomValueRange var4) {
      ScoreObjective scoreobjective = scoreboardIn.getObjective(objectiveStr);
      if (scoreobjective == null) {
         return false;
      } else {
         String s = entityIn instanceof EntityPlayerMP ? entityIn.getName() : entityIn.getCachedUniqueIdString();
         return !scoreboardIn.entityHasObjective(s, scoreobjective) ? false : rand.isInRange(scoreboardIn.getOrCreateScore(s, scoreobjective).getScorePoints());
      }
   }

   public static class Serializer extends LootCondition.Serializer {
      protected Serializer() {
         super(new ResourceLocation("entity_scores"), EntityHasScore.class);
      }

      public void serialize(JsonObject var1, EntityHasScore var2, JsonSerializationContext var3) {
         JsonObject jsonobject = new JsonObject();

         for(Entry entry : value.scores.entrySet()) {
            jsonobject.add((String)entry.getKey(), context.serialize(entry.getValue()));
         }

         json.add("scores", jsonobject);
         json.add("entity", context.serialize(value.target));
      }

      public EntityHasScore deserialize(JsonObject var1, JsonDeserializationContext var2) {
         Set set = JsonUtils.getJsonObject(json, "scores").entrySet();
         Map map = Maps.newLinkedHashMap();

         for(Entry entry : set) {
            map.put(entry.getKey(), JsonUtils.deserializeClass((JsonElement)entry.getValue(), "score", context, RandomValueRange.class));
         }

         return new EntityHasScore(map, (LootContext.EntityTarget)JsonUtils.deserializeClass(json, "entity", context, LootContext.EntityTarget.class));
      }
   }
}
