package net.minecraft.entity.item;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import javax.annotation.Nullable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class EntityPainting extends EntityHanging {
   public EntityPainting.EnumArt art;

   public EntityPainting(World var1) {
      super(var1);
      this.art = EntityPainting.EnumArt.values()[this.rand.nextInt(EntityPainting.EnumArt.values().length)];
   }

   public EntityPainting(World var1, BlockPos var2, EnumFacing var3) {
      super(var1, var2);
      ArrayList var4 = Lists.newArrayList();

      for(EntityPainting.EnumArt var8 : EntityPainting.EnumArt.values()) {
         this.art = var8;
         this.updateFacingWithBoundingBox(var3);
         if (this.onValidSurface()) {
            var4.add(var8);
         }
      }

      if (!var4.isEmpty()) {
         this.art = (EntityPainting.EnumArt)var4.get(this.rand.nextInt(var4.size()));
      }

      this.updateFacingWithBoundingBox(var3);
   }

   public void writeEntityToNBT(NBTTagCompound var1) {
      var1.setString("Motive", this.art.title);
      super.writeEntityToNBT(var1);
   }

   public void readEntityFromNBT(NBTTagCompound var1) {
      String var2 = var1.getString("Motive");

      for(EntityPainting.EnumArt var6 : EntityPainting.EnumArt.values()) {
         if (var6.title.equals(var2)) {
            this.art = var6;
         }
      }

      if (this.art == null) {
         this.art = EntityPainting.EnumArt.KEBAB;
      }

      super.readEntityFromNBT(var1);
   }

   public int getWidthPixels() {
      return this.art.sizeX;
   }

   public int getHeightPixels() {
      return this.art.sizeY;
   }

   public void onBroken(@Nullable Entity var1) {
      if (this.world.getGameRules().getBoolean("doEntityDrops")) {
         this.playSound(SoundEvents.ENTITY_PAINTING_BREAK, 1.0F, 1.0F);
         if (var1 instanceof EntityPlayer) {
            EntityPlayer var2 = (EntityPlayer)var1;
            if (var2.capabilities.isCreativeMode) {
               return;
            }
         }

         this.entityDropItem(new ItemStack(Items.PAINTING), 0.0F);
      }

   }

   public void playPlaceSound() {
      this.playSound(SoundEvents.ENTITY_PAINTING_PLACE, 1.0F, 1.0F);
   }

   public void setLocationAndAngles(double var1, double var3, double var5, float var7, float var8) {
      this.setPosition(var1, var3, var5);
   }

   public static enum EnumArt {
      KEBAB("Kebab", 16, 16, 0, 0),
      AZTEC("Aztec", 16, 16, 16, 0),
      ALBAN("Alban", 16, 16, 32, 0),
      AZTEC_2("Aztec2", 16, 16, 48, 0),
      BOMB("Bomb", 16, 16, 64, 0),
      PLANT("Plant", 16, 16, 80, 0),
      WASTELAND("Wasteland", 16, 16, 96, 0),
      POOL("Pool", 32, 16, 0, 32),
      COURBET("Courbet", 32, 16, 32, 32),
      SEA("Sea", 32, 16, 64, 32),
      SUNSET("Sunset", 32, 16, 96, 32),
      CREEBET("Creebet", 32, 16, 128, 32),
      WANDERER("Wanderer", 16, 32, 0, 64),
      GRAHAM("Graham", 16, 32, 16, 64),
      MATCH("Match", 32, 32, 0, 128),
      BUST("Bust", 32, 32, 32, 128),
      STAGE("Stage", 32, 32, 64, 128),
      VOID("Void", 32, 32, 96, 128),
      SKULL_AND_ROSES("SkullAndRoses", 32, 32, 128, 128),
      WITHER("Wither", 32, 32, 160, 128),
      FIGHTERS("Fighters", 64, 32, 0, 96),
      POINTER("Pointer", 64, 64, 0, 192),
      PIGSCENE("Pigscene", 64, 64, 64, 192),
      BURNING_SKULL("BurningSkull", 64, 64, 128, 192),
      SKELETON("Skeleton", 64, 48, 192, 64),
      DONKEY_KONG("DonkeyKong", 64, 48, 192, 112);

      public static final int MAX_NAME_LENGTH = "SkullAndRoses".length();
      public final String title;
      public final int sizeX;
      public final int sizeY;
      public final int offsetX;
      public final int offsetY;

      private EnumArt(String var3, int var4, int var5, int var6, int var7) {
         this.title = var3;
         this.sizeX = var4;
         this.sizeY = var5;
         this.offsetX = var6;
         this.offsetY = var7;
      }
   }
}
