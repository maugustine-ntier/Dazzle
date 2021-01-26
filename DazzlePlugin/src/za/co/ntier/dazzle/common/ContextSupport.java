package za.co.ntier.dazzle.common;

import java.util.Properties;

import org.compiere.process.SvrProcess;

/**
 * @author Neil Gordon
 * 14 Feb 2014
 * Context and transaction support, and a result object
 * 2015/11/15 NCG Copied from DPW project and renamed from BaseSupport
 */
public abstract class ContextSupport implements IContextSupport {

	protected String m_trxName;
	protected Properties m_ctx;
//	protected ICEResult m_result1 = new ICEResult();
	protected SvrProcess m_process;
	
	public ContextSupport() {
		
	}

	public ContextSupport(Properties ctx, String trxName) {
		this.m_trxName = trxName;
		this.m_ctx = ctx;
	}
	
	@Override
	public String get_TrxName() {
		return m_trxName;
	}

	@Override
	public void set_TrxName(String trxName) {
		this.m_trxName = trxName;
	}

	@Override
	public Properties getCtx() {
		return m_ctx;
	}

	@Override
	public void setCtx(Properties ctx) {
		this.m_ctx = ctx;
	}
	
//	public ICEResult process() throws Exception {
//		return m_result;
//	}
	
//	@Override
//	public ICEResult getResult() {
//		return m_result1;
//	}

	@Override
	public SvrProcess getProcess() {
		return m_process;
	}

	@Override
	public void setProcess(SvrProcess process) {
		this.m_process = process;
	}
	
	// NCG: From SvrProcess
	/**
	 * 	Add Log
	 *	@param msg message
	 */
	public void addProcessLog (String msg)
	{
		if (msg != null)
			m_process.addLog (0, null, null, msg);
	}	//	addLog
	
}
