package org.adempiere.modelvalidator;

import org.compiere.model.MClient;
import org.compiere.model.MPayment;
import org.compiere.model.MReconHdr;
import org.compiere.model.MReconLine;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.util.CLogger;
import org.compiere.util.Env;

public class CreateReconValidatorOld implements ModelValidator {
    private static CLogger log = CLogger.getCLogger(CreateReconValidatorOld.class);
    private int m_AD_Client_ID = -1;
    private int m_AD_Org_ID = -1;
    private int m_AD_Role_ID = -1;
    private int m_AD_User_ID = -1;




	@Override
	public void initialize(ModelValidationEngine engine, MClient client) {
		if (client != null) {
			m_AD_Client_ID = client.getAD_Client_ID();
			engine.addModelChange(MPayment.Table_Name, this);
		}

	}

	@Override
	public int getAD_Client_ID() {
		// TODO Auto-generated method stub
		return m_AD_Client_ID;
	}

	@Override
	public String login(int AD_Org_ID, int AD_Role_ID, int AD_User_ID) {
		m_AD_Org_ID  = AD_Org_ID;
	    m_AD_Role_ID = AD_Role_ID;
	    m_AD_User_ID = AD_User_ID;
		return null;
	}

	@Override
	public String modelChange(PO po, int type) throws Exception {
		log.info("PO : " + po.toString() + " timing " + type);
		if (type == TIMING_AFTER_COMPLETE) {
			MPayment mPayment = (MPayment)po;
			if (mPayment.isReceipt() || mPayment.isCashTrx()) {
				MReconHdr mZZReconHdr = MReconHdr.getCurrentRecon(Env.getCtx(), mPayment.getCreatedBy());
				MReconLine mZZReconLine = new MReconLine(Env.getCtx(),0,null);
				mZZReconLine.setC_Payment_ID(mPayment.getC_Payment_ID());
				if (!mPayment.isReceipt()) {
					mZZReconLine.setAmount(mPayment.getPayAmt().negate());
				} else {
					mZZReconLine.setAmount(mPayment.getPayAmt());
				}
				mZZReconLine.setZZ_Recon_Hdr_ID(mZZReconHdr.getZZ_Recon_Hdr_ID());
				mZZReconLine.setTenderType(mPayment.getTenderType());
				mZZReconLine.save();
			}
		}
		return null;
	}

	@Override
	public String docValidate(PO po, int timing) {
		// TODO Auto-generated method stub
		return null;
	}

}
