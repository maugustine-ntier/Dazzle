package org.compiere.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;

import org.compiere.acct.Doc;
import org.compiere.acct.DocLine_Bank;
import org.compiere.acct.DocTax;
import org.compiere.acct.Doc_BankStatement;
import org.compiere.acct.Fact;
import org.compiere.acct.FactLine;
import org.compiere.util.Env;

/**
 * @author NCG
 * Date: 09 Sep 2014
 * Description:	#1000248: Allow charges on the bank statement line to have tax implications
 */
public class Doc_BankStatementDazzle extends Doc_BankStatement {

	//protected DocLine[]			p_lines;
	
	protected MBankStatement m_po;
	
	public Doc_BankStatementDazzle(MAcctSchema as, ResultSet rs, String trxName) {
		super(as, rs, trxName);
	}
	
	/**
	 *  Create Facts (the accounting logic) for
	 *  CMB.
	 *  <pre>
	 *      BankAsset       DR      CR  (Statement)
	 *      BankInTransit   DR      CR              (Payment)
	 *      Charge          DR          (Charge)
	 *      Interest        DR      CR  (Interest)
	 *  </pre>
	 *  @param as accounting schema
	 *  @return Fact
	 *  
	 *  @author NCG
	 *  Modified for: NCG 2014/09/09 #1000248: Allow charges on the bank statement line to have tax implications
	 */
	public ArrayList<Fact> createFactsDazzle (MAcctSchema as)
	{
		
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		
		loadDocumentDetails();
		
		m_po = (MBankStatement)p_po;
		//  create Fact Header
		Fact fact = new Fact(this, as, Fact.POST_Actual);
		// boolean isInterOrg = isInterOrg(as);

		//  Header -- there may be different currency amounts

		FactLine fl = null;
		int AD_Org_ID = getBank_Org_ID();	//	Bank Account Org
		//  Lines
		for (int i = 0; i < p_lines.length; i++)
		{
			DocLine_Bank line = (DocLine_Bank)p_lines[i];
			//TODO: Dazzle Model factory, to return the correct model
			MBankStatementLine  po1 = (MBankStatementLine)line.getPO();
			ZZ_BankStatementLine statementLine = new ZZ_BankStatementLine(as.getCtx(), po1.get_ID(), as.get_TrxName());
			int C_BPartner_ID = line.getC_BPartner_ID();

			// Avoid usage of clearing accounts
			// If both accounts BankAsset and BankInTransit are equal
			// then remove the posting

			MAccount acct_bank_asset =  getAccount(Doc.ACCTTYPE_BankAsset, as);
			MAccount acct_bank_in_transit = getAccount(Doc.ACCTTYPE_BankInTransit, as);

			// if ((!as.isPostIfClearingEqual()) && acct_bank_asset.equals(acct_bank_in_transit) && (!isInterOrg)) {
			// don't validate interorg on banks for this - normally banks are balanced by orgs
			if ((!as.isPostIfClearingEqual()) && acct_bank_asset.equals(acct_bank_in_transit)) {
				// Not using clearing accounts
				// just post the difference (if any)

				BigDecimal amt_stmt_minus_trx = line.getStmtAmt().subtract(line.getTrxAmt());
				if (amt_stmt_minus_trx.compareTo(Env.ZERO) != 0) {

					//  BankAsset       DR      CR  (Statement minus Payment)
					fl = fact.createLine(line,
						getAccount(Doc.ACCTTYPE_BankAsset, as),
						line.getC_Currency_ID(), amt_stmt_minus_trx);
					if (fl != null && AD_Org_ID != 0)
						fl.setAD_Org_ID(AD_Org_ID);
					if (fl != null && C_BPartner_ID != 0)
						fl.setC_BPartner_ID(C_BPartner_ID);

				}

			} else {

				// Normal Adempiere behavior -- unchanged if using clearing accounts

				//  BankAsset       DR      CR  (Statement)
				fl = fact.createLine(line,
					getAccount(Doc.ACCTTYPE_BankAsset, as),
					line.getC_Currency_ID(), line.getStmtAmt());
				if (fl != null && AD_Org_ID != 0)
					fl.setAD_Org_ID(AD_Org_ID);
				if (fl != null && C_BPartner_ID != 0)
					fl.setC_BPartner_ID(C_BPartner_ID);

				//  BankInTransit   DR      CR              (Payment)
				fl = fact.createLine(line,
					getAccount(Doc.ACCTTYPE_BankInTransit, as),
					line.getC_Currency_ID(), line.getTrxAmt().negate());
				if (fl != null)
				{
					if (C_BPartner_ID != 0)
						fl.setC_BPartner_ID(C_BPartner_ID);
					if (AD_Org_ID != 0)
						fl.setAD_Org_ID(AD_Org_ID);
					else
						fl.setAD_Org_ID(line.getAD_Org_ID(true)); // from payment
				}

			}
			// End Avoid usage of clearing accounts

			//  Charge          DR          (Charge)
			if ( line.getChargeAmt().compareTo(Env.ZERO)  != 0 ) {
				
				//NCG 2014/09/09 #1000248: Allow charges on the bank statement line to have tax implications
				BigDecimal VAT = BigDecimal.ZERO;
				BigDecimal chargeExcl;
				BigDecimal chargeIncl;
				boolean isTaxIncluded;

				int tax_ID = 0;
				MTax mTax = null;
				MAccount taxAccount = null;
				DocTax taxLine = null;
				
				isTaxIncluded = statementLine.isTaxIncluded();
				chargeIncl = line.getChargeAmt();
				chargeExcl = chargeIncl;

				//These fields are updated in the model's before save
				tax_ID = statementLine.getC_Tax_ID();
				VAT = statementLine.getTaxAmt();
				chargeExcl = chargeIncl.subtract( VAT );
				mTax = new MTax(statementLine.getCtx(), tax_ID, null);
				
				
				if (line.getChargeAmt().compareTo(Env.ZERO) > 0) {
					fl = fact.createLine(line,
							line.getChargeAccount(as, chargeExcl.negate()),
							line.getC_Currency_ID(), null, chargeExcl);
					if (fl != null && C_BPartner_ID != 0)
						fl.setC_BPartner_ID(C_BPartner_ID);
					if ( isTaxIncluded ) {
						
						//////////////////////////////
						//Create tax line
						////////////////////////////
						//From Doc_Invoice.loadTaxes()
						taxLine = new DocTax(tax_ID, mTax.getName(), mTax.getRate(),
								/*taxBaseAmt*/ chargeExcl, VAT /* amount */,  /* salesTax */ true);
						taxAccount = taxLine.getAccount(DocTax.ACCTTYPE_TaxDue, as);
						//Create tax line, because amount includes tax
						fl = fact.createLine(line,
								taxAccount,
								line.getC_Currency_ID(), null, VAT);
					}
				} else {
					fl = fact.createLine(line,
							line.getChargeAccount(as, chargeExcl.negate()),
							line.getC_Currency_ID(), chargeExcl.negate(), null);
					if (fl != null && C_BPartner_ID != 0)
						fl.setC_BPartner_ID(C_BPartner_ID);
					if ( isTaxIncluded ) {
						
						//////////////////////////////
						//Create tax line
						////////////////////////////
						taxLine = new DocTax(tax_ID, mTax.getName(), mTax.getRate(),
								/*taxBaseAmt*/ chargeExcl, VAT.negate() /* amount */,  /* salesTax */ false);
						//TODO: NCG Correct ACCTYPE below
						taxAccount = taxLine.getAccount(DocTax.ACCTTYPE_TaxExpense, as);
						fl = fact.createLine(line,
								taxAccount,
								line.getC_Currency_ID(), VAT.negate(), null);
					
					}
				}
				
			}
			if (fl != null && C_BPartner_ID != 0)
				fl.setC_BPartner_ID(C_BPartner_ID);

			//  Interest        DR      CR  (Interest)
			if (line.getInterestAmt().signum() < 0)
				fl = fact.createLine(line,
					getAccount(Doc.ACCTTYPE_InterestExp, as), getAccount(Doc.ACCTTYPE_InterestExp, as),
					line.getC_Currency_ID(), line.getInterestAmt().negate());
			else
				fl = fact.createLine(line,
					getAccount(Doc.ACCTTYPE_InterestRev, as), getAccount(Doc.ACCTTYPE_InterestRev, as),
					line.getC_Currency_ID(), line.getInterestAmt().negate());
			if (fl != null && C_BPartner_ID != 0)
				fl.setC_BPartner_ID(C_BPartner_ID);
			//
		//	fact.createTaxCorrection();
		}
		//
		ArrayList<Fact> facts = new ArrayList<Fact>();
		facts.add(fact);
		return facts;
	}   //  createFact
	
	/**
	 * 	Get AD_Org_ID from Bank Account
	 * 	@return AD_Org_ID or 0
	 */
	protected int getBank_Org_ID ()
	{
//		if (m_C_BankAccount_ID == 0)
//			return 0;
		//
//		MBankAccount ba = MBankAccount.get(getCtx(), m_C_BankAccount_ID);
		MBankAccount ba = MBankAccount.get(getCtx(), m_po.getC_BankAccount_ID());
		return ba.getAD_Org_ID();
	}	//	getBank_Org_ID
}
