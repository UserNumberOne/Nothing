package net.minecraft.server.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import javax.swing.JComponent;
import javax.swing.Timer;
import net.minecraft.src.MinecraftServer;

public class StatsComponent extends JComponent {
   private static final DecimalFormat FORMATTER = new DecimalFormat("########0.000");
   private final int[] values = new int[256];
   private int vp;
   private final String[] msgs = new String[11];
   private final MinecraftServer server;

   public StatsComponent(MinecraftServer var1) {
      this.server = var1;
      this.setPreferredSize(new Dimension(456, 246));
      this.setMinimumSize(new Dimension(456, 246));
      this.setMaximumSize(new Dimension(456, 246));
      (new Timer(500, new ActionListener() {
         public void actionPerformed(ActionEvent var1) {
            StatsComponent.this.tick();
         }
      })).start();
      this.setBackground(Color.BLACK);
   }

   private void tick() {
      long var1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
      System.gc();
      this.msgs[0] = "Memory use: " + var1 / 1024L / 1024L + " mb (" + Runtime.getRuntime().freeMemory() * 100L / Runtime.getRuntime().maxMemory() + "% free)";
      this.msgs[1] = "Avg tick: " + FORMATTER.format(this.mean(this.server.h) * 1.0E-6D) + " ms";
      this.repaint();
   }

   private double mean(long[] var1) {
      long var2 = 0L;

      for(long var7 : var1) {
         var2 += var7;
      }

      return (double)var2 / (double)var1.length;
   }

   public void paint(Graphics var1) {
      var1.setColor(new Color(16777215));
      var1.fillRect(0, 0, 456, 246);

      for(int var2 = 0; var2 < 256; ++var2) {
         int var3 = this.values[var2 + this.vp & 255];
         var1.setColor(new Color(var3 + 28 << 16));
         var1.fillRect(var2, 100 - var3, 1, var3);
      }

      var1.setColor(Color.BLACK);

      for(int var4 = 0; var4 < this.msgs.length; ++var4) {
         String var5 = this.msgs[var4];
         if (var5 != null) {
            var1.drawString(var5, 32, 116 + var4 * 16);
         }
      }

   }
}
