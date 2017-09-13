package net.minecraft.tileentity;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;

public abstract class TileEntity {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Map nameToClassMap = Maps.newHashMap();
   private static final Map classToNameMap = Maps.newHashMap();
   protected World world;
   protected BlockPos pos = BlockPos.ORIGIN;
   protected boolean tileEntityInvalid;
   private int blockMetadata = -1;
   protected Block blockType;

   static {
      addMapping(TileEntityFurnace.class, "Furnace");
      addMapping(TileEntityChest.class, "Chest");
      addMapping(TileEntityEnderChest.class, "EnderChest");
      addMapping(BlockJukebox.TileEntityJukebox.class, "RecordPlayer");
      addMapping(TileEntityDispenser.class, "Trap");
      addMapping(TileEntityDropper.class, "Dropper");
      addMapping(TileEntitySign.class, "Sign");
      addMapping(TileEntityMobSpawner.class, "MobSpawner");
      addMapping(TileEntityNote.class, "Music");
      addMapping(TileEntityPiston.class, "Piston");
      addMapping(TileEntityBrewingStand.class, "Cauldron");
      addMapping(TileEntityEnchantmentTable.class, "EnchantTable");
      addMapping(TileEntityEndPortal.class, "Airportal");
      addMapping(TileEntityBeacon.class, "Beacon");
      addMapping(TileEntitySkull.class, "Skull");
      addMapping(TileEntityDaylightDetector.class, "DLDetector");
      addMapping(TileEntityHopper.class, "Hopper");
      addMapping(TileEntityComparator.class, "Comparator");
      addMapping(TileEntityFlowerPot.class, "FlowerPot");
      addMapping(TileEntityBanner.class, "Banner");
      addMapping(TileEntityStructure.class, "Structure");
      addMapping(TileEntityEndGateway.class, "EndGateway");
      addMapping(TileEntityCommandBlock.class, "Control");
   }

   private static void addMapping(Class oclass, String s) {
      if (nameToClassMap.containsKey(s)) {
         throw new IllegalArgumentException("Duplicate id: " + s);
      } else {
         nameToClassMap.put(s, oclass);
         classToNameMap.put(oclass, s);
      }
   }

   public World getWorld() {
      return this.world;
   }

   public void setWorld(World world) {
      this.world = world;
   }

   public boolean hasWorld() {
      return this.world != null;
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      this.pos = new BlockPos(nbttagcompound.getInteger("x"), nbttagcompound.getInteger("y"), nbttagcompound.getInteger("z"));
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      return this.writeInternal(nbttagcompound);
   }

   private NBTTagCompound writeInternal(NBTTagCompound nbttagcompound) {
      String s = (String)classToNameMap.get(this.getClass());
      if (s == null) {
         throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
      } else {
         nbttagcompound.setString("id", s);
         nbttagcompound.setInteger("x", this.pos.getX());
         nbttagcompound.setInteger("y", this.pos.getY());
         nbttagcompound.setInteger("z", this.pos.getZ());
         return nbttagcompound;
      }
   }

   public static TileEntity create(World world, NBTTagCompound nbttagcompound) {
      TileEntity tileentity = null;
      String s = nbttagcompound.getString("id");

      try {
         Class oclass = (Class)nameToClassMap.get(s);
         if (oclass != null) {
            tileentity = (TileEntity)oclass.newInstance();
         }
      } catch (Throwable var6) {
         LOGGER.error("Failed to create block entity {}", new Object[]{s, var6});
      }

      if (tileentity != null) {
         try {
            tileentity.setWorldCreate(world);
            tileentity.readFromNBT(nbttagcompound);
         } catch (Throwable var5) {
            LOGGER.error("Failed to load data for block entity {}", new Object[]{s, var5});
            tileentity = null;
         }
      } else {
         LOGGER.warn("Skipping BlockEntity with id {}", new Object[]{s});
      }

      return tileentity;
   }

   protected void setWorldCreate(World world) {
   }

   public int getBlockMetadata() {
      if (this.blockMetadata == -1) {
         IBlockState iblockdata = this.world.getBlockState(this.pos);
         this.blockMetadata = iblockdata.getBlock().getMetaFromState(iblockdata);
      }

      return this.blockMetadata;
   }

