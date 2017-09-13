package net.minecraft.init;

import com.mojang.authlib.GameProfile;
import java.io.PrintStream;
import java.util.Random;
import java.util.UUID;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockPumpkin;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
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
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.LoggingPrintStream;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_10_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_10_R1.projectiles.CraftBlockProjectileSource;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.util.Vector;

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
         public ItemStack dispense(IBlockSource var1, ItemStack var2) {
            return (new BehaviorProjectileDispense() {
               protected IProjectile getProjectileEntity(World var1, IPosition var2, ItemStack var3) {
                  return new EntityPotion(var1, var2.getX(), var2.getY(), var2.getZ(), var3.copy());
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
         public ItemStack dispense(IBlockSource var1, ItemStack var2) {
            return (new BehaviorProjectileDispense() {
               protected IProjectile getProjectileEntity(World var1, IPosition var2, ItemStack var3) {
                  return new EntityPotion(var1, var2.getX(), var2.getY(), var2.getZ(), var3.copy());
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
            World var10 = var1.getWorld();
            ItemStack var11 = var2.splitStack(1);
            Block var12 = var10.getWorld().getBlockAt(var1.getBlockPos().getX(), var1.getBlockPos().getY(), var1.getBlockPos().getZ());
            CraftItemStack var13 = CraftItemStack.asCraftMirror(var11);
            BlockDispenseEvent var14 = new BlockDispenseEvent(var12, var13.clone(), new Vector(var4, var6, var8));
            if (!BlockDispenser.eventFired) {
               var10.getServer().getPluginManager().callEvent(var14);
            }

            if (var14.isCancelled()) {
               ++var2.stackSize;
               return var2;
            } else {
               if (!var14.getItem().equals(var13)) {
                  ++var2.stackSize;
                  ItemStack var15 = CraftItemStack.asNMSCopy(var14.getItem());
                  IBehaviorDispenseItem var16 = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(var15.getItem());
                  if (var16 != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && var16 != this) {
                     var16.dispense(var1, var15);
                     return var2;
                  }
               }

               var11 = CraftItemStack.asNMSCopy(var14.getItem());
               Entity var18 = ItemMonsterPlacer.spawnCreature(var1.getWorld(), ItemMonsterPlacer.getEntityIdFromItem(var2), var14.getVelocity().getX(), var14.getVelocity().getY(), var14.getVelocity().getZ(), SpawnReason.DISPENSE_EGG);
               if (var18 instanceof EntityLivingBase && var2.hasDisplayName()) {
                  var18.setCustomNameTag(var2.getDisplayName());
               }

               ItemMonsterPlacer.applyItemEntityDataToEntity(var1.getWorld(), (EntityPlayer)null, var2, var18);
               return var2;
            }
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.FIREWORKS, new BehaviorDefaultDispenseItem() {
         public ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
            EnumFacing var3 = (EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING);
            double var4 = var1.getX() + (double)var3.getFrontOffsetX();
            double var6 = (double)((float)var1.getBlockPos().getY() + 0.2F);
            double var8 = var1.getZ() + (double)var3.getFrontOffsetZ();
            World var10 = var1.getWorld();
            ItemStack var11 = var2.splitStack(1);
            Block var12 = var10.getWorld().getBlockAt(var1.getBlockPos().getX(), var1.getBlockPos().getY(), var1.getBlockPos().getZ());
            CraftItemStack var13 = CraftItemStack.asCraftMirror(var11);
            BlockDispenseEvent var14 = new BlockDispenseEvent(var12, var13.clone(), new Vector(var4, var6, var8));
            if (!BlockDispenser.eventFired) {
               var10.getServer().getPluginManager().callEvent(var14);
            }

            if (var14.isCancelled()) {
               ++var2.stackSize;
               return var2;
            } else {
               if (!var14.getItem().equals(var13)) {
                  ++var2.stackSize;
                  ItemStack var15 = CraftItemStack.asNMSCopy(var14.getItem());
                  IBehaviorDispenseItem var16 = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(var15.getItem());
                  if (var16 != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && var16 != this) {
                     var16.dispense(var1, var15);
                     return var2;
                  }
               }

               var11 = CraftItemStack.asNMSCopy(var14.getItem());
               EntityFireworkRocket var18 = new EntityFireworkRocket(var1.getWorld(), var14.getVelocity().getX(), var14.getVelocity().getY(), var14.getVelocity().getZ(), var11);
               var1.getWorld().spawnEntity(var18);
               return var2;
            }
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
            ItemStack var19 = var2.splitStack(1);
            Block var20 = var11.getWorld().getBlockAt(var1.getBlockPos().getX(), var1.getBlockPos().getY(), var1.getBlockPos().getZ());
            CraftItemStack var21 = CraftItemStack.asCraftMirror(var19);
            BlockDispenseEvent var22 = new BlockDispenseEvent(var20, var21.clone(), new Vector(var13, var15, var17));
            if (!BlockDispenser.eventFired) {
               var11.getServer().getPluginManager().callEvent(var22);
            }

            if (var22.isCancelled()) {
               ++var2.stackSize;
               return var2;
            } else {
               if (!var22.getItem().equals(var21)) {
                  ++var2.stackSize;
                  ItemStack var23 = CraftItemStack.asNMSCopy(var22.getItem());
                  IBehaviorDispenseItem var24 = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(var23.getItem());
                  if (var24 != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && var24 != this) {
                     var24.dispense(var1, var23);
                     return var2;
                  }
               }

               EntitySmallFireball var25 = new EntitySmallFireball(var11, var5, var7, var9, var22.getVelocity().getX(), var22.getVelocity().getY(), var22.getVelocity().getZ());
               var25.projectileSource = new CraftBlockProjectileSource((TileEntityDispenser)var1.getBlockTileEntity());
               var11.spawnEntity(var25);
               return var2;
            }
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
         private final BehaviorDefaultDispenseItem b = new BehaviorDefaultDispenseItem();

         public ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
            ItemBucket var3 = (ItemBucket)var2.getItem();
            BlockPos var4 = var1.getBlockPos().offset((EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING));
            World var5 = var1.getWorld();
            int var6 = var4.getX();
            int var7 = var4.getY();
            int var8 = var4.getZ();
            if (var5.isAirBlock(var4) || !var5.getBlockState(var4).getMaterial().isSolid()) {
               Block var9 = var5.getWorld().getBlockAt(var1.getBlockPos().getX(), var1.getBlockPos().getY(), var1.getBlockPos().getZ());
               CraftItemStack var10 = CraftItemStack.asCraftMirror(var2);
               BlockDispenseEvent var11 = new BlockDispenseEvent(var9, var10.clone(), new Vector(var6, var7, var8));
               if (!BlockDispenser.eventFired) {
                  var5.getServer().getPluginManager().callEvent(var11);
               }

               if (var11.isCancelled()) {
                  return var2;
               }

               if (!var11.getItem().equals(var10)) {
                  ItemStack var12 = CraftItemStack.asNMSCopy(var11.getItem());
                  IBehaviorDispenseItem var13 = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(var12.getItem());
                  if (var13 != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && var13 != this) {
                     var13.dispense(var1, var12);
                     return var2;
                  }
               }

               var3 = (ItemBucket)CraftItemStack.asNMSCopy(var11.getItem()).getItem();
            }

            if (var3.tryPlaceContainedLiquid((EntityPlayer)null, var1.getWorld(), var4)) {
               Item var14 = Items.BUCKET;
               if (--var2.stackSize == 0) {
                  var2.setItem(Items.BUCKET);
                  var2.stackSize = 1;
               } else if (((TileEntityDispenser)var1.getBlockTileEntity()).addItemStack(new ItemStack(var14)) < 0) {
                  this.b.dispense(var1, new ItemStack(var14));
               }

               return var2;
            } else {
               return this.b.dispense(var1, var2);
            }
         }
      };
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.LAVA_BUCKET, var0);
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.WATER_BUCKET, var0);
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.BUCKET, new BehaviorDefaultDispenseItem() {
         private final BehaviorDefaultDispenseItem b = new BehaviorDefaultDispenseItem();

         public ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
            World var3 = var1.getWorld();
            BlockPos var4 = var1.getBlockPos().offset((EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING));
            IBlockState var5 = var3.getBlockState(var4);
            net.minecraft.block.Block var6 = var5.getBlock();
            Material var7 = var5.getMaterial();
            Item var8;
            if (Material.WATER.equals(var7) && var6 instanceof BlockLiquid && ((Integer)var5.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
               var8 = Items.WATER_BUCKET;
            } else {
               if (!Material.LAVA.equals(var7) || !(var6 instanceof BlockLiquid) || ((Integer)var5.getValue(BlockLiquid.LEVEL)).intValue() != 0) {
                  return super.dispenseStack(var1, var2);
               }

               var8 = Items.LAVA_BUCKET;
            }

            Block var9 = var3.getWorld().getBlockAt(var1.getBlockPos().getX(), var1.getBlockPos().getY(), var1.getBlockPos().getZ());
            CraftItemStack var10 = CraftItemStack.asCraftMirror(var2);
            BlockDispenseEvent var11 = new BlockDispenseEvent(var9, var10.clone(), new Vector(var4.getX(), var4.getY(), var4.getZ()));
            if (!BlockDispenser.eventFired) {
               var3.getServer().getPluginManager().callEvent(var11);
            }

            if (var11.isCancelled()) {
               return var2;
            } else {
               if (!var11.getItem().equals(var10)) {
                  ItemStack var12 = CraftItemStack.asNMSCopy(var11.getItem());
                  IBehaviorDispenseItem var13 = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(var12.getItem());
                  if (var13 != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && var13 != this) {
                     var13.dispense(var1, var12);
                     return var2;
                  }
               }

               var3.setBlockToAir(var4);
               if (--var2.stackSize == 0) {
                  var2.setItem(var8);
                  var2.stackSize = 1;
               } else if (((TileEntityDispenser)var1.getBlockTileEntity()).addItemStack(new ItemStack(var8)) < 0) {
                  this.b.dispense(var1, new ItemStack(var8));
               }

               return var2;
            }
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.FLINT_AND_STEEL, new BehaviorDefaultDispenseItem() {
         private boolean b = true;

         protected ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
            World var3 = var1.getWorld();
            BlockPos var4 = var1.getBlockPos().offset((EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING));
            Block var5 = var3.getWorld().getBlockAt(var1.getBlockPos().getX(), var1.getBlockPos().getY(), var1.getBlockPos().getZ());
            CraftItemStack var6 = CraftItemStack.asCraftMirror(var2);
            BlockDispenseEvent var7 = new BlockDispenseEvent(var5, var6.clone(), new Vector(0, 0, 0));
            if (!BlockDispenser.eventFired) {
               var3.getServer().getPluginManager().callEvent(var7);
            }

            if (var7.isCancelled()) {
               return var2;
            } else {
               if (!var7.getItem().equals(var6)) {
                  ItemStack var8 = CraftItemStack.asNMSCopy(var7.getItem());
                  IBehaviorDispenseItem var9 = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(var8.getItem());
                  if (var9 != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && var9 != this) {
                     var9.dispense(var1, var8);
                     return var2;
                  }
               }

               if (var3.isAirBlock(var4)) {
                  if (!CraftEventFactory.callBlockIgniteEvent(var3, var4.getX(), var4.getY(), var4.getZ(), var1.getBlockPos().getX(), var1.getBlockPos().getY(), var1.getBlockPos().getZ()).isCancelled()) {
                     var3.setBlockState(var4, Blocks.FIRE.getDefaultState());
                     if (var2.attemptDamageItem(1, var3.rand)) {
                        var2.stackSize = 0;
                     }
                  }
               } else if (var3.getBlockState(var4).getBlock() == Blocks.TNT) {
                  Blocks.TNT.onBlockDestroyedByPlayer(var3, var4, Blocks.TNT.getDefaultState().withProperty(BlockTNT.EXPLODE, Boolean.valueOf(true)));
                  var3.setBlockToAir(var4);
               } else {
                  this.b = false;
               }

               return var2;
            }
         }

         protected void playDispenseSound(IBlockSource var1) {
            if (this.b) {
               var1.getWorld().playEvent(1000, var1.getBlockPos(), 0);
            } else {
               var1.getWorld().playEvent(1001, var1.getBlockPos(), 0);
            }

         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.DYE, new BehaviorDefaultDispenseItem() {
         private boolean b = true;

         protected ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
            if (EnumDyeColor.WHITE == EnumDyeColor.byDyeDamage(var2.getMetadata())) {
               World var3 = var1.getWorld();
               BlockPos var4 = var1.getBlockPos().offset((EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING));
               Block var5 = var3.getWorld().getBlockAt(var1.getBlockPos().getX(), var1.getBlockPos().getY(), var1.getBlockPos().getZ());
               CraftItemStack var6 = CraftItemStack.asNewCraftStack(var2.getItem());
               BlockDispenseEvent var7 = new BlockDispenseEvent(var5, var6.clone(), new Vector(0, 0, 0));
               if (!BlockDispenser.eventFired) {
                  var3.getServer().getPluginManager().callEvent(var7);
               }

               if (var7.isCancelled()) {
                  return var2;
               } else {
                  if (!var7.getItem().equals(var6)) {
                     ItemStack var8 = CraftItemStack.asNMSCopy(var7.getItem());
                     IBehaviorDispenseItem var9 = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(var8.getItem());
                     if (var9 != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && var9 != this) {
                        var9.dispense(var1, var8);
                        return var2;
                     }
                  }

                  if (ItemDye.applyBonemeal(var2, var3, var4)) {
                     if (!var3.isRemote) {
                        var3.playEvent(2005, var4, 0);
                     }
                  } else {
                     this.b = false;
                  }

                  return var2;
               }
            } else {
               return super.dispenseStack(var1, var2);
            }
         }

         protected void playDispenseSound(IBlockSource var1) {
            if (this.b) {
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
            ItemStack var5 = var2.splitStack(1);
            Block var6 = var3.getWorld().getBlockAt(var1.getBlockPos().getX(), var1.getBlockPos().getY(), var1.getBlockPos().getZ());
            CraftItemStack var7 = CraftItemStack.asCraftMirror(var5);
            BlockDispenseEvent var8 = new BlockDispenseEvent(var6, var7.clone(), new Vector((double)var4.getX() + 0.5D, (double)var4.getY(), (double)var4.getZ() + 0.5D));
            if (!BlockDispenser.eventFired) {
               var3.getServer().getPluginManager().callEvent(var8);
            }

            if (var8.isCancelled()) {
               ++var2.stackSize;
               return var2;
            } else {
               if (!var8.getItem().equals(var7)) {
                  ++var2.stackSize;
                  ItemStack var9 = CraftItemStack.asNMSCopy(var8.getItem());
                  IBehaviorDispenseItem var10 = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(var9.getItem());
                  if (var10 != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && var10 != this) {
                     var10.dispense(var1, var9);
                     return var2;
                  }
               }

               EntityTNTPrimed var11 = new EntityTNTPrimed(var3, var8.getVelocity().getX(), var8.getVelocity().getY(), var8.getVelocity().getZ(), (EntityLivingBase)null);
               var3.spawnEntity(var11);
               var3.playSound((EntityPlayer)null, var11.posX, var11.posY, var11.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
               return var2;
            }
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.SKULL, new BehaviorDefaultDispenseItem() {
         private boolean b = true;

         protected ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
            World var3 = var1.getWorld();
            EnumFacing var4 = (EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING);
            BlockPos var5 = var1.getBlockPos().offset(var4);
            BlockSkull var6 = Blocks.SKULL;
            Block var7 = var3.getWorld().getBlockAt(var1.getBlockPos().getX(), var1.getBlockPos().getY(), var1.getBlockPos().getZ());
            CraftItemStack var8 = CraftItemStack.asCraftMirror(var2);
            BlockDispenseEvent var9 = new BlockDispenseEvent(var7, var8.clone(), new Vector(var5.getX(), var5.getY(), var5.getZ()));
            if (!BlockDispenser.eventFired) {
               var3.getServer().getPluginManager().callEvent(var9);
            }

            if (var9.isCancelled()) {
               return var2;
            } else {
               if (!var9.getItem().equals(var8)) {
                  ItemStack var10 = CraftItemStack.asNMSCopy(var9.getItem());
                  IBehaviorDispenseItem var11 = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(var10.getItem());
                  if (var11 != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && var11 != this) {
                     var11.dispense(var1, var10);
                     return var2;
                  }
               }

               if (var3.isAirBlock(var5) && var6.canDispenserPlace(var3, var5, var2)) {
                  if (!var3.isRemote) {
                     var3.setBlockState(var5, var6.getDefaultState().withProperty(BlockSkull.FACING, EnumFacing.UP), 3);
                     TileEntity var14 = var3.getTileEntity(var5);
                     if (var14 instanceof TileEntitySkull) {
                        if (var2.getMetadata() == 3) {
                           GameProfile var15 = null;
                           if (var2.hasTagCompound()) {
                              NBTTagCompound var12 = var2.getTagCompound();
                              if (var12.hasKey("SkullOwner", 10)) {
                                 var15 = NBTUtil.readGameProfileFromNBT(var12.getCompoundTag("SkullOwner"));
                              } else if (var12.hasKey("SkullOwner", 8)) {
                                 String var13 = var12.getString("SkullOwner");
                                 if (!StringUtils.isNullOrEmpty(var13)) {
                                    var15 = new GameProfile((UUID)null, var13);
                                 }
                              }
                           }

                           ((TileEntitySkull)var14).setPlayerProfile(var15);
                        } else {
                           ((TileEntitySkull)var14).setType(var2.getMetadata());
                        }

                        ((TileEntitySkull)var14).setSkullRotation(var4.getOpposite().getHorizontalIndex() * 4);
                        Blocks.SKULL.checkWitherSpawn(var3, var5, (TileEntitySkull)var14);
                     }

                     --var2.stackSize;
                  }
               } else if (ItemArmor.dispenseArmor(var1, var2) == null) {
                  this.b = false;
               }

               return var2;
            }
         }

         protected void playDispenseSound(IBlockSource var1) {
            if (this.b) {
               var1.getWorld().playEvent(1000, var1.getBlockPos(), 0);
            } else {
               var1.getWorld().playEvent(1001, var1.getBlockPos(), 0);
            }

         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Item.getItemFromBlock(Blocks.PUMPKIN), new BehaviorDefaultDispenseItem() {
         private boolean b = true;

         protected ItemStack dispenseStack(IBlockSource var1, ItemStack var2) {
            World var3 = var1.getWorld();
            BlockPos var4 = var1.getBlockPos().offset((EnumFacing)var1.getBlockState().getValue(BlockDispenser.FACING));
            BlockPumpkin var5 = (BlockPumpkin)Blocks.PUMPKIN;
            Block var6 = var3.getWorld().getBlockAt(var1.getBlockPos().getX(), var1.getBlockPos().getY(), var1.getBlockPos().getZ());
            CraftItemStack var7 = CraftItemStack.asCraftMirror(var2);
            BlockDispenseEvent var8 = new BlockDispenseEvent(var6, var7.clone(), new Vector(var4.getX(), var4.getY(), var4.getZ()));
            if (!BlockDispenser.eventFired) {
               var3.getServer().getPluginManager().callEvent(var8);
            }

            if (var8.isCancelled()) {
               return var2;
            } else {
               if (!var8.getItem().equals(var7)) {
                  ItemStack var9 = CraftItemStack.asNMSCopy(var8.getItem());
                  IBehaviorDispenseItem var10 = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(var9.getItem());
                  if (var10 != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && var10 != this) {
                     var10.dispense(var1, var9);
                     return var2;
                  }
               }

               if (var3.isAirBlock(var4) && var5.canDispenserPlace(var3, var4)) {
                  if (!var3.isRemote) {
                     var3.setBlockState(var4, var5.getDefaultState(), 3);
                  }

                  --var2.stackSize;
               } else {
                  ItemStack var11 = ItemArmor.dispenseArmor(var1, var2);
                  if (var11 == null) {
                     this.b = false;
                  }
               }

               return var2;
            }
         }

         protected void playDispenseSound(IBlockSource var1) {
            if (this.b) {
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
         net.minecraft.block.Block.registerBlocks();
         BlockFire.init();
         Potion.registerPotions();
         Enchantment.registerEnchantments();
         Item.registerItems();
         PotionType.registerPotionTypes();
         PotionHelper.init();
         StatList.init();
         Biome.registerBiomes();
         registerDispenserBehaviors();
      }

   }

   private static void redirectOutputToLog() {
      System.setErr(new LoggingPrintStream("STDERR", System.err));
      System.setOut(new LoggingPrintStream("STDOUT", SYSOUT));
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

         ItemStack var15 = var2.splitStack(1);
         Block var16 = var4.getWorld().getBlockAt(var1.getBlockPos().getX(), var1.getBlockPos().getY(), var1.getBlockPos().getZ());
         CraftItemStack var17 = CraftItemStack.asCraftMirror(var15);
         BlockDispenseEvent var18 = new BlockDispenseEvent(var16, var17.clone(), new Vector(var5, var7 + var13, var9));
         if (!BlockDispenser.eventFired) {
            var4.getServer().getPluginManager().callEvent(var18);
         }

         if (var18.isCancelled()) {
            ++var2.stackSize;
            return var2;
         } else {
            if (!var18.getItem().equals(var17)) {
               ++var2.stackSize;
               ItemStack var19 = CraftItemStack.asNMSCopy(var18.getItem());
               IBehaviorDispenseItem var20 = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(var19.getItem());
               if (var20 != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && var20 != this) {
                  var20.dispense(var1, var19);
                  return var2;
               }
            }

            EntityBoat var21 = new EntityBoat(var4, var18.getVelocity().getX(), var18.getVelocity().getY(), var18.getVelocity().getZ());
            var21.setBoatType(this.boatType);
            var21.rotationYaw = var3.getOpposite().getHorizontalAngle();
            var4.spawnEntity(var21);
            return var2;
         }
      }

      protected void playDispenseSound(IBlockSource var1) {
         var1.getWorld().playEvent(1000, var1.getBlockPos(), 0);
      }
   }
}
