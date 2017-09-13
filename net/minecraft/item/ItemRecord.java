package net.minecraft.item;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemRecord extends Item {
   private static final Map RECORDS = Maps.newHashMap();
   private final SoundEvent sound;
   private final String displayName;

   protected ItemRecord(String s, SoundEvent soundeffect) {
      this.displayName = "item.record." + s + ".desc";
      this.sound = soundeffect;
      this.maxStackSize = 1;
      this.setCreativeTab(CreativeTabs.MISC);
      RECORDS.put(this.sound, this);
   }

   public EnumActionResult onItemUse(ItemStack itemstack, EntityPlayer entityhuman, World world, BlockPos blockposition, EnumHand enumhand, EnumFacing enumdirection, float f, float f1, float f2) {
      IBlockState iblockdata = world.getBlockState(blockposition);
      if (iblockdata.getBlock() == Blocks.JUKEBOX && !((Boolean)iblockdata.getValue(BlockJukebox.HAS_RECORD)).booleanValue()) {
         return !world.isRemote ? EnumActionResult.SUCCESS : EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.PASS;
      }
   }

   public EnumRarity getRarity(ItemStack itemstack) {
      return EnumRarity.RARE;
   }
}
