package fr.silv.api;

/**
 * A simple discriminated-union result type carrying either a success value or an error code.
 *
 * @param <T> type of the success value
 * @param <E> type of the error
 */
public final class ApiResult<T, E> {
    private final T value;
    private final E error;
    private final boolean success;

    private ApiResult(T value, E error, boolean success) {
        this.value = value;
        this.error = error;
        this.success = success;
    }

    /**
     * Creates a successful result carrying the given value.
     *
     * @param value the success value
     * @param <T>   success type
     * @param <E>   error type
     * @return a successful {@code ApiResult}
     */
    @SuppressWarnings("null") // null is intentional for the absent error slot
    public static <T, E> ApiResult<T, E> ok(T value) {
        return new ApiResult<>(value, null, true);
    }

    /**
     * Creates a failed result carrying the given error.
     *
     * @param error the error value
     * @param <T>   success type
     * @param <E>   error type
     * @return a failed {@code ApiResult}
     */
    @SuppressWarnings("null") // null is intentional for the absent value slot
    public static <T, E> ApiResult<T, E> err(E error) {
        return new ApiResult<>(null, error, false);
    }

    /**
     * Returns whether this result represents a success.
     *
     * @return {@code true} when a success value is present
     */
    public boolean isOk() {
        return success;
    }

    /**
     * Returns the success value; {@code null} when this result is an error.
     *
     * @return success value or {@code null}
     */
    public T getValue() {
        return value;
    }

    /**
     * Returns the error value; {@code null} when this result is a success.
     *
     * @return error value or {@code null}
     */
    public E getError() {
        return error;
    }
}
