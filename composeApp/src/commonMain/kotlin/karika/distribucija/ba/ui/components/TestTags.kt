package karika.distribucija.ba.ui.components

import karika.distribucija.ba.domain.model.ChatConversation

/** Test tag of a conversation row in a message list, for the end-to-end tests. */
fun conversationTag(conversation: ChatConversation) = "conversation_${conversation.conversationId}"
