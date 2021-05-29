package xyz.tcreopargh.amttd.common.bean.request

import java.util.*

data class ShareWorkGroupRequest(
    var userId: UUID? = null,
    var groupId: UUID? = null,
    var expirationTimeInDays: Int? = null
) : IRequestBody