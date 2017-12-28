package flat.math;

/**
 * Provides read-only access to a {@link Vector}.
 */
public interface IVector extends XY {

    /** Computes and returns the dot product of this and the specified other vector. */
    float dot (IVector other);

    /** Computes the cross product of this and the specified other vector.
     * @return a new vector containing the result. */
    Vector cross (IVector other);

    /** Computes the cross product of this and the specified other vector, placing the result in
     * the object supplied.
     * @return a reference to the result, for chaining. */
    Vector cross (IVector other, Vector result);

    /** Negates this vector.
     * @return a new vector containing the result. */
    Vector negate ();

    /** Negates this vector, storing the result in the supplied object.
     * @return a reference to the result, for chaining. */
    Vector negate (Vector result);

    /** Normalizes this vector.
     * @return a new vector containing the result. */
    Vector normalize ();

    /** Normalizes this vector, storing the result in the object supplied.
     * @return a reference to the result, for chaining. */
    Vector normalize (Vector result);

    /** Returns the length (magnitude) of this vector. */
    float length ();

    /** Returns the squared length of this vector. */
    float lengthSq ();

    /** Returns true if this vector has zero magnitude. */
    boolean isZero ();

    /** Returns the distance from this vector to the specified other vector. */
    float distance (IVector other);

    /** Returns the squared distance from this vector to the specified other. */
    float distanceSq (IVector other);

    /** Returns the angle of this vector. */
    float angle ();

    /** Returns the angle between this vector and the specified other vector. */
    float angleBetween (IVector other);

    /** Scales this vector uniformly by the specified magnitude.
     * @return a new vector containing the result. */
    Vector scale (float v);

    /** Scales this vector uniformly by the specified magnitude, and places the result in the
     * supplied object.
     * @return a reference to the result, for chaining. */
    Vector scale (float v, Vector result);

    /** Scales this vector's x and y components independently by the x and y components of the
     * supplied vector.
     * @return a new vector containing the result. */
    Vector scale (IVector other);

    /** Scales this vector's x and y components independently by the x and y components of the
     * supplied vector, and stores the result in the object provided.
     * @return a reference to the result vector, for chaining. */
    Vector scale (IVector other, Vector result);

    /** Adds a vector to this one.
     * @return a new vector containing the result. */
    Vector add (IVector other);

    /** Adds a vector to this one, storing the result in the object provided.
     * @return a reference to the result, for chaining. */
    Vector add (IVector other, Vector result);

    /** Adds a vector to this one.
     * @return a new vector containing the result. */
    Vector add (float x, float y);

    /** Adds a vector to this one and stores the result in the object provided.
     * @return a reference to the result, for chaining. */
    Vector add (float x, float y, Vector result);

    /** Adds a scaled vector to this one.
     * @return a new vector containing the result. */
    Vector addScaled (IVector other, float v);

    /** Adds a scaled vector to this one and stores the result in the supplied vector.
     * @return a reference to the result, for chaining. */
    Vector addScaled (IVector other, float v, Vector result);

    /** Subtracts a vector from this one.
     * @return a new vector containing the result. */
    Vector subtract (IVector other);

    /** Subtracts a vector from this one and places the result in the supplied object.
     * @return a reference to the result, for chaining. */
    Vector subtract (IVector other, Vector result);

    /** Subtracts a vector from this one.
     * @return a new vector containing the result. */
    Vector subtract (float x, float y);

    /** Subtracts a vector from this one and places the result in the supplied object.
     * @return a reference to the result, for chaining. */
    Vector subtract (float x, float y, Vector result);

    /** Rotates this vector by the specified angle.
     * @return a new vector containing the result. */
    Vector rotate (float angle);

    /** Rotates this vector by the specified angle, storing the result in the vector provided.
     * @return a reference to the result vector, for chaining. */
    Vector rotate (float angle, Vector result);

    /** Rotates this vector by the specified angle and adds another vector to it, placing the
     * result in the object provided.
     * @return a reference to the result, for chaining. */
    Vector rotateAndAdd (float angle, IVector add, Vector result);

    /** Rotates this vector by the specified angle, applies a uniform scale, and adds another
     * vector to it, placing the result in the object provided.
     * @return a reference to the result, for chaining. */
    Vector rotateScaleAndAdd (float angle, float scale, IVector add, Vector result);

    /** Linearly interpolates between this and the specified other vector by the supplied amount.
     * @return a new vector containing the result. */
    Vector lerp (IVector other, float t);

    /** Linearly interpolates between this and the supplied other vector by the supplied amount,
     * storing the result in the supplied object.
     * @return a reference to the result, for chaining. */
    Vector lerp (IVector other, float t, Vector result);

    /** Returns a mutable copy of this vector. */
    Vector clone ();
}
