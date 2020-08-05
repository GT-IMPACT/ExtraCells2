package extracells.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiImgButton extends GuiButton {

    protected static final ResourceLocation buttonTextures = new ResourceLocation("extracells", "textures/gui/states.png");
    private int xPic, yPic;

    public GuiImgButton(int index, int xPic, int yPic, int xGui, int yGui, String name) {
        super(index, 0, 16, "");

        this.xPosition = xGui;
        this.yPosition = yGui;
        this.width = 16;
        this.height = 16;
        this.xPic = xPic;
        this.yPic = yPic;

    }

    @Override
    public void drawButton(Minecraft mc, int par2, int par3) {
        if (this.visible) {
            if (this.enabled) GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            else GL11.glColor4f(0.5f, 0.5f, 0.5f, 1.0f);

            mc.renderEngine.bindTexture(buttonTextures);
            this.field_146123_n = par2 >= this.xPosition && par3 >= this.yPosition && par2 < this.xPosition + this.width && par3 < this.yPosition + this.height;

            this.drawTexturedModalRect(this.xPosition, this.yPosition, 32, 0, 16, 16);
            this.drawTexturedModalRect(this.xPosition, this.yPosition, xPic, yPic, 16, 16);
            this.mouseDragged(mc, par2, par3);
        }
        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }

}
