package org.adempiere.webui.apps.form;
//package org.adempiere.webui.apps.form;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;

import org.adempiere.webui.apps.AEnv;
import org.adempiere.webui.component.Borderlayout;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.Checkbox;
import org.adempiere.webui.component.ComboboxNew;
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
import org.compiere.model.MInOut;
import org.compiere.model.MInvoice;
import org.compiere.model.MInvoiceLine;
import org.compiere.model.MOrder;
import org.compiere.model.MOrg;
import org.compiere.model.MOrgInfo;
import org.compiere.model.MPInstance;
import org.compiere.model.MPOS;
import org.compiere.model.MPayment;
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
import org.zkoss.zul.West;

import za.co.ntier.dazzle.common.DazzleConfigManager;

/**
 * @author 	Martin Augustine
 * 				Original author
 * @author 	Neil Gordon
 * 				Various modifications
 * 
 * Important Note: Before starting any development, or addressing any bug fix, please be sure to read the documents under:
 *  		etc/doc/systemsdocumentation, especially the document: Development Notes. Consider also: dazzleDevelopment/etc/doc/tickets
 */
public class WDazzleCreditNote extends WDazzleBase
implements IFormController, EventListener<Event>, WTableModelListener, ValueChangeListener,TableValueChangeListener
{
	/**
	 *
	 */
	public final static int PROCESS_M_INOUT_GENERATERMA_CUST = 1000019;
	public final static int PROCESS_C_INVOICE_GENERATE_RMA_DAZZLE = 1000026;
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

	private Label lblCustomer = new Label("Customer");
	private ComboboxNew cmbCustomer = new ComboboxNew();

	private static CLogger log = CLogger.getCLogger(WDazzleCreditNote.class);

	private Intbox intQuantity = new Intbox();
	private Label lblQuantity = new Label("Quantity");

	private NumberBox discountOverall = new NumberBox(true);
	private Label lbldiscountOverall = new Label("Overall Discount %");

	private ComboboxNew cmbProduct = new ComboboxNew();
	private Label lblProduct = new Label("Product");


	private Label lblCustomerRefNo = new Label("Reference No");
	private Textbox txtCustomerRefNo = new Textbox();
	private Label lblComment = new Label("Comment");
	private Textbox txtComment = new Textbox("Initial Comment");
	private Label lblInvoiceNo = new Label("Invoice No");
	private ComboboxNew cmbInvoiceNo = new ComboboxNew();



	private MPOS m_pos = null;

//	private boolean newProduct = false;

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
	private int charge_ID_sel = 0;
	private ComboboxNew cmbCharge = new ComboboxNew();
	private Label lblCharge = new Label("Charge");

	private Textbox txtAmount = new Textbox();
	private Label lblAmount= new Label("Amount");

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
	private Button btnCreate = new Button("Complete");
	private Button btnCancel = new Button("Cancel");
	private Button btnBack = new Button("Back");


	private Label lblEFT = new Label("EFT");
	private Textbox txtEFT = new Textbox();
	private Label lblChequeNo = new Label("Cheque Number");
	private Textbox txtChequeNo = new Textbox();

	private Button F6 = new Button("F6 - Clear");
	private Button F7 = new Button("F7 - Delete Line");
	private Button F8 = new Button("F8 - Checkout");
	private Button F9 = new Button("F9 - Payment");
	
	protected Label lblHelp; 			//= new Label("F6 - Clear Screen, F7 - Delete line, F8 - Checkout, F9 - Payment");
	protected boolean m_isDazzleQECreditNoteEasyRefundsEnabled;
	private boolean back = false;
//	private Label lblOverRideCrExceeded = new Label("OverRide Credit Limit Exceeded");
//	private Checkbox chOverRideCrExceeded = new Checkbox();
//	private Checkbox chAllowCrOrder = new Checkbox();
//	private Label lblAllowCrOrder = new Label("Allow Credit Order");
	
	private int linesAdded = 0;


	protected void initForm() {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		try {
			if (!setMPOS()) {
				FDialog.error(form.getWindowNo(), "No Pos Terminal for this Branch");
				return;
			}
			
			//NCG 2014/10/02: #1000338: Cater for easy refunds on Dazzle Credit Note
			m_isDazzleQECreditNoteEasyRefundsEnabled = DazzleConfigManager.isDazzleQECreditNoteEasyRefundsEnabled(m_ctx, null);
			
			String msg = "F6 - Clear Screen, F7 - Delete line, F8 - Checkout";
			if (m_isDazzleQECreditNoteEasyRefundsEnabled) {
				msg += ", F9 - Payment";
			}
			lblHelp = new Label(msg);
			
			//NCG 2014/09/08 Resolve problems with F6
			//zkInit();
			restartWindow();
		} catch (Exception e) {
			log.log(Level.SEVERE, "", e);
		}

	}


	private void zkInit () {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		super.setHeight("100%");
		this.setCtrlKeys("#f8#f6#f7#f9");
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
		//FIXME NCG 2014/10/02: #1000338: Cater for easy refunds on Dazzle Credit Note
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



		String sql2 = "select bp.value || '-' || bp.name,bp.C_Bpartner_ID from C_Bpartner bp,c_bp_group bpg where  "
				+ " (upper(bp.value)) like ?"
				+ " and bp.c_bp_group_id = bpg.c_bp_group_id and bp.iscustomer = 'Y'"
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
								throw new WrongValueException(cmbCustomer, "Customer requires an address");
							}
							cmbInvoiceNo.setReadonly(false);
							cmbInvoiceNo.setDisabled(false);
							setComboInvoiceNo();
//							cmbCharge.setReadonly(false);
//							cmbCharge.setDisabled(false);
						}

						cmbInvoiceNo.setFocus(true);
					} else {
						cmbCustomer.setValue(null);
						cmbCustomer.setFocus(true);
						currentBPartner = null;
						cmbInvoiceNo.setReadonly(true);
						cmbInvoiceNo.setDisabled(true);
//						cmbCharge.setReadonly(true);
//						cmbCharge.setDisabled(true);
						cmbInvoiceNo.setValue(null);
						throw new WrongValueException(cmbCustomer, "Please select a Customer");
					}

				}
			});
		}

		

		if (!cmbInvoiceNo.isListenerAvailable(Events.ON_CHANGE, false)) {
			cmbInvoiceNo.addEventListener(Events.ON_CHANGE,new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(org.zkoss.zk.ui.event.Event event)
						throws Exception {
					clearBottomScreen();
					if (cmbInvoiceNo.getValue() != null && cmbInvoiceNo.getSelectedItem() != null) {
						int selIndex = cmbInvoiceNo.getSelectedIndex();
						if (selIndex > -1) {
							int invoiceID = ((WObjRet)cmbInvoiceNo.getSelectedItem().getValue()).getIDOfRecord();
							populateScreen(invoiceID);
							cmbCharge.setReadonly(false);
							cmbCharge.setDisabled(false);
						}
						cmbInvoiceNo.setFocus(true);
					} else {
						cmbCharge.setReadonly(true);
						cmbCharge.setDisabled(true);
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
		cmbInvoiceNo.setValue(null);
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
		txtComment.setValue("After ");
//NCG 2014/07/28
//		String sql = "select p.name || ' | ' ||  (case when p.description is null then '' else p.description end) as " +
		String sql = "select p.name ||  (case when p.description is null then '' else ' | ' || p.description end) as " +
//		String sql = "select p.name as " +
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
		 cmbCharge.setReadonly(true);
		 cmbCharge.setDisabled(true);
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
//					 newProduct = true;
					 if (cmbCharge.getSelectedItem() != null)
						 txtAmount.setFocus(true);
					 //End

				 }
			 });
		 }

		 
		 if (!txtAmount.isListenerAvailable(Events.ON_OK, false)) {
				txtAmount.addEventListener(Events.ON_OK,
						new org.zkoss.zk.ui.event.EventListener(){
					public void onEvent(org.zkoss.zk.ui.event.Event event) throws Exception{
						EnterPressedOnAmount();
//						if (newProduct) {
//							EnterPressedOnAmount ();
//							newProduct = false;
//
//						} else {
////							if (charge_ID_sel == 0 || cmbCharge.getValue() == null) {
////								cmbCharge.setFocus(true);
////								throw new WrongValueException(cmbCharge, "Please select a charge");
////							}
//						}
					}
				});
			}

		 txtAmount.addEventListener(Events.ON_FOCUS,
					new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(org.zkoss.zk.ui.event.Event event)
						throws Exception {
					//Highlight all digits in the textbox when field gets focus
					int len = (txtAmount.getValue()).length();
					txtAmount.setSelectionRange(0, len);
				}

			});
		 
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
		
		
		txtAmount.setValue("0.00");
		txtAmount.setStyle("text-align:right");
		txtAmount.setConstraint("/^[-+]?\\d+(\\.\\d{1,2})?$/: Please enter a valid amount");
