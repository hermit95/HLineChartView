package tw.hermitlin.linechart

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.support.annotation.ColorInt
import android.util.AttributeSet
import android.util.Log
import android.view.View
import tw.hermitlin.linechart.spec.ChartData
import tw.hermitlin.linechart.spec.ChartRequest
import java.util.*


/**
 * -----------------------------------------------------------------
 * Copyright (C) 2012-2018 by Hermit Lin. All rights reserved.
 * -----------------------------------------------------------------
 *
 * File: HLineChartView.kt
 * Author: Hermit Lin (hermitnull@gmail.com)
 * Version: V0.0.1 2018/12/6
 * Create: 2018/12/6 下午 04:16
 *
 * -----------------------------------------------------------------
 * Release note:
 * V0.0.2(未發布)
 * 1.新增移除及清除資料
 *
 * V0.0.1 2018/12/6 (Hermit Lin)
 * 1.Create HLineChartView.kt
 * -----------------------------------------------------------------
 * Description:
 * 畫折線圖用的View
 */
class HLineChartView: View {
    //繪圖Object
    private val mPaint = Paint()
    private val mLineChartMap: HashMap<String, ArrayList<out ChartData>>
    private val mLineChartOrder: ArrayList<String> //繪製的順序 FIFO
    /**圖表的數據規格*/
    private var mChartRequest: ChartRequest = ChartRequest(0f, 100f, 10f, "X",
    0f, 1000f, 50f, "Y")
    //UI 顯示功能參數
    private var isDrawAxis = true
    private var isDrawGridScale = true
    private var isDrawLabel = true
    private var isDrawPoint = true
    private var isDrawAxisLabel = true

    //UI參數設置
    /** 繪圖區德padding */
    private val defaultLineChatColors: IntArray
    private var defaultChartLeftPadding = 150f //為了空出左半部刻度空間
    private var defaultChartRightPadding = 150f
    private var defaultChartTopPadding = 40f
    private var defaultChartBottomPadding = 50f //為了空出下半部刻度空間
    private var textSize = 40f//sp
    private val startPoint = PointF()
    private val endPoint = PointF()
    private var xAxisScale = 1f//水平點距對應View 寬的比例尺
    private var yAxisScale = 1f//垂直點距對應View 高的比例尺

