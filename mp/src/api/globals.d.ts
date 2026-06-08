/* tslint:disable */
/* eslint-disable */
import type { Alova, AlovaMethodCreateConfig, AlovaGenerics, Method } from 'alova'
import type { $$userConfigMap, alovaInstance } from './index'
import type apiDefinitions from './apiDefinitions'
import type {
  CheckInDTO,
  CheckInRecordDTO,
  HabitDTO,
  HabitStatsDTO,
  TodayOverviewDTO,
  UserDTO,
} from '@/types/type'

type CollapsedAlova = typeof alovaInstance
type UserMethodConfigMap = typeof $$userConfigMap

type Alova2MethodConfig<Responded> =
  CollapsedAlova extends Alova<
    AlovaGenerics<
      any,
      any,
      infer RequestConfig,
      infer Response,
      infer ResponseHeader,
      infer L1Cache,
      infer L2Cache,
      infer SE
    >
  >
    ? Omit<
        AlovaMethodCreateConfig<
          AlovaGenerics<Responded, any, RequestConfig, Response, ResponseHeader, L1Cache, L2Cache, SE>,
          any,
          Responded
        >,
        'params'
      >
    : never

/**
 * 提取用户自定义 transform 的返回类型。
 * 如果没有定义 transform，则回退到默认返回值类型。
 */
type ExtractUserDefinedTransformed<
  DefinitionKey extends keyof typeof apiDefinitions,
  Default,
> = DefinitionKey extends keyof UserMethodConfigMap
  ? UserMethodConfigMap[DefinitionKey]['transform'] extends (...args: any[]) => any
    ? Awaited<ReturnType<UserMethodConfigMap[DefinitionKey]['transform']>>
    : Default
  : Default

type Alova2Method<
  Responded,
  DefinitionKey extends keyof typeof apiDefinitions,
  CurrentConfig extends Alova2MethodConfig<any>,
> =
  CollapsedAlova extends Alova<
    AlovaGenerics<
      any,
      any,
      infer RequestConfig,
      infer Response,
      infer ResponseHeader,
      infer L1Cache,
      infer L2Cache,
      infer SE
    >
  >
    ? Method<
        AlovaGenerics<
          CurrentConfig extends undefined
            ? ExtractUserDefinedTransformed<DefinitionKey, Responded>
            : CurrentConfig['transform'] extends (...args: any[]) => any
              ? Awaited<ReturnType<CurrentConfig['transform']>>
              : ExtractUserDefinedTransformed<DefinitionKey, Responded>,
          any,
          RequestConfig,
          Response,
          ResponseHeader,
          L1Cache,
          L2Cache,
          SE
        >
      >
    : never

/**
 * 后端统一返回结构。
 */
export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

declare global {
  interface Apis {
    // 用户相关接口
    universal: {
      // 登录
      login<
        Config extends Alova2MethodConfig<ApiResult<string>> & {
          data: {
            username?: string
            password?: string
          }
        },
      >(
        config: Config,
      ): Alova2Method<ApiResult<string>, 'universal.login', Config>

      // 注册
      register<
        Config extends Alova2MethodConfig<ApiResult<string>> & {
          data: {
            username?: string
            password?: string
          }
        },
      >(
        config: Config,
      ): Alova2Method<ApiResult<string>, 'universal.register', Config>

      // 获取用户信息
      getUserInfo<Config extends Alova2MethodConfig<ApiResult<UserDTO>>>(
        config: Config,
      ): Alova2Method<ApiResult<UserDTO>, 'universal.getUserInfo', Config>

      // 退出登录
      logout<Config extends Alova2MethodConfig<ApiResult<string>>>(
        config: Config,
      ): Alova2Method<ApiResult<string>, 'universal.logout', Config>
    }

    // 习惯相关接口
    habit: {
      // 创建习惯
      create<
        Config extends Alova2MethodConfig<ApiResult<string>> & {
          data: HabitDTO
        },
      >(
        config: Config,
      ): Alova2Method<ApiResult<string>, 'habit.create', Config>

      // 更新习惯
      update<
        Config extends Alova2MethodConfig<ApiResult<string>> & {
          data: HabitDTO
        },
      >(
        config: Config,
      ): Alova2Method<ApiResult<string>, 'habit.update', Config>

      // 删除习惯
      delete<
        Config extends Alova2MethodConfig<ApiResult<string>> & {
          params: {
            id: number
          }
        },
      >(
        config: Config,
      ): Alova2Method<ApiResult<string>, 'habit.delete', Config>

      // 暂停习惯
      pause<
        Config extends Alova2MethodConfig<ApiResult<string>> & {
          params: {
            id: number
          }
        },
      >(
        config: Config,
      ): Alova2Method<ApiResult<string>, 'habit.pause', Config>

      // 恢复习惯
      resume<
        Config extends Alova2MethodConfig<ApiResult<string>> & {
          params: {
            id: number
          }
        },
      >(
        config: Config,
      ): Alova2Method<ApiResult<string>, 'habit.resume', Config>

      // 获取习惯详情
      detail<
        Config extends Alova2MethodConfig<ApiResult<HabitDTO>> & {
          params: {
            id: number
          }
        },
      >(
        config: Config,
      ): Alova2Method<ApiResult<HabitDTO>, 'habit.detail', Config>

      // 获取习惯列表
      list<Config extends Alova2MethodConfig<ApiResult<HabitDTO[]>>>(
        config: Config,
      ): Alova2Method<ApiResult<HabitDTO[]>, 'habit.list', Config>
    }

    // 打卡相关接口
    checkIn: {
      // 提交普通打卡
      submit<
        Config extends Alova2MethodConfig<ApiResult<string>> & {
          data: CheckInDTO
        },
      >(
        config: Config,
      ): Alova2Method<ApiResult<string>, 'checkIn.submit', Config>

      // 提交补打卡
      makeup<
        Config extends Alova2MethodConfig<ApiResult<string>> & {
          data: CheckInDTO
        },
      >(
        config: Config,
      ): Alova2Method<ApiResult<string>, 'checkIn.makeup', Config>

      // 查询打卡记录
      list<
        Config extends Alova2MethodConfig<ApiResult<CheckInRecordDTO[]>> & {
          params: {
            habitId: number
          }
        },
      >(
        config: Config,
      ): Alova2Method<ApiResult<CheckInRecordDTO[]>, 'checkIn.list', Config>
    }

    // 统计相关接口
    habitStats: {
      // 获取今日总览
      todayOverview<Config extends Alova2MethodConfig<ApiResult<TodayOverviewDTO>>>(
        config: Config,
      ): Alova2Method<ApiResult<TodayOverviewDTO>, 'habitStats.todayOverview', Config>

      // 获取习惯统计详情
      detail<
        Config extends Alova2MethodConfig<ApiResult<HabitStatsDTO>> & {
          params: {
            habitId: number
          }
        },
      >(
        config: Config,
      ): Alova2Method<ApiResult<HabitStatsDTO>, 'habitStats.detail', Config>
    }
  }

  var Apis: Apis
}
