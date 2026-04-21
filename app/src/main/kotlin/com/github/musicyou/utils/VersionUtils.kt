package com.github.musicyou.utils

/**
 * Compares two semantic version strings (e.g. "1.2.3" or "v1.2.3").
 *
 * @return a positive integer if [v1] is newer, a negative integer if [v2] is newer, or 0 if equal.
 */
fun compareVersions(v1: String, v2: String): Int {
    val parts1 = v1.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
    val parts2 = v2.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
    for (i in 0 until maxOf(parts1.size, parts2.size)) {
        val p1 = parts1.getOrElse(i) { 0 }
        val p2 = parts2.getOrElse(i) { 0 }
        if (p1 != p2) return p1 - p2
    }
    return 0
}
