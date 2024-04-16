package com.craftinginterpreter.babylang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.craftinginterpreter.babylang.TokenType.*;

class Scanner {
	private final String source;
	private final List<Token> tokens = new ArrayList<>();
	
	private static int start = 0;
	private static int current = 0;
	private static int line = 1;
	
	private static final Map<String, TokenType> keywords;

	static {
		keywords = new HashMap<>();
	    keywords.put("and",    TokenType.AND);
	    keywords.put("class",  TokenType.CLASS);
	    keywords.put("else",   TokenType.ELSE);
	    keywords.put("false",  TokenType.FALSE);
	    keywords.put("for",    TokenType.FOR);
	    keywords.put("fun",    TokenType.FUN);
	    keywords.put("if",     TokenType.IF);
	    keywords.put("nil",    TokenType.NIL);
	    keywords.put("or",     TokenType.OR);
	    keywords.put("print",  TokenType.PRINT);
	    keywords.put("return", TokenType.RETURN);
	    keywords.put("super",  TokenType.SUPER);
	    keywords.put("this",   TokenType.THIS);
	    keywords.put("true",   TokenType.TRUE);
	    keywords.put("var",    TokenType.VAR);
	    keywords.put("while",  TokenType.WHILE);
	}
	
	
	Scanner(String source) {
		this.source = source;
	}
	
	List<Token> scanTokens() {
		while(!isAtEnd()) {
			start = current;
			scanToken();
		}
		
		tokens.add(new Token(TokenType.EOF, "", null, line));
		return tokens;
	}

	private boolean isAtEnd() {
		return current >= source.length();
	}
	
	private void scanToken() {
		char c = advance();
		
		switch (c) {
			/* LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE,
			 COMMA, DOT, MINUS, PLUS, SLASH, STAR, */
			case '(': addToken(TokenType.LEFT_PAREN); break;
			case ')': addToken(TokenType.RIGHT_PAREN); break;
			case '{': addToken(TokenType.LEFT_BRACE); break;
			case '}': addToken(TokenType.RIGHT_BRACE); break;
			case ',': addToken(TokenType.COMMA); break;
			case '.': addToken(TokenType.DOT); break;
			case '-': addToken(TokenType.MINUS); break;
			case '+': addToken(TokenType.PLUS); break;
			case ';': addToken(TokenType.SEMICOLON); break;
			case '*': addToken(TokenType.STAR); break;
			
			/* BANG, BANG_EQUAL,
		  	EQUAL, EQUAL_EQUAL,
		  	GREATER, GREATER_EQUAL,
		  	LESS, LESS_EQUAL, */
			case '!':
				addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;
			case '=':
				addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
			case '>':
				addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
			case '<':
				addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;
			case 'o':
				if (match('r')) {
					addToken(TokenType.OR);
				}
				break;
			
			// SLASH
			case '/':
				if (match('/')) {
					while (peek() != '\n' && !isAtEnd()) {
						advance();
					}
				} else if (match('*')) {
					// TODO implements /* */
					command();
				} else {
					addToken(TokenType.SLASH);
				}
				break;
				
			case ' ':
			case '\r':
			case '\t':
				break;
			
			case '\n': line++; break;
				
			// Literals STRING
			case '"': string(); break;
			
			default:
				// Check for number
				if (isDigit(c)) {
					number();
				} else if (isAlpha(c)) {
					identifier();
				} else {
					BabyLang.error(line, "Unexpected character.");
				}
				
				break;
		}
	}

	private boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}
	
	private boolean isAlpha(char c) {
		return (c >= 'a' && c <= 'z') || 
				(c >= 'A' && c <= 'Z') ||
				(c == '_');
	}
	
	private boolean isAlphaNumeric(char c) {
		return isAlpha(c) || isDigit(c);
	}
	
	private void identifier() {
		while (isAlphaNumeric(peek())) advance();
		
		String text = this.source.substring(start, current);
		TokenType type = keywords.get(text);
		if (type == null) type = TokenType.IDENTIFIER;	
		addToken(TokenType.IDENTIFIER);
	}
	
	private void number() {
		while (isDigit(peek())) advance();
		
		if (peek() == '.' && isDigit(peekNext())) {
			advance();
			
			while (isDigit(peek())) advance();
			
			addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
			return;
		}
		
		addToken(TokenType.NUMBER, Integer.parseInt(source.substring(start, current)));
	}
	
	private void command() {
		while (peek() != '*' && peekNext() != '/' && !isAtEnd()) {
			if (peek() != '\n') line++;
			advance();
		}
		
		if (isAtEnd()) {
			BabyLang.error(line, "Unexpected end command.");
		}
		
		advance();advance();
	}

	private void string() {
		while (peek() != '"' && !isAtEnd()) {
			if (peek() != '\n') line++;
			advance();
		}
		
		if (isAtEnd()) {
			BabyLang.error(line, "Underminated string.");
		}
		
		// skip to "
		advance();
		
		// trim surrounding quotes
		String value = this.source.substring(start + 1, current - 1);
		addToken(TokenType.STRING, value);
	}

	private char peek() {
		if (isAtEnd()) return '\0';
		return this.source.charAt(current);
	}
	
	private char peekNext() {
		if (current + 1 >= source.length()) return '\0';
		return this.source.charAt(current + 1);
	}

	private boolean match(char c) {
		if (isAtEnd()) return false;
		if (this.source.charAt(current) != c) return false;
		
		current++;
		return true;
	}

	private void addToken(TokenType type) {
		addToken(type, null);
		
	}
	
	private void addToken(TokenType type, Object literal) {
		String text = this.source.substring(start, current);
		tokens.add(new Token(type, text, literal, line));
	}

	private char advance() {
		return this.source.charAt(current++);
	}
}
