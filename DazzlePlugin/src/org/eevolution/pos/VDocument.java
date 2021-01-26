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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.adempiere.exceptions.AdempiereException;
import org.adempiere.util.IProcessUI;
import org.adempiere.webui.apps.WProcessCtl;
import org.compiere.model.I_C_PaymentTerm;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MAllocationHdr;
import org.compiere.model.MAllocationLine;
import org.compiere.model.MBPartner;
import org.compiere.model.MCharge;
import org.compiere.model.MCost;
import org.compiere.model.MCostElement;
import org.compiere.model.MDocType;
import org.compiere.model.MInOut;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MInvoicePaySchedule;
import org.compiere.model.MLocator;
import org.compiere.model.MMovement;
import org.compiere.model.MMovementLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MOrderPaySchedule;
import org.compiere.model.MPInstance;
import org.compiere.model.MPInstancePara;
import org.compiere.model.MPOS;
import org.compiere.model.MPayment;
import org.compiere.model.MPriceList;
import org.compiere.model.MProduct;
import org.compiere.model.MProductCategoryAcct;
import org.compiere.model.MRMA;
import org.compiere.model.MRMALine;
import org.compiere.model.MReconHdr;
import org.compiere.model.MReconLine;
import org.compiere.model.MStorageOnHand;
import org.compiere.model.MWarehouse;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.TaxMA;
import org.compiere.model.X_C_Order;
import org.compiere.process.DocAction;
import org.compiere.process.ProcessInfo;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
import org.compiere.util.Trx;



/**
 *
 * @author antonio.canaveral@e-evolution.com, www.e-evolution.com
 * @author victor.perez@e-evolution.com, www.e-evolution.com
 */
public class VDocument
{
	private int inOutID = 0;

	public int getInOutID() {
		return inOutID;
	}
	public void setInOutID(int inOutID) {
		this.inOutID = inOutID;
	}
	private int C_DocTypeShipment_ID = 0;
	public int getC_DocTypeShipment_ID() {
		return C_DocTypeShipment_ID;
	}
	public void setC_DocTypeShipment_ID(int c_DocTypeShipment_ID) {
		C_DocTypeShipment_ID = c_DocTypeShipment_ID;
	}
	private boolean CreatePO = false;
	public boolean isCreatePO() {
		return CreatePO;
	}
	public void setCreatePO(boolean createPO) {
		CreatePO = createPO;
	}
	private boolean SOTrx = true;
	public boolean isSOTrx() {
		return SOTrx;
	}
	public void setSOTrx(boolean sOTrx) {
		SOTrx = sOTrx;
	}
	private int C_Order_ID = 0;
	public int getC_Order_ID() {
		return C_Order_ID;
	}
	public void setC_Order_ID(int c_Order_ID) {
		C_Order_ID = c_Order_ID;
	}
	private String refNo = "";
	public String getRefNo() {
		return refNo;
	}
	public void setRefNo(String refNo) {
		this.refNo = refNo;
	}
	private String Comment = "";
	public String getComment() {
		return Comment;
	}
	public void setComment(String comment) {
		Comment = comment;
	}
	private int discount_charge_id;
	private BigDecimal discountOverallAmt;
	private BigDecimal roundCentsAmt;
	private int roundcents_charge_id;
	private MPriceList pl = null;
	private int hostess_ID = 0;
	private boolean rebateOrder = false;
	private BigDecimal hostessDiscount = Env.ZERO;
	private int M_Warehouse_ID = 0;
	private int C_BPartner_Location_ID;
	//private String Orderref = "";
	//private String OrderComment = "";
	private BigDecimal discountLevel = null;
	private boolean isQuote = false;
	private int c_Currency_ID = 0;
	private MOrder mOrder = null;
	private String CreditCardNo = null;
	private String m_processMsg = null;
	private Timestamp invoiceDate = null;
	private Timestamp movementDate = null;
	private Timestamp poDate = null;

	public Timestamp getPoDate() {
		return poDate;
	}
	public void setPoDate(Timestamp poDate) {
		this.poDate = poDate;
	}
	public Timestamp getMovementDate() {
		return movementDate;
	}
	public void setMovementDate(Timestamp movementDate) {
		this.movementDate = movementDate;
	}
	public Timestamp getInvoiceDate() {
		return invoiceDate;
	}
	public void setInvoiceDate(Timestamp invoiceDate) {
		this.invoiceDate = invoiceDate;
	}
	public String getM_processMsg() {
		return m_processMsg;
	}
	public void setM_processMsg(String m_processMsg) {
		this.m_processMsg = m_processMsg;
	}
	public MPriceList getPl() {
		return pl;
	}
	public void setPl(MPriceList pl) {
		this.pl = pl;
	}
	/**	Logger							*/
	protected transient CLogger	log = CLogger.getCLogger (getClass());
	public static void Allocation(MInvoice invoice, List<MPayment> payments) {
		MAllocationHdr a = new MAllocationHdr(Env.getCtx(), true,
				new Timestamp(System.currentTimeMillis()), invoice
				.getC_Currency_ID(), invoice.getDocumentNo(), null);
		a.setAD_Org_ID(invoice.getAD_Org_ID());
		a.saveEx();

		MAllocationLine al = new MAllocationLine(Env.getCtx(), 0, null);
		al.setC_Invoice_ID(invoice.getC_Invoice_ID());
		al.setAmount(invoice.getGrandTotal());

		for (MPayment line : payments) {
			Timestamp now = new Timestamp(System.currentTimeMillis());
			al = new MAllocationLine(a, line.getPayAmt(), BigDecimal.ZERO,
					BigDecimal.ZERO, BigDecimal.ZERO);
			al.setDocInfo(invoice.getC_BPartner_ID(), invoice.getC_Order_ID(),
					invoice.getC_Invoice_ID());
			al.setPaymentInfo(line.getC_Payment_ID(), 0);
			al.setDateTrx(now);

			if (line.getTenderType().equals(
					VPayMethodLine.TENDERTYPE_CreditNote)) {
				int c_invoice_id = line.getRef_Payment_ID();// //OJO Hay que
				// grabar el id de
				// la nota de
				// crédito.

				MInvoice cnote = new MInvoice(Env.getCtx(), c_invoice_id, null);
				cnote.setRef_Invoice_ID(invoice.getC_Invoice_ID());
				invoice.setRef_Invoice_ID(cnote.getC_Invoice_ID());
				cnote.saveEx();
				invoice.saveEx();
				al.setDocInfo(invoice.getC_BPartner_ID(), invoice
						.getC_Order_ID(), invoice.getC_Invoice_ID());
			}
			al.saveEx();
		}
		a.setDocStatus(MAllocationHdr.DOCSTATUS_Completed);
		a.completeIt();
	}
	@Deprecated
	public static void CompleteIt(boolean kk, int shipper_id, MOrder order) {
		if (shipper_id != 0) {
			order.setDeliveryViaRule(MOrder.DELIVERYVIARULE_Shipper);
			order.setM_Shipper_ID(shipper_id);
			order.saveEx();
		}
		order.setDateOrdered(new Timestamp(System.currentTimeMillis()));
		order.setDateAcct(new Timestamp(System.currentTimeMillis()));
		order.setDocAction(DocAction.ACTION_Complete);
		order.processIt(DocAction.ACTION_Complete);
		order.saveEx();

		MInvoice invoice = MInvoice.get(Env.getCtx(), order.getC_Invoice_ID());
		if (order.getDescription() != null)
			invoice.setDescription(order.getDescription());
		if (order.getAD_Org_ID() > 0)
			invoice.setAD_OrgTrx_ID(order.getAD_Org_ID());
		if (VUtil.getParameterAsInt("Activity_ID") > 0)
			invoice.setC_Activity_ID(VUtil.getParameterAsInt("Activity_ID"));
		for (MOrderLine line : order.getLines()) {
			for (MInvoiceLine iline : invoice.getLines()) {
				if (line.getM_Product_ID() == iline.getM_Product_ID()) {
					iline.set_CustomColumn("Group2", line.get_Value("Group2"));
					iline.saveEx();
				} else
					continue;
			}
		}
		invoice.setDateInvoiced(new Timestamp(System.currentTimeMillis()));
		invoice.setDateAcct(new Timestamp(System.currentTimeMillis()));
		invoice.saveEx();
	}
	public static MInvoice CompleteIt(int shipper_id, MOrder order) {
		// //DESDE AQUI /////
		if (shipper_id != 0) {
			order.setDeliveryViaRule(MOrder.DELIVERYVIARULE_Shipper);
			order.setM_Shipper_ID(shipper_id);
			order.saveEx();
		}
		order.setDateOrdered(new Timestamp(System.currentTimeMillis()));
		order.setDateAcct(new Timestamp(System.currentTimeMillis()));
		order.setDocAction(DocAction.ACTION_Complete);
		order.processIt(DocAction.ACTION_Complete);
		order.saveEx();

		MInvoice invoice = MInvoice.get(Env.getCtx(), order.getC_Invoice_ID());
		if (invoice.getC_Invoice_ID() == 0)
			return null;
		else {
			if (order.getDescription() != null)
				invoice.setDescription(order.getDescription());
			if (order.getAD_Org_ID() > 0)
				invoice.setAD_OrgTrx_ID(order.getAD_Org_ID());
			if (VUtil.getParameterAsInt("Activity_ID") > 0)
				invoice
				.setC_Activity_ID(VUtil
						.getParameterAsInt("Activity_ID"));
			for (MOrderLine line : order.getLines()) {
				for (MInvoiceLine iline : invoice.getLines()) {
					if (line.getM_Product_ID() == iline.getM_Product_ID()) {
						iline.set_CustomColumn("Group2", line
								.get_Value("Group2"));
						iline.saveEx();
					} else
						continue;
				}
			}
			invoice.setDateInvoiced(new Timestamp(System.currentTimeMillis()));
			invoice.setDateAcct(new Timestamp(System.currentTimeMillis()));
			invoice.saveEx();
		}
		// /COMPLETAR LOS PAGOS
		String whereClause = " (docStatus='DR' OR docStatus='CO')  AND C_Order_ID = ?";
		Query sql = new Query(Env.getCtx(), MPayment.Table_Name, whereClause,
				null).setParameters(new Object[] { order.getC_Order_ID() });
		List<MPayment> payments = sql.list();
		for (MPayment pay : payments) {
			// Martin added code to set the INVOICE ID
			if (invoice != null) {
			  pay.setC_Invoice_ID(invoice.getC_Invoice_ID());
			}
			pay.processIt(MPayment.ACTION_Complete);
			pay.setDocAction(MPayment.ACTION_Close);
			pay.setDocStatus(MPayment.DOCSTATUS_Completed);
			pay.saveEx();
		}
		// CREAR LAS ASIGNACIONES
		// Allocation(invoice,payments);
		return invoice;
	}
	public static void PrintInvoice(VDocument doc, String stamp, MPOS pos) {
		//VPrintManager printter = new VPrintManager(pos, doc);
		//printter.IsInvoice(true);
		//printter.Print(stamp, true);
	}
	private int C_DocType_ID;
	public int getC_DocType_ID() {
		return C_DocType_ID;
	}
	public void setC_DocType_ID(int c_DocType_ID) {
		C_DocType_ID = c_DocType_ID;
	}
	private String DocumentNo;
	private int lastORDER_ID;
	private MBPartner partner;

	private MPOS p_pos = null;
	private int C_SalesRep_ID = 0;
	private String m_bp_default;
	private boolean transfer;
	private int m_M_Shipper_ID = 0;
	private boolean isInvoice = false;
	private String tDiscount;

	private String m_POreference;

	// protected String trxName = null;
	// protected Trx trx = null;
	// ----

	private boolean enableWarning = true;
	private VPayMethods payments;
	private int modePOS = 0;
	private boolean validateAsink;

	private MOrder m_order;
	private MInvoice m_invoice;
	private MInOut m_inout;
	private boolean isSelected;
	public static final int DOC_TYPE_QUOTATION = 0;
	public static final int DOC_TYPE_ORDER = 1;

	public static final int DOC_TYPE_INVOICE = 2;
	public static final int DOC_TYPE_TVCARD = 3;

	public static final int DOC_TYPE_CREDIT_NOTE = 4;

	// private Trx trxOrder = Trx.get(Trx.createTrxName(), true);

	public static final int DOC_TYPE_RE_INVOICE = 5;

