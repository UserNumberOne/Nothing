package net.minecraft.client.player.inventory;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ContainerLocalMenu extends InventoryBasic implements ILockableContainer {
   private final String guiID;
   private final Map dataValues = Maps.newHashMap();

   public ContainerLocalMenu(String var1, ITextComponent var2, int var3) {
      super(title, slotCount);
      this.guiID = id;
   }

   public int getField(int var1) {
      return this.dataValues.containsKey(Integer.valueOf(id)) ? ((Integer)this.dataValues.get(Integer.valueOf(id))).intValue() : 0;
   }

   public void setField(int var1, int var2) {
      this.dataValues.put(Integer.valueOf(id), Integer.valueOf(value));
   }

   public int getFieldCount() {
      return this.dataValues.size();
   }

   public boolean isLocked() {
      return false;
   }

   public void setLockCode(LockCode var1) {
   }

   public LockCode getLockCode() {
      return LockCode.EMPTY_CODE;
   }

   public String getGuiID() {
      return this.guiID;
   }

   public Container createContainer(InventoryPlayer var1, EntityPlayer var2) {
      throw new UnsupportedOperationException();
   }
}
