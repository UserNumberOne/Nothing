package net.minecraft.client.gui.spectator.categories;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSpectator;
import net.minecraft.client.gui.spectator.ISpectatorMenuObject;
import net.minecraft.client.gui.spectator.ISpectatorMenuView;
import net.minecraft.client.gui.spectator.PlayerMenuObject;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TeleportToPlayer implements ISpectatorMenuView, ISpectatorMenuObject {
   private static final Ordering PROFILE_ORDER = Ordering.from(new Comparator() {
      public int compare(NetworkPlayerInfo var1, NetworkPlayerInfo var2) {
         return ComparisonChain.start().compare(var1.getGameProfile().getId(), var2.getGameProfile().getId()).result();
      }
   });
   private final List items;

   public TeleportToPlayer() {
      this(PROFILE_ORDER.sortedCopy(Minecraft.getMinecraft().getConnection().getPlayerInfoMap()));
   }

   public TeleportToPlayer(Collection var1) {
      this.items = Lists.newArrayList();

      for(NetworkPlayerInfo var3 : PROFILE_ORDER.sortedCopy(var1)) {
         if (var3.getGameType() != GameType.SPECTATOR) {
            this.items.add(new PlayerMenuObject(var3.getGameProfile()));
         }
      }

   }

   public List getItems() {
      return this.items;
   }

   public ITextComponent getPrompt() {
      return new TextComponentString("Select a player to teleport to");
   }

   public void selectItem(SpectatorMenu var1) {
      var1.selectCategory(this);
   }

   public ITextComponent getSpectatorName() {
      return new TextComponentString("Teleport to player");
   }

   public void renderIcon(float var1, int var2) {
      Minecraft.getMinecraft().getTextureManager().bindTexture(GuiSpectator.SPECTATOR_WIDGETS);
      Gui.drawModalRectWithCustomSizedTexture(0, 0, 0.0F, 0.0F, 16, 16, 256.0F, 256.0F);
   }

   public boolean isEnabled() {
      return !this.items.isEmpty();
   }
}
