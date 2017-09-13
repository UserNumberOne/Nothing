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
   public EntityEgg(World var1) {
      super(var1);
   }

   public EntityEgg(World var1, EntityLivingBase var2) {
      super(var1, var2);
   }

   public EntityEgg(World var1, double var2, double var4, double var6) {
      super(var1, var2, var4, var6);
   }

   public static void registerFixesEgg(DataFixer var0) {
      EntityThrowable.registerFixesThrowable(var0, "ThrownEgg");
   }

   protected void onImpact(RayTraceResult var1) {
      if (var1.entityHit != null) {
         var1.entityHit.attackEntityFrom(DamageSource.causeThrownDamage(this, this.getThrower()), 0.0F);
      }

      boolean var2 = !this.world.isRemote && this.rand.nextInt(8) == 0;
      int var3 = this.rand.nextInt(32) == 0 ? 4 : 1;
      if (!var2) {
         var3 = 0;
      }

      EntityType var4 = EntityType.CHICKEN;
      EntityLivingBase var5 = this.getThrower();
      if (var5 instanceof EntityPlayerMP) {
         Player var6 = var5 == null ? null : (Player)var5.getBukkitEntity();
         PlayerEggThrowEvent var7 = new PlayerEggThrowEvent(var6, (Egg)this.getBukkitEntity(), var2, (byte)var3, var4);
         this.world.getServer().getPluginManager().callEvent(var7);
         var2 = var7.isHatching();
         var3 = var7.getNumHatches();
         var4 = var7.getHatchingType();
      }

      if (var2) {
         for(int var8 = 0; var8 < var3; ++var8) {
            Entity var10 = this.world.getWorld().createEntity(new Location(this.world.getWorld(), this.posX, this.posY, this.posZ, this.rotationYaw, 0.0F), var4.getEntityClass());
            if (var10.getBukkitEntity() instanceof Ageable) {
               ((Ageable)var10.getBukkitEntity()).setBaby();
            }

            this.world.getWorld().addEntity(var10, SpawnReason.EGG);
         }
      }

      for(int var9 = 0; var9 < 8; ++var9) {
         this.world.spawnParticle(EnumParticleTypes.ITEM_CRACK, this.posX, this.posY, this.posZ, ((double)this.rand.nextFloat() - 0.5D) * 0.08D, ((double)this.rand.nextFloat() - 0.5D) * 0.08D, ((double)this.rand.nextFloat() - 0.5D) * 0.08D, Item.getIdFromItem(Items.EGG));
      }

      if (!this.world.isRemote) {
         this.setDead();
      }

   }
}
