package net.minecraft.client.renderer.chunk;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CompiledChunk {
   public static final CompiledChunk DUMMY = new CompiledChunk() {
      protected void setLayerUsed(BlockRenderLayer var1) {
         throw new UnsupportedOperationException();
      }

      public void setLayerStarted(BlockRenderLayer var1) {
         throw new UnsupportedOperationException();
      }

      public boolean isVisible(EnumFacing var1, EnumFacing var2) {
         return false;
      }
   };
   private final boolean[] layersUsed = new boolean[BlockRenderLayer.values().length];
   private final boolean[] layersStarted = new boolean[BlockRenderLayer.values().length];
   private boolean empty = true;
   private final List tileEntities = Lists.newArrayList();
   private SetVisibility setVisibility = new SetVisibility();
   private VertexBuffer.State state;

   public boolean isEmpty() {
      return this.empty;
   }

   protected void setLayerUsed(BlockRenderLayer var1) {
      this.empty = false;
      this.layersUsed[var1.ordinal()] = true;
   }

   public boolean isLayerEmpty(BlockRenderLayer var1) {
      return !this.layersUsed[var1.ordinal()];
   }

   public void setLayerStarted(BlockRenderLayer var1) {
      this.layersStarted[var1.ordinal()] = true;
   }

   public boolean isLayerStarted(BlockRenderLayer var1) {
      return this.layersStarted[var1.ordinal()];
   }

   public List getTileEntities() {
      return this.tileEntities;
   }

   public void addTileEntity(TileEntity var1) {
      this.tileEntities.add(var1);
   }

   public boolean isVisible(EnumFacing var1, EnumFacing var2) {
      return this.setVisibility.isVisible(var1, var2);
   }

   public void setVisibility(SetVisibility var1) {
      this.setVisibility = var1;
   }

   public VertexBuffer.State getState() {
      return this.state;
   }

   public void setState(VertexBuffer.State var1) {
      this.state = var1;
   }
}
