package net.minecraft.util;

public class ActionResult {
   private final EnumActionResult type;
   private final Object result;

   public ActionResult(EnumActionResult var1, Object var2) {
      this.type = var1;
      this.result = var2;
   }

   public EnumActionResult getType() {
      return this.type;
   }

   public Object getResult() {
      return this.result;
   }

   public static ActionResult newResult(EnumActionResult var0, Object var1) {
      return new ActionResult(var0, var1);
   }
}
