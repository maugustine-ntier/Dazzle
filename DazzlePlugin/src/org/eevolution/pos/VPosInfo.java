/******************************************************************************
 * Product: Adempiere ERP & CRM Smart Business Solution                       *
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
 * Copyright (C) 2003-2009 e-Evolution,SC. All Rights Reserved.               *
 * Contributor(s): Antonio Ca�averal www.e-evolution.com                      *
 * Contributor(s): Victor P�rez www.e-evolution.com                           *
 *****************************************************************************/

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.eevolution.pos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Properties;

import javax.swing.JOptionPane;

//import org.compiere.apps.form.FormFrame;
import org.compiere.model.MPOS;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;

/**
 *
 * @author antonio.canaveral@e-evolution.com, www.e-evolution.com
 * @author victor.perez@e-evolution.com, www.e-evolution.com
 */
public class VPosInfo {
	private MPOS p_pos = null;
	private int m_Cashier_ID;
	private Properties m_ctx = Env.getCtx();
	//private FormFrame m_frame;

	public int getCashier() {
		return m_Cashier_ID;
	}

	/**
	 * Get POSs for specific Sales Rep or all
	 *
	 * @param SalesRep_ID
	 * @return array of POS
	 */

	public MPOS getPOS() {
		return p_pos;
	}

	public MPOS[] getPOSs(int SalesRep_ID) {
		ArrayList<MPOS> list = new ArrayList<MPOS>();
		String sql = "SELECT * FROM C_POS WHERE SalesRep_ID=?";
		if (SalesRep_ID == 0)
			sql = "SELECT * FROM C_POS WHERE AD_Client_ID=?";
		PreparedStatement pstmt = null;
		try {
			pstmt = DB.prepareStatement(sql, null);
			if (SalesRep_ID != 0)
				pstmt.setInt(1, m_Cashier_ID);
			else
				pstmt.setInt(1, Env.getAD_Client_ID(m_ctx));
			ResultSet rs = pstmt.executeQuery();
			while (rs.next())
				list.add(new MPOS(m_ctx, rs, null));
			rs.close();
			pstmt.close();
			pstmt = null;
		} catch (Exception e) {

		}
		try {
			if (pstmt != null)
				pstmt.close();
			pstmt = null;
		} catch (Exception e) {
			pstmt = null;
		}
		MPOS[] retValue = new MPOS[list.size()];
		list.toArray(retValue);
		return retValue;
	} // getPOSs

	public void setCashier_ID(int value) {
		m_Cashier_ID = value;
	}

	public boolean setMPOS() {
		MPOS[] poss = null;
		if (m_Cashier_ID == 100) // superUser
			poss = getPOSs(0);
		else
			poss = getPOSs(m_Cashier_ID);
		//
		if (poss.length == 0) {

			return false;
		} else if (poss.length == 1) {
			p_pos = poss[0];
			return true;
		}

		// Select POS
		String msg = Msg.getMsg(m_ctx, "SelectPOS");
		String title = Env.getHeader(m_ctx, 0);
		Object selection = null;//JOptionPane.showInputDialog(m_frame, msg, title,
				//JOptionPane.QUESTION_MESSAGE, null, poss, poss[0]);
		if (selection != null) {
			p_pos = (MPOS) selection;
			;
			return true;
		}
		return false;
	} // setMPOS

	public void setMPOS(int C_POS_ID) {
		p_pos = MPOS.get(m_ctx, C_POS_ID);
	}

	public void setMPOS(MPOS pos) {
		p_pos = pos;
	}

}
