package org.adempiere.process;

import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MRMA;
import org.compiere.model.PO;
import org.compiere.model.SystemIDs;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;
import org.eevolution.pos.VDocument;

public class ZZ_Create_MissingReturns extends SvrProcess{

	public final static int PROCESS_M_INOUT_GENERATERMA_CUST = 1000019;

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub

	}

	@Override
	protected String doIt() throws Exception {
		// TODO Auto-generated method stub

		VDocument doc = new VDocument(null);
		int ids [] = PO.getAllIDs("M_RMA", "AD_Client_ID = " + Env.getAD_Client_ID(Env.getCtx())
				+ " and not exists(select 'x' from adempiere.M_INOut ret where ret.m_rma_id = M_RMA.M_RMA_ID)" +
				" and m_RMA.docstatus <> 'VO'", null);
		for (int i = 0; i < ids.length; i++) {
			MRMA mRMA = new MRMA(Env.getCtx(), ids[i], null);
			if (mRMA != null) {
				int AD_Process_ID = PROCESS_M_INOUT_GENERATERMA_CUST;
				doc.createCreditNoteOrShipment(mRMA,AD_Process_ID);  // generate the invoice

				try {
				    Thread.sleep(2000);
				} catch(InterruptedException ex) {
				    Thread.currentThread().interrupt();
				}
			}
		}
		return null;
	}

}
