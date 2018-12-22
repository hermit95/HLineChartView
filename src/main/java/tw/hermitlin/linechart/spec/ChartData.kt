package tw.hermitlin.linechart.spec

/**
 * -----------------------------------------------------------------
 * Copyright (C) 2012-2018 by Hermit Lin. All rights reserved.
 * -----------------------------------------------------------------
 *
 * File: ChartData.kt
 * Author: Hermit Lin (hermitnull@gmail.com)
 * Version: V0.0.1 2018/12/6
 * Create: 2018/12/6 下午 05:51
 *
 * -----------------------------------------------------------------
 * Release note:
 * V0.0.1 2018/12/6 (Hermit Lin)
 * 1.Create ChartData.kt
 * -----------------------------------------------------------------
 * Description:
 *  統計圖表的資料結構
 *
 */
interface ChartData {
    //水平軸資料值(副資料軸，代表要顯示的位置)
    fun getHorizontalAxisValue(): Float
    //垂直軸資料值(主資料軸，代表要顯示的量(在圓餅圖會有差))
    fun getVerticalAxisValue(): Float
    //顯示的Label(標示於點上，LineChartView裡做開關)
    fun getLabel(): String
}