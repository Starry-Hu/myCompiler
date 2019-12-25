package service;

import java.util.Scanner;

public class Test {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		System.out.println("------------------ 请输入要分析的源程序txt文件名： ------------------");
		String filename = scanner.nextLine();
		String input = TxtTool.readFile(filename);

		// 词法分析
		Morphology morphology = new Morphology();
		morphology.initial(input);
		morphology.implement();

		// 语法分析
		Grammar grammar = new Grammar();
		grammar.initial(morphology.tokenList, morphology.symbolList);
		grammar.implement();

		// 语义分析
		Semantic semantic = new Semantic();
		semantic.initial(morphology.tokenList, morphology.symbolList);
		semantic.implement();

		// 目标代码生成
		Create create = new Create();
		create.initial(semantic.symbolList, semantic.equality4List);// 使用语义分析后的信息来初始化

		System.out.println("------------------ 请输入要显示的分析信息： ------------------");
		System.out.println("(1)词法分析;	(2)语法分析;	(3)语义分析;	(4)目标代码生成;	(9)退出");

		int choose = scanner.nextInt();
		while (choose != 9) {
			if (choose == 1) {
				morphology.showResult();
			}else if(choose == 2){
				grammar.showResult();
			}else if (choose == 3) {
				semantic.showResult();
			}else if (choose == 4) {
				create.implement();
				create.showResult();
			}else {
				System.out.println("错误输入！请重新输入命令");
			}
			System.out.println("------------------ 请输入要显示的分析信息： ------------------");
			System.out.println("(1)词法分析;	(2)语法分析;	(3)语义分析;	(4)目标代码生成;	(9)退出");
			choose = scanner.nextInt();
		}
		
		scanner.close();
		System.out.println("------------------ 已退出程序！ ------------------");
	}
	
}
