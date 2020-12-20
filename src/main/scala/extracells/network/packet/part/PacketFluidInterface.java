package extracells.network.packet.part;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerFluidInterface;
import extracells.gui.GuiFluidInterface;
import extracells.network.AbstractPacket;
import extracells.util.GuiUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import java.io.DataInput;
import java.io.DataOutput;

public class PacketFluidInterface extends AbstractPacket {

    FluidStack[] tank;
    String[] filter;

    public PacketFluidInterface() {
    }

    public PacketFluidInterface(FluidStack[] tank, String[] filter) {
        this.tank = tank;
        this.filter = filter;
    }

    @Override
    public void execute() {
    }

    @SideOnly(Side.CLIENT)
    private void mode0() {
        EntityPlayer p = Minecraft.getMinecraft().thePlayer;
        if (p.openContainer != null && p.openContainer instanceof ContainerFluidInterface) {
            ContainerFluidInterface container = (ContainerFluidInterface) p.openContainer;
            if (Minecraft.getMinecraft().currentScreen != null && Minecraft.getMinecraft().currentScreen instanceof GuiFluidInterface) {
                GuiFluidInterface gui = (GuiFluidInterface) Minecraft.getMinecraft().currentScreen;
                for (int i = 0; i < this.tank.length; i++) {
                    container.fluidInterface.setFluidTank(ForgeDirection.getOrientation(i), this.tank[i]);
                }
                for (int i = 0; i < this.filter.length; i++) {
                    if (gui.filter[i] != null)
                    	gui.filter[i].setFluid(FluidRegistry.getFluid(this.filter[i]));
                }
            }
        }
    }

    @Override
    public void readData(ByteBuf in) {
        NBTTagCompound tag = ByteBufUtils.readTag(in);
		FluidStack[] tank = new FluidStack[tag.getInteger("lengthTank")];
		for (int i = 0; i < tank.length; i++) {
			if (tag.hasKey("tank#" + i)) {
				tank[i] = FluidStack.loadFluidStackFromNBT(tag
						.getCompoundTag("tank#" + i));
			} else {
				tank[i] = null;
			}
		}
		String[] filter = new String[tag.getInteger("lengthFilter")];
		for (int i = 0; i < filter.length; i++) {
			if (tag.hasKey("filter#" + i)) {
				filter[i] = tag.getString("filter#" + i);
			} else {
				filter[i] = "";
			}
		}

		GuiFluidInterface gui = GuiUtil.getGui(GuiFluidInterface.class);
		ContainerFluidInterface container = GuiUtil.getContainer(gui, ContainerFluidInterface.class);
		if (container == null) {
			return;
		}
		for (int i = 0; i < tank.length; i++) {
			container.fluidInterface.setFluidTank(ForgeDirection.getOrientation(i), tank[i]);
		}
		for (int i = 0; i < filter.length; i++) {
			if (gui.filter[i] != null) {
				gui.filter[i].setFluid(FluidRegistry.getFluid(filter[i]));
			}
		}

    }

    @Override
    public void writeData(ByteBuf out) {

        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("lengthTank", this.tank.length);
        for (int i = 0; i < this.tank.length; i++) {
            if (this.tank[i] != null) {
                tag.setTag("tank#" + i,
                        this.tank[i].writeToNBT(new NBTTagCompound()));
            }
        }
        tag.setInteger("lengthFilter", this.filter.length);
        for (int i = 0; i < this.filter.length; i++) {
            if (this.filter[i] != null) {
                tag.setString("filter#" + i, this.filter[i]);
            }
        }

        ByteBufUtils.writeTag(out, tag);
    }

}
