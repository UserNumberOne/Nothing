package net.minecraft.world.storage.loot;

import com.google.common.base.Charsets;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.storage.loot.conditions.LootCondition;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.common.ForgeHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LootTableManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Gson GSON_INSTANCE = (new GsonBuilder()).registerTypeAdapter(RandomValueRange.class, new RandomValueRange.Serializer()).registerTypeAdapter(LootPool.class, new LootPool.Serializer()).registerTypeAdapter(LootTable.class, new LootTable.Serializer()).registerTypeHierarchyAdapter(LootEntry.class, new LootEntry.Serializer()).registerTypeHierarchyAdapter(LootFunction.class, new LootFunctionManager.Serializer()).registerTypeHierarchyAdapter(LootCondition.class, new LootConditionManager.Serializer()).registerTypeHierarchyAdapter(LootContext.EntityTarget.class, new LootContext.EntityTarget.Serializer()).create();
   private final LoadingCache registeredLootTables = CacheBuilder.newBuilder().build(new LootTableManager.Loader());
   private final File baseFolder;

   public LootTableManager(File var1) {
      this.baseFolder = var1;
      this.reloadLootTables();
   }

   public LootTable getLootTableFromLocation(ResourceLocation var1) {
      return (LootTable)this.registeredLootTables.getUnchecked(var1);
   }

   public void reloadLootTables() {
      this.registeredLootTables.invalidateAll();

      for(ResourceLocation var2 : LootTableList.getAll()) {
         this.getLootTableFromLocation(var2);
      }

   }

   class Loader extends CacheLoader {
      private Loader() {
      }

      public LootTable load(ResourceLocation var1) throws Exception {
         if (var1.getResourcePath().contains(".")) {
            LootTableManager.LOGGER.debug("Invalid loot table name '{}' (can't contain periods)", new Object[]{var1});
            return LootTable.EMPTY_LOOT_TABLE;
         } else {
            LootTable var2 = this.loadLootTable(var1);
            if (var2 == null) {
               var2 = this.loadBuiltinLootTable(var1);
            }

            if (var2 == null) {
               var2 = LootTable.EMPTY_LOOT_TABLE;
               LootTableManager.LOGGER.warn("Couldn't find resource table {}", new Object[]{var1});
            }

            return var2;
         }
      }

      @Nullable
      private LootTable loadLootTable(ResourceLocation var1) {
         File var2 = new File(new File(LootTableManager.this.baseFolder, var1.getResourceDomain()), var1.getResourcePath() + ".json");
         if (var2.exists()) {
            if (var2.isFile()) {
               String var3;
               try {
                  var3 = Files.toString(var2, Charsets.UTF_8);
               } catch (IOException var6) {
                  LootTableManager.LOGGER.warn("Couldn't load loot table {} from {}", new Object[]{var1, var2, var6});
                  return LootTable.EMPTY_LOOT_TABLE;
               }

               try {
                  return ForgeHooks.loadLootTable(LootTableManager.GSON_INSTANCE, var1, var3, true);
               } catch (JsonParseException var5) {
                  LootTableManager.LOGGER.error("Couldn't load loot table {} from {}", new Object[]{var1, var2, var5});
                  return LootTable.EMPTY_LOOT_TABLE;
               }
            } else {
               LootTableManager.LOGGER.warn("Expected to find loot table {} at {} but it was a folder.", new Object[]{var1, var2});
               return LootTable.EMPTY_LOOT_TABLE;
            }
         } else {
            return null;
         }
      }

      @Nullable
      private LootTable loadBuiltinLootTable(ResourceLocation var1) {
         URL var2 = LootTableManager.class.getResource("/assets/" + var1.getResourceDomain() + "/loot_tables/" + var1.getResourcePath() + ".json");
         if (var2 != null) {
            String var3;
            try {
               var3 = Resources.toString(var2, Charsets.UTF_8);
            } catch (IOException var6) {
               LootTableManager.LOGGER.warn("Couldn't load loot table {} from {}", new Object[]{var1, var2, var6});
               return LootTable.EMPTY_LOOT_TABLE;
            }

            try {
               return ForgeHooks.loadLootTable(LootTableManager.GSON_INSTANCE, var1, var3, false);
            } catch (JsonParseException var5) {
               LootTableManager.LOGGER.error("Couldn't load loot table {} from {}", new Object[]{var1, var2, var5});
               return LootTable.EMPTY_LOOT_TABLE;
            }
         } else {
            return null;
         }
      }
   }
}
