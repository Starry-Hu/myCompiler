package bean;

import java.util.ArrayList;

/**
 * 生成目标代码时使用的基本块
 * 
 * @author StarryHu
 *
 */
public class BasicBlock {
	// 基本块名称
	private String name;
	// 所包含四元式序列
	private ArrayList<Equality4> equality4List = new ArrayList<>();
	// 所生成的目标代码
	private ArrayList<Assemble> assembleList = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<Equality4> getEquality4List() {
		return equality4List;
	}

	public void setEquality4List(ArrayList<Equality4> equality4List) {
		this.equality4List = equality4List;
	}

	public ArrayList<Assemble> getAssembleList() {
		return assembleList;
	}

	public void setAssembleList(ArrayList<Assemble> assembleList) {
		this.assembleList = assembleList;
	}
}
