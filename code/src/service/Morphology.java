package service;

import java.util.ArrayList;

import bean.Symbol;
import bean.Token;
import tool.TxtTool;
import bean.Error;

/**
 * 词法分析
 * 
 * @author StarryHu
 *
 */
public class Morphology {
	// 存放机内码
	String[] machineCodes = new String[38];
	// token表
	ArrayList<Token> tokenList = new ArrayList<>();
	// symbol符号表
	ArrayList<Symbol> symbolList = new ArrayList<>();
	// 错误记录表
	ArrayList<Error> errorList = new ArrayList<>();

	// 输入字符串
	private String input;
	// 扫描输入字符串的下标
	private int i;
	// 扫描行号
	private int rowNum = 1;

	/**
	 * --- 传入字符串进行初始化 ---
	 *
	 * 注意！此处对token表、symbol表首先填入一个测试字符，使得起始位置从1开始
	 * 所以每个token/symbol其编号值直接为当前表的大小（值对应）
	 * 
	 * @param s
	 */
	public void initial(String s) {
		input = s;
		// 对两表填充第一个字符占0号位
		Token token = new Token();
		token.setLabel(0);
		token.setName("************");
		token.setType(99);
		token.setAddress(-1);
		tokenList.add(token);
		
		Symbol symbol = new Symbol();
		symbol.setNumber(0);
		symbol.setName("************");
		symbol.setType(99);
		symbolList.add(symbol);
		
		makeMachineCodes();
	}

	/**
	 * --- 主要处理的代码块，调用 ---
	 */
	public void implement() {
		while (i < input.length()) {
			char nowChar = input.charAt(i);
			// 如果是空格，判断下一个字符

			if (nowChar == ' ') {
				i++;
				continue;
			}
			// 如果是字母，判断是标识符还是关键字，并处理
			else if (isAlpha(nowChar)) {
				recognizeId();
			}
			// 如果是数字，判断是否是常数并处理
			else if (isDight(nowChar)) {
				recognizeConstant();
			}
			// 如果是/，判断下一个字符
			else if (nowChar == '/') {
				// 若下一个字符是/，说明是注释，一直往后到下一行
				if (input.charAt(i+1) == '/') {
					while (input.charAt(i) != '\r' && i < input.length()) {
						i++;
					}
				}
				// 否则认为是除号，进行除法处理
				else {
					recognizeOtherSym();
				}
			}

			// 如果字符为回车且下一字符为换行时，行数加一，字符加2
			else if (nowChar == '\r' && input.charAt(i + 1) == '\n') {
				rowNum++;
				i++;
				i++;
			}
			// 如果为其他界符，判断是否为其他合法符号并处理
			else if (isDelimiter(nowChar)) {
				recognizeOtherSym();
			}
			// 其他情况出错
			else {
				Error error = new Error();
				error.setRow(rowNum);
				error.setErrorSrc("" + nowChar);
				error.setErrorType("非法字符");
				errorList.add(error);
				i++;
			}

		}
	}

	/**
	 * 定义机内码
	 */
	private void makeMachineCodes() {
		machineCodes[0] = "";
		machineCodes[1] = "and";
		machineCodes[2] = "begin";
		machineCodes[3] = "bool";
		machineCodes[4] = "do";
		machineCodes[5] = "else";
		machineCodes[6] = "end";
		machineCodes[7] = "false";
		machineCodes[8] = "if";
		machineCodes[9] = "integer";
		machineCodes[10] = "not";
		machineCodes[11] = "or";
		machineCodes[12] = "program";
		machineCodes[13] = "real";
		machineCodes[14] = "then";
		machineCodes[15] = "true";
		machineCodes[16] = "var";
		machineCodes[17] = "while";
		machineCodes[18] = "标识符";
		machineCodes[19] = "整数";
		machineCodes[20] = "实数";
		machineCodes[21] = "(";
		machineCodes[22] = ")";
		machineCodes[23] = "+";
		machineCodes[24] = "-";
		machineCodes[25] = "*";
		machineCodes[26] = "/";
		machineCodes[27] = ".";
		machineCodes[28] = ",";
		machineCodes[29] = ":";
		machineCodes[30] = ";";
		machineCodes[31] = ":=";
		machineCodes[32] = "=";
		machineCodes[33] = "<=";
		machineCodes[34] = "<";
		machineCodes[35] = "<>";
		machineCodes[36] = ">";
		machineCodes[37] = ">=";
	}

