package com.hbhs.algorithm.leecode;

import java.util.Stack;

/**
 * Created by walter.xu on 2016/9/9.
 * <br>Implement a basic calculator to evaluate a simple expression string.
 * <br>The expression string may contain open ( and closing parentheses ), the plus + or minus sign -, non-negative integers and empty spaces
 * <br>You may assume that the given expression is always valid.
 Some examples:
 * <br>"1 + 1" = 2
 * <br>" 2-1 + 2 " = 3
 * <br>"(1+(4+5+2)-3)+(6+8)" = 23
 */
public class BasicCalculator {
    Stack<Integer> valuesStack = new Stack<Integer>();
    Stack<Character> operationStack = new Stack<Character>();

    public static void main(String[] args){
        BasicCalculator test = new BasicCalculator();

        System.out.println(test.calculate("(1 - 11)"));
//        System.out.println(test.calculate(" 2-1 + 2 "));
//        System.out.println(test.calculate("(1+(4+5+2)-3)+(6+8)" ));
    }

    public int calculate(String s) {
        char[] chars = s.toCharArray();
        for(char current: chars){
            switch (current){
                case ' ': break;
                case '(': operationStack.push(current); break;
                case ')': break;
                case '+': break;
                case '-': break;
                default:
                    break;
            }
        }
        return valuesStack.pop();
    }

    private void doOperationWhenEndWith(){

    }
}
