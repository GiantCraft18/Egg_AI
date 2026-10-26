package com.eggai.pikabuddy

import android.app.Activity
import android.os.Bundle
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.graphics.drawable.GradientDrawable
import kotlin.math.min

class MainActivity : Activity() {
    private lateinit var stats: TextView
    private lateinit var mood: TextView
    private lateinit var petView: PetView
    private lateinit var sparkText: TextView
    private var sparks = 0
    private var berries = 5
    private var energy = 80
    private var happiness = 70
    private var level = 1
    private var taps = 0
    private var petName = "Пика"
    private val prefs by lazy { getSharedPreferences("eggai_save", Context.MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadState()
        buildUi()
        updateUi()
    }

    private fun buildUi() {
        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(20), dp(16), dp(20), dp(18))
            background = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(Color.rgb(18, 23, 48), Color.rgb(38, 24, 70))
            )
        }

        val title = TextView(this).apply {
            text = "EggAI"
            textSize = 30f
            setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-black", Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        page.addView(title, matchWrap())

        val subtitle = TextView(this).apply {
            text = "Твой электрический питомец ⚡"
            textSize = 14f
            setTextColor(Color.rgb(190, 199, 255))
            gravity = Gravity.CENTER
            setPadding(0, dp(2), 0, dp(8))
        }
        page.addView(subtitle, matchWrap())

        val statCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(14), dp(10), dp(14), dp(10))
            background = rounded(Color.rgb(31, 38, 72), 18)
        }
        stats = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }
        sparkText = TextView(this).apply {
            textSize = 16f
            setTextColor(Color.rgb(255, 220, 94))
            typeface = Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, 0)
        }
        statCard.addView(stats, matchWrap())
        statCard.addView(sparkText, matchWrap())
        page.addView(statCard, LinearLayout.LayoutParams(-1, dp(76)))

        petView = PetView(this) {
            taps++
            sparks += 1 + (level / 3)
            happiness = (happiness + 2).coerceAtMost(100)
            energy = (energy - 1).coerceAtLeast(0)
            if (taps % 12 == 0) {
                level++
                Toast.makeText(this, "Уровень повышен! Теперь уровень $level 🎉", Toast.LENGTH_SHORT).show()
            }
            saveState()
            updateUi()
        }
        val petParams = LinearLayout.LayoutParams(-1, 0, 1f).apply {
            topMargin = dp(6)
            bottomMargin = dp(4)
        }
        page.addView(petView, petParams)

        mood = TextView(this).apply {
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, dp(2), 0, dp(10))
        }
        page.addView(mood, matchWrap())

        val row1 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row1.addView(actionButton("🍓 Покормить", Color.rgb(225, 76, 127)) {
            if (berries <= 0) {
                toast("Ягоды закончились. Собери искры и купи ещё!")
            } else if (happiness >= 100 && energy >= 100) {
                toast("Питомец уже сыт и полон энергии!")
            } else {
                berries--
                happiness = (happiness + 18).coerceAtMost(100)
                energy = (energy + 12).coerceAtMost(100)
                toast("Ням! Пика счастливее ❤️")
                saveState(); updateUi()
            }
        }, 1f)
        row1.addView(actionButton("⚡ Тренировка", Color.rgb(93, 91, 230)) {
            if (energy < 15) {
                toast("Питомец устал. Дай ему ягодку 🍓")
            } else {
                energy -= 15
                sparks += 8 + level
                happiness = (happiness + 5).coerceAtMost(100)
                toast("Тренировка завершена: +${8 + level} искр!")
                saveState(); updateUi()
            }
        }, 1f)
        page.addView(row1, LinearLayout.LayoutParams(-1, dp(52)).apply { bottomMargin = dp(8) })

        val row2 = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row2.addView(actionButton("🌙 Отдых", Color.rgb(54, 151, 166)) {
            energy = (energy + 30).coerceAtMost(100)
            happiness = (happiness + 3).coerceAtMost(100)
            toast("Пика отдохнул и восстановил силы.")
            saveState(); updateUi()
        }, 1f)
        row2.addView(actionButton("🛒 Купить ягоды", Color.rgb(187, 124, 48)) {
            if (sparks >= 10) {
                sparks -= 10
                berries += 3
                toast("Куплено 3 ягоды за 10 искр.")
                saveState(); updateUi()
            } else toast("Нужно 10 искр. Нажимай на питомца!")
        }, 1f)
        page.addView(row2, LinearLayout.LayoutParams(-1, dp(52)))

        val footer = TextView(this).apply {
            text = "Нажимай на питомца, собирай искры и повышай уровень"
            textSize = 11f
            setTextColor(Color.rgb(170, 178, 220))
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, 0)
        }
        page.addView(footer, matchWrap())

        setContentView(page)
    }

    private fun updateUi() {
        stats.text = "⭐ Уровень $level    ❤️ $happiness%    🔋 $energy%"
        sparkText.text = "⚡ Искры: $sparks       🍓 Ягоды: $berries"
        mood.text = when {
            happiness >= 80 -> "$petName счастлив! Продолжай в том же духе 💛"
            happiness >= 45 -> "$petName доволен. Поиграй с ним!"
            else -> "$petName грустит — пора покормить его 🍓"
        }
        petView.invalidate()
    }

    private fun saveState() {
        prefs.edit()
            .putInt("sparks", sparks).putInt("berries", berries)
            .putInt("energy", energy).putInt("happiness", happiness)
            .putInt("level", level).putInt("taps", taps).apply()
    }

    private fun loadState() {
        sparks = prefs.getInt("sparks", 0)
        berries = prefs.getInt("berries", 5)
        energy = prefs.getInt("energy", 80)
        happiness = prefs.getInt("happiness", 70)
        level = prefs.getInt("level", 1)
        taps = prefs.getInt("taps", 0)
    }

    private fun actionButton(label: String, color: Int, weight: Float, action: () -> Unit): Button {
        return Button(this).apply {
            text = label
            textSize = 12f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = rounded(color, 16)
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(0, -1, weight).apply {
                marginEnd = dp(5)
                marginStart = dp(5)
            }
        }
    }

    private fun rounded(color: Int, radiusDp: Int) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
    }

    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2)
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    override fun onPause() {
        super.onPause()
        if (::stats.isInitialized) saveState()
    }
}

