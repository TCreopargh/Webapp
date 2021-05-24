package xyz.tcreopargh.amttd.common.data.action

import java.io.Serializable

/**
 * @author TCreopargh
 */
enum class ActionType : Serializable {
    COMMENT,
    STATUS_CHANGED,
    DEADLINE_CHANGED,
    TASK_COMPLETED,
    TASK_UNCOMPLETED,
    TASK_ADDED,
    TASK_EDITED,
    TASK_REMOVED;
}