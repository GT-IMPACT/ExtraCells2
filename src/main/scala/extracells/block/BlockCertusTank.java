package extracells.block;

import appeng.api.implementations.items.IAEWrench;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.common.Optional;
import extracells.network.ChannelHandler;
import extracells.registries.BlockEnum;
import extracells.render.RenderHandler;
import extracells.tileentity.TileEntityCertusTank;
import gregtech.api.items.GT_MetaGenerated_Tool;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.Loader;

@Optional.Interface(iface = "gregtech.api.items.GT_MetaGenerated_Tool", modid = "gregtech")
public class BlockCertusTank extends BlockEC {

	IIcon breakIcon;
	IIcon topIcon;
	IIcon bottomIcon;
	IIcon sideIcon;
	IIcon sideMiddleIcon;
	IIcon sideTopIcon;
	IIcon sideBottomIcon;

	public BlockCertusTank() {
		super(Material.glass, 2.0F, 10.0F);
		setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 1.0F, 0.9375F);
	}

	@Override
	public boolean canRenderInPass(int pass) {
		RenderHandler.renderPass = pass;
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileEntityCertusTank();
	}

	public ItemStack getDropWithNBT(World world, int x, int y, int z) {
		NBTTagCompound tileEntity = new NBTTagCompound();
		TileEntity worldTE = world.getTileEntity(x, y, z);
		if (worldTE != null && worldTE instanceof TileEntityCertusTank) {
			ItemStack dropStack = new ItemStack(
					BlockEnum.CERTUSTANK.getBlock(), 1);

			((TileEntityCertusTank) worldTE)
					.writeToNBTWithoutCoords(tileEntity);

			if (!tileEntity.hasKey("Empty")) {
				dropStack.setTagCompound(new NBTTagCompound());
				dropStack.stackTagCompound.setTag("tileEntity", tileEntity);
			}
			return dropStack;

		}
		return null;
	}

	@Override
	public IIcon getIcon(int side, int b) {
		switch (b) {
			case 1:
				return this.sideTopIcon;
			case 2:
				return this.sideBottomIcon;
			case 3:
				return this.sideMiddleIcon;
			default:
				return side == 0 ? this.bottomIcon : side == 1 ? this.topIcon
						: this.sideIcon;
		}
	}

	@Override
	public String getLocalizedName() {
		return StatCollector.translateToLocal(getUnlocalizedName() + ".name");
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {
		return getDropWithNBT(world, x, y, z);
	}

	@Override
	public int getRenderBlockPass() {
		return 1;
	}

	@Override
	public int getRenderType() {
		return RenderHandler.getId();
	}

	@Override
	public String getUnlocalizedName() {
		return super.getUnlocalizedName().replace("tile.", "");
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Optional.Method(modid = "gregtech")
	public boolean gtWrench(ItemStack current, World worldObj, int x, int y, int z, EntityPlayer entityplayer) {
		if (current.getItem() instanceof GT_MetaGenerated_Tool && ((GT_MetaGenerated_Tool) current.getItem()).canWrench(entityplayer, x, y, z)) {
			dropBlockAsItem(worldObj, x, y, z, getDropWithNBT(worldObj, x, y, z));
			worldObj.setBlockToAir(x, y, z);
			((GT_MetaGenerated_Tool) current.getItem()).wrenchUsed(entityplayer, x, y, z);
			return true;
		}
		return false;
	}

	@Override
	public boolean onBlockActivated(World worldObj, int x, int y, int z,
									EntityPlayer entityplayer, int blockID, float offsetX,
									float offsetY, float offsetZ) {
		ItemStack current = entityplayer.inventory.getCurrentItem();

		if (entityplayer.isSneaking() && current != null) {
			try {
				if (current.getItem() instanceof IToolWrench && ((IToolWrench) current.getItem()).canWrench(entityplayer, x, y, z)) {
					dropBlockAsItem(worldObj, x, y, z, getDropWithNBT(worldObj, x, y, z));
					worldObj.setBlockToAir(x, y, z);
					((IToolWrench) current.getItem()).wrenchUsed(entityplayer, x, y, z);
					return true;
				}
			} catch (Throwable e) {
				// No IToolWrench
			}
			if (current.getItem() instanceof IAEWrench && ((IAEWrench) current.getItem()).canWrench(current, entityplayer, x, y, z)) {
				dropBlockAsItem(worldObj, x, y, z, getDropWithNBT(worldObj, x, y, z));
				worldObj.setBlockToAir(x, y, z);
				return true;
			}
			try {
				if (gtWrench(current, worldObj, x, y, z, entityplayer)) {
					return true;
				}
			} catch (Throwable e) {
				// No GT_MetaGenerated_Tool
			}

		}
		if (current != null) {
			FluidStack liquid = FluidContainerRegistry.getFluidForFilledItem(current);
			TileEntityCertusTank tank = (TileEntityCertusTank) worldObj.getTileEntity(x, y, z);

			if (liquid != null) {
				int amountFilled = tank.fill(ForgeDirection.UNKNOWN, liquid, true);

				if (amountFilled != 0
						&& !entityplayer.capabilities.isCreativeMode) {
					ItemStack emptyContainer = current.getItem().getContainerItem(current);
					if  (emptyContainer == null && Loader.isModLoaded("IC2")) {
						if (current.getItem() == GameRegistry.findItem("IC2", "itemCellEmpty")) {
							emptyContainer = new ItemStack(GameRegistry.findItem("IC2", "itemCellEmpty"), 1, 0);
						}
					}
					if (current.stackSize > 1) {
						entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem].stackSize -= 1;
						entityplayer.inventory.addItemStackToInventory(emptyContainer);
					} else {
						entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = emptyContainer;
					}
				}

				return true;

				// Handle empty containers
			} else {

				FluidStack available = tank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid;
				if (available != null) {
					ItemStack filled = FluidContainerRegistry.fillFluidContainer(available, current);

					liquid = FluidContainerRegistry.getFluidForFilledItem(filled);

					if (liquid != null) {
						if (!entityplayer.capabilities.isCreativeMode) {
							if (current.stackSize > 1) {
								if (!entityplayer.inventory.addItemStackToInventory(filled)) {
									return false;
								} else {
									entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem].stackSize -= 1;
								}
							} else {
								entityplayer.inventory.mainInventory[entityplayer.inventory.currentItem] = filled;
							}
						}
						tank.drain(ForgeDirection.UNKNOWN, liquid.amount, true);
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z,
									  Block neighborBlock) {
		if (!world.isRemote) {

			ChannelHandler.sendPacketToAllPlayers(world.getTileEntity(x, y, z).getDescriptionPacket(), world);
		}
	}

	@Override
	public void registerBlockIcons(IIconRegister iconregister) {
		this.breakIcon = iconregister.registerIcon("extracells:certustank");
		this.topIcon = iconregister.registerIcon("extracells:CTankTop");
		this.bottomIcon = iconregister.registerIcon("extracells:CTankBottom");
		this.sideIcon = iconregister.registerIcon("extracells:CTankSide");
		this.sideMiddleIcon = iconregister.registerIcon("extracells:CTankSideMiddle");
		this.sideTopIcon = iconregister.registerIcon("extracells:CTankSideTop");
		this.sideBottomIcon = iconregister.registerIcon("extracells:CTankSideBottom");
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
}