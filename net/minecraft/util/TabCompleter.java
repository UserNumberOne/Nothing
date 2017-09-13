package net.minecraft.util;

import com.google.common.collect.Lists;
import com.google.common.collect.ObjectArrays;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.network.play.client.CPacketTabComplete;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public abstract class TabCompleter {
   protected final GuiTextField textField;
   protected final boolean hasTargetBlock;
   protected boolean didComplete;
   protected boolean requestedCompletions;
   protected int completionIdx;
   protected List completions = Lists.newArrayList();

   public TabCompleter(GuiTextField var1, boolean var2) {
      this.textField = var1;
      this.hasTargetBlock = var2;
   }

   public void complete() {
      if (this.didComplete) {
         this.textField.deleteFromCursor(0);
         this.textField.deleteFromCursor(this.textField.getNthWordFromPosWS(-1, this.textField.getCursorPosition(), false) - this.textField.getCursorPosition());
         if (this.completionIdx >= this.completions.size()) {
            this.completionIdx = 0;
         }
      } else {
         int var1 = this.textField.getNthWordFromPosWS(-1, this.textField.getCursorPosition(), false);
         this.completions.clear();
         this.completionIdx = 0;
         String var2 = this.textField.getText().substring(0, this.textField.getCursorPosition());
         this.requestCompletions(var2);
         if (this.completions.isEmpty()) {
            return;
         }

         this.didComplete = true;
         this.textField.deleteFromCursor(var1 - this.textField.getCursorPosition());
      }

      this.textField.writeText(TextFormatting.getTextWithoutFormattingCodes((String)this.completions.get(this.completionIdx++)));
   }

   private void requestCompletions(String var1) {
      if (var1.length() >= 1) {
         ClientCommandHandler.instance.autoComplete(var1);
         Minecraft.getMinecraft().player.connection.sendPacket(new CPacketTabComplete(var1, this.getTargetBlockPos(), this.hasTargetBlock));
         this.requestedCompletions = true;
      }

   }

   @Nullable
   public abstract BlockPos getTargetBlockPos();

   public void setCompletions(String... var1) {
      if (this.requestedCompletions) {
         this.didComplete = false;
         this.completions.clear();
         String[] var2 = ClientCommandHandler.instance.latestAutoComplete;
         if (var2 != null) {
            var1 = (String[])ObjectArrays.concat(var2, var1, String.class);
         }

         for(String var6 : var1) {
            if (!var6.isEmpty()) {
               this.completions.add(var6);
            }
         }

         String var7 = this.textField.getText().substring(this.textField.getNthWordFromPosWS(-1, this.textField.getCursorPosition(), false));
         String var8 = org.apache.commons.lang3.StringUtils.getCommonPrefix(var1);
         var8 = TextFormatting.getTextWithoutFormattingCodes(var8);
         if (!var8.isEmpty() && !var7.equalsIgnoreCase(var8)) {
            this.textField.deleteFromCursor(0);
            this.textField.deleteFromCursor(this.textField.getNthWordFromPosWS(-1, this.textField.getCursorPosition(), false) - this.textField.getCursorPosition());
            this.textField.writeText(var8);
         } else if (!this.completions.isEmpty()) {
            this.didComplete = true;
            this.complete();
         }
      }

   }

   public void resetDidComplete() {
      this.didComplete = false;
   }

   public void resetRequested() {
      this.requestedCompletions = false;
   }
}
