package service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import bean.Assemble;
import bean.Equality4;
import bean.Symbol;
import bean.Token;

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
	
	// 入口四元式序号数组
	private ArrayList<Integer> entranceNum = new ArrayList<>(); 
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
		divideBlock();
		for(Equality4 equality4 : equality4List) {
			System.out.println(equality4.toString() + equality4.isEntrance());
		}
		System.out.println();
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
		// 显示生成的待用信息
		for (int i = 1; i < symbolList.size(); i++) {
			System.out.println(symbolList.get(i).toStringWithInfoLink());
		}
	}

	/**
	 * 划分基本块
	 */
	private void divideBlock() {
		// 第一条语句作为入口
		equality4List.get(1).setEntrance(true);
		entranceNum.add(1);
		
		// 遍历处理每个语句
		for (int i = 1; i < equality4List.size(); i++) {
			int op = equality4List.get(i).getOperator();
			// 转移语句的op速记符，且转移到的地址不为0(真实存在)。设置其转移到的语句为入口
			if ((op <= 58 && op >= 52)) {
				int turnAddr = equality4List.get(i).getResultAddress();
				if (turnAddr != 0 && turnAddr < equality4List.size()) {
					equality4List.get(turnAddr).setEntrance(true);
					entranceNum.add(turnAddr);
				}

				// 如果是条件转移，则其后第一个语句为入口
				if (op != 52) {
					equality4List.get(i + 1).setEntrance(true);
					entranceNum.add(i + 1);
				}
			}
		}
		// 对各入口序号排序
		Collections.sort(entranceNum);
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
	private void createTarget() {
		// 遍历每个四元式
		for (Equality4 equality4 : equality4List) {
			int op = equality4.getOperator();
			int leftAddr = equality4.getLeftAddress();
			int rightAddr = equality4.getRightAddress();
			int resultAddr = equality4.getResultAddress();
			
			String leftName = symbolList.get(leftAddr).getName();
			String rightName = symbolList.get(rightAddr).getName();
			String resultName = symbolList.get(resultAddr).getName();
			
			// j 对于无条件跳转，跳转序号只与四元式的结果数有关，且放入目标代码的左操作数中
			if (op == 52) {
				Assemble assemble = new Assemble();
				// JMP x _
				assemble.setLabel(assembleList.size());
				assemble.setOpreator("JMP");
				assemble.setLeftObj(resultAddr + "");// 转移的目标地址
				assembleList.add(assemble);
			}
			// j< j<= j> j>= j<> j=
			else if (op == 53 || op == 54 || op == 57 || op == 58 || op == 55 || op == 56) {
				String register = getRegister(equality4);

				// MOV reg left
				Assemble a1 = new Assemble();
				a1.setLabel(assembleList.size());
				a1.setOpreator("MOV");
				a1.setLeftObj(register);
				a1.setRightObj(leftName);
				assembleList.add(a1);
				// CMP reg right
				Assemble a2 = new Assemble();
				a2.setLabel(assembleList.size());
				a2.setOpreator("CMP");
				a2.setLeftObj(register);
				a2.setRightObj(rightName);
				assembleList.add(a2);
				// JL(小于转j<)/JLE(小于等于转j<=)/JG(大于转j>)/JGE(大于等于转j>=)/JNZ(不等转j<>)/JZ(等于转j=)
				// 第三步的跳转序号与四元式的结果数有关，且放入目标代码的左操作数中
				Assemble a3 = new Assemble();
				a3.setLabel(assembleList.size());
				a3.setLeftObj(resultName);
				a3.setRightObj(" ");
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
			}
			// + - * /
			else if (op == 43 || op == 45 || op == 41 || op == 48) {
				String register = getRegister(equality4);

				// MOV reg left
				// 此处在分配寄存器的时候已经移动了左操作数，这里不必再生成一遍
				Assemble a1 = new Assemble();
				a1.setLabel(assembleList.size());
				a1.setLeftObj(register);
				a1.setRightObj(rightName);
				switch (op) {
				case 43: // +
					a1.setOpreator("ADD");
					break;
				case 45: // -
					a1.setOpreator("SUB");
					break;
				case 41: // *
					a1.setOpreator("MUL");
					break;
				case 48: // /
					a1.setOpreator("DIV");
					break;
				}
				assembleList.add(a1);
				
				// 如果左右操作数在register中，需要删去（此时运算后变成了结果数）
				if (isSaveIn(leftName, register) || isSaveIn(rightName, register) ) {
					for(Symbol symbol : symbolList) {
						if (symbol.getName().equals(leftName) || symbol.getName().equals(rightName) ) {
							// 检测同名register并将其从存储信息中删掉
							symbol.getSaveValue().remove(register);
							// 将该register的左操作数删去
							if (register.equals("bx")) {
								bx.remove(leftName);
								bx.remove(rightName);
							}else if (register.equals("dx")) {
								dx.remove(leftName);
								bx.remove(rightName);
							}
						}
						
					}
				}
			}
			// :=
			else if (op == 51) {
				String register = getRegister(equality4);
				Assemble assemble = new Assemble();
				assemble.setLabel(assembleList.size());
				assemble.setOpreator("MOV");// 注意以下迪操作数互换位置
				assemble.setLeftObj(resultName);
				assemble.setRightObj(register);
				assembleList.add(assemble);
				// 清相关信息链
				// delSymbolOnceInfoLink(symbolList.get(leftAddr).getName(), "");
			}
			
			// 生成完这一句四元式对应的目标代码之后，检测Y,Z是否之后非待用/非活跃
			// 如果是则将它所处的寄存器给释放
			delSymbolOnceInfoLink(leftName, rightName);
		}
	}

	/**
	 * 删除同名left/right的symbol代用信息链的栈顶，看知乎是否还被使用
	 * 若未再使用，则释放它占用的寄存器
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
				// 查看是否之后还有使用，若未使用则释放其占用的寄存器
				if (symbol.getInfoLink().isEmpty()) {
					symbol.getSaveValue().remove("bx");
					bx.remove(leftName);
					symbol.getSaveValue().remove("dx");
					dx.remove(leftName);
				}
			}
			if (symbol.getName().equals(rightName)) {
				// 移除栈顶（从后往前push待用信息），所以越靠近顶对应的式子序号越小
				if (symbol.getInfoLink().size() > 1) {
					symbol.getInfoLink().remove(0);
				}
				// 查看是否之后还有使用，若未使用则释放其占用的寄存器
				if (symbol.getInfoLink().isEmpty()) {
					symbol.getSaveValue().remove("bx");
					bx.remove(rightName);
					symbol.getSaveValue().remove("dx");
					dx.remove(rightName);
				}
			}
		}
	}
	
}
