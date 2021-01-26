package org.adempiere.process;

import java.math.BigDecimal;

import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MOrg;
import org.compiere.model.MPOS;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.X_C_Invoice;
import org.compiere.model.X_C_InvoiceLine;
import org.compiere.process.SvrProcess;
import org.compiere.util.Env;
import org.eevolution.pos.VDocument;

public class ZZ_Create_MissingOrders extends SvrProcess{

	public final static int PROCESS_M_INOUT_GENERATERMA_CUST = 1000019;
	private MPOS m_pos = null;
	int cnt = 0;

	@Override
	protected void prepare() {
		// TODO Auto-generated method stub

	}

	@Override
	protected String doIt() throws Exception {
		// TODO Auto-generated method stub

		VDocument doc = new VDocument(null);
		int ids [] = PO.getAllIDs("C_Invoice", "AD_Client_ID = " + Env.getAD_Client_ID(Env.getCtx()) +
				" and c_invoice.c_doctype_id = 1000002" +
				" and C_Invoice.docstatus = 'CO' and C_Invoice.C_Order_ID is null" +
				" and C_invoice.dateinvoiced >= to_date('01082013','ddmmyyyy')", null);
		for (int i = 0; i < ids.length; i++) {
			X_C_Invoice inv = new X_C_Invoice(Env.getCtx(), ids[i], null);
			if (inv != null) {
				createOrder(inv);
				cnt++;

			}
		}
		return "Records created" + cnt;
	}

	private MOrder createOrder(X_C_Invoice i)
	{
		MOrder m_order = new MOrder(Env.getCtx(), 0, null);
		m_order.setIsSOTrx(true);
		m_order.setIsSelected(true);
		m_order.setSalesRep_ID(i.getSalesRep_ID());
		m_order.setPOReference(i.getPOReference());
		m_order.setDescription(i.getDescription());
		m_order.setM_PriceList_ID(i.getM_PriceList_ID());
		m_order.setC_Currency_ID(i.getC_Currency_ID());

		m_order.setDateOrdered(i.getDateInvoiced());
		m_order.setDateAcct (i.getDateInvoiced());
		m_order.setDatePromised(i.getDateInvoiced());


		MOrg org = new MOrg(Env.getCtx(),m_order.getAD_Org_ID(), null);
		int userID = Env.getAD_User_ID(Env.getCtx());
		String whereClause = " AD_Org_ID = ?  ";
		m_pos = new Query(Env.getCtx(), MPOS.Table_Name, whereClause, null)
		.setParameters(new Object[] { Env.getAD_Org_ID(Env.getCtx()) })
		.firstOnly();

		if (i.getC_BPartner_Location_ID() != 0) {
			m_order.setC_BPartner_Location_ID(i.getC_BPartner_Location_ID());
		}
		if (m_pos.getM_Warehouse_ID() != 0) {
			m_order.setM_Warehouse_ID(m_pos.getM_Warehouse_ID());
		}

		if (i.getC_BPartner_ID() != 0)
		{
			m_order.setC_BPartner_ID(i.getC_BPartner_ID());
		}
		m_order.setC_DocTypeTarget_ID(MOrder.DocSubTypeSO_Standard);
		m_order.setC_PaymentTerm_ID(i.getC_PaymentTerm_ID());
		m_order.setDeliveryRule(MOrder.DELIVERYRULE_Force);
		m_order.saveEx();
		createLines(m_order,i);
		return m_order;
	}

	private void createLines(MOrder pOrder,X_C_Invoice inv) {
		int ids [] = PO.getAllIDs("C_InvoiceLine", "C_Invoice_ID = " + inv.getC_Invoice_ID(),null);

		int seqNo = 0;
		for (int i = 0; i < ids.length; i++) {
			X_C_InvoiceLine il = new X_C_InvoiceLine(Env.getCtx(), ids[i], null);
			seqNo = seqNo + 10;
			MOrderLine ol = new MOrderLine(pOrder);
			ol.setLine(seqNo);
			ol.setM_Product_ID(il.getM_Product_ID());
			ol.setQty(il.getQtyInvoiced());
			ol.setPriceActual(il.getPriceActual());
			ol.setPriceEntered(il.getPriceEntered());
			ol.setPriceList(il.getPriceList());
			ol.setPriceLimit (il.getPriceLimit());
			ol.setLineNetAmt();
			ol.setC_UOM_ID(il.getC_UOM_ID());
			ol.save();
		}
		pOrder = new MOrder(Env.getCtx(), pOrder.getC_Order_ID(), null);   // Refresh order - order was updated by orderlines
		pOrder.setDocAction(MOrder.ACTION_Complete);
		pOrder.processIt(MOrder.ACTION_Complete);

		pOrder.save();
		inv.setC_Order_ID(pOrder.getC_Order_ID());
		inv.save();
	}

}