//		txtAmount.setConstraint("/^\\d+(\\.\\d{1,2})?$/: Please enter a valid amount");
		txtAmount.setReadonly(false);
		
		
		
		row.appendChild(txtAmount);
		
		







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
		//Span: help was a bit longer on this screen because of F9
		row2.setSpans("2,1,1");
		row2.appendChild(lblHelp);					//NCG #1000328: Display help on Dazzle screens
		//row2.appendChild(new Space());
		//row2.appendChild(new Space());
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
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		cmbInvoiceNo.setValue(null);
		String sql2 = "select I.Documentno || '_ ' || left(cast(i.dateinvoiced as varchar(10)),10) || '_ R' || I.grandTotal,I.C_invoice_ID " +
				" from C_Invoice I where  "
				+ " I.C_Bpartner_ID = " + currentBPartner.getC_BPartner_ID()
				+ " and I.documentno like ?"
				+ " and I.docstatus in ('CO','CL')"
				+ " and I.isactive='Y' "
				+ " and I.C_DocType_ID in "
				+ " (select dt.c_docType_ID from c_docType dt where dt.docbasetype = 'ARI' and dt.ad_client_ID = i.ad_client_ID)"
				+ " and I.ad_client_Id = "	+ Env.getAD_Client_ID(m_ctx)
				//+ " and not exists (select 'x' from c_invoice cn where cn.c_docType_ID = "
				//+ " (select dt.c_docType_ID from c_docType dt where dt.docbasetype = 'ARC' and dt.ad_client_ID = i.ad_client_ID)"
				//+ " and cn.C_Order_ID is not null"
				//+ " and cn.c_order_ID = I.C_Order_ID)"
				+ " ORDER BY I.dateinvoiced desc, I.documentno desc";
		SimpleListModelNew dictModel2 = new SimpleListModelNew();

		dictModel2.setCbox(cmbInvoiceNo);
		cmbInvoiceNo.setModel(dictModel2);
		cmbInvoiceNo.setQueryName(sql2);
		cmbInvoiceNo.setAutodrop(true);
		cmbInvoiceNo.setButtonVisible(true);
		cmbInvoiceNo.setWidth("400px");
	}

	private void populateScreen(int c_Invoice_ID) {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		MInvoice mInvoice = new MInvoice(m_ctx, c_Invoice_ID, null);
		orderID = mInvoice.getC_Order_ID();
		if (orderID == 0) {
			throw new WrongValueException(cmbInvoiceNo,"There is no order for this invoice");
		} else {
			MOrder origOrder = new MOrder(Env.getCtx(),orderID,null);
			MInOut [] mInOut = origOrder.getShipments();
			if (mInOut == null || mInOut.length <= 0) {
				//FDialog.error(form.getWindowNo(),
				//		"Order was never shipped, Cannot return goods");
				//return;
				throw new WrongValueException(cmbInvoiceNo, "Order was never shipped, Cannot return goods");
			}
		}
		//NCG: #1000185: Dazzle Credit Note - The ref. number should be pre-populated with the invoice number.
		txtCustomerRefNo.setValue(mInvoice.getDocumentNo());
//		if (mInvoice.getPOReference() != null && !mInvoice.getPOReference().equals(null)) {
//			txtCustomerRefNo.setValue(mInvoice.getPOReference());
//		}
		if (mInvoice.getDescription() != null && !mInvoice.getDescription().equals(null)) {
			String desc = mInvoice.getDescription();
			txtComment.setValue(desc);
		}


        headerload();
        linesAdded = 0;
		int ids [] = PO.getAllIDs("C_InvoiceLine", "C_Invoice_ID = " + mInvoice.getC_Invoice_ID() + " order by line" , null);
		for (int i = 0; i < ids.length; i++) {
			MInvoiceLine mInvoiceLine = new MInvoiceLine(m_ctx,ids[i],null);
// 2014/07/28 NCG commmented the following, so that charges would be included
//			if (mInvoiceLine.getC_Charge_ID() > 0) {
//				continue;
//			}
			int m_InOut_ID = mInvoiceLine.getM_InOutLine_ID();
			String sql = "Select sum(r.qty) from M_RMALine r,M_RMA rh where r.m_inOutLine_ID = ?" +
					" and r.m_rma_id = rh.m_rma_id and rh.docstatus <> 'VO'";
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

	private void deleteLine() {

		if (log.isLoggable(Level.CONFIG))
			log.config("");
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
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		BigDecimal calcTot = calculateTotal(model.toArray(), 4, false, prodIDs,chargeIDs);
		txtTotalLines.setValue(calcTot.toString());

	}


	private BigDecimal calculateTotal(Object[] lmt, int col,
			boolean otherItems, ArrayList<Integer> PprodIDs,ArrayList<Integer> PchargeIDs) {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
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
		if (currentBPartner.isTaxExempt()) {
			/*int tax_ID = TaxMA.getExemptTax(m_ctx, Env.getAD_Org_ID(m_ctx));
			MTax mTax = new MTax(m_ctx, tax_ID, null);
			BigDecimal tax = mTax.calculateTax(Price,pl.isTaxIncluded(), 8);
			return tax;*/
			return new BigDecimal("0.00");
		} else {
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
			BigDecimal tax = mTax.calculateTax(Price,pl.isTaxIncluded(), 2);
			return tax;
		}

	}



	public void headerload()
    {
			if (log.isLoggable(Level.CONFIG))
			log.config("");
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

          //NCG 2014/09/08 Resolve problems with F6
//            Vector<Object> line = new Vector<Object>();
//            data.clear();
//            olddata.clear();
//	    	data.add(line);
//	    	olddata.add(null);

            model = new ListModelTable(data);
            model.addTableModelListener(this);
            resultsTable.setData(model, columnNames, columnWidths);
            setLineColumnClass(resultsTable);


    }
	
	public void setLineColumnClass(IMiniTable lineTable) {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		int i = 0;
		lineTable.setColumnClass(i++, Integer.class, true); // 0 seq
		lineTable.setColumnClass(i++, String.class, true); // 1-Description
		lineTable.setColumnClass(i++, Integer.class, false); // 2-Quantity
		lineTable.setColumnClass(i++, BigDecimal.class, true); // 3-Unit Price
		//lineTable.setColumnClass(i++, BigDecimal.class, true); // 4 - Incl Price
		//lineTable.setColumnClass(i++, BigDecimal.class, false); // 5-Discount
		lineTable.setColumnClass(i++, BigDecimal.class, true); // 6-Total
		// Table UI
		lineTable.autoSize();
	}

	private boolean setMPOS() {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
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
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		int keyCode = event.getKeyCode();
		if (keyCode == KeyEvent.F8) {
			//Create without tender, and don't create a payment
			String docNo = createCreditNote(false);
			restartWindow();
			//createCreditNote();
		} else if (keyCode == KeyEvent.F6) {
			restartWindow();
		} else if (keyCode == KeyEvent.F7) {
			deleteLine();
			intQuantity.setFocus(true);
		} else if (keyCode == KeyEvent.F9 && m_isDazzleQECreditNoteEasyRefundsEnabled ) {
			//Create with tender
			settlementScr();
			/*
			 * WLocationDialog ld = new WLocationDialog(Msg.getMsg(Env.getCtx(),
			 * "Location"), null); ld.setVisible(true); AEnv.showWindow(ld);
			 * MLocation m_value = ld.getValue();
			 */
		}

	}

	/**
	 * NCG 2014/10/02: #1000338: Cater for easy refunds on Dazzle Credit Note
	 * From WDazzlePOS
	 */
	private void settlementScr() {

		if (log.isLoggable(Level.CONFIG))
			log.config("");
		
		
		if (currentBPartner == null || cmbCustomer.getValue() == null || "".equals(cmbCustomer.getValue()) ) {
			throw new WrongValueException(cmbCustomer, "Please select a Customer");
		}
		
		if (data.size()<=1) {
			throw new WrongValueException(cmbCustomer, "Please add one or more lines to the document first.");
		}
		clearScreenExcData();
		
		topLayout.getChildren().clear();
		// bottomLayout.getChildren().clear();
		Grid topLayout2 = GridFactory.newGridLayout();
		mainLayout.getChildren().clear();
		this.removeChild(mainLayout);
		this.appendChild(mainLayout);
		topPanel.getChildren().clear();
		topPanel.appendChild(topLayout2);
		West west = new West();
		west.setSize("75%");
		west.setBorder("none");
		mainLayout.appendChild(west);
		west.appendChild(topPanel);


		Rows rows3 = null;
		Row row3 = null;

		topLayout2.setWidth("100%");
		rows3 = topLayout2.newRows();
		row3 = rows3.newRow();

		row3 = rows3.newRow();



		BigDecimal amt = new BigDecimal(txtTotalLines.getValue());
		BigDecimal totalDue = amt;
		txttotalDue.setValue(totalDue.toPlainString());;




		boolean paid = false;



		txtCC.setValue("0.00");
		txtCC.setStyle("text-align:right");
		txtCC
		.setConstraint("/^\\d+(\\.\\d{1,2})?$/: Please enter a valid amount");
		txtCC.setReadonly(false);
		txtCash.setValue("0.00");
		txtCash.setStyle("text-align:right");
		txtCash.setConstraint("/^\\d+(\\.\\d{1,2})?$/: Please enter a valid amount");
		txtCash.setReadonly(false);
		txtEFT.setValue("0.00");
		txtEFT.setStyle("text-align:right");
		txtEFT.setConstraint("/^\\d+(\\.\\d{1,2})?$/: Please enter a valid amount");
		txtEFT.setReadonly(false);
		txtCheques.setValue("0.00");
		txtCheques.setStyle("text-align:right");
		txtCheques.setConstraint("/^\\d+(\\.\\d{1,2})?$/: Please enter a valid amount");
		txtCheques.setReadonly(false);
		txtBalDue.setValue("0.00");
		txtBalDue.setStyle("text-align:right");
		txtBalDue.setReadonly(true);
		txtChange.setValue("0.00");
		txtChange.setStyle("text-align:right");
		txtChange.setReadonly(true);


		//if (!txtMiglioCC.isListenerAvailable(Events.ON_FOCUS, false)) {
			txtCC.addEventListener(Events.ON_FOCUS,
					new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(org.zkoss.zk.ui.event.Event event)
						throws Exception {
					int len = (txtCC.getValue()).length();
					txtCC.setSelectionRange(0, len);
				}

			});
		//}
	// if (!txtDirectDep.isListenerAvailable(Events.ON_FOCUS, false)) {

		txtCash.addEventListener(Events.ON_FOCUS,
				new org.zkoss.zk.ui.event.EventListener() {
			public void onEvent(org.zkoss.zk.ui.event.Event event)
					throws Exception {
				int len = (txtCash.getValue()).length();
				txtCash.setSelectionRange(0, len);
			}

		});

		txtEFT.addEventListener(Events.ON_FOCUS,
				new org.zkoss.zk.ui.event.EventListener() {
			public void onEvent(org.zkoss.zk.ui.event.Event event)
					throws Exception {
				int len = (txtEFT.getValue()).length();
				txtEFT.setSelectionRange(0, len);
			}

		});
	// }
	 //if (!txtEFT.isListenerAvailable(Events.ON_FOCUS, false)) {
		txtCheques.addEventListener(Events.ON_FOCUS,
				new org.zkoss.zk.ui.event.EventListener() {
			public void onEvent(org.zkoss.zk.ui.event.Event event)
					throws Exception {
				int len = (txtCheques.getValue()).length();
				txtCheques.setSelectionRange(0, len);
			}

		});
	 //}

		if (!txtCash.isListenerAvailable(Events.ON_CHANGE, false)) {
			txtCash.addEventListener(Events.ON_CHANGE,
					new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(org.zkoss.zk.ui.event.Event event)
						throws Exception {
					BigDecimal balDue = calcBalDue();
					BigDecimal change = calcChange(balDue);
					txtChange.setValue(change.toPlainString());
					if (change != null) {
						balDue = balDue.add(change);
					}
					txtBalDue.setValue(balDue.toPlainString());
					txtCheques.setFocus(true);
					//if (balDue.compareTo(Env.ZERO) <= 0) {
						//btnCreate.setDisabled(false);
						//btnCreate.setFocus(true);
					//} else {
						//btnCreate.setDisabled(true);
					//}
				}

			});
		}

		if (!txtCheques.isListenerAvailable(Events.ON_CHANGE, false)) {
			txtCheques.addEventListener(Events.ON_CHANGE,
					new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(org.zkoss.zk.ui.event.Event event)
						throws Exception {
					BigDecimal balDue = calcBalDue();


					BigDecimal change = calcChange(balDue);
					txtChange.setValue(change.toPlainString());
					if (change != null) {
						balDue = balDue.add(change);
					}
					txtBalDue.setValue(balDue.toPlainString());
					txtCC.setFocus(true);
					//if (balDue.compareTo(Env.ZERO) <= 0) {
						//btnCreate.setDisabled(false);
						//btnCreate.setFocus(true);

					//} else {
						//btnCreate.setDisabled(true);
					//}
				}

			});
		}

		if (!txtCC.isListenerAvailable(Events.ON_CHANGE, false)) {
			txtCC.addEventListener(Events.ON_CHANGE,
					new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(org.zkoss.zk.ui.event.Event event)
						throws Exception {
					BigDecimal balDue = calcBalDue();
					BigDecimal change = calcChange(balDue);
					txtChange.setValue(change.toPlainString());
					if (change != null) {
						balDue = balDue.add(change);
					}
					txtBalDue.setValue(balDue.toPlainString());

					//if (balDue.compareTo(Env.ZERO) >= 0) {
						//btnCreate.setDisabled(false);
						//btnCreate.setFocus(true);
					//} else {
						txtEFT.setFocus(true);
						//btnCreate.setDisabled(true);
					//}
				}

			});
		}

		if (!txtEFT.isListenerAvailable(Events.ON_CHANGE, false)) {
			txtEFT.addEventListener(Events.ON_CHANGE,
					new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(org.zkoss.zk.ui.event.Event event)
						throws Exception {
					BigDecimal balDue = calcBalDue();
					BigDecimal change = calcChange(balDue);
					txtChange.setValue(change.toPlainString());
					if (change != null) {
						balDue = balDue.add(change);
					}
					txtBalDue.setValue(balDue.toPlainString());
					//if (balDue.compareTo(Env.ZERO) >= 0) {
						//btnCreate.setDisabled(false);
						//btnCreate.setFocus(true);
					//} else {
						btnCreate.setFocus(true);
						//btnCreate.setDisabled(true);
					//}
				}

			});
		}







		row3 = rows3.newRow();
		row3.appendChild(lblCash);
		row3.appendChild(txtCash);
		Clients.response(new AuFocus(txtCash));


		row3 = rows3.newRow();
		row3.appendChild(lblCheques);
		row3.appendChild(txtCheques);


		row3.appendChild(lblChequeNo);
		row3.appendChild(txtChequeNo);

		row3 = rows3.newRow();
		row3.appendChild(lblCC);
		row3.appendChild(txtCC);

		row3 = rows3.newRow();
		row3.appendChild(lblEFT);
		row3.appendChild(txtEFT);


		row3 = rows3.newRow();
		// End Modified

		BigDecimal balDue = calcBalDue();
		txtBalDue.setValue(balDue.toPlainString());
		row3.appendChild(lblBalDue);
		row3.appendChild(txtBalDue);

		row3 = rows3.newRow();
		txtChange.setValue("0.00");
		row3.appendChild(lblChange);
		row3.appendChild(txtChange);

