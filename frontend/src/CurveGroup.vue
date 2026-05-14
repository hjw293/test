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
              <!-- 四级树结构：月份 -> 日期 -> 组别 -> 曲线 -->
              <el-collapse v-model="expandedMonths" class="group-collapse" @change="handleMonthExpand">
                <el-collapse-item
                  v-for="month in monthOptions"
                  :key="month"
                  :name="month"
                >
                  <template #title>
                    <div class="month-header" :class="{ active: selectedMonth === month }">
                      <el-icon><Calendar /></el-icon>
                      <span class="month-label">{{ month }}</span>
                      <el-tag size="small" type="primary">
                        {{ getGroupCountByMonth(month) }} 组
                      </el-tag>
                    </div>
                  </template>
                  <!-- 日期列表 -->
                  <el-collapse v-model="expandedDates" class="group-collapse inner-collapse" @change="handleDateExpand">
                    <el-collapse-item
                      v-for="date in getDatesByMonth(month)"
                      :key="date"
                      :name="date"
                    >
                      <template #title>
                        <div class="date-header" :class="{ active: selectedDate === date }">
                          <span class="date-label">{{ date }}</span>
                          <el-tag size="small" type="success">
                            {{ getGroupCountByDate(month, date) }} 组
                          </el-tag>
                        </div>
                      </template>
                      <!-- 组别列表 -->
                      <el-collapse v-model="expandedGroups" class="group-collapse inner-collapse">
                        <el-collapse-item
                          v-for="gid in getGroupsByDate(month, date)"
                          :key="gid"
                          :name="gid"
                        >
                          <template #title>
                            <div class="group-header" :class="{ active: selectedGroupId === gid && !selectedCurveNameId }" @click.stop="expandAndSelectGroup(gid)">
                              <span class="group-item-label">组 {{ gid }}</span>
                              <el-tag size="small" type="info">
                                {{ getCurveCountByGroup(gid) }} 条
                              </el-tag>
                              <el-icon class="expand-icon" :class="{ expanded: selectedGroupId === gid }">
                                <ArrowRight />
                              </el-icon>
                            </div>
                          </template>
                          <div v-if="selectedGroupId === gid" class="curve-list">
                            <div
                              v-for="curve in getCurvesByGroup(gid)"
                              :key="curve.curveNameId"
                              class="curve-item"
                              :class="{ active: selectedCurveNameId === curve.curveNameId }"
                              @click.stop="selectCurve(curve)"
                            >
                              <span class="color-dot small" :style="{ backgroundColor: curve.curveColor || '#ccc' }"></span>
                              <span class="curve-name-id" :title="curve.curveNameId">{{ getCurveDisplayName(curve.curveNameId) }}</span>
                            </div>
                          </div>
                        </el-collapse-item>
                      </el-collapse>
                    </el-collapse-item>
                  </el-collapse>
                </el-collapse-item>
              </el-collapse>
              <el-empty v-if="monthOptions.length === 0 && !groupLoading" description="暂无月份数据" :image-size="60" />
            </div>
          </el-card>
        </el-col>

        <!-- 右侧：图表区域 -->
        <el-col :xs="24" :sm="24" :md="18" :lg="19">
          <el-card class="chart-card" shadow="hover">
            <template #header>
              <div class="card-header">
                <div class="title-tabs">
                  <span
                    :class="{ active: activeTab === 'chart' }"
                    @click="switchTab('chart')"
                    class="tab-item"
                  >
                    曲线展示
                  </span>
                  <span
                    :class="{ active: activeTab === 'compare' }"
                    @click="switchTab('compare')"
                    class="tab-item"
                  >
                    对比分析
                  </span>
                </div>
                <div class="header-controls" v-if="selectedGroupId">
                  <el-button type="primary" :loading="chartLoading" @click="refreshCurves">
                    {{ chartLoading ? '加载中...' : '刷新' }}
                  </el-button>
                </div>
              </div>
            </template>

            <div v-loading="chartLoading" class="chart-area">
              <!-- 曲线展示视图 -->
              <div v-if="activeTab === 'chart'">
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

              <!-- 对比分析视图 -->
              <div v-else-if="activeTab === 'compare'" class="compare-container">
                <div v-if="compareCurves.length === 0" class="compare-empty">
                  <el-empty description="请在左侧选择要对比的曲线（支持多选）">
                    <template #image>
                      <div style="font-size: 64px; color: #ddd;">📊</div>
                    </template>
                  </el-empty>
                </div>
                <div v-else class="compare-content">
                  <!-- 已选曲线列表 -->
                  <div class="compare-devices-header">
                    <div class="compare-devices-title">已选择曲线 ({{ compareCurves.length }})</div>
                    <div class="compare-devices-list">
                      <div
                        v-for="curve in compareCurves"
                        :key="curve.curveNameId"
                        class="compare-device-tag"
                      >
                        <span class="device-tag-info">
                          <span class="device-tag-name" :style="{ color: curve.curveColor }">
                            <span class="color-dot small" :style="{ backgroundColor: curve.curveColor }"></span>
                            {{ getCurveDisplayName(curve.curveNameId) }}
                          </span>
                          <span class="device-tag-date">ID: {{ curve.curveNameId }}</span>
                        </span>
                        <el-button
                          type="danger"
                          size="small"
                          :icon="Close"
                          circle
                          @click="removeFromCompare(curve.curveNameId)"
                        />
                      </div>
                    </div>
                  </div>
                  <!-- 对比图表 -->
                  <div ref="compareChartRef" class="compare-chart"></div>
                </div>
              </div>
            </div>
          </el-card>

          <!-- 下方：曲线参数表格 -->
          <el-card v-if="currentCurves.length > 0" class="detail-card" shadow="hover" style="margin-top: 20px;">
            <template #header>
              <div class="card-header">
                <div class="header-title">{{ selectedCurveNameId ? `曲线参数详情 - ${selectedCurveNameId}` : `曲线参数详情 (${currentCurves.length}条)` }}</div>
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
              <el-table-column label="曲线名称" width="180">
                <template #default="{ row }">
                  <span>{{ getCurveDisplayName(row.curveNameId) }}</span>
                  <br>
                  <span class="text-muted" style="font-size: 11px;">ID: {{ row.curveNameId }}</span>
                </template>
              </el-table-column>
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
              <el-table-column label="组最小值" width="100">
                <template #default="{ row }">{{ curveStats.min ?? '-' }}</template>
              </el-table-column>
              <el-table-column label="组最大值" width="100">
                <template #default="{ row }">{{ curveStats.max ?? '-' }}</template>
              </el-table-column>
              <el-table-column label="组平均值" width="100">
                <template #default="{ row }">{{ curveStats.avg ?? '-' }}</template>
              </el-table-column>
              <el-table-column label="数据点数" width="90" align="center">
                <template #default="{ row }">{{ getCurveDataCount(row.curveNameId) }}</template>
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
import { ArrowLeft, User, ArrowDown, Calendar, ArrowRight } from '@element-plus/icons-vue'

