package com.example.data.repository

import com.example.data.local.dao.ChatDao
import com.example.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {
    val allMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()

    suspend fun insertMessage(message: ChatMessageEntity): Long = chatDao.insertMessage(message)

    suspend fun clearChat() = chatDao.clearChat()
}