	public static void RevertCorrectIt(MOrder order) {
		order.processIt(MOrder.ACTION_Reverse_Correct);
		order.setDocAction(MOrder.ACTION_None);
		order.setDocStatus(MOrder.DOCSTATUS_Reversed);
		order.saveEx();
		revertPayments(order);
	}

	private static void revertPayments(MOrder order) {
		// BORRAR LOS PAGOS A MANO!!
		String whereClause = " docStatus='CO' AND C_Order_ID = ?";
		Query sql = new Query(Env.getCtx(), MPayment.Table_Name, whereClause,
				null).setParameters(new Object[] { order.getC_Order_ID() });
		List<MPayment> payments = sql.list();
		for (MPayment pay : payments) {
			pay.processIt(MPayment.ACTION_Reverse_Correct);
			pay.setDocAction(MPayment.ACTION_None);
			pay.setDocStatus(MPayment.DOCSTATUS_Reversed);
			pay.saveEx();
		}
	}

	public static void VoidIt(MOrder order) {
		String whereClause = " (docStatus='DR' OR docStatus='CO')  AND C_Order_ID = ?";
		Query sql = new Query(Env.getCtx(), MPayment.Table_Name, whereClause,
				null).setParameters(new Object[] { order.getC_Order_ID() });
		List<MPayment> payments = sql.list();
		for (MPayment pay : payments) {
			pay.processIt(MPayment.ACTION_Complete);
			pay.setDocAction(MPayment.ACTION_Close);
			pay.setDocStatus(MPayment.DOCSTATUS_Completed);
			pay.saveEx();
		}
		revertPayments(order);
		order.processIt(MOrder.ACTION_Void);
		order.setDocAction(MOrder.ACTION_None);
		order.setDocStatus(MOrder.DOCSTATUS_Voided);
		order.saveEx();
	}

	private String DOCACTION = MOrder.DOCACTION_Prepare;

	private String DOCSTATUS = MOrder.DOCSTATUS_Drafted;

	protected int doctype;

	public VDocument() {

		createOrder();
	}

	public VDocument(boolean create) {
		if (create)
			createOrder();
	}

	public VDocument(int m_order_id, MPOS pos) {
		p_pos = pos;
		m_order = new MOrder(Env.getCtx(), m_order_id, null);
		isSelected = m_order.isSelected();
		m_order.setIsSOTrx(true);
		partner = MBPartner.get(Env.getCtx(), m_order.getC_BPartner_ID());
		C_BPartner_Location_ID = m_order.getC_BPartner_Location_ID();
		m_M_Shipper_ID = m_order.getM_Shipper_ID();
		C_SalesRep_ID = m_order.getSalesRep_ID();
		if (m_order.getC_Invoice_ID() != 0) {
			m_invoice = MInvoice.get(Env.getCtx(), m_order.getC_Invoice_ID());
		}
		if (m_order.getDocStatus().equals(MOrder.ACTION_Complete)) {
			if (m_order.getC_Invoice_ID() != 0) {
				m_invoice = m_order.getInvoices()[0];
				doctype = DOC_TYPE_INVOICE;
			} else
				doctype = DOC_TYPE_RE_INVOICE;
		} else {
			if (m_order.getC_DocTypeTarget_ID() == getC_DocSubType(MOrder.DocSubTypeSO_RMA))
				doctype = DOC_TYPE_CREDIT_NOTE;
			else if (m_order.getDocStatus().equals(MOrder.DOCSTATUS_InProgress))
				doctype = DOC_TYPE_ORDER;
			else
				doctype = DOC_TYPE_QUOTATION;

			if (m_order.getDocAction().equals(MOrder.DOCACTION_None))
				doctype = DOC_TYPE_RE_INVOICE;
		}
	}

	public VDocument(MBPartner current) {
		partner = current;
		//createOrder();
	}

	public boolean allocatePayments() {
		if (payments != null) {
			if (payments.completePayment().length() == 0) {
				payments.allocation(this);
				return true;
			} else {
				VUtil
				.setMsg(
						"La factura se ha generado con errores. Se va a anular la factura. Revise los pagos.",
						true);
				setDocStatus(MOrder.DOCSTATUS_Reversed,
						MOrder.DOCACTION_Reverse_Correct);
				return false;
			}
		}
		return false;
	}

	public boolean allocatePaymentsCompleted() {
		if (payments != null) {
			payments.allocation(this);
			return true;
		}
		return false;
	}

	public boolean analizeCreditNote(MOrderLine oline) {
		BigDecimal qty = BigDecimal.ZERO;

		if (m_invoice != null) {
			String whereClause = "docstatus='CO' AND poreference = ?";
			Query sql = new Query(Env.getCtx(), MInvoice.Table_Name,
					whereClause, null).setParameters(new Object[] { String
							.valueOf(m_invoice.getC_Invoice_ID()) });
			List<MInvoice> notes = sql.list();
			for (MInvoice curnote : notes) {
				for (MInvoiceLine line : curnote.getLines()) {
					if (oline.getM_Product_ID() > 0)   {   // Martin added
						if (line.getM_Product_ID() == oline.getM_Product_ID()) {
							qty = qty.add(line.getQtyInvoiced());
						}
					}
					else {
						if (line.getC_Charge_ID() == oline.getC_Charge_ID()) {  // Martin added
							qty = qty.add(line.getQtyInvoiced());
						}
					}
				}
			}
			for (MInvoiceLine currline : m_invoice.getLines()) {
				if (oline.getM_Product_ID() > 0) {  // Martin added
					if (currline.getM_Product_ID() == oline.getM_Product_ID()) {
						if (currline.getQtyInvoiced().subtract(qty).subtract(
								oline.getQtyOrdered()).signum() >= 0)
							return true;
						else
							return false;
					}
				} else {
					if (currline.getC_Charge_ID() == oline.getC_Charge_ID()) {  // Martin added
						if (currline.getQtyInvoiced().subtract(qty).subtract(
								oline.getQtyOrdered()).signum() >= 0)
							return true;
						else
							return false;
					}
				}
			}
		}
		return false;
	}

	private void CompleteAsink() {
		if (m_M_Shipper_ID != 0) {
			m_order.setDeliveryViaRule(MOrder.DELIVERYVIARULE_Shipper);
			m_order.setM_Shipper_ID(m_M_Shipper_ID);
			// m_order.saveEx();
		}
		m_order.setDateOrdered(new Timestamp(System.currentTimeMillis()));
		m_order.setDateAcct(new Timestamp(System.currentTimeMillis()));
		m_order.setDocAction(DocAction.ACTION_Complete);

		m_order.processIt(DocAction.ACTION_Complete);
		String result = m_order.getDocStatus();
		//String result = m_order.completePOSOrder(this); TODO: Review the implementation asynchrone to complete Order
		m_order.setDocStatus(result);
		m_order.saveEx();
		if (!result.equals(DocAction.STATUS_Completed)) {
			VUtil
			.setMsg(
					"Se ha producido un error al procesar la Factura. Estado IN",
					true);
			return;
		}
		m_invoice = MInvoice.get(Env.getCtx(), m_order.getC_Invoice_ID());
		if (m_invoice.getC_Invoice_ID() == 0)
			doctype = DOC_TYPE_ORDER;
		else {
			if (m_order.getDescription() != null)
				m_invoice.setDescription(m_order.getDescription().toLowerCase());
			if (m_order.getAD_Org_ID() > 0)
				m_invoice.setAD_OrgTrx_ID(m_order.getAD_Org_ID());
			if (VUtil.getParameterAsInt("Activity_ID") > 0)
				m_invoice.setC_Activity_ID(VUtil
						.getParameterAsInt("Activity_ID"));
			//A.C added
			if(m_order.getPOReference() != null)
				m_invoice.setPOReference(m_order.getPOReference());
			//End A.C added
			// Creamos las confirmaciones
			/*if (m_order.getShipments() != null
					& modePOS == VPOS.MODE_CallCenter) {
				MInOutConfirm confirm = new MInOutConfirm(Env.getCtx(), 0, null);
				confirm.setAD_Org_ID(m_order.getAD_Org_ID());
				confirm.setDocumentNo(m_invoice.getDocumentNo());
				confirm
				.setM_InOut_ID(m_order.getShipments()[0]
				                                      .getM_InOut_ID());
				confirm
				.setConfirmType(MInOutConfirm.CONFIRMTYPE_ShipReceiptConfirm);
				confirm.saveEx();
				for (MInOutLine lines : m_order.getShipments()[0].getLines()) {
					MInOutLineConfirm confLine = new MInOutLineConfirm(Env
							.getCtx(), 0, null);
					confLine.setM_InOutConfirm_ID(confirm.get_ID());
					confLine.setM_InOutLine_ID(lines.getM_InOutLine_ID());
					confLine.setConfirmedQty(lines.getQtyEntered());
					confLine.setTargetQty(lines.getQtyEntered());
					confLine.saveEx();
				}
			}*/
			// Ponemos valores de Group2
			/*for (MOrderLine line : m_order.getLines()) {
				for (MInvoiceLine iline : m_invoice.getLines()) {
					if (line.getM_Product_ID() == iline.getM_Product_ID()) {
						iline.set_CustomColumn("Group2", line
								.get_Value("Group2"));
						iline.saveEx();
					} else
						continue;
				}
			}*/
			m_invoice
			.setDateInvoiced(new Timestamp(System.currentTimeMillis()));
			m_invoice.setDateAcct(new Timestamp(System.currentTimeMillis()));
			m_invoice.saveEx();
			isInvoice=true;
			/*Mietras se revisa la inclución del metodo completePOSOrder*/
			completePayments();
			allocatePaymentsCompleted();
			Print();
			/*FIN*/
			doctype = DOC_TYPE_INVOICE;
		}
	}

	public boolean CompleteCreditNote() {
		int C_DocTypeShipment = 0;
		int C_DocTypeInvoice = 0;
		Trx trx = Trx.get(Trx.createTrxName("RMA"), true);
		MRMA rma;
		try {
			if (isDocumentApprobed((Integer) p_pos
					.get_Value("C_DocTypeReval_ID"))) {
				rma = new MRMA(Env.getCtx(), 0, trx.getTrxName());

				rma.setName("Devolucion de Material: "
						+ m_order.getC_Order_ID());

				C_DocTypeInvoice = (Integer) p_pos
				.get_Value("C_DocTypeReval_ID");
				for (MDocType doc : MDocType.getOfDocBaseType(Env.getCtx(),
						MDocType.DOCBASETYPE_SalesOrder)) {
					if (doc.isSOTrx() == true && doc.getDocSubTypeSO() !=null
							&& doc.getDocSubTypeSO().equals(
									MDocType.DOCSUBTYPESO_ReturnMaterial)) {
						C_DocTypeShipment = doc.getC_DocTypeShipment_ID();
						rma.setC_DocType_ID(doc.getC_DocType_ID());
						continue;
					}
				}
				rma.setSalesRep_ID(p_pos.getSalesRep_ID());
				rma.setM_InOut_ID(Integer.valueOf(
						m_order.get_ValueAsString("Help")).intValue());
				rma.setM_RMAType_ID(PO.getAllIDs("M_RMAType", "", null)[0]);
				rma.setIsSOTrx(true);
				rma.saveEx();

				// NEW SHIPMENT
				MInOut ship = new MInOut(Env.getCtx(), 0, trx.getTrxName());
				ship.setC_BPartner_ID(m_order.getC_BPartner_ID());
				ship.setC_BPartner_Location_ID(m_order
						.getC_BPartner_Location_ID());
				ship.setM_Warehouse_ID(m_order.getM_Warehouse_ID());
				ship.setMovementType(MInOut.MOVEMENTTYPE_CustomerReturns);
				ship.setC_DocType_ID(C_DocTypeShipment);
				ship.setC_Order_ID(lastORDER_ID);
				// ship.saveEx(); Martin commented and moved below

				// Martin added
				boolean shipSaved = false;

				// NEW INVOICE
				for (MOrderLine oline : m_order.getLines()) {
					if (oline.getM_Product_ID() > 0) {
						// Martin added to check if at least one product on order before save shipment
						if (!shipSaved) {
							ship.saveEx();
							shipSaved = true;
						}

						if (analizeCreditNote(oline)) { // Martin added code for charges

							for (MInvoiceLine iline : m_invoice.getLines()) {
								if (iline.getProduct().getM_Product_ID() == oline
										.getM_Product_ID()) {
									// NEW RMA LINES
									MRMALine rmaline = new MRMALine(Env.getCtx(),
											0, trx.getTrxName());
									rmaline.setM_RMA_ID(rma.getM_RMA_ID());
									if (oline.getQtyEntered().compareTo(
											iline.getQtyEntered()) > 0)
										throw new Exception(
												Msg.translate(Env.getCtx(), "La cantidad introducida del producto ")
												+ iline.getProduct()
												.getName()
												+ Msg.translate(Env.getCtx(), " es mayor que la facturada.(")
												+ iline.getQtyEntered()
												+ ")");
									rmaline.setQty(oline.getQtyEntered());
									rmaline.setM_InOutLine_ID(iline
											.getM_InOutLine_ID());
									rmaline.saveEx();
									// NEW SHIPMENT LINES
									MInOutLine inOutNewLine = new MInOutLine(ship);
									inOutNewLine.setProduct(oline.getProduct());
									inOutNewLine.setM_Warehouse_ID(p_pos
											.getM_Warehouse_ID());
									inOutNewLine.setQty(oline.getQtyEntered());
									inOutNewLine.setM_Locator_ID(MLocator.getDefault((MWarehouse) p_pos.getM_Warehouse()).getM_Locator_ID());
									inOutNewLine.saveEx();
									continue;
								}
							}
						} else
							throw new Exception(
									Msg.translate(Env.getCtx(), "La factura no dispone de crédito suficiente.\nVerifique que no existan Notas de Crédito previas"));
					}
				}
				rma.setDocAction(DocAction.ACTION_Complete);
				rma.processIt(DocAction.ACTION_Complete);
				rma.saveEx();
				// Martin added
				if (shipSaved) {
					ship.setDocAction(DocAction.ACTION_Complete);
					ship.processIt(DocAction.ACTION_Complete);
					ship.saveEx();
				}
				trx.commit();
				trx.close();
				trx = null;
				MInvoice invoice = createInvoice(rma, C_DocTypeInvoice);
				// MInvoiceLine invoiceLines[] = createInvoiceLines(rma, invoice);  // Martin commented
				MInvoiceLine invoiceLines[] = createInvoiceLines(m_order, invoice);  // Martin added instead
				invoice.setC_Order_ID(lastORDER_ID);
				invoice.setDescription(m_order.getDescription());
				MOrder last = new MOrder(Env.getCtx(), lastORDER_ID, null);
				last.setDescription(m_order.getDescription());
				last.saveEx();
				recreatePrices(invoice);
				if (m_POreference != null)
					invoice.setPOReference(m_POreference);
				invoice.processIt(DocAction.ACTION_Complete);
				invoice.saveEx();
				m_invoice = invoice;
				return true;
			} else
				VUtil
				.setMsg(
						"Secuencia caducada. ¡No se puede Generar el Documento!. Consulte con su Administrador",
						true);
			return false;
		} catch (Exception e) {
			StringBuffer str = new StringBuffer();
			str.append(e.getMessage() + "\n");
			VUtil.setMsg("Error: " + str, true);
			trx.rollback();
			trx.close();
			trx = null;
			return false;
		}
	}

