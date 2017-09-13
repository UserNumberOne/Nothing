package net.minecraft.world;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import net.minecraftforge.common.util.EnumHelper;

public enum DimensionType {
   OVERWORLD(0, "Overworld", "", WorldProviderSurface.class),
   NETHER(-1, "Nether", "_nether", WorldProviderHell.class),
   THE_END(1, "The End", "_end", WorldProviderEnd.class);

   private final int id;
   private final String name;
   private final String suffix;
   private final Class clazz;
   private boolean shouldLoadSpawn = false;
   private static Class[] ENUM_ARGS = new Class[]{Integer.TYPE, String.class, String.class, Class.class};

   private DimensionType(int var3, String var4, String var5, Class var6) {
      this.id = var3;
      this.name = var4;
      this.suffix = var5;
      this.clazz = var6;
      this.shouldLoadSpawn = var3 == 0;
   }

   public int getId() {
      return this.id;
   }

   public String getName() {
      return this.name;
   }

   public String getSuffix() {
      return this.suffix;
   }

   public WorldProvider createDimension() {
      try {
         Constructor var1 = this.clazz.getConstructor();
         return (WorldProvider)var1.newInstance();
      } catch (NoSuchMethodException var2) {
         throw new Error("Could not create new dimension", var2);
      } catch (InvocationTargetException var3) {
         throw new Error("Could not create new dimension", var3);
      } catch (InstantiationException var4) {
         throw new Error("Could not create new dimension", var4);
      } catch (IllegalAccessException var5) {
         throw new Error("Could not create new dimension", var5);
      }
   }

   public static DimensionType getById(int var0) {
      for(DimensionType var4 : values()) {
         if (var4.getId() == var0) {
            return var4;
         }
      }

      throw new IllegalArgumentException("Invalid dimension id " + var0);
   }

   public boolean shouldLoadSpawn() {
      return this.shouldLoadSpawn;
   }

   public DimensionType setLoadSpawn(boolean var1) {
      this.shouldLoadSpawn = var1;
      return this;
   }

   public static DimensionType register(String var0, String var1, int var2, Class var3, boolean var4) {
      String var5 = var0.replace(" ", "_").toLowerCase();
      DimensionType var6 = (DimensionType)EnumHelper.addEnum(DimensionType.class, var5, ENUM_ARGS, new Object[]{var2, var0, var1, var3});
      return var6.setLoadSpawn(var4);
   }

   static {
      EnumHelper.testEnum(DimensionType.class, ENUM_ARGS);
   }
}
