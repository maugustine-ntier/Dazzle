package za.co.ntier.dazzle.common;

import java.util.Properties;

import org.compiere.process.SvrProcess;

/**
 * @author Neil Gordon
 * 14 Feb 2014
 * Context and transaction support, and a result object
 * 2015/11/15 NCG Copied from DPW project and renamed from BaseSupport
 */
public interface IContextSupport {

	public String get_TrxName();

	public void set_TrxName(String trxName);

	public Properties getCtx();

	public void setCtx(Properties ctx);

//	public ICEResult getResult();
	
	public SvrProcess getProcess();
	
	public void setProcess(SvrProcess process);

}