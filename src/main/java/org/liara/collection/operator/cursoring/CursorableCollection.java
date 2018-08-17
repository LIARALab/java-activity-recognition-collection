package org.liara.collection.operator.cursoring;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.Collection;

public interface CursorableCollection extends Collection
{
  CursorableCollection setCursor (@NonNull final Cursor cursor);

  Cursor getCursor ();
}
