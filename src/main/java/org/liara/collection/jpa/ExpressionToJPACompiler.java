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

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.operator.selection.SelectionPlaceholder;
import org.liara.collection.source.JoinSourcePlaceholder;
import org.liara.collection.source.TableSourcePlaceholder;
import org.liara.expression.Expression;
import org.liara.expression.Placeholder;
import org.liara.expression.sql.ExpressionToSQLCompiler;
import org.liara.support.tree.TreeWalker;

import java.util.List;

public class ExpressionToJPACompiler
{
  @NonNull
  private final StringBuilder _result;

  @NonNull
  private final ExpressionToSQLCompiler _compiler;

  /**
   * Instantiate a new expression to SQL transpiler.
   */
  public ExpressionToJPACompiler () {
    _compiler = new ExpressionToSQLCompiler();
    _result = new StringBuilder();
  }

  /**
   * Compile the given expression into an SQL string.
   *
   * @param output The string builder to fill with the compiled content.
   */
  public void compile (@NonNull final StringBuilder output) {
    while (!_compiler.isAtEnd()) {
      while (_compiler.canEnter()) {
        enter(output);
      }

      if (_compiler.canExit()) {
        exit(output);
      }
    }
  }

  /**
   * Let the compiler enter into the next child expression.
   *
   * @param output The string builder to fill with the compiled content.
   *
   * @return The entered expression.
   */
  public @NonNull Expression<?> enter (@NonNull final StringBuilder output) {
    return _compiler.enter(output);
  }

  /**
   * Let the compiler moves out of its current child expression.
   *
   * @param output The string builder to fill with the compiled content.
   *
   * @return The exited expression.
   */
  public @NonNull Expression<?> exit (@NonNull final StringBuilder output) {
    @NonNull final Expression<?> expression = _compiler.exit(output);

    if (expression instanceof Placeholder) {
      exitPlaceholder((Placeholder<?>) expression, output);
    }

    if (_compiler.hasCurrent()) {
      _compiler.back(output);
    }

    return expression;
  }

  private <T> void exitPlaceholder (
    @NonNull final Placeholder<T> placeholder,
    @NonNull final StringBuilder output
  ) {
    if (placeholder instanceof TableSourcePlaceholder) {
      exitTableSourcePlaceholder((TableSourcePlaceholder<T>) placeholder, output);
    } else if (placeholder instanceof JoinSourcePlaceholder) {
      exitJoinSourcePlaceholder((JoinSourcePlaceholder<T>) placeholder, output);
    } else if (placeholder instanceof SelectionPlaceholder) {
      exitSelectionPlaceholder((SelectionPlaceholder<T>) placeholder, output);
    } else {
      output.append("?");
    }
  }

  private <T> void exitSelectionPlaceholder (
    @NonNull final SelectionPlaceholder<T> placeholder,
    @NonNull final StringBuilder output
  ) {
    output.append(placeholder.getSelect().getName());
  }

  private <T> void exitJoinSourcePlaceholder (
    @NonNull final JoinSourcePlaceholder<T> placeholder,
    @NonNull final StringBuilder output
  ) {
    output.append(placeholder.getSource().getName());
    output.append('.');
    output.append(placeholder.getColumn().getName());
  }

  private <T> void exitTableSourcePlaceholder (
    @NonNull final TableSourcePlaceholder<T> placeholder,
    @NonNull final StringBuilder output
  ) {
    output.append(placeholder.getSource().getName());
    output.append('.');
    output.append(placeholder.getColumn().getName());
  }

  public void reset () {
    _compiler.reset();
  }

  public boolean canEnter () {
    return _compiler.canEnter();
  }

  public @NonNull Expression current () {
    return _compiler.current();
  }

  public boolean hasCurrent () {
    return _compiler.hasCurrent();
  }

  public boolean canExit () {
    return _compiler.canExit();
  }

  public @Nullable Expression<?> getExpression () {
    return _compiler.getExpression();
  }

  public void setExpression (@Nullable final Expression<?> expression) {
    _compiler.setExpression(expression);
  }

  public boolean isAtLocation (@NonNull final TreeWalker<Expression> walker) {
    return _compiler.isAtLocation(walker);
  }

  public boolean isAtEnd () {
    return _compiler.isAtEnd();
  }

  public boolean isAtStart () {
    return _compiler.isAtStart();
  }

  public boolean doesMoveForward () {
    return _compiler.doesMoveForward();
  }

  public void setMovingForward (final boolean forward) {
    _compiler.setMovingForward(forward);
  }

  public @NonNull List<@NonNull Expression> getPath () {
    return _compiler.getPath();
  }
}
