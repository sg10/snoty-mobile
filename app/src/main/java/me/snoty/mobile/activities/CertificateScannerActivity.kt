package me.snoty.mobile.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.widget.Toast
import com.google.zxing.BarcodeFormat
import me.snoty.mobile.Cryptography
import me.snoty.mobile.ServerPreferences
import me.snoty.mobile.server.connection.ConnectionHandler


/**
 * handles scanning the QR code and initalizes encrypting/saving
 * the server connection data
 */
class CertificateScannerActivity : AppCompatActivity(), ZXingScannerView.ResultHandler{

    private var TAG = "CertScan"

    private var mScannerView: ZXingScannerView? = null

    private val RESULT_DELIMITER = " "

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Programmatically initialize the scanner view
        val scannerView = ZXingScannerView(this)
        scannerView.setFormats(listOf(BarcodeFormat.QR_CODE))

        mScannerView = scannerView
        setContentView(mScannerView)

    }

    override fun handleResult(rawResult: Result?) {
        Log.d("TAG", rawResult?.text)
        Log.d("TAG", rawResult?.barcodeFormat.toString())

        if(rawResult?.text != null) {
            saveCertificateToPreferences(rawResult.text)
            this.finish()
        }
        else {
            mScannerView?.resumeCameraPreview(this)
        }
    }

    private fun saveCertificateToPreferences(resultText: String) {
        if(resultText.length < 10 || !resultText.contains(RESULT_DELIMITER)) {
            Toast.makeText(this, "Invalid Connection Data", Toast.LENGTH_LONG).show()
            Log.d(TAG, "Invalid Connection Data\n$resultText")
            return
        }

        val parts = resultText.split(RESULT_DELIMITER)
        if(parts.size != 3) {
            Toast.makeText(this, "Invalid Connection Data (parts)", Toast.LENGTH_LONG).show()
            Log.d(TAG, "Invalid Connection Data (parts)\n$resultText")
            return
        }

        var ip = parts[0]
        val fingerprint = parts[1]
        val fingerprintCleared = fingerprint.replace(":", "").toLowerCase()
        val secret = parts[2]
        val port = 0

        val saved = ServerPreferences.instance.saveServerConnectionData(ip, fingerprintCleared, secret, port)

        if(saved) {
            Log.d(TAG, "Saved Server Connection Details")
            ConnectionHandler.instance.updateServerPreferences(fingerprintCleared, secret)
        }
        else {
            Toast.makeText(this@CertificateScannerActivity, "Error Saving Server Connection Details", Toast.LENGTH_LONG).show()
            Log.e(TAG, "Error Saving Server Connection Details")
        }

    }

    private val MY_PERMISSIONS_REQUEST_CAMERA: Int = 42

    public override fun onResume() {
        requestCameraPermission()
        activateScanner()
        super.onResume()
    }

    private fun activateScanner() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            mScannerView?.setResultHandler(this)
            mScannerView?.startCamera()
        }
    }

    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.CAMERA)) {

                val mBuilder = AlertDialog.Builder(this)
                mBuilder.setTitle("Camera Permission")
                mBuilder.setMessage("In order to connect securely to the server, " +
                        "you need to scan the QR code displayed on your PC.\n\n" +
                        "Thus, please allow camera access to this app.")
                mBuilder.setCancelable(true)
                mBuilder.setNeutralButton("Got it!", { v, _ ->
                    v.dismiss()
                    ActivityCompat.requestPermissions(this,
                            arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)
                })
                mBuilder.show()
            }
            else {

                ActivityCompat.requestPermissions(this,
                        arrayOf(Manifest.permission.CAMERA), MY_PERMISSIONS_REQUEST_CAMERA)

            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty()){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                activateScanner()
            }else if (grantResults[0] == PackageManager.PERMISSION_DENIED){
                // nothing
            }
        }
    }

    public override fun onPause() {
        mScannerView?.stopCamera()
        super.onPause()
    }



}