package net.minecraft.init;

import com.mojang.authlib.GameProfile;
import java.io.PrintStream;
import java.util.Random;
import java.util.UUID;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.Material;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionHelper;
import net.minecraft.potion.PotionType;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.LoggingPrintStream;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fluids.DispenseFluidContainer;
import net.minecraftforge.fml.common.registry.GameData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Bootstrap {
   public static final PrintStream SYSOUT = System.out;
   private static boolean alreadyRegistered;
   private static final Logger LOGGER = LogManager.getLogger();

   public static boolean isRegistered() {
      return alreadyRegistered;
   }

   static void registerDispenserBehaviors() {
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.ARROW, new BehaviorProjectileDispense() {
         protected IProjectile getProjectileEntity(World var1, IPosition var2, ItemStack var3) {
            EntityTippedArrow var4 = new EntityTippedArrow(var1, var2.getX(), var2.getY(), var2.getZ());
            var4.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
            return var4;
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.TIPPED_ARROW, new BehaviorProjectileDispense() {
         protected IProjectile getProjectileEntity(World var1, IPosition var2, ItemStack var3) {
            EntityTippedArrow var4 = new EntityTippedArrow(var1, var2.getX(), var2.getY(), var2.getZ());
            var4.setPotionEffect(var3);
            var4.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
            return var4;
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.SPECTRAL_ARROW, new BehaviorProjectileDispense() {
         protected IProjectile getProjectileEntity(World var1, IPosition var2, ItemStack var3) {
            EntitySpectralArrow var4 = new EntitySpectralArrow(var1, var2.getX(), var2.getY(), var2.getZ());
            var4.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
            return var4;
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.EGG, new BehaviorProjectileDispense() {
         protected IProjectile getProjectileEntity(World var1, IPosition var2, ItemStack var3) {
            return new EntityEgg(var1, var2.getX(), var2.getY(), var2.getZ());
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.SNOWBALL, new BehaviorProjectileDispense() {
         protected IProjectile getProjectileEntity(World var1, IPosition var2, ItemStack var3) {
            return new EntitySnowball(var1, var2.getX(), var2.getY(), var2.getZ());
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.EXPERIENCE_BOTTLE, new BehaviorProjectileDispense() {
         protected IProjectile getProjectileEntity(World var1, IPosition var2, ItemStack var3) {
            return new EntityExpBottle(var1, var2.getX(), var2.getY(), var2.getZ());
         }

         protected float getProjectileInaccuracy() {
            return super.getProjectileInaccuracy() * 0.5F;
         }

         protected float getProjectileVelocity() {
            return super.getProjectileVelocity() * 1.25F;
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.SPLASH_POTION, new IBehaviorDispenseItem() {
         public ItemStack dispense(IBlockSource var1, final ItemStack var2) {
            return (new BehaviorProjectileDispense() {
               protected IProjectile getProjectileEntity(World var1, IPosition var2x, ItemStack var3) {
                  return new EntityPotion(var1, var2x.getX(), var2x.getY(), var2x.getZ(), var2.copy());
               }

               protected float getProjectileInaccuracy() {
                  return super.getProjectileInaccuracy() * 0.5F;
               }

               protected float getProjectileVelocity() {
                  return super.getProjectileVelocity() * 1.25F;
               }
            }).dispense(var1, var2);
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.LINGERING_POTION, new IBehaviorDispenseItem() {
         public ItemStack dispense(IBlockSource var1, final ItemStack var2) {
            return (new BehaviorProjectileDispense() {
               protected IProjectile getProjectileEntity(World var1, IPosition var2x, ItemStack var3) {
                  return new EntityPotion(var1, var2x.getX(), var2x.getY(), var2x.getZ(), var2.copy());
               }

               protected float getProjectileInaccuracy() {
                  return super.getProjectileInaccuracy() * 0.5F;
               }

               protected float getProjectileVelocity() {
                  return super.getProjectileVelocity() * 1.25F;
               }
            }).dispense(var1, var2);
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.SPAWN_EGG, new BehaviorDefaultDispenseItem() {
         public ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
            EnumFacing var3 = (EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING);
            double var4 = var1.getX() + (double)var3.getFrontOffsetX();
            double var6 = (double)((float)var1.getBlockPos().getY() + 0.2F);
            double var8 = var1.getZ() + (double)var3.getFrontOffsetZ();
            Entity var10 = ItemMonsterPlacer.spawnCreature(var1.getWorld(), ItemMonsterPlacer.getEntityIdFromItem(var2), var4, var6, var8);
            if (var10 instanceof EntityLivingBase && var2.hasDisplayName()) {
               var10.setCustomNameTag(var2.getDisplayName());
            }

            ItemMonsterPlacer.applyItemEntityDataToEntity(var1.getWorld(), (EntityPlayer)null, var2, var10);
            var2.splitStack(1);
            return var2;
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.FIREWORKS, new BehaviorDefaultDispenseItem() {
         public ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
            EnumFacing var3 = (EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING);
            double var4 = var1.getX() + (double)var3.getFrontOffsetX();
            double var6 = (double)((float)var1.getBlockPos().getY() + 0.2F);
            double var8 = var1.getZ() + (double)var3.getFrontOffsetZ();
            EntityFireworkRocket var10 = new EntityFireworkRocket(var1.getWorld(), var4, var6, var8, var2);
            var1.getWorld().spawnEntity(var10);
            var2.splitStack(1);
            return var2;
         }

         protected void playDispenseSound(IBlockSource var1) {
            var1.getWorld().playEvent(1004, var1.getBlockPos(), 0);
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.FIRE_CHARGE, new BehaviorDefaultDispenseItem() {
         public ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
            EnumFacing var3 = (EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING);
            IPosition var4 = BlockDispenser.getDispensePosition(var1);
            double var5 = var4.getX() + (double)((float)var3.getFrontOffsetX() * 0.3F);
            double var7 = var4.getY() + (double)((float)var3.getFrontOffsetY() * 0.3F);
            double var9 = var4.getZ() + (double)((float)var3.getFrontOffsetZ() * 0.3F);
            World var11 = var1.getWorld();
            Random var12 = var11.rand;
            double var13 = var12.nextGaussian() * 0.05D + (double)var3.getFrontOffsetX();
            double var15 = var12.nextGaussian() * 0.05D + (double)var3.getFrontOffsetY();
            double var17 = var12.nextGaussian() * 0.05D + (double)var3.getFrontOffsetZ();
            var11.spawnEntity(new EntitySmallFireball(var11, var5, var7, var9, var13, var15, var17));
            var2.splitStack(1);
            return var2;
         }

         protected void playDispenseSound(IBlockSource var1) {
            var1.getWorld().playEvent(1018, var1.getBlockPos(), 0);
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.BOAT, new Bootstrap.BehaviorDispenseBoat(EntityBoat.Type.OAK));
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.SPRUCE_BOAT, new Bootstrap.BehaviorDispenseBoat(EntityBoat.Type.SPRUCE));
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.BIRCH_BOAT, new Bootstrap.BehaviorDispenseBoat(EntityBoat.Type.BIRCH));
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.JUNGLE_BOAT, new Bootstrap.BehaviorDispenseBoat(EntityBoat.Type.JUNGLE));
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.DARK_OAK_BOAT, new Bootstrap.BehaviorDispenseBoat(EntityBoat.Type.DARK_OAK));
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.ACACIA_BOAT, new Bootstrap.BehaviorDispenseBoat(EntityBoat.Type.ACACIA));
      BehaviorDefaultDispenseItem var0 = new BehaviorDefaultDispenseItem() {
         private final BehaviorDefaultDispenseItem dispenseBehavior = new BehaviorDefaultDispenseItem();

         public ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
            ItemBucket var3 = (ItemBucket)var2.getItem();
            BlockPos var4 = var1.getBlockPos().offset((EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING));
            if (var3.tryPlaceContainedLiquid((EntityPlayer)null, var1.getWorld(), var4)) {
               var2.setItem(Items.BUCKET);
               var2.stackSize = 1;
               return var2;
            } else {
               return this.dispenseBehavior.dispense(var1, var2);
            }
         }
      };
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.LAVA_BUCKET, var0);
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.WATER_BUCKET, var0);
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.MILK_BUCKET, DispenseFluidContainer.getInstance());
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.BUCKET, DispenseFluidContainer.getInstance());
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.FLINT_AND_STEEL, new BehaviorDefaultDispenseItem() {
         private boolean succeeded = true;

         protected ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
            World var3 = var1.getWorld();
            BlockPos var4 = var1.getBlockPos().offset((EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING));
            if (var3.isAirBlock(var4)) {
               var3.setBlockState(var4, Blocks.FIRE.getDefaultState());
               if (var2.attemptDamageItem(1, var3.rand)) {
                  var2.stackSize = 0;
               }
            } else if (var3.getBlockState(var4).getBlock() == Blocks.TNT) {
               Blocks.TNT.onBlockDestroyedByPlayer(var3, var4, Blocks.TNT.getDefaultState().withProperty(BlockTNT.EXPLODE, Boolean.valueOf(true)));
               var3.setBlockToAir(var4);
            } else {
               this.succeeded = false;
            }

            return var2;
         }

         protected void playDispenseSound(IBlockSource var1) {
            if (this.succeeded) {
               var1.getWorld().playEvent(1000, var1.getBlockPos(), 0);
            } else {
               var1.getWorld().playEvent(1001, var1.getBlockPos(), 0);
            }

         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.DYE, new BehaviorDefaultDispenseItem() {
         private boolean succeeded = true;

         protected ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
            if (EnumDyeColor.WHITE == EnumDyeColor.byDyeDamage(var2.getMetadata())) {
               World var3 = var1.getWorld();
               BlockPos var4 = var1.getBlockPos().offset((EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING));
               if (ItemDye.applyBonemeal(var2, var3, var4)) {
                  if (!var3.isRemote) {
                     var3.playEvent(2005, var4, 0);
                  }
               } else {
                  this.succeeded = false;
               }

               return var2;
            } else {
               return super.dispenseStack(var1, var2);
            }
         }

         protected void playDispenseSound(IBlockSource var1) {
            if (this.succeeded) {
               var1.getWorld().playEvent(1000, var1.getBlockPos(), 0);
            } else {
               var1.getWorld().playEvent(1001, var1.getBlockPos(), 0);
            }

         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Item.getItemFromBlock(Blocks.TNT), new BehaviorDefaultDispenseItem() {
         protected ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
            World var3 = var1.getWorld();
            BlockPos var4 = var1.getBlockPos().offset((EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING));
            EntityTNTPrimed var5 = new EntityTNTPrimed(var3, (double)var4.getX() + 0.5D, (double)var4.getY(), (double)var4.getZ() + 0.5D, (EntityLivingBase)null);
            var3.spawnEntity(var5);
            var3.playSound((EntityPlayer)null, var5.posX, var5.posY, var5.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
            --var2.stackSize;
            return var2;
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.SKULL, new BehaviorDefaultDispenseItem() {
         private boolean succeeded = true;

         protected ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
            World var3 = var1.getWorld();
            EnumFacing var4 = (EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING);
            BlockPos var5 = var1.getBlockPos().offset(var4);
            BlockSkull var6 = Blocks.SKULL;
            if (var3.isAirBlock(var5) && var6.canDispenserPlace(var3, var5, var2)) {
               if (!var3.isRemote) {
                  var3.setBlockState(var5, var6.getDefaultState().withProperty(BlockSkull.FACING, EnumFacing.UP), 3);
                  TileEntity var7 = var3.getTileEntity(var5);
                  if (var7 instanceof TileEntitySkull) {
                     if (var2.getMetadata() == 3) {
                        GameProfile var8 = null;
                        if (var2.hasTagCompound()) {
                           NBTTagCompound var9 = var2.getTagCompound();
                           if (var9.hasKey("SkullOwner", 10)) {
                              var8 = NBTUtil.readGameProfileFromNBT(var9.getCompoundTag("SkullOwner"));
                           } else if (var9.hasKey("SkullOwner", 8)) {
                              String var10 = var9.getString("SkullOwner");
                              if (!StringUtils.isNullOrEmpty(var10)) {
                                 var8 = new GameProfile((UUID)null, var10);
                              }
                           }
                        }

                        ((TileEntitySkull)var7).setPlayerProfile(var8);
                     } else {
                        ((TileEntitySkull)var7).setType(var2.getMetadata());
                     }

                     ((TileEntitySkull)var7).setSkullRotation(var4.getOpposite().getHorizontalIndex() * 4);
                     Blocks.SKULL.checkWitherSpawn(var3, var5, (TileEntitySkull)var7);
                  }

                  --var2.stackSize;
               }
            } else if (ItemArmor.dispenseArmor(var1, var2) == null) {
               this.succeeded = false;
            }

            return var2;
         }

         protected void playDispenseSound(IBlockSource var1) {
            if (this.succeeded) {
               var1.getWorld().playEvent(1000, var1.getBlockPos(), 0);
            } else {
               var1.getWorld().playEvent(1001, var1.getBlockPos(), 0);
            }

         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Item.getItemFromBlock(Blocks.PUMPKIN), new BehaviorDefaultDispenseItem() {
         private boolean succeeded = true;

         protected ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
            World var3 = var1.getWorld();
            BlockPos var4 = var1.getBlockPos().offset((EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING));
            BlockPumpkin var5 = (BlockPumpkin)Blocks.PUMPKIN;
            if (var3.isAirBlock(var4) && var5.canDispenserPlace(var3, var4)) {
               if (!var3.isRemote) {
                  var3.setBlockState(var4, var5.getDefaultState(), 3);
               }

               --var2.stackSize;
            } else {
               ItemStack var6 = ItemArmor.dispenseArmor(var1, var2);
               if (var6 == null) {
                  this.succeeded = false;
               }
            }

            return var2;
         }

         protected void playDispenseSound(IBlockSource var1) {
            if (this.succeeded) {
               var1.getWorld().playEvent(1000, var1.getBlockPos(), 0);
            } else {
               var1.getWorld().playEvent(1001, var1.getBlockPos(), 0);
            }

         }
      });
   }

   public static void register() {
      if (!alreadyRegistered) {
         alreadyRegistered = true;
         if (LOGGER.isDebugEnabled()) {
            redirectOutputToLog();
         }

         SoundEvent.registerSounds();
         Block.registerBlocks();
         BlockFire.init();
         Potion.registerPotions();
         Enchantment.registerEnchantments();
         Item.registerItems();
         PotionType.registerPotionTypes();
         PotionHelper.init();
         StatList.init();
         Biome.registerBiomes();
         registerDispenserBehaviors();
         GameData.vanillaSnapshot();
      }

   }

   private static void redirectOutputToLog() {
      System.setErr(new LoggingPrintStream("STDERR", System.err));
      System.setOut(new LoggingPrintStream("STDOUT", SYSOUT));
   }

   @SideOnly(Side.CLIENT)
   public static void printToSYSOUT(String var0) {
      SYSOUT.println(var0);
   }

   public static class BehaviorDispenseBoat extends BehaviorDefaultDispenseItem {
      private final BehaviorDefaultDispenseItem dispenseBehavior = new BehaviorDefaultDispenseItem();
      private final EntityBoat.Type boatType;

      public BehaviorDispenseBoat(EntityBoat.Type var1) {
         this.boatType = var1;
      }

      public ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
         EnumFacing var3 = (EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING);
         World var4 = var1.getWorld();
         double var5 = var1.getX() + (double)((float)var3.getFrontOffsetX() * 1.125F);
         double var7 = var1.getY() + (double)((float)var3.getFrontOffsetY() * 1.125F);
         double var9 = var1.getZ() + (double)((float)var3.getFrontOffsetZ() * 1.125F);
         BlockPos var11 = var1.getBlockPos().offset(var3);
         Material var12 = var4.getBlockState(var11).getMaterial();
         double var13;
         if (Material.WATER.equals(var12)) {
            var13 = 1.0D;
         } else {
            if (!Material.AIR.equals(var12) || !Material.WATER.equals(var4.getBlockState(var11.down()).getMaterial())) {
               return this.dispenseBehavior.dispense(var1, var2);
            }

            var13 = 0.0D;
         }

         EntityBoat var15 = new EntityBoat(var4, var5, var7 + var13, var9);
         var15.setBoatType(this.boatType);
         var15.rotationYaw = var3.getOpposite().getHorizontalAngle();
         var4.spawnEntity(var15);
         var2.splitStack(1);
         return var2;
      }

      protected void playDispenseSound(IBlockSource var1) {
         var1.getWorld().playEvent(1000, var1.getBlockPos(), 0);
      }
   }
}
