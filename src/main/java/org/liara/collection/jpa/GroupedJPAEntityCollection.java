package org.liara.collection.jpa;

import org.checkerframework.checker.index.qual.LessThan;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.com.google.common.collect.Iterables;
import org.checkerframework.common.value.qual.MinLen;
import org.liara.collection.Collection;
import org.liara.collection.operator.cursoring.Cursor;
import org.liara.collection.operator.cursoring.CursorableCollection;
import org.liara.collection.operator.filtering.Filter;
import org.liara.collection.operator.filtering.FilterableCollection;
import org.liara.collection.operator.grouping.Group;
import org.liara.collection.operator.grouping.GroupableCollection;
import org.liara.collection.operator.ordering.Order;
import org.liara.collection.operator.ordering.OrderableCollection;

import java.util.*;

public class GroupedJPAEntityCollection<Entity>
       implements Collection,
                  FilterableCollection,
                  OrderableCollection,
                  GroupableCollection,
                  CursorableCollection
{
  @NonNull
  private final JPAEntityCollection<Entity> _groupedCollection;

  @NonNull
  private final List<@NonNull Group> _groups;

  @NonNull
  private final Set<@NonNull Filter> _filters;

  @NonNull
  private final List<@NonNull Order> _orderings;

  @NonNull
  private final Cursor _cursor;

  /**
   * Create a new grouped collection from an existing entity collection and a list of groups.
   *
   * @param groupedCollection An existing entity collection to group.
   * @param groups Groups to apply.
   */
  public GroupedJPAEntityCollection (
    @NonNull final JPAEntityCollection<Entity> groupedCollection,
    @NonNull @MinLen(1) final List<@NonNull Group> groups
  ) {
    _groupedCollection = groupedCollection;
    _groups = new ArrayList<>(groups);
    _filters = new HashSet<>();
    _orderings = new ArrayList<>();
    _cursor = Cursor.ALL;
  }

  /**
   * Create a copy of an existing grouped collection.
   *
   * @param toCopy A grouped collection to copy.
   */
  public GroupedJPAEntityCollection (
    @NonNull final GroupedJPAEntityCollection<Entity> toCopy
  ) {
    _groupedCollection = toCopy.getGroupedCollection();
    _groups = new ArrayList<>(toCopy.getGroups());
    _filters = new HashSet<>(toCopy.getFilters());
    _orderings = new ArrayList<>(toCopy.getOrderings());
    _cursor = toCopy.getCursor();
  }

  private GroupedJPAEntityCollection (
    @NonNull final GroupedJPAEntityCollection<Entity> toCopy,
    @NonNull final JPAEntityCollection<Entity> groupedCollection
  ) {
    _groupedCollection = groupedCollection;
    _groups = new ArrayList<>(toCopy.getGroups());
    _filters = new HashSet<>(toCopy.getFilters());
    _orderings = new ArrayList<>(toCopy.getOrderings());
    _cursor = toCopy.getCursor();
  }

  private GroupedJPAEntityCollection (
    @NonNull final GroupedJPAEntityCollection<Entity> toCopy,
    @NonNull final Cursor cursor
  ) {
    _groupedCollection = toCopy.getGroupedCollection();
    _groups = new ArrayList<>(toCopy.getGroups());
    _filters = new HashSet<>(toCopy.getFilters());
    _orderings = new ArrayList<>(toCopy.getOrderings());
    _cursor = cursor;
  }

  private GroupedJPAEntityCollection (
    @NonNull final GroupedJPAEntityCollection<Entity> toCopy,
    @NonNull final Iterable<Group> groups,
    @NonNull final Iterable<Filter> filters,
    @NonNull final Iterable<Order> orderings
  ) {
    _groupedCollection = toCopy.getGroupedCollection();
    _groups = new ArrayList<>();
    _filters = new HashSet<>();
    _orderings = new ArrayList<>();
    _cursor = toCopy.getCursor();

    groups.forEach(_groups::add);
    filters.forEach(_filters::add);
    orderings.forEach(_orderings::add);
  }

  /**
   * Return the underlying grouped collection.
   *
   * @return The underlying grouped collection.
   */
  public JPAEntityCollection<Entity> getGroupedCollection () {
    return _groupedCollection;
  }

  /**
   * Return a new grouped collection like this one except that the new operate over another underlying entity
   * collection.
   *
   * @param groupedCollection A collection to group like this one.
   *
   * @return A grouped collection that group the given collection like this one.
   */
  public GroupedJPAEntityCollection<Entity> setGroupedCollection (
    @NonNull final JPAEntityCollection<Entity> groupedCollection
  ) {
    return new GroupedJPAEntityCollection<Entity>(
      this, groupedCollection
    );
  }

  /**
   * @see CursorableCollection#setCursor(Cursor)
   */
  @Override
  public GroupedJPAEntityCollection<Entity> setCursor (@NonNull final Cursor cursor) {
    return new GroupedJPAEntityCollection<>(this, cursor);
  }

  /**
   * @see CursorableCollection#getCursor()
   */
  @Override
  public Cursor getCursor () {
    return _cursor;
  }

  /**
   * @see FilterableCollection#addFilter(Filter)
   */
  @Override
  public @NonNull GroupedJPAEntityCollection<Entity> addFilter (@NonNull final Filter filter) {
    return new GroupedJPAEntityCollection<>(
      this,
      _groups,
      Iterables.concat(_filters, Collections.singleton(filter)),
      _orderings
    );
  }

  /**
   * @see FilterableCollection#removeFilter(Filter)
   */
  @Override
  public @NonNull GroupedJPAEntityCollection<Entity> removeFilter (@NonNull final Filter filter) {
    return new GroupedJPAEntityCollection<>(
      this,
      _groups,
      Iterables.filter(_filters, x -> !Objects.equals(filter, x)),
      _orderings
    );
  }

  /**
   * @see FilterableCollection#getFilterCount()
   */
  @Override
  public @NonNegative int getFilterCount () {
    return _filters.size();
  }

  /**
   * @see FilterableCollection#getFilters()
   */
  @Override
  public @NonNull Set<@NonNull Filter> getFilters () {
    return Collections.unmodifiableSet(_filters);
  }

  /**
   * @see FilterableCollection#filters()
   */
  @Override
  public @NonNull Iterable<@NonNull Filter> filters () {
    return Collections.unmodifiableSet(_filters);
  }

  /**
   * @see GroupableCollection#groupBy(Group)
   */
  @Override
  public @NonNull GroupedJPAEntityCollection<Entity> groupBy (@NonNull final Group group) {
    return new GroupedJPAEntityCollection<>(
      this,
      Iterables.concat(_groups, Collections.singleton(group)),
      _filters,
      _orderings
    );
  }

  /**
   * @see GroupableCollection#getGroup(int)
   */
  @Override
  public @NonNull Group getGroup (@NonNegative @LessThan("this.getGroupCount()") final int index) {
    return _groups.get(index);
  }

  /**
   * @see GroupableCollection#getGroupCount()
   */
  @Override
  public @NonNegative int getGroupCount () {
    return _groups.size();
  }

  /**
   * @see GroupableCollection#getGroups()
   */
  @Override
  public @NonNull List<@NonNull Group> getGroups () {
    return Collections.unmodifiableList(_groups);
  }

  /**
   * @see GroupableCollection#groups()
   */
  @Override
  public @NonNull Iterable<@NonNull Group> groups () {
    return Collections.unmodifiableList(_groups);
  }

  /**
   * @see OrderableCollection#orderBy(Order)
   */
  @Override
  public @NonNull GroupedJPAEntityCollection<Entity> orderBy (@NonNull final Order order) {
    return new GroupedJPAEntityCollection<>(
      this,
      _groups,
      _filters,
      Iterables.concat(_orderings, Collections.singleton(order))
    );
  }

  /**
   * @see OrderableCollection#getOrdering(int)
   */
  @Override
  public @NonNull Order getOrdering (@NonNegative @LessThan("this.getOrderingCount()") int index) {
    return _orderings.get(index);
  }

  /**
   * @see OrderableCollection#getOrderingCount()
   */
  @Override
  public @NonNegative int getOrderingCount () {
    return _orderings.size();
  }

  /**
   * @see OrderableCollection#getOrderings()
   */
  @Override
  public @NonNull List<@NonNull Order> getOrderings () {
    return Collections.unmodifiableList(_orderings);
  }

  /**
   * @see OrderableCollection#orderings()
   */
  @Override
  public @NonNull Iterable<@NonNull Order> orderings () {
    return Collections.unmodifiableList(_orderings);
  }
}
