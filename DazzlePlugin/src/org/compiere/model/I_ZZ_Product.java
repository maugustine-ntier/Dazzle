package org.compiere.model;

/**
 * @author NCG
 * Date: 16 Apr 2015
 * Description:	
 */
public interface I_ZZ_Product extends I_M_Product {
	
	/** Column name Logo_ID */
    public static final String COLUMNNAME_AD_Image_ID = "AD_Image_ID";

	/** Set Image	  */
	public void setAD_Image_ID (int AD_Image_ID);

	/** Get Image	  */
	public int getAD_Image_ID();
	
}
