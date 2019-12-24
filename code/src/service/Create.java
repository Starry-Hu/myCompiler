package service;

import java.util.ArrayList;
import bean.Assemble;
import bean.Equality4;
import bean.Symbol;

/**
 * 生成目标代码
 * 
 * @author StarryHu
 *
 */
public class Create {
	// symbol符号表
	ArrayList<Symbol> symbolList = new ArrayList<>();
	// 汇编代码表
	ArrayList<Assemble> assembleList = new ArrayList<>();
	// 四元式序列表
	private ArrayList<Equality4> equality4List = new ArrayList<>();
	// bx寄存器存放的内容
	private ArrayList<String> bx = new ArrayList<>();
	// dx寄存器存放的内容
	private ArrayList<String> dx = new ArrayList<>();
	// 判断是否寄存器都被使用，且左操作数未使用
	private boolean isRegFullUse = false;

	/**
	 * 
	 * 
	 * @param symbolList
	 */

	/**
	 * 使用**语义分析后**的符号表与四元式序列表来初始化。 两表的有效字符都从1开始。 注意此处将汇编代码表填充一个，使其有效位从1开始
	 * 
	 * @param symbolList
	 * @param equality4List
	 */
	public void initial(ArrayList<Symbol> symbolList, ArrayList<Equality4> equality4List) {
		this.symbolList = symbolList;
		this.equality4List = equality4List;
	}

	/**
	 * --- 主要处理的代码块，调用 ---
	 */
	public void implement() {
		// 划分基本块
		divideCodeByBlock();
		// 对每个块回填待用信息链
		backFilling();
		// 生成目标代码
		createTarget();
	}

	/**
	 * 从后往前的回填待用信息
	 */
	private void backFilling() {
		for (int i = equality4List.size() - 1; i >= 1; i--) {
			int op = equality4List.get(i).getOperator();
			// 非转移语句的op速记符范围（全运算）
			if (op < 52) {
				// 获取结果数（被赋值数）
				int resultAddr = equality4List.get(i).getResultAddress();
				String resultName = symbolList.get(resultAddr).getName();
				// 将其对应的symbol的待用/活跃的初始值N,N
				for (Symbol symbol : symbolList) {
					if (symbol.getName().equals(resultName)) {
						symbol.getInfoLink().add("N,N");
					}
				}

				// 获取左操作数
				int leftAddr = equality4List.get(i).getLeftAddress();
				String leftName = symbolList.get(leftAddr).getName();
				// 将其对应的symbol设置待用/活跃
				for (Symbol symbol : symbolList) {
					if (symbol.getName().equals(leftName)) {
						String info = i + ",Y";
						symbol.getInfoLink().add(info);
					}
				}

				// ----- 非:=运算才需再获取右操作数，否则获取错误 -----
				if (op != 51) {
					// 获取右操作数
					int rightAddr = equality4List.get(i).getRightAddress();
					String rightName = symbolList.get(rightAddr).getName();
					// 将其对应的symbol设置待用/活跃
					for (Symbol symbol : symbolList) {
						if (symbol.getName().equals(rightName)) {
							String info = i + ",Y";
							symbol.getInfoLink().add(info);
						}
					}
				}
			}
		}
	}

	/**
	 * 划分基本块
	 */
	private void divideCodeByBlock() {
		// 第一条语句作为入口
		equality4List.get(1).setEntrance(true);

		// 遍历处理每个语句
		for (int i = 0; i < equality4List.size(); i++) {
			int op = equality4List.get(i).getOperator();
			// 转移语句的op速记符，且转移到的地址不为-1(真执行)。设置其转移到的语句为入口
			if ((op <= 58 && op >= 52)) {
				int turnAddr = equality4List.get(i).getResultAddress();
				if (turnAddr != -1) {
					equality4List.get(turnAddr).setEntrance(true);
				}

				// 如果是条件转移，则其后第一个语句为入口
				if (op != 52) {
					equality4List.get(i + 1).setEntrance(true);
				}
			}
		}
	}

