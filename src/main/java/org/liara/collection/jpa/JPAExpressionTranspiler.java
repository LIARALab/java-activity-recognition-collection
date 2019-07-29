/*
 * Copyright (C) 2019 Cedric DEMONGIVERT <cedric.demongivert@gmail.com>
 *
 * Permission is hereby granted,  free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction,  including without limitation the rights
 * to use,  copy, modify, merge,  publish,  distribute, sublicense,  and/or sell
 * copies  of the  Software, and  to  permit persons  to  whom  the  Software is
 * furnished to do so, subject to the following conditions:
 *
 * The  above  copyright  notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED,  INCLUDING  BUT  NOT LIMITED  TO THE  WARRANTIES  OF MERCHANTABILITY,
 * FITNESS  FOR  A PARTICULAR  PURPOSE  AND  NONINFRINGEMENT. IN NO  EVENT SHALL
 * THE  AUTHORS OR  COPYRIGHT  HOLDERS  BE  LIABLE FOR  ANY  CLAIM,  DAMAGES  OR
 * OTHER  LIABILITY, WHETHER  IN  AN  ACTION  OF  CONTRACT,  TORT  OR  OTHERWISE,
 * ARISING  FROM,  OUT  OF OR  IN  CONNECTION  WITH THE  SOFTWARE OR  THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.liara.collection.jpa;

import org.apache.commons.text.StringEscapeUtils;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.operator.selection.SelectionPlaceholder;
import org.liara.collection.source.JoinSourcePlaceholder;
import org.liara.collection.source.TableSourcePlaceholder;
import org.liara.expression.Constant;
import org.liara.expression.Expression;
import org.liara.expression.Placeholder;
import org.liara.expression.operation.CommonOperator;
import org.liara.expression.operation.Operation;
import org.liara.expression.operation.Operator;
import org.liara.support.tree.TreeWalker;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class JPAExpressionTranspiler
{
  @NonNull
  private static final List<@NonNegative @NonNull Integer> PRECEDENCE;
  @NonNull
  private static final List<@NonNull String>               SYMBOLS;

  static {
    CommonOperator.load();
  }

  static {
    PRECEDENCE = new ArrayList<>(Operator.getCount());

    for (int index = 0; index < Operator.getCount(); ++index) {
      PRECEDENCE.add(0);
    }

    PRECEDENCE.set(CommonOperator.MINUS.getIdentifier(), 1);
    PRECEDENCE.set(CommonOperator.BITWISE_NOT.getIdentifier(), 1);
    PRECEDENCE.set(CommonOperator.BITWISE_XOR.getIdentifier(), 2);
    PRECEDENCE.set(CommonOperator.MULTIPLICATION.getIdentifier(), 3);
    PRECEDENCE.set(CommonOperator.DIVISION.getIdentifier(), 3);
    PRECEDENCE.set(CommonOperator.MODULUS.getIdentifier(), 3);
    PRECEDENCE.set(CommonOperator.SUBTRACTION.getIdentifier(), 4);
    PRECEDENCE.set(CommonOperator.ADDITION.getIdentifier(), 4);
    PRECEDENCE.set(CommonOperator.SHIFT_LEFT.getIdentifier(), 5);
    PRECEDENCE.set(CommonOperator.SHIFT_RIGHT.getIdentifier(), 5);
    PRECEDENCE.set(CommonOperator.BITWISE_AND.getIdentifier(), 6);
    PRECEDENCE.set(CommonOperator.BITWISE_OR.getIdentifier(), 7);
    PRECEDENCE.set(CommonOperator.EQUAL.getIdentifier(), 8);
    PRECEDENCE.set(CommonOperator.GREATER_THAN_OR_EQUAL.getIdentifier(), 8);
    PRECEDENCE.set(CommonOperator.GREATER_THAN.getIdentifier(), 8);
    PRECEDENCE.set(CommonOperator.LESS_THAN_OR_EQUAL.getIdentifier(), 8);
    PRECEDENCE.set(CommonOperator.LESS_THAN.getIdentifier(), 8);
    PRECEDENCE.set(CommonOperator.NOT_EQUAL.getIdentifier(), 8);
    PRECEDENCE.set(CommonOperator.NOT.getIdentifier(), 9);
    PRECEDENCE.set(CommonOperator.AND.getIdentifier(), 10);
    PRECEDENCE.set(CommonOperator.XOR.getIdentifier(), 11);
    PRECEDENCE.set(CommonOperator.OR.getIdentifier(), 12);
  }

  static {
    SYMBOLS = new ArrayList<>(Operator.getCount());

    for (int index = 0; index < Operator.getCount(); ++index) {
      SYMBOLS.add("");
    }

    SYMBOLS.set(CommonOperator.BITWISE_XOR.getIdentifier(), "^");
    SYMBOLS.set(CommonOperator.MULTIPLICATION.getIdentifier(), "*");
    SYMBOLS.set(CommonOperator.DIVISION.getIdentifier(), "/");
    SYMBOLS.set(CommonOperator.MODULUS.getIdentifier(), "%");
    SYMBOLS.set(CommonOperator.SUBTRACTION.getIdentifier(), "-");
    SYMBOLS.set(CommonOperator.ADDITION.getIdentifier(), "+");
    SYMBOLS.set(CommonOperator.SHIFT_LEFT.getIdentifier(), "<<");
    SYMBOLS.set(CommonOperator.SHIFT_RIGHT.getIdentifier(), ">>");
    SYMBOLS.set(CommonOperator.BITWISE_AND.getIdentifier(), "&");
    SYMBOLS.set(CommonOperator.BITWISE_OR.getIdentifier(), "|");
    SYMBOLS.set(CommonOperator.EQUAL.getIdentifier(), "=");
    SYMBOLS.set(CommonOperator.GREATER_THAN_OR_EQUAL.getIdentifier(), ">=");
    SYMBOLS.set(CommonOperator.GREATER_THAN.getIdentifier(), ">");
    SYMBOLS.set(CommonOperator.LESS_THAN_OR_EQUAL.getIdentifier(), "<=");
    SYMBOLS.set(CommonOperator.LESS_THAN.getIdentifier(), "<");
    SYMBOLS.set(CommonOperator.NOT_EQUAL.getIdentifier(), "!=");
    SYMBOLS.set(CommonOperator.AND.getIdentifier(), "AND");
    SYMBOLS.set(CommonOperator.XOR.getIdentifier(), "XOR");
    SYMBOLS.set(CommonOperator.OR.getIdentifier(), "OR");
    SYMBOLS.set(CommonOperator.PLUS.getIdentifier(), "+");
    SYMBOLS.set(CommonOperator.MINUS.getIdentifier(), "-");
    SYMBOLS.set(CommonOperator.NOT.getIdentifier(), "NOT");
    SYMBOLS.set(CommonOperator.BITWISE_NOT.getIdentifier(), "~");
  }

  @NonNull
  private final List<@NonNull Operator> _operators;

  @NonNull
  private final StringBuilder _result;

  @NonNull
  private final TreeWalker<Expression> _walker;

  /**
   * Instantiate a new expression to SQL transpiler.
   */
  public JPAExpressionTranspiler () {
    _operators = new LinkedList<>();
    _result = new StringBuilder();
    _walker = new TreeWalker<>(Expression.class);
  }

  /**
   * Transpile the given expression into an SQL string.
   *
   * @param expression An expression to transpile.
   *
   * @return An SQL string equivalent to the given expression.
   */
  public @NonNull String transpile (@NonNull final Expression<?> expression) {
    _walker.setRoot(expression);
    _walker.movesForward();

    while (!_walker.isAtEnd()) {
      while (_walker.canEnter()) {
        enter((Expression<?>) _walker.enter());
      }

      if (_walker.canExit()) {
        exit((Expression<?>) _walker.exit());
        if (_walker.hasCurrent()) {
          back((Expression<?>) _walker.current());
        }
      }
    }

    @NonNull final String result = _result.toString();

    _result.setLength(0);
    _walker.setRoot(null);

    return result;
  }

  /**
   * Called when the transpiler goes back to the given expression.
   *
   * @param expression The expression that was entered.
   */
  private <T> void back (@NonNull final Expression<T> expression) {
    if (expression instanceof Operation) {
      backOperation((Operation<T>) expression);
    }
  }

  /**
   * Called when the transpiler goes back to the given operation.
   *
   * @param operation The operation that was entered.
   */
  private <T> void backOperation (@NonNull final Operation<T> operation) {
    if (_walker.canEnter()) {
      _result.append(' ');
      _result.append(SYMBOLS.get(operation.getOperator().getIdentifier()));
      _result.append(' ');
    }
  }

  /**
   * Called after the transpiler enter for the first time into the given expression.
   *
   * @param expression The visited expression.
   */
  private <T> void enter (@NonNull final Expression<T> expression) {
    if (expression instanceof Operation) {
      enterOperation((Operation<T>) expression);
    }
  }

  /**
   * Called when the transpiler enter for the first time into the given operation.
   *
   * @param operation The visited operation.
   * @param <T>       The expected result type of the given operation.
   */
  private <T> void enterOperation (@NonNull final Operation<T> operation) {
    _operators.add(operation.getOperator());

    if (doViolatePrecedence()) _result.append('(');

    if (
      operation.getOperator() == CommonOperator.PLUS ||
      operation.getOperator() == CommonOperator.MINUS ||
      operation.getOperator() == CommonOperator.BITWISE_NOT ||
      operation.getOperator() == CommonOperator.NOT
    ) {
      enterUnitaryOperation(operation);
    }
  }

  /**
   * Called when the transpiler enter for the first time into the given unitary operation.
   *
   * @param operation The visited unitary operation.
   * @param <T>       The expected result type of the given operation.
   */
  private <T> void enterUnitaryOperation (@NonNull final Operation<T> operation) {
    _result.append(SYMBOLS.get(operation.getOperator().getIdentifier()));
    _result.append(' ');
  }

  /**
   * Called after the transpiler quit the given expression.
   *
   * @param expression The expression that was exited.
   */
  private <T> void exit (@NonNull final Expression<T> expression) {
    if (expression instanceof Constant) {
      exitConstant((Constant<T>) expression);
    } else if (expression instanceof Operation) {
      exitOperation((Operation<T>) expression);
    } else if (expression instanceof Placeholder) {
      exitPlaceholder((Placeholder<T>) expression);
    }
  }

  private <T> void exitPlaceholder (@NonNull final Placeholder<T> placeholder) {
    if (placeholder instanceof TableSourcePlaceholder) {
      exitTableSourcePlaceholder((TableSourcePlaceholder<T>) placeholder);
    } else if (placeholder instanceof JoinSourcePlaceholder) {
      exitJoinSourcePlaceholder((JoinSourcePlaceholder<T>) placeholder);
    } else if (placeholder instanceof SelectionPlaceholder) {
      exitSelectionPlaceholder((SelectionPlaceholder<T>) placeholder);
    } else {
      _result.append("?");
    }
  }

  private <T> void exitSelectionPlaceholder (@NonNull final SelectionPlaceholder<T> placeholder) {
    _result.append(placeholder.getSelection().getName());
  }

  private <T> void exitJoinSourcePlaceholder (
    @NonNull final JoinSourcePlaceholder<T> placeholder
  ) {
    _result.append(placeholder.getSource().getName());
    _result.append('.');
    _result.append(placeholder.getColumn().getName());
  }

  private <T> void exitTableSourcePlaceholder (
    @NonNull final TableSourcePlaceholder<T> placeholder
  ) {
    _result.append(placeholder.getSource().getName());
    _result.append('.');
    _result.append(placeholder.getColumn().getName());
  }

  /**
   * Called after the transpiler quit the given operation.
   *
   * @param operation The operation that was exited.
   */
  private <T> void exitOperation (@NonNull final Operation<T> operation) {
    if (doViolatePrecedence()) _result.append(')');
    _operators.remove(_operators.size() - 1);
  }

  /**
   * Called when the transpiler exit a constant expression.
   *
   * @param expression The exited expression.
   * @param <T>        DataType of the constant expression.
   */
  private <T> void exitConstant (@NonNull final Constant<T> expression) {
    if (Boolean.class.isAssignableFrom(expression.getResultType().getJavaClass())) {
      output((Boolean) expression.getValue());
    } else if (Number.class.isAssignableFrom(expression.getResultType().getJavaClass())) {
      output((Number) expression.getValue());
    } else if (Character.class.isAssignableFrom(expression.getResultType().getJavaClass())) {
      output((Character) expression.getValue());
    } else if (String.class.isAssignableFrom(expression.getResultType().getJavaClass())) {
      output((String) expression.getValue());
    } else {
      throw new Error("Unhandled constant type " + expression.getResultType());
    }
  }

  /**
   * Output a numeric value.
   *
   * @param value A numeric value to output.
   */
  private void output (@Nullable final Number value) {
    _result.append(value == null ? "NULL" : value.toString());
  }

  /**
   * Output a boolean value.
   *
   * @param value A boolean value to output.
   */
  private void output (@Nullable final Boolean value) {
    _result.append(value == null ? "NULL" : (value ? "1" : "0"));
  }

  /**
   * Output a string value.
   *
   * @param value A string value to output.
   */
  private void output (@Nullable final String value) {
    if (value == null) {
      _result.append("NULL");
    } else {
      _result.append('"');
      _result.append(StringEscapeUtils.escapeJava(value));
      _result.append('"');
    }
  }

  /**
   * Output a character value.
   *
   * @param value A character value to output.
   */
  private void output (@Nullable final Character value) {
    if (value == null) {
      _result.append("NULL");
    } else {
      _result.append('\"');
      _result.append(StringEscapeUtils.escapeJava(value.toString()));
      _result.append('\"');
    }
  }

  /**
   * @return True if the current operation violates the operator precedence.
   */
  private boolean doViolatePrecedence () {
    return _operators.size() > 1 && (
                                      PRECEDENCE.get(_operators.get(_operators.size() - 2)
                                                               .getIdentifier())
                                    ) < PRECEDENCE.get(_operators.get(_operators.size() - 1)
                                                                 .getIdentifier());
  }
}