const router = useRouter()

const currentUser = computed(() => {
  const userStr = localStorage.getItem('user')
  return userStr ? JSON.parse(userStr) : null
})

const GROUP_IDS_API = 'http://localhost:8080/api/curve-group/group-ids'
const BY_GROUP_API = 'http://localhost:8080/api/curve-group/by-group'
const ALL_GROUPED_API = 'http://localhost:8080/api/curve-group/all-grouped'
const CURVE_DATA_API = 'http://localhost:8080/api/curve-data/by-name-ids'
const MONTHS_API = 'http://localhost:8080/api/curve-data/months'
const DATES_API = 'http://localhost:8080/api/curve-data/dates'
const TEXT_NAMES_API = 'http://localhost:8080/api/text-source/names'

// 状态
const groupLoading = ref(false)
const chartLoading = ref(false)
const errorMessage = ref('')
const selectedGroupId = ref(null)
const selectedCurveNameId = ref(null)
const selectedCurve = ref(null)
const currentCurves = ref([])
const curveDataMap = ref({})  // 曲线数据 Map<nameId, CurveData[]>
const expandedGroups = ref([])  // 展开的组
const expandedMonths = ref([])  // 展开的月份
const expandedDates = ref([])  // 展开的日期
const groupedCurves = ref({})  // 按组分组的曲线 Map<groupId, Curve[]>
const selectedMonth = ref(null)  // 选中的月份
const selectedDate = ref(null)  // 选中的日期
const monthOptions = ref([])  // 月份选项
const dateOptions = ref({})  // 按月份存储的日期选项 Map<month, dates[]>
// 按月份分组的曲线数据 Map<month, Map<groupId, Curve[]>>
const monthGroupedCurves = ref({})
// 曲线名称映射 Map<nameId, 中文名>
const nameMap = ref({})
// 曲线对比列表
const compareCurves = ref([])
// 当前视图标签：chart | compare
const activeTab = ref('chart')