private class PetView(context: Context, val onPetTap: () -> Unit) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var pulse = 0f

    init {
        setOnClickListener { onPetTap(); pulse = 1f; invalidate() }
        contentDescription = "Электрический жёлтый питомец. Нажми, чтобы получить искры."
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        val cx = w / 2f
        val cy = h * 0.53f
        val scale = min(w / 330f, h / 390f).coerceAtMost(1.1f)

        // Soft glowing backdrop
        paint.shader = RadialGradientCompat(cx, cy, 145f * scale)
        canvas.drawCircle(cx, cy, 145f * scale, paint)
        paint.shader = null

        // Ground shadow
        paint.color = Color.argb(70, 0, 0, 0)
        canvas.drawOval(RectF(cx - 88*scale, cy + 108*scale, cx + 88*scale, cy + 133*scale), paint)

        // Tail: lightning bolt
        paint.color = Color.rgb(255, 205, 57)
        val tail = android.graphics.Path().apply {
            moveTo(cx + 62*scale, cy + 26*scale)
            lineTo(cx + 108*scale, cy + 7*scale)
            lineTo(cx + 91*scale, cy + 36*scale)
            lineTo(cx + 119*scale, cy + 30*scale)
            lineTo(cx + 71*scale, cy + 78*scale)
            lineTo(cx + 82*scale, cy + 44*scale)
            lineTo(cx + 60*scale, cy + 51*scale)
            close()
        }
        canvas.drawPath(tail, paint)
        paint.color = Color.rgb(151, 94, 35)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 4f * scale
        canvas.drawPath(tail, paint)
        paint.style = Paint.Style.FILL

        // Ears
        paint.color = Color.rgb(255, 210, 59)
        val leftEar = android.graphics.Path().apply {
            moveTo(cx - 56*scale, cy - 62*scale)
            lineTo(cx - 78*scale, cy - 145*scale)
            lineTo(cx - 35*scale, cy - 130*scale)
            lineTo(cx - 17*scale, cy - 65*scale)
            close()
        }
        val rightEar = android.graphics.Path().apply {
            moveTo(cx + 36*scale, cy - 65*scale)
            lineTo(cx + 63*scale, cy - 140*scale)
            lineTo(cx + 82*scale, cy - 115*scale)
            lineTo(cx + 60*scale, cy - 51*scale)
            close()
        }
        canvas.drawPath(leftEar, paint)
        canvas.drawPath(rightEar, paint)
        paint.color = Color.rgb(67, 43, 47)
        canvas.drawOval(RectF(cx - 74*scale, cy - 145*scale, cx - 57*scale, cy - 112*scale), paint)
        canvas.drawOval(RectF(cx + 64*scale, cy - 139*scale, cx + 78*scale, cy - 111*scale), paint)

        // Body
        paint.color = Color.rgb(255, 218, 75)
        canvas.drawOval(RectF(cx - 72*scale, cy - 75*scale, cx + 72*scale, cy + 112*scale), paint)

        // Belly highlight
        paint.color = Color.rgb(255, 235, 135)
        canvas.drawOval(RectF(cx - 37*scale, cy + 7*scale, cx + 39*scale, cy + 89*scale), paint)

        // Arms
        paint.color = Color.rgb(255, 218, 75)
        canvas.drawOval(RectF(cx - 91*scale, cy - 5*scale, cx - 52*scale, cy + 38*scale), paint)
        canvas.drawOval(RectF(cx + 52*scale, cy - 5*scale, cx + 91*scale, cy + 38*scale), paint)

        // Feet
        paint.color = Color.rgb(214, 151, 49)
        canvas.drawOval(RectF(cx - 62*scale, cy + 88*scale, cx - 18*scale, cy + 115*scale), paint)
        canvas.drawOval(RectF(cx + 18*scale, cy + 88*scale, cx + 62*scale, cy + 115*scale), paint)

        // Cheeks
        paint.color = Color.rgb(247, 82, 91)
        canvas.drawCircle(cx - 49*scale, cy + 1*scale, 15*scale, paint)
        canvas.drawCircle(cx + 49*scale, cy + 1*scale, 15*scale, paint)

        // Eyes
        paint.color = Color.rgb(45, 39, 53)
        canvas.drawOval(RectF(cx - 35*scale, cy - 30*scale, cx - 23*scale, cy - 7*scale), paint)
        canvas.drawOval(RectF(cx + 23*scale, cy - 30*scale, cx + 35*scale, cy - 7*scale), paint)
        paint.color = Color.WHITE
        canvas.drawCircle(cx - 28*scale, cy - 24*scale, 3.5f*scale, paint)
        canvas.drawCircle(cx + 30*scale, cy - 24*scale, 3.5f*scale, paint)

        // Smile
        paint.color = Color.rgb(99, 58, 43)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f*scale
        paint.strokeCap = Paint.Cap.ROUND
        val smile = android.graphics.Path().apply {
            moveTo(cx - 12*scale, cy + 14*scale)
            quadTo(cx, cy + 26*scale, cx + 12*scale, cy + 14*scale)
        }
        canvas.drawPath(smile, paint)
        paint.style = Paint.Style.FILL

        // Sparkles
        paint.color = Color.rgb(255, 240, 128)
        drawSpark(canvas, cx - 110*scale, cy - 50*scale, 9*scale)
        drawSpark(canvas, cx + 112*scale, cy - 63*scale, 7*scale)
        drawSpark(canvas, cx - 96*scale, cy + 63*scale, 6*scale)

        paint.color = Color.WHITE
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
        paint.textSize = 15f * scale
        canvas.drawText("НАЖМИ НА МЕНЯ!", cx, h - 14f*scale, paint)
    }

    private fun drawSpark(canvas: Canvas, x: Float, y: Float, r: Float) {
        val p = android.graphics.Path().apply {
            moveTo(x, y-r); lineTo(x+r*0.25f, y-r*0.25f)
            lineTo(x+r, y); lineTo(x+r*0.25f, y+r*0.25f)
            lineTo(x, y+r); lineTo(x-r*0.25f, y+r*0.25f)
            lineTo(x-r, y); lineTo(x-r*0.25f, y-r*0.25f); close()
        }
        canvas.drawPath(p, paint)
    }

    private fun RadialGradientCompat(cx: Float, cy: Float, radius: Float): Shader {
        return android.graphics.RadialGradient(
            cx, cy, radius,
            intArrayOf(Color.argb(100, 110, 92, 255), Color.argb(0, 110, 92, 255)),
            null, Shader.TileMode.CLAMP
        )
    }
}
