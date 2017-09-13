package net.minecraft.util.datafix.fixes;

import com.google.gson.JsonParseException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.StringUtils;
import net.minecraft.util.datafix.IFixableData;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public class BookPagesStrictJSON implements IFixableData {
   public int getFixVersion() {
      return 165;
   }

   public NBTTagCompound fixTagCompound(NBTTagCompound var1) {
      if ("minecraft:written_book".equals(var1.getString("id"))) {
         NBTTagCompound var2 = var1.getCompoundTag("tag");
         if (var2.hasKey("pages", 9)) {
            NBTTagList var3 = var2.getTagList("pages", 8);

            for(int var4 = 0; var4 < var3.tagCount(); ++var4) {
               String var5 = var3.getStringTagAt(var4);
               Object var6 = null;
               if (!"null".equals(var5) && !StringUtils.isNullOrEmpty(var5)) {
                  if (var5.charAt(0) == '"' && var5.charAt(var5.length() - 1) == '"' || var5.charAt(0) == '{' && var5.charAt(var5.length() - 1) == '}') {
                     try {
                        var6 = (ITextComponent)SignStrictJSON.GSON_INSTANCE.fromJson(var5, ITextComponent.class);
                        if (var6 == null) {
                           var6 = new TextComponentString("");
                        }
                     } catch (JsonParseException var10) {
                        ;
                     }

                     if (var6 == null) {
                        try {
                           var6 = ITextComponent.Serializer.jsonToComponent(var5);
                        } catch (JsonParseException var9) {
                           ;
                        }
                     }

                     if (var6 == null) {
                        try {
                           var6 = ITextComponent.Serializer.fromJsonLenient(var5);
                        } catch (JsonParseException var8) {
                           ;
                        }
                     }

                     if (var6 == null) {
                        var6 = new TextComponentString(var5);
                     }
                  } else {
                     var6 = new TextComponentString(var5);
                  }
               } else {
                  var6 = new TextComponentString("");
               }

               var3.set(var4, new NBTTagString(ITextComponent.Serializer.componentToJson((ITextComponent)var6)));
            }

            var2.setTag("pages", var3);
         }
      }

      return var1;
   }
}
