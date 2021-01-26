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

/** Generated Interface for ZZ_Recon_Hdr
 *  @author iDempiere (generated) 
 *  @version Release 1.0a
 */
public interface I_ZZ_Recon_Hdr 
{

    /** TableName=ZZ_Recon_Hdr */
    public static final String Table_Name = "ZZ_Recon_Hdr";

    /** AD_Table_ID=1000000 */
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

	/** Set Organization.
	  * Organizational entity within client
	  */
	public void setAD_Org_ID (int AD_Org_ID);

	/** Get Organization.
	  * Organizational entity within client
	  */
	public int getAD_Org_ID();

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

    /** Column name DocumentNo */
    public static final String COLUMNNAME_DocumentNo = "DocumentNo";

	/** Set Document No.
	  * Document sequence number of the document
	  */
	public void setDocumentNo (String DocumentNo);

	/** Get Document No.
	  * Document sequence number of the document
	  */
	public String getDocumentNo();

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

    /** Column name Processed */
    public static final String COLUMNNAME_Processed = "Processed";

	/** Set Processed.
	  * The document has been processed
	  */
	public void setProcessed (boolean Processed);

	/** Get Processed.
	  * The document has been processed
	  */
	public boolean isProcessed();

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

    /** Column name ZZ_Cash */
    public static final String COLUMNNAME_ZZ_Cash = "ZZ_Cash";

	/** Set Cash	  */
	public void setZZ_Cash (BigDecimal ZZ_Cash);

	/** Get Cash	  */
	public BigDecimal getZZ_Cash();

    /** Column name ZZ_Cheques */
    public static final String COLUMNNAME_ZZ_Cheques = "ZZ_Cheques";

	/** Set Cheques	  */
	public void setZZ_Cheques (BigDecimal ZZ_Cheques);

	/** Get Cheques	  */
	public BigDecimal getZZ_Cheques();

    /** Column name ZZ_Credit_Cards */
    public static final String COLUMNNAME_ZZ_Credit_Cards = "ZZ_Credit_Cards";

	/** Set Credit Cards	  */
	public void setZZ_Credit_Cards (BigDecimal ZZ_Credit_Cards);

	/** Get Credit Cards	  */
	public BigDecimal getZZ_Credit_Cards();

    /** Column name ZZ_Direct_Deposits */
    public static final String COLUMNNAME_ZZ_Direct_Deposits = "ZZ_Direct_Deposits";

	/** Set Direct Deposits	  */
	public void setZZ_Direct_Deposits (BigDecimal ZZ_Direct_Deposits);

	/** Get Direct Deposits	  */
	public BigDecimal getZZ_Direct_Deposits();

    /** Column name ZZ_Float */
    public static final String COLUMNNAME_ZZ_Float = "ZZ_Float";

	/** Set Float	  */
	public void setZZ_Float (BigDecimal ZZ_Float);

	/** Get Float	  */
	public BigDecimal getZZ_Float();

    /** Column name ZZ_Recon_Date */
    public static final String COLUMNNAME_ZZ_Recon_Date = "ZZ_Recon_Date";

	/** Set Recon Date	  */
	public void setZZ_Recon_Date (Timestamp ZZ_Recon_Date);

	/** Get Recon Date	  */
	public Timestamp getZZ_Recon_Date();

    /** Column name ZZ_Recon_Hdr_ID */
    public static final String COLUMNNAME_ZZ_Recon_Hdr_ID = "ZZ_Recon_Hdr_ID";

	/** Set Recon Header	  */
	public void setZZ_Recon_Hdr_ID (int ZZ_Recon_Hdr_ID);

	/** Get Recon Header	  */
	public int getZZ_Recon_Hdr_ID();
}
