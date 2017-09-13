package net.minecraft.client.gui.spectator.categories;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSpectator;
import net.minecraft.client.gui.spectator.ISpectatorMenuObject;
import net.minecraft.client.gui.spectator.ISpectatorMenuView;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TeleportToTeam implements ISpectatorMenuView, ISpectatorMenuObject {
   private final List items = Lists.newArrayList();

   public TeleportToTeam() {
      Minecraft var1 = Minecraft.getMinecraft();

      for(ScorePlayerTeam var3 : var1.world.getScoreboard().getTeams()) {
         this.items.add(new TeleportToTeam.TeamSelectionObject(var3));
      }

   }

   public List getItems() {
      return this.items;
   }

   public ITextComponent getPrompt() {
      return new TextComponentString("Select a team to teleport to");
   }

   public void selectItem(SpectatorMenu var1) {
      var1.selectCategory(this);
   }

   public ITextComponent getSpectatorName() {
      return new TextComponentString("Teleport to team member");
   }

   public void renderIcon(float var1, int var2) {
      Minecraft.getMinecraft().getTextureManager().bindTexture(GuiSpectator.SPECTATOR_WIDGETS);
      Gui.drawModalRectWithCustomSizedTexture(0, 0, 16.0F, 0.0F, 16, 16, 256.0F, 256.0F);
   }

   public boolean isEnabled() {
      for(ISpectatorMenuObject var2 : this.items) {
         if (var2.isEnabled()) {
            return true;
         }
      }

      return false;
   }

   @SideOnly(Side.CLIENT)
   class TeamSelectionObject implements ISpectatorMenuObject {
      private final ScorePlayerTeam team;
      private final ResourceLocation location;
      private final List players;

      public TeamSelectionObject(ScorePlayerTeam var2) {
         this.team = var2;
         this.players = Lists.newArrayList();

         for(String var4 : var2.getMembershipCollection()) {
            NetworkPlayerInfo var5 = Minecraft.getMinecraft().getConnection().getPlayerInfo(var4);
            if (var5 != null) {
               this.players.add(var5);
            }
         }

         if (this.players.isEmpty()) {
            this.location = DefaultPlayerSkin.getDefaultSkinLegacy();
         } else {
            String var6 = ((NetworkPlayerInfo)this.players.get((new Random()).nextInt(this.players.size()))).getGameProfile().getName();
            this.location = AbstractClientPlayer.getLocationSkin(var6);
            AbstractClientPlayer.getDownloadImageSkin(this.location, var6);
         }

      }

      public void selectItem(SpectatorMenu var1) {
         var1.selectCategory(new TeleportToPlayer(this.players));
      }

      public ITextComponent getSpectatorName() {
         return new TextComponentString(this.team.getTeamName());
      }

      public void renderIcon(float var1, int var2) {
         int var3 = -1;
         String var4 = FontRenderer.getFormatFromString(this.team.getColorPrefix());
         if (var4.length() >= 2) {
            var3 = Minecraft.getMinecraft().fontRendererObj.getColorCode(var4.charAt(1));
         }

         if (var3 >= 0) {
            float var5 = (float)(var3 >> 16 & 255) / 255.0F;
            float var6 = (float)(var3 >> 8 & 255) / 255.0F;
            float var7 = (float)(var3 & 255) / 255.0F;
            Gui.drawRect(1, 1, 15, 15, MathHelper.rgb(var5 * var1, var6 * var1, var7 * var1) | var2 << 24);
         }

         Minecraft.getMinecraft().getTextureManager().bindTexture(this.location);
         GlStateManager.color(var1, var1, var1, (float)var2 / 255.0F);
         Gui.drawScaledCustomSizeModalRect(2, 2, 8.0F, 8.0F, 8, 8, 12, 12, 64.0F, 64.0F);
         Gui.drawScaledCustomSizeModalRect(2, 2, 40.0F, 8.0F, 8, 8, 12, 12, 64.0F, 64.0F);
      }

      public boolean isEnabled() {
         return !this.players.isEmpty();
      }
   }
}
