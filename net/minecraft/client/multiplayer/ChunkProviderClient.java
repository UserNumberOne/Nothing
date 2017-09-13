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
         if (p_rehash_1_ > this.key.length) {
            super.rehash(p_rehash_1_);
         }

      }
   };
   private final World world;

   public ChunkProviderClient(World var1) {
      this.blankChunk = new EmptyChunk(worldIn, 0, 0);
      this.world = worldIn;
   }

   public void unloadChunk(int var1, int var2) {
      Chunk chunk = this.provideChunk(x, z);
      if (!chunk.isEmpty()) {
         chunk.onChunkUnload();
      }

      this.chunkMapping.remove(ChunkPos.asLong(x, z));
   }

   @Nullable
   public Chunk getLoadedChunk(int var1, int var2) {
      return (Chunk)this.chunkMapping.get(ChunkPos.asLong(x, z));
   }

   public Chunk loadChunk(int var1, int var2) {
      Chunk chunk = new Chunk(this.world, chunkX, chunkZ);
      this.chunkMapping.put(ChunkPos.asLong(chunkX, chunkZ), chunk);
      MinecraftForge.EVENT_BUS.post(new Load(chunk));
      chunk.setChunkLoaded(true);
      return chunk;
   }

   public Chunk provideChunk(int var1, int var2) {
      return (Chunk)Objects.firstNonNull(this.getLoadedChunk(x, z), this.blankChunk);
   }

   public boolean tick() {
      long i = System.currentTimeMillis();
      ObjectIterator var3 = this.chunkMapping.values().iterator();

      while(var3.hasNext()) {
         Chunk chunk = (Chunk)var3.next();
         chunk.onTick(System.currentTimeMillis() - i > 5L);
      }

      if (System.currentTimeMillis() - i > 100L) {
         LOGGER.info("Warning: Clientside chunk ticking took {} ms", new Object[]{System.currentTimeMillis() - i});
      }

      return false;
   }

   public String makeString() {
      return "MultiplayerChunkCache: " + this.chunkMapping.size() + ", " + this.chunkMapping.size();
   }
}
