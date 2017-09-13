package net.minecraft.item;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

public class ItemSkull extends Item {
   private static final String[] SKULL_TYPES = new String[]{"skeleton", "wither", "zombie", "char", "creeper", "dragon"};

   public ItemSkull() {
      this.setCreativeTab(CreativeTabs.DECORATIONS);
      this.setMaxDamage(0);
      this.setHasSubtypes(true);
   }

   public EnumActionResult onItemUse(ItemStack var1, EntityPlayer var2, World var3, BlockPos var4, EnumHand var5, EnumFacing var6, float var7, float var8, float var9) {
      if (var6 == EnumFacing.DOWN) {
         return EnumActionResult.FAIL;
      } else {
         IBlockState var10 = var3.getBlockState(var4);
         Block var11 = var10.getBlock();
         boolean var12 = var11.isReplaceable(var3, var4);
         if (!var12) {
            if (!var3.getBlockState(var4).getMaterial().isSolid()) {
               return EnumActionResult.FAIL;
            }

            var4 = var4.offset(var6);
         }

         if (var2.canPlayerEdit(var4, var6, var1) && Blocks.SKULL.canPlaceBlockAt(var3, var4)) {
            if (var3.isRemote) {
               return EnumActionResult.SUCCESS;
            } else {
               var3.setBlockState(var4, Blocks.SKULL.getDefaultState().withProperty(BlockSkull.FACING, var6), 11);
               int var13 = 0;
               if (var6 == EnumFacing.UP) {
                  var13 = MathHelper.floor((double)(var2.rotationYaw * 16.0F / 360.0F) + 0.5D) & 15;
               }

               TileEntity var14 = var3.getTileEntity(var4);
               if (var14 instanceof TileEntitySkull) {
                  TileEntitySkull var15 = (TileEntitySkull)var14;
                  if (var1.getMetadata() == 3) {
                     GameProfile var16 = null;
                     if (var1.hasTagCompound()) {
                        NBTTagCompound var17 = var1.getTagCompound();
                        if (var17.hasKey("SkullOwner", 10)) {
                           var16 = NBTUtil.readGameProfileFromNBT(var17.getCompoundTag("SkullOwner"));
                        } else if (var17.hasKey("SkullOwner", 8) && !var17.getString("SkullOwner").isEmpty()) {
                           var16 = new GameProfile((UUID)null, var17.getString("SkullOwner"));
                        }
                     }

                     var15.setPlayerProfile(var16);
                  } else {
                     var15.setType(var1.getMetadata());
                  }

                  var15.setSkullRotation(var13);
                  Blocks.SKULL.checkWitherSpawn(var3, var4, var15);
               }

               --var1.stackSize;
               return EnumActionResult.SUCCESS;
            }
         } else {
            return EnumActionResult.FAIL;
         }
      }
   }

   public int getMetadata(int var1) {
      return var1;
   }

   public String getUnlocalizedName(ItemStack var1) {
      int var2 = var1.getMetadata();
      if (var2 < 0 || var2 >= SKULL_TYPES.length) {
         var2 = 0;
      }

      return super.getUnlocalizedName() + "." + SKULL_TYPES[var2];
   }

   public String getItemStackDisplayName(ItemStack var1) {
      if (var1.getMetadata() == 3 && var1.hasTagCompound()) {
         if (var1.getTagCompound().hasKey("SkullOwner", 8)) {
            return I18n.translateToLocalFormatted("item.skull.player.name", var1.getTagCompound().getString("SkullOwner"));
         }

         if (var1.getTagCompound().hasKey("SkullOwner", 10)) {
            NBTTagCompound var2 = var1.getTagCompound().getCompoundTag("SkullOwner");
            if (var2.hasKey("Name", 8)) {
               return I18n.translateToLocalFormatted("item.skull.player.name", var2.getString("Name"));
            }
         }
      }

      return super.getItemStackDisplayName(var1);
   }

   public boolean updateItemStackNBT(NBTTagCompound var1) {
      super.updateItemStackNBT(var1);
      if (var1.hasKey("SkullOwner", 8) && !var1.getString("SkullOwner").isEmpty()) {
         GameProfile var4 = new GameProfile((UUID)null, var1.getString("SkullOwner"));
         var4 = TileEntitySkull.updateGameprofile(var4);
         var1.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), var4));
         return true;
      } else {
         NBTTagList var2 = var1.getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", 10);

         for(int var3 = 0; var3 < var2.tagCount(); ++var3) {
            if (var2.getCompoundTagAt(var3) instanceof NBTTagCompound && !var2.getCompoundTagAt(var3).hasKey("Signature", 8) && var2.getCompoundTagAt(var3).getString("Value").trim().isEmpty()) {
               var1.removeTag("SkullOwner");
               break;
            }
         }

         return false;
      }
   }
}
