import request from './request'

export function sendQuestion(documentId, question) {
  return request({
    url: '/chat/ask',
    method: 'post',
    params: { documentId, question }
  })
}

export function getChatHistory() {
  return request({
    url: '/chat/history',
    method: 'get'
  })
}

export function deleteChatHistory(id) {
  return request({
    url: `/chat/history/${id}`,
    method: 'delete'
  })
}

export function streamChat(documentId, question, onMessage, onDone, onError) {
  const token = localStorage.getItem('token')
  const eventSource = new EventSource(
    `/api/chat/stream?documentId=${documentId}&question=${encodeURIComponent(question)}&token=${encodeURIComponent(token || '')}`
  )

  eventSource.onmessage = (event) => {
    if (event.data === '[DONE]') {
      eventSource.close()
      if (onDone) onDone()
    } else {
      if (onMessage) onMessage(event.data)
    }
  }

  eventSource.onerror = (error) => {
    eventSource.close()
    if (onError) onError(error)
  }

  return eventSource
}
