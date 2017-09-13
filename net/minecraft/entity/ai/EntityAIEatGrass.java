package net.minecraft.entity.ai;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.state.pattern.BlockStateMatcher;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;

public class EntityAIEatGrass extends EntityAIBase {
   private static final Predicate IS_TALL_GRASS = BlockStateMatcher.forBlock(Blocks.TALLGRASS).where(BlockTallGrass.TYPE, Predicates.equalTo(BlockTallGrass.EnumType.GRASS));
   private final EntityLiving grassEaterEntity;
   private final World entityWorld;
   int eatingGrassTimer;

   public EntityAIEatGrass(EntityLiving var1) {
      this.grassEaterEntity = var1;
      this.entityWorld = var1.world;
      this.setMutexBits(7);
   }

   public boolean shouldExecute() {
      if (this.grassEaterEntity.getRNG().nextInt(this.grassEaterEntity.isChild() ? 50 : 1000) != 0) {
         return false;
      } else {
         BlockPos var1 = new BlockPos(this.grassEaterEntity.posX, this.grassEaterEntity.posY, this.grassEaterEntity.posZ);
         return IS_TALL_GRASS.apply(this.entityWorld.getBlockState(var1)) ? true : this.entityWorld.getBlockState(var1.down()).getBlock() == Blocks.GRASS;
      }
   }

   public void startExecuting() {
      this.eatingGrassTimer = 40;
      this.entityWorld.setEntityState(this.grassEaterEntity, (byte)10);
      this.grassEaterEntity.getNavigator().clearPathEntity();
   }

   public void resetTask() {
      this.eatingGrassTimer = 0;
   }

   public boolean continueExecuting() {
      return this.eatingGrassTimer > 0;
   }

   public int getEatingGrassTimer() {
      return this.eatingGrassTimer;
   }

   public void updateTask() {
      this.eatingGrassTimer = Math.max(0, this.eatingGrassTimer - 1);
      if (this.eatingGrassTimer == 4) {
         BlockPos var1 = new BlockPos(this.grassEaterEntity.posX, this.grassEaterEntity.posY, this.grassEaterEntity.posZ);
         if (IS_TALL_GRASS.apply(this.entityWorld.getBlockState(var1))) {
            if (!CraftEventFactory.callEntityChangeBlockEvent(this.grassEaterEntity, this.grassEaterEntity.world.getWorld().getBlockAt(var1.getX(), var1.getY(), var1.getZ()), Material.AIR, !this.entityWorld.getGameRules().getBoolean("mobGriefing")).isCancelled()) {
               this.entityWorld.destroyBlock(var1, false);
            }

            this.grassEaterEntity.eatGrassBonus();
         } else {
            BlockPos var2 = var1.down();
            if (this.entityWorld.getBlockState(var2).getBlock() == Blocks.GRASS) {
               if (!CraftEventFactory.callEntityChangeBlockEvent(this.grassEaterEntity, this.grassEaterEntity.world.getWorld().getBlockAt(var1.getX(), var1.getY(), var1.getZ()), Material.AIR, !this.entityWorld.getGameRules().getBoolean("mobGriefing")).isCancelled()) {
                  this.entityWorld.playEvent(2001, var2, Block.getIdFromBlock(Blocks.GRASS));
                  this.entityWorld.setBlockState(var2, Blocks.DIRT.getDefaultState(), 2);
               }

               this.grassEaterEntity.eatGrassBonus();
            }
         }
      }

   }
}
