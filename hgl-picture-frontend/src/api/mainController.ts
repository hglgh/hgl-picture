// @ts-ignore
/* eslint-disable */
import request from '@/request'

/** health GET /hgl/health */
export async function healthUsingGet(options?: { [key: string]: any }) {
  return request<API.BaseResponseString_>('/hgl/health', {
    method: 'GET',
    ...(options || {}),
  })
}