	/**
	 * 为某四元式分配寄存器
	 * 
	 * @param equality4
	 * @return
	 */
	private String getRegister(Equality4 equality4) {
		// 判断是否寄存器bx/dx里面只有左操作数的值，那么则可接着用该寄存器
		int leftAddr = equality4.getLeftAddress();
		String leftName = symbolList.get(leftAddr).getName();

		// 如果bx当前存放为空，或左操作数存放在bx中，则直接拿bx来用
		if (isSaveIn(leftName, "bx") || bx.size() == 0) {
			// 如果之前不在bx中，则往bx中加入该操作数，且该操作数的存放情况中加入bx，并生成MOV四元式
			if (!isSaveIn(leftName, "bx")) {
				symbolSaveOneReg(leftName, bx, "bx");
			}
			return "bx";
		}
		// 同理判断dx
		else if (isSaveIn(leftName, "dx") || bx.size() == 0) {
			if (!isSaveIn(leftName, "dx")) {
				symbolSaveOneReg(leftName, dx, "dx");
			}
			return "dx";
		}
		// 如果都有存信息，且左操作数不在其中。则选一个最晚被使用的寄存器，并将其内容备份移到内存中
		else {
			isRegFullUse = true;
			if (bx.size() > dx.size())// 选取一个现在存放变量最少的寄存器
			{
				symbolSaveOneReg(leftName, dx, "dx");
				return "dx";
			} else {
				symbolSaveOneReg(leftName, bx, "bx");
				return "bx";
			}
		}
	}

	/**
	 * 根据token名寻找某token是否有存放在指定的register中
	 * 
	 * @param tokenName
	 * @param register
	 * @return
	 */
	private boolean isSaveIn(String tokenName, String register) {
		// 所有同名symbol的地址存放情况是一样的，所以找到一个即可结束寻找
		for (Symbol symbol : symbolList) {
			if (symbol.getName().equals(tokenName)) {
				// 查找register是否在存放情况中
				if (!symbol.getSaveValue().isEmpty()) {
					return symbol.getSaveValue().contains(register);
				}
			}
		}
		return false;
	}

	/**
	 * ---- 多次重复内部使用 ----，将该符号名对应的符号存放信息加上该寄存器；而该寄存器存放信息加上该符号名
	 * 
	 * @param symbolName
	 * @param register
	 */
	private void symbolSaveOneReg(String symbolName, ArrayList<String> register, String registerName) {
		register.add(symbolName);
		for (Symbol symbol : symbolList) {
			if (symbol.getName().equals(symbolName)) {
				symbol.getSaveValue().add("register");
			}
		}
		// 输出MOV四元式
		Assemble assemble = new Assemble();
		assemble.setLabel(assembleList.size());
		assemble.setOpreator("MOV");
		assemble.setLeftObj(registerName);
		assemble.setRightObj(symbolName);
		assembleList.add(assemble);
	}

