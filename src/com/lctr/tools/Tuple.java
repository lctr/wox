package com.lctr.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class Tuple {
    public static class Pair<A, B> {
        private final A first;
        private final B second;

        Pair(A a, B b) {
            this.first = a;
            this.second = b;
        }

        public A fst() {
            return this.first;
        }

        public B snd() {
            return this.second;
        }

        public Pair<B, A> flip() {
            return new Pair<B, A>(this.second, this.first);
        }

        public <C> Pair<C, B> replace_fst(C c) {
            return new Pair<C, B>(c, this.second);
        }

        public <C> Pair<A, C> replace_snd(C c) {
            return new Pair<A, C>(this.first, c);
        }

        public String toString() {
            return "(" + this.first.toString() + ", " + this.second.toString() + ")";
        }

        public boolean nonNull() {
            return this.first != null && this.second != null;
        }

        public boolean equals(Pair<A, B> other) {
            return this.first == other.fst() && this.second == other.snd();
        }

        public static <K, V> Map<K, V> map_from_list(List<Pair<K, V>> pairs) {
            Map<K, V> map = new HashMap<>();
            for (Pair<K, V> pair : pairs) {
                map.put(pair.fst(), pair.snd());
            }
            return map;
        }

        public static <K, V> Map<K, V> map_from_list(Pair<K, V>[] pairs) {
            Map<K, V> map = new HashMap<>();
            for (Pair<K, V> pair : pairs) {
                map.put(pair.fst(), pair.snd());
            }
            return map;
        }

        /**
         * Returns a list of `(key, value)` pairs. Usefule for constructing a
         * `HashMap` out of an array of keys and values.
         * 
         * @apiNote Since the key and value lists are provided separately, the
         *          `(key, value)` pairs returned by this method will have the
         *          same length as the minimum of the `size`s of the two lists
         *          provided.
         * 
         * @param <K> key
         * @param <V> value
         * @param ks  List of keys
         * @param vs  List of values
         * @return A list of `(key, value)` pairs
         */
        public static <K, V> List<Pair<K, V>> zip(List<K> ks, List<V> vs) {
            int len = Math.min(ks.size(), vs.size());
            List<Pair<K, V>> pairs = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                K key = ks.get(i);
                V val = vs.get(i);
                pairs.add(new Pair<K, V>(key, val));
            }
            return pairs;
        }

        /**
         * Returns a list of `(key, value)` pairs. Useful for constructing a
         * `HashMap` out of an array of keys and values.
         * 
         * @apiNote Since the key and value lists are provided separately, the
         *          `(key, value)` pairs returned by this method will have the
         *          same length as the minimum of the `length`s of the two
         *          arrays provided.
         * 
         * @param <K> key
         * @param <V> value
         * @param ks  Array of keys
         * @param vs  Array of values
         * @return A list of `(key, value)` pairs
         */
        public static <K, V> List<Pair<K, V>> zip(K[] ks, V[] vs) {
            int len = Math.min(ks.length, vs.length);
            List<Pair<K, V>> pairs = new ArrayList<>();
            for (int i = 0; i < len; i++) {
                K key = ks[i];
                V val = vs[i];
                pairs.add(new Pair<K, V>(key, val));
            }
            return pairs;
        }

        public static <K, V> Map<K, V> pairs_map(List<K> ks, List<V> vs) {
            return map_from_list(zip(ks, vs));
        }
    }

    public static class Triple<A, B, C> {
        private final A first;
        private final B second;
        private final C third;

        Triple(A a, B b, C c) {
            this.first = a;
            this.second = b;
            this.third = c;
        }

        public A fst() {
            return this.first;
        }

        public B snd() {
            return this.second;
        }

        public C thrd() {
            return this.third;
        }

        public <D> Triple<D, B, C> replace_fst(D d) {
            return new Triple<D, B, C>(d, this.second, this.third);
        }

        public <D> Triple<A, D, C> replace_snd(D d) {
            return new Triple<A, D, C>(this.first, d, this.third);
        }

        public <D> Triple<A, B, D> replace_thrd(D d) {
            return new Triple<A, B, D>(this.first, this.second, d);
        }

        public Triple<B, C, A> rotate_left() {
            return new Triple<B, C, A>(this.second, this.third, this.first);
        }

        public Triple<C, A, B> rotate_right() {
            return new Triple<C, A, B>(this.third, this.first, this.second);
        }

        public Pair<A, Pair<B, C>> split_first() {
            A a = this.first;
            B b = this.second;
            C c = this.third;
            Pair<B, C> pair = new Pair<B, C>(b, c);
            return new Pair<A, Pair<B, C>>(a, pair);
        }

        public Pair<B, Pair<A, C>> split_second() {
            A a = this.first;
            B b = this.second;
            C c = this.third;
            Pair<A, C> pair = new Pair<A, C>(a, c);
            return new Pair<B, Pair<A, C>>(b, pair);
        }

        public Pair<C, Pair<A, B>> split_third() {
            return new Pair<C, Pair<A, B>>(this.third, new Pair<A, B>(this.first, this.second));
        }

        public String toString() {
            return "(" + this.first.toString() + ", " + this.second.toString() + ", " + this.third.toString() + ")";
        }

        public boolean equals(Triple<A, B, C> other) {
            return this.first == other.fst() && this.second == other.snd() && this.third == other.thrd();
        }
    }
}
