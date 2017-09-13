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
      this.displayName = "item.record." + var1 + ".desc";
      this.sound = var2;
      this.maxStackSize = 1;
      this.setCreativeTab(CreativeTabs.MISC);
      RECORDS.put(this.sound, this);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      IBlockState var10 = var3.getBlockState(var4);
      if (var10.getBlock() == Blocks.JUKEBOX && !((Boolean)var10.getValue(BlockJukebox.HAS_RECORD)).booleanValue()) {
         if (!var3.isRemote) {
            ((BlockJukebox)Blocks.JUKEBOX).insertRecord(var3, var4, var10, var1);
            var3.playEvent((EntityPlayer)null, 1010, var4, Item.getIdFromItem(this));
            --var1.stackSize;
            var2.addStat(StatList.RECORD_PLAYED);
         }

         return EnumActionResult.SUCCESS;
      } else {
         return EnumActionResult.PASS;
      }
   }

   @SideOnly(Side.CLIENT)
   public void addInformation(ItemStack var1, EntityPlayer var2, List var3, boolean var4) {
      var3.add(this.getRecordNameLocal());
   }

   public ResourceLocation getRecordResource(String var1) {
      return new ResourceLocation(var1);
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
      return (ItemRecord)RECORDS.get(var0);
   }

   @SideOnly(Side.CLIENT)
   public SoundEvent getSound() {
      return this.sound;
   }
}
