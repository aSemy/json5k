package io.github.xn32.json5k

/** `@Ignore` tests on JS targets. */
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
expect annotation class IgnoreOnJs()


/** `@Ignore` tests on non-JS targets. */
@OptIn(ExperimentalMultiplatform::class)
@OptionalExpectation
expect annotation class IgnoreOnNonJs()
