package org.adempiere.modelvalidator;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.acct.Fact;
import org.compiere.model.Doc_BankStatementDazzle;
import org.compiere.model.FactsValidator;
import org.compiere.model.I_C_BankStatement;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MBankStatement;
import org.compiere.model.MBankStatementLine;
import org.compiere.model.MClient;
import org.compiere.model.MFactAcct;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPrice;
import org.compiere.model.ModelValidationEngine;
import org.compiere.model.ModelValidator;
import org.compiere.model.PO;
import org.compiere.model.X_C_BankStatement;
import org.compiere.util.CLogger;
import org.compiere.util.DB;

import za.co.ntier.common.NTierCost;
import za.co.ntier.dazzle.common.DazzleConfigManager;

/**
 */
public class DazzleValidator implements ModelValidator, FactsValidator {
	private static CLogger log = CLogger.getCLogger(DazzleValidator.class);
	private int m_AD_Client_ID = -1;
	
	private boolean m_isDazzleCostAutoCreate;
	

	@Override
	public void initialize(final ModelValidationEngine engine, final MClient client) {
		log.warning("*********** DazzleValidator ***********");
		if (client != null) {
			m_AD_Client_ID = client.getAD_Client_ID();
		}
		
		engine.addFactsValidate(I_C_BankStatement.Table_Name, this);
		
		// NCG: RQ100007: Allow bank statement to be re-activated
		engine.addDocValidate(MBankStatement.Table_Name, this);	

		//NCG: #RQ100021: Default the cost to price list, and give error if no cost
		engine.addModelChange(MProductPrice.Table_Name, this);

	}

	@Override
	public int getAD_Client_ID() {
		return m_AD_Client_ID;
	}

	@Override
	public String login(final int AD_Org_ID, final int AD_Role_ID, final int AD_User_ID) {
		return null;
	}

	@Override
	public String modelChange(final PO po, final int type) throws Exception {
		log.info("PO : " + po.toString() + " timing " + type);
		//See CreateReconValidator for example
		if (po instanceof MProductPrice) {
			modelChangeCostCreate(po, type);
		}
		return null;
	}

	@Override
	public String docValidate(final PO po, final int timing) {
		if (log.isLoggable(Level.INFO)) 
			log.info("Doc Validator for table=" + po.get_TableName() + ", for timing=" + timing);
		String result = null;
		
		// From Megafreight project by Anesh or Simba
		// NCG: RQ100007: Allow bank statement to be re-activated
		if (timing == TIMING_AFTER_REACTIVATE){
			
			String tableName = po.get_TableName();
			
			if (tableName.equals(MBankStatement.Table_Name))
			{
				MBankStatement bs = (MBankStatement)po;
				
				/* globalqss - 2317928 - Reactivating/Voiding order must reset posted */
				MFactAcct.deleteEx(MBankStatement.Table_ID, bs.getC_BankStatement_ID(), bs.get_TrxName());
				bs.setPosted(false);
				
				bs.setDocStatus(X_C_BankStatement.DOCSTATUS_InProgress);
				bs.setDocAction(X_C_BankStatement.DOCACTION_Complete);
				bs.setProcessed(false);
				
				MBankStatementLine[] bslines = bs.getLines(false);
					for ( MBankStatementLine line : bslines)
					{
						line.setProcessed(false);
						line.save();
					}
			} 
			
		}
	
		return result;
	}

	/*
	 * NCG 2014/09/09 #1000248: Allow charges on the bank statement line to have tax implications
	 * Override posting of Bank Statement, add VAT into charge (where applicable)
	 */
	@Override
	public String factsValidate(MAcctSchema as, List<Fact> facts, PO po) {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		String sql = "select * from " + MBankStatement.Table_Name + "" +
				" where c_bankstatement_id=?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try
		{
			pstmt = DB.prepareStatement(sql, po.get_TrxName());
			pstmt.setInt(1, po.get_ID());
			rs = pstmt.executeQuery();
			rs.next();
			
			Doc_BankStatementDazzle doc = new Doc_BankStatementDazzle(as, rs, po.get_TrxName());
			List<Fact> newFacts = doc.createFactsDazzle(as);
			
			//Add newFact(s) back
			facts.clear();
			for (Fact fact:newFacts) {
				facts.add(fact);
			}
			
			rs.close();
			pstmt.close();
			pstmt = null;
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, sql, e);
		}
		finally
		{
			DB.close(rs, pstmt);
			rs = null; pstmt = null;
		}
		facts = new ArrayList<Fact>();
		return "";
	}
	
	/**
	 * NCG: #RQ100021: Default the cost to price list, and give error if no cost
	 */
	public String modelChangeCostCreate(final PO po, final int type) {
		MProductPrice pp = (MProductPrice) po;
		Properties ctx = pp.getCtx();
		String trxName = pp.get_TrxName();
		
		if ( 
				(type == TYPE_AFTER_CHANGE || type == TYPE_AFTER_NEW) 
				&& po != null ) {
			
			m_isDazzleCostAutoCreate = DazzleConfigManager.isDazzleCostAutoCreate(po.getCtx(), null);
			
			if ( m_isDazzleCostAutoCreate ) {
				if ( ! pp.getM_PriceList_Version().getM_PriceList().isSOPriceList() ) {
					BigDecimal costPrice = pp.getPriceLimit();
					// Only for purchase price list
					MProduct prod = MProduct.get(pp.getCtx(), pp.getM_Product_ID());
					
					if (NTierCost.isCostRecordRequired( prod )) {
						// Create a costing record - if one does not exist already.
						NTierCost.create( prod, costPrice );
					}
				}
			}
		}
		return "";
	}

}
