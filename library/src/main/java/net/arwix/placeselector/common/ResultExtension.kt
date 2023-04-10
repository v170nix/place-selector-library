package net.arwix.placeselector.common

inline fun <reified T : Throwable, R> Result<R>.except(): Result<R> {
    return onFailure { if (it is T) throw it }
}