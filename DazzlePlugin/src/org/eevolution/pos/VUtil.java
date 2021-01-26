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
package org.eevolution.pos;

import java.awt.Color;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComboBox;

import org.adempiere.exceptions.AdempiereException;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MSysConfig;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.util.KeyNamePair;


/**
 *
 * @author antonio.canaveral@e-evolution.com, www.e-evolution.com
 * @author victor.perez@e-evolution.com, www.e-evolution.com
 */
public class VUtil {

	private Object component;
	private boolean valid;
	private int scope;
	private List<VUtil> arlList = new ArrayList<VUtil>();
	public static final int HEADER = 0;
	public static final int CLIENT = 1;
	public static final int CHECK = 2;
	public static final int VISA = 3;
	public static final int CREDITNOTE = 4;
	public static final int TRANSFER = 5;

	public static final Color BLUE = new Color(215, 224, 240);
	public static final Color RED = new Color(245, 214, 217);
	public static final Color HARD_RED = new Color(255, 0, 0);
	public static final Color GRAY = new Color(238, 238, 238);
	public static final Color WHITE = new Color(255, 255, 255);
	public static final Color BLACK = new Color(0, 0, 0);

	private static String msj;

	private static boolean error;

	/*public static boolean ask(String AD_Message) {
		boolean retValue = false;
		int m_WindowNo = Env.getContextAsInt(Env.getCtx(), "POS_WindowNo");
		Window parent = Env.getWindow(m_WindowNo);
		// retValue= ADialog.ask(m_WindowNo, parent, AD_Message);
		VDialog dialog = new VDialog(Env.getFrame(parent), true);
		dialog.setFocusCycleRoot(true);
		dialog.setAlwaysOnTop(true);
		if(parent==null)
			parent=dialog;
		Dimension dim = parent.getToolkit().getScreenSize();
		Rectangle abounds = dialog.getBounds();
		dialog.setLocation((dim.width - abounds.width) / 2,
				(dim.height - abounds.height) / 2);
		// Martin commented
		// dialog.setMsj(AD_Message);

		dialog.setMsj(Msg.translate(Env.getCtx(), AD_Message)); // martin changed

		dialog.setInfoIcon(VDialog.QUESTION_MESSAGE);
		dialog.requestFocus(); // Martin Added
		dialog.okButton.requestFocus();
		dialog.setVisible(true);
		if (dialog.getReturnStatus() == VTransfer.RET_OK)
			retValue = true;

		return retValue;
	}*/

	public static String getParameter(String param) {
		return MSysConfig.getValue("POS_" + param);
	}

	public static int getParameterAsInt(String param) {
		return MSysConfig.getIntValue("POS_" + param, 0);
	}

	public static double julianDate(Timestamp today) {
		int D = today.getDay();
		int M = today.getMonth();
		int Y = today.getYear() + 1900;
		int H = today.getHours();
		int H1 = today.getMinutes();
		int H2 = today.getSeconds();
		int G = 0;
		int S = Y + M / 100 + D / 10000;
		String com;
		if (S <= 1582.1004)
			G = 0;
		if (S >= 1582.1015)
			G = 1;
		if (S > 1582.1004 && S < 1582.1015) {
			G = -1;
			com = "Esta fecha no existe. Por la reforma gregoriana se pasa del 4 al 15 de Octubre de 1582.";
		} else {
			com = " ";
		}
		H = H + ((H2 / 60 + H1) / 60);
		D = D + H / 24;
		double D1 = Math.floor(D);
		double F = D - D1 - 0.5;
		double J = -1 * Math.floor(7 * (Math.floor((M + 9) / 12) + Y) / 4);
		double J1;
		S = 1;
		if (G == 1) {
			if ((M - 9) < 0)
				S = -1;
			double A = Math.abs(M - 9);
			J1 = Math.floor(Y + S * Math.floor(A / 7));
			J1 = -1 * Math.floor((Math.floor(J1 / 100) + 1) * 3 / 4);
		} else
			J1 = 0;
		J = J + Math.floor(275 * M / 9) + D1 + G * J1;
		J = J + 1721027 + 2 * G + 367 * Y;
		if (F < 0) {
			F = F + 1;
			J = J - 1;
		}
		J = J + F;
		if (G == -1)
			J = 0;

		return J;
	}

