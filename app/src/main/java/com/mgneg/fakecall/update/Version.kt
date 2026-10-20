package com.mgneg.fakecall.update

/** Numeric version like 1.0, 1.2, or 1.1.1 (an optional leading "v" is ok). */
data class Version(val major: Int, val minor: Int, val patch: Int = 0) : Comparable<Version> {

    override fun compareTo(other: Version): Int =
        compareValuesBy(this, other, { it.major }, { it.minor }, { it.patch })

    override fun toString(): String =
        if (patch == 0) "$major.$minor" else "$major.$minor.$patch"

    companion object {
        fun parse(raw: String): Version? {
            val s = raw.trim().removePrefix("v").removePrefix("V")
            if (s.isEmpty()) return null
            val parts = s.split(".")
            if (parts.size !in 2..3) return null
            val nums = parts.map { it.toIntOrNull() ?: return null }
            if (nums.any { it < 0 }) return null
            return Version(nums[0], nums[1], nums.getOrElse(2) { 0 })
        }
    }
}
