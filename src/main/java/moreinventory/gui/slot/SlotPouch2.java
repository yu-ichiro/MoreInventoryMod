package moreinventory.gui.slot;

import moreinventory.MoreInventoryMod;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotPouch2 extends Slot{

	public SlotPouch2(IInventory par1iInventory, int par2, int par3, int par4) {
		super(par1iInventory, par2, par3, par4);
	}

    public boolean isItemValid(ItemStack par1ItemStack)
    {
        return par1ItemStack == null || par1ItemStack.getItem() !=  MoreInventoryMod.Pouch;
    }

}