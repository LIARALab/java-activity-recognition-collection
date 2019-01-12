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

package org.liara.collection.operator.filtering;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.Collection;
import org.liara.collection.jpa.JPAEntityCollection;
import org.liara.collection.operator.Operator;
import org.liara.collection.operator.joining.Join;

import java.util.Map;

public interface Filter
       extends Operator
{
  static @NonNull Filter expression (@NonNull final String expression) {
    return new ExpressionFilter(expression);
  }

  static @NonNull Filter exists (@NonNull final JPAEntityCollection collection) {
    return new ExistsFilter(collection);
  }

  static @NonNull Filter join (@NonNull final Join join, @NonNull final Filter filter) {
    return new JoinFilter(join, filter);
  }

  @Override
  default @NonNull Collection apply (@NonNull final Collection input) {
    if (input instanceof FilterableCollection) {
      return ((FilterableCollection) input).addFilter(this);
    }

    return input;
  }

  @NonNull String getExpression ();

  @NonNull Filter setParameter (@NonNull final String name, @Nullable final Object value);

  @NonNull Filter setParameters (@NonNull final Map<@NonNull String, @NonNull Object> parameters);

  @NonNull Filter removeParameter (@NonNull final String name);

  @NonNull Map<@NonNull String, @NonNull Object> getParameters ();
}