	/**
	 * 生成目标代码
	 */
	public void createTarget() {
		// 遍历每个四元式
		for (Equality4 equality4 : equality4List) {
			int op = equality4.getOperator();
			// j
			if (op == 52) {
				Assemble assemble = new Assemble();
				// JMP x _
				assemble.setLabel(assembleList.size());
				assemble.setOpreator("JMP");
				assemble.setLeftObj(equality4.getResultAddress() + "");// 转移的目标地址
				assembleList.add(assemble);
			}
			// j< j<= j> j>= j<> j=
			else if (op == 53 || op == 54 || op == 57 || op == 58 || op == 55 || op == 56) {
				int leftAddr = equality4.getLeftAddress();
				int rightAddr = equality4.getRightAddress();
				int resultAddr = equality4.getResultAddress();

				String register = getRegister(equality4);

				// MOV reg left
				Assemble a1 = new Assemble();
				a1.setLabel(assembleList.size());
				a1.setOpreator("MOV");
				a1.setLeftObj(register);
				a1.setRightObj(symbolList.get(rightAddr).getName());
				assembleList.add(a1);
				// CMP reg right
				Assemble a2 = new Assemble();
				a2.setLabel(assembleList.size());
				a2.setOpreator("MOV");
				a2.setLeftObj(register);
				a2.setRightObj(symbolList.get(rightAddr).getName());
				assembleList.add(a2);
				// JL(小于转j<)/JLE(小于等于转j<=)/JG(大于转j>)/JGE(大于等于转j>=)/JNZ(不等转j<>)/JZ(等于转j=)
				Assemble a3 = new Assemble();
				a3.setLabel(assembleList.size());
				a3.setLeftObj(symbolList.get(resultAddr).getName());
				switch (op) {
				case 53:// j<
					a3.setOpreator("JL");
					break;
				case 54:// j<=
					a3.setOpreator("JLE");
					break;
				case 57:// j>
					a3.setOpreator("JG");
					break;
				case 58:// j>=
					a3.setOpreator("JGE");
					break;
				case 55:// j<>
					a3.setOpreator("JNZ");
					break;
				case 56:// j=
					a3.setOpreator("JZ");
					break;
				}
				assembleList.add(a3);
				// 清相关信息链
				// delSymbolOnceInfoLink(symbolList.get(leftAddr).getName(),
				// symbolList.get(rightAddr).getName());
			}
			// + - * /
			else if (op == 43 || op == 45 || op == 41 || op == 48) {
				int leftAddr = equality4.getLeftAddress();
				int rightAddr = equality4.getRightAddress();

				String register = getRegister(equality4);

				// MOV reg left
				// 此处在分配寄存器的时候已经移动了左操作数，这里不必再生成一遍
				// Assemble a1 = new Assemble();
				// a1.setLabel(assembleList.size());
				// a1.setOpreator("MOV");
				// a1.setLeftObj(register);
				// a1.setRightObj(symbolList.get(leftAddr).getName());
				// assembleList.add(a1);
				// op reg right
				Assemble a2 = new Assemble();
				a2.setLabel(assembleList.size());
				a2.setLeftObj(register);
				a2.setRightObj(symbolList.get(rightAddr).getName());
				switch (op) {
				case 43: // +
					a2.setOpreator("ADD");
					break;
				case 45: // -
					a2.setOpreator("SUB");
					break;
				case 41: // *
					a2.setOpreator("MUL");
					break;
				case 48: // /
					a2.setOpreator("DIV");
					break;
				}
				assembleList.add(a2);

				// 清相关信息链
				// delSymbolOnceInfoLink(symbolList.get(leftAddr).getName(),
				// symbolList.get(rightAddr).getName());
			}
			// :=
			else if (op == 51) {
				int resultAddr = equality4.getResultAddress();
				String register = getRegister(equality4);
				Assemble assemble = new Assemble();
				assemble.setLabel(assembleList.size());
				assemble.setOpreator("MOV");// 注意以下迪操作数互换位置
				assemble.setLeftObj(symbolList.get(resultAddr).getName());
				assemble.setRightObj(register);
				assembleList.add(assemble);
				// 清相关信息链
				// delSymbolOnceInfoLink(symbolList.get(leftAddr).getName(), "");
			}
		}
	}

	/**
	 * ----- 内部使用，多次调用 ----- 删除同名left/right的symbol代用信息链的栈顶（即此时四元式的对应的）
	 * 
	 * @param leftName
	 * @param rightName
	 */
	private void delSymbolOnceInfoLink(String leftName, String rightName) {
		for (Symbol symbol : symbolList) {
			if (symbol.getName().equals(leftName)) {
				// 移除栈顶（从后往前push待用信息），所以越靠近顶对应的式子序号越小
				if (symbol.getInfoLink().size() > 1) {
					symbol.getInfoLink().remove(0);
				}
			}
			if (symbol.getName().equals(rightName)) {
				// 移除栈顶（从后往前push待用信息），所以越靠近顶对应的式子序号越小
				if (symbol.getInfoLink().size() > 1) {
					symbol.getInfoLink().remove(0);
				}
			}
		}
	}
}
