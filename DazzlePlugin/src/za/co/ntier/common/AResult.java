package za.co.ntier.common;

/**
 * @author Neil Gordon 
 * 28 Jan 2014
 */
public class AResult {

	public static final int RESULT_OK = 0;
	public static final int RESULT_AN_EXCEPTION_OCCURRED = 1;
	public static final int RESULT_ERROR = 2;
	
	private int m_resultCode = 0;
	private String m_message = "";
	
	public AResult() {
	}
	
	public AResult(int m_resultCode, String m_message) {
		this.m_resultCode = m_resultCode;
		this.m_message = m_message;
	}

	public int getResultCode() {
		return m_resultCode;
	}

	public void setResultCode(int m_resultCode) {
		this.m_resultCode = m_resultCode;
	}

	public String getMessage() {
		return m_message;
	}
	
	/**
	 * Just set the message
	 */
	public void setMessage(String message) {
		this.m_message = message;
	}
	
}
