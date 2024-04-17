package com.craftinginterpreter.babylang;

import java.util.List;

import com.craftinginterpreter.babylang.TokenType.*;
import com.craftinginterpreter.babylang.Expr.*;

/*
 * grammar rules for parser
 * expression -> comma 
 * comma -> equality ((",") equality) TODO add later comma operator 
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
		return conditional();
	}
	
	private Expr conditional() {
		Expr expr = bitwise();
		
		if (match(TokenType.QUESTION)) {
			Expr thenStatement = bitwise();
			consume(TokenType.COLON, "Expect ':' after then branch of conditional expression.");
			Expr elseStatement = bitwise();
			expr = new Conditional(expr, thenStatement, elseStatement);
		}
		
		return expr;
	}
	
	private Expr bitwise() {
		Expr expr = equality();
		
		while (match(TokenType.AND, TokenType.OR)) {
			Token token = previous();
			Expr right = equality();
			expr = new Binary(expr, token, right);
		}
		
		return expr;
	}
	
	private Expr equality() {
		Expr expr = comparison();
		
		while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
			Token token = previous();
			Expr right = comparison();
			expr = new Binary(expr, token, right);
		}
		
		return expr;
	}

	private Expr comparison() {
		Expr expr = term();
		
		while (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
			Token token = previous();
			Expr right = term();
			expr = new Binary(expr, token, right);
		}
		
		return expr;
	}
	
	private Expr term() {
		Expr expr = factor();
		
		while (match(TokenType.MINUS, TokenType.PLUS)) {
			Token token = previous();
			Expr right = factor();
			expr = new Binary(expr, token, right);
		}
		
		return expr;
	}
	
	private Expr factor() {
		Expr expr = unary();
		
		while (match(TokenType.SLASH, TokenType.STAR)) {
			Token token = previous();
			Expr right = unary();
			expr = new Binary(expr, token, right);
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
	    
	    // check for equality
	    if (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
	    	error(previous(), "Missing left-hand operand.");
	    	equality();
	    	return null;
	    }
	    
	    // check for comparison
	    if (match(TokenType.GREATER, TokenType.GREATER_EQUAL, TokenType.LESS, TokenType.LESS_EQUAL)) {
	    	error(previous(), "Missing left-hand operand.");
	    	comparison();
	    	return null;
	    }
	    
	    // check for term
	    if (match(TokenType.PLUS)) {
	    	error(previous(), "Missing left-hand operand.");
	    	term();
	    	return null;
	    }
	    
	    // check for factor
	    if (match(TokenType.SLASH, TokenType.STAR)) {
	    	error(previous(), "Missing left-hand operand.");
	    	factor();
	    	return null;
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
		if (!isAtEnd()) current++;
		return previous();
	}

	private boolean check(TokenType type) {
		if (isAtEnd()) return false;
		return peek().getType() == type;
	}
	
	private boolean isAtEnd() {
		return tokens.get(current).getType() == TokenType.EOF;
	}
	
	private Token peek() {
		return tokens.get(current);
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
