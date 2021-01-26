package org.compiere.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.CLogger;
import org.compiere.util.Env;

/**
 * @author NCG
 * Date: 12 Sep 2014
 * Description:	
 */
public class ZZ_BankStatementLine extends MBankStatementLine implements I_ZZ_BankStatementLine {


	/** Logger */
	private static CLogger log = CLogger.getCLogger(ZZ_BankStatementLine.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ZZ_BankStatementLine(MBankStatement statement, int lineNo) {
		super(statement, lineNo);
	}

	public ZZ_BankStatementLine(MBankStatement statement) {
		super(statement);
	}

	public ZZ_BankStatementLine(Properties ctx, int C_BankStatementLine_ID,
			String trxName) {
		super(ctx, C_BankStatementLine_ID, trxName);
	}

	public ZZ_BankStatementLine(Properties ctx, ResultSet rs, String trxName) {
		super(ctx, rs, trxName);
	}
	
	public org.compiere.model.I_C_Tax getC_Tax() throws RuntimeException
    {
		return (org.compiere.model.I_C_Tax)MTable.get(getCtx(), org.compiere.model.I_C_Tax.Table_Name)
			.getPO(getC_Tax_ID(), get_TrxName());	}

	/** Set Tax.
		@param C_Tax_ID 
		Tax identifier
	  */
	public void setC_Tax_ID (int C_Tax_ID)
	{
		if (C_Tax_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_C_Tax_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_C_Tax_ID, Integer.valueOf(C_Tax_ID));
	}

	/** Get Tax.
		@return Tax identifier
	  */
	public int getC_Tax_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Tax_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}
	
	/** Set Tax Amount.
	@param TaxAmt 
	Tax Amount for a document
	 */
	public void setTaxAmt (BigDecimal TaxAmt)
	{
		set_Value (COLUMNNAME_TaxAmt, TaxAmt);
	}
	
	/** Get Tax Amount.
		@return Tax Amount for a document
	  */
	public BigDecimal getTaxAmt () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_TaxAmt);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}
	
	/** Set Price includes Tax.
	@param IsTaxIncluded 
	Tax is included in the price 
	*/
	public void setIsTaxIncluded (boolean IsTaxIncluded)
	{
		set_Value (COLUMNNAME_IsTaxIncluded, Boolean.valueOf(IsTaxIncluded));
	}
	
	/** Get Price includes Tax.
		@return Tax is included in the price 
	  */
	public boolean isTaxIncluded () 
	{
		Object oo = get_Value(COLUMNNAME_IsTaxIncluded);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}
	
	@Override
	protected boolean beforeSave(boolean newRecord) {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		boolean result = super.beforeSave(newRecord);
		if (!result) return result;
		updateTax();
		return true;
	}
	
	/**
	 * #1000248: Allow charges on the bank statement line to have tax implications
	 */
	private void updateTax() {
		
		BigDecimal VAT = BigDecimal.ZERO;
		BigDecimal chargeIncl;
		boolean isTaxIncluded;

		int tax_ID = 0;
		MTax mTax = null;
		
		isTaxIncluded = isTaxIncluded();
		chargeIncl = getChargeAmt();
//		chargeExcl = chargeIncl;
		
		if ( isTaxIncluded ) {
			MCharge mCharge = new MCharge(getCtx(), getC_Charge_ID(), get_TrxName());
			tax_ID = TaxCustom.get(getCtx(), mCharge.getC_TaxCategory_ID());
			mTax = new MTax(getCtx(), tax_ID, null);
			
			VAT = mTax.calculateTax(chargeIncl, true /*pl.isTaxIncluded()*/, 2);
			VAT = VAT.setScale( 2, BigDecimal.ROUND_HALF_UP );
			
			
		}
		
		// Write VAT back to statement line - for the VAT report
		setTaxAmt( VAT );
		setC_Tax_ID(tax_ID);
		
	}

}
