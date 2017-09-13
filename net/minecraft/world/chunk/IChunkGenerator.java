package net.minecraft.world.chunk;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IChunkGenerator {
   Chunk provideChunk(int var1, int var2);

   void populate(int var1, int var2);

   boolean generateStructures(Chunk var1, int var2, int var3);

   List getPossibleCreatures(EnumCreatureType var1, BlockPos var2);

   @Nullable
   BlockPos getStrongholdGen(World var1, String var2, BlockPos var3);

   void recreateStructures(Chunk var1, int var2, int var3);
}
