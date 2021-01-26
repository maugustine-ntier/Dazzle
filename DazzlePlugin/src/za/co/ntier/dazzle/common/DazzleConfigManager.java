package za.co.ntier.dazzle.common;

import java.util.Properties;

import org.compiere.model.MSysConfig;

import za.co.ntier.common.NTierConfigurator;

/**
 * @author NCG
 * Date: 15 Apr 2015
 * Description:	Dazzle Configurator Manager
 */
public class DazzleConfigManager {

	public static boolean isDazzleQECreditNoteEasyRefundsEnabled(Properties ctx, String trxName) {
		return NTierConfigurator.newOrGetBoolean(ctx, trxName, false, 
				"DAZZLE_QE_CREDITNOTE_EASYREFUNDS_ENABLED", "N", "Dazzle Quick Entry: Credit Note: Enable easy refunds");
	}
	
	/**
	 * NCG: #RQ100021: Default the cost to price list, and give error if no cost
	 */
	public static boolean isDazzleCostAutoCreate(Properties ctx, String trxName) {
		return NTierConfigurator.newOrGetBoolean(ctx, trxName, true /* System Setting */, 
				"DAZZLE_COST_AUTOCREATE", "Y", "Auto create cost when product price limit price on supplier's price list is changed");
	}
	
	/**
	 * NCG: #RQ100021: Default the cost to price list, and give error if no cost
	 */
	public static boolean isDazzleCostStopInvoiceIfNoCost(Properties ctx, String trxName) {
		return NTierConfigurator.newOrGetBoolean(ctx, trxName, true /* System Setting */, 
				"DAZZLE_COST_STOP_INVOICE_IF_NO_COST", "Y", "Stop invoice or credit note quick entry, if no cost");
	}
	
	/**
	 * NCG: RQ100032: Too many items in quote selection dropdown
	 */
	public static int getDazzleQuoteDropdownMonths(Properties ctx, String trxName) {
		MSysConfig config = NTierConfigurator.newOrGet(ctx, trxName, false,
				"DAZZLE_QUOTE_DROPDOWN_MONTHS", "3", "Dazzle Quote: Number of months history to show in quote dropdown");
		int r = config.get_ValueAsInt("Value");
		return r;
	}
	
//	public static int getSequenceNumberNext(Properties ctx, String trxName) {
//		MSysConfig config = NTierConfigurator.newOrGet(ctx, trxName, false, 
//				"NTIER_SEQUENCE_NEXT", "5000", "Next sequence number");
//		int r = config.get_ValueAsInt("Value");
//		config.setValue( (new Integer(r+1)).toString() );
//		config.saveEx();
//		return r;
////		return NTierConfigurator.newOrGetInt(ctx, trxName, false, 
////				"NTIER_SEQUENCE_NEXT", "100", "Next sequence number");
//	}
//	
//	public static int getSequenceReceivedNext(Properties ctx, String trxName) {
//		MSysConfig config = NTierConfigurator.newOrGet(ctx, trxName, false, 
//				"NTIER_SEQUENCE_RECEIVED_NEXT", "100", "Next received sequence number");
//		int r = config.get_ValueAsInt("Value");
//		config.setValue( (new Integer(r+1)).toString() );
//		config.saveEx();
//		return r;
////		int r = NTierConfigurator.newOrGetInt(ctx, trxName, false, 
////				"NTIER_SEQUENCE_RECEIVED_NEXT", "100", "Next received sequence number");
//	}
//	
//	public static int getSequenceProcessedNext(Properties ctx, String trxName) {
//		MSysConfig config = NTierConfigurator.newOrGet(ctx, trxName, false, 
//				"NTIER_SEQUENCE_PROCESSED_NEXT", "70000", "Next processed sequence number");
//		int r = config.get_ValueAsInt("Value");
//		config.setValue( (new Integer(r+1)).toString() );
//		config.saveEx();
//		return r;
//	}
//	
//	public static boolean isRequireUniqueSequence(Properties ctx, String trxName) {
//		return NTierConfigurator.newOrGetBoolean(ctx, trxName, false, 
//				"NTIER_REQUIRE_UNIQUE_SEQUENCE", "Y", "Require a unique sequence number?");
//	}
//	
//	public static String getDevelopmentMode(Properties ctx, String trxName) {
//		return NTierConfigurator.newOrGetString(ctx, trxName, false, 
//				"NTIER_DEVELOPMENT_MODE", "Test", "Development mode - Test or Live");
//	}
//	
//	public static boolean isRunMessageProcessorAfterReadingMessage(Properties ctx, String trxName) {
//		return NTierConfigurator.newOrGetBoolean(ctx, trxName, false, 
//				"NTIER_RUN_PROCESSOR_AFTER_READ", "Y", "Run message processor after reading message (otherwise require manual run of processor)?");
//	}
//	
//	public static Timestamp getFirstDayDateOfCurrentFinancialYear(Properties ctx, String trxName) {
//		MSysConfig config = NTierConfigurator.newOrGet(ctx, trxName, false, 
//				"NTIER_FIRST_DAY_CURRENT_YEAR", "2014-03-01", "First day of current financial year: for take-on");
//		String v = config.get_ValueAsString("Value");
//		Timestamp r = NTierDateUtils.parse(v, "yyyy-MM-dd") ;
//		return r;
//	}
//	
//	public static Timestamp getTakeOnPostAsAtDate(Properties ctx, String trxName) {
//		MSysConfig config = NTierConfigurator.newOrGet(ctx, trxName, false, 
//				"NTIER_TAKEON_POSTASATDATE", "2015-02-01", "Historical date (prior year date) at which to post historical transactions");
//		String v = config.get_ValueAsString("Value");
//		Timestamp r = NTierDateUtils.parse(v, "yyyy-MM-dd") ;
//		return r;
//	}
//	
//	public static boolean isTopicIncomingEnable(Properties ctx, String trxName) {
//		return NTierConfigurator.newOrGetBoolean(ctx, trxName, false, 
//				"NTIER_TOPIC_INCOMING_ENABLE", "Y", "Enable incoming message listener, takes affect after approximately 1 minute");
//	}
//	
//	public static boolean isCurrentYearOrLater(Properties ctx, String trxName, Timestamp dtDate) {
//		if ( dtDate.compareTo(getFirstDayDateOfCurrentFinancialYear(ctx, trxName)) >= 0 ) {
//			return true;
//		}
//		return false;
//	}
//	
//	/**
//	 * Get all the MAssetAdditions belonging to the specified asset
//	 */
//	public static MAssetAddition[] getAssetAdditions(Properties ctx, String trxName, int assetID, String docStatus) {
//		List<MAssetAddition> list = new Query(ctx, MAssetAddition.Table_Name,
//				MAsset.Table_Name + "_ID=? and docStatus=?", trxName)
//				.setParameters(assetID, docStatus)		//.setOnlyActiveRecords(true)
//				//.setOrderBy("coalesce(sequence, zz_earlywarningdays)")
//				.list();
//
//		MAssetAddition[] retValue = new MAssetAddition[list.size()];
//		list.toArray(retValue);
//		return retValue;
//	}
//	
//	public static boolean isHasAssetAdditions(Properties ctx, String trxName, int assetID, String docStatus) {
//		int i = getAssetAdditions(ctx, trxName, assetID, docStatus).length;
//		return i>0;
//	}
	
	
}
