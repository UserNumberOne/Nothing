package net.minecraft.tileentity;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Mirror;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.gen.structure.template.PlacementSettings;
import net.minecraft.world.gen.structure.template.Template;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityStructure extends TileEntity {
   private String name = "";
   private String author = "";
   private String metadata = "";
   private BlockPos position = new BlockPos(0, 1, 0);
   private BlockPos size = BlockPos.ORIGIN;
   private Mirror mirror = Mirror.NONE;
   private Rotation rotation = Rotation.NONE;
   private TileEntityStructure.Mode mode = TileEntityStructure.Mode.DATA;
   private boolean ignoreEntities = true;
   private boolean powered;
   private boolean showAir;
   private boolean showBoundingBox = true;
   private float integrity = 1.0F;
   private long seed = 0L;

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);
      var1.setString("name", this.name);
      var1.setString("author", this.author);
      var1.setString("metadata", this.metadata);
      var1.setInteger("posX", this.position.getX());
      var1.setInteger("posY", this.position.getY());
      var1.setInteger("posZ", this.position.getZ());
      var1.setInteger("sizeX", this.size.getX());
      var1.setInteger("sizeY", this.size.getY());
      var1.setInteger("sizeZ", this.size.getZ());
      var1.setString("rotation", this.rotation.toString());
      var1.setString("mirror", this.mirror.toString());
      var1.setString("mode", this.mode.toString());
      var1.setBoolean("ignoreEntities", this.ignoreEntities);
      var1.setBoolean("powered", this.powered);
      var1.setBoolean("showair", this.showAir);
      var1.setBoolean("showboundingbox", this.showBoundingBox);
      var1.setFloat("integrity", this.integrity);
      var1.setLong("seed", this.seed);
      return var1;
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      this.setName(var1.getString("name"));
      this.author = var1.getString("author");
      this.metadata = var1.getString("metadata");
      int var2 = MathHelper.clamp(var1.getInteger("posX"), -32, 32);
      int var3 = MathHelper.clamp(var1.getInteger("posY"), -32, 32);
      int var4 = MathHelper.clamp(var1.getInteger("posZ"), -32, 32);
      this.position = new BlockPos(var2, var3, var4);
      int var5 = MathHelper.clamp(var1.getInteger("sizeX"), 0, 32);
      int var6 = MathHelper.clamp(var1.getInteger("sizeY"), 0, 32);
      int var7 = MathHelper.clamp(var1.getInteger("sizeZ"), 0, 32);
      this.size = new BlockPos(var5, var6, var7);

      try {
         this.rotation = Rotation.valueOf(var1.getString("rotation"));
      } catch (IllegalArgumentException var11) {
         this.rotation = Rotation.NONE;
      }

      try {
         this.mirror = Mirror.valueOf(var1.getString("mirror"));
      } catch (IllegalArgumentException var10) {
         this.mirror = Mirror.NONE;
      }

      try {
         this.mode = TileEntityStructure.Mode.valueOf(var1.getString("mode"));
      } catch (IllegalArgumentException var9) {
         this.mode = TileEntityStructure.Mode.DATA;
      }

      this.ignoreEntities = var1.getBoolean("ignoreEntities");
      this.powered = var1.getBoolean("powered");
      this.showAir = var1.getBoolean("showair");
      this.showBoundingBox = var1.getBoolean("showboundingbox");
      if (var1.hasKey("integrity")) {
         this.integrity = var1.getFloat("integrity");
      } else {
         this.integrity = 1.0F;
      }

      this.seed = var1.getLong("seed");
      this.updateBlockState();
   }

   private void updateBlockState() {
      if (this.world != null) {
         BlockPos var1 = this.getPos();
         IBlockState var2 = this.world.getBlockState(var1);
         if (var2.getBlock() == Blocks.STRUCTURE_BLOCK) {
            this.world.setBlockState(var1, var2.withProperty(BlockStructure.MODE, this.mode), 2);
         }
      }

   }

   @Nullable
   public SPacketUpdateTileEntity getUpdatePacket() {
      return new SPacketUpdateTileEntity(this.pos, 7, this.getUpdateTag());
   }

   public NBTTagCompound getUpdateTag() {
      return this.writeToNBT(new NBTTagCompound());
   }

   public boolean usedBy(EntityPlayer var1) {
      if (!var1.canUseCommandBlock()) {
         return false;
      } else {
         if (var1.getEntityWorld().isRemote) {
            var1.openEditStructure(this);
         }

         return true;
      }
   }

   public String getName() {
      return this.name;
   }

   public void setName(String var1) {
      String var2 = var1;

      for(char var6 : ChatAllowedCharacters.ILLEGAL_STRUCTURE_CHARACTERS) {
         var2 = var2.replace(var6, '_');
      }

      this.name = var2;
   }

   public void createdBy(EntityLivingBase var1) {
      if (!StringUtils.isNullOrEmpty(var1.getName())) {
         this.author = var1.getName();
      }

   }

   @SideOnly(Side.CLIENT)
   public BlockPos getPosition() {
      return this.position;
   }

   public void setPosition(BlockPos var1) {
      this.position = var1;
   }

   @SideOnly(Side.CLIENT)
   public BlockPos getStructureSize() {
      return this.size;
   }

   public void setSize(BlockPos var1) {
      this.size = var1;
   }

   @SideOnly(Side.CLIENT)
   public Mirror getMirror() {
      return this.mirror;
   }

   public void setMirror(Mirror var1) {
      this.mirror = var1;
   }

   public void setRotation(Rotation var1) {
      this.rotation = var1;
   }

   public void setMetadata(String var1) {
      this.metadata = var1;
   }

   @SideOnly(Side.CLIENT)
   public Rotation getRotation() {
      return this.rotation;
   }

   @SideOnly(Side.CLIENT)
   public String getMetadata() {
      return this.metadata;
   }

   public TileEntityStructure.Mode getMode() {
      return this.mode;
   }

   public void setMode(TileEntityStructure.Mode var1) {
      this.mode = var1;
      IBlockState var2 = this.world.getBlockState(this.getPos());
      if (var2.getBlock() == Blocks.STRUCTURE_BLOCK) {
         this.world.setBlockState(this.getPos(), var2.withProperty(BlockStructure.MODE, var1), 2);
      }

   }

   public void setIgnoresEntities(boolean var1) {
      this.ignoreEntities = var1;
   }

   public void setIntegrity(float var1) {
      this.integrity = var1;
   }

   public void setSeed(long var1) {
      this.seed = var1;
   }

   @SideOnly(Side.CLIENT)
   public void nextMode() {
      switch(this.getMode()) {
      case SAVE:
         this.setMode(TileEntityStructure.Mode.LOAD);
         break;
      case LOAD:
         this.setMode(TileEntityStructure.Mode.CORNER);
         break;
      case CORNER:
         this.setMode(TileEntityStructure.Mode.DATA);
         break;
      case DATA:
         this.setMode(TileEntityStructure.Mode.SAVE);
      }

   }

   @SideOnly(Side.CLIENT)
   public boolean ignoresEntities() {
      return this.ignoreEntities;
   }

   @SideOnly(Side.CLIENT)
   public float getIntegrity() {
      return this.integrity;
   }

   @SideOnly(Side.CLIENT)
   public long getSeed() {
      return this.seed;
   }

   public boolean detectSize() {
      if (this.mode != TileEntityStructure.Mode.SAVE) {
         return false;
      } else {
         BlockPos var1 = this.getPos();
         boolean var2 = true;
         BlockPos var3 = new BlockPos(var1.getX() - 80, 0, var1.getZ() - 80);
         BlockPos var4 = new BlockPos(var1.getX() + 80, 255, var1.getZ() + 80);
         List var5 = this.getNearbyCornerBlocks(var3, var4);
         List var6 = this.filterRelatedCornerBlocks(var5);
         if (var6.size() < 1) {
            return false;
         } else {
            StructureBoundingBox var7 = this.calculateEnclosingBoundingBox(var1, var6);
            if (var7.maxX - var7.minX > 1 && var7.maxY - var7.minY > 1 && var7.maxZ - var7.minZ > 1) {
               this.position = new BlockPos(var7.minX - var1.getX() + 1, var7.minY - var1.getY() + 1, var7.minZ - var1.getZ() + 1);
               this.size = new BlockPos(var7.maxX - var7.minX - 1, var7.maxY - var7.minY - 1, var7.maxZ - var7.minZ - 1);
               this.markDirty();
               IBlockState var8 = this.world.getBlockState(var1);
               this.world.notifyBlockUpdate(var1, var8, var8, 3);
               return true;
            } else {
               return false;
            }
         }
      }
   }

   private List filterRelatedCornerBlocks(List var1) {
      Iterable var2 = Iterables.filter(var1, new Predicate() {
         public boolean apply(@Nullable TileEntityStructure var1) {
            return var1.mode == TileEntityStructure.Mode.CORNER && TileEntityStructure.this.name.equals(var1.name);
         }
      });
      return Lists.newArrayList(var2);
   }

   private List getNearbyCornerBlocks(BlockPos var1, BlockPos var2) {
      ArrayList var3 = Lists.newArrayList();

      for(BlockPos.MutableBlockPos var5 : BlockPos.getAllInBoxMutable(var1, var2)) {
         IBlockState var6 = this.world.getBlockState(var5);
         if (var6.getBlock() == Blocks.STRUCTURE_BLOCK) {
            TileEntity var7 = this.world.getTileEntity(var5);
            if (var7 != null && var7 instanceof TileEntityStructure) {
               var3.add((TileEntityStructure)var7);
            }
         }
      }

      return var3;
   }

   private StructureBoundingBox calculateEnclosingBoundingBox(BlockPos var1, List var2) {
      StructureBoundingBox var3;
      if (var2.size() > 1) {
         BlockPos var4 = ((TileEntityStructure)var2.get(0)).getPos();
         var3 = new StructureBoundingBox(var4, var4);
      } else {
         var3 = new StructureBoundingBox(var1, var1);
      }

      for(TileEntityStructure var5 : var2) {
         BlockPos var6 = var5.getPos();
         if (var6.getX() < var3.minX) {
            var3.minX = var6.getX();
         } else if (var6.getX() > var3.maxX) {
            var3.maxX = var6.getX();
         }

         if (var6.getY() < var3.minY) {
            var3.minY = var6.getY();
         } else if (var6.getY() > var3.maxY) {
            var3.maxY = var6.getY();
         }

         if (var6.getZ() < var3.minZ) {
            var3.minZ = var6.getZ();
         } else if (var6.getZ() > var3.maxZ) {
            var3.maxZ = var6.getZ();
         }
      }

      return var3;
   }

   @SideOnly(Side.CLIENT)
   public void writeCoordinates(ByteBuf var1) {
      var1.writeInt(this.pos.getX());
      var1.writeInt(this.pos.getY());
      var1.writeInt(this.pos.getZ());
   }

   public boolean save() {
      return this.save(true);
   }

   public boolean save(boolean var1) {
      if (this.mode == TileEntityStructure.Mode.SAVE && !this.world.isRemote && !StringUtils.isNullOrEmpty(this.name)) {
         BlockPos var2 = this.getPos().add(this.position);
         WorldServer var3 = (WorldServer)this.world;
         MinecraftServer var4 = this.world.getMinecraftServer();
         TemplateManager var5 = var3.getStructureTemplateManager();
         Template var6 = var5.getTemplate(var4, new ResourceLocation(this.name));
         var6.takeBlocksFromWorld(this.world, var2, this.size, !this.ignoreEntities, Blocks.STRUCTURE_VOID);
         var6.setAuthor(this.author);
         return !var1 || var5.writeTemplate(var4, new ResourceLocation(this.name));
      } else {
         return false;
      }
   }

   public boolean load() {
      return this.load(true);
   }

   public boolean load(boolean var1) {
      if (this.mode == TileEntityStructure.Mode.LOAD && !this.world.isRemote && !StringUtils.isNullOrEmpty(this.name)) {
         BlockPos var2 = this.getPos();
         BlockPos var3 = var2.add(this.position);
         WorldServer var4 = (WorldServer)this.world;
         MinecraftServer var5 = this.world.getMinecraftServer();
         TemplateManager var6 = var4.getStructureTemplateManager();
         Template var7 = var6.get(var5, new ResourceLocation(this.name));
         if (var7 == null) {
            return false;
         } else {
            if (!StringUtils.isNullOrEmpty(var7.getAuthor())) {
               this.author = var7.getAuthor();
            }

            BlockPos var8 = var7.getSize();
            boolean var9 = this.size.equals(var8);
            if (!var9) {
               this.size = var8;
               this.markDirty();
               IBlockState var10 = this.world.getBlockState(var2);
               this.world.notifyBlockUpdate(var2, var10, var10, 3);
            }

            if (var1 && !var9) {
               return false;
            } else {
               PlacementSettings var11 = (new PlacementSettings()).setMirror(this.mirror).setRotation(this.rotation).setIgnoreEntities(this.ignoreEntities).setChunk((ChunkPos)null).setReplacedBlock((Block)null).setIgnoreStructureBlock(false);
               if (this.integrity < 1.0F) {
                  var11.setIntegrity(MathHelper.clamp(this.integrity, 0.0F, 1.0F)).setSeed(Long.valueOf(this.seed));
               }

               var7.addBlocksToWorldChunk(this.world, var3, var11);
               return true;
            }
         }
      } else {
         return false;
      }
   }

   public void unloadStructure() {
      WorldServer var1 = (WorldServer)this.world;
      TemplateManager var2 = var1.getStructureTemplateManager();
      var2.remove(new ResourceLocation(this.name));
   }

   public boolean isStructureLoadable() {
      if (this.mode == TileEntityStructure.Mode.LOAD && !this.world.isRemote) {
         WorldServer var1 = (WorldServer)this.world;
         MinecraftServer var2 = this.world.getMinecraftServer();
         TemplateManager var3 = var1.getStructureTemplateManager();
         return var3.get(var2, new ResourceLocation(this.name)) != null;
      } else {
         return false;
      }
   }

   public boolean isPowered() {
      return this.powered;
   }

   public void setPowered(boolean var1) {
      this.powered = var1;
   }

   @SideOnly(Side.CLIENT)
   public boolean showsAir() {
      return this.showAir;
   }

   public void setShowAir(boolean var1) {
      this.showAir = var1;
   }

   @SideOnly(Side.CLIENT)
   public boolean showsBoundingBox() {
      return this.showBoundingBox;
   }

   public void setShowBoundingBox(boolean var1) {
      this.showBoundingBox = var1;
   }

   @Nullable
   public ITextComponent getDisplayName() {
      return new TextComponentTranslation("structure_block.hover." + this.mode.modeName, new Object[]{this.mode == TileEntityStructure.Mode.DATA ? this.metadata : this.name});
   }

   public static enum Mode implements IStringSerializable {
      SAVE("save", 0),
      LOAD("load", 1),
      CORNER("corner", 2),
      DATA("data", 3);

      private static final TileEntityStructure.Mode[] MODES = new TileEntityStructure.Mode[values().length];
      private final String modeName;
      private final int modeId;

      private Mode(String var3, int var4) {
         this.modeName = var3;
         this.modeId = var4;
      }

      public String getName() {
         return this.modeName;
      }

      public int getModeId() {
         return this.modeId;
      }

      public static TileEntityStructure.Mode getById(int var0) {
         return var0 >= 0 && var0 < MODES.length ? MODES[var0] : MODES[0];
      }

      static {
         for(TileEntityStructure.Mode var3 : values()) {
            MODES[var3.getModeId()] = var3;
         }

      }
   }
}
