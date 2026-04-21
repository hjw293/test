<template>
  <div class="container">
    <el-header class="header">
      <h1>曲线展示</h1>
      <div class="header-right">
        <el-button type="default" @click="goBack" class="back-btn">
          <el-icon><ArrowLeft /></el-icon>
          返回
        </el-button>
        <el-dropdown @command="handleLogout" trigger="click">
          <span class="user-info">
            <el-icon><User /></el-icon>
            <span>{{ currentUser?.username || '用户' }}</span>
            <el-icon class="el-icon--right"><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-main class="main-content">
      <el-row :gutter="20">
        <!-- 左侧：曲线组列表 -->
        <el-col :xs="24" :sm="24" :md="6" :lg="5">
          <el-card class="group-list-card" shadow="hover">
            <template #header>
              <span>曲线组导航</span>
            </template>
            <div v-loading="groupLoading" class="group-list">
              <div
                v-for="gid in groupIdOptions"
                :key="gid"
                class="group-item"
                :class="{ active: selectedGroupId === gid }"
                @click="selectGroup(gid)"
              >
                <div class="group-item-main">
                  <span class="group-item-label">组 {{ gid }}</span>
                  <el-tag size="small" type="info">
                    {{ groupCurveCounts[gid] || 0 }} 条曲线
                  </el-tag>
                </div>
              </div>
              <el-empty v-if="groupIdOptions.length === 0 && !groupLoading" description="暂无曲线组" :image-size="60" />
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：图表区域 -->
        <el-col :xs="24" :sm="24" :md="18" :lg="19">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="header-title">
                  {{ selectedGroupId ? `曲线组 ${selectedGroupId} - 曲线图` : '请选择左侧曲线组' }}
                </div>
                <div class="header-controls" v-if="selectedGroupId">
                  <el-button type="primary" :loading="chartLoading" @click="loadGroupCurves(selectedGroupId)">
                    刷新
                  </el-button>
                </div>
              </div>
            </template>

            <div v-loading="chartLoading" class="chart-area">
              <!-- ECharts 图表 -->
              <div v-if="selectedGroupId && currentCurves.length > 0" class="chart-wrapper">
                <div ref="chartRef" class="echarts-container"></div>
              </div>
              <el-empty v-else-if="selectedGroupId && !chartLoading" description="该曲线组暂无数据" />
              <el-empty v-else-if="!selectedGroupId && !chartLoading" description="请在左侧选择一个曲线组查看曲线">
                <template #image>
                  <div style="font-size: 64px; color: #ddd;">📈</div>
                </template>
              </el-empty>
            </div>
          </el-card>

          <!-- 下方：曲线参数表格 -->
          <el-card v-if="currentCurves.length > 0" class="detail-card" shadow="hover" style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <div class="header-title">曲线参数详情</div>
                <div class="group-meta">
                  <span class="meta-item">
                    背景色:
                    <span class="color-dot" :style="{ backgroundColor: groupMeta.bgColor || '#fff' }"></span>
                    {{ groupMeta.bgColor }}
                  </span>
                  <span class="meta-item">
                    时间线色:
                    <span class="color-dot" :style="{ backgroundColor: groupMeta.timeDivColor || '#000' }"></span>
                    {{ groupMeta.timeDivColor }}
                  </span>
                  <span class="meta-item">
                    启用:
                    <el-tag :type="groupMeta.enableNid === 0 ? 'success' : 'warning'" size="small">
                      {{ groupMeta.enableNid === 0 ? '永久启动' : '条件启动' }}
                    </el-tag>
                  </span>
                </div>
              </div>
            </template>
            <el-table :data="currentCurves" style="width: 100%" stripe>
              <el-table-column label="颜色" width="70" align="center">
                <template #default="{ row }">
                  <span class="color-dot table-dot" :style="{ backgroundColor: row.curveColor || '#ccc' }"></span>
                </template>
              </el-table-column>
              <el-table-column prop="curveNameId" label="曲线名称ID" width="130" />
              <el-table-column label="曲线类型" width="100">
                <template #default="{ row }">
                  <el-tag :type="getTypeTagType(row.curveType)" size="small">{{ getTypeText(row.curveType) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="曲线模式" width="110">
                <template #default="{ row }">
                  <el-tag :type="getModeTagType(row.curveMode)" size="small">{{ getModeText(row.curveMode) }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column label="最小值" width="90">
                <template #default="{ row }">{{ row.curveMin ?? '-' }}</template>
              </el-table-column>
              <el-table-column label="最大值" width="90">
                <template #default="{ row }">{{ row.curveMax ?? '-' }}</template>
              </el-table-column>
              <el-table-column label="引用" width="80" prop="curveRef" />
              <el-table-column label="预估" width="70" align="center">
                <template #default="{ row }">
                  <el-tag v-if="row.curvePredict === 1" type="danger" size="small">是</el-tag>
                  <span v-else class="text-muted">否</span>
                </template>
              </el-table-column>
              <el-table-column label="启用NID" width="100" prop="curveEnableNid" />
              <el-table-column label="最大值NID" width="110" prop="curveMaxNid" />
              <el-table-column label="最小值NID" width="110" prop="curveMinNid" />
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </el-main>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import axios from 'axios'
import { ArrowLeft, User, ArrowDown } from '@element-plus/icons-vue'

const router = useRouter()

const currentUser = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : null
})

const GROUP_IDS_API = 'http://localhost:8080/api/curve-group/group-ids'
const BY_GROUP_API = 'http://localhost:8080/api/curve-group/by-group'
const ALL_GROUPED_API = 'http://localhost:8080/api/curve-group/all-grouped'
const ALL_DATA_API = 'http://localhost:8080/api/data'

// 状态
const groupLoading = ref(false)
const chartLoading = ref(false)
const errorMessage = ref('')
const groupIdOptions = ref([])
const groupCurveCounts = ref({})
const selectedGroupId = ref(null)
const currentCurves = ref([])
const allSensorData = ref({})  // 所有传感器数据
const availableDevices = ref([])  // 可用设备列表

// 图表
const chartRef = ref(null)
let chart = null

// 组元信息
const groupMeta = computed(() => {
  if (currentCurves.value.length === 0) return {}
  const first = currentCurves.value[0]
  return {
    bgColor: first.groupBgColor,
    timeDivColor: first.groupTimeDivColor,
    enableNid: first.groupEnableNid,
    enableValue: first.groupEnableValue,
    nameType: first.groupNameType
  }
})

// 获取 token header
const getAuthHeaders = () => {
  const token = localStorage.getItem('token')
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  }
}

