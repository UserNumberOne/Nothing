package net.minecraft.item.crafting;

import javax.annotation.Nullable;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.bukkit.inventory.Recipe;

public interface IRecipe {
   boolean matches(InventoryCrafting var1, World var2);

   @Nullable
   ItemStack getCraftingResult(InventoryCrafting var1);

   int getRecipeSize();

   @Nullable
   ItemStack getRecipeOutput();

   ItemStack[] getRemainingItems(InventoryCrafting var1);

   Recipe toBukkitRecipe();
}
