package com.example.sharenote

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import com.mihir.drawingcanvas.drawingView

class PaintActivity : AppCompatActivity() {
    private lateinit var backButton : Button
    private lateinit var drawing_view : com.mihir.drawingcanvas.drawingView
    private lateinit var btn_undo : ImageButton
    private lateinit var btn_redo : ImageButton
    private lateinit var btn_color : ImageButton
    private lateinit var btn_brush : ImageButton
    private lateinit var btn_clearscreen : ImageButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_paint)

        backButton = findViewById(R.id.backButton)
        drawing_view = findViewById(R.id.drawing_view)
        btn_undo = findViewById(R.id.btn_undo)
        btn_redo = findViewById(R.id.btn_redo)
        btn_brush = findViewById(R.id.btn_brush)
        btn_color = findViewById(R.id.btn_color)
        btn_clearscreen = findViewById(R.id.btn_clearscreen)

        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        btn_undo.setOnClickListener {
            drawing_view.undo()
        }

        btn_redo.setOnClickListener {
            drawing_view.redo()
        }

        btn_color.setOnClickListener {
            drawing_view.setBrushColor(R.color.yellow)
        }

        btn_brush.setOnClickListener {
            drawing_view.setSizeForBrush(25)//0-35
            drawing_view.setBrushAlpha(100) //0-255
        }
        btn_clearscreen.setOnClickListener {
            drawing_view.clearDrawingBoard()
        }
        val alpha = drawing_view.getBrushAlpha()
        drawing_view.erase()
        val brushSize = drawing_view.getBrushSize()
        val brushColor = drawing_view.getBrushColor()

        val drawing = drawing_view.getDrawing()
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


