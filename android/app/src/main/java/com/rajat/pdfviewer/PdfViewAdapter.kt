package com.rajat.pdfviewer

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.example.sharenote.databinding.ListItemPdfPageBinding
import com.rajat.pdfviewer.util.CommonUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal class PdfViewAdapter(
    private val context: Context,
    private val renderer: PdfRendererCore,
    private val pageSpacing: Rect,
    private val enableLoadingForPages: Boolean,
    private val selectPdf: (Int) -> Unit,  // selectPdf 함수를 추가합니다.
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<PdfViewAdapter.PdfPageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfPageViewHolder =
        PdfPageViewHolder(ListItemPdfPageBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun getItemCount(): Int = renderer.getPageCount()



    override fun onBindViewHolder(holder: PdfPageViewHolder, position: Int) {
        holder.bind(position)
        holder.itemView.setOnClickListener {
            selectPdf(position)
            // 클릭 이벤트는 동작하네
            Log.e("Clicked" , "Clicked")
        }
    }



    // PdfViewAdapter에 getPdfViewByPage 메서드를 추가합니다.
    fun getPdfViewByPage(pageNumber: Int): PdfRendererView? {
        // 페이지 번호에 해당하는 ViewHolder를 찾습니다.
        val viewHolder = recyclerView.findViewHolderForAdapterPosition(pageNumber)
        // ViewHolder의 itemView를 PdfRendererView로 캐스팅하여 반환합니다.
        return viewHolder?.itemView as? PdfRendererView

    }



    inner class PdfPageViewHolder(private val itemBinding: ListItemPdfPageBinding) : RecyclerView.ViewHolder(itemBinding.root) {
//        fun bind(position: Int) {
////            with(itemBinding) {
////                handleLoadingForPage(position)
////                if (pageView.width == 0 || pageView.height == 0) {
////                    pageView.post { bind(position) }  // Delay the binding if the layout isn't ready.
////                    return
////                }
////
////                val pageDimensions = renderer.getPageDimensions(position)
////                val aspectRatio = pageDimensions.width.toFloat() / pageDimensions.height.toFloat()
////                val width = pageView.width
////                val height = (width / aspectRatio).toInt()
////
////                // Use cached or dynamically calculated height here as per your original logic.
////                val bitmap = CommonUtils.Companion.BitmapPool.getBitmap(width, height)
////                updateLayoutParams(height)
////
////                renderer.renderPage(position, bitmap) { success, pageNo, renderedBitmap ->
////                    if (success && pageNo == position) {
////                        CoroutineScope(Dispatchers.Main).launch {
////                            itemBinding.pageView.setImageBitmap(renderedBitmap ?: bitmap)
////                            applyFadeInAnimation(pageView)
////                            pageLoadingLayout.pdfViewPageLoadingProgress.visibility = View.GONE
////                        }
////                    } else {
////                        CommonUtils.Companion.BitmapPool.recycleBitmap(bitmap)
////                    }
////                }
////            }
//
//            with(itemBinding) {
//                // Show a placeholder or loading indicator
//                pageLoadingLayout.pdfViewPageLoadingProgress.visibility = View.VISIBLE
//
//                // Fetch dimensions asynchronously
//                renderer.getPageDimensionsAsync(position) { size ->
//                    val aspectRatio = size.width.toFloat() / size.height.toFloat()
//                    val width = pageView.width
//                    val height = (width / aspectRatio).toInt()
//
//                    // Update layout params based on the actual page size
//                    updateLayoutParams(height)
//
//                    // Now load the actual page
//                    val bitmap = CommonUtils.Companion.BitmapPool.getBitmap(width, height)
//                    renderer.renderPage(position, bitmap) { success, pageNo, renderedBitmap ->
//                        if (success && pageNo == position) {
//                            pageView.setImageBitmap(renderedBitmap ?: bitmap)
//                            applyFadeInAnimation(pageView)
//                            pageLoadingLayout.pdfViewPageLoadingProgress.visibility = View.GONE
//                        } else {
//                            CommonUtils.Companion.BitmapPool.recycleBitmap(bitmap)
//                        }
//                    }
//                }
//            }
//
//        }
//
//        private fun ListItemPdfPageBinding.updateLayoutParams(height: Int) {
//            root.layoutParams.height = height
//            (root.layoutParams as? ViewGroup.MarginLayoutParams)?.setMargins(
//                pageSpacing.left, pageSpacing.top, pageSpacing.right, pageSpacing.bottom
//            )
//        }


        fun bind(position: Int) {
            with(itemBinding) {
                pageLoadingLayout.pdfViewPageLoadingProgress.visibility = if (enableLoadingForPages) View.VISIBLE else View.GONE

                renderer.getPageDimensionsAsync(position) { size ->
                    val width = pageView.width.takeIf { it > 0 } ?: context.resources.displayMetrics.widthPixels
                    val aspectRatio = size.width.toFloat() / size.height.toFloat()
                    val height = (width / aspectRatio).toInt()

                    updateLayoutParams(height)

                    val bitmap = CommonUtils.Companion.BitmapPool.getBitmap(width, maxOf(1, height))
                    renderer.renderPage(position, bitmap) { success, pageNo, renderedBitmap ->
                        if (success && pageNo == position) {
                            CoroutineScope(Dispatchers.Main).launch {
                                pageView.setImageBitmap(renderedBitmap ?: bitmap)
                                applyFadeInAnimation(pageView)
                                pageLoadingLayout.pdfViewPageLoadingProgress.visibility = View.GONE
                            }
                        } else {
                            CommonUtils.Companion.BitmapPool.recycleBitmap(bitmap)
                        }
                    }
                }
            }
        }

        private fun ListItemPdfPageBinding.updateLayoutParams(height: Int) {
            root.layoutParams = root.layoutParams.apply {
                this.height = height
                (this as? ViewGroup.MarginLayoutParams)?.setMargins(
                    pageSpacing.left, pageSpacing.top, pageSpacing.right, pageSpacing.bottom
                )
            }
        }

        private fun applyFadeInAnimation(view: View) {
            view.startAnimation(AlphaAnimation(0F, 1F).apply {
                interpolator = LinearInterpolator()
                duration = 300
            })
        }

        private fun handleLoadingForPage(position: Int) {
            itemBinding.pageLoadingLayout.pdfViewPageLoadingProgress.visibility =
                if (enableLoadingForPages && !renderer.pageExistInCache(position)) View.VISIBLE else View.GONE
        }
    }
}
