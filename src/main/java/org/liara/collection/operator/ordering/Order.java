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

package org.liara.collection.operator.ordering;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.Collection;
import org.liara.collection.operator.Operator;
import org.liara.collection.operator.joining.Join;

import javax.persistence.metamodel.Attribute;

public interface Order
  extends Operator
{
  static @NonNull Order expression (@NonNull final String name) {
    return new ExpressionOrder(name);
  }

  static @NonNull Order expression (@NonNull final Attribute<?, ?> attribute) {
    return new ExpressionOrder(attribute);
  }

  static @NonNull Order join (@NonNull final Join join, @NonNull final Order order) {
    return new JoinOrder(join, order);
  }

  /**
   * @see Operator#apply(Collection)
   */
  @Override
  default <Model> @NonNull Collection<Model> apply (@NonNull final Collection<Model> collection) {
    if (collection instanceof OrderableCollection) {
      return ((OrderableCollection<Model>) collection).orderBy(this);
    }

    return collection;
  }

  @NonNull String getExpression ();

  @NonNull OrderingDirection getDirection ();

  @NonNull Order setDirection (@NonNull final OrderingDirection direction);

  @NonNull Order ascending ();

  @NonNull Order descending ();
}
