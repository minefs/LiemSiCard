package net.minefs.DoiCard;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;

import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.plugin.java.JavaPlugin;

import net.minefs.DoiCard.Gui.TelcoSelector;
import net.minefs.GuiAPI.GuiInventory;

public class DoiCard extends JavaPlugin {

	private String server, id, key, user, host, db, pass;
	private int port;
	private static DoiCard _instance;
	private PlayerPoints pp;
	private double tile;

	@Override
	public void onEnable() {
		pp = (PlayerPoints) getServer().getPluginManager().getPlugin("PlayerPoints");
		_instance = this;
		saveDefaultConfig();
		try {
			loadConfig();
			new MySQL(host, port, user, pass, db);
			Bukkit.getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler
				public void onClick(InventoryClickEvent e) {
					Inventory inv = e.getClickedInventory();
					if (inv != null && inv.getHolder() != null && inv.getHolder() instanceof GuiInventory)
						((GuiInventory) inv.getHolder()).onClick(e);
				}

				@EventHandler(priority = EventPriority.HIGHEST)
				public void onChat(AsyncPlayerChatEvent e) {
					NapRequest request = NapRequest.getRequests().get(e.getPlayer().getName());
					if (request != null) {
						e.setCancelled(true);
						request.onChat(e.getMessage());
					}
				}
			}, this);
		} catch (Exception e) {
			getLogger().log(Level.SEVERE, e.getMessage(), e);
			getServer().getPluginManager().disablePlugin(this);
		}
	}

	@Override
	public void onDisable() {
		MySQL sql = MySQL.getInstance();
		if (sql != null) {
			MySQL.getInstance().close();
		}
		getLogger().info("Dong inventory...");
		Bukkit.getOnlinePlayers().forEach(p -> {
			InventoryView view = p.getOpenInventory();
			if (view == null)
				return;
			Inventory inv = view.getTopInventory();
			if (inv != null && inv.getHolder() != null && inv.getHolder() instanceof GuiInventory)
				p.closeInventory();
		});
	}

	@SuppressWarnings("rawtypes")
	public String createRequestUrl(Map<String, String> map) {
		String url_params = "";
		for (Map.Entry entry : map.entrySet()) {
			if (url_params == "")
				url_params += entry.getKey() + "=" + entry.getValue();
			else
				url_params += "&" + entry.getKey() + "=" + entry.getValue();
		}
		return url_params;
	}

	public String get(String urlz, String info) {
		String stuff = "";
		try {
			byte[] postData = info.getBytes(StandardCharsets.UTF_8);
			int postDataLength = postData.length;
			URL url = new URL(urlz);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setInstanceFollowRedirects(false);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("Referer", "http://grassminevn.com/");
			conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
			conn.setUseCaches(false);
			try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
				wr.write(postData);
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			stuff = in.readLine();
			if (conn.getResponseCode() != 200) {
				return "error " + conn.getResponseCode();
			}
			return stuff;
		} catch (IOException e) {
			e.printStackTrace();
			return stuff;
		}
	}

	public void loadConfig() {
		server = getConfig().getString("server");
		id = getConfig().getString("id");
		key = getConfig().getString("key");
		tile = getConfig().getDouble("tile");
		user = getConfig().getString("mysql.user");
		host = getConfig().getString("mysql.host");
		db = getConfig().getString("mysql.database");
		pass = getConfig().getString("mysql.password");
		port = getConfig().getInt("mysql.port");
	}

	public String getServerName() {
		return server;
	}

	public static DoiCard getInstance() {
		return _instance;
	}

	public String getID() {
		return id;
	}

	public String getKey() {
		return key;
	}

	public double getTile() {
		return tile;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
			reloadConfig();
			loadConfig();
			new MySQL(host, port, user, pass, db);
			sender.sendMessage("§aLiemSiCard: Đã nạp lại liêm sỉ.");
			return true;
		}
		if (sender instanceof Player) {
			((Player) sender).openInventory(new TelcoSelector().getInventory());
		}
		return true;
	}

	public PlayerPoints getPlayerPointsIns() {
		return pp;
	}
}
