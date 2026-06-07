import request from '../utils/request.ts'
import type * as API from '../types.ts'

/**
 * 发布通知。
 */
export function publishNoticeApi(data: API.Notice): Promise<string> {
  return request({
    url: '/notice/publish',
    method: 'post',
    data,
  })
}

/**
 * 获取当前用户的通知分页列表。
 */
export function getMyNoticePageApi(
  params: { current: number, size: number },
  data?: Partial<Pick<API.Notice, 'isRead' | 'type' | 'title'>>,
): Promise<API.PageResult<API.Notice>> {
  return request({
    url: '/notice/my-page',
    method: 'post',
    params,
    data,
  })
}

/**
 * 将单条通知标记为已读。
 */
export function readNoticeApi(id: number): Promise<string> {
  return request({
    url: `/notice/read/${id}`,
    method: 'get',
  })
}

/**
 * 将当前用户全部通知标记为已读。
 */
export function readAllNoticesApi(): Promise<string> {
  return request({
    url: '/notice/read-all',
    method: 'post',
  })
}

/**
 * 获取未读通知数量。
 */
export function getUnreadCountApi(): Promise<number> {
  return request({
    url: '/notice/unread-count',
    method: 'get',
  })
}
