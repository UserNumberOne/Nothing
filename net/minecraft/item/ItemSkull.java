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

   public EnumActionResult onItemUse(ItemStack itemstack, EntityPlayer entityhuman, World world, BlockPos blockposition, EnumHand enumhand, EnumFacing enumdirection, float f, float f1, float f2) {
      if (enumdirection == EnumFacing.DOWN) {
         return EnumActionResult.FAIL;
      } else {
         IBlockState iblockdata = world.getBlockState(blockposition);
         Block block = iblockdata.getBlock();
         boolean flag = block.isReplaceable(world, blockposition);
         if (!flag) {
            if (!world.getBlockState(blockposition).getMaterial().isSolid()) {
               return EnumActionResult.FAIL;
            }

            blockposition = blockposition.offset(enumdirection);
         }

         if (entityhuman.canPlayerEdit(blockposition, enumdirection, itemstack) && Blocks.SKULL.canPlaceBlockAt(world, blockposition)) {
            if (world.isRemote) {
               return EnumActionResult.SUCCESS;
            } else {
               world.setBlockState(blockposition, Blocks.SKULL.getDefaultState().withProperty(BlockSkull.FACING, enumdirection), 11);
               int i = 0;
               if (enumdirection == EnumFacing.UP) {
                  i = MathHelper.floor((double)(entityhuman.rotationYaw * 16.0F / 360.0F) + 0.5D) & 15;
               }

               TileEntity tileentity = world.getTileEntity(blockposition);
               if (tileentity instanceof TileEntitySkull) {
                  TileEntitySkull tileentityskull = (TileEntitySkull)tileentity;
                  if (itemstack.getMetadata() == 3) {
                     GameProfile gameprofile = null;
                     if (itemstack.hasTagCompound()) {
                        NBTTagCompound nbttagcompound = itemstack.getTagCompound();
                        if (nbttagcompound.hasKey("SkullOwner", 10)) {
                           gameprofile = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("SkullOwner"));
                        } else if (nbttagcompound.hasKey("SkullOwner", 8) && !nbttagcompound.getString("SkullOwner").isEmpty()) {
                           gameprofile = new GameProfile((UUID)null, nbttagcompound.getString("SkullOwner"));
                        }
                     }

                     tileentityskull.setPlayerProfile(gameprofile);
                  } else {
                     tileentityskull.setType(itemstack.getMetadata());
                  }

                  tileentityskull.setSkullRotation(i);
                  Blocks.SKULL.checkWitherSpawn(world, blockposition, tileentityskull);
               }

               --itemstack.stackSize;
               return EnumActionResult.SUCCESS;
            }
         } else {
            return EnumActionResult.FAIL;
         }
      }
   }

   public int getMetadata(int i) {
      return i;
   }

   public String getUnlocalizedName(ItemStack itemstack) {
      int i = itemstack.getMetadata();
      if (i < 0 || i >= SKULL_TYPES.length) {
         i = 0;
      }

      return super.getUnlocalizedName() + "." + SKULL_TYPES[i];
   }

   public String getItemStackDisplayName(ItemStack itemstack) {
      if (itemstack.getMetadata() == 3 && itemstack.hasTagCompound()) {
         if (itemstack.getTagCompound().hasKey("SkullOwner", 8)) {
            return I18n.translateToLocalFormatted("item.skull.player.name", itemstack.getTagCompound().getString("SkullOwner"));
         }

         if (itemstack.getTagCompound().hasKey("SkullOwner", 10)) {
            NBTTagCompound nbttagcompound = itemstack.getTagCompound().getCompoundTag("SkullOwner");
            if (nbttagcompound.hasKey("Name", 8)) {
               return I18n.translateToLocalFormatted("item.skull.player.name", nbttagcompound.getString("Name"));
            }
         }
      }

      return super.getItemStackDisplayName(itemstack);
   }

   public boolean updateItemStackNBT(NBTTagCompound nbttagcompound) {
      super.updateItemStackNBT(nbttagcompound);
      if (nbttagcompound.hasKey("SkullOwner", 8) && !nbttagcompound.getString("SkullOwner").isEmpty()) {
         GameProfile gameprofile = new GameProfile((UUID)null, nbttagcompound.getString("SkullOwner"));
         gameprofile = TileEntitySkull.updateGameprofile(gameprofile);
         nbttagcompound.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), gameprofile));
         return true;
      } else {
         NBTTagList textures = nbttagcompound.getCompoundTag("SkullOwner").getCompoundTag("Properties").getTagList("textures", 10);

         for(int i = 0; i < textures.tagCount(); ++i) {
            if (textures.getCompoundTagAt(i) instanceof NBTTagCompound && !textures.getCompoundTagAt(i).hasKey("Signature", 8) && textures.getCompoundTagAt(i).getString("Value").trim().isEmpty()) {
               nbttagcompound.removeTag("SkullOwner");
               break;
            }
         }

         return false;
      }
   }
}
