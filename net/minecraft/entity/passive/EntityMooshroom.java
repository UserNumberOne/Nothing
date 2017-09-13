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
      super(worldIn);
      this.setSize(0.9F, 1.4F);
      this.spawnableBlock = Blocks.MYCELIUM;
   }

   public static void registerFixesMooshroom(DataFixer var0) {
      EntityLiving.registerFixesMob(fixer, "MushroomCow");
   }

   public boolean processInteract(EntityPlayer var1, EnumHand var2, @Nullable ItemStack var3) {
      if (stack != null && stack.getItem() == Items.BOWL && this.getGrowingAge() >= 0 && !player.capabilities.isCreativeMode) {
         if (--stack.stackSize == 0) {
            player.setHeldItem(hand, new ItemStack(Items.MUSHROOM_STEW));
         } else if (!player.inventory.addItemStackToInventory(new ItemStack(Items.MUSHROOM_STEW))) {
            player.dropItem(new ItemStack(Items.MUSHROOM_STEW), false);
         }

         return true;
      } else {
         return super.processInteract(player, hand, stack);
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
      EntityCow entitycow = new EntityCow(this.world);
      entitycow.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
      entitycow.setHealth(this.getHealth());
      entitycow.renderYawOffset = this.renderYawOffset;
      if (this.hasCustomName()) {
         entitycow.setCustomNameTag(this.getCustomNameTag());
      }

      this.world.spawnEntity(entitycow);
      List ret = new ArrayList();

      for(int i = 0; i < 5; ++i) {
         ret.add(new ItemStack(Blocks.RED_MUSHROOM));
      }

      this.playSound(SoundEvents.ENTITY_MOOSHROOM_SHEAR, 1.0F, 1.0F);
      return ret;
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_MUSHROOM_COW;
   }
}
