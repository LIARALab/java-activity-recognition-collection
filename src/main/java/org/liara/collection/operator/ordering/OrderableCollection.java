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

import java.util.List;

/**
 * An interface for collections that can be ordered.
 */
public interface OrderableCollection<Model>
  extends Collection<Model>
{
  /**
   * Create a new collection that is a copy of this collection ordered in accordance with a given ordering operator.
   * <p>
   * Calling this method multiple times will stack orderings operations in priority from the oldest one to the newest
   * one.
   *
   * @param order An ordering operation to apply.
   *
   * @return A new ordered instance of this collection.
   */
  @NonNull OrderableCollection<Model> orderBy (@NonNull final Order order);

  @NonNull OrderableCollection<Model> removeOrder (@NonNull final Order order);

  @NonNull List<@NonNull Order> getOrderings ();

  default boolean isOrdered () {
    return !getOrderings().isEmpty();
  }
}
