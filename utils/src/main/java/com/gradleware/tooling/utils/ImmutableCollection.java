package com.gradleware.tooling.utils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method's return type as being an immutable collection of values (list, set, queue, bag, map, etc.). This annotation is useful for public APIs that return a collection
 * that is of a type defined in the standard JDK and want to express the immutability of the returned collection. By default, the standard JDK does not provide a way to express the
 * immutability of a returned collection. Note that the immutability of a collection does not imply that each element in the collection is immutable as well.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ImmutableCollection {

}
