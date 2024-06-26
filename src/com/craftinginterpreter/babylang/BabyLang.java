package com.craftinginterpreter.babylang;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.craftinginterpreter.babylang.TokenType.Token;
import com.craftinginterpreter.babylang.TokenType.TokenType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class BabyLang {
	static boolean hadError = false;
	
	public static void main(String args[]) throws IOException {
		if (args.length > 1) {
			System.out.println("Usage: babylang [script]");
			System.exit(64);
		} else if (args.length == 1) {
			runFile(args[0]);
		} else {
			runPrompt();
		}
	}
	
	private static void runFile(String path) throws IOException {
		byte[] bytes = Files.readAllBytes(Paths.get(path));
		run(new String(bytes, Charset.defaultCharset()));
		
		if (hadError) System.exit(65);
	}

	private static void runPrompt() throws IOException {
		InputStreamReader input = new InputStreamReader(System.in);
		BufferedReader reader = new BufferedReader(input);
		
		for (;;) {
			System.out.print(">");
			String line = reader.readLine();
			if (line == null) break;
			run(line);
			
			hadError = false;
		}
	}
	
	private static void run(String source) {
		Scanner scanner = new Scanner(source);
		List<Token> tokens = scanner.scanTokens();
		
		for (Token token : tokens) {
			System.out.println(token.getType() + " " + token.getLexeme() + " " + token.getLine());
		}
		
		Parser parser = new Parser(tokens);
		Expr expression = parser.parse();
		
		if (hadError) return;
		
		System.out.println(new AstPrinter().print(expression));
	}
	
	static void error(int line, String message) {
		report(line, "", message);
	}
	
	static void error(Token token, String message) {
	    if (token.getType() == TokenType.EOF) {
	      report(token.getLine(), " at end", message);
	    } else {
	      report(token.getLine(), " at '" + token.getLexeme() + "'", message);
	    }
	}
	
	private static void report(int line, String where, String message) {
		System.err.println(
		        "[line " + line + "] Error" + where + ": " + message);
		hadError = true;
	}
}
