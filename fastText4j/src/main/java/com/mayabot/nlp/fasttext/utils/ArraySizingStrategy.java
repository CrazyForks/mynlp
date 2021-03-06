package com.mayabot.nlp.fasttext.utils;

public interface ArraySizingStrategy {
  /**
   * @param currentBufferLength
   *          Current size of the array (buffer). This number should comply with
   *          the strategy's policies (it is a result of initial rounding or
   *          further growCalls). It can also be zero, indicating the growth
   *          from an empty buffer.
   *
   * @param elementsCount
   *          Number of elements stored in the buffer.
   * 
   * @param expectedAdditions
   *          Expected number of additions (resize hint).
   * 
   * @return Must return a new size at least as big as to hold
   *         <code>elementsCount + expectedAdditions</code>.
   * 
   */
  int grow(int currentBufferLength, int elementsCount, int expectedAdditions) throws RuntimeException;
}