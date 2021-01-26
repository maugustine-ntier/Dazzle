package za.co.ntier.common;

import java.util.Properties;

/**
 * @author Neil Gordon
 * 14 Feb 2014
 * Context and transaction support, and a result object
 */
public abstract class BaseSupport {

	protected String m_trxName;
	protected Properties m_ctx;
	protected AResult m_result = new AResult();
	
	public BaseSupport() {
		
	}

	public BaseSupport(Properties m_ctx, String m_trxName) {
		this.m_trxName = m_trxName;
		this.m_ctx = m_ctx;
	}

	public String get_TrxName() {
		return m_trxName;
	}

	public void set_TrxName(String m_trxName) {
		this.m_trxName = m_trxName;
	}

	public Properties getCtx() {
		return m_ctx;
	}

	public void setCtx(Properties m_ctx) {
		this.m_ctx = m_ctx;
	}
	
	public AResult process() throws Exception {
		return m_result;
	}
	
	
	public AResult getResult() {
		return m_result;
	}
}
