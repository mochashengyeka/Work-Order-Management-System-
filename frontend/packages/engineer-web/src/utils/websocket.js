class WebSocketClient {
  constructor() {
    this.ws = null
    this.userId = null
    this.reconnectTimer = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 5
    this.reconnectInterval = 5000
    this.handlers = {}
    this.statusHandlers = []
  }

  connect(userId) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) return
    this.userId = userId

    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    const url = `${protocol}//${host}/ws-notify/${userId}`

    this.ws = new WebSocket(url)

    this.ws.onopen = () => {
      this.reconnectAttempts = 0
      this.notifyStatus(true)
    }

    this.ws.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.type && this.handlers[data.type]) {
          this.handlers[data.type].forEach(fn => fn(data))
        }
        Object.keys(this.handlers).forEach(key => {
          if (key === '*') {
            this.handlers[key].forEach(fn => fn(data))
          }
        })
      } catch {}
    }

    this.ws.onclose = () => {
      this.notifyStatus(false)
      this.tryReconnect()
    }

    this.ws.onerror = () => {
      this.notifyStatus(false)
    }
  }

  on(type, handler) {
    if (!this.handlers[type]) this.handlers[type] = []
    this.handlers[type].push(handler)
  }

  off(type, handler) {
    if (!this.handlers[type]) return
    this.handlers[type] = this.handlers[type].filter(fn => fn !== handler)
  }

  onStatusChange(handler) {
    this.statusHandlers.push(handler)
  }

  notifyStatus(connected) {
    this.statusHandlers.forEach(fn => fn(connected))
  }

  tryReconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) return
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer)
    this.reconnectTimer = setTimeout(() => {
      this.reconnectAttempts++
      if (this.userId) this.connect(this.userId)
    }, this.reconnectInterval)
  }

  disconnect() {
    if (this.reconnectTimer) clearTimeout(this.reconnectTimer)
    if (this.ws) {
      this.ws.onclose = null
      this.ws.close()
      this.ws = null
    }
  }
}

export const wsClient = new WebSocketClient()
