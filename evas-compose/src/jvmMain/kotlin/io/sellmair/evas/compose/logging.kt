package io.sellmair.evas.compose

import org.slf4j.LoggerFactory
import java.lang.invoke.MethodHandles

@Suppress("NOTHING_TO_INLINE") // We want the caller class!
@JvmName("createLookupLogger")
internal inline fun createLogger() = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

internal inline fun <reified T : Any> createLogger() = LoggerFactory.getLogger(T::class.java)