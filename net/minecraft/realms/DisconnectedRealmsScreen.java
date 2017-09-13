package net.minecraft.realms;

import java.util.List;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DisconnectedRealmsScreen extends RealmsScreen {
   private final String title;
   private final ITextComponent reason;
   private List lines;
   private final RealmsScreen parent;
   private int textHeight;

   public DisconnectedRealmsScreen(RealmsScreen var1, String var2, ITextComponent var3) {
      this.parent = parentIn;
      this.title = getLocalizedString(unlocalizedTitle);
      this.reason = reasonIn;
   }

   public void init() {
      Realms.setConnectedToRealms(false);
      Realms.clearResourcePack();
      this.buttonsClear();
      this.lines = this.fontSplit(this.reason.getFormattedText(), this.width() - 50);
      this.textHeight = this.lines.size() * this.fontLineHeight();
      this.buttonsAdd(newButton(0, this.width() / 2 - 100, this.height() / 2 + this.textHeight / 2 + this.fontLineHeight(), getLocalizedString("gui.back")));
   }

   public void keyPressed(char var1, int var2) {
      if (p_keyPressed_2_ == 1) {
         Realms.setScreen(this.parent);
      }

   }

   public void buttonClicked(RealmsButton var1) {
      if (p_buttonClicked_1_.id() == 0) {
         Realms.setScreen(this.parent);
      }

   }

   public void render(int var1, int var2, float var3) {
      this.renderBackground();
      this.drawCenteredString(this.title, this.width() / 2, this.height() / 2 - this.textHeight / 2 - this.fontLineHeight() * 2, 11184810);
      int i = this.height() / 2 - this.textHeight / 2;
      if (this.lines != null) {
         for(String s : this.lines) {
            this.drawCenteredString(s, this.width() / 2, i, 16777215);
            i += this.fontLineHeight();
         }
      }

      super.render(p_render_1_, p_render_2_, p_render_3_);
   }
}
