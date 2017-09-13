package net.minecraft.client.gui;

import net.minecraft.client.settings.GameSettings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiOptionButton extends GuiButton {
   private final GameSettings.Options enumOptions;

   public GuiOptionButton(int var1, int var2, int var3, String var4) {
      this(p_i45011_1_, p_i45011_2_, p_i45011_3_, (GameSettings.Options)null, p_i45011_4_);
   }

   public GuiOptionButton(int var1, int var2, int var3, int var4, int var5, String var6) {
      super(p_i45012_1_, p_i45012_2_, p_i45012_3_, p_i45012_4_, p_i45012_5_, p_i45012_6_);
      this.enumOptions = null;
   }

   public GuiOptionButton(int var1, int var2, int var3, GameSettings.Options var4, String var5) {
      super(p_i45013_1_, p_i45013_2_, p_i45013_3_, 150, 20, p_i45013_5_);
      this.enumOptions = p_i45013_4_;
   }

   public GameSettings.Options returnEnumOptions() {
      return this.enumOptions;
   }
}
