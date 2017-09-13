package net.minecraft.client.entity;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ElytraSound;
import net.minecraft.client.audio.MovingSoundMinecartRiding;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraft.client.gui.GuiEnchantment;
import net.minecraft.client.gui.GuiHopper;
import net.minecraft.client.gui.GuiMerchant;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiScreenBook;
import net.minecraft.client.gui.inventory.GuiBeacon;
import net.minecraft.client.gui.inventory.GuiBrewingStand;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiCrafting;
import net.minecraft.client.gui.inventory.GuiDispenser;
import net.minecraft.client.gui.inventory.GuiEditCommandBlockMinecart;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.gui.inventory.GuiEditStructure;
import net.minecraft.client.gui.inventory.GuiFurnace;
import net.minecraft.client.gui.inventory.GuiScreenHorseInventory;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IJumpingMount;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemElytra;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketClientStatus;
import net.minecraft.network.play.client.CPacketCloseWindow;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerAbilities;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketVehicleMove;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntityStructure;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MovementInput;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.IInteractionObject;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntityPlayerSP extends AbstractClientPlayer {
   public final NetHandlerPlayClient connection;
   private final StatisticsManager statWriter;
   private int permissionLevel = 0;
   private double lastReportedPosX;
   private double lastReportedPosY;
   private double lastReportedPosZ;
   private float lastReportedYaw;
   private float lastReportedPitch;
   private boolean prevOnGround;
   private boolean serverSneakState;
   private boolean serverSprintState;
   private int positionUpdateTicks;
   private boolean hasValidHealth;
   private String serverBrand;
   public MovementInput movementInput;
   protected Minecraft mc;
   protected int sprintToggleTimer;
   public int sprintingTicksLeft;
   public float renderArmYaw;
   public float renderArmPitch;
   public float prevRenderArmYaw;
   public float prevRenderArmPitch;
   private int horseJumpPowerCounter;
   private float horseJumpPower;
   public float timeInPortal;
   public float prevTimeInPortal;
   private boolean handActive;
   private EnumHand activeHand;
   private boolean rowingBoat;
   private boolean autoJumpEnabled = true;
   private int autoJumpTime;
   private boolean wasFallFlying;

   public EntityPlayerSP(Minecraft var1, World var2, NetHandlerPlayClient var3, StatisticsManager var4) {
      super(var2, var3.getGameProfile());
      this.connection = var3;
      this.statWriter = var4;
      this.mc = var1;
      this.dimension = 0;
   }

   public boolean attackEntityFrom(DamageSource var1, float var2) {
      return false;
   }

   public void heal(float var1) {
   }

   public boolean startRiding(Entity var1, boolean var2) {
      if (!super.startRiding(var1, var2)) {
         return false;
      } else {
         if (var1 instanceof EntityMinecart) {
            this.mc.getSoundHandler().playSound(new MovingSoundMinecartRiding(this, (EntityMinecart)var1));
         }

         if (var1 instanceof EntityBoat) {
            this.prevRotationYaw = var1.rotationYaw;
            this.rotationYaw = var1.rotationYaw;
            this.setRotationYawHead(var1.rotationYaw);
         }

         return true;
      }
   }

   public void dismountRidingEntity() {
      super.dismountRidingEntity();
      this.rowingBoat = false;
   }

   public void onUpdate() {
      if (this.world.isBlockLoaded(new BlockPos(this.posX, 0.0D, this.posZ))) {
         super.onUpdate();
         if (this.isRiding()) {
            this.connection.sendPacket(new CPacketPlayer.Rotation(this.rotationYaw, this.rotationPitch, this.onGround));
            this.connection.sendPacket(new CPacketInput(this.moveStrafing, this.moveForward, this.movementInput.jump, this.movementInput.sneak));
            Entity var1 = this.getLowestRidingEntity();
            if (var1 != this && var1.canPassengerSteer()) {
               this.connection.sendPacket(new CPacketVehicleMove(var1));
            }
         } else {
            this.onUpdateWalkingPlayer();
         }
      }

   }

   public void onUpdateWalkingPlayer() {
      boolean var1 = this.isSprinting();
      if (var1 != this.serverSprintState) {
         if (var1) {
            this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SPRINTING));
         } else {
            this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SPRINTING));
         }

         this.serverSprintState = var1;
      }

      boolean var2 = this.isSneaking();
      if (var2 != this.serverSneakState) {
         if (var2) {
            this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_SNEAKING));
         } else {
            this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.STOP_SNEAKING));
         }

         this.serverSneakState = var2;
      }

      if (this.isCurrentViewEntity()) {
         AxisAlignedBB var3 = this.getEntityBoundingBox();
         double var4 = this.posX - this.lastReportedPosX;
         double var6 = var3.minY - this.lastReportedPosY;
         double var8 = this.posZ - this.lastReportedPosZ;
         double var10 = (double)(this.rotationYaw - this.lastReportedYaw);
         double var12 = (double)(this.rotationPitch - this.lastReportedPitch);
         ++this.positionUpdateTicks;
         boolean var14 = var4 * var4 + var6 * var6 + var8 * var8 > 9.0E-4D || this.positionUpdateTicks >= 20;
         boolean var15 = var10 != 0.0D || var12 != 0.0D;
         if (this.isRiding()) {
            this.connection.sendPacket(new CPacketPlayer.PositionRotation(this.motionX, -999.0D, this.motionZ, this.rotationYaw, this.rotationPitch, this.onGround));
            var14 = false;
         } else if (var14 && var15) {
            this.connection.sendPacket(new CPacketPlayer.PositionRotation(this.posX, var3.minY, this.posZ, this.rotationYaw, this.rotationPitch, this.onGround));
         } else if (var14) {
            this.connection.sendPacket(new CPacketPlayer.Position(this.posX, var3.minY, this.posZ, this.onGround));
         } else if (var15) {
            this.connection.sendPacket(new CPacketPlayer.Rotation(this.rotationYaw, this.rotationPitch, this.onGround));
         } else if (this.prevOnGround != this.onGround) {
            this.connection.sendPacket(new CPacketPlayer(this.onGround));
         }

         if (var14) {
            this.lastReportedPosX = this.posX;
            this.lastReportedPosY = var3.minY;
            this.lastReportedPosZ = this.posZ;
            this.positionUpdateTicks = 0;
         }

         if (var15) {
            this.lastReportedYaw = this.rotationYaw;
            this.lastReportedPitch = this.rotationPitch;
         }

         this.prevOnGround = this.onGround;
         this.autoJumpEnabled = this.mc.gameSettings.autoJump;
      }

   }

   @Nullable
   public EntityItem dropItem(boolean var1) {
      CPacketPlayerDigging.Action var2 = var1 ? CPacketPlayerDigging.Action.DROP_ALL_ITEMS : CPacketPlayerDigging.Action.DROP_ITEM;
      this.connection.sendPacket(new CPacketPlayerDigging(var2, BlockPos.ORIGIN, EnumFacing.DOWN));
      return null;
   }

   @Nullable
   public ItemStack dropItemAndGetStack(EntityItem var1) {
      return null;
   }

   public void sendChatMessage(String var1) {
      this.connection.sendPacket(new CPacketChatMessage(var1));
   }

   public void swingArm(EnumHand var1) {
      super.swingArm(var1);
      this.connection.sendPacket(new CPacketAnimation(var1));
   }

   public void respawnPlayer() {
      this.connection.sendPacket(new CPacketClientStatus(CPacketClientStatus.State.PERFORM_RESPAWN));
   }

   protected void damageEntity(DamageSource var1, float var2) {
      if (!this.isEntityInvulnerable(var1)) {
         this.setHealth(this.getHealth() - var2);
      }

   }

   public void closeScreen() {
      this.connection.sendPacket(new CPacketCloseWindow(this.openContainer.windowId));
      this.closeScreenAndDropStack();
   }

   public void closeScreenAndDropStack() {
      this.inventory.setItemStack((ItemStack)null);
      super.closeScreen();
      this.mc.displayGuiScreen((GuiScreen)null);
   }

   public void setPlayerSPHealth(float var1) {
      if (this.hasValidHealth) {
         float var2 = this.getHealth() - var1;
         if (var2 <= 0.0F) {
            this.setHealth(var1);
            if (var2 < 0.0F) {
               this.hurtResistantTime = this.maxHurtResistantTime / 2;
            }
         } else {
            this.lastDamage = var2;
            this.setHealth(this.getHealth());
            this.hurtResistantTime = this.maxHurtResistantTime;
            this.damageEntity(DamageSource.generic, var2);
            this.maxHurtTime = 10;
            this.hurtTime = this.maxHurtTime;
         }
      } else {
         this.setHealth(var1);
         this.hasValidHealth = true;
      }

   }

   public void addStat(StatBase var1, int var2) {
      if (var1 != null && var1.isIndependent) {
         super.addStat(var1, var2);
      }

   }

   public void sendPlayerAbilities() {
      this.connection.sendPacket(new CPacketPlayerAbilities(this.capabilities));
   }

   public boolean isUser() {
      return true;
   }

   protected void sendHorseJump() {
      this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_RIDING_JUMP, MathHelper.floor(this.getHorseJumpPower() * 100.0F)));
   }

   public void sendHorseInventory() {
      this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.OPEN_INVENTORY));
   }

   public void setServerBrand(String var1) {
      this.serverBrand = var1;
   }

   public String getServerBrand() {
      return this.serverBrand;
   }

   public StatisticsManager getStatFileWriter() {
      return this.statWriter;
   }

   public int getPermissionLevel() {
      return this.permissionLevel;
   }

   public void setPermissionLevel(int var1) {
      this.permissionLevel = var1;
   }

   public void sendStatusMessage(ITextComponent var1) {
      this.mc.ingameGUI.getChatGUI().printChatMessage(var1);
   }

   private boolean isHeadspaceFree(BlockPos var1, int var2) {
      for(int var3 = 0; var3 < var2; ++var3) {
         if (!this.isOpenBlockSpace(var1.add(0, var3, 0))) {
            return false;
         }
      }

      return true;
   }

   protected boolean pushOutOfBlocks(double var1, double var3, double var5) {
      if (this.noClip) {
         return false;
      } else {
         BlockPos var7 = new BlockPos(var1, var3, var5);
         double var8 = var1 - (double)var7.getX();
         double var10 = var5 - (double)var7.getZ();
         int var12 = Math.max((int)Math.ceil((double)this.height), 1);
         boolean var13 = !this.isHeadspaceFree(var7, var12);
         if (var13) {
            byte var14 = -1;
            double var15 = 9999.0D;
            if (this.isHeadspaceFree(var7.west(), var12) && var8 < var15) {
               var15 = var8;
               var14 = 0;
            }

            if (this.isHeadspaceFree(var7.east(), var12) && 1.0D - var8 < var15) {
               var15 = 1.0D - var8;
               var14 = 1;
            }

            if (this.isHeadspaceFree(var7.north(), var12) && var10 < var15) {
               var15 = var10;
               var14 = 4;
            }

            if (this.isHeadspaceFree(var7.south(), var12) && 1.0D - var10 < var15) {
               var15 = 1.0D - var10;
               var14 = 5;
            }

            float var17 = 0.1F;
            if (var14 == 0) {
               this.motionX = -0.10000000149011612D;
            }

            if (var14 == 1) {
               this.motionX = 0.10000000149011612D;
            }

            if (var14 == 4) {
               this.motionZ = -0.10000000149011612D;
            }

            if (var14 == 5) {
               this.motionZ = 0.10000000149011612D;
            }
         }

         return false;
      }
   }

   private boolean isOpenBlockSpace(BlockPos var1) {
      return !this.world.getBlockState(var1).isNormalCube();
   }

   public void setSprinting(boolean var1) {
      super.setSprinting(var1);
      this.sprintingTicksLeft = 0;
   }

   public void setXPStats(float var1, int var2, int var3) {
      this.experience = var1;
      this.experienceTotal = var2;
      this.experienceLevel = var3;
   }

   public void sendMessage(ITextComponent var1) {
      this.mc.ingameGUI.getChatGUI().printChatMessage(var1);
   }

   public boolean canUseCommand(int var1, String var2) {
      return var1 <= this.getPermissionLevel();
   }

   public void handleStatusUpdate(byte var1) {
      if (var1 >= 24 && var1 <= 28) {
         this.setPermissionLevel(var1 - 24);
      } else {
         super.handleStatusUpdate(var1);
      }

   }

   public BlockPos getPosition() {
      return new BlockPos(this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D);
   }

   public void playSound(SoundEvent var1, float var2, float var3) {
      PlaySoundAtEntityEvent var4 = ForgeEventFactory.onPlaySoundAtEntity(this, var1, this.getSoundCategory(), var2, var3);
      if (!var4.isCanceled() && var4.getSound() != null) {
         var1 = var4.getSound();
         var2 = var4.getVolume();
         var3 = var4.getPitch();
         this.world.playSound(this.posX, this.posY, this.posZ, var1, var4.getCategory(), var2, var3, false);
      }
   }

   public boolean isServerWorld() {
      return true;
   }

   public void setActiveHand(EnumHand var1) {
      ItemStack var2 = this.getHeldItem(var1);
      if (var2 != null && !this.isHandActive()) {
         super.setActiveHand(var1);
         this.handActive = true;
         this.activeHand = var1;
      }

   }

   public boolean isHandActive() {
      return this.handActive;
   }

   public void resetActiveHand() {
      super.resetActiveHand();
      this.handActive = false;
   }

   public EnumHand getActiveHand() {
      return this.activeHand;
   }

   public void notifyDataManagerChange(DataParameter var1) {
      super.notifyDataManagerChange(var1);
      if (HAND_STATES.equals(var1)) {
         boolean var2 = (((Byte)this.dataManager.get(HAND_STATES)).byteValue() & 1) > 0;
         EnumHand var3 = (((Byte)this.dataManager.get(HAND_STATES)).byteValue() & 2) > 0 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
         if (var2 && !this.handActive) {
            this.setActiveHand(var3);
         } else if (!var2 && this.handActive) {
            this.resetActiveHand();
         }
      }

      if (FLAGS.equals(var1) && this.isElytraFlying() && !this.wasFallFlying) {
         this.mc.getSoundHandler().playSound(new ElytraSound(this));
      }

   }

   public boolean isRidingHorse() {
      Entity var1 = this.getRidingEntity();
      return this.isRiding() && var1 instanceof IJumpingMount && ((IJumpingMount)var1).canJump();
   }

   public float getHorseJumpPower() {
      return this.horseJumpPower;
   }

   public void openEditSign(TileEntitySign var1) {
      this.mc.displayGuiScreen(new GuiEditSign(var1));
   }

   public void displayGuiEditCommandCart(CommandBlockBaseLogic var1) {
      this.mc.displayGuiScreen(new GuiEditCommandBlockMinecart(var1));
   }

   public void displayGuiCommandBlock(TileEntityCommandBlock var1) {
      this.mc.displayGuiScreen(new GuiCommandBlock(var1));
   }

   public void openEditStructure(TileEntityStructure var1) {
      this.mc.displayGuiScreen(new GuiEditStructure(var1));
   }

   public void openBook(ItemStack var1, EnumHand var2) {
      Item var3 = var1.getItem();
      if (var3 == Items.WRITABLE_BOOK) {
         this.mc.displayGuiScreen(new GuiScreenBook(this, var1, true));
      }

   }

   public void displayGUIChest(IInventory var1) {
      String var2 = var1 instanceof IInteractionObject ? ((IInteractionObject)var1).getGuiID() : "minecraft:container";
      if ("minecraft:chest".equals(var2)) {
         this.mc.displayGuiScreen(new GuiChest(this.inventory, var1));
      } else if ("minecraft:hopper".equals(var2)) {
         this.mc.displayGuiScreen(new GuiHopper(this.inventory, var1));
      } else if ("minecraft:furnace".equals(var2)) {
         this.mc.displayGuiScreen(new GuiFurnace(this.inventory, var1));
      } else if ("minecraft:brewing_stand".equals(var2)) {
         this.mc.displayGuiScreen(new GuiBrewingStand(this.inventory, var1));
      } else if ("minecraft:beacon".equals(var2)) {
         this.mc.displayGuiScreen(new GuiBeacon(this.inventory, var1));
      } else if (!"minecraft:dispenser".equals(var2) && !"minecraft:dropper".equals(var2)) {
         this.mc.displayGuiScreen(new GuiChest(this.inventory, var1));
      } else {
         this.mc.displayGuiScreen(new GuiDispenser(this.inventory, var1));
      }

   }

   public void openGuiHorseInventory(EntityHorse var1, IInventory var2) {
      this.mc.displayGuiScreen(new GuiScreenHorseInventory(this.inventory, var2, var1));
   }

   public void displayGui(IInteractionObject var1) {
      String var2 = var1.getGuiID();
      if ("minecraft:crafting_table".equals(var2)) {
         this.mc.displayGuiScreen(new GuiCrafting(this.inventory, this.world));
      } else if ("minecraft:enchanting_table".equals(var2)) {
         this.mc.displayGuiScreen(new GuiEnchantment(this.inventory, this.world, var1));
      } else if ("minecraft:anvil".equals(var2)) {
         this.mc.displayGuiScreen(new GuiRepair(this.inventory, this.world));
      }

   }

   public void displayVillagerTradeGui(IMerchant var1) {
      this.mc.displayGuiScreen(new GuiMerchant(this.inventory, var1, this.world));
   }

   public void onCriticalHit(Entity var1) {
      this.mc.effectRenderer.emitParticleAtEntity(var1, EnumParticleTypes.CRIT);
   }

   public void onEnchantmentCritical(Entity var1) {
      this.mc.effectRenderer.emitParticleAtEntity(var1, EnumParticleTypes.CRIT_MAGIC);
   }

   public boolean isSneaking() {
      boolean var1 = this.movementInput != null ? this.movementInput.sneak : false;
      return var1 && !this.sleeping;
   }

   public void updateEntityActionState() {
      super.updateEntityActionState();
      if (this.isCurrentViewEntity()) {
         this.moveStrafing = this.movementInput.moveStrafe;
         this.moveForward = this.movementInput.moveForward;
         this.isJumping = this.movementInput.jump;
         this.prevRenderArmYaw = this.renderArmYaw;
         this.prevRenderArmPitch = this.renderArmPitch;
         this.renderArmPitch = (float)((double)this.renderArmPitch + (double)(this.rotationPitch - this.renderArmPitch) * 0.5D);
         this.renderArmYaw = (float)((double)this.renderArmYaw + (double)(this.rotationYaw - this.renderArmYaw) * 0.5D);
      }

   }

   protected boolean isCurrentViewEntity() {
      return this.mc.getRenderViewEntity() == this;
   }

   public void onLivingUpdate() {
      ++this.sprintingTicksLeft;
      if (this.sprintToggleTimer > 0) {
         --this.sprintToggleTimer;
      }

      this.prevTimeInPortal = this.timeInPortal;
      if (this.inPortal) {
         if (this.mc.currentScreen != null && !this.mc.currentScreen.doesGuiPauseGame()) {
            this.mc.displayGuiScreen((GuiScreen)null);
         }

         if (this.timeInPortal == 0.0F) {
            this.mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.BLOCK_PORTAL_TRIGGER, this.rand.nextFloat() * 0.4F + 0.8F));
         }

         this.timeInPortal += 0.0125F;
         if (this.timeInPortal >= 1.0F) {
            this.timeInPortal = 1.0F;
         }

         this.inPortal = false;
      } else if (this.isPotionActive(MobEffects.NAUSEA) && this.getActivePotionEffect(MobEffects.NAUSEA).getDuration() > 60) {
         this.timeInPortal += 0.006666667F;
         if (this.timeInPortal > 1.0F) {
            this.timeInPortal = 1.0F;
         }
      } else {
         if (this.timeInPortal > 0.0F) {
            this.timeInPortal -= 0.05F;
         }

         if (this.timeInPortal < 0.0F) {
            this.timeInPortal = 0.0F;
         }
      }

      if (this.timeUntilPortal > 0) {
         --this.timeUntilPortal;
      }

      boolean var1 = this.movementInput.jump;
      boolean var2 = this.movementInput.sneak;
      float var3 = 0.8F;
      boolean var4 = this.movementInput.moveForward >= 0.8F;
      this.movementInput.updatePlayerMoveState();
      if (this.isHandActive() && !this.isRiding()) {
         this.movementInput.moveStrafe *= 0.2F;
         this.movementInput.moveForward *= 0.2F;
         this.sprintToggleTimer = 0;
      }

      boolean var5 = false;
      if (this.autoJumpTime > 0) {
         --this.autoJumpTime;
         var5 = true;
         this.movementInput.jump = true;
      }

      AxisAlignedBB var6 = this.getEntityBoundingBox();
      this.pushOutOfBlocks(this.posX - (double)this.width * 0.35D, var6.minY + 0.5D, this.posZ + (double)this.width * 0.35D);
      this.pushOutOfBlocks(this.posX - (double)this.width * 0.35D, var6.minY + 0.5D, this.posZ - (double)this.width * 0.35D);
      this.pushOutOfBlocks(this.posX + (double)this.width * 0.35D, var6.minY + 0.5D, this.posZ - (double)this.width * 0.35D);
      this.pushOutOfBlocks(this.posX + (double)this.width * 0.35D, var6.minY + 0.5D, this.posZ + (double)this.width * 0.35D);
      boolean var7 = (float)this.getFoodStats().getFoodLevel() > 6.0F || this.capabilities.allowFlying;
      if (this.onGround && !var2 && !var4 && this.movementInput.moveForward >= 0.8F && !this.isSprinting() && var7 && !this.isHandActive() && !this.isPotionActive(MobEffects.BLINDNESS)) {
         if (this.sprintToggleTimer <= 0 && !this.mc.gameSettings.keyBindSprint.isKeyDown()) {
            this.sprintToggleTimer = 7;
         } else {
            this.setSprinting(true);
         }
      }

      if (!this.isSprinting() && this.movementInput.moveForward >= 0.8F && var7 && !this.isHandActive() && !this.isPotionActive(MobEffects.BLINDNESS) && this.mc.gameSettings.keyBindSprint.isKeyDown()) {
         this.setSprinting(true);
      }

      if (this.isSprinting() && (this.movementInput.moveForward < 0.8F || this.isCollidedHorizontally || !var7)) {
         this.setSprinting(false);
      }

      if (this.capabilities.allowFlying) {
         if (this.mc.playerController.isSpectatorMode()) {
            if (!this.capabilities.isFlying) {
               this.capabilities.isFlying = true;
               this.sendPlayerAbilities();
            }
         } else if (!var1 && this.movementInput.jump && !var5) {
            if (this.flyToggleTimer == 0) {
               this.flyToggleTimer = 7;
            } else {
               this.capabilities.isFlying = !this.capabilities.isFlying;
               this.sendPlayerAbilities();
               this.flyToggleTimer = 0;
            }
         }
      }

      if (this.movementInput.jump && !var1 && !this.onGround && this.motionY < 0.0D && !this.isElytraFlying() && !this.capabilities.isFlying) {
         ItemStack var8 = this.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
         if (var8 != null && var8.getItem() == Items.ELYTRA && ItemElytra.isBroken(var8)) {
            this.connection.sendPacket(new CPacketEntityAction(this, CPacketEntityAction.Action.START_FALL_FLYING));
         }
      }

      this.wasFallFlying = this.isElytraFlying();
      if (this.capabilities.isFlying && this.isCurrentViewEntity()) {
         if (this.movementInput.sneak) {
            this.movementInput.moveStrafe = (float)((double)this.movementInput.moveStrafe / 0.3D);
            this.movementInput.moveForward = (float)((double)this.movementInput.moveForward / 0.3D);
            this.motionY -= (double)(this.capabilities.getFlySpeed() * 3.0F);
         }

         if (this.movementInput.jump) {
            this.motionY += (double)(this.capabilities.getFlySpeed() * 3.0F);
         }
      }

      if (this.isRidingHorse()) {
         IJumpingMount var9 = (IJumpingMount)this.getRidingEntity();
         if (this.horseJumpPowerCounter < 0) {
            ++this.horseJumpPowerCounter;
            if (this.horseJumpPowerCounter == 0) {
               this.horseJumpPower = 0.0F;
            }
         }

         if (var1 && !this.movementInput.jump) {
            this.horseJumpPowerCounter = -10;
            var9.setJumpPower(MathHelper.floor(this.getHorseJumpPower() * 100.0F));
            this.sendHorseJump();
         } else if (!var1 && this.movementInput.jump) {
            this.horseJumpPowerCounter = 0;
            this.horseJumpPower = 0.0F;
         } else if (var1) {
            ++this.horseJumpPowerCounter;
            if (this.horseJumpPowerCounter < 10) {
               this.horseJumpPower = (float)this.horseJumpPowerCounter * 0.1F;
            } else {
               this.horseJumpPower = 0.8F + 2.0F / (float)(this.horseJumpPowerCounter - 9) * 0.1F;
            }
         }
      } else {
         this.horseJumpPower = 0.0F;
      }

      super.onLivingUpdate();
      if (this.onGround && this.capabilities.isFlying && !this.mc.playerController.isSpectatorMode()) {
         this.capabilities.isFlying = false;
         this.sendPlayerAbilities();
      }

   }

   public void updateRidden() {
      super.updateRidden();
      this.rowingBoat = false;
      if (this.getRidingEntity() instanceof EntityBoat) {
         EntityBoat var1 = (EntityBoat)this.getRidingEntity();
         var1.updateInputs(this.movementInput.leftKeyDown, this.movementInput.rightKeyDown, this.movementInput.forwardKeyDown, this.movementInput.backKeyDown);
         this.rowingBoat |= this.movementInput.leftKeyDown || this.movementInput.rightKeyDown || this.movementInput.forwardKeyDown || this.movementInput.backKeyDown;
      }

   }

   public boolean isRowingBoat() {
      return this.rowingBoat;
   }

   @Nullable
   public PotionEffect removeActivePotionEffect(@Nullable Potion var1) {
      if (var1 == MobEffects.NAUSEA) {
         this.prevTimeInPortal = 0.0F;
         this.timeInPortal = 0.0F;
      }

      return super.removeActivePotionEffect(var1);
   }

   public void move(double var1, double var3, double var5) {
      double var7 = this.posX;
      double var9 = this.posZ;
      super.move(var1, var3, var5);
      this.updateAutoJump((float)(this.posX - var7), (float)(this.posZ - var9));
   }

   public boolean isAutoJumpEnabled() {
      return this.autoJumpEnabled;
   }

   protected void updateAutoJump(float var1, float var2) {
      if (this.isAutoJumpEnabled() && this.autoJumpTime <= 0 && this.onGround && !this.isSneaking() && !this.isRiding()) {
         Vec2f var3 = this.movementInput.getMoveVector();
         if (var3.x != 0.0F || var3.y != 0.0F) {
            Vec3d var4 = new Vec3d(this.posX, this.getEntityBoundingBox().minY, this.posZ);
            double var5 = this.posX + (double)var1;
            double var7 = this.posZ + (double)var2;
            Vec3d var9 = new Vec3d(var5, this.getEntityBoundingBox().minY, var7);
            Vec3d var10 = new Vec3d((double)var1, 0.0D, (double)var2);
            float var11 = this.getAIMoveSpeed();
            float var12 = (float)var10.lengthSquared();
            if (var12 <= 0.001F) {
               float var13 = var11 * var3.x;
               float var14 = var11 * var3.y;
               float var15 = MathHelper.sin(this.rotationYaw * 0.017453292F);
               float var16 = MathHelper.cos(this.rotationYaw * 0.017453292F);
               var10 = new Vec3d((double)(var13 * var16 - var14 * var15), var10.yCoord, (double)(var14 * var16 + var13 * var15));
               var12 = (float)var10.lengthSquared();
               if (var12 <= 0.001F) {
                  return;
               }
            }

            float var45 = (float)MathHelper.fastInvSqrt((double)var12);
            Vec3d var46 = var10.scale((double)var45);
            Vec3d var47 = this.getForward();
            float var48 = (float)(var47.xCoord * var46.xCoord + var47.zCoord * var46.zCoord);
            if (var48 >= -0.15F) {
               BlockPos var17 = new BlockPos(this.posX, this.getEntityBoundingBox().maxY, this.posZ);
               IBlockState var18 = this.world.getBlockState(var17);
               if (var18.getCollisionBoundingBox(this.world, var17) == null) {
                  var17 = var17.up();
                  IBlockState var19 = this.world.getBlockState(var17);
                  if (var19.getCollisionBoundingBox(this.world, var17) == null) {
                     float var20 = 7.0F;
                     float var21 = 1.2F;
                     if (this.isPotionActive(MobEffects.JUMP_BOOST)) {
                        var21 += (float)(this.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.75F;
                     }

                     float var22 = Math.max(var11 * 7.0F, 1.0F / var45);
                     Vec3d var23 = var9.add(var46.scale((double)var22));
                     float var24 = this.width;
                     float var25 = this.height;
                     AxisAlignedBB var26 = (new AxisAlignedBB(var4, var23.addVector(0.0D, (double)var25, 0.0D))).expand((double)var24, 0.0D, (double)var24);
                     Vec3d var27 = var4.addVector(0.0D, 0.5099999904632568D, 0.0D);
                     var23 = var23.addVector(0.0D, 0.5099999904632568D, 0.0D);
                     Vec3d var28 = var46.crossProduct(new Vec3d(0.0D, 1.0D, 0.0D));
                     Vec3d var29 = var28.scale((double)(var24 * 0.5F));
                     Vec3d var30 = var27.subtract(var29);
                     Vec3d var31 = var23.subtract(var29);
                     Vec3d var32 = var27.add(var29);
                     Vec3d var33 = var23.add(var29);
                     List var34 = this.world.getCollisionBoxes(this, var26);
                     if (!var34.isEmpty()) {
                        ;
                     }

                     float var35 = Float.MIN_VALUE;

                     for(AxisAlignedBB var37 : var34) {
                        if (var37.intersects(var30, var31) || var37.intersects(var32, var33)) {
                           var35 = (float)var37.maxY;
                           Vec3d var38 = var37.getCenter();
                           BlockPos var39 = new BlockPos(var38);

                           for(int var40 = 1; (float)var40 < var21; ++var40) {
                              BlockPos var41 = var39.up(var40);
                              IBlockState var42 = this.world.getBlockState(var41);
                              AxisAlignedBB var43;
                              if ((var43 = var42.getCollisionBoundingBox(this.world, var41)) != null) {
                                 var35 = (float)var43.maxY + (float)var41.getY();
                                 if ((double)var35 - this.getEntityBoundingBox().minY > (double)var21) {
                                    return;
                                 }
                              }

                              if (var40 > 1) {
                                 var17 = var17.up();
                                 IBlockState var44 = this.world.getBlockState(var17);
                                 if (var44.getCollisionBoundingBox(this.world, var17) != null) {
                                    return;
                                 }
                              }
                           }
                           break;
                        }
                     }

                     if (var35 != Float.MIN_VALUE) {
                        float var51 = (float)((double)var35 - this.getEntityBoundingBox().minY);
                        if (var51 > 0.5F && var51 <= var21) {
                           this.autoJumpTime = 1;
                        }
                     }
                  }
               }
            }
         }
      }

   }
}
