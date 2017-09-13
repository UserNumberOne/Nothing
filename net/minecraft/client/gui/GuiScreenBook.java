package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.google.gson.JsonParseException;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

@SideOnly(Side.CLIENT)
public class GuiScreenBook extends GuiScreen {
   private static final Logger LOGGER = LogManager.getLogger();
   private static final ResourceLocation BOOK_GUI_TEXTURES = new ResourceLocation("textures/gui/book.png");
   private final EntityPlayer editingPlayer;
   private final ItemStack bookObj;
   private final boolean bookIsUnsigned;
   private boolean bookIsModified;
   private boolean bookGettingSigned;
   private int updateCount;
   private final int bookImageWidth = 192;
   private final int bookImageHeight = 192;
   private int bookTotalPages = 1;
   private int currPage;
   private NBTTagList bookPages;
   private String bookTitle = "";
   private List cachedComponents;
   private int cachedPage = -1;
   private GuiScreenBook.NextPageButton buttonNextPage;
   private GuiScreenBook.NextPageButton buttonPreviousPage;
   private GuiButton buttonDone;
   private GuiButton buttonSign;
   private GuiButton buttonFinalize;
   private GuiButton buttonCancel;

   public GuiScreenBook(EntityPlayer var1, ItemStack var2, boolean var3) {
      this.editingPlayer = var1;
      this.bookObj = var2;
      this.bookIsUnsigned = var3;
      if (var2.hasTagCompound()) {
         NBTTagCompound var4 = var2.getTagCompound();
         this.bookPages = var4.getTagList("pages", 8);
         if (this.bookPages != null) {
            this.bookPages = this.bookPages.copy();
            this.bookTotalPages = this.bookPages.tagCount();
            if (this.bookTotalPages < 1) {
               this.bookTotalPages = 1;
            }
         }
      }

      if (this.bookPages == null && var3) {
         this.bookPages = new NBTTagList();
         this.bookPages.appendTag(new NBTTagString(""));
         this.bookTotalPages = 1;
      }

   }

   public void updateScreen() {
      super.updateScreen();
      ++this.updateCount;
   }

   public void initGui() {
      this.buttonList.clear();
      Keyboard.enableRepeatEvents(true);
      if (this.bookIsUnsigned) {
         this.buttonSign = this.addButton(new GuiButton(3, this.width / 2 - 100, 196, 98, 20, I18n.format("book.signButton")));
         this.buttonDone = this.addButton(new GuiButton(0, this.width / 2 + 2, 196, 98, 20, I18n.format("gui.done")));
         this.buttonFinalize = this.addButton(new GuiButton(5, this.width / 2 - 100, 196, 98, 20, I18n.format("book.finalizeButton")));
         this.buttonCancel = this.addButton(new GuiButton(4, this.width / 2 + 2, 196, 98, 20, I18n.format("gui.cancel")));
      } else {
         this.buttonDone = this.addButton(new GuiButton(0, this.width / 2 - 100, 196, 200, 20, I18n.format("gui.done")));
      }

      int var1 = (this.width - 192) / 2;
      boolean var2 = true;
      this.buttonNextPage = (GuiScreenBook.NextPageButton)this.addButton(new GuiScreenBook.NextPageButton(1, var1 + 120, 156, true));
      this.buttonPreviousPage = (GuiScreenBook.NextPageButton)this.addButton(new GuiScreenBook.NextPageButton(2, var1 + 38, 156, false));
      this.updateButtons();
   }

   public void onGuiClosed() {
      Keyboard.enableRepeatEvents(false);
   }

   private void updateButtons() {
      this.buttonNextPage.visible = !this.bookGettingSigned && (this.currPage < this.bookTotalPages - 1 || this.bookIsUnsigned);
      this.buttonPreviousPage.visible = !this.bookGettingSigned && this.currPage > 0;
      this.buttonDone.visible = !this.bookIsUnsigned || !this.bookGettingSigned;
      if (this.bookIsUnsigned) {
         this.buttonSign.visible = !this.bookGettingSigned;
         this.buttonCancel.visible = this.bookGettingSigned;
         this.buttonFinalize.visible = this.bookGettingSigned;
         this.buttonFinalize.enabled = !this.bookTitle.trim().isEmpty();
      }

   }

