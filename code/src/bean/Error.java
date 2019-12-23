package bean;

/**
 * 错误记录
 * 
 * @author StarryHu
 *
 */
public class Error {
	// 错误定位行数
	private int row;
	// 错误源
	private String errorSrc;
	// 错误类型
	private String errorType;

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public String getErrorSrc() {
		return errorSrc;
	}

	public void setErrorSrc(String errorSrc) {
		this.errorSrc = errorSrc;
	}

	public String getErrorType() {
		return errorType;
	}

	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

}
