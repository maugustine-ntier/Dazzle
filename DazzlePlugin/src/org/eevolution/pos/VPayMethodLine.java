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
import java.math.MathContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.compiere.model.MBPartner;
import org.compiere.model.MBank;
import org.compiere.model.MBankAccount;
import org.compiere.model.MCash;
import org.compiere.model.MCashLine;
import org.compiere.model.MInvoice;
import org.compiere.model.MOrderLine;
import org.compiere.model.MPOS;
import org.compiere.model.MPayment;
import org.compiere.model.MTax;
import org.compiere.model.Query;
import org.compiere.process.DocAction;
import org.compiere.util.DB;
import org.compiere.util.Env;
import org.compiere.util.Msg;
//import org.eevolution.pos.net.DBServerConnection;
//import org.eevolution.pos.net.VCardResponse;
//import org.eevolution.pos.net.VCardValidator;


/**
 *
 * @author antonio.canaveral@e-evolution.com, www.e-evolution.com
 * @author victor.perez@e-evolution.com, www.e-evolution.com
 */
public class VPayMethodLine {

	// Martin added
	private int C_Invoice_ID = 0;

	private int MPayment_ID = 0;
	private int lineNumber;
	private String method = "";
	private String methodDesc = "";
	private BigDecimal ammount = BigDecimal.ZERO;
	private int type = 99;
	private boolean validated;
	private int currency;
	private boolean isvalid;
	// /////CHEQUE////////////
	private Timestamp checkDate;
	private String bankName;
	private int bankId;
	private String accountNumber;
	private String checkNumber;
	private String checkAutori;
	// //////TARJETA///////////
	private boolean blnaproved = false;
	private int cardEnterType;
	private String cardPayTerm;
	private String cardNumber;
	private String cardPing = "";
	private String cardUserName;
	private String cardDate;
	private String cardTracks;
	private String cardTrack = "";
	private String cardAutorization = "0";
	private String cardNameTmp = "";
	private boolean cardResponse;
	private String cardMsj = "";
	private String cardoperation;
	private MPayment payment;
	private String creditNote;
	private MInvoice cnote;
	private String tvcard;
	private String transaction = "";

	private String credittype = "00000000";
	private String payform = "01";
	private String processCode = CODE_AUT;
	private String processCodeLocal = "3000";
	private String hostIp = null;
	private int hostPort = 0;
	private int C_BankAccount_ID = 0;
	private String cctDescription;
	private int PO_PaymentTerm_ID = 0;
	private String numberOfPay;
	private BigDecimal minimumamt;
	private int C_CreditCardTerm_ID;
	private boolean isCredit;
	private int TV_cardLoggerResponse_ID;
	private int M_Shipper_ID = 0;
	private MPOS m_pos = null;
	private boolean isManualCard = false;

	private boolean isForCredit = false;

	private boolean isCardName = false;

	public final static String CODE_AUT = "00";
	public final static String CODE_DEL = "20";

	public final static int CARD_LECTOR = 1;
	public final static int CARD_MANUAL = 2;

	public final static int M_CASH = 0;
	public final static int M_CREDIT_CARD = 1;
	public final static int M_CREDIT_NOTE = 2;
	public final static int M_CHECK = 3;
	public final static int M_OTHER = 4;
	public final static int M_TRANSFER = 5;

	public final static int CURR_USD = 2;
	public final static int CURR_BONOS = 2;
	public final static int CURR_MILLES = 2;

	public String TENDERTYPE_Other;
	// Martin commented
	//public final static String TENDERTYPE_Cash = "B";
	public final static String TENDERTYPE_Cash = "X";  // Martin changed
	public final static String TENDERTYPE_CreditCard = "C";
	public final static String TENDERTYPE_Check = "K";
	public final static String TENDERTYPE_CreditNote = "N";
	public final static String TENDERTYPE_Transfer = "A";

	public final static String TENDERFRONTTYPE_Other = "O";
	public final static String TENDERFRONTTYPE_Transfer = "A";
	//public final static String TENDERFRONTTYPE_Cash = "E";
	public final static String TENDERFRONTTYPE_Cash = "C";
	public final static String TENDERFRONTTYPE_CreditCard = "T";
	//public final static String TENDERFRONTTYPE_ManualCreditCard = "M";
	public final static String TENDERFRONTTYPE_ManualCreditCard = "V";
	//public final static String TENDERFRONTTYPE_Check = "C";
	public final static String TENDERFRONTTYPE_Check = "Q";
	public final static String TENDERFRONTTYPE_CreditNote = "N";

	public VPayMethodLine(int line, MPOS pos) {
		m_pos = pos;
		lineNumber = line;
	}

	/*
	 * public VPayMethodLine(String _method,BigDecimal _ammount,int line) {
	 * method=_method; ammount=_ammount; lineNumber=line; setMethod(_method); }
	 * public VPayMethodLine(String _method,int line) { method=_method;
	 * lineNumber=line; setMethod(_method); }
	 */

	public boolean getIsCompleted() {
		if (MPayment_ID > 0) {
			if (payment != null) {
				if (payment.getDocStatus().equals(MPayment.DOCSTATUS_Completed))
					return true;
				else
					return false;
			} else {
				payment = new MPayment(Env.getCtx(), MPayment_ID, null);
				return getIsCompleted();
			}
		}
		return false;
	}

	public void setID(int id) {
		MPayment_ID = id;
	}

	public boolean getIsForCredit() {
		return isForCredit;
	}

	/*
	 * public static VPayMethodLine getMethodLine(String code) { return new
	 * VPayMethodLine(code); } public static VPayMethodLine getMethodLine(String
	 * code, String _ammount) { return new VPayMethodLine(code,_ammount); }
	 */
	public void setCreditNote(String note) {
		creditNote = note;
	}

