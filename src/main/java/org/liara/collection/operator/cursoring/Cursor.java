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

package org.liara.collection.operator.cursoring;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.Collection;
import org.liara.collection.operator.Operator;

import java.util.Objects;

/**
 * A cursor that select a range of entities in a collection.
 *
 * @author C&eacute;dric DEMONGIVERT [cedric.demongivert@gmail.com](mailto:cedric.demongivert@gmail.com)
 */
public class Cursor implements Operator
{
  /**
   * A cursor that select an entire collection of entities.
   */
  @NonNull
  public static final Cursor ALL = new Cursor(0, Integer.MAX_VALUE);

  /**
   * The default application cursor. Skip 0 entities and display 10 entities from the given collection.
   */
  @NonNull
  public static final Cursor DEFAULT = new Cursor(0, 10);

  /**
   * A cursor that select the first element.
   */
  @NonNull
  public static final Cursor FIRST = new Cursor(0, 1);

  /**
   * An empty cursor that does not skip entities and does not display any entities of the given collection.
   */
  @NonNull
  public static final Cursor NONE = new Cursor(0, 0);

  @NonNegative
  private final int _offset;

  @NonNegative
  private final int _limit;

  /**
   * Create a new empty cursor that does not skip entities and display all entities of a given collection.
   */
  public Cursor () {
    _offset = 0;
    _limit = Integer.MAX_VALUE;
  }

  /**
   * Create a cursor that does not skip any entities and limit entities to display.
   *
   * @param limit Maximum number of entities to display.
   */
  public Cursor (@NonNegative final int limit) {
    _offset = 0;
    _limit = limit;
  }

  /**
   * Create a cursor that skip a given amount of entities and also limit entities to display.
   *
   * @param offset Amount of entities to skip.
   * @param limit  Maximum number of entities to display.
   */
  public Cursor (
    @NonNegative final int offset,
    @NonNegative final int limit
  )
  {
    _offset = offset;
    _limit = limit;
  }

  /**
   * Create a copy of a given cursor.
   *
   * @param cursor The cursor instance to copy.
   */
  public Cursor (@NonNull final Cursor cursor) {
    _offset = cursor.getOffset();
    _limit = cursor.getLimit();
  }

  /**
   * @return The amount of entities to skip.
   */
  public @NonNegative int getOffset () {
    return _offset;
  }

  /**
   * Return a new cursor based on this one with a new offset value.
   *
   * @param offset The new amount of entities to skip.
   *
   * @return An updated cursor instance with the given offset.
   */
  public @NonNull Cursor setOffset (@NonNegative final int offset) {
    return new Cursor(offset, _limit);
  }

  /**
   * @return The maximum number of entities to display.
   */
  public @NonNegative int getLimit () {
    return _limit;
  }

  /**
   * Return a new cursor based on this one with a new limit value.
   *
   * @param limit The new maximum number of entities to display.
   *
   * @return An updated cursor instance with the given limit.
   */
  public @NonNull Cursor setLimit (@NonNegative final int limit) {
    return new Cursor(_offset, limit);
  }

  /**
   * Return a new cursor based on this one without any limits.
   *
   * @return An updated cursor instance that does not limit the number of entities to display.
   */
  public @NonNull Cursor unlimit () {
    return new Cursor(_offset, Integer.MAX_VALUE);
  }

  /**
   * Return a new cursor based on this one without any skipped entities.
   *
   * @return An updated cursor instance that does not skip any entities.
   */
  public @NonNull Cursor unskip () {
    return new Cursor(0, _limit);
  }

  /**
   * @return True if the given cursor limit the number of entities to display.
   */
  public boolean hasLimit () {
    return _limit != Integer.MAX_VALUE;
  }

  /**
   * @see Operator#apply(Operator)
   */
  public <Model> @NonNull Collection<Model> apply (@NonNull final Collection<Model> collection) {
    if (collection instanceof CursorableCollection) {
      return ((CursorableCollection<Model>) collection).setCursor(this);
    }

    return collection;
  }

  /**
   * @see Object#hashCode()
   */
  @Override
  public int hashCode () {
    return Objects.hash(_offset, _limit);
  }

  /**
   * @see Object#equals(Object)
   */
  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof Cursor) {
      final Cursor cursor = (Cursor) other;
      return getLimit() == cursor.getLimit() && getOffset() == cursor.getOffset();
    } else {
      return false;
    }
  }
}
