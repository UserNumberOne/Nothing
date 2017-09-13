package net.minecraft.entity.monster;

import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

public class EntityCaveSpider extends EntitySpider {
   public EntityCaveSpider(World var1) {
      super(var1);
      this.setSize(0.7F, 0.5F);
   }

   public static void registerFixesCaveSpider(DataFixer var0) {
      EntityLiving.registerFixesMob(var0, "CaveSpider");
   }

   protected void applyEntityAttributes() {
      super.applyEntityAttributes();
      this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12.0D);
   }

   public boolean attackEntityAsMob(Entity var1) {
      if (super.attackEntityAsMob(var1)) {
         if (var1 instanceof EntityLivingBase) {
            byte var2 = 0;
            if (this.world.getDifficulty() == EnumDifficulty.NORMAL) {
               var2 = 7;
            } else if (this.world.getDifficulty() == EnumDifficulty.HARD) {
               var2 = 15;
            }

            if (var2 > 0) {
               ((EntityLivingBase)var1).addPotionEffect(new PotionEffect(MobEffects.POISON, var2 * 20, 0));
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Nullable
   public IEntityLivingData onInitialSpawn(DifficultyInstance var1, @Nullable IEntityLivingData var2) {
      return var2;
   }

   public float getEyeHeight() {
      return 0.45F;
   }

   @Nullable
   protected ResourceLocation getLootTable() {
      return LootTableList.ENTITIES_CAVE_SPIDER;
   }
}
