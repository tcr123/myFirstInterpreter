package com.craftinginterpreter.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
	public static void main(String args[]) throws IOException {
		if (args.length != 1) {
			System.err.println("Usage: generate_ast <output directory>");
			System.exit(64);
		}
		
		String outputDir = args[0];
		
		defineAst(outputDir, "Expr", Arrays.asList(
			"Binary: Expr left, Token operator, Expr right",
			"Grouping: Expr expression",
			"Literal: Object value",
			"Unary: Token operator, Expr right"
		));
	}

	private static void defineAst(String outputDir, String baseName, List<String> types) throws IOException {
		String path = outputDir + "/" + baseName + ".java";
		PrintWriter writer = new PrintWriter(path, "UTF-8");
		
		writer.println("package com.craftinginterpreter.babylang;");
		writer.println();
		writer.println("import com.craftinginterpreter.babylang.TokenType.*;");
		writer.println();
		
		writer.println("abstract class " + baseName + " {");
		writer.println();
		
		defineVisitor(writer, baseName, types);
		
		writer.println("	abstract <R> R accept(Visitor<R> visitor);\n");
		
		for (String type : types) {
			String className = type.split(":")[0].trim();
			String fields = type.split(":")[1].trim();
			defineType(writer, baseName, className, fields);
		}
		writer.println("}");
		
		writer.close();
	}
	
	private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
		writer.println("	interface Visitor<R> {");
		for (String type : types) {
			String className = type.split(":")[0].trim();
			writer.println("		R visit" + className + "(" + className + " " + baseName.toLowerCase() + ");"); 
		}
		writer.println("	}\n");
	}

	private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
		String variables[] = fieldList.split(",");
		
		writer.println("	static class " + className + " extends " + baseName + " {");
		writer.println("		" + className + "(" + fieldList + ") {");
		
		for (String variable : variables) {
			writer.printf("			this.%s = %s;\n", variable.trim().split(" ")[1], variable.trim().split(" ")[1]);
		}
		
		writer.println("		}\n");
		
		writer.println("		@Override");
		writer.println("		<R> R accept(Visitor<R> visitor) {");
		writer.println("			return visitor.visit" + className + "(this);");
		writer.println("		}\n");
		
		for (String variable : variables) {
			writer.printf("		final %s;\n", variable.trim());
		}
		
		writer.println("	}\n");
	}
}
