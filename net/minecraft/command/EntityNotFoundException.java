package net.minecraft.command;

public class EntityNotFoundException extends CommandException {
   public EntityNotFoundException() {
      this("commands.generic.entity.notFound");
   }

   public EntityNotFoundException(String var1, Object... var2) {
      super(message, args);
   }
}
