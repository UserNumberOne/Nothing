package net.minecraft.entity.passive;

import javax.annotation.Nullable;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerShearEntityEvent;

public class EntityMooshroom extends EntityCow {
   public EntityMooshroom(World var1) {
      super(var1);
      this.setSize(0.9F, 1.4F);
      this.spawnableBlock = Blocks.MYCELIUM;
   }

   public static void registerFixesMooshroom(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "MushroomCow");
   }

   public boolean processInteract(EntityPlayer var1, EnumHand var2, @Nullable ItemStack var3) {
      if (var3 != null && var3.getItem() == Items.BOWL && this.getGrowingAge() >= 0 && !var1.capabilities.isCreativeMode) {
         if (--var3.stackSize == 0) {
            var1.setHeldItem(var2, new ItemStack(Items.MUSHROOM_STEW));
         } else if (!var1.inventory.addItemStackToInventory(new ItemStack(Items.MUSHROOM_STEW))) {
            var1.dropItem(new ItemStack(Items.MUSHROOM_STEW), false);
         }

         return true;
      } else if (var3 != null && var3.getItem() == Items.SHEARS && this.getGrowingAge() >= 0) {
         PlayerShearEntityEvent var4 = new PlayerShearEntityEvent((Player)var1.getBukkitEntity(), this.getBukkitEntity());
         this.world.getServer().getPluginManager().callEvent(var4);
         if (var4.isCancelled()) {
            return false;
         } else {
            this.setDead();
            this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)(this.height / 2.0F), this.posZ, 0.0D, 0.0D, 0.0D);
            if (!this.world.isRemote) {
               EntityCow var5 = new EntityCow(this.world);
               var5.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
               var5.setHealth(this.getHealth());
               var5.renderYawOffset = this.renderYawOffset;
               if (this.hasCustomName()) {
                  var5.setCustomNameTag(this.getCustomNameTag());
               }

               this.world.spawnEntity(var5);

               for(int var6 = 0; var6 < 5; ++var6) {
                  this.world.spawnEntity(new EntityItem(this.world, this.posX, this.posY + (double)this.height, this.posZ, new ItemStack(Blocks.RED_MUSHROOM)));
               }

               var3.damageItem(1, var1);
               this.playSound(SoundEvents.ENTITY_MOOSHROOM_SHEAR, 1.0F, 1.0F);
            }

            return true;
         }
      } else {
         return super.processInteract(var1, var2, var3);
      }
   }

   public EntityMooshroom createChild(EntityAgeable var1) {
      return new EntityMooshroom(this.world);
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_MUSHROOM_COW;
   }

   public EntityCow createChild(EntityAgeable var1) {
      return this.createChild(var1);
   }

   public EntityAgeable createChild(EntityAgeable var1) {
      return this.createChild(var1);
   }
}
