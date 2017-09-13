package net.minecraft.scoreboard;

import java.util.Comparator;

public class Score {
   public static final Comparator SCORE_COMPARATOR = new Comparator() {
      public int compare(Score var1, Score var2) {
         return var1.getScorePoints() > var2.getScorePoints() ? 1 : (var1.getScorePoints() < var2.getScorePoints() ? -1 : var2.getPlayerName().compareToIgnoreCase(var1.getPlayerName()));
      }
   };
   private final Scoreboard theScoreboard;
   private final ScoreObjective theScoreObjective;
   private final String scorePlayerName;
   private int scorePoints;
   private boolean locked;
   private boolean forceUpdate;

   public Score(Scoreboard var1, ScoreObjective var2, String var3) {
      this.theScoreboard = var1;
      this.theScoreObjective = var2;
      this.scorePlayerName = var3;
      this.forceUpdate = true;
   }

   public void increaseScore(int var1) {
      if (this.theScoreObjective.getCriteria().isReadOnly()) {
         throw new IllegalStateException("Cannot modify read-only score");
      } else {
         this.setScorePoints(this.getScorePoints() + var1);
      }
   }

   public void decreaseScore(int var1) {
      if (this.theScoreObjective.getCriteria().isReadOnly()) {
         throw new IllegalStateException("Cannot modify read-only score");
      } else {
         this.setScorePoints(this.getScorePoints() - var1);
      }
   }

   public void incrementScore() {
      if (this.theScoreObjective.getCriteria().isReadOnly()) {
         throw new IllegalStateException("Cannot modify read-only score");
      } else {
         this.increaseScore(1);
      }
   }

   public int getScorePoints() {
      return this.scorePoints;
   }

   public void setScorePoints(int var1) {
      int var2 = this.scorePoints;
      this.scorePoints = var1;
      if (var2 != var1 || this.forceUpdate) {
         this.forceUpdate = false;
         this.getScoreScoreboard().onScoreUpdated(this);
      }

   }

   public ScoreObjective getObjective() {
      return this.theScoreObjective;
   }

   public String getPlayerName() {
      return this.scorePlayerName;
   }

   public Scoreboard getScoreScoreboard() {
      return this.theScoreboard;
   }

   public boolean isLocked() {
      return this.locked;
   }

   public void setLocked(boolean var1) {
      this.locked = var1;
   }
}
