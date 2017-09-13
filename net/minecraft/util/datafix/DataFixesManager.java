package net.minecraft.util.datafix;

import net.minecraft.block.BlockJukebox;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.entity.item.EntityFireworkRocket;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.item.EntityMinecartChest;
import net.minecraft.entity.item.EntityMinecartCommandBlock;
import net.minecraft.entity.item.EntityMinecartEmpty;
import net.minecraft.entity.item.EntityMinecartFurnace;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.item.EntityMinecartMobSpawner;
import net.minecraft.entity.item.EntityMinecartTNT;
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
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityDragonFireball;
import net.minecraft.entity.projectile.EntityEgg;
import net.minecraft.entity.projectile.EntityLargeFireball;
import net.minecraft.entity.projectile.EntityPotion;
import net.minecraft.entity.projectile.EntitySmallFireball;
import net.minecraft.entity.projectile.EntitySnowball;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.tileentity.TileEntityDropper;
import net.minecraft.tileentity.TileEntityFlowerPot;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.datafix.fixes.ArmorStandSilent;
import net.minecraft.util.datafix.fixes.BookPagesStrictJSON;
import net.minecraft.util.datafix.fixes.CookedFishIDTypo;
import net.minecraft.util.datafix.fixes.EntityArmorAndHeld;
import net.minecraft.util.datafix.fixes.EntityHealth;
import net.minecraft.util.datafix.fixes.ForceVBOOn;
import net.minecraft.util.datafix.fixes.HorseSaddle;
import net.minecraft.util.datafix.fixes.ItemIntIDToString;
import net.minecraft.util.datafix.fixes.MinecartEntityTypes;
import net.minecraft.util.datafix.fixes.PaintingDirection;
import net.minecraft.util.datafix.fixes.PotionItems;
import net.minecraft.util.datafix.fixes.RedundantChanceTags;
import net.minecraft.util.datafix.fixes.RidingToPassengers;
import net.minecraft.util.datafix.fixes.SignStrictJSON;
import net.minecraft.util.datafix.fixes.SpawnEggNames;
import net.minecraft.util.datafix.fixes.SpawnerEntityTypes;
import net.minecraft.util.datafix.fixes.StringToUUID;
import net.minecraft.util.datafix.fixes.ZombieProfToType;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.storage.WorldInfo;

public class DataFixesManager {
   private static void registerFixes(DataFixer var0) {
      var0.registerFix(FixTypes.ENTITY, new EntityArmorAndHeld());
      var0.registerFix(FixTypes.BLOCK_ENTITY, new SignStrictJSON());
      var0.registerFix(FixTypes.ITEM_INSTANCE, new ItemIntIDToString());
      var0.registerFix(FixTypes.ITEM_INSTANCE, new PotionItems());
      var0.registerFix(FixTypes.ITEM_INSTANCE, new SpawnEggNames());
      var0.registerFix(FixTypes.ENTITY, new MinecartEntityTypes());
      var0.registerFix(FixTypes.BLOCK_ENTITY, new SpawnerEntityTypes());
      var0.registerFix(FixTypes.ENTITY, new StringToUUID());
      var0.registerFix(FixTypes.ENTITY, new EntityHealth());
      var0.registerFix(FixTypes.ENTITY, new HorseSaddle());
      var0.registerFix(FixTypes.ENTITY, new PaintingDirection());
      var0.registerFix(FixTypes.ENTITY, new RedundantChanceTags());
      var0.registerFix(FixTypes.ENTITY, new RidingToPassengers());
      var0.registerFix(FixTypes.ENTITY, new ArmorStandSilent());
      var0.registerFix(FixTypes.ITEM_INSTANCE, new BookPagesStrictJSON());
      var0.registerFix(FixTypes.ITEM_INSTANCE, new CookedFishIDTypo());
      var0.registerFix(FixTypes.ENTITY, new ZombieProfToType());
      var0.registerFix(FixTypes.OPTIONS, new ForceVBOOn());
   }