	public static boolean previusAdded(MInOut iout, int M_Product_ID) {
		boolean res = false;
		MInOutLine[] lines = iout.getLines(false);
		for (MInOutLine line : lines) {
			if (line.getM_Product_ID() == M_Product_ID) {
				return true;
			}
		}
		return res;
	}

	public static boolean previusAdded(MOrder order, int M_Product_ID) {
		boolean res = false;
		MOrderLine[] lines = order.getLines(true, "M_Product_ID");
		for (MOrderLine line : lines) {
			if (line.getM_Product_ID() == M_Product_ID) {
				return true;
			}
		}
		return res;
	}

	public static void setMsg(String _msg, boolean isError) {
		// Martin commented
		//msj = _msg;
		/*msj = Msg.translate(Env.getCtx(), _msg); // Martin added
		error = isError;
		int m_WindowNo = Env.getContextAsInt(Env.getCtx(), "POS_WindowNo");
		Window parent = null;

		if (m_WindowNo <= 0)
			parent = Env.getWindow(m_WindowNo);

		VDialog dialog = new VDialog(Env.getFrame(parent), true);
		if(parent==null)
			parent=dialog;
		dialog.setAlwaysOnTop(true);
		Dimension dim = parent.getToolkit().getScreenSize();
		Rectangle abounds = dialog.getBounds();
		dialog.setLocation((dim.width - abounds.width) / 2,
				(dim.height - abounds.height) / 2);
		dialog.setMsj(msj);

		if (error)
			dialog.setInfoIcon(VDialog.ERROR_MESSAGE);
		else
			dialog.setInfoIcon(VDialog.WARNING_MESSAGE);
		dialog.requestFocus(); // Martin added
		dialog.continueButton.requestFocus();
		dialog.setVisible(true);*/

	}

	/*
	 * public static void loadComboFromQuery(ComboBox combo,Query q,String
	 * columnName) { if(q!=null) { combo.removeAllItems(); KeyNamePair pp = new
	 * KeyNamePair(0, "");
	 * 
	 * if(!combo.isMandatory()) { try{ combo.addItem(pp); }catch (Exception e) {
	 * System.err.println("No se le para bola"); } }
	 * 
	 * List<PO> list = q.list();
	 * 
	 * for(PO element:list) { pp = new KeyNamePair(element.get_ID(), (String)
	 * element.get_Value(columnName)); combo.addItem(pp); } }else { throw new
	 * AdempiereException("DataNotFound "+combo.getName()); } }
	 */

	// Martin added
	public static void loadComboFromQuery(JComboBox combo,Query q,String columnName)
	{
		if(q!=null)
		{
			combo.removeAllItems();
			KeyNamePair pp = new KeyNamePair(0, "");

			//if(!combo.isMandatory())
			//{
				try{
					combo.addItem(pp);
				}catch (Exception e)
				{
					System.err.println("No se le para bola");
				}
			//}

			List<PO> list = q.list();

			for(PO element:list)
			{
				pp = new KeyNamePair(element.get_ID(), (String) element.get_Value(columnName));
				combo.addItem(pp);
			}
		}else
		{
			throw new AdempiereException("DataNotFound "+combo.getName());
		}
	}

	public VUtil() {
	}

	public VUtil(Object _component, int _scope) {
		component = _component;
		scope = _scope;
	}

	public void addVPValidator(VUtil val) {
		arlList.add(val);
	}

	public void disableAll() {
		for (int i = 0; i < arlList.size(); i++) {
			if (arlList.get(i).getObject().getClass().getName().compareTo(
					"javax.swing.JTextField") == 0) {
				javax.swing.JTextField kk = (javax.swing.JTextField) arlList
						.get(i).getObject();
				kk.setEditable(false);
			} else if (arlList.get(i).getObject().getClass().getName()
					.compareTo("javax.swing.JComboBox") == 0) {
				javax.swing.JComboBox kk = (javax.swing.JComboBox) arlList.get(
						i).getObject();
				kk.setEnabled(false);
			}
		}
	}

	public void disableAll(int[] noDisable) {
		for (int i = 0; i < arlList.size(); i++) {
			if (needToDisable(noDisable, arlList.get(i).getScope())) {
				if (arlList.get(i).getObject().getClass().getName().compareTo(
						"javax.swing.JTextField") == 0) {
					javax.swing.JTextField kk = (javax.swing.JTextField) arlList
							.get(i).getObject();
					kk.setEditable(false);
				} else if (arlList.get(i).getObject().getClass().getName()
						.compareTo("javax.swing.JComboBox") == 0) {
					javax.swing.JComboBox kk = (javax.swing.JComboBox) arlList
							.get(i).getObject();
					kk.setEnabled(false);
				}
			}
		}
	}

