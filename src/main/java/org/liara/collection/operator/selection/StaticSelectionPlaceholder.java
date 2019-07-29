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

package org.liara.collection.operator.selection;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.data.primitive.Primitive;
import org.liara.expression.Expression;
import org.liara.support.view.View;

public class StaticSelectionPlaceholder<Type>
  implements SelectionPlaceholder<Type>
{
  @NonNull
  private static final View<@NonNull Expression> CHILDREN = View.readonly(Expression.class);

  @NonNull
  private final Select<Type> _select;

  public StaticSelectionPlaceholder (@NonNull final Select<Type> select) {
    _select = select;
  }

  @Override
  public @NonNull Select<Type> getSelect () {
    return _select;
  }

  @Override
  public @NonNull Primitive<Type> getResultType () {
    return _select.getExpression().getResultType();
  }

  @Override
  public @NonNull View<Expression> getChildren () {
    return CHILDREN;
  }
}
