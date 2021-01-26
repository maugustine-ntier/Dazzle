package org.compiere.model;

import org.adempiere.base.IProcessFactory;
import org.adempiere.process.InOutGenerateRMACust;
import org.adempiere.process.InOutGenerateRMAVendor;
import org.adempiere.process.InvoiceGenerateRMADazzle;
import org.adempiere.process.InvoiceGenerateRMAVendor;
import org.adempiere.process.ZZ_CompleteOrders;
import org.adempiere.process.ZZ_Create_Addresses;
import org.adempiere.process.ZZ_Create_MissingOrders;
import org.adempiere.process.ZZ_Create_MissingReturns;
import org.compiere.process.AgingDazzle;
import org.compiere.process.ImportBankStatementDazzle;
import org.compiere.process.ProcessCall;
import org.compiere.report.DazzleReportStarter;

/**
 * (c) 2015 nTier Software Services
 * @author Neil Gordon
 * Date: 13 Feb 2015
 * Description:	
 */
public class DazzleProcessFactory implements IProcessFactory {

	@Override
	public ProcessCall newProcessInstance(String className) {
		if ("org.compiere.report.DazzleReportStarter".equals(className))
			return new DazzleReportStarter();
		if ("org.compiere.process.ImportBankStatementDazzle".equals(className))
			return new ImportBankStatementDazzle();
		if ("org.compiere.process.AgingDazzle".equals(className))
			return new AgingDazzle();
		if ("org.adempiere.process.InOutGenerateRMACust".equals(className))
			return new InOutGenerateRMACust();
		if ("org.adempiere.process.ZZ_Create_MissingReturns".equals(className))
			return new ZZ_Create_MissingReturns();
		if ("org.adempiere.process.ZZ_Create_MissingOrders".equals(className))
			return new ZZ_Create_MissingOrders();
		if ("org.adempiere.process.ZZ_Create_Addresses".equals(className))
			return new ZZ_Create_Addresses();
		if ("org.adempiere.process.ZZ_CompleteOrders".equals(className))
			return new ZZ_CompleteOrders();
		if ("org.adempiere.process.InvoiceGenerateRMAVendor".equals(className))
			return new InvoiceGenerateRMAVendor();
		if ("org.adempiere.process.InvoiceGenerateRMADazzle".equals(className))
			return new InvoiceGenerateRMADazzle();
		if ("org.adempiere.process.InOutGenerateRMAVendor".equals(className))
			return new InOutGenerateRMAVendor();
//		if ("com.smj.process.SmjReport".equals(className))
//			return new SmjReport();
		return null;
	}
}
