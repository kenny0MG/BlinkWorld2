package org.robotics.blinkworld.Utils

import android.animation.ValueAnimator
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.SweepGradient
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.graphics.ColorUtils
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import org.robotics.blinkworld.R
import org.robotics.blinkworld.models.Chat
import org.robotics.blinkworld.models.Message
import org.robotics.blinkworld.models.User

class Utils {
    class ValueEventListenerAdapter(val handler: (DataSnapshot) -> Unit) : ValueEventListener {
        private val TAG = "ValueEventListenerAdapt"

        override fun onDataChange(data: DataSnapshot) {
            handler(data)
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e(TAG, "onCancelled: ", error.toException())
        }
    }








    }
fun ImageView.fromUrl(url: String?) {
    Glide.with(this)
        .load(url)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .skipMemoryCache(false)
        .encodeQuality(100)
        .format(DecodeFormat.PREFER_ARGB_8888)
        .placeholder(R.drawable.empty_box).centerCrop()
        .into(this)
}
fun ImageView.loadUserPhoto(photoUrl: String?) =
    Glide.with(this).load(photoUrl)
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .skipMemoryCache(false)
        .encodeQuality(100)
        .format(DecodeFormat.PREFER_ARGB_8888)
         .centerCrop()
        .into(this)



//тень градиентная с анимацией
 fun createShadowDrawable(
    @ColorInt colors: IntArray,
    cornerRadius: Float,
    elevation: Float,
    centerX: Float,
    centerY: Float
): ShapeDrawable {

    val shadowDrawable = ShapeDrawable()

    // Устанавливаем черную тень по умолчанию
    shadowDrawable.paint.setShadowLayer(
        elevation, // размер тени
        0f, // смещение тени по оси Х
        0f, // по У
        Color.BLACK // цвет тени
    )

    /**
     * Применяем покраску градиентом
     *
     * @param centerX - Центр SweepGradient по оси Х. Берем центр вьюхи
     * @param centerY - Центр по оси У
     * @param colors - Цвета градиента. Последний цвет должен быть равен первому,
     * иначе между ними не будет плавного перехода
     * @param position - позиции смещения градиента одного цвета относительно другого от 0 до 1.
     * В нашем случае null т.к. нам нужен равномерный градиент
     */
    shadowDrawable.paint.shader = SweepGradient(
        centerX,
        centerY,
        colors,
        null
    )

    // Делаем закугление углов
    val outerRadius = FloatArray(8) { cornerRadius }
    shadowDrawable.shape = RoundRectShape(outerRadius, null, null)

    return shadowDrawable
}


 fun createColorDrawable(
    @ColorInt backgroundColor: Int,
    cornerRadius: Float
) = GradientDrawable().apply {
    setColor(backgroundColor)
    setCornerRadius(cornerRadius)
}

 fun View.setColorShadowBackground(
    shadowDrawable: ShapeDrawable,
    colorDrawable: Drawable,
    padding: Int
) {
    val drawable = LayerDrawable(arrayOf(shadowDrawable, colorDrawable))
    drawable.setLayerInset(0, padding, padding, padding, padding)
    drawable.setLayerInset(1, padding, padding, padding, padding)
    setPadding(padding, padding, padding, padding)
    background = drawable
}

 fun animateShadow(
    shapeDrawable: ShapeDrawable,
    @ColorInt startColors: IntArray,
    @ColorInt endColors: IntArray,
    duration: Long,
    centerX: Float,
    centerY: Float
) {
    /**
     * Меняем значение с 0f до 1f для применения плавного изменения
     * цвета с помощью [ColorUtils.blendARGB]
     */
    ValueAnimator.ofFloat(0f, 1f).apply {
        // Задержка перерисовки тени. Грубо говоря, фпс анимации
        val invalidateDelay = 100
        var deltaTime = System.currentTimeMillis()

        // Новый массив со смешанными цветами
        val mixedColors = IntArray(startColors.size)

        addUpdateListener { animation ->
            if (System.currentTimeMillis() - deltaTime > invalidateDelay) {
                val animatedFraction = animation.animatedValue as Float
                deltaTime = System.currentTimeMillis()

                // Смешиваем цвета
                for (i in 0..mixedColors.lastIndex) {
                    mixedColors[i] = ColorUtils.blendARGB(startColors[i], endColors[i], animatedFraction)
                }

                // Устанавливаем новую тень
                shapeDrawable.paint.shader = SweepGradient(
                    centerX,
                    centerY,
                    mixedColors,
                    null
                )
                shapeDrawable.invalidateSelf()
            }
        }
        repeatMode = ValueAnimator.REVERSE
        repeatCount = Animation.INFINITE
        setDuration(duration)
        start()
    }




}


//переключение кнопки в зависимости подписан или нет
fun checkButton(uid: String,button: AppCompatButton,textView: TextView) {
    database.child(NODE_USERS).child(currentUid()!!).child("friends").child(uid).get()
        .addOnSuccessListener {
            if (it.exists()) {
                button.visibility = View.GONE
                textView.visibility = View.VISIBLE


            } else {
                database.child(NODE_USERS).child(currentUid()!!).child(NODE_FOLLOWING).child(uid).get()
                    .addOnSuccessListener {
                        if (it.exists()) {
                            button.visibility = View.GONE
                            textView.text = "You follow"
                            textView.visibility = View.VISIBLE
                        }
                        else{
                            button.visibility = View.VISIBLE
                            textView.visibility = View.GONE
                        }
                    }



            }
        }
}


fun follow(uid: String){

    database.child("user").child(currentUid()!!).child("following").child(uid).setValue(true)
    database.child("user").child(uid).child("followers").child(currentUid()!!).setValue(true)


}


fun DataSnapshot.getUserModel(): User =
    getValue(User::class.java) ?: User()
fun DataSnapshot.getMessageModel(): Message =
    getValue(Message::class.java) ?: Message()
fun DataSnapshot.getChatModel(): Chat =
    getValue(Chat::class.java) ?: Chat()


fun setFollow(uid: String, follow: Boolean, onSuccess: () -> Unit) {
    val followsTask = database.child(NODE_USERS).child(currentUid()!!).child("following")
        .child(uid).setValue(follow)
    val followersTask = database.child(NODE_USERS).child(uid).child("followers")
        .child(currentUid()!!).setValue(follow)




    Tasks.whenAll(followsTask, followersTask).addOnCompleteListener {
        if (it.isSuccessful) {
            onSuccess()
        } else {
        }
    }


}



class CustomDialog(context: Context) : Dialog(context, R.style.CustomDialogTheme) {
    init {
        // Set Semi-Transparent Color for Dialog Background
        window?.decorView?.rootView?.setBackgroundResource(R.color.loadBack)
        window?.decorView?.setOnApplyWindowInsetsListener { _, insets ->
            insets.consumeSystemWindowInsets()
        }
    }
}