	public String getCreditNote() {
		return creditNote;
	}

	public MPayment getMPayment() {
		return payment;
	}

	public void setTVcard(String number) {
		tvcard = number;
	}

	public MInvoice getNote() {
		return cnote;
	}

	public int getCurrency() {
		return currency;
	}

	public void setCurrency(int curr) {
		currency = curr;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public boolean getIsValid() {
		return isvalid;
	}

	public void setIsValid(boolean val) {
		isvalid = val;
	}

	// ////////CHEQUE///////////////
	public String getBankName() {
		return bankName;
	}

	public void setBankName(String value) {
		bankName = value;
	}
	public int getBankId() {
		return bankId;
	}

	public void setBankId(int value) {
		bankId = value;
	}
	public void setCheckDate(Timestamp date) {
		checkDate = date;
	}

	public String getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(String value) {
		accountNumber = value;
	}

	public String getCheckNumber() {
		return checkNumber;
	}

	public void setCheckNumber(String value) {
		checkNumber = value;
	}

	public String getCheckAutorization() {
		return checkAutori;
	}

	public void setCheckAutorization(String value) {
		checkAutori = value;
	}

	public void setTenderTypeOther(String tt) {
		TENDERTYPE_Other = tt;
		method = TENDERTYPE_Other;
	}

	public boolean isMethodValid() {
		if (type != 99)
			return true;

		return false;
	}

	private boolean validateBank() {
		if (ammount.compareTo(BigDecimal.ZERO) > 0)
			validated = true;
		return validated;
	}

	private boolean validateChek() {
		if (ammount.compareTo(BigDecimal.ZERO) > 0)
			validated = true;
		return validated;
	}

	// ////////CREDIT-CARD////////////
	public void setCardLoggerResponse_ID(int id) {
		TV_cardLoggerResponse_ID = id;
	}

	public void setCardOperation(String operation) {
		cardoperation = operation;
	}

	public void setCardEnterType(int value) {
		cardEnterType = value;
	}

	public int getCardEnterType() {
		return cardEnterType;
	}

	public String getCardMesage() {
		return cardMsj;
	}

	public void setCardPayTerm(String value) {
		cardPayTerm = value;
	}

	public String getCardPayTerm() {
		return cardPayTerm;
	}

	public void setCardNumber(String value) {
		cardNumber = value;
	}

	public String getCardNumber() {
		return cardNumber;
	}

	public void setCardPin(String value) {
		cardPing = value;
	}

	public boolean getCardResponse() {
		return cardResponse;
	}

	public String getCardPin() {
		return cardPing;
	}

	public void setCardUserName(String value) {
		cardUserName = value;
	}

	public void setCardDate(String value) {
		cardDate = value;
	}

	public String getCardUserName() {
		return cardUserName;
	}

	public void setCardTracks(String value) {
		cardTracks = value;
	}

	public String getCardTracks() {
		return cardTracks;
	}

	public String getCargAutorization() {
		return cardAutorization;
	}

	public void setCardAutorization(String Auth) {
		cardAutorization = Auth;
	}

	public String getIP() {
		return hostIp;
	}

	public int getPort() {
		return hostPort;
	}

	public void setM_Shipper_ID(int ship) {
		M_Shipper_ID = ship;
	}

	public int getC_CreditCardTerm_ID() {
		return C_CreditCardTerm_ID;
	}

	public void setC_CreditCardTerm_ID(int value) {
		C_CreditCardTerm_ID = value;
	}

	public void setCardApproved(boolean aproved) {
		blnaproved = aproved;
	}

	public boolean getCardApproved() {
		return blnaproved;
	}

	public void setProcessCode(String code) {
		processCode = code;
	}

	public void getNextTransaction() {
		transaction = "";
		// Martin added the replaceall to cater for spaces in the name - postgres cannot handle spaces
		try {
			String sql = "select nextval('adempiere.eevolution_transaction_"
					+ m_pos.getName().replaceAll(" ","") + "');";
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				transaction = rs.getString(1);
			}
			DB.close(rs, pstmt);
		} catch (Exception ex) {
			String sqlSec = "CREATE SEQUENCE eevolution_transaction_"
					+ m_pos.getName().replaceAll(" ","") + " INCREMENT 1" + " START 100000";
			try {
				DB.executeUpdate(sqlSec);
				getNextTransaction();
			} catch (Exception e) {
				Logger.getLogger(VBPartner.class.getName()).log(Level.SEVERE,
						null, ex);
			}

		}
		// return transaction;
	}

	public void DeleteTransaction() {
		getNextTransaction();
	}

	public String getProcessCodeLocal() {
		return processCodeLocal;
	}




	public boolean setHostPort() {
		boolean res = false;
		String sql = "SELECT PP.hostaddress,PP.hostport"
				+ " FROM C_CREDITCARDTERM CCT"
				+ " INNER JOIN C_CREDITCARD CC ON CC.C_CREDITCARD_ID=CCT.C_CREDITCARD_ID"
				+ " INNER JOIN C_BANKACCOUNT BA ON BA.C_CREDITCARD_ID=CC.C_CREDITCARD_ID"
				+ " INNER JOIN C_PAYMENTPROCESSOR PP ON PP.C_PAYMENTPROCESSOR_ID=BA.C_PAYMENTPROCESSOR_ID"
				+ " WHERE C_CREDITCARDTERM_ID= ? ";

		PreparedStatement pstmt = DB.prepareStatement(sql, null);
		try {
			pstmt.setInt(1, (Integer) getMPayment().get_Value(
					"C_CREDITCARDTERM_ID"));
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
				hostIp = rs.getString("hostaddress");
				hostPort = rs.getInt("hostport");
				res = true;
			}
			DB.close(rs, pstmt);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}




