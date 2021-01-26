package org.eevolution.pos;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.model.MBPGroup;
import org.compiere.model.MBPartner;
import org.compiere.model.MPOS;
import org.compiere.model.MPriceList;
import org.compiere.model.MProductPrice;
import org.compiere.model.MProductPricing;
import org.compiere.model.Query;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Trace;

public class VPricing {

	private BigDecimal m_PriceStd = Env.ZERO;
	private BigDecimal m_PriceList = Env.ZERO;
	private BigDecimal m_PriceLimit = Env.ZERO;
	private boolean usePOPriceList = false;


	public boolean isUsePOPriceList() {
		return usePOPriceList;
	}

	public void setUsePOPriceList(boolean usePOPriceList) {
		this.usePOPriceList = usePOPriceList;
	}

	public MPriceList getPriceList(int Product_ID,Properties m_ctx,MBPartner currentBPartner)  {
		int userID = Env.getAD_User_ID(m_ctx);
		String whereClause = " AD_Org_ID = ? and SalesRep_ID = ? ";
		MPOS m_pos = new Query(m_ctx,MPOS.Table_Name,whereClause,null)
		.setParameters(new Object[]{Env.getAD_Org_ID(m_ctx),userID})
		.firstOnly();
		if (m_pos == null) {
			whereClause = " AD_Org_ID = ?  ";
			m_pos = new Query(m_ctx, MPOS.Table_Name, whereClause, null)
					.setParameters(new Object[] { Env.getAD_Org_ID(m_ctx) })
					.firstOnly();
			if (m_pos == null) {
				return null;
			}
		}

		MPriceList pl = null;
		if (currentBPartner != null) {
			// Martin added this and removed below
			System.out.println("USING CUST/supp PRICE LIST");


			pl = MPriceList.get(Env.getCtx(),
					(usePOPriceList ? currentBPartner.getPO_PriceList_ID() : currentBPartner.getM_PriceList_ID()), null);
			if (pl == null || pl.get_ID() == 0) {
				System.out.println("TRYING CUST group PRICE LIST");
				MBPGroup bpg = (MBPGroup)currentBPartner.getBPGroup();
				if (bpg != null && bpg.getM_PriceList_ID() != 0) {
					pl = MPriceList.get(Env.getCtx(),
						 (usePOPriceList ? bpg.getPO_PriceList_ID() : bpg.getM_PriceList_ID()),null);
					System.out.println("USING CUST/Supp group PRICE LIST");
				} else {
					System.out.println("USING POS PRICE LIST");
					pl = MPriceList.get(Env.getCtx(), m_pos.getM_PriceList_ID(),
						null);
				}
			}

		} else {
			if (m_pos.getM_PriceList_ID() != 0) {
				pl = MPriceList.get(Env.getCtx(), m_pos.getM_PriceList_ID(),null);
			}
		}

		return pl;

	}

