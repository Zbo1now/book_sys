import { ref } from 'vue'
import { fetchUnreadCount } from '../api/messages'

const unreadCount = ref(0)

export function useMessageUnread() {
  async function refresh() {
    try {
      const result = await fetchUnreadCount()
      unreadCount.value = result.count
    } catch {
      // ignore — user might not be logged in
    }
  }

  function setCount(n: number) {
    unreadCount.value = n
  }

  return { unreadCount, refresh, setCount }
}
