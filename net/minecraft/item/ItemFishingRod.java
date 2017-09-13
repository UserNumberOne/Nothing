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

   public ActionResult onItemRightClick(ItemStack itemstack, World world, EntityPlayer entityhuman, EnumHand enumhand) {
      if (entityhuman.fishEntity != null) {
         int i = entityhuman.fishEntity.handleHookRetraction();
         itemstack.damageItem(i, entityhuman);
         entityhuman.swingArm(enumhand);
      } else {
         EntityFishHook hook = new EntityFishHook(world, entityhuman);
         PlayerFishEvent playerFishEvent = new PlayerFishEvent((Player)entityhuman.getBukkitEntity(), (Entity)null, (Fish)hook.getBukkitEntity(), State.FISHING);
         world.getServer().getPluginManager().callEvent(playerFishEvent);
         if (playerFishEvent.isCancelled()) {
            return new ActionResult(EnumActionResult.PASS, itemstack);
         }

         world.playSound((EntityPlayer)null, entityhuman.posX, entityhuman.posY, entityhuman.posZ, SoundEvents.ENTITY_BOBBER_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
         if (!world.isRemote) {
            world.spawnEntity(hook);
         }

         entityhuman.swingArm(enumhand);
         entityhuman.addStat(StatList.getObjectUseStats(this));
      }

      return new ActionResult(EnumActionResult.SUCCESS, itemstack);
   }

   public boolean isEnchantable(ItemStack itemstack) {
      return super.isEnchantable(itemstack);
   }

   public int getItemEnchantability() {
      return 1;
   }
}
