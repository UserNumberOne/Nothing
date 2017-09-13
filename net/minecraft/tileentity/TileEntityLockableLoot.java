package net.minecraft.tileentity;

import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.ILootContainer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;

public abstract class TileEntityLockableLoot extends TileEntityLockable implements ILootContainer {
   protected ResourceLocation lootTable;
   protected long lootTableSeed;

   protected boolean checkLootAndRead(NBTTagCompound var1) {
      if (var1.hasKey("LootTable", 8)) {
         this.lootTable = new ResourceLocation(var1.getString("LootTable"));
         this.lootTableSeed = var1.getLong("LootTableSeed");
         return true;
      } else {
         return false;
      }
   }

   protected boolean checkLootAndWrite(NBTTagCompound var1) {
      if (this.lootTable != null) {
         var1.setString("LootTable", this.lootTable.toString());
         if (this.lootTableSeed != 0L) {
            var1.setLong("LootTableSeed", this.lootTableSeed);
         }

         return true;
      } else {
         return false;
      }
   }

   protected void fillWithLoot(@Nullable EntityPlayer var1) {
      if (this.lootTable != null) {
         LootTable var2 = this.world.getLootTableManager().getLootTableFromLocation(this.lootTable);
         this.lootTable = null;
         Random var3;
         if (this.lootTableSeed == 0L) {
            var3 = new Random();
         } else {
            var3 = new Random(this.lootTableSeed);
         }

         LootContext.Builder var4 = new LootContext.Builder((WorldServer)this.world);
         if (var1 != null) {
            var4.withLuck(var1.getLuck());
         }

         var2.fillInventory(this, var3, var4.build());
      }

   }

   public ResourceLocation getLootTable() {
      return this.lootTable;
   }

   public void setLootTable(ResourceLocation var1, long var2) {
      this.lootTable = var1;
      this.lootTableSeed = var2;
   }
}