// 加载组列表和曲线数量
const fetchGroupData = async () => {
  groupLoading.value = true
  try {
    const [idsRes, allRes] = await Promise.all([
      axios.get(GROUP_IDS_API, { headers: getAuthHeaders() }),
      axios.get(ALL_GROUPED_API, { headers: getAuthHeaders() })
    ])

    if (idsRes.data.code === 200) {
      groupIdOptions.value = idsRes.data.data || []
    }

    if (allRes.data.code === 200) {
      const grouped = allRes.data.data || {}
      const counts = {}
      for (const [gid, curves] of Object.entries(grouped)) {
        counts[gid] = curves.length
      }
      groupCurveCounts.value = counts
    }

    // 自动选中第一个
    if (groupIdOptions.value.length > 0 && !selectedGroupId.value) {
      selectGroup(groupIdOptions.value[0])
    }
  } catch (error) {
    handleApiError(error)
  } finally {
    groupLoading.value = false
  }
}

// 加载指定组的曲线
const loadGroupCurves = async (groupNameId) => {
  chartLoading.value = true
  try {
    const response = await axios.get(BY_GROUP_API, {
      params: { groupNameId },
      headers: getAuthHeaders()
    })

    if (response.data.code === 200) {
      currentCurves.value = response.data.data || []
      await nextTick()

      // 如果传感器数据还没加载，先加载传感器数据
      if (availableDevices.value.length === 0) {
        await fetchAllSensorData()
      }

      renderChart()
    }
  } catch (error) {
    handleApiError(error)
    currentCurves.value = []
  } finally {
    chartLoading.value = false
  }
}

// 选择组
const selectGroup = (gid) => {
  selectedGroupId.value = gid
  loadGroupCurves(gid)
}

// 加载所有传感器数据
const fetchAllSensorData = async () => {
  try {
    const response = await axios.get(ALL_DATA_API, { headers: getAuthHeaders() })
    if (response.data.code === 200) {
      allSensorData.value = response.data.data || {}
      // 获取所有设备名称列表
      availableDevices.value = Object.keys(allSensorData.value)
      console.log('已加载传感器数据，设备列表:', availableDevices.value)
      console.log('传感器数据详情:', JSON.stringify(allSensorData.value, null, 2))
    } else {
      console.error('获取传感器数据失败:', response.data.message)
    }
  } catch (error) {
    console.error('获取传感器数据失败:', error)
  }
}

// 格式化时间显示
const formatTime = (realTime) => {
  if (!realTime) return ''
  // realTime 格式: "2026-03-17 17:09:18"，提取时间部分
  if (realTime.length > 10) {
    return realTime.substring(11) // "17:09:18"
  }
  return realTime
}

