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
      super(worldIn);
   }

   public void updateBlockRemoving() {
      super.updateBlockRemoving();
      ++this.gameModeTicks;
      long i = this.world.getTotalWorldTime();
      long j = i / 24000L + 1L;
      if (!this.displayedIntro && this.gameModeTicks > 20) {
         this.displayedIntro = true;
         this.player.connection.sendPacket(new SPacketChangeGameState(5, 0.0F));
      }

      this.demoTimeExpired = i > 120500L;
      if (this.demoTimeExpired) {
         ++this.demoEndedReminder;
      }

      if (i % 24000L == 500L) {
         if (j <= 6L) {
            this.player.sendMessage(new TextComponentTranslation("demo.day." + j, new Object[0]));
         }
      } else if (j == 1L) {
         if (i == 100L) {
            this.player.connection.sendPacket(new SPacketChangeGameState(5, 101.0F));
         } else if (i == 175L) {
            this.player.connection.sendPacket(new SPacketChangeGameState(5, 102.0F));
         } else if (i == 250L) {
            this.player.connection.sendPacket(new SPacketChangeGameState(5, 103.0F));
         }
      } else if (j == 5L && i % 24000L == 22000L) {
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
         super.onBlockClicked(pos, side);
      }

   }

   public void blockRemoving(BlockPos var1) {
      if (!this.demoTimeExpired) {
         super.blockRemoving(pos);
      }

   }

   public boolean tryHarvestBlock(BlockPos var1) {
      return this.demoTimeExpired ? false : super.tryHarvestBlock(pos);
   }

   public EnumActionResult processRightClick(EntityPlayer var1, World var2, ItemStack var3, EnumHand var4) {
      if (this.demoTimeExpired) {
         this.sendDemoReminder();
         return EnumActionResult.PASS;
      } else {
         return super.processRightClick(player, worldIn, stack, hand);
      }
   }

   public EnumActionResult processRightClickBlock(EntityPlayer var1, World var2, @Nullable ItemStack var3, EnumHand var4, BlockPos var5, EnumFacing var6, float var7, float var8, float var9) {
      if (this.demoTimeExpired) {
         this.sendDemoReminder();
         return EnumActionResult.PASS;
      } else {
         return super.processRightClickBlock(player, worldIn, stack, hand, pos, facing, hitX, hitY, hitZ);
      }
   }
}
