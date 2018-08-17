package org.liara.collection.operator.grouping;

import org.checkerframework.checker.index.qual.LessThan;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.Collection;

import java.util.List;

public interface GroupableCollection extends Collection
{
  @NonNull GroupableCollection groupBy (@NonNull final Group group);

  @NonNull Group getGroup (@NonNegative @LessThan("this.getGroupCount()") final int index);

  @NonNegative int getGroupCount ();

  @NonNull List<@NonNull Group> getGroups ();

  @NonNull Iterable<@NonNull Group> groups ();

  default boolean isGrouped () {
    return getGroupCount() > 0;
  }
}
