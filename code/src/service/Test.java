package service;

import bean.Assemble;
import bean.Equality4;
import bean.Error;
import bean.Symbol;
import bean.Token;

public class Test {

	public static void main(String[] args) {
		String input = ReadTxt.readFile("input.txt");
		Morphology morphology = new Morphology();
		morphology.initial(input);
		morphology.implement();

//		 System.out.println("------------------ 词法分析得到token表如下： ------------------");
//		 for(Token token : morphology.tokenList) {
//		 System.out.println(token.toString());
//		 }
//		
//		 System.out.println("------------------ 词法分析得到symbol表如下： ------------------");
//		 for(Symbol symbol : morphology.symbolList) {
//		 System.out.println(symbol.toString());
//		 }
		//
		// System.out.println("------------------ 词法分析的错误信息如下： ------------------");
		// System.out.println("共有" + morphology.errorList.size() + "个错误");
		// for(Error error : morphology.errorList) {
		// System.out.println("第" + error.getRow() + "行： " + error.getErrorSrc() + "
		// 原因：" + error.getErrorType());
		// }

		//
		// Grammar grammar = new Grammar();
		// grammar.initial(morphology.tokenList, morphology.symbolList);
		// grammar.implement();
		//
		// System.out.println("---------------------------------------------------------");
		//
		// System.out.println("------------------ 语法分析的错误信息如下： ------------------");
		// System.out.println(grammar.error);
		//
		// System.out.println("------------------ 语法分析更新symbol表如下： ------------------");
		// for(Symbol symbol : grammar.symbolList) {
		// System.out.println(symbol.toString());
		// }

		Semantic semantic = new Semantic();
		semantic.initial(morphology.tokenList, morphology.symbolList);
		semantic.implement();

		System.out.println("---------------------------------------------------------");
		System.out.println("------------------ 语义分析的错误信息如下： ------------------");
		System.out.println(semantic.error);

		System.out.println("------------------ 语义分析更新symbol表如下： ------------------");
		for (int i = 1; i < semantic.symbolList.size(); i++) {
			Symbol symbol = semantic.symbolList.get(i);
			System.out.println(symbol.toString());
		}

		System.out.println("------------------ 语义分析得到的四元式序列如下： ------------------");
		for (int i = 1; i < semantic.equality4List.size(); i++) {
			Equality4 equality4 = semantic.equality4List.get(i);
			System.out.println(equality4.toString());
		}
		
//		Create create = new Create();
//		create.initial(semantic.symbolList, semantic.equality4List);// 使用语义分析后的信息来初始化
//		create.implement();
//		
//		System.out.println("---------------------------------------------------------");
//
//		System.out.println("------------------ 生成目标代码时更新symbol表如下： ------------------");
//		for (Symbol symbol : create.symbolList) {
//			System.out.println(symbol.toStringWithInfoLink());
//		}
//
//		System.out.println("------------------ 生成目标代码如下： ------------------");
//		for (Assemble assemble : create.assembleList) {
//			System.out.println(assemble.toString());
//		}
	}

}
