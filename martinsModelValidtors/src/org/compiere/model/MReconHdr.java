package org.compiere.model;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Properties;

import org.compiere.util.DB;
import org.compiere.util.Env;

public class MReconHdr extends X_ZZ_Recon_Hdr {

	public MReconHdr(Properties ctx, int ZZ_Recon_Hdr_ID, String trxName) {
		super(ctx, ZZ_Recon_Hdr_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MReconHdr(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	public static MReconHdr getCurrentRecon(Properties ctx,int ad_User_ID) {

		MReconHdr mZZReconHdr = null;
		String whereClause = " AD_Org_ID = " + Env.getAD_Org_ID(ctx) +
				" and createdBy = " + ad_User_ID +
				" and Processed = 'N' ";


		int ids[] = PO.getAllIDs("ZZ_Recon_Hdr", whereClause, null);
		/*mZZReconHdr = new Query(ctx, MZZReconHdr.Table_Name, whereClause, null)
		.setParameters(new Object[] { Env.getAD_Org_ID(ctx),ad_User_ID }).firstOnly();*/
		if (ids != null && ids.length > 0) {
			mZZReconHdr = new MReconHdr(ctx,ids[0],null);
		}
		if (mZZReconHdr == null) {
			mZZReconHdr = new MReconHdr(ctx,0,null);
			mZZReconHdr.setZZ_Recon_Date(new Timestamp(System.currentTimeMillis()));
			mZZReconHdr.save();

		}
		return mZZReconHdr;

	}

	public static MReconHdr getReconForPayment(MPayment mPayment) {
		String whereClause = " C_Payment_ID = " + mPayment.getC_Payment_ID();
		int ids[] = PO.getAllIDs("ZZ_Recon_line", whereClause, null);
		if (ids != null && ids.length > 0) {
			MReconLine mZZReconLine = new MReconLine(mPayment.getCtx(), ids[0], null);
			MReconHdr mZZReconHdr = new MReconHdr(mPayment.getCtx(),mZZReconLine.getZZ_Recon_Hdr_ID(),null);
			return mZZReconHdr;
		}
		return null;

	}

	public static MReconLine getReconLineForPayment(MPayment mPayment) {
		String whereClause = " C_Payment_ID = " + mPayment.getC_Payment_ID();
		int ids[] = PO.getAllIDs("ZZ_Recon_line", whereClause, null);
		if (ids != null && ids.length > 0) {
			MReconLine mZZReconLine = new MReconLine(mPayment.getCtx(), ids[0], null);
			return mZZReconLine;
		}
		return null;

	}

	public void setTenderTotal(String tenderType,BigDecimal total) {
		if (tenderType.equals("X")) {
			setZZ_Cash(total);
		} else if (tenderType.equals("K")) {
			setZZ_Cheques(total);
		}  else if (tenderType.equals("C")) {
			setZZ_Credit_Cards(total);
		} else if (tenderType.equals("A") || tenderType.equals("D") || tenderType.equals("T")) {
			setZZ_Direct_Deposits(total);
		}
	}

	public void recalcTotals() {
		boolean cashSet       = false;
		boolean chequeSet     = false;
		boolean creditCardSet = false;
		boolean dirDepSet     = false;
		String sql = "Select tendertype,sum(amount) from ZZ_Recon_line where ZZ_Recon_Hdr_ID = ? " +
				" group by TenderType";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {

			pstmt = DB.prepareStatement(sql, null);
			pstmt.setInt(1,getZZ_Recon_Hdr_ID());
			rs = pstmt.executeQuery();

			while (rs.next()) {
				BigDecimal total = rs.getBigDecimal(2);
				setTenderTotal(rs.getString(1),total);
				if (rs.getString(1).equals("X")) {
					cashSet = true;
				} else if (rs.getString(1).equals("K")) {
					chequeSet = true;
				}  else if (rs.getString(1).equals("C")) {
					creditCardSet = true;
				} else if (rs.getString(1).equals("A") || rs.getString(1).equals("D") || rs.getString(1).equals("T")) {
					dirDepSet = true;
				}

			}
			DB.close(rs, pstmt);
		} catch (Exception e) {

		}
		finally
		{
			DB.close(rs, pstmt);
		}


		if (!cashSet) {
			setTenderTotal("X",Env.ZERO);
		}
		if (!chequeSet) {
			setTenderTotal("K",Env.ZERO);
		}
		if (!creditCardSet) {
			setTenderTotal("C",Env.ZERO);
		}
		if (!dirDepSet) {
			setTenderTotal("D",Env.ZERO);
		}
	}

}