   private void sendBookToServer(boolean var1) throws IOException {
      if (this.bookIsUnsigned && this.bookIsModified && this.bookPages != null) {
         while(this.bookPages.tagCount() > 1) {
            String var2 = this.bookPages.getStringTagAt(this.bookPages.tagCount() - 1);
            if (!var2.isEmpty()) {
               break;
            }

            this.bookPages.removeTag(this.bookPages.tagCount() - 1);
         }

         if (this.bookObj.hasTagCompound()) {
            NBTTagCompound var4 = this.bookObj.getTagCompound();
            var4.setTag("pages", this.bookPages);
         } else {
            this.bookObj.setTagInfo("pages", this.bookPages);
         }

         String var5 = "MC|BEdit";
         if (var1) {
            var5 = "MC|BSign";
            this.bookObj.setTagInfo("author", new NBTTagString(this.editingPlayer.getName()));
            this.bookObj.setTagInfo("title", new NBTTagString(this.bookTitle.trim()));
         }

         PacketBuffer var3 = new PacketBuffer(Unpooled.buffer());
         var3.writeItemStack(this.bookObj);
         this.mc.getConnection().sendPacket(new CPacketCustomPayload(var5, var3));
      }

   }

   protected void actionPerformed(GuiButton var1) throws IOException {
      if (var1.enabled) {
         if (var1.id == 0) {
            this.mc.displayGuiScreen((GuiScreen)null);
            this.sendBookToServer(false);
         } else if (var1.id == 3 && this.bookIsUnsigned) {
            this.bookGettingSigned = true;
         } else if (var1.id == 1) {
            if (this.currPage < this.bookTotalPages - 1) {
               ++this.currPage;
            } else if (this.bookIsUnsigned) {
               this.addNewPage();
               if (this.currPage < this.bookTotalPages - 1) {
                  ++this.currPage;
               }
            }
         } else if (var1.id == 2) {
            if (this.currPage > 0) {
               --this.currPage;
            }
         } else if (var1.id == 5 && this.bookGettingSigned) {
            this.sendBookToServer(true);
            this.mc.displayGuiScreen((GuiScreen)null);
         } else if (var1.id == 4 && this.bookGettingSigned) {
            this.bookGettingSigned = false;
         }

         this.updateButtons();
      }

   }

   private void addNewPage() {
      if (this.bookPages != null && this.bookPages.tagCount() < 50) {
         this.bookPages.appendTag(new NBTTagString(""));
         ++this.bookTotalPages;
         this.bookIsModified = true;
      }

   }

   protected void keyTyped(char var1, int var2) throws IOException {
      super.keyTyped(var1, var2);
      if (this.bookIsUnsigned) {
         if (this.bookGettingSigned) {
            this.keyTypedInTitle(var1, var2);
         } else {
            this.keyTypedInBook(var1, var2);
         }
      }

   }

   private void keyTypedInBook(char var1, int var2) {
      if (GuiScreen.isKeyComboCtrlV(var2)) {
         this.pageInsertIntoCurrent(GuiScreen.getClipboardString());
      } else {
         switch(var2) {
         case 14:
            String var3 = this.pageGetCurrent();
            if (!var3.isEmpty()) {
               this.pageSetCurrent(var3.substring(0, var3.length() - 1));
            }

            return;
         case 28:
         case 156:
            this.pageInsertIntoCurrent("\n");
            return;
         default:
            if (ChatAllowedCharacters.isAllowedCharacter(var1)) {
               this.pageInsertIntoCurrent(Character.toString(var1));
            }
         }
      }

   }

   private void keyTypedInTitle(char var1, int var2) throws IOException {
      switch(var2) {
      case 14:
         if (!this.bookTitle.isEmpty()) {
            this.bookTitle = this.bookTitle.substring(0, this.bookTitle.length() - 1);
            this.updateButtons();
         }

         return;
      case 28:
      case 156:
         if (!this.bookTitle.isEmpty()) {
            this.sendBookToServer(true);
            this.mc.displayGuiScreen((GuiScreen)null);
         }

         return;
      default:
         if (this.bookTitle.length() < 16 && ChatAllowedCharacters.isAllowedCharacter(var1)) {
            this.bookTitle = this.bookTitle + Character.toString(var1);
            this.updateButtons();
            this.bookIsModified = true;
         }

      }
   }