//		row3 = rows3.newRow();
//		chOverRideCrExceeded.setChecked(false);
//		row3.appendChild(lblOverRideCrExceeded);
//		row3.appendChild(chOverRideCrExceeded);

//		row3 = rows3.newRow();
//		chAllowCrOrder.setChecked(false);
//		row3.appendChild(lblAllowCrOrder );
//		row3.appendChild(chAllowCrOrder);



		row3 = rows3.newRow();
		btnCreate.setDisabled(false);
		btnCreate.setAutodisable("self");
		row3.appendChild(btnCreate);
		btnBack.setDisabled(false);
		row3.appendChild(btnCancel);
		row3.appendChild(btnBack);

		if (paid) {
			btnCreate.setFocus(true);
		}


		if (!btnBack.isListenerAvailable(Events.ON_CLICK, false)) {
			btnBack.addEventListener(Events.ON_CLICK,
					new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(org.zkoss.zk.ui.event.Event event)
						throws Exception {
					back = true;
					clearScreenExcData();
					zkInit();
				}
			});
		}


		if (!btnCancel.isListenerAvailable(Events.ON_CLICK, false)) {
			btnCancel.addEventListener(Events.ON_CLICK,
					new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(org.zkoss.zk.ui.event.Event event)
						throws Exception {
					restartWindow();
				}
			});
		}

		// Button on settlement screen - Complete the transaction
		if (!btnCreate.isListenerAvailable(Events.ON_CLICK, false)) {
			btnCreate.addEventListener(Events.ON_CLICK,
					new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(org.zkoss.zk.ui.event.Event event)
						throws Exception {
					if (MBPartner.SOCREDITSTATUS_CreditHold.equals(currentBPartner.getSOCreditStatus())
							|| MBPartner.SOCREDITSTATUS_CreditStop.equals(currentBPartner.getSOCreditStatus())) {
						FDialog.error(form.getWindowNo(), "Credit on hold or Stopped. Cannot process pos orders ");
						return;
					}
					//throw new WrongValueException(cmbCustomer, "Please select Customer");
					BigDecimal balDue = new BigDecimal(txtBalDue.getValue());
					if (balDue.compareTo(new BigDecimal("0.00")) > 0) {
						// refresh bp just incase the credit limit was changed
						currentBPartner = new MBPartner(Env.getCtx(), currentBPartner.getC_BPartner_ID(), null);
						BigDecimal creditUsed = currentBPartner.getSO_CreditUsed().add(balDue);
						if (MBPartner.SOCREDITSTATUS_CreditHold.equals(currentBPartner.getSOCreditStatus())
								|| MBPartner.SOCREDITSTATUS_CreditStop.equals(currentBPartner.getSOCreditStatus())) {
							FDialog.error(form.getWindowNo(), "Credit on hold or Stopped.  Current Credit Used: " +
								currentBPartner.getSO_CreditUsed().toPlainString() );
							return;
						}
//						if (chOverRideCrExceeded.isChecked() && creditUsed.compareTo(currentBPartner.getSO_CreditLimit()) > 0
//								&& !MBPartner.SOCREDITSTATUS_NoCreditCheck .equals(currentBPartner.getSOCreditStatus())) {
//							FDialog.error(form.getWindowNo(), "Cannot override credit exceeded." +
//									" Change to NO Credit Check on Customer Record");
//							return;
//						}
						/*if (chOverRideCrExceeded.isChecked() && creditUsed.compareTo(currentBPartner.getSO_CreditLimit()) > 0
								&& MBPartner.SOCREDITSTATUS_CreditOK .equals(currentBPartner.getSOCreditStatus())) {
							FDialog.error(form.getWindowNo(), "Cannot override credit exceeded if Customer has credit status OK." +
									" Change to no Credit Check");
							return;
						}*/

//						if (!chOverRideCrExceeded.isChecked() && creditUsed.compareTo(currentBPartner.getSO_CreditLimit()) > 0) {
//							BigDecimal overBy = creditUsed.subtract(currentBPartner.getSO_CreditLimit());
//							if (overBy != null) {
//								overBy = overBy.setScale(2, BigDecimal.ROUND_HALF_UP);
//							}
//							FDialog.error(form.getWindowNo(), "The credit limit for this customer has been exceeded By " + overBy.toPlainString() +
//						               "  Tick Override Credit Exceeded Box to allow");
//							return;
//						} else if (!chAllowCrOrder.isChecked()) {
//							FDialog.error(form.getWindowNo(),"This will generate a credit order.  Tick Allow Credit Order to create Credit order");
//							return;
//
//
//						}
					}

					
					BigDecimal changeAmt = new BigDecimal(txtChange.getValue());
					if ( changeAmt.compareTo(BigDecimal.ZERO) != 0 ) {
						throw new WrongValueException(txtChange, "Amount tendered should be less than the balance due");
					}
					String docNo = createCreditNote(true);
					restartWindow();

				}

			});
		}








	}

	private BigDecimal calcBalDue() {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		BigDecimal balDue = new BigDecimal("0.00");
		balDue = new BigDecimal(txttotalDue.getValue());
		balDue = balDue.subtract(new BigDecimal(txtCC.getValue()));
		balDue = balDue.subtract(new BigDecimal(txtCash.getValue()));
		balDue = balDue.subtract(new BigDecimal(txtCheques.getValue()));
		balDue = balDue.subtract(new BigDecimal(txtEFT.getValue()));
		return balDue;
	}

	private BigDecimal calcChange(BigDecimal balDue) {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		BigDecimal change = new BigDecimal("0.00");
		if (balDue.compareTo(new BigDecimal("0.00")) < 0) {
			if (txtCash != null || txtCheques != null) {
				BigDecimal amt = (new BigDecimal("0.00"));
				if (txtCash != null) {
					amt = new BigDecimal(txtCash.getValue());
				}
				/*if (txtCheques != null ) {
					amt = amt.add(new BigDecimal(txtCheques.getValue()));
				}*/
				BigDecimal balDueABS = balDue.abs();
				if (balDueABS.compareTo(amt) <= 0) {
					change = balDueABS;
				} else {
					change = amt;
				}
			}
		}
		return change;

	}
	

	/**
	 * @author NCG
	 * Reset the window to starting state
	 * NCG 2014/09/08 Resolve problems with F6
	 */
	private void restartWindow() {
		
		currentBPartner = null;
		cmbCustomer.setValue(null);
		txtCustomerRefNo.setValue("");
		txtComment.setValue("");
		cmbCharge.setValue(null);

		cmbInvoiceNo.setReadonly(true);
		cmbInvoiceNo.setDisabled(true);
		cmbInvoiceNo.setValue(null);
		
		clearBottomScreen();
		
		//So that elements can be re-added to the layout
		clearScreenExcData();
		
		zkInit();
		
		cmbCustomer.focus();
	}
	

	/**
	 * Call this method, to clear everything on the bottom two sections of the screen
	 *  (Lines and totals)
	 *  NCG 2014/09/08 Resolve problems with F6
	 */
	private void clearBottomScreen() {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		resultsTable.clear();
		
		//The first line in the detail is always blank
		data.clear();
		olddata.clear();
		Vector<Object> line = new Vector<Object>();
	    data.add(line);					
	    olddata.add(new Integer("0"));
		
	    prodIDs.clear();
		chargeIDs.clear();
		inOutLineIDs.clear();
		c_InvoiceIDs.clear();
		
		orderID = 0;
		index = 0;
		
		headerload();

		txtTotalLines.setValue("0.00");
		totalLines = new BigDecimal("0.00");
		txtTotalTax.setValue("0.00");
		txtDiscountAmt.setValue("0.00");
		txtSubTotal.setValue("0.00");
		discountOverall.setValue(new Integer("0"));
	}

	private void clearScreenExcData() {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
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



	private String createCreditNote(boolean isCreatePayment) {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		if (currentBPartner == null) {
			//FDialog.error(form.getWindowNo(), "Please select Customer");
			cmbCustomer.setFocus(true);
			throw new WrongValueException(cmbCustomer, "Please select Customer");
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


		doc.setSalesRep_ID(Env.getAD_User_ID(m_ctx));

		MRMA mRMA = doc.createRMALines(objs,prodIDs,chargeIDs,0,inOutLineIDs,c_InvoiceIDs,null);
		if (mRMA != null) {
//			int AD_Process_ID = SystemIDs.PROCESS_C_INVOICE_GENERATERMA_MANUAL;
			//Modified NCG Ticket #1000182: No discount distributions on credit note
			int AD_Process_ID = PROCESS_C_INVOICE_GENERATE_RMA_DAZZLE;
			doc.createCreditNoteOrShipment(mRMA,AD_Process_ID);  // generate the invoice

			/*try {
			    Thread.sleep(1000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}*/

			AD_Process_ID = PROCESS_M_INOUT_GENERATERMA_CUST;  // New process copy of vendor , change to customer
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
				
				if ( isCreatePayment ) {
					createPayments(doc,invoice);
				}
				
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

		return "";

	}


	/**
	 * NCG 2014/10/02: #1000338: Cater for easy refunds on Dazzle Credit Note
	 */
	private void createPayments(VDocument doc,MInvoice invoice) {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		//-1 for payment reversal
		BigDecimal multiplicand = new BigDecimal("-1.00");
		if (txtCash != null && txtCash.getValue() != null) {
			BigDecimal amt = new BigDecimal(txtCash.getValue());
			if (txtChange.getValue() != null) {
				BigDecimal change = new BigDecimal(txtChange.getValue());
				if (change != null) {
					amt = amt.subtract(change);
				}
			}
			if (amt != null && amt.compareTo(Env.ZERO) != 0) {
				doc.createPayment(0, amt.multiply(multiplicand), MPayment.TENDERTYPE_Cash,
						m_pos.getC_BankAccount_ID(), "", "",invoice,new BigDecimal("0.00"));
			}
		}
		// 24/03/2014  we need to not allocate excess amts
		BigDecimal excessAmt = new BigDecimal("0.00");
		if (txtBalDue != null && (new BigDecimal(txtBalDue.getValue())).compareTo(Env.ZERO) <= 0) {  // only negative values are excess
			excessAmt = new BigDecimal(txtBalDue.getValue());
		}
		if (txtCC != null && txtCC.getValue() != null) {
			BigDecimal amt = new BigDecimal(txtCC.getValue());

			if (amt != null && amt.compareTo(Env.ZERO) != 0) {
				BigDecimal specificExcessAmt = new BigDecimal("0.00");
				if (excessAmt.compareTo(Env.ZERO) < 0) {
					specificExcessAmt = amt.add(excessAmt);
					if (specificExcessAmt.compareTo(Env.ZERO) < 0) {
						 excessAmt = specificExcessAmt;
						 specificExcessAmt = amt;
					} else {
						specificExcessAmt = excessAmt.negate();  // it must be positive , negated below
						excessAmt = new BigDecimal("0.00");
					}
				}
				doc.createPayment(0, amt.multiply(multiplicand), MPayment.TENDERTYPE_CreditCard, m_pos.getC_BankAccount_ID(), doc.getmOrder()
						.getDocumentNo(), "C",invoice,specificExcessAmt.negate());
			}
		}
		if (txtCheques != null && txtCheques.getValue() != null) {
			BigDecimal amt = new BigDecimal(txtCheques.getValue());
			if (amt != null && amt.compareTo(Env.ZERO) != 0) {
				BigDecimal specificExcessAmt = new BigDecimal("0.00");
				if (excessAmt.compareTo(Env.ZERO) < 0) {
					specificExcessAmt = amt.add(excessAmt);
					if (specificExcessAmt.compareTo(Env.ZERO) < 0) {
						 excessAmt = specificExcessAmt;
						 specificExcessAmt = amt;
					} else {
						specificExcessAmt = excessAmt.negate();  // it must be positive , negated below
						excessAmt = new BigDecimal("0.00");
					}
				}
				doc.createPayment(0, amt.multiply(multiplicand), MPayment.TENDERTYPE_Check,
						m_pos.getC_BankAccount_ID(), "", "",invoice,specificExcessAmt.negate());
			}
		}

		if (txtEFT != null && txtEFT.getValue() != null) {
			BigDecimal amt = new BigDecimal(txtEFT.getValue());
			if (amt != null && amt.compareTo(Env.ZERO) != 0) {
				BigDecimal specificExcessAmt = new BigDecimal("0.00");
				if (excessAmt.compareTo(Env.ZERO) < 0) {
					specificExcessAmt = amt.add(excessAmt);
					if (specificExcessAmt.compareTo(Env.ZERO) < 0) {
						 excessAmt = specificExcessAmt;
						 specificExcessAmt = amt;
					} else {
						specificExcessAmt = excessAmt.negate();  // it must be positive , negated below
						excessAmt = new BigDecimal("0.00");
					}
				}
				doc.createPayment(0, amt.multiply(multiplicand), MPayment.TENDERTYPE_DirectDeposit,
						m_pos.getC_BankAccount_ID(), "", "",invoice,specificExcessAmt.negate());
			}
		}

	}

	@Override
	public void valueChange(ValueChangeEvent evt) {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
	}

	@Override
	public void tableChanged(WTableModelEvent event) {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		//data.get(event.getIndex0());
		Vector<Object> line = (Vector) data.get(event.getIndex0());
		//Vector<Object> oldline = (Vector) olddata.get(event.getIndex0());
		Integer chargeID = chargeIDs.get(event.getIndex0()-1);
		Integer qty = ((BigDecimal) line.get(2)).intValue();
		Integer oldqty = (Integer)olddata.get(event.getIndex0());
		if (qty == null || oldqty == null) {
			return;
		}
		if ( chargeID != null && chargeID > 0 ) {
			//NCG Don't allow change of qty on charge, always must be 1
			line.set(2, new BigDecimal(1));
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

	private void EnterPressedOnAmount() {

		if (log.isLoggable(Level.CONFIG))
			log.config("");
		
		if (currentBPartner == null || cmbCustomer.getValue() == null || "".equals(cmbCustomer.getValue()) ) {
			//FDialog.error(form.getWindowNo(), "Please select a Supplier");
			cmbCustomer.setValue(null);
			txtAmount.setValue("1");
			cmbCustomer.setFocus(true);
			throw new WrongValueException(cmbCustomer, "Please select a Customer");
		}

		if (charge_ID_sel == 0 || cmbCharge.getValue() == null) {
			charge_ID_sel = 0;
			throw new WrongValueException(cmbCharge, "Please select a charge");
		}

//		txtAmount.setValue( (new BigDecimal(txtAmount.getValue())).abs().toString() );
		//Make sure is negative, and reduces the C/N
		BigDecimal unitPrice = new BigDecimal(txtAmount.getValue()).abs().negate();
		MCharge mCharge = new MCharge(m_ctx, charge_ID_sel, null);
		
		//NCG: Moved tax calculation into routine
		int tax_ID = TaxCustom.get(m_ctx, mCharge.getC_TaxCategory_ID());
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
		txtAmount.setValue("0");
		cmbCharge.setValue("");
		charge_ID_sel = 0;
	    cmbCharge.setFocus(true);


	}

	public void chargeLoad( BigDecimal unitPrice,int Pcharge_ID_sel, BigDecimal discount,
			BigDecimal priceAfterDiscount, BigDecimal PSV, BigDecimal unitPSV) {
		
		if (log.isLoggable(Level.CONFIG))
			log.config("");
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
			//NCG 2014/07/28
			line.add(charge.getName() + ((charge.getDescription() == null) ? " " : "-" + charge.getDescription()));
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



	@Override
	public void onEvent(Event event) throws Exception {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		System.out.println("aaa");

	}

	@Override
	public ADForm getForm() {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		return null;
	}


	@Override
	public void tableValueChange(TableValueChangeEvent event) {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
	}






}
