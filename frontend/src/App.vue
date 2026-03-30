<template>
  <div class="container">
    <el-header class="header">
      <h1>传感器数据实时展示系统</h1>
    </el-header>
    
    <el-main class="main-content">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="24" :md="6" :lg="5">
          <el-card class="tree-card" shadow="hover">
            <template #header>
              <span>设备导航</span>
            </template>
            <el-tree
              :data="treeData"
              node-key="id"
              default-expand-all
              @node-click="handleTreeClick"
              :render-content="renderTreeContent"
            />
          </el-card>
        </el-col>
        <el-col :xs="24" :sm="24" :md="18" :lg="19">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="title-tabs">
                  <span 
                    :class="{ active: activeTab === 'chart' }" 
                    @click="activeTab = 'chart'"
                    class="tab-item"
                  >
                    传感器数据曲线图
                  </span>
                  <span 
                    :class="{ active: activeTab === 'table' }" 
                    @click="activeTab = 'table'"
                    class="tab-item"
                  >
                    传感器数据表
                  </span>
                </div>
                <div class="header-controls">
                  <el-button type="primary" :loading="loading" @click="fetchData">
                    {{ loading ? '加载中...' : '刷新数据' }}
                  </el-button>
                </div>
              </div>
            </template>
            
            <div v-loading="loading">
              <!-- 图表视图 -->
              <div v-if="activeTab === 'chart'" class="chart-container">
                <div ref="chartRef" style="width: 100%; height: 500px;"></div>
              </div>
              
              <!-- 表格视图 -->
              <div v-else-if="activeTab === 'table'" class="table-container">
                <el-table :data="paginatedTableData" style="width: 100%">
                  <el-table-column prop="device" label="设备" width="120" />
                  <el-table-column prop="month" label="月份" width="100" />
                  <el-table-column prop="date" label="日期" width="100" />
                  <el-table-column prop="time" label="时间" width="120" />
                  <el-table-column prop="value" label="数值" width="100" />
                </el-table>
                <div style="margin-top: 20px; text-align: right;">
                  <el-pagination
                    v-model:current-page="currentPage"
                    v-model:page-size="pageSize"
                    :page-sizes="[10, 20, 50, 100]"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="tableData.length"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                  />
                </div>
              </div>
              
              <el-alert 
                v-if="errorMessage" 
                :title="errorMessage" 
                type="error" 
                :closable="true"
                style="margin-top: 16px;"
              />
            </div>
          </el-card>
          
          <!-- 数据统计卡片 -->
          <el-row :gutter="20" style="margin-top: 20px;">
            <el-col :xs="24" :sm="12" :md="6" v-for="(stat, index) in statistics" :key="index">
              <el-card class="stat-card">
                <div class="stat-content">
                  <div class="stat-label">{{ stat.label }}</div>
                  <div class="stat-value">{{ stat.value }}</div>
                </div>
              </el-card>
            </el-col>
          </el-row>
        </el-col>
      </el-row>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import * as echarts from 'echarts'
import axios from 'axios'

const API_URL = 'http://localhost:8080/api/data'

const chartRef = ref(null)
const loading = ref(false)
const errorMessage = ref('')
const rawData = ref({})
const selectedDevice = ref('all')
const activeTab = ref('chart')
const currentPage = ref(1)
const pageSize = ref(10)
let chart = null

// 树形数据结构
const treeData = computed(() => {
  const data = []
  const root = {
    id: 'root',
    label: 'localStorage',
    children: []
  }
  
  // 收集所有数据点
  const allData = Object.values(rawData.value).flat()
  
  // 按月份分组
  const months = new Set()
  allData.forEach(item => {
    if (item.month) months.add(item.month)
  })
  
  // 生成月份节点
  Array.from(months).sort().forEach(month => {
    const monthNode = {
      id: `month-${month}`,
      label: `月份 ${month}`,
      children: []
    }
    
    // 按日期分组
    const dates = new Set()
    allData.filter(item => item.month === month).forEach(item => {
      if (item.date) dates.add(item.date)
    })
    
    // 生成日期节点
    Array.from(dates).sort().forEach(date => {
      const dateNode = {
        id: `date-${month}-${date}`,
        label: `日期 ${date}`,
        children: []
      }
      
      // 按设备分组
      const devices = new Set()
      allData.filter(item => item.month === month && item.date === date).forEach(item => {
        if (item.device) devices.add(item.device)
      })
      
      // 生成设备节点
      Array.from(devices).sort().forEach(device => {
        dateNode.children.push({
          id: `device-${device}`,
          label: device,
          device: device
        })
      })
      
      monthNode.children.push(dateNode)
    })
    
    root.children.push(monthNode)
  })
  
  data.push(root)
  return data
})

// 处理树形节点点击
const handleTreeClick = (data) => {
  if (data.device) {
    selectedDevice.value = data.device
  }
}

// 渲染树形节点内容
const renderTreeContent = (h, { node, data }) => {
  return h('span', {
    style: {
      display: 'inline-block',
      width: '100%'
    }
  }, [
    h('span', {
      style: {
        marginRight: '8px'
      }
    }, data.label)
  ])
}

// 时间戳转换为 HH:mm:ss 格式
const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

// 获取后端数据
const fetchData = async () => {
  loading.value = true
  errorMessage.value = ''
  try {
    // 先刷新缓存（清除旧缓存并重新加载数据）
    try {
      await axios.post('http://localhost:8080/api/refresh-cache')
      console.log('缓存刷新成功')
    } catch (refreshError) {
      console.warn('刷新缓存失败，继续获取数据:', refreshError.message)
    }

    // 获取数据
    const response = await axios.get(API_URL, {
      headers: {
        'Content-Type': 'application/json'
      }
    })

    if (response.data.code === 200) {
      rawData.value = response.data.data || {}
      updateChart()
    } else {
      errorMessage.value = response.data.message || '获取数据失败'
    }
  } catch (error) {
    console.error('Error fetching data:', error)
    errorMessage.value = `请求失败: ${error.message || '无法连接到后端服务器'}`
  } finally {
    loading.value = false
  }
}

// 初始化并更新图表
const updateChart = () => {
  // 确保DOM已经渲染
  if (!chartRef.value) {
    // 如果chartRef为空，延迟重试
    setTimeout(() => {
      updateChart()
    }, 50)
    return
  }
  
  // 初始化 ECharts
  if (!chart) {
    chart = echarts.init(chartRef.value)
    window.addEventListener('resize', () => chart && chart.resize())
  } else {
    // 确保图表大小正确
    chart.resize()
  }
  
  // 准备数据
  const allTimestamps = new Set()
  const series = []
  
  // 过滤设备数据
  const filteredData = selectedDevice.value === 'all' 
    ? rawData.value 
    : { [selectedDevice.value]: rawData.value[selectedDevice.value] }
  
  // 收集所有时间戳并去重
  Object.entries(filteredData).forEach(([deviceName, data]) => {
    if (data) {
      data.forEach(item => {
        allTimestamps.add(item.timestamp)
      })
    }
  })
  
  // 排序时间戳
  const sortedTimestamps = Array.from(allTimestamps).sort((a, b) => a - b)
  const xAxisData = sortedTimestamps.map(formatTime)
  
  // 为每个设备创建一条曲线
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#773C1A']
  let colorIndex = 0
  
  Object.entries(filteredData).forEach(([deviceName, data]) => {
    if (data) {
      const dataMap = {}
      data.forEach(item => {
        dataMap[item.timestamp] = item.value
      })
      
      const seriesData = sortedTimestamps.map(timestamp => dataMap[timestamp] ?? null)
      
      series.push({
        name: deviceName,
        type: 'line',
        data: seriesData,
        smooth: true,
        lineStyle: {
          width: 2
        },
        itemStyle: {
          borderRadius: [50, 50],
          borderWidth: 2,
          borderColor: '#fff'
        },
        areaStyle: {
          opacity: 0.1
        },
        color: colors[colorIndex % colors.length]
      })
      colorIndex++
    }
  })
  
  const option = {
    title: {
      text: '',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(50, 50, 50, 0.8)',
      borderColor: '#333',
      textStyle: {
        color: '#fff'
      },
      axisPointer: {
        type: 'cross',
        lineStyle: {
          type: 'dashed'
        }
      }
    },
    grid: {
      left: '10%',
      right: '10%',
      bottom: '10%',
      top: '5%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xAxisData,
      boundaryGap: false,
      axisLine: {
        lineStyle: {
          color: '#ccc'
        }
      },
      axisLabel: {
        color: '#666'
      }
    },
    yAxis: {
      type: 'value',
      axisLine: {
        lineStyle: {
          color: '#ccc'
        }
      },
      axisLabel: {
        color: '#666'
      },
      splitLine: {
        lineStyle: {
          type: 'dashed',
          color: '#eee'
        }
      }
    },
    series: series
  }
  
  chart.setOption(option, true)
}

// 表格数据
const tableData = computed(() => {
  const data = []
  const filteredData = selectedDevice.value === 'all' 
    ? rawData.value 
    : { [selectedDevice.value]: rawData.value[selectedDevice.value] }
  
  Object.entries(filteredData).forEach(([deviceName, deviceData]) => {
    if (deviceData) {
      deviceData.forEach(item => {
        data.push({
          device: item.device,
          month: item.month,
          date: item.date,
          time: formatTime(item.timestamp),
          value: item.value
        })
      })
    }
  })
  
  // 按时间戳排序
  return data.sort((a, b) => new Date(a.time) - new Date(b.time))
})

// 分页后的数据
const paginatedTableData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return tableData.value.slice(start, end)
})

// 处理每页显示条数变化
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
}

// 处理页码变化
const handleCurrentChange = (current) => {
  currentPage.value = current
}

// 监听设备选择变化
watch(selectedDevice, () => {
  if (activeTab.value === 'chart') {
    updateChart()
  }
  // 重置页码
  currentPage.value = 1
})

// 监听标签切换
watch(activeTab, (newTab) => {
  if (newTab === 'chart') {
    // 当切换到图表视图时，延迟执行更新，确保DOM已经渲染
    setTimeout(() => {
      // 强制重新初始化图表
      chart = null
      updateChart()
    }, 100)
  }
})

// 计算统计信息
const statistics = computed(() => {
  const deviceCount = Object.keys(rawData.value).length
  const totalPoints = Object.values(rawData.value).reduce((sum, data) => sum + data.length, 0)
  const allValues = Object.values(rawData.value).flat().map(item => item.value)
  const avgValue = allValues.length > 0 ? (allValues.reduce((a, b) => a + b, 0) / allValues.length).toFixed(2) : 0

  return [
    { label: '设备数量', value: deviceCount },
    { label: '数据点总数', value: totalPoints },
    { label: '平均值', value: avgValue },
    { label: '最新更新', value: new Date().toLocaleTimeString('zh-CN') }
  ]
})

// 页面加载时获取数据
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.container {
  min-height: 100vh;
  background-color: #f5f7fa;
  display: flex;
  flex-direction: column;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.header h1 {
  margin: 0;
  font-size: 28px;
  font-weight: 600;
}

.main-content {
  flex: 1;
  padding: 30px;
}

.tree-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  border: 1px solid #ebeef5;
  height: 800px;
  overflow-y: auto;
}

.chart-card {
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.title-tabs {
  display: flex;
  align-items: center;
  gap: 20px;
}

.tab-item {
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 16px;
  font-weight: 500;
  color: #606266;
  transition: all 0.3s ease;
}

.tab-item:hover {
  color: #409EFF;
  background-color: #ecf5ff;
}

.tab-item.active {
  color: #409EFF;
  background-color: #ecf5ff;
  border-bottom: 2px solid #409EFF;
}

.header-controls {
  display: flex;
  align-items: center;
}

.chart-container {
  width: 100%;
  padding: 20px 0;
}

.table-container {
  width: 100%;
  padding: 20px 0;
  overflow-x: auto;
  min-width: 1000px;
}

.table-container :deep(.el-table) {
  border-radius: 8px;
  overflow: hidden;
  width: 100%;
  min-width: 1000px;
  margin: 0;
}

.table-container :deep(.el-table__header-wrapper),
.table-container :deep(.el-table__body-wrapper) {
  width: 100%;
}

.table-container :deep(.el-table__header) {
  width: 1000px !important;
}

.table-container :deep(.el-table__body) {
  width: 1000px !important;
}

.table-container :deep(.el-table th),
.table-container :deep(.el-table td) {
  text-align: center;
  padding: 12px;
}

.table-container :deep(.el-table th) {
  background-color: #f5f7fa;
  font-weight: 600;
  white-space: nowrap;
}

.table-container :deep(.el-table td) {
  white-space: nowrap;
}

.table-container :deep(.el-table tr:hover) {
  background-color: #f5f7fa;
}

.stat-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #ebeef5;
  text-align: center;
}

.stat-content {
  padding: 16px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-value {
  color: #303133;
  font-size: 24px;
  font-weight: 600;
}

:deep(.el-loading-overlay) {
  border-radius: 8px;
}

:deep(.el-tree-node__content) {
  height: 32px;
  line-height: 32px;
}

:deep(.el-tree-node.is-current > .el-tree-node__content) {
  background-color: #ecf5ff;
}

:deep(.el-tree-node.is-current > .el-tree-node__content:hover) {
  background-color: #ecf5ff;
}

:deep(.el-tree-node__content:hover) {
  background-color: #f5f7fa;
}

@media (max-width: 768px) {
  .header h1 {
    font-size: 20px;
  }
  
  .main-content {
    padding: 16px;
  }
  
  .stat-value {
    font-size: 18px;
  }
  
  .tree-card {
    height: 300px;
    margin-bottom: 20px;
  }
}
</style>
