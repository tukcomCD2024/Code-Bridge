package com.example.sharenote

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.github.dhaval2404.colorpicker.ColorPickerDialog
import com.github.dhaval2404.colorpicker.model.ColorShape
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.mihir.drawingcanvas.drawingView



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

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paint)

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


        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
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
            // 테스트 로직(autoDraw로 그린 선만 노란색으로 바꾸기)
            // 이미지 업로드하고 해당 이미지 url 받아오기
            val imageUrl = drawingView.autoDraw()

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

        val alpha = drawingView.getBrushAlpha()
        drawingView.erase()
        val brushSize = drawingView.getBrushSize()
        val brushColor = drawingView.getBrushColor()

        val drawing = drawingView.getDrawing()
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
                val imageView = ImageView(this).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        300,
                        200
                    )
                    // 위치 가운데로
                    x = 600f
                    y = 700f
                    scaleType = ImageView.ScaleType.CENTER_CROP
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
                            // 초기 오프셋과 현재 터치 위치를 기반으로 뷰 이동
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


