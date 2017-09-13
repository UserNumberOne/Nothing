package net.minecraft.network.play.server;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;

public class SPacketPlayerListItem implements Packet {
   private SPacketPlayerListItem.Action action;
   private final List players = Lists.newArrayList();

   public SPacketPlayerListItem() {
   }

   public SPacketPlayerListItem(SPacketPlayerListItem.Action var1, EntityPlayerMP... var2) {
      this.action = var1;

      for(EntityPlayerMP var6 : var2) {
         this.players.add(new SPacketPlayerListItem.AddPlayerData(var6.getGameProfile(), var6.ping, var6.interactionManager.getGameType(), var6.getTabListDisplayName()));
      }

   }

   public SPacketPlayerListItem(SPacketPlayerListItem.Action var1, Iterable var2) {
      this.action = var1;

      for(EntityPlayerMP var4 : var2) {
         this.players.add(new SPacketPlayerListItem.AddPlayerData(var4.getGameProfile(), var4.ping, var4.interactionManager.getGameType(), var4.getTabListDisplayName()));
      }

   }

   public void readPacketData(PacketBuffer var1) throws IOException {
      this.action = (SPacketPlayerListItem.Action)var1.readEnumValue(SPacketPlayerListItem.Action.class);
      int var2 = var1.readVarInt();

      for(int var3 = 0; var3 < var2; ++var3) {
         GameProfile var4 = null;
         int var5 = 0;
         GameType var6 = null;
         ITextComponent var7 = null;
         switch(this.action) {
         case ADD_PLAYER:
            var4 = new GameProfile(var1.readUniqueId(), var1.readString(16));
            int var8 = var1.readVarInt();
            int var9 = 0;

            for(; var9 < var8; ++var9) {
               String var10 = var1.readString(32767);
               String var11 = var1.readString(32767);
               if (var1.readBoolean()) {
                  var4.getProperties().put(var10, new Property(var10, var11, var1.readString(32767)));
               } else {
                  var4.getProperties().put(var10, new Property(var10, var11));
               }
            }

            var6 = GameType.getByID(var1.readVarInt());
            var5 = var1.readVarInt();
            if (var1.readBoolean()) {
               var7 = var1.readTextComponent();
            }
            break;
         case UPDATE_GAME_MODE:
            var4 = new GameProfile(var1.readUniqueId(), (String)null);
            var6 = GameType.getByID(var1.readVarInt());
            break;
         case UPDATE_LATENCY:
            var4 = new GameProfile(var1.readUniqueId(), (String)null);
            var5 = var1.readVarInt();
            break;
         case UPDATE_DISPLAY_NAME:
            var4 = new GameProfile(var1.readUniqueId(), (String)null);
            if (var1.readBoolean()) {
               var7 = var1.readTextComponent();
            }
            break;
         case REMOVE_PLAYER:
            var4 = new GameProfile(var1.readUniqueId(), (String)null);
         }

         this.players.add(new SPacketPlayerListItem.AddPlayerData(var4, var5, var6, var7));
      }

   }

   public void writePacketData(PacketBuffer var1) throws IOException {
      var1.writeEnumValue(this.action);
      var1.writeVarInt(this.players.size());

      for(SPacketPlayerListItem.AddPlayerData var3 : this.players) {
         switch(this.action) {
         case ADD_PLAYER:
            var1.writeUniqueId(var3.getProfile().getId());
            var1.writeString(var3.getProfile().getName());
            var1.writeVarInt(var3.getProfile().getProperties().size());

            for(Property var5 : var3.getProfile().getProperties().values()) {
               var1.writeString(var5.getName());
               var1.writeString(var5.getValue());
               if (var5.hasSignature()) {
                  var1.writeBoolean(true);
                  var1.writeString(var5.getSignature());
               } else {
                  var1.writeBoolean(false);
               }
            }

            var1.writeVarInt(var3.getGameMode().getID());
            var1.writeVarInt(var3.getPing());
            if (var3.getDisplayName() == null) {
               var1.writeBoolean(false);
            } else {
               var1.writeBoolean(true);
               var1.writeTextComponent(var3.getDisplayName());
            }
            break;
         case UPDATE_GAME_MODE:
            var1.writeUniqueId(var3.getProfile().getId());
            var1.writeVarInt(var3.getGameMode().getID());
            break;
         case UPDATE_LATENCY:
            var1.writeUniqueId(var3.getProfile().getId());
            var1.writeVarInt(var3.getPing());
            break;
         case UPDATE_DISPLAY_NAME:
            var1.writeUniqueId(var3.getProfile().getId());
            if (var3.getDisplayName() == null) {
               var1.writeBoolean(false);
            } else {
               var1.writeBoolean(true);
               var1.writeTextComponent(var3.getDisplayName());
            }
            break;
         case REMOVE_PLAYER:
            var1.writeUniqueId(var3.getProfile().getId());
         }
      }

   }

   public void processPacket(INetHandlerPlayClient var1) {
      var1.handlePlayerListItem(this);
   }

   public String toString() {
      return Objects.toStringHelper(this).add("action", this.action).add("entries", this.players).toString();
   }

   public static enum Action {
      ADD_PLAYER,
      UPDATE_GAME_MODE,
      UPDATE_LATENCY,
      UPDATE_DISPLAY_NAME,
      REMOVE_PLAYER;
   }

   public class AddPlayerData {
      private final int ping;
      private final GameType gamemode;
      private final GameProfile profile;
      private final ITextComponent displayName;

      public AddPlayerData(GameProfile var2, int var3, GameType var4, @Nullable ITextComponent var5) {
         this.profile = var2;
         this.ping = var3;
         this.gamemode = var4;
         this.displayName = var5;
      }

      public GameProfile getProfile() {
         return this.profile;
      }

      public int getPing() {
         return this.ping;
      }

      public GameType getGameMode() {
         return this.gamemode;
      }

      @Nullable
      public ITextComponent getDisplayName() {
         return this.displayName;
      }

      public String toString() {
         return Objects.toStringHelper(this).add("latency", this.ping).add("gameMode", this.gamemode).add("profile", this.profile).add("displayName", this.displayName == null ? null : ITextComponent.Serializer.componentToJson(this.displayName)).toString();
      }
   }
}
