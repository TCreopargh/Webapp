package xyz.tcreopargh.amttd.common.bean.response

data class ShareWorkGroupResponse(
    override var success: Boolean? = false,
    var invitationCode: String? = null,
    override var error: Int?
) : IResponseBody
