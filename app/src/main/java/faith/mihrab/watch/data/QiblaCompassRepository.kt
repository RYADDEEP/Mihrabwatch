package faith.mihrab.watch.data

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

private const val SMOOTHING_ALPHA = 0.15f

class QiblaCompassRepository(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationVectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magnetometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    /**
     * True when this watch can determine its orientation at all — the rotation vector, or an
     * accelerometer and magnetometer to fuse. When false the screen must not draw an arrow: a
     * compass that cannot know must not point.
     */
    val hasOrientationSensor: Boolean =
        rotationVectorSensor != null || (accelerometer != null && magnetometer != null)

    /** Null until the first sample arrives — never a fabricated zero. */
    private val _heading = MutableStateFlow<Float?>(null)
    val heading: StateFlow<Float?> = _heading.asStateFlow()

    private val rotationMatrix = FloatArray(9)
    private val orientation = FloatArray(3)
    private val identityMatrix = FloatArray(9)

    private var gravity: FloatArray? = null
    private var geomagnetic: FloatArray? = null

    fun start() {
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_GAME)
        } else {
            accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
            magnetometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) }
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
        gravity = null
        geomagnetic = null
        _heading.value = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                SensorManager.getOrientation(rotationMatrix, orientation)
                publishHeading(orientation[0])
            }
            Sensor.TYPE_ACCELEROMETER -> {
                gravity = event.values.copyOf()
                tryFallbackHeading()
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                geomagnetic = event.values.copyOf()
                tryFallbackHeading()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun tryFallbackHeading() {
        val g = gravity ?: return
        val m = geomagnetic ?: return
        if (SensorManager.getRotationMatrix(rotationMatrix, identityMatrix, g, m)) {
            SensorManager.getOrientation(rotationMatrix, orientation)
            publishHeading(orientation[0])
        }
    }

    private fun publishHeading(azimuthRadians: Float) {
        val rawDeg = ((Math.toDegrees(azimuthRadians.toDouble()).toFloat() % 360f) + 360f) % 360f
        val previous = _heading.value
        _heading.value = if (previous == null) rawDeg else smoothCircular(previous, rawDeg, SMOOTHING_ALPHA)
    }

    // Smooth in sin/cos space so the 0°/360° wraparound doesn't cause the arrow to spin
    // the long way around when the heading crosses North.
    private fun smoothCircular(prev: Float, raw: Float, alpha: Float): Float {
        val prevRad = Math.toRadians(prev.toDouble())
        val rawRad = Math.toRadians(raw.toDouble())
        val sinS = (1 - alpha) * sin(prevRad) + alpha * sin(rawRad)
        val cosS = (1 - alpha) * cos(prevRad) + alpha * cos(rawRad)
        val deg = Math.toDegrees(atan2(sinS, cosS)).toFloat()
        return ((deg % 360f) + 360f) % 360f
    }
}