	public boolean completePayments() {
		if (payments != null) {
			// Martin added set invoice
			if (m_invoice != null) {
				payments.setC_Invoice_ID(m_invoice.getC_Invoice_ID());
			}
			if (payments.completePayment().length() == 0) {
				return true;
			} else {
				VUtil
				.setMsg(
						"La factura se ha generado con errores. Se va a anular la factura. Revise los pagos.",
						true);
				setDocStatus(MOrder.DOCSTATUS_Reversed,
						MOrder.DOCACTION_Reverse_Correct);
				return false;
			}
		}
		return false;
	}

	private MInvoice createInvoice(MRMA rma, int docTypeId) {
		// int docTypeId = getInvoiceDocTypeId(rma.get_ID());

		if (docTypeId == -1) {
			throw new IllegalStateException(
					Msg.translate(Env.getCtx(), "No se Puede Obtener el tipo de documento de factura para RMA."));
		}

		MInvoice invoice = new MInvoice(Env.getCtx(), 0, null);
		invoice.setRMA(rma);

		invoice.setC_DocTypeTarget_ID(docTypeId);
		if (!invoice.save()) {
			throw new IllegalStateException(
					Msg.translate(Env.getCtx(), "No se ha podido generar la Nota de Crédito."));
		}

		return invoice;
	}

	private MInvoiceLine[] createInvoiceLines(MRMA rma, MInvoice invoice) {
		ArrayList<MInvoiceLine> invLineList = new ArrayList<MInvoiceLine>();

		MRMALine rmaLines[] = rma.getLines(true);

		for (MRMALine rmaLine : rmaLines) {
			if (!rmaLine.isShipLineInvoiced()
					&& rmaLine.getM_InOutLine_ID() != 0) {
				throw new IllegalStateException("No invoice line - RMA = "
						+ rma.getDocumentNo() + ", Line = " + rmaLine.getLine());
			}

			MInvoiceLine invLine = new MInvoiceLine(invoice);
			invLine.setRMALine(rmaLine);

			if (!invLine.save()) {
				throw new IllegalStateException("Could not create invoice line");
			}

			invLineList.add(invLine);
		}

		MInvoiceLine invLines[] = new MInvoiceLine[invLineList.size()];
		invLineList.toArray(invLines);

		return invLines;
	}

	// Martin added
	private MInvoiceLine[] createInvoiceLines(MOrder ord, MInvoice invoice) {
		ArrayList<MInvoiceLine> invLineList = new ArrayList<MInvoiceLine>();

		MOrderLine olines[] = ord.getLines();

		for (MOrderLine oLine : olines) {

			MInvoiceLine invLine = new MInvoiceLine(invoice);
			invLine.setOrderLine(oLine);
			invLine.setC_OrderLine_ID(0);  // martin added - this is a temp order that is created for Credit notes.
			                               // I set it to 0 so the orderline can be deleted later.
			invLine.setQty(oLine.getQtyEntered());

			if (!invLine.save()) {
				throw new IllegalStateException("Could not create invoice line");
			}

			invLineList.add(invLine);
		}

		MInvoiceLine invLines[] = new MInvoiceLine[invLineList.size()];
		invLineList.toArray(invLines);

		return invLines;
	}

	/*public MOrderLine createLine(ProductPOS product, BigDecimal QtyOrdered) {
		return createLine(product, QtyOrdered, product
				.getM_PriceList_Version_ID());
	}*/



    /**
     * create Sales Order Line
     * @param product
     * @param QtyOrdered
     * @param M_PriceList_Version_ID
     * @return MOrderLine Sales Order Line
     */
	/*public MOrderLine createLine(ProductPOS product, BigDecimal QtyOrdered, int M_PriceList_Version_ID)
	{
		if (DOC_TYPE_INVOICE != doctype)
		{
			try
			{
				if (m_order == null)
				{
					throw new AdempiereException("Sales Order do not exist");
				}
				if (!( MOrder.DOCSTATUS_Drafted.equals(m_order.getDocStatus())
					|| MOrder.DOCSTATUS_InProgress.equals(m_order.getDocStatus())))
				{
					VUtil.setMsg("El estado del Documento es " + m_order.getDocStatus(), false);
					return null;
				}
				// Set the product to get its properties
				MProduct m_product = new MProduct(Env.getCtx(), product .getM_Product_ID(), null);
				if (QtyOrdered == null)
					QtyOrdered = BigDecimal.ONE;
				// pricelist
				log.fine("Create line :"+ product.getName() + " Quantity: " + QtyOrdered);

				MPriceList pl = null;
				//commnet static logic
				if (partner != null)
				{
					// Martin added this and removed below
					pl = MPriceList.get(Env.getCtx(), partner.getM_PriceList_ID(), null);
					if (pl == null || pl.get_ID() == 0)
					{
						pl = MPriceList.get(Env.getCtx(), p_pos.getM_PriceList_ID(), null);
					}
					if (partner.getBPGroup().getName().equals("SELECTO") || partner.getBPGroup().getName().equals("EMPLEADOS"))
					{
						pl = MPriceList.get(Env.getCtx(), partner.getM_PriceList_ID(), null);
						if (pl.get_ID() == 0)
						{
							pl = MPriceList.get(Env.getCtx(), p_pos.getM_PriceList_ID(), null);
						}
					}
					else
						pl = MPriceList.get(Env.getCtx(), p_pos.getM_PriceList_ID(), null);
				} else
					pl = MPriceList.get(Env.getCtx(),p_pos.getM_PriceList_ID(), null);

				m_order.setM_PriceList_ID(pl.get_ID());
				m_order.saveEx();
				for (MOrderLine line : m_order.getLines())
				{
					if (line.getM_Product_ID() == product.getM_Product_ID())
					{
						line.setQty(QtyOrdered);
						line.saveEx();
						validateOnHand(line);
						line.setPrice();
						m_order.renumberLines(10);
						if (MOrder.DOCSTATUS_InProgress.equals(m_order.getDocStatus()))
						{
							preparateIt();
							m_order.renumberLines(10);
						}
						return line;
					}
				}

				MOrderLine line = new MOrderLine(m_order);
				line.setM_Product_ID(product.getM_Product_ID(), true);
				if (MOrder.DOCSTATUS_InProgress.equals(m_order.getDocStatus())
				&&  MOrder.DOCACTION_Complete.equals(m_order.getDocAction()))
				{
					if (validateOnHand(line))
					{
						line.setQty(QtyOrdered);
						line.setPrice();
						line.saveEx();
						m_order.renumberLines(10);
						if (MOrder.DOCSTATUS_InProgress.equals(m_order.getDocStatus()))
						{
							preparateIt();
							m_order.renumberLines(10);
						}
						return line;
					}
				} else
				{
					line.setQty(QtyOrdered);
					line.setPrice();
					line.saveEx();
					m_order.renumberLines(10);
					validateOnHand(line);
					if (MOrder.DOCSTATUS_InProgress.equals(m_order.getDocStatus()))
					{
						preparateIt();
						m_order.renumberLines(10);
					}
					return line;
				}
				line.saveEx();
				line.setPrice();
				m_order.renumberLines(10);
			} catch (Exception e)
			{
				throw new AdempiereException("Error createLine: " + e.getStackTrace());
			}
		}
		return null;
	}*/


	/*public MOrderLine createLine(ChargePOS chargePOS, BigDecimal amt)
	{
		if (DOC_TYPE_INVOICE != doctype)
		{
			try
			{
				if (m_order == null)
				{
					throw new AdempiereException("Sales Order do not exist");
				}
				if (!( MOrder.DOCSTATUS_Drafted.equals(m_order.getDocStatus())
					|| MOrder.DOCSTATUS_InProgress.equals(m_order.getDocStatus())))
				{
					VUtil.setMsg("El estado del Documento es " + m_order.getDocStatus(), false);
					return null;
				}



				for (MOrderLine line : m_order.getLines())
				{
					if (line.getC_Charge_ID() == chargePOS.getC_charge_id() )
					{
						line.setQty(Env.ONE);
						line.setPriceEntered((amt));
						line.setPriceActual(amt);
						line.setPriceList(amt);
						line.saveEx();
						m_order.renumberLines(10);
						if (MOrder.DOCSTATUS_InProgress.equals(m_order.getDocStatus()))
						{
							preparateIt();
							m_order.renumberLines(10);
						}
						return line;
					}
				}

				MOrderLine line = new MOrderLine(m_order);
				line.setC_Charge_ID(chargePOS.getC_charge_id());
				if (MOrder.DOCSTATUS_InProgress.equals(m_order.getDocStatus())
				&&  MOrder.DOCACTION_Complete.equals(m_order.getDocAction()))
				{

						line.setQty(Env.ONE);
						line.setPriceEntered((amt));
						line.setPriceActual(amt);
						line.setPriceList(amt);
						line.saveEx();
						m_order.renumberLines(10);
						if (MOrder.DOCSTATUS_InProgress.equals(m_order.getDocStatus()))
						{
							preparateIt();
							m_order.renumberLines(10);
						}
						return line;

				} else
				{
					line.setQty(Env.ONE);
					line.setPriceEntered((amt));
					line.setPriceActual(amt);
					line.setPriceList(amt);
					line.saveEx();
					m_order.renumberLines(10);
					//validateOnHand(line);
					if (MOrder.DOCSTATUS_InProgress.equals(m_order.getDocStatus()))
					{
						preparateIt();
						m_order.renumberLines(10);
					}
					return line;
				}

			} catch (Exception e)
			{
				throw new AdempiereException("Error createLine: " + e.getStackTrace());
			}
		}
		return null;
	}*/



