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

package org.liara.collection;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.org.apache.commons.lang3.NotImplementedException;
import org.liara.collection.operator.cursoring.Cursor;
import org.liara.collection.operator.cursoring.CursorableCollection;
import org.liara.collection.operator.filtering.Filter;
import org.liara.collection.operator.filtering.FilterableCollection;
import org.liara.collection.operator.grouping.Group;
import org.liara.collection.operator.grouping.GroupableCollection;
import org.liara.collection.operator.ordering.Order;
import org.liara.collection.operator.ordering.OrderableCollection;
import org.liara.collection.operator.selection.SelectableCollection;
import org.liara.collection.operator.selection.Selection;
import org.liara.collection.source.GraphSource;
import org.liara.collection.util.Filters;
import org.liara.collection.util.Groups;
import org.liara.collection.util.Orderings;
import org.liara.collection.util.Selections;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GraphCollection
  implements Collection,
             SelectableCollection,
             FilterableCollection,
             OrderableCollection,
             GroupableCollection,
             CursorableCollection
{
  @NonNull
  private final GraphSource _source;

  @NonNull
  private final Cursor _cursor;

  @NonNull
  private final Filters _filters;

  @NonNull
  private final Orderings _orderings;

  @NonNull
  private final Groups _groups;

  @NonNull
  private final Selections _selections;

  public GraphCollection (@NonNull final GraphSource source) {
    _source = source;
    _cursor = Cursor.ALL;
    _filters = new Filters();
    _orderings = new Orderings();
    _groups = new Groups();
    _selections = new Selections();
  }

  private GraphCollection (
    @NonNull final GraphCollection collection,
    @NonNull final Cursor cursor
  ) {
    _source = collection._source;
    _cursor = cursor;
    _filters = collection._filters;
    _orderings = collection._orderings;
    _groups = collection._groups;
    _selections = collection._selections;
  }

  private GraphCollection (
    @NonNull final GraphCollection collection,
    @NonNull final Filters filters
  ) {
    _source = collection._source;
    _cursor = collection._cursor;
    _filters = filters;
    _orderings = collection._orderings;
    _groups = collection._groups;
    _selections = collection._selections;
  }

  private GraphCollection (
    @NonNull final GraphCollection collection,
    @NonNull final Groups groups
  ) {
    _source = collection._source;
    _cursor = collection._cursor;
    _filters = collection._filters;
    _orderings = collection._orderings;
    _groups = groups;
    _selections = collection._selections;
  }

  private GraphCollection (
    @NonNull final GraphCollection collection,
    @NonNull final Orderings orderings
  ) {
    _source = collection._source;
    _cursor = collection._cursor;
    _filters = collection._filters;
    _orderings = orderings;
    _groups = collection._groups;
    _selections = collection._selections;
  }

  private GraphCollection (
    @NonNull final GraphCollection collection,
    @NonNull final Selections selections
  ) {
    _source = collection._source;
    _cursor = collection._cursor;
    _filters = collection._filters;
    _orderings = collection._orderings;
    _groups = collection._groups;
    _selections = selections;
  }

  @Override
  public @NonNull Cursor getCursor () {
    return _cursor;
  }

  @Override
  public @NonNull GraphCollection setCursor (@NonNull final Cursor cursor) {
    return new GraphCollection(this, cursor);
  }

  @Override
  public @NonNull GraphCollection addFilter (@NonNull final Filter filter) {
    return new GraphCollection(this, _filters.add(filter));
  }

  @Override
  public @NonNull GraphCollection removeFilter (@NonNull final Filter filter) {
    return new GraphCollection(this, _filters.remove(filter));
  }

  @Override
  public @NonNull Set<@NonNull Filter> getFilters () {
    return _filters.getFilters();
  }

  @Override
  public @NonNull GraphCollection groupBy (@NonNull final Group group) {
    return new GraphCollection(this, _groups.groupBy(group));
  }

  @Override
  public @NonNull GraphCollection ungroup (@NonNull final Group group) {
    return new GraphCollection(this, _groups.remove(group));
  }

  @Override
  public @NonNull List<@NonNull Group> getGroups () {
    return _groups.getGroups();
  }

  @Override
  public @NonNull GraphCollection orderBy (@NonNull final Order order) {
    return new GraphCollection(this, _orderings.orderBy(order));
  }

  @Override
  public @NonNull GraphCollection removeOrder (@NonNull final Order order) {
    return new GraphCollection(this, _orderings.orderBy(order));
  }

  @Override
  public @NonNull List<@NonNull Order> getOrderings () {
    return _orderings.getOrderings();
  }

  @Override
  public @NonNull GraphCollection select (@NonNull final Selection selection) {
    return new GraphCollection(this, _selections.select(selection));
  }

  @Override
  public @NonNull GraphCollection deselect (@NonNull final Selection selection) {
    return new GraphCollection(this, _selections.remove(selection));
  }

  @Override
  public @NonNull Selection<?> getSelection (@NonNull final String name) {
    throw new NotImplementedException("GraphCollection#getSelection(String)");
  }

  @Override
  public @NonNull List<@NonNull Selection> getSelections () {
    return _selections.getSelections();
  }

  public @NonNull GraphSource getSource () {
    return _source;
  }

  @Override
  public boolean equals (@Nullable final Object other) {
    if (other == null) return false;
    if (other == this) return true;

    if (other instanceof GraphCollection) {
      @NonNull final GraphCollection otherGraphCollection = (GraphCollection) other;

      return (
        Objects.equals(
          _source,
          otherGraphCollection.getSource()
        ) &&
        Objects.equals(
          _cursor,
          otherGraphCollection.getCursor()
        ) &&
        Objects.equals(
          _filters,
          otherGraphCollection._filters
        ) &&
        Objects.equals(
          _orderings,
          otherGraphCollection._orderings
        ) &&
        Objects.equals(
          _groups,
          otherGraphCollection._groups
        ) &&
        Objects.equals(
          _selections,
          otherGraphCollection._selections
        )
      );
    }

    return false;
  }

  @Override
  public int hashCode () {
    return Objects.hash(_source, _cursor, _filters, _orderings, _groups, _selections);
  }
}
