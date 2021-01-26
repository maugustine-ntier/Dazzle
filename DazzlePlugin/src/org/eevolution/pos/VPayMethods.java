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

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;

import org.compiere.model.MAllocationHdr;
import org.compiere.model.MAllocationLine;
import org.compiere.model.MBank;
import org.compiere.model.MCurrency;
import org.compiere.model.MInvoice;
import org.compiere.model.MRefList;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.util.Trx;
import org.compiere.util.ValueNamePair;
//import org.eevolution.pos.net.VCardValidator;

/**
 * 
 * @author antonio.canaveral@e-evolution.com, www.e-evolution.com
 * @author victor.perez@e-evolution.com, www.e-evolution.com
 */
public class VPayMethods {

	// Martin Added
	private int C_Invoice_ID = 0;
	
	private BigDecimal total = null;
	private BigDecimal subtotal = null;
	private BigDecimal change = null;
	private BigDecimal tax = null;
	private BigDecimal cash = null;
	/*
	 * private String cardTrack=""; private String cardName; private String
	 * cardNameTmp=""; private boolean isCardName=false;
	 */
	private String millesCard;
	private String selectedCard;
	private boolean hasCreditPayments = false;

	private LinkedList<ValueNamePair> code = new LinkedList<ValueNamePair>();
	private LinkedList<ValueNamePair> currency = new LinkedList<ValueNamePair>();
	private CLogger log = CLogger.getCLogger(VPayMethods.class);

	private List<VPayMethodLine> arlList = new ArrayList<VPayMethodLine>();

	public VPayMethods() {

	}

	public void addMethod(VPayMethodLine line) {
		if (line.getLineNumber() < arlList.size())
			arlList.set(line.getLineNumber(), line);
		else
			arlList.add(line);
	}

	public void allocation(MInvoice inv) {
		String trans = Trx.createTrxName();
		MAllocationHdr a = new MAllocationHdr(Env.getCtx(), true,
				new Timestamp(System.currentTimeMillis()), inv
						.getC_Currency_ID(), inv.getDocumentNo(), trans);
		a.setAD_Org_ID(inv.getAD_Org_ID());
		if (!a.save(trans)) {
			log.log(Level.SEVERE, "Allocation not created");
		}

		MAllocationLine al = new MAllocationLine(Env.getCtx(), 0, trans);
		al.setC_Invoice_ID(inv.getC_Invoice_ID());
		al.setAmount(inv.getGrandTotal());
		for (VPayMethodLine line : arlList) {
			if (line.getIsForCredit()) {
				hasCreditPayments = true;
				if (inv != null) {
					inv.setC_PaymentTerm_ID(VUtil
							.getParameterAsInt("C_PaymentTerm_ID"));
					inv.saveEx();
				}
				continue;
			}
			Timestamp now = new Timestamp(System.currentTimeMillis());
			al = new MAllocationLine(a, line.getAmmount(), BigDecimal.ZERO,
					BigDecimal.ZERO, BigDecimal.ZERO);
			al.setDocInfo(inv.getC_BPartner_ID(), inv.getC_Order_ID(), inv
					.getC_Invoice_ID());
			if (line.getMPayment() != null)
				al.setPaymentInfo(line.getMPayment().getC_Payment_ID(), 0);
			al.setDateTrx(now);

			if (line.getTypeOfMethod() == VPayMethodLine.M_CREDIT_NOTE) {
				line.getNote().setRef_Invoice_ID(inv.getC_Invoice_ID());
				inv.setRef_Invoice_ID(line.getNote().getC_Invoice_ID());
				line.getNote().save(trans);
				inv.save(trans);
				// al = new MAllocationLine (Env.getCtx(),0,doc.getTrxName());
				al.setDocInfo(inv.getC_BPartner_ID(), inv.getC_Order_ID(), inv
						.getC_Invoice_ID());
				// al.setAmount(line.getAmmount());
				// al.setC_AllocationHdr_ID(a.getC_AllocationHdr_ID());
				// al.setDateTrx(now);
				// if (!al.save(doc.getTrxName()))
				// log.log(Level.SEVERE,
				// "Allocation Line not written - Invoice=" +
				// doc.getInvoice().getC_Invoice_ID());
			}

			if (!al.save(trans))
				log.log(Level.SEVERE, "Allocation Line not written - Invoice="
						+ inv.getC_Invoice_ID());

		}
		a.setDocStatus(MAllocationHdr.DOCSTATUS_Completed);
		a.completeIt();
	}

