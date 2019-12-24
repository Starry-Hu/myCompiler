package service;

import bean.Assemble;
import bean.Equality4;
import bean.Error;
import bean.Symbol;
import bean.Token;

public class Test {

	public static void main(String[] args) {
		String input = TxtTool.readFile("input.txt");
		String output = "";
		Morphology morphology = new Morphology();
		morphology.initial(input);
		morphology.implement();

		System.out.println("------------------ 词法分析得到token表如下： ------------------");
		for (int i = 1; i < morphology.tokenList.size(); i++) {
			Token token = morphology.tokenList.get(i);
			output += token.toString() + "\r\n";
			System.out.println(token.toString());
		}
		TxtTool.writeFile(output, "tokenMorphology.txt");

		System.out.println("------------------ 词法分析得到symbol表如下： ------------------");
		output = "";
		for (int i = 1; i < morphology.symbolList.size(); i++) {
			Symbol symbol = morphology.symbolList.get(i);
			output += symbol.toString() + "\r\n";
			System.out.println(symbol.toString());
		}
		TxtTool.writeFile(output, "symbolMorphology.txt");

		System.out.println("------------------ 词法分析的错误信息如下： ------------------");
		output = "";
		output += "共有" + morphology.errorList.size() + "个错误";
		System.out.println("共有" + morphology.errorList.size() + "个错误");
		for (Error error : morphology.errorList) {
			output += error.toString() + "\r\n";
			System.out.println(error.toString());
		}
		TxtTool.writeFile(output, "errorMorphology.txt");


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

//		System.out.println("------------------ 语义分析更新symbol表如下： ------------------");
//		output = "";
//		for (int i = 1; i < semantic.symbolList.size(); i++) {
//			Symbol symbol = semantic.symbolList.get(i);
//			output += symbol.toString() + "\r\n";
//			System.out.println(symbol.toString());
//		}
//		TxtTool.writeFile(output, "symbolSemantic.txt");
//
//		System.out.println("------------------ 语义分析得到的四元式序列如下： ------------------");
//		output = "";
//		for (int i = 1; i < semantic.equality4List.size(); i++) {
//			Equality4 equality4 = semantic.equality4List.get(i);
//			output += equality4.toString() + "\r\n";
//			System.out.println(equality4.toString());
//		}
//		TxtTool.writeFile(output, "equality4Semantic.txt");

		Create create = new Create();
		create.initial(semantic.symbolList, semantic.equality4List);// 使用语义分析后的信息来初始化
		create.implement();

		System.out.println("---------------------------------------------------------");

		System.out.println("------------------ 生成目标代码时更新symbol表如下：------------------");
		output = "";
		for (Symbol symbol : create.symbolList) {
			System.out.println(symbol.toStringWithInfoLink());
		}
		TxtTool.writeFile(output, "symbolCreateTarget.txt");

		System.out.println("------------------ 生成目标代码如下： ------------------");
		for (Assemble assemble : create.assembleList) {
			System.out.println(assemble.toString());
		}
		TxtTool.writeFile(output, "symbolCreateTarget.txt");
	}

}