	public BigDecimal getPrice(int Product_ID,MPriceList pl,Properties m_ctx,MPOS m_pos) {
		BigDecimal res = null;
		int M_Warehouse_ID = m_pos.getM_Warehouse_ID();


		// int M_PiceList_ID=pos.getM_PriceList_ID();
		// , uom.name as uom_name, pc.Name as product_category_name
		/*String productSearchSQL = "SELECT p.m_product_id, plv.M_PriceList_Version_id,p.value, p.name, p.upc, p.sku , p.classification"
				+ "  , bomqtyonhand(p.m_product_id, "
				+ M_Warehouse_ID
				+ ", 0::numeric) AS qtyonhand"
				+ "  , bomqtyreserved(p.m_product_id, "
				+ M_Warehouse_ID
				+ ", 0::numeric) AS qtyreserved"
				+ "  ,	bomqtyordered(p.m_product_id, "
				+ M_Warehouse_ID
				+ ", 0::numeric) AS qtyordered"
				+ "  , bompricelist(p.m_product_id, pp.m_pricelist_version_id) AS pricelist "
				+ "  , bompricestd(p.m_product_id, pp.m_pricelist_version_id) AS pricestd"
				+ "  , coalesce(bompricestd(p.m_product_id, pp.m_pricelist_version_id)*"
				+ "	(select round(max(rate)/100,2) from c_tax t where c_taxcategory_id=(select c_taxcategory_id from m_product  where m_product_id=p.m_product_id)"
				+ "		)+bompricestd(p.m_product_id, pp.m_pricelist_version_id) "
				+ "		, bompricelist(p.m_product_id, pp.m_pricelist_version_id)) AS priceIVA"
				+ "  , bompricelimit(p.m_product_id, pp.m_pricelist_version_id) AS pricelimit"
				+ "  , coalesce(plv.NAME,'') as codTV"
				+ "  from m_product p"
				+ "  INNER JOIN M_PriceList pl ON pl.M_PriceList_ID = ?"
				+ "  INNER JOIN M_PriceList_Version plv ON pl.M_PriceList_ID = plv.M_PriceList_ID"
				+ "  INNER JOIN m_productprice pp ON p.m_product_id = pp.m_product_id AND pp.M_PriceList_Version_ID=plv.M_PriceList_Version_id"
				+ "  LEFT  JOIN PP_Product_BOM ppb on ppb.m_product_id=p.m_product_id"
				+ "  WHERE p.ad_client_ID = " + Env.getCtx().getProperty("#AD_Client_ID")   // Martin added client
				+ " and p.IsActive='Y' AND pl.IsActive='Y' AND plv.IsActive='Y'"
				+ "  and (pp.ad_org_id=0 or pp.ad_org_id="
				+ Env.getAD_Org_ID(m_ctx)
				+ ")"
				+ "  AND ((current_date BETWEEN ppb.validfrom AND ppb.validto OR ppb.validto is null) OR ppb.PP_Product_BOM_id is null)"
				+ "  AND (   UPPER(p.value) LIKE '" // %" martin commented
				+ filtro.toUpperCase()
				+ "%'"
				+ "         OR UPPER(p.name) LIKE '" // %" Martin COmmented
				+ filtro.toUpperCase()
				+ "%'"
				+ "         OR UPPER(p.upc) LIKE '" // %" Martin commented
				+ filtro.toUpperCase()
				+ "%'"
				+ "         OR UPPER(coalesce(plv.NAME,'')) LIKE '" // '%" Martin commented
				+ filtro.toUpperCase()
				+ "%')"
				+ " ORDER BY coalesce(plv.NAME,'') ";
				+ "and p.M_Product_ID = ?";*/


		String productSearchSQL = "SELECT  pp.pricestd AS pricestd "
			+ "  from m_product p"
			+ "  INNER JOIN M_PriceList pl ON pl.M_PriceList_ID = ?"
			+ "  INNER JOIN M_PriceList_Version plv ON pl.M_PriceList_ID = plv.M_PriceList_ID"
			+ "  INNER JOIN m_productprice pp ON p.m_product_id = pp.m_product_id AND pp.M_PriceList_Version_ID=plv.M_PriceList_Version_id"
			+ "  LEFT  JOIN PP_Product_BOM ppb on ppb.m_product_id=p.m_product_id"
			+ "  WHERE p.ad_client_ID = " + Env.getCtx().getProperty("#AD_Client_ID")   // Martin added client
			+ " and p.IsActive='Y' AND pl.IsActive='Y' AND plv.IsActive='Y'"
			+ "  and (pp.ad_org_id=0 or pp.ad_org_id="
			+ Env.getAD_Org_ID(m_ctx)
			+ ")"
			+ "  AND ((current_date BETWEEN ppb.validfrom AND ppb.validto OR ppb.validto is null) OR ppb.PP_Product_BOM_id is null)"
			/*+ "  AND (   UPPER(p.value) LIKE '" // %" martin commented
			+ filtro.toUpperCase()
			+ "%'"
			+ "         OR UPPER(p.name) LIKE '" // %" Martin COmmented
			+ filtro.toUpperCase()
			+ "%'"
			+ "         OR UPPER(p.upc) LIKE '" // %" Martin commented
			+ filtro.toUpperCase()
			+ "%'"
			+ "         OR UPPER(coalesce(plv.NAME,'')) LIKE '" // '%" Martin commented
			+ filtro.toUpperCase()
			+ "%')"
			+ " ORDER BY coalesce(plv.NAME,'') ";*/
			+ "and p.M_Product_ID = ?";

		try {
			PreparedStatement pstmt = DB.prepareStatement(productSearchSQL, null);
			pstmt.setInt(1,pl.getM_PriceList_ID());
			pstmt.setInt(2, Product_ID);
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {

					res = rs.getBigDecimal("pricestd");
			}
			DB.close(rs, pstmt);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	public boolean calculatePL(int m_M_Product_ID , int m_M_PriceList_ID, Timestamp m_PriceDate)
	{
		if (m_M_Product_ID == 0)
			return false;


		if (m_M_PriceList_ID == 0)
		{
		//	log.log(Level.SEVERE, "No PriceList");
			Trace.printStack();
			return false;
		}

		//	Get Prices for Price List
		String sql = "SELECT bomPriceStd(p.M_Product_ID,pv.M_PriceList_Version_ID) AS PriceStd,"	//	1
			+ " bomPriceList(p.M_Product_ID,pv.M_PriceList_Version_ID) AS PriceList,"		//	2
			+ " bomPriceLimit(p.M_Product_ID,pv.M_PriceList_Version_ID) AS PriceLimit,"	//	3
			+ " p.C_UOM_ID,pv.ValidFrom,pl.C_Currency_ID,p.M_Product_Category_ID,pl.EnforcePriceLimit "	// 4..8
			+ "FROM M_Product p"
			+ " INNER JOIN M_ProductPrice pp ON (p.M_Product_ID=pp.M_Product_ID)"
			+ " INNER JOIN  M_PriceList_Version pv ON (pp.M_PriceList_Version_ID=pv.M_PriceList_Version_ID)"
			+ " INNER JOIN M_Pricelist pl ON (pv.M_PriceList_ID=pl.M_PriceList_ID) "
			+ "WHERE pv.IsActive='Y'"
			+ " AND pp.IsActive='Y'"
			+ " AND p.M_Product_ID=?"				//	#1
			+ " AND pv.M_PriceList_ID=?"			//	#2
			+ " ORDER BY pv.ValidFrom DESC";
		boolean m_calculated = false;
		if (m_PriceDate == null)
			m_PriceDate = new Timestamp (System.currentTimeMillis());
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1, m_M_Product_ID);
			pstmt.setInt(2, m_M_PriceList_ID);
			rs = pstmt.executeQuery();
			while (!m_calculated && rs.next())
			{
				Timestamp plDate = rs.getTimestamp(5);
				//	we have the price list
				//	if order date is after or equal PriceList validFrom
				if (plDate == null || !m_PriceDate.before(plDate))
				{
					//	Prices
					m_PriceStd = rs.getBigDecimal (1);
					if (rs.wasNull ())
						m_PriceStd = Env.ZERO;
					m_PriceList = rs.getBigDecimal (2);
					if (rs.wasNull ())
						m_PriceList = Env.ZERO;
					m_PriceLimit = rs.getBigDecimal (3);
					if (rs.wasNull ())
						m_PriceLimit = Env.ZERO;
						//
					int m_C_UOM_ID = rs.getInt (4);
					int m_C_Currency_ID = rs.getInt (6);
					int m_M_Product_Category_ID = rs.getInt(7);
					boolean m_enforcePriceLimit = "Y".equals(rs.getString(8));
					//

					m_calculated = true;
					break;
				}
			}
		}
		catch (Exception e)
		{
		//	log.log(Level.SEVERE, sql, e);
			m_calculated = false;
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null;
			pstmt = null;
		}
		//if (!m_calculated)
			//log.finer("Not found (PL)");
		return m_calculated;
	}	//	calculatePL

