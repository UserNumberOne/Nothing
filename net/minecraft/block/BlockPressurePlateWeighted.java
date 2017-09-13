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

   protected BlockPressurePlateWeighted(Material var1, int var2) {
      this(var1, var2, var1.getMaterialMapColor());
   }

   protected BlockPressurePlateWeighted(Material var1, int var2, MapColor var3) {
      super(var1, var3);
      this.setDefaultState(this.blockState.getBaseState().withProperty(POWER, Integer.valueOf(0)));
      this.maxWeight = var2;
   }

   protected int computeRedstoneStrength(World var1, BlockPos var2) {
      int var3 = 0;

      for(Entity var5 : var1.getEntitiesWithinAABB(Entity.class, PRESSURE_AABB.offset(var2))) {
         Object var6;
         if (var5 instanceof EntityPlayer) {
            var6 = CraftEventFactory.callPlayerInteractEvent((EntityPlayer)var5, Action.PHYSICAL, var2, (EnumFacing)null, (ItemStack)null, (EnumHand)null);
         } else {
            var6 = new EntityInteractEvent(var5.getBukkitEntity(), var1.getWorld().getBlockAt(var2.getX(), var2.getY(), var2.getZ()));
            var1.getServer().getPluginManager().callEvent((EntityInteractEvent)var6);
         }

         if (!((Cancellable)var6).isCancelled()) {
            ++var3;
         }
      }

      var3 = Math.min(var3, this.maxWeight);
      if (var3 > 0) {
         float var8 = (float)Math.min(this.maxWeight, var3) / (float)this.maxWeight;
         return MathHelper.ceil(var8 * 15.0F);
      } else {
         return 0;
      }
   }

   protected void playClickOnSound(World var1, BlockPos var2) {
      var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_METAL_PRESSPLATE_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.90000004F);
   }

   protected void playClickOffSound(World var1, BlockPos var2) {
      var1.playSound((EntityPlayer)null, var2, SoundEvents.BLOCK_METAL_PRESSPLATE_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.75F);
   }

   protected int getRedstoneStrength(IBlockState var1) {
      return ((Integer)var1.getValue(POWER)).intValue();
   }

   protected IBlockState setRedstoneStrength(IBlockState var1, int var2) {
      return var1.withProperty(POWER, Integer.valueOf(var2));
   }

   public int tickRate(World var1) {
      return 10;
   }

   public IBlockState getStateFromMeta(int var1) {
      return this.getDefaultState().withProperty(POWER, Integer.valueOf(var1));
   }

   public int getMetaFromState(IBlockState var1) {
      return ((Integer)var1.getValue(POWER)).intValue();
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{POWER});
   }
}
