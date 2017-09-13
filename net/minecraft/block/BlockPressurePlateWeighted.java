package net.minecraft.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.event.Cancellable;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityInteractEvent;

public class BlockPressurePlateWeighted extends BlockBasePressurePlate {
   public static final PropertyInteger POWER = PropertyInteger.create("power", 0, 15);
   private final int maxWeight;

   protected BlockPressurePlateWeighted(Material material, int i) {
      this(material, i, material.getMaterialMapColor());
   }

   protected BlockPressurePlateWeighted(Material material, int i, MapColor materialmapcolor) {
      super(material, materialmapcolor);
      this.setDefaultState(this.blockState.getBaseState().withProperty(POWER, Integer.valueOf(0)));
      this.maxWeight = i;
   }

   protected int computeRedstoneStrength(World world, BlockPos blockposition) {
      int i = 0;

      for(Entity entity : world.getEntitiesWithinAABB(Entity.class, PRESSURE_AABB.offset(blockposition))) {
         Cancellable cancellable;
         if (entity instanceof EntityPlayer) {
            cancellable = CraftEventFactory.callPlayerInteractEvent((EntityPlayer)entity, Action.PHYSICAL, blockposition, (EnumFacing)null, (ItemStack)null, (EnumHand)null);
         } else {
            cancellable = new EntityInteractEvent(entity.getBukkitEntity(), world.getWorld().getBlockAt(blockposition.getX(), blockposition.getY(), blockposition.getZ()));
            world.getServer().getPluginManager().callEvent((EntityInteractEvent)cancellable);
         }

         if (!cancellable.isCancelled()) {
            ++i;
         }
      }

      i = Math.min(i, this.maxWeight);
      if (i > 0) {
         float f = (float)Math.min(this.maxWeight, i) / (float)this.maxWeight;
         return MathHelper.ceil(f * 15.0F);
      } else {
         return 0;
      }
   }

   protected void playClickOnSound(World world, BlockPos blockposition) {
      world.playSound((EntityPlayer)null, blockposition, SoundEvents.BLOCK_METAL_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.90000004F);
   }

   protected void playClickOffSound(World world, BlockPos blockposition) {
      world.playSound((EntityPlayer)null, blockposition, SoundEvents.BLOCK_METAL_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.75F);
   }

   protected int getRedstoneStrength(IBlockState iblockdata) {
      return ((Integer)iblockdata.getValue(POWER)).intValue();
   }

   protected IBlockState setRedstoneStrength(IBlockState iblockdata, int i) {
      return iblockdata.withProperty(POWER, Integer.valueOf(i));
   }

   public int tickRate(World world) {
      return 10;
   }

   public IBlockState getStateFromMeta(int i) {
      return this.getDefaultState().withProperty(POWER, Integer.valueOf(i));
   }

   public int getMetaFromState(IBlockState iblockdata) {
      return ((Integer)iblockdata.getValue(POWER)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{POWER});
   }
}
