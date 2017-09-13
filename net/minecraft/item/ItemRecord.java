package net.minecraft.item;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemRecord extends Item {
   private static final Map RECORDS = Maps.newHashMap();
   private final SoundEvent sound;
   private final String displayName;

   protected ItemRecord(String var1, SoundEvent var2) {
      this.displayName = "item.record." + p_i46742_1_ + ".desc";
      this.sound = soundIn;
      this.maxStackSize = 1;
      this.setCreativeTab(CreativeTabs.MISC);
      RECORDS.put(this.sound, this);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      IBlockState iblockstate = worldIn.getBlockState(pos);
      if (iblockstate.getBlock() == Blocks.JUKEBOX && !((Boolean)iblockstate.getValue(BlockJukebox.HAS_RECORD)).booleanValue()) {
         if (!worldIn.isRemote) {
            ((BlockJukebox)Blocks.JUKEBOX).insertRecord(worldIn, pos, iblockstate, stack);
            worldIn.playEvent((EntityPlayer)null, 1010, pos, Item.getIdFromItem(this));
            --stack.stackSize;
            playerIn.addStat(StatList.RECORD_PLAYED);
         }

         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.PASS;
      }
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
      tooltip.add(this.getRecordNameLocal());
   }

   public ResourceLocation getRecordResource(String var1) {
      return new ResourceLocation(name);
   }

   @SideOnly(Side.CLIENT)
   public String getRecordNameLocal() {
      return I18n.translateToLocal(this.displayName);
   }

   public EnumRarity getRarity(ItemStack var1) {
      return EnumRarity.RARE;
   }

   @Nullable
   @SideOnly(Side.CLIENT)
   public static ItemRecord getBySound(SoundEvent var0) {
      return (ItemRecord)RECORDS.get(soundIn);
   }

   @SideOnly(Side.CLIENT)
   public SoundEvent getSound() {
      return this.sound;
   }
}
