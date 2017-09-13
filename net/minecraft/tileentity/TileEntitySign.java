package net.minecraft.tileentity;

import javax.annotation.Nullable;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandResultStats;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntitySign extends TileEntity {
   public final ITextComponent[] signText = new ITextComponent[]{new TextComponentString(""), new TextComponentString(""), new TextComponentString(""), new TextComponentString("")};
   public int lineBeingEdited = -1;
   private boolean isEditable = true;
   private EntityPlayer player;
   private final CommandResultStats stats = new CommandResultStats();

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);

      for(int var2 = 0; var2 < 4; ++var2) {
         String var3 = ITextComponent.Serializer.componentToJson(this.signText[var2]);
         var1.setString("Text" + (var2 + 1), var3);
      }

      this.stats.writeStatsToNBT(var1);
      return var1;
   }

   protected void setWorldCreate(World var1) {
      this.setWorld(var1);
   }

   public void readFromNBT(NBTTagCompound var1) {
      this.isEditable = false;
      super.readFromNBT(var1);
      ICommandSender var2 = new ICommandSender() {
         public String getName() {
            return "Sign";
         }

         public ITextComponent getDisplayName() {
            return new TextComponentString(this.getName());
         }

         public void sendMessage(ITextComponent var1) {
         }

         public boolean canUseCommand(int var1, String var2) {
            return var1 <= 2;
         }

         public BlockPos getPosition() {
            return TileEntitySign.this.pos;
         }

         public Vec3d getPositionVector() {
            return new Vec3d((double)TileEntitySign.this.pos.getX() + 0.5D, (double)TileEntitySign.this.pos.getY() + 0.5D, (double)TileEntitySign.this.pos.getZ() + 0.5D);
         }

         public World getEntityWorld() {
            return TileEntitySign.this.world;
         }

         public Entity getCommandSenderEntity() {
            return null;
         }

         public boolean sendCommandFeedback() {
            return false;
         }

         public void setCommandStat(CommandResultStats.Type var1, int var2) {
         }

         public MinecraftServer getServer() {
            return TileEntitySign.this.world.getMinecraftServer();
         }
      };

      for(int var3 = 0; var3 < 4; ++var3) {
         String var4 = var1.getString("Text" + (var3 + 1));
         ITextComponent var5 = ITextComponent.Serializer.jsonToComponent(var4);

         try {
            this.signText[var3] = TextComponentUtils.processComponent(var2, var5, (Entity)null);
         } catch (CommandException var7) {
            this.signText[var3] = var5;
         }
      }

      this.stats.readStatsFromNBT(var1);
   }

   @Nullable
   public SPacketUpdateTileEntity getUpdatePacket() {
      return new SPacketUpdateTileEntity(this.pos, 9, this.getUpdateTag());
   }

   public NBTTagCompound getUpdateTag() {
      return this.writeToNBT(new NBTTagCompound());
   }

   public boolean onlyOpsCanSetNbt() {
      return true;
   }

   public boolean getIsEditable() {
      return this.isEditable;
   }

   @SideOnly(Side.CLIENT)
   public void setEditable(boolean var1) {
      this.isEditable = var1;
      if (!var1) {
         this.player = null;
      }

   }

   public void setPlayer(EntityPlayer var1) {
      this.player = var1;
   }

   public EntityPlayer getPlayer() {
      return this.player;
   }

   public boolean executeCommand(final EntityPlayer var1) {
      ICommandSender var2 = new ICommandSender() {
         public String getName() {
            return var1.getName();
         }

         public ITextComponent getDisplayName() {
            return var1.getDisplayName();
         }

         public void sendMessage(ITextComponent var1x) {
         }

         public boolean canUseCommand(int var1x, String var2) {
            return var1x <= 2;
         }

         public BlockPos getPosition() {
            return TileEntitySign.this.pos;
         }

         public Vec3d getPositionVector() {
            return new Vec3d((double)TileEntitySign.this.pos.getX() + 0.5D, (double)TileEntitySign.this.pos.getY() + 0.5D, (double)TileEntitySign.this.pos.getZ() + 0.5D);
         }

         public World getEntityWorld() {
            return var1.getEntityWorld();
         }

         public Entity getCommandSenderEntity() {
            return var1;
         }

         public boolean sendCommandFeedback() {
            return false;
         }

         public void setCommandStat(CommandResultStats.Type var1x, int var2) {
            if (TileEntitySign.this.world != null && !TileEntitySign.this.world.isRemote) {
               TileEntitySign.this.stats.setCommandStatForSender(TileEntitySign.this.world.getMinecraftServer(), this, var1x, var2);
            }

         }

         public MinecraftServer getServer() {
            return var1.getServer();
         }
      };

      for(ITextComponent var6 : this.signText) {
         Style var7 = var6 == null ? null : var6.getStyle();
         if (var7 != null && var7.getClickEvent() != null) {
            ClickEvent var8 = var7.getClickEvent();
            if (var8.getAction() == ClickEvent.Action.RUN_COMMAND) {
               var1.getServer().getCommandManager().executeCommand(var2, var8.getValue());
            }
         }
      }

      return true;
   }

   public CommandResultStats getStats() {
      return this.stats;
   }
}
