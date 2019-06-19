package net.minefs.DoiCard.Gui;

import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.minefs.DoiCard.Telco;
import net.minefs.GuiAPI.GuiInventory;
import net.minefs.GuiAPI.GuiItemStack;

public class TelcoSelector extends GuiInventory {

	public TelcoSelector() {
		super(9, "§a§l Chọn loại thẻ");
		int a = 0;
		for (Telco telco : Telco.values()) {
			GuiItemStack item = new GuiItemStack(Material.PAPER, "§b§l" + telco.getName(), "§aClick để bắt đầu nạp",
					"§athẻ §b" + telco.getName()) {
				@Override
				public void onClick(InventoryClickEvent e) {
					e.setCancelled(true);
					e.getWhoClicked().openInventory(new AmountSelector(telco).getInventory());
				}
			};
			setItem(a++, item);
		}
	}

}
