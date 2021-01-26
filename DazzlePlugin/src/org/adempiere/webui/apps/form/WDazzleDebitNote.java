package org.adempiere.webui.apps.form;
//package org.adempiere.webui.apps.form;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;

import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Borderlayout;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.ComboboxNew;
import org.adempiere.webui.component.Datebox;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.ListModelTable;
import org.adempiere.webui.component.ListboxFactoryMA;
import org.adempiere.webui.component.NumberBox;
import org.adempiere.webui.component.Panel;
import org.adempiere.webui.component.Row;
import org.adempiere.webui.component.Rows;
import org.adempiere.webui.component.SimpleListModelNew;
import org.adempiere.webui.component.Textbox;
import org.adempiere.webui.component.WListboxMA;
import org.adempiere.webui.event.TableValueChangeEvent;
import org.adempiere.webui.event.TableValueChangeListener;
import org.adempiere.webui.event.ValueChangeEvent;
import org.adempiere.webui.event.ValueChangeListener;
import org.adempiere.webui.event.WTableModelEvent;
import org.adempiere.webui.event.WTableModelListener;
import org.adempiere.webui.panel.ADForm;
import org.adempiere.webui.panel.CustomForm;
import org.adempiere.webui.panel.IFormController;
import org.adempiere.webui.window.FDialog;
import org.compiere.minigrid.IMiniTable;
import org.compiere.model.MBPartner;
import org.compiere.model.MBPartnerLocation;
import org.compiere.model.MCharge;
import org.compiere.model.MInOutLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MMatchInv;
import org.compiere.model.MOrder;
import org.compiere.model.MOrg;
import org.compiere.model.MOrgInfo;
import org.compiere.model.MPInstance;
import org.compiere.model.MPOS;
import org.compiere.model.MPriceList;
import org.compiere.model.MProcess;
import org.compiere.model.MProduct;
import org.compiere.model.MRMA;
import org.compiere.model.MTax;
import org.compiere.model.MUser;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.SystemIDs;
import org.compiere.model.TaxCustom;
import org.compiere.model.X_C_Charge;
import org.compiere.model.X_M_Product;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.report.ReportStarterDazzle;
import org.compiere.util.CLogger;
import org.compiere.util.DB;
import org.compiere.util.DisplayType;
import org.compiere.util.Env;
import org.eevolution.pos.VDocument;
import org.eevolution.pos.VPricing;
import org.zkoss.zk.au.out.AuFocus;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Center;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.North;
import org.zkoss.zul.South;
import org.zkoss.zul.Space;

/**
 * @author 	Martin Augustine
 * 				Original author
 * @author 	Neil Gordon
 * 				Various modifications
 * 
 * Important Note: Before starting any development, or addressing any bug fix, please be sure to read the documents under:
 *  		etc/doc/systemsdocumentation, especially the document: Development Notes. Consider also: dazzleDevelopment/etc/doc/tickets

 * NB: This is "Dazzle Supplier Credit Note" in ad_form
 */
public class WDazzleDebitNote extends WDazzleBase
implements IFormController, EventListener<Event>, WTableModelListener, ValueChangeListener,TableValueChangeListener
{
	/**
	 *
	 */
	public final static int PROCESS_M_INOUT_GENERATERMA_CUST = 1000000;
	public final static int PROCESS_C_INVOICE_GENERATERMA_MANUAL_VENDOR = 1000015;
	public final static int PROCESS_M_INOUT_GENERATERMA_MANUAL_VENDOR = 1000018;
	private BigDecimal discountOverallAmt = Env.ZERO;
	private BigDecimal roundCentsAmt = Env.ZERO;
	private static final int DISCOUNT_CHARGE_ID   = 1000000;
	private static final int ROUNDCENTS_CHARGE_ID = 1000001;
	private static final long serialVersionUID = 1L;
	private boolean taxIncluded = true;
	private Properties m_ctx = Env.getCtx();
	private Vector<Object> data = new Vector<Object>();
	private Vector<Integer> olddata = new Vector<Integer>();
	private ListModelTable model = new ListModelTable(data);
	private Vector<String> columnNames = new Vector<String>();
	private Vector<Integer> columnWidths = new Vector<Integer>();
	private WListboxMA resultsTable = ListboxFactoryMA.newDataTable();

	private Borderlayout mainLayout = new Borderlayout();
	private Panel topPanel = new Panel();
	private Grid topLayout = GridFactory.newGridLayout();
	private Grid bottomLayout = GridFactory.newGridLayout();
	// private Grid resultLayout = GridFactory.newGridLayout();
	private Borderlayout resultLayout = new Borderlayout();
	private Panel centerPanel = new Panel();
	private Panel bottomPanel = new Panel();
	private CustomForm form = new CustomForm();

	private Label lblCustomer = new Label("Supplier");
	private ComboboxNew cmbCustomer = new ComboboxNew();

	private static CLogger log = CLogger.getCLogger(WDazzleDebitNote.class);

	private Intbox intQuantity = new Intbox();
	private Label lblQuantity = new Label("Quantity");

	private NumberBox discountOverall = new NumberBox(true);
	private Label lbldiscountOverall = new Label("Overall Discount %");

	private ComboboxNew cmbCharge = new ComboboxNew();
	private Label lblCharge = new Label("Charge");
	private NumberBox nbrAmount= new NumberBox(false,true);
	private Label lblAmount= new Label("Amount");
	private int charge_ID_sel = 0;

	private ComboboxNew cmbProduct = new ComboboxNew();
	private Label lblProduct = new Label("Product");


	private Label lblCustomerRefNo = new Label("Reference No");
	private Textbox txtCustomerRefNo = new Textbox();
	private Label lblComment = new Label("Comment");
	private Textbox txtComment = new Textbox("Initial Comment");
	private Label lblInvoiceNo = new Label("Invoice No");
	private ComboboxNew cmbInvoiceNo = new ComboboxNew();
	private Label lblDate = new Label("Date");
	private Datebox dateInvoiceDate = new Datebox();



	private MPOS m_pos = null;

	private boolean newProduct = false;

	private MBPartner currentBPartner;

	private MPriceList pl = null;
	private MUser user = null;

	private int[] prod_id = null;
	private Integer index = 0;
	private int orderID = 0;

	private ArrayList<Integer> prodIDs = new ArrayList<Integer>();
	private ArrayList<Integer> chargeIDs = new ArrayList<Integer>();
	private ArrayList<Integer> inOutLineIDs = new ArrayList<Integer>();
	private ArrayList<Integer> c_InvoiceIDs = new ArrayList<Integer>();

	private int inOutID = 0;

	private Label lbltotalDue = new Label("Total Due");
	private Label lblTotalLines = new Label("Total");
	private Textbox txtTotalLines = new Textbox();
	private BigDecimal totalLines = new BigDecimal("0.00");
	private Label lblTotalTax = new Label("Tax");
	private Textbox txtTotalTax = new Textbox();
	private Label lblDiscountAmt = new Label("Discount Amount");
	private Textbox txtDiscountAmt = new Textbox();
	private Label lblSubTotal = new Label("SubTotal");
	private Textbox txtSubTotal = new Textbox();

	private Label lblCC = new Label("Credit cards");
	private Textbox txtCC = new Textbox();
	private Label lblCash = new Label("Cash");
	private Textbox txtCash = new Textbox();
	private Label lblCheques = new Label("Cheques");
	private Textbox txtCheques = new Textbox();
	private Label lblBalDue = new Label("Balance Due");
	private Textbox txtBalDue = new Textbox();
	private Textbox txttotalDue = new Textbox();
	private Label lblChange = new Label("Change");
	private Textbox txtChange = new Textbox();
	private Button btnCreate = new Button("Complete Order");
	private Button btnCancel = new Button("Cancel");
	private Button btnBack = new Button("Back");


	private Label lblEFT = new Label("EFT");
	private Textbox txtEFT = new Textbox();
	private Label lblChequeNo = new Label("Cheque Number");
	private Textbox txtChequeNo = new Textbox();

	private Button F6 = new Button("F6 - Clear");
	private Button F7 = new Button("F7 - Delete Line");
	private Button F8 = new Button("F8 - Checkout");
	
	protected Label lblHelp = new Label("F7 - Delete line, F8 - Checkout");
	
	private boolean back = false;
	private int linesAdded = 0;


	protected void initForm() {

		try {
			if (!setMPOS()) {
				FDialog.error(form.getWindowNo(), "No Pos Terminal for this Branch");
				return;
			}
			zkInit();
		} catch (Exception e) {
			log.log(Level.SEVERE, "", e);
		}

	}


	private void zkInit () {
		super.setHeight("100%");
		this.setCtrlKeys("#f8#f7#f9");
//		this.setCtrlKeys("#f8#f6#f7#f9");
		this.appendChild(mainLayout);
		mainLayout.setWidth("100%");
		mainLayout.setHeight("100%");
		//mainLayout.setStyle("background-image: url('/webui/images/MainScreen.jpg')  ");
		mainLayout.setStyle("position:absolute;");
		//topLayout.setStyle("border: none; background:transparent;");
		topPanel.appendChild(topLayout);
		//resultLayout.setStyle("background:transparent;");
		centerPanel.appendChild(resultLayout);

		North north = new North();
		//north.setFlex(true);


		mainLayout.appendChild(north);
		//topPanel.setStyle("background:transparent;");
		north.appendChild(topPanel);

		Center center = new Center();
		mainLayout.appendChild(center);
		//resultsTable.setStyle("background:transparent;");
		center.appendChild(resultsTable);
		resultsTable.setWidth("100%");
		resultsTable.setHeight("90%");
		center.setStyle("border: none; background:transparent;");
		center.setFlex(true);
		if (!back) {  // back key pressed
			headerload();
		}
		back = false;

		South south = new South();
		south.setFlex(true);
		//south.setStyle("border: none; background:transparent");
		mainLayout.appendChild(south);
		//bottomPanel.setStyle("background:transparent");
		south.appendChild(bottomPanel);
		//bottomLayout.setStyle("background:transparent");
		bottomPanel.appendChild(bottomLayout);
		bottomLayout.setWidth("1200px");

		Rows rows = null;
		Row row = null;

	    topLayout.setWidth("1000px");
		rows = topLayout.newRows();




		// Row 2
		row = rows.newRow();

		row.setSpans("1,2,1,2");
		row.appendChild(lblCustomer);
		row.appendChild(cmbCustomer);

		//cmbCustomer.setReadonly(true);
		//cmbCustomer.setDisabled(true);

		cmbCustomer.setValue(null);



		String sql2 = "select bp.value || '-' || bp.name,bp.C_Bpartner_ID from C_Bpartner bp,c_bp_group bpg where  "
				+ " (upper(bp.value)) like ?"
				+ " and bp.c_bp_group_id = bpg.c_bp_group_id and bp.isvendor = 'Y'"
				+ " and bp.isactive='Y' "
				+ " and bp.ad_client_Id = "
				+ Env.getAD_Client_ID(m_ctx) + " ORDER BY bp.value";
		SimpleListModelNew dictModel2 = new SimpleListModelNew();

		dictModel2.setCbox(cmbCustomer);
		cmbCustomer.setModel(dictModel2);
		cmbCustomer.setQueryName(sql2);
		cmbCustomer.setAutodrop(true);
		cmbCustomer.setButtonVisible(true);
		cmbCustomer.setWidth("400px");



		if (!cmbCustomer.isListenerAvailable(Events.ON_CHANGE, false)) {
			cmbCustomer.addEventListener(Events.ON_CHANGE,new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(org.zkoss.zk.ui.event.Event event)
						throws Exception {
					clearBottomScreen();
					clearScreen();
					//cmbInvoiceNo.setValue(null);
					if (cmbCustomer.getValue() != null && cmbCustomer.getSelectedItem() != null) {
						int cust_ID_sel = ((WObjRet)cmbCustomer.getSelectedItem().getValue()).getIDOfRecord();
						int selIndex = cmbCustomer.getSelectedIndex();
						if (selIndex > -1) {
							currentBPartner = new MBPartner(Env.getCtx(), cust_ID_sel, null);
							MBPartnerLocation [] loc  = MBPartnerLocation.getForBPartner(m_ctx, currentBPartner.getC_BPartner_ID()
									,null);
							if (loc == null || loc.length <= 0) {
								cmbCustomer.setFocus(true);
								throw new WrongValueException(cmbCustomer, "Supplier requires an address");
							}
							cmbInvoiceNo.setReadonly(false);
							cmbInvoiceNo.setDisabled(false);
							setComboInvoiceNo();
						}

						cmbInvoiceNo.setFocus(true);
					} else {
						cmbCustomer.setValue(null);
						cmbCustomer.setFocus(true);
						currentBPartner = null;
						cmbInvoiceNo.setReadonly(true);
						cmbInvoiceNo.setDisabled(true);
						cmbInvoiceNo.setValue(null);
						throw new WrongValueException(cmbCustomer, "Please select a Supplier");
					}

				}
			});
		}


		if (!cmbInvoiceNo.isListenerAvailable(Events.ON_CHANGE, false)) {
			cmbInvoiceNo.addEventListener(Events.ON_CHANGE,new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(org.zkoss.zk.ui.event.Event event)
						throws Exception {
					clearBottomScreen();
					clearScreen();
					if (cmbInvoiceNo.getValue() != null && cmbInvoiceNo.getSelectedItem() != null) {
						int selIndex = cmbInvoiceNo.getSelectedIndex();
						if (selIndex > -1) {
							int invoiceID = ((WObjRet)cmbInvoiceNo.getSelectedItem().getValue()).getIDOfRecord();
							populateScreen(invoiceID);
						}
						cmbInvoiceNo.setFocus(true);
					} else {

					}

				}
			});
		}

		if (!txtComment.isListenerAvailable(Events.ON_CHANGE, false)) {
			 txtComment.addEventListener(Events.ON_CHANGE,new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(org.zkoss.zk.ui.event.Event event)
						throws Exception {
					if (txtComment.getValue() != null) {

					}
				}
			});
		}

		row = rows.newRow();
		row.appendChild(lblInvoiceNo);
		cmbInvoiceNo.setReadonly(true);
		cmbInvoiceNo.setDisabled(true);
	    row.appendChild(cmbInvoiceNo);


		row.appendChild(lblCustomerRefNo);
		txtCustomerRefNo.setWidth("400px");
		row.appendChild(txtCustomerRefNo);

		row = rows.newRow();
		row.setSpans("1,3");
		row.appendChild(lblComment);
		txtComment.setStyle("width:99%; height: 99%; margin: auto; display: inline-block;");
		txtComment.setMultiline(true);
		txtComment.setRows(3);
		//txtComment.setWidth("400px");
		row.appendChild(txtComment);
		txtComment.setValue("");

		row = rows.newRow();
		row.appendChild(lblDate);
		Date currentDate = new Date();
		dateInvoiceDate.setValue(currentDate);
		row.appendChild(dateInvoiceDate);

		//String sql = "select p.name || ' | ' ||  (case when p.description is null then '' else p.description end) as " +
		String sql = "select p.name ||  (case when p.description is null then '' else ' | ' || p.description end) as " +
				" description,p.C_Charge_id " +
                " from C_Charge p" +
                " where (upper(p.name)) like ?"  +
                " and p.ad_client_ID = " + Env.getAD_Client_ID(m_ctx) +
                " order by p.name";

		// sqlProd = sql;
		 SimpleListModelNew dictModel= new SimpleListModelNew();
		 dictModel.setCbox(cmbCharge);
		 cmbCharge.setModel(dictModel);
		 cmbCharge.setQueryName(sql);
		 cmbCharge.setAutodrop(true);
		 //cmbCharge.setFocus(true);

		 if (!cmbCharge.isListenerAvailable(Events.ON_CHANGE, false)) {
			 cmbCharge.addEventListener(Events.ON_CHANGE,
					 new org.zkoss.zk.ui.event.EventListener(){
				 public void onEvent(org.zkoss.zk.ui.event.Event event) throws Exception{
					 if (cmbCharge.getSelectedItem() == null || cmbCharge.getSelectedItem().getValue() == null) {
						 charge_ID_sel = 0;
						 throw new WrongValueException(cmbCharge, "Please select a charge");
						 //return;
					 }
					 charge_ID_sel = ((WObjRet) cmbCharge.getSelectedItem().getValue()).getIDOfRecord();
					 newProduct = true;
					 if (cmbCharge.getSelectedItem() != null)
						 nbrAmount.setFocus(true);
					 //End

				 }
			 });
		 }


			if (!nbrAmount.isListenerAvailable(Events.ON_OK, false)) {
				nbrAmount.addEventListener(Events.ON_OK,
						new org.zkoss.zk.ui.event.EventListener(){
					public void onEvent(org.zkoss.zk.ui.event.Event event) throws Exception{
						if (newProduct) {
							EnterPressedOnAmount ();
							newProduct = false;

						} else {
							if (charge_ID_sel == 0 || cmbCharge.getValue() == null) {
								cmbCharge.setFocus(true);
								throw new WrongValueException(cmbCharge, "Please select a charge");
							}
						}
					}
				});
			}

		cmbCharge.setButtonVisible(false);
		row = rows.newRow();
		row.setStyle("border: none; background:transparent;");
		row.setSpans("1,3,1");
		lblCharge.setStyle("background:transparent;");
		row.appendChild(lblCharge);
		cmbCharge.setStyle("background:transparent;");
		cmbCharge.setWidth("600px");
		row.appendChild(cmbCharge);
		lblAmount.setStyle("padding-left:60px");
		row.appendChild(lblAmount);
		nbrAmount.setTooltiptext("Enter Amount");
		nbrAmount.setFormat(DisplayType.getNumberFormat(DisplayType.Amount, AEnv.getLanguage(Env.getCtx())));
		row.appendChild(nbrAmount);







		// End of Row 3
		//   Bottom Panel
		Rows rows2 = null;
		Row row2 = null;
		rows2 = bottomLayout.newRows();

		row2 = rows2.newRow();
		row2.appendChild(new Space());
		row2.appendChild(new Space());
		row2.appendChild(lblSubTotal);
		lblSubTotal.setHeight("40px");
		lblSubTotal.setStyle("font-size:14pt");
		txtSubTotal.setWidth("150px");
		txtSubTotal.setReadonly(true);
		txtSubTotal.setStyle("text-align:right;font-size:14pt;padding: 5px");
		row2.appendChild(txtSubTotal);

		/*row2 = rows2.newRow();
		row2.appendChild(new Space());
		row2.appendChild(new Space());
		row2.appendChild(lblDiscountAmt);
		lblDiscountAmt.setHeight("40px");
		lblDiscountAmt.setStyle("font-size:14pt");
		txtDiscountAmt.setWidth("150px");
		txtDiscountAmt.setReadonly(true);
		txtDiscountAmt.setStyle("text-align:right;font-size:14pt;padding: 5px");
		row2.appendChild(txtDiscountAmt);*/

		row2 = rows2.newRow();
		row2.appendChild(new Space());
		row2.appendChild(new Space());
		row2.appendChild(lblTotalTax);
		lblTotalTax.setHeight("40px");
		lblTotalTax.setStyle("font-size:14pt");
		txtTotalTax.setWidth("150px");
		txtTotalTax.setReadonly(true);
		txtTotalTax.setStyle("text-align:right;font-size:14pt;padding: 5px");
		row2.appendChild(txtTotalTax);


		// ROW1
		row2 = rows2.newRow();
		row2.appendChild(lblHelp);					//NCG #1000328: Display help on Dazzle screens
		//row2.appendChild(new Space());
		row2.appendChild(new Space());
		row2.appendChild(lblTotalLines);
		lblTotalLines.setHeight("40px");
		lblTotalLines.setStyle("font-size:14pt");
		txtTotalLines.setWidth("150px");
		txtTotalLines.setReadonly(true);
		txtTotalLines.setStyle("text-align:right;font-size:14pt;padding: 5px");
		row2.appendChild(txtTotalLines);



			if (!F7.isListenerAvailable(Events.ON_CLICK, false)) {
				F7.addEventListener(Events.ON_CLICK,
						new org.zkoss.zk.ui.event.EventListener() {
							public void onEvent(org.zkoss.zk.ui.event.Event event)
									throws Exception {
								deleteLine();
								intQuantity.setFocus(true);
							}
						});
			}

			Clients.response(new AuFocus(cmbCustomer));

			//cmbCustomer.setFocus(true);






	}

	private void setComboInvoiceNo() {
		cmbInvoiceNo.setValue(null);
		String sql2 = "select I.Documentno || '_ R' || I.grandTotal,I.C_invoice_ID " +
				" from C_Invoice I where  "
				+ " I.C_Bpartner_ID = " + currentBPartner.getC_BPartner_ID()
				+ " and I.documentno like ?"
				+ " and I.docstatus in ('CO','CL')"
				+ " and I.isactive='Y' "
				+ " and I.C_DocType_ID in "
				+ " (select dt.c_docType_ID from c_docType dt where dt.docbasetype = 'API' and dt.ad_client_ID = i.ad_client_ID)"
				+ " and I.ad_client_Id = "	+ Env.getAD_Client_ID(m_ctx)
				//+ " and not exists (select 'x' from c_invoice cn where cn.c_docType_ID = "
				//+ " (select dt.c_docType_ID from c_docType dt where dt.docbasetype = 'ARC' and dt.ad_client_ID = i.ad_client_ID)"
				//+ " and cn.C_Order_ID is not null"
				//+ " and cn.c_order_ID = I.C_Order_ID)"
				+ " ORDER BY I.documentno";
		SimpleListModelNew dictModel2 = new SimpleListModelNew();

		dictModel2.setCbox(cmbInvoiceNo);
		cmbInvoiceNo.setModel(dictModel2);
		cmbInvoiceNo.setQueryName(sql2);
		cmbInvoiceNo.setAutodrop(true);
		cmbInvoiceNo.setButtonVisible(true);
		cmbInvoiceNo.setWidth("400px");
	}

	private void populateScreen(int c_Invoice_ID) {
		inOutID = 0;
		MInvoice mInvoice = new MInvoice(m_ctx, c_Invoice_ID, null);
		orderID = mInvoice.getC_Order_ID();
		if (orderID == 0) {
			throw new WrongValueException(cmbInvoiceNo,"There is no order for this invoice");
		} else {
			MOrder origOrder = new MOrder(Env.getCtx(),orderID,null);
			MMatchInv [] mmatchInv = MMatchInv.getInvoice(m_ctx, c_Invoice_ID, null);
			//MInOut [] mInOut = origOrder.getShipments();
			if (mmatchInv == null || mmatchInv.length <= 0) {
				//FDialog.error(form.getWindowNo(),
				//		"Order was never shipped, Cannot return goods");
				//return;
				throw new WrongValueException(cmbInvoiceNo, "Purchase Order was never shipped, Cannot return goods");
			}
		}
		if (mInvoice.getPOReference() != null && !mInvoice.getPOReference().equals(null)) {
			txtCustomerRefNo.setValue(mInvoice.getPOReference());
		}
		if (mInvoice.getDescription() != null && !mInvoice.getDescription().equals(null)) {
			String desc = mInvoice.getDescription();
			txtComment.setValue(desc);
		}


        headerload();
        linesAdded = 0;
		int ids [] = PO.getAllIDs("C_InvoiceLine", "C_Invoice_ID = " + mInvoice.getC_Invoice_ID() + " order by line" , null);
		for (int i = 0; i < ids.length; i++) {
			MInvoiceLine mInvoiceLine = new MInvoiceLine(m_ctx,ids[i],null);
			if (mInvoiceLine.getC_Charge_ID() > 0) {
				continue;
			}
			int m_InOut_ID = mInvoiceLine.getM_InOutLine_ID();
			String sql = "Select sum(r.qty) from M_RMALine r where r.m_inOutLine_ID = ?";
			int qtyLeftToCredit = mInvoiceLine.getQtyInvoiced().intValue();
			BigDecimal qtyRet = DB.getSQLValueBD(null, sql, m_InOut_ID);
			if (qtyRet != null) {
				qtyLeftToCredit = qtyLeftToCredit - qtyRet.intValue();
			}
			if (qtyLeftToCredit <= 0) {
				continue;
			}


			String Description = "";
			if (mInvoiceLine.getM_Product_ID() != 0) {
				MProduct mProduct = new MProduct(m_ctx,mInvoiceLine.getM_Product_ID(),null);
				Description = mProduct.getValue() + "-" + mProduct.getName();
			} else if (mInvoiceLine.getC_Charge_ID() != 0) {
				MCharge mCharge = new MCharge(m_ctx,mInvoiceLine.getC_Charge_ID(),null);
				Description = mCharge.getName();
			}
			prodIDs.add(new Integer(mInvoiceLine.getM_Product_ID()));
			chargeIDs.add(new Integer(mInvoiceLine.getC_Charge_ID()));
			inOutLineIDs.add(new Integer(mInvoiceLine.getM_InOutLine_ID()));
			if (inOutID == 0 && mInvoiceLine.getM_InOutLine_ID() > 0) {
				MInOutLine minoutLine = new MInOutLine(m_ctx, mInvoiceLine.getM_InOutLine_ID(), null);
				inOutID = minoutLine.getM_InOut_ID();
			}
			c_InvoiceIDs.add(new Integer(mInvoiceLine.getC_InvoiceLine_ID()));
			index = new Integer(index.intValue() + 10);
			BigDecimal RSP = (mInvoiceLine.getPriceActual().multiply(new BigDecimal(qtyLeftToCredit)))
					.setScale(2, BigDecimal.ROUND_HALF_UP);
			Vector<Object> line = new Vector<Object>();
			line.add(index);
			line.add(Description);
			line.add(new Integer(qtyLeftToCredit));
			line.add(mInvoiceLine.getPriceActual());
			line.add(RSP);
			data.add(line);
			olddata.add(new Integer(qtyLeftToCredit));
			linesAdded++;
		}

		/*olddata = (Vector<Object>)data.clone(); */

		resultsTable.clear();
		model = new ListModelTable(data);
		model.addTableModelListener(this);
		resultsTable.setData(model, columnNames, columnWidths);
		setLineColumnClass(resultsTable);
		calculateTotsBottomPanel();
		if (linesAdded <= 0) {
		  cmbInvoiceNo.setFocus(true);
 		  throw new WrongValueException(cmbInvoiceNo, "All lines were already credited");
		}






	}

	private void EnterPressedOnAmount() {

		if (currentBPartner == null || cmbCustomer.getValue() == null) {
			//FDialog.error(form.getWindowNo(), "Please select a Supplier");
			cmbCustomer.setValue(null);
			nbrAmount.setValue(1);
			cmbCustomer.setFocus(true);
			throw new WrongValueException(cmbCustomer, "Please select a Supplier");
		}

		if (charge_ID_sel == 0 || cmbCharge.getValue() == null) {
			charge_ID_sel = 0;
			throw new WrongValueException(cmbCharge, "Please select a charge");
		}

		BigDecimal unitPrice = nbrAmount.getValue();
		MCharge mCharge = new MCharge(m_ctx, charge_ID_sel, null);

		MOrg org = new MOrg(m_ctx, Env.getAD_Org_ID(m_ctx), null);
		MOrgInfo mOrgInfo = MOrgInfo.get(m_ctx, org.getAD_Org_ID(),null);
		int tax_ID = TaxCustom.get(m_ctx, mCharge.getC_TaxCategory_ID(), true,
				new Timestamp(System.currentTimeMillis()), mOrgInfo
						.getC_Location_ID(), mOrgInfo
						.getC_Location_ID(),
				new Timestamp(System.currentTimeMillis()), mOrgInfo
						.getC_Location_ID(), mOrgInfo
						.getC_Location_ID());
		MTax mTax = new MTax(m_ctx, tax_ID, null);
		BigDecimal tax = mTax.calculateTax(unitPrice,pl.isTaxIncluded(), 2);
		Integer qty = new Integer("1");
		BigDecimal unitPSV = unitPrice.subtract(tax);
		tax = tax.multiply(new BigDecimal(qty.intValue()));

		BigDecimal linePSV = (unitPrice.multiply(new BigDecimal(qty
				.intValue())));
		if (pl.isTaxIncluded()) {
			linePSV = linePSV.subtract(tax);
		} else {
			taxIncluded = false;
		}

		BigDecimal[] prices = {Env.ZERO,unitPrice};
		chargeLoad(unitPrice, charge_ID_sel, prices[0],prices[1], linePSV, unitPSV);
		nbrAmount.setValue("0");
		cmbCharge.setValue("");
		charge_ID_sel = 0;
	    cmbCharge.setFocus(true);


	}

	public void chargeLoad( BigDecimal unitPrice,int Pcharge_ID_sel, BigDecimal discount,
			BigDecimal priceAfterDiscount, BigDecimal PSV, BigDecimal unitPSV) {

		if (Pcharge_ID_sel != 0) {
			// Creating the table grid
			MCharge charge = new MCharge(Env.getCtx(), Pcharge_ID_sel, null);

			Integer qty = 1;


			chargeIDs.add(new Integer(charge.getC_Charge_ID()));
			prodIDs.add(null);
			inOutLineIDs.add(null);
			c_InvoiceIDs.add(null);
			index = new Integer(index.intValue() + 10);
			BigDecimal RSP = (unitPrice.multiply(new BigDecimal(qty)))
					.setScale(2, BigDecimal.ROUND_HALF_UP);
			BigDecimal discountAmt = new BigDecimal("0.00");
			Vector<Object> line = new Vector<Object>();
			line.add(index);
			line.add(charge.getName() + "-" + ((charge.getDescription() == null) ? " " : charge.getDescription()));
			line.add(qty);
			line.add(unitPrice);
			line.add(RSP);
			data.add(line);
			olddata.add(new Integer("0"));
			resultsTable.clear();
			model = new ListModelTable(data);
			model.addTableModelListener(this);
			resultsTable.setData(model, columnNames, columnWidths);
			setLineColumnClass(resultsTable);
			calculateTotsBottomPanel();
		}
	}


	private void deleteLine() {


		int selInx = resultsTable.getSelectedIndex();
		if (selInx > -1) {

			prodIDs.remove(selInx - 1); // First line on grid blank
			chargeIDs.remove(selInx - 1); // First line on grid blank
			inOutLineIDs.remove(selInx - 1);
			c_InvoiceIDs.remove(selInx - 1);
			resultsTable.clear();
			data.remove(selInx);
			olddata.remove(selInx);
			model = new ListModelTable(data);
			model.addTableModelListener(this);
			resultsTable.setData(model, columnNames, columnWidths);
			setLineColumnClass(resultsTable);
			calculateTotsBottomPanel();
		}
	}





	private void calculateTotsBottomPanel() {
		BigDecimal calcTot = calculateTotal(model.toArray(), 4, false, prodIDs,chargeIDs);
		txtTotalLines.setValue(calcTot.toString());

	}


	private BigDecimal calculateTotal(Object[] lmt, int col,
			boolean otherItems, ArrayList<Integer> PprodIDs,ArrayList<Integer> PchargeIDs) {
		BigDecimal tot = Env.ZERO;
		BigDecimal totTax = Env.ZERO;

		int rowCnt = lmt.length;
		for (int i = 0; i < rowCnt; i++) {
			if (((Vector) lmt[i]).get(0) != null) {
				Integer prodID = (Integer) PprodIDs.get(i - 1); // we store this
																// per row,
																// first line is
																// blank
				Integer chargeID = (Integer) PchargeIDs.get(i - 1);
				int taxCategory_ID = 0;
				if (prodID != null && prodID > 0) {
					X_M_Product x_prod = new X_M_Product(m_ctx, prodID, null);
					VPricing vpr = new VPricing();
					pl = vpr.getPriceList(prodID, m_ctx, currentBPartner);
					taxCategory_ID = x_prod.getC_TaxCategory_ID();
				} else if (chargeID > 0) {
					X_C_Charge x_charge = new X_C_Charge(m_ctx, chargeID, null);
					VPricing vpr = new VPricing();
					pl = vpr.getPriceList(0, m_ctx, currentBPartner);
					taxCategory_ID = x_charge.getC_TaxCategory_ID();
				}
				BigDecimal totalLine = (BigDecimal) ((Vector) lmt[i]).get(col);
				tot = tot.add(totalLine);
				if (taxCategory_ID > 0) {
					totTax = totTax.add(taxCalc(taxCategory_ID, totalLine));
				}

			}
		}

		txtSubTotal.setValue(tot.toPlainString());

		discountOverallAmt = Env.ZERO;



		totTax = totTax.setScale(2, BigDecimal.ROUND_HALF_UP);
		txtTotalTax.setValue(totTax.toPlainString());

		tot = tot.add(totTax);
		tot = tot.setScale(2, BigDecimal.ROUND_HALF_UP);




		return tot;
	}

	private BigDecimal taxCalc(int c_TaxCategory_ID, BigDecimal Price) {
		if (pl == null) {
			return Env.ZERO;
		}
		MOrg org = new MOrg(m_ctx, Env.getAD_Org_ID(m_ctx), null);
		MOrgInfo mOrgInfo = MOrgInfo.get(m_ctx, org.getAD_Org_ID(),null);
		int tax_ID = TaxCustom.get(m_ctx, c_TaxCategory_ID, true,
				new Timestamp(System.currentTimeMillis()), mOrgInfo
						.getC_Location_ID(), mOrgInfo
						.getC_Location_ID(),
				new Timestamp(System.currentTimeMillis()), mOrgInfo
						.getC_Location_ID(), mOrgInfo
						.getC_Location_ID());
		MTax mTax = new MTax(m_ctx, tax_ID, null);
		BigDecimal tax = mTax.calculateTax(Price,pl.isTaxIncluded(), 12);
		return tax;

	}



	public void headerload()
    {
            columnNames.clear();
            columnNames.add("Index");
            columnNames.add("Description");
            columnNames.add("Quantity");
            columnNames.add("Unit Price");
            //columnNames.add("Incl Price");
            //columnNames.add("Discount");
            columnNames.add("Total");

            columnWidths.clear();
            columnWidths.add(new Integer(60));
            columnWidths.add(new Integer(800));
            columnWidths.add(new Integer(100));
            columnWidths.add(new Integer(100));
           // columnWidths.add(new Integer(15));
            //columnWidths.add(new Integer(15));
            columnWidths.add(new Integer(100));


            Vector<Object> line = new Vector<Object>();
            data.clear();
            olddata.clear();
	    	data.add(line);
	    	olddata.add(null);

            model = new ListModelTable(data);
            model.addTableModelListener(this);
            resultsTable.setData(model, columnNames, columnWidths);
            setLineColumnClass(resultsTable);


    }

	public void setLineColumnClass(IMiniTable lineTable) {
		int i = 0;
		lineTable.setColumnClass(i++, Integer.class, true); // 0 seq
		lineTable.setColumnClass(i++, String.class, true); // 1-Description
		lineTable.setColumnClass(i++, Integer.class, false); // 2-Quantity
		lineTable.setColumnClass(i++, BigDecimal.class, false); // 3-Unit Price
		//lineTable.setColumnClass(i++, BigDecimal.class, true); // 4 - Incl Price
		//lineTable.setColumnClass(i++, BigDecimal.class, false); // 5-Discount
		lineTable.setColumnClass(i++, BigDecimal.class, true); // 6-Total
		// Table UI
		lineTable.autoSize();
	}

	private boolean setMPOS() {
		// Martin added so that the terminal assigned to a particular user is
		// selected

		int userID = Env.getAD_User_ID(m_ctx);
		String whereClause = " AD_Org_ID = ? and SalesRep_ID = ? ";
		m_pos = new Query(m_ctx, MPOS.Table_Name, whereClause, null)
				.setParameters(new Object[] { Env.getAD_Org_ID(m_ctx), userID })
				.firstOnly();

		// Check that there are no previous open recons
		/*
		 * SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy"); if
		 * (DB.getSQLValue(null,
		 * "Select count(*) from POS_Teller_Recon where Processed = 'N' " +
		 * " and C_Pos_ID = " + m_pos.getC_POS_ID()+
		 * " and trunc(recon_Date) < trunc(current_Date)") > 0) { //
		 * VUtil.setMsg
		 * ("There are previous OPEN Teller Recons .  Please Close those first"
		 * ,true); //TODO create AD message return false ; }
		 */

		if (m_pos == null) {
			// VUtil.setMsg("Can not find the POS Terminal for this user",true);//TODO
			// create AD Message
			whereClause = " AD_Org_ID = ?  ";
			m_pos = new Query(m_ctx, MPOS.Table_Name, whereClause, null)
					.setParameters(new Object[] { Env.getAD_Org_ID(m_ctx) })
					.firstOnly();
			if (m_pos == null) {
				return false;
			}
		}
		// get the default POS using to this Organization
		/*
		 * if (currentPos != null) { currentPos.setMPOS(m_pos);
		 * //org.compiere.util.Ini.setProperty(Env.POS_ID,m_pos.getC_POS_ID());
		 * // Martin Added so that payment will know the pos ID
		 * Env.setContext(Env.getCtx(), Env.POS_ID,
		 * String.valueOf(m_pos.getC_POS_ID())); }
		 */

		return true;
	}




	public void onCtrlKey(KeyEvent event) throws Exception {
		int keyCode = event.getKeyCode();
		if (keyCode == KeyEvent.F8) {
			createCreditNote();
			clearScreen();

		} else if (keyCode == KeyEvent.F6) {
			clearScreen();
		} else if (keyCode == KeyEvent.F7) {
			deleteLine();
			intQuantity.setFocus(true);
		} else if (keyCode == KeyEvent.F9) {

			/*
			 * WLocationDialog ld = new WLocationDialog(Msg.getMsg(Env.getCtx(),
			 * "Location"), null); ld.setVisible(true); AEnv.showWindow(ld);
			 * MLocation m_value = ld.getValue();
			 */
		}

	}







	private void clearBottomScreen() {
		txtTotalLines.setValue("0.00");
		totalLines = new BigDecimal("0.00");
		txtTotalTax.setValue("0.00");
		txtDiscountAmt.setValue("0.00");
		txtSubTotal.setValue("0.00");
		discountOverall.setValue(new Integer("0"));
	}

	private void clearScreenExcData() {
		topLayout.getChildren().clear();
		bottomLayout.getChildren().clear();
		mainLayout.getChildren().clear();
		if (mainLayout.getNorth() != null) {
			mainLayout.getNorth().detach();
		}
		this.removeChild(mainLayout);
		this.appendChild(mainLayout);
		topPanel.getChildren().clear();
	}


	private void clearScreen() {
		//clearScreenExcData();

		//txtDocNo.setText("");
		resultsTable.clear();
		data.clear();
		olddata.clear();
		prodIDs.clear();
		chargeIDs.clear();
		inOutLineIDs.clear();
		c_InvoiceIDs.clear();

		orderID = 0;

		//prodIDs.clear();
		//txtTotalLines.setValue("0.00");
		//totalLines = new BigDecimal("0.00");
		// txtQuantity.setFocus(true);
		//ctxtrebateOrder.setChecked(false);
		//txtHostessDiscount.setValue("0.00");


		index = 0;
		txtCustomerRefNo.setValue(null);
		txtComment.setValue(null);




	}


	private String createCreditNote() {
		if (currentBPartner == null) {
			//FDialog.error(form.getWindowNo(), "Please select Customer");
			cmbCustomer.setFocus(true);
			throw new WrongValueException(cmbCustomer, "Please select Supplier");
		}

		if (inOutID == 0) {
			cmbInvoiceNo.setFocus(true);
			throw new WrongValueException(cmbInvoiceNo, "No Receipt of Goods Found");
		}
		Object[] objs = model.toArray();
		VDocument doc = new VDocument(currentBPartner);
		doc.setC_Order_ID(orderID);
		doc.setM_Warehouse_ID(m_pos.getM_Warehouse_ID());
		doc.setPl(pl);
		int m_C_Currency_ID = Env.getContextAsInt(Env.getCtx(), "$C_Currency_ID"); // default
		doc.setC_Currency_ID(m_C_Currency_ID);

		MBPartnerLocation bpl = MBPartnerLocation.getForBPartner(m_ctx, currentBPartner.getC_BPartner_ID(),null)[0];

		doc.setC_BPartner_Location_ID(bpl.getC_BPartner_Location_ID());

		doc.setRefNo(txtCustomerRefNo.getValue());
		doc.setComment(txtComment.getValue());

		if (dateInvoiceDate.getValue() != null) {
			doc.setInvoiceDate(new Timestamp(dateInvoiceDate.getValue().getTime()));
		}


		doc.setSalesRep_ID(Env.getAD_User_ID(m_ctx));
		doc.setSOTrx(false);
		doc.setInOutID(inOutID);
		MRMA mRMA = doc.createRMALines(objs,prodIDs,chargeIDs,0,inOutLineIDs,c_InvoiceIDs,null);
		if (mRMA != null) {
			int AD_Process_ID = PROCESS_C_INVOICE_GENERATERMA_MANUAL_VENDOR;
			doc.createCreditNoteOrShipment(mRMA,AD_Process_ID);  // generate the invoice

			/*try {
			    Thread.sleep(1000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}
*/
			AD_Process_ID = PROCESS_M_INOUT_GENERATERMA_MANUAL_VENDOR;  // Martin changed to add movement date
			doc.createCreditNoteOrShipment(mRMA,AD_Process_ID);  // Generate the return shipment

			/*try {
			    Thread.sleep(1000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}*/

			String sql = "Select i.C_Invoice_ID from C_Invoice i where i.M_RMA_ID = ?";
			int invoice_ID = DB.getSQLValue(null, sql, mRMA.getM_RMA_ID());
			if (invoice_ID > 0) {
				MInvoice invoice = new MInvoice(m_ctx, invoice_ID, null);
				int m_AD_Process_ID = MProcess.getProcess_ID("POS_Print_InvoiceCr", null);

				MPInstance instance = new MPInstance(Env.getCtx(),
						m_AD_Process_ID, 0);
				if (!instance.save()) {
					return "";
				}
				// call process
				org.compiere.process.ProcessInfo pi = new org.compiere.process.ProcessInfo(
						"Credit Invoice", m_AD_Process_ID);
				pi.setAD_PInstance_ID(instance.getAD_PInstance_ID());
				ProcessInfoParameter piOrgId = new ProcessInfoParameter(
						"AD_Org_ID", new BigDecimal(invoice.getAD_Org_ID()), null,
						null, null);
				ProcessInfoParameter piClientId = new ProcessInfoParameter(
						"AD_Client_ID", new BigDecimal(invoice.getAD_Client_ID()),
						null, null, null);
				ProcessInfoParameter piInvoiceId = new ProcessInfoParameter(
						"RECORD_ID", invoice.getC_Invoice_ID(), null, null, null);
				ProcessInfoParameter[] piParameters = new ProcessInfoParameter[] {
						piClientId, piOrgId, piInvoiceId };
				pi.setParameter(piParameters);
				pi.setRecord_ID(invoice.getC_Invoice_ID());

				ReportStarterDazzle rs = new ReportStarterDazzle();
				rs.startProcess(Env.getCtx(), pi, null);

			}
		}


		//MInvoice mInvoice = doc.createInvoiceLines(objs,prodIDs,chargeIDs);



		//createPayments(doc,invoice);
		//FDialog.info(form.getWindowNo(), form,"Credit Note Created");
		intQuantity.setFocus(true);
		prodIDs.clear();
		chargeIDs.clear();
		inOutLineIDs.clear();
		c_InvoiceIDs.clear();
		txtCustomerRefNo.setValue(null);
		cmbCustomer.setValue(null);
		cmbCustomer.setFocus(true);

		cmbInvoiceNo.setReadonly(true);
		cmbInvoiceNo.setDisabled(true);
		cmbInvoiceNo.setValue(null);



		clearBottomScreen();
		clearScreen();
		//txtComment.setValue(null);





		return "";

	}




	@Override
	public void valueChange(ValueChangeEvent evt) {
		// TODO Auto-generated method stub
		System.out.println("aaa");

	}

	@Override
	public void tableChanged(WTableModelEvent event) {
		// TODO Auto-generated method stub
		System.out.println("aaa");
		//data.get(event.getIndex0());
		Vector<Object> line = (Vector) data.get(event.getIndex0());
		//Vector<Object> oldline = (Vector) olddata.get(event.getIndex0());
		//Integer qty = ((BigDecimal) line.get(2)).intValue();
		Integer qty = 0;
		if (line.get(2) instanceof Integer) {
			qty = ((Integer) line.get(2));
		} else {
			qty = ((BigDecimal) line.get(2)).intValue();
		}
		Integer oldqty = (Integer)olddata.get(event.getIndex0());
		if (qty == null || oldqty == null) {
			return;
		}
		if (qty > oldqty) {
			line.set(2, new BigDecimal(oldqty));
			qty = oldqty;
		}
		BigDecimal unitPrice = ((BigDecimal) line.get(3));
		BigDecimal rsp = unitPrice.multiply(new BigDecimal(qty));
		rsp = rsp.setScale(2, BigDecimal.ROUND_HALF_UP);
		line.set(4, rsp);
		int idx = data.indexOf(line);
		data.set(idx, line);
		//olddata = (Vector<Object>)data.clone();
		calculateTotsBottomPanel();

	}



	@Override
	public void onEvent(Event event) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("aaa");

	}

	@Override
	public ADForm getForm() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void tableValueChange(TableValueChangeEvent event) {
		// TODO Auto-generated method stub

	}






}
