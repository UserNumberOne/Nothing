package net.minecraft.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityEnderEye;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.item.EntityMinecartCommandBlock;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.item.EntityMinecartFurnace;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.item.EntityMinecartMobSpawner;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.item.EntityPainting;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityBlaze;
import net.minecraft.entity.monster.EntityCaveSpider;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityEndermite;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGiantZombie;
import net.minecraft.entity.monster.EntityGuardian;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.entity.monster.EntityMagmaCube;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityPolarBear;
import net.minecraft.entity.monster.EntityShulker;
import net.minecraft.entity.monster.EntitySilverfish;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.monster.EntitySnowman;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityWitch;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityMooshroom;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntityRabbit;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntityShulkerBullet;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EntityList {
   private static final Logger LOGGER = LogManager.getLogger();
   public static final Map NAME_TO_CLASS = Maps.newHashMap();
   public static final Map CLASS_TO_NAME = Maps.newHashMap();
   public static final Map ID_TO_CLASS = Maps.newHashMap();
   private static final Map CLASS_TO_ID = Maps.newHashMap();
   private static final Map NAME_TO_ID = Maps.newHashMap();
   public static final Map ENTITY_EGGS = Maps.newLinkedHashMap();

   public static void addMapping(Class var0, String var1, int var2) {
      if (var2 >= 0 && var2 <= 255) {
         if (NAME_TO_CLASS.containsKey(var1)) {
            throw new IllegalArgumentException("ID is already registered: " + var1);
         } else if (ID_TO_CLASS.containsKey(Integer.valueOf(var2))) {
            throw new IllegalArgumentException("ID is already registered: " + var2);
         } else if (var2 == 0) {
            throw new IllegalArgumentException("Cannot register to reserved id: " + var2);
         } else if (var0 == null) {
            throw new IllegalArgumentException("Cannot register null clazz for id: " + var2);
         } else {
            NAME_TO_CLASS.put(var1, var0);
            CLASS_TO_NAME.put(var0, var1);
            ID_TO_CLASS.put(Integer.valueOf(var2), var0);
            CLASS_TO_ID.put(var0, Integer.valueOf(var2));
            NAME_TO_ID.put(var1, Integer.valueOf(var2));
         }
      } else {
         throw new IllegalArgumentException("Attempted to register a entity with invalid ID: " + var2 + " Name: " + var1 + " Class: " + var0);
      }
   }

   public static void addMapping(Class var0, String var1, int var2, int var3, int var4) {
      addMapping(var0, var1, var2);
      ENTITY_EGGS.put(var1, new EntityList.EntityEggInfo(var1, var3, var4));
   }

   @Nullable
   public static Entity createEntityByName(String var0, World var1) {
      Entity var2 = null;

      try {
         Class var3 = (Class)NAME_TO_CLASS.get(var0);
         if (var3 != null) {
            var2 = (Entity)var3.getConstructor(World.class).newInstance(var1);
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      return var2;
   }

   @Nullable
   public static Entity createEntityFromNBT(NBTTagCompound var0, World var1) {
      Entity var2 = null;
      Class var3 = null;

      try {
         var3 = (Class)NAME_TO_CLASS.get(var0.getString("id"));
         if (var3 != null) {
            var2 = (Entity)var3.getConstructor(World.class).newInstance(var1);
         }
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      if (var2 != null) {
         try {
            var2.readFromNBT(var0);
         } catch (Exception var5) {
            FMLLog.log(Level.ERROR, var5, "An Entity %s(%s) has thrown an exception during loading, its state cannot be restored. Report this to the mod author", new Object[]{var0.getString("id"), var3.getName()});
            var2 = null;
         }
      } else {
         LOGGER.warn("Skipping Entity with id {}", new Object[]{var0.getString("id")});
      }

      return var2;
   }

   @Nullable
   public static Entity createEntityByID(int var0, World var1) {
      Entity var2 = null;

      try {
         Class var3 = getClassFromID(var0);
         if (var3 != null) {
            var2 = (Entity)var3.getConstructor(World.class).newInstance(var1);
         }
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      if (var2 == null) {
         LOGGER.warn("Skipping Entity with id {}", new Object[]{var0});
      }

      return var2;
   }

   @Nullable
   public static Entity createEntityByIDFromName(String var0, World var1) {
      Entity var2 = createEntityByName(var0, var1);
      return var2 == null ? createEntityByName("Pig", var1) : var2;
   }

   public static int getEntityID(Entity var0) {
      Integer var1 = (Integer)CLASS_TO_ID.get(var0.getClass());
      return var1 == null ? 0 : var1.intValue();
   }

   @Nullable
   public static Class getClassFromID(int var0) {
      return (Class)ID_TO_CLASS.get(Integer.valueOf(var0));
   }

   public static String getEntityString(Entity var0) {
      return getEntityStringFromClass(var0.getClass());
   }

   public static String getEntityStringFromClass(Class var0) {
      return (String)CLASS_TO_NAME.get(var0);
   }

   public static int getIDFromString(String var0) {
      Integer var1 = (Integer)NAME_TO_ID.get(var0);
      return var1 == null ? 90 : var1.intValue();
   }

   public static void init() {
   }

   public static List getEntityNameList() {
      Set var0 = NAME_TO_CLASS.keySet();
      ArrayList var1 = Lists.newArrayList();

      for(String var3 : var0) {
         Class var4 = (Class)NAME_TO_CLASS.get(var3);
         if ((var4.getModifiers() & 1024) != 1024) {
            var1.add(var3);
         }
      }

      var1.add("LightningBolt");
      return var1;
   }

   public static boolean isStringEntityName(Entity var0, String var1) {
      String var2 = getEntityString(var0);
      if (var2 == null) {
         if (var0 instanceof EntityPlayer) {
            var2 = "Player";
         } else {
            if (!(var0 instanceof EntityLightningBolt)) {
               return false;
            }

            var2 = "LightningBolt";
         }
      }

      return var1.equals(var2);
   }

   public static boolean isStringValidEntityName(String var0) {
      return "Player".equals(var0) || getEntityNameList().contains(var0);
   }

   static {
      addMapping(EntityItem.class, "Item", 1);
      addMapping(EntityXPOrb.class, "XPOrb", 2);
      addMapping(EntityAreaEffectCloud.class, "AreaEffectCloud", 3);
      addMapping(EntityEgg.class, "ThrownEgg", 7);
      addMapping(EntityLeashKnot.class, "LeashKnot", 8);
      addMapping(EntityPainting.class, "Painting", 9);
      addMapping(EntityTippedArrow.class, "Arrow", 10);
      addMapping(EntitySnowball.class, "Snowball", 11);
      addMapping(EntityLargeFireball.class, "Fireball", 12);
      addMapping(EntitySmallFireball.class, "SmallFireball", 13);
      addMapping(EntityEnderPearl.class, "ThrownEnderpearl", 14);
      addMapping(EntityEnderEye.class, "EyeOfEnderSignal", 15);
      addMapping(EntityPotion.class, "ThrownPotion", 16);
      addMapping(EntityExpBottle.class, "ThrownExpBottle", 17);
      addMapping(EntityItemFrame.class, "ItemFrame", 18);
      addMapping(EntityWitherSkull.class, "WitherSkull", 19);
      addMapping(EntityTNTPrimed.class, "PrimedTnt", 20);
      addMapping(EntityFallingBlock.class, "FallingSand", 21);
      addMapping(EntityFireworkRocket.class, "FireworksRocketEntity", 22);
      addMapping(EntitySpectralArrow.class, "SpectralArrow", 24);
      addMapping(EntityShulkerBullet.class, "ShulkerBullet", 25);
      addMapping(EntityDragonFireball.class, "DragonFireball", 26);
      addMapping(EntityArmorStand.class, "ArmorStand", 30);
      addMapping(EntityBoat.class, "Boat", 41);
      addMapping(EntityMinecartEmpty.class, EntityMinecart.Type.RIDEABLE.getName(), 42);
      addMapping(EntityMinecartChest.class, EntityMinecart.Type.CHEST.getName(), 43);
      addMapping(EntityMinecartFurnace.class, EntityMinecart.Type.FURNACE.getName(), 44);
      addMapping(EntityMinecartTNT.class, EntityMinecart.Type.TNT.getName(), 45);
      addMapping(EntityMinecartHopper.class, EntityMinecart.Type.HOPPER.getName(), 46);
      addMapping(EntityMinecartMobSpawner.class, EntityMinecart.Type.SPAWNER.getName(), 47);
      addMapping(EntityMinecartCommandBlock.class, EntityMinecart.Type.COMMAND_BLOCK.getName(), 40);
      addMapping(EntityLiving.class, "Mob", 48);
      addMapping(EntityMob.class, "Monster", 49);
      addMapping(EntityCreeper.class, "Creeper", 50, 894731, 0);
      addMapping(EntitySkeleton.class, "Skeleton", 51, 12698049, 4802889);
      addMapping(EntitySpider.class, "Spider", 52, 3419431, 11013646);
      addMapping(EntityGiantZombie.class, "Giant", 53);
      addMapping(EntityZombie.class, "Zombie", 54, 44975, 7969893);
      addMapping(EntitySlime.class, "Slime", 55, 5349438, 8306542);
      addMapping(EntityGhast.class, "Ghast", 56, 16382457, 12369084);
      addMapping(EntityPigZombie.class, "PigZombie", 57, 15373203, 5009705);
      addMapping(EntityEnderman.class, "Enderman", 58, 1447446, 0);
      addMapping(EntityCaveSpider.class, "CaveSpider", 59, 803406, 11013646);
      addMapping(EntitySilverfish.class, "Silverfish", 60, 7237230, 3158064);
      addMapping(EntityBlaze.class, "Blaze", 61, 16167425, 16775294);
      addMapping(EntityMagmaCube.class, "LavaSlime", 62, 3407872, 16579584);
      addMapping(EntityDragon.class, "EnderDragon", 63);
      addMapping(EntityWither.class, "WitherBoss", 64);
      addMapping(EntityBat.class, "Bat", 65, 4996656, 986895);
      addMapping(EntityWitch.class, "Witch", 66, 3407872, 5349438);
      addMapping(EntityEndermite.class, "Endermite", 67, 1447446, 7237230);
      addMapping(EntityGuardian.class, "Guardian", 68, 5931634, 15826224);
      addMapping(EntityShulker.class, "Shulker", 69, 9725844, 5060690);
      addMapping(EntityPig.class, "Pig", 90, 15771042, 14377823);
      addMapping(EntitySheep.class, "Sheep", 91, 15198183, 16758197);
      addMapping(EntityCow.class, "Cow", 92, 4470310, 10592673);
      addMapping(EntityChicken.class, "Chicken", 93, 10592673, 16711680);
      addMapping(EntitySquid.class, "Squid", 94, 2243405, 7375001);
      addMapping(EntityWolf.class, "Wolf", 95, 14144467, 13545366);
      addMapping(EntityMooshroom.class, "MushroomCow", 96, 10489616, 12040119);
      addMapping(EntitySnowman.class, "SnowMan", 97);
      addMapping(EntityOcelot.class, "Ozelot", 98, 15720061, 5653556);
      addMapping(EntityIronGolem.class, "VillagerGolem", 99);
      addMapping(EntityHorse.class, "EntityHorse", 100, 12623485, 15656192);
      addMapping(EntityRabbit.class, "Rabbit", 101, 10051392, 7555121);
      addMapping(EntityPolarBear.class, "PolarBear", 102, 15921906, 9803152);
      addMapping(EntityVillager.class, "Villager", 120, 5651507, 12422002);
      addMapping(EntityEnderCrystal.class, "EnderCrystal", 200);
   }

   public static class EntityEggInfo {
      public final String spawnedID;
      public final int primaryColor;
      public final int secondaryColor;
      public final StatBase killEntityStat;
      public final StatBase entityKilledByStat;

      public EntityEggInfo(String var1, int var2, int var3) {
         this.spawnedID = var1;
         this.primaryColor = var2;
         this.secondaryColor = var3;
         this.killEntityStat = StatList.getStatKillEntity(this);
         this.entityKilledByStat = StatList.getStatEntityKilledBy(this);
      }
   }
}