	public static void updateLimitPrices(int Product_ID,Properties m_ctx,BigDecimal cost) {
		String sql = "SELECT pv.M_PriceList_Version_ID,pl.isSopriceList"
				+ " FROM M_Product p"
				+ " INNER JOIN M_ProductPrice pp ON (p.M_Product_ID=pp.M_Product_ID)"
				+ " INNER JOIN  M_PriceList_Version pv ON (pp.M_PriceList_Version_ID=pv.M_PriceList_Version_ID)"
				+ " INNER JOIN M_Pricelist pl ON (pv.M_PriceList_ID=pl.M_PriceList_ID) "
				+ " WHERE pv.IsActive='Y'"
				+ " AND pp.ad_client_ID = " + m_ctx.getProperty("#AD_Client_ID")   // Martin added client
				+ " AND pp.IsActive='Y'"
				+ " AND p.M_Product_ID=?"				//	#1
				+ " ORDER BY pv.ValidFrom DESC";
			boolean m_calculated = false;
			PreparedStatement pstmt = null;
			ResultSet rs = null;
			try
			{
				pstmt = DB.prepareStatement(sql, null);
				pstmt.setInt(1, Product_ID);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					MProductPrice mpp = MProductPrice.get(m_ctx, rs.getInt(1), Product_ID, null);
					mpp.setPriceLimit(cost);
					if (rs.getString(2) != null && rs.getString(2).equalsIgnoreCase("N")) {
						mpp.setPriceList(cost);
						mpp.setPriceStd(cost);
					}
					mpp.saveEx();

				}
			}
			catch (Exception e)
			{
			//	log.log(Level.SEVERE, sql, e);
				m_calculated = false;
			}
			finally
			{
				DB.close(rs, pstmt);
				rs = null;
				pstmt = null;
			}
	}

	public BigDecimal getM_PriceStd() {
		return m_PriceStd;
	}

	public void setM_PriceStd(BigDecimal mPriceStd) {
		m_PriceStd = mPriceStd;
	}

	public BigDecimal getM_PriceList() {
		return m_PriceList;
	}

	public void setM_PriceList(BigDecimal mPriceList) {
		m_PriceList = mPriceList;
	}

	public BigDecimal getM_PriceLimit() {
		return m_PriceLimit;
	}

	public void setM_PriceLimit(BigDecimal mPriceLimit) {
		m_PriceLimit = mPriceLimit;
	}



}
