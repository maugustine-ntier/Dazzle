package org.adempiere.webui.apps.form;
//package org.adempiere.webui.apps.form;


import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;

import javax.swing.Popup;

import org.adempiere.webui.component.Borderlayout;
import org.adempiere.webui.component.Button;
import org.adempiere.webui.component.ComboboxNew;
import org.adempiere.webui.component.Datebox;
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
import org.compiere.model.Query;
import org.compiere.model.TaxCustom;
import org.compiere.model.X_M_Product;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.report.ReportStarterDazzle;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
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

/**
 * @author 	Martin Augustine
 * 				Original author
 * @author 	Neil Gordon
 * 				Various modifications
 * 
 * Important Note: Before starting any development, or addressing any bug fix, please be sure to read the documents under:
 *  		etc/doc/systemsdocumentation, especially the document: Development Notes. Consider also: dazzleDevelopment/etc/doc/tickets
 */
public class WDazzlePOPos extends WDazzleBase
implements IFormController, EventListener<Event>, WTableModelListener, ValueChangeListener,TableValueChangeListener
{
	/**
	 *
	 */
	//private BigDecimal discountOverallAmt = Env.ZERO;
	private BigDecimal roundCentsAmt = Env.ZERO;
	private static final int DISCOUNT_CHARGE_ID   = 0; //1000000;  Not for PO
	private static final int ROUNDCENTS_CHARGE_ID = 0; // 1000001; Not for PO
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

	private Label lblCustomer = new Label("Supplier");
	private ComboboxNew cmbCustomer = new ComboboxNew();

	private static CLogger log = CLogger.getCLogger(WDazzlePOPos.class);

	private Intbox intQuantity = new Intbox();
	private Label lblQuantity = new Label("Quantity");



	private ComboboxNew cmbProduct = new ComboboxNew();
	private Label lblProduct = new Label("Product");

	private Textbox txtCustomer = new Textbox();
	private Label lblCustomerRefNo = new Label("Reference No");
	private Textbox txtCustomerRefNo = new Textbox();
	private Label lblComment = new Label("Comment");
	private Textarea txtComment = new Textarea();



	private MPOS m_pos = null;

	private boolean newProduct = false;

	private MBPartner currentBPartner;
	private int prod_ID_sel = 0;
	private int prev_prod_ID_sel = 0;

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
	private final Label lblDate = new Label("PO Date");
	private final Datebox dateOrder = new Datebox();


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
								//return;
							}
						}

						cmbProduct.setFocus(true);
					} else {
						//SimpleConstraint constr = new SimpleConstraint("");
						//cmbCustomer.setConstraint(constr);
						cmbCustomer.setValue(null);
						//constr = new SimpleConstraint("no empty");
						//cmbCustomer.setConstraint(constr);
						cmbCustomer.setFocus(true);
						currentBPartner = null;
						throw new WrongValueException(cmbCustomer, "Please select a valid Supplier");

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

		row = rows.newRow();
		row.appendChild(lblDate);
		final Date currentDate = new Date();
		dateOrder.setValue(currentDate);
		row.appendChild(dateOrder);



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
		 String sql = "select p.value || ' | ' ||  p.description as name,p.M_product_id as available " +
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
						//		 "The Product Code for the selected product does not exist");
						 //cmbProduct.setFocus(true);
						 prod_ID_sel = 0;
						 throw new WrongValueException(cmbProduct, "Please select a product");
						 //return;
					 }
					 prod_ID_sel = ((WObjRet) cmbProduct.getSelectedItem().getValue()).getIDOfRecord();
					 prev_prod_ID_sel = prod_ID_sel;

					 newProduct = true;
					 if (cmbProduct.getSelectedItem() != null) {
						 intQuantity.setFocus(true);
					 }
					 //End

				 }
			 });
		 }

		 if (!cmbProduct.isListenerAvailable(Events.ON_OK, false)) {
			 cmbProduct.addEventListener(Events.ON_OK,
					 new org.zkoss.zk.ui.event.EventListener(){
				 public void onEvent(org.zkoss.zk.ui.event.Event event) throws Exception{
					 if (cmbProduct.getSelectedItem() == null || cmbProduct.getSelectedItem().getValue() == null) {
						 prod_ID_sel = prev_prod_ID_sel;
						 newProduct = true;
						 intQuantity.setFocus(true);
					 } else {
						 if (prod_ID_sel == 0) {
							 prod_ID_sel = ((WObjRet) cmbProduct.getSelectedItem().getValue()).getIDOfRecord();
							 prev_prod_ID_sel = prod_ID_sel;
							 newProduct = true;
							 intQuantity.setFocus(true);

						 }
					 }


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

						} else {
							if (prod_ID_sel == 0 ) { //|| cmbProduct.getValue() == null) {
								cmbProduct.setFocus(true);
								throw new WrongValueException(cmbProduct, "Please select a product");
							}
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



	private  void EnterPressedOnQty() {

		if (currentBPartner == null || cmbCustomer.getValue() == null) {
			//FDialog.error(form.getWindowNo(), "Please select a Supplier");
			if (cmbCustomer.getValue() == null || cmbCustomer.getSelectedItem() == null) {

			}
			cmbCustomer.setValue(null);
			intQuantity.setValue(1);
			cmbCustomer.setFocus(true);
			throw new WrongValueException(cmbCustomer, "Please select a Supplier");
			//
			//return cmbCustomer;
		}

		if (prod_ID_sel == 0 ) { //|| cmbProduct.getValue() == null) {
			prod_ID_sel = 0;
			throw new WrongValueException(cmbProduct, "Please select a product");
		}

		MProduct mProduct = new MProduct(m_ctx, prod_ID_sel, null);
		VPricing vpr = new VPricing();
		vpr.setUsePOPriceList(true);

		pl = vpr.getPriceList(prod_ID_sel, m_ctx, currentBPartner);
		if (pl == null) {
			//FDialog.error(form.getWindowNo(), "No Price List for Product");
			intQuantity.setValue(1);
			cmbProduct.setValue("");
			prod_ID_sel = 0;
			cmbProduct.setFocus(true);
			throw new WrongValueException(cmbProduct, "No Price List for Product");
		} else {
			BigDecimal unitPrice = vpr.getPrice(prod_ID_sel, pl, m_ctx, m_pos);

			if (unitPrice == null) {
				//FDialog.error(form.getWindowNo(),
					//	"Price could not be found for product");
				intQuantity.setValue(1);
				cmbProduct.setValue("");
				prod_ID_sel = 0;
				cmbProduct.setFocus(true);
				throw new WrongValueException(cmbProduct, "Price could not be found for product");
			} else {
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
				Integer qty = new Integer(intQuantity.getValue());
				BigDecimal unitPSV = unitPrice.subtract(tax);
				tax = tax.multiply(new BigDecimal(qty.intValue()));

				BigDecimal linePSV = (unitPrice.multiply(new BigDecimal(qty
						.intValue())));
				if (pl.isTaxIncluded()) {
					linePSV = linePSV.subtract(tax);
				} else {
					taxIncluded = false;
				}

				BigDecimal[] prices = getDiscount(prod_ID_sel,currentBPartner.getC_BPartner_ID(),
						new BigDecimal(intQuantity.getValue()));

				productLoad(prod_id, unitPrice, prod_ID_sel, prices[0],
							prices[1], linePSV, unitPSV);


			}
		}
		intQuantity.setValue(null);
		cmbProduct.setValue("");
		prod_ID_sel = 0;
		// End
		//return cmbProduct;
	    cmbProduct.setFocus(true);

	}

	private void calculateTotsBottomPanel() {
		BigDecimal calcTot = calculateTotal(model.toArray(), 4, false, prodIDs);
		txtTotalLines.setValue(calcTot.toString());

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
		if (currentBPartner.isPOTaxExempt()) {
			/*int tax_ID = TaxMA.getExemptTax(m_ctx, Env.getAD_Org_ID(m_ctx));
			MTax mTax = new MTax(m_ctx, tax_ID, null);
			BigDecimal tax = mTax.calculateTax(Price,pl.isTaxIncluded(), 8);
			return tax;*/
			return new BigDecimal("0.00");
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

	public void productLoad(int[] prodArr, BigDecimal unitPrice,
			int Pprod_ID_sel, BigDecimal discount,
			BigDecimal priceAfterDiscount, BigDecimal PSV, BigDecimal unitPSV) {

		if (Pprod_ID_sel != 0) {
			// Creating the table grid
			MProduct product = new MProduct(Env.getCtx(), Pprod_ID_sel, null);

			Integer qty = intQuantity.getValue();
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

			} else*/ {

				prodIDs.add(new Integer(product.getM_Product_ID()));
				index = new Integer(index.intValue() + 10);
				BigDecimal RSP = (unitPrice.multiply(new BigDecimal(qty)))
						.setScale(2, BigDecimal.ROUND_HALF_UP);
				BigDecimal discountAmt = new BigDecimal("0.00");
				Vector<Object> line = new Vector<Object>();
				line.add(index);
				line.add(product.getValue() + "-" + product.getDescription());
				line.add(qty);
				line.add(unitPrice);
				line.add(RSP);
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
            columnNames.add("Total");

            columnWidths.clear();
            columnWidths.add(new Integer(60));
            columnWidths.add(new Integer(800));
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
		//quoteOrderLineIDs.clear();
	}

	public void setLineColumnClass(IMiniTable lineTable) {
		int i = 0;
		lineTable.setColumnClass(i++, Integer.class, true); // 0 seq
		lineTable.setColumnClass(i++, String.class, true); // 1-Description
		lineTable.setColumnClass(i++, Integer.class, false); // 2-Quantity
		lineTable.setColumnClass(i++, BigDecimal.class, false); // 3-Unit Price
		lineTable.setColumnClass(i++, BigDecimal.class, true); // 4-Total
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


	public void onCtrlKey(KeyEvent event) throws Exception {
		int keyCode = event.getKeyCode();
		if (keyCode == KeyEvent.F8) {
			String docNo = createOrder();
			clearBottomScreen();
			clearScreen();
			zkInit();

		} else if (keyCode == KeyEvent.F6) {
			//clearScreen();
			clearBottomScreen();
			clearScreen();
			clearIDs();
			zkInit();	//NCG 2014/09/08 Added to resolve problem where screen blanked after pressing F6
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

		cmbProduct.setValue(null);
		index = 0;
		//txtOrderRef.setValue(null);
		// txtComment.setValue(null);




	}


	private String createOrder() {
		if (currentBPartner == null) {
			//FDialog.error(form.getWindowNo(), "Please select Supplier");
			cmbCustomer.setFocus(true);
			throw new WrongValueException(cmbCustomer, "Please select a Supplier");
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
		doc.setCreatePO(true);



		String docno = null;
		doc.setSOTrx(false);
		if (dateOrder.getValue() != null) {
			doc.setPoDate(new Timestamp(dateOrder.getValue().getTime()));
		}

		MOrder mOrder = doc.createOrderLines(null /* Existing order */, objs,prodIDs, null, DISCOUNT_CHARGE_ID,null,ROUNDCENTS_CHARGE_ID,roundCentsAmt);
		int m_AD_Process_ID = MProcess.getProcess_ID(
				"POS_Print_PO", null);

		MPInstance instance = new MPInstance(Env.getCtx(),
				m_AD_Process_ID, 0);
		if (!instance.save()) {
			return docno;
		}
		// call process
		org.compiere.process.ProcessInfo pi = new org.compiere.process.ProcessInfo(
				"Purchase Order", m_AD_Process_ID);
		pi.setAD_PInstance_ID(instance.getAD_PInstance_ID());
		ProcessInfoParameter piOrgId = new ProcessInfoParameter(
				"AD_Org_ID", new BigDecimal(mOrder.getAD_Org_ID()), null,
				null, null);
		ProcessInfoParameter piClientId = new ProcessInfoParameter(
				"AD_Client_ID", new BigDecimal(mOrder.getAD_Client_ID()),
				null, null, null);
		ProcessInfoParameter piInvoiceId = new ProcessInfoParameter(
				"RECORD_ID", mOrder.getC_Order_ID(), null, null, null);
		ProcessInfoParameter[] piParameters = new ProcessInfoParameter[] {
				piClientId, piOrgId, piInvoiceId };
		pi.setParameter(piParameters);
		pi.setRecord_ID(mOrder.getC_Order_ID()	);

		ReportStarterDazzle rs = new ReportStarterDazzle();
		rs.startProcess(Env.getCtx(), pi, null);
		//FDialog.info(form.getWindowNo(), form,"Purchase Order Created - DocumentNo : " + mOrder.getDocumentNo());
		intQuantity.setFocus(true);
		prodIDs.clear();
		txtCustomerRefNo.setValue(null);
		txtComment.setValue(null);
		final Date currentDate = new Date();
		dateOrder.setValue(currentDate);





		return docno;

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
		Vector<Object> line = (Vector) data.get(event.getIndex0());
		Integer qty = null;
		if (line.get(2) instanceof Integer) {
			qty = ((Integer) line.get(2));
		} else {
			qty = ((BigDecimal) line.get(2)).intValue();
		}
		BigDecimal unitPrice = ((BigDecimal) line.get(3));
		BigDecimal rsp = unitPrice.multiply(new BigDecimal(qty));
		rsp = rsp.setScale(2, BigDecimal.ROUND_HALF_UP);
		line.set(4, rsp);
		int idx = data.indexOf(line);
		data.set(idx, line);
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
