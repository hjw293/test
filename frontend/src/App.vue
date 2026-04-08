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
                    :class="{ active: activeTab === 'compare' }" 
                    @click="activeTab = 'compare'"
                    class="tab-item"
                  >
                    对比分析
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
                <div class="time-range-selector">
                  <span class="time-label">时间段：</span>
                  <el-time-picker
                    v-model="startTime"
                    placeholder="开始时间"
                    format="HH:mm:ss"
                    value-format="HH:mm:ss"
                    @change="handleTimeRangeChange"
                  />
                  <span class="time-separator">至</span>
                  <el-time-picker
                    v-model="endTime"
                    placeholder="结束时间"
                    format="HH:mm:ss"
                    value-format="HH:mm:ss"
                    @change="handleTimeRangeChange"
                  />
                  <el-button
                    v-if="startTime || endTime"
                    type="danger"
                    size="small"
                    @click="clearTimeRange"
                  >
                    清除筛选
                  </el-button>
                </div>
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

              <!-- 对比分析视图 -->
              <div v-else-if="activeTab === 'compare'" class="compare-container">
                <div v-if="compareDevices.length === 0" class="compare-empty">
                  <el-empty description="请在左侧设备导航中选择要对比的设备（支持多选）">
                    <template #image>
                      <div style="font-size: 64px; color: #ddd;">📊</div>
                    </template>
                  </el-empty>
                </div>
                <div v-else class="compare-content">
                  <!-- 设备列表 -->
                  <div class="compare-devices-header">
                    <div class="compare-devices-title">已选择设备</div>
                    <div class="compare-devices-list">
                      <div
                        v-for="(deviceInfo, index) in compareDevices"
                        :key="`${deviceInfo.device}-${deviceInfo.month}-${deviceInfo.date}`"
                        class="compare-device-tag"
                      >
                        <span class="device-tag-info">
                          <span class="device-tag-name">{{ deviceInfo.device }}</span>
                          <span class="device-tag-date">{{ getDeviceDateInfo(deviceInfo) }}</span>
                        </span>
                        <el-button
                          type="danger"
                          size="small"
                          :icon="Close"
                          circle
                          @click="removeCompareDevice(deviceInfo)"
                        />
                      </div>
                    </div>
                  </div>
                  <!-- 统一图表容器 -->
                  <div class="compare-chart-wrapper">
                    <div ref="compareChartRef" class="compare-chart"></div>
                  </div>
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
import { Close } from '@element-plus/icons-vue'

const API_URL = 'http://localhost:8080/api/data'

const chartRef = ref(null)
const loading = ref(false)
const errorMessage = ref('')
const rawData = ref({})
const selectedDevice = ref(null)
const selectedMonth = ref(null)
const selectedDate = ref(null)
const activeTab = ref('chart')
const currentPage = ref(1)
const pageSize = ref(10)
let chart = null

// 对比分析相关状态
const compareDevices = ref([])
const compareCharts = ref([])
const compareChartRef = ref(null)

// 时间范围选择
const startTime = ref('')
const endTime = ref('')

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
          id: `device-${month}-${date}-${device}`,
          label: device,
          device: device,
          month: month,
          date: date
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
    if (activeTab.value === 'compare') {
      // 对比分析模式：多选设备（包含月份和日期信息）
      const deviceInfo = {
        device: data.device,
        month: data.month,
        date: data.date
      }
      
      // 检查是否已经选中
      const existingIndex = compareDevices.value.findIndex(d => d.device === deviceInfo.device && d.month === deviceInfo.month && d.date === deviceInfo.date)
      
      if (existingIndex > -1) {
        // 已选中，取消选中
        compareDevices.value.splice(existingIndex, 1)
      } else {
        // 未选中，添加到选中列表
        compareDevices.value.push(deviceInfo)
      }
      // 更新对比分析图表
      updateCompareCharts()
    } else {
      // 普通模式：单选设备（包含月份和日期信息）
      selectedDevice.value = data.device
      selectedMonth.value = data.month
      selectedDate.value = data.date
    }
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

// 时间戳转换为 HH:mm:ss 格式，优先从 realTime 提取
const formatTime = (item) => {
  // 如果有 realTime 字段，直接提取时间部分
  if (item && item.realTime && item.realTime.length > 10) {
    return item.realTime.substring(11) // 提取时间部分 (例如: "17:09:18")
  }
  // 否则从 timestamp 转换
  if (item && item.timestamp) {
    const date = new Date(item.timestamp)
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    })
  }
  return ''
}

