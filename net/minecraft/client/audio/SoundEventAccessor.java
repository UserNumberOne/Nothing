package net.minecraft.client.audio;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class SoundEventAccessor implements ISoundEventAccessor {
   private final List accessorList = Lists.newArrayList();
   private final Random rnd = new Random();
   private final ResourceLocation location;
   private final ITextComponent subtitle;

   public SoundEventAccessor(ResourceLocation var1, @Nullable String var2) {
      this.location = var1;
      this.subtitle = var2 == null ? null : new TextComponentTranslation(var2, new Object[0]);
   }

   public int getWeight() {
      int var1 = 0;

      for(ISoundEventAccessor var3 : this.accessorList) {
         var1 += var3.getWeight();
      }

      return var1;
   }

   public Sound cloneEntry() {
      int var1 = this.getWeight();
      if (!this.accessorList.isEmpty() && var1 != 0) {
         int var2 = this.rnd.nextInt(var1);

         for(ISoundEventAccessor var4 : this.accessorList) {
            var2 -= var4.getWeight();
            if (var2 < 0) {
               return (Sound)var4.cloneEntry();
            }
         }

         return SoundHandler.MISSING_SOUND;
      } else {
         return SoundHandler.MISSING_SOUND;
      }
   }

   public void addSound(ISoundEventAccessor var1) {
      this.accessorList.add(var1);
   }

   public ResourceLocation getLocation() {
      return this.location;
   }

   @Nullable
   public ITextComponent getSubtitle() {
      return this.subtitle;
   }
}
