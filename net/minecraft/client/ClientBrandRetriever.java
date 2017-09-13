package net.minecraft.client;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ClientBrandRetriever {
   public static String getClientModName() {
      return FMLCommonHandler.instance().getModName();
   }
}