	@Deprecated
	public void allocation(VDocument doc) {
		// /VALIDAR QUE EXISTAN PAGOS PARA ASIGNAR
		if (arlList.size() <= 0)
			return;

		// VALIDAR QUE NO EXISTAN ASIGNACIÓNES CON ESE PAGO????
		Query q = new Query(Env.getCtx(), MAllocationLine.Table_Name,
				"C_Invoice_ID= ? ", doc.getTrxName())
				.setParameters(new Object[] { doc.getInvoice()
						.getC_Invoice_ID() });
		MAllocationLine Aline = q.first();
		MAllocationHdr a = null;

		if (Aline == null) {
			a = new MAllocationHdr(Env.getCtx(), true, new Timestamp(System
					.currentTimeMillis()), doc.getInvoice().getC_Currency_ID(),
					doc.getInvoice().getDocumentNo(), doc.getTrxName());
		} else {
			// Martin commented and just replaced with return
			return;
			/*if (!VUtil
					.ask("Ya existe una asignación previa, está seguro de continuar ?"))
				return;
			a = new MAllocationHdr(Env.getCtx(), true, new Timestamp(System
					.currentTimeMillis()), doc.getInvoice().getC_Currency_ID(),
					doc.getInvoice().getDocumentNo(), doc.getTrxName());*/
			
		}

		a.setAD_Org_ID(doc.getOrder().getAD_Org_ID());
		if (!a.save(doc.getTrxName())) {
			log.log(Level.SEVERE, "Allocation not created");
		}

		MAllocationLine al = new MAllocationLine(Env.getCtx(), 0, doc
				.getTrxName());
		al.setC_Invoice_ID(doc.getInvoice().getC_Invoice_ID());
		al.setAmount(doc.getInvoice().getGrandTotal());

		boolean anyLine = false;

		for (VPayMethodLine line : arlList) {
			// Martin added to ignore Cash Payments - allocations are done by the Cash Line
			if (line.getMPayment().isCashTrx()) {
				continue;
			}
			// vemos si ya esta asignado este pago a esta factura.
			q = new Query(Env.getCtx(), MAllocationLine.Table_Name,
					"C_Invoice_ID= ? AND  C_Payment_ID= ?", doc.getTrxName())
					.setParameters(new Object[] {
							doc.getInvoice().getC_Invoice_ID(),
							line.getMPayment().getC_Payment_ID() });
			MAllocationLine verif = q.first();
			if (verif != null)
				continue;

			anyLine = true;
			if (line.getIsForCredit()) {
				hasCreditPayments = true;
				if (doc.getInvoice() != null) {
					doc.getInvoice().setC_PaymentTerm_ID(
							VUtil.getParameterAsInt("C_PaymentTerm_ID"));
					doc.getInvoice().saveEx();
				}
				continue;
			}
			Timestamp now = new Timestamp(System.currentTimeMillis());
			al = new MAllocationLine(a, line.getAmmount(), BigDecimal.ZERO,
					BigDecimal.ZERO, BigDecimal.ZERO);
			al.setDocInfo(doc.getBPartner_ID(), doc.getOrder().getC_Order_ID(),
					doc.getInvoice().getC_Invoice_ID());
			if (line.getMPayment() != null)
				al.setPaymentInfo(line.getMPayment().getC_Payment_ID(), 0);
			al.setDateTrx(now);

			if (line.getTypeOfMethod() == VPayMethodLine.M_CREDIT_NOTE) {
				/*
				 * line.getNote().setRef_Invoice_ID(doc.getInvoice().getC_Invoice_ID
				 * ()); doc.getInvoice().setRef_Invoice_ID(line.getNote
				 * ().getC_Invoice_ID());
				 */
				// line.getNote().save(doc.getTrxName());
				doc.getInvoice().save(doc.getTrxName());
				// al = new MAllocationLine (Env.getCtx(),0,doc.getTrxName());
				al.setDocInfo(doc.getBPartner_ID(), doc.getOrder()
						.getC_Order_ID(), doc.getInvoice().getC_Invoice_ID());
				// al.setAmount(line.getAmmount());
				// al.setC_AllocationHdr_ID(a.getC_AllocationHdr_ID());
				// al.setDateTrx(now);
				// if (!al.save(doc.getTrxName()))
				// log.log(Level.SEVERE,
				// "Allocation Line not written - Invoice=" +
				// doc.getInvoice().getC_Invoice_ID());
			}

			if (!al.save(doc.getTrxName()))
				log.log(Level.SEVERE, "Allocation Line not written - Invoice="
						+ doc.getInvoice().getC_Invoice_ID());

		}
		if (anyLine) {
			a.setDocAction(DocAction.ACTION_Complete);
			a.processIt(DocAction.ACTION_Complete);
			a.saveEx();
		} else
			a.deleteEx(true);
		// a.setDocStatus(MAllocationHdr.DOCSTATUS_Completed);
		// a.completeIt();
	}

