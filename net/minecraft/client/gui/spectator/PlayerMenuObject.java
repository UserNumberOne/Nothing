package net.minecraft.client.gui.spectator;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.play.client.CPacketSpectate;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class PlayerMenuObject implements ISpectatorMenuObject {
   private final GameProfile profile;
   private final ResourceLocation resourceLocation;

   public PlayerMenuObject(GameProfile var1) {
      this.profile = var1;
      this.resourceLocation = AbstractClientPlayer.getLocationSkin(var1.getName());
      AbstractClientPlayer.getDownloadImageSkin(this.resourceLocation, var1.getName());
   }

   public void selectItem(SpectatorMenu var1) {
      Minecraft.getMinecraft().getConnection().sendPacket(new CPacketSpectate(this.profile.getId()));
   }

   public ITextComponent getSpectatorName() {
      return new TextComponentString(this.profile.getName());
   }

   public void renderIcon(float var1, int var2) {
      Minecraft.getMinecraft().getTextureManager().bindTexture(this.resourceLocation);
      GlStateManager.color(1.0F, 1.0F, 1.0F, (float)var2 / 255.0F);
      Gui.drawScaledCustomSizeModalRect(2, 2, 8.0F, 8.0F, 8, 8, 12, 12, 64.0F, 64.0F);
      Gui.drawScaledCustomSizeModalRect(2, 2, 40.0F, 8.0F, 8, 8, 12, 12, 64.0F, 64.0F);
   }

   public boolean isEnabled() {
      return true;
   }
}
