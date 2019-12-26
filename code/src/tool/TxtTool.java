package tool;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class TxtTool {
//	public static void main(String args[]) {
//		readFile("input.txt");
//		writeFile("123\r\n", "output.txt");
//	}

	/**
	 * 读入TXT文件
	 * 
	 * @return
	 */
	public static String readFile(String pathname) {
		// String pathname = "input.txt"; // 绝对路径或相对路径都可以，写入文件时演示相对路径,读取以上路径的input.txt文件
		// 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw;
		// 不关闭文件会导致资源的泄露，读写文件都同理
		// Java7的try-with-resources可以优雅关闭文件，异常时自动关闭文件；详细解读https://stackoverflow.com/a/12665271
		try (FileReader reader = new FileReader(pathname); BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
		) {
			String line;
			// 网友推荐更加简洁的写法
			String sumLine = "";
			while ((line = br.readLine()) != null) {
				// 一次读入一行数据
				// System.out.println(line);

				// ---- 此处额外每行加入回车换行符，方便处理注释 ----
				sumLine += line + '\r' + '\n';
			}

			return sumLine;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * 写入TXT文件
	 */
	public static void writeFile(String output, String filename) {
		try {
			File writeName = new File(filename); // 相对路径，如果没有则要建立一个新的output.txt文件
			writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
			try (FileWriter writer = new FileWriter(writeName); BufferedWriter out = new BufferedWriter(writer)) {
				out.write(output); // \r\n即为换行
				out.flush(); // 把缓存区内容压入文件
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
