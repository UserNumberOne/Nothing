package net.minecraft.tileentity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.ArrayList;
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
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerBeacon;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.potion.Potion;
import net.minecraft.stats.AchievementList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_10_R1.potion.CraftPotionUtil;
import org.bukkit.potion.PotionEffect;

public class TileEntityBeacon extends TileEntityLockable implements ITickable, ISidedInventory {
   public static final Potion[][] EFFECTS_LIST = new Potion[][]{{MobEffects.SPEED, MobEffects.HASTE}, {MobEffects.RESISTANCE, MobEffects.JUMP_BOOST}, {MobEffects.STRENGTH}, {MobEffects.REGENERATION}};
   private static final Set VALID_EFFECTS = Sets.newHashSet();
   private final List beamSegments = Lists.newArrayList();
   private boolean isComplete;
   public int levels = -1;
   @Nullable
   public Potion primaryEffect;
   @Nullable
   public Potion secondaryEffect;
   private ItemStack payment;
   private String customName;
   public List transaction = new ArrayList();
   private int maxStack = 64;

   static {
      for(Potion[] amobeffectlist1 : EFFECTS_LIST) {
         Collections.addAll(VALID_EFFECTS, amobeffectlist1);
      }

   }

   public ItemStack[] getContents() {
      return new ItemStack[]{this.payment};
   }

   public void onOpen(CraftHumanEntity who) {
      this.transaction.add(who);
   }

   public void onClose(CraftHumanEntity who) {
      this.transaction.remove(who);
   }

   public List getViewers() {
      return this.transaction;
   }

   public void setMaxStackSize(int size) {
      this.maxStack = size;
   }

   public PotionEffect getPrimaryEffect() {
      return this.primaryEffect != null ? CraftPotionUtil.toBukkit(new net.minecraft.potion.PotionEffect(this.primaryEffect, this.getLevel(), this.getAmplification(), true, true)) : null;
   }

   public PotionEffect getSecondaryEffect() {
      return this.hasSecondaryEffect() ? CraftPotionUtil.toBukkit(new net.minecraft.potion.PotionEffect(this.secondaryEffect, this.getLevel(), this.getAmplification(), true, true)) : null;
   }

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

   private byte getAmplification() {
      byte b0 = 0;
      if (this.levels >= 4 && this.primaryEffect == this.secondaryEffect) {
         b0 = 1;
      }

      return b0;
   }

   private int getLevel() {
      int i = (9 + this.levels * 2) * 20;
      return i;
   }

   public List getHumansInRange() {
      double d0 = (double)(this.levels * 10 + 10);
      int j = this.pos.getX();
      int k = this.pos.getY();
      int l = this.pos.getZ();
      AxisAlignedBB axisalignedbb = (new AxisAlignedBB((double)j, (double)k, (double)l, (double)(j + 1), (double)(k + 1), (double)(l + 1))).expandXyz(d0).addCoord(0.0D, (double)this.world.getHeight(), 0.0D);
      List list = this.world.getEntitiesWithinAABB(EntityPlayer.class, axisalignedbb);
      return list;
   }

   private void applyEffect(List list, Potion effects, int i, int b0) {
      for(EntityPlayer entityhuman : list) {
         entityhuman.addPotionEffect(new net.minecraft.potion.PotionEffect(effects, i, b0, true, true));
      }

   }

   private boolean hasSecondaryEffect() {
      return this.levels >= 4 && this.primaryEffect != this.secondaryEffect && this.secondaryEffect != null;
   }

