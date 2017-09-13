package net.minecraft.command;

import net.minecraft.entity.Entity;
import net.minecraft.src.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

class CommandExecuteAt$1 implements ICommandSender {
   // $FF: synthetic field
   final Entity field_174804_a;
   // $FF: synthetic field
   final ICommandSender field_189650_b;
   // $FF: synthetic field
   final BlockPos field_174803_c;
   // $FF: synthetic field
   final double field_174800_d;
   // $FF: synthetic field
   final double field_174801_e;
   // $FF: synthetic field
   final double field_174798_f;
   // $FF: synthetic field
   final MinecraftServer field_184170_g;
   // $FF: synthetic field
   final CommandExecuteAt field_174799_g;

   CommandExecuteAt$1(CommandExecuteAt var1, Entity var2, ICommandSender var3, BlockPos var4, double var5, double var7, double var9, MinecraftServer var11) {
      this.field_174799_g = var1;
      this.field_174804_a = var2;
      this.field_189650_b = var3;
      this.field_174803_c = var4;
      this.field_174800_d = var5;
      this.field_174801_e = var7;
      this.field_174798_f = var9;
      this.field_184170_g = var11;
   }

   public String getName() {
      return this.field_174804_a.getName();
   }

   public ITextComponent getDisplayName() {
      return this.field_174804_a.getDisplayName();
   }

   public void sendMessage(ITextComponent var1) {
      this.field_189650_b.sendMessage(var1);
   }

   public boolean canUseCommand(int var1, String var2) {
      return this.field_189650_b.canUseCommand(var1, var2);
   }

   public BlockPos getPosition() {
      return this.field_174803_c;
   }

   public Vec3d getPositionVector() {
      return new Vec3d(this.field_174800_d, this.field_174801_e, this.field_174798_f);
   }

   public World getEntityWorld() {
      return this.field_174804_a.world;
   }

   public Entity getCommandSenderEntity() {
      return this.field_174804_a;
   }

   public boolean sendCommandFeedback() {
      return this.field_184170_g == null || this.field_184170_g.worldServer[0].getGameRules().getBoolean("commandBlockOutput");
   }

   public void setCommandStat(CommandResultStats.Type var1, int var2) {
      this.field_174804_a.setCommandStat(var1, var2);
   }

   public MinecraftServer h() {
      return this.field_174804_a.h();
   }
}