   public void markDirty() {
      if (this.world != null) {
         IBlockState iblockdata = this.world.getBlockState(this.pos);
         this.blockMetadata = iblockdata.getBlock().getMetaFromState(iblockdata);
         this.world.markChunkDirty(this.pos, this);
         if (this.getBlockType() != Blocks.AIR) {
            this.world.updateComparatorOutputLevel(this.pos, this.getBlockType());
         }
      }

   }

   public BlockPos getPos() {
      return this.pos;
   }

   public Block getBlockType() {
      if (this.blockType == null && this.world != null) {
         this.blockType = this.world.getBlockState(this.pos).getBlock();
      }

      return this.blockType;
   }

   @Nullable
   public SPacketUpdateTileEntity getUpdatePacket() {
      return null;
   }

   public NBTTagCompound getUpdateTag() {
      return this.writeInternal(new NBTTagCompound());
   }

   public boolean isInvalid() {
      return this.tileEntityInvalid;
   }

   public void invalidate() {
      this.tileEntityInvalid = true;
   }

   public void validate() {
      this.tileEntityInvalid = false;
   }

   public boolean receiveClientEvent(int i, int j) {
      return false;
   }

   public void updateContainingBlockInfo() {
      this.blockType = null;
      this.blockMetadata = -1;
   }

   public void addInfoToCrashReport(CrashReportCategory crashreportsystemdetails) {
      crashreportsystemdetails.setDetail("Name", new ICrashReportDetail() {
         public String call() throws Exception {
            return (String)TileEntity.classToNameMap.get(TileEntity.this.getClass()) + " // " + TileEntity.this.getClass().getCanonicalName();
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      if (this.world != null) {
         CrashReportCategory.addBlockInfo(crashreportsystemdetails, this.pos, this.getBlockType(), this.getBlockMetadata());
         crashreportsystemdetails.setDetail("Actual block type", new ICrashReportDetail() {
            public String call() throws Exception {
               int i = Block.getIdFromBlock(TileEntity.this.world.getBlockState(TileEntity.this.pos).getBlock());

               try {
                  return String.format("ID #%d (%s // %s)", i, Block.getBlockById(i).getUnlocalizedName(), Block.getBlockById(i).getClass().getCanonicalName());
               } catch (Throwable var2) {
                  return "ID #" + i;
               }
            }

            public Object call() throws Exception {
               return this.call();
            }
         });
         crashreportsystemdetails.setDetail("Actual block data value", new ICrashReportDetail() {
            public String call() throws Exception {
               IBlockState iblockdata = TileEntity.this.world.getBlockState(TileEntity.this.pos);
               int i = iblockdata.getBlock().getMetaFromState(iblockdata);
               if (i < 0) {
                  return "Unknown? (Got " + i + ")";
               } else {
                  String s = String.format("%4s", Integer.toBinaryString(i)).replace(" ", "0");
                  return String.format("%1$d / 0x%1$X / 0b%2$s", i, s);
               }
            }

            public Object call() throws Exception {
               return this.call();
            }
         });
      }

   }

   public void setPos(BlockPos blockposition) {
      if (blockposition instanceof BlockPos.MutableBlockPos || blockposition instanceof BlockPos.PooledMutableBlockPos) {
         LOGGER.warn("Tried to assign a mutable BlockPos to a block entity...", new Error(blockposition.getClass().toString()));
         blockposition = new BlockPos(blockposition);
      }

      this.pos = blockposition;
   }

   public boolean onlyOpsCanSetNbt() {
      return false;
   }

   @Nullable
   public ITextComponent getDisplayName() {
      return null;
   }

   public void rotate(Rotation enumblockrotation) {
   }

   public void mirror(Mirror enumblockmirror) {
   }

   public InventoryHolder getOwner() {
      if (this.world == null) {
         return null;
      } else {
         BlockState state = this.world.getWorld().getBlockAt(this.pos.getX(), this.pos.getY(), this.pos.getZ()).getState();
         return state instanceof InventoryHolder ? (InventoryHolder)state : null;
      }
   }
}
