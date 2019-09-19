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

package org.liara.collection.source;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.expression.Expression;
import org.liara.expression.ExpressionFactory;
import org.liara.expression.operation.StaticOperationBuilder;
import org.liara.support.tree.TreeWalker;

import java.util.*;

public class JoinExpressionLinker
{
  @NonNull
  private static final Map<@NonNull Thread, @NonNull JoinExpressionLinker> SINGLETONS = (
    Collections.synchronizedMap(new WeakHashMap<>())
  );
  @NonNull
  private final        StaticOperationBuilder                              _builder;
  @NonNull
  private final        TreeWalker<Expression>                              _walker;
  @NonNull
  private final        ArrayList<@NonNull Expression>                      _stack;
  @NonNull
  private final        ArrayList<@NonNull Integer>                         _cursors;
  @NonNull
  private final        ArrayList<@NonNull Boolean>                         _identity;
  @Nullable
  private              JoinSource                                          _linked;
  @NonNull
  private final        ExpressionFactory                                   _expressionFactory;

  public JoinExpressionLinker () {
    _walker = new TreeWalker<>(Expression.class);
    _stack = new ArrayList<>(10);
    _cursors = new ArrayList<>(10);
    _identity = new ArrayList<>(10);
    _builder = new StaticOperationBuilder();
    _linked = null;
    _expressionFactory = new ExpressionFactory();
  }

  public static @NonNull JoinExpressionLinker getInstance () {
    @NonNull final Thread thread = Thread.currentThread();

    if (!SINGLETONS.containsKey(thread)) {
      SINGLETONS.put(thread, new JoinExpressionLinker());
    }

    return SINGLETONS.get(thread);
  }

  @SuppressWarnings("unchecked")
  public <Result> @NonNull Expression<Result> link (
    @NonNull final JoinSource linked,
    @NonNull final Expression<Boolean> expression
  ) {
    _linked = linked;

    _walker.setRoot(expression);
    _walker.movesForward();

    while (!_walker.isAtEnd()) {
      while (_walker.canEnter()) {
        _walker.enter();
        onEntering((Expression<?>) _walker.current());
      }

      if (_walker.canExit()) {
        onExiting(_walker.current());
        _walker.exit();
      }
    }

    _walker.setRoot(null);

    @NonNull final Expression<Result> result = (Expression<Result>) _stack.get(0);

    _stack.clear();
    _cursors.clear();
    _identity.clear();

    return result;
  }

  private <Type> void onExiting (@NonNull final Expression<Type> current) {
    if (current instanceof SourcePlaceholder) {
      onExitingSourcePlaceholder((SourcePlaceholder<?>) current);
    } else if (!_identity.get(_identity.size() - 1)) {
      onExitingLinkedExpression(current);
    } else {
      onExitingIdentityExpression(current);
    }
  }

  private <Type> void onExitingIdentityExpression (@NonNull final Expression<Type> current) {
    final int cursor = _cursors.get(_cursors.size() - 1);

    while (_stack.size() > cursor) {
      _stack.remove(_stack.size() - 1);
    }

    _stack.add(current);
    _identity.remove(_identity.size() - 1);
    _cursors.remove(_cursors.size() - 1);
  }

  private <Type> void onExitingLinkedExpression (@NonNull final Expression<Type> current) {
    @NonNull final Expression<Type> linked;
    final int                       cursor = _cursors.get(_cursors.size() - 1);

    @NonNull final Expression[] children = new Expression[_stack.size() - cursor];

    for (int index = 0, size = children.length; index < size; ++index) {
      children[index] = _stack.get(cursor + index);
    }

    linked = (Expression<Type>) _expressionFactory.rewrite(current, children);

    while (_stack.size() > cursor) {
      _stack.remove(_stack.size() - 1);
    }

    _stack.add(linked);
    _identity.remove(_identity.size() - 1);
    if (_identity.size() > 0) {
      _identity.set(_identity.size() - 1, false);
    }
    _cursors.remove(_cursors.size() - 1);
  }


  private void onExitingSourcePlaceholder (@NonNull final SourcePlaceholder<?> current) {
    Objects.requireNonNull(_linked);

    if (_linked.getOrigin().contains(current)) {
      onExitingIdentityExpression(current);
    } else if (_linked.getJoined().contains(current)) {
      onExitingLinkedPlaceholder((TableSourcePlaceholder<?>) current);
    } else {
      throw new IllegalArgumentException(
        "Unable to link the given expression to the join " + _linked + " because some " +
        "source placeholders of the given expression does not belongs to its origin source or to " +
        "its linked source."
      );
    }
  }

  private void onExitingLinkedPlaceholder (@NonNull final TableSourcePlaceholder<?> current) {
    Objects.requireNonNull(_linked);

    _stack.add(_linked.getOwnPlaceholder(current.getColumn()));
    _identity.remove(_identity.size() - 1);
    _cursors.remove(_cursors.size() - 1);
  }

  private <Type> void onEntering (@NonNull final Expression<Type> entered) {
    _cursors.add(_stack.size());
    _identity.add(true);
  }
}
