package com.numad.firesidechat

/**
 * Creating some Kotlin data objects to help with the database.
 * */
data class MessageHistoryObject(
    val notificationTracker: NotificationTracker,
    val messagesSent: ArrayList<Message>
) {
    constructor() : this(NotificationTracker(true, 0), ArrayList<Message>())

    fun incrementNotificationCount() {
        this.notificationTracker.count++
    }

    fun markNotificationAsRead() {
        this.notificationTracker.isRead = true
        resetNotificationCount()
    }

    private fun resetNotificationCount() {
        this.notificationTracker.count = 0
    }

    fun resetNotificationReadStatus() {
        this.notificationTracker.isRead = false
    }

    fun addMessage(message: Message) {
        this.messagesSent.add(message)
    }

    fun findAndRemoveMessage(message: Message) {
        this.messagesSent.remove(message)
    }
}

data class NotificationTracker(
    var isRead: Boolean,
    var count: Int
) {
    constructor() : this(true, 0)
}

data class Message(
    val sender: String,
    val message: String,
    val timestamp: Long
) {
    constructor() : this("", "", 0)
}
