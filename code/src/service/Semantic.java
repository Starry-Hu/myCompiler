package service;

import java.util.ArrayList;

import bean.Equality4;
import bean.S;
import bean.E;
import bean.Symbol;
import bean.Token;

/**
 * 语义分析
 * 
 * @author StarryHu
 *
 */
public class Semantic {
	// token表
	ArrayList<Token> tokenList = new ArrayList<>();
	// symbol符号表
	ArrayList<Symbol> symbolList = new ArrayList<>();
	// 四元式序列表 -- 下一个可用地址nextquad = equality4List.size()// 填充一个对象占了0号位，所以大小就是下次的位置
	ArrayList<Equality4> equality4List = new ArrayList<>();
	// 错误信息
	String error = "";

	// 扫描token表的下标
	private int i = 1;
	// 临时变量下标
	private int ti = 1;
	// 某次运算对象的地址（具有传递性）
	private int tt;

	/**
	 * token表当前指针是否可前移，true时指针已经前移了一位；false表示到达了token表的最后一项
	 */
	private void next() {
		if (i < tokenList.size() - 1) {
			i++;
		}
	}

	/**
	 * token表当前指针是否可后退一位，true时指针已经后退了一位；false表示到达了token表的最前一项
	 */
	private void before() {
		if (i > 0) {
			i--;
		}
	}

	/**
	 * 创建临时变量，返回它在符号表的编号地址
	 * @return
	 */
	private int newTemp() {
		// symbol表当前的大小就是下一次要存放的位置
		int temp = symbolList.size();
		// 存入symbol表中
		Symbol symbol = new Symbol();
		symbol.setName("T" + ti);
		symbol.setNumber(temp);
		symbolList.add(symbol);
		ti++;
		return temp;
	}

	/**
	 * 完成四元式中跳转语句转移目标的回填
	 * 
	 * @param addr
	 * @param addr2
	 */
	private void backPatch(int updateWhich, int updateValue) {
		if (updateWhich < equality4List.size()) {
			// 把updateWhich对应四元式的第四分量都改写为地址updateValue
			equality4List.get(updateWhich).setResultAddress(updateValue);
		}
	}

	/**
	 * 产生四元式
	 * 
	 * @param operator
	 * @param leftAddress
	 * @param rightAddress
	 * @param resultAddress
	 */
	private void emit(int operator, int leftAddress, int rightAddress, int resultAddress) {
		Equality4 equality4 = new Equality4();
		equality4.setLabel(equality4List.size());
		equality4.setOperator(operator);
		equality4.setLeftAddress(leftAddress);
		equality4.setRightAddress(rightAddress);
		equality4.setResultAddress(resultAddress);
		// 将新生成的四元式表项添加到四元式列表中
		equality4List.add(equality4);
	}

	/**
	 * 传入词法分析产生的token文件和symbol文件，进行语法分析
	 * 注意！ 对四元式序列表填充一个对象，使其起始位置从1开始
	 * @param tokenList
	 * @param symbolList
	 */
	public void initial(ArrayList<Token> tokenList, ArrayList<Symbol> symbolList) {
		this.tokenList = tokenList;
		this.symbolList = symbolList;
		
		Equality4 equality4 = new Equality4();
		equality4List.add(equality4);
	}

	public void showResult() {
		String output = "";

		System.out.println("------------------ 语义分析更新symbol表如下： ------------------");
		for (int i = 1; i < symbolList.size(); i++) {
			Symbol symbol = symbolList.get(i);
			output += symbol.toString() + "\r\n";
			System.out.println(symbol.toString());
		}
		TxtTool.writeFile(output, "symbolSemantic.txt");
		
		System.out.println("------------------ 语义分析得到的四元式序列如下： ------------------");
		output = "";
		for (int i = 1; i < equality4List.size(); i++) {
			Equality4 equality4 = equality4List.get(i);
			output += equality4.toString() + "\r\n";
			System.out.println(equality4.toString());
		}
		TxtTool.writeFile(output, "equality4Semantic.txt");
		
	}
	/**
	 * --- 主要处理的代码块，调用 ---
	 */
	public void implement() {
		// 含有program
		if (tokenList.get(i).getType() == 12) {
			next();
			// 是标识符，作为程序名字
			if (tokenList.get(i).getType() == 18) {
				next();
				// 判断是否有;结束
				if (tokenList.get(i).getType() == 30) {
					next();
					programBody();// 执行程序体
				}else {
					error = "该程序program方法名后缺少： ;";
				}
			} else {
				error = "该程序program缺少方法名";
			}
		} else {
			error = "该程序缺少关键字：program";
		}
	}

	/**
	 * 程序体programBody //〈程序体〉→〈变量说明〉〈复合句〉//〈变量说明〉→ var〈变量定义〉|ε
	 */
	private void programBody() {
		// 如果是var，执行变量定义
		if (tokenList.get(i).getType() == 16) {
			next();
			varDefine();
		}
		// 如果是begin，执行复合句
		else if (tokenList.get(i).getType() == 2) {
			next();
			complexSentence();
		}
		// 否则缺少var或begin，报错
		else {
			error = "程序体缺少var或begin";
		}
	}

	/**
	 * 变量定义varDefine//〈变量定义〉→〈标识符表〉:〈类型〉;｜〈标识符表〉:〈类型〉;〈变量定义〉
	 */
	private void varDefine() {
		// 如果该字符为标识符，则判断下一个字符。若为冒号，继续判断类型定义是否正确
		if (isIdlist()) {
			next();
			if (tokenList.get(i).getType() == 29) { // :
				next();
				// 类型限定为integer或bool或real，定义正确时，在symbol表中记录该标识符的定义类型
				if (tokenList.get(i).getType() == 9 || tokenList.get(i).getType() == 3
						|| tokenList.get(i).getType() == 13) {
					int j = i;
					j = j - 2;
					// 获取当前标识符在symbol表中的地址，给各标识符在symbol表中赋定义类型
					int index = tokenList.get(j).getAddress();
					symbolList.get(index).setType(tokenList.get(i).getType());
					j--;

					// 从后往前看，若标识符前面有逗号，表示同时定义了几个相同类型的变量，将他们都在symbol表中进行赋定义类型
					while (tokenList.get(j).getType() == 28) {
						j--; // 跳过,
						index = tokenList.get(j).getAddress();
						symbolList.get(index).setType(tokenList.get(i).getType());
						j--; // 跳过当前处理的
					}

					next();
					// 如果是分号，判断下一个单词。若为begin，执行复合句；否则继续循环执行变量定义
					if (tokenList.get(i).getType() == 30) {// ;
						next();
						if (tokenList.get(i).getType() == 2) { // begin，执行复合句
							next();
							complexSentence();
						} else if (tokenList.get(i).getType() == 16){ // var，继续变量定义
							next();
							varDefine();// 继续执行变量定义
						} else {
							error = "变量定义结束后随语句错误";
						}
					} else {
						error = "变量定义后面缺少；";
					}
				} else {
					error = "变量定义缺少类型或类型定义错误";
					return;
				}
			} else {
				error = "var后面缺少冒号";
			}
		} else {
			error = "变量定义标识符出错";
		}
	}

	/**
	 * 判断是不是标识符表 isIdlist//〈标识符表〉→〈标识符〉，〈标识符表〉｜〈标识符〉
	 * 
	 * @return
	 */
	private boolean isIdlist() {
		if (tokenList.get(i).getType() == 18)// 标识符
		{
			// 若是标识符，判断下一个字符，如果是逗号，继续判断下一个字符
			// 如果不是逗号，指向前一个字符，返回true，否则返回false —— 此方法用来判断是否将几个标识符变量定义为同一个类型
			next();
			if (tokenList.get(i).getType() == 28) {
				next();
				return isIdlist();
			}
			// 指向前一个标识符，表示是一个标识符表，返回true
			else {
				before();
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * ----------------有问题 复合句complexSentence//〈复合句〉→ begin〈语句表〉end
	 */
	private void complexSentence() {
		sentenceList();// 执行语句表
		if (error.equals("")) {
			if (tokenList.get(i).getType() == 30 && tokenList.get(i + 1).getType() == 6)// ;end
			{
				return;
			} else {
				error = "复合句末尾缺少end 或end前一语句末尾缺少;";
			}
		}
	}

	/**
	 * 语句表sentenceList//〈语句表〉→〈执行句〉；〈语句表〉｜〈执行句〉
	 */
	private void sentenceList() {
		S s = new S();
		executeSentence(s);// 执行句
		if (error.equals("")) {
			next();
			if (tokenList.get(i).getType() == 30)// 若为分号，继续执行语句表
			{
				next();
				sentenceList();
			} else {
				before();
			}
		}
	}

	/**
	 * 执行句executeSentence//〈执行句〉→〈简单句〉｜〈结构句〉//〈简单句〉→〈赋值句〉
	 */
	private void executeSentence(S s) {
		// 如果是标识符，为简单句，之后推出赋值句
		if (tokenList.get(i).getType() == 18) {
			next();
			assignSent(s);
		}
		// 如果出现了begin/if/while，则之后为结构句的处理
		else if (tokenList.get(i).getType() == 2 || tokenList.get(i).getType() == 8
				|| tokenList.get(i).getType() == 17) {
			structureSentence(s);
		} else {
			before();// 回退一个
		}
	}

	/**
	 * 赋值句assignSent//〈赋值句〉→〈变量〉：＝〈表达式〉
	 */
	private void assignSent(S s) {
		if (tokenList.get(i).getType() == 31)// :=
		{
			// assignIdAddr记录上一个token文件项的地址，即被赋值标识符的地址
			int assignIdAddr = tokenList.get(i - 1).getAddress();
			next();
			expression();// 表达式
			emit(51, tt, 0, assignIdAddr);// 生成四元式，即assignId:=tt
			
			// s.next初始化， equality4List.size = nextquad
			s.getNext().add(equality4List.size());
		} else {
			error = "赋值句变量后缺少：=";
		}
	}

	/**
	 * 表达式expression//〈表达式〉→〈算术表达式〉｜〈布尔表达式〉
	 */
	private void expression() {
		// false或true或 为保留字且在symbol表中的类型为bool型
		if (tokenList.get(i).getType() == 7 || tokenList.get(i).getType() == 15 || (tokenList.get(i).getAddress() != -1
				&& symbolList.get(tokenList.get(i).getAddress()).getType() == 3)) {

			E e = new E(); // 记录布尔表达式的两类出口
			boolExpression(e);// 布尔表达式
		} else {
			arithExpression();// 算术表达式
		}
	}

	/** 布尔表达式优先级关系为not>and>or,在方法布尔表达式（or）、布尔项（and）、布尔因子（not）中有所体现 **/
	/**
	 * 布尔表达式boolExpression//〈布尔表达式〉→〈布尔表达式〉or M 〈布尔项〉｜〈布尔项〉
	 */
	private void boolExpression(E e) {
		// E → E1 or M E2 | E1 （此处的E1为布尔项）

		E e1 = new E();
		boolItem(e1);// 布尔项

		if (error.equals("")) {
			next();
			// 如果是or，执行E → E1 or M E2
			if (tokenList.get(i).getType() == 11) {
				// 记录四元式表项的数量值，即地址M.quad
				int mquad = equality4List.size();
				E e2 = new E();
				next();
				boolExpression(e2);

				// 合并出口到e中
				// 即e.t={e1.t,e2.t}
				e.getTrueExits().addAll(e1.getTrueExits());
				e.getTrueExits().addAll(e2.getTrueExits());
				// 即e.f=e2.f
				e.setFalseExits(e2.getFalseExits());
				
				// 执行回填函数,即backpatch(e1.false,m.quad)
				for (int e1_false : e1.getFalseExits()) {
					backPatch(e1_false, mquad);
				}
			}
			// 如果不是or，执行的E → E1，直接e.t=e1.t;e.f=e1.f
			// 注意Java的引用传值，此处不允许直接赋=
			else {
				e.setTrueExits(e1.getTrueExits());
				e.setFalseExits(e1.getFalseExits());
				before();
			}
		}
	}

	/**
	 * 布尔项boolItem//〈布尔项〉→〈布尔项〉and M〈布尔因子〉｜〈布尔因子〉
	 */
	private void boolItem(E e) {
		E e1 = new E();

		boolFactor(e1);// 布尔因子
		if (error.equals("")) {
			next();
			if (tokenList.get(i).getType() == 1)// and
			{
				int mquad = equality4List.size();
				next();
				E e2 = new E();
				boolItem(e2);// 执行布尔项

				// 合并出口到e中
				// 即e.t=e2.t
				e.setTrueExits(e2.getTrueExits());
				// 即e.f={e1.f,e2.f}
				e.getFalseExits().addAll(e1.getFalseExits());
				e.getFalseExits().addAll(e2.getFalseExits());

				for (int e1_true : e1.getTrueExits()) {
					// 执行回填函数,把链首k所链接的每个四元式的第四分量都改写为地址m;即backpatch(e1.false,m.quad)
					backPatch(e1_true, mquad);
				}
			}

			// 如果不是and，直接e.t=e1.t,e.f=e1.f
			else {
				e.setTrueExits(e1.getTrueExits());
				e.setFalseExits(e1.getFalseExits());
				before();
			}
		}
	}

	/**
	 * 布尔因子boolFactor//〈布尔因子〉→ not〈布尔因子〉｜〈布尔量〉
	 */
	private void boolFactor(E e) {
		// 是not
		if (tokenList.get(i).getType() == 10) {
			next();
			E e1 = new E();
			boolFactor(e1);// 布尔因子
			// 合并出口到e中
			// 即e.t=e1.f
			e.setTrueExits(e1.getFalseExits());
			// 即e.f=e1.t
			e.setFalseExits(e1.getTrueExits());
		}
		// 不是not
		else {
			E e1 = new E();
			boolValue(e1);// 布尔量
			// 合并出口到e中
			// 即e.f=e1.f
			e.setFalseExits(e1.getFalseExits());
			// 即e.t=e1.t
			e.setTrueExits(e1.getTrueExits());
		}
	}

	/**
	 * 布尔量boolValue//〈布尔量〉→〈布尔常数〉｜〈标识符〉｜（〈布尔表达式〉）｜〈关系表达式〉
	 */
	private void boolValue(E e) {
		// 如果是true或false，布尔常数
		if (tokenList.get(i).getType() == 15 || tokenList.get(i).getType() == 7) {
			// 合并出口到e中
			// 即e.t=nextquad,e.f=nextquad+1
			int nextquad = equality4List.size();
			e.getTrueExits().add(nextquad);
			e.getFalseExits().add(nextquad + 1);
			// tt记录布尔量的地址
			tt = tokenList.get(i).getAddress();
		}

		// 如果是标识符（关系表达式 / ）
		else if (tokenList.get(i).getType() == 18) {
			next();

			// 若其后紧跟相关关系运算符，要求是==或<=或<或<>或>或>=，则进入布尔表达式的选项处理
			if (tokenList.get(i).getType() == 32 || tokenList.get(i).getType() == 33 || tokenList.get(i).getType() == 34
					|| tokenList.get(i).getType() == 35 || tokenList.get(i).getType() == 36
					|| tokenList.get(i).getType() == 37) {
				next();
				// 如果是运算符，后需再接标识符/常数/实数。 e.t=nextquad，e.f=nextquad+1
				if (tokenList.get(i).getType() == 18 || tokenList.get(i).getType() == 19 || tokenList.get(i).getType() == 20) {
					// 合并出口  e.t=nextquad，e.f=nextquad+1
					int nextquad = equality4List.size();
					e.getTrueExits().add(nextquad);
					e.getFalseExits().add(nextquad + 1);
					// 生成四元式，即a<b的四元式为(j<,a,b,0)，需要根据前一项运算符来匹配op值。
					int op = 0;
					switch (tokenList.get(i-1).getType()) {
					case 32:// =
						op = 56;
						break;
					case 33:// <=
						op = 54;
						break;
					case 34:// <
						op = 53;
						break;
					case 35:// <>
						op = 55;
						break;
					case 36:// >
						op = 57;
						break;
					case 37:// >=
						op = 58;
						break;
					}
					emit(op, tokenList.get(i - 2).getAddress(),tokenList.get(i).getAddress(), 0);// 真执行
					emit(52, 0, 0, 0);// -- 假执行 -- 需要块结束后回填

				} else {
					error = "关系运算符后缺少标识符/整数/实数";
				}
			}

			// 只有一个标识符，说明该标识符为布尔型，则e.t=nextquad，e.f=nextquad+1
			else {
				before();
				// 合并出口 e.t=nextquad，e.f=nextquad+1
				int nextquad = equality4List.size();
				e.getTrueExits().add(nextquad);
				e.getFalseExits().add(nextquad + 1);
				// 生成四元式,即E—>a的四元式为(jnz,a,_,0)
				emit(55, tokenList.get(i).getAddress(), 0, 0);
				// 生成四元式，(j,_,_,0)
				emit(52, 0, 0, 0);
				next();
			}
		}
		// 如果字符为(，即布尔表达式。定义E—>(E1)
		else if (tokenList.get(i).getType() == 21) {
			E e1 = new E();
			boolExpression(e1);// 执行布尔表达式
			// 合并出口
			e.setTrueExits(e1.getTrueExits());
			e.setFalseExits(e1.getFalseExits());

			// 配套)字符结束
			if (tokenList.get(i).getType() == 22) {
				return;
			} else {
				error = "布尔量中的布尔表达式缺少一个）";
			}
		} else {
			error = "布尔量出错";
		}
	}

	/** 运算符优先级关系为()>*或\>+或-,在方法算术表达式（+或-）、项（*或\）、因子（()）中有所体现 **/
	/**
	 * 算术表达式 arithExpression//〈算术表达式〉→〈算术表达式〉＋〈算术项〉｜〈算术表达式〉－〈算术项〉｜〈算术项〉
	 */
	private void arithExpression() {
		// 必有算术项
		arithItem();
		if (error.equals("")) {
			next();
			// 如果其后有符号，且为+或-，再执行算术表达式
			if (tokenList.get(i).getType() == 23 || tokenList.get(i).getType() == 24) {
				// left记录该运算符前面的变量的地址
				int left = tokenList.get(i - 1).getAddress();
				// 若符号为)，说明前面是一个算术表达式，用上次运算得到的tt代替其地址
				if (tokenList.get(i - 1).getType() == 22) {
					left = tt;
				}

				// 设置+-运算的速记码
				int op = 0;
				if(tokenList.get(i).getType() == 23) {
					op = 43;
				}else {
					op = 45;
				}
				
				next();
				arithExpression();

				// 生成四元式，即x:=y+z的前半部分运算的四元式：(+,y,z,T1)
				int temp = newTemp();
				emit(op, left, tt, temp);
				tt = temp;
			} else {
				before();
				return;
			}
		} else {
			return;
		}
	}

	/**
	 * 算术项 arithItem//〈算术项〉→〈算术项〉*〈因子〉｜〈算术项〉/〈因子〉｜〈因子〉
	 */
	private void arithItem() {
		arithFactor();// 执行因子
		if (error.equals("")) {
			next();
			if (tokenList.get(i).getType() == 25 || tokenList.get(i).getType() == 26)// 符号为*或/
			{
				// left记录该运算符前面的变量的地址
				int left = tokenList.get(i - 1).getAddress();
				// 若符号为)，说明前面是一个算术表达式，用上次运算得到的tt代替其地址
				if (tokenList.get(i - 1).getType() == 22) {
					left = tt;
				}

				// 设置*/运算的速记码
				int op = 0;
				if(tokenList.get(i).getType() == 25) {// *
					op = 41;
				}else { // /
					op = 48;
				}
				
				next();
				arithItem();

				// 生成四元式，即x:=y*z的前半部分运算的四元式：(*,y,z,T1)
				int temp = newTemp();
				emit(op, left, tt, temp);
				tt = temp;
			} else {
				before();
				return;
			}
		} else {
			return;
		}
	}

	/**
	 * 算术因子arithFactor//〈算术因子〉→〈算术量〉｜ (〈算术表达式〉)
	 */
	private void arithFactor() {
		if (tokenList.get(i).getType() == 21)// 字符为(
		{
			next();
			arithExpression();// 执行算术表达式
			next();
			if (tokenList.get(i).getType() == 22)// 字符为)
			{
				return;
			} else {
				error = "算术因子中算数表达式缺少）";
			}
		} else {
			arithValue();// 执行算术量
		}
	}

	/**
	 * 算术量arithValue//〈算术量〉→〈标识符〉｜〈整数〉｜〈实数〉
	 */
	private void arithValue() {
		// 要求为标识符或整数或实数
		if (tokenList.get(i).getType() == 18 || tokenList.get(i).getType() == 19 || tokenList.get(i).getType() == 20) {
			// 记录变量地址
			tt = tokenList.get(i).getAddress();
		} else {
			error = "算术量出错";
		}
	}

	/**
	 * 结构句 structureSentence//〈结构句〉→〈复合句〉｜〈if句〉｜〈WHILE句〉
	 */
	private void structureSentence(S s) {
		// 如果是begin，执行复合句
		if (tokenList.get(i).getType() == 2) {
			next();
			complexSentence();
		}
		// 如果是if，执行if语句
		else if (tokenList.get(i).getType() == 8) {
			next();
			ifSentence(s);
		}
		// 如果是while，执行while语句
		else if (tokenList.get(i).getType() == 17) {
			next();
			whileSentence(s);
		}
	}

	/**
	 * if语句ifSent// 〈if句〉→if〈布尔表达式〉then〈执行句〉| if〈布尔表达式〉then〈执行句〉else〈执行句〉
	 */
	private void ifSentence(S s) {
		// S→if E then M S1 | S→if E then M1 S1 N else M2 S2
		E e = new E();// 该if语句块的两类出口
		boolExpression(e);

		if (error.equals("")) {
			next();
			// 如果是then，之后对执行句进行处理
			if (tokenList.get(i).getType() == 14) {
				int mquad1 = equality4List.size();
				S s1 = new S();
				next();
				executeSentence(s1);// 执行句
				next();

				// 若有else，则里面还需对else对应的执行句进行处理
				if (tokenList.get(i).getType() == 5) {
					// 若N—>ε,n.next=nextquad,并生成四元式(j,_,_,0)
					S n = new S();
					n.getNext().add(equality4List.size());
					emit(52, 0, 0, 0);

					int mquad2 = equality4List.size();

					S s2 = new S();
					next();
					executeSentence(s2); // 执行执行句

					// 合并next
					s.getNext().addAll(s1.getNext());
					s.getNext().addAll(n.getNext());
					s.getNext().addAll(s2.getNext());

					// 回填backpath(E.true,M1.quad)
					for (int e_true : e.getTrueExits()) {
						backPatch(e_true, mquad1);
					}

					// 回填backpath(E.false,M2.quad)
					for (int e_false : e.getFalseExits()) {
						backPatch(e_false, mquad2);
					}
					
					// --- 整个块结束，跳出块 ---
					for (int s_next : s.getNext()) {
						backPatch(s_next, equality4List.size());
					}
				} 
				// 没有else情况的
				else {
					// 回填backpath(E.true,M.quad)
					for (int e_true : e.getTrueExits()) {
						backPatch(e_true, mquad1);
					}
					// 合并出口
					s.setNext(e.getFalseExits());
					s.getNext().addAll(s1.getNext());
					
					// --- 整个块结束，跳出块 ---
					for (int s_next : s.getNext()) {
						backPatch(s_next, equality4List.size());
					}
					before();
				}
			} else {
				error = "if...then语句缺少then";
			}
		} else {
			error = "if语句布尔表达式出错";
		}
	}

	/**
	 * while语句 whileSentence//〈while句〉→while〈布尔表达式〉do〈执行句〉
	 */
	private void whileSentence(S s) {
		// S → while M1 E〈布尔表达式〉 do M2 S1〈执行句〉
		int mquad1 = equality4List.size();
		E e = new E();
		boolExpression(e);
		
		if (error.equals("")) {
			next();
			// 与while配套内部语句需要有do引导一个执行句
			if (tokenList.get(i).getType() == 4) {
				int mquad2 = equality4List.size();
				S s1 = new S();
				next();
				executeSentence(s1);

				// 回填
				for (int s1_next : s1.getNext()) {
					backPatch(s1_next, mquad1);
				}
				for (int e_true : e.getTrueExits()) {
					backPatch(e_true, mquad2);
				}
			
				// 合并next
				s.setNext(e.getFalseExits());
				// 生成四元式(j,_,_,mquad1)
				emit(52, 0, 0, mquad1);
				
				// --- 整个块结束，跳出块 ---
				for (int s_next : s.getNext()) {
					backPatch(s_next, equality4List.size());
				}
			} else {
				error = "while语句缺少do";
			}
		}
	}
}
