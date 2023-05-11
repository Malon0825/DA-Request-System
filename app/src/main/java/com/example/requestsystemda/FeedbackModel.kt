package com.example.requestsystemda

data class FeedbackModel (
    var client_id: String? = null,
    var detail_req: String? = null,
    var quantity: String? = null,
    var message: String? = null,
    var granted: Boolean? = null,
)