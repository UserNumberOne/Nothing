package net.minecraft.scoreboard;

import java.util.Comparator;

public class Score {
   public static final Comparator SCORE_COMPARATOR = new Comparator() {
      public int compare(Score var1, Score var2) {
         return p_compare_1_.getScorePoints() > p_compare_2_.getScorePoints() ? 1 : (p_compare_1_.getScorePoints() < p_compare_2_.getScorePoints() ? -1 : p_compare_2_.getPlayerName().compareToIgnoreCase(p_compare_1_.getPlayerName()));
      }
   };
   private final Scoreboard theScoreboard;
   private final ScoreObjective theScoreObjective;
   private final String scorePlayerName;
   private int scorePoints;
   private boolean locked;
   private boolean forceUpdate;

   public Score(Scoreboard var1, ScoreObjective var2, String var3) {
      this.theScoreboard = theScoreboardIn;
      this.theScoreObjective = theScoreObjectiveIn;
      this.scorePlayerName = scorePlayerNameIn;
      this.forceUpdate = true;
   }

   public void increaseScore(int var1) {
      if (this.theScoreObjective.getCriteria().isReadOnly()) {
         throw new IllegalStateException("Cannot modify read-only score");
      } else {
         this.setScorePoints(this.getScorePoints() + amount);
      }
   }

   public void decreaseScore(int var1) {
      if (this.theScoreObjective.getCriteria().isReadOnly()) {
         throw new IllegalStateException("Cannot modify read-only score");
      } else {
         this.setScorePoints(this.getScorePoints() - amount);
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
      int i = this.scorePoints;
      this.scorePoints = points;
      if (i != points || this.forceUpdate) {
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
      this.locked = locked;
   }
}