// 日期格式化为 YYYY-MM-DD HH:mm:ss 格式
const formatDate = (date) => {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
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

      // 首次加载时，自动选择第一个设备
      if (selectedDevice.value === null && rawData.value) {
        const devices = Object.keys(rawData.value).sort()
        if (devices.length > 0) {
          selectedDevice.value = devices[0]
        }
      }

      // 不自动设置时间范围，让用户看到全部数据
      // 清除之前的时间范围选择
      startTime.value = ''
      endTime.value = ''

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

  // 过滤设备数据（同时考虑设备、月份和日期）
  const filteredData = {}
  if (selectedDevice.value === null) {
    // 未选择设备，显示所有数据
    Object.assign(filteredData, rawData.value)
  } else {
    // 选择设备，过滤出该设备的数据
    const deviceData = rawData.value[selectedDevice.value]
    if (deviceData) {
      // 进一步按月份和日期过滤
      let filteredList = deviceData
      if (selectedMonth.value) {
        filteredList = filteredList.filter(item => item.month === selectedMonth.value)
      }
      if (selectedDate.value) {
        filteredList = filteredList.filter(item => item.date === selectedDate.value)
      }
      filteredData[selectedDevice.value] = filteredList
    }
  }

  // 时间范围过滤
  let filterStartTime = null
  let filterEndTime = null
  if (startTime.value || endTime.value) {
    // 将时间转换为秒数，便于比较
    if (startTime.value) {
      const [hours, minutes, seconds] = startTime.value.split(':').map(Number)
      filterStartTime = hours * 3600 + minutes * 60 + (seconds || 0)
    }

    if (endTime.value) {
      const [hours, minutes, seconds] = endTime.value.split(':').map(Number)
      filterEndTime = hours * 3600 + minutes * 60 + (seconds || 59)
    }
  }

  // 应用时间范围过滤
  const timeFilteredData = {}
  Object.entries(filteredData).forEach(([deviceName, data]) => {
    if (data) {
      const filteredList = data.filter(item => {
        if (!filterStartTime && !filterEndTime) return true

        // 优先从 realTime 提取时间部分，否则从 timestamp 提取
        let itemTimeSeconds = null
        if (item.realTime && item.realTime.length > 10) {
          const timePart = item.realTime.substring(11) // "17:09:18"
          const [hours, minutes, seconds] = timePart.split(':').map(Number)
          itemTimeSeconds = hours * 3600 + minutes * 60 + (seconds || 0)
        } else if (item.timestamp) {
          const itemDate = new Date(item.timestamp)
          itemTimeSeconds = itemDate.getHours() * 3600 + itemDate.getMinutes() * 60 + itemDate.getSeconds()
        }

        if (itemTimeSeconds === null) return true

        if (filterStartTime && filterEndTime) {
          return itemTimeSeconds >= filterStartTime && itemTimeSeconds <= filterEndTime
        } else if (filterStartTime) {
          return itemTimeSeconds >= filterStartTime
        } else if (filterEndTime) {
          return itemTimeSeconds <= filterEndTime
        }
        return true
      })
      if (filteredList.length > 0) {
        timeFilteredData[deviceName] = filteredList
      }
    }
  })

  // 收集所有时间戳并去重，同时创建 timestamp 到 realTime 的映射
  const timestampToRealTime = new Map()
  Object.entries(timeFilteredData).forEach(([deviceName, data]) => {
    if (data) {
      data.forEach(item => {
        allTimestamps.add(item.timestamp)
        // 如果该 timestamp 还没有对应的 realTime，则记录
        if (!timestampToRealTime.has(item.timestamp) && item.realTime) {
          timestampToRealTime.set(item.timestamp, item.realTime)
        }
      })
    }
  })

  // 排序时间戳
  const sortedTimestamps = Array.from(allTimestamps).sort((a, b) => a - b)
  const xAxisData = sortedTimestamps.map(timestamp => {
    const realTime = timestampToRealTime.get(timestamp)
    if (realTime && realTime.length > 10) {
      return realTime.substring(11) // 提取时间部分
    }
    // 否则从 timestamp 转换
    const date = new Date(timestamp)
    return date.toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      hour12: false
    })
  })

  // 为每个设备创建一条曲线
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#773C1A']
  let colorIndex = 0

  Object.entries(timeFilteredData).forEach(([deviceName, data]) => {
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
        smooth: 0.8,
        symbol: 'circle',
        symbolSize: 4,
        showSymbol: false,
        lineStyle: {
          width: 2.5,
          shadowColor: 'rgba(0, 0, 0, 0.1)',
          shadowBlur: 10
        },
        itemStyle: {
          borderRadius: [50, 50],
          borderWidth: 2,
          borderColor: '#fff'
        },
        areaStyle: {
          opacity: 0.15,
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 255, 255, 0.3)' },
            { offset: 1, color: 'rgba(255, 255, 255, 0.05)' }
          ])
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
    series: series,
    animation: true,
    animationDuration: 1000,
    animationEasing: 'cubicOut',
    animationEasingUpdate: 'quadraticOut',
    // 数据采样策略，当数据点过多时自动采样
    large: true,
    largeThreshold: 2000,
    // 启用渐进式渲染
    progressive: 200,
    progressiveThreshold: 1000
  }
  
  chart.setOption(option, true)
}

