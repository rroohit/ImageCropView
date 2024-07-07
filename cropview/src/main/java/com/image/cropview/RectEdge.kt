package com.image.cropview

/**
 *  - Enum class representing edges and sides of a rectangle within a crop view.
 *  - [NULL]: Represents no specific edge or side.
 *  - [TOP_LEFT], [TOP_RIGHT], [BOTTOM_LEFT], [BOTTOM_RIGHT]: Represent the four edges of a rectangle.
 */
public enum class RectEdge {
    NULL,
    // TOP, BOTTOM, LEFT, RIGHT, // Represent the four sides of a rectangle.
    TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT // rect edges
}
