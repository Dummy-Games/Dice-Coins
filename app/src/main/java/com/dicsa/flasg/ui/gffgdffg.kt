package com.dicsa.flasg.ui

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.dicsa.flasg.R
import com.dicsa.flasg.util.getDrawableId
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.random.nextInt

class gffgdffg : Fragment(R.layout.hfdfgfdgfdgdfgfdgfg) {

    private var gdffdgfdd: Job? = null
    private var hgfdgfgfdg = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rhdghddfgd(view)
        view.findViewById<Button>(R.id.button).setOnClickListener { rhdghddfgd(view) }
    }

    private fun rhdghddfgd(view: View) {
        gdffdgfdd?.cancel()
        with(view) {
            val ivContainer = findViewById<FrameLayout>(R.id.flImages)
            gdffdgfdd = viewLifecycleOwner.lifecycleScope.launch {
                var angle = 0
                val images = listOf<ImageView>(
                    findViewById(R.id.iv1),
                    findViewById(R.id.iv2),
                    findViewById(R.id.iv3),
                    findViewById(R.id.iv4)
                )
                images.forEach(::gfdgfdhdfhfd)
                hfdfhdhd(images, ivContainer.width / 4, ivContainer.width / 2, ivContainer.height / 2)
                val score = images.map { it.isVisible }.map { if (it) 100 else 0 }
                    .reduce { acc, i -> acc + i }.toString()
                view.findViewById<TextView>(R.id.tvScore).text = "SCORE: $score"
                Toast.makeText(requireContext(), "You won $score points!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun gfdgfdhdfhfd(hgghghf: ImageView) {
        hgghghf.isVisible = true
        hgghghf.setImageResource(requireContext().getDrawableId("s" + Random.nextInt(1..4)))
    }

    private suspend fun hfdfhdhd(
        ivs: List<ImageView>,
        radius: Int,
        startX: Int,
        startY: Int
    ) {
        val sAngles = listOf(0.0, Math.PI / 2, Math.PI, Math.PI * 1.5)
        for (angle in 0..628) {
            ivs.forEachIndexed { index, iv ->
                dsfgsgffgfdgdf(
                    iv,
                    sAngles[index].toFloat() + (angle / 100f),
                    radius,
                    startX,
                    startY
                )
            }
            delay(5)
        }
        ivs.forEach { it.isVisible = Random.nextBoolean() }
    }

    private fun dsfgsgffgfdgdf(iv: ImageView, angle: Float, radius: Int, startX: Int, startY: Int) {
        iv.x = gfdgdfdgfdhf(angle, radius) + startX
        iv.y = gfgfdgfdgfd(angle, radius) + startY
    }

    private fun gfdgdfdgfdhf(angle: Float, radius: Int) = sin(angle) * radius

    private fun gfgfdgfdgfd(angle: Float, radius: Int) = cos(angle) * radius
}
