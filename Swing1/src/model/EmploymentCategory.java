package model;

public enum EmploymentCategory {
	employed("Employed"),
	selfEmployed("Self-employed"),
	unemployed("Unemployed"),
	other("Other");
	
	private String text;
	
	private EmploymentCategory(String text) {
		this.text = text;
	}
	
	public String toString() {
		return text;
	}
}
