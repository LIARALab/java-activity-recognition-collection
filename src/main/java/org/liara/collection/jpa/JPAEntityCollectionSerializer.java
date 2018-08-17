package org.liara.collection.jpa;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.IOException;

public class JPAEntityCollectionSerializer
       extends JsonSerializer<JPAEntityCollection>
{
  @Override
  public void serialize (
    @NonNull final JPAEntityCollection value,
    @NonNull final JsonGenerator generator,
    @NonNull final SerializerProvider serializers
  )
  throws IOException
  { generator.writeObject(value.find()); }
}
