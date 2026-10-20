package com.example.samsungcontroll

/** What to do when a connection attempt ends in [ConnectionState.FAILED]. */
internal enum class FailureAction {
    /** Send Wake-on-LAN, then reconnect. */
    WAKE_AND_RECONNECT,

    /** Try again shortly (the TV is still waking up). */
    RETRY,

    /** Report the failure; do nothing else. */
    SURFACE
}

/**
 * Recovery (waking the TV, retrying) only makes sense for a connection that never opened.
 * Once it was established ([connectedOnce]), a later failure means the TV went away, usually
 * because someone turned it off, and waking it again would switch it right back on.
 */
internal fun failureAction(
    connectedOnce: Boolean,
    wakeOnFailure: Boolean,
    hasMacAddress: Boolean,
    retriesAfterWake: Int
): FailureAction = when {
    connectedOnce -> FailureAction.SURFACE
    wakeOnFailure && hasMacAddress -> FailureAction.WAKE_AND_RECONNECT
    retriesAfterWake > 0 -> FailureAction.RETRY
    else -> FailureAction.SURFACE
}
