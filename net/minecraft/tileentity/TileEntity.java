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

   private static void addMapping(Class var0, String var1) {
      if (nameToClassMap.containsKey(var1)) {
         throw new IllegalArgumentException("Duplicate id: " + var1);
      } else {
         nameToClassMap.put(var1, var0);
         classToNameMap.put(var0, var1);
      }
   }

   public World getWorld() {
      return this.world;
   }

   public void setWorld(World var1) {
      this.world = var1;
   }

   public boolean hasWorld() {
      return this.world != null;
   }

   public void readFromNBT(NBTTagCompound var1) {
      this.pos = new BlockPos(var1.getInteger("x"), var1.getInteger("y"), var1.getInteger("z"));
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      return this.writeInternal(var1);
   }

   private NBTTagCompound writeInternal(NBTTagCompound var1) {
      String var2 = (String)classToNameMap.get(this.getClass());
      if (var2 == null) {
         throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
      } else {
         var1.setString("id", var2);
         var1.setInteger("x", this.pos.getX());
         var1.setInteger("y", this.pos.getY());
         var1.setInteger("z", this.pos.getZ());
         return var1;
      }
   }

   public static TileEntity create(World var0, NBTTagCompound var1) {
      TileEntity var2 = null;
      String var3 = var1.getString("id");

      try {
         Class var4 = (Class)nameToClassMap.get(var3);
         if (var4 != null) {
            var2 = (TileEntity)var4.newInstance();
         }
      } catch (Throwable var6) {
         LOGGER.error("Failed to create block entity {}", new Object[]{var3, var6});
      }

      if (var2 != null) {
         try {
            var2.setWorldCreate(var0);
            var2.readFromNBT(var1);
         } catch (Throwable var5) {
            LOGGER.error("Failed to load data for block entity {}", new Object[]{var3, var5});
            var2 = null;
         }
      } else {
         LOGGER.warn("Skipping BlockEntity with id {}", new Object[]{var3});
      }

      return var2;
   }

   protected void setWorldCreate(World var1) {
   }

   public int getBlockMetadata() {
      if (this.blockMetadata == -1) {
         IBlockState var1 = this.world.getBlockState(this.pos);
         this.blockMetadata = var1.getBlock().getMetaFromState(var1);
      }

      return this.blockMetadata;
   }

   public void markDirty() {
      if (this.world != null) {
         IBlockState var1 = this.world.getBlockState(this.pos);
         this.blockMetadata = var1.getBlock().getMetaFromState(var1);
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

   public boolean receiveClientEvent(int var1, int var2) {
      return false;
   }

   public void updateContainingBlockInfo() {
      this.blockType = null;
      this.blockMetadata = -1;
   }

   public void addInfoToCrashReport(CrashReportCategory var1) {
      var1.setDetail("Name", new ICrashReportDetail() {
         public String call() throws Exception {
            return (String)TileEntity.classToNameMap.get(TileEntity.this.getClass()) + " // " + TileEntity.this.getClass().getCanonicalName();
         }

         public Object call() throws Exception {
            return this.call();
         }
      });
      if (this.world != null) {
         CrashReportCategory.addBlockInfo(var1, this.pos, this.getBlockType(), this.getBlockMetadata());
         var1.setDetail("Actual block type", new ICrashReportDetail() {
            public String call() throws Exception {
               int var1 = Block.getIdFromBlock(TileEntity.this.world.getBlockState(TileEntity.this.pos).getBlock());

               try {
                  return String.format("ID #%d (%s // %s)", var1, Block.getBlockById(var1).getUnlocalizedName(), Block.getBlockById(var1).getClass().getCanonicalName());
               } catch (Throwable var2) {
                  return "ID #" + var1;
               }
            }

            public Object call() throws Exception {
               return this.call();
            }
         });
         var1.setDetail("Actual block data value", new ICrashReportDetail() {
            public String call() throws Exception {
               IBlockState var1 = TileEntity.this.world.getBlockState(TileEntity.this.pos);
               int var2 = var1.getBlock().getMetaFromState(var1);
               if (var2 < 0) {
                  return "Unknown? (Got " + var2 + ")";
               } else {
                  String var3 = String.format("%4s", Integer.toBinaryString(var2)).replace(" ", "0");
                  return String.format("%1$d / 0x%1$X / 0b%2$s", var2, var3);
               }
            }

            public Object call() throws Exception {
               return this.call();
            }
         });
      }

   }

   public void setPos(BlockPos var1) {
      if (var1 instanceof BlockPos.MutableBlockPos || var1 instanceof BlockPos.PooledMutableBlockPos) {
         LOGGER.warn("Tried to assign a mutable BlockPos to a block entity...", new Error(var1.getClass().toString()));
         var1 = new BlockPos(var1);
      }

      this.pos = var1;
   }

   public boolean onlyOpsCanSetNbt() {
      return false;
   }

   @Nullable
   public ITextComponent getDisplayName() {
      return null;
   }

   public void rotate(Rotation var1) {
   }

   public void mirror(Mirror var1) {
   }

   public InventoryHolder getOwner() {
      if (this.world == null) {
         return null;
      } else {
         BlockState var1 = this.world.getWorld().getBlockAt(this.pos.getX(), this.pos.getY(), this.pos.getZ()).getState();
         return var1 instanceof InventoryHolder ? (InventoryHolder)var1 : null;
      }
   }
}
