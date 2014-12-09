package moreinventory.tileentity;

import moreinventory.core.MoreInventoryMod;
import moreinventory.network.CatchallMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;

public class TileEntityCatchall extends TileEntity implements IInventory
{
	private ItemStack[] containerItems = new ItemStack[36];
	private ItemStack[] displayedItem = new ItemStack[1];

	@Override
	public int getSizeInventory()
	{
		return containerItems.length;
	}

	@Override
	public ItemStack getStackInSlot(int slot)
	{
		return containerItems[slot];
	}

	public ItemStack[] getDisplayedItem()
	{
		return displayedItem;
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack itemstack)
	{
		containerItems[slot] = itemstack;

		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}

		sendPacket();
		markDirty();
	}

	@Override
	public String getInventoryName()
	{
		return "TileEntityCatchall";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount)
	{
		ItemStack itemstack = getStackInSlot(slot);

		if (itemstack != null)
		{
			if (itemstack.stackSize <= amount)
			{
				setInventorySlotContents(slot, null);
			}
			else
			{
				itemstack = itemstack.splitStack(amount);

				if (itemstack.stackSize == 0)
				{
					setInventorySlotContents(slot, null);
				}
			}
		}

		markDirty();

		return itemstack;
	}

	public void transferToBlock(EntityPlayer player)
	{
		InventoryPlayer inventory = player.inventory;

		for (int i = 0; i < getSizeInventory(); i++)
		{
			setInventorySlotContents(i, inventory.getStackInSlot(i));

			inventory.mainInventory[i] = null;
		}

		player.onUpdate();
	}

	public void transferToPlayer(EntityPlayer player)
	{
		InventoryPlayer inventory = player.inventory;

		for (int i = 0; i < getSizeInventory(); i++)
		{
			ItemStack itemstack = getStackInSlot(i);

			if (itemstack != null)
			{
				inventory.mainInventory[i] = itemstack;
			}

			setInventorySlotContents(i, null);
		}

		player.onUpdate();
	}

	public boolean transferTo(EntityPlayer player)
	{
		if (!isFilled())
		{
			transferToBlock(player);

			return true;
		}

		if (canTransferToPlayer(player))
		{
			transferToPlayer(player);

			return true;
		}

		return false;
	}

	public boolean isFilled()
	{
		for (int i = 0; i < containerItems.length; i++)
		{
			if (getStackInSlot(i) != null)
			{
				return true;
			}
		}

		return false;
	}

	public boolean canTransferToPlayer(EntityPlayer player)
	{
		for (int i = 0; i < getSizeInventory(); i++)
		{
			if (getStackInSlot(i) != null && player.inventory.mainInventory[i] != null)
			{
				return false;
			}
		}

		return true;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot)
	{
		ItemStack itemstack = getStackInSlot(slot);

		if (itemstack != null)
		{
			setInventorySlotContents(slot, null);
		}

		return itemstack;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player)
	{
		return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this && player.getDistanceSq(xCoord + 0.5, yCoord + 0.5, zCoord + 0.5) < 64;
	}

	@Override
	public void openInventory() {}

	@Override
	public void closeInventory() {}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		NBTTagList list = nbt.getTagList("Items", 10);
		containerItems = new ItemStack[getSizeInventory()];

		for (int i = 0; i < list.tagCount(); ++i)
		{
			NBTTagCompound data = list.getCompoundTagAt(i);
			int slot = data.getByte("Slot") & 255;

			if (slot >= 0 && slot < containerItems.length)
			{
				containerItems[slot] = ItemStack.loadItemStackFromNBT(data);
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		NBTTagList list = new NBTTagList();

		for (int i = 0; i < containerItems.length; ++i)
		{
			if (containerItems[i] != null)
			{
				NBTTagCompound data = new NBTTagCompound();
				data.setByte("Slot", (byte) i);
				containerItems[i].writeToNBT(data);
				list.appendTag(data);
			}
		}

		nbt.setTag("Items", list);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}

	public void handlePacketData(ItemStack[] items)
	{
		displayedItem = items;
	}

	@Override
	public Packet getDescriptionPacket()
	{
		sendPacket();

		return null;
	}

	private void sendPacket()
	{
		MoreInventoryMod.network.sendToDimension(new CatchallMessage(xCoord, yCoord, zCoord, containerItems), worldObj.provider.dimensionId);
	}
}