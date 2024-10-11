package io.sellmair.evas.compose.gradle

import java.util.*

internal val String.capitalized
    get() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }