package net.minecraft.client.multiplayer;

import com.google.common.base.Objects;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import javax.annotation.Nullable;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.EmptyChunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.ChunkEvent.Load;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class ChunkProviderClient implements IChunkProvider {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Chunk blankChunk;
   private final Long2ObjectMap chunkMapping = new Long2ObjectOpenHashMap(8192) {
      protected void rehash(int var1) {
         if (var1 > this.key.length) {
            super.rehash(var1);
         }

      }
   };
   private final World world;

   public ChunkProviderClient(World var1) {
      this.blankChunk = new EmptyChunk(var1, 0, 0);
      this.world = var1;
   }

   public void unloadChunk(int var1, int var2) {
      Chunk var3 = this.provideChunk(var1, var2);
      if (!var3.isEmpty()) {
         var3.onChunkUnload();
      }

      this.chunkMapping.remove(ChunkPos.asLong(var1, var2));
   }

   @Nullable
   public Chunk getLoadedChunk(int var1, int var2) {
      return (Chunk)this.chunkMapping.get(ChunkPos.asLong(var1, var2));
   }

   public Chunk loadChunk(int var1, int var2) {
      Chunk var3 = new Chunk(this.world, var1, var2);
      this.chunkMapping.put(ChunkPos.asLong(var1, var2), var3);
      MinecraftForge.EVENT_BUS.post(new Load(var3));
      var3.setChunkLoaded(true);
      return var3;
   }

   public Chunk provideChunk(int var1, int var2) {
      return (Chunk)Objects.firstNonNull(this.getLoadedChunk(var1, var2), this.blankChunk);
   }

   public boolean tick() {
      long var1 = System.currentTimeMillis();
      ObjectIterator var3 = this.chunkMapping.values().iterator();

      while(var3.hasNext()) {
         Chunk var4 = (Chunk)var3.next();
         var4.onTick(System.currentTimeMillis() - var1 > 5L);
      }

      if (System.currentTimeMillis() - var1 > 100L) {
         LOGGER.info("Warning: Clientside chunk ticking took {} ms", new Object[]{System.currentTimeMillis() - var1});
      }

      return false;
   }

   public String makeString() {
      return "MultiplayerChunkCache: " + this.chunkMapping.size() + ", " + this.chunkMapping.size();
   }
}
