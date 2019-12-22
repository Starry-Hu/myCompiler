package bean;

import java.util.ArrayList;

/**
 * 布尔表达式的true和false两类出口记录
 * 
 * @author StarryHu
 *
 */
public class E {
	// 该语句块的true出口链表
	private ArrayList<Integer> trueExits;
	// 该该语句块的false出口链表
	private ArrayList<Integer> falseExits;

	public ArrayList<Integer> getTrueExits() {
		return trueExits;
	}

	public void setTrueExits(ArrayList<Integer> trueExits) {
		this.trueExits = trueExits;
	}

	public ArrayList<Integer> getFalseExits() {
		return falseExits;
	}

	public void setFalseExits(ArrayList<Integer> falseExits) {
		this.falseExits = falseExits;
	}

}
