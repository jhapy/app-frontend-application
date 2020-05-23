package org.jhapy.frontend.utils;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * A tuple of things.
 *
 * @param <S> Type of the first thing.
 * @param <T> Type of the second thing.
 * @author Tobias Trelle
 * @author Oliver Gierke
 * @author Christoph Strobl
 * @since 1.12
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class Pair<S, T> {

  private final @NonNull S first;
  private final @NonNull T second;

  /**
   * Creates a new {@link Pair} for the given elements.
   *
   * @param first must not be {@literal null}.
   * @param second must not be {@literal null}.
   */
  public static <S, T> Pair<S, T> of(S first, T second) {
    return new Pair<>(first, second);
  }

  /**
   * A collector to create a {@link Map} from a {@link Stream} of {@link Pair}s.
   */
  public static <S, T> Collector<Pair<S, T>, ?, Map<S, T>> toMap() {
    return Collectors.toMap(Pair::getFirst, Pair::getSecond);
  }

  /**
   * Returns the first element of the {@link Pair}.
   */
  public S getFirst() {
    return first;
  }

  /**
   * Returns the second element of the {@link Pair}.
   */
  public T getSecond() {
    return second;
  }
}