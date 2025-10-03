package com.aviatorassist
enum class TipoVela { AZUL, ROXO, ROSA }
data class RegistroRodada(val id:Long=System.currentTimeMillis(),val tipoVela:TipoVela,val multiplicador:Double,val dataHora:Long=System.currentTimeMillis())
class ClassificadorVela{ fun classificarCorDoBitmap(R:Int,G:Int,B:Int):TipoVela?{ if(R>200 && G<150 && B>180) return TipoVela.ROSA; if(R>150 && G<100 && B>150) return TipoVela.ROXO; if(B>R && B>G && B<150) return TipoVela.AZUL; return null}}
