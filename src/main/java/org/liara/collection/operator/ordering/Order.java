/*******************************************************************************
 * Copyright (C) 2018 Cedric DEMONGIVERT <cedric.demongivert@gmail.com>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package org.liara.collection.operator.ordering;

import javax.persistence.metamodel.Attribute;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.liara.collection.Collection;
import org.liara.collection.operator.Operator;

import java.util.Objects;

public final class Order
  implements Operator
{
  @NonNull
  private final String _field;
  
  @NonNull
  private final OrderingDirection _direction;

  public static @NonNull Order field (@NonNull final String name) {
    return new Order(name);
  }

  public static @NonNull Order field (@NonNull final Attribute<?, ?> attribute) {
    return new Order(attribute);
  }
  
  /**
   * Create a new ascending ordering operation for a given field.
   * 
   * @param field A field to order.
   */
  public Order (
    @NonNull final String field
  ) {
    _field = field;
    _direction = OrderingDirection.ASCENDING;
  }

  /**
   * Create a new ascending ordering operation for a given field.
   *
   * @param field A field to order.
   */
  public Order (
    @NonNull final Attribute<?, ?> field
  ) {
    _field = ":this." + field.getName();
    _direction = OrderingDirection.ASCENDING;
  }

  /**
   * Create a new ordering operation for a given field.
   * 
   * @param field A field to order.
   * @param direction An ordering direction.
   */
  public Order (
    @NonNull final String field,
    @NonNull final OrderingDirection direction
  ) {
    _field = field;
    _direction = direction;
  }

  /**
   * Create a new ordering operation for a given field.
   *
   * @param field A field to order.
   * @param direction An ordering direction.
   */
  public Order (
    @NonNull final Attribute<?, ?> field,
    @NonNull final OrderingDirection direction
  ) {
    _field = field.getName();
    _direction = direction;
  }

  /**
   * Create a copy of another ordering operator.
   *
   * @param toCopy An ordering operator to copy.
   */
  public Order (
    @NonNull final Order toCopy
  ) {
    _field = toCopy.getField();
    _direction = toCopy.getDirection();
  }

  /**
   * @see Operator#apply(Collection)
   */
  @Override
  public Collection apply (@NonNull final Collection collection) {
    if (collection instanceof OrderableCollection) {
      return OrderableCollection.class.cast(collection).orderBy(this);
    }

    return collection;
  }

  /**
   * @return The field to order.
   */
  public @NonNull String getField () {
    return _field;
  }

  /**
   * Return a new ordering operator instance based on this one with another ordered field.
   *
   * @param field The new field to order.
   *
   * @return A new ordering operator instance based on this one with another ordered field.
   */
  public @NonNull Order setField (@NonNull final String field) {
    return new Order(field, _direction);
  }

  /**
   * Return a new ordering operator instance based on this one with another ordered field.
   *
   * @param field The new field to order.
   *
   * @return A new ordering operator instance based on this one with another ordered field.
   */
  public @NonNull Order setField (@NonNull final Attribute<?, ?> field) {
    return new Order(field, _direction);
  }

  /**
   * @return The ordering direction of this operator.
   */
  public @NonNull OrderingDirection getDirection () {
    return _direction;
  }

  /**
   * Return a new ordering operator instance based on this one with another ordering direction.
   *
   * @param direction The new ordering direction.
   *
   * @return A new ordering operator instance based on this one with another ordering direction.
   */
  public @NonNull Order setDirection (@NonNull final OrderingDirection direction) {
    return new Order(_field, direction);
  }

  /**
   * Alias of Order#setDirection(OrderingDirection.ASCENDING).
   *
   * @see Order#setDirection(OrderingDirection)
   */
  public @NonNull Order ascending () {
    return setDirection(OrderingDirection.ASCENDING);
  }


  /**
   * Alias of Order#setDirection(OrderingDirection.DESCENDING).
   *
   * @see Order#setDirection(OrderingDirection)
   */
  public @NonNull Order descending () {
    return setDirection(OrderingDirection.DESCENDING);
  }

  /**
   * @see Order#hashCode()
   */
  @Override
  public int hashCode () {
    return Objects.hash(_field, _direction);
  }

  /**
   * @see Order#equals(Object)
   */
  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof Order) {
      final Order otherOrder = (Order) other;
      return _field == otherOrder.getField() && _direction == otherOrder.getDirection();
    }

    return false;
  }
}
