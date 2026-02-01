package xyz.memothelemo.albasset.util

@Suppress("unused")
class AssertionException : RuntimeException {
    constructor() : super(MESSAGE)
    constructor(cause: Exception?) : super(MESSAGE, cause)

    companion object {
        private const val MESSAGE = "Please report this error at https://github.com/memothelemo/albasset/issues"
    }
}