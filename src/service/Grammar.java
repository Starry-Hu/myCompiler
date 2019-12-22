package service;

import java.util.ArrayList;

import bean.Symbol;
import bean.Token;

public class Grammar {
	// token表
	ArrayList<Token> tokenList = new ArrayList<>();
	// symbol符号表
	ArrayList<Symbol> symbolList = new ArrayList<>();
	// 错误记录表
	ArrayList<String> errorList = new ArrayList<>();

	// 扫描下标
	private int i;
	// 错误信息
	String error;

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
	 * 传入词法分析产生的token文件和symbol文件，进行语法分析
	 * 
	 * @param morphology
	 */
	public void initial(ArrayList<Token> tokenList, ArrayList<Symbol> symbolList) {
		this.tokenList = tokenList;
		this.symbolList = symbolList;
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
				programBody();// 执行程序体
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

					}

					next();
					// 如果是分号，判断下一个单词。若为begin，执行复合句；否则继续循环执行变量定义
					if (tokenList.get(i).getType() == 30) {// ;
						next();
						if (tokenList.get(i).getType() == 2) { // begin，执行复合句
							next();
							complexSentence();
						} else {
							varDefine();// 继续执行变量定义
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
		if (error == "") {
			if (tokenList.get(i).getType() == 30 && tokenList.get(i + 1).getType() == 6)// end
			{
				return;
			} else {
				error = "复合句末尾缺少end";
			}
		}
	}

	/**
	 * 语句表sentenceList//〈语句表〉→〈执行句〉；〈语句表〉｜〈执行句〉
	 */
	private void sentenceList() {
		executeSentence();// 执行句
		if (error == "") {
			next();
			if (tokenList.get(i).getType() == 30)// 若为分号，继续循环执行语句表
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
	private void executeSentence() {
		// 如果是标识符，为简单句，之后推出赋值句
		if (tokenList.get(i).getType() == 18) {
			next();
			assignSent();
		}
		// 如果出现了begin/if/while，则之后为结构句的处理
		else if (tokenList.get(i).getType() == 2 || tokenList.get(i).getType() == 8
				|| tokenList.get(i).getType() == 17) {
			structureSentence();
		} else {
			before();// 回退一个
		}
	}

	/**
	 * 赋值句assignSent//〈赋值句〉→〈变量〉：＝〈表达式〉
	 */
	private void assignSent() {
		if (tokenList.get(i).getType() == 31)// :=
		{
			next();
			expression();// 表达式
		} else {
			error = "赋值句变量后缺少：=";
		}
	}

	/**
	 * 表达式expression//〈表达式〉→〈算术表达式〉｜〈布尔表达式〉
	 */
	private void expression() {
		// false或true或单词为保留字且在符号表中的类型为bool型
		if (tokenList.get(i).getType() == 7 || tokenList.get(i).getType() == 15 || (tokenList.get(i).getAddress() != -1
				&& symbolList.get(tokenList.get(i).getAddress()).getType() == 3)) {
			boolExpression();// 布尔表达式
		} else {
			arithExpression();// 算术表达式
		}
	}

	/**
	 * 布尔表达式boolExpression//〈布尔表达式〉→〈布尔表达式〉or 〈布尔项〉｜〈布尔项〉
	 */
	private void boolExpression() {
		boolItem();// 布尔项
		if (error == "") {
			next();
			if (tokenList.get(i).getType() == 11)// or
			{
				next();
				boolExpression();
			} else {
				before();
			}
		} else {
			return;
		}
	}

	/**
	 * 布尔项boolItem//〈布尔项〉→〈布尔项〉and〈布尔因子〉｜〈布尔因子〉
	 */
	private void boolItem() {
		boolFactor();// 布尔因子
		if (error == "") {
			next();
			if (tokenList.get(i).getType() == 1)// and
			{
				next();
				boolItem();
			} else {
				before();
			}
		}
	}

	/**
	 * 布尔因子boolFactor//〈布尔因子〉→ not〈布尔因子〉｜〈布尔量〉
	 */
	private void boolFactor() {
		if (tokenList.get(i).getType() == 10)// not
		{
			next();
			boolFactor();// 布尔因子
		} else {
			boolValue();// 布尔量
		}
	}

	/**
	 * 布尔量boolValue//〈布尔量〉→〈布尔常数〉｜〈标识符〉｜（〈布尔表达式〉）｜〈关系表达式〉
	 */
	private void boolValue() {
		// 如果是true或false，布尔常数
		if (tokenList.get(i).getType() == 15 || tokenList.get(i).getType() == 7) {
			return;
		}
		// 如果是标识符（关系表达式 / ）
		else if (tokenList.get(i).getType() == 18) {
			next();

			// 若其后紧跟相关关系运算符，要求是==或<=或<或<>或>或>=，则进入布尔表达式的选项处理
			if (tokenList.get(i).getType() == 32 || tokenList.get(i).getType() == 33 || tokenList.get(i).getType() == 34
					|| tokenList.get(i).getType() == 35 || tokenList.get(i).getType() == 36
					|| tokenList.get(i).getType() == 37) {
				next();
				// 运算符后再需再接标识符
				if (tokenList.get(i).getType() == 18) {
				} else {
					error = "关系运算符后缺少标识符";
				}
			} else {
				before();
			}
		}
		// 如果字符为(，即布尔表达式
		else if (tokenList.get(i).getType() == 21) {
			boolExpression();// 执行布尔表达式
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

	/**
	 * 算术表达式 arithExpression//〈算术表达式〉→〈算术表达式〉＋〈算术项〉｜〈算术表达式〉－〈算术项〉｜〈算术项〉
	 */
	private void arithExpression() {
		// 必有算术项
		arithItem();
		if (error == "") {
			next();
			// 如果有符号，要求为+或-，再执行算术表达式
			if (tokenList.get(i).getType() == 23 || tokenList.get(i).getType() == 24) {
				next();
				arithExpression();
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
		if (error == "") {
			next();
			if (tokenList.get(i).getType() == 25 || tokenList.get(i).getType() == 26)// 符号为*或/
			{
				next();
				arithItem();// 执行项
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
			return;
		} else {
			error = "算术量出错";
		}
	}

	/**
	 * 结构句 structureSentence//〈结构句〉→〈复合句〉｜〈if句〉｜〈WHILE句〉
	 */
	private void structureSentence() {
		// 如果是begin，执行复合句
		if (tokenList.get(i).getType() == 2) {
			next();
			complexSentence();
		}
		// 如果是if，执行if语句
		else if (tokenList.get(i).getType() == 8) {
			next();
			ifSentence();
		}
		// 如果是while，执行while语句
		else if (tokenList.get(i).getType() == 17) {
			next();
			whileSentence();
		}
	}

	/**
	 * if语句ifSent// 〈if句〉→if〈布尔表达式〉then〈执行句〉| if〈布尔表达式〉then〈执行句〉else〈执行句〉
	 */
	private void ifSentence() {
		boolExpression();
		if (error == "") {
			next();
			// 如果是then，之后对执行句进行处理
			if (tokenList.get(i).getType() == 14) {
				next();
				executeSentence();// 执行句
				next();
				// 若有else，则里面还需对else对应的执行句进行处理
				if (tokenList.get(i).getType() == 5) {
					next();
					executeSentence();
				} else {
					before();
					return;
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
	private void whileSentence() {
		boolExpression();
		if (error == "") {
			next();
			// 与while配套内部语句需要有do引导一个执行句
			if (tokenList.get(i).getType() == 4) {
				next();
				executeSentence();
			} else {
				error = "while语句缺少do";
			}
		}
	}
}
