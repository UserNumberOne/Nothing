package net.minecraft.client.gui.spectator.categories;

import com.google.common.base.Objects;
import java.util.List;
import net.minecraft.client.gui.spectator.ISpectatorMenuObject;
import net.minecraft.client.gui.spectator.ISpectatorMenuView;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SpectatorDetails {
   private final ISpectatorMenuView category;
   private final List items;
   private final int selectedSlot;

   public SpectatorDetails(ISpectatorMenuView var1, List var2, int var3) {
      this.category = p_i45494_1_;
      this.items = p_i45494_2_;
      this.selectedSlot = p_i45494_3_;
   }

   public ISpectatorMenuObject getObject(int var1) {
      return p_178680_1_ >= 0 && p_178680_1_ < this.items.size() ? (ISpectatorMenuObject)Objects.firstNonNull(this.items.get(p_178680_1_), SpectatorMenu.EMPTY_SLOT) : SpectatorMenu.EMPTY_SLOT;
   }

   public int getSelectedSlot() {
      return this.selectedSlot;
   }
}
