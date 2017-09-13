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
      super.writeToNBT(compound);

      for(int i = 0; i < 4; ++i) {
         String s = ITextComponent.Serializer.componentToJson(this.signText[i]);
         compound.setString("Text" + (i + 1), s);
      }

      this.stats.writeStatsToNBT(compound);
      return compound;
   }

   protected void setWorldCreate(World var1) {
      this.setWorld(worldIn);
   }

   public void readFromNBT(NBTTagCompound var1) {
      this.isEditable = false;
      super.readFromNBT(compound);
      ICommandSender icommandsender = new ICommandSender() {
         public String getName() {
            return "Sign";
         }

         public ITextComponent getDisplayName() {
            return new TextComponentString(this.getName());
         }

         public void sendMessage(ITextComponent var1) {
         }

         public boolean canUseCommand(int var1, String var2) {
            return permLevel <= 2;
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

      for(int i = 0; i < 4; ++i) {
         String s = compound.getString("Text" + (i + 1));
         ITextComponent itextcomponent = ITextComponent.Serializer.jsonToComponent(s);

         try {
            this.signText[i] = TextComponentUtils.processComponent(icommandsender, itextcomponent, (Entity)null);
         } catch (CommandException var7) {
            this.signText[i] = itextcomponent;
         }
      }

      this.stats.readStatsFromNBT(compound);
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
      this.isEditable = isEditableIn;
      if (!isEditableIn) {
         this.player = null;
      }

   }

   public void setPlayer(EntityPlayer var1) {
      this.player = playerIn;
   }

   public EntityPlayer getPlayer() {
      return this.player;
   }

   public boolean executeCommand(final EntityPlayer var1) {
      ICommandSender icommandsender = new ICommandSender() {
         public String getName() {
            return playerIn.getName();
         }

         public ITextComponent getDisplayName() {
            return playerIn.getDisplayName();
         }

         public void sendMessage(ITextComponent var1x) {
         }

         public boolean canUseCommand(int var1x, String var2) {
            return permLevel <= 2;
         }

         public BlockPos getPosition() {
            return TileEntitySign.this.pos;
         }

         public Vec3d getPositionVector() {
            return new Vec3d((double)TileEntitySign.this.pos.getX() + 0.5D, (double)TileEntitySign.this.pos.getY() + 0.5D, (double)TileEntitySign.this.pos.getZ() + 0.5D);
         }

         public World getEntityWorld() {
            return playerIn.getEntityWorld();
         }

         public Entity getCommandSenderEntity() {
            return playerIn;
         }

         public boolean sendCommandFeedback() {
            return false;
         }

         public void setCommandStat(CommandResultStats.Type var1x, int var2) {
            if (TileEntitySign.this.world != null && !TileEntitySign.this.world.isRemote) {
               TileEntitySign.this.stats.setCommandStatForSender(TileEntitySign.this.world.getMinecraftServer(), this, type, amount);
            }

         }

         public MinecraftServer getServer() {
            return playerIn.getServer();
         }
      };

      for(ITextComponent itextcomponent : this.signText) {
         Style style = itextcomponent == null ? null : itextcomponent.getStyle();
         if (style != null && style.getClickEvent() != null) {
            ClickEvent clickevent = style.getClickEvent();
            if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
               playerIn.getServer().getCommandManager().executeCommand(icommandsender, clickevent.getValue());
            }
         }
      }

      return true;
   }

   public CommandResultStats getStats() {
      return this.stats;
   }
}
