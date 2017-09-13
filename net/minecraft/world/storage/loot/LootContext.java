package net.minecraft.world.storage.loot;

import com.google.common.collect.Sets;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.WorldServer;

public class LootContext {
   private final float luck;
   private final WorldServer world;
   private final LootTableManager lootTableManager;
   @Nullable
   private final Entity lootedEntity;
   @Nullable
   private final EntityPlayer player;
   @Nullable
   private final DamageSource damageSource;
   private final Set lootTables = Sets.newLinkedHashSet();

   public LootContext(float var1, WorldServer var2, LootTableManager var3, @Nullable Entity var4, @Nullable EntityPlayer var5, @Nullable DamageSource var6) {
      this.luck = var1;
      this.world = var2;
      this.lootTableManager = var3;
      this.lootedEntity = var4;
      this.player = var5;
      this.damageSource = var6;
   }

   @Nullable
   public Entity getLootedEntity() {
      return this.lootedEntity;
   }

   @Nullable
   public Entity getKillerPlayer() {
      return this.player;
   }

   @Nullable
   public Entity getKiller() {
      return this.damageSource == null ? null : this.damageSource.getEntity();
   }

   public boolean addLootTable(LootTable var1) {
      return this.lootTables.add(var1);
   }

   public void removeLootTable(LootTable var1) {
      this.lootTables.remove(var1);
   }

   public LootTableManager getLootTableManager() {
      return this.lootTableManager;
   }

   public float getLuck() {
      return this.luck;
   }

   @Nullable
   public Entity getEntity(LootContext.EntityTarget var1) {
      switch(var1) {
      case THIS:
         return this.getLootedEntity();
      case KILLER:
         return this.getKiller();
      case KILLER_PLAYER:
         return this.getKillerPlayer();
      default:
         return null;
      }
   }

   public static class Builder {
      private final WorldServer world;
      private float luck;
      private Entity lootedEntity;
      private EntityPlayer player;
      private DamageSource damageSource;

      public Builder(WorldServer var1) {
         this.world = var1;
      }

      public LootContext.Builder withLuck(float var1) {
         this.luck = var1;
         return this;
      }

      public LootContext.Builder withLootedEntity(Entity var1) {
         this.lootedEntity = var1;
         return this;
      }

      public LootContext.Builder withPlayer(EntityPlayer var1) {
         this.player = var1;
         return this;
      }

      public LootContext.Builder withDamageSource(DamageSource var1) {
         this.damageSource = var1;
         return this;
      }

      public LootContext build() {
         return new LootContext(this.luck, this.world, this.world.getLootTableManager(), this.lootedEntity, this.player, this.damageSource);
      }
   }

   public static enum EntityTarget {
      THIS("this"),
      KILLER("killer"),
      KILLER_PLAYER("killer_player");

      private final String targetType;

      private EntityTarget(String var3) {
         this.targetType = var3;
      }

      public static LootContext.EntityTarget fromString(String var0) {
         for(LootContext.EntityTarget var4 : values()) {
            if (var4.targetType.equals(var0)) {
               return var4;
            }
         }

         throw new IllegalArgumentException("Invalid entity target " + var0);
      }

      public static class Serializer extends TypeAdapter {
         public void write(JsonWriter var1, LootContext.EntityTarget var2) throws IOException {
            var1.value(var2.targetType);
         }

         public LootContext.EntityTarget read(JsonReader var1) throws IOException {
            return LootContext.EntityTarget.fromString(var1.nextString());
         }

         // $FF: synthetic method
         public Object read(JsonReader var1) throws IOException {
            return this.read(var1);
         }

         // $FF: synthetic method
         public void write(JsonWriter var1, Object var2) throws IOException {
            this.write(var1, (LootContext.EntityTarget)var2);
         }
      }
   }
}
