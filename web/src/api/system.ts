import request from '../utils/request.ts'

/**
 * 获取所有后端接口列表
 */
export interface FieldDoc {
  field: string
  type: string
  description: string
  required: boolean
  example: string
  children?: FieldDoc[]
}

export interface EndpointParam {
  name: string
  type: string
  source: string
  schema?: FieldDoc[]
}

export interface Endpoint {
  name: string
  methodName: string
  controller: string
  path: string[]
  methods: string[]
  description: string
  order: number
  params: EndpointParam[]
  response: FieldDoc[]
}

/**
 * 系统监控信息接口
 */
export interface SystemMonitorInfo {
  systemInfo: {
    osName: string
    osVersion: string
    osArch: string
    userHome: string
    userDir: string
    javaVersion: string
    javaVendor: string
    javaHome: string
    hostName: string
    hostAddress: string
  }
  jvmInfo: {
    jvmName: string
    jvmVersion: string
    jvmVendor: string
    jvmSpecVersion: string
    totalMemory: number
    freeMemory: number
    maxMemory: number
    availableProcessors: number
    jvmArgs: string
    systemProperties: Record<string, string>
  }
  memoryInfo: {
    totalMemory: number
    freeMemory: number
    maxMemory: number
    usedMemory: number
    memoryUsageRate: number
    totalSwap: number
    freeSwap: number
    usedSwap: number
    swapUsageRate: number
  }
  diskInfos: {
    fileSystem: string
    mountPoint: string
    totalSpace: number
    freeSpace: number
    usableSpace: number
    usedSpace: number
    usageRate: number
  }[]
  runtimeInfo: {
    uptime: number
    startTime: number
    threadCount: number
    peakThreadCount: number
    totalStartedThreadCount: number
    heapMemoryUsed: number
    heapMemoryCommitted: number
    heapMemoryMax: number
    nonHeapMemoryUsed: number
    nonHeapMemoryCommitted: number
    nonHeapMemoryMax: number
  }
}

export function getEndpointsApi(): Promise<Endpoint[]> {
  return request({
    url: '/system/endpoints',
    method: 'get',
  })
}

export function getSystemMonitorInfoApi(): Promise<SystemMonitorInfo> {
  return request({
    url: '/system/monitor',
    method: 'get',
  })
}
