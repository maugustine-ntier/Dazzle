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
package org.compiere.model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import org.compiere.util.KeyNamePair;

/** Generated Interface for ZZ_Dazzle_Defaults
 *  @author iDempiere (generated) 
 *  @version Release 1.0c
 */
public interface I_ZZ_Dazzle_Defaults 
{

    /** TableName=ZZ_Dazzle_Defaults */
    public static final String Table_Name = "ZZ_Dazzle_Defaults";

    /** AD_Table_ID=1000004 */
    public static final int Table_ID = MTable.getTable_ID(Table_Name);

    KeyNamePair Model = new KeyNamePair(Table_ID, Table_Name);

    /** AccessLevel = 3 - Client - Org 
     */
    BigDecimal accessLevel = BigDecimal.valueOf(3);

    /** Load Meta Data */

    /** Column name AD_Client_ID */
    public static final String COLUMNNAME_AD_Client_ID = "AD_Client_ID";

	/** Get Client.
	  * Client/Tenant for this installation.
	  */
	public int getAD_Client_ID();

    /** Column name AD_Org_ID */
    public static final String COLUMNNAME_AD_Org_ID = "AD_Org_ID";

	/** Set Branch.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Branch.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();

    /** Column name C_Charge_Discount_ID */
    public static final String COLUMNNAME_C_Charge_Discount_ID = "C_Charge_Discount_ID";

	/** Set Discount	  */
	public void setC_Charge_Discount_ID (int C_Charge_Discount_ID);

	/** Get Discount	  */
	public int getC_Charge_Discount_ID();

	public org.compiere.model.I_C_Charge getC_Charge_Discount() throws RuntimeException;

    /** Column name C_Charge_RoundCents_ID */
    public static final String COLUMNNAME_C_Charge_RoundCents_ID = "C_Charge_RoundCents_ID";

	/** Set Round Cents	  */
	public void setC_Charge_RoundCents_ID (int C_Charge_RoundCents_ID);

	/** Get Round Cents	  */
	public int getC_Charge_RoundCents_ID();

	public org.compiere.model.I_C_Charge getC_Charge_RoundCents() throws RuntimeException;

    /** Column name Created */
    public static final String COLUMNNAME_Created = "Created";

	/** Get Created.
	  * Date this record was created
	  */
	public Timestamp getCreated();

    /** Column name CreatedBy */
    public static final String COLUMNNAME_CreatedBy = "CreatedBy";

	/** Get Created By.
	  * User who created this records
	  */
	public int getCreatedBy();

    /** Column name IsActive */
    public static final String COLUMNNAME_IsActive = "IsActive";

	/** Set Active.
	  * The record is active in the system
	  */
	public void setIsActive (boolean IsActive);

	/** Get Active.
	  * The record is active in the system
	  */
	public boolean isActive();

    /** Column name limit_percent */
    public static final String COLUMNNAME_limit_percent = "limit_percent";

	/** Set Limit Percent	  */
	public void setlimit_percent (BigDecimal limit_percent);

	/** Get Limit Percent	  */
	public BigDecimal getlimit_percent();

    /** Column name Updated */
    public static final String COLUMNNAME_Updated = "Updated";

	/** Get Updated.
	  * Date this record was updated
	  */
	public Timestamp getUpdated();

    /** Column name UpdatedBy */
    public static final String COLUMNNAME_UpdatedBy = "UpdatedBy";

	/** Get Updated By.
	  * User who updated this records
	  */
	public int getUpdatedBy();

    /** Column name ZZ_Dazzle_Defaults_ID */
    public static final String COLUMNNAME_ZZ_Dazzle_Defaults_ID = "ZZ_Dazzle_Defaults_ID";

	/** Set Dazzle Defaults	  */
	public void setZZ_Dazzle_Defaults_ID (int ZZ_Dazzle_Defaults_ID);

	/** Get Dazzle Defaults	  */
	public int getZZ_Dazzle_Defaults_ID();

    /** Column name ZZ_Dazzle_Defaults_UU */
    public static final String COLUMNNAME_ZZ_Dazzle_Defaults_UU = "ZZ_Dazzle_Defaults_UU";

	/** Set ZZ_Dazzle_Defaults_UU	  */
	public void setZZ_Dazzle_Defaults_UU (String ZZ_Dazzle_Defaults_UU);

	/** Get ZZ_Dazzle_Defaults_UU	  */
	public String getZZ_Dazzle_Defaults_UU();
}