	private MOrder createOrder()
	{
		m_order = new MOrder(Env.getCtx(), 0, null);
		m_order.setIsSOTrx(SOTrx);
		m_order.setIsSelected(true);
		m_order.setSalesRep_ID(getSalesRep_ID());
		m_order.setPOReference(refNo);
		m_order.setDescription(Comment);
		m_order.setM_PriceList_ID(pl.getM_PriceList_ID());
		m_order.setC_Currency_ID(getC_Currency_ID());
		if (poDate != null) {
			m_order.setDateOrdered(poDate);
			m_order.setDateAcct (poDate);
			m_order.setDatePromised(poDate);
		}



		if (C_BPartner_Location_ID != 0) {
			m_order.setC_BPartner_Location_ID(C_BPartner_Location_ID);
		}
		if (M_Warehouse_ID != 0) {
			m_order.setM_Warehouse_ID(M_Warehouse_ID);
		}

		if (partner != null)
		{
			m_order.setC_BPartner_ID(partner.getC_BPartner_ID());
		}
		if (C_DocType_ID != 0)
		{
			m_order.setC_DocTypeTarget_ID(C_DocType_ID);
		}
		else
		{
			if (SOTrx) {
			// Martin changed to default to quote
				//m_order.setC_DocTypeTarget_ID(MOrder.DocSubTypeSO_Standard);
				//m_order.setC_DocTypeTarget_ID(MOrder.DocSubTypeSO_Proposal);
				if (isQuote) {
					m_order.setC_DocTypeTarget_ID(MOrder.DocSubTypeSO_Proposal);
				} else {
					//m_order.setC_DocTypeTarget_ID(MOrder.DocSubTypeSO_POS);
					m_order.setC_DocTypeTarget_ID(MOrder.DocSubTypeSO_Standard);
				}
			} else {
				m_order.setC_DocTypeTarget_ID();  // For POs
			}
		}
		if (partner.getC_PaymentTerm_ID() != 0 && SOTrx ) {
			m_order.setC_PaymentTerm_ID(partner.getC_PaymentTerm_ID());
		} else {
			int ii = Env.getContextAsInt(Env.getCtx(), "#C_PaymentTerm_ID");
			if (ii != 0)
			{
				m_order.setC_PaymentTerm_ID(ii);
			}
			else
			{
				ii = new Query(Env.getCtx(), I_C_PaymentTerm.Table_Name,"IsDefault=?", null)
				.setOnlyActiveRecords(true)
				.setClient_ID()
				.setParameters(new Object[]{"Y"}).firstId();
				if (ii != 0)
					m_order.setC_PaymentTerm_ID(ii);
				else
					throw new AdempiereException("@C_PaymentTerm_ID@ @FindZeroRecords@");
			}
		}
		m_order.saveEx();
		return m_order;
	}


	// Creat Supplier Invoice
	private MInvoice createInvoice()
	{
		m_invoice = new MInvoice(Env.getCtx(), 0, null);
		m_invoice.setIsSOTrx(false);;
		m_invoice.setSalesRep_ID(getSalesRep_ID());
		m_invoice.setPOReference(refNo);
		m_invoice.setDocumentNo(refNo);
		m_invoice.setDescription(Comment);
		m_invoice.setM_PriceList_ID(pl.getM_PriceList_ID());
		m_invoice.setC_Order_ID(C_Order_ID);
		if (getInvoiceDate() != null) {
			m_invoice.setDateInvoiced (getInvoiceDate());
			m_invoice.setDateAcct (getInvoiceDate());
		}



		if (C_BPartner_Location_ID != 0) {
			m_invoice.setC_BPartner_Location_ID(C_BPartner_Location_ID);
		}


		if (partner != null)
		{
			m_invoice.setC_BPartner_ID(partner.getC_BPartner_ID());
			//if (SOTrx) {
				//m_invoice.setC_PaymentTerm_ID(partner.getC_PaymentTerm_ID());  // use the payment term from the BP
			//}
		}
		if (C_DocType_ID != 0)
		{
			m_invoice.setC_DocTypeTarget_ID(C_DocType_ID);
		}
		else
		{

			m_invoice.setC_DocTypeTarget_ID();
		}
		/*int ii = Env.getContextAsInt(Env.getCtx(), "#C_PaymentTerm_ID");
		if (ii != 0)
		{
			m_order.setC_PaymentTerm_ID(ii);
		}
		else
		{
			ii = new Query(Env.getCtx(), I_C_PaymentTerm.Table_Name,"IsDefault=?", null)
			.setOnlyActiveRecords(true)
			.setClient_ID()
			.setParameters(new Object[]{"Y"}).firstId();
			if (ii != 0)
				m_order.setC_PaymentTerm_ID(ii);
			else
				throw new AdempiereException("@C_PaymentTerm_ID@ @FindZeroRecords@");
		}*/
		m_invoice.saveEx();
		return m_invoice;
	}

	/**
	 * createLine to currentDocument
	 *
	 * @param product
	 *            product added
	 * @param QtyOrdered
	 *            Cuantity
	 * @param PriceActual
	 * @return null if error
	 */



	public boolean deleteInvoice() {
		return reverseCorrectItOrder();
	}



	public boolean deleteLine(int charge_ID,boolean isCharge) {
		boolean res = false;
		if (doctype != DOC_TYPE_INVOICE) {
			if (m_order == null)
				res = false;
			if (!m_order.getDocStatus().equals("DR"))
				res = false;
			MOrderLine[] lineas = m_order.getLines();
			int numLineas = lineas.length;
			for (int i = 0; i < numLineas; i++) {
					res = lineas[i].delete(true);
					if (res	& m_order.getDocStatus().equals(
									MOrder.DOCSTATUS_InProgress))
						preparateIt();
					m_order.renumberLines(10);
					return res;
			}
			m_order.renumberLines(10);
		}
		return res;
	}

	/*Hay que ver si el producto "isBom", para borrar los elementos escondidos*/


