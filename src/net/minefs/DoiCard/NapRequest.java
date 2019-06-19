package net.minefs.DoiCard;

import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class NapRequest {

	private static Map<String, NapRequest> _requests;

	public static Map<String, NapRequest> getRequests() {
		if (_requests == null)
			_requests = new ConcurrentHashMap<String, NapRequest>();
		return _requests;
	}

	private Player p;
	private String seri = null, code = null;
	private int amount;
	private Telco telco;

	public NapRequest(Player p, Telco telco, int amount) {
		this.p = p;
		this.amount = amount;
		this.telco = telco;
		getRequests().put(p.getName(), this);
		p.sendMessage("§b§lLoại thẻ: §a§l" + telco.getName() + "§b§l, mệnh giá: §a§l" + amount);
		p.sendMessage("§b§lBây giờ hãy nhập số seri vào khung chat");
	}

	public void setSeri(String seri) {
		this.seri = seri;
	}

	public String getSeri() {
		return seri;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void onChat(String msg) {
		if (seri == null) {
			setSeri(msg);
			p.sendMessage("§b§lSố seri: §a§l" + seri);
			p.sendMessage("§b§lHãy tiếp tục nhập mã thẻ vào khung chat");
			return;
		}
		setCode(msg);
		p.sendMessage("§b§lMã thẻ: §a§l" + code);
		p.sendMessage("§b§lĐang tiến hành nạp thẻ, xin chờ...");
		Map<String, String> mapparams = new HashMap<String, String>();
//        'partner_id' => $partner_id,
//        'request_id' => $request_id,
//        'telco' => $telco,
//        'amount' => $amount,
//        'serial' => $serial,
//        'code' => $code,
//        'sign' => $sign
		DoiCard dc = DoiCard.getInstance();
		MySQL mysql = MySQL.getInstance();
		int requestid = mysql != null ? mysql.log(p, telco.name(), seri, code, amount, 0, 0)
				: (int) (System.currentTimeMillis() / 1000);
		String encrypt = dc.getID() + dc.getKey() + telco.name() + code + seri + amount + requestid;
		// $sign = md5($partner_id . $partner_key . $telco . $code . $serial . $amount .
		// $request_id);
		String md2;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(encrypt.getBytes());
			byte[] byteData = md.digest();
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < byteData.length; ++i) {
				sb.append(Integer.toString((byteData[i] & 0xFF) + 256, 16).substring(1));
			}
			md2 = sb.toString();
		} catch (Exception ex) {
			p.sendMessage("§c§lMã hóa dữ liệu thấy bại, vui lòng liên hệ ban quản trị");
			return;
		}
		mapparams.put("sign", md2);
		mapparams.put("partner_id", dc.getID());
		mapparams.put("request_id", requestid + "");
		mapparams.put("telco", telco.name());
		mapparams.put("amount", amount + "");
		mapparams.put("serial", seri);
		mapparams.put("code", code);
		String url = "https://doicard.vn/chargingws";
		String params = dc.createRequestUrl(mapparams);
		String get = dc.get(url, params);
		if (get == null || get.startsWith("error")) {
			p.sendMessage("§c§lCó lỗi xảy ra, vui lòng thử lại sau.");
			return;
		}
		dc.getLogger().info(get);
		JsonObject root = new JsonParser().parse(get).getAsJsonObject();
		String message = root.get("message").getAsString();
		int ketqua = root.get("status").getAsInt();
		switch (ketqua) {
		case 1:
			int amount = root.get("value").getAsInt();
			int pts = amount / 1000;
			dc.getPlayerPointsIns().getAPI().give(p.getUniqueId(), (int) pts);
			p.sendMessage("§a§lNạp thẻ thành công, bạn nhận được " + pts + " points");
			int thucnhan = root.get("amount").getAsInt();
			if (mysql != null)
				mysql.logSuccess(requestid, pts, thucnhan);
			break;
		case 99:
			p.sendMessage("§a§lThẻ đã nạp thành công và đang chờ Admin duyệt, hãy giữ lại thẻ và báo cho admin biết.");
			break;
		default:
			p.sendMessage("§c§lCó lỗi xảy ra: " + message + " (mã lỗi " + ketqua + ")");
			break;
		}
	}
}