	public void enableAll() {
		for (int i = 0; i < arlList.size(); i++) {
			if (arlList.get(i).getObject().getClass().getName().compareTo(
					"javax.swing.JTextField") == 0) {
				javax.swing.JTextField kk = (javax.swing.JTextField) arlList
						.get(i).getObject();
				kk.setEditable(true);
			} else if (arlList.get(i).getObject().getClass().getName()
					.compareTo("javax.swing.JComboBox") == 0) {
				javax.swing.JComboBox kk = (javax.swing.JComboBox) arlList.get(
						i).getObject();
				kk.setEnabled(true);
			}
		}
	}

	public Object getObject() {
		return component;
	}

	public int getScope() {
		return scope;
	}

	private boolean needToDisable(int[] noDisable, int toFind) {
		for (int i = 0; i < noDisable.length; i++) {
			if (noDisable[i] == toFind)
				return false;
		}
		return true;
	}
	public void paintAllColors() {
		for (int i = 0; i < arlList.size(); i++) {
			if (arlList.get(i).getObject().getClass().getName().compareTo(
					"javax.swing.JTextField") == 0) {
				javax.swing.JTextField kk = (javax.swing.JTextField) arlList
						.get(i).getObject();
				if (kk.getText().length() != 0)
					kk.setBackground(BLUE);
				else
					kk.setBackground(RED);
			} else if (arlList.get(i).getObject().getClass().getName()
					.compareTo("javax.swing.JComboBox") == 0) {
				javax.swing.JComboBox kk = (javax.swing.JComboBox) arlList.get(
						i).getObject();
				if (kk.getSelectedItem() != null)
					kk.setBackground(BLUE);
				else
					kk.setBackground(RED);
			}
		}
	}

	public void paintColor(Object field, Color c, Color font) {
		if (field.getClass().getName().compareTo("javax.swing.JTextField") == 0) {
			javax.swing.JTextField kk = (javax.swing.JTextField) field;
			kk.setBackground(c);
			kk.setForeground(font);
		} else if (field.getClass().getName()
				.compareTo("javax.swing.JComboBox") == 0) {
			javax.swing.JComboBox kk = (javax.swing.JComboBox) field;
			kk.setBackground(c);
			kk.setForeground(font);
		}
	}

	public void paintInBlack(Object field) {
		if (field.getClass().getName().compareTo("javax.swing.JTextField") == 0) {
			javax.swing.JTextField kk = (javax.swing.JTextField) field;
			kk.setBackground(HARD_RED);
			kk.setForeground(WHITE);
		} else if (field.getClass().getName()
				.compareTo("javax.swing.JComboBox") == 0) {
			javax.swing.JComboBox kk = (javax.swing.JComboBox) field;
			kk.setBackground(HARD_RED);
			kk.setForeground(WHITE);
		}
	}

	public boolean paintScopeColors(int _scope) {
		boolean isblue = true;
		for (int i = 0; i < arlList.size(); i++) {
			if (arlList.get(i).getScope() == _scope) {
				if (arlList.get(i).getObject().getClass().getName().compareTo(
						"javax.swing.JTextField") == 0) {
					javax.swing.JTextField kk = (javax.swing.JTextField) arlList
							.get(i).getObject();
					if (kk.getText().length() != 0)
						kk.setBackground(BLUE);
					else {
						kk.setBackground(RED);
						isblue = false;
					}
					kk.setForeground(BLACK);
				} else if (arlList.get(i).getObject().getClass().getName()
						.compareTo("javax.swing.JComboBox") == 0) {
					javax.swing.JComboBox kk = (javax.swing.JComboBox) arlList
							.get(i).getObject();
					if (kk.getSelectedItem() != null)
						kk.setBackground(BLUE);
					else {
						kk.setBackground(RED);
						isblue = false;
					}
					kk.setForeground(BLACK);
				}
			}
		}
		return isblue;
	}

	public boolean validateAll() {
		boolean res = false;

		return res;
	}

	public boolean validateScope(int _scope) {
		return paintScopeColors(_scope);
	}
}
