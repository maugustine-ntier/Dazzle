package org.adempiere.process;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MLocation;
import org.compiere.model.MOrder;
import org.compiere.process.SvrProcess;
import org.compiere.util.DB;
import org.compiere.util.Env;

public class ZZ_CompleteOrders extends SvrProcess {

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub

	}

	@Override
	protected String doIt() throws Exception {
		// TODO Auto-generated method stub
		int cnt = 0;
		String sql = "select C_Order_ID from C_Order where docstatus in ( 'DR','IP') and " +
				" AD_Client_ID = " + Env.getAD_Client_ID(Env.getCtx());
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
	            	int orderID = rs.getInt(1);
	            	MOrder order = new MOrder(Env.getCtx(), orderID, null);
	            	order.setDocAction(MOrder.ACTION_Complete);
	    			order.processIt(MOrder.ACTION_Complete);
	    			order.save();
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
