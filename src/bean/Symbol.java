package bean;

/**
 * 符号表文件结构 - 用于存放标识符和常数
 * 
 * @author StarryHu
 *
 */
public class Symbol {
	// 序号
	private int number;
	// 名字
	private String name;
	// 类型
	private int type;

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "(" + number + ")(" + name + ", " + type + ")";
	}
}
