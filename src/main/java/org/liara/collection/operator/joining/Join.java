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

package org.liara.collection.operator.joining;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.Collection;
import org.liara.collection.operator.Operator;

public interface Join<Related>
  extends Operator,
          JoinableOperator
{
  static <T> InnerJoin<T> inner (@NonNull final Class<T> relatedClass, @NonNull final String name) {
    return new InnerJoin<>(relatedClass, name);
  }

  static <T> InnerJoin<T> inner (@NonNull final Class<T> relatedClass) {
    return new InnerJoin<>(relatedClass);
  }

  static <T> Embeddable<T> embeddable (@NonNull final Class<T> relatedClass, @NonNull final String name) {
    return new Embeddable<>(relatedClass, name);
  }

  static <T> Embeddable<T> embeddable (@NonNull final Class<T> relatedClass) {
    return new Embeddable<>(relatedClass);
  }

  @NonNull String getName ();

  default @NonNull String identifier () {
    return getName();
  }

  @NonNull Class<Related> getRelatedClass ();

  @Override
  default @NonNull Join<Related> join (@NonNull final Join join) {
    return new DeepJoin<>(join, this);
  }

  @Override
  default @NonNull Collection apply (final @NonNull Collection input) {
    if (input instanceof JoinableCollection) {
      return ((JoinableCollection) input).join(this);
    }

    return input;
  }

  @Override
  default @NonNull Operator apply (@NonNull final Operator child) {
    if (child instanceof JoinableOperator) {
      return ((JoinableOperator) child).join(this);
    } else {
      return JoinableOperator.super.apply(child);
    }
  }
}
