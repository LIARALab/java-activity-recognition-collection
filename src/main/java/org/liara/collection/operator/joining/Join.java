/*
 * Copyright (C) 2018 Cedric DEMONGIVERT <cedric.demongivert@gmail.com>
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

package org.liara.collection.operator.joining;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.Collection;
import org.liara.collection.operator.Operator;

public class Join
  implements Operator
{
  @NonNull
  private final String _field;

  public Join (@NonNull final String field) {
    _field = field;
  }

  public Join (@NonNull final Join toCopy) {
    _field = toCopy.getField();
  }

  @Override
  public @NonNull Operator apply (@NonNull final Operator child) {
    if (child instanceof JoinableOperator) {
      return ((JoinableOperator) child).join(this);
    }

    return child;
  }

  @Override
  public @NonNull Collection apply (@NonNull final Collection input) {
    @NonNull final Operator operator = input.getOperator();
    @NonNull final Operator joined   = this.apply(operator);

    return input.clear().apply(joined);
  }


  public @NonNull String getField () {
    return _field;
  }
}
