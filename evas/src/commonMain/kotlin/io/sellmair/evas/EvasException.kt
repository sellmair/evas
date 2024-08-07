package io.sellmair.evas

public open class EvasException internal constructor(message: String) : Exception(message)

public class MissingEventsException(message: String) : EvasException(message)

public class MissingStatesException(message: String) : EvasException(message)