package com.example.sharenote

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.drawToBitmap
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.sharenote.RetrofitClient.apiService
import com.example.sharenote.RetrofitClient.apiService2
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.mihir.drawingcanvas.drawingView
import com.rajat.pdfviewer.util.saveTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.min


class PaintActivity : AppCompatActivity() {
    private lateinit var backButton : Button
    //
    private lateinit var drawingView : drawingView
    private lateinit var btnUndo : ImageButton
    private lateinit var btnRedo : ImageButton
    private lateinit var btnColor : ImageButton
    private lateinit var btnBrush : ImageButton
    private lateinit var btnClearscreen : ImageButton

    private lateinit var autoDrawButton : FloatingActionButton
    private lateinit var aiSendButton : Button
    private lateinit var imageViewFixButton: Button

    private lateinit var pdfButton: FloatingActionButton
    private lateinit var plusButton: FloatingActionButton

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data // 선택된 PDF 파일의 URI
            uri?.let {
                openPdfViewer(uri.toString())
            }

        }
    }



    private lateinit var imageView: ImageView

    private val startPdfViewerForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // 성공적으로 PDF를 선택했을 때 처리
            val pdfUri = result.data?.getStringExtra("selected_pdf_uri")
            pdfUri?.let {
                // PDF 파일의 URI 사용
                val imageUri = Uri.parse(pdfUri)
                drawingView.background = Drawable.createFromStream(
                    contentResolver.openInputStream(imageUri), imageUri.toString()
                )
            }
        }
    }

    private lateinit var imageViewList: ArrayList<ImageView>

    private fun openPdfViewer(pdfUri: String) {
        val intent = PdfViewerActivity.launchPdfFromPath(
            context = this,
            path = pdfUri,
            pdfTitle = "View PDF",
            saveTo = saveTo.ASK_EVERYTIME,
            fromAssets = false
        )
        startPdfViewerForResult.launch(intent)
    }

    companion object {
        const val IMAGE_URL = "image_url"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paint)
        imageViewList = ArrayList()

        backButton = findViewById(R.id.backButton)
        drawingView = findViewById(R.id.drawing_view)
        btnUndo = findViewById(R.id.btn_undo)
        btnRedo = findViewById(R.id.btn_redo)
        btnBrush = findViewById(R.id.btn_brush)
        btnColor = findViewById(R.id.btn_color)
        btnClearscreen = findViewById(R.id.btn_clearscreen)

        autoDrawButton = findViewById(R.id.autoDrawButton)
        aiSendButton = findViewById(R.id.aiButton)
        imageViewFixButton = findViewById(R.id.imageViewFixButton)

        pdfButton = findViewById(R.id.pdfButton)
        plusButton = findViewById(R.id.plusButton)


        backButton.setOnClickListener {
            // AlertDialog를 생성하여 사용자에게 확인 요청
            AlertDialog.Builder(this)
                .setTitle("이미지 업로드") // 다이얼로그 제목
                .setMessage("이미지 업로드 하시겠습니까?") // 다이얼로그 메시지
                .setNegativeButton("네") { dialog, which ->
                    // "Yes" 버튼 클릭 시, 원래 backButton의 로직 실행
                    imageViewFixButton.performClick()

                    val finalBitmap = Bitmap.createBitmap(drawingView.width, drawingView.height, Bitmap.Config.ARGB_8888)
                    val finalCanvas = Canvas(finalBitmap)

                    // 배경 그리기
                    if (drawingView.background != null) {
                        finalCanvas.drawBitmap(drawingView.background.toBitmap(drawingView.width, drawingView.height), 0f, 0f, null)
                    } else {
                        // 배경이 null인 경우, 흰색 비트맵 생성 및 그리기
                        val whiteBitmap = Bitmap.createBitmap(drawingView.width, drawingView.height, Bitmap.Config.ARGB_8888)
                        whiteBitmap.eraseColor(Color.WHITE)
                        finalCanvas.drawBitmap(whiteBitmap, 0f, 0f, null)
                        whiteBitmap.recycle() // 사용 후 메모리 해제
                    }

                    // path 그리기
                    drawingView.getDrawing().forEach { path ->
                        val paint = Paint().apply {
                            color = path.color
                            strokeWidth = path.brushThickness.toFloat()
                            style = Paint.Style.STROKE
                            strokeJoin = Paint.Join.ROUND
                            strokeCap = Paint.Cap.ROUND
                            alpha = path.alpha
                        }
                        finalCanvas.drawPath(path, paint)
                    }

                    // imageView 그리기
                    imageViewList.forEach { imageView ->
                        val bitmap = imageView.drawable.toBitmap()
                        finalCanvas.drawBitmap(bitmap, imageView.x, imageView.y, null)
                    }



                    val fileName = UUID.randomUUID().toString() + ".png"
                    // 비트맵을 멀티파트 바디 파트로 변환
                    val imagePart = convertBitmapToMultipartBodyPart(finalBitmap, "multipartFile", fileName)

                    val resultIntent = Intent()
                    val intent = Intent(this, PageActivity::class.java)

                    // 파일을 서버로 업로드하는 로직 (Retrofit 등 사용)
                    lifecycleScope.launch {
                        try {
                            val response = apiService.uploadImage(imagePart)

                            withContext(Dispatchers.Main) {
                                if (response.isSuccessful) {
                                    val imageUrl = response.body()!!.image_url
                                    intent.putExtra(IMAGE_URL, imageUrl)
                                    startActivity(intent)
                                    finish()
                                    /*resultIntent.putExtra("imageUrl", imageUrl)
                                    setResult(Activity.RESULT_OK, resultIntent)
                                    Log.d("PaintActivity", "{$imageUrl}")
                                    super.finish()*/
                                } else {
                                    Log.e("PaintActivity", "Error: ${response.errorBody()}")
                                    setResult(Activity.RESULT_OK, resultIntent)
                                    super.finish()
                                }
                            }

                        } catch (e: Exception) {
                            Log.e("PaintActivity", "Exception: ${e.message}")
                        }
                    }
                    finish() // 예를 들어 액티비티를 종료
                }
                .setPositiveButton("아니요") { dialog, which ->
                    // "No" 버튼 클릭 시, 아무 일도 하지 않음
                    dialog.dismiss()
                }
                .show() // 다이얼로그 표시
            // 원래 이거 밑의 한줄코드였음
            //onBackPressed()
        }

        btnUndo.setOnClickListener {
            drawingView.undo()
        }

        btnRedo.setOnClickListener {
            drawingView.redo()
        }

        btnColor.setOnClickListener {
            val initialColor = drawingView.getBrushColor()

            ColorPickerDialog
                .Builder(this)            			// 현재 Activity를 Context로 사용
                .setTitle("Choose Color")         	// 다이얼로그 제목
                .setColorShape(ColorShape.CIRCLE) 	// 색상 모양 설정 (CIRCLE 또는 SQUARE)
                .setDefaultColor(initialColor)    	// 초기 색상 설정
                .setColorListener { color, _ ->
                    // 선택된 색상으로 브러시 색상 설정
                    drawingView.setBrushColor(color)
                }
                .show()                           	// 다이얼로그 표시
        }

        btnBrush.setOnClickListener {


            val dialogView = LayoutInflater.from(this).inflate(R.layout.brush_settings_dialog, null)

            val brushSizeSlider = dialogView.findViewById<Slider>(R.id.brushSizeSlider)
            val brushAlphaSlider = dialogView.findViewById<Slider>(R.id.brushAlphaSlider)

            // 현재 브러시 사이즈와 투명도 값을 슬라이더에 설정
            brushSizeSlider.value = drawingView.getBrushSize().toFloat()
            // 둘이 같아야 하는거지
            Log.e("GetbrushSize", drawingView.getBrushSize().toString())

            brushAlphaSlider.value = drawingView.getBrushAlpha().toFloat()

            // 다이얼로그 생성
            val dialog = AlertDialog.Builder(this)
                .setTitle("Brush 설정")
                .setView(dialogView)
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Apply", null)
                .create()


            dialog.setOnShowListener {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener {
                    val brushSize = brushSizeSlider.value.toInt()
                    val brushAlpha = brushAlphaSlider.value.toInt()
                    Log.e("brushSize" , brushSize.toString())

                    // 입력된 값으로 브러시 설정을 업데이트
                    drawingView.setSizeForBrush(brushSize)
                    Log.e("GetBrushSizeFromDrawingView", drawingView.getBrushSize().toString())
                    drawingView.setBrushAlpha(brushAlpha)

                    // 모든 설정 후 다이얼로그 닫기
                    dialog.dismiss()
                }
            }

            // 다이얼로그 화면에 표시
            dialog.show()
        }
        btnClearscreen.setOnClickListener {
            drawingView.clearDrawingBoard()
        }
        aiSendButton.setOnClickListener {
            //




            // autoDraw 모드 강제 해제
            autoDrawButton.performClick()

            aiSendButton.visibility = View.GONE
            imageViewFixButton.visibility = View.VISIBLE
            // 테스트 로직(autoDraw로 그린 선만 노란색으로 바꾸기)
            // 이미지 업로드하고 해당 이미지 url 받아오기
            val url = drawingView.autoDraw()


            lifecycleScope.launch {
                try {
                    // API 호출
                    val response = apiService2.aiPickImages(url)

                    // 메인 스레드에서 UI 업데이트
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful && response.body() != null) {
                            // 서버로부터 받은 이미지 URL 리스트 처리
                            val urlList = response.body()!!.imageUrls // 이거 리스트야

                            // Intent 생성 및 시작
                            val intent = Intent(this@PaintActivity, ImageSelect::class.java)
                            intent.putStringArrayListExtra("urlList", ArrayList(urlList))
                            startActivityForResult(intent, 1520)
                        } else {
                            Log.e("PaintActivity", "Error: ${response.errorBody()}")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("PaintActivity", "Exception: ${e.message}")
                    }

                }
            }

            // 이미지 url을 서버로 전송하고 서버에서 받아온 이미지 url로 이미지 띄우기
            // 3 초간 정지 AI 서버에 요청한 척
            Thread.sleep(3000)

            // 서버에서 받아온 JSON 객체에서 6개의 url 꺼내서 다음 액티비티로 전달
            // list 만들어줘
            val urlList = ArrayList<String>()
            urlList.add("https://sharenotebucket.s3.ap-northeast-2.amazonaws.com/apple-line.png")
            urlList.add("https://sharenotebucket.s3.ap-northeast-2.amazonaws.com/airplane-outline.png")
            urlList.add("https://sharenotebucket.s3.ap-northeast-2.amazonaws.com/bag-line.png")
            urlList.add("https://sharenotebucket.s3.ap-northeast-2.amazonaws.com/bath-outline.png")
            urlList.add("https://sharenotebucket.s3.ap-northeast-2.amazonaws.com/bed-outline.png")
            urlList.add("https://sharenotebucket.s3.ap-northeast-2.amazonaws.com/calendar-line.png")



            val intent = Intent(this, ImageSelect::class.java)
            intent.putStringArrayListExtra("urlList", urlList)
            // 100은 고유한 코드
            startActivityForResult(intent, 100)  // IMAGE_SELECT_REQUEST_CODE는 상수


        }


        autoDrawButton.setOnClickListener {
            //View Fix Button 강제 클릭해서 Gone
            imageViewFixButton.performClick()

            if (drawingView.getDrawingMode() == 0) {
                drawingView.setDrawingMode(1)
                Toast.makeText(this, "Auto Draw Mode : " + drawingView.getDrawingMode(), Toast.LENGTH_SHORT).show()
                aiSendButton.visibility = View.VISIBLE
                autoDrawButton.backgroundTintList = resources.getColorStateList(R.color.blue)
                drawingView.setBrushColor(Color.BLUE)
            } else {
                drawingView.setDrawingMode(0)
                Toast.makeText(this, "Auto Draw Mode : " + drawingView.getDrawingMode(), Toast.LENGTH_SHORT).show()
                aiSendButton.visibility = View.GONE
                autoDrawButton.backgroundTintList = resources.getColorStateList(R.color.white)
                drawingView.setBrushColor(Color.RED)
            }


        }

        plusButton.setOnClickListener{
            val isVisible = findViewById<FloatingActionButton>(R.id.autoDrawButton).visibility == View.VISIBLE
            toggleButton(findViewById(R.id.autoDrawButton), !isVisible, 150)
            toggleButton(findViewById(R.id.pdfButton), !isVisible, 300) // 딜레이를 다르게 주어 순차적으로 나타나게 함
        }



        pdfButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            filePickerLauncher.launch(intent)
        }

        val alpha = drawingView.getBrushAlpha()
        drawingView.setBrushColor(R.color.black)
        drawingView.setBrushAlpha(180)
        val brushSize = drawingView.getBrushSize()
        val brushColor = drawingView.getBrushColor()

        val drawing = drawingView.getDrawing()
    }
    fun captureScreen(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }



    fun prepareFilePart(file: File): MultipartBody.Part {
        val requestFile = file.asRequestBody("image/png".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("image", file.name, requestFile)
    }

    private fun convertBitmapToMultipartBodyPart(bitmap: Bitmap, paramName: String, fileName: String): MultipartBody.Part {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        byteArrayOutputStream.close()

        val requestBody = byteArray.toRequestBody("image/png".toMediaTypeOrNull())
        return MultipartBody.Part.createFormData(paramName, fileName, requestBody)
    }

    // 플로팅 버튼 2개 위로 짜라란 하면서 등장시켜주는 함수
    fun toggleButton(button: FloatingActionButton, show: Boolean, delay: Long) {
        if (show) {
            button.visibility = View.VISIBLE
            button.translationY = 100f // 시작 위치
            button.alpha = 0.0f
            button.animate()
                .translationY(0f)
                .alpha(1.0f)
                .setDuration(300)
                .setStartDelay(delay)
                .start()
        } else {
            button.animate()
                .translationY(100f)
                .alpha(0.0f)
                .setDuration(300)
                .withEndAction { button.visibility = View.GONE }
                .start()
        }
    }

    // override 없어도 되나??
    // 다른 액티비티에서 돌아올 때 requestCode가 100 인 경우에 대해서 처리

    @SuppressLint("ClickableViewAccessibility")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            val selectedUrl = data?.getStringExtra("selectedImageUrl")
            Log.e("PaintActivity", "Selected Image URL: $selectedUrl")
            // 여기서 선택된 이미지 URL로 필요한 작업을 수행합니다.
            if (selectedUrl != null) {
                // autoDraw 선들 삭제하기
                drawingView.autoDrawClear()


                // 동적으로 ImageView 생성
                // 위치 가운데로 좀 와라
                imageView = ImageView(this).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        300,
                        200
                    )
                    // 위치 가운데로
                    x = 600f
                    y = 700f
                    scaleType = ImageView.ScaleType.CENTER_INSIDE
                    adjustViewBounds = true
                }

                // 찐 동적으로 수행하기 위한 코드
                // 밑의 로직은 잘 모르겠음, 잘 동작 하니까 그냥 쓰려고
                imageView.setOnTouchListener { view, event ->
                    val action = event.action

                    when (action) {
                        MotionEvent.ACTION_DOWN -> {
                            // 드래그 시작할 때 초기 위치 기억
                            val offsetX = event.rawX - view.x
                            val offsetY = event.rawY - view.y
                            view.tag = floatArrayOf(offsetX, offsetY)
                        }
                        MotionEvent.ACTION_MOVE -> {
                            // 드래그 중일 때 현재 위치로 이동
                            val offsets = view.tag as FloatArray
                            view.x = event.rawX - offsets[0]
                            view.y = event.rawY - offsets[1]
                        }
                        MotionEvent.ACTION_UP -> {
                            // 필요하다면 여기서 드래그 종료 처리
                        }
                    }
                    true // 터치 이벤트가 처리되었음을 나타냄
                }

                imageViewFixButton.setOnClickListener {
                    // 버튼 클릭 시 ImageView 위치 고정
                    // 위치 고정 로직은 특별히 필요하지 않습니다. 사용자가 원하는 위치에 ImageView가 있고,
                    // 더 이상 이동하지 않도록 하려면 이벤트 핸들러를 비활성화하면 됩니다.
                    imageView.setOnTouchListener(null) // 드래그 비활성화
                    imageViewFixButton.visibility = View.GONE // 버튼 비활성화
                    imageViewList.add(imageView)
                }


                // 레이아웃에 ImageView 추가
                val layout = findViewById<ConstraintLayout>(R.id.paintLayout) // 미리 XML에 LinearLayout 등의 레이아웃을 정의해 두어야 합니다.
                layout.addView(imageView)

                // Glide를 사용하여 이미지 로드
                Glide.with(this)
                    .load(selectedUrl)
                    .into(imageView)
            }
        }


    }


//        backButton = findViewById(R.id.backButton)


//        backButton.setOnClickListener {
//            val intent = Intent(this, MainActivity::class.java)
//            startActivity(intent)
//        }

//        drawingView = findViewById(R.id.drawingView)
//        drawingView.setBrushAlpha(120)
//        drawingView.setBrushColor(R.color.black)
//        drawingView.setSizeForBrush(12) // takes value from 0-200
//        drawingView.undo()
//        drawingView.redo()
//        drawingView.erase(Color.WHITE) // give the color same as the background color
//        drawingView.clearDrawingBoard()
//
//        //getter methods
//        val alpha = drawingView.getBrushAlpha() // returns INT
//        val brushSize = drawingView.getBrushSize() // returns INT
//        val brushColor = drawingView.getBrushColor() // returns INT
//
//        val drawing = drawingView.getDrawing() // returns ArrayList<CustomPath>(); where CustomPath(var color:Int , var brushThickness:Int, var alpha:Int)





    }