	public void Validate() {
		switch (type) {
		case M_CASH:
			isvalid = true;
			break;
		case M_CREDIT_CARD:
			isvalid = false;
			break;
		case M_CHECK:
			isvalid = validateChek();
			break;
		case M_OTHER:
			isvalid = validateBank();
			break;
		default:
			isvalid = false;
			break;
		}
	}

	public boolean presendValidation() {
		if (ammount == null || minimumamt == null)
			return false;
		if (ammount.compareTo(minimumamt) >= 0)
			return true;
		else
			return false;
	}

	public BigDecimal getPreconfigureValue(VDocument curdoc, String products) {
		BigDecimal res = BigDecimal.ZERO;
		try {
			String sql = "SELECT M_PRODUCT_ID, C_PAYMENTTERM_ID"
					+ " FROM C_CREDITCARDTERM CCT"
					+ " INNER JOIN TV_TERMPRODUCT TP ON CCT.C_CREDITCARDTERM_ID = TP.c_creditcardterm_id"
					+ " WHERE TP.ISACTIVE='Y' AND CCT.ISINCLUDE='N'"
					+ " AND M_PRODUCT_ID IN (" + products + ") AND CCT.VALUE='"
					+ cardPayTerm + "'"
					+ " ORDER BY C_PAYMENTTERM_ID,M_PRODUCT_ID";
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			System.err.println("VALIDACION : " + sql);
			ResultSet rs = pstmt.executeQuery();
			List<Integer> validProducts = new ArrayList<Integer>();
			if (rs.next()) {
				if (PO_PaymentTerm_ID == rs.getInt("C_Paymentterm_ID")) {
					validProducts.add(rs.getInt("M_Product_ID"));
				}
			}
			DB.close(rs, pstmt);
			for (MOrderLine line : curdoc.getOrder().getLines()) {
				if (validProducts.contains(line.getM_Product_ID())) {
					MTax taza = MTax.get(Env.getCtx(), line.getC_Tax_ID());
					res = res.add(line.getLineNetAmt().add(
							taza.getRate().multiply(line.getLineNetAmt())
									.divide(new BigDecimal(100))).setScale(2,
							BigDecimal.ROUND_HALF_UP));
				}
			}

		} catch (SQLException ex) {
			Logger.getLogger(VBPartner.class.getName()).log(Level.SEVERE, null,
					ex);
		}
		return res;
	}

	public boolean isValidType() {
		String bins = cardNumber.substring(0, 6);
		try {
			String sql = "SELECT CCT.C_CreditCardTerm_ID,CCT.CODE,CCT.NAME,CCT.po_paymentterm_id,CCT.description,IsCreditApproved,"
					+ " CCT.value,PP.hostaddress,PP.HOSTPORT,BA.C_BANKACCOUNT_ID,CCT.minimumamt,CC.description as processCodeLocal"
					+ " FROM C_BINLINE BL"
					+ " INNER JOIN C_BIN B ON B.C_BIN_ID=BL.C_BIN_ID"
					+ " INNER JOIN C_CREDITCARDTERM CCT ON CCT.C_BIN_ID=B.C_BIN_ID"
					+ " INNER JOIN C_CREDITCARD CC ON CC.C_CREDITCARD_ID=CCT.C_CREDITCARD_ID"
					+ " INNER JOIN C_BANKACCOUNT BA ON BA.C_CREDITCARD_ID=CC.C_CREDITCARD_ID"
					+ " INNER JOIN C_PAYMENTPROCESSOR PP ON PP.C_PAYMENTPROCESSOR_ID=BA.C_PAYMENTPROCESSOR_ID"
					+ " WHERE "
					+ bins
					+ " BETWEEN BL.IDRANGESTART AND BL.IDRANGEEND AND CCT.VALUE='"
					+ cardPayTerm
					+ "'"
					+ " AND BL.ISACTIVE='Y' AND B.ISACTIVE='Y' AND CCT.ISACTIVE='Y' AND CC.ISACTIVE='Y'"
					+ " AND BA.ISACTIVE='Y' AND PP.ISACTIVE='Y'";
			PreparedStatement pstmt = DB.prepareStatement(sql, null);
			ResultSet rs = pstmt.executeQuery();
			System.err.println("BINES : " + sql);
			if (rs.next()) {
				payform = rs.getString("NAME");
				credittype = rs.getString("CODE");
				hostIp = rs.getString("hostaddress");
				hostPort = rs.getInt("HOSTPORT");
				C_BankAccount_ID = rs.getInt("C_BankAccount_ID");
				minimumamt = rs.getBigDecimal("minimumamt");
				C_CreditCardTerm_ID = rs.getInt("C_CreditCardTerm_ID");
				processCodeLocal = rs.getString("processCodeLocal");
				isCredit = rs.getString("IsCreditApproved").equals("Y") ? true
						: false;
				//
				PO_PaymentTerm_ID = rs.getInt("PO_PaymentTerm_ID");
				cctDescription = rs.getString("Description");
				numberOfPay = rs.getString("Value");
				return true;
			}
			DB.close(rs, pstmt);
		} catch (SQLException ex) {
			Logger.getLogger(VBPartner.class.getName()).log(Level.SEVERE, null,
					ex);
		}
		return false;
	}

	public void setInput(char _input) {
		cardTrack += _input;
		if (_input == '&') {
			isCardName = !isCardName;
			if (cardNameTmp.length() != 0)
				cardUserName = cardNameTmp.replace("&", "");
		}
		if (isCardName)
			cardNameTmp += _input;
	}

