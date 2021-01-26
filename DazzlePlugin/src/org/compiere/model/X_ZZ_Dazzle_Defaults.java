/******************************************************************************
 * Product: iDempiere ERP & CRM Smart Business Solution                       *
 * Copyright (C) 1999-2012 ComPiere, Inc. All Rights Reserved.                *
 * This program is free software, you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY, without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program, if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 * For the text or an alternative of this public license, you may reach us    *
 * ComPiere, Inc., 2620 Augustine Dr. #245, Santa Clara, CA 95054, USA        *
 * or via info@compiere.org or http://www.compiere.org/license.html           *
 *****************************************************************************/
/** Generated Model - DO NOT CHANGE */
package org.compiere.model;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.Properties;
import org.compiere.util.Env;

/** Generated Model for ZZ_Dazzle_Defaults
 *  @author iDempiere (generated) 
 *  @version Release 1.0c - $Id$ */
public class X_ZZ_Dazzle_Defaults extends PO implements I_ZZ_Dazzle_Defaults, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20140306L;

    /** Standard Constructor */
    public X_ZZ_Dazzle_Defaults (Properties ctx, int ZZ_Dazzle_Defaults_ID, String trxName)
    {
      super (ctx, ZZ_Dazzle_Defaults_ID, trxName);
      /** if (ZZ_Dazzle_Defaults_ID == 0)
        {
			setC_Charge_Discount_ID (0);
			setZZ_Dazzle_Defaults_ID (0);
        } */
    }

    /** Load Constructor */
    public X_ZZ_Dazzle_Defaults (Properties ctx, ResultSet rs, String trxName)
    {
      super (ctx, rs, trxName);
    }

    /** AccessLevel
      * @return 3 - Client - Org 
      */
    protected int get_AccessLevel()
    {
      return accessLevel.intValue();
    }

    /** Load Meta Data */
    protected POInfo initPO (Properties ctx)
    {
      POInfo poi = POInfo.getPOInfo (ctx, Table_ID, get_TrxName());
      return poi;
    }

    public String toString()
    {
      StringBuffer sb = new StringBuffer ("X_ZZ_Dazzle_Defaults[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	public org.compiere.model.I_C_Charge getC_Charge_Discount() throws RuntimeException
    {
		return (org.compiere.model.I_C_Charge)MTable.get(getCtx(), org.compiere.model.I_C_Charge.Table_Name)
			.getPO(getC_Charge_Discount_ID(), get_TrxName());	}

	/** Set Discount.
		@param C_Charge_Discount_ID Discount	  */
	public void setC_Charge_Discount_ID (int C_Charge_Discount_ID)
	{
		if (C_Charge_Discount_ID < 1) 
			set_Value (COLUMNNAME_C_Charge_Discount_ID, null);
		else 
			set_Value (COLUMNNAME_C_Charge_Discount_ID, Integer.valueOf(C_Charge_Discount_ID));
	}

	/** Get Discount.
		@return Discount	  */
	public int getC_Charge_Discount_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Charge_Discount_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	public org.compiere.model.I_C_Charge getC_Charge_RoundCents() throws RuntimeException
    {
		return (org.compiere.model.I_C_Charge)MTable.get(getCtx(), org.compiere.model.I_C_Charge.Table_Name)
			.getPO(getC_Charge_RoundCents_ID(), get_TrxName());	}

	/** Set Round Cents.
		@param C_Charge_RoundCents_ID Round Cents	  */
	public void setC_Charge_RoundCents_ID (int C_Charge_RoundCents_ID)
	{
		if (C_Charge_RoundCents_ID < 1) 
			set_Value (COLUMNNAME_C_Charge_RoundCents_ID, null);
		else 
			set_Value (COLUMNNAME_C_Charge_RoundCents_ID, Integer.valueOf(C_Charge_RoundCents_ID));
	}

	/** Get Round Cents.
		@return Round Cents	  */
	public int getC_Charge_RoundCents_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_C_Charge_RoundCents_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set Limit Percent.
		@param limit_percent Limit Percent	  */
	public void setlimit_percent (BigDecimal limit_percent)
	{
		set_Value (COLUMNNAME_limit_percent, limit_percent);
	}

	/** Get Limit Percent.
		@return Limit Percent	  */
	public BigDecimal getlimit_percent () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_limit_percent);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Dazzle Defaults.
		@param ZZ_Dazzle_Defaults_ID Dazzle Defaults	  */
	public void setZZ_Dazzle_Defaults_ID (int ZZ_Dazzle_Defaults_ID)
	{
		if (ZZ_Dazzle_Defaults_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_ZZ_Dazzle_Defaults_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_ZZ_Dazzle_Defaults_ID, Integer.valueOf(ZZ_Dazzle_Defaults_ID));
	}

	/** Get Dazzle Defaults.
		@return Dazzle Defaults	  */
	public int getZZ_Dazzle_Defaults_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_ZZ_Dazzle_Defaults_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

	/** Set ZZ_Dazzle_Defaults_UU.
		@param ZZ_Dazzle_Defaults_UU ZZ_Dazzle_Defaults_UU	  */
	public void setZZ_Dazzle_Defaults_UU (String ZZ_Dazzle_Defaults_UU)
	{
		set_Value (COLUMNNAME_ZZ_Dazzle_Defaults_UU, ZZ_Dazzle_Defaults_UU);
	}

	/** Get ZZ_Dazzle_Defaults_UU.
		@return ZZ_Dazzle_Defaults_UU	  */
	public String getZZ_Dazzle_Defaults_UU () 
	{
		return (String)get_Value(COLUMNNAME_ZZ_Dazzle_Defaults_UU);
	}
}