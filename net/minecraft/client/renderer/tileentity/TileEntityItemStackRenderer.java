package net.minecraft.client.renderer.tileentity;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelShield;
import net.minecraft.client.renderer.BannerTextures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TileEntityItemStackRenderer {
   public static TileEntityItemStackRenderer instance = new TileEntityItemStackRenderer();
   private final TileEntityChest chestBasic = new TileEntityChest(BlockChest.Type.BASIC);
   private final TileEntityChest chestTrap = new TileEntityChest(BlockChest.Type.TRAP);
   private final TileEntityEnderChest enderChest = new TileEntityEnderChest();
   private final TileEntityBanner banner = new TileEntityBanner();
   private final TileEntitySkull skull = new TileEntitySkull();
   private final ModelShield modelShield = new ModelShield();

   public void renderByItem(ItemStack var1) {
      if (var1.getItem() == Items.BANNER) {
         this.banner.setItemValues(var1);
         TileEntityRendererDispatcher.instance.renderTileEntityAt(this.banner, 0.0D, 0.0D, 0.0D, 0.0F);
      } else if (var1.getItem() == Items.SHIELD) {
         if (var1.getSubCompound("BlockEntityTag", false) != null) {
            this.banner.setItemValues(var1);
            Minecraft.getMinecraft().getTextureManager().bindTexture(BannerTextures.SHIELD_DESIGNS.getResourceLocation(this.banner.getPatternResourceLocation(), this.banner.getPatternList(), this.banner.getColorList()));
         } else {
            Minecraft.getMinecraft().getTextureManager().bindTexture(BannerTextures.SHIELD_BASE_TEXTURE);
         }

         GlStateManager.pushMatrix();
         GlStateManager.scale(1.0F, -1.0F, -1.0F);
         this.modelShield.render();
         GlStateManager.popMatrix();
      } else if (var1.getItem() == Items.SKULL) {
         GameProfile var2 = null;
         if (var1.hasTagCompound()) {
            NBTTagCompound var3 = var1.getTagCompound();
            if (var3.hasKey("SkullOwner", 10)) {
               var2 = NBTUtil.readGameProfileFromNBT(var3.getCompoundTag("SkullOwner"));
            } else if (var3.hasKey("SkullOwner", 8) && !var3.getString("SkullOwner").isEmpty()) {
               GameProfile var4 = new GameProfile((UUID)null, var3.getString("SkullOwner"));
               var2 = TileEntitySkull.updateGameprofile(var4);
               var3.removeTag("SkullOwner");
               var3.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), var2));
            }
         }

         if (TileEntitySkullRenderer.instance != null) {
            GlStateManager.pushMatrix();
            GlStateManager.disableCull();
            TileEntitySkullRenderer.instance.renderSkull(0.0F, 0.0F, 0.0F, EnumFacing.UP, 0.0F, var1.getMetadata(), var2, -1, 0.0F);
            GlStateManager.enableCull();
            GlStateManager.popMatrix();
         }
      } else {
         Block var5 = Block.getBlockFromItem(var1.getItem());
         if (var5 == Blocks.ENDER_CHEST) {
            TileEntityRendererDispatcher.instance.renderTileEntityAt(this.enderChest, 0.0D, 0.0D, 0.0D, 0.0F);
         } else if (var5 == Blocks.TRAPPED_CHEST) {
            TileEntityRendererDispatcher.instance.renderTileEntityAt(this.chestTrap, 0.0D, 0.0D, 0.0D, 0.0F);
         } else if (var5 != Blocks.CHEST) {
            ForgeHooksClient.renderTileItem(var1.getItem(), var1.getMetadata());
         } else {
            TileEntityRendererDispatcher.instance.renderTileEntityAt(this.chestBasic, 0.0D, 0.0D, 0.0D, 0.0F);
         }
      }

   }
}
