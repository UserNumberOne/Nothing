package net.minecraft.server.integrated;

import com.mojang.authlib.GameProfile;
import java.net.SocketAddress;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class IntegratedPlayerList extends PlayerList {
   private NBTTagCompound hostPlayerData;

   public IntegratedPlayerList(IntegratedServer var1) {
      super(var1);
      this.setViewDistance(10);
   }

   protected void writePlayerData(EntityPlayerMP var1) {
      if (var1.getName().equals(this.getServerInstance().getServerOwner())) {
         this.hostPlayerData = var1.writeToNBT(new NBTTagCompound());
      }

      super.writePlayerData(var1);
   }

   public String allowUserToConnect(SocketAddress var1, GameProfile var2) {
      return var2.getName().equalsIgnoreCase(this.getServerInstance().getServerOwner()) && this.getPlayerByUsername(var2.getName()) != null ? "That name is already taken." : super.allowUserToConnect(var1, var2);
   }

   public IntegratedServer getServerInstance() {
      return (IntegratedServer)super.getServerInstance();
   }

   public NBTTagCompound getHostPlayerData() {
      return this.hostPlayerData;
   }
}
