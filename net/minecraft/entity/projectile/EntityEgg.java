package net.minecraft.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.bukkit.Location;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerEggThrowEvent;

public class EntityEgg extends EntityThrowable {
   public EntityEgg(World world) {
      super(world);
   }

   public EntityEgg(World world, EntityLivingBase entityliving) {
      super(world, entityliving);
   }

   public EntityEgg(World world, double d0, double d1, double d2) {
      super(world, d0, d1, d2);
   }

   public static void registerFixesEgg(DataFixer dataconvertermanager) {
      EntityThrowable.registerFixesThrowable(dataconvertermanager, "ThrownEgg");
   }

   protected void onImpact(RayTraceResult movingobjectposition) {
      if (movingobjectposition.entityHit != null) {
         movingobjectposition.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 0.0F);
      }

      boolean hatching = !this.world.isRemote && this.rand.nextInt(8) == 0;
      int numHatching = this.rand.nextInt(32) == 0 ? 4 : 1;
      if (!hatching) {
         numHatching = 0;
      }

      EntityType hatchingType = EntityType.CHICKEN;
      Entity shooter = this.getThrower();
      if (shooter instanceof EntityPlayerMP) {
         Player player = shooter == null ? null : (Player)shooter.getBukkitEntity();
         PlayerEggThrowEvent event = new PlayerEggThrowEvent(player, (Egg)this.getBukkitEntity(), hatching, (byte)numHatching, hatchingType);
         this.world.getServer().getPluginManager().callEvent(event);
         hatching = event.isHatching();
         numHatching = event.getNumHatches();
         hatchingType = event.getHatchingType();
      }

      if (hatching) {
         for(int k = 0; k < numHatching; ++k) {
            Entity entity = this.world.getWorld().createEntity(new Location(this.world.getWorld(), this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F), hatchingType.getEntityClass());
            if (entity.getBukkitEntity() instanceof Ageable) {
               ((Ageable)entity.getBukkitEntity()).setBaby();
            }

            this.world.getWorld().addEntity(entity, SpawnReason.EGG);
         }
      }

      for(int j = 0; j < 8; ++j) {
         this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX, this.posY, this.posZ, ((double)this.rand.nextFloat() - 0.5D) * 0.08D, ((double)this.rand.nextFloat() - 0.5D) * 0.08D, ((double)this.rand.nextFloat() - 0.5D) * 0.08D, Item.getIdFromItem(Items.EGG));
      }

      if (!this.world.isRemote) {
         this.setDead();
      }

   }
}
