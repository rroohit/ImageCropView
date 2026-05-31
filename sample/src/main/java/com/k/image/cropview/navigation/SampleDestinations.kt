package com.k.image.cropview.navigation

/** Navigation routes for non-example destinations. */
object SampleRoutes {
    const val HOME = "home"
}

/**
 * Registry of the example screens listed on the Home screen.
 *
 * To add a new example:
 *  1. Add an entry here (route, title, description).
 *  2. Add a matching `composable(<route>) { ... }` in [SampleNavHost].
 */
enum class SampleExample(
    val route: String,
    val title: String,
    val description: String,
) {
    EXAMPLE_ONE(
        route = "example_one",
        title = "Example One",
        description = "Basic usage (portrait only) — all crop types, pinch/double-tap zoom, reset, and save to gallery.",
    ),
    EXAMPLE_TWO(
        route = "example_two",
        title = "Example Two",
        description = "Adaptive layout supporting both portrait and landscape (rotation-safe).",
    ),
}