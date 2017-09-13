package net.minecraft.entity.player;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public enum EnumPlayerModelParts {
   CAPE(0, "cape"),
   JACKET(1, "jacket"),
   LEFT_SLEEVE(2, "left_sleeve"),
   RIGHT_SLEEVE(3, "right_sleeve"),
   LEFT_PANTS_LEG(4, "left_pants_leg"),
   RIGHT_PANTS_LEG(5, "right_pants_leg"),
   HAT(6, "hat");

   private final int partId;
   private final int partMask;
   private final String partName;
   private final ITextComponent name;

   private EnumPlayerModelParts(int var3, String var4) {
      this.partId = var3;
      this.partMask = 1 << var3;
      this.partName = var4;
      this.name = new TextComponentTranslation("options.modelPart." + var4, new Object[0]);
   }

   public int getPartMask() {
      return this.partMask;
   }

   public int getPartId() {
      return this.partId;
   }

   public String getPartName() {
      return this.partName;
   }

   public ITextComponent getName() {
      return this.name;
   }
}