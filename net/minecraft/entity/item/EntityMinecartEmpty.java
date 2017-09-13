package net.minecraft.entity.item;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;

public class EntityMinecartEmpty extends EntityMinecart {
   public EntityMinecartEmpty(World var1) {
      super(worldIn);
   }

   public EntityMinecartEmpty(World var1, double var2, double var4, double var6) {
      super(worldIn, x, y, z);
   }

   public static void registerFixesMinecartEmpty(DataFixer var0) {
      EntityMinecart.registerFixesMinecart(fixer, "MinecartRideable");
   }

   public boolean processInitialInteract(EntityPlayer var1, @Nullable ItemStack var2, EnumHand var3) {
      if (MinecraftForge.EVENT_BUS.post(new MinecartInteractEvent(this, player, stack, hand))) {
         return true;
      } else if (player.isSneaking()) {
         return false;
      } else if (this.isBeingRidden()) {
         return true;
      } else {
         if (!this.world.isRemote) {
            player.startRiding(this);
         }

         return true;
      }
   }

   public void onActivatorRailPass(int var1, int var2, int var3, boolean var4) {
      if (receivingPower) {
         if (this.isBeingRidden()) {
            this.removePassengers();
         }

         if (this.getRollingAmplitude() == 0) {
            this.setRollingDirection(-this.getRollingDirection());
            this.setRollingAmplitude(10);
            this.setDamage(50.0F);
            this.setBeenAttacked();
         }
      }

   }

   public EntityMinecart.Type getType() {
      return EntityMinecart.Type.RIDEABLE;
   }
}
