package tw.hermitlin.linechart.spec

/**
 * -----------------------------------------------------------------
 * Copyright (C) 2012-2018 by Hermit Lin. All rights reserved.
 * -----------------------------------------------------------------
 *
 * File: ChartDetail.kt
 * Author: Hermit Lin (hermitnull@gmail.com)
 * Version: V0.0.1 2018/12/7
 * Create: 2018/12/7 下午 02:19
 *
 * -----------------------------------------------------------------
 * Release note:
 * V0.0.1 2018/12/7 (Hermit Lin)
 * 1.Create ChartDetail.kt
 * -----------------------------------------------------------------
 * Description:
 *  折線圖的詳細資料規格
 * 1.垂直水平的最大、最小、每格刻度、資料單位
 *
 * 備註：UI規範統一在View處理
 */
data class ChartRequest(
        var xMinValue: Float,
        var xMaxValue: Float,
        var xGridScale: Float,
        var xUnitName: String,
        var yMinValue: Float,
        var yMaxValue: Float,
        var yGridScale: Float,
        var yUnitName: String
)