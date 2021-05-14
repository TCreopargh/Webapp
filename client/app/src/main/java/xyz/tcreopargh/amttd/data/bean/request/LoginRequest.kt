package xyz.tcreopargh.amttd.data.bean.request

import xyz.tcreopargh.amttd.data.bean.request.IRequestBody
import java.util.*

data class LoginRequest(
    var email: String? = null,
    var password: String? = null,
    var token: String? = null,
    var uuid: UUID? = null
) : IRequestBody
