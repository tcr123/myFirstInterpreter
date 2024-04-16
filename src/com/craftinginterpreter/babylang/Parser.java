package com.craftinginterpreter.babylang;

import java.util.List;

import com.craftinginterpreter.babylang.TokenType.*;
import com.craftinginterpreter.babylang.Expr.*;

/*
 * grammar rules for parser
 * expression -> comma
 * comma -> equality ((",") equality)
 * equality -> comparison (("!=" | "==") comparison)*
 * comparison -> term((">"|">="|"<"|"<=") term)*
 * term -> factor(("-","+") factor)*
 * factor -> unary (("/" | "*") unary)*
 * unary -> ("!" | "-") unary | primary;
 * primary -> Number | String | "true" | "false" | "nil" | "(" expression ")"
*/

public class Parser {
	private final List<Token> tokens;
	private int current = 0;
	
	private static class ParseError extends RuntimeException {}
	
	Parser(List<Token> tokens) {
		this.tokens = tokens;
	}
	
	Expr parse() {
	    try {
	      return expression();
	    } catch (ParseError error) {
	      return null;
	    }
	 }
	
	private Expr expression() {
		return equality();
	}
	
	private Expr equality() {
		Expr expr = comparison();
		
		while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
			Token token = previous();
			Expr right = comparison();
			return new Binary(expr, token, right);
		}
		
		return expr;
	}

	private Expr comparison() {
		Expr expr = term();
		
		while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
			Token token = previous();
			Expr right = term();
			return new Binary(expr, token, right);
		}
		
		return expr;
	}
	
	private Expr term() {
		Expr expr = factor();
		
		while (match(TokenType.MINUS, TokenType.PLUS)) {
			Token token = previous();
			Expr right = factor();
			return new Binary(expr, token, right);
		}
		
		return expr;
	}
	
	private Expr factor() {
		Expr expr = unary();
		
		while (match(TokenType.SLASH, TokenType.STAR)) {
			Token token = previous();
			Expr right = unary();
			return new Binary(expr, token, right);
		}
		
		return expr;
	}
	
	private Expr unary() {
		if (match(TokenType.BANG, TokenType.MINUS)) {
			Token token = previous();
			Expr right = unary();
			return new Unary(token, right);
		}
		
		return primary();
	}
	
	private Expr primary() {
		if (match(TokenType.FALSE)) return new Expr.Literal(false);
	    if (match(TokenType.TRUE)) return new Expr.Literal(true);
	    if (match(TokenType.NIL)) return new Expr.Literal(null);

	    if (match(TokenType.NUMBER, TokenType.STRING)) {
	      return new Expr.Literal(previous().getLiteral());
	    }

	    if (match(TokenType.LEFT_PAREN)) {
	      Expr expr = expression();
	      consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
	      return new Expr.Grouping(expr);
	    }
	    
	    throw error(peek(), "Expect expression.");
	}
	
	private Token consume(TokenType type, String message) {
		if (check(type)) return advance();
		
		throw error(peek(), message);
	}

	private boolean match(TokenType...types) {
		for (TokenType type : types) {
			if (check(type)) {
				advance();
				return true;
			}
		}
		
		return false;
	}
	
	private Token advance() {
		return this.tokens.get(current++);
	}

	private boolean check(TokenType type) {
		return peek().getType() == type;
	}
	
	private boolean isAtEnd() {
		return tokens.get(current) == null;
	}
	
	private Token peek() {
		if (!isAtEnd())return tokens.get(current);
		return previous();
	}

	private Token previous() {
		return tokens.get(current - 1);
	}
	
	private void synchronize() {
	    advance();

	    while (!isAtEnd()) {
	      if (previous().getType() == TokenType.SEMICOLON) return;

	      switch (peek().getType()) {
	        case CLASS:
	        case FUN:
	        case VAR:
	        case FOR:
	        case IF:
	        case WHILE:
	        case PRINT:
	        case RETURN:
	          return;
	      }

	      advance();
	    }
	}
	
	private ParseError error(Token token, String message) {
		BabyLang.error(token, message);
		return new ParseError();
	}
}