	private int docNumberToID(int doc) {
		int res = 0;
		try {
			String sql = " SELECT ad_user_id as ID "
				+ " FROM ad_user,C_BPARTNER"
				+ " WHERE C_BPARTNER.c_bpartner_id=ad_user.c_bpartner_id"
				+ " AND ad_user.value='" + doc + "'";
			// duns
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				res = rs.getInt("ID");
			}
			DB.close(rs, pstmt);
		} catch (SQLException ex) {
			Logger.getLogger(VBPartner.class.getName()).log(Level.SEVERE, null,
					ex);
		}
		return res;
	}

	public boolean get0Transfer() {
		return transfer;
	}

	public int getBPartner_ID() {
		return partner.getC_BPartner_ID();
	}



	private int getC_DocSubType(String DocSubTypeSO_x) {
		String sql = "SELECT C_DocType_ID FROM C_DocType "
			+ "WHERE AD_Client_ID=? AND AD_Org_ID IN (0,"
			+ p_pos.getAD_Org_ID() + ") AND DocSubTypeSO=? "
			+ "ORDER BY AD_Org_ID DESC, IsDefault DESC";
		int C_DocType_ID = DB.getSQLValue(null, sql, p_pos.getAD_Client_ID(),
				DocSubTypeSO_x);
		if (C_DocType_ID <= 0)
			System.err.println("Not found for AD_Client_ID="
					+ p_pos.getAD_Client_ID() + ", SubType=" + DocSubTypeSO_x);
		else {
			System.err.println("(SO) - " + DocSubTypeSO_x);
		}
		return C_DocType_ID;
	}

	/*public javax.swing.DefaultComboBoxModel getChannelCombo() {

		int[] bpId;
		bpId = PO.getAllIDs("C_Channel", " AD_Client_ID="
				+ Env.getAD_Client_ID(Env.getCtx()), null);
		Vector vector = new Vector(bpId.length);
		// vector.add(null);
		VComboBoxLoaderInt selected = null;
		try {
			String sql = "SELECT * FROM C_CHANNEL WHERE AD_Client_ID="
				+ Env.getAD_Client_ID(Env.getCtx());
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				VComboBoxLoaderInt loader = new VComboBoxLoaderInt();
				loader.setIndex(rs.getInt("C_Channel_ID"));
				loader.setText(rs.getString("Name"));
				vector.add(loader);
				if (m_order.get_Value("C_Channel_ID") != null)
					if ((Integer) m_order.get_Value("C_Channel_ID") == rs
							.getInt("C_Channel_ID"))
						selected = loader;
			}
			DB.close(rs, pstmt);
		} catch (Exception e) {
			Logger.getLogger(VBPartner.class.getName()).log(Level.SEVERE, null,
					e);
		}
		javax.swing.DefaultComboBoxModel comboVector = new javax.swing.DefaultComboBoxModel(
				vector);
		if (selected != null)
			comboVector.setSelectedItem(selected);
		return comboVector;
	}*/

	/*public javax.swing.DefaultComboBoxModel getCampaignCombo() {

		int[] bpId;
		bpId = PO.getAllIDs("C_Campaign", " AD_Client_ID="
				+ Env.getAD_Client_ID(Env.getCtx()), null);
		Vector vector = new Vector(bpId.length);
		// vector.add(null);
		VComboBoxLoaderInt selected = null;
		try {
			String sql = "SELECT * FROM C_Campaign WHERE AD_Client_ID="
				+ Env.getAD_Client_ID(Env.getCtx());
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();

			while (rs.next()) {
				VComboBoxLoaderInt loader = new VComboBoxLoaderInt();
				loader.setIndex(rs.getInt("C_Campaign_ID"));
				loader.setText(rs.getString("Name"));
				vector.add(loader);
				if (m_order.get_Value("C_Campaign_ID") != null)
					if ((Integer) m_order.get_Value("C_Campaign_ID") == rs
							.getInt("C_Campaign_ID"))
						selected = loader;
			}
			DB.close(rs, pstmt);
		} catch (Exception e) {
			Logger.getLogger(VBPartner.class.getName()).log(Level.SEVERE, null,
					e);
		}
		javax.swing.DefaultComboBoxModel comboVector = new javax.swing.DefaultComboBoxModel(
				vector);
		if (selected != null)
			comboVector.setSelectedItem(selected);
		return comboVector;
	}
*/
	public String getDefaultBP() {
		// String dbp=null;
		if (m_bp_default == null) {
			if (p_pos.getC_BPartnerCashTrx_ID() != 0)
				m_bp_default = new MBPartner(Env.getCtx(), p_pos
						.getC_BPartnerCashTrx_ID(), null).getValue();
		}
		return m_bp_default;
	}

	public int getDocType() {
		return doctype;
	}

	public String getDocumentNo() {
		return DocumentNo;
	}

	public MInvoice getInvoice() {
		return m_invoice;
	}

	private int getInvoiceDocTypeId(int M_RMA_ID) {
		String docTypeSQl = "SELECT dt.C_DocTypeInvoice_ID FROM C_DocType dt "
			+ "INNER JOIN M_RMA rma ON dt.C_DocType_ID=rma.C_DocType_ID "
			+ "WHERE rma.M_RMA_ID=?";

		int docTypeId = DB.getSQLValue(null, docTypeSQl, M_RMA_ID);

		return docTypeId;
	}

	public boolean getIsSelected() {
		return isSelected;
	}

	public int getM_Shipper_ID() {
		return m_M_Shipper_ID;
	}

	public MOrder getOrder() {
		return m_order;
	}

	public void setC_PaymentTerm_ID(int C_PaymentTerm_ID)
	{
		if(m_order!=null)
		{
			m_order.setC_PaymentTerm_ID(C_PaymentTerm_ID);
			if(C_PaymentTerm_ID>0)
			{
				m_order.setPaymentRule(MOrder.PAYMENTRULE_OnCredit);
			}else
			{
				m_order.setPaymentRule(MOrder.PAYMENTRULE_Cash);
			}
			m_order.saveEx();
		}
	}

	/*public javax.swing.DefaultComboBoxModel getOrderTypeCombo() {
		int[] bpId;
		bpId = PO.getAllIDs("ad_ref_list", " AD_Reference_ID ="
				+ VUtil.getParameter("OrderType_Reference"), null);
		Vector vector = new Vector(bpId.length);
		vector.add(null);
		VComboBoxLoaderString selected = null;
		String sel = null;
		if (m_order != null) {
			sel = m_order.getOrderType();
		}
		for (int i = 0; i < bpId.length; i++) {
			MRefList list = new MRefList(Env.getCtx(), bpId[i], null);
			VComboBoxLoaderString loader = new VComboBoxLoaderString();
			loader.setIndex(list.getValue());
			loader.setText(list.getName());
			vector.add(loader);
			if (sel != null) {
				if (sel.equals(list.getValue()))
					selected = loader;
			}
		}
		javax.swing.DefaultComboBoxModel comboVector = new javax.swing.DefaultComboBoxModel(
				vector);
		if (selected != null)
			comboVector.setSelectedItem(selected);
		return comboVector;
	}*/

	public MBPartner getPartner() {
		return partner;
	}

	public MPOS getPOS() {
		return p_pos;
	}

	public int getSalesRep_ID() {
		return C_SalesRep_ID;
	}

	/*public javax.swing.DefaultComboBoxModel getShipperCombo() {
		int[] shId;
		String Shipper = VUtil.getParameter("M_Shipper_ID");
		if (Shipper != null && Shipper.length() > 0)
			Shipper = " AND M_SHIPPER_ID IN(" + Shipper + ")";
		else
			Shipper = "";
		shId = MShipper.getAllIDs(MShipper.Table_Name, "ad_client_id="
				+ Env.getAD_Client_ID(Env.getCtx()) + Shipper, null);
		Vector vector = new Vector(shId.length);
		// vector.add(null);
		VComboBoxLoaderInt selected = null;
		for (int id : shId) {
			MShipper shipper = new MShipper(Env.getCtx(), id, null);
			if (shipper.isActive() == true) {
				VComboBoxLoaderInt loader = new VComboBoxLoaderInt();
				loader.setIndex(shipper.getM_Shipper_ID());
				loader.setText(shipper.getName());
				vector.add(loader);
				if (m_M_Shipper_ID == shipper.getM_Shipper_ID()) {
					selected = loader;
				}
			}
		}
		javax.swing.DefaultComboBoxModel comboVector = new javax.swing.DefaultComboBoxModel(
				vector);
		if (selected != null)
			comboVector.setSelectedItem(selected);

		return comboVector;
	}*/

	/*
	 * public int getC_DocType_ID() { return C_DocType_ID; }
	 */
	/*
	 * public MBPartner getPartner() { return partner; }
	 */

	public String getTrxName() {
		return m_order.get_TrxName();
	}

	public boolean getValidateAsink() {
		return validateAsink;
	}

	public boolean hasCreditNoteRelated() {
		if (m_invoice != null) {
			String whereClause = "poreference = '"
				+ m_invoice.getC_Invoice_ID() + "'";
			Query sql = new Query(Env.getCtx(), MInvoice.Table_Name,
					whereClause, null);
			return sql.match();
		}
		return false;
	}

	private boolean isDocumentApprobed(int c_doctype_id) {
		// VEMOS LAS SEQUENCIAS EsPECIFICAS
		return true;
		/*int res = DB.getSQLValue(null, "SELECT COUNT(getsrivalidto( ? ))",
				c_doctype_id);
		if (res > 0) {
			// VAMOS A AVERIGUAR SI LA AUTORIZACIÓN ACTUAL HA SIDO USADA CON
			// ANTERIORIDAD,
			// SI NO ACTUALIZAMOS EL "VALOR DESDE" PARA QUE SE REPLIQUE.
			String auth = DB.getSQLValueString(null,
					"SELECT getsridocumentnote( ? )", c_doctype_id);
			Timestamp validto = DB.getSQLValueTS(null,
					"SELECT getsrivalidto( ? )", c_doctype_id);
			if (auth != null & validto != null) {
				MSequence sec = new MSequence(Env.getCtx(), MDocType.get(
						Env.getCtx(), c_doctype_id).getDocNoSequence_ID(), null);
				String where = " documentnote=? and validto=? and ad_sequence_id = ? ";
				PO ctrSec = new Query(Env.getCtx(), "TV_CtrlSequence", where,
						null)
				.setParameters(
						new Object[] { auth, validto,
								sec.getAD_Sequence_ID() }).first();
				if (((Integer) ctrSec.get_Value("valuemin")) <= 0) {
					ctrSec.set_CustomColumn("valuemin", sec.getCurrentNext());
					ctrSec.saveEx();
					// PONEMOS EL VALOR FINAL EN EL CONTROL DE SECUENCIA
					// ANTERIOR
					where = " (valuemax is null or valuemax=0) and validto < ? and ad_sequence_id = ?";
					PO ctrSecAnt = new Query(Env.getCtx(), "TV_CtrlSequence",
							where, null).setParameters(
									new Object[] {
											new Timestamp(System.currentTimeMillis()),
											sec.getAD_Sequence_ID() }).first();
					ctrSecAnt.set_CustomColumn("valuemax",
							sec.getCurrentNext() - 1);
					ctrSecAnt.saveEx();
				}
			}
			return true;
		} else
			return false;*/
	}

	public void loadFromInvoice(int C_Invoice_ID) {
		m_invoice = MInvoice.get(Env.getCtx(), C_Invoice_ID);
		if (m_invoice.getC_Order_ID() != 0) {
			m_order = new MOrder(Env.getCtx(), m_invoice.getC_Order_ID(), null);
			m_order.setIsSOTrx(true);
			partner = MBPartner.get(Env.getCtx(), m_order.getC_BPartner_ID());

			if (m_order.getDocStatus().equals(MOrder.ACTION_Complete))
				doctype = DOC_TYPE_INVOICE;
			else
				doctype = DOC_TYPE_ORDER;
		}
	}

	public void preparateIt() {
		m_order.setDocAction(DocAction.ACTION_Prepare);
		m_order.processIt(DocAction.ACTION_Prepare);
		m_order.saveEx(null);
		/*
		 * if (m_order != null) if (m_order.getDocStatus().equals("DR")) {
		 * m_order.prepareIt(); m_order.saveEx(); }
		 */
	}

	public void Print() {
		if (m_order != null) {
			/*VPrintManager printter = new VPrintManager(p_pos, this);
			printter.IsInvoice(isInvoice);
			printter.set_tDiscount(tDiscount);
			printter.Print();*/
		}
	}

	public void Print(String stamp) {
		/*VPrintManager printter = new VPrintManager(p_pos, this);
		printter.IsInvoice(isInvoice);
		printter.set_tDiscount(tDiscount);
		printter.Print(stamp);*/
	}

	public void PrintShipper() {
		if (m_order != null) {
			/*VPrintManager printter = new VPrintManager(p_pos, this);
			printter.IsInvoice(isInvoice);
			printter.set_tDiscount(tDiscount);
			printter.PrintShipper();*/
		}
	}


	public void recalcPriceList() {
		MPriceList pl = null;
		if (partner != null) {
			// Martin added this and removed below
			pl = MPriceList.get(Env.getCtx(), partner.getM_PriceList_ID(),
					m_order.get_TrxName());
			if (pl == null || pl.get_ID() == 0) {
				pl = MPriceList.get(Env.getCtx(),
						p_pos.getM_PriceList_ID(), m_order.get_TrxName());
			}
			/*if (partner.getBPGroup().getName().equals("SELECTO")
					|| partner.getBPGroup().getName().equals("EMPLEADOS")) {
				pl = MPriceList.get(Env.getCtx(), partner.getM_PriceList_ID(),
						m_order.get_TrxName());
				if (pl.get_ID() == 0) {
					pl = MPriceList.get(Env.getCtx(),
							p_pos.getM_PriceList_ID(), m_order.get_TrxName());
				}
			} else
				pl = MPriceList.get(Env.getCtx(), p_pos.getM_PriceList_ID(),
						m_order.get_TrxName());*/
		} else
			pl = MPriceList.get(Env.getCtx(), p_pos.getM_PriceList_ID(),
					m_order.get_TrxName());
		m_order.setM_PriceList_ID(pl.get_ID());
		m_order.save(m_order.get_TrxName());
		MOrderLine[] lines = m_order.getLines();
		// System.out.println("NumLineas: "+numLineas);
		for (MOrderLine line : lines) {
			line.setPrice();
			line.saveEx();
			m_order.renumberLines(10);
		}
		m_order.saveEx();
	}

	private void recreatePrices(MInvoice invoice) {
		for (MOrderLine line : m_order.getLines()) {

			for (MInvoiceLine iline : invoice.getLines()) {
				if ((line.getM_Product_ID() > 0 &&
						line.getM_Product_ID() == iline.getM_Product_ID())
						||
					(line.getC_Charge_ID() > 0 &&
							line.getC_Charge_ID() == iline.getC_Charge_ID())) {
					iline.setPriceList(line.getPriceList());
					iline.setPriceActual(line.getPriceActual());
					iline.setLineNetAmt(line.getLineNetAmt());
					iline.saveEx();
				}
			}
		}
	}

	public boolean reverseCorrectItOrder() {
		boolean res = false;
		if (m_order != null
				&& m_order.getDocStatus().equals(MOrder.DOCSTATUS_Completed)) {
			Timestamp date = m_invoice.getDateInvoiced();
			Timestamp now = new Timestamp(System.currentTimeMillis());
			if (date.getDate() == now.getDate()
					& date.getMonth() == now.getMonth()
					& date.getYear() == now.getYear())
				res = true;
		}
		return res;
	}

	public boolean Save() {
		m_order.setBPartner(partner);
		m_order.setAD_Org_ID(p_pos.getAD_Org_ID());
		if (m_M_Shipper_ID > 0)
			m_order.setM_Shipper_ID(m_M_Shipper_ID);
		int id = getC_BPartner_Location_ID();
		if (id != 0)
			m_order.setC_BPartner_Location_ID(id);
		// Cajero
		m_order.setAD_User_ID(Env.getAD_User_ID(Env.getCtx()));

		m_order.setAD_Org_ID(p_pos.getAD_Org_ID());
		m_order.setM_PriceList_ID(p_pos.getM_PriceList_ID());
		m_order.setM_Warehouse_ID(p_pos.getM_Warehouse_ID());
		m_order.setDeliveryRule(MOrder.DELIVERYRULE_Availability);
		if(m_order.getC_PaymentTerm_ID()==0)
			m_order.setPaymentRule(MOrder.PAYMENTRULE_Cash);
		else
			m_order.setPaymentRule(MOrder.PAYMENTRULE_OnCredit);

		if (!m_order.getDocStatus().equals(MOrder.DOCSTATUS_Completed)) {
			m_order.saveEx();
			/*
			 * {
			 * VUtil.setMsg("El Documento no se ha podido cargar correctamente",
			 * true); m_order = null; return false; }
			 */
		}
		DocumentNo = m_order.getDocumentNo();
		return true;
	}

	public void set0Transfer(boolean trans) {
		transfer = trans;
	}

	public void setC_BPartner_Location_ID(int value) {
		C_BPartner_Location_ID = value;
	}

	public void setChannel(int cha) {
		m_order.set_ValueOfColumn("C_Channel_ID", cha);
		m_order.saveEx();
		System.err.println("Saving Channel " + cha);
	}

	public void setCampaign(int camp_id){
		m_order.set_ValueOfColumn("C_Campaign_ID", camp_id);
		m_order.saveEx();
		System.err.println("Saving Campaign " + camp_id);
	}

	public void setDefaultBP(String bp) {
		m_bp_default = bp;
	}

	public void setDocStatus(String status, String action) {}

	public void setDocType(int type) {
		doctype = type;
	}

	public void setDocument_ID(int value) {
		//DOCUMENT_ID = value;
	}

	public void setEnableWarning(boolean enable) {
		enableWarning = enable;
	}

	public void setInvoice(MInvoice inv) {
		m_invoice = inv;
	}

	public void setIsInvoice(boolean invoice) {
		isInvoice = invoice;
		doctype = DOC_TYPE_INVOICE;
	}

	public void setLastOrderID(int order) {
		lastORDER_ID = order;
	}

	public void setM_Shipper_ID(int id) {
		m_M_Shipper_ID = id;
		m_order.setM_Shipper_ID(m_M_Shipper_ID);
		m_order.saveEx();
	}

	public void setModePOS(int mode) {
		modePOS = mode;
	}

	public void setOrder(MOrder order) {
		m_order = order;
	}

	public void setOrderDescription(String des) {
		//if(m_order.getDescription()!=null&&!m_order.getDescription().equals(des
		// ))
		// des=m_order.getDescription()+des;
		// Martin removed the toUpperCase
		m_order.setDescription(des);
		m_order.saveEx();
	}

	/*public void setOrderType(String ot) {
		m_order.setOrderType(ot);
		m_order.saveEx();
	}*/

	public void setPartner(MBPartner value) {
		partner = value;
	}

	public void setPayments(VPayMethods _pay) {
		payments = _pay;
	}

	public void setPOreference(String po) {
		m_POreference = po;
		//A.C added
		m_order.setPOReference(po);
		m_order.saveEx();
		//End A.C added
	}

	public void setPos(MPOS m_pos) {
		p_pos = m_pos;
	}

	public void setSalesRep_ID(int id) {
		C_SalesRep_ID = id;
		// Vendedor
		// Martin commented
		/*if (C_SalesRep_ID > 0 & m_order != null) {
			m_order.setSalesRep_ID(C_SalesRep_ID);
			m_order.saveEx();
		}*/
	}

	public void setTarjetDiscount(String tarjet) {
		tDiscount = tarjet;
	}

	public void updateLines() {
		MOrderLine[] lines = m_order.getLines();
		for (MOrderLine line : lines) {
			// line.setDiscount(partner.getFlatDiscount());
			if (partner.getFlatDiscount().compareTo(BigDecimal.ZERO) > 0) {
				BigDecimal priceActual = line.getPriceList().subtract(
						line.getPriceList().multiply(
								partner.getFlatDiscount().divide(
										new BigDecimal(100))));
				line.setPriceActual(priceActual);
				line.setDiscount();
				line.save(m_order.get_TrxName());
			}

		}

	}

	/*public boolean validateAvailable() {
		MOrderLine[] lines = m_order.getLines();
		boolean result = false;
		for (MOrderLine line : lines) {
			// BigDecimal onhand=ProductPOS.getQtyOnHand(line.getM_Product_ID(),
			// line.getM_Warehouse_ID());

			 * if(onhand.compareTo(line.getQtyOrdered())<=0) {
			 * if(onhand.compareTo(BigDecimal.ZERO)>0) { return false; }else {
			 * VUtil.setMsg("Stock no Disponible. Actual:"+onhand.toString()+
			 * "\nPara Hacer una Transferencia 0 Presione. Alt+T.", c, true);
			 * return false; }
			 *
			 * }else result=true;

			if (!validateOnHand(line)) {
				VUtil.setMsg("Stock no Disponible. Actual:"
						+ ProductPOS.getQtyOnHand(line.getM_Product_ID(), line
								.getM_Warehouse_ID())
								+ "\n Puede Hacer una Transferencia 0.", true);
				return false;
			} else
				result = true;

		}
		return result;
	}*/




	/**
	 * @param quoteLineIDs	The list of associated quote line orderline id's (for invoicing of a quote)
	 */
	public MOrder createOrderLines(MOrder existingOrder, Object [] lmt,ArrayList prodIDs, ArrayList quoteLineIDs, int discount_charge_id,
			BigDecimal discountOverallAmt,
			int roundcents_charge_id,BigDecimal roundCentsAmt) {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		this.discount_charge_id = discount_charge_id;
		this.discountOverallAmt = discountOverallAmt;
		this.roundcents_charge_id = roundcents_charge_id;
		this.roundCentsAmt = roundCentsAmt;

		rebateOrder = false;
		MOrder order;
		
		if ( existingOrder !=null ) {
			//Reuse existing order (must have no lines)
			order = existingOrder;
		} else {
			//Create new 
			order = createOrder();
		}
		setmOrder(order);
		Integer seqNo = new Integer("0");


		seqNo = createLines(order,lmt,prodIDs,quoteLineIDs, seqNo) + 10;
		if (!CreatePO) {
			createRebateLine(order,seqNo);
			createRoundCentsLine(order,seqNo);
		}

		order = new MOrder(Env.getCtx(), order.getC_Order_ID(), null);   // Refresh order - order was updated by orderlines
		//if (!isQuote() || CreatePO) {
			order.setDocAction(MOrder.ACTION_Complete);
			order.processIt(MOrder.ACTION_Complete);
		//} else {
		//	order.setDocAction(MOrder.ACTION_Prepare);
		//	order.processIt(MOrder.ACTION_Prepare);
		//}
		order.save();




		return order;

	}

	/**
	 * @param quoteLineIDs	The list of associated quote line orderline id's (for invoicing of a quote)
	 */
	private Integer createLines(MOrder pOrder,Object [] plmt,ArrayList prodIDs,
			ArrayList quoteLineIDs, Integer seqNo) {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		int rowCnt = plmt.length;
		//Integer seqNo = new Integer("0");
		for (int i =0; i < rowCnt; i++) {
			if (((Vector)plmt[i]).get(0) != null) {
				seqNo = seqNo + 10;
				Integer prodID = (Integer)prodIDs.get(i-1);   // we store this per row, first line is blank
				Integer qty = null;
				if (((Vector)plmt[i]).get(2) instanceof Integer) {
					qty = (Integer)((Vector)plmt[i]).get(2);
				} else {
				    qty = ((BigDecimal)((Vector)plmt[i]).get(2)).intValue();
				}
				BigDecimal unitPrice = (BigDecimal)((Vector)plmt[i]).get(3);
				BigDecimal unitDiscount = new BigDecimal("0.00");
				if (!CreatePO) {
					BigDecimal newDiscount = ((BigDecimal) ((Vector)plmt[i]).get(5));
					unitDiscount = newDiscount.divide(new BigDecimal(qty),2,BigDecimal.ROUND_HALF_UP);
				}
				BigDecimal newUnitPrice = unitPrice.subtract(unitDiscount);
				//newUnitPrice = newUnitPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
				MOrderLine ol = new MOrderLine(pOrder);
				ol.setLine(seqNo);
				ol.setM_Product_ID(prodID);
				ol.setQty(new BigDecimal(qty));
			    MProduct mProd = new MProduct(Env.getCtx(),prodID,null);
			    String desc = (String)((Vector)plmt[i]).get(1);
			    if (mProd.getDescription() != null && desc != null && !mProd.getDescription().equals(desc)) {
			    	ol.setDescription(desc);
			    }
				/*if (!rebateOrder) {
					ol.setPrice();  // Calculates dicount
				} else */
				{
					// NO Discount
					VPricing vpr = new VPricing();
					boolean m_calculated = vpr.calculatePL(prodID,pOrder.getM_PriceList_ID(),pOrder.getDateOrdered());
					ol.setPriceActual(newUnitPrice);
					ol.setPriceEntered(newUnitPrice);
					ol.setPriceList(unitPrice);
					/*ol.setPriceActual (vpr.getM_PriceStd());
					ol.setPriceList (vpr.getM_PriceList());*/
					ol.setPriceLimit (vpr.getM_PriceLimit());
					//
				}
				ol.setLineNetAmt();
				ol.save();
				
				if (quoteLineIDs!=null) {
					//NCG: For POS invoice - update the associated quote line
					Integer quoteLineID = (Integer)quoteLineIDs.get(i-1);
					if ( quoteLineID != null ) {
						MOrderLine ql = new MOrderLine(Env.getCtx(), quoteLineID, null);
						ql.setQtyInvoiced(new BigDecimal(qty));
						//Not always 1 to 1 , but update anyway
						ql.setRef_OrderLine_ID(ol.getC_OrderLine_ID());
						ql.saveEx();
					}
				}

			}
		}
		return seqNo;
	}

	public void createRebateLine(MOrder mOrder,int seq) {


		if (discount_charge_id > 0  && discountOverallAmt != null && discountOverallAmt.compareTo(Env.ZERO) != 0) {
			MCharge mc = new MCharge(Env.getCtx(),discount_charge_id,null);
			MOrderLine ol = new MOrderLine(mOrder);
			ol.setLine(seq + 10);
			ol.setC_Charge_ID(mc.getC_Charge_ID());
			ol.setQty(new BigDecimal("1"));
			ol.setPriceActual (discountOverallAmt.negate());
			ol.setPriceList   (discountOverallAmt.negate());
			ol.setPriceLimit  (discountOverallAmt.negate());
			ol.save();
		}
	}


	public void createRoundCentsLine(MOrder mOrder,int seq) {


		if (roundcents_charge_id > 0  && roundCentsAmt != null && roundCentsAmt.compareTo(Env.ZERO) != 0) {
			MCharge mc = new MCharge(Env.getCtx(),roundcents_charge_id,null);
			MOrderLine ol = new MOrderLine(mOrder);
			ol.setLine(seq + 10);
			ol.setC_Charge_ID(mc.getC_Charge_ID());
			ol.setQty(new BigDecimal("1"));
			//Issue #1000181: NCG 2014/07/28
			ol.setPriceEntered(roundCentsAmt.negate());
			ol.setPriceActual (roundCentsAmt.negate());
			ol.setPriceList (roundCentsAmt.negate());
			ol.setPriceLimit (roundCentsAmt.negate());
			ol.save();
		}
	}

	public MInvoice createInvoiceLines(Object [] lmt,ArrayList prodIDs,ArrayList chargeIDs, ArrayList orderIDs) {
		MInvoice invoice = createInvoice();
		//setmOrder(order);
		Integer seqNo = new Integer("0");


		seqNo = createLines(invoice,lmt,prodIDs,chargeIDs,orderIDs,seqNo) + 10;

		invoice = new MInvoice(Env.getCtx(), invoice.getC_Invoice_ID(), null);   // Refresh order - order was updated by orderlines
		invoice.setDocAction(MInvoice.ACTION_Complete);
	    invoice.processIt(MInvoice.ACTION_Complete);

		invoice.save();
		return invoice;

	}

	private void updateCosting(MProduct product, BigDecimal costPrice) {

		String costingLevel = null;
		MAcctSchema[] aschemas = MAcctSchema.getClientAcctSchema(Env.getCtx(), Env.getAD_Client_ID(Env.getCtx()));
		if (aschemas == null || aschemas[0] == null || !aschemas[0].getCostingMethod().equals("i")) {
			return;
		}
		if(product.getM_Product_Category_ID() > 0){
			MProductCategoryAcct pca = MProductCategoryAcct.get(Env.getCtx(), product.getM_Product_Category_ID(),
					aschemas[0].getC_AcctSchema_ID(), null);
			costingLevel = pca.getCostingLevel();
			if (costingLevel == null) {
				costingLevel = aschemas[0].getCostingLevel();
			}

		}

		int costOrgID = 0; //p_AD_OrgTrx_ID;
		int costASI = 0; //line.getM_AttributeSetInstance_ID();
		if (MAcctSchema.COSTINGLEVEL_Client.equals(costingLevel)){
			costOrgID = 0;
			costASI = 0;
		} else if (MAcctSchema.COSTINGLEVEL_Organization.equals(costingLevel)) {
			costASI = 0;
		}
		MCostElement ces[] = MCostElement.getElements(Env.getCtx(), null);
		if (ces == null) {
			return;
		}
		MCostElement ce = null;
		for (int i = 0; i < ces.length; i++) {
			if (ces[i].isLastInvoice()) {
				ce = ces[i];
				break;
			}
		}
		int p_M_CostElement_ID = ce.getM_CostElement_ID();
		MCost cost = MCost.get (MProduct.get(Env.getCtx(), product.getM_Product_ID()), costASI
				, aschemas[0], costOrgID, p_M_CostElement_ID, null);
		cost.setCurrentCostPrice( costPrice );
		cost.saveEx();
		// Update price lists when costs are updated
		VPricing.updateLimitPrices(product.getM_Product_ID(), Env.getCtx(), costPrice);
	}

	private Integer createLines(MInvoice pInvoice,Object [] plmt,ArrayList prodIDs,ArrayList chargeIDs,ArrayList orderIDs,
			Integer seqNo) {
		int rowCnt = plmt.length;
		//Integer seqNo = new Integer("0");
		for (int i =0; i < rowCnt; i++) {
			if (((Vector)plmt[i]).get(0) != null) {
				seqNo = seqNo + 10;
				Integer prodID = (Integer)prodIDs.get(i-1);   // we store this per row, first line is blank
				Integer chargeID = (Integer)chargeIDs.get(i-1);
				Integer orderID = (Integer)orderIDs.get(i-1);
				Integer qty = 0;
				if (((Vector)plmt[i]).get(2) instanceof Integer) {
					qty = ((Integer)((Vector)plmt[i]).get(2));
				} else {
					qty = ((BigDecimal)((Vector)plmt[i]).get(2)).intValue();
				}
				BigDecimal unitPrice = (BigDecimal)((Vector)plmt[i]).get(3);
				MInvoiceLine il = new MInvoiceLine(pInvoice);
				il.setLine(seqNo);
				il.setM_Product_ID((prodID==null) ? 0 : prodID);  // prodID or charge will be 0
				il.setC_Charge_ID((chargeID==null) ? 0 : chargeID);
				il.setQty(new BigDecimal(qty));
				if (orderID != null) {  // For charges entered this will be null
					il.setC_OrderLine_ID(orderID);
				}
				{

					il.setPriceActual(unitPrice);
					il.setPriceList(unitPrice);
					/*ol.setPriceActual (vpr.getM_PriceStd());
					ol.setPriceList (vpr.getM_PriceList());*/
					il.setPriceLimit (unitPrice);
					il.setPriceEntered(unitPrice);
					//NCG: #1000394: Duplicate key error on F8 Dazzle Supplier Invoice - NPE - added && prodID!=0
					if (prodID != null && prodID!=0) {
						MProduct mProd = new MProduct(Env.getCtx(),prodID,null);
						updateCosting(mProd, unitPrice);
					}
					//
				}
				il.setLineNetAmt();
				il.save();

			}
		}
		return seqNo;
	}



	//Modified: A. Meghraj (nTier) - Changed from void to returning the payment ID