	/**
	 * 当前指针是否可前移，true时指针已经前移了一位；false表示到达了输入串的末尾
	 */
	private boolean next() {
		if (i < input.length() - 1) {
			i++;
			return true;
		}
		return false;
	}

	/**
	 * 当前指针是否可后退一位，true时指针已经后退了一位；false表示到达了输入串的最前端
	 */
	private boolean before() {
		if (i > 0) {
			i--;
			return true;
		}
		return false;
	}

	/**
	 * 以数字开头，识别整数还是实数 (都是常数，需加入symbol表)
	 */
	private void recognizeConstant() {
		String str = input.substring(i, i + 1);

		// 设置两个标志位，flag记录是否发生错误，point记录为实数（true）还是整数（false）
		boolean flag = true;
		boolean point = true;

		// ---- 进行相应串的选择 ----
		while (flag) {
			if (!next()) {
				break;
			}
			// 如果是数字，append
			if (isDight(input.charAt(i))) {
				str += input.charAt(i);

			}
			// 如果是小数点，进行相应的判断
			else if (input.charAt(i) == '.') {
				// 若是在整数的基础上出现第一个小数点，设置为实数
				if (point) {
					str += input.charAt(i);
					point = false;// 实数，point为false

				}
				// 若已为实数其后还有小数点，则发生错误，flag置为false
				else {
					flag = false;
					Error error = new Error();
					error.setRow(rowNum);
					error.setErrorSrc(str);
					error.setErrorType("错误单词：	出现第二个'.'号");
					errorList.add(error);

					// 处理错误（忽略后续字符直到;）
					// 将第二个小数点之后的数字去掉，只显示第二个小数点之前的数字，即3.14.15转换为3.14
					while (input.charAt(i) != ';') {
						if (!next()) {
							break;
						}
					}
				}
			}

			// 如果出现字母，进行错误处理
			else if (isAlpha(input.charAt(i))) {
				flag = false;
				Error error = new Error();
				error.setRow(rowNum);
				error.setErrorSrc(str + input.charAt(i));
				error.setErrorType("错误单词：数字开头的数字、字母串");
				errorList.add(error);

				if (!point) {
					error.setErrorType("错误单词：实数的小数部分出现字母");
				}

				// 处理错误：忽略字母及其后面的串
				// 整数：将第二个小数点之后的数字去掉，只显示第二个小数点之前的数字，即3.14.15转换为3.14
				// 实数：将第一个出现的字母之后的数字或字母串去掉，只显示第一个字母之前的数字，即5.26B78转换为5.26
				while (input.charAt(i) != ';') {
					if (!next()) {
						break;
					}
				}
			}

			// 以上情况都不是，则发生错误，停止执行
			else {
				flag = false;
			}
		}

		// ---- 进行token表和symbol表的填写 ----
		Token token = new Token();
		token.setLabel(tokenList.size());
		token.setName(str);
		token.setType(19);
		token.setAddress(symbolList.size());

		Symbol symbol = new Symbol();
		symbol.setNumber(symbolList.size());
		symbol.setName(str);
		symbol.setType(19);

		// point为false，说明是实数，修改两表的type值
		if (!point) {
			token.setType(20);
			symbol.setType(20);
		}
		tokenList.add(token);
		symbolList.add(symbol);

	}

	/**
	 * 以字母开头，识别标识符还是关键字
	 */
	private void recognizeId() {
		String str = "";
		// 标志位，记录是标识符还是关键字
		int type;

		// 判断当前字符和后续是不是字母或数字，是的话继续加入
		do {
			str = str + input.charAt(i);
			if (!next()) {
				break;
			}
		} while (isAlpha(input.charAt(i)) || isDight(input.charAt(i)));

		// 匹配关键字机内码
		type = matchKeyWords(str);
		Token token = new Token();
		token.setLabel(tokenList.size());
		token.setName(str);

		// 如果为标识符，将属性值添加到token表项中
		// 同时加入到symbol表中
		if (type == 0) {
			token.setType(18);
			token.setAddress(symbolList.size());// 当前的大小为下一个存放的位置

			// 新建一个symbol表表项，记录各个属性
			Symbol symbol = new Symbol();
			symbol.setNumber(symbolList.size());
			symbol.setName(str);
			symbol.setType(18);
			symbolList.add(symbol);
		}

		// 如果为关键字,只用记录token表项即可
		else {
			token.setType(type);
			token.setAddress(-1); // 关键字在token文件中的地址为-1
		}

		tokenList.add(token);
	}