	public String completePayment() {
		String res = "";
		int err = 0;
		for (int i = 0; i < arlList.size(); i++) {
			VPayMethodLine tmp = arlList.get(i);
			// Martin added the set Invoice ID
			tmp.setC_Invoice_ID(getC_Invoice_ID());
			if (!tmp.getIsForCredit()) {
				if (!tmp.completeIt()) {
					err++;
					res = "Errores ";
				}
			}
		}
		return res;
	}

	public boolean CreditPayments() {
		return hasCreditPayments;
	}

	public boolean delLine(VPayMethodLine line, VDocument curdoc) {
		for (VPayMethodLine lines : arlList) {
			if (lines.equals(line)) {
				lines.payback(curdoc);
				return arlList.remove(lines);
			}
		}
		return false;
	}

	public void delMethod(VPayMethodLine line) {
		arlList.remove(line);
	}

	public boolean existLine(VPayMethodLine compare) {
		boolean exist = false;
		for (int i = 0; i < arlList.size(); i++) {
			VPayMethodLine tmp = arlList.get(i);
			if (compare.getMethod().compareTo(tmp.getMethod()) == 0
					&& compare.getAmmount().compareTo(tmp.getAmmount()) == 0)
				exist = true;
		}
		return exist;
	}

	public BigDecimal getChange() {
		return change;
	}

	public LinkedList getCurrencyList() {
		for (int i : MCurrency.getAllIDs(MCurrency.Table_Name, "ISACTIVE='Y' AND  AD_CLIENT_ID = "+Env.getAD_Client_ID(Env.getCtx()),
				null)) {
			MCurrency cur = new MCurrency(Env.getCtx(), i, null);
			ValueNamePair vnp = new ValueNamePair(cur.getISO_Code(), cur
					.getDescription());
			currency.add(vnp);
		}
		return currency;
	}

	public String getCurrencyList(String code) {
		for (int i : MCurrency.getAllIDs(MCurrency.Table_Name, "ISACTIVE='Y' AND  AD_CLIENT_ID = "+Env.getAD_Client_ID(Env.getCtx()),
				null)) {

		}
		return "";
	}

	public int getLastIndex() {
		return arlList.size();
	}

	public VPayMethodLine getLine(int position) {
		return arlList.get(position);
	}

