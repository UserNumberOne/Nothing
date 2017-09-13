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
         protected IProjectile getProjectileEntity(World world, IPosition iposition, ItemStack itemstack) {
            EntityTippedArrow entitytippedarrow = new EntityTippedArrow(world, iposition.getX(), iposition.getY(), iposition.getZ());
            entitytippedarrow.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
            return entitytippedarrow;
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.TIPPED_ARROW, new BehaviorProjectileDispense() {
         protected IProjectile getProjectileEntity(World world, IPosition iposition, ItemStack itemstack) {
            EntityTippedArrow entitytippedarrow = new EntityTippedArrow(world, iposition.getX(), iposition.getY(), iposition.getZ());
            entitytippedarrow.setPotionEffect(itemstack);
            entitytippedarrow.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
            return entitytippedarrow;
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.SPECTRAL_ARROW, new BehaviorProjectileDispense() {
         protected IProjectile getProjectileEntity(World world, IPosition iposition, ItemStack itemstack) {
            EntitySpectralArrow entityspectralarrow = new EntitySpectralArrow(world, iposition.getX(), iposition.getY(), iposition.getZ());
            entityspectralarrow.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
            return entityspectralarrow;
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.EGG, new BehaviorProjectileDispense() {
         protected IProjectile getProjectileEntity(World world, IPosition iposition, ItemStack itemstack) {
            return new EntityEgg(world, iposition.getX(), iposition.getY(), iposition.getZ());
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.SNOWBALL, new BehaviorProjectileDispense() {
         protected IProjectile getProjectileEntity(World world, IPosition iposition, ItemStack itemstack) {
            return new EntitySnowball(world, iposition.getX(), iposition.getY(), iposition.getZ());
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.EXPERIENCE_BOTTLE, new BehaviorProjectileDispense() {
         protected IProjectile getProjectileEntity(World world, IPosition iposition, ItemStack itemstack) {
            return new EntityExpBottle(world, iposition.getX(), iposition.getY(), iposition.getZ());
         }

         protected float getProjectileInaccuracy() {
            return super.getProjectileInaccuracy() * 0.5F;
         }

         protected float getProjectileVelocity() {
            return super.getProjectileVelocity() * 1.25F;
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.SPLASH_POTION, new IBehaviorDispenseItem() {
         public ItemStack dispense(IBlockSource isourceblock, ItemStack itemstack) {
            return (new BehaviorProjectileDispense() {
               protected IProjectile getProjectileEntity(World world, IPosition iposition, ItemStack itemstack1) {
                  return new EntityPotion(world, iposition.getX(), iposition.getY(), iposition.getZ(), itemstack1.copy());
               }

               protected float getProjectileInaccuracy() {
                  return super.getProjectileInaccuracy() * 0.5F;
               }

               protected float getProjectileVelocity() {
                  return super.getProjectileVelocity() * 1.25F;
               }
            }).dispense(isourceblock, itemstack);
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.LINGERING_POTION, new IBehaviorDispenseItem() {
         public ItemStack dispense(IBlockSource isourceblock, ItemStack itemstack) {
            return (new BehaviorProjectileDispense() {
               protected IProjectile getProjectileEntity(World world, IPosition iposition, ItemStack itemstack1) {
                  return new EntityPotion(world, iposition.getX(), iposition.getY(), iposition.getZ(), itemstack1.copy());
               }

               protected float getProjectileInaccuracy() {
                  return super.getProjectileInaccuracy() * 0.5F;
               }

               protected float getProjectileVelocity() {
                  return super.getProjectileVelocity() * 1.25F;
               }
            }).dispense(isourceblock, itemstack);
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.SPAWN_EGG, new BehaviorDefaultDispenseItem() {
         public ItemStack dispenseStack(IBlockSource isourceblock, ItemStack itemstack) {
            EnumFacing enumdirection = (EnumFacing)isourceblock.getBlockState().getValue(BlockDispenser.FACING);
            double d0 = isourceblock.getX() + (double)enumdirection.getFrontOffsetX();
            double d1 = (double)((float)isourceblock.getBlockPos().getY() + 0.2F);
            double d2 = isourceblock.getZ() + (double)enumdirection.getFrontOffsetZ();
            World world = isourceblock.getWorld();
            ItemStack itemstack1 = itemstack.splitStack(1);
            Block block = world.getWorld().getBlockAt(isourceblock.getBlockPos().getX(), isourceblock.getBlockPos().getY(), isourceblock.getBlockPos().getZ());
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);
            BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector(d0, d1, d2));
            if (!BlockDispenser.eventFired) {
               world.getServer().getPluginManager().callEvent(event);
            }

            if (event.isCancelled()) {
               ++itemstack.stackSize;
               return itemstack;
            } else {
               if (!event.getItem().equals(craftItem)) {
                  ++itemstack.stackSize;
                  ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                  IBehaviorDispenseItem idispensebehavior = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(eventStack.getItem());
                  if (idispensebehavior != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && idispensebehavior != this) {
                     idispensebehavior.dispense(isourceblock, eventStack);
                     return itemstack;
                  }
               }

               itemstack1 = CraftItemStack.asNMSCopy(event.getItem());
               Entity entity = ItemMonsterPlacer.spawnCreature(isourceblock.getWorld(), ItemMonsterPlacer.getEntityIdFromItem(itemstack), event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ(), SpawnReason.DISPENSE_EGG);
               if (entity instanceof EntityLivingBase && itemstack.hasDisplayName()) {
                  entity.setCustomNameTag(itemstack.getDisplayName());
               }

               ItemMonsterPlacer.applyItemEntityDataToEntity(isourceblock.getWorld(), (EntityPlayer)null, itemstack, entity);
               return itemstack;
            }
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.FIREWORKS, new BehaviorDefaultDispenseItem() {
         public ItemStack dispenseStack(IBlockSource isourceblock, ItemStack itemstack) {
            EnumFacing enumdirection = (EnumFacing)isourceblock.getBlockState().getValue(BlockDispenser.FACING);
            double d0 = isourceblock.getX() + (double)enumdirection.getFrontOffsetX();
            double d1 = (double)((float)isourceblock.getBlockPos().getY() + 0.2F);
            double d2 = isourceblock.getZ() + (double)enumdirection.getFrontOffsetZ();
            World world = isourceblock.getWorld();
            ItemStack itemstack1 = itemstack.splitStack(1);
            Block block = world.getWorld().getBlockAt(isourceblock.getBlockPos().getX(), isourceblock.getBlockPos().getY(), isourceblock.getBlockPos().getZ());
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);
            BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector(d0, d1, d2));
            if (!BlockDispenser.eventFired) {
               world.getServer().getPluginManager().callEvent(event);
            }

            if (event.isCancelled()) {
               ++itemstack.stackSize;
               return itemstack;
            } else {
               if (!event.getItem().equals(craftItem)) {
                  ++itemstack.stackSize;
                  ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                  IBehaviorDispenseItem idispensebehavior = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(eventStack.getItem());
                  if (idispensebehavior != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && idispensebehavior != this) {
                     idispensebehavior.dispense(isourceblock, eventStack);
                     return itemstack;
                  }
               }

               itemstack1 = CraftItemStack.asNMSCopy(event.getItem());
               EntityFireworkRocket entityfireworks = new EntityFireworkRocket(isourceblock.getWorld(), event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ(), itemstack1);
               isourceblock.getWorld().spawnEntity(entityfireworks);
               return itemstack;
            }
         }

         protected void playDispenseSound(IBlockSource isourceblock) {
            isourceblock.getWorld().playEvent(1004, isourceblock.getBlockPos(), 0);
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.FIRE_CHARGE, new BehaviorDefaultDispenseItem() {
         public ItemStack dispenseStack(IBlockSource isourceblock, ItemStack itemstack) {
            EnumFacing enumdirection = (EnumFacing)isourceblock.getBlockState().getValue(BlockDispenser.FACING);
            IPosition iposition = BlockDispenser.getDispensePosition(isourceblock);
            double d0 = iposition.getX() + (double)((float)enumdirection.getFrontOffsetX() * 0.3F);
            double d1 = iposition.getY() + (double)((float)enumdirection.getFrontOffsetY() * 0.3F);
            double d2 = iposition.getZ() + (double)((float)enumdirection.getFrontOffsetZ() * 0.3F);
            World world = isourceblock.getWorld();
            Random random = world.rand;
            double d3 = random.nextGaussian() * 0.05D + (double)enumdirection.getFrontOffsetX();
            double d4 = random.nextGaussian() * 0.05D + (double)enumdirection.getFrontOffsetY();
            double d5 = random.nextGaussian() * 0.05D + (double)enumdirection.getFrontOffsetZ();
            ItemStack itemstack1 = itemstack.splitStack(1);
            Block block = world.getWorld().getBlockAt(isourceblock.getBlockPos().getX(), isourceblock.getBlockPos().getY(), isourceblock.getBlockPos().getZ());
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);
            BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector(d3, d4, d5));
            if (!BlockDispenser.eventFired) {
               world.getServer().getPluginManager().callEvent(event);
            }

            if (event.isCancelled()) {
               ++itemstack.stackSize;
               return itemstack;
            } else {
               if (!event.getItem().equals(craftItem)) {
                  ++itemstack.stackSize;
                  ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                  IBehaviorDispenseItem idispensebehavior = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(eventStack.getItem());
                  if (idispensebehavior != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && idispensebehavior != this) {
                     idispensebehavior.dispense(isourceblock, eventStack);
                     return itemstack;
                  }
               }

               EntitySmallFireball fireball = new EntitySmallFireball(world, d0, d1, d2, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ());
               fireball.projectileSource = new CraftBlockProjectileSource((TileEntityDispenser)isourceblock.getBlockTileEntity());
               world.spawnEntity(fireball);
               return itemstack;
            }
         }

         protected void playDispenseSound(IBlockSource isourceblock) {
            isourceblock.getWorld().playEvent(1018, isourceblock.getBlockPos(), 0);
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.BOAT, new Bootstrap.BehaviorDispenseBoat(EntityBoat.Type.OAK));
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.SPRUCE_BOAT, new Bootstrap.BehaviorDispenseBoat(EntityBoat.Type.SPRUCE));
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.BIRCH_BOAT, new Bootstrap.BehaviorDispenseBoat(EntityBoat.Type.BIRCH));
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.JUNGLE_BOAT, new Bootstrap.BehaviorDispenseBoat(EntityBoat.Type.JUNGLE));
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.DARK_OAK_BOAT, new Bootstrap.BehaviorDispenseBoat(EntityBoat.Type.DARK_OAK));
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.ACACIA_BOAT, new Bootstrap.BehaviorDispenseBoat(EntityBoat.Type.ACACIA));
      BehaviorDefaultDispenseItem dispensebehavioritem = new BehaviorDefaultDispenseItem() {
         private final BehaviorDefaultDispenseItem b = new BehaviorDefaultDispenseItem();

         public ItemStack dispenseStack(IBlockSource isourceblock, ItemStack itemstack) {
            ItemBucket itembucket = (ItemBucket)itemstack.getItem();
            BlockPos blockposition = isourceblock.getBlockPos().offset((EnumFacing)isourceblock.getBlockState().getValue(BlockDispenser.FACING));
            World world = isourceblock.getWorld();
            int x = blockposition.getX();
            int y = blockposition.getY();
            int z = blockposition.getZ();
            if (world.isAirBlock(blockposition) || !world.getBlockState(blockposition).getMaterial().isSolid()) {
               Block block = world.getWorld().getBlockAt(isourceblock.getBlockPos().getX(), isourceblock.getBlockPos().getY(), isourceblock.getBlockPos().getZ());
               CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack);
               BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector(x, y, z));
               if (!BlockDispenser.eventFired) {
                  world.getServer().getPluginManager().callEvent(event);
               }

               if (event.isCancelled()) {
                  return itemstack;
               }

               if (!event.getItem().equals(craftItem)) {
                  ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                  IBehaviorDispenseItem idispensebehavior = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(eventStack.getItem());
                  if (idispensebehavior != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && idispensebehavior != this) {
                     idispensebehavior.dispense(isourceblock, eventStack);
                     return itemstack;
                  }
               }

               itembucket = (ItemBucket)CraftItemStack.asNMSCopy(event.getItem()).getItem();
            }

            if (itembucket.tryPlaceContainedLiquid((EntityPlayer)null, isourceblock.getWorld(), blockposition)) {
               Item item = Items.BUCKET;
               if (--itemstack.stackSize == 0) {
                  itemstack.setItem(Items.BUCKET);
                  itemstack.stackSize = 1;
               } else if (((TileEntityDispenser)isourceblock.getBlockTileEntity()).addItemStack(new ItemStack(item)) < 0) {
                  this.b.dispense(isourceblock, new ItemStack(item));
               }

               return itemstack;
            } else {
               return this.b.dispense(isourceblock, itemstack);
            }
         }
      };
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.LAVA_BUCKET, dispensebehavioritem);
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.WATER_BUCKET, dispensebehavioritem);
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.BUCKET, new BehaviorDefaultDispenseItem() {
         private final BehaviorDefaultDispenseItem b = new BehaviorDefaultDispenseItem();

         public ItemStack dispenseStack(IBlockSource isourceblock, ItemStack itemstack) {
            World world = isourceblock.getWorld();
            BlockPos blockposition = isourceblock.getBlockPos().offset((EnumFacing)isourceblock.getBlockState().getValue(BlockDispenser.FACING));
            IBlockState iblockdata = world.getBlockState(blockposition);
            net.minecraft.block.Block block = iblockdata.getBlock();
            Material material = iblockdata.getMaterial();
            Item item;
            if (Material.WATER.equals(material) && block instanceof BlockLiquid && ((Integer)iblockdata.getValue(BlockLiquid.LEVEL)).intValue() == 0) {
               item = Items.WATER_BUCKET;
            } else {
               if (!Material.LAVA.equals(material) || !(block instanceof BlockLiquid) || ((Integer)iblockdata.getValue(BlockLiquid.LEVEL)).intValue() != 0) {
                  return super.dispenseStack(isourceblock, itemstack);
               }

               item = Items.LAVA_BUCKET;
            }

            Block bukkitBlock = world.getWorld().getBlockAt(isourceblock.getBlockPos().getX(), isourceblock.getBlockPos().getY(), isourceblock.getBlockPos().getZ());
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack);
            BlockDispenseEvent event = new BlockDispenseEvent(bukkitBlock, craftItem.clone(), new Vector(blockposition.getX(), blockposition.getY(), blockposition.getZ()));
            if (!BlockDispenser.eventFired) {
               world.getServer().getPluginManager().callEvent(event);
            }

            if (event.isCancelled()) {
               return itemstack;
            } else {
               if (!event.getItem().equals(craftItem)) {
                  ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                  IBehaviorDispenseItem idispensebehavior = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(eventStack.getItem());
                  if (idispensebehavior != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && idispensebehavior != this) {
                     idispensebehavior.dispense(isourceblock, eventStack);
                     return itemstack;
                  }
               }

               world.setBlockToAir(blockposition);
               if (--itemstack.stackSize == 0) {
                  itemstack.setItem(item);
                  itemstack.stackSize = 1;
               } else if (((TileEntityDispenser)isourceblock.getBlockTileEntity()).addItemStack(new ItemStack(item)) < 0) {
                  this.b.dispense(isourceblock, new ItemStack(item));
               }

               return itemstack;
            }
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.FLINT_AND_STEEL, new BehaviorDefaultDispenseItem() {
         private boolean b = true;

         protected ItemStack dispenseStack(IBlockSource isourceblock, ItemStack itemstack) {
            World world = isourceblock.getWorld();
            BlockPos blockposition = isourceblock.getBlockPos().offset((EnumFacing)isourceblock.getBlockState().getValue(BlockDispenser.FACING));
            Block block = world.getWorld().getBlockAt(isourceblock.getBlockPos().getX(), isourceblock.getBlockPos().getY(), isourceblock.getBlockPos().getZ());
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack);
            BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector(0, 0, 0));
            if (!BlockDispenser.eventFired) {
               world.getServer().getPluginManager().callEvent(event);
            }

            if (event.isCancelled()) {
               return itemstack;
            } else {
               if (!event.getItem().equals(craftItem)) {
                  ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                  IBehaviorDispenseItem idispensebehavior = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(eventStack.getItem());
                  if (idispensebehavior != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && idispensebehavior != this) {
                     idispensebehavior.dispense(isourceblock, eventStack);
                     return itemstack;
                  }
               }

               if (world.isAirBlock(blockposition)) {
                  if (!CraftEventFactory.callBlockIgniteEvent(world, blockposition.getX(), blockposition.getY(), blockposition.getZ(), isourceblock.getBlockPos().getX(), isourceblock.getBlockPos().getY(), isourceblock.getBlockPos().getZ()).isCancelled()) {
                     world.setBlockState(blockposition, Blocks.FIRE.getDefaultState());
                     if (itemstack.attemptDamageItem(1, world.rand)) {
                        itemstack.stackSize = 0;
                     }
                  }
               } else if (world.getBlockState(blockposition).getBlock() == Blocks.TNT) {
                  Blocks.TNT.onBlockDestroyedByPlayer(world, blockposition, Blocks.TNT.getDefaultState().withProperty(BlockTNT.EXPLODE, Boolean.valueOf(true)));
                  world.setBlockToAir(blockposition);
               } else {
                  this.b = false;
               }

               return itemstack;
            }
         }

         protected void playDispenseSound(IBlockSource isourceblock) {
            if (this.b) {
               isourceblock.getWorld().playEvent(1000, isourceblock.getBlockPos(), 0);
            } else {
               isourceblock.getWorld().playEvent(1001, isourceblock.getBlockPos(), 0);
            }

         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.DYE, new BehaviorDefaultDispenseItem() {
         private boolean b = true;

         protected ItemStack dispenseStack(IBlockSource isourceblock, ItemStack itemstack) {
            if (EnumDyeColor.WHITE == EnumDyeColor.byDyeDamage(itemstack.getMetadata())) {
               World world = isourceblock.getWorld();
               BlockPos blockposition = isourceblock.getBlockPos().offset((EnumFacing)isourceblock.getBlockState().getValue(BlockDispenser.FACING));
               Block block = world.getWorld().getBlockAt(isourceblock.getBlockPos().getX(), isourceblock.getBlockPos().getY(), isourceblock.getBlockPos().getZ());
               CraftItemStack craftItem = CraftItemStack.asNewCraftStack(itemstack.getItem());
               BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector(0, 0, 0));
               if (!BlockDispenser.eventFired) {
                  world.getServer().getPluginManager().callEvent(event);
               }

               if (event.isCancelled()) {
                  return itemstack;
               } else {
                  if (!event.getItem().equals(craftItem)) {
                     ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                     IBehaviorDispenseItem idispensebehavior = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(eventStack.getItem());
                     if (idispensebehavior != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && idispensebehavior != this) {
                        idispensebehavior.dispense(isourceblock, eventStack);
                        return itemstack;
                     }
                  }

                  if (ItemDye.applyBonemeal(itemstack, world, blockposition)) {
                     if (!world.isRemote) {
                        world.playEvent(2005, blockposition, 0);
                     }
                  } else {
                     this.b = false;
                  }

                  return itemstack;
               }
            } else {
               return super.dispenseStack(isourceblock, itemstack);
            }
         }

         protected void playDispenseSound(IBlockSource isourceblock) {
            if (this.b) {
               isourceblock.getWorld().playEvent(1000, isourceblock.getBlockPos(), 0);
            } else {
               isourceblock.getWorld().playEvent(1001, isourceblock.getBlockPos(), 0);
            }

         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Item.getItemFromBlock(Blocks.TNT), new BehaviorDefaultDispenseItem() {
         protected ItemStack dispenseStack(IBlockSource isourceblock, ItemStack itemstack) {
            World world = isourceblock.getWorld();
            BlockPos blockposition = isourceblock.getBlockPos().offset((EnumFacing)isourceblock.getBlockState().getValue(BlockDispenser.FACING));
            ItemStack itemstack1 = itemstack.splitStack(1);
            Block block = world.getWorld().getBlockAt(isourceblock.getBlockPos().getX(), isourceblock.getBlockPos().getY(), isourceblock.getBlockPos().getZ());
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);
            BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector((double)blockposition.getX() + 0.5D, (double)blockposition.getY(), (double)blockposition.getZ() + 0.5D));
            if (!BlockDispenser.eventFired) {
               world.getServer().getPluginManager().callEvent(event);
            }

            if (event.isCancelled()) {
               ++itemstack.stackSize;
               return itemstack;
            } else {
               if (!event.getItem().equals(craftItem)) {
                  ++itemstack.stackSize;
                  ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                  IBehaviorDispenseItem idispensebehavior = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(eventStack.getItem());
                  if (idispensebehavior != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && idispensebehavior != this) {
                     idispensebehavior.dispense(isourceblock, eventStack);
                     return itemstack;
                  }
               }

               EntityTNTPrimed entitytntprimed = new EntityTNTPrimed(world, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ(), (EntityLivingBase)null);
               world.spawnEntity(entitytntprimed);
               world.playSound((EntityPlayer)null, entitytntprimed.posX, entitytntprimed.posY, entitytntprimed.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
               return itemstack;
            }
         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Items.SKULL, new BehaviorDefaultDispenseItem() {
         private boolean b = true;

         protected ItemStack dispenseStack(IBlockSource isourceblock, ItemStack itemstack) {
            World world = isourceblock.getWorld();
            EnumFacing enumdirection = (EnumFacing)isourceblock.getBlockState().getValue(BlockDispenser.FACING);
            BlockPos blockposition = isourceblock.getBlockPos().offset(enumdirection);
            BlockSkull blockskull = Blocks.SKULL;
            Block bukkitBlock = world.getWorld().getBlockAt(isourceblock.getBlockPos().getX(), isourceblock.getBlockPos().getY(), isourceblock.getBlockPos().getZ());
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack);
            BlockDispenseEvent event = new BlockDispenseEvent(bukkitBlock, craftItem.clone(), new Vector(blockposition.getX(), blockposition.getY(), blockposition.getZ()));
            if (!BlockDispenser.eventFired) {
               world.getServer().getPluginManager().callEvent(event);
            }

            if (event.isCancelled()) {
               return itemstack;
            } else {
               if (!event.getItem().equals(craftItem)) {
                  ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                  IBehaviorDispenseItem idispensebehavior = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(eventStack.getItem());
                  if (idispensebehavior != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && idispensebehavior != this) {
                     idispensebehavior.dispense(isourceblock, eventStack);
                     return itemstack;
                  }
               }

               if (world.isAirBlock(blockposition) && blockskull.canDispenserPlace(world, blockposition, itemstack)) {
                  if (!world.isRemote) {
                     world.setBlockState(blockposition, blockskull.getDefaultState().withProperty(BlockSkull.FACING, EnumFacing.UP), 3);
                     TileEntity tileentity = world.getTileEntity(blockposition);
                     if (tileentity instanceof TileEntitySkull) {
                        if (itemstack.getMetadata() == 3) {
                           GameProfile gameprofile = null;
                           if (itemstack.hasTagCompound()) {
                              NBTTagCompound nbttagcompound = itemstack.getTagCompound();
                              if (nbttagcompound.hasKey("SkullOwner", 10)) {
                                 gameprofile = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("SkullOwner"));
                              } else if (nbttagcompound.hasKey("SkullOwner", 8)) {
                                 String s = nbttagcompound.getString("SkullOwner");
                                 if (!StringUtils.isNullOrEmpty(s)) {
                                    gameprofile = new GameProfile((UUID)null, s);
                                 }
                              }
                           }

                           ((TileEntitySkull)tileentity).setPlayerProfile(gameprofile);
                        } else {
                           ((TileEntitySkull)tileentity).setType(itemstack.getMetadata());
                        }

                        ((TileEntitySkull)tileentity).setSkullRotation(enumdirection.getOpposite().getHorizontalIndex() * 4);
                        Blocks.SKULL.checkWitherSpawn(world, blockposition, (TileEntitySkull)tileentity);
                     }

                     --itemstack.stackSize;
                  }
               } else if (ItemArmor.dispenseArmor(isourceblock, itemstack) == null) {
                  this.b = false;
               }

               return itemstack;
            }
         }

         protected void playDispenseSound(IBlockSource isourceblock) {
            if (this.b) {
               isourceblock.getWorld().playEvent(1000, isourceblock.getBlockPos(), 0);
            } else {
               isourceblock.getWorld().playEvent(1001, isourceblock.getBlockPos(), 0);
            }

         }
      });
      BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(Item.getItemFromBlock(Blocks.PUMPKIN), new BehaviorDefaultDispenseItem() {
         private boolean b = true;

         protected ItemStack dispenseStack(IBlockSource isourceblock, ItemStack itemstack) {
            World world = isourceblock.getWorld();
            BlockPos blockposition = isourceblock.getBlockPos().offset((EnumFacing)isourceblock.getBlockState().getValue(BlockDispenser.FACING));
            BlockPumpkin blockpumpkin = (BlockPumpkin)Blocks.PUMPKIN;
            Block bukkitBlock = world.getWorld().getBlockAt(isourceblock.getBlockPos().getX(), isourceblock.getBlockPos().getY(), isourceblock.getBlockPos().getZ());
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack);
            BlockDispenseEvent event = new BlockDispenseEvent(bukkitBlock, craftItem.clone(), new Vector(blockposition.getX(), blockposition.getY(), blockposition.getZ()));
            if (!BlockDispenser.eventFired) {
               world.getServer().getPluginManager().callEvent(event);
            }

            if (event.isCancelled()) {
               return itemstack;
            } else {
               if (!event.getItem().equals(craftItem)) {
                  ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                  IBehaviorDispenseItem idispensebehavior = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(eventStack.getItem());
                  if (idispensebehavior != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && idispensebehavior != this) {
                     idispensebehavior.dispense(isourceblock, eventStack);
                     return itemstack;
                  }
               }

               if (world.isAirBlock(blockposition) && blockpumpkin.canDispenserPlace(world, blockposition)) {
                  if (!world.isRemote) {
                     world.setBlockState(blockposition, blockpumpkin.getDefaultState(), 3);
                  }

                  --itemstack.stackSize;
               } else {
                  ItemStack itemstack1 = ItemArmor.dispenseArmor(isourceblock, itemstack);
                  if (itemstack1 == null) {
                     this.b = false;
                  }
               }

               return itemstack;
            }
         }

         protected void playDispenseSound(IBlockSource isourceblock) {
            if (this.b) {
               isourceblock.getWorld().playEvent(1000, isourceblock.getBlockPos(), 0);
            } else {
               isourceblock.getWorld().playEvent(1001, isourceblock.getBlockPos(), 0);
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

      public BehaviorDispenseBoat(EntityBoat.Type entityboat_enumboattype) {
         this.boatType = entityboat_enumboattype;
      }

      public ItemStack dispenseStack(IBlockSource isourceblock, ItemStack itemstack) {
         EnumFacing enumdirection = (EnumFacing)isourceblock.getBlockState().getValue(BlockDispenser.FACING);
         World world = isourceblock.getWorld();
         double d0 = isourceblock.getX() + (double)((float)enumdirection.getFrontOffsetX() * 1.125F);
         double d1 = isourceblock.getY() + (double)((float)enumdirection.getFrontOffsetY() * 1.125F);
         double d2 = isourceblock.getZ() + (double)((float)enumdirection.getFrontOffsetZ() * 1.125F);
         BlockPos blockposition = isourceblock.getBlockPos().offset(enumdirection);
         Material material = world.getBlockState(blockposition).getMaterial();
         double d3;
         if (Material.WATER.equals(material)) {
            d3 = 1.0D;
         } else {
            if (!Material.AIR.equals(material) || !Material.WATER.equals(world.getBlockState(blockposition.down()).getMaterial())) {
               return this.dispenseBehavior.dispense(isourceblock, itemstack);
            }

            d3 = 0.0D;
         }

         ItemStack itemstack1 = itemstack.splitStack(1);
         Block block = world.getWorld().getBlockAt(isourceblock.getBlockPos().getX(), isourceblock.getBlockPos().getY(), isourceblock.getBlockPos().getZ());
         CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);
         BlockDispenseEvent event = new BlockDispenseEvent(block, craftItem.clone(), new Vector(d0, d1 + d3, d2));
         if (!BlockDispenser.eventFired) {
            world.getServer().getPluginManager().callEvent(event);
         }

         if (event.isCancelled()) {
            ++itemstack.stackSize;
            return itemstack;
         } else {
            if (!event.getItem().equals(craftItem)) {
               ++itemstack.stackSize;
               ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
               IBehaviorDispenseItem idispensebehavior = (IBehaviorDispenseItem)BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.getObject(eventStack.getItem());
               if (idispensebehavior != IBehaviorDispenseItem.DEFAULT_BEHAVIOR && idispensebehavior != this) {
                  idispensebehavior.dispense(isourceblock, eventStack);
                  return itemstack;
               }
            }

            EntityBoat entityboat = new EntityBoat(world, event.getVelocity().getX(), event.getVelocity().getY(), event.getVelocity().getZ());
            entityboat.setBoatType(this.boatType);
            entityboat.rotationYaw = enumdirection.getOpposite().getHorizontalAngle();
            world.spawnEntity(entityboat);
            return itemstack;
         }
      }

      protected void playDispenseSound(IBlockSource isourceblock) {
         isourceblock.getWorld().playEvent(1000, isourceblock.getBlockPos(), 0);
      }
   }
}
