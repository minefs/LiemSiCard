package net.minefs.DoiCard.Gui;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.minefs.DoiCard.NapRequest;
import net.minefs.DoiCard.Telco;
import net.minefs.GuiAPI.GuiInventory;
import net.minefs.GuiAPI.GuiItemStack;

public class AmountSelector extends GuiInventory {

//	private Telco telco;

	public AmountSelector(Telco telco) {
		super(9, "§a§lChọn mệnh giá");
//		this.telco = telco;
		int[] amounts = { 10000, 20000, 30000, 50000, 100000, 200000, 300000, 500000 };
		int i = 0;
		for (int amount : amounts) {
			GuiItemStack item = new GuiItemStack(Material.PAPER, "§b§l" + amount + "đ", "§eClick để bắt đầu nạp",
					"§ethẻ " + telco.getName() + " mệnh giá " + amount + "đ", "§c§lChọn sai mệnh giá sẽ mất thẻ.") {
				@Override
				public void onClick(InventoryClickEvent e) {
					e.setCancelled(true);
					Player p = (Player) e.getWhoClicked();
					p.closeInventory();
					new NapRequest(p, telco, amount);
				}
			};
			setItem(i++, item);
		}
	}
}