// 基于配置获取曲线数据（当前从 sensor_data 表获取，没有数据则返回空）
const getRealCurveData = (curve) => {
  // 如果没有可用数据，返回空数组（不显示曲线）
  if (availableDevices.value.length === 0) {
    console.log(`曲线 ${curve.curveNameId}: 没有传感器数据`)
    return []
  }

  let deviceName = ''

  // 如果有 curveRef，尝试匹配设备
  if (curve.curveRef) {
    deviceName = curve.curveRef.toString()
    let deviceData = allSensorData.value[deviceName]

    // 如果设备不存在，尝试模糊匹配
    if (!deviceData) {
      const matchedDevice = availableDevices.value.find(d =>
        d.includes(deviceName) || deviceName.includes(d)
      )
      if (matchedDevice) {
        deviceName = matchedDevice
        deviceData = allSensorData.value[deviceName]
      }
    }

    if (deviceData && deviceData.length > 0) {
      // 按时间戳排序并提取数值
      const sortedData = [...deviceData].sort((a, b) => a.timestamp - b.timestamp)
      console.log(`曲线 ${curve.curveNameId} 使用设备 ${deviceName}, 数据点: ${sortedData.length}`)
      return sortedData.map(item => item.value)
    }
  }

  // 没有 curveRef 或匹配失败，不显示
  console.log(`曲线 ${curve.curveNameId}: 没有关联数据`)
  return []
}

// 生成X轴时间标签（基于真实数据的时间范围）
const generateXAxisFromData = (dataList) => {
  if (!dataList || dataList.length === 0) {
    return []
  }

  // 找到所有数据中最长的时间序列
  const maxLength = Math.max(...dataList.map(d => d.length))

  if (maxLength === 0) return []

  // 找到第一个有数据的设备来生成时间标签
  const firstDeviceWithData = dataList.find(d => d.length > 0)
  if (!firstDeviceWithData) return []

  // 按时间戳排序
  const sortedData = [...firstDeviceWithData].sort((a, b) => a.timestamp - b.timestamp)

  // 生成时间标签
  return sortedData.map(item => formatTime(item.realTime))
}

// 备用：生成模拟曲线数据（当没有真实数据时使用）
const generateCurveData = (curve, index, pointCount) => {
  const min = Number(curve.curveMin) || 0
  const max = Number(curve.curveMax) || 1500
  const range = max - min || 100
  const mid = (max + min) / 2
  const amplitude = range / 2

  const data = []
  // 使用不同的种子给每条曲线不同的形态
  const seed = index * 1.7 + 0.3
  const freq1 = 0.8 + index * 0.3
  const freq2 = 2.1 + index * 0.5
  const phase = index * Math.PI / 3

  for (let i = 0; i < pointCount; i++) {
    const t = i / (pointCount - 1)
    // 组合多个正弦波产生自然的曲线形态
    const wave1 = Math.sin(t * Math.PI * freq1 * 2 + phase)
    const wave2 = Math.sin(t * Math.PI * freq2 * 2 + phase + 1) * 0.3
    const wave3 = Math.sin(t * Math.PI * 0.5 + seed) * 0.2
    const noise = (Math.sin(t * 37.7 + seed * 13.1) * 0.05)

    let value = mid + amplitude * 0.7 * (wave1 + wave2 + wave3 + noise)
    // 限制在范围内
    value = Math.max(min, Math.min(max, value))
    data.push(Math.round(value * 100) / 100)
  }
  return data
}

