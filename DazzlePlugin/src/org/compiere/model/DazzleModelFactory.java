package org.compiere.model;

import java.sql.ResultSet;

import org.adempiere.base.IModelFactory;
import org.compiere.util.CLogger;
import org.compiere.util.Env;

/**
 * @author ngordon
 * Date: 15 Sep 2014
 */
public class DazzleModelFactory implements IModelFactory {

	/** Logger */
	private static CLogger log = CLogger.getCLogger(DazzleModelFactory.class);

	@Override
	public Class<?> getClass(String tableName) {
	
		//#1
		if ( ZZ_BankStatementLine.Table_Name.equals(tableName))
			return ZZ_BankStatementLine.class;

		//#2
		if ( ZZ_Product.Table_Name.equals(tableName))
			return ZZ_Product.class;
		
		//#3
		if ( ZZ_BankStatement.Table_Name.equals(tableName))
			return ZZ_BankStatement.class;
				
		return null;
	}

	@Override
	public PO getPO(String tableName, int Record_ID, String trxName) {
		
		//#1
		if ( ZZ_BankStatementLine.Table_Name.equals(tableName))
			return new ZZ_BankStatementLine(Env.getCtx(), Record_ID, trxName);

		//#2
		if ( ZZ_Product.Table_Name.equals(tableName))
			return new ZZ_Product(Env.getCtx(), Record_ID, trxName);

		//#3
		if ( ZZ_BankStatement.Table_Name.equals(tableName))
			return new ZZ_BankStatement(Env.getCtx(), Record_ID, trxName);
		
				
		return null;
		
	}

	@Override
	public PO getPO(String tableName, ResultSet rs, String trxName) {
		
		//#1
		if ( ZZ_BankStatementLine.Table_Name.equals(tableName))
			return new ZZ_BankStatementLine(Env.getCtx(), rs, trxName);

		//#2
		if ( ZZ_Product.Table_Name.equals(tableName))
			return new ZZ_Product(Env.getCtx(), rs, trxName);

		//#3
		if ( ZZ_BankStatement.Table_Name.equals(tableName))
			return new ZZ_BankStatement(Env.getCtx(), rs, trxName);
				
		
		return null;
	}

}
