package bean;

/**
 * token文件结构，存放此法分析后的各单词
 * 
 * @author StarryHu
 *
 */
public class Token {
	// 单词序号
	private int label;
	// 单词本身
	private String name;
	// 单词种别编码
	private int type;
	// 单词在符号表中登记的指针（其序号），仅用于标识符或常数，其他情况为-1
	private int address;

	public int getLabel() {
		return label;
	}

	public void setLabel(int label) {
		this.label = label;
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

	public int getAddress() {
		return address;
	}

	public void setAddress(int address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "(" + label + ")(" + name + ", " + type + ", " + address + ")";
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Token other = new Token();
		other.setAddress(address);
		other.setLabel(label);
		other.setName(name);
		other.setType(type);
		
		return other;
	}
}
