/**
 * 
 */
package org.compiere.model;

import java.sql.ResultSet;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.process.DocOptions;
import org.compiere.process.DocumentEngine;
import org.compiere.util.Env;

/**
 * @author simba
 *
 */
public class ZZ_BankStatement extends MBankStatement implements DocOptions {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * @param ctx
	 * @param C_BankStatement_ID
	 * @param trxName
	 */
	public ZZ_BankStatement(Properties ctx, int C_BankStatement_ID,
			String trxName) {
		super(ctx, C_BankStatement_ID, trxName);
	}

	/**
	 * @param ctx
	 * @param rs
	 * @param trxName
	 */
	public ZZ_BankStatement(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}

	/**
	 * @param account
	 * @param isManual
	 */
	public ZZ_BankStatement(MBankAccount account, boolean isManual) {
		super(account, isManual);
	}

	/**
	 * @param account
	 */
	public ZZ_BankStatement(MBankAccount account) {
		super(account);
	}

	/* (non-Javadoc)
	 * @see org.compiere.process.DocOptions#customizeValidActions(java.lang.String, java.lang.Object, java.lang.String, java.lang.String, int, java.lang.String[], java.lang.String[], int)
	 */
	@Override
	public int customizeValidActions(String docStatus, Object processing,
			String orderType, String isSOTrx, int AD_Table_ID,
			String[] docAction, String[] options, int index) {
		
		//int AD_RefList_ID = MRefList.get(getCtx(), 135 /* _Document Action */, DOCACTION_Re_Activate, get_TrxName());
		if (AD_Table_ID == MBankStatement.Table_ID && 
				ZZ_DocumentActionAccess.isDocActionAccess(getCtx(), get_TrxName(), 
						Env.getAD_Role_ID(getCtx()), "Bank Statement", DOCACTION_Re_Activate))
		{
			//	Complete                    ..  CO
			if (docStatus.equals(DocumentEngine.STATUS_Completed))
			{
				options[index++] = DocumentEngine.ACTION_ReActivate;
			}
		}
		return index;
	}
	
	@Override
	public boolean reActivateIt()
	{
		String m_processMsg = null;
		if (log.isLoggable(Level.INFO)) log.info("reActivateIt - " + toString());
		// Before reActivate
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_BEFORE_REACTIVATE);
		if (m_processMsg != null)
			return false;		
		
		// After reActivate
		m_processMsg = ModelValidationEngine.get().fireDocValidate(this,ModelValidator.TIMING_AFTER_REACTIVATE);
		if (m_processMsg != null)
			return false;		
		return true;
	}

}
