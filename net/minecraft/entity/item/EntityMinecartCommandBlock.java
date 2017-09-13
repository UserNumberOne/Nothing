package net.minecraft.entity.item;

import io.netty.buffer.ByteBuf;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.util.EnumHand;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.IDataFixer;
import net.minecraft.util.datafix.IDataWalker;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.minecart.MinecartInteractEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityMinecartCommandBlock extends EntityMinecart {
   private static final DataParameter COMMAND = EntityDataManager.createKey(EntityMinecartCommandBlock.class, DataSerializers.STRING);
   private static final DataParameter LAST_OUTPUT = EntityDataManager.createKey(EntityMinecartCommandBlock.class, DataSerializers.TEXT_COMPONENT);
   private final CommandBlockBaseLogic commandBlockLogic = new CommandBlockBaseLogic() {
      public void updateCommand() {
         EntityMinecartCommandBlock.this.getDataManager().set(EntityMinecartCommandBlock.COMMAND, this.getCommand());
         EntityMinecartCommandBlock.this.getDataManager().set(EntityMinecartCommandBlock.LAST_OUTPUT, this.getLastOutput());
      }

      @SideOnly(Side.CLIENT)
      public int getCommandBlockType() {
         return 1;
      }

      @SideOnly(Side.CLIENT)
      public void fillInInfo(ByteBuf var1) {
         buf.writeInt(EntityMinecartCommandBlock.this.getEntityId());
      }

      public BlockPos getPosition() {
         return new BlockPos(EntityMinecartCommandBlock.this.posX, EntityMinecartCommandBlock.this.posY + 0.5D, EntityMinecartCommandBlock.this.posZ);
      }

      public Vec3d getPositionVector() {
         return new Vec3d(EntityMinecartCommandBlock.this.posX, EntityMinecartCommandBlock.this.posY, EntityMinecartCommandBlock.this.posZ);
      }

      public World getEntityWorld() {
         return EntityMinecartCommandBlock.this.world;
      }

      public Entity getCommandSenderEntity() {
         return EntityMinecartCommandBlock.this;
      }

      public MinecraftServer getServer() {
         return EntityMinecartCommandBlock.this.world.getMinecraftServer();
      }
   };
   private int activatorRailCooldown;

   public EntityMinecartCommandBlock(World var1) {
      super(worldIn);
   }

   public EntityMinecartCommandBlock(World var1, double var2, double var4, double var6) {
      super(worldIn, x, y, z);
   }

   public static void registerFixesMinecartCommand(DataFixer var0) {
      EntityMinecart.registerFixesMinecart(fixer, "MinecartCommandBlock");
      fixer.registerWalker(FixTypes.ENTITY, new IDataWalker() {
         public NBTTagCompound process(IDataFixer var1, NBTTagCompound var2, int var3) {
            if ("MinecartCommandBlock".equals(compound.getString("id"))) {
               compound.setString("id", "Control");
               fixer.process(FixTypes.BLOCK_ENTITY, compound, versionIn);
               compound.setString("id", "MinecartCommandBlock");
            }

            return compound;
         }
      });
   }

   protected void entityInit() {
      super.entityInit();
      this.getDataManager().register(COMMAND, "");
      this.getDataManager().register(LAST_OUTPUT, new TextComponentString(""));
   }

   protected void readEntityFromNBT(NBTTagCompound var1) {
      super.readEntityFromNBT(compound);
      this.commandBlockLogic.readDataFromNBT(compound);
      this.getDataManager().set(COMMAND, this.getCommandBlockLogic().getCommand());
      this.getDataManager().set(LAST_OUTPUT, this.getCommandBlockLogic().getLastOutput());
   }

   protected void writeEntityToNBT(NBTTagCompound var1) {
      super.writeEntityToNBT(compound);
      this.commandBlockLogic.writeToNBT(compound);
   }

   public EntityMinecart.Type getType() {
      return EntityMinecart.Type.COMMAND_BLOCK;
   }

   public IBlockState getDefaultDisplayTile() {
      return Blocks.COMMAND_BLOCK.getDefaultState();
   }

   public CommandBlockBaseLogic getCommandBlockLogic() {
      return this.commandBlockLogic;
   }

   public void onActivatorRailPass(int var1, int var2, int var3, boolean var4) {
      if (receivingPower && this.ticksExisted - this.activatorRailCooldown >= 4) {
         this.getCommandBlockLogic().trigger(this.world);
         this.activatorRailCooldown = this.ticksExisted;
      }

   }

   public boolean processInitialInteract(EntityPlayer var1, @Nullable ItemStack var2, EnumHand var3) {
      if (MinecraftForge.EVENT_BUS.post(new MinecartInteractEvent(this, player, stack, hand))) {
         return true;
      } else {
         this.commandBlockLogic.tryOpenEditCommandBlock(player);
         return false;
      }
   }

   public void notifyDataManagerChange(DataParameter var1) {
      super.notifyDataManagerChange(key);
      if (LAST_OUTPUT.equals(key)) {
         try {
            this.commandBlockLogic.setLastOutput((ITextComponent)this.getDataManager().get(LAST_OUTPUT));
         } catch (Throwable var3) {
            ;
         }
      } else if (COMMAND.equals(key)) {
         this.commandBlockLogic.setCommand((String)this.getDataManager().get(COMMAND));
      }

   }

   public boolean ignoreItemEntityData() {
      return true;
   }
}
