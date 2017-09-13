package net.minecraft.command.server;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

public class CommandSummon extends CommandBase {
   public String getName() {
      return "summon";
   }

   public int getRequiredPermissionLevel() {
      return 2;
   }

   public String getUsage(ICommandSender var1) {
      return "commands.summon.usage";
   }

   public void execute(MinecraftServer var1, ICommandSender var2, String[] var3) throws CommandException {
      if (var3.length < 1) {
         throw new WrongUsageException("commands.summon.usage", new Object[0]);
      } else {
         String var4 = var3[0];
         BlockPos var5 = var2.getPosition();
         Vec3d var6 = var2.getPositionVector();
         double var7 = var6.xCoord;
         double var9 = var6.yCoord;
         double var11 = var6.zCoord;
         if (var3.length >= 4) {
            var7 = parseDouble(var7, var3[1], true);
            var9 = parseDouble(var9, var3[2], false);
            var11 = parseDouble(var11, var3[3], true);
            var5 = new BlockPos(var7, var9, var11);
         }

         World var13 = var2.getEntityWorld();
         if (!var13.isBlockLoaded(var5)) {
            throw new CommandException("commands.summon.outOfWorld", new Object[0]);
         } else {
            if ("LightningBolt".equals(var4)) {
               var13.addWeatherEffect(new EntityLightningBolt(var13, var7, var9, var11, false));
               notifyCommandListener(var2, this, "commands.summon.success", new Object[0]);
            } else {
               NBTTagCompound var14 = new NBTTagCompound();
               boolean var15 = false;
               if (var3.length >= 5) {
                  ITextComponent var16 = getChatComponentFromNthArg(var2, var3, 4);

                  try {
                     var14 = JsonToNBT.getTagFromJson(var16.getUnformattedText());
                     var15 = true;
                  } catch (NBTException var18) {
                     throw new CommandException("commands.summon.tagError", new Object[]{var18.getMessage()});
                  }
               }

               var14.setString("id", var4);
               Entity var19 = AnvilChunkLoader.readWorldEntityPos(var14, var13, var7, var9, var11, true);
               if (var19 == null) {
                  throw new CommandException("commands.summon.failed", new Object[0]);
               }

               var19.setLocationAndAngles(var7, var9, var11, var19.rotationYaw, var19.rotationPitch);
               if (!var15 && var19 instanceof EntityLiving) {
                  ((EntityLiving)var19).onInitialSpawn(var13.getDifficultyForLocation(new BlockPos(var19)), (IEntityLivingData)null);
               }

               notifyCommandListener(var2, this, "commands.summon.success", new Object[0]);
            }

         }
      }
   }

   public List getTabCompletions(MinecraftServer var1, ICommandSender var2, String[] var3, @Nullable BlockPos var4) {
      return var3.length == 1 ? getListOfStringsMatchingLastWord(var3, EntityList.getEntityNameList()) : (var3.length > 1 && var3.length <= 4 ? getTabCompletionCoordinate(var3, 1, var4) : Collections.emptyList());
   }
}
