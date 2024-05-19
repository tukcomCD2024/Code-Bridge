package com.mihir.drawingcanvas

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.annotation.IntRange
import com.example.sharenote.ImageResponse


import com.example.sharenote.RetrofitClient.apiService2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

import okhttp3.Call
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody

import okhttp3.RequestBody.Companion.toRequestBody

import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.UUID


// 배경 지식 코너(그리기 작업 핵심 삼총사)
// Paint 객체 : 스타일과 색상 관리 (색상, 굵기, 투명도 등)

// Path 객체 : 복잡한 그림이나 선을 정의할 때 사용. 여러개의 직선이나 곣너 세그먼트를
// 결합하여 형태를 만들 수 있다. 사용자가 그린 선을 추적하고 이를 저장해서 Canvas에 그릴 때 사용

// Bitmap 이란? : 픽셀 데이터의 배열을 표현, 이미지를 메모리에 저장하고 처리하는데 사용
// 안드로이드에서는 Bitmap 객체를 사용하여 Canvas 객체에 그릴 수 있음
class drawingView(context: Context, attrs: AttributeSet) : View(context,attrs){

    // 현재 그리고 있는 경로
    private var mDrawPath:CustomPath?=null
    // 사용자가 그림을 그리는 캔버스에 해당하는 비트맵
    private var mCanvasBitmap:Bitmap?=null
    // 도형을 그리는데 사용되는 Paint 객체(스타일 지정)
    private var mDrawPaint:Paint?=null
    // 캔버스에 그리기 작업을 할 때 사용되는 페인트 객체(비트맵 그리기)
    private var mCanvasPaint:Paint?=null
    // 브러쉬 크기
    private var mBrushSize:Int = 0
    // 브러쉬 색상
    private var currentColor = Color.BLACK
    // 실제 그리기 작업이 이루어지는 캔버스 객체
    private var canvas: Canvas?=null
    // 브러쉬 투명도
    private var mAlpha:Int = 50
    // 그려진 모든 경로를 저장하는 배열
    private var mPaths = ArrayList<CustomPath>()
    // 실행 취소된 경로를 임시로 저장하는 배열
    private var mUndoPath = ArrayList<CustomPath>()

    // autoDraw 모드로 실행되는 선들 저장
    private var autoDrawPath = ArrayList<CustomPath>()

    // 0을 기본 모드, 1을 autoDraw 모드로 설정
    private var drawingMode:Int = 0;

    private var autoDrawUndoPath = ArrayList<CustomPath>()

    // 클래스가 인스턴스화 될 때 호출되는 초기화 블록
    init {
        setUpDrawing()
    }

    fun getDrawingMode(): Int {
        return drawingMode
    }

    fun setDrawingMode(mode: Int) {
        drawingMode = mode
    }

