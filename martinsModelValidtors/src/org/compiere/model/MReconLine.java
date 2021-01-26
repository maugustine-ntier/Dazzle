package org.compiere.model;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

import org.compiere.util.DB;
import org.compiere.util.Env;

public class MReconLine extends X_ZZ_Recon_Line {

	public MReconLine(Properties ctx, int ZZ_Recon_Line_ID, String trxName) {
		super(ctx, ZZ_Recon_Line_ID, trxName);
		// TODO Auto-generated constructor stub
	}

	public MReconLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean afterSave(boolean newRecord, boolean success) {
		MReconHdr mZZReconHdr = new MReconHdr(getCtx(),getZZ_Recon_Hdr_ID(),null);
		setTotal(mZZReconHdr,newRecord,get_TrxName());

		if (get_ValueOldAsInt("ZZ_Recon_Hdr_ID") != getZZ_Recon_Hdr_ID()) {
			mZZReconHdr = new MReconHdr(getCtx(),get_ValueOldAsInt("ZZ_Recon_Hdr_ID"),null);
			setTotal(mZZReconHdr,newRecord,get_TrxName());
		}
		return super.afterSave(newRecord, success);
	}

	private void setTotal(MReconHdr mZZReconHdr,boolean newRecord,String trxName) {
		BigDecimal total = null;

		String sql = "Select sum(amount) from ZZ_Recon_line where ZZ_Recon_Hdr_ID = ? and " +
				" TenderType = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {

			pstmt = DB.prepareStatement(sql, trxName);
			pstmt.setInt(1,mZZReconHdr.getZZ_Recon_Hdr_ID());
			pstmt.setString(2,getTenderType());
			rs = pstmt.executeQuery();

			while (rs.next()) {
				total = rs.getBigDecimal(1);
			}
			DB.close(rs, pstmt);
		} catch (Exception e) {

		}
		finally
		{
			DB.close(rs, pstmt);
		}
		if (newRecord) {
			if (total == null) {
				total = getAmount();
			} else {
				total = total.add(getAmount());
			}
		}
		if (total == null) {
			total = new BigDecimal("0.00");
		}
		if (total != null) {
			mZZReconHdr.setTenderTotal(getTenderType(), total);
			mZZReconHdr.save();
		}

	}

}