	public void resetCard() {
		cardNameTmp = "";
		cardUserName = null;
		cardTrack = "";
		isCardName = false;
	}

	public void setMethodDesc(String desc) {
		methodDesc = desc;
	}

	public String getMethodDesc() {
		return methodDesc;
	}

	public int getTypeOfMethod() {
		return type;
	}

	public String getMethod() {
		return method;
	}

	public boolean getValidation() {
		return this.validated;
	}

	public void setValidation(boolean _valid) {
		this.validated = _valid;
	}

	public void setMethod(String value) {
		method = value;
		if (value.equals(TENDERFRONTTYPE_Check)) {
			type = M_CHECK;
			method = TENDERTYPE_Check;
		} else if (value.equals(TENDERFRONTTYPE_CreditCard)) {
			type = M_CREDIT_CARD;
			method = TENDERTYPE_CreditCard;
		} else if (value.equals(TENDERFRONTTYPE_ManualCreditCard)) {
			isManualCard = true;
			type = M_CREDIT_CARD;
			method = TENDERFRONTTYPE_ManualCreditCard;
		} else if (value.equals(TENDERFRONTTYPE_Cash)) {
			type = M_CASH;
			method = TENDERTYPE_Cash;
		} else if (value.equals(TENDERFRONTTYPE_CreditNote)) {
			type = M_CREDIT_NOTE;
			method = TENDERTYPE_CreditNote;
		} else if (value.equals(TENDERFRONTTYPE_Other)) {
			type = M_OTHER;
			method = TENDERTYPE_Other;
		} else if (value.equals(TENDERFRONTTYPE_Transfer)) {
			type = M_TRANSFER;
			method = TENDERTYPE_Transfer;
		}
		// else
		// type=99;

	}

	public boolean isManualCard() {
		return isManualCard;
	}

	public String getFrontMethod() {
		if (type == M_CHECK)
			return TENDERFRONTTYPE_Check;
		else if (type == M_CREDIT_CARD)
			return TENDERFRONTTYPE_CreditCard;
		else if (type == M_CASH)
			return TENDERFRONTTYPE_Cash;
		else if (type == M_CREDIT_NOTE)
			return TENDERFRONTTYPE_CreditNote;
		else if (type == M_OTHER)
			return TENDERFRONTTYPE_Other;
		else if (type == M_TRANSFER)
			return TENDERFRONTTYPE_Transfer;
		else
			return null;
	}

	public BigDecimal getAmmount() {
		return ammount;
	}

	public void setAmmount(String value) {
		ammount = new BigDecimal(value);
	}

	public Vector getVectorLine() {
		Vector vec = new Vector();
		vec.add(method);
		vec.add(methodDesc);
		vec.add(ammount);
		vec.add(lineNumber);

		return vec;
	}

