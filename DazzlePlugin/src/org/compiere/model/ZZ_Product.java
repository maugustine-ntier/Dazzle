package org.compiere.model;

import java.sql.ResultSet;
import java.util.Properties;

public class ZZ_Product extends MProduct implements I_ZZ_Product
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ZZ_Product(MExpenseType et) {
		super(et);
	}

	public ZZ_Product(MResource resource, MResourceType resourceType) {
		super(resource, resourceType);
	}

	public ZZ_Product(Properties ctx, int M_Product_ID, String trxName) {
		super(ctx, M_Product_ID, trxName);
	}

	public ZZ_Product(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	public ZZ_Product(X_I_Product impP) {
		super(impP);
	}
	
	/** Set Logo.
	@param Logo_ID Logo	  */
	public void setAD_Image_ID (int AD_Image_ID)
	{
		if (AD_Image_ID < 1) 
			set_Value (COLUMNNAME_AD_Image_ID, null);
		else 
			set_Value (COLUMNNAME_AD_Image_ID, Integer.valueOf(AD_Image_ID));
	}
	
	/** Get Logo.
		@return Logo	  */
	public int getAD_Image_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_AD_Image_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	
}	//	ZZ_Product