// 渲染 ECharts
const renderChart = () => {
  if (!chartRef.value || currentCurves.value.length === 0) return

  if (!chart) {
    chart = echarts.init(chartRef.value)
  } else {
    chart.resize()
  }

  const curves = currentCurves.value
  const bgColor = groupMeta.value.bgColor || '#ffffff'
  const timeDivColor = groupMeta.value.timeDivColor || '#cccccc'

  // 获取每条曲线的真实数据
  const curvesDataList = curves.map((curve) => getRealCurveData(curve))

  // 生成X轴时间标签（基于真实数据）
  const xData = generateXAxisFromData(curvesDataList)

  // 如果没有真实数据，回退到模拟数据
  const useRealData = xData.length > 0

  if (!useRealData) {
    console.warn('没有真实数据可用，使用模拟数据')
  }

  // 判断是否有副轴曲线
  const hasSecondaryAxis = curves.some(c => c.curveType === 2)

  // 构建 series
  const series = curves.map((curve, index) => {
    // 使用真实数据或生成模拟数据
    let data
    if (useRealData) {
      data = curvesDataList[index]
    } else {
      // 模拟数据（备用）
      data = generateCurveData(curve, index, xData.length || 120)
    }

    const isPredict = curve.curvePredict === 1
    const isSecondary = curve.curveType === 2

    return {
      name: `曲线 ${curve.curveNameId}`,
      type: 'line',
      yAxisIndex: isSecondary ? 1 : 0,
      data: data,
      smooth: 0.4,
      symbol: 'none',
      lineStyle: {
        width: isPredict ? 2 : 2.5,
        type: isPredict ? 'dashed' : 'solid',
        color: curve.curveColor || undefined
      },
      itemStyle: {
        color: curve.curveColor || undefined
      },
      areaStyle: isPredict ? null : {
        opacity: 0.05,
        color: curve.curveColor || undefined
      }
    }
  })

  // 计算左右轴的范围（基于真实数据或配置）
  const leftCurves = curves.filter(c => c.curveType !== 2)
  const rightCurves = curves.filter(c => c.curveType === 2)

  const getAxisRange = (curvesArr, dataList) => {
    // 如果有真实数据，基于真实数据计算范围
    if (dataList && dataList.length > 0) {
      const validData = dataList.filter(d => d && d.length > 0)
      if (validData.length > 0) {
        const allValues = validData.flat()
        if (allValues.length > 0) {
          const dataMin = Math.min(...allValues)
          const dataMax = Math.max(...allValues)
          const padding = (dataMax - dataMin) * 0.1
          return {
            min: Math.floor(dataMin - padding),
            max: Math.ceil(dataMax + padding)
          }
        }
      }
    }

    // 否则使用配置中的范围
    if (curvesArr.length === 0) return { min: 0, max: 100 }
    let allMin = Infinity, allMax = -Infinity
    curvesArr.forEach(c => {
      const cmin = Number(c.curveMin) || 0
      const cmax = Number(c.curveMax) || 1500
      if (cmin < allMin) allMin = cmin
      if (cmax > allMax) allMax = cmax
    })
    // 自动模式或范围为0时给默认范围
    if (allMin === Infinity) allMin = 0
    if (allMax === -Infinity) allMax = 100
    if (allMin === allMax) { allMin -= 50; allMax += 50 }
    const padding = (allMax - allMin) * 0.1
    return { min: Math.floor(allMin - padding), max: Math.ceil(allMax + padding) }
  }

  // 获取每个轴对应的数据列表
  const leftDataList = leftCurves.map((_, i) => curvesDataList[i])
  const rightDataList = rightCurves.map((_, i) => curvesDataList[leftCurves.length + i])

  const leftRange = getAxisRange(leftCurves, leftDataList)
  const rightRange = getAxisRange(rightCurves, rightDataList)

  // 构建Y轴
  const yAxis = [
    {
      type: 'value',
      name: '主轴',
      min: leftRange.min,
      max: leftRange.max,
      axisLine: { lineStyle: { color: '#666' } },
      axisLabel: { color: '#666' },
      splitLine: { lineStyle: { type: 'dashed', color: timeDivColor + '40' } }
    }
  ]

  if (hasSecondaryAxis) {
    yAxis.push({
      type: 'value',
      name: '副轴',
      min: rightRange.min,
      max: rightRange.max,
      axisLine: { lineStyle: { color: '#999' } },
      axisLabel: { color: '#999' },
      splitLine: { show: false }
    })
  }

  const option = {
    backgroundColor: bgColor,
    tooltip: {
      trigger: 'axis',
      backgroundColor: 'rgba(50, 50, 50, 0.85)',
      borderColor: '#555',
      textStyle: { color: '#fff', fontSize: 13 },
      axisPointer: { type: 'cross', lineStyle: { type: 'dashed' } }
    },
    legend: {
      data: series.map(s => s.name),
      top: 10,
      textStyle: {
        color: bgColor === '#ffffff' || bgColor === '#fff' ? '#333' : '#666'
      }
    },
    grid: {
      left: '8%',
      right: hasSecondaryAxis ? '8%' : '5%',
      bottom: '12%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xData,
      boundaryGap: false,
      axisLine: { lineStyle: { color: timeDivColor || '#ccc' } },
      axisLabel: { color: '#666', rotate: 0, interval: Math.floor(pointCount / 8) },
      splitLine: { show: true, lineStyle: { color: timeDivColor + '20', type: 'dashed' } }
    },
    yAxis: yAxis,
    series: series,
    animation: true,
    animationDuration: 800,
    animationEasing: 'cubicOut'
  }

  chart.setOption(option, true)
}

