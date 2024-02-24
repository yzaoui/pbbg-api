package com.bitwiserain.pbbg.app

/**
 * Returns a list containing all elements of the original list, with one element reordered and all elements in-between shifted to make room for it.
 *
 * @throws IllegalArgumentException when [fromIndex] or [toIndex] are not in [indices].
 */
fun <T> List<T>.reorder(fromIndex: Int, toIndex: Int): List<T> {
    if (fromIndex !in indices || toIndex !in indices) throw IllegalArgumentException()

    return if (fromIndex == toIndex) {
        toList()
    } else if (toIndex < fromIndex) {
        slice(0..<toIndex) +
                get(fromIndex) +
                slice(toIndex..<fromIndex) +
                slice((fromIndex + 1)..lastIndex)
    } else {
        slice (0..<fromIndex) +
                slice((fromIndex + 1)..toIndex) +
                get(fromIndex) +
                slice((toIndex + 1)..lastIndex)
    }
}