	public VPayMethodLine getLineByIndex(int value) {
		for (VPayMethodLine line : arlList) {
			if (line.getLineNumber() == value)
				return line;
		}
		return null;
	}

	public javax.swing.DefaultComboBoxModel getPaymentList() {
		/*String args = VUtil.getParameter("OtherPayments");
		Query q = new Query(Env.getCtx(), MRefList.Table_Name,
				"AD_Reference_ID = " + 214 + " AND VALUE IN (" + args + ")",
				null);
		List<MRefList> list = q.list();
		Vector vector = new Vector(list.size());
		for (MRefList item : list) {
			VComboBoxLoaderString loader = new VComboBoxLoaderString();
			loader.setIndex(item.getValue());
			loader.setText(new Query(Env.getCtx(),
					MRefList.Table_Name + "_trl", "ad_ref_list_id = ? ", null)
					.setParameters(new Object[] { item.get_ID() }).first()
					.get_ValueAsString("name"));
			vector.add(loader);
		}
		javax.swing.DefaultComboBoxModel comboVector = new javax.swing.DefaultComboBoxModel(
				vector);*/
		//return comboVector;
		return null;
	}

	public String getPaymentList(String code) {
		if (code == null)
			return "Otro";
		ValueNamePair[] list = MRefList.getList(Env.getCtx(), 214, false);
		System.out.println(list.toString());
		for (ValueNamePair item : list) {
			if (item.getValue().compareTo(code) == 0)
				return item.getName();
		}

		return "";
	}

	public BigDecimal getRestToPay() {
		BigDecimal restToPay = null;
		if (total != null) {
			restToPay = total;
			cash = BigDecimal.ZERO;
			change = null;
			for (VPayMethodLine line : arlList) {
				if (line.getAmmount() != null
						&& line.getAmmount().compareTo(BigDecimal.ZERO) > 0) {
					restToPay = restToPay.subtract(line.getAmmount());
					if (line.getTypeOfMethod() == VPayMethodLine.M_CASH) {
						/*
						 * BigDecimal calc=line.getAmmount(); calc.add(cash);
						 * cash=calc;
						 */
						cash = cash.add(line.getAmmount());
					}
				}
			}
			if (restToPay.compareTo(BigDecimal.ZERO) < 0
					& cash.compareTo(restToPay.negate()) > 0) {
				change = restToPay.negate();
			}
		}
		return restToPay;
	}

	public int getSize() {
		return arlList.size();
	}

	public String getSubTotal() {
		return subtotal.toString();
	}

	public String getTax() {
		return tax.toString();
	}

	public String getTotal() {
		return total.toString();
	}

	public javax.swing.DefaultComboBoxModel loadBanks() {
		/*int[] bpId;
		bpId = MBank.getAllIDs("C_BANK", " AD_CLIENT_ID = "+Env.getAD_Client_ID(Env.getCtx()), null);
		Vector vector = new Vector(bpId.length);
		for (int i = 0; i < bpId.length; i++) {
			MBank bpg = new MBank(Env.getCtx(), bpId[i], null);
			VComboBoxLoaderInt loader = new VComboBoxLoaderInt();
			loader.setIndex(bpId[i]);
			loader.setText(bpg.getName());
			vector.add(loader);
		}
		javax.swing.DefaultComboBoxModel comboVector = new javax.swing.DefaultComboBoxModel(
				vector);
		return comboVector;*/
		return null;
	}

