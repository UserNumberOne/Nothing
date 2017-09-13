package net.minecraft.inventory;

public enum EntityEquipmentSlot {
   MAINHAND(EntityEquipmentSlot.Type.HAND, 0, 0, "mainhand"),
   OFFHAND(EntityEquipmentSlot.Type.HAND, 1, 5, "offhand"),
   FEET(EntityEquipmentSlot.Type.ARMOR, 0, 1, "feet"),
   LEGS(EntityEquipmentSlot.Type.ARMOR, 1, 2, "legs"),
   CHEST(EntityEquipmentSlot.Type.ARMOR, 2, 3, "chest"),
   HEAD(EntityEquipmentSlot.Type.ARMOR, 3, 4, "head");

   private final EntityEquipmentSlot.Type slotType;
   private final int index;
   private final int slotIndex;
   private final String name;

   private EntityEquipmentSlot(EntityEquipmentSlot.Type var3, int var4, int var5, String var6) {
      this.slotType = var3;
      this.index = var4;
      this.slotIndex = var5;
      this.name = var6;
   }

   public EntityEquipmentSlot.Type getSlotType() {
      return this.slotType;
   }

   public int getIndex() {
      return this.index;
   }

   public int getSlotIndex() {
      return this.slotIndex;
   }

   public String getName() {
      return this.name;
   }

   public static EntityEquipmentSlot fromString(String var0) {
      for(EntityEquipmentSlot var4 : values()) {
         if (var4.getName().equals(var0)) {
            return var4;
         }
      }

      throw new IllegalArgumentException("Invalid slot '" + var0 + "'");
   }

   public static enum Type {
      HAND,
      ARMOR;
   }
}
