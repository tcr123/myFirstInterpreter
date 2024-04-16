package com.craftinginterpreter.babylang.TokenType;

public class Token {
	final TokenType type;
	final String lexeme;
	final Object literal;
	final int line;
	
	public Token(TokenType type, String lexeme, Object literal, int line) {
		this.type = type;
		this.lexeme = lexeme;
		this.literal = literal;
		this.line = line;
	}
	
	public String toString() {
		return type + " " + lexeme + " " + literal;
	}
	
	public TokenType getType() {
		return this.type;
	}
	
	public String getLexeme() {
		return this.lexeme;
	}
	
	public Object getLiteral() {
		return this.literal;
	}
	
	public int getLine() {
		return this.line;
	}
}
