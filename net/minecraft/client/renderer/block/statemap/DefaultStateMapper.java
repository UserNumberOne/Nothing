package net.minecraft.client.renderer.block.statemap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class DefaultStateMapper extends StateMapperBase {
   protected ModelResourceLocation getModelResourceLocation(IBlockState var1) {
      return new ModelResourceLocation((ResourceLocation)Block.REGISTRY.getNameForObject(var1.getBlock()), this.getPropertyString(var1.getProperties()));
   }
}
