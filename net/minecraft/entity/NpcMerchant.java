package net.minecraft.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryMerchant;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class NpcMerchant implements IMerchant {
   private final InventoryMerchant theMerchantInventory;
   private final EntityPlayer customer;
   private MerchantRecipeList recipeList;
   private final ITextComponent name;

   public NpcMerchant(EntityPlayer var1, ITextComponent var2) {
      this.customer = var1;
      this.name = var2;
      this.theMerchantInventory = new InventoryMerchant(var1, this);
   }

   public EntityPlayer getCustomer() {
      return this.customer;
   }

   public void setCustomer(EntityPlayer var1) {
   }

   public MerchantRecipeList getRecipes(EntityPlayer var1) {
      return this.recipeList;
   }

   public void setRecipes(MerchantRecipeList var1) {
      this.recipeList = var1;
   }

   public void useRecipe(MerchantRecipe var1) {
      var1.incrementToolUses();
   }

   public void verifySellingItem(ItemStack var1) {
   }

   public ITextComponent getDisplayName() {
      return (ITextComponent)(this.name != null ? this.name : new TextComponentTranslation("entity.Villager.name", new Object[0]));
   }
}
