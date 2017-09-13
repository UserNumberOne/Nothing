package net.minecraft.util.datafix.walkers;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BlockEntityTag implements IDataWalker {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Map ITEM_ID_TO_BLOCK_ENTITY_ID = Maps.newHashMap();

   @Nullable
   private static String getBlockEntityID(String var0) {
      return (String)ITEM_ID_TO_BLOCK_ENTITY_ID.get((new ResourceLocation(var0)).toString());
   }

   public NBTTagCompound process(IDataFixer var1, NBTTagCompound var2, int var3) {
      if (!var2.hasKey("tag", 10)) {
         return var2;
      } else {
         NBTTagCompound var4 = var2.getCompoundTag("tag");
         if (var4.hasKey("BlockEntityTag", 10)) {
            NBTTagCompound var5 = var4.getCompoundTag("BlockEntityTag");
            String var6 = var2.getString("id");
            String var7 = getBlockEntityID(var6);
            boolean var8;
            if (var7 == null) {
               LOGGER.warn("Unable to resolve BlockEntity for ItemInstance: {}", new Object[]{var6});
               var8 = false;
            } else {
               var8 = !var5.hasKey("id");
               var5.setString("id", var7);
            }

            var1.process(FixTypes.BLOCK_ENTITY, var5, var3);
            if (var8) {
               var5.removeTag("id");
            }
         }

         return var2;
      }
   }

   static {
      Map var0 = ITEM_ID_TO_BLOCK_ENTITY_ID;
      var0.put("minecraft:furnace", "Furnace");
      var0.put("minecraft:lit_furnace", "Furnace");
      var0.put("minecraft:chest", "Chest");
      var0.put("minecraft:trapped_chest", "Chest");
      var0.put("minecraft:ender_chest", "EnderChest");
      var0.put("minecraft:jukebox", "RecordPlayer");
      var0.put("minecraft:dispenser", "Trap");
      var0.put("minecraft:dropper", "Dropper");
      var0.put("minecraft:sign", "Sign");
      var0.put("minecraft:mob_spawner", "MobSpawner");
      var0.put("minecraft:noteblock", "Music");
      var0.put("minecraft:brewing_stand", "Cauldron");
      var0.put("minecraft:enhanting_table", "EnchantTable");
      var0.put("minecraft:command_block", "CommandBlock");
      var0.put("minecraft:beacon", "Beacon");
      var0.put("minecraft:skull", "Skull");
      var0.put("minecraft:daylight_detector", "DLDetector");
      var0.put("minecraft:hopper", "Hopper");
      var0.put("minecraft:banner", "Banner");
      var0.put("minecraft:flower_pot", "FlowerPot");
      var0.put("minecraft:repeating_command_block", "CommandBlock");
      var0.put("minecraft:chain_command_block", "CommandBlock");
      var0.put("minecraft:standing_sign", "Sign");
      var0.put("minecraft:wall_sign", "Sign");
      var0.put("minecraft:piston_head", "Piston");
      var0.put("minecraft:daylight_detector_inverted", "DLDetector");
      var0.put("minecraft:unpowered_comparator", "Comparator");
      var0.put("minecraft:powered_comparator", "Comparator");
      var0.put("minecraft:wall_banner", "Banner");
      var0.put("minecraft:standing_banner", "Banner");
      var0.put("minecraft:structure_block", "Structure");
      var0.put("minecraft:end_portal", "Airportal");
      var0.put("minecraft:end_gateway", "EndGateway");
      var0.put("minecraft:shield", "Shield");
   }
}