// 窗口resize处理
const handleResize = () => {
  if (chart) chart.resize()
}

// API 错误处理
const handleApiError = (error) => {
  if (error.response && error.response.status === 401) {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  } else {
    errorMessage.value = `请求失败: ${error.message}`
  }
}

// 类型/模式文本
const getTypeText = (type) => ({ 0: '标准', 1: '主轴', 2: '副轴' }[type] ?? '未知')
const getTypeTagType = (type) => ({ 0: 'info', 1: 'primary', 2: 'warning' }[type] || 'info')
const getModeText = (mode) => ({ 0: '固定值', 1: '节点值', 2: '混合(min固定)', 3: '混合(max固定)', 4: '自动' }[mode] ?? '未知')
const getModeTagType = (mode) => ({ 0: 'info', 1: 'primary', 2: 'warning', 3: 'warning', 4: 'success' }[mode] || 'info')

const goBack = () => { router.push('/') }
const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}

// 监听 selectedGroupId 变化，确保图表在 DOM 就绪后渲染
watch(currentCurves, async () => {
  await nextTick()
  if (currentCurves.value.length > 0 && chartRef.value) {
    // 给 DOM 一点时间完成渲染
    setTimeout(renderChart, 50)
  }
})

onMounted(() => {
  fetchGroupData()
  fetchAllSensorData()  // 获取传感器数据
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (chart) {
    chart.dispose()
    chart = null
  }
})
</script>

<style scoped>
.container {
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.header {
  background: transparent;
  border-radius: 16px;
  margin-bottom: 20px;
  padding: 20px 40px;
  border: none;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header h1 {
  color: #fff;
  font-size: 28px;
  font-weight: 600;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #fff;
  cursor: pointer;
  padding: 8px 16px;
  border-radius: 8px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.1);
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.2);
}

.back-btn {
  background: rgba(255, 255, 255, 0.9);
  border: none;
  color: #667eea;
  font-weight: 500;
}

.back-btn:hover {
  background: #fff;
  color: #764ba2;
}

.main-content {
  padding: 0;
}

/* 左侧组列表 */
.group-list-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  border: none;
  height: calc(100vh - 140px);
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.group-list-card :deep(.el-card__header) {
  background: linear-gradient(135deg, #5568d3 0%, #3e4a8d 100%);
  color: #fff;
  font-weight: 600;
  padding: 15px 20px;
  border-radius: 16px 16px 0 0;
}

.group-list-card :deep(.el-card__body) {
  flex: 1;
  overflow: hidden;
  padding: 0;
}

.group-list {
  height: 100%;
  overflow-y: auto;
  padding: 12px;
}

.group-item {
  padding: 14px 16px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.25s ease;
  margin-bottom: 8px;
  background: #f8f9fc;
  border: 2px solid transparent;
}

.group-item:hover {
  background: rgba(102, 126, 234, 0.08);
  border-color: rgba(102, 126, 234, 0.2);
}

.group-item.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.12) 0%, rgba(118, 75, 162, 0.08) 100%);
  border-color: #667eea;
}

.group-item-main {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.group-item-label {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

.group-item.active .group-item-label {
  color: #667eea;
}

/* 右侧图表区 */
.chart-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  border: none;
}

.chart-card :deep(.el-card__header) {
  background: rgba(255, 255, 255, 0.98);
  padding: 20px;
  border-bottom: 2px solid #f0f0f0;
  border-radius: 16px 16px 0 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.chart-area {
  min-height: 480px;
}

.chart-wrapper {
  padding: 16px;
}

.echarts-container {
  width: 100%;
  height: 480px;
  border-radius: 12px;
  overflow: hidden;
}

/* 详情卡片 */
.detail-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  border: none;
}

.detail-card :deep(.el-card__header) {
  background: rgba(255, 255, 255, 0.98);
  padding: 16px 20px;
  border-bottom: 2px solid #f0f0f0;
  border-radius: 16px 16px 0 0;
}

.group-meta {
  display: flex;
  align-items: center;
  gap: 20px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #606266;
}

.color-dot {
  display: inline-block;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: 2px solid #e0e0e0;
  flex-shrink: 0;
}

.table-dot {
  width: 18px;
  height: 18px;
}

.text-muted {
  color: #c0c4cc;
  font-size: 13px;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  background: #f5f7fa !important;
  color: #606266;
  font-weight: 600;
}

@media (max-width: 768px) {
  .header {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .header h1 {
    text-align: center;
  }

  .card-header {
    flex-direction: column;
    gap: 12px;
  }

  .group-meta {
    flex-wrap: wrap;
  }
}
</style>
