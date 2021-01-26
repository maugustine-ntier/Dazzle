package org.adempiere.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MLocation;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class ZZ_Create_Addresses extends SvrProcess {

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub

	}

	@Override
	protected String doIt() throws Exception {
		// TODO Auto-generated method stub
		int cnt = 0;
		String sql = "select bp.c_bpartner_ID from c_bpartner bp " +
                     "where not exists (select 'x' from c_bpartner_location bpl where bpl.c_bpartner_ID = bp.c_bpartner_ID)" +
                     " and bp.ad_client_ID = 1000001";
		 PreparedStatement pstmt = null;
	        ResultSet rs = null;
	        try
	        {
	            pstmt = DB.prepareStatement(sql, get_TrxName());
	           // pstmt.setInt(1, Env.getAD_Client_ID(getCtx()));
	            //pstmt.setInt(2, getAD_PInstance_ID());
	            rs = pstmt.executeQuery();

	            while (rs.next())
	            {
	            	int bpID = rs.getInt(1);
	            	MBPartner bp = new MBPartner(getCtx(), bpID, get_TrxName());
	            	MLocation loc = new MLocation(getCtx(), 305, 0, "", get_TrxName());
	            	loc.setAddress1(".");
	            	loc.saveEx();
	            	MBPartnerLocation bpl = new MBPartnerLocation(bp);
	            	bpl.setName(".");
	            	bpl.setC_Location_ID(loc.getC_Location_ID());
	            	bpl.saveEx();
	            	cnt++;

	            }
	        }
	        catch (Exception ex)
	        {
	            log.log(Level.SEVERE, sql, ex);
	        }
	        finally
	        {
	            DB.close(rs,pstmt);
	            rs = null;pstmt = null;
	        }



		return "Updated : " + cnt;
	}

}
