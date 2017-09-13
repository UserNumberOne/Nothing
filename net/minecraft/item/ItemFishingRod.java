package net.minecraft.item;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.init.SoundEvents;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerFishEvent.State;

public class ItemFishingRod extends Item {
   public ItemFishingRod() {
      this.setMaxDamage(64);
      this.setMaxStackSize(1);
      this.setCreativeTab(CreativeTabs.TOOLS);
      this.addPropertyOverride(new ResourceLocation("cast"), new IItemPropertyGetter() {
      });
   }

   public ActionResult onItemRightClick(ItemStack var1, World var2, EntityPlayer var3, EnumHand var4) {
      if (var3.fishEntity != null) {
         int var5 = var3.fishEntity.handleHookRetraction();
         var1.damageItem(var5, var3);
         var3.swingArm(var4);
      } else {
         EntityFishHook var7 = new EntityFishHook(var2, var3);
         PlayerFishEvent var6 = new PlayerFishEvent((Player)var3.getBukkitEntity(), (Entity)null, (Fish)var7.getBukkitEntity(), State.FISHING);
         var2.getServer().getPluginManager().callEvent(var6);
         if (var6.isCancelled()) {
            return new ActionResult(EnumActionResult.PASS, var1);
         }

         var2.playSound((EntityPlayer)null, var3.posX, var3.posY, var3.posZ, SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
         if (!var2.isRemote) {
            var2.spawnEntity(var7);
         }

         var3.swingArm(var4);
         var3.addStat(StatList.getObjectUseStats(this));
      }

      return new ActionResult(EnumActionResult.SUCCESS, var1);
   }

   public boolean isEnchantable(ItemStack var1) {
      return super.isEnchantable(var1);
   }

   public int getItemEnchantability() {
      return 1;
   }
}
