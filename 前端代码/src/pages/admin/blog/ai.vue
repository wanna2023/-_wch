<template>
    <div class="chat-container">
        <!-- 聊天记录区域 -->
        <div class="chat-header">
            <h2>AI助手</h2>
            <div class="chat-actions">
                <el-button type="warning" size="small" @click="clearChat" :icon="Delete">清空对话</el-button>
                <el-button type="info" size="small" @click="exportChat" :icon="Download">导出记录</el-button>
            </div>
        </div>
        <div class="chat-messages" ref="messagesContainer">
            <div v-for="(message, index) in messages" :key="index" :class="['message', message.role]">
                <div class="message-content">
                    <div class="avatar">
                        <el-avatar :size="40" :src="message.role === 'user' ? $store.state.setting.avatar : 'https://api.dicebear.com/6.x/bottts/svg?seed=gpt'" />
                    </div>
                    <div class="text">
                        {{ message.content }}
                    </div>
                </div>
            </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input">
            <el-input
                v-model="inputMessage"
                type="textarea"
                :rows="3"
                placeholder="输入您的问题..."
                @keyup.enter.native.exact="sendMessage"
            />
            <el-button type="primary" @click="sendMessage" :loading="loading">
                发送
            </el-button>
        </div>
    </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from '@/axios'
import { Delete, Download } from '@element-plus/icons-vue'

const inputMessage = ref('')
const messages = ref([])
const loading = ref(false)
const messagesContainer = ref(null)

// 加载历史对话记录
const loadHistory = async () => {
    try {
        const res = await axios.get('/admin/chat/history')
        messages.value = res.data
        await nextTick()
        scrollToBottom()
    } catch (error) {
        ElMessage.error('加载历史记录失败')
    }
}

// 发送消息
const sendMessage = async () => {
    if (!inputMessage.value.trim() || loading.value) return

    const userMessage = {
        role: 'user',
        content: inputMessage.value
    }

    // 添加用户消息
    messages.value.push(userMessage)
    const currentMessage = inputMessage.value
    loading.value = true
    inputMessage.value = ''

    try {
        // 调用后端API获取AI回复
        const res = await axios.post('/auth/ai', {
            message: currentMessage
        },{
            timeout: 60000  // 设置为30秒
        })

        // 修改数据处理逻辑，正确解析answer字段
        console.log('AI回复数据:', res) // 添加日志查看返回数据结构
        let aiResponse = ''
        if (typeof res.answer === 'string') {
            aiResponse = res.answer
        } else if (res.data && typeof res.data.answer === 'string') {
            aiResponse = res.data.answer
        } else if (typeof res.message === 'string') {
            aiResponse = res.message
        } else {
            aiResponse = '抱歉，我暂时无法回答这个问题'
        }
        
        messages.value.push({
            role: 'assistant',
            content: aiResponse
        })
    } catch (error) {
        ElMessage.error('获取AI回复失败，请重试')
        // 移除用户消息
        messages.value.pop()
        inputMessage.value = currentMessage
    } finally {
        loading.value = false
        scrollToBottom()
    }
}

// 清空对话
const clearChat = async () => {
    try {
        await axios.post('/admin/chat/clear')
        messages.value = []
        ElMessage.success('对话已清空')
    } catch (error) {
        ElMessage.error('清空对话失败')
    }
}

// 导出对话记录
const exportChat = () => {
    const chatContent = messages.value.map(msg => `${msg.role}: ${msg.content}`).join('\n')
    const blob = new Blob([chatContent], { type: 'text/plain' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `chat-history-${new Date().toISOString().slice(0, 10)}.txt`
    a.click()
    URL.revokeObjectURL(url)
}

onMounted(() => {
    loadHistory()
})

// 滚动到底部
const scrollToBottom = async () => {
    await nextTick()
    if (messagesContainer.value) {
        messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
}
</script>

<style scoped>
.chat-container {
    height: calc(100vh - 180px);
    display: flex;
    flex-direction: column;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
    padding: 20px;
}

.chat-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-bottom: 20px;
    border-bottom: 1px solid #eee;
    margin-bottom: 20px;
}

.chat-header h2 {
    margin: 0;
    color: #409EFF;
}

.chat-actions {
    display: flex;
    gap: 10px;
}

.chat-messages {
    flex: 1;
    overflow-y: auto;
    padding: 20px;
    margin-bottom: 20px;
}

.message {
    margin-bottom: 20px;
}

.message-content {
    display: flex;
    align-items: flex-start;
    gap: 12px;
}

.message.user .message-content {
    flex-direction: row-reverse;
}

.text {
    background-color: #f4f4f5;
    padding: 12px 16px;
    border-radius: 8px;
    max-width: 80%;
    word-break: break-word;
}

.message.assistant .text {
    background-color: #e6f4ff;
}

.chat-input {
    display: flex;
    gap: 12px;
    align-items: flex-start;
}

.chat-input .el-input {
    flex: 1;
}

.chat-input .el-button {
    margin-top: 8px;
}
</style>