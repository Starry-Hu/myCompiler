package service;

import bean.Error;
import bean.Symbol;
import bean.Token;

public class Test {

	public static void main(String[] args) {
		String input = ReadTxt.readFile();
		Morphology morphology = new Morphology();
		morphology.initial(input);
		morphology.implement();
		
		System.out.println("------------------ 词法分析得到token表如下： ------------------");
		for(Token token : morphology.tokenList) {
			System.out.println(token.toString());
		}
		
		System.out.println("------------------ 词法分析得到symbol表如下： ------------------");
		for(Symbol symbol : morphology.symbolList) {
			System.out.println(symbol.toString());
		}
		
		System.out.println("------------------ 词法分析的错误信息如下： ------------------");
		System.out.println("共有" + morphology.errorList.size() + "个错误");
		for(Error error : morphology.errorList) {
			System.out.println("第" + error.getRow() + "行：		" + error.getErrorSrc() + "		原因：" + error.getErrorType());
		}
		
//		
//		Grammar grammar = new Grammar();
//		grammar.initial(morphology.tokenList, morphology.symbolList);
//		grammar.implement();
//		
//		System.out.println("---------------------------------------------------------");
//		
//		System.out.println("------------------ 语法分析的错误信息如下： ------------------");
//		System.out.println(grammar.error);
//		
//		System.out.println("------------------ 语法分析更新symbol表如下： ------------------");
//		for(Symbol symbol : grammar.symbolList) {
//			System.out.println(symbol.toString());
//		}
	}

}
