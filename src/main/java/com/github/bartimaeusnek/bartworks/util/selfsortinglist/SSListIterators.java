/*
 * Copyright (c) 2018-2019 bartimaeusnek
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.bartimaeusnek.bartworks.util.selfsortinglist;

import org.apache.commons.lang3.NotImplementedException;

import java.util.Iterator;
import java.util.ListIterator;

public class SSListIterators {


    public static class SSListListIterator<E> implements ListIterator<E> {
        final SSListNode<E> head;
        final SSListNode<E> tail;
        SSListNode<E> current;
        int counter = 0;
        public SSListListIterator(SSListNode<E> head,SSListNode<E> tail) {
            this.head = head;
            this.tail = tail;
            current = null;
        }

        @Override
        public boolean hasNext() {
            return head != current;
        }

        @Override
        public E next() {
            counter++;
            E ret = current.getELEMENT();
            current = current.getNext();
            return ret;
        }

        @Override
        public boolean hasPrevious() {
            return tail != current;
        }

        @Override
        public E previous() {
            counter--;
            E ret = current.getELEMENT();
            current = current.getBefore();
            return ret;
        }

        @Override
        public int nextIndex() {
            return counter+1;
        }

        @Override
        public int previousIndex() {
            return counter-1;
        }

        @Override
        public void remove() {
            throw new NotImplementedException("Not Implemented");
        }

        @Override
        public void set(E e) {
            throw new NotImplementedException("Not Implemented");
        }

        @Override
        public void add(E e) {
            throw new NotImplementedException("Not Implemented");
        }
    }

    public static class SSListIterator<E> implements Iterator<E> {
        final SSListNode<E> head;
        SSListNode<E> current;
        public SSListIterator(SSListNode<E> head) {
            this.head = head;
            current = null;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public E next() {
            E ret = current.getELEMENT();
            current = current.getNext();
            return ret;
        }
    }

    public static class SSListReverseIterator<E> implements Iterator<E> {
        final SSListNode<E> tail;
        SSListNode<E> current;

        public SSListReverseIterator(SSListNode<E> head) {
            this.tail = head;
            current = null;
        }

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public E next() {
            E ret = current.getELEMENT();
            current = current.getBefore();
            return ret;
        }
    }

}