   public static DataFixer createFixer() {
      DataFixer var0 = new DataFixer(512);
      WorldInfo.registerFixes(var0);
      EntityPlayer.registerFixesPlayer(var0);
      AnvilChunkLoader.registerFixes(var0);
      ItemStack.registerFixes(var0);
      EntityArmorStand.registerFixesArmorStand(var0);
      EntityArrow.registerFixesArrow(var0);
      EntityBat.registerFixesBat(var0);
      EntityBlaze.registerFixesBlaze(var0);
      EntityCaveSpider.registerFixesCaveSpider(var0);
      EntityChicken.registerFixesChicken(var0);
      EntityCow.registerFixesCow(var0);
      EntityCreeper.registerFixesCreeper(var0);
      EntityDragonFireball.registerFixesDragonFireball(var0);
      EntityDragon.registerFixesDragon(var0);
      EntityEnderman.registerFixesEnderman(var0);
      EntityEndermite.registerFixesEndermite(var0);
      EntityFallingBlock.registerFixesFallingBlock(var0);
      EntityLargeFireball.registerFixesLargeFireball(var0);
      EntityFireworkRocket.registerFixesFireworkRocket(var0);
      EntityGhast.registerFixesGhast(var0);
      EntityGiantZombie.registerFixesGiantZombie(var0);
      EntityGuardian.registerFixesGuardian(var0);
      EntityHorse.registerFixesHorse(var0);
      EntityItem.registerFixesItem(var0);
      EntityItemFrame.registerFixesItemFrame(var0);
      EntityMagmaCube.registerFixesMagmaCube(var0);
      EntityMinecartChest.registerFixesMinecartChest(var0);
      EntityMinecartCommandBlock.registerFixesMinecartCommand(var0);
      EntityMinecartFurnace.registerFixesMinecartFurnace(var0);
      EntityMinecartHopper.registerFixesMinecartHopper(var0);
      EntityMinecartEmpty.registerFixesMinecartEmpty(var0);
      EntityMinecartMobSpawner.registerFixesMinecartMobSpawner(var0);
      EntityMinecartTNT.registerFixesMinecartTNT(var0);
      EntityLiving.registerFixesMob(var0);
      EntityMob.registerFixesMonster(var0);
      EntityMooshroom.registerFixesMooshroom(var0);
      EntityOcelot.registerFixesOcelot(var0);
      EntityPig.registerFixesPig(var0);
      EntityPigZombie.registerFixesPigZombie(var0);
      EntityRabbit.registerFixesRabbit(var0);
      EntitySheep.registerFixesSheep(var0);
      EntityShulker.registerFixesShulker(var0);
      EntitySilverfish.registerFixesSilverfish(var0);
      EntitySkeleton.registerFixesSkeleton(var0);
      EntitySlime.registerFixesSlime(var0);
      EntitySmallFireball.registerFixesSmallFireball(var0);
      EntitySnowman.registerFixesSnowman(var0);
      EntitySnowball.registerFixesSnowball(var0);
      EntitySpectralArrow.registerFixesSpectralArrow(var0);
      EntitySpider.registerFixesSpider(var0);
      EntitySquid.registerFixesSquid(var0);
      EntityEgg.registerFixesEgg(var0);
      EntityEnderPearl.registerFixesEnderPearl(var0);
      EntityExpBottle.registerFixesExpBottle(var0);
      EntityPotion.registerFixesPotion(var0);
      EntityTippedArrow.registerFixesTippedArrow(var0);
      EntityVillager.registerFixesVillager(var0);
      EntityIronGolem.registerFixesIronGolem(var0);
      EntityWitch.registerFixesWitch(var0);
      EntityWither.registerFixesWither(var0);
      EntityWitherSkull.registerFixesWitherSkull(var0);
      EntityWolf.registerFixesWolf(var0);
      EntityZombie.registerFixesZombie(var0);
      TileEntityPiston.registerFixesPiston(var0);
      TileEntityFlowerPot.registerFixesFlowerPot(var0);
      TileEntityFurnace.registerFixesFurnace(var0);
      TileEntityChest.registerFixesChest(var0);
      TileEntityDispenser.registerFixes(var0);
      TileEntityDropper.registerFixesDropper(var0);
      TileEntityBrewingStand.registerFixesBrewingStand(var0);
      TileEntityHopper.registerFixesHopper(var0);
      BlockJukebox.registerFixesJukebox(var0);
      TileEntityMobSpawner.registerFixesMobSpawner(var0);
      registerFixes(var0);
      return var0;
   }

   public static NBTTagCompound processItemStack(IDataFixer var0, NBTTagCompound var1, int var2, String var3) {
      if (var1.hasKey(var3, 10)) {
         var1.setTag(var3, var0.process(FixTypes.ITEM_INSTANCE, var1.getCompoundTag(var3), var2));
      }

      return var1;
   }

   public static NBTTagCompound processInventory(IDataFixer var0, NBTTagCompound var1, int var2, String var3) {
      if (var1.hasKey(var3, 9)) {
         NBTTagList var4 = var1.getTagList(var3, 10);

         for(int var5 = 0; var5 < var4.tagCount(); ++var5) {
            var4.set(var5, var0.process(FixTypes.ITEM_INSTANCE, var4.getCompoundTagAt(var5), var2));
         }
      }

      return var1;
   }
}
