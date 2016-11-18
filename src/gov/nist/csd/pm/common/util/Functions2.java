/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.nist.csd.pm.common.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Helper class for working with Function2 objects
 *
 * Function2 is an interface defining a single method which takes two parameters and returns a value
 * It is an extension to The Function<T> interface in Google's Guava library.  This class includes functionality
 * that should make interoperating with the Guava library possible.
 * 
 * @author Administrator
 */
public class Functions2 {
	/**
	 * Conforms the Function2 object to a Function object by packing two
	 * parameters into a single Tuple2.
	 * 
	 * Useful for working with Guava's functional libs.
	 * 
	 * @param <T1>
	 * @param <T2>
	 * @param <RT>
	 * @param function2
	 *            a two parameter function
	 * @return a one parameter function that takes a Tuple with types T1 and T2
	 *         and returns RT
	 */
	public static <T1, T2, RT> Function<Tuple2<T1, T2>, RT> toFunction1(
			final Function2<T1, T2, RT> function2) {
		return new Function<Tuple2<T1, T2>, RT>() {
			@Override
			public RT apply(Tuple2<T1, T2> tupple) {
				return function2.apply(tupple.getFirst(), tupple.getSecond());
			}
		};
	}

	/**
	 * Curries a two parameter function replacing the left most parameter with
	 * the curried value.
	 * 
	 * @param <T1>
	 * @param <T2>
	 * @param <RT>
	 * @param function2
	 * @param curriedValue
	 * @return
	 */
	public static <T1, T2, RT> Function<T2, RT> curryLeft(
			final Function2<T1, T2, RT> function2, final T1 curriedValue) {
		return new Function<T2, RT>() {

			@Override
			public RT apply(T2 p2) {
				return function2.apply(curriedValue, p2);
			}

		};
	}

	/**
	 * Curries a two parameter function replacing the right most parameter with
	 * the curried value.
	 * 
	 * @param <T1>
	 * @param <T2>
	 * @param <RT>
	 * @param function2
	 * @param curriedValue
	 * @return
	 */
	public static <T1, T2, RT> Function<T1, RT> curryRight(
			final Function2<T1, T2, RT> function2, final T2 curriedValue) {
		return new Function<T1, RT>() {
			@Override
			public RT apply(T1 p1) {
				return function2.apply(p1, curriedValue);
			}
		};
	}

	/**
	 * Converts the passed in function to work on collections of equal typing.
	 * 
	 * This is helpful for composition and in implementing fluent interfaces.
	 * 
	 * @param <T1>
	 * @param <T2>
	 * @param functor
	 * @return
	 */
	public static <T1, T2> Function<Collection<T1>, Collection<T2>> map(
			final Function<T1, T2> functor) {
		return new Function<Collection<T1>, Collection<T2>>() {
			@Override
			public Collection<T2> apply(Collection<T1> f) {
				Collection<T2> results = Lists.newArrayList();
				for (T1 t : f) {
					results.add(functor.apply(t));
				}
				return results;
			}

		};
	}

	/**
	 * Function for getting a an object's class. Useful when combined with
	 * Functions2.overCollection
	 */
	public static Function<Object, Class<?>> getObjectClass() {
		return new Function<Object, Class<?>>() {

			@Override
			public Class<?> apply(Object f) {
				if (null != f) {
					return f.getClass();
				}
				return null;
			}
		};
	}

	public static <A, B> Function<A, B> pipe(final Function<A, B> first) {
		return new Function<A, B>() {
			@Override
			public B apply(@Nullable A a) {
				return first.apply(a);
			}
		};
	}

	public static <A, B, C> Function<A, C> pipe(final Function<A, B> first,
			final Function<? super B, C> second) {
		return new Function<A, C>() {
			@Override
			public C apply(@Nullable A a) {
				return second.apply(first.apply(a));
			}
		};
	}

	public static <A, B, C, D> Function<A, D> pipe(final Function<A, B> first,
			final Function<? super B, C> second,
			final Function<? super C, D> third) {
		return new Function<A, D>() {
			@Override
			public D apply(@Nullable A a) {
				return third.apply(second.apply(first.apply(a)));
			}
		};
	}

	public static <A, B, C, D, E> Function<A, E> pipe(
			final Function<A, B> first, final Function<? super B, C> second,
			final Function<? super C, D> third,
			final Function<? super D, E> fourth) {
		return new Function<A, E>() {
			@Override
			public E apply(@Nullable A a) {
				return fourth.apply(third.apply(second.apply(first.apply(a))));
			}
		};
	}

	public static <A, B, C, D, E, F> Function<A, F> pipe(
			final Function<A, B> first, final Function<? super B, C> second,
			final Function<? super C, D> third,
			final Function<? super D, E> fourth,
			final Function<? super E, F> fifth) {
		return new Function<A, F>() {
			@Override
			public F apply(@Nullable A a) {
				return fifth.apply(fourth.apply(third.apply(second.apply(first
						.apply(a)))));
			}
		};
	}

	public static <A, B, C, D, E, F, G> Function<A, G> pipe(
			final Function<A, B> first, final Function<? super B, C> second,
			final Function<? super C, D> third,
			final Function<? super D, E> fourth,
			final Function<? super E, F> fifth,
			final Function<? super F, G> sixth) {
		return new Function<A, G>() {
			@Override
			public G apply(@Nullable A a) {
				return sixth.apply(fifth.apply(fourth.apply(third.apply(second.apply(first
						.apply(a))))));
			}
		};
	}

	public static <S, T> Function<S, T> varpipe(final Function<S, T> first,
			final Function<T, T>... pipeline) {
		return new Function<S, T>() {

			@Override
			public T apply(@Nullable S s) {
				T result = first.apply(s);
				for (Function<T, T> function : pipeline) {
					result = function.apply(result);
				}
				return result;
			}
		};
	}

	private static class NullFunction<T, V> implements Function<T, V> {

		@Override
		public V apply(T t) {
			return null;
		}
	}

	public static <T, V> Function<T, V> nullFunction() {
		return new NullFunction<T, V>();
	}
}
