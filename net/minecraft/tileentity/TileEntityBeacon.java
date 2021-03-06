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
      for(Potion[] var3 : EFFECTS_LIST) {
         Collections.addAll(VALID_EFFECTS, var3);
      }

   }

   public ItemStack[] getContents() {
      return new ItemStack[]{this.payment};
   }

   public void onOpen(CraftHumanEntity var1) {
      this.transaction.add(var1);
   }

   public void onClose(CraftHumanEntity var1) {
      this.transaction.remove(var1);
   }

   public List getViewers() {
      return this.transaction;
   }

   public void setMaxStackSize(int var1) {
      this.maxStack = var1;
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
      byte var1 = 0;
      if (this.levels >= 4 && this.primaryEffect == this.secondaryEffect) {
         var1 = 1;
      }

      return var1;
   }

   private int getLevel() {
      int var1 = (9 + this.levels * 2) * 20;
      return var1;
   }

   public List getHumansInRange() {
      double var1 = (double)(this.levels * 10 + 10);
      int var3 = this.pos.getX();
      int var4 = this.pos.getY();
      int var5 = this.pos.getZ();
      AxisAlignedBB var6 = (new AxisAlignedBB((double)var3, (double)var4, (double)var5, (double)(var3 + 1), (double)(var4 + 1), (double)(var5 + 1))).expandXyz(var1).addCoord(0.0D, (double)this.world.getHeight(), 0.0D);
      List var7 = this.world.getEntitiesWithinAABB(EntityPlayer.class, var6);
      return var7;
   }

   private void applyEffect(List var1, Potion var2, int var3, int var4) {
      for(EntityPlayer var6 : var1) {
         var6.addPotionEffect(new net.minecraft.potion.PotionEffect(var2, var3, var4, true, true));
      }

   }

   private boolean hasSecondaryEffect() {
      return this.levels >= 4 && this.primaryEffect != this.secondaryEffect && this.secondaryEffect != null;
   }

   private void addEffectsToPlayers() {
      if (this.isComplete && this.levels > 0 && !this.world.isRemote && this.primaryEffect != null) {
         byte var1 = this.getAmplification();
         int var2 = this.getLevel();
         List var3 = this.getHumansInRange();
         this.applyEffect(var3, this.primaryEffect, var2, var1);
         if (this.hasSecondaryEffect()) {
            this.applyEffect(var3, this.secondaryEffect, var2, 0);
         }
      }

   }

   private void updateSegmentColors() {
      int var1 = this.levels;
      int var2 = this.pos.getX();
      int var3 = this.pos.getY();
      int var4 = this.pos.getZ();
      this.levels = 0;
      this.beamSegments.clear();
      this.isComplete = true;
      TileEntityBeacon.BeamSegment var5 = new TileEntityBeacon.BeamSegment(EntitySheep.getDyeRgb(EnumDyeColor.WHITE));
      this.beamSegments.add(var5);
      boolean var6 = true;
      BlockPos.MutableBlockPos var7 = new BlockPos.MutableBlockPos();

      for(int var8 = var3 + 1; var8 < 256; ++var8) {
         IBlockState var9 = this.world.getBlockState(var7.setPos(var2, var8, var4));
         float[] var10;
         if (var9.getBlock() == Blocks.STAINED_GLASS) {
            var10 = EntitySheep.getDyeRgb((EnumDyeColor)var9.getValue(BlockStainedGlass.COLOR));
         } else {
            if (var9.getBlock() != Blocks.STAINED_GLASS_PANE) {
               if (var9.getLightOpacity() >= 15 && var9.getBlock() != Blocks.BEDROCK) {
                  this.isComplete = false;
                  this.beamSegments.clear();
                  break;
               }

               var5.incrementHeight();
               continue;
            }

            var10 = EntitySheep.getDyeRgb((EnumDyeColor)var9.getValue(BlockStainedGlassPane.COLOR));
         }

         if (!var6) {
            var10 = new float[]{(var5.getColors()[0] + var10[0]) / 2.0F, (var5.getColors()[1] + var10[1]) / 2.0F, (var5.getColors()[2] + var10[2]) / 2.0F};
         }

         if (Arrays.equals(var10, var5.getColors())) {
            var5.incrementHeight();
         } else {
            var5 = new TileEntityBeacon.BeamSegment(var10);
            this.beamSegments.add(var5);
         }

         var6 = false;
      }

      if (this.isComplete) {
         for(int var14 = 1; var14 <= 4; this.levels = var14++) {
            int var15 = var3 - var14;
            if (var15 < 0) {
               break;
            }

            boolean var17 = true;

            for(int var11 = var2 - var14; var11 <= var2 + var14 && var17; ++var11) {
               for(int var12 = var4 - var14; var12 <= var4 + var14; ++var12) {
                  Block var13 = this.world.getBlockState(new BlockPos(var11, var15, var12)).getBlock();
                  if (var13 != Blocks.EMERALD_BLOCK && var13 != Blocks.GOLD_BLOCK && var13 != Blocks.DIAMOND_BLOCK && var13 != Blocks.IRON_BLOCK) {
                     var17 = false;
                     break;
                  }
               }
            }

            if (!var17) {
               break;
            }
         }

         if (this.levels == 0) {
            this.isComplete = false;
         }
      }

      if (!this.world.isRemote && this.levels == 4 && var1 < this.levels) {
         for(EntityPlayer var18 : this.world.getEntitiesWithinAABB(EntityPlayer.class, (new AxisAlignedBB((double)var2, (double)var3, (double)var4, (double)var2, (double)(var3 - 4), (double)var4)).expand(10.0D, 5.0D, 10.0D))) {
            var18.addStat(AchievementList.FULL_BEACON);
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
   private static Potion isBeaconEffect(int var0) {
      Potion var1 = Potion.getPotionById(var0);
      return VALID_EFFECTS.contains(var1) ? var1 : null;
   }

   public void readFromNBT(NBTTagCompound var1) {
      super.readFromNBT(var1);
      this.primaryEffect = isBeaconEffect(var1.getInteger("Primary"));
      this.secondaryEffect = isBeaconEffect(var1.getInteger("Secondary"));
      this.levels = var1.getInteger("Levels");
   }

   public NBTTagCompound writeToNBT(NBTTagCompound var1) {
      super.writeToNBT(var1);
      var1.setInteger("Primary", Potion.getIdFromPotion(this.primaryEffect));
      var1.setInteger("Secondary", Potion.getIdFromPotion(this.secondaryEffect));
      var1.setInteger("Levels", this.levels);
      return var1;
   }

   public int getSizeInventory() {
      return 1;
   }

   @Nullable
   public ItemStack getStackInSlot(int var1) {
      return var1 == 0 ? this.payment : null;
   }

   @Nullable
   public ItemStack decrStackSize(int var1, int var2) {
      if (var1 == 0 && this.payment != null) {
         if (var2 >= this.payment.stackSize) {
            ItemStack var3 = this.payment;
            this.payment = null;
            return var3;
         } else {
            this.payment.stackSize -= var2;
            return new ItemStack(this.payment.getItem(), var2, this.payment.getMetadata());
         }
      } else {
         return null;
      }
   }

   @Nullable
   public ItemStack removeStackFromSlot(int var1) {
      if (var1 == 0) {
         ItemStack var2 = this.payment;
         this.payment = null;
         return var2;
      } else {
         return null;
      }
   }

   public void setInventorySlotContents(int var1, @Nullable ItemStack var2) {
      if (var1 == 0) {
         this.payment = var2;
      }

   }

   public String getName() {
      return this.hasCustomName() ? this.customName : "container.beacon";
   }

   public boolean hasCustomName() {
      return this.customName != null && !this.customName.isEmpty();
   }

   public void setName(String var1) {
      this.customName = var1;
   }

   public int getInventoryStackLimit() {
      return 1;
   }

   public boolean isUsableByPlayer(EntityPlayer var1) {
      return this.world.getTileEntity(this.pos) != this ? false : var1.getDistanceSq((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 0.5D, (double)this.pos.getZ() + 0.5D) <= 64.0D;
   }

   public void openInventory(EntityPlayer var1) {
   }

   public void closeInventory(EntityPlayer var1) {
   }

   public boolean isItemValidForSlot(int var1, ItemStack var2) {
      return var2.getItem() == Items.EMERALD || var2.getItem() == Items.DIAMOND || var2.getItem() == Items.GOLD_INGOT || var2.getItem() == Items.IRON_INGOT;
   }

   public String getGuiID() {
      return "minecraft:beacon";
   }

   public Container createContainer(InventoryPlayer var1, EntityPlayer var2) {
      return new ContainerBeacon(var1, this);
   }

   public int getField(int var1) {
      switch(var1) {
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
      switch(var1) {
      case 0:
         this.levels = var2;
         break;
      case 1:
         this.primaryEffect = isBeaconEffect(var2);
         break;
      case 2:
         this.secondaryEffect = isBeaconEffect(var2);
      }

   }

   public int getFieldCount() {
      return 3;
   }

   public void clear() {
      this.payment = null;
   }

   public boolean receiveClientEvent(int var1, int var2) {
      if (var1 == 1) {
         this.updateBeacon();
         return true;
      } else {
         return super.receiveClientEvent(var1, var2);
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

   public static class BeamSegment {
      private final float[] colors;
      private int height;

      public BeamSegment(float[] var1) {
         this.colors = var1;
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
