package net.minefs.DoiCard;

public enum Telco {

	MOBIFONE("Mobifone"), VIETTEL("Viettel"), VINAPHONE("Vinaphone");

	private String name;

	Telco(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
