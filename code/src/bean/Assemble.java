package bean;

/**
 * 汇编代码结构
 * 
 * @author StarryHu
 *
 */
public class Assemble {
	// 操作符
	private String opreator;
	// 左操作数
	private String leftObj;
	// 右操作数
	private String rightObj;

	public String getOpreator() {
		return opreator;
	}

	public void setOpreator(String opreator) {
		this.opreator = opreator;
	}

	public String getLeftObj() {
		return leftObj;
	}

	public void setLeftObj(String leftObj) {
		this.leftObj = leftObj;
	}

	public String getRightObj() {
		return rightObj;
	}

	public void setRightObj(String rightObj) {
		this.rightObj = rightObj;
	}

	@Override
	public String toString() {
		return "" + opreator + " " + leftObj + ", " + rightObj;
	}

}