   private String pageGetCurrent() {
      return this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount() ? this.bookPages.getStringTagAt(this.currPage) : "";
   }

   private void pageSetCurrent(String var1) {
      if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount()) {
         this.bookPages.set(this.currPage, new NBTTagString(var1));
         this.bookIsModified = true;
      }

   }

   private void pageInsertIntoCurrent(String var1) {
      String var2 = this.pageGetCurrent();
      String var3 = var2 + var1;
      int var4 = this.fontRendererObj.splitStringWidth(var3 + "" + TextFormatting.BLACK + "_", 118);
      if (var4 <= 128 && var3.length() < 256) {
         this.pageSetCurrent(var3);
      }

   }

   public void drawScreen(int var1, int var2, float var3) {
      GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      this.mc.getTextureManager().bindTexture(BOOK_GUI_TEXTURES);
      int var4 = (this.width - 192) / 2;
      boolean var5 = true;
      this.drawTexturedModalRect(var4, 2, 0, 0, 192, 192);
      if (this.bookGettingSigned) {
         String var6 = this.bookTitle;
         if (this.bookIsUnsigned) {
            if (this.updateCount / 6 % 2 == 0) {
               var6 = var6 + "" + TextFormatting.BLACK + "_";
            } else {
               var6 = var6 + "" + TextFormatting.GRAY + "_";
            }
         }

         String var7 = I18n.format("book.editTitle");
         int var8 = this.fontRendererObj.getStringWidth(var7);
         this.fontRendererObj.drawString(var7, var4 + 36 + (116 - var8) / 2, 34, 0);
         int var9 = this.fontRendererObj.getStringWidth(var6);
         this.fontRendererObj.drawString(var6, var4 + 36 + (116 - var9) / 2, 50, 0);
         String var10 = I18n.format("book.byAuthor", this.editingPlayer.getName());
         int var11 = this.fontRendererObj.getStringWidth(var10);
         this.fontRendererObj.drawString(TextFormatting.DARK_GRAY + var10, var4 + 36 + (116 - var11) / 2, 60, 0);
         String var12 = I18n.format("book.finalizeWarning");
         this.fontRendererObj.drawSplitString(var12, var4 + 36, 82, 116, 0);
      } else {
         String var14 = I18n.format("book.pageIndicator", this.currPage + 1, this.bookTotalPages);
         String var15 = "";
         if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount()) {
            var15 = this.bookPages.getStringTagAt(this.currPage);
         }

         if (this.bookIsUnsigned) {
            if (this.fontRendererObj.getBidiFlag()) {
               var15 = var15 + "_";
            } else if (this.updateCount / 6 % 2 == 0) {
               var15 = var15 + "" + TextFormatting.BLACK + "_";
            } else {
               var15 = var15 + "" + TextFormatting.GRAY + "_";
            }
         } else if (this.cachedPage != this.currPage) {
            if (ItemWrittenBook.validBookTagContents(this.bookObj.getTagCompound())) {
               try {
                  ITextComponent var16 = ITextComponent.Serializer.jsonToComponent(var15);
                  this.cachedComponents = var16 != null ? GuiUtilRenderComponents.splitText(var16, 116, this.fontRendererObj, true, true) : null;
               } catch (JsonParseException var13) {
                  this.cachedComponents = null;
               }
            } else {
               TextComponentString var17 = new TextComponentString(TextFormatting.DARK_RED + "* Invalid book tag *");
               this.cachedComponents = Lists.newArrayList(var17);
            }

            this.cachedPage = this.currPage;
         }

         int var18 = this.fontRendererObj.getStringWidth(var14);
         this.fontRendererObj.drawString(var14, var4 - var18 + 192 - 44, 18, 0);
         if (this.cachedComponents == null) {
            this.fontRendererObj.drawSplitString(var15, var4 + 36, 34, 116, 0);
         } else {
            int var19 = Math.min(128 / this.fontRendererObj.FONT_HEIGHT, this.cachedComponents.size());

            for(int var20 = 0; var20 < var19; ++var20) {
               ITextComponent var22 = (ITextComponent)this.cachedComponents.get(var20);
               this.fontRendererObj.drawString(var22.getUnformattedText(), var4 + 36, 34 + var20 * this.fontRendererObj.FONT_HEIGHT, 0);
            }

            ITextComponent var21 = this.getClickedComponentAt(var1, var2);
            if (var21 != null) {
               this.handleComponentHover(var21, var1, var2);
            }
         }
      }

      super.drawScreen(var1, var2, var3);
   }

   protected void mouseClicked(int var1, int var2, int var3) throws IOException {
      if (var3 == 0) {
         ITextComponent var4 = this.getClickedComponentAt(var1, var2);
         if (var4 != null && this.handleComponentClick(var4)) {
            return;
         }
      }

      super.mouseClicked(var1, var2, var3);
   }

   protected boolean handleComponentClick(ITextComponent var1) {
      ClickEvent var2 = var1.getStyle().getClickEvent();
      if (var2 == null) {
         return false;
      } else if (var2.getAction() == ClickEvent.Action.CHANGE_PAGE) {
         String var6 = var2.getValue();

         try {
            int var4 = Integer.parseInt(var6) - 1;
            if (var4 >= 0 && var4 < this.bookTotalPages && var4 != this.currPage) {
               this.currPage = var4;
               this.updateButtons();
               return true;
            }
         } catch (Throwable var5) {
            ;
         }

         return false;
      } else {
         boolean var3 = super.handleComponentClick(var1);
         if (var3 && var2.getAction() == ClickEvent.Action.RUN_COMMAND) {
            this.mc.displayGuiScreen((GuiScreen)null);
         }

         return var3;
      }
   }

   @Nullable
   public ITextComponent getClickedComponentAt(int var1, int var2) {
      if (this.cachedComponents == null) {
         return null;
      } else {
         int var3 = var1 - (this.width - 192) / 2 - 36;
         int var4 = var2 - 2 - 16 - 16;
         if (var3 >= 0 && var4 >= 0) {
            int var5 = Math.min(128 / this.fontRendererObj.FONT_HEIGHT, this.cachedComponents.size());
            if (var3 <= 116 && var4 < this.mc.fontRendererObj.FONT_HEIGHT * var5 + var5) {
               int var6 = var4 / this.mc.fontRendererObj.FONT_HEIGHT;
               if (var6 >= 0 && var6 < this.cachedComponents.size()) {
                  ITextComponent var7 = (ITextComponent)this.cachedComponents.get(var6);
                  int var8 = 0;

                  for(ITextComponent var10 : var7) {
                     if (var10 instanceof TextComponentString) {
                        var8 += this.mc.fontRendererObj.getStringWidth(((TextComponentString)var10).getText());
                        if (var8 > var3) {
                           return var10;
                        }
                     }
                  }
               }

               return null;
            } else {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   @SideOnly(Side.CLIENT)
   static class NextPageButton extends GuiButton {
      private final boolean isForward;

      public NextPageButton(int var1, int var2, int var3, boolean var4) {
         super(var1, var2, var3, 23, 13, "");
         this.isForward = var4;
      }

      public void drawButton(Minecraft var1, int var2, int var3) {
         if (this.visible) {
            boolean var4 = var2 >= this.xPosition && var3 >= this.yPosition && var2 < this.xPosition + this.width && var3 < this.yPosition + this.height;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            var1.getTextureManager().bindTexture(GuiScreenBook.BOOK_GUI_TEXTURES);
            int var5 = 0;
            int var6 = 192;
            if (var4) {
               var5 += 23;
            }

            if (!this.isForward) {
               var6 += 13;
            }

            this.drawTexturedModalRect(this.xPosition, this.yPosition, var5, var6, 23, 13);
         }

      }
   }
}
