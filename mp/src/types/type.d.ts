// src/types/index.d.ts

export interface UserDTO {
  id?: number
  username?: string
  password?: string
  nickname?: string
  avatar?: string
  email?: string
  phone?: string
  status?: {
    code: number
    desc: string
  }
  createTime?: string
  updateTime?: string
  roles?: string[]
}

export interface Page<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 习惯表单 DTO。
 * 与后端 HabitDTO 的主要字段保持一致。
 */
export interface HabitDTO {
  id?: number
  name: string
  description?: string
  category?: string
  icon?: string
  color?: string
  status?: number
  startDate: string
  endDate?: string | null
  allowMakeup: number
  makeupLimitDays: number
  reminderEnabled: number
  reminderTime?: string
  sortOrder?: number
  ruleType: 'DAILY' | 'WEEKLY'
  ruleDays: number[]
}

/**
 * 首页习惯卡片 DTO。
 */
export interface TodayHabitDTO {
  habitId: number
  habitName: string
  icon?: string
  color?: string
  category?: string
  reminderTime?: string
  currentStreak: number
  todayChecked: number
  todayDate: string
  ruleText: string
}

/**
 * 首页总览 DTO。
 */
export interface TodayOverviewDTO {
  date: string
  plannedCount: number
  completedCount: number
  completionRate: number
  habits: TodayHabitDTO[]
}

/**
 * 打卡提交 DTO。
 */
export interface CheckInDTO {
  habitId: number
  recordDate: string
  note?: string
  isMakeup?: number
}

/**
 * 打卡记录 DTO。
 */
export interface CheckInRecordDTO {
  id: number
  habitId: number
  habitName: string
  recordDate: string
  checkInTime: string
  status: number
  note?: string
  isMakeup: number
  source: string
}

/**
 * 习惯统计 DTO。
 */
export interface HabitStatsDTO {
  habitId: number
  habitName: string
  currentStreak: number
  longestStreak: number
  totalCheckInCount: number
  completionRate7d: number
  completionRate30d: number
  lastCheckInDate?: string | null
}
