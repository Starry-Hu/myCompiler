package bean;

/**
 * 四元式
 * 
 * @author StarryHu
 *
 */
public class Equality4 {
	// 四元式序号
	private int label;
	// 操作码
	private int operator;
	// 左操作数在symbol表的入口地址
	private int leftAddress;
	// 右操作数在symbol表的入口地址
	private int rightAddress;
	// 结果在symbol表的入口地址
	private int resultAddress;

	// **是否基本块的入口 - 生成目标代码中使用**
	private boolean isEntrance;

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}

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

	public boolean isEntrance() {
		return isEntrance;
	}

	public void setEntrance(boolean isEntrance) {
		this.isEntrance = isEntrance;
	}

	@Override
	public String toString() {
		return "(" + label + ")(" + operator + ", " + leftAddress + ", " + rightAddress + ", " + resultAddress + ")";
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		Equality4 other = new Equality4();
		other.setLabel(label);
		other.setOperator(operator);
		other.setLeftAddress(leftAddress);
		other.setRightAddress(rightAddress);
		other.setResultAddress(resultAddress);
		other.setEntrance(isEntrance);
		
		return other;
	}

}
