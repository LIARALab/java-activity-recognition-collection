package org.liara.collection.operator;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.liara.collection.Collection;

/**
 * An identity operator.
 * 
 * An identity operator that always return is given view. The composition of an identity operator with another operator
 * is the another operator.
 *
 * @author C&eacute;dric DEMONGIVERT [cedric.demongivert@gmail.com](mailto:cedric.demongivert@gmail.com)
 */
public class Identity implements Operator
{
  @NonNull
  public static final Identity INSTANCE = new Identity();

  /**
   * @see Operator#apply(Collection)
   */
  @Override
  public @NonNull Collection apply (@NonNull final Collection output) {
    return output;
  }

  /**
   * @see Operator#apply(Operator)
   */
  @Override
  public @NonNull Operator apply (@NonNull final Operator child) {
    return child;
  }
}
