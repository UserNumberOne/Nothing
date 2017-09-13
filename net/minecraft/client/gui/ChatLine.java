package net.minecraft.client.gui;

import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ChatLine {
   private final int updateCounterCreated;
   private final ITextComponent lineString;
   private final int chatLineID;

   public ChatLine(int var1, ITextComponent var2, int var3) {
      this.lineString = p_i45000_2_;
      this.updateCounterCreated = p_i45000_1_;
      this.chatLineID = p_i45000_3_;
   }

   public ITextComponent getChatComponent() {
      return this.lineString;
   }

   public int getUpdatedCounter() {
      return this.updateCounterCreated;
   }

   public int getChatLineID() {
      return this.chatLineID;
   }
}
