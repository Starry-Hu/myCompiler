package bean;

import java.util.ArrayList;
import java.util.Stack;

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

	// **待用信息与活跃信息链 - 生成目标代码中使用** --初始为N,N
	// 使用栈方便从后往前记录使用信息
	private Stack<String> infoLink = new Stack<>();

	// **存放地址 - 分配寄存器时使用**
	private ArrayList<String> saveValue = new ArrayList<>();

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

	public Stack<String> getInfoLink() {
		return infoLink;
	}

	public void setInfoLink(Stack<String> infoLink) {
		this.infoLink = infoLink;
	}

	public ArrayList<String> getSaveValue() {
		return saveValue;
	}

	public void setSaveValue(ArrayList<String> saveValue) {
		this.saveValue = saveValue;
	}


	@Override
	public String toString() {
		return "(" + number + ")(" + name + ", " + type + ")";
	}

	public String toStringWithInfoLink() {
		String base = toString() + " [";
		for (int i = 0; i < infoLink.size(); i++) {
			if (i != infoLink.size() - 1) {
				base += "(" + infoLink.get(i) + ") ->";
			} else {
				base += "(" + infoLink.get(i) + ") ]";
			}
		}
		return base;
	}
}