	// TARJETAS TVENTAS
	/*public String makeTVTrack(String card) {
		VCardValidator validador = new VCardValidator();
		validador.setValueOf("TPDU", "0102");
		validador.setValueOf("TipoMensaje", "9999");
		validador.setValueOf("CodigoProceso", "999999");
		validador.setValueOf("NumeroTarjeta", card);
		validador.setValueOf("CVV", "000");
		validador.setValueOf("Monto", "0");
		validador.setValueOf("Transaccion", "999999");
		validador.setValueOf("ModoEntrada", "022");
		validador.setValueOf("NumeroAutorizacion", "0");
		validador.setValueOf("CodigoTerminal", "00602001");
		validador.setValueOf("CodigoCajero", "000001");
		validador.setValueOf("CodigoEstablecimiento", "602");
		validador.setValueOf("PinBLock", "0");
		validador.setValueOf("TipoTransaccion", "2");
		validador.setValueOf("FormaPago", "99");
		validador.setValueOf("CupoSupermaxi", "1");
		validador.setValueOf("CodigoProveedor", "01");
		validador.setValueOf("IdentificadorServicio", "0");
		validador.setValueOf("Credito", "00000000");
		validador.setValueOf("Filler", "0");
		System.out.println(validador.toString());
		return validador.getTrama();
	}
*/
	/*public String makeTVTrackaddMilles(String card, VDocument curdoc) {
		VCardValidator validador = new VCardValidator();
		BigDecimal amount = BigDecimal.ZERO;
		for (VPayMethodLine lines : arlList) {
			if (lines.getTypeOfMethod() != VPayMethodLine.M_CREDIT_NOTE) {
				if (lines.getTypeOfMethod() == VPayMethodLine.M_CREDIT_CARD) {
					if (!lines.getIP().equals(VUtil.getParameter("ServerIP")))
						amount = amount.add(lines.getAmmount());
				} else
					amount = amount.add(lines.getAmmount());
			}
		}

		validador.setValueOf("TPDU", "0102");
		validador.setValueOf("TipoMensaje", "9999");
		validador.setValueOf("CodigoProceso", "999998");
		validador.setValueOf("NumeroTarjeta", card);
		validador.setValueOf("CVV", "000");
		validador.setValueOf("Monto", amount.toString());
		validador.setValueOf("Transaccion", "999999");
		validador.setValueOf("ModoEntrada", "022");
		validador.setValueOf("NumeroAutorizacion", "0");
		validador.setValueOf("CodigoTerminal", "00602001");
		validador.setValueOf("CodigoCajero", "000001");
		validador.setValueOf("CodigoEstablecimiento", "602");
		validador.setValueOf("PinBLock", "0");
		validador.setValueOf("TipoTransaccion", "2");
		validador.setValueOf("FormaPago", "99");
		validador.setValueOf("CupoSupermaxi", "1");
		validador.setValueOf("CodigoProveedor", "01");
		validador.setValueOf("IdentificadorServicio", "0");
		validador.setValueOf("Credito", "00000000");
		String filler = curdoc.getInvoice().getC_Invoice_ID() + "@"
				+ curdoc.getInvoice().getDocumentNo();
		validador.setValueOf("Filler", filler);
		System.out.println(validador.toString());
		return validador.getTrama();
	}*/

	public String prevalidatePayments() {
		String res = "";
		int err = 0;
		for (int i = 0; i < arlList.size(); i++) {
			VPayMethodLine tmp = arlList.get(i);
			if (!tmp.getIsValid()) {
				err++;
				res = "Error " + err + " pago incorrecto:  Nº" + (i + 1)
						+ " - " + tmp.getAmmount();
			}
		}
		return res;
	}

	public void Print(VDocument curdoc) {
		for (int i = 0; i < arlList.size(); i++) {
			VPayMethodLine tmp = arlList.get(i);
			tmp.Print(false);
		}
	}

	public void setCategoryPayments() {
		for (int i = 0; i < arlList.size(); i++) {
			VPayMethodLine tmp = arlList.get(i);
			tmp.getMPayment().setC_DocType_ID(1000233);
			tmp.getMPayment().saveEx();
		}
	}

	public void setSubTotal(BigDecimal val) {
		subtotal = val;
	}

	public void setTotal(BigDecimal val) {
		total = val;
	}

	public int getC_Invoice_ID() {
		return C_Invoice_ID;
	}

	public void setC_Invoice_ID(int invoice_ID) {
		C_Invoice_ID = invoice_ID;
	}

}
