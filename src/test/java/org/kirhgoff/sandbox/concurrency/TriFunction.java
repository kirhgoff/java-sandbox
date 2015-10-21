package org.kirhgoff.sandbox.concurrency;

/**
 * Created by Kirill Lastovirya (kirill.lastovirya@gmail.com) aka kirhgoff on 21/10/15.
 */
public interface TriFunction<T, U, R> {
  R apply(T t, U u1, U u2);

}