//	public void createPayment(int C_Order_ID,BigDecimal Amt,String tenderType,int c_BankAccount_ID) {
	public int createPayment(int C_Order_ID,BigDecimal Amt,String tenderType,int c_BankAccount_ID,String creditCardNo,
			String creditCardType, MInvoice invoice,BigDecimal ExcessAmt) {
		MPayment mPayment = new MPayment(Env.getCtx(), 0, null);
	    mPayment.setAmount(getC_Currency_ID(), Amt);
	    mPayment.setOverUnderAmt(ExcessAmt);
	    mPayment.setC_BPartner_ID(partner.getC_BPartner_ID());
	    if (C_Order_ID > 0) {
	    	mPayment.setC_Order_ID(C_Order_ID);
	    	mPayment.setIsPrepayment(false);
	    } else {
	    	mPayment.setIsPrepayment(true);
	    }
	    if (invoice != null) {
	    	mPayment.setC_Invoice_ID(invoice.getC_Invoice_ID());
	    }
	    mPayment.setC_BankAccount_ID(c_BankAccount_ID);
	    mPayment.setTenderType(tenderType);
	    mPayment.setC_DocType_ID(true);


		if (creditCardNo != null && !creditCardNo.equals("")) {
			mPayment.setCreditCardNumber(creditCardNo);
		}

		if (creditCardType != null && !creditCardType.equals("")) {
			mPayment.setCreditCardType(creditCardType);
		}



	    mPayment.save();  // save so we have a payment ID for the allocation
	    mPayment.setDocAction(MPayment.ACTION_Complete);
		mPayment.processIt(MPayment.ACTION_Complete);
		mPayment.save();
		//processRecon(mPayment);

		//Added: A. Meghraj (nTier) - returning the payment ID
	    return mPayment.getC_Payment_ID();
	}

	private void processRecon(MPayment mPayment) {
		MReconHdr mZZReconHdr = MReconHdr.getCurrentRecon(Env.getCtx(), mPayment.getCreatedBy());
		MReconLine mZZReconLine = new MReconLine(Env.getCtx(),0,null);
		mZZReconLine.setC_Payment_ID(mPayment.getC_Payment_ID());
		mZZReconLine.setAmount(mPayment.getPayAmt());
		mZZReconLine.setZZ_Recon_Hdr_ID(mZZReconHdr.getZZ_Recon_Hdr_ID());
		mZZReconLine.setTenderType(mPayment.getTenderType());
		mZZReconLine.save();
	}



	public MRMA createRMA() {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		MOrder origOrder = null;

		if (getC_Order_ID() != 0) {
			origOrder = new MOrder(Env.getCtx(),getC_Order_ID(),null);
		}
		MRMA mRMA = null;
		int C_DocTypeShipment_ID = 0;
		int C_DocTypeInvoice_ID = 0;
		try {
			mRMA = new MRMA(Env.getCtx(),0,null);
			mRMA.setName("Return Of Goods: "	+ (origOrder == null ? "":origOrder.getDocumentNo()));

			//C_DocTypeInvoice_ID = (Integer) p_pos.get_Value("C_DocTypeReval_ID");
			if (isSOTrx()) {
				for (MDocType doc : MDocType.getOfDocBaseType(Env.getCtx(),MDocType.DOCBASETYPE_SalesOrder)) {
					if (doc.isSOTrx() == true && doc.getDocSubTypeSO() !=null
							&& doc.getDocSubTypeSO().equals(MDocType.DOCSUBTYPESO_ReturnMaterial)) {
						C_DocTypeShipment_ID = doc.getC_DocTypeShipment_ID();
						mRMA.setC_DocType_ID(doc.getC_DocType_ID());
						continue;
					}
				}
			} else {
				for (MDocType doc : MDocType.getOfDocBaseType(Env.getCtx(),MDocType.DOCBASETYPE_PurchaseOrder)) {
					if (!doc.isSOTrx() && doc.getDocSubTypeSO() !=null
							&& doc.getDocSubTypeSO().equals(MDocType.DOCSUBTYPESO_ReturnMaterial)) {
						C_DocTypeShipment_ID = doc.getC_DocTypeShipment_ID();
						mRMA.setC_DocType_ID(doc.getC_DocType_ID());
						continue;
					}
				}
			}
			mRMA.setSalesRep_ID(getSalesRep_ID());
			//mRMA.setM_InOut_ID((origOrder == null ? 0 :Integer.valueOf(origOrder.get_ValueAsString("Help")).intValue()));
			if (inOutID > 0) {
				mRMA.setM_InOut_ID(inOutID);
			} else {
				MInOut [] mInOut = origOrder.getShipments();
				if (mInOut != null && mInOut.length >= 1) {
					mRMA.setM_InOut_ID(mInOut[0].getM_InOut_ID());
				}
			}
			mRMA.setM_RMAType_ID(PO.getAllIDs("M_RMAType", "", null)[0]);
			mRMA.setIsSOTrx(isSOTrx());
			mRMA.setDescription(getComment());
			//NCG 07 Aug 2014: See InvoiceGenerateRMADazzle, where invoice's POReference is set using this field 
			mRMA.setHelp( getRefNo() );
			mRMA.saveEx();

		} catch (Exception e) {

		}
		return mRMA;

	}

	public MRMA createRMALines(Object [] plmt,ArrayList<Integer> prodIDs,ArrayList<Integer> chargeIDs,Integer seqNo,
			ArrayList<Integer>  m_InOutLineIDs,ArrayList<Integer> c_InvoiceIDs,String trxName) {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		MRMA mRMA = createRMA();
		if (mRMA == null) {
			return null;
		}
		MOrder origOrder = new MOrder(Env.getCtx(),getC_Order_ID(),null);
		int rowCnt = plmt.length;
		//Integer seqNo = new Integer("0");
		for (int i =0; i < rowCnt; i++) {
			if (((Vector)plmt[i]).get(0) != null) {

				Integer prodID = (Integer)prodIDs.get(i-1);   // we store this per row, first line is blank
				Integer chargeID = (Integer)chargeIDs.get(i-1);
				Integer invoiceID = (Integer)c_InvoiceIDs.get(i-1);

				BigDecimal qty = null;
				if (((Vector)plmt[i]).get(2) instanceof BigDecimal) {
					qty = ((BigDecimal)((Vector)plmt[i]).get(2));
				} else  {
					qty = new BigDecimal(((Integer)((Vector)plmt[i]).get(2)));
				}
				if (qty.compareTo(Env.ZERO) == 0) {  // exclude 0 lines
					continue;
				}
				seqNo = seqNo + 10;
				BigDecimal unitPrice = (BigDecimal)((Vector)plmt[i]).get(3);
				BigDecimal lineAmt = qty.multiply(unitPrice);
				lineAmt = lineAmt.setScale(2,BigDecimal.ROUND_HALF_UP);
				MRMALine rmaLine = new MRMALine(Env.getCtx(), 0, trxName);
				rmaLine.setLine(seqNo);
				rmaLine.setM_RMA_ID(mRMA.getM_RMA_ID());
				if (prodID != null) {
					rmaLine.setM_Product_ID(prodID);
				}
				if (chargeID != null && chargeID > 0 ) {
					//NCG: #1000183: Tax not being calculated on the charge on Dazzle Credit Note 
					//Use the tax from the charge
					rmaLine.setC_Charge_ID(chargeID);
					int tax_ID;
					tax_ID = TaxMA.get(Env.getCtx(), 0 /*getM_Product_ID()*/, 
							chargeID, 
							origOrder.getDateOrdered(), 
							origOrder.getDateOrdered(),
							origOrder.getAD_Org_ID(), 
							getM_Warehouse_ID(),
							getC_BPartner_Location_ID(),		//	should be bill to
							getC_BPartner_Location_ID(), 
							origOrder.isSOTrx() 
							/* trxName */);
					rmaLine.setC_Tax_ID( tax_ID );
				} else {
					//Use the tax from the original invoice line
					MInvoiceLine mInvoiceLine = null;
					if (invoiceID != null && invoiceID > 0) {
						mInvoiceLine = new MInvoiceLine(Env.getCtx(), invoiceID, trxName);
						rmaLine.setC_Tax_ID(mInvoiceLine.getC_Tax_ID());
					}
				}
				rmaLine.setQty(qty);
				if (m_InOutLineIDs != null && m_InOutLineIDs.get(i-1) != null) {
					rmaLine.setM_InOutLine_ID((Integer)m_InOutLineIDs.get(i-1));
					int inoutLineID = rmaLine.getM_InOutLine_ID();
//					log.warning("***************************************************************** M_Inoutline_id=" + inoutLineID);
//					log.warning("***************************************************************** Doc No=" + rmaLine.getM_InOutLine().getC_OrderLine().getC_Order().getDocumentNo());
				}
				rmaLine.setAmt(unitPrice);
				rmaLine.setLineNetAmt(lineAmt);
				try {
					rmaLine.saveEx();
				} catch (Exception e) {
					//NCG 2014/07/28
					log.warning("************** WARNING **************: Silent exception: " + e);
					e.printStackTrace();
				}
			}
		}
		mRMA = new MRMA(Env.getCtx(),mRMA.getM_RMA_ID(),null);  // lines change the header so we need to get the new version
		mRMA.setDocAction(MPayment.ACTION_Complete);
	    mRMA.processIt(MPayment.ACTION_Complete);
	    mRMA.saveEx();
		return mRMA;
	}


	/**
	 * 	Create Invoice
	 *	@param dt order document type
	 *	@param shipment optional shipment
	 *	@param invoiceDate invoice date
	 *	@return invoice or null
	 */
	public MInvoice createInvoice (MDocType dt, MInOut shipment, Timestamp invoiceDate,MOrder mOrder)
	{
		if (log.isLoggable(Level.INFO)) log.info(dt.toString());
		MInvoice invoice = new MInvoice (mOrder, dt.getC_DocTypeInvoice_ID(), invoiceDate);
		if (SOTrx) {
			invoice.setC_PaymentTerm_ID(mOrder.getC_PaymentTerm_ID());
		}
		if (!invoice.save(null))
		{
			m_processMsg = "Could not create Invoice";
			return null;
		}

		//	If we have a Shipment - use that as a base
		if (shipment != null)
		{
			if (!X_C_Order.INVOICERULE_AfterDelivery.equals(mOrder.getInvoiceRule()))
				mOrder.setInvoiceRule(X_C_Order.INVOICERULE_AfterDelivery);
			//
			MInOutLine[] sLines = shipment.getLines(false);
			for (int i = 0; i < sLines.length; i++)
			{
				MInOutLine sLine = sLines[i];
				//
				MInvoiceLine iLine = new MInvoiceLine(invoice);
				iLine.setShipLine(sLine);
				//	Qty = Delivered
				if (sLine.sameOrderLineUOM())
					iLine.setQtyEntered(sLine.getQtyEntered());
				else
					iLine.setQtyEntered(sLine.getMovementQty());
				iLine.setQtyInvoiced(sLine.getMovementQty());
				if (!iLine.save(null))
				{
					m_processMsg = "Could not create Invoice Line from Shipment Line";
					return null;
				}
				//
				sLine.setIsInvoiced(true);
				if (!sLine.save(null))
				{
					log.warning("Could not update Shipment line: " + sLine);
				}
			}
		}
		else	//	Create Invoice from Order
		{
			if (!X_C_Order.INVOICERULE_Immediate.equals(mOrder.getInvoiceRule()))
				mOrder.setInvoiceRule(X_C_Order.INVOICERULE_Immediate);
			//
			MOrderLine[] oLines = mOrder.getLines();
			for (int i = 0; i < oLines.length; i++)
			{
				MOrderLine oLine = oLines[i];
				//
				MInvoiceLine iLine = new MInvoiceLine(invoice);
				iLine.setOrderLine(oLine);
				//	Qty = Ordered - Invoiced
				iLine.setQtyInvoiced(oLine.getQtyOrdered().subtract(oLine.getQtyInvoiced()));
				if (oLine.getQtyOrdered().compareTo(oLine.getQtyEntered()) == 0)
					iLine.setQtyEntered(iLine.getQtyInvoiced());
				else
					iLine.setQtyEntered(iLine.getQtyInvoiced().multiply(oLine.getQtyEntered())
						.divide(oLine.getQtyOrdered(), 12, BigDecimal.ROUND_HALF_UP));
				if (!iLine.save(null))
				{
					m_processMsg = "Could not create Invoice Line from Order Line";
					return null;
				}
			}
		}

		// Copy payment schedule from order to invoice if any
		for (MOrderPaySchedule ops : MOrderPaySchedule.getOrderPaySchedule(Env.getCtx(), mOrder.getC_Order_ID(), 0, null)) {
			MInvoicePaySchedule ips = new MInvoicePaySchedule(Env.getCtx(), 0, null);
			PO.copyValues(ops, ips);
			ips.setC_Invoice_ID(invoice.getC_Invoice_ID());
			ips.setAD_Org_ID(ops.getAD_Org_ID());
			ips.setProcessing(ops.isProcessing());
			ips.setIsActive(ops.isActive());
			if (!ips.save()) {
				m_processMsg = "ERROR: creating pay schedule for invoice from : "+ ops.toString();
				return null;
			}
		}

		// added AdempiereException by zuhri
		if (!invoice.processIt(DocAction.ACTION_Complete))
			throw new AdempiereException("Failed when processing document - " + invoice.getProcessMsg());
		// end added
		invoice.saveEx(null);
		mOrder.setC_CashLine_ID(invoice.getC_CashLine_ID());
		if (!X_C_Order.DOCSTATUS_Completed.equals(invoice.getDocStatus()))
		{
			m_processMsg = "@C_Invoice_ID@: " + invoice.getProcessMsg();
			return null;
		}
		return invoice;
	}

	/**
	 * 	Create Shipment
	 *	@param dt order document type
	 *	@param movementDate optional movement date (default today)
	 *	@return shipment or null
	 */
	public MInOut createShipment(MOrder order,MInvoice invoice,MDocType dt, Timestamp movementDate,String trxName)
	{
		if (log.isLoggable(Level.INFO)) log.info("For " + dt);
		//MInOut shipment = new MInOut (order, dt.getC_DocTypeShipment_ID(), movementDate);
		//MInOut shipment = new MInOut (invoice, dt.getC_DocTypeShipment_ID(), movementDate, order.getM_Warehouse_ID());
	//	shipment.setDateAcct(getDateAcct());


		//
		m_inout = null;
		for (MInvoiceLine invoiceLine : invoice.getLines(false))
		{
			createLine(invoice, invoiceLine,order.getM_Warehouse_ID());
		}
		// added AdempiereException by zuhri
		if (m_inout != null) {
			if (!m_inout.processIt(DocAction.ACTION_Complete))
				throw new AdempiereException("Failed when processing document - " + m_inout.getProcessMsg());
			// end added
			m_inout.saveEx(trxName);
			if (!order.DOCSTATUS_Completed.equals(m_inout.getDocStatus()))
			{
				m_processMsg = "@M_InOut_ID@: " + m_inout.getProcessMsg();
				return null;
			}
		}
		return m_inout;
	}	//	createShipment

	private MInOutLine createLine(MInvoice invoice, MInvoiceLine invoiceLine,int p_M_Warehouse_ID)
	{
		BigDecimal qtyMatched = invoiceLine.getMatchedQty();
		BigDecimal qtyInvoiced = invoiceLine.getQtyInvoiced();
		BigDecimal qtyNotMatched = qtyInvoiced.subtract(qtyMatched);
		// If is fully matched don't create anything
		if (qtyNotMatched.signum() == 0)
		{
			return null;
		}
		MInOut inout = getCreateHeader(invoice,p_M_Warehouse_ID);
		MInOutLine sLine = new MInOutLine(inout);
		sLine.setInvoiceLine(invoiceLine, 0,	//	Locator
			invoice.isSOTrx() ? qtyNotMatched : Env.ZERO);
		sLine.setQtyEntered(qtyNotMatched);
		sLine.setMovementQty(qtyNotMatched);
		if (invoice.isCreditMemo())
		{
			sLine.setQtyEntered(sLine.getQtyEntered().negate());
			sLine.setMovementQty(sLine.getMovementQty().negate());
		}
		sLine.saveEx();
		//
		invoiceLine.setM_InOutLine_ID(sLine.getM_InOutLine_ID());
		invoiceLine.saveEx();
		//
		return sLine;
	}



	private MInOut getCreateHeader(MInvoice invoice,int p_M_Warehouse_ID)
	{
		if (m_inout != null)
			return m_inout;
		m_inout = new MInOut (invoice, C_DocTypeShipment_ID, getInvoiceDate(), p_M_Warehouse_ID);
		m_inout.saveEx();
		return m_inout;
	}

	public MMovement createMovementLines(Object [] lmt,ArrayList prodIDs,ArrayList locatorToIDs,int warehouseFrom, int warehouseTo) {
		MMovement m_movement = CreateMovementHeader();
		//setmOrder(order);
		Integer seqNo = new Integer("0");


		seqNo = createLines(m_movement,lmt,prodIDs,locatorToIDs,seqNo,warehouseFrom, warehouseTo) + 10;

		m_movement = new MMovement(Env.getCtx(), m_movement.getM_Movement_ID(), null);   // Refresh movement
		m_movement.setDocAction(MMovement.ACTION_Complete);
		m_movement.processIt(MMovement.ACTION_Complete);

		m_movement.saveEx();
		return m_movement;

	}

	private Integer createLines(MMovement pMovement,Object [] plmt,ArrayList prodIDs,ArrayList locatorToIDs,
			 Integer seqNo,int warehouseFrom, int warehouseTo) {
		int rowCnt = plmt.length;
		MWarehouse mWarehouseFrom = MWarehouse.get(Env.getCtx(), warehouseFrom);
		MLocator mLocatorFrom = MLocator.getDefault(mWarehouseFrom);

		MWarehouse mWarehouseTo = MWarehouse.get(Env.getCtx(), warehouseTo);
		MLocator mLocatorTo = MLocator.getDefault(mWarehouseTo);
		//Integer seqNo = new Integer("0");
		for (int i =0; i < rowCnt; i++) {
			if (((Vector)plmt[i]).get(0) != null) {
				seqNo = seqNo + 10;
				Integer prodID = (Integer)prodIDs.get(i-1);   // we store this per row, first line is blank
				Integer locatorToID = (Integer)locatorToIDs.get(i-1);
				Integer qty = 0;
				if (((Vector)plmt[i]).get(3) instanceof Integer) {
					qty = ((Integer)((Vector)plmt[i]).get(3));
				} else {
					qty = ((BigDecimal)((Vector)plmt[i]).get(3)).intValue();
				}
				MMovementLine ml = new MMovementLine(pMovement);
				ml.setLine(seqNo);
				ml.setM_Product_ID((prodID==null) ? 0 : prodID);  // prodID o
				ml.setMovementQty(new BigDecimal(qty));
				int M_LocatorFrom_ID = MStorageOnHand.getM_Locator_ID (mWarehouseFrom.getM_Warehouse_ID(),prodID, 0,new BigDecimal(qty), null);
				ml.setM_Locator_ID((M_LocatorFrom_ID==0) ? mLocatorFrom.getM_Locator_ID() : M_LocatorFrom_ID);  //Use the locator with storage
				//ml.setM_Locator_ID(mLocatorFrom.getM_Locator_ID());

				ml.setM_LocatorTo_ID((locatorToID==null) ? mLocatorTo.getM_Locator_ID() : locatorToID);
				//ml.setM_LocatorTo_ID(mLocatorTo.getM_Locator_ID());
				ml.saveEx();

			}
		}
		return seqNo;
	}

	private MMovement CreateMovementHeader() {
		MMovement m_movement = new MMovement(Env.getCtx(), 0, null);
		m_movement.setMovementDate(movementDate);
		m_movement.setDescription(Comment);
		m_movement.saveEx();
		return m_movement;
	}


	// For returns
	public int createCreditNoteOrShipment(MRMA mRMA,int AD_Process_ID) {  // Return instance ID
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		// Generate the credit memo
		String docno = null;
		int instance_ID = 0;


		//int AD_Process_ID = SystemIDs.PROCESS_C_INVOICE_GENERATERMA_MANUAL;
		MPInstance instance = new MPInstance(Env.getCtx(), AD_Process_ID, 0);
		if (!instance.save())
		{
			//info.setText(Msg.getMsg(Env.getCtx(), "ProcessNoInstance"));
			//return;
		} else {
			StringBuilder insert = new StringBuilder();
			insert.append("INSERT INTO T_SELECTION(AD_PINSTANCE_ID, T_SELECTION_ID) " +
					" Values (" + instance.getAD_PInstance_ID() +
					" ," + mRMA.getM_RMA_ID() +
					")");
			if ( DB.executeUpdate(insert.toString(), null) < 0 )
			{
				String msg = "No Shipments";     //  not translated!
				log.config(msg);
				//info.setText(msg);
				//trx.rollback();
				//return;
			} else {
				//InvoiceGenerateRMA IGRMA = new InvoiceGenerateRMA();
				//call process
				ProcessInfo pi = new ProcessInfo ("GenCreditMemo", AD_Process_ID);
				pi.setAD_PInstance_ID (instance.getAD_PInstance_ID());
				instance_ID = instance.getAD_PInstance_ID();

				//	Add Parameter - Selection=Y
				MPInstancePara ip = new MPInstancePara(instance, 10);
				ip.setParameter("Selection","Y");
				if (!ip.save())
				{
					String msg = "No Parameter added";  //  not translated
					//info.setText(msg);
					//log.log(Level.SEVERE, msg);
					//return;
				}
				ip = new MPInstancePara(instance, 20);
				ip.setParameter("InvoiceDate",getInvoiceDate());
				if (!ip.save())
				{
					String msg = "No Parameter added";  //  not translated
					//info.setText(msg);
					//log.log(Level.SEVERE, msg);
					//return;
				}

				else {
					//								Execute Process
					Trx trx = null;
					IProcessUI parent = null;
					WProcessCtl worker = new WProcessCtl(parent, 0, pi, trx);
					//worker.start();     //  complete tasks in unlockUI
					worker.run();
				}
			}
		}
		return instance_ID;

	}

	public int getHostess_ID() {
		return hostess_ID;
	}
	public void setHostess_ID(int hostessID) {
		hostess_ID = hostessID;
	}
	public boolean isRebateOrder() {
		return rebateOrder;
	}
	public void setRebateOrder(boolean rebateOrder) {
		this.rebateOrder = rebateOrder;
	}
	public BigDecimal getHostessDiscount() {
		return hostessDiscount;
	}
	public void setHostessDiscount(BigDecimal hostessDiscount) {
		this.hostessDiscount = hostessDiscount;
	}
	public int getM_Warehouse_ID() {
		return M_Warehouse_ID;
	}
	public void setM_Warehouse_ID(int mWarehouseID) {
		M_Warehouse_ID = mWarehouseID;
	}
	public int getC_BPartner_Location_ID() {
		return C_BPartner_Location_ID;
	}

	public BigDecimal getDiscountLevel() {
		return discountLevel;
	}
	public void setDiscountLevel(BigDecimal discountLevel) {
		this.discountLevel = discountLevel;
	}
	public boolean isQuote() {
		return isQuote;
	}
	public void setQuote(boolean isQuote) {
		this.isQuote = isQuote;
	}
	public int getC_Currency_ID() {
		return c_Currency_ID;
	}
	public void setC_Currency_ID(int c_Currency_ID) {
		this.c_Currency_ID = c_Currency_ID;
	}
	public MOrder getmOrder() {
		return mOrder;
	}
	public void setmOrder(MOrder mOrder) {
		this.mOrder = mOrder;
	}
	public String getCreditCardNo() {
		return CreditCardNo;
	}
	public void setCreditCardNo(String creditCardNo) {
		CreditCardNo = creditCardNo;
	}


}
