package org.compiere.model;

import java.sql.Timestamp;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.Env;

public class TaxCustom {


	/** Logger */
	private static CLogger log = CLogger.getCLogger(TaxCustom.class);
	
	public static int get (Properties ctx,
			int C_TaxCategory_ID, boolean IsSOTrx,
			Timestamp shipDate, int shipFromC_Location_ID, int shipToC_Location_ID,
			Timestamp billDate, int billFromC_Location_ID, int billToC_Location_ID)
		{
		   return TaxMA.get(ctx, C_TaxCategory_ID, IsSOTrx, shipDate,
				   shipFromC_Location_ID, shipToC_Location_ID, billDate, billFromC_Location_ID, billToC_Location_ID);
		}

	
	/**
	 * @author NCG
	 * Return the Tax ID given the C_TaxCategory_ID
	 */
	public static int get(Properties m_ctx, int C_TaxCategory_ID) {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		
		MOrg org = new MOrg(m_ctx, Env.getAD_Org_ID(m_ctx), null);
		MOrgInfo mOrgInfo = MOrgInfo.get(m_ctx, org.getAD_Org_ID(),null);
		int tax_ID = TaxCustom.get(m_ctx, C_TaxCategory_ID, true,
				new Timestamp(System.currentTimeMillis()), mOrgInfo
						.getC_Location_ID(), mOrgInfo
						.getC_Location_ID(),
				new Timestamp(System.currentTimeMillis()), mOrgInfo
						.getC_Location_ID(), mOrgInfo
						.getC_Location_ID());
		return tax_ID;

	}
}
