package com.example.sharenote

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

        backButton.setOnClickListener {
            onBackPressed()
        }

        btnUndo.setOnClickListener {
            drawingView.undo()
        }

        btnRedo.setOnClickListener {
            drawingView.redo()
        }

        btnColor.setOnClickListener {
            drawingView.setBrushColor(Color.RED)
        }

        btnBrush.setOnClickListener {
            drawingView.setSizeForBrush(25)//0-35
            drawingView.setBrushAlpha(100) //0-255
        }
        btnClearscreen.setOnClickListener {
            drawingView.clearDrawingBoard()
        }
        aiSendButton.setOnClickListener {
            // 테스트 로직(autoDraw로 그린 선만 노란색으로 바꾸기)
            drawingView.autoDraw()


            // 이미지를 압축하는 로직

            // AI 서버에 전송하는 로직
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


