package net.minecraft.tileentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityBeacon extends TileEntityLockable implements ITickable, ISidedInventory {
   public static final Potion[][] EFFECTS_LIST = new Potion[][]{{MobEffects.SPEED, MobEffects.HASTE}, {MobEffects.RESISTANCE, MobEffects.JUMP_BOOST}, {MobEffects.STRENGTH}, {MobEffects.REGENERATION}};
   private static final Set VALID_EFFECTS = Sets.newHashSet();
   private final List beamSegments = Lists.newArrayList();
   @SideOnly(Side.CLIENT)
   private long beamRenderCounter;
   @SideOnly(Side.CLIENT)
   private float beamRenderScale;
   private boolean isComplete;
   private int levels = -1;
   @Nullable
   private Potion primaryEffect;
   @Nullable
   private Potion secondaryEffect;
   private ItemStack payment;
   private String customName;

   public void update() {
      if (this.world.getTotalWorldTime() % 80L == 0L) {
         this.updateBeacon();
      }

   }

   public void updateBeacon() {
      if (this.world != null) {
         this.updateSegmentColors();
         this.addEffectsToPlayers();
      }

   }

   private void addEffectsToPlayers() {
      if (this.isComplete && this.levels > 0 && !this.world.isRemote && this.primaryEffect != null) {
         double d0 = (double)(this.levels * 10 + 10);
         int i = 0;
         if (this.levels >= 4 && this.primaryEffect == this.secondaryEffect) {
            i = 1;
         }

         int j = (9 + this.levels * 2) * 20;
         int k = this.pos.getX();
         int l = this.pos.getY();
         int i1 = this.pos.getZ();
         AxisAlignedBB axisalignedbb = (new AxisAlignedBB((double)k, (double)l, (double)i1, (double)(k + 1), (double)(l + 1), (double)(i1 + 1))).expandXyz(d0).addCoord(0.0D, (double)this.world.getHeight(), 0.0D);
         List list = this.world.getEntitiesWithinAABB(EntityPlayer.class, axisalignedbb);

         for(EntityPlayer entityplayer : list) {
            entityplayer.addPotionEffect(new PotionEffect(this.primaryEffect, j, i, true, true));
         }

         if (this.levels >= 4 && this.primaryEffect != this.secondaryEffect && this.secondaryEffect != null) {
            for(EntityPlayer entityplayer1 : list) {
               entityplayer1.addPotionEffect(new PotionEffect(this.secondaryEffect, j, 0, true, true));
            }
         }
      }

   }

   private void updateSegmentColors() {
      int i = this.levels;
      int j = this.pos.getX();
      int k = this.pos.getY();
      int l = this.pos.getZ();
      this.levels = 0;
      this.beamSegments.clear();
      this.isComplete = true;
      TileEntityBeacon.BeamSegment tileentitybeacon$beamsegment = new TileEntityBeacon.BeamSegment(EntitySheep.getDyeRgb(EnumDyeColor.WHITE));
      this.beamSegments.add(tileentitybeacon$beamsegment);
      boolean flag = true;
      BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

      for(int i1 = k + 1; i1 < 256; ++i1) {
         IBlockState iblockstate = this.world.getBlockState(blockpos$mutableblockpos.setPos(j, i1, l));
         float[] afloat;
         if (iblockstate.getBlock() == Blocks.STAINED_GLASS) {
            afloat = EntitySheep.getDyeRgb((EnumDyeColor)iblockstate.getValue(BlockStainedGlass.COLOR));
         } else if (iblockstate.getBlock() != Blocks.STAINED_GLASS_PANE) {
            if (iblockstate.getLightOpacity(this.world, blockpos$mutableblockpos) >= 15 && iblockstate.getBlock() != Blocks.BEDROCK) {
               this.isComplete = false;
               this.beamSegments.clear();
               break;
            }

            float[] customColor = iblockstate.getBlock().getBeaconColorMultiplier(iblockstate, this.world, blockpos$mutableblockpos, this.getPos());
            if (customColor == null) {
               tileentitybeacon$beamsegment.incrementHeight();
               continue;
            }

            afloat = customColor;
         } else {
            afloat = EntitySheep.getDyeRgb((EnumDyeColor)iblockstate.getValue(BlockStainedGlassPane.COLOR));
         }

         if (!flag) {
            afloat = new float[]{(tileentitybeacon$beamsegment.getColors()[0] + afloat[0]) / 2.0F, (tileentitybeacon$beamsegment.getColors()[1] + afloat[1]) / 2.0F, (tileentitybeacon$beamsegment.getColors()[2] + afloat[2]) / 2.0F};
         }

         if (Arrays.equals(afloat, tileentitybeacon$beamsegment.getColors())) {
            tileentitybeacon$beamsegment.incrementHeight();
         } else {
            tileentitybeacon$beamsegment = new TileEntityBeacon.BeamSegment(afloat);
            this.beamSegments.add(tileentitybeacon$beamsegment);
         }

         flag = false;
      }

      if (this.isComplete) {
         for(int l1 = 1; l1 <= 4; this.levels = l1++) {
            int i2 = k - l1;
            if (i2 < 0) {
               break;
            }

            boolean flag1 = true;

            for(int j1 = j - l1; j1 <= j + l1 && flag1; ++j1) {
               for(int k1 = l - l1; k1 <= l + l1; ++k1) {
                  Block block = this.world.getBlockState(new BlockPos(j1, i2, k1)).getBlock();
                  if (!block.isBeaconBase(this.world, new BlockPos(j1, i2, k1), this.getPos())) {
                     flag1 = false;
                     break;
                  }
               }
            }

            if (!flag1) {
               break;
            }
         }

         if (this.levels == 0) {
            this.isComplete = false;
         }
      }

      if (!this.world.isRemote && this.levels == 4 && i < this.levels) {
         for(EntityPlayer entityplayer : this.world.getEntitiesWithinAABB(EntityPlayer.class, (new AxisAlignedBB((double)j, (double)k, (double)l, (double)j, (double)(k - 4), (double)l)).expand(10.0D, 5.0D, 10.0D))) {
            entityplayer.addStat(AchievementList.FULL_BEACON);
         }
      }

   }

   @SideOnly(Side.CLIENT)
   public List getBeamSegments() {
      return this.beamSegments;
   }

   @SideOnly(Side.CLIENT)
   public float shouldBeamRender() {
      if (!this.isComplete) {
         return 0.0F;
      } else {
         int i = (int)(this.world.getTotalWorldTime() - this.beamRenderCounter);
         this.beamRenderCounter = this.world.getTotalWorldTime();
         if (i > 1) {
            this.beamRenderScale -= (float)i / 40.0F;
            if (this.beamRenderScale < 0.0F) {
               this.beamRenderScale = 0.0F;
            }
         }

         this.beamRenderScale += 0.025F;
         if (this.beamRenderScale > 1.0F) {
            this.beamRenderScale = 1.0F;
         }

         return this.beamRenderScale;
      }
   }

   @Nullable
   public SPacketUpdateTileEntity getUpdatePacket() {
      return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
   }

   public NBTTagCompound getUpdateTag() {
      return this.writeToNBT(new NBTTagCompound());
   }

   @SideOnly(Side.CLIENT)
   public double getMaxRenderDistanceSquared() {
      return 65536.0D;
   }

   @Nullable
   private static Potion isBeaconEffect(int var0) {
      Potion potion = Potion.getPotionById(p_184279_0_);
      return VALID_EFFECTS.contains(potion) ? potion : null;
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(compound);
      this.primaryEffect = isBeaconEffect(compound.getInteger("Primary"));
      this.secondaryEffect = isBeaconEffect(compound.getInteger("Secondary"));
      this.levels = compound.getInteger("Levels");
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(compound);
      compound.setInteger("Primary", Potion.getIdFromPotion(this.primaryEffect));
      compound.setInteger("Secondary", Potion.getIdFromPotion(this.secondaryEffect));
      compound.setInteger("Levels", this.levels);
      return compound;
   }

   public int getSizeInventory() {
      return 1;
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      return index == 0 ? this.payment : null;
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      if (index == 0 && this.payment != null) {
         if (count >= this.payment.stackSize) {
            ItemStack itemstack = this.payment;
            this.payment = null;
            return itemstack;
         } else {
            this.payment.stackSize -= count;
            return new ItemStack(this.payment.getItem(), count, this.payment.getMetadata());
         }
      } else {
         return null;
      }
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      if (index == 0) {
         ItemStack itemstack = this.payment;
         this.payment = null;
         return itemstack;
      } else {
         return null;
      }
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      if (index == 0) {
         this.payment = stack;
      }

   }

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.beacon";
   }

   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   public void setName(String var1) {
      this.customName = name;
   }

   public int getInventoryStackLimit() {
      return 1;
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return this.world.getTileEntity(this.pos) != this ? false : player.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
   }

   public void openInventory(EntityPlayer var1) {
   }

   public void closeInventory(EntityPlayer var1) {
   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
      return stack.getItem() != null && stack.getItem().isBeaconPayment(stack);
   }

   public String getGuiID() {
      return "minecraft:beacon";
   }

   public Container createContainer(InventoryPlayer var1, EntityPlayer var2) {
      return new ContainerBeacon(playerInventory, this);
   }

   public int getField(int var1) {
      switch(id) {
      case 0:
         return this.levels;
      case 1:
         return Potion.getIdFromPotion(this.primaryEffect);
      case 2:
         return Potion.getIdFromPotion(this.secondaryEffect);
      default:
         return 0;
      }
   }

   public void setField(int var1, int var2) {
      switch(id) {
      case 0:
         this.levels = value;
         break;
      case 1:
         this.primaryEffect = isBeaconEffect(value);
         break;
      case 2:
         this.secondaryEffect = isBeaconEffect(value);
      }

   }

   public int getFieldCount() {
      return 3;
   }

   public void clear() {
      this.payment = null;
   }

   public boolean receiveClientEvent(int var1, int var2) {
      if (id == 1) {
         this.updateBeacon();
         return true;
      } else {
         return super.receiveClientEvent(id, type);
      }
   }

   public int[] getSlotsForFace(EnumFacing var1) {
      return new int[0];
   }

   public boolean canInsertItem(int var1, ItemStack var2, EnumFacing var3) {
      return false;
   }

   public boolean canExtractItem(int var1, ItemStack var2, EnumFacing var3) {
      return false;
   }

   static {
      for(Potion[] apotion : EFFECTS_LIST) {
         Collections.addAll(VALID_EFFECTS, apotion);
      }

   }

   public static class BeamSegment {
      private final float[] colors;
      private int height;

      public BeamSegment(float[] var1) {
         this.colors = colorsIn;
         this.height = 1;
      }

      protected void incrementHeight() {
         ++this.height;
      }

      public float[] getColors() {
         return this.colors;
      }

      @SideOnly(Side.CLIENT)
      public int getHeight() {
         return this.height;
      }
   }
}
