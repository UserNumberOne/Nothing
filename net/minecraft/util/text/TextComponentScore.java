package net.minecraft.util.text;

import net.minecraft.command.ICommandSender;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.StringUtils;

public class TextComponentScore extends TextComponentBase {
   private final String name;
   private final String objective;
   private String value = "";

   public TextComponentScore(String var1, String var2) {
      this.name = var1;
      this.objective = var2;
   }

   public String getName() {
      return this.name;
   }

   public String getObjective() {
      return this.objective;
   }

   public void setValue(String var1) {
      this.value = var1;
   }

   public String getUnformattedComponentText() {
      return this.value;
   }

   public void resolve(ICommandSender var1) {
      MinecraftServer var2 = var1.h();
      if (var2 != null && var2.M() && StringUtils.isNullOrEmpty(this.value)) {
         Scoreboard var3 = var2.getWorldServer(0).getScoreboard();
         ScoreObjective var4 = var3.getObjective(this.objective);
         if (var3.entityHasObjective(this.name, var4)) {
            Score var5 = var3.getOrCreateScore(this.name, var4);
            this.setValue(String.format("%d", var5.getScorePoints()));
            return;
         }
      }

      this.value = "";
   }

   public TextComponentScore createCopy() {
      TextComponentScore var1 = new TextComponentScore(this.name, this.objective);
      var1.setValue(this.value);
      var1.setStyle(this.getStyle().createShallowCopy());

      for(ITextComponent var3 : this.getSiblings()) {
         var1.appendSibling(var3.createCopy());
      }

      return var1;
   }

   public boolean equals(Object var1) {
      if (this == var1) {
         return true;
      } else if (!(var1 instanceof TextComponentScore)) {
         return false;
      } else {
         TextComponentScore var2 = (TextComponentScore)var1;
         return this.name.equals(var2.name) && this.objective.equals(var2.objective) && super.equals(var1);
      }
   }

   public String toString() {
      return "ScoreComponent{name='" + this.name + '\'' + "objective='" + this.objective + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
   }

   // $FF: synthetic method
   public ITextComponent createCopy() {
      return this.createCopy();
   }
}
