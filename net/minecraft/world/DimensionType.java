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

   private DimensionType(int idIn, String nameIn, String suffixIn, Class clazzIn) {
      this.id = idIn;
      this.name = nameIn;
      this.suffix = suffixIn;
      this.clazz = clazzIn;
      this.shouldLoadSpawn = idIn == 0;
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
         Constructor constructor = this.clazz.getConstructor();
         return (WorldProvider)constructor.newInstance();
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

   public static DimensionType getById(int id) {
      for(DimensionType dimensiontype : values()) {
         if (dimensiontype.getId() == id) {
            return dimensiontype;
         }
      }

      throw new IllegalArgumentException("Invalid dimension id " + id);
   }

   public boolean shouldLoadSpawn() {
      return this.shouldLoadSpawn;
   }

   public DimensionType setLoadSpawn(boolean value) {
      this.shouldLoadSpawn = value;
      return this;
   }

   public static DimensionType register(String name, String suffix, int id, Class provider, boolean keepLoaded) {
      String enum_name = name.replace(" ", "_").toLowerCase();
      DimensionType ret = (DimensionType)EnumHelper.addEnum(DimensionType.class, enum_name, ENUM_ARGS, new Object[]{id, name, suffix, provider});
      return ret.setLoadSpawn(keepLoaded);
   }

   static {
      EnumHelper.testEnum(DimensionType.class, ENUM_ARGS);
   }
}
