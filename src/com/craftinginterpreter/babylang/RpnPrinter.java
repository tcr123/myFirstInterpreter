package com.craftinginterpreter.babylang;

import com.craftinginterpreter.babylang.Expr.*;
import com.craftinginterpreter.babylang.TokenType.*;

class RpnPrinter implements Expr.Visitor<String> {
	String print(Expr expr) {
		return expr.accept(this);
	}

	@Override
	public String visitBinary(Binary expr) {
		return expr.left.accept(this) + " " +
				expr.right.accept(this) + " " + expr.operator.getLexeme();
	}

	@Override
	public String visitGrouping(Grouping expr) {
		return expr.expression.accept(this);
	}

	@Override
	public String visitLiteral(Literal expr) {
		if (expr.value == null) return "nil";
		return expr.value.toString();
	}

	@Override
	public String visitUnary(Unary expr) {
		String operator = expr.operator.getLexeme();
		if (expr.operator.getType() == TokenType.MINUS) {
			operator = "~";
		}
		return expr.right.accept(this) + " " + operator;
	}

	
	public static void main(String args[]) {
		Expr expression = new Expr.Binary(
		        new Expr.Unary(
		            new Token(TokenType.MINUS, "-", null, 1),
		            new Expr.Literal(123)),
		        new Token(TokenType.STAR, "*", null, 1),
		        new Expr.Grouping(
		            new Expr.Literal("str")));

		System.out.println(new RpnPrinter().print(expression));
	}
	
}