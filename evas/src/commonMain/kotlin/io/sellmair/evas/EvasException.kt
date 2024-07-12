package io.sellmair.evas

public open class EvasException: Exception {
    public constructor(): super()
    public constructor(message: String): super(message)
    public constructor(message: String, cause: Throwable): super(message, cause)
    public constructor(cause: Throwable): super(cause)
}

public class MissingEventsException(message: String) : EvasException(message)

public class MissingStatesException(message: String) : EvasException(message)