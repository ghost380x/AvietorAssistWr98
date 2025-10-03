package com.aviatorassist
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.*
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import java.util.LinkedList
import kotlin.random.Random

class FloatingWidgetService : Service() {
private lateinit var windowManager: WindowManager
private lateinit var floatingView: View
private lateinit var tvAlertaIA: TextView
private lateinit var tvHistoricoRecente: TextView
private val historicoRodadas: LinkedList<RegistroRodada> = LinkedList()
private val MAX_HISTORICO_ANALIZE = 20 
private var limiteAzuis: Int = 3
private var mediaProjectionIntent: Intent? = null
private var mediaProjection: MediaProjection? = null
private lateinit var mediaProjectionManager: MediaProjectionManager

override fun onBind(intent: Intent?): IBinder? = null
override fun onCreate(){ super.onCreate()
windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
floatingView = LayoutInflater.from(this).inflate(R.layout.layout_floating_widget, null)
val params = WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,PixelFormat.TRANSLUCENT)
params.gravity = Gravity.TOP or Gravity.START; params.x = 0; params.y = 100 
windowManager.addView(floatingView, params)
tvAlertaIA = floatingView.findViewById(R.id.tv_alerta_ia)
tvHistoricoRecente = floatingView.findViewById(R.id.tv_historico_recente)
floatingView.findViewById<Button>(R.id.btn_capturar_vela).setOnClickListener{ iniciarAnaliseDeTela() }
}
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
if (intent != null) {
mediaProjectionIntent = intent.getParcelableExtra("MediaProjectionIntent")
limiteAzuis = intent.getIntExtra("LIMIT_AZUIS", 3)
mediaProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
if (mediaProjectionIntent != null) {
mediaProjection = mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, mediaProjectionIntent!!)
Toast.makeText(this, "Analisador pronto para SCAN.", Toast.LENGTH_SHORT).show()
} else { Toast.makeText(this, "Erro: Permissão de Captura não recebida.", Toast.LENGTH_LONG).show() } }
return START_STICKY
}

private fun iniciarAnaliseDeTela() {
if (mediaProjection == null) { tvAlertaIA.text = "Erro! MediaProjection não iniciado."; return }
val tipoVelaCapturada = gerarVelaSimulada()
if (tipoVelaCapturada != null) {
val novoRegistro = RegistroRodada(tipoVela = tipoVelaCapturada, multiplicador = obterMultiplicadorSimulado(tipoVelaCapturada))
historicoRodadas.add(novoRegistro)
if (historicoRodadas.size > MAX_HISTORICO_ANALIZE) historicoRodadas.removeFirst()
val sequenciaRecente = historicoRodadas.map { it.tipoVela }
val analisador = AnalisadorPadroes(historicoRodadas, limiteAzuis)
val alertaRisco = analisador.verificarRiscoAtual(sequenciaRecente)
val alertaOportunidade = analisador.analisarOportunidade(sequenciaRecente)
atualizarUI(alertaRisco, alertaOportunidade)
} else { tvAlertaIA.text = "SCAN: Nenhuma vela identificada. Tente novamente."; tvAlertaIA.setTextColor(Color.YELLOW) }
}

private fun atualizarUI(risco: String, oportunidade: String) {
val sequenciaStr = historicoRodadas.map { it.tipoVela.name.first() }.joinToString("-")
tvHistoricoRecente.text = "Sequência: $sequenciaStr (${historicoRodadas.size}/${MAX_HISTORICO_ANALIZE})"
val alertaPrincipal: String
if (risco.contains("RISCO ALTO") || risco.contains("ATENÇÃO")) { tvAlertaIA.setTextColor(Color.RED); alertaPrincipal = risco 
} else { tvAlertaIA.setTextColor(Color.GREEN); alertaPrincipal = oportunidade }
tvAlertaIA.text = alertaPrincipal
}

private fun gerarVelaSimulada(): TipoVela {
    val cores = listOf(TipoVela.AZUL, TipoVela.AZUL, TipoVela.ROXO, TipoVela.ROSA)
    return cores[Random.nextInt(cores.size)] 
}
private fun obterMultiplicadorSimulado(tipo: TipoVela): Double {
    return when(tipo) { TipoVela.AZUL -> 1.50; TipoVela.ROXO -> 5.00; TipoVela.ROSA -> 15.00 }
}
override fun onDestroy(){ super.onDestroy(); if(::floatingView.isInitialized) windowManager.removeView(floatingView)
mediaProjection?.stop()
}
}