    private fun setUpDrawing() {
        mDrawPaint= Paint()
        mDrawPath=CustomPath(currentColor,mBrushSize,mAlpha)
        mDrawPaint!!.color = currentColor
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.alpha = mAlpha
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        // DITHER_FLAG를 사용해 캔버스 페인트에 디더링을 활성화합니다.(???)
        // 디더링이 뭐냐? -> 색상 전환을 부드럽게 표현하여 시각적 품질 향상 시키는 기술
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        mBrushSize = 20
    }
    // 뷰의 크기가 변경될 때 호출됩니다.
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888)
        canvas =  Canvas(mCanvasBitmap!!)
    }

    // 설명 : 캔버스는 그림을 그리기 위한 도구이고 그 결과가 비트맵에 저장됩니다.
    @SuppressLint("SuspiciousIndentation")
    fun autoDraw() : String {
//autoDraw로 그린 선만 전부 노란색으로 바꾸기 성공 코드
//        for(path in autoDrawPath){
//            path.color = Color.YELLOW
//        }
//        invalidate()


        // autoDraw로 그린 선으로 만들어진 비트맵을 압축하기
        // 새로운 비트맵 생성
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas?.drawColor(Color.WHITE)

        // 임시 Paint 객체 생성
        val paint = Paint().apply {
            color = currentColor
            style = Paint.Style.STROKE
            strokeJoin = Paint.Join.ROUND
            strokeCap = Paint.Cap.ROUND
            strokeWidth = mBrushSize.toFloat()
            alpha = mAlpha
        }

        // autoDrawPath에 저장된 모든 Path를 새로운 캔버스에 그림
        for (path in autoDrawPath) {
            paint.color = path.color
            paint.strokeWidth = path.brushThickness.toFloat()
            paint.alpha = path.alpha
            canvas.drawPath(path, paint)
        }


        // 랜덤한 파일 이름을 생성
        val fileName = UUID.randomUUID().toString() + ".png"
        // 비트맵을 멀티파트 바디 파트로 변환
        val imagePart = convertBitmapToMultipartBodyPart(bitmap, "multipartFile", fileName)
        val file = File(context.filesDir, fileName)
        val outputStream: OutputStream = FileOutputStream(file)
        // 비트맵을 PNG 형식으로 압축
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.close()
        //Toast.makeText(context, "이미지 저장 완료", Toast.LENGTH_SHORT).show()
        Log.e("DrawingView", "이미지 저장 완료: $fileName")

        // 3. 이미지 업로드 API 호출
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService2.uploadImage(imagePart)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        //Toast.makeText(context, "이미지 업로드 성공!", Toast.LENGTH_SHORT).show()
                        Log.e("DrawingView", "이미지 업로드 성공! ${response.body()!!.image_url})")
                        // 비트맵을 멀티파트 바디 파트로 변환
                        val imagePart = convertBitmapToMultipartBodyPart(bitmap, "multipartFile", "drawing.png")
                        Log.e("imageUpload", "이미지 업로드 성공! ${response.body()!!.image_url})")
                        return@withContext fileName.toString()

                    } else {
                        Log.e("DrawingView", "이미지 업로드 실패: ${response.message()}")
                        //Toast.makeText(context, "이미지 업로드 실패: ${response.message()}", Toast.LENGTH_SHORT).show()
                        return@withContext "error"
                    }
                }
            } catch (t: Throwable) {
                withContext(Dispatchers.Main) {
                    Log.e("DrawingView", "네트워크 오류: ${t.message}")
                    //Toast.makeText(context, "네트워크 오류: ${t.message}", Toast.LENGTH_SHORT).show()
                    Log.e("imageUpload", "네트워크 오류: ${t.message}")
                    return@withContext "error"
                }
            }
        }

        return "error"
    }

    private fun convertBitmapToMultipartBodyPart(bitmap: Bitmap, paramName: String, fileName: String): MultipartBody.Part {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        byteArrayOutputStream.close()

        val requestBody = byteArray.toRequestBody("image/png".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(paramName, fileName, requestBody)
    }

    private fun Bitmap.toByteByteArray(): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }


    // 뷰를 다시 그려야 할 때 호출됨.
    // ex) View가 처음 로딩, 뷰의 크기 변경, 뷰 내의 데이터 변경 -> 그래픽 업데이트 필요한 상황
    override fun onDraw(canvas: Canvas) {
        if (canvas != null) {
            super.onDraw(canvas)
        }
        // 0,0 경로에 비트맵 그리기
        canvas?.drawBitmap(mCanvasBitmap!!,0f,0f,mCanvasPaint)

        // 경로 그리기
        // 이전에 그린 모든 경로(mPaths)를 순회 하면서 캔버스에 그린다.
        for(path in mPaths){
            mDrawPaint!!.strokeWidth = path.brushThickness.toFloat()
            mDrawPaint!!.color=  path.color
            mDrawPaint!!.alpha = path.alpha

            // 매개변수로 path 와 Paint 객체(색깔, 굵기, 투명도 등의 스타일)를 받음
            canvas?.drawPath(path,mDrawPaint!!)
        }

        // autoDraw로 그린 선이 안남고 사라지길래 추가해 봄
        for(path in autoDrawPath){
            mDrawPaint!!.strokeWidth = path.brushThickness.toFloat()
            mDrawPaint!!.color=  path.color
            mDrawPaint!!.alpha = path.alpha
            canvas?.drawPath(path,mDrawPaint!!)
        }

        // 현재 그리기 경로 그리기
        // mDrawPath가 현재 그리고 있는 경로야(실시간을 보장 한다는 듯)
        if(!mDrawPath!!.isEmpty){
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness.toFloat()
            mDrawPaint!!.color=  mDrawPath!!.color
            mDrawPaint!!.alpha = mDrawPath!!.alpha
            canvas?.drawPath(mDrawPath!!,mDrawPaint!!)
        }

    }

    // 사용자가 화면 터치 시 발생하는 다양한 행동을 감지하고 해당 해동에 따라 적절한
    // 로직을 수행.
    @SuppressLint("ClickableViewAccessibility")
    // 터치 이벤트 처리 시 true, 실패 시 false
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            // 사용자가 화면을 처음 터치할 때 발생
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = currentColor
                mDrawPath!!.brushThickness = mBrushSize
                mDrawPath!!.alpha = mAlpha
                mDrawPath!!.reset()
                if (touchX != null) {
                    if (touchY != null) {
                        // 그리기 시작점으로 이동(펜으로 화면에 점 찍을 때 발생하는거지)
                        mDrawPath!!.moveTo(touchX,touchY)
                    }
                }
            }
            // 사용자가 화면을 터치한 채로 움직일 때 발생
            MotionEvent.ACTION_MOVE ->{
                if (touchX != null) {
                    if (touchY != null) {
                        // 현재 터치 위치까지 선을 그림
                        mDrawPath!!.lineTo(touchX,touchY)
                    }
                }
            }

            // 사용자가 화면에서 손을 뗄 때 발생
            MotionEvent.ACTION_UP ->{
                // 현재 그리고 있는 경로를 mPaths에 추가
                // mDrawPath는 방금 그린 선이야
                if(drawingMode == 0)
                    mPaths.add(mDrawPath!!)
                else if(drawingMode == 1)
                    autoDrawPath.add(mDrawPath!!)
                // 다음 그리기 작업을 위해 새로운 Path 객체 생성
                mDrawPath = CustomPath(currentColor,mBrushSize,mAlpha)
            }

            else-> return false
        }
        // 화면을 다시 그림(갱신) : 즉각적으로 사용자가 그린 선이 화면에 출력됨.
        invalidate()

        return true

    }
    /**
     * Helps setting the thickness of brush stroke
     * @param newSize Int 0-200
     */
    @SuppressLint("SupportAnnotationUsage")
    @IntRange(from = 0, to = 50)
    fun setSizeForBrush(newSize: Int){
//        mBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
//            newSize.toFloat(),resources.displayMetrics).toInt()
//        mDrawPaint!!.strokeWidth = mBrushSize.toFloat()
        mBrushSize = newSize
        mDrawPaint!!.strokeWidth = mBrushSize.toFloat()
    }

    fun getBrushSize(): Int {
        return mBrushSize
    }
    /**
     * Helps setting the transparency of brush stroke
     * @param newAlpha Int 0-255
     */
    @SuppressLint("SupportAnnotationUsage")
    @IntRange(from = 0,to = 255)
    fun setBrushAlpha(newAlpha:Int){
        mAlpha = newAlpha
        mDrawPaint!!.alpha = newAlpha
    }

    fun getBrushAlpha(): Int {
        return mAlpha
    }

    /**
     * Helps to set the color of brush
     * @param color Int
     */
    fun setBrushColor(color: Int){
        currentColor = color
        mDrawPaint!!.color = color
    }
    /**
     * Helps to set the color of brush
     * @return color Int
     */
    fun getBrushColor(): Int {
        return currentColor
    }

    // you can pass a color according to your background and the default is white
    /**
     * Helps to set the color of brush
     * @param color Int default white
     */
    fun erase(colorBackground: Int= Color.WHITE){
        mAlpha = 50
        mDrawPaint!!.alpha = 50
        currentColor = colorBackground
        mDrawPaint!!.color = colorBackground
    }
    /**
     * will undo strokes, can be changed by redo()
     */
    fun undo(){
        if(drawingMode == 0){
            // mPaths에 아무것도 없으면 그냥 리턴
            if (mPaths.size == 0){
                return
            }
            // mPaths에 마지막으로 추가된 놈 임시 보호
            mUndoPath.add(mPaths[mPaths.size -1])
            // mPaths에서 마지막 놈 제거
            mPaths.removeAt(mPaths.size -1)
            // 다시 그리기
            invalidate()
        }
        else if(drawingMode == 1){
            if (autoDrawPath.size == 0){
                return
            }
            autoDrawUndoPath.add(autoDrawPath[autoDrawPath.size -1])
            autoDrawPath.removeAt(autoDrawPath.size -1)
            invalidate()
        }

    }

    /**
     * will redo the undo-ed strokes
     */
    fun redo(){
        if(drawingMode == 0){
            // mUndoPath에 아무것도 없으면 그냥 리턴
            if (mUndoPath.size == 0){
                return
            }
            // 임시 보관한 놈 다시 mPaths에 추가
            mPaths.add(mUndoPath[mUndoPath.size -1])
            // 임시 보관 해제
            mUndoPath.removeAt(mUndoPath.size -1)
            // 다시 그리기
            invalidate()
        }
        else if(drawingMode == 1){
            if (autoDrawPath.size == 0){
                return
            }
            autoDrawPath.add(autoDrawUndoPath[autoDrawUndoPath.size -1])
            autoDrawUndoPath.removeAt(autoDrawUndoPath.size -1)
            invalidate()
        }
    }
    /**
     * will remove all the stores but not those saved in redo()
     */
    fun clearDrawingBoard(){
        autoDrawPath.clear()
        // 싹 Path들 날려
        mPaths.clear()
        // 다시 그리기
        invalidate()
    }

    fun autoDrawClear() {
        autoDrawPath.clear()
        invalidate()
    }

    fun getDrawing(): ArrayList<CustomPath> {
        return mPaths
    }
    inner class CustomPath(var color:Int , var brushThickness:Int, var alpha:Int) : Path() {


    }


}