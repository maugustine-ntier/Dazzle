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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

import org.compiere.util.DB;
import org.compiere.util.Env;


/**
 *	Bank Statement Callout
 *
 *  @author Jorg Janke
 *  @version $Id: CalloutBankStatement.java,v 1.3 2006/07/30 00:51:05 jjanke Exp $
 */
public class CalloutMovementConfirmMA extends CalloutEngine
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
	public String confirmedQty (Properties ctx, int WindowNo, GridTab mTab, GridField mField, Object value)
	{
		if (mTab.getValue("Description") == null) {
			mTab.setValue("Description",mTab.getValue("TargetQty") == null ? "0" : ((BigDecimal)mTab.getValue("TargetQty")).toPlainString());
		}
		if (value == null) {
			mTab.setValue("TargetQty", Env.ZERO);
			return "";
		}
		mTab.setValue("TargetQty", value);
		return "";
	}	//	bankAccount



}	//	CalloutBankStatementMA
