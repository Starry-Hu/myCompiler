package bean;

/**
 * 汇编代码结构
 * 
 * @author StarryHu
 *
 */
public class Assemble {
	// 序号
	private int label;
	// 操作符
	private String opreator;
	// 左操作数
	private String leftObj;
	// 右操作数
	private String rightObj;

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
	}

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

}