	public void getPayTerms(JTable table, String prods) {
		DefaultTableModel modelo = new DefaultTableModel() {
			boolean[] blnEdit = new boolean[] { false, false };

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return blnEdit[columnIndex];
			}
		};
		modelo.addColumn(Msg.translate(Env.getCtx(), "Código"));
		modelo.addColumn(Msg.translate(Env.getCtx(), "Descripción"));
		table.getColumnModel().getColumn(0).setPreferredWidth(60);
		table.getColumnModel().getColumn(0).setMinWidth(60);
		table.getColumnModel().getColumn(0).setMaxWidth(60);
		table.getColumnModel().getColumn(0).setWidth(60);
		// table.getColumnModel().getColumn(1).setPreferredWidth(250);
		if (cardNumber.length() > 6) {
			String bins = cardNumber.substring(0, 6);
			try {
				String sql = "SELECT CCT.VALUE,CCT.DESCRIPTION,cct.validfrom,cct.validto"
						+ " FROM c_binline BL"
						+ " INNER JOIN c_bin B ON B.c_bin_id=BL.c_bin_id"
						+ " INNER JOIN C_CreditCardTerm CCT ON CCT.c_bin_id=B.c_bin_id"
						+ " INNER JOIN c_creditcard CC ON CC.c_creditcard_id=CCT.c_creditcard_id"
						+ " INNER JOIN c_bankaccount BA ON BA.c_creditcard_id=CC.c_creditcard_id"
						+ " INNER JOIN C_PaymentProcessoR PP ON PP.C_PAYMENTPROCESSOR_ID=BA.C_PAYMENTPROCESSOR_id"
						+ " WHERE "
						+ bins
						+ " between BL.idrangestart and BL.idrangeend"
						+ " AND BL.ISACTIVE='Y' AND B.ISACTIVE='Y' AND CCT.ISACTIVE='Y' AND CC.ISACTIVE='Y'"
						+ " AND BA.ISACTIVE='Y' AND PP.ISACTIVE='Y' AND CCT.ISINCLUDE='N'"
						+ " UNION"
						+ " SELECT CCT.VALUE,CCT.DESCRIPTION,TP.validfrom,TP.validto"
						+ " FROM C_BINLINE BL"
						+ " INNER JOIN C_BIN B ON B.C_BIN_ID=BL.C_BIN_ID"
						+ " INNER JOIN C_CREDITCARDTERM CCT ON CCT.C_BIN_ID=B.C_BIN_ID"
						+ " INNER JOIN C_CREDITCARD CC ON CC.C_CREDITCARD_ID=CCT.C_CREDITCARD_ID"
						+ " INNER JOIN C_BANKACCOUNT BA ON BA.C_CREDITCARD_ID=CC.C_CREDITCARD_ID"
						+ " INNER JOIN C_PAYMENTPROCESSOR PP ON PP.C_PAYMENTPROCESSOR_ID=BA.C_PAYMENTPROCESSOR_ID"
						+ " INNER JOIN TV_TERMPRODUCT TP ON CCT.C_CREDITCARDTERM_ID = TP.c_creditcardterm_id"
						+ " WHERE "
						+ bins
						+ " BETWEEN BL.IDRANGESTART AND BL.IDRANGEEND"
						+ " AND TP.M_PRODUCT_ID IN ("
						+ prods
						+ ")"
						+ " AND BL.ISACTIVE='Y' AND B.ISACTIVE='Y' AND CCT.ISACTIVE='Y' AND CC.ISACTIVE='Y'"
						+ " AND BA.ISACTIVE='Y' AND PP.ISACTIVE='Y' AND CCT.ISINCLUDE='Y'";

				PreparedStatement pstmt = DB.prepareStatement(sql, null);
				ResultSet rs = pstmt.executeQuery();
				Timestamp today = new Timestamp(System.currentTimeMillis());
				boolean canAdd;
				while (rs.next()) {
					Timestamp validfrom = rs.getTimestamp("validfrom");
					Timestamp validto = rs.getTimestamp("validto");
					canAdd = false;
					if (validfrom == null & validto == null)
						canAdd = true;
					if (validfrom != null && validfrom.compareTo(today) < 0)
						canAdd = true;
					else if (validfrom != null)
						canAdd = false;
					if (validto != null && validto.compareTo(today) > 0)
						canAdd = true;
					else if (validto != null)
						canAdd = false;
					if (canAdd) {
						Vector<Object> line = new Vector<Object>(2);
						line.add(rs.getString("VALUE"));
						line.add(rs.getString("DESCRIPTION"));
						modelo.addRow(line);
					}
				}
				DB.close(rs, pstmt);
			} catch (SQLException ex) {
				Logger.getLogger(VBPartner.class.getName()).log(Level.SEVERE,
						null, ex);
			}
		}
		table.setModel(modelo);
	}

	public void payback(VDocument curdoc) {
		if (type == M_CREDIT_CARD)
			getNextTransaction();
		deletePay(curdoc);
	}

	@Deprecated
	/*public boolean payNote(VDocument curdoc, boolean kk) {
		boolean res = false;
		int[] ids = MInvoice
				.getAllIDs("C_INVOICE", "DOCSTATUS='CO' AND DOCUMENTNO='"
						+ getCreditNote() + "'", null);
		type = M_CREDIT_NOTE;
		if (ids.length > 0) {
			int C_Invoice_ID = ids[0];
			cnote = new MInvoice(Env.getCtx(), C_Invoice_ID, null);
			try {
				if (!cnote.getC_BPartner().getValue().equals(
						curdoc.getPartner().getValue()))
					if (!VUtil
							.ask("ALERTA! La nota de crédito y el documento actuál son de clientes distintos."))
						return false;
			} catch (Exception e) {
				VUtil
						.setMsg(
								"No se puede comprobar la propiedad de esta Nota de Crédito",
								true);
				return false;
			}
			if (cnote.getRef_Invoice_ID() == 0) {
				if (cnote.getGrandTotal().compareTo(
						curdoc.getOrder().getGrandTotal()) <= 0) {
					ammount = cnote.getGrandTotal();
					cnote.setRef_Invoice_ID(curdoc.getOrder().getC_Order_ID());
					cnote.save();
					res = true;
				} else
					VUtil.setMsg(
							"El monto de la factura es menor que el de la Nota de Crédito: "
									+ cnote.getGrandTotal(), true);
			} else
				VUtil.setMsg("Esta Nota de Crédito ya ha sido usada", true);
		} else {
			VUtil.setMsg("La Nota de Credito Presentada No existe.", true);
		}

		return res;
	}
*/
/*	public boolean payNote(VDocument curdoc) {
		String sql;
		boolean res = false;
		sql = "SELECT c_invoice_id FROM C_INVOICE WHERE DOCUMENTNO='"
				+ getCreditNote() + "' AND ISSOTRX='Y'";
		DBServerConnection sc = new DBServerConnection();
		BigDecimal c_invoice_id = (BigDecimal) sc.getSQLValue(sql);
		if (c_invoice_id != null && c_invoice_id.intValue() > 0) {
			sql = "SELECT BP.value from C_BPartner BP JOIN C_INVOICE I ON I.c_bpartner_id=BP.c_bpartner_id  where c_invoice_id="
					+ c_invoice_id.intValue();
			String bpValue = (String) sc.getSQLValue(sql);
			if (bpValue == null)
				return false;
			if (bpValue.equals(curdoc.getPartner().getValue())) {
				sql = "SELECT Ref_Invoice_ID FROM C_INVOICE WHERE c_invoice_id="
						+ c_invoice_id.intValue();
				BigDecimal ref_Invoice = (BigDecimal) sc.getSQLValue(sql);
				if (ref_Invoice == null || ref_Invoice.intValue() == 0) {
					sql = "select GrandTotal from C_INVOICE WHERE C_INVOICE_ID="
							+ c_invoice_id.intValue();
					BigDecimal total = (BigDecimal) sc.getSQLValue(sql);
					if (total.compareTo(curdoc.getOrder().getGrandTotal()) <= 0) {
						ammount = total;
						sql = "UPDATE C_INVOICE SET Ref_Invoice_ID="
								+ curdoc.getOrder().getC_Order_ID()
								+ " WHERE C_INVOICE_ID="
								+ c_invoice_id.intValue();
						sc.update(sql);
						res = true;
					} else
						VUtil.setMsg(
								"El monto de la factura es menor que el de la Nota de Crédito: "
										+ cnote.getGrandTotal(), true);
				} else
					VUtil
							.setMsg("Esta Nota de Crédito ya ha sido usada",
									true);
			} else
				VUtil
						.setMsg(
								"ALERTA! La nota de crédito y el documento actuál son de clientes distintos.",
								true);
		} else
			VUtil.setMsg("La Nota de Credito Presentada No existe.", true);
		return res;
	}*/

	public boolean chargeTVCard() {
		int C_Cash_ID = MCash
				.getAllIDs("C_CASH", "NAME='" + tvcard + "'", null)[0];
		if (C_Cash_ID != 0) {
			MCash caja = new MCash(Env.getCtx(), C_Cash_ID, null);
			// New line
			MCashLine cashline = new MCashLine(Env.getCtx(), 0, null);
			cashline.setAmount(ammount);
			cashline.setC_Cash_ID(C_Cash_ID);
			cashline.setCashType(MCashLine.CASHTYPE_GeneralExpense);
			cashline.save();
			caja.save();
			return true;
		} else
			return false;
	}

	private boolean deletePay(VDocument curdoc) {
		if (MPayment_ID > 0) {
			MPayment m_payment = new MPayment(Env.getCtx(), MPayment_ID, curdoc
					.getTrxName());
			if (type == M_CREDIT_NOTE) {
				type = M_CREDIT_NOTE;
				String sql = "SELECT c_invoice_id FROM C_INVOICE WHERE DOCUMENTNO='"
						+ m_payment.getR_AuthCode() + "' AND ISSOTRX='Y'";
			//	DBServerConnection sc = new DBServerConnection();
				int c_invoice_id =  DB.getSQLValue(null, sql);
				if (c_invoice_id > 0) {
					sql = "update c_invoice set ref_invoice_id=null where c_invoice_id =  "
							+ c_invoice_id;
					DB.executeUpdate(sql, null);
				}
			}
			if (m_payment.isAllocated()) {
			//	VPValidator.setMsg("Este pago ya ha sido consignado", null,
				//		true);
				return false;
			} else {
				if (!m_payment.getDocStatus().equals(
						MPayment.DOCSTATUS_Completed)) {
					m_payment.processIt(MPayment.ACTION_Complete);
					m_payment.setDocAction(MPayment.ACTION_Close);
					m_payment.setDocStatus(MPayment.DOCSTATUS_Completed);
					m_payment.save();
				}
				m_payment.processIt(MPayment.ACTION_Reverse_Correct);
				m_payment.setDocAction(MPayment.ACTION_None);
				m_payment.setDocStatus(MPayment.DOCSTATUS_Reversed);
				m_payment.save();
				return true;
			}
		}
		return false;
	}

	public void CleanPayment() {
		payment = null;
		MPayment_ID = 0;
	}

	public boolean completeIt() {
		if (MPayment_ID != 0) {
			MPayment m_payment = new MPayment(Env.getCtx(), MPayment_ID, null);
			// Martin added set C_Invoice_ID
			m_payment.setC_Invoice_ID(getC_Invoice_ID());
			if (m_payment.getDocStatus().equals(MPayment.DOCSTATUS_Completed))
				return true;
			m_payment.setDocAction(DocAction.ACTION_Complete);
			m_payment.processIt(DocAction.ACTION_Complete);
			m_payment.save();
			return true;
		} else if (getTypeOfMethod() == VPayMethodLine.M_CREDIT_NOTE) {
			return true;
		}
		return false;
	}

	private BigDecimal getPaymentTaxAmt(VDocument curdoc) {
		BigDecimal tax = BigDecimal.ZERO;
		BigDecimal ivaTotal = curdoc.getOrder().getGrandTotal().subtract(
				curdoc.getOrder().getTotalLines());
		BigDecimal parcial = ammount;
		BigDecimal total = curdoc.getOrder().getGrandTotal();
		BigDecimal ivaPagado = getIvaPagado(curdoc.getOrder().getC_Order_ID());
		BigDecimal pagados = getPagoPagados(curdoc.getOrder().getC_Order_ID());

		tax = (ivaTotal.subtract(ivaPagado)).multiply((parcial.divide((total
				.subtract(pagados)), new MathContext(100))));
		return tax.setScale(2, BigDecimal.ROUND_HALF_UP);
	}

	private BigDecimal getIvaPagado(int C_Order_ID) {
		BigDecimal res = BigDecimal.ZERO;
		Query sql = new Query(Env.getCtx(), MPayment.Table_Name, "C_Order_ID="
				+ C_Order_ID, null);
		List<MPayment> pagos = sql.list();
		for (MPayment pago : pagos) {
			res = res.add(pago.getTaxAmt());
		}

		return res;
	}

	private BigDecimal getPagoPagados(int C_Order_ID) {
		BigDecimal res = BigDecimal.ZERO;
		Query sql = new Query(Env.getCtx(), MPayment.Table_Name, "C_Order_ID="
				+ C_Order_ID, null);
		List<MPayment> pagos = sql.list();
		for (MPayment pago : pagos) {
			res = res.add(pago.getPayAmt());
		}

		return res;
	}

	// Called when Payment line is validated
	public boolean pay(VDocument curdoc) {
		if (getIsValid()) {
			MPayment m_payment = new MPayment(Env.getCtx(), MPayment_ID, curdoc
					.getTrxName());

			if (C_BankAccount_ID == 0)
				C_BankAccount_ID = m_pos.getC_BankAccount_ID();
			m_payment.setAD_Org_ID(m_pos.getAD_Org_ID());
			m_payment.setAD_OrgTrx_ID(m_pos.getAD_Org_ID());
			m_payment.setC_BankAccount_ID(C_BankAccount_ID);// OJO AKI VA LA
															// CUENTA
			m_payment.setC_Order_ID(curdoc.getOrder().getC_Order_ID());
			/*m_payment.set_CustomColumn("Link_Order_ID", curdoc.getOrder()
					.getC_Order_ID());*/
			// m_payment.setC_DocType_ID(curdoc.getOrder().getC_DocType_ID());
			m_payment.setAmount(getCurrency(), getAmmount());
			m_payment.setC_BPartner_ID(curdoc.getBPartner_ID());
			m_payment.setTaxAmt(getPaymentTaxAmt(curdoc));// EL IVA DEL PAGO!!
			switch (getTypeOfMethod()) {
			case VPayMethodLine.M_CASH:
				// TERMINO DE PAGO
				// m_payment.setTenderType(TENDERTYPE_Cash);
				m_payment.set_CustomColumn(MPayment.COLUMNNAME_TenderType,
						TENDERTYPE_Cash);
				m_payment.setC_CashBook_ID(curdoc.getPOS().getC_CashBook_ID()); // Ntier - Martin added -all cash payments need a cashbook
				break;
			case VPayMethodLine.M_TRANSFER:
				// TERMINO DE PAGO
				// m_payment.setTenderType(TENDERTYPE_Check);
				m_payment.set_CustomColumn(MPayment.COLUMNNAME_TenderType,
						TENDERTYPE_Transfer);
				m_payment.setRoutingNo(getCheckAutorization());
				m_payment.setCheckNo(getCheckNumber());
				m_payment.setA_Name(getBankName());
				m_payment.setAccountNo(getAccountNumber());
				break;
			case VPayMethodLine.M_CHECK:
				// TERMINO DE PAGO
				// m_payment.setTenderType(TENDERTYPE_Check);
				m_payment.set_CustomColumn(MPayment.COLUMNNAME_TenderType,
						TENDERTYPE_Check);
				m_payment.setRoutingNo(getCheckAutorization());
				m_payment.setCheckNo(getCheckNumber());
				m_payment.setA_Name(getBankName());
				m_payment.setAccountNo(getAccountNumber());
				break;
			case VPayMethodLine.M_CREDIT_CARD:
				// TERMINO DE PAGO
				// m_payment.setTenderType(TENDERTYPE_CreditCard);
				m_payment.set_CustomColumn(MPayment.COLUMNNAME_TenderType,
						TENDERTYPE_CreditCard);
				m_payment.setR_AuthCode(cardAutorization);
				m_payment.set_CustomColumn("C_CreditCardTerm_ID",
						C_CreditCardTerm_ID);
				m_payment.setCreditCardNumber(cardNumber);
				m_payment.setR_PnRef_DC(julianDay());
				if (TV_cardLoggerResponse_ID > 0)
					m_payment.set_CustomColumn("TV_CardloggerResponse_ID",
							new BigDecimal(TV_cardLoggerResponse_ID));
				if (cardPing != null && cardPing.length() > 0)
					m_payment.setCreditCardVV(cardPing);
				if (cardDate != null && cardDate.length() > 0)
					m_payment.setCreditCardExp(cardDate.substring(2)
							+ cardDate.substring(0, 2));
				if (cardUserName != null && cardUserName.length() > 0)
					m_payment.setR_Info(cardUserName);
				break;
			case VPayMethodLine.M_OTHER:
				// m_payment.setTenderType(TENDERTYPE_DirectDeposit);
				m_payment.set_CustomColumn(MPayment.COLUMNNAME_TenderType,
						TENDERTYPE_Other);
				m_payment.setRoutingNo(checkAutori);
				break;
			case VPayMethodLine.M_CREDIT_NOTE:
				m_payment.set_CustomColumn(MPayment.COLUMNNAME_TenderType,
						TENDERTYPE_CreditNote);
				m_payment.setR_AuthCode(creditNote);
				break;

			}// end switch
			payment = m_payment;
			if (checkDate != null
					&& getTypeOfMethod() == VPayMethodLine.M_CHECK
					& checkDate
							.after(new Timestamp(System.currentTimeMillis()))) {
				isForCredit = true;
				m_payment.save();
			} else {
				m_payment.save();
				// Ntier - Martin commented not to complete Payment Now.  Complete on Checkout. (IN VDocument.CompleteAsink)
				// m_payment.setDocAction(DocAction.ACTION_Complete);
				// m_payment.processIt(DocAction.ACTION_Complete);
				//m_payment.save();
			}
			MPayment_ID = m_payment.get_ID();
			return true;
		}// end if
		return true;
	}

	private String julianDay() {
		Timestamp today = new Timestamp(System.currentTimeMillis());
		double now = VUtil.julianDate(today);
		Timestamp dateCompare = new Timestamp(today.getYear(), 0, 1, 0, 0, 0, 0);
		double compare = VUtil.julianDate(dateCompare);
		return new BigDecimal(now - compare).setScale(0,
				BigDecimal.ROUND_HALF_UP).toString();
	}

	public boolean pay(MBPartner curBPartner,VDocument curdoc) {  // Martin added parm curdoc
		if (getIsValid()) {
			if(bankId>0)
			{
				Query q = new Query(Env.getCtx(),MBankAccount.Table_Name," C_Bank_ID=? AND isdefault='Y'",null)
							.setParameters(new Object[]{bankId});
				MBankAccount ba = (MBankAccount) q.first();
				if(ba!=null)
					C_BankAccount_ID = ba.getC_BankAccount_ID();
			}
			MPayment m_payment = new MPayment(Env.getCtx(), MPayment_ID, null);
			if (C_BankAccount_ID == 0)
				C_BankAccount_ID = m_pos.getC_BankAccount_ID();
			m_payment.setAD_Org_ID(m_pos.getAD_Org_ID());
			m_payment.setAD_OrgTrx_ID(m_pos.getAD_Org_ID());
			m_payment.setC_BankAccount_ID(C_BankAccount_ID);// OJO AKI VA LA
															// CUENTA
			m_payment.setAmount(getCurrency(), getAmmount());
			m_payment.setC_BPartner_ID(curBPartner.get_ID());
			switch (getTypeOfMethod()) {
			case VPayMethodLine.M_CASH:
				// TERMINO DE PAGO
				m_payment.set_CustomColumn(MPayment.COLUMNNAME_TenderType,
						TENDERTYPE_Cash);
				if (curdoc.getPOS() != null) {
					m_payment.setC_CashBook_ID(curdoc.getPOS().getC_CashBook_ID()); // Ntier - Martin added -all cash payments need a cashbook
				}
				break;
			case VPayMethodLine.M_TRANSFER:
				// TERMINO DE PAGO
				// m_payment.setTenderType(TENDERTYPE_Check);
				m_payment.set_CustomColumn(MPayment.COLUMNNAME_TenderType,
						TENDERTYPE_Transfer);
				m_payment.setRoutingNo(getCheckAutorization());
				m_payment.setCheckNo(getCheckNumber());
				m_payment.setA_Name(getBankName());
				m_payment.setAccountNo(getAccountNumber());
				break;
			case VPayMethodLine.M_CHECK:
				// TERMINO DE PAGO
				m_payment.set_CustomColumn(MPayment.COLUMNNAME_TenderType,
						TENDERTYPE_Check);
				m_payment.setRoutingNo(getCheckAutorization());
				m_payment.setCheckNo(getCheckNumber());
				m_payment.setA_Name(getBankName());
				m_payment.setAccountNo(getAccountNumber());
				break;
			case VPayMethodLine.M_CREDIT_CARD:
				// TERMINO DE PAGO
				m_payment.set_CustomColumn(MPayment.COLUMNNAME_TenderType,
						TENDERTYPE_CreditCard);
				m_payment.setR_AuthCode(cardAutorization);
				m_payment.set_CustomColumn("C_CreditCardTerm_ID",
						C_CreditCardTerm_ID);
				m_payment.setCreditCardNumber(cardNumber);
				m_payment.setR_PnRef_DC(julianDay());
				if (TV_cardLoggerResponse_ID > 0)
					m_payment.set_CustomColumn("TV_CardloggerResponse_ID",
							TV_cardLoggerResponse_ID);
				if (cardPing != null && cardPing.length() > 0)
					m_payment.setCreditCardVV(cardPing);
				if (cardDate != null && cardDate.length() > 0)
					m_payment.setCreditCardExp(cardDate.substring(2)
							+ cardDate.substring(0, 2));
				if (cardUserName != null && cardUserName.length() > 0)
					m_payment.setR_Info(cardUserName);
				break;
			case VPayMethodLine.M_OTHER:
				m_payment.set_CustomColumn(MPayment.COLUMNNAME_TenderType,
						TENDERTYPE_Other);
				m_payment.setRoutingNo(checkAutori);
				break;
			case VPayMethodLine.M_CREDIT_NOTE:
				m_payment.set_CustomColumn(MPayment.COLUMNNAME_TenderType,
						TENDERTYPE_CreditNote);
				m_payment.setR_AuthCode(creditNote);
				break;
			}// end switch
			payment = m_payment;
			if (checkDate != null
					&& getTypeOfMethod() == VPayMethodLine.M_CHECK
					& checkDate
							.after(new Timestamp(System.currentTimeMillis()))) {
				isForCredit = true;
				m_payment.save();
			} else {
				m_payment.save();
				// Ntier - Martin commented not to complete Payment Now.  Complete on Checkout. (IN VDocument.Complete)
				// m_payment.setDocAction(DocAction.ACTION_Complete);
				// m_payment.processIt(DocAction.ACTION_Complete);
				//m_payment.save();
			}
			MPayment_ID = m_payment.get_ID();
			return true;
		}// end if
		return true;
	}

	public void tenderToType(MPayment pay) {
		String ttype;
		if (pay.getTenderType().equals(TENDERTYPE_Cash))
			setMethod(TENDERFRONTTYPE_Cash);
		else if (pay.getTenderType().equals(TENDERTYPE_Check))
			setMethod(TENDERFRONTTYPE_Check);
		else if (pay.getTenderType().equals(TENDERTYPE_Transfer))
			setMethod(TENDERFRONTTYPE_Transfer);
		else if (pay.getTenderType().equals(TENDERTYPE_CreditCard))
			setMethod(TENDERFRONTTYPE_CreditCard);
		else if (pay.getTenderType().equals(TENDERTYPE_CreditNote))
			setMethod(TENDERFRONTTYPE_CreditNote);
		else {
			TENDERTYPE_Other = pay.getTenderType();
			setMethod(TENDERFRONTTYPE_Other);
		}

	}

	public void Print(boolean deleting) {
		/*VPrintManager pm = new VPrintManager(m_pos, this);
		pm.setM_Shipper(M_Shipper_ID);
		pm.Print();*/
	}

	public void Print(String stamp, boolean deleting) {
		/*VPrintManager pm = new VPrintManager(m_pos, this);
		pm.setM_Shipper(M_Shipper_ID);
		pm.Print(stamp);*/
	}

	public int getC_Invoice_ID() {
		return C_Invoice_ID;
	}

	public void setC_Invoice_ID(int invoice_ID) {
		C_Invoice_ID = invoice_ID;
	}

}