// 图表
const chartRef = ref(null)
const compareChartRef = ref(null)
let chart = null
let compareChart = null

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

// 曲线组统计数据（基于所有曲线数据计算）
const curveStats = computed(() => {
  const values = []
  // 遍历所有曲线的数据
  for (const [nameId, dataList] of Object.entries(curveDataMap.value)) {
    if (dataList && dataList.length > 0) {
      for (const item of dataList) {
        if (item.value != null) {
          values.push(item.value)
        }
      }
    }
  }
  if (values.length === 0) {
    return { min: null, max: null, avg: null }
  }
  const min = Math.min(...values)
  const max = Math.max(...values)
  const avg = values.reduce((a, b) => a + b, 0) / values.length
  return {
    min: Math.round(min * 100) / 100,
    max: Math.round(max * 100) / 100,
    avg: Math.round(avg * 100) / 100
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

// 加载月份列表
const fetchMonths = async () => {
  try {
    const response = await axios.get(MONTHS_API, { headers: getAuthHeaders() })
    console.log('月份API响应:', response.data)
    if (response.data.code === 200) {
      monthOptions.value = response.data.data || []
      console.log('月份列表:', monthOptions.value)
      // 默认展开第一个月份，但不要加载数据（图表区域保持空白）
      if (monthOptions.value.length > 0) {
        const firstMonth = monthOptions.value[0]
        expandedMonths.value = [firstMonth]
        selectedMonth.value = firstMonth
        await fetchDatesByMonth(firstMonth)
        // 不再自动调用 fetchDataByMonth，图表区域保持空白
      }
    }
  } catch (error) {
    console.error('获取月份列表失败:', error)
  }
}

// 根据月份获取日期列表
const fetchDatesByMonth = async (month) => {
  if (!month) return
  try {
    const response = await axios.get(DATES_API, {
      params: { month },
      headers: getAuthHeaders()
    })
    if (response.data.code === 200) {
      dateOptions.value[month] = response.data.data || []
      console.log('月份', month, '的日期列表:', dateOptions.value[month])
    }
  } catch (error) {
    console.error('获取日期列表失败:', error)
  }
}

// 根据月份获取数据（可选日期过滤）
const fetchDataByMonth = async (month, date = null) => {
  if (!month) return
  groupLoading.value = true
  try {
    const params = { month }
    if (date) {
      params.date = date
    }
    const response = await axios.get(ALL_GROUPED_API, {
      params,
      headers: getAuthHeaders()
    })
    if (response.data.code === 200) {
      const data = response.data.data || {}
      groupedCurves.value = data
      // 使用响应式方式更新对象
      monthGroupedCurves.value = {
        ...monthGroupedCurves.value,
        [month]: data
      }
      console.log('月份', month, '的组数据:', data)
      // 获取曲线名称映射
      await fetchCurveNames()
      // 不再自动选中组，保持空白状态等待用户选择
    }
  } catch (error) {
    console.error('获取分组数据失败:', error)
  } finally {
    groupLoading.value = false
  }
}

// 获取所有曲线的名称映射
const fetchCurveNames = async () => {
  try {
    // 收集所有 nameIds
    const allNameIds = []
    for (const [month, groups] of Object.entries(monthGroupedCurves.value)) {
      for (const [gid, curves] of Object.entries(groups)) {
        for (const curve of curves) {
          if (curve.curveNameId && !allNameIds.includes(curve.curveNameId)) {
            allNameIds.push(curve.curveNameId)
          }
        }
      }
    }
    if (allNameIds.length === 0) return

    const response = await axios.get(TEXT_NAMES_API, {
      params: { nameIds: allNameIds.join(',') },
      headers: getAuthHeaders()
    })
    if (response.data.code === 200) {
      nameMap.value = response.data.data || {}
      console.log('曲线名称映射:', nameMap.value)
    }
  } catch (error) {
    console.error('获取曲线名称失败:', error)
  }
}

// 处理月份展开变化
const handleMonthExpand = async (expanded) => {
  if (expanded.length > 0) {
    const latestMonth = expanded[expanded.length - 1]
    if (latestMonth !== selectedMonth.value) {
      selectedMonth.value = latestMonth
      selectedDate.value = null
      selectedGroupId.value = null
      selectedCurveNameId.value = null
      expandedGroups.value = []
      expandedDates.value = []
      // 获取该月份的日期列表
      if (!dateOptions.value[latestMonth]) {
        await fetchDatesByMonth(latestMonth)
      }
      await fetchDataByMonth(latestMonth)
    }
  }
}

// 处理日期展开变化
const handleDateExpand = async (expanded) => {
  if (expanded.length > 0) {
    const latestDate = expanded[expanded.length - 1]
    if (latestDate !== selectedDate.value) {
      selectedDate.value = latestDate
      selectedGroupId.value = null
      selectedCurveNameId.value = null
      expandedGroups.value = []
      // 清空曲线数据缓存
      curveDataMap.value = {}
      // 清空月份分组数据缓存，强制重新获取
      monthGroupedCurves.value = {}
      // 重新加载组数据（不带日期参数，树形显示所有组）
      if (selectedMonth.value) {
        await fetchDataByMonth(selectedMonth.value)
        // 等待数据加载后，默认展开第一个有数据的组
        await nextTick()
        const groups = getGroupsByDate(selectedMonth.value)
        if (groups.length > 0) {
          expandedGroups.value = [groups[0]]
          selectGroup(groups[0])
        }
      }
    }
  }
}

// 点击选择某个组（展开该组并选中）
const expandAndSelectGroup = (gid) => {
  // 只展开选中的组，折叠其他组
  expandedGroups.value = [gid]
  selectGroup(gid)
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

      // 获取该组所有曲线的名称ID
      const nameIds = currentCurves.value.map(c => c.curveNameId).filter(id => id != null)
      if (nameIds.length > 0) {
        await fetchCurveDataByNameIds(nameIds)
      }

      // 等待数据加载完成后再渲染图表
      await nextTick()
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
  selectedCurveNameId.value = null
  selectedCurve.value = null
  loadGroupCurves(gid)
}

// 处理组展开/折叠变化
const handleGroupExpand = (expanded) => {
  if (expanded.length > 0) {
    const latestExpanded = expanded[expanded.length - 1]
    if (latestExpanded !== selectedGroupId.value) {
      selectGroup(latestExpanded)
    }
  }
}

// 监听展开的组变化，自动选中
watch(expandedGroups, (newVal, oldVal) => {
  if (newVal.length > 0 && newVal !== oldVal) {
    const latest = newVal[newVal.length - 1]
    if (latest !== selectedGroupId.value) {
      selectGroup(latest)
    }
  }
}, { deep: true })

// 根据组ID获取该组的曲线列表
const getCurvesByGroup = (gid) => {
  return groupedCurves.value[gid] || []
}

// 获取某月份下的所有组ID
const getGroupsByMonth = (month) => {
  console.log('getGroupsByMonth called, month:', month, 'data:', monthGroupedCurves.value[month])
  if (!monthGroupedCurves.value[month]) return []
  return Object.keys(monthGroupedCurves.value[month]).map(g => parseInt(g))
}

// 获取某月份下的所有日期
const getDatesByMonth = (month) => {
  return dateOptions.value[month] || []
}

// 获取某月份某日期下的所有组ID
const getGroupsByDate = (month, date) => {
  if (!monthGroupedCurves.value[month]) return []
  return Object.keys(monthGroupedCurves.value[month]).map(g => parseInt(g))
}

// 获取某月份某日期的组数量
const getGroupCountByDate = (month, date) => {
  if (!monthGroupedCurves.value[month]) return 0
  return Object.keys(monthGroupedCurves.value[month]).length
}

// 获取某月份的组数量
const getGroupCountByMonth = (month) => {
  if (!monthGroupedCurves.value[month]) {
    console.log('getGroupCountByMonth: no data for month', month)
    return 0
  }
  const count = Object.keys(monthGroupedCurves.value[month]).length
  console.log('getGroupCountByMonth:', month, 'count:', count)
  return count
}

// 获取某组的曲线数量
const getCurveCountByGroup = (gid) => {
  return groupedCurves.value[gid]?.length || 0
}

// 获取曲线的显示名称（优先使用中文名）
const getCurveDisplayName = (curveNameId) => {
  if (!curveNameId) return ''
  return nameMap.value[curveNameId] || String(curveNameId)
}

// 获取某条曲线的数据点数量
const getCurveDataCount = (curveNameId) => {
  if (!curveNameId) return 0
  const dataList = curveDataMap.value[curveNameId]
  return dataList ? dataList.length : 0
}

// 选择曲线
const selectCurve = (curve) => {
  if (activeTab.value === 'compare') {
    // 对比模式：添加或移除曲线（再次点击取消选择）
    const existingIndex = compareCurves.value.findIndex(c => c.curveNameId === curve.curveNameId)
    if (existingIndex > -1) {
      compareCurves.value.splice(existingIndex, 1)
    } else {
      compareCurves.value.push(curve)
    }
    updateCompareChart()
  } else {
    // 普通模式：单选曲线
    selectedCurveNameId.value = curve.curveNameId
    selectedCurve.value = curve
    loadSingleCurveData(curve)
  }
}

// 切换标签页
const switchTab = (tab) => {
  activeTab.value = tab
  if (tab === 'compare') {
    // 清空对比相关状态
    compareCurves.value = []
    selectedCurveNameId.value = null
    selectedCurve.value = null
    currentCurves.value = []
    // 销毁主图表，避免干扰
    if (chart) {
      chart.dispose()
      chart = null
    }
    // 等待 DOM 更新后初始化对比图表
    nextTick(() => {
      if (compareChartRef.value && !compareChart) {
        compareChart = echarts.init(compareChartRef.value)
      }
    })
  } else if (tab === 'chart') {
    // 销毁对比图表，避免干扰
    if (compareChart) {
      compareChart.dispose()
      compareChart = null
    }
    // 切换回曲线展示视图
    // 如果有已选曲线，重新加载数据
    if (selectedCurveNameId.value && selectedCurve.value) {
      loadSingleCurveData(selectedCurve.value)
    } else if (selectedGroupId.value) {
      // 否则加载组内所有曲线
      loadGroupCurves(selectedGroupId.value)
    }
  }
}

// 加载单条曲线数据
const loadSingleCurveData = async (curve) => {
  chartLoading.value = true
  try {
    const nameId = curve.curveNameId
    const params = { nameIds: nameId }
    if (selectedDate.value) {
      params.date = selectedDate.value
    } else if (selectedMonth.value) {
      params.month = selectedMonth.value
    }
    const response = await axios.get(CURVE_DATA_API, {
      params,
      headers: getAuthHeaders()
    })
    if (response.data.code === 200) {
      const dataList = response.data.data || []
      // 清空旧数据
      curveDataMap.value = {}
      const grouped = {}
      dataList.forEach(item => {
        const key = String(item.nameId)
        if (!grouped[key]) {
          grouped[key] = []
        }
        grouped[key].push(item)
      })
      for (const key in grouped) {
        grouped[key].sort((a, b) => new Date(a.realTime) - new Date(b.realTime))
      }
      curveDataMap.value = grouped
      currentCurves.value = [curve]
      await nextTick()
      renderChart()
    }
  } catch (error) {
    handleApiError(error)
  } finally {
    chartLoading.value = false
  }
}

// 清除曲线选择，返回显示组内所有曲线
const clearCurveSelection = () => {
  selectedCurveNameId.value = null
  selectedCurve.value = null
  loadGroupCurves(selectedGroupId.value)
}

// 清空对比列表
const clearCompare = () => {
  compareCurves.value = []
}

// 从对比列表移除
const removeFromCompare = (curveNameId) => {
  const index = compareCurves.value.findIndex(c => c.curveNameId === curveNameId)
  if (index > -1) {
    compareCurves.value.splice(index, 1)
    updateCompareChart()
  }
}

// 更新对比图表
const updateCompareChart = async () => {
  // 等待 DOM 更新
  await nextTick()

  if (!compareChartRef.value) return

  // 清空图表
  if (compareChart) {
    compareChart.dispose()
    compareChart = null
  }

  if (compareCurves.value.length === 0) {
    return
  }

  // 获取所有选中曲线的 nameIds
  const nameIds = compareCurves.value.map(c => c.curveNameId)

  // 获取数据
  let curveDataTmpMap = {}
  try {
    const params = { nameIds: nameIds.join(',') }
    if (selectedMonth.value) {
      params.month = selectedMonth.value
    }
    const response = await axios.get(CURVE_DATA_API, {
      params,
      headers: getAuthHeaders()
    })
    if (response.data.code === 200) {
      const dataList = response.data.data || []
      dataList.forEach(item => {
        const key = String(item.nameId)
        if (!curveDataTmpMap[key]) {
          curveDataTmpMap[key] = []
        }
        curveDataTmpMap[key].push(item)
      })
      for (const key in curveDataTmpMap) {
        curveDataTmpMap[key].sort((a, b) => new Date(a.realTime) - new Date(b.realTime))
      }
    }
  } catch (error) {
    console.error('获取对比曲线数据失败:', error)
  }

  // 初始化图表
  compareChart = echarts.init(compareChartRef.value)

  // 获取背景色
  const bgColor = groupMeta.value.bgColor || '#ffffff'

  // 构建 series
  const colors = ['#409EFF', '#67C23A', '#E6A23C', '#F56C6C', '#909399', '#773C1A', '#F7BA2A', '#8e44ad']
  const series = compareCurves.value.map((curve, index) => {
    const nameId = String(curve.curveNameId)
    const dataList = curveDataTmpMap[nameId] || []
    const data = dataList.map(item => item.value)

    return {
      name: getCurveDisplayName(curve.curveNameId),
      type: 'line',
      data: data,
      smooth: 0.4,
      symbol: 'none',
      lineStyle: {
        width: 2,
        color: curve.curveColor || colors[index % colors.length]
      },
      itemStyle: {
        color: curve.curveColor || colors[index % colors.length]
      },
      areaStyle: curve.curvePredict === 1 ? null : {
        opacity: 0.05,
        color: curve.curveColor || colors[index % colors.length]
      }
    }
  })

  // 获取 X 轴数据（时间标签）
  let xData = []
  if (compareCurves.value.length > 0) {
    const firstNameId = String(compareCurves.value[0].curveNameId)
    const firstData = curveDataTmpMap[firstNameId] || []
    xData = firstData.map(item => {
      if (item.realTime && item.realTime.length > 10) {
        return item.realTime.substring(11)
      }
      return ''
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
      right: '5%',
      bottom: '12%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: xData,
      boundaryGap: false,
      axisLine: { lineStyle: { color: '#ccc' } },
      axisLabel: {
        color: '#666',
        rotate: 0,
        interval: xData.length > 8 ? Math.floor(xData.length / 8) : 0
      }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#666' } },
      axisLabel: { color: '#666' },
      splitLine: { lineStyle: { type: 'dashed', color: '#eee' } }
    },
    series: series,
    animation: false
  }

  compareChart.setOption(option, true)
}

// 刷新当前曲线
const refreshCurves = () => {
  if (selectedCurveNameId.value && selectedCurve.value) {
    loadSingleCurveData(selectedCurve.value)
  } else if (selectedGroupId.value) {
    loadGroupCurves(selectedGroupId.value)
  }
}

// 获取图表标题
const getChartTitle = () => {
  const monthStr = selectedMonth.value ? ` (${selectedMonth.value})` : ''
  if (selectedCurveNameId.value && selectedCurve.value) {
    const curveName = getCurveDisplayName(selectedCurve.value.curveNameId)
    return `曲线组 ${selectedGroupId.value} - ${curveName}${monthStr}`
  } else if (selectedGroupId.value) {
    return `曲线组 ${selectedGroupId.value} - 全部曲线${monthStr}`
  }
  return '请选择左侧曲线组'
}

// 根据曲线名称ID列表获取曲线数据
const fetchCurveDataByNameIds = async (nameIds) => {
  try {
    const params = { nameIds: nameIds.join(',') }
    if (selectedDate.value) {
      params.date = selectedDate.value
    } else if (selectedMonth.value) {
      params.month = selectedMonth.value
    }
    const response = await axios.get(CURVE_DATA_API, {
      params,
      headers: getAuthHeaders()
    })
    if (response.data.code === 200) {
      const dataList = response.data.data || []
      // 清空旧数据
      curveDataMap.value = {}
      // 按 nameId 分组
      const grouped = {}
      dataList.forEach(item => {
        const key = String(item.nameId)
        if (!grouped[key]) {
          grouped[key] = []
        }
        grouped[key].push(item)
      })
      // 每个组内按时间排序
      for (const key in grouped) {
        grouped[key].sort((a, b) => new Date(a.realTime) - new Date(b.realTime))
      }
      curveDataMap.value = grouped
    } else {
      console.error('获取曲线数据失败:', response.data.message)
    }
  } catch (error) {
    console.error('获取曲线数据失败:', error)
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

// 基于配置获取曲线数据（从 curve_data 表获取）
const getRealCurveData = (curve) => {
  const nameId = String(curve.curveNameId)
  if (!nameId) {
    console.log(`曲线 ${curve.curveNameId}: 没有曲线名称ID`)
    return []
  }

  const dataList = curveDataMap.value[nameId]
  if (!dataList || dataList.length === 0) {
    console.log(`曲线 ${nameId}: 没有数据`)
    return []
  }

  console.log(`曲线 ${nameId} 使用真实数据, 数据点: ${dataList.length}`)
  return dataList.map(item => item.value)
}

// 生成X轴时间标签（基于真实数据的时间范围）
const generateXAxisFromData = (curves) => {
  if (!curves || curves.length === 0) {
    return []
  }

  // 找到第一个有数据的曲线
  for (const curve of curves) {
    const nameId = curve.curveNameId
    const dataList = curveDataMap.value[nameId]
    if (dataList && dataList.length > 0) {
      return dataList.map(item => formatTime(item.realTime))
    }
  }

  return []
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
  // DOM 未准备好时，延迟重试
  if (!chartRef.value) {
    setTimeout(renderChart, 100)
    return
  }
  if (currentCurves.value.length === 0) return

  doRenderChart()
}

const doRenderChart = () => {
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
  const xData = generateXAxisFromData(curves)

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
      name: getCurveDisplayName(curve.curveNameId),
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
      axisLabel: { color: '#666', rotate: 0, interval: xData.length > 8 ? Math.floor(xData.length / 8) : 0 },
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
  fetchMonths()
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
  gap: 10px;
  color: #fff;
  cursor: pointer;
  padding: 10px 18px;
  border-radius: 12px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  font-weight: 500;
  font-size: 14px;
}

.user-info:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.02);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.user-info .el-icon {
  font-size: 18px;
}

.back-btn {
  background: linear-gradient(135deg, #fff 0%, #f8f9fa 100%);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #4a5568;
  font-weight: 600;
  font-size: 14px;
  padding: 10px 18px;
  border-radius: 10px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.back-btn:hover {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
  border-color: transparent;
}

.back-btn .el-icon {
  margin-right: 6px;
  font-size: 16px;
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

/* 折叠面板样式 */
.group-collapse {
  border: none;
}

.group-collapse :deep(.el-collapse-item__header) {
  background: transparent;
  border: none;
  padding: 0 8px;
  height: auto;
  line-height: normal;
}

.group-collapse :deep(.el-collapse-item__wrap) {
  background: transparent;
  border: none;
}

.group-collapse :deep(.el-collapse-item__content) {
  padding: 0 8px 8px 8px;
}

.group-collapse :deep(.el-collapse-item__arrow) {
  color: #667eea;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 8px 0;
  cursor: pointer;
}

.date-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding: 6px 8px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s ease;
}

.date-header:hover {
  background: rgba(64, 158, 255, 0.1);
}

.date-header.active {
  background: rgba(64, 158, 255, 0.15);
  color: #409EFF;
}

.date-label {
  font-weight: 600;
  font-size: 13px;
  color: #606266;
}

.expand-icon {
  transition: transform 0.3s;
  margin-left: 5px;
}

.expand-icon.expanded {
  transform: rotate(90deg);
}

.group-item-label {
  font-weight: 600;
  font-size: 14px;
  color: #333;
}

/* 曲线列表 */
.curve-list {
  background: #f8f9fc;
  border-radius: 8px;
  padding: 4px 0;
}

.curve-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s ease;
  font-size: 13px;
  color: #606266;
}

.curve-item:hover {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.curve-item.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
  font-weight: 600;
}

.curve-name-id {
  font-family: 'Consolas', monospace;
}

.color-dot.small {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
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
  flex: 1;
}

.title-tabs {
  display: flex;
  gap: 4px;
}

.tab-item {
  padding: 8px 20px;
  background: #f0f0f0;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-weight: 500;
  font-size: 14px;
  color: #606266;
  user-select: none;
}

.tab-item:hover {
  background: #e6e6e6;
  color: #409EFF;
}

.tab-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
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

/* 曲线对比视图样式 */
.compare-container {
  padding: 20px;
}

.compare-empty {
  padding: 60px 20px;
}

.compare-content {
  min-height: 400px;
}

.compare-devices-header {
  margin-bottom: 20px;
  padding: 15px;
  background: #f5f7fa;
  border-radius: 8px;
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
  gap: 12px;
}

.compare-device-tag {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border-radius: 8px;
  font-size: 14px;
}

.device-tag-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.device-tag-name {
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 6px;
}

.device-tag-date {
  font-size: 12px;
  opacity: 0.9;
}

.compare-chart {
  width: 100%;
  height: 500px;
  border-radius: 12px;
  overflow: hidden;
}
</style>