	/**
	 * 以界符开头，判断是否其他符号
	 */
	private void recognizeOtherSym() {
		String str = input.substring(i, i + 1);

		// 如果为：或<或>，还需继续判断下一个字符，看是否是组合
		if (str.equals(":") || str.equals("<") || str.equals(">") ){
			i++;

			// 若为=，则符号为:=或<=或>=
			if (input.charAt(i) == '=') {
				str += input.charAt(i);
			}
			// 若str为<且当前字符为>,说明是尖括号<>
			else if (input.charAt(i) == '>' && str.equals("<")) {

				str += input.charAt(i);
			}
			// 若都不是，指向前一个字符
			else {
				if (!before()) {
					return;
				}
			}
		}

		// 将串与机内码进行匹配，看是否为界符
		for (int j = 21; j <= 37; j++)// 判断得到的字符串是机内码21-37的哪个符号
		{
			// 如果匹配成功，将各个属性添加到新建的token文件表项中，地址为-1
			if (str.equals(machineCodes[j])) {
				Token token = new Token();
				token.setLabel(tokenList.size());
				token.setName(str);
				token.setType(j);
				token.setAddress(-1);
				tokenList.add(token);
				i++;
				break;
			}
		}

	}

	/**
	 * 判断该字符是否数字
	 * 
	 * @param c
	 * @return
	 */
	private boolean isDight(char c) {
		if (c >= '0' && c <= '9') {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断该字符是否字母
	 * 
	 * @param c
	 * @return
	 */
	private boolean isAlpha(char c) {
		if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断该字符是否其他界符
	 * 
	 * @param c
	 * @return
	 */
	private boolean isDelimiter(char c) {
		switch (c) {
		case '(':
			return true;
		case ')':
			return true;
		case '+':
			return true;
		case '-':
			return true;
		case '*':
			return true;
		case '.':
			return true;
		case ',':
			return true;
		case ':':
			return true;
		case ';':
			return true;
		case '=':
			return true;
		case '<':
			return true;
		case '>':
			return true;
		default:
			return false;
		}
	}

	/**
	 * 匹配关键字机内码 匹配成功，返回关键字的机内码；匹配失败，则为标识符，返回0
	 * 
	 * @param str
	 * @return
	 */
	private int matchKeyWords(String str) {
		for (int i = 1; i <= 17; i++) {
			if (str.equals(machineCodes[i])) {
				return i;// 返回匹配成功的关键字的机内码
			}
		}
		return 0;// 匹配失败，则为标识符
	}
	
	/**
	 * 显示分析结果
	 */
	public void showResult() {
		String output = "";
		System.out.println("------------------ 词法分析得到token表如下： ------------------");
		for (int i = 1; i < tokenList.size(); i++) {
			Token token = tokenList.get(i);
			output += token.toString() + "\r\n";
			System.out.println(token.toString());
		}
		TxtTool.writeFile(output, "tokenMorphology.txt");
		
		System.out.println("------------------ 词法分析得到symbol表如下： ------------------");
		output = "";
		for (int i = 1; i < symbolList.size(); i++) {
			Symbol symbol = symbolList.get(i);
			output += symbol.toString() + "\r\n";
			System.out.println(symbol.toString());
		}
		TxtTool.writeFile(output, "symbolMorphology.txt");
		
		
		System.out.println("------------------ 词法分析的错误信息如下： ------------------");
		output = "";
		output += "共有" + errorList.size() + "个错误" + "\r\n";
		System.out.println("共有" + errorList.size() + "个错误" );
		for (Error error : errorList) {
			output += error.toString() + "\r\n";
			System.out.println(error.toString());
		}
		TxtTool.writeFile(output, "errorMorphology.txt");
	}
}
