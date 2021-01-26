package org.adempiere.modelvalidator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.compiere.model.FactsValidator;
import org.compiere.model.I_C_BankStatement;
import org.compiere.model.I_C_Payment;
import org.compiere.model.I_M_Inventory;
import org.compiere.model.MBankStatement;
import org.compiere.model.MClient;
import org.compiere.model.MDocType;
import org.compiere.model.MInventory;
import org.compiere.model.MPayment;
import org.compiere.model.MReconHdr;
import org.compiere.model.MReconLine;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.X_C_DocType;
import org.compiere.model.X_C_Payment;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;

/**
 */
public class CreateReconValidator implements ModelValidator  {
	private static CLogger log = CLogger.getCLogger(CreateReconValidator.class);
	private int m_AD_Client_ID = -1;

	@Override
	public void initialize(final ModelValidationEngine engine, final MClient client) {
		if (client != null) {
			m_AD_Client_ID = client.getAD_Client_ID();
		}
		engine.addModelChange(I_C_Payment.Table_Name, this);
		engine.addDocValidate(I_C_Payment.Table_Name, this);
		engine.addDocValidate(I_C_BankStatement.Table_Name, this);
		engine.addModelChange(I_M_Inventory.Table_Name, this);
		//engine.addFactsValidate(I_C_BankStatement.Table_Name, this);

	}

	@Override
	public int getAD_Client_ID() {
		// TODO Auto-generated method stub
		return m_AD_Client_ID;
	}

	@Override
	public String login(final int AD_Org_ID, final int AD_Role_ID, final int AD_User_ID) {
		return null;
	}

	@Override
	public String modelChange(final PO po, final int type) throws Exception {
		log.info("PO : " + po.toString() + " timing " + type);
		if (po instanceof MPayment) {
			if (type == TYPE_AFTER_CHANGE) {
				final MPayment mPayment = (MPayment) po;
				if (mPayment.getDocStatus().equals(X_C_Payment.DOCACTION_Complete)) {
					if (mPayment.isReceipt() || mPayment.getTenderType().equals(X_C_Payment.TENDERTYPE_Cash)) {
						final MReconLine mReconLine = MReconHdr.getReconLineForPayment(mPayment);
						if (mReconLine != null && mReconLine.getTenderType() != mPayment.getTenderType()) {
							mReconLine.setTenderType(mPayment.getTenderType());
							mReconLine.save();
						}
						MReconHdr mZZReconHdr = MReconHdr.getReconForPayment(mPayment);
						if (mZZReconHdr == null) {  // Not linked to a recon
							mZZReconHdr = createReconLine(mPayment);
						}
						if (mZZReconHdr != null) {
							mZZReconHdr.recalcTotals();
							mZZReconHdr.save();
						}
					}
				}

			}
		}

		if (po instanceof MInventory) {
			if (type == TYPE_BEFORE_NEW) {
				final MInventory mInventory = (MInventory) po;
				if (mInventory.getC_DocType_ID() == 0) {
					final MDocType types[] = MDocType.getOfDocBaseType(Env.getCtx(),
							X_C_DocType.DOCBASETYPE_MaterialPhysicalInventory);
					if (types.length > 0) {
						mInventory.setC_DocType_ID(types[0].getC_DocType_ID());
					} else {
						log.saveError("Error", Msg.parseTranslation(Env.getCtx(), "@NotFound@ @C_DocType_ID@"));
						//return false;
					}
				}
			}
		}



		return null;
	}

	@Override
	public String docValidate(final PO po, final int timing) {
		if (po instanceof MPayment) {
			if (timing == TIMING_AFTER_COMPLETE) {
				final MPayment mPayment = (MPayment) po;
				if (mPayment.isReceipt() || mPayment.getTenderType().equals(X_C_Payment.TENDERTYPE_Cash)) {
					createReconLine(mPayment);
				}
			}
		}
		if (po instanceof MBankStatement) {
			if (timing == TIMING_AFTER_COMPLETE) {
				final MBankStatement bs = (MBankStatement) po;

				try {
					String sql = "SELECT dbl.C_Payment_ID FROM C_BankStatementLine bsl,c_depositbatch db ,C_DepositBatchLine dbl" +
							" WHERE " +
							" bsl.C_bankstatement_ID = " + bs.getC_BankStatement_ID() +
							" and bsl.c_depositbatch_id = db.c_depositbatch_id" +
							" and db.c_depositbatch_id = dbl.c_depositbatch_id" ;

					PreparedStatement pstmt = DB.prepareStatement(sql, null);
					ResultSet rs = pstmt.executeQuery();

					while (rs.next()) {
						final MPayment payment = new MPayment(Env.getCtx(), rs.getInt(1), bs.get_TrxName());
						payment.setIsReconciled(true);
						payment.saveEx(bs.get_TrxName());
					}
					DB.close(rs, pstmt);
				} catch (Exception e) {

				}
			}

			if (timing == TIMING_AFTER_VOID) {
				final MBankStatement bs = (MBankStatement) po;
				try {
					String sql = "SELECT dbl.C_Payment_ID FROM C_BankStatementLine bsl,c_depositbatch db ,C_DepositBatchLine dbl" +
							" WHERE " +
							" bsl.C_bankstatement_ID = " + bs.getC_BankStatement_ID() +
							" and bsl.c_depositbatch_id = db.c_depositbatch_id" +
							" and db.c_depositbatch_id = dbl.c_depositbatch_id" ;

					PreparedStatement pstmt = DB.prepareStatement(sql, null);
					ResultSet rs = pstmt.executeQuery();

					while (rs.next()) {
						final MPayment payment = new MPayment(Env.getCtx(), rs.getInt(1), bs.get_TrxName());
						payment.setIsReconciled(false);
						payment.saveEx(bs.get_TrxName());
					}
					DB.close(rs, pstmt);
				} catch (Exception e) {

				}
			}
		}
		return null;
	}

	private MReconHdr createReconLine(final MPayment mPayment) {
		final MReconHdr mZZReconHdr = MReconHdr.getCurrentRecon(Env.getCtx(), mPayment.getCreatedBy());
		final MReconLine mZZReconLine = new MReconLine(Env.getCtx(), 0, null);
		mZZReconLine.setC_Payment_ID(mPayment.getC_Payment_ID());
		if (!mPayment.isReceipt()) {
			mZZReconLine.setAmount(mPayment.getPayAmt().negate());
		} else {
			mZZReconLine.setAmount(mPayment.getPayAmt());
		}
		mZZReconLine.setZZ_Recon_Hdr_ID(mZZReconHdr.getZZ_Recon_Hdr_ID());
		mZZReconLine.setTenderType(mPayment.getTenderType());
		mZZReconLine.save();
		return mZZReconHdr;
	}

}
