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
import java.sql.Timestamp;
import java.util.Properties;
import org.compiere.util.Env;
import org.compiere.util.KeyNamePair;

/** Generated Model for ZZ_Recon_Hdr
 *  @author iDempiere (generated) 
 *  @version Release 1.0a - $Id$ */
public class X_ZZ_Recon_Hdr extends PO implements I_ZZ_Recon_Hdr, I_Persistent 
{

	/**
	 *
	 */
	private static final long serialVersionUID = 20130522L;

    /** Standard Constructor */
    public X_ZZ_Recon_Hdr (Properties ctx, int ZZ_Recon_Hdr_ID, String trxName)
    {
      super (ctx, ZZ_Recon_Hdr_ID, trxName);
      /** if (ZZ_Recon_Hdr_ID == 0)
        {
			setZZ_Recon_Hdr_ID (0);
        } */
    }

    /** Load Constructor */
    public X_ZZ_Recon_Hdr (Properties ctx, ResultSet rs, String trxName)
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
      StringBuffer sb = new StringBuffer ("X_ZZ_Recon_Hdr[")
        .append(get_ID()).append("]");
      return sb.toString();
    }

	/** Set Document No.
		@param DocumentNo 
		Document sequence number of the document
	  */
	public void setDocumentNo (String DocumentNo)
	{
		set_ValueNoCheck (COLUMNNAME_DocumentNo, DocumentNo);
	}

	/** Get Document No.
		@return Document sequence number of the document
	  */
	public String getDocumentNo () 
	{
		return (String)get_Value(COLUMNNAME_DocumentNo);
	}

	/** Set Processed.
		@param Processed 
		The document has been processed
	  */
	public void setProcessed (boolean Processed)
	{
		set_Value (COLUMNNAME_Processed, Boolean.valueOf(Processed));
	}

	/** Get Processed.
		@return The document has been processed
	  */
	public boolean isProcessed () 
	{
		Object oo = get_Value(COLUMNNAME_Processed);
		if (oo != null) 
		{
			 if (oo instanceof Boolean) 
				 return ((Boolean)oo).booleanValue(); 
			return "Y".equals(oo);
		}
		return false;
	}

	/** Set Cash.
		@param ZZ_Cash Cash	  */
	public void setZZ_Cash (BigDecimal ZZ_Cash)
	{
		set_Value (COLUMNNAME_ZZ_Cash, ZZ_Cash);
	}

	/** Get Cash.
		@return Cash	  */
	public BigDecimal getZZ_Cash () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_ZZ_Cash);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Cheques.
		@param ZZ_Cheques Cheques	  */
	public void setZZ_Cheques (BigDecimal ZZ_Cheques)
	{
		set_Value (COLUMNNAME_ZZ_Cheques, ZZ_Cheques);
	}

	/** Get Cheques.
		@return Cheques	  */
	public BigDecimal getZZ_Cheques () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_ZZ_Cheques);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Credit Cards.
		@param ZZ_Credit_Cards Credit Cards	  */
	public void setZZ_Credit_Cards (BigDecimal ZZ_Credit_Cards)
	{
		set_Value (COLUMNNAME_ZZ_Credit_Cards, ZZ_Credit_Cards);
	}

	/** Get Credit Cards.
		@return Credit Cards	  */
	public BigDecimal getZZ_Credit_Cards () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_ZZ_Credit_Cards);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Direct Deposits.
		@param ZZ_Direct_Deposits Direct Deposits	  */
	public void setZZ_Direct_Deposits (BigDecimal ZZ_Direct_Deposits)
	{
		set_Value (COLUMNNAME_ZZ_Direct_Deposits, ZZ_Direct_Deposits);
	}

	/** Get Direct Deposits.
		@return Direct Deposits	  */
	public BigDecimal getZZ_Direct_Deposits () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_ZZ_Direct_Deposits);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Float.
		@param ZZ_Float Float	  */
	public void setZZ_Float (BigDecimal ZZ_Float)
	{
		set_Value (COLUMNNAME_ZZ_Float, ZZ_Float);
	}

	/** Get Float.
		@return Float	  */
	public BigDecimal getZZ_Float () 
	{
		BigDecimal bd = (BigDecimal)get_Value(COLUMNNAME_ZZ_Float);
		if (bd == null)
			 return Env.ZERO;
		return bd;
	}

	/** Set Recon Date.
		@param ZZ_Recon_Date Recon Date	  */
	public void setZZ_Recon_Date (Timestamp ZZ_Recon_Date)
	{
		set_Value (COLUMNNAME_ZZ_Recon_Date, ZZ_Recon_Date);
	}

	/** Get Recon Date.
		@return Recon Date	  */
	public Timestamp getZZ_Recon_Date () 
	{
		return (Timestamp)get_Value(COLUMNNAME_ZZ_Recon_Date);
	}

	/** Set Recon Header.
		@param ZZ_Recon_Hdr_ID Recon Header	  */
	public void setZZ_Recon_Hdr_ID (int ZZ_Recon_Hdr_ID)
	{
		if (ZZ_Recon_Hdr_ID < 1) 
			set_ValueNoCheck (COLUMNNAME_ZZ_Recon_Hdr_ID, null);
		else 
			set_ValueNoCheck (COLUMNNAME_ZZ_Recon_Hdr_ID, Integer.valueOf(ZZ_Recon_Hdr_ID));
	}

	/** Get Recon Header.
		@return Recon Header	  */
	public int getZZ_Recon_Hdr_ID () 
	{
		Integer ii = (Integer)get_Value(COLUMNNAME_ZZ_Recon_Hdr_ID);
		if (ii == null)
			 return 0;
		return ii.intValue();
	}

    /** Get Record ID/ColumnName
        @return ID/ColumnName pair
      */
    public KeyNamePair getKeyNamePair() 
    {
        return new KeyNamePair(get_ID(), String.valueOf(getZZ_Recon_Hdr_ID()));
    }
}