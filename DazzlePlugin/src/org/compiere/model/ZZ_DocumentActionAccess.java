package org.compiere.model;

import java.sql.ResultSet;
import java.util.Properties;

import za.co.ntier.common.NTierUtils;

/**
 * @author Neil Gordon nTier Software Services
 * http://www.ntier.co.za
 * Date: 29 Apr 2015
 * Description:	
 */
public class ZZ_DocumentActionAccess extends X_AD_Document_Action_Access {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public ZZ_DocumentActionAccess(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	public ZZ_DocumentActionAccess(Properties ctx,
			int AD_Document_Action_Access_ID, String trxName) {
		super(ctx, AD_Document_Action_Access_ID, trxName);
	}

	public static boolean isDocActionAccess(Properties ctx, String trxName,
			int AD_Role_ID, String docTypeName, String docAction) {
		MRefList refList = MRefList.get(ctx, 135 /* _Document Action */, 
				docAction, trxName);
		int C_DocType_ID = NTierUtils.getDocTypeIDForName(ctx, trxName, docTypeName);
		X_AD_Document_Action_Access po = new Query(ctx, I_AD_Document_Action_Access.Table_Name,
				"AD_Role_ID=? and C_DocType_ID=? and AD_Ref_List_ID=?", trxName).setParameters(
						AD_Role_ID, C_DocType_ID, refList.get_ID()).first();
		if ( po==null ) return false;
		return po.isActive();
	}
	

}
