package org.adempiere.webui.apps.form;
//package org.adempiere.webui.apps.form;


import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;

import org.adempiere.webui.component.Borderlayout;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.ComboboxNew;
import org.adempiere.webui.component.Grid;
import org.adempiere.webui.component.GridFactory;
import org.adempiere.webui.component.ListModelTable;
import org.adempiere.webui.component.ListboxFactoryMA;
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
import org.compiere.model.MOrder;
import org.compiere.model.MOrderLine;
import org.compiere.model.MOrg;
import org.compiere.model.MOrgInfo;
import org.compiere.model.MPInstance;
import org.compiere.model.MPOS;
import org.compiere.model.MPriceList;
import org.compiere.model.MProcess;
import org.compiere.model.MProduct;
import org.compiere.model.MProductPricing;
import org.compiere.model.MTax;
import org.compiere.model.MUser;
import org.compiere.model.PO;
import org.compiere.model.Query;
import org.compiere.model.TaxCustom;
import org.compiere.model.X_M_Product;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.report.ReportStarterDazzle;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.compiere.utils.Calcs;
import org.eevolution.pos.VDocument;
import org.eevolution.pos.VPricing;
import org.zkoss.zhtml.Textarea;
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

import za.co.ntier.common.NTierDateUtil;
import za.co.ntier.common.NTierUtils;
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
public class WDazzleQuotePos extends WDazzleBase
implements IFormController, EventListener<Event>, WTableModelListener, ValueChangeListener,TableValueChangeListener
{
	/**
	 *
	 */
	//private BigDecimal discountOverallAmt = Env.ZERO;
	private MOrder mQuote = null;
	private BigDecimal roundCentsAmt = Env.ZERO;
	private static final int DISCOUNT_CHARGE_ID   = 1000000;
	private static final int ROUNDCENTS_CHARGE_ID = 1000001;
	private static final long serialVersionUID = 1L;
	private boolean taxIncluded = true;
	private Properties m_ctx = Env.getCtx();
	private Vector<Object> data = new Vector<Object>();
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

	private static CLogger log = CLogger.getCLogger(WDazzleQuotePos.class);

	private Intbox intQuantity = new Intbox();
	private Label lblQuantity = new Label("Quantity");



	private ComboboxNew cmbProduct = new ComboboxNew();
	private Label lblProduct = new Label("Product");

	private Textbox txtCustomer = new Textbox();
	private Label lblCustomerRefNo = new Label("Reference No");
	private Textbox txtCustomerRefNo = new Textbox();
	private Label lblComment = new Label("Comment");
	private Textarea txtComment = new Textarea();

	private Label lblQuotationNo = new Label("Quotation No");
	private ComboboxNew cmbQuotationNo = new ComboboxNew();

	private MPOS m_pos = null;

	private boolean newProduct = false;

	private MBPartner currentBPartner;
	private int prod_ID_sel = 0;

	private MPriceList pl = null;
	private MUser user = null;

	private int[] prod_id = null;
	private Integer index = 0;

	private ArrayList<Integer> prodIDs = new ArrayList<Integer>();

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
	
	protected Label lblHelp = new Label("F6 - Clear Screen, F7 - Delete line, F8 - Checkout");
	
	private boolean back = false;


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
		row.setSpans("1,3,1,2");
		row.appendChild(lblCustomer);

		//cmbCustomer.setReadonly(true);
		//cmbCustomer.setDisabled(true);

		cmbCustomer.setValue(null);



		String sql2 = "select bp.value || '-' || bp.name,bp.C_Bpartner_ID from C_Bpartner bp,c_bp_group bpg where  "
				+ " (upper(bp.value)) like ?"
				+ " and bp.c_bp_group_id = bpg.c_bp_group_id and bp.isCustomer = 'Y'"
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
					if (cmbCustomer.getValue() != null && cmbCustomer.getSelectedItem() != null) {
						int cust_ID_sel = ((WObjRet)cmbCustomer.getSelectedItem().getValue()).getIDOfRecord();
						int selIndex = cmbCustomer.getSelectedIndex();
						if (selIndex > -1) {
							currentBPartner = new MBPartner(Env.getCtx(), cust_ID_sel, null);
							MBPartnerLocation [] loc  = MBPartnerLocation.getForBPartner(m_ctx, currentBPartner.getC_BPartner_ID()
									,null);
							if (loc == null || loc.length <= 0) {
								//FDialog.error(form.getWindowNo(), "Supplier requires an address");
								cmbCustomer.setFocus(true);
								throw new WrongValueException(cmbCustomer, "Supplier requires an address");
							}
							
							cmbQuotationNo.setReadonly(false);
							cmbQuotationNo.setDisabled(false);
							setComboQuotationNo();
						}

						cmbProduct.setFocus(true);
					} else {

						cmbCustomer.setValue(null);
						cmbCustomer.setFocus(true);
						cmbQuotationNo.setReadonly(true);
						cmbQuotationNo.setDisabled(true);
						cmbQuotationNo.setValue(null);
						currentBPartner = null;
					}

				}
			});
		}

		if (!cmbQuotationNo.isListenerAvailable(Events.ON_CHANGE, false)) {
			cmbQuotationNo.addEventListener(Events.ON_CHANGE,new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(org.zkoss.zk.ui.event.Event event)
						throws Exception {
					//clearBottomScreen();
					//clearScreen();
					if (cmbQuotationNo.getValue() != null && cmbQuotationNo.getSelectedItem() != null) {
						int selIndex = cmbQuotationNo.getSelectedIndex();
						if (selIndex > -1) {
							int orderID = ((WObjRet)cmbQuotationNo.getSelectedItem().getValue()).getIDOfRecord();
							log.info("Populate with orderID=" + orderID);
							populateScreenFromQuote(orderID);
						}
						cmbQuotationNo.setFocus(true);
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

		cmbCustomer.setWidth("600px");
		//SimpleConstraint constr = new SimpleConstraint("no empty");
		//cmbCustomer.setConstraint(constr);
		row.appendChild(cmbCustomer);
		lblCustomerRefNo.setStyle("padding-left:60px");
		row.appendChild(lblCustomerRefNo);
		txtCustomerRefNo.setWidth("400px");
		row.appendChild(txtCustomerRefNo);

		row = rows.newRow();
		row.setSpans("1,3");
		row.appendChild(lblComment);
		txtComment.setStyle("width:99%; height: 99%; margin: auto; display: inline-block;");
		//txtComment.setWidth("400px");
		//txtComment.setV
		row.appendChild(txtComment);

		// NCG 07 Aug 2014: Add quotation no.
		lblQuotationNo.setStyle("padding-left:60px");
		row.appendChild(lblQuotationNo);
		
		cmbQuotationNo.setReadonly(true);
		cmbQuotationNo.setDisabled(true);
		row.appendChild(cmbQuotationNo);



		// Row 3
		//intQuantity.setConstraint("no empty, no negative, no zero");

		// cmbProduct.setConstraint("no empty");//set constraint that the combo
		// box should no be empty
		cmbProduct.setButtonVisible(false);
		row = rows.newRow();
		row.setStyle("border: none; background:transparent;");
		row.setSpans("1,3,1");
		lblProduct.setStyle("background:transparent;");
		row.appendChild(lblProduct);
		cmbProduct.setStyle("background:transparent;");
		cmbProduct.setWidth("600px");
		row.appendChild(cmbProduct);
		lblQuantity.setStyle("padding-left:60px");
		row.appendChild(lblQuantity);
		intQuantity.setTooltiptext("Enter quantity");
		intQuantity.setStyle("background:transparent;");
		row.appendChild(intQuantity);

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

		 /*String sql = "select p.value || '-' ||  p.name as name,0" +
                 " from M_Product p" +
                 " where (upper(p.value)) like ?"  +
                 " and p.ad_client_ID = " + Env.getAD_Client_ID(m_ctx) +
                 " group by p.value, p.name" +
                 " order by p.value";*/
		 String sql = "select p.value || ' | ' ||  p.description as name,p.M_Product_ID " +
                 " from M_Product p" +
                 " where (upper(p.value)) like ?"  +
                 " and p.ad_client_ID = " + Env.getAD_Client_ID(m_ctx) +
                 " and p.description  is not null" +
                 " order by p.value";

		// sqlProd = sql;
		 SimpleListModelNew dictModel= new SimpleListModelNew();
		 dictModel.setCbox(cmbProduct);
		 cmbProduct.setModel(dictModel);
		 cmbProduct.setQueryName(sql);
		 cmbProduct.setAutodrop(true);
		 //cmbProduct.setFocus(true);

		 if (!cmbProduct.isListenerAvailable(Events.ON_CHANGE, false)) {
			 cmbProduct.addEventListener(Events.ON_CHANGE,
					 new org.zkoss.zk.ui.event.EventListener(){
				 public void onEvent(org.zkoss.zk.ui.event.Event event) throws Exception{
					 if (cmbProduct.getSelectedItem() == null || cmbProduct.getSelectedItem().getValue() == null) {
						// FDialog.error(form.getWindowNo(),
							//	 "The Product Code for the selected product does not exist");
						 cmbProduct.setFocus(true);
						 throw new WrongValueException(cmbCustomer, "The Product Code for the selected product does not exist");
						// return;
					 }
					 if (cmbProduct.getSelectedItem() != null) {
						 prod_ID_sel = ((WObjRet) cmbProduct.getSelectedItem().getValue()).getIDOfRecord();
						 newProduct = true;
						 if (cmbProduct.getSelectedItem() != null)
							 intQuantity.setFocus(true);
					 } else {

					 }
					 //End

				 }
			 });
		 }


			if (!intQuantity.isListenerAvailable(Events.ON_OK, false)) {
				intQuantity.addEventListener(Events.ON_OK,
						new org.zkoss.zk.ui.event.EventListener(){
					public void onEvent(org.zkoss.zk.ui.event.Event event) throws Exception{
						if (newProduct) {
							EnterPressedOnQty ();
							newProduct = false;
						}
					}
				});
			}



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

	// NCG 07 Aug 2014: #1000018
	private void setComboQuotationNo() {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		// NCG RQ100032: Too many items in quote selection dropdown
		int monthsHistory = DazzleConfigManager.getDazzleQuoteDropdownMonths(m_ctx, null);
		Timestamp cutOffDate = NTierDateUtil.addMonth(
				NTierDateUtil.getCurrentDate(), (monthsHistory*-1));
		String cutOffDateStr = new SimpleDateFormat("yyyy/MM/dd").format(
				cutOffDate).toString();
		cmbQuotationNo.setValue(null);
		StringBuffer sql = new StringBuffer(
				"SELECT  o.Documentno || '_ ' || to_char(o.dateordered, 'YYYY-MM-DD') || '_ R' || o.grandTotal,o.C_Order_ID ")
				.append(" FROM C_Order o "
						+ " WHERE o.updated>=to_date('" + cutOffDateStr + "', 'YYYY/MM/DD') "
						+ " and o.c_doctype_id in ( select c_doctype_id from c_doctype where name='Non binding offer' ) "
						+ " and o.C_BPartner_ID= "
						+ currentBPartner.getC_BPartner_ID()
						+ " and o.documentno like ? " + " AND o.C_Order_ID IN "
						+ "(SELECT ol.C_Order_ID FROM C_OrderLine ol"
						+ " WHERE ol.QtyInvoiced = 0 ")
				.append(" and ol.m_product_id is not null) "
						+ " AND o.C_Order_ID NOT IN "
						+ "(SELECT ol.C_Order_ID FROM C_OrderLine ol"
						+ " WHERE ol.QtyInvoiced  <> 0 ")
				.append(" and ol.m_product_id is not null and ol.updated>=to_date('" + cutOffDateStr + "', 'YYYY/MM/DD') ")
				.append(" and ol.ad_client_id=" + Env.getAD_Client_ID(Env.getCtx())).append(" )")
				.append(" ORDER BY o.dateordered desc ");
		// .append(" ORDER BY o.documentno");
		SimpleListModelNew dictModel2 = new SimpleListModelNew();

		dictModel2.setCbox(cmbQuotationNo);
		cmbQuotationNo.setModel(dictModel2);
		cmbQuotationNo.setQueryName(sql.toString());
		cmbQuotationNo.setAutodrop(true);
		cmbQuotationNo.setButtonVisible(true);
		cmbQuotationNo.setWidth("400px");
	}
		
	/**
	 * NCG 2014/08/28 Adapted from WDazzleSupplierInvoice.populateScreen
	 */
	private void populateScreenFromQuote(int c_Order_ID) {
		
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		
		int cnt = 0;
		
		headerload();

		mQuote = new MOrder(m_ctx, c_Order_ID, null);
		
		if (mQuote.getPOReference() != null && !mQuote.getPOReference().equals(null)) {
			txtCustomerRefNo.setValue(mQuote.getPOReference());
		}
		if (mQuote.getDescription() != null && !mQuote.getDescription().equals(null)) {
			String desc = mQuote.getDescription();
			txtComment.setValue(desc);
		}


		int ids [] = PO.getAllIDs("C_OrderLine", "C_Order_ID = " + mQuote.getC_Order_ID() + " order by line" , null);
		for (int i = 0; i < ids.length; i++) {
			MOrderLine mOrderLine = new MOrderLine(m_ctx,ids[i],null);
			//log.warning("add orderline=" + mOrderLine);
			if ( mOrderLine.getM_Product_ID() == 0 ) {
				//ignore charge eg cents rounding
				continue;
			}
			int qty = mOrderLine.getQtyOrdered().intValue() - mOrderLine.getQtyInvoiced().intValue();
//			log.warning("i=" + i + ", qty=" + qty +", mOrderLine.getQtyOrdered()=" +
//					mOrderLine.getQtyOrdered() + ", mOrderLine.getQtyInvoiced()=" +
//					mOrderLine.getQtyInvoiced());
			if ( qty <=0 ) {
				continue;
			}
			cnt++;
//			addLine(mOrderLine.getM_Product_ID(), qty, mOrderLine.getDiscount(), 
//					mOrderLine.get_ID());
			addLine(mOrderLine.getM_Product_ID(), mOrderLine.getPriceList(), qty, 
					mOrderLine.getPriceList().subtract(mOrderLine.getPriceActual()) /* discount amt */,
					mOrderLine.get_ID());
		}

		if ( cnt == 0 ) {
			throw new WrongValueException(cmbQuotationNo, "All lines on quote already invoiced.");
		}
		resultsTable.clear();
		model = new ListModelTable(data);
		model.addTableModelListener(this);
		resultsTable.setData(model, columnNames, columnWidths);
		setLineColumnClass(resultsTable);
		calculateTotsBottomPanel();

	}
	
	private void deleteLine() {


		int selInx = resultsTable.getSelectedIndex();
		if (selInx > -1) {
			MProduct product = new MProduct(Env.getCtx(),
					prodIDs.get(selInx - 1), null);

			prodIDs.remove(selInx - 1); // First line on grid blank
			resultsTable.clear();
			data.remove(selInx);
			model = new ListModelTable(data);
			model.addTableModelListener(this);
			resultsTable.setData(model, columnNames, columnWidths);
			setLineColumnClass(resultsTable);
			calculateTotsBottomPanel();
		}
	}



	private void EnterPressedOnQty() {


		if (currentBPartner == null || cmbCustomer.getValue() == null) {
			//FDialog.error(form.getWindowNo(), "Please select a Supplier");
			cmbCustomer.setValue(null);
			intQuantity.setValue(1);
			cmbCustomer.setFocus(true);
			throw new WrongValueException(cmbCustomer, "Please select a Supplier");
			//return;
		}

		
		try {
			addLine(prod_ID_sel, null /* Recalc price */, intQuantity.getValue(), null, null /* Discount amt */);
//			addLine(prod_ID_sel, intQuantity.getValue(), BigDecimal.ZERO, null);
		} catch (WrongValueException ex) {
			intQuantity.setValue(1);
			cmbProduct.setValue("");
			prod_ID_sel = 0;
			cmbProduct.setFocus(true);
			throw new WrongValueException(ex.getComponent(), ex.getMessage());
		}
		

//		MProduct mProduct = new MProduct(m_ctx, prod_ID_sel, null);
//		VPricing vpr = new VPricing();
//
//		pl = vpr.getPriceList(prod_ID_sel, m_ctx, currentBPartner);
//		if (pl == null) {
//			//FDialog.error(form.getWindowNo(), "No Price List for Product");
//			intQuantity.setValue(1);
//			cmbProduct.setValue("");
//			prod_ID_sel = 0;
//			// End
//			cmbProduct.setFocus(true);
//			throw new WrongValueException(cmbProduct, "No Price List for Product");
//		} else {
//			BigDecimal unitPrice = vpr.getPrice(prod_ID_sel, pl, m_ctx, m_pos);
//
//			if (unitPrice == null) {
//				//FDialog.error(form.getWindowNo(),
//				//		"Price could not be found for product");
//				intQuantity.setValue(1);
//				cmbProduct.setValue("");
//				prod_ID_sel = 0;
//				// End
//				cmbProduct.setFocus(true);
//				throw new WrongValueException(cmbProduct, "No Price found for Product");
//			} else {
//				MOrg org = new MOrg(m_ctx, Env.getAD_Org_ID(m_ctx), null);
//				MOrgInfo mOrgInfo = MOrgInfo.get(m_ctx, org.getAD_Org_ID(),null);
//				int tax_ID = TaxCustom.get(m_ctx, mProduct.getC_TaxCategory_ID(), true,
//						new Timestamp(System.currentTimeMillis()), mOrgInfo
//								.getC_Location_ID(), mOrgInfo
//								.getC_Location_ID(),
//						new Timestamp(System.currentTimeMillis()), mOrgInfo
//								.getC_Location_ID(), mOrgInfo
//								.getC_Location_ID());
//				MTax mTax = new MTax(m_ctx, tax_ID, null);
//				BigDecimal tax = mTax.calculateTax(unitPrice,pl.isTaxIncluded(), 8);
//				Integer qty = new Integer(intQuantity.getValue());
//				BigDecimal unitPSV = unitPrice.subtract(tax);
//				tax = tax.multiply(new BigDecimal(qty.intValue()));
//
//				BigDecimal linePSV = (unitPrice.multiply(new BigDecimal(qty
//						.intValue())));
//				if (pl.isTaxIncluded()) {
//					linePSV = linePSV.subtract(tax);
//				} else {
//					taxIncluded = false;
//				}
//
//				BigDecimal[] prices = getDiscount(prod_ID_sel,currentBPartner.getC_BPartner_ID(),
//						new BigDecimal(intQuantity.getValue()));
//
//				productLoad(prod_id, unitPrice, prod_ID_sel, prices[0],
//							prices[1], linePSV, unitPSV);
//
//
//			}
//		}
		// Added by Simbarashe Musabaike
		// To clear text
		// Start
		intQuantity.setValue(1);
		cmbProduct.setValue("");
		prod_ID_sel = 0;
		// End
		cmbProduct.setFocus(true);

	}
	
	/**
	 * Add a line to the invoice
	 * 	NCG 2014/09/03 Common routine for EnterPressedOnQty & populateScreenFromQuote
	 *  NCG 2015/06/05 Incorrect discount used when quote is transferred to an invoice
	 * @param m_Product_ID
	 * @param price					If null - recalc price, otherwise use supplied price
	 * @param qty
	 * @param discountAmt 			If null - recalc discount, otherwise use supplied discount
	 * @param quoteOrderLineID
	 */
	private void addLine(int m_Product_ID, BigDecimal price, Integer qty, BigDecimal discountAmt, 
			Integer quoteOrderLineID) {
//		if (currentBPartner == null || cmbCustomer.getValue() == null) {
//			//FDialog.error(form.getWindowNo(), "Please select a Supplier");
//			cmbCustomer.setValue(null);
//			intQuantity.setValue(1);
//			cmbCustomer.setFocus(true);
//			throw new WrongValueException(cmbCustomer, "Please select a Supplier");
//			//return;
//		}


		MProduct mProduct = new MProduct(m_ctx, m_Product_ID, null);
		VPricing vpr = new VPricing();
		String prodDescr = String.format("'%s' - '%s'", mProduct.getValue(), mProduct.getName());
		
		pl = vpr.getPriceList(m_Product_ID, m_ctx, currentBPartner);
		if (pl == null) {
			throw new WrongValueException(cmbProduct, "No Price List for product: " + prodDescr);
		} else {
			
			BigDecimal unitPrice;
			if ( price == null) {
				// Adding a new line - calculate the price
				unitPrice = vpr.getPrice(m_Product_ID, pl, m_ctx, m_pos);
			} else {
				// Quotation - use the quoted price
				unitPrice = price;
			}
			
			if (unitPrice == null) {
				throw new WrongValueException(cmbProduct, "Price could not be found for product: "  + prodDescr);
			} else {
				
				unitPrice = unitPrice.setScale( 2, BigDecimal.ROUND_HALF_UP );	//#1000215: Line discount causing invoice to be unpaid
				MOrg org = new MOrg(m_ctx, Env.getAD_Org_ID(m_ctx), null);
				MOrgInfo mOrgInfo = MOrgInfo.get(m_ctx, org.getAD_Org_ID(),null);
				int tax_ID = TaxCustom.get(m_ctx, mProduct.getC_TaxCategory_ID(), true,
						new Timestamp(System.currentTimeMillis()), mOrgInfo
								.getC_Location_ID(), mOrgInfo
								.getC_Location_ID(),
						new Timestamp(System.currentTimeMillis()), mOrgInfo
								.getC_Location_ID(), mOrgInfo
								.getC_Location_ID());
				MTax mTax = new MTax(m_ctx, tax_ID, null);
				BigDecimal tax = mTax.calculateTax(unitPrice,pl.isTaxIncluded(), 2);
				if (currentBPartner.isTaxExempt()) {
					tax = new BigDecimal("0.00");
				}
				//qty is a parameter
				//Integer qty = new Integer(intQuantity.getValue());
				BigDecimal unitPSV = unitPrice.subtract(tax);
				tax = tax.multiply(new BigDecimal(qty.intValue()));

				BigDecimal linePSV = (unitPrice.multiply(new BigDecimal(qty.intValue())));
				if (pl.isTaxIncluded()) {
					linePSV = linePSV.subtract(tax);
				} else {
					taxIncluded = false;
				}
				
				//nTier Software Services  Neil Gordon NCG: 2015/06/03: #RQ100010: Incorrect discount used when quote is transferred to an invoice
				if ( discountAmt == null ) {
					// Recalculate discount amount (adding a new line, not from quote)
					BigDecimal[] prices = getDiscount(m_Product_ID,currentBPartner.getC_BPartner_ID(),
							new BigDecimal(qty));
					BigDecimal discountPerc = prices[0];
					// NCG: Discount not being extended
					discountAmt = linePSV.multiply(discountPerc.divide(new BigDecimal("100")));
//					discountAmt = unitPrice.multiply(discountPerc.divide(new BigDecimal("100")));
					discountAmt = discountAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
				}
				
//				BigDecimal discount;
//				if ( discountPerc == null ) {
//					// Recalculate (adding a new line, not from quote)
//					BigDecimal[] prices = getDiscount(m_Product_ID,currentBPartner.getC_BPartner_ID(),
//							new BigDecimal(qty));
//					discount = prices[0];
//				} else {
//					// Use supplied discount (from quote)
//					discount = discountPerc;
//				}
				// Removed: prices[1]
				productLoad(prod_id, unitPrice, m_Product_ID, discountAmt,
						linePSV, unitPSV, qty);

//				productLoad(prod_id, unitPrice, m_Product_ID, prices[0],
//							prices[1], linePSV, unitPSV, qty);


			}
		}
	}

	private void calculateTotsBottomPanel() {
		BigDecimal calcTot = calculateTotal(model.toArray(), 6, false, prodIDs);
		BigDecimal newCalcTot = Calcs.round(calcTot,new BigDecimal(".05"),RoundingMode.HALF_UP);
		roundCentsAmt = calcTot.subtract(newCalcTot);
		txtTotalLines.setValue(newCalcTot.toString());

	}


	private BigDecimal calculateTotal(Object[] lmt, int col,
			boolean otherItems, ArrayList<Integer> PprodIDs) {
		BigDecimal tot = Env.ZERO;
		BigDecimal totTax = Env.ZERO;

		int rowCnt = lmt.length;
		for (int i = 0; i < rowCnt; i++) {
			if (((Vector) lmt[i]).get(0) != null) {
				Integer prodID = (Integer) PprodIDs.get(i - 1); // we store this
																// per row,
																// first line is
																// blank
				X_M_Product x_prod = new X_M_Product(m_ctx, prodID, null);
				BigDecimal totalLine = (BigDecimal) ((Vector) lmt[i]).get(col);
				tot = tot.add(totalLine);
				totTax = totTax.add(taxCalc(x_prod.getC_TaxCategory_ID(), totalLine));

			}
		}

		txtSubTotal.setValue(tot.toPlainString());

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
		BigDecimal tax = mTax.calculateTax(Price,pl.isTaxIncluded(), 2);
		return tax;

	}
	//nTier Software Services Neil Gordon NCG: 2015/06/03: #RQ100010: Incorrect discount used when quote is transferred to an invoice
	// Added parameter discountAmt
	public void productLoad(int[] prodArr, BigDecimal unitPrice,
			int Pprod_ID_sel, BigDecimal discountAmt, BigDecimal PSV, BigDecimal unitPSV,
			Integer qty) {

		if (Pprod_ID_sel != 0) {
			// Creating the table grid
			MProduct product = new MProduct(Env.getCtx(), Pprod_ID_sel, null);

			//Integer qty = intQuantity.getValue();
			/*if (prodIDs != null && prodIDs.contains(Pprod_ID_sel)) {
				int idx = prodIDs.indexOf(new Integer(Pprod_ID_sel));
				if (idx >= 0) {

					Vector<Object> line = (Vector) data.get(idx + 1);
					int newQty = ((Integer) line.get(2)).intValue()
							+ qty.intValue();

					line.set(2, newQty);
					line.set(3,unitPrice);
					BigDecimal RSP = (unitPrice.multiply(new BigDecimal(
							newQty))).setScale(2, BigDecimal.ROUND_HALF_UP);
					line.set(4, RSP);
					idx = data.indexOf(line);
					data.set(idx, line);
				}

			} else */{

				prodIDs.add(new Integer(product.getM_Product_ID()));
				index = new Integer(index.intValue() + 10);
				BigDecimal RSP = (unitPrice.multiply(new BigDecimal(qty)))
						.setScale(2, BigDecimal.ROUND_HALF_UP);
//				BigDecimal discountAmt = new BigDecimal("0.00");
//
//				discountAmt = RSP.multiply(discountPerc.divide(new BigDecimal("100"), 2,BigDecimal.ROUND_HALF_UP));
//				discountAmt = discountAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
				if ( discountAmt == null ) {
					// From enterpressedonqty - no discount
					discountAmt = Env.ZERO;
				}

				//BigDecimal total = priceAfterDiscount.multiply(new BigDecimal(qty)).setScale(2,BigDecimal.ROUND_HALF_UP);
				// NCG: NB: Differs from WDazzlePOS
				Vector<Object> line = new Vector<Object>();
				line.add(index);													// #0
				line.add(product.getValue());		// #1
//				line.add(product.getValue() + "-" + product.getDescription());		// #1
				line.add(qty);														// #2
				line.add(unitPrice);												// #3
				line.add(RSP);														// #4
				line.add(discountAmt);												// #5
				line.add(RSP.subtract(discountAmt));								// #6
				// NCG: NB: Differs from WDazzlePOS
//				line.add(product.getValue());										// #7
				data.add(line);
			}


		}


		resultsTable.clear();
		model = new ListModelTable(data);
		model.addTableModelListener(this);
		resultsTable.setData(model, columnNames, columnWidths);
		setLineColumnClass(resultsTable);


		calculateTotsBottomPanel();

	}

	public void headerload()
    {

        columnNames.clear();
        columnNames.add("Index");
        columnNames.add("Description");
        columnNames.add("Quantity");
        columnNames.add("Unit Price");
        columnNames.add("Orig Total");
        columnNames.add("Discount");
        columnNames.add("Total");

        columnWidths.clear();
        columnWidths.add(new Integer(60));
        columnWidths.add(new Integer(650));
        columnWidths.add(new Integer(100));
        columnWidths.add(new Integer(100));
        columnWidths.add(new Integer(100));
        columnWidths.add(new Integer(100));
        columnWidths.add(new Integer(100));

        clearIDs();
        Vector<Object> line = new Vector<Object>();
        data.clear();
    	data.add(line);

        model = new ListModelTable(data);
        model.addTableModelListener(this);
        resultsTable.setData(model, columnNames, columnWidths);
        setLineColumnClass(resultsTable);


    }

	private void clearIDs() {
		index = 0;
		prodIDs.clear();
		mQuote = null;
		//quoteOrderLineIDs.clear();
	}
	
	public void setLineColumnClass(IMiniTable lineTable) {
		
		int i = 0;
		lineTable.setColumnClass(i++, Integer.class, true); // 0 seq
		lineTable.setColumnClass(i++, String.class, true); // 1-Description
		lineTable.setColumnClass(i++, Integer.class, false); // 2-Quantity
		lineTable.setColumnClass(i++, BigDecimal.class, true); // 3-Unit Price
		lineTable.setColumnClass(i++, BigDecimal.class, true); // 4 - Incl Price
		lineTable.setColumnClass(i++, BigDecimal.class, false); // 5-Discount
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

	public BigDecimal[] getDiscount(int M_Product_ID, int C_BPartner_ID,
			BigDecimal Qty) {
		BigDecimal discount = Env.ZERO;
		BigDecimal stdPrice = Env.ZERO;
		BigDecimal[] prices = new BigDecimal[2];
		MProductPricing pp = new MProductPricing(M_Product_ID, C_BPartner_ID,
				Qty, true);
		pp.setM_PriceList_ID(pl.getM_PriceList_ID());
		boolean calculate = pp.calculatePrice();
		discount = pp.getDiscount();
		stdPrice = pp.getPriceStd();
		prices[0] = discount;
		prices[1] = stdPrice;

		return prices;
	}

	/**
	 * @author NCG
	 * 2016/04/12 Discount not being extended
	 */
	public BigDecimal getDiscountPercent(int M_Product_ID, int C_BPartner_ID,
			BigDecimal Qty) {
		BigDecimal[] prices = getDiscount(M_Product_ID, C_BPartner_ID,
				Qty);
		BigDecimal discountPerc = prices[0];
		return discountPerc;
	}

	public void onCtrlKey(KeyEvent event) throws Exception {
		int keyCode = event.getKeyCode();
		if (keyCode == KeyEvent.F8) {
			String docNo = createOrder();
			//NCG 2014/09/08 Resolve problems with F6
			clearBottomScreen();
			clearScreen();
			zkInit();

		} else if (keyCode == KeyEvent.F6) {
			//clearScreen();
			clearBottomScreen();
			clearScreen();
			clearIDs();
			zkInit();		//NCG 2014/09/08 Added to resolve problem where screen blanked after pressing F6
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



	private BigDecimal calcBalDue() {
		BigDecimal balDue = new BigDecimal("0.00");
		balDue = new BigDecimal(txttotalDue.getValue());
		balDue = balDue.subtract(new BigDecimal(txtCC.getValue()));
		balDue = balDue.subtract(new BigDecimal(txtCash.getValue()));
		balDue = balDue.subtract(new BigDecimal(txtCheques.getValue()));
		balDue = balDue.subtract(new BigDecimal(txtEFT.getValue()));
		return balDue;
	}

	private BigDecimal calcChange(BigDecimal balDue) {
		BigDecimal change = new BigDecimal("0.00");
		if (balDue.compareTo(new BigDecimal("0.00")) < 0) {
			if (txtCash != null || txtCheques != null) {
				BigDecimal amt = (new BigDecimal("0.00"));
				if (txtCash != null) {
					amt = new BigDecimal(txtCash.getValue());
				}
				if (txtCheques != null ) {
					amt = amt.add(new BigDecimal(txtCheques.getValue()));
				}
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

	private void clearBottomScreen() {
		txtTotalLines.setValue("0.00");
		totalLines = new BigDecimal("0.00");
		txtTotalTax.setValue("0.00");
		txtDiscountAmt.setValue("0.00");
		txtSubTotal.setValue("0.00");
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
		clearScreenExcData();

		//txtDocNo.setText("");
		resultsTable.clear();
		data.clear();

		//prodIDs.clear();
		//txtTotalLines.setValue("0.00");
		//totalLines = new BigDecimal("0.00");
		// txtQuantity.setFocus(true);
		//ctxtrebateOrder.setChecked(false);
		//txtHostessDiscount.setValue("0.00");

		//Reset fields here
		cmbProduct.setValue(null);
		cmbQuotationNo.setValue(null);
		txtComment.setValue("");
		txtCustomerRefNo.setValue("");
		index = 0;
		//txtOrderRef.setValue(null);
		// txtComment.setValue(null);




	}


	private String createOrder() {
		if (currentBPartner == null || cmbCustomer.getValue() == null) {
			FDialog.error(form.getWindowNo(), "Please select Supplier");
			cmbCustomer.setFocus(true);
			intQuantity.setValue(1);
			throw new WrongValueException(cmbCustomer, "Please select Supplier");
		}
		
		if ( mQuote != null ) {
			mQuote.setDocAction(MOrder.ACTION_ReActivate);
			mQuote.processIt(MOrder.ACTION_ReActivate);
			mQuote.saveEx();
			
			//Delete lines off quote (they will be recreated)
			for (MOrderLine line:mQuote.getLines()) {
				line.deleteEx(false);
			}
		}
		
		Object[] objs = model.toArray();
		VDocument doc = new VDocument(currentBPartner);

		//	doc.setQuote(true);

		//docOrder = doc;
		//doc.setOrderref(txtOrderRef.getValue());
		//doc.setOrderComment(txtComment.getValue());
		doc.setM_Warehouse_ID(m_pos.getM_Warehouse_ID());
		doc.setPl(pl);
		int m_C_Currency_ID = Env.getContextAsInt(Env.getCtx(), "$C_Currency_ID"); // default
		doc.setC_Currency_ID(m_C_Currency_ID);

		MBPartnerLocation bpl = MBPartnerLocation.getForBPartner(m_ctx, currentBPartner.getC_BPartner_ID(),null)[0];

		doc.setC_BPartner_Location_ID(bpl.getC_BPartner_Location_ID());

		doc.setRefNo(txtCustomerRefNo.getValue());
		doc.setComment(txtComment.getValue());


		doc.setSalesRep_ID(Env.getAD_User_ID(m_ctx));
		doc.setCreatePO(false);



		String docno = null;
		doc.setSOTrx(true);
		doc.setQuote(true);

		MOrder mOrder = doc.createOrderLines(mQuote, objs,prodIDs,null, DISCOUNT_CHARGE_ID,null,ROUNDCENTS_CHARGE_ID,roundCentsAmt);
		int m_AD_Process_ID = MProcess.getProcess_ID(
				"POS_Print_Quote", null);

		MPInstance instance = new MPInstance(Env.getCtx(),
				m_AD_Process_ID, 0);
		if (!instance.save()) {
			return docno;
		}
		// call process
		org.compiere.process.ProcessInfo pi = new org.compiere.process.ProcessInfo(
				"Quote", m_AD_Process_ID);
		pi.setAD_PInstance_ID(instance.getAD_PInstance_ID());
		ProcessInfoParameter piInvoiceId = new ProcessInfoParameter(
				"RECORD_ID", mOrder.getC_Order_ID(), null, null, null);
		ProcessInfoParameter[] piParameters = new ProcessInfoParameter[] {
				piInvoiceId };
		// NCG 11 Aug 2014: Removed AD_Client_ID and AD_Org_ID parameters, report infers from record instead
//		ProcessInfoParameter[] piParameters = new ProcessInfoParameter[] {
//				piClientId, piOrgId, piInvoiceId };
		pi.setParameter(piParameters);
		pi.setRecord_ID(mOrder.getC_Order_ID()	);

		ReportStarterDazzle rs = new ReportStarterDazzle();
		rs.startProcess(Env.getCtx(), pi, null);
		//FDialog.info(form.getWindowNo(), form,"Pur Created - DocumentNo : " + mOrder.getDocumentNo());
		intQuantity.setFocus(true);
		//prodIDs.clear();
		clearIDs();
		txtCustomerRefNo.setValue(null);
		txtComment.setValue(null);





		return docno;

	}



	@Override
	public void valueChange(ValueChangeEvent evt) {
		// TODO Auto-generated method stub
		System.out.println("aaa");

	}

	/*****************************************************************************
	 * This is the line change event for the individual lines on the invoice
	 *  Line discount change is handled in this method
	 *****************************************************************************/
//	@Override
//	public void tableChanged(WTableModelEvent event) {
//		/////////////////////////////////////////////////////////////////////////
//		//
//		//////////////////////////////////
//		if (log.isLoggable(Level.CONFIG))
//			log.config("");
//		System.out.println("aaa");
//		//data.get(event.getIndex0());
//		Vector<Object> line = (Vector) data.get(event.getIndex0());
//		BigDecimal newDiscount = ((BigDecimal) line.get(5));
//		//BigDecimal rsp = ((BigDecimal) line.get(4));
//		Integer qty;
//		if ( line.get(2) instanceof Integer )  {
//			//If qty has not been editted
//			qty = ((Integer) line.get(2));
//		} else {
//			//If qty has been editted
//			qty = (((BigDecimal) line.get(2))).intValue();
//			if ( qty <= 0 ) qty = 1;
//			//Round back to zero decimal places and remove - sign
//			line.set(2, qty);
//		}
//		BigDecimal unitPrice = ((BigDecimal) line.get(3));
//		BigDecimal unitDiscount = newDiscount.divide(new BigDecimal(qty) ,2, RoundingMode.HALF_UP);
//		BigDecimal newUnitPrice = unitPrice.subtract(unitDiscount);
//		BigDecimal rsp = newUnitPrice.multiply(new BigDecimal(qty));
//		rsp = rsp.setScale(2, BigDecimal.ROUND_HALF_UP);
//		line.set(6, rsp);
//		int idx = data.indexOf(line);
//		data.set(idx, line);
//		calculateTotsBottomPanel();
//
//	}
	// NCG: Discount not being extended
	@Override
	public void tableChanged(WTableModelEvent event) {
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		
		lineChanged(event);
		
		return;
		
	}

	/**
	 * NCG 2016/04/12 Discount not being extended
	 * 
	 * 	The user can change either the qty or the discount amount.
	 * (1) If the qty only is changed, then lookup the discount applicable for the product, and recalculate the line total.
	 *		line total = (qty * unit price) - (qty * unitprice * (applicable discount/100))
	 * (2) If the discount amount only is changed, then calculate the line total as follows: 
	 * 		line total = (qty * unit price) - ( discount amount entered) 
	 * (3) If the discount amount is changed as well as the quantity, then ignore the change on the discount amount, and apply logic in (1) 
	 */
	private void lineChanged(WTableModelEvent event) {
		
		if (log.isLoggable(Level.CONFIG))
			log.config("");
		
		Vector<Object> line = (Vector) data.get(event.getIndex0());
		BigDecimal lineTotal;
		BigDecimal discAmount;
		BigDecimal extendedPrice;
		// NCG: NB: Differs from WDazzleQuotePos
		String productValue = (String)line.get(1);
		MProduct product = NTierUtils.getProduct(m_ctx, null, productValue);
		BigDecimal discPercent = new BigDecimal(0);
		boolean isQtyChanged;
		discAmount = ((BigDecimal) line.get(5));
		BigDecimal qty1;
		Integer qty;
		if ( line.get(2) instanceof Integer )  {
			isQtyChanged = false;
			qty = ((Integer) line.get(2));
		} else {
			isQtyChanged = true;
			qty = (((BigDecimal) line.get(2))).intValue();
			if ( qty <= 0 ) qty = 1;
			//Round back to zero decimal places and remove - sign
			line.set(2, qty);
		}
		qty1 = new BigDecimal(qty);
		BigDecimal unitPrice = ((BigDecimal) line.get(3));
		extendedPrice = qty1.multiply(unitPrice);
		if (isQtyChanged) {
			discPercent = getDiscountPercent(product.get_ID(), currentBPartner.get_ID(), qty1);
			discAmount = extendedPrice.multiply(discPercent.divide(new BigDecimal(100)));
		}
		discAmount = discAmount.setScale(2, RoundingMode.HALF_UP);
		lineTotal = extendedPrice.subtract(discAmount);
		
		// Recalculate the unit price
		////////////////////////////////////////////////////////////////////////////////////////
		// The following 3 lines are explained in Development Notes under the doc folder
		////////////////////////////////////////////////////////////////////////////////////////
		BigDecimal unitDiscount = discAmount.divide(new BigDecimal(qty) ,2, RoundingMode.HALF_UP);
		BigDecimal unitPriceAdjusted = unitPrice.subtract(unitDiscount);
		BigDecimal adjustedUnitPrice = unitPriceAdjusted.multiply(new BigDecimal(qty));
		
		// Save values back to line
		line.set(5, discAmount);
//		line.set(6, lineTotal);
		line.set(6, adjustedUnitPrice);
		
		int idx = data.indexOf(line);
		data.set(idx, line);
		calculateTotsBottomPanel();
		
		log.warning(String.format(
				"isQtyChanged=%b, unitPrice=%f, qty=%f, discPercent=%f, " +
				" discAmount=%f, extendedPrice=%f, lineTotal=%f", 
				isQtyChanged, unitPrice, qty1, discPercent, 
				discAmount, extendedPrice,lineTotal));
	}


	@Override
	public void onEvent(Event event) throws Exception {
		System.out.println("aaa");

	}

	@Override
	public ADForm getForm() {
		return null;
	}


	@Override
	public void tableValueChange(TableValueChangeEvent event) {

	}






}