    /**
     * 已確定View Size
     * */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        //計算水平、垂直點距
        startPoint.x = 0f + defaultChartLeftPadding
        startPoint.y = 0f + defaultChartTopPadding
        endPoint.x = width.toFloat() - defaultChartRightPadding
        endPoint.y = height - defaultChartBottomPadding
        //計算水平、垂直的點距對應View寬、高的比例尺
        calculateAxisScale()
    }
    /**
     * 計算水平、垂直的點距對應View寬、高的比例尺
     * */
    private fun calculateAxisScale(){
        xAxisScale = (endPoint.x - startPoint.x) / (mChartRequest.xMaxValue - mChartRequest.xMinValue)
        yAxisScale = (endPoint.y - startPoint.y) / (mChartRequest.yMaxValue - mChartRequest.yMinValue)
    }

    private var axisLabelValue = 0f
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isDrawAxis) {
            setAxisPaint()
            //x axis
            canvas.drawLine(startPoint.x, endPoint.y, endPoint.x, endPoint.y, mPaint)
            //y axis
            canvas.drawLine(startPoint.x, startPoint.y, startPoint.x, endPoint.y, mPaint)
        }

        if(isDrawAxisLabel){
            setTextPaint(Paint.Align.CENTER, getColor(R.color.base_line))
            canvas.drawText(mChartRequest.xUnitName, (startPoint.x + endPoint.x) / 2f, height.toFloat(), mPaint)
            canvas.save()
            canvas.rotate(-90f, 0f, (startPoint.y + endPoint.y) / 2f)
            canvas.drawText(mChartRequest.yUnitName, 0f, (startPoint.y + endPoint.y) / 2f + textSize, mPaint)
            canvas.restore()
        }

        if(isDrawGridScale){
            //x grid scale
            axisLabelValue  = mChartRequest.xMinValue
            setTextPaint(Paint.Align.CENTER, getColor(R.color.base_line))
            while (axisLabelValue <= mChartRequest.xMaxValue){
                val point = convertDataPointToUIPoint(axisLabelValue, 0f)
                canvas.drawText(axisLabelValue.toString(), point[0], point[1] + textSize, mPaint)
                axisLabelValue += mChartRequest.xGridScale
            }


            //y grid scale
            axisLabelValue  = mChartRequest.yMinValue
            setTextPaint(Paint.Align.RIGHT, getColor(R.color.base_line))
            while (axisLabelValue <= mChartRequest.yMaxValue){
                val point = convertDataPointToUIPoint(0f, axisLabelValue)
                canvas.drawText(axisLabelValue.toString(), point[0] - textSize/4f, point[1] + textSize/2f, mPaint)
                axisLabelValue += mChartRequest.yGridScale
            }
        }


        //Line Chart
        mLineChartOrder.indices.forEach {lineChartIndex ->
            mLineChartMap[mLineChartOrder[lineChartIndex]]?.let {dataList ->
                //畫線
                val pointList = convertDataListToUIList(dataList)
                setLinePaint(defaultLineChatColors[lineChartIndex % defaultLineChatColors.size])
                canvas.drawPath(getLineChartPath(pointList), mPaint)
                if(isDrawPoint) {
                    //畫點-圓形點
                    setPointPaint(defaultLineChatColors[lineChartIndex % defaultLineChatColors.size])
                    (0 until pointList.size step 2).forEach {
                        canvas.drawCircle(pointList[it], pointList[it + 1], 15f, mPaint)
                    }
//                //畫點-方形點
//                canvas.drawPoints(pointList, mPaint)
                }
                if (isDrawLabel){
                    //畫Label
                    setTextPaint(Paint.Align.LEFT, Color.MAGENTA)
                    (0 until pointList.size step 2).forEach {
                        canvas.drawText(dataList[it/2].getLabel(), pointList[it] + 10, pointList[it + 1], mPaint)
                    }
                }
            }
        }

    }


    /**
     * 將轉換完的點 換成 path
     * */
    private fun getLineChartPath(pointList: FloatArray): Path {
        val path = Path()
        (0 until pointList.size step 2).forEach {
            if(it != 0){
                path.lineTo(pointList[it], pointList[it + 1])
            }else{
                path.moveTo(pointList[it], pointList[it + 1])
            }
        }
        return path
    }

    private fun convertDataListToUIList(dataList: ArrayList<out ChartData>): FloatArray {
        val pointList = ArrayList<Float>()
        dataList.forEach {
            pointList.addAll(convertDataPointToUIPoint(it))
        }
        return pointList.toFloatArray()
    }

    /**
     * 將Data的座標點轉換成UI上要繪製的Point
     * 1.Y軸刻度反向
     * 2.比例尺轉換
     * */
    private fun convertDataPointToUIPoint(data: ChartData): Array<Float> {
        return convertDataPointToUIPoint(data.getHorizontalAxisValue(), data.getVerticalAxisValue())
    }

    private fun convertDataPointToUIPoint(xData: Float, yData: Float): Array<Float> {
        val x: Float = (xData - mChartRequest.xMinValue) * xAxisScale + startPoint.x
        val y: Float = (endPoint.y - startPoint.y) - (yData - mChartRequest.yMinValue) * yAxisScale + startPoint.y
        return arrayOf(x, y)
    }

    //==================== Paint =======================
    private fun setLinePaint(@ColorInt color: Int){
        mPaint.color = color
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 6f
    }

    private fun setPointPaint(@ColorInt color: Int){
        mPaint.color = color
        mPaint.style = Paint.Style.FILL
        mPaint.strokeWidth = 30f
    }

    private fun setAxisPaint(){
        mPaint.color = getColor(R.color.base_line)
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 10f
    }

    private fun setTextPaint(align: Paint.Align, @ColorInt color: Int){
        mPaint.textSize = textSize
        mPaint.color = color
        mPaint.style = Paint.Style.FILL
        mPaint.textAlign = align
        mPaint.strokeWidth = 3f
    }
    //==================== interface =======================
    /**
     *@param lineData 線段資料
     * @param tag 標籤，用於刪除、找查項段資料，標籤重覆會取代現有資料
     * */
    fun addLineData(lineData: ArrayList<out ChartData>, tag: String) {
        mLineChartMap[tag] = lineData
        mLineChartOrder.add(tag)
        invalidate()
    }

    fun removeLineData(tag: String){
        mLineChartMap.remove(tag)
        mLineChartOrder.remove(tag)
        invalidate()
    }

    fun clearLineData(){
        mLineChartMap.clear()
        mLineChartOrder.clear()
        invalidate()
    }

    /**
     * 坐標軸是否可見
     * */
    fun setAxisVisible(isVisible: Boolean){
        isDrawAxis = isVisible
        invalidate()
    }

    fun setAxisLabelVisible(isVisible: Boolean){
        isDrawAxisLabel = isVisible
    }

    /**
     * 刻度是否可見
     * */
    fun setGirdScaleVisible(isVisible: Boolean){
        isDrawGridScale = isVisible
        invalidate()
    }

    /**
     * 座標點標籤是否可見
     * */
    fun setLabelVisible(isVisible: Boolean){
        isDrawLabel = isVisible
        invalidate()
    }

    /**
     * 座標點是否可見
     * */
    fun setPointVisible(isVisible: Boolean){
        isDrawPoint = isVisible
        invalidate()
    }

    fun setXAxisRange(minimum: Float, maximum:Float){
        mChartRequest.xMinValue = minimum
        mChartRequest.xMaxValue = maximum
        calculateAxisScale()
        invalidate()
    }

    fun setYAxisRange(minimum: Float, maximum:Float){
        mChartRequest.yMinValue = minimum
        mChartRequest.yMaxValue = maximum
        calculateAxisScale()
        invalidate()
    }


    //==================== base ==========================
    private fun getColor(color: Int): Int{
        return this.context.resources.getColor(color)
    }

    //==================== constructor ==========================
    init {
        mPaint.color = Color.BLACK    //設置畫筆顏色
        mPaint.style = Paint.Style.STROKE  //設置畫筆模式
        mPaint.strokeWidth = 10f         //設置畫筆寬度為10px

        defaultLineChatColors = context.resources.getIntArray(R.array.line_chart_color)

        mLineChartMap = HashMap()
        mLineChartOrder = ArrayList()
    }

    constructor(context: Context) : super(context){
        setChartStyle(context, null, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet): super(context, attrs){
        setChartStyle(context, attrs, 0, 0)
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int): super(context, attrs, defStyleAttr){
        setChartStyle(context, attrs, defStyleAttr, 0)
    }
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes:Int): super(context, attrs, defStyleAttr, defStyleRes){
        setChartStyle(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun setChartStyle(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes:Int){
        Log.w("TAG", "setChartStyle")
        if(attrs == null) return
        val typedArray: TypedArray = context.obtainStyledAttributes( attrs, R.styleable.HLineChartView, defStyleAttr, defStyleRes)
        (0 until typedArray.indexCount).forEach {
            val attr = typedArray.getIndex(it)
            when(attr){
                R.styleable.HLineChartView_hlc_format_x_min -> mChartRequest.xMinValue = typedArray.getFloat(attr, mChartRequest.xMinValue)
                R.styleable.HLineChartView_hlc_format_x_max -> mChartRequest.xMaxValue = typedArray.getFloat(attr, mChartRequest.xMaxValue)
                R.styleable.HLineChartView_hlc_format_x_gird_scale -> mChartRequest.xGridScale = typedArray.getFloat(attr, mChartRequest.xGridScale)
                R.styleable.HLineChartView_hlc_format_x_label -> {
                    mChartRequest.xUnitName = if (typedArray.getString(attr) == null) {
                        ""
                    } else {
                        typedArray.getString(attr)!!
                    }
                }
                R.styleable.HLineChartView_hlc_format_y_min -> mChartRequest.yMinValue = typedArray.getFloat(attr, mChartRequest.yMinValue)
                R.styleable.HLineChartView_hlc_format_y_max -> mChartRequest.yMaxValue = typedArray.getFloat(attr, mChartRequest.yMaxValue)
                R.styleable.HLineChartView_hlc_format_y_gird_scale -> mChartRequest.yGridScale = typedArray.getFloat(attr, mChartRequest.yGridScale)
                R.styleable.HLineChartView_hlc_format_y_label -> {
                    mChartRequest.yUnitName = if (typedArray.getString(attr) == null) {
                        ""
                    } else {
                        typedArray.getString(attr)!!
                    }
                }
                R.styleable.HLineChartView_hlc_axis_visible -> isDrawAxis = typedArray.getBoolean(attr, isDrawAxis)
                R.styleable.HLineChartView_hlc_axis_label_visible -> isDrawAxisLabel = typedArray.getBoolean(attr, isDrawAxisLabel)
                R.styleable.HLineChartView_hlc_gird_scale_visible -> isDrawGridScale = typedArray.getBoolean(attr, isDrawGridScale)
                R.styleable.HLineChartView_hlc_label_visible -> isDrawLabel = typedArray.getBoolean(attr, isDrawLabel)
                R.styleable.HLineChartView_hlc_point_visible -> isDrawPoint = typedArray.getBoolean(attr, isDrawPoint)
                R.styleable.HLineChartView_hlc_chart_padding_left -> {
                    defaultChartLeftPadding = typedArray.getDimensionPixelSize(attr, defaultChartLeftPadding.toInt()).toFloat()
                }
                R.styleable.HLineChartView_hlc_chart_padding_top -> {
                    defaultChartTopPadding = typedArray.getDimensionPixelSize(attr, defaultChartTopPadding.toInt()).toFloat()
                }
                R.styleable.HLineChartView_hlc_chart_padding_right -> {
                    defaultChartRightPadding = typedArray.getDimensionPixelSize(attr, defaultChartRightPadding.toInt()).toFloat()
                }
                R.styleable.HLineChartView_hlc_chart_padding_bottom -> {
                    defaultChartBottomPadding = typedArray.getDimensionPixelSize(attr, defaultChartBottomPadding.toInt()).toFloat()
                }
            }
        }
        typedArray.recycle()
    }
}