// 表格数据
const tableData = computed(() => {
  const data = []

  // 过滤设备数据（同时考虑设备、月份和日期）
  const filteredData = {}
  if (selectedDevice.value === null) {
    // 未选择设备，显示所有数据
    Object.assign(filteredData, rawData.value)
  } else {
    // 选择设备，过滤出该设备的数据
    const deviceData = rawData.value[selectedDevice.value]
    if (deviceData) {
      // 进一步按月份和日期过滤
      let filteredList = deviceData
      if (selectedMonth.value) {
        filteredList = filteredList.filter(item => item.month === selectedMonth.value)
      }
      if (selectedDate.value) {
        filteredList = filteredList.filter(item => item.date === selectedDate.value)
      }
      filteredData[selectedDevice.value] = filteredList
    }
  }

  Object.entries(filteredData).forEach(([deviceName, deviceData]) => {
    if (deviceData) {
      deviceData.forEach(item => {
        data.push({
          device: item.device,
          month: item.month,
          date: item.date,
          time: formatTime(item),
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

// 处理时间范围变化
const handleTimeRangeChange = (value) => {
  if (activeTab.value === 'chart') {
    updateChart()
  }
}

// 清除时间范围筛选
const clearTimeRange = () => {
  startTime.value = ''
  endTime.value = ''
  if (activeTab.value === 'chart') {
    updateChart()
  }
}

// 处理页码变化
const handleCurrentChange = (current) => {
  currentPage.value = current
}

// 对比分析：获取设备的日期信息
const getDeviceDateInfo = (deviceInfo) => {
  if (!rawData.value || !rawData.value[deviceInfo.device]) {
    return ''
  }

  const deviceData = rawData.value[deviceInfo.device]
  const dateSet = new Set()

  deviceData.forEach(item => {
    // 如果有指定月份和日期，只显示该日期
    if (deviceInfo.month && deviceInfo.date) {
      if (item.month === deviceInfo.month && item.date === deviceInfo.date) {
        dateSet.add(`${item.month}月${item.date}日`)
      }
    } else {
      // 没有指定，显示所有日期
      if (item.month && item.date) {
        dateSet.add(`${item.month}月${item.date}日`)
      }
    }
  })

  const dates = Array.from(dateSet).sort()
  if (dates.length === 0) {
    return ''
  } else if (dates.length <= 3) {
    return dates.join(', ')
  } else {
    return `${dates.slice(0, 3).join(', ')} 等${dates.length}天`
  }
}

// 对比分析：移除设备
const removeCompareDevice = (deviceInfo) => {
  const index = compareDevices.value.findIndex(d => d.device === deviceInfo.device && d.month === deviceInfo.month && d.date === deviceInfo.date)
  if (index > -1) {
    compareDevices.value.splice(index, 1)
    updateCompareCharts()
  }
}

// 对比分析：更新所有对比图表
const updateCompareCharts = () => {
  // 清理旧图表
  if (compareCharts.value.length > 0) {
    compareCharts.value.forEach(chart => {
      if (chart) {
        chart.dispose()
      }
    })
    compareCharts.value = []
  }

  // 等待DOM更新后创建图表
  setTimeout(() => {
    const ref = compareChartRef.value
    if (!ref || compareDevices.value.length === 0) {
      return
    }

    // 为每个设备准备数据
    const allDeviceSeriesData = []
    const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#773C1A', '#F7BA2A', '#8e44ad']

    compareDevices.value.forEach((deviceInfo, deviceIndex) => {
      const allDeviceData = rawData.value[deviceInfo.device]

      if (allDeviceData && allDeviceData.length > 0) {
        // 根据月份和日期过滤数据
        let deviceData = allDeviceData
        if (deviceInfo.month && deviceInfo.date) {
          deviceData = allDeviceData.filter(item =>
            item.month === deviceInfo.month && item.date === deviceInfo.date
          )
        }

        if (deviceData.length > 0) {
          // 准备数据
          const sortedData = deviceData.sort((a, b) => a.timestamp - b.timestamp)
          const timeLabels = sortedData.map(item => formatTime(item))
          const values = sortedData.map(item => item.value)

          allDeviceSeriesData.push({
            timeLabels: timeLabels,
            values: values,
            deviceInfo: deviceInfo,
            deviceIndex: deviceIndex
          })
        }
      }
    })

    if (allDeviceSeriesData.length === 0) {
      return
    }

    // 找出最长的数据作为X轴
    const maxDataSeries = allDeviceSeriesData.reduce((max, current) =>
      current.timeLabels.length > max.timeLabels.length ? current : max
    )

    // 创建图表
    const chart = echarts.init(ref)
    const series = allDeviceSeriesData.map((data, index) => ({
      name: `${data.deviceInfo.device} (${data.deviceInfo.month || '全部'}/${data.deviceInfo.date || '全部'})`,
      type: 'line',
      data: data.values,
      smooth: 0.4,
      symbol: 'circle',
      symbolSize: 4,
      showSymbol: false,
      lineStyle: {
        width: 2.5,
        shadowColor: 'rgba(0, 0, 0, 0.1)',
        shadowBlur: 10
      },
      itemStyle: {
        borderWidth: 2,
        borderColor: '#fff'
      },
      areaStyle: {
        opacity: 0.1,
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: colors[index % colors.length] },
          { offset: 1, color: echarts.color.modifyAlpha(colors[index % colors.length], 0.05) }
        ])
      },
      color: colors[index % colors.length]
    }))

    const option = {
      title: {
        text: '设备数据对比',
        left: 'center',
        textStyle: {
          fontSize: 18,
          fontWeight: 'bold'
        }
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
          crossStyle: {
            color: '#999'
          }
        }
      },
      legend: {
        data: series.map(s => s.name),
        top: '10%',
        type: 'scroll'
      },
      grid: {
        left: '10%',
        right: '10%',
        bottom: '10%',
        top: '20%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: maxDataSeries.timeLabels,
        boundaryGap: false,
        axisLine: {
          lineStyle: {
            color: '#ccc'
          }
        },
        axisLabel: {
          color: '#666',
          rotate: 45
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
      series: series,
      animation: true,
      animationDuration: 1000,
      animationEasing: 'cubicOut'
    }

    chart.setOption(option)

    // 监听窗口大小变化
    window.addEventListener('resize', () => {
      chart.resize()
    })

    compareCharts.value.push(chart)
  }, 100)
}
// 监听activeTab变化，切换到对比分析时更新图表
watch(activeTab, (newTab) => {
  if (newTab === 'compare') {
    // 延迟执行，确保DOM已渲染
    setTimeout(() => {
      updateCompareCharts()
    }, 100)
  } else if (newTab === 'chart') {
    // 当切换到图表视图时，延迟执行更新，确保DOM已经渲染
    setTimeout(() => {
      // 强制重新初始化图表
      chart = null
      updateChart()
    }, 100)
  }
})

// 监听设备选择变化
watch(selectedDevice, () => {
  // 切换设备时清除时间范围，显示全部数据
  startTime.value = ''
  endTime.value = ''
  
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

.time-range-selector {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding: 12px 16px;
  background: #f5f7fa;
  border-radius: 6px;
  border: 1px solid #ebeef5;
}

.time-label {
  font-size: 14px;
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}

.time-separator {
  font-size: 14px;
  color: #909399;
  margin: 0 8px;
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

.compare-container {
  width: 100%;
  min-height: 500px;
}

.compare-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 500px;
}

.compare-charts {
  display: flex;
  flex-direction: column;
  gap: 20px;
  width: 100%;
}

.compare-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
}

.compare-devices-header {
  background: white;
  border-radius: 8px;
  padding: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #ebeef5;
}

.compare-devices-title {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 12px;
}

.compare-devices-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.compare-device-tag {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.compare-device-tag:hover {
  background: #ecf5ff;
  border-color: #409eff;
}

.device-tag-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.device-tag-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.device-tag-date {
  font-size: 11px;
  color: #909399;
}

.compare-chart-wrapper {
  background: white;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid #ebeef5;
  min-height: 500px;
}

.compare-chart {
  width: 100%;
  height: 500px;
  min-height: 500px;
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
