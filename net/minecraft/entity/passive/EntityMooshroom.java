package net.minecraft.entity.passive;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EntityAgeable;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.IShearable;

public class EntityMooshroom extends EntityCow implements IShearable {
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
      } else {
         return super.processInteract(var1, var2, var3);
      }
   }

   public EntityMooshroom createChild(EntityAgeable var1) {
      return new EntityMooshroom(this.world);
   }

   public boolean isShearable(ItemStack var1, IBlockAccess var2, BlockPos var3) {
      return this.getGrowingAge() >= 0;
   }

   public List onSheared(ItemStack var1, IBlockAccess var2, BlockPos var3, int var4) {
      this.setDead();
      this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX, this.posY + (double)(this.height / 2.0F), this.posZ, 0.0D, 0.0D, 0.0D);
      EntityCow var5 = new EntityCow(this.world);
      var5.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
      var5.setHealth(this.getHealth());
      var5.renderYawOffset = this.renderYawOffset;
      if (this.hasCustomName()) {
         var5.setCustomNameTag(this.getCustomNameTag());
      }

      this.world.spawnEntity(var5);
      ArrayList var6 = new ArrayList();

      for(int var7 = 0; var7 < 5; ++var7) {
         var6.add(new ItemStack(Blocks.RED_MUSHROOM));
      }

      this.playSound(SoundEvents.ENTITY_MOOSHROOM_SHEAR, 1.0F, 1.0F);
      return var6;
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_MUSHROOM_COW;
   }
}
