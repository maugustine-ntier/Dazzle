package org.adempiere.webui.apps.form;

//package org.adempiere.webui.apps.form;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;

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
import org.compiere.model.I_C_POS;
import org.compiere.model.MAcctSchema;
import org.compiere.model.MClientInfo;
import org.compiere.model.MLocator;
import org.compiere.model.MMovement;
import org.compiere.model.MPInstance;
import org.compiere.model.MPOS;
import org.compiere.model.MProcess;
import org.compiere.model.MProduct;
import org.compiere.model.MStorageOnHand;
import org.compiere.model.MWarehouse;
import org.compiere.model.ProductCost;
import org.compiere.model.Query;
import org.compiere.process.ProcessInfoParameter;
import org.compiere.report.ReportStarterDazzle;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.eevolution.pos.VDocument;
import org.zkoss.zhtml.Textarea;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zul.Center;
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
public class WDazzleMovement extends WDazzleBase implements IFormController, EventListener<Event>, WTableModelListener,
		ValueChangeListener, TableValueChangeListener {
	private static final long serialVersionUID = 1L;
	private final Properties m_ctx = Env.getCtx();
	private final Vector<Object> data = new Vector<Object>();
	private ListModelTable model = new ListModelTable(data);
	private final Vector<String> columnNames = new Vector<String>();
	private final Vector<Integer> columnWidths = new Vector<Integer>();
	private final WListboxMA resultsTable = ListboxFactoryMA.newDataTable();

	private final Borderlayout mainLayout = new Borderlayout();
	private final Panel topPanel = new Panel();
	private final Grid topLayout = GridFactory.newGridLayout();
	private final Grid bottomLayout = GridFactory.newGridLayout();
	// private Grid resultLayout = GridFactory.newGridLayout();
	private final Borderlayout resultLayout = new Borderlayout();
	private final Panel centerPanel = new Panel();
	private final Panel bottomPanel = new Panel();
	private final CustomForm form = new CustomForm();

	private static CLogger log = CLogger.getCLogger(WDazzleMovement.class);

	private final NumberBox nbrQuantity = new NumberBox(false);
	private final Label lblQuantity = new Label("Quantity");

	private final ComboboxNew cmbProduct = new ComboboxNew();
	private final Label lblProduct = new Label("Product");

	private final ComboboxNew cmbWarehouseFrom = new ComboboxNew();
	private final Label lblWarehoueFrom = new Label("Warehouse From");

	private final ComboboxNew cmbWarehouseTo = new ComboboxNew();
	private final Label lblWarehoueTo = new Label("Warehouse To");

	private final Label lblComment = new Label("Comment");
	private final Textarea txtComment = new Textarea();
	private final Label lblDate = new Label("Movement Date");
	private final Datebox dateMovement = new Datebox();

	private final Label lblToLocator = new Label("To Locator");
	private final ComboboxNew cmbToLocator = new ComboboxNew();

	private MPOS m_pos = null;

	private boolean newProduct = false;

	private int prod_ID_sel = 0;
	private int warehouseFrom_ID_sel = 0;
	private int warehouseTo_ID_sel = 0;
	private int locatorTo_ID_sel = 0;
	private int locatorTo_ID_default = 0;
	private int userSelectedLocator_ID = 0;
	private boolean onlyOneLocator = false;

	private final int[] prod_id = null;
	private Integer index = 0;

	private final ArrayList<Integer> prodIDs = new ArrayList<Integer>();
	private final ArrayList<Integer> locatorToIDs = new ArrayList<Integer>();

	private final Label lblTotalLines = new Label("Total Quantity");
	private final Textbox txtTotalLines = new Textbox();

	private final Button btnCreate = new Button("Complete Order");
	private final Button btnCancel = new Button("Cancel");
	private final Button btnBack = new Button("Back");

	private final Button F6 = new Button("F6 - Clear");
	private final Button F7 = new Button("F7 - Delete Line");
	private final Button F8 = new Button("F8 - Checkout");
	
	protected Label lblHelp = new Label("F7 - Delete line, F8 - Checkout");
	
	private boolean back = false;


	@Override
	protected void initForm() {

		try {
			if (!setMPOS()) {
				FDialog.error(form.getWindowNo(), "No Pos Terminal for this Branch");
				return;
			}
			zkInit();
		} catch (final Exception e) {
			log.log(Level.SEVERE, "", e);
		}

	}

	private void zkInit() {
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

		final North north = new North();
		//north.setFlex(true);

		mainLayout.appendChild(north);
		//topPanel.setStyle("background:transparent;");
		north.appendChild(topPanel);

		final Center center = new Center();
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

		final South south = new South();
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

		if (!txtComment.isListenerAvailable(Events.ON_CHANGE, false)) {
			txtComment.addEventListener(Events.ON_CHANGE, new org.zkoss.zk.ui.event.EventListener() {
				@Override
				public void onEvent(final org.zkoss.zk.ui.event.Event event) throws Exception {
					if (txtComment.getValue() != null) {

					}
				}
			});
		}

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
		dateMovement.setValue(currentDate);
		row.appendChild(dateMovement);

		row = rows.newRow();
		row.setStyle("border: none; background:transparent;");
		row.setSpans("1,3,1");
		lblWarehoueFrom.setStyle("background:transparent;");
		row.appendChild(lblWarehoueFrom);
		cmbWarehouseFrom.setStyle("background:transparent;");
		cmbWarehouseFrom.setWidth("600px");
		row.appendChild(cmbWarehouseFrom);

		row = rows.newRow();
		row.setStyle("border: none; background:transparent;");
		row.setSpans("1,3,1");
		lblWarehoueTo.setStyle("background:transparent;");
		row.appendChild(lblWarehoueTo);
		cmbWarehouseTo.setStyle("background:transparent;");
		cmbWarehouseTo.setWidth("600px");
		row.appendChild(cmbWarehouseTo);

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
		row.appendCellChild(lblToLocator);
		row.appendCellChild(cmbToLocator);




		lblQuantity.setStyle("padding-left:60px");
		row.appendChild(lblQuantity);
		nbrQuantity.setTooltiptext("Enter quantity");
		nbrQuantity.setStyle("background:transparent;");
		row.appendChild(nbrQuantity);

		// End of Row 3
		//   Bottom Panel
		Rows rows2 = null;
		Row row2 = null;
		rows2 = bottomLayout.newRows();

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

		final String sql = "select p.value || ' | ' ||  p.description as name,p.M_product_id as available "
				+ " from M_Product p" + " where (upper(p.value)) like ?" + " and p.ad_client_ID = "
				+ Env.getAD_Client_ID(m_ctx) + " and p.description  is not null" + " order by p.value";

		// sqlProd = sql;
		final SimpleListModelNew dictModel = new SimpleListModelNew();
		dictModel.setCbox(cmbProduct);
		cmbProduct.setModel(dictModel);
		cmbProduct.setQueryName(sql);
		cmbProduct.setAutodrop(true);
		//cmbProduct.setFocus(true);

		if (!cmbProduct.isListenerAvailable(Events.ON_CHANGE, false)) {
			cmbProduct.addEventListener(Events.ON_CHANGE, new org.zkoss.zk.ui.event.EventListener() {
				@Override
				public void onEvent(final org.zkoss.zk.ui.event.Event event) throws Exception {
					if (cmbProduct.getSelectedItem() == null || cmbProduct.getSelectedItem().getValue() == null) {
						// FDialog.error(form.getWindowNo(),
						//		 "The Product Code for the selected product does not exist");
						//cmbProduct.setFocus(true);
						prod_ID_sel = 0;
						throw new WrongValueException(cmbProduct, "Please select a product");
						//return;
					}
					prod_ID_sel = ((WObjRet) cmbProduct.getSelectedItem().getValue()).getIDOfRecord();
					newProduct = true;
					if (onlyOneLocator) {
						locatorTo_ID_sel = locatorTo_ID_default;
						nbrQuantity.setFocus(true);
					} else {
						if (cmbProduct.getSelectedItem() != null) {
							if (warehouseFrom_ID_sel > 0) {
								int M_LocatorTo_ID = MStorageOnHand.getM_Locator_ID (warehouseTo_ID_sel,prod_ID_sel, 0,
									new BigDecimal("1"), null);
								if (M_LocatorTo_ID > 0) {
									locatorTo_ID_sel = M_LocatorTo_ID;
									nbrQuantity.setFocus(true);
								} else {
									locatorTo_ID_sel = userSelectedLocator_ID;
									cmbToLocator.setFocus(true);
								}
							} else {
								locatorTo_ID_sel = userSelectedLocator_ID;
								cmbToLocator.setFocus(true);
							}
						}
					}
					//End

				}
			});
		}



		final String sql2 = "select w.name ,w.m_warehouse_id " + "from adempiere.m_warehouse w "
				+ "where w.ad_client_id = " + Env.getAD_Client_ID(m_ctx) + " and w.name like ?";
		final SimpleListModelNew dictModel2 = new SimpleListModelNew();
		dictModel2.setCbox(cmbWarehouseFrom);
		cmbWarehouseFrom.setModel(dictModel2);
		cmbWarehouseFrom.setQueryName(sql2);
		cmbWarehouseFrom.setAutodrop(true);
		//cmbProduct.setFocus(true);

		if (!cmbWarehouseFrom.isListenerAvailable(Events.ON_CHANGE, false)) {
			cmbWarehouseFrom.addEventListener(Events.ON_CHANGE, new org.zkoss.zk.ui.event.EventListener() {
				@Override
				public void onEvent(final org.zkoss.zk.ui.event.Event event) throws Exception {
					if (cmbWarehouseFrom.getSelectedItem() == null
							|| cmbWarehouseFrom.getSelectedItem().getValue() == null) {
						// FDialog.error(form.getWindowNo(),
						//		 "The Product Code for the selected product does not exist");
						//cmbProduct.setFocus(true);
						warehouseFrom_ID_sel = 0;
						throw new WrongValueException(cmbWarehouseFrom, "Please select a from warehouse");
						//return;
					}
					warehouseFrom_ID_sel = ((WObjRet) cmbWarehouseFrom.getSelectedItem().getValue()).getIDOfRecord();

					if (cmbWarehouseFrom.getSelectedItem() != null) {
						cmbWarehouseTo.setFocus(true);
					}
					//End

				}
			});
		}

		final String sql3 = "select w.name ,w.m_warehouse_id " + "from adempiere.m_warehouse w "
				+ "where w.ad_client_id = " + Env.getAD_Client_ID(m_ctx) + " and w.name like ?";
		final SimpleListModelNew dictModel3 = new SimpleListModelNew();
		dictModel3.setCbox(cmbWarehouseTo);
		cmbWarehouseTo.setModel(dictModel3);
		cmbWarehouseTo.setQueryName(sql3);
		cmbWarehouseTo.setAutodrop(true);

		if (!cmbWarehouseTo.isListenerAvailable(Events.ON_CHANGE, false)) {
			cmbWarehouseTo.addEventListener(Events.ON_CHANGE, new org.zkoss.zk.ui.event.EventListener() {
				@Override
				public void onEvent(final org.zkoss.zk.ui.event.Event event) throws Exception {
					if (cmbWarehouseFrom.getSelectedItem() == null
							|| cmbWarehouseFrom.getSelectedItem().getValue() == null) {
						// FDialog.error(form.getWindowNo(),
						//		 "The Product Code for the selected product does not exist");
						//cmbProduct.setFocus(true);
						warehouseTo_ID_sel = 0;
						throw new WrongValueException(cmbWarehouseTo, "Please select a from warehouse");
						//return;
					}
					warehouseTo_ID_sel = ((WObjRet) cmbWarehouseTo.getSelectedItem().getValue()).getIDOfRecord();

					if (cmbWarehouseTo.getSelectedItem() != null) {
						setValuesForComboLoc();
						MWarehouse mWarehouseTo = MWarehouse.get(Env.getCtx(), warehouseTo_ID_sel);
						MLocator[] locs = mWarehouseTo.getLocators(true);
						if (locs == null || locs.length <= 1) {
							onlyOneLocator = true;
						} else {
							onlyOneLocator = false;
						}
						MLocator mLocatorTo = MLocator.getDefault(mWarehouseTo);  // get default locator
						locatorTo_ID_default = mLocatorTo.getM_Locator_ID();

						cmbProduct.setFocus(true);
					}
					//End

				}
			});
		}

		if (!cmbToLocator.isListenerAvailable(Events.ON_CHANGE, false)) {
			cmbToLocator.addEventListener(Events.ON_CHANGE, new org.zkoss.zk.ui.event.EventListener() {
				@Override
				public void onEvent(final org.zkoss.zk.ui.event.Event event) throws Exception {
					if (cmbToLocator.getSelectedItem() == null
							|| cmbToLocator.getSelectedItem().getValue() == null) {
						// FDialog.error(form.getWindowNo(),
						//		 "The Product Code for the selected product does not exist");
						//cmbProduct.setFocus(true);
						locatorTo_ID_sel = 0;
						throw new WrongValueException(cmbToLocator, "Please select a TO Locator");
						//return;
					}
					locatorTo_ID_sel = ((WObjRet) cmbToLocator.getSelectedItem().getValue()).getIDOfRecord();
					userSelectedLocator_ID = locatorTo_ID_sel;

					if (cmbToLocator.getSelectedItem() != null) {
						nbrQuantity.setFocus(true);
					}
					//End

				}
			});
		}


		/*if (!cmbProduct.isListenerAvailable(Events.ON_OK, false)) {
			cmbProduct.addEventListener(Events.ON_OK, new org.zkoss.zk.ui.event.EventListener() {
				@Override
				public void onEvent(final org.zkoss.zk.ui.event.Event event) throws Exception {
					if (cmbProduct.getSelectedItem() == null || cmbProduct.getSelectedItem().getValue() == null) {
						prod_ID_sel = prev_prod_ID_sel;
						newProduct = true;
						nbrQuantity.setFocus(true);
					} else {
						if (prod_ID_sel == 0) {
							prod_ID_sel = ((WObjRet) cmbProduct.getSelectedItem().getValue()).getIDOfRecord();
							prev_prod_ID_sel = prod_ID_sel;
							newProduct = true;
							nbrQuantity.setFocus(true);

						}
					}

				}
			});
		}
		*/
		if (!nbrQuantity.isListenerAvailable(Events.ON_OK, false)) {
			nbrQuantity.addEventListener(Events.ON_OK, new org.zkoss.zk.ui.event.EventListener() {
				@Override
				public void onEvent(final org.zkoss.zk.ui.event.Event event) throws Exception {
					if (newProduct) {
						EnterPressedOnQty();
						newProduct = false;

					} else {
						if (prod_ID_sel == 0) { //|| cmbProduct.getValue() == null) {
							cmbProduct.setFocus(true);
							throw new WrongValueException(cmbProduct, "Please select a product");
						}
					}
				}
			});
		}

		if (!F7.isListenerAvailable(Events.ON_CLICK, false)) {
			F7.addEventListener(Events.ON_CLICK, new org.zkoss.zk.ui.event.EventListener() {
				@Override
				public void onEvent(final org.zkoss.zk.ui.event.Event event) throws Exception {
					deleteLine();
					nbrQuantity.setFocus(true);
				}
			});
		}

		//Clients.response(new AuFocus(cmbCustomer));

		//cmbCustomer.setFocus(true);

	}

	private void deleteLine() {

		final int selInx = resultsTable.getSelectedIndex();
		if (selInx > -1) {
			new MProduct(Env.getCtx(), prodIDs.get(selInx - 1), null);

			prodIDs.remove(selInx - 1); // First line on grid blank
			locatorToIDs.remove(selInx - 1);
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

		if (prod_ID_sel == 0) { //|| cmbProduct.getValue() == null) {
			prod_ID_sel = 0;
			throw new WrongValueException(cmbProduct, "Please select a product");
		}
		if (locatorTo_ID_sel == 0) {
			throw new WrongValueException(cmbToLocator, "Please select a Locator");
		}

		if (nbrQuantity.getValue() == null || nbrQuantity.getValue().compareTo(Env.ZERO) <= 0) {
			throw new WrongValueException(nbrQuantity, "Please enter a positive quantity");
		}

		new MProduct(m_ctx, prod_ID_sel, null);

		productLoad(prod_id, prod_ID_sel,locatorTo_ID_sel);

		nbrQuantity.setValue((BigDecimal) null);
		cmbProduct.setValue("");
		prod_ID_sel = 0;
		cmbToLocator.setValue("");
		//locatorTo_ID_sel = 0;
		// End
		//return cmbProduct;
		cmbProduct.setFocus(true);

	}

	private void calculateTotsBottomPanel() {
		final BigDecimal calcTot = calculateTotal(model.toArray(), 3, false, prodIDs);
		txtTotalLines.setValue(calcTot.toString());

	}

	private BigDecimal calculateTotal(final Object[] lmt, final int col, final boolean otherItems,
			final ArrayList<Integer> PprodIDs) {
		BigDecimal tot = Env.ZERO;
		final int rowCnt = lmt.length;
		for (int i = 0; i < rowCnt; i++) {
			if (((Vector) lmt[i]).get(0) != null) {
				final Integer prodID = PprodIDs.get(i - 1); // we store this
				//new X_M_Product(m_ctx, prodID, null);
				final BigDecimal totalLine = (BigDecimal) ((Vector) lmt[i]).get(col);
				tot = tot.add(totalLine);

			}
		}

		tot = tot.setScale(2, BigDecimal.ROUND_HALF_UP);

		return tot;
	}

	public void productLoad(final int[] prodArr, final int Pprod_ID_sel, final int PlocatorTo_ID_sel) {

		if (Pprod_ID_sel != 0) {
			// Creating the table grid
			final MProduct product = new MProduct(Env.getCtx(), Pprod_ID_sel, null);

			final BigDecimal qty = nbrQuantity.getValue();
			{

				prodIDs.add(new Integer(product.getM_Product_ID()));
				locatorToIDs.add(PlocatorTo_ID_sel);
				index = new Integer(index.intValue() + 10);
				new BigDecimal("0.00");
				final Vector<Object> line = new Vector<Object>();
				line.add(index);
				line.add(product.getValue() + "-" + product.getDescription());
				MLocator toLoc = new MLocator(m_ctx, PlocatorTo_ID_sel, null);
				line.add(toLoc.getValue());
				line.add(qty);
				MClientInfo m_clientInfo = MClientInfo.get(Env.getCtx(), Env.getAD_Client_ID(Env.getCtx()), null);
				MAcctSchema as = new MAcctSchema(Env.getCtx(), m_clientInfo.getC_AcctSchema1_ID(), null);
				ProductCost m_productCost = new ProductCost (Env.getCtx(), product.getM_Product_ID(), 0, null);
				m_productCost.setQty(BigDecimal.ONE);
				BigDecimal costs = m_productCost.getProductCosts(as, Env.getAD_Org_ID(Env.getCtx()), null, 0, false);
				line.add(costs);
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

	public void headerload() {
		columnNames.clear();
		columnNames.add("Index");
		columnNames.add("Description");
		columnNames.add("To Locator");
		columnNames.add("Quantity");
		columnNames.add("Product Cost");

		columnWidths.clear();
		columnWidths.add(new Integer(60));
		columnWidths.add(new Integer(700));
		columnWidths.add(new Integer(100));
		columnWidths.add(new Integer(100));
		columnWidths.add(new Integer(100));

		final Vector<Object> line = new Vector<Object>();
		data.clear();
		data.add(line);

		model = new ListModelTable(data);
		model.addTableModelListener(this);
		resultsTable.setData(model, columnNames, columnWidths);
		setLineColumnClass(resultsTable);

	}

	public void setLineColumnClass(final IMiniTable lineTable) {
		int i = 0;
		lineTable.setColumnClass(i++, Integer.class, true); // 0 seq
		lineTable.setColumnClass(i++, String.class, true); // 1-Description
		lineTable.setColumnClass(i++, String.class, true); // 2-To Locator
		lineTable.setColumnClass(i++, Integer.class, false); // 3-Quantity
		lineTable.setColumnClass(i++, BigDecimal.class, true); // 4- Cost
		// Table UI
		lineTable.autoSize();
	}

	private boolean setMPOS() {
		// Martin added so that the terminal assigned to a particular user is
		// selected

		final int userID = Env.getAD_User_ID(m_ctx);
		String whereClause = " AD_Org_ID = ? and SalesRep_ID = ? ";
		m_pos = new Query(m_ctx, I_C_POS.Table_Name, whereClause, null).setParameters(
				new Object[] { Env.getAD_Org_ID(m_ctx), userID }).firstOnly();

		if (m_pos == null) {
			// VUtil.setMsg("Can not find the POS Terminal for this user",true);//TODO
			// create AD Message
			whereClause = " AD_Org_ID = ?  ";
			m_pos = new Query(m_ctx, I_C_POS.Table_Name, whereClause, null).setParameters(
					new Object[] { Env.getAD_Org_ID(m_ctx) }).firstOnly();
			if (m_pos == null) {
				return false;
			}
		}

		return true;
	}

	public void onCtrlKey(final KeyEvent event) throws Exception {
		final int keyCode = event.getKeyCode();
		if (keyCode == KeyEvent.F8) {
			createMovement();
			clearBottomScreen();
			clearScreen();
			zkInit();

		} else if (keyCode == KeyEvent.F6) {
			clearScreen();
		} else if (keyCode == KeyEvent.F7) {
			deleteLine();
			nbrQuantity.setFocus(true);
		} else if (keyCode == KeyEvent.F9) {

			/*
			 * WLocationDialog ld = new WLocationDialog(Msg.getMsg(Env.getCtx(),
			 * "Location"), null); ld.setVisible(true); AEnv.showWindow(ld);
			 * MLocation m_value = ld.getValue();
			 */
		}

	}

	private void setValuesForComboLoc() {
		final String sql6 = "Select l.Value , l.M_Locator_ID FROM M_Locator l WHERE IsActive = 'Y' "
				+ " and l.ad_client_id = " + Env.getAD_Client_ID(m_ctx)
				+ " and l.M_Warehouse_ID = " + warehouseTo_ID_sel
				+ " and upper(l.value) like ?";
		final SimpleListModelNew dictModel6 = new SimpleListModelNew();
		dictModel6.setCbox(cmbToLocator);
		cmbToLocator.setModel(dictModel6);
		cmbToLocator.setQueryName(sql6);
		cmbToLocator.setAutodrop(true);
	}

	private void clearBottomScreen() {
		txtTotalLines.setValue("0.00");
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

	private String createMovement() {

		final Object[] objs = model.toArray();
		final VDocument doc = new VDocument(false);
		doc.setMovementDate(new Timestamp(dateMovement.getValue().getTime()));
		doc.setComment(txtComment.getValue());
		final String docno = null;

		final MMovement mMovement = doc.createMovementLines(objs, prodIDs, locatorToIDs,warehouseFrom_ID_sel, warehouseTo_ID_sel);
		final int m_AD_Process_ID = MProcess.getProcess_ID("POS_Print_Movement", null);

		final MPInstance instance = new MPInstance(Env.getCtx(), m_AD_Process_ID, 0);
		if (!instance.save()) {
			return docno;
		}
		// call process
		final org.compiere.process.ProcessInfo pi = new org.compiere.process.ProcessInfo("Movement", m_AD_Process_ID);
		pi.setAD_PInstance_ID(instance.getAD_PInstance_ID());
		final ProcessInfoParameter piOrgId = new ProcessInfoParameter("AD_Org_ID", new BigDecimal(
				mMovement.getAD_Org_ID()), null, null, null);
		final ProcessInfoParameter piClientId = new ProcessInfoParameter("AD_Client_ID", new BigDecimal(
				mMovement.getAD_Client_ID()), null, null, null);
		final ProcessInfoParameter piInvoiceId = new ProcessInfoParameter("RECORD_ID", mMovement.getM_Movement_ID(),
				null, null, null);
		final ProcessInfoParameter[] piParameters = new ProcessInfoParameter[] { piClientId, piOrgId, piInvoiceId };
		pi.setParameter(piParameters);
		pi.setRecord_ID(mMovement.getM_Movement_ID());

		final ReportStarterDazzle rs = new ReportStarterDazzle();
		rs.startProcess(Env.getCtx(), pi, null);
		//FDialog.info(form.getWindowNo(), form,"Purchase Order Created - DocumentNo : " + mOrder.getDocumentNo());
		nbrQuantity.setFocus(true);
		prodIDs.clear();
		locatorToIDs.clear();
		onlyOneLocator = false;
		userSelectedLocator_ID = 0;
		txtComment.setValue(null);

		return docno;

	}

	@Override
	public void valueChange(final ValueChangeEvent evt) {
		// TODO Auto-generated method stub
		System.out.println("aaa");

	}

	@Override
	public void tableChanged(final WTableModelEvent event) {
		// TODO Auto-generated method stub
		System.out.println("aaa");
		final Vector<Object> line = (Vector) data.get(event.getIndex0());
		Integer qty = null;
		if (line.get(3) instanceof Integer) {
			qty = ((Integer) line.get(3));
		} else {
			qty = ((BigDecimal) line.get(3)).intValue();
		}
		/*final BigDecimal unitPrice = ((BigDecimal) line.get(4));
		BigDecimal rsp = unitPrice.multiply(new BigDecimal(qty));
		rsp = rsp.setScale(2, BigDecimal.ROUND_HALF_UP);
		line.set(4, rsp);
		final int idx = data.indexOf(line);
		data.set(idx, line);*/
		calculateTotsBottomPanel();

	}

	@Override
	public void onEvent(final Event event) throws Exception {
		// TODO Auto-generated method stub
		System.out.println("aaa");

	}

	@Override
	public ADForm getForm() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void tableValueChange(final TableValueChangeEvent event) {
		// TODO Auto-generated method stub

	}

}
