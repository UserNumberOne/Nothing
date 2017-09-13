package net.minecraft.world.demo;

import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class DemoWorldManager extends PlayerInteractionManager {
   private boolean displayedIntro;
   private boolean demoTimeExpired;
   private int demoEndedReminder;
   private int gameModeTicks;

   public DemoWorldManager(World var1) {
      super(var1);
   }

   public void updateBlockRemoving() {
      super.updateBlockRemoving();
      ++this.gameModeTicks;
      long var1 = this.world.getTotalWorldTime();
      long var3 = var1 / 24000L + 1L;
      if (!this.displayedIntro && this.gameModeTicks > 20) {
         this.displayedIntro = true;
         this.player.connection.sendPacket(new SPacketChangeGameState(5, 0.0F));
      }

      this.demoTimeExpired = var1 > 120500L;
      if (this.demoTimeExpired) {
         ++this.demoEndedReminder;
      }

      if (var1 % 24000L == 500L) {
         if (var3 <= 6L) {
            this.player.sendMessage(new TextComponentTranslation("demo.day." + var3, new Object[0]));
         }
      } else if (var3 == 1L) {
         if (var1 == 100L) {
            this.player.connection.sendPacket(new SPacketChangeGameState(5, 101.0F));
         } else if (var1 == 175L) {
            this.player.connection.sendPacket(new SPacketChangeGameState(5, 102.0F));
         } else if (var1 == 250L) {
            this.player.connection.sendPacket(new SPacketChangeGameState(5, 103.0F));
         }
      } else if (var3 == 5L && var1 % 24000L == 22000L) {
         this.player.sendMessage(new TextComponentTranslation("demo.day.warning", new Object[0]));
      }

   }

   private void sendDemoReminder() {
      if (this.demoEndedReminder > 100) {
         this.player.sendMessage(new TextComponentTranslation("demo.reminder", new Object[0]));
         this.demoEndedReminder = 0;
      }

   }

   public void onBlockClicked(BlockPos var1, EnumFacing var2) {
      if (this.demoTimeExpired) {
         this.sendDemoReminder();
      } else {
         super.onBlockClicked(var1, var2);
      }

   }

   public void blockRemoving(BlockPos var1) {
      if (!this.demoTimeExpired) {
         super.blockRemoving(var1);
      }

   }

   public boolean tryHarvestBlock(BlockPos var1) {
      return this.demoTimeExpired ? false : super.tryHarvestBlock(var1);
   }

   public EnumActionResult processRightClick(EntityPlayer var1, World var2, ItemStack var3, EnumHand var4) {
      if (this.demoTimeExpired) {
         this.sendDemoReminder();
         return EnumActionResult.PASS;
      } else {
         return super.processRightClick(var1, var2, var3, var4);
      }
   }

   public EnumActionResult processRightClickBlock(EntityPlayer var1, World var2, @Nullable ItemStack var3, EnumHand var4, BlockPos var5, EnumFacing var6, float var7, float var8, float var9) {
      if (this.demoTimeExpired) {
         this.sendDemoReminder();
         return EnumActionResult.PASS;
      } else {
         return super.processRightClickBlock(var1, var2, var3, var4, var5, var6, var7, var8, var9);
      }
   }
}
