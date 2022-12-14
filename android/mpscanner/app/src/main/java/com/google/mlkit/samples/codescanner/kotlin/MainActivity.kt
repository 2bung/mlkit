package com.google.mlkit.samples.codescanner.kotlin
import com.rabbitmq.client.*
import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.common.MlKitException
import com.google.mlkit.samples.codescanner.R
import com.google.mlkit.vision.barcode.common.Barcode
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.*

private const val USERNAME = "admin"
private const val PASSWORD = "MaxLop2015"
private const val QUEUE = "test"

class MainActivity : AppCompatActivity() {

  private var allowManualInput = false
  private var barcodeResultView: TextView? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    barcodeResultView = findViewById(R.id.barcode_result_view)
  }

  fun onAllowManualInputCheckboxClicked(view: View) {
    allowManualInput = (view as CheckBox).isChecked
  }

  fun apiSender() {
    val url = URL("https://api.tashir.solvintech.ru/api/scanner")
    val postData = "foo1=bar1&foo2=bar2"

    val conn = url.openConnection()
    conn.doOutput = true
    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
    conn.setRequestProperty("Content-Length", postData.length.toString())

    DataOutputStream(conn.getOutputStream()).use { it.writeBytes(postData) }
    BufferedReader(InputStreamReader(conn.getInputStream())).use { bf ->
      var line: String?
      while (bf.readLine().also { line = it } != null) {
        println(line)
      }
    }
  }

  fun onScanButtonClicked(view: View) {
    val thread = Thread {
      try {
        val factory = ConnectionFactory()
          .apply {
            username = "admin"
            password = "MaxLop2015"
            host = "62.113.111.16"
            virtualHost = ConnectionFactory.DEFAULT_VHOST
            port = ConnectionFactory.DEFAULT_AMQP_PORT
          }

        val channel = factory
          .newConnection()
          .createChannel()

        channel.basicConsume(QUEUE, true, object : Consumer {
          override fun handleConsumeOk(consumerTag: String?) {
            consumerTag?.let {
              val url = URL("https://api.tashir.solvintech.ru/api/scanner")
              val postData = consumerTag

              val conn = url.openConnection()
              conn.doOutput = true
              conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
              conn.setRequestProperty("Content-Length", postData.length.toString())

              DataOutputStream(conn.getOutputStream()).use { it.writeBytes(postData) }
              BufferedReader(InputStreamReader(conn.getInputStream())).use { bf ->
                var line: String?
                while (bf.readLine().also { line = it } != null) {
                  println(line)
                }
              }
            }
          }

          override fun handleCancelOk(consumerTag: String?) {
            //Perform cancellation tasks such as closing resources here
          }

          override fun handleCancel(consumerTag: String?) {
            //Perform cancellation tasks such as closing resources here
          }

          override fun handleShutdownSignal(consumerTag: String?, sig: ShutdownSignalException?) {
            sig?.let {
              throw it
            }
          }

          override fun handleRecoverOk(consumerTag: String?) {
            // If connection issues, try to receive messages again
          }

          override fun handleDelivery(
            consumerTag: String?,
            envelope: Envelope?,
            properties: AMQP.BasicProperties?,
            body: ByteArray?
          ) {
            body?.let {
              val url = URL("https://api.tashir.solvintech.ru/api/scanner")
              val postData = body.toString()

              val conn = url.openConnection()
              conn.doOutput = true
              conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
              conn.setRequestProperty("Content-Length", postData.length.toString())

              DataOutputStream(conn.getOutputStream()).use { it.writeBytes(postData) }
              BufferedReader(InputStreamReader(conn.getInputStream())).use { bf ->
                var line: String?
                while (bf.readLine().also { line = it } != null) {
                  println(line)
                }
              }
            }
          }

        })
        } catch (e: java.lang.Exception) {
        e.printStackTrace()
      }
      catch (e: java.lang.Exception) {
        println(e)
      }
    }

    thread.start()
//    val optionsBuilder = GmsBarcodeScannerOptions.Builder()
//    if (allowManualInput) {
//      optionsBuilder.allowManualInput()
//    }
//    val gmsBarcodeScanner = GmsBarcodeScanning.getClient(this, optionsBuilder.build())
//    gmsBarcodeScanner
//      .startScan()
//      .addOnSuccessListener { barcode: Barcode ->
//        barcodeResultView!!.text = apiSender(barcode)
//      }
//      .addOnFailureListener { e: Exception -> barcodeResultView!!.text = getErrorMessage(e) }
//      .addOnCanceledListener {
//        barcodeResultView!!.text = getString(R.string.error_scanner_cancelled)
//      }
  }

  override fun onSaveInstanceState(savedInstanceState: Bundle) {
    savedInstanceState.putBoolean(KEY_ALLOW_MANUAL_INPUT, allowManualInput)
    super.onSaveInstanceState(savedInstanceState)
  }

  override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    allowManualInput = savedInstanceState.getBoolean(KEY_ALLOW_MANUAL_INPUT)
  }

  private fun getSuccessfulMessage(barcode: Barcode): String {
    val barcodeValue =
      String.format(
        Locale.US,
        "Display Value: %s\nRaw Value: %s\nFormat: %s\nValue Type: %s",
        barcode.displayValue,
        barcode.rawValue,
        barcode.format,
        barcode.valueType
      )
    return getString(R.string.barcode_result, barcodeValue)
  }

  private fun getErrorMessage(e: Exception): String? {
    return if (e is MlKitException) {
      when (e.errorCode) {
        MlKitException.CODE_SCANNER_CAMERA_PERMISSION_NOT_GRANTED ->
          getString(R.string.error_camera_permission_not_granted)
        MlKitException.CODE_SCANNER_APP_NAME_UNAVAILABLE ->
          getString(R.string.error_app_name_unavailable)
        else -> getString(R.string.error_default_message, e)
      }
    } else {
      e.message
    }
  }

  companion object {
    private const val KEY_ALLOW_MANUAL_INPUT = "allow_manual_input"
  }
}
