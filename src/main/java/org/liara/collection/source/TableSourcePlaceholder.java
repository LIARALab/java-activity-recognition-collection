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
import org.liara.data.graph.Column;
import org.liara.data.primitive.Primitive;
import org.liara.expression.Expression;
import org.liara.expression.Placeholder;
import org.liara.support.view.View;

public class TableSourcePlaceholder<Type>
  implements SourcePlaceholder<Type>
{
  @NonNull
  private static final View<@NonNull Expression> CHILDREN = View.readonly(Expression.class);

  @NonNull
  private final TableSource _source;

  @NonNull
  private final Column<Type> _column;

  public TableSourcePlaceholder (
    @NonNull final TableSource source,
    @NonNull final Column<Type> column
  ) {
    _source = source;
    _column = column;
  }

  /**
   * @see SourcePlaceholder#getSource()
   */
  @Override
  public @NonNull TableSource getSource () {
    return _source;
  }

  /**
   * @return The table column's targeted by this placeholder.
   */
  public @NonNull Column<Type> getColumn () {
    return _column;
  }

  /**
   * @see Placeholder#getResultType()
   */
  @Override
  public @NonNull Primitive<Type> getResultType () {
    return _column.getType();
  }

  /**
   * @see Expression#getChildren()
   */
  @Override
  public @NonNull View<Expression> getChildren () {
    return CHILDREN;
  }
}
