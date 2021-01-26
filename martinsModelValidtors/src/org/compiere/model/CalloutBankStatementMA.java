/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2006 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
package org.compiere.model;

import java.math.BigDecimal;
import java.util.Properties;

import org.compiere.util.Env;


/**
 *	Bank Statement Callout
 *
 *  @author Jorg Janke
 *  @version $Id: CalloutBankStatement.java,v 1.3 2006/07/30 00:51:05 jjanke Exp $
 */
public class CalloutBankStatementMA extends CalloutEngine
{
	/**
	 * 	Bank Account Changed.
	 * 	Update Beginning Balance
	 *	@param ctx context
	 *	@param WindowNo window no
	 *	@param mTab tab
	 *	@param mField field
	 *	@param value value
	 *	@return null or error message
	 */
	public String depositbatch (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		if (value == null) {
			mTab.setValue("TrxAmt", Env.ZERO);
			mTab.setValue("StmtAmt", Env.ZERO);
			return "";
		}
		int C_DepositBatch_ID = ((Integer)value).intValue();
		if (C_DepositBatch_ID > 0) {
			MDepositBatch db = new MDepositBatch(Env.getCtx(), C_DepositBatch_ID,null);
			mTab.setValue("TrxAmt", db.getDepositAmt());
			mTab.setValue("StmtAmt", db.getDepositAmt());
		}
		return "";
	}	//	bankAccount


	/**
	 * NCG 2014/09/09 #1000248: Allow charges on the bank statement line to have tax implications
	 */
	public String charge (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		if (value == null) {
			return "";
		}
		int chargeID = ((Integer)value).intValue();
		MCharge charge = new MCharge(ctx, chargeID, null);
		if (charge.isTaxIncluded()) {
			mTab.setValue("IsTaxIncluded", true);
		} else {
			mTab.setValue("IsTaxIncluded", false);
		}
		return "";
	}
	
	/**
	 * NCG 2015/04/30: #RQ100006: Interest amount on bank statement has incorrect value due to difficult screen logic
	 */
	public String amount (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		if (isCalloutActive())
			return "";

		//  Get Stmt & Trx & ChargeAmt
		BigDecimal stmt = (BigDecimal)mTab.getValue("StmtAmt");
		if (stmt == null)
			stmt = Env.ZERO;
		BigDecimal trx = (BigDecimal)mTab.getValue("TrxAmt");
		if (trx == null)
			trx = Env.ZERO;
		BigDecimal charge = (BigDecimal)mTab.getValue("ChargeAmt");
		if (charge == null)
			charge = Env.ZERO;
		BigDecimal interest = (BigDecimal)mTab.getValue("InterestAmt");
		if (interest == null)
			interest = Env.ZERO;
		BigDecimal bd = stmt.subtract(trx.add(interest));
		
		if (mField.getColumnName().equals("StmtAmt")) {
			mTab.setValue("ChargeAmt", bd);
		} else if (mField.getColumnName().equals("TrxAmt")) {
			mTab.setValue("ChargeAmt", bd);
		} else if (mField.getColumnName().equals("ChargeAmt")) {
			//nTier - new logic
			trx = stmt.subtract(charge.add(interest));
			mTab.setValue("TrxAmt", trx);
		} else if (mField.getColumnName().equals("InterestAmt")) {
			trx = stmt.subtract(charge.add(interest));
			mTab.setValue("TrxAmt", trx);
		}
		
		return "";
	}   //  amount

}	//	CalloutBankStatementMA
