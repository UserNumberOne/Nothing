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

   protected ItemRecord(String var1, SoundEvent var2) {
      this.displayName = "item.record." + var1 + ".desc";
      this.sound = var2;
      this.maxStackSize = 1;
      this.setCreativeTab(CreativeTabs.MISC);
      RECORDS.put(this.sound, this);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      IBlockState var10 = var3.getBlockState(var4);
      if (var10.getBlock() == Blocks.JUKEBOX && !((Boolean)var10.getValue(BlockJukebox.HAS_RECORD)).booleanValue()) {
         return !var3.isRemote ? EnumActionResult.SUCCESS : EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.PASS;
      }
   }

   public EnumRarity getRarity(ItemStack var1) {
      return EnumRarity.RARE;
   }
}
