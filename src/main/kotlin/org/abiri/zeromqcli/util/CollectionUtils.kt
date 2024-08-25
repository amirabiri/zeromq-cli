package org.abiri.zeromqcli.util

fun <T> List<T>.asMutable(): MutableList<T> = this as MutableList<T>
fun <K, V> Map<K, V>.asMutable(): MutableMap<K, V> = this as MutableMap<K, V>