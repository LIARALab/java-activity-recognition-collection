package org.liara.collection.operator.cursoring;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.Collection;

public interface CursorableCollection extends Collection
{
  @NonNull CursorableCollection setCursor (@NonNull final Cursor cursor);

  @NonNull Cursor getCursor ();
}
