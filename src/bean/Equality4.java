package bean;

/**
 * 四元式
 * 
 * @author StarryHu
 *
 */
public class Equality4 {
	// 操作码
	private int operator;
	// 左操作数在symbol表的入口地址
	private int leftAddress;
	// 右操作数在symbol表的入口地址
	private int rightAddress;
	// 结果在symbol表的入口地址
	private int resultAddress;

	public int getOperator() {
		return operator;
	}

	public void setOperator(int operator) {
		this.operator = operator;
	}

	public int getLeftAddress() {
		return leftAddress;
	}

	public void setLeftAddress(int leftAddress) {
		this.leftAddress = leftAddress;
	}

	public int getRightAddress() {
		return rightAddress;
	}

	public void setRightAddress(int rightAddress) {
		this.rightAddress = rightAddress;
	}

	public int getResultAddress() {
		return resultAddress;
	}

	public void setResultAddress(int resultAddress) {
		this.resultAddress = resultAddress;
	}

}
