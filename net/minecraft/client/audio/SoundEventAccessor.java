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
      this.location = locationIn;
      this.subtitle = subtitleIn == null ? null : new TextComponentTranslation(subtitleIn, new Object[0]);
   }

   public int getWeight() {
      int i = 0;

      for(ISoundEventAccessor isoundeventaccessor : this.accessorList) {
         i += isoundeventaccessor.getWeight();
      }

      return i;
   }

   public Sound cloneEntry() {
      int i = this.getWeight();
      if (!this.accessorList.isEmpty() && i != 0) {
         int j = this.rnd.nextInt(i);

         for(ISoundEventAccessor isoundeventaccessor : this.accessorList) {
            j -= isoundeventaccessor.getWeight();
            if (j < 0) {
               return (Sound)isoundeventaccessor.cloneEntry();
            }
         }

         return SoundHandler.MISSING_SOUND;
      } else {
         return SoundHandler.MISSING_SOUND;
      }
   }

   public void addSound(ISoundEventAccessor var1) {
      this.accessorList.add(p_188715_1_);
   }

   public ResourceLocation getLocation() {
      return this.location;
   }

   @Nullable
   public ITextComponent getSubtitle() {
      return this.subtitle;
   }
}
