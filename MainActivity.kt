package com.aviatorassist
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
class MainActivity : Activity() {
private val OVERLAY_PERMISSION_CODE=100
private val MEDIA_PROJECTION_REQUEST_CODE=101
private lateinit var mediaProjectionManager: MediaProjectionManager
override fun onCreate(savedInstanceState: Bundle?){ super.onCreate(savedInstanceState); setContentView(R.layout.activity_main)
mediaProjectionManager=getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
findViewById<Button>(R.id.btn_iniciar_analisador).setOnClickListener{ verificarEIniciarServico() } }
private fun verificarEIniciarServico(){ if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)){ Toast.makeText(this,"Permissão de Sobreposição necessária!",Toast.LENGTH_LONG).show()
val intent=Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")); startActivityForResult(intent,OVERLAY_PERMISSION_CODE); return }
startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(),MEDIA_PROJECTION_REQUEST_CODE) }
override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){ 
    super.onActivityResult(requestCode,resultCode,data)
    when(requestCode){
        OVERLAY_PERMISSION_CODE -> {
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && Settings.canDrawOverlays(this)){
                verificarEIniciarServico()
            } else {
                Toast.makeText(this,"Sobreposição negada. Analisador não pode funcionar.",Toast.LENGTH_LONG).show()
            }
        }
        MEDIA_PROJECTION_REQUEST_CODE -> {
            if(resultCode==RESULT_OK){
                iniciarFloatingService(data)
            } else {
                Toast.makeText(this,"Captura de Tela negada. Análise por imagem não é possível.",Toast.LENGTH_LONG).show()
            }
        }
    }
}
private fun iniciarFloatingService(mediaProjectionIntent:Intent?){
    val intent=Intent(this,FloatingWidgetService::class.java)
    intent.putExtra("MediaProjectionIntent",mediaProjectionIntent)
    intent.putExtra("LIMIT_AZUIS",3) 
    startService(intent)
    finish()
}
}
