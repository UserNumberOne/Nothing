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
      if (nameToClassMap.containsKey(id)) {
         throw new IllegalArgumentException("Duplicate id: " + id);
      } else {
         nameToClassMap.put(id, cl);
         classToNameMap.put(cl, id);
      }
   }

   public World getWorld() {
      return this.world;
   }

   public void setWorld(World var1) {
      this.world = worldIn;
   }

   public boolean hasWorld() {
      return this.world != null;
   }

   public void readFromNBT(NBTTagCompound var1) {
      this.pos = new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z"));
      if (compound.hasKey("ForgeData")) {
         this.customTileData = compound.getCompoundTag("ForgeData");
      }

      if (this.capabilities != null && compound.hasKey("ForgeCaps")) {
         this.capabilities.deserializeNBT(compound.getCompoundTag("ForgeCaps"));
      }

   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      return this.writeInternal(compound);
   }

   private NBTTagCompound writeInternal(NBTTagCompound var1) {
      String s = (String)classToNameMap.get(this.getClass());
      if (s == null) {
         throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
      } else {
         compound.setString("id", s);
         compound.setInteger("x", this.pos.getX());
         compound.setInteger("y", this.pos.getY());
         compound.setInteger("z", this.pos.getZ());
         if (this.customTileData != null) {
            compound.setTag("ForgeData", this.customTileData);
         }

         if (this.capabilities != null) {
            compound.setTag("ForgeCaps", this.capabilities.serializeNBT());
         }

         return compound;
      }
   }

   public static TileEntity create(World var0, NBTTagCompound var1) {
      TileEntity tileentity = null;
      String s = compound.getString("id");
      Class oclass = null;

      try {
         oclass = (Class)nameToClassMap.get(s);
         if (oclass != null) {
            tileentity = (TileEntity)oclass.newInstance();
         }
      } catch (Throwable var7) {
         LOGGER.error("Failed to create block entity {}", new Object[]{s, var7});
         FMLLog.log(Level.ERROR, var7, "A TileEntity %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author", new Object[]{s, oclass.getName()});
      }

      if (tileentity != null) {
         try {
            tileentity.setWorldCreate(worldIn);
            tileentity.readFromNBT(compound);
         } catch (Throwable var6) {
            LOGGER.error("Failed to load data for block entity {}", new Object[]{s, var6});
            FMLLog.log(Level.ERROR, var6, "A TileEntity %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author", new Object[]{s, oclass.getName()});
            tileentity = null;
         }
      } else {
         LOGGER.warn("Skipping BlockEntity with id {}", new Object[]{s});
      }

      return tileentity;
   }

   protected void setWorldCreate(World var1) {
   }

   public int getBlockMetadata() {
      if (this.blockMetadata == -1) {
         IBlockState iblockstate = this.world.getBlockState(this.pos);
         this.blockMetadata = iblockstate.getBlock().getMetaFromState(iblockstate);
      }

      return this.blockMetadata;
   }

   public void markDirty() {
      if (this.world != null) {
         IBlockState iblockstate = this.world.getBlockState(this.pos);
         this.blockMetadata = iblockstate.getBlock().getMetaFromState(iblockstate);
         this.world.markChunkDirty(this.pos, this);
         if (this.getBlockType() != Blocks.AIR) {
            this.world.updateComparatorOutputLevel(this.pos, this.getBlockType());
         }
      }

   }

   public double getDistanceSq(double var1, double var3, double var5) {
      double d0 = (double)this.pos.getX() + 0.5D - x;
      double d1 = (double)this.pos.getY() + 0.5D - y;
      double d2 = (double)this.pos.getZ() + 0.5D - z;
      return d0 * d0 + d1 * d1 + d2 * d2;
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
      reportCategory.setDetail("Name", new ICrashReportDetail() {
         public String call() throws Exception {
            return (String)TileEntity.classToNameMap.get(TileEntity.this.getClass()) + " // " + TileEntity.this.getClass().getCanonicalName();
         }
      });
      if (this.world != null) {
         CrashReportCategory.addBlockInfo(reportCategory, this.pos, this.getBlockType(), this.getBlockMetadata());
         reportCategory.setDetail("Actual block type", new ICrashReportDetail() {
            public String call() throws Exception {
               int i = Block.getIdFromBlock(TileEntity.this.world.getBlockState(TileEntity.this.pos).getBlock());

               try {
                  return String.format("ID #%d (%s // %s)", i, Block.getBlockById(i).getUnlocalizedName(), Block.getBlockById(i).getClass().getCanonicalName());
               } catch (Throwable var3) {
                  return "ID #" + i;
               }
            }
         });
         reportCategory.setDetail("Actual block data value", new ICrashReportDetail() {
            public String call() throws Exception {
               IBlockState iblockstate = TileEntity.this.world.getBlockState(TileEntity.this.pos);
               int i = iblockstate.getBlock().getMetaFromState(iblockstate);
               if (i < 0) {
                  return "Unknown? (Got " + i + ")";
               } else {
                  String s = String.format("%4s", Integer.toBinaryString(i)).replace(" ", "0");
                  return String.format("%1$d / 0x%1$X / 0b%2$s", i, s);
               }
            }
         });
      }

   }

   public void setPos(BlockPos var1) {
      if (posIn instanceof BlockPos.MutableBlockPos || posIn instanceof BlockPos.PooledMutableBlockPos) {
         LOGGER.warn("Tried to assign a mutable BlockPos to a block entity...", new Error(posIn.getClass().toString()));
         posIn = new BlockPos(posIn);
      }

      this.pos = posIn;
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
      this.readFromNBT(tag);
   }

   public void onChunkUnload() {
   }

   public boolean shouldRefresh(World var1, BlockPos var2, IBlockState var3, IBlockState var4) {
      return this.isVanilla ? oldState.getBlock() != newSate.getBlock() : oldState != newSate;
   }

   public boolean shouldRenderInPass(int var1) {
      return pass == 0;
   }

   @SideOnly(Side.CLIENT)
   public AxisAlignedBB getRenderBoundingBox() {
      AxisAlignedBB bb = INFINITE_EXTENT_AABB;
      Block type = this.getBlockType();
      BlockPos pos = this.getPos();
      if (type == Blocks.ENCHANTING_TABLE) {
         bb = new AxisAlignedBB(pos, pos.add(1, 1, 1));
      } else if (type != Blocks.CHEST && type != Blocks.TRAPPED_CHEST) {
         if (type == Blocks.STRUCTURE_BLOCK) {
            bb = INFINITE_EXTENT_AABB;
         } else if (type != null && type != Blocks.BEACON) {
            AxisAlignedBB cbb = null;

            try {
               cbb = this.world.getBlockState(this.getPos()).getCollisionBoundingBox(this.world, pos).addCoord((double)pos.getX(), (double)pos.getY(), (double)pos.getZ());
            } catch (Exception var6) {
               cbb = new AxisAlignedBB(this.getPos().add(-1, 0, -1), this.getPos().add(1, 1, 1));
            }

            if (cbb != null) {
               bb = cbb;
            }
         }
      } else {
         bb = new AxisAlignedBB(pos.add(-1, 0, -1), pos.add(2, 2, 2));
      }

      return bb;
   }

   public boolean canRenderBreaking() {
      Block block = this.getBlockType();
      return block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockSign || block instanceof BlockSkull;
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
      return this.capabilities == null ? false : this.capabilities.hasCapability(capability, facing);
   }

   public Object getCapability(Capability var1, EnumFacing var2) {
      return this.capabilities == null ? null : this.capabilities.getCapability(capability, facing);
   }

   public void deserializeNBT(NBTTagCompound var1) {
      this.readFromNBT(nbt);
   }

   public NBTTagCompound serializeNBT() {
      NBTTagCompound ret = new NBTTagCompound();
      this.writeToNBT(ret);
      return ret;
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
