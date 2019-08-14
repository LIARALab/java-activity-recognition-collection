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

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.data.primitive.Primitive;
import org.liara.data.primitive.Primitives;
import org.liara.expression.Constant;
import org.liara.expression.Expression;
import org.liara.expression.Placeholder;

public interface GraphSource
  extends Source
{
  /**
   * Return a placeholder expression for the column with the given name of this source.
   *
   * @param name Name of the column from which getting a placeholder.
   *
   * @return A placeholder for the column with the given name.
   */
  @NonNull Placeholder<?> getOwnPlaceholder (@NonNegative final String name);

  default @NonNull GraphSource innerJoin (
    @NonNull final TableSource source
  ) {
    return innerJoin(source, new Constant<>(Primitives.BOOLEAN, true));
  }

  default @NonNull GraphSource innerJoin (
    @NonNull final TableSource source,
    @NonNull final Expression<Boolean> expression
  ) {
    return JoinSource.inner(this, source, expression);
  }

  /**
   * Return a placeholder expression for the column with the given name of this source.
   *
   * @param expectedType Expected type of the column.
   * @param name         Name of the column from which getting a placeholder.
   *
   * @return A placeholder for the column with the given name.
   */
  @SuppressWarnings("unchecked")
  // Placeholder type test.
  <Type> @NonNull Placeholder<Type> getOwnPlaceholder (
    @NonNull final Primitive<Type> expectedType,
    @NonNegative final String name
  );
}
