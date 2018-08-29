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
package org.liara.collection.operator;

import org.checkerframework.checker.index.qual.LTLengthOf;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.com.google.common.collect.Iterators;
import org.liara.collection.Collection;

import java.util.*;

/**
 * A conjunction of two or more transformation.
 *
 * @author C&eacute;dric DEMONGIVERT [cedric.demongivert@gmail.com](mailto:cedric.demongivert@gmail.com)
 */
public final class Composition implements Operator, Iterable<Operator>
{
  @NonNull
  public static final Composition EMPTY = new Composition();

  /**
   * Return the content of the given iterator as a composition array.
   *
   * @param operators Operators to compose.
   *
   * @return A composition array.
   */
  private static @NonNull Operator[] toArray (@NonNull final Iterator<@NonNull Operator> operators) {
    final LinkedList<Operator> result = new LinkedList<>();

    while (operators.hasNext()) {
      final Operator operator = operators.next();

      if (operator instanceof Composition) {
        for (@NonNull final Operator childOperator : (Composition) operator) {
          result.addLast(operator);
        }
      } else {
        result.addLast(operator);
      }
    }

    return result.toArray(new Operator[result.size()]);
  }

  /**
   * Compose operators.
   *
   * Operators will be applied by using their declaration order, the most right operator will be applied first and
   * the most left operator will be the last applied.
   *
   * @param operators Operators to compose.
   *
   * @return A composition of the given operators.
   */
  public static Operator of (@NonNull final Operator... operators)
  {
    if (operators.length <= 0) {
      return Composition.EMPTY;
    } else {
      return new Composition(operators);
    }
  }

  @NonNull
  private final Operator[] _operators;

  /**
   * Create a new empty composition.
   *
   * An empty composition is equivalent to an identity operator.
   */
  public Composition () {
    _operators = new Operator[0];
  }

  /**
   * Create a composition of operators.
   *
   * Operators will be applied by using their declaration order, the most right operator will be applied first and
   * the most left operator will be the last applied.
   *
   * @param operators Operators to compose.
   */
  public Composition (@NonNull final Operator... operators) {
    _operators = Composition.toArray(Arrays.asList(operators).iterator());
  }

  /**
   * Create a composition of operators.
   *
   * Operators will be applied by using their iteration order, the last itered operator will be applied first and the
   * first itered operator will be the last applied.
   *
   * @param operators Operators to compose.
   */
  public Composition (@NonNull final Iterable<Operator> operators) {
    _operators = Composition.toArray(operators.iterator());
  }

  /**
   * Create a composition of operators.
   *
   * Operators will be applied by using their iteration order, the last itered operator will be applied first and the
   * first itered operator will be the last applied.
   *
   * @param operators Operators to compose.
   */
  public Composition (@NonNull final Iterator<Operator> operators) {
    _operators = Composition.toArray(operators);
  }

  /**
   * @see Operator#apply(Collection)
   */
  @Override
  public org.liara.collection.@NonNull Collection apply (final org.liara.collection.@NonNull Collection input) {
    Collection result = input;

    for (int index = 0; index < _operators.length; ++index) {
      result = _operators[_operators.length - index - 1].apply(result);
    }

    return result;
  }

  /**
   * Return an operator of this composition.
   *
   * Operators are ordered from the last called to the first called, so the operator at the first index is the last
   * called, the operator at the last index is the first called.
   *
   * @param index Index of the operator to return from 0 to the size of this composition (excluded).
   *
   * @return The operator at the given index.
   */
  public @NonNull Operator getOperator (@NonNegative @LTLengthOf("_operators") final int index) {
    return _operators[index];
  }

  /**
   * Alias of getOperator for groovy indexing support.
   *
   * @see Composition#getOperator(int)
   */
  public @NonNull Operator getAt (@NonNegative @LTLengthOf("_operators") final int index) {
    return getOperator(index);
  }

  /**
   * Return all operators of this composition as an array.
   *
   * Operators are ordered from the last called to the first called, so the operator at the first index is the last
   * called, the operator at the last index is the first called.
   *
   * @return All operators of this composition as an array.
   */
  public @NonNull Operator[] getOperators () {
    return Arrays.copyOf(_operators, _operators.length);
  }

  /**
   * Return the number of operators into this composition.
   *
   * @return The number of operators into this composition.
   */
  public @NonNegative int getSize () {
    return _operators.length;
  }

  /**
   * @see Iterable#iterator()
   */
  public @NonNull Iterator<Operator> iterator () {
    return Collections.unmodifiableList(Arrays.asList(_operators)).iterator();
  }

  @Override
  public int hashCode () {
    return Arrays.deepHashCode(_operators);
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof Composition) {
      @NonNull final Composition otherComposition = (Composition) other;

      if (otherComposition.getSize() != _operators.length) return false;

      for (int index = 0; index < _operators.length; ++index) {
        if (!Objects.equals(_operators[index], otherComposition.getOperator(index))) {
          return false;
        }
      }

      return true;
    }

    return false;
  }
}
