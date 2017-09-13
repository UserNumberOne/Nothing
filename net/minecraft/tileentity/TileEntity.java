package net.minecraft.tileentity;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.BlockSign;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class TileEntity implements ICapabilitySerializable {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final Map nameToClassMap = Maps.newHashMap();
   private static final Map classToNameMap = Maps.newHashMap();
   protected World world;
   protected BlockPos pos = BlockPos.ORIGIN;
   protected boolean tileEntityInvalid;
   private int blockMetadata = -1;
   protected Block blockType;
   private boolean isVanilla = this.getClass().getName().startsWith("net.minecraft.");
   public static final AxisAlignedBB INFINITE_EXTENT_AABB = new AxisAlignedBB(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
   private NBTTagCompound customTileData;
   private CapabilityDispatcher capabilities = ForgeEventFactory.gatherCapabilities(this);

   public static void addMapping(Class var0, String var1) {
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
      if (var1.hasKey("ForgeData")) {
         this.customTileData = var1.getCompoundTag("ForgeData");
      }

      if (this.capabilities != null && var1.hasKey("ForgeCaps")) {
         this.capabilities.deserializeNBT(var1.getCompoundTag("ForgeCaps"));
      }

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
         if (this.customTileData != null) {
            var1.setTag("ForgeData", this.customTileData);
         }

         if (this.capabilities != null) {
            var1.setTag("ForgeCaps", this.capabilities.serializeNBT());
         }

         return var1;
      }
   }

   public static TileEntity create(World var0, NBTTagCompound var1) {
      TileEntity var2 = null;
      String var3 = var1.getString("id");
      Class var4 = null;

      try {
         var4 = (Class)nameToClassMap.get(var3);
         if (var4 != null) {
            var2 = (TileEntity)var4.newInstance();
         }
      } catch (Throwable var7) {
         LOGGER.error("Failed to create block entity {}", new Object[]{var3, var7});
         FMLLog.log(Level.ERROR, var7, "A TileEntity %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author", new Object[]{var3, var4.getName()});
      }

      if (var2 != null) {
         try {
            var2.setWorldCreate(var0);
            var2.readFromNBT(var1);
         } catch (Throwable var6) {
            LOGGER.error("Failed to load data for block entity {}", new Object[]{var3, var6});
            FMLLog.log(Level.ERROR, var6, "A TileEntity %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author", new Object[]{var3, var4.getName()});
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

   public double getDistanceSq(double var1, double var3, double var5) {
      double var7 = (double)this.pos.getX() + 0.5D - var1;
      double var9 = (double)this.pos.getY() + 0.5D - var3;
      double var11 = (double)this.pos.getZ() + 0.5D - var5;
      return var7 * var7 + var9 * var9 + var11 * var11;
   }

   @SideOnly(Side.CLIENT)
   public double getMaxRenderDistanceSquared() {
      return 4096.0D;
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
      });
      if (this.world != null) {
         CrashReportCategory.addBlockInfo(var1, this.pos, this.getBlockType(), this.getBlockMetadata());
         var1.setDetail("Actual block type", new ICrashReportDetail() {
            public String call() throws Exception {
               int var1 = Block.getIdFromBlock(TileEntity.this.world.getBlockState(TileEntity.this.pos).getBlock());

               try {
                  return String.format("ID #%d (%s // %s)", var1, Block.getBlockById(var1).getUnlocalizedName(), Block.getBlockById(var1).getClass().getCanonicalName());
               } catch (Throwable var3) {
                  return "ID #" + var1;
               }
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

   public void onDataPacket(NetworkManager var1, SPacketUpdateTileEntity var2) {
   }

   public void handleUpdateTag(NBTTagCompound var1) {
      this.readFromNBT(var1);
   }

   public void onChunkUnload() {
   }

   public boolean shouldRefresh(World var1, BlockPos var2, IBlockState var3, IBlockState var4) {
      return this.isVanilla ? var3.getBlock() != var4.getBlock() : var3 != var4;
   }

   public boolean shouldRenderInPass(int var1) {
      return var1 == 0;
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      AxisAlignedBB var1 = INFINITE_EXTENT_AABB;
      Block var2 = this.getBlockType();
      BlockPos var3 = this.getPos();
      if (var2 == Blocks.ENCHANTING_TABLE) {
         var1 = new AxisAlignedBB(var3, var3.add(1, 1, 1));
      } else if (var2 != Blocks.CHEST && var2 != Blocks.TRAPPED_CHEST) {
         if (var2 == Blocks.STRUCTURE_BLOCK) {
            var1 = INFINITE_EXTENT_AABB;
         } else if (var2 != null && var2 != Blocks.BEACON) {
            AxisAlignedBB var4 = null;

            try {
               var4 = this.world.getBlockState(this.getPos()).getCollisionBoundingBox(this.world, var3).addCoord((double)var3.getX(), (double)var3.getY(), (double)var3.getZ());
            } catch (Exception var6) {
               var4 = new AxisAlignedBB(this.getPos().add(-1, 0, -1), this.getPos().add(1, 1, 1));
            }

            if (var4 != null) {
               var1 = var4;
            }
         }
      } else {
         var1 = new AxisAlignedBB(var3.add(-1, 0, -1), var3.add(2, 2, 2));
      }

      return var1;
   }

   public boolean canRenderBreaking() {
      Block var1 = this.getBlockType();
      return var1 instanceof BlockChest || var1 instanceof BlockEnderChest || var1 instanceof BlockSign || var1 instanceof BlockSkull;
   }

   public NBTTagCompound getTileData() {
      if (this.customTileData == null) {
         this.customTileData = new NBTTagCompound();
      }

      return this.customTileData;
   }

   public boolean restrictNBTCopy() {
      return this instanceof TileEntityCommandBlock || this instanceof TileEntityMobSpawner || this instanceof TileEntitySign;
   }

   public void onLoad() {
   }

   public boolean hasFastRenderer() {
      return false;
   }

   public boolean hasCapability(Capability var1, EnumFacing var2) {
      return this.capabilities == null ? false : this.capabilities.hasCapability(var1, var2);
   }

   public Object getCapability(Capability var1, EnumFacing var2) {
      return this.capabilities == null ? null : this.capabilities.getCapability(var1, var2);
   }

   public void deserializeNBT(NBTTagCompound var1) {
      this.readFromNBT(var1);
   }

   public NBTTagCompound serializeNBT() {
      NBTTagCompound var1 = new NBTTagCompound();
      this.writeToNBT(var1);
      return var1;
   }

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
}
