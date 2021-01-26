package org.adempiere.webui.apps.form;

public class WObjRet {
	private String value = "";
	private int IDOfRecord = 0;
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public int getIDOfRecord() {
		return IDOfRecord;
	}
	public void setIDOfRecord(int iDOfRecord) {
		IDOfRecord = iDOfRecord;
	}
	@Override
	public String toString() {
		return getValue();
	}


}