   private void addEffectsToPlayers() {
      if (this.isComplete && this.levels > 0 && !this.world.isRemote && this.primaryEffect != null) {
         byte b0 = this.getAmplification();
         int i = this.getLevel();
         List list = this.getHumansInRange();
         this.applyEffect(list, this.primaryEffect, i, b0);
         if (this.hasSecondaryEffect()) {
            this.applyEffect(list, this.secondaryEffect, i, 0);
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
      TileEntityBeacon.BeamSegment tileentitybeacon_beaconcolortracker = new TileEntityBeacon.BeamSegment(EntitySheep.getDyeRgb(EnumDyeColor.WHITE));
      this.beamSegments.add(tileentitybeacon_beaconcolortracker);
      boolean flag = true;
      BlockPos.MutableBlockPos blockposition_mutableblockposition = new BlockPos.MutableBlockPos();

      for(int i1 = k + 1; i1 < 256; ++i1) {
         IBlockState iblockdata = this.world.getBlockState(blockposition_mutableblockposition.setPos(j, i1, l));
         float[] afloat;
         if (iblockdata.getBlock() == Blocks.STAINED_GLASS) {
            afloat = EntitySheep.getDyeRgb((EnumDyeColor)iblockdata.getValue(BlockStainedGlass.COLOR));
         } else {
            if (iblockdata.getBlock() != Blocks.STAINED_GLASS_PANE) {
               if (iblockdata.getLightOpacity() >= 15 && iblockdata.getBlock() != Blocks.BEDROCK) {
                  this.isComplete = false;
                  this.beamSegments.clear();
                  break;
               }

               tileentitybeacon_beaconcolortracker.incrementHeight();
               continue;
            }

            afloat = EntitySheep.getDyeRgb((EnumDyeColor)iblockdata.getValue(BlockStainedGlassPane.COLOR));
         }

         if (!flag) {
            afloat = new float[]{(tileentitybeacon_beaconcolortracker.getColors()[0] + afloat[0]) / 2.0F, (tileentitybeacon_beaconcolortracker.getColors()[1] + afloat[1]) / 2.0F, (tileentitybeacon_beaconcolortracker.getColors()[2] + afloat[2]) / 2.0F};
         }

         if (Arrays.equals(afloat, tileentitybeacon_beaconcolortracker.getColors())) {
            tileentitybeacon_beaconcolortracker.incrementHeight();
         } else {
            tileentitybeacon_beaconcolortracker = new TileEntityBeacon.BeamSegment(afloat);
            this.beamSegments.add(tileentitybeacon_beaconcolortracker);
         }

         flag = false;
      }

      if (this.isComplete) {
         for(int var14 = 1; var14 <= 4; this.levels = var14++) {
            int j1 = k - var14;
            if (j1 < 0) {
               break;
            }

            boolean flag1 = true;

            for(int k1 = j - var14; k1 <= j + var14 && flag1; ++k1) {
               for(int l1 = l - var14; l1 <= l + var14; ++l1) {
                  Block block = this.world.getBlockState(new BlockPos(k1, j1, l1)).getBlock();
                  if (block != Blocks.EMERALD_BLOCK && block != Blocks.GOLD_BLOCK && block != Blocks.DIAMOND_BLOCK && block != Blocks.IRON_BLOCK) {
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
         for(EntityPlayer entityhuman : this.world.getEntitiesWithinAABB(EntityPlayer.class, (new AxisAlignedBB((double)j, (double)k, (double)l, (double)j, (double)(k - 4), (double)l)).expand(10.0D, 5.0D, 10.0D))) {
            entityhuman.addStat(AchievementList.FULL_BEACON);
         }
      }

   }

   @Nullable
   public SPacketUpdateTileEntity getUpdatePacket() {
      return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
   }

   public NBTTagCompound getUpdateTag() {
      return this.writeToNBT(new NBTTagCompound());
   }

   @Nullable
   private static Potion isBeaconEffect(int i) {
      Potion mobeffectlist = Potion.getPotionById(i);
      return VALID_EFFECTS.contains(mobeffectlist) ? mobeffectlist : null;
   }

   public void readFromNBT(NBTTagCompound nbttagcompound) {
      super.readFromNBT(nbttagcompound);
      this.primaryEffect = isBeaconEffect(nbttagcompound.getInteger("Primary"));
      this.secondaryEffect = isBeaconEffect(nbttagcompound.getInteger("Secondary"));
      this.levels = nbttagcompound.getInteger("Levels");
   }

   public NBTTagCompound writeToNBT(NBTTagCompound nbttagcompound) {
      super.writeToNBT(nbttagcompound);
      nbttagcompound.setInteger("Primary", Potion.getIdFromPotion(this.primaryEffect));
      nbttagcompound.setInteger("Secondary", Potion.getIdFromPotion(this.secondaryEffect));
      nbttagcompound.setInteger("Levels", this.levels);
      return nbttagcompound;
   }

   public int getSizeInventory() {
      return 1;
   }

   @Nullable
   public ItemStack getStackInSlot(int i) {
      return i == 0 ? this.payment : null;
   }

   @Nullable
   public ItemStack decrStackSize(int i, int j) {
      if (i == 0 && this.payment != null) {
         if (j >= this.payment.stackSize) {
            ItemStack itemstack = this.payment;
            this.payment = null;
            return itemstack;
         } else {
            this.payment.stackSize -= j;
            return new ItemStack(this.payment.getItem(), j, this.payment.getMetadata());
         }
      } else {
         return null;
      }
   }

   @Nullable
   public ItemStack removeStackFromSlot(int i) {
      if (i == 0) {
         ItemStack itemstack = this.payment;
         this.payment = null;
         return itemstack;
      } else {
         return null;
      }
   }

   public void setInventorySlotContents(int i, @Nullable ItemStack itemstack) {
      if (i == 0) {
         this.payment = itemstack;
      }

   }

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.beacon";
   }

   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   public void setName(String s) {
      this.customName = s;
   }

   public int getInventoryStackLimit() {
      return 1;
   }

   public boolean isUsableByPlayer(EntityPlayer entityhuman) {
      return this.world.getTileEntity(this.pos) != this ? false : entityhuman.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
   }

   public void openInventory(EntityPlayer entityhuman) {
   }

   public void closeInventory(EntityPlayer entityhuman) {
   }

   public boolean isItemValidForSlot(int i, ItemStack itemstack) {
      return itemstack.getItem() == Items.EMERALD || itemstack.getItem() == Items.DIAMOND || itemstack.getItem() == Items.GOLD_INGOT || itemstack.getItem() == Items.IRON_INGOT;
   }

   public String getGuiID() {
      return "minecraft:beacon";
   }

   public Container createContainer(InventoryPlayer playerinventory, EntityPlayer entityhuman) {
      return new ContainerBeacon(playerinventory, this);
   }

   public int getField(int i) {
      switch(i) {
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

   public void setField(int i, int j) {
      switch(i) {
      case 0:
         this.levels = j;
         break;
      case 1:
         this.primaryEffect = isBeaconEffect(j);
         break;
      case 2:
         this.secondaryEffect = isBeaconEffect(j);
      }

   }

   public int getFieldCount() {
      return 3;
   }

   public void clear() {
      this.payment = null;
   }

   public boolean receiveClientEvent(int i, int j) {
      if (i == 1) {
         this.updateBeacon();
         return true;
      } else {
         return super.receiveClientEvent(i, j);
      }
   }

   public int[] getSlotsForFace(EnumFacing enumdirection) {
      return new int[0];
   }

   public boolean canInsertItem(int i, ItemStack itemstack, EnumFacing enumdirection) {
      return false;
   }

   public boolean canExtractItem(int i, ItemStack itemstack, EnumFacing enumdirection) {
      return false;
   }

   public static class BeamSegment {
      private final float[] colors;
      private int height;

      public BeamSegment(float[] afloat) {
         this.colors = afloat;
         this.height = 1;
      }

      protected void incrementHeight() {
         ++this.height;
      }

      public float[] getColors() {
         return this.colors;
      }
   }
}
