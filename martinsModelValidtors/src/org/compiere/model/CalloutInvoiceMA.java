/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;

/**
 * NCG: nTier: 2015/05/15: RQ100011: After entering the BP on the invoice, the order ref. is blanked out.
 */
public class CalloutInvoiceMA extends CalloutEngine
{

	public String bPartner (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		Integer C_BPartner_ID = (Integer)value;
		if (C_BPartner_ID == null || C_BPartner_ID.intValue() == 0)
			return "";

		String sql = "SELECT p.AD_Language,p.C_PaymentTerm_ID,"
			+ " COALESCE(p.M_PriceList_ID,g.M_PriceList_ID) AS M_PriceList_ID, p.PaymentRule,p.POReference,"
			+ " p.SO_Description,p.IsDiscountPrinted,"
			+ " p.SO_CreditLimit, p.SO_CreditLimit-p.SO_CreditUsed AS CreditAvailable,"
			+ " l.C_BPartner_Location_ID,c.AD_User_ID,"
			+ " COALESCE(p.PO_PriceList_ID,g.PO_PriceList_ID) AS PO_PriceList_ID, p.PaymentRulePO,p.PO_PaymentTerm_ID " 
			+ "FROM C_BPartner p"
			+ " INNER JOIN C_BP_Group g ON (p.C_BP_Group_ID=g.C_BP_Group_ID)"			
			+ " LEFT OUTER JOIN C_BPartner_Location l ON (p.C_BPartner_ID=l.C_BPartner_ID AND l.IsBillTo='Y' AND l.IsActive='Y')"
			+ " LEFT OUTER JOIN AD_User c ON (p.C_BPartner_ID=c.C_BPartner_ID AND c.IsActive='Y') "
			+ "WHERE p.C_BPartner_ID=? AND p.IsActive='Y'";		//	#1

		boolean IsSOTrx = Env.getContext(ctx, WindowNo, "IsSOTrx").equals("Y");
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, C_BPartner_ID.intValue());
			rs = pstmt.executeQuery();
			//
			if (rs.next())
			{
				//	PriceList & IsTaxIncluded & Currency
				Integer ii = new Integer(rs.getInt(IsSOTrx ? "M_PriceList_ID" : "PO_PriceList_ID"));
				if (!rs.wasNull())
					mTab.setValue("M_PriceList_ID", ii);
				else
				{	//	get default PriceList
					int i = Env.getContextAsInt(ctx, "#M_PriceList_ID");
					if (i != 0)
					{
						MPriceList pl = new MPriceList(ctx, i, null);
						if (IsSOTrx == pl.isSOPriceList())
							mTab.setValue("M_PriceList_ID", new Integer(i));
						else
						{
							String sql2 = "SELECT M_PriceList_ID FROM M_PriceList WHERE AD_Client_ID=? AND IsSOPriceList=? AND IsActive='Y' ORDER BY IsDefault DESC";
							ii = DB.getSQLValue (null, sql2, Env.getAD_Client_ID(ctx), IsSOTrx);
							if (ii != 0)
								mTab.setValue("M_PriceList_ID", new Integer(ii));
						}
					}
				}

				//	PaymentRule
				String s = rs.getString(IsSOTrx ? "PaymentRule" : "PaymentRulePO");
				if (s != null && s.length() != 0)
				{
					if (Env.getContext(ctx, WindowNo, "DocBaseType").endsWith("C"))	//	Credits are Payment Term
						s = "P";
					else if (IsSOTrx && (s.equals("S") || s.equals("U")))	//	No Check/Transfer for SO_Trx
						s = "P";											//  Payment Term
					mTab.setValue("PaymentRule", s);
				}
				//  Payment Term
				ii = new Integer(rs.getInt(IsSOTrx ? "C_PaymentTerm_ID" : "PO_PaymentTerm_ID"));
				if (!rs.wasNull())
					mTab.setValue("C_PaymentTerm_ID", ii);

				//	Location
				int locID = rs.getInt("C_BPartner_Location_ID");
				//	overwritten by InfoBP selection - works only if InfoWindow
				//	was used otherwise creates error (uses last value, may belong to differnt BP)
				if (C_BPartner_ID.toString().equals(Env.getContext(ctx, WindowNo, Env.TAB_INFO, "C_BPartner_ID")))
				{
					String loc = Env.getContext(ctx, WindowNo, Env.TAB_INFO, "C_BPartner_Location_ID");
					if (loc.length() > 0)
						locID = Integer.parseInt(loc);
				}
				if (locID == 0)
					mTab.setValue("C_BPartner_Location_ID", null);
				else
					mTab.setValue("C_BPartner_Location_ID", new Integer(locID));

				//	Contact - overwritten by InfoBP selection
				int contID = rs.getInt("AD_User_ID");
				if (C_BPartner_ID.toString().equals(Env.getContext(ctx, WindowNo, Env.TAB_INFO, "C_BPartner_ID")))
				{
					String cont = Env.getContext(ctx, WindowNo, Env.TAB_INFO, "AD_User_ID");
					if (cont.length() > 0)
						contID = Integer.parseInt(cont);
				}
				if (contID == 0)
					mTab.setValue("AD_User_ID", null);
				else
					mTab.setValue("AD_User_ID", new Integer(contID));

				//	CreditAvailable
				if (IsSOTrx)
				{
					double CreditLimit = rs.getDouble("SO_CreditLimit");
					if (CreditLimit != 0)
					{
						double CreditAvailable = rs.getDouble("CreditAvailable");
						if (!rs.wasNull() && CreditAvailable < 0)
							mTab.fireDataStatusEEvent("CreditLimitOver",
								DisplayType.getNumberFormat(DisplayType.Amount).format(CreditAvailable),
								false);
					}
				}
				
				//	PO Reference
				s = rs.getString("POReference");
				if (s != null && s.length() != 0)
					mTab.setValue("POReference", s);
				//NCG: nTier: 2015/05/15: RQ100011: After entering the BP on the invoice, the order ref. is blanked out.
				//	The following 2 lines have been commented out.
//				else
//					mTab.setValue("POReference", null);
				//	SO Description
				s = rs.getString("SO_Description");
				if (s != null && s.trim().length() != 0)
					mTab.setValue("Description", s);
				//	IsDiscountPrinted
				s = rs.getString("IsDiscountPrinted");
				if (s != null && s.length() != 0)
					mTab.setValue("IsDiscountPrinted", s);
				else
					mTab.setValue("IsDiscountPrinted", "N");
			}
		}
		catch (SQLException e)
		{
			log.log(Level.SEVERE, "bPartner", e);
			return e.getLocalizedMessage();
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		return "";
	}	//	bPartner

}	//	CalloutInvoice